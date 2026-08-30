package semantic.web;

import AST.hast.*;
import AST.hast.jinja.*;
import AST.cssast.*;
import AST.webast.WebASTNode;
import SymbolTable.webSymboltable.WebSymbolTable;
import semantic.SemanticError;
import semantic.SemanticError.Severity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSemanticAnalyzer {

    private final List<SemanticError> errors = new ArrayList<>();

    private final Set<String> htmlIds = new HashSet<>();
    private final Set<String> htmlClasses = new HashSet<>();

    private int formDepth = 0;
    private final Set<String> usedIds = new HashSet<>();

    private final WebSymbolTable symbolTable;
    private final Stack<Map<String, String>> localScopes = new Stack<>();

    private static final Set<String> JINJA_KEYWORDS = Set.of(
            "in", "if", "for", "else", "endif", "endfor", "not", "and", "or",
            "true", "false", "none", "True", "False", "None"
    );

    private static final Set<String> JINJA_FILTERS = Set.of(
            "length", "upper", "lower", "trim", "default", "safe",
            "capitalize", "title", "reverse", "sort", "join", "replace",
            "abs", "round", "int", "float", "string", "list", "first", "last"
    );

    public WebSemanticAnalyzer(WebSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        localScopes.push(new HashMap<>());
    }

    public void analyze(WebASTNode root) {
        collectHTML(root);
        visit(root);
    }

    private void collectHTML(WebASTNode node) {
        if (node == null) return;

        if (node instanceof ElementNode el) {

            if (el.getAttributes().containsKey("id")) {
                String id = el.getAttributes().get("id").getValue();
                htmlIds.add(id);
            }

            if (el.getAttributes().containsKey("class")) {
                String value = el.getAttributes().get("class").getValue();
                for (String c : value.split("\\s+")) {
                    if (!c.isEmpty())
                        htmlClasses.add(c);
                }
            }

            for (WebASTNode c : el.getChildren()) {
                collectHTML(c);
            }
        }

        else if (node instanceof DocumentNode doc) {
            collectHTML(doc.getHtmlElement());
        }

        else if (node instanceof JinjaForNode jfor) {
            for (WebASTNode c : jfor.getChildren()) collectHTML(c);
        }

        else if (node instanceof JinjaIfNode jif) {
            if (jif.getThenBlock() != null)
                for (ASTNode c : jif.getThenBlock()) collectHTML(c);
            if (jif.getElseBlock() != null)
                for (ASTNode c : jif.getElseBlock()) collectHTML(c);
        }
    }

    private void visit(WebASTNode node) {
        if (node == null) return;

        if (node instanceof DocumentNode doc) {
            visit(doc.getHtmlElement());
        }

        else if (node instanceof ElementNode el) {
            visitElement(el);
        }

        else if (node instanceof StylesheetNode css) {
            visitStylesheet(css);
        }

        else if (node instanceof RuleSetNode rule) {
            visitRuleSet(rule);
        }

        else if (node instanceof JinjaForNode jfor) {
            visitJinjaFor(jfor);
        }

        else if (node instanceof JinjaIfNode jif) {
            visitJinjaIf(jif);
        }

        else if (node instanceof JinjaExprNode expr) {
            visitJinjaExpr(expr);
        }
    }

    private void visitElement(ElementNode node) {

        String tag = node.getTagName();

        if (tag.equals("form")) {
            if (formDepth > 0)
                error("Nested <form> is not allowed", node.getLine(), "HTML");

            formDepth++;

            if (!node.getAttributes().containsKey("action"))
                warn("<form> missing action", node.getLine(), "HTML");
        }

        if (tag.equals("input") && formDepth == 0)
            warn("<input> outside <form>", node.getLine(), "HTML");

        if (tag.equals("textarea") && formDepth == 0)
            warn("<textarea> outside <form>", node.getLine(), "HTML");

        if (tag.equals("img") && !node.getAttributes().containsKey("src"))
            warn("<img> missing src", node.getLine(), "HTML");

        if (tag.equals("link") && !node.getAttributes().containsKey("href"))
            warn("<link> missing href", node.getLine(), "HTML");

        if (tag.equals("head")) {
            boolean hasTitle = false;

            for (WebASTNode c : node.getChildren()) {
                if (c instanceof ElementNode e &&
                        e.getTagName().equals("title")) {
                    hasTitle = true;
                }
            }

            if (!hasTitle)
                warn("<head> missing <title>", node.getLine(), "HTML");
        }

        if (node.getAttributes().containsKey("id")) {
            String id = node.getAttributes().get("id").getValue();

            if (!usedIds.add(id))
                error("Duplicate id: " + id, node.getLine(), "HTML");
        }

        for (AttributeNode a : node.getAttributes().values()) {
            visitAttribute(a);
        }

        for (WebASTNode c : node.getChildren()) {
            visit(c);
        }

        if (tag.equals("form"))
            formDepth--;
    }

    private void visitAttribute(AttributeNode node) {
        if (node.isJinjaExpr()) {
            checkJinjaExpressionVars(node.getValue(), node.getLine());
        }
    }

    private void enterLocalScope() {
        localScopes.push(new HashMap<>());
    }

    private void exitLocalScope() {
        if (localScopes.size() > 1)
            localScopes.pop();
    }

    private void defineLocal(String name) {
        localScopes.peek().put(name, "loop_variable");
    }

    private void visitJinjaFor(JinjaForNode node) {

        if (!isKnownIdentifier(node.getIterable())) {
            error("Undefined Jinja iterable: '" + node.getIterable() + "'", node.getLine(), "JINJA");
        }

        enterLocalScope();
        defineLocal(node.getVariable());

        for (WebASTNode c : node.getChildren()) {
            visit(c);
        }

        exitLocalScope();
    }

    private void visitJinjaIf(JinjaIfNode node) {

        checkJinjaCondition(node.getCondition(), node.getLine());

        enterLocalScope();

        if (node.getThenBlock() != null)
            for (ASTNode c : node.getThenBlock()) visit(c);

        if (node.getElseBlock() != null)
            for (ASTNode c : node.getElseBlock()) visit(c);

        exitLocalScope();
    }

    private void visitJinjaExpr(JinjaExprNode node) {
        checkJinjaExpressionVars(node.getExpression(), node.getLine());
    }

    private void checkJinjaExpressionVars(String expr, int line) {
        if (expr == null || expr.isBlank()) return;

        // ⬇️ الخطوة الأولى صارت إزالة الـ string literals قبل أي فحص تاني
        String cleaned = expr.replaceAll("'[^']*'|\"[^\"]*\"", " ");

        // ⬇️ فحص الـ dot pattern هلأ عم يشتغل على النسخة "المنظّفة" مش الخام
        Pattern dotPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\.");
        Matcher dotMatcher = dotPattern.matcher(cleaned);
        while (dotMatcher.find()) {
            String base = dotMatcher.group(1);
            if (!isKnownIdentifier(base)) {
                error("Undefined Jinja variable: '" + base + "'", line, "JINJA");
            }
        }

        cleaned = cleaned.replaceAll("\\b(url_for|range|namespace|super|dict|cycler|joiner|lipsum)\\s*\\([^)]*\\)", " ");
        cleaned = cleaned.replaceAll("\\|\\s*[a-zA-Z_][a-zA-Z0-9_]*", " ");
        cleaned = cleaned.replaceAll("\\b[a-zA-Z_][a-zA-Z0-9_]*\\.[a-zA-Z_][a-zA-Z0-9_.]*", " ");

        Pattern idPattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
        Matcher idMatcher = idPattern.matcher(cleaned);
        while (idMatcher.find()) {
            String id = idMatcher.group(1);
            if (isKnownIdentifier(id)) continue;

            int start = idMatcher.start();
            int end = idMatcher.end();
            if (start > 0 && cleaned.charAt(start - 1) == '.') continue;
            if (end < cleaned.length() && cleaned.charAt(end) == '=') continue;

            error("Undefined Jinja variable: '" + id + "'", line, "JINJA");
        }
    }

    private void checkJinjaCondition(String condition, int line) {
        checkJinjaExpressionVars(condition, line);
    }

    private boolean isKnownIdentifier(String name) {
        if (name == null || name.isEmpty()) return true;
        if (symbolTable.isJinjaBuiltin(name)) return true;
        if (JINJA_KEYWORDS.contains(name)) return true;
        if (JINJA_FILTERS.contains(name)) return true;

        for (int i = localScopes.size() - 1; i >= 0; i--) {
            if (localScopes.get(i).containsKey(name)) return true;
        }
        return symbolTable.jinjaLookup(name);
    }

    private void visitStylesheet(StylesheetNode node) {

        if (node.getChildren().isEmpty())
            warn("Empty stylesheet", node.getLineNumber(), "CSS");

        for (WebASTNode c : node.getChildren()) {
            visit(c);
        }
    }

    private void visitRuleSet(RuleSetNode node) {

        boolean hasDeclaration = false;

        for (WebASTNode c : node.getChildren()) {

            if (c instanceof DeclarationNode)
                hasDeclaration = true;

            if (c instanceof SelectorGroupNode group) {
                for (WebASTNode s : group.getChildren()) {
                    checkSelector(s);
                }
            }

            visit(c);
        }

        if (!hasDeclaration)
            warn("Empty CSS rule", node.getLineNumber(), "CSS");
    }

    private void checkSelector(WebASTNode node) {

        if (node instanceof SelectorNode sel) {
            for (WebASTNode c : sel.getChildren()) {
                checkSelector(c);
            }
            return;
        }

        if (!(node instanceof SimpleSelectorNode simple))
            return;

        for (WebASTNode c : simple.getChildren()) {

            if (!(c instanceof SelectorModifierNode mod))
                continue;

            String value = mod.getValue();
            if (value == null) continue;

            String clean = value.trim();

            if (clean.startsWith("#")) {
                String id = clean.substring(1);

                if (!htmlIds.contains(id)) {
                    warn("CSS uses undefined id: #" + id, mod.getLineNumber(), "CSS");
                }
            }

            else if (clean.startsWith(".")) {
                String cls = clean.substring(1);

                if (!htmlClasses.contains(cls)) {
                    warn("CSS uses undefined class: ." + cls, mod.getLineNumber(), "CSS");
                }
            }
        }
    }

    private void error(String msg, int line, String source) {
        errors.add(new SemanticError(Severity.ERROR, msg, line, source));
    }

    private void warn(String msg, int line, String source) {
        errors.add(new SemanticError(Severity.WARNING, msg, line, source));
    }

    public List<SemanticError> getErrors() {
        return errors;
    }

    public List<SemanticError> getErrorsOnly() {
        List<SemanticError> result = new ArrayList<>();
        for (SemanticError e : errors) {
            if (e.getSeverity() == Severity.ERROR) result.add(e);
        }
        return result;
    }

    public boolean hasErrors() {
        return !getErrorsOnly().isEmpty();
    }
}
