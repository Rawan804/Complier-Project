package codegenerate;

import AST.webast.WebASTNode;
import LexerandParser.webantlr.WebLexer;
import LexerandParser.webantlr.WebParser;
import SymbolTable.webSymboltable.WebSymbolTable;
import codegenerate.GenerateCode.AstJsonSerializer;
import codegenerate.GenerateCode.RenderTemplateCallFinder;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import semantic.SemanticError;
import semantic.web.WebSemanticAnalyzer;
import visitor.webvisitor.WebCompilerVisitor;

import java.nio.file.*;
import java.util.*;

/**
 * مرحلة توليد Jinja → HTML:
 * Parser → Jinja AST → Semantic → WebCodeGenerator → output/*.html
 *
 * يستلم من Python مباشرة:
 *   - contextData  : Map&lt;String,Object&gt; (مثل products = [...])
 *   - renderCalls  : أي قالب وبأي متغيرات من render_template(...)
 * بدون مكتبة JSON خارجية.
 */
public class JinjaGenerator {

    public static class Result {
        public final int passed;
        public final int failed;
        public final List<Map<String, Object>> astEntries;

        public Result(int passed, int failed, List<Map<String, Object>> astEntries) {
            this.passed = passed;
            this.failed = failed;
            this.astEntries = astEntries;
        }
    }

    public static Result generateAll(
            Path templatesDir,
            Path outputDir,
            Path compilerOutputDir,
            Map<String, Object> contextData,
            List<RenderTemplateCallFinder.RenderTemplateCall> renderCalls,
            StringBuilder generationLog,
            StringBuilder semanticReport
    ) throws Exception {
        List<RouteInfo> routes = RouteLoader.loadRoutes(renderCalls, contextData, templatesDir);

        Map<String, String> routeFileMap = new HashMap<>();
        for (RouteInfo r : routes) {
            routeFileMap.put(r.routeName, r.outputFileName);
        }

        List<?> productsList = asList(contextData.get("products"));

        List<Map<String, Object>> jinjaAstEntries = new ArrayList<>();
        int passed = 0;
        int failed = 0;

        for (RouteInfo route : routes) {
            if ("details".equals(route.routeName)) {
                for (Object item : productsList) {
                    if (!(item instanceof Map<?, ?> productMap)) continue;

                    Object id = productMap.get("id");
                    String outputName = "details_" + id + ".html";

                    @SuppressWarnings("unchecked")
                    Map<String, Object> productValues = new HashMap<>();
                    productValues.put("product", item);

                    RouteInfo perItem = new RouteInfo(
                            route.routeName,
                            route.templateFile,
                            outputName,
                            route.contextVars,
                            productValues
                    );

                    if (compileOne(templatesDir, outputDir, perItem, routeFileMap,
                            jinjaAstEntries, generationLog, semanticReport)) {
                        passed++;
                        System.out.println("OK: " + outputName);
                    } else {
                        failed++;
                    }
                }
                continue;
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("Template: " + route.templateFile + "  (route: " + route.routeName + ")");
            System.out.println("=".repeat(60));

            if (compileOne(templatesDir, outputDir, route, routeFileMap,
                    jinjaAstEntries, generationLog, semanticReport)) {
                passed++;
                System.out.println("OK: " + route.outputFileName);
            } else {
                failed++;
            }
        }

        String jinjaAstJson = AstJsonSerializer.prettyPrint(AstJsonSerializer.toJson(jinjaAstEntries));
        Files.writeString(compilerOutputDir.resolve("ast_jinja.json"), jinjaAstJson);
        generationLog.append("OK: ast_jinja.json saved (")
                .append(jinjaAstEntries.size())
                .append(" template AST(s)).\n");

        return new Result(passed, failed, jinjaAstEntries);
    }

    private static List<?> asList(Object value) {
        if (value instanceof List) return (List<?>) value;
        return List.of();
    }

    private static boolean compileOne(
            Path templatesDir,
            Path outputDir,
            RouteInfo route,
            Map<String, String> routeFileMap,
            List<Map<String, Object>> jinjaAstEntries,
            StringBuilder generationLog,
            StringBuilder semanticReport
    ) throws Exception {
        Path templatePath = templatesDir.resolve(route.templateFile);

        if (!Files.exists(templatePath)) {
            String msg = "Template not found: " + templatePath;
            System.err.println(msg);
            generationLog.append("FAIL: ").append(msg).append("\n");
            return false;
        }

        String source = Files.readString(templatePath);

        WebLexer lexer = new WebLexer(CharStreams.fromString(source));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        WebParser parser = new WebParser(tokens);

        try {
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg,
                                        RecognitionException e) {
                    throw new RuntimeException(
                            "Syntax error at line " + line + ":" + charPositionInLine + " - " + msg
                    );
                }
            });

            ParseTree tree = parser.html();
            WebSymbolTable symbolTable = new WebSymbolTable();

            for (Map.Entry<String, String> entry : route.contextVars.entrySet()) {
                symbolTable.defineContextVar(entry.getKey(), entry.getValue());
            }

            WebCompilerVisitor visitor = new WebCompilerVisitor(symbolTable);
            WebASTNode ast = visitor.visit(tree);

            Map<String, Object> astEntry = new LinkedHashMap<>();
            astEntry.put("template", route.templateFile);
            astEntry.put("output", route.outputFileName);
            astEntry.put("route", route.routeName);
            astEntry.put("ast", ast);
            jinjaAstEntries.add(astEntry);

            WebSemanticAnalyzer semantic = new WebSemanticAnalyzer(symbolTable);
            semantic.analyze(ast);

            if (semantic.getErrors().isEmpty()) {
                semanticReport.append("[")
                        .append(route.templateFile)
                        .append("] No Jinja semantic errors.\n");
            } else {
                for (SemanticError err : semantic.getErrors()) {
                    semanticReport.append("[")
                            .append(route.templateFile)
                            .append("] ")
                            .append(err)
                            .append("\n");
                }
                generationLog.append("FAIL: Semantic errors in ")
                        .append(route.templateFile)
                        .append("\n");
                return false;
            }

            WebCodeGenerator generator = new WebCodeGenerator();
            String generated = generator.generate(ast, route.contextValues, routeFileMap);

            Path outFile = outputDir.resolve(route.outputFileName);
            Files.createDirectories(outFile.getParent());
            Files.writeString(outFile, generated);

            generationLog.append("OK: Generated ")
                    .append(route.outputFileName)
                    .append(" from ")
                    .append(route.templateFile)
                    .append("\n");
            return true;
        } catch (RuntimeException ex) {
            System.err.println("FAIL: " + route.templateFile + " — " + ex.getMessage());
            generationLog.append("FAIL: ").append(route.templateFile)
                    .append(" — ").append(ex.getMessage()).append("\n");
            semanticReport.append("[").append(route.templateFile).append("] ")
                    .append(ex.getMessage()).append("\n");
            return false;
        }
    }

    public static class RouteInfo {
        public final String routeName;
        public final String templateFile;
        public final String outputFileName;
        public final Map<String, String> contextVars;
        public final Map<String, Object> contextValues;

        public RouteInfo(
                String routeName,
                String templateFile,
                String outputFileName,
                Map<String, String> contextVars,
                Map<String, Object> contextValues
        ) {
            this.routeName = routeName;
            this.templateFile = templateFile;
            this.outputFileName = outputFileName;
            this.contextVars = contextVars;
            this.contextValues = contextValues;
        }
    }
}
