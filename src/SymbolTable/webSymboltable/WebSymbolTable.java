package SymbolTable.webSymboltable;

import java.util.*;

public class WebSymbolTable {

    public enum Source { HTML, CSS, JINJA }

    public static class Row {
        public final Source source;
        public final String name;
        public final String type;
        public final String scope;
        public final String value;
        public final List<String> properties = new ArrayList<>();

        public Row(Source source, String name, String type, String scope, String value) {
            this.source = source;
            this.name = name;
            this.type = type;
            this.scope = scope;
            this.value = value;
        }

        public String extraDisplay() {
            if (properties.isEmpty()) return "-";
            return String.join(", ", properties);
        }
    }

    private static final Set<String> JINJA_BUILTINS = new HashSet<>(Arrays.asList(
            "url_for", "range", "loop", "namespace", "super",
            "lipsum", "dict", "class", "joiner", "cycler",
            "true", "false", "none", "True", "False", "None"
    ));

    private final List<Row> rows = new ArrayList<>();

    private final Stack<String> htmlScopeStack = new Stack<>();

    private final Stack<Map<String, Row>> jinjaScopes = new Stack<>();

    public WebSymbolTable() {
        htmlScopeStack.push("global");
        jinjaScopes.push(new LinkedHashMap<>());

        for (String builtin : JINJA_BUILTINS) {
            Row row = new Row(Source.JINJA, builtin, "builtin", "0", "jinja_builtin");
            rows.add(row);
            jinjaScopes.peek().put(builtin, row);
        }
    }

    // ── HTML ────────────────────────────────────────────────────────────────

    public void enterHtmlScope(String scopeName) {
        htmlScopeStack.push(scopeName);
    }

    public void exitHtmlScope() {
        if (htmlScopeStack.size() > 1) htmlScopeStack.pop();
    }

    public String getCurrentHtmlScope() {
        return htmlScopeStack.peek();
    }

    public void defineHtml(String name, String type, String value) {
        rows.add(new Row(Source.HTML, name, type, getCurrentHtmlScope(), value));
    }

    // ── CSS ─────────────────────────────────────────────────────────────────

    public void defineCss(String selector, String property, String value) {
        rows.add(new Row(Source.CSS, selector, property, selector, value));
    }

    public boolean containsCssSelector(String selector) {
        return rows.stream().anyMatch(r -> r.source == Source.CSS && r.name.equals(selector));
    }

    public List<Row> getCssRowsForSelector(String selector) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            if (row.source == Source.CSS && row.name.equals(selector)) {
                result.add(row);
            }
        }
        return result;
    }

    // ── Jinja ───────────────────────────────────────────────────────────────

    public void enterJinjaScope() {
        jinjaScopes.push(new LinkedHashMap<>());
    }

    public void exitJinjaScope() {
        if (jinjaScopes.size() > 1) jinjaScopes.pop();
    }

    public int currentJinjaScopeLevel() {
        return jinjaScopes.size() - 1;
    }

    public void defineJinja(String name, String type, String value) {
        Row row = new Row(Source.JINJA, name, type, String.valueOf(currentJinjaScopeLevel()), value);
        rows.add(row);
        jinjaScopes.peek().put(name, row);
    }

    public void defineContextVar(String name, String objectType) {
        Row row = new Row(Source.JINJA, name, "context_var", "0", objectType);
        rows.add(row);
        jinjaScopes.get(0).put(name, row);
    }

    public void registerJinjaProperty(String varName, String property) {
        for (int i = jinjaScopes.size() - 1; i >= 0; i--) {
            Row row = jinjaScopes.get(i).get(varName);
            if (row != null) {
                if (!row.properties.contains(property)) {
                    row.properties.add(property);
                }
                return;
            }
        }
    }

    public boolean jinjaLookup(String name) {
        if (JINJA_BUILTINS.contains(name)) return true;
        for (int i = jinjaScopes.size() - 1; i >= 0; i--) {
            if (jinjaScopes.get(i).containsKey(name)) return true;
        }
        return false;
    }

    public Row getJinjaEntry(String name) {
        for (int i = jinjaScopes.size() - 1; i >= 0; i--) {
            Row row = jinjaScopes.get(i).get(name);
            if (row != null) return row;
        }
        return null;
    }

    public String getJinjaType(String name) {
        Row row = getJinjaEntry(name);
        return row != null ? row.type : null;
    }

    public boolean isJinjaBuiltin(String name) {
        return JINJA_BUILTINS.contains(name);
    }

    // ── Print ───────────────────────────────────────────────────────────────

    public void printTable(String testName) {
        System.out.println("\n" + "=".repeat(95));
        System.out.println("UNIFIED SYMBOL TABLE — " + testName);
        System.out.println("=".repeat(95));
        System.out.printf("%-6s | %-20s | %-18s | %-15s | %-20s | %s%n",
                "Source", "Name", "Type", "Scope", "Value", "Properties");
        System.out.println("-".repeat(95));

        if (rows.isEmpty()) {
            System.out.println("  (no symbols)");
        } else {
            for (Row row : rows) {
                if (row.source == Source.JINJA && "builtin".equals(row.type)) {
                    continue;
                }
                String scopeDisplay = row.scope;
                if (row.source == Source.JINJA) {
                    scopeDisplay = "scope " + row.scope;
                }
                System.out.printf("%-6s | %-20s | %-18s | %-15s | %-20s | %s%n",
                        row.source, row.name, row.type, scopeDisplay, row.value, row.extraDisplay());
            }
        }

        System.out.println("=".repeat(95));
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
