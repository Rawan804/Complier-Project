package compiler;

import AST.Python_AST.ProgramNode;
import LexerandParser.puthon_antlr.python_lexer;
import LexerandParser.puthon_antlr.python_parser;
import codegenerate.GenerateCode.AstJsonSerializer;
import codegenerate.GenerateCode.PythonContextExtractor;
import codegenerate.GenerateCode.RenderTemplateCallFinder;
import codegenerate.JinjaGenerator;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import semantic.python_semantic.SemanticAnalyzer;
import visitor.python_visitor.PythonASTVisitor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompilerMain {

    public static class PythonPhaseResult {
        public final ProgramNode ast;
        public final List<semantic.python_semantic.SemanticError> semanticErrors;
        public final Map<String, Object> contextData;
        public final List<RenderTemplateCallFinder.RenderTemplateCall> renderCalls;

        public PythonPhaseResult(
                ProgramNode ast,
                List<semantic.python_semantic.SemanticError> semanticErrors,
                Map<String, Object> contextData,
                List<RenderTemplateCallFinder.RenderTemplateCall> renderCalls
        ) {
            this.ast = ast;
            this.semanticErrors = semanticErrors;
            this.contextData = contextData;
            this.renderCalls = renderCalls;
        }

        public boolean hasSemanticErrors() {
            return semanticErrors != null && !semanticErrors.isEmpty();
        }
    }

    public static int run(Path projectRoot) throws Exception {
        Path appPy = projectRoot.resolve("app.py");
        Path templatesDir = projectRoot.resolve("templates");
        Path outputDir = projectRoot.resolve("output");
        Path compilerOutputDir = projectRoot.resolve("compiler_output");

        Files.createDirectories(outputDir);
        Files.createDirectories(compilerOutputDir);
        Files.createDirectories(outputDir.resolve("static"));

        StringBuilder generationLog = new StringBuilder();
        StringBuilder semanticReport = new StringBuilder();

        generationLog.append("===== GENERATION LOG =====\n\n");
        semanticReport.append("===== SEMANTIC REPORT =====\n\n");

        System.out.println("--- Phase 1: Python (app.py) ---");
        generationLog.append("--- Phase 1: Python (app.py) ---\n");
        semanticReport.append("--- Python Semantic Analysis ---\n");

        if (!Files.exists(appPy)) {
            System.err.println("ERROR: app.py not found at " + appPy);
            generationLog.append("FAIL: app.py not found.\n");
            writeReports(compilerOutputDir, generationLog, semanticReport);
            return 1;
        }

        PythonPhaseResult pythonResult = runPythonPhase(appPy, compilerOutputDir, generationLog, semanticReport);

        int passed = 0;
        int failed = 0;

        if (pythonResult.hasSemanticErrors()) {
            generationLog.append("\nSKIP: Jinja phase skipped due to Python semantic errors.\n");
            System.out.println("SKIP Jinja: Python semantic errors.");
            failed++;
        } else {
            System.out.println("\n--- Phase 2: Jinja (templates/*.jinja) ---");
            generationLog.append("\n--- Phase 2: Jinja (templates/*.jinja) ---\n");
            generationLog.append("BRIDGE: Context Map passed from Python to Jinja in memory.\n");
            semanticReport.append("\n--- Jinja Semantic Analysis ---\n");

            JinjaGenerator.Result jinjaResult = JinjaGenerator.generateAll(
                    templatesDir,
                    outputDir,
                    compilerOutputDir,
                    pythonResult.contextData,
                    pythonResult.renderCalls,
                    generationLog,
                    semanticReport
            );
            passed = jinjaResult.passed;
            failed = jinjaResult.failed;
        }

        System.out.println("\n--- Phase 3: Copy supporting files ---");
        generationLog.append("\n--- Phase 3: Copy supporting files ---\n");
        copySupportingFiles(projectRoot, outputDir, generationLog);

        writeReports(compilerOutputDir, generationLog, semanticReport);

        System.out.println("\n=== DONE ===");
        System.out.println("HTML passed: " + passed + " | failed: " + failed);

        return failed > 0 ? 1 : 0;
    }

    private static PythonPhaseResult runPythonPhase(
            Path appPy,
            Path compilerOutputDir,
            StringBuilder generationLog,
            StringBuilder semanticReport
    ) throws Exception {
        generationLog.append("Input: ").append(appPy).append("\n");

        try (InputStream is = Files.newInputStream(appPy)) {
            CharStream input = CharStreams.fromStream(is);
            python_lexer lexer = new python_lexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            python_parser parser = new python_parser(tokens);
            ParseTree tree = parser.prog();
            generationLog.append("OK: Lexing/Parsing completed.\n");

            PythonASTVisitor visitor = new PythonASTVisitor();
            ProgramNode ast = (ProgramNode) visitor.visit(tree);
            generationLog.append("OK: Python AST built.\n");

            String astJson = AstJsonSerializer.prettyPrint(AstJsonSerializer.toJson(ast));
            Files.writeString(compilerOutputDir.resolve("ast_python.json"), astJson);
            generationLog.append("OK: ast_python.json saved.\n");

            SemanticAnalyzer analyzer = new SemanticAnalyzer(visitor.getSymbolTable());
            analyzer.analyze(ast);

            if (analyzer.getErrors().isEmpty()) {
                semanticReport.append("No Python semantic errors found.\n");
                generationLog.append("OK: No Python semantic errors.\n");
            } else {
                for (semantic.python_semantic.SemanticError err : analyzer.getErrors()) {
                    semanticReport.append("[Python] ").append(err).append("\n");
                }
                generationLog.append("FAIL: ")
                        .append(analyzer.getErrors().size())
                        .append(" Python semantic error(s).\n");
            }

            Map<String, Object> contextData = new LinkedHashMap<>();
            List<RenderTemplateCallFinder.RenderTemplateCall> renderCalls = new ArrayList<>();

            if (analyzer.getErrors().isEmpty()) {
                PythonContextExtractor extractor = new PythonContextExtractor();
                contextData = filterDynamic(extractor.extract(ast), generationLog);

                generationLog.append("\n--- Context Data extraction ---\n");
                for (String line : extractor.getLog()) {
                    generationLog.append(line).append("\n");
                }

                RenderTemplateCallFinder finder = new RenderTemplateCallFinder();
                renderCalls = finder.find(ast);

                generationLog.append("\n--- render_template calls ---\n");
                for (RenderTemplateCallFinder.RenderTemplateCall call : renderCalls) {
                    generationLog.append(call).append("\n");
                    if (generationLog.length() < 5000) {
                        System.out.println("  " + call);
                    }
                }
            }

            return new PythonPhaseResult(ast, analyzer.getErrors(), contextData, renderCalls);
        }
    }

    private static Map<String, Object> filterDynamic(
            Map<String, Object> contextData,
            StringBuilder generationLog
    ) {
        Map<String, Object> exported = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : contextData.entrySet()) {
            boolean isDynamic = entry.getValue() instanceof String
                    && ((String) entry.getValue()).startsWith("<dynamic:");
            if (isDynamic) {
                generationLog.append("SKIP: '").append(entry.getKey())
                        .append("' (dynamic placeholder).\n");
                continue;
            }
            exported.put(entry.getKey(), entry.getValue());
        }
        return exported;
    }

    private static void copySupportingFiles(
            Path projectRoot,
            Path outputDir,
            StringBuilder generationLog
    ) throws Exception {
        copyIfExists(projectRoot.resolve("app.py"), outputDir.resolve("app.py"), generationLog);

        Path style = firstExisting(
                projectRoot.resolve("style.css"),
                projectRoot.resolve("static/style.css")
        );
        Path script = firstExisting(
                projectRoot.resolve("script.js"),
                projectRoot.resolve("static/script.js")
        );

        copyIfExists(style, outputDir.resolve("style.css"), generationLog);
        copyIfExists(script, outputDir.resolve("script.js"), generationLog);
        copyIfExists(style, outputDir.resolve("static/style.css"), generationLog);
        copyIfExists(script, outputDir.resolve("static/script.js"), generationLog);

        Path imagesDir = projectRoot.resolve("static/images");
        if (Files.isDirectory(imagesDir)) {
            copyDirectory(imagesDir, outputDir.resolve("static/images"), generationLog);
        } else {
            generationLog.append("SKIP: static/images/ not found.\n");
        }
    }

    private static void copyDirectory(Path src, Path dest, StringBuilder generationLog) throws Exception {
        if (!Files.isDirectory(src)) return;
        Files.walk(src).forEach(source -> {
            try {
                Path target = dest.resolve(src.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        generationLog.append("OK: Copied static/images/ → output/static/images/\n");
    }

    private static Path firstExisting(Path... candidates) {
        for (Path p : candidates) {
            if (Files.exists(p)) return p;
        }
        return candidates[0];
    }

    private static void copyIfExists(Path src, Path dest, StringBuilder generationLog) throws Exception {
        if (!Files.exists(src)) {
            generationLog.append("SKIP: ").append(src.getFileName()).append(" not found.\n");
            return;
        }
        Files.createDirectories(dest.getParent());
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        generationLog.append("OK: Copied ").append(src.getFileName())
                .append(" → ").append(dest.getFileName()).append("\n");
    }

    private static void writeReports(
            Path compilerOutputDir,
            StringBuilder generationLog,
            StringBuilder semanticReport
    ) throws Exception {
        Files.writeString(compilerOutputDir.resolve("generation_log.txt"), generationLog.toString());
        Files.writeString(compilerOutputDir.resolve("semantic_report.txt"), semanticReport.toString());
    }
}
