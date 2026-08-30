package codegenerate;

import AST.cssast.DeclarationNode;
import AST.cssast.RuleSetNode;
import AST.cssast.StylesheetNode;
import AST.hast.*;
import AST.hast.jinja.*;
import AST.webast.WebASTNode;

import java.util.*;

public class WebCodeGenerator {
    private Map<String, String> routeFileMap = new HashMap<>();
    private final StringBuilder out = new StringBuilder();
    private int indentLevel = 0;

<<<<<<< HEAD
=======
    // ⬇️ جديد: مكدس نطاقات (Scopes) للمتغيرات أثناء التوليد
>>>>>>> 9a836d3 (add test file for python)
    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();
    private void emit(String text) {
        out.append(text);
    }
    public String generate(WebASTNode root, Map<String, Object> contextValues, Map<String, String> routeFileMap) {
        out.setLength(0);
        indentLevel = 0;
        scopes.clear();
        scopes.push(new HashMap<>(contextValues));
<<<<<<< HEAD
        this.routeFileMap = routeFileMap;
=======
        this.routeFileMap = routeFileMap;   // ⬅️ جديد
>>>>>>> 9a836d3 (add test file for python)

        if (root instanceof DocumentNode doc) {
            emit("<!DOCTYPE html>\n");
            if (doc.getHtmlElement() != null) {
                generateNode(doc.getHtmlElement());
            }
        } else if (root != null) {
            generateNode(root);
        }
        return out.toString();
    }
<<<<<<< HEAD
=======
    /** يحل url_for('route_name', ...) لاسم الملف الفعلي */
>>>>>>> 9a836d3 (add test file for python)
    private String resolveUrlFor(String expr) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "url_for\\(\\s*['\"]([a-zA-Z_][a-zA-Z0-9_]*)['\"](.*)\\)"
        ).matcher(expr);

        if (!m.find()) return null;

        String routeName = m.group(1);
<<<<<<< HEAD
        String argsPart = m.group(2);
=======
        String argsPart = m.group(2); // كل شي بعد اسم الـ route، مثلاً: ", product_id=product.id"

        // معالجة static
>>>>>>> 9a836d3 (add test file for python)
        if (routeName.equals("static")) {
            java.util.regex.Matcher fm = java.util.regex.Pattern.compile(
                    "filename\\s*=\\s*['\"]([^'\"]+)['\"]"
            ).matcher(argsPart);
            if (fm.find()) {
                return "/static/" + fm.group(1);
            }
        }

<<<<<<< HEAD
=======
        // مسارات سيرفر Java (إضافة / حذف / عرض)
>>>>>>> 9a836d3 (add test file for python)
        if (routeName.equals("add_product")) {
            return "/add";
        }
        if (routeName.equals("index")) {
            return "/";
        }

<<<<<<< HEAD
=======
        // معالجة أي route فيه *_id ديناميكي
>>>>>>> 9a836d3 (add test file for python)
        java.util.regex.Matcher idMatch = java.util.regex.Pattern.compile(
                "(\\w+_id)\\s*=\\s*([a-zA-Z_][a-zA-Z0-9_.]*)"
        ).matcher(argsPart);

        if (idMatch.find()) {
            String idExpr = idMatch.group(2);
            Object idVal = resolveExpression(idExpr);
            if (idVal != null) {
                if (routeName.equals("delete_product_route")) {
                    return "/delete/" + idVal;
                }
                String baseFile = routeFileMap.get(routeName);
                String nameNoExt = (baseFile != null && baseFile.endsWith(".html"))
                        ? baseFile.substring(0, baseFile.length() - 5)
                        : routeName;
                return nameNoExt + "_" + idVal + ".html";
            }
        }

        return routeFileMap.get(routeName);
    }

    private void generateNode(WebASTNode node) {
        if (node == null) return;

        if (node instanceof ElementNode el) {
            generateElement(el);
        } else if (node instanceof TextNode text) {
            out.append(text.getText());
        } else if (node instanceof JinjaExprNode expr) {
            generateJinjaExpr(expr);
        } else if (node instanceof JinjaForNode jfor) {
            generateJinjaFor(jfor);
        } else if (node instanceof JinjaIfNode jif) {
            generateJinjaIf(jif);
        } else if (node instanceof StylesheetNode sheet) {
            for (AST.cssast.AST cssChild : sheet.getChildren()) {
                if (cssChild instanceof RuleSetNode rule) {
                    generateRuleSet(rule);
                }
            }
        }
    }

<<<<<<< HEAD
=======
    // ⬇️⬇️⬇️ الجزء الجديد بالكامل: استبدال {{ expr }} بقيمته الحقيقية
>>>>>>> 9a836d3 (add test file for python)

    private void generateJinjaExpr(JinjaExprNode expr) {
        String raw = expr.getExpression().trim();

<<<<<<< HEAD
=======
        // url_for(...) مو جزء من context data - نتركها متل ما هي
>>>>>>> 9a836d3 (add test file for python)
        if (raw.contains("url_for(")) {
            String resolvedFile = resolveUrlFor(raw);
            out.append(resolvedFile != null ? resolvedFile : raw);
            return;
        }

        Object resolved = resolveExpression(raw);

        if (resolved == null) {
<<<<<<< HEAD
=======
            // ما قدرنا نحلها (فلتر معقد، أو متغير غير معروف) - نتركها متل ما هي كـ fallback آمن
>>>>>>> 9a836d3 (add test file for python)
            out.append("{{ ").append(raw).append(" }}");
        } else {
            String val = stringifyValue(resolved);
            if (raw.contains("image") || looksLikeImageFile(val)) {
                val = toStaticImagePath(val);
            }
            out.append(val);
        }
    }

<<<<<<< HEAD
=======
    /** يحوّل اسم ملف صورة إلى مسار static/images/ (مثل Flask static folder) */
>>>>>>> 9a836d3 (add test file for python)
    private String toStaticImagePath(String value) {
        if (value == null || value.isEmpty()) return value;
        if (value.startsWith("http://") || value.startsWith("https://")
                || value.startsWith("/") || value.startsWith("static/")) {
            return value;
        }
        return "/static/images/" + value;
    }

    private boolean looksLikeImageFile(String s) {
        return s.matches("(?i)[^/\\\\]+\\.(jpg|jpeg|png|gif|webp|svg)$");
    }

    private void generateJinjaFor(JinjaForNode jfor) {
        Object iterableVal = resolveExpression(jfor.getIterable().trim());

        if (!(iterableVal instanceof List<?> list)) {
            indent();
            out.append("{% for ").append(jfor.getVariable())
                    .append(" in ").append(jfor.getIterable()).append(" %}\n");
            indentLevel++;
            for (WebASTNode child : jfor.getChildren()) generateNode(child);
            indentLevel--;
            indent();
            out.append("{% endfor %}\n");
            return;
        }

        for (Object item : list) {
            Map<String, Object> loopScope = new HashMap<>();
            loopScope.put(jfor.getVariable(), item);
            scopes.push(loopScope);

            for (WebASTNode child : jfor.getChildren()) {
                generateNode(child);
            }

            scopes.pop();
        }
    }

    private void generateJinjaIf(JinjaIfNode jif) {
        Object condVal = resolveExpression(jif.getCondition().trim());
        boolean truthy = isTruthy(condVal);

        scopes.push(new HashMap<>());
        if (truthy) {
            if (jif.getThenBlock() != null)
                for (ASTNode child : jif.getThenBlock()) generateNode((WebASTNode) child);
        } else {
            if (jif.getElseBlock() != null)
                for (ASTNode child : jif.getElseBlock()) generateNode((WebASTNode) child);
        }
        scopes.pop();
    }

<<<<<<< HEAD
=======
    /** يحل expression زي "product.name" لقيمته الحقيقية بالمرور على النطاقات من الأقرب للأبعد */
>>>>>>> 9a836d3 (add test file for python)
    private Object resolveExpression(String expr) {
        if (expr == null || expr.isBlank()) return null;
        if (expr.contains("|") || expr.contains("(")) return null; // فلاتر/دوال معقدة - غير مدعومة حاليًا

        String[] parts = expr.split("\\.");
        String base = parts[0].trim();

        Object current = null;
        boolean found = false;
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(base)) {
                current = scope.get(base);
                found = true;
                break;
            }
        }
        if (!found) return null;

        for (int i = 1; i < parts.length; i++) {
            String field = parts[i].trim();
            if (current instanceof Map<?, ?> map && map.containsKey(field)) {
                current = map.get(field);
            } else {
                return null;
            }
        }
        return current;
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return !s.isEmpty() && !s.equalsIgnoreCase("false") && !s.equalsIgnoreCase("none");
        if (value instanceof List<?> list) return !list.isEmpty();
        if (value instanceof Number n) return n.doubleValue() != 0;
        return true;
    }

    private String stringifyValue(Object value) {
        if (value == null) return "";
        return String.valueOf(value);
    }

<<<<<<< HEAD
=======
    // ⬆️⬆️⬆️ نهاية الجزء الجديد
>>>>>>> 9a836d3 (add test file for python)

    private void generateElement(ElementNode el) {
        indent();
        out.append('<').append(el.getTagName());

        for (AttributeNode attr : el.getAttributes().values()) {
            out.append(' ').append(attr.getName()).append('=');
            if (attr.isJinjaExpr()) {
                String rawVal = attr.getValue().trim();
                String innerExpr = rawVal;
                if (innerExpr.contains("url_for(")) {
                    String resolvedFile = resolveUrlFor(innerExpr);
                    out.append('"').append(resolvedFile != null ? resolvedFile : innerExpr).append('"');
                } else {
                    String exprBody = rawVal
                            .replace("{{", "")
                            .replace("}}", "")
                            .trim();
                    Object resolved = resolveExpression(exprBody.trim());
                    if (resolved != null) {
                        String val = stringifyValue(resolved);
                        if ("img".equals(el.getTagName()) && "src".equals(attr.getName())) {
                            val = toStaticImagePath(val);
                        }
                        out.append('"').append(escapeAttr(val)).append('"');
                    } else {
                        String finalVal = rawVal.startsWith("{{") ? rawVal : "{{ " + rawVal + " }}";
                        out.append('"').append(finalVal).append('"');
                    }
                }
            } else if ("true".equals(attr.getValue()) && isBooleanAttr(attr.getName())) {
                out.append('"').append(attr.getName()).append('"');
            } else {
                out.append('"').append(escapeAttr(attr.getValue())).append('"');
            }
        }

        if (el.isSelfClosing() || isVoidElement(el.getTagName())) {
            out.append(" />\n");
            return;
        }

        if (el.getTagName().equals("style") && hasStylesheetChild(el)) {
            out.append(">\n");
            indentLevel++;
            for (WebASTNode child : el.getChildren()) generateNode(child);
            indentLevel--;
            indent();
            out.append("</style>\n");
            return;
        }

        out.append('>');

        if (el.getChildren().isEmpty()) {
            out.append("</").append(el.getTagName()).append(">\n");
            return;
        }

        boolean inline = isInlineContainer(el);
        if (!inline) out.append('\n');

        indentLevel++;
        for (WebASTNode child : el.getChildren()) generateNode(child);
        indentLevel--;

        if (!inline) indent();
        out.append("</").append(el.getTagName()).append(">\n");
    }

    private void generateRuleSet(RuleSetNode rule) {
        indent();
        out.append(rule.getSelectorText()).append(" {\n");
        indentLevel++;
        for (AST.cssast.AST child : rule.getChildren()) {
            if (child instanceof DeclarationNode decl) {
                indent();
                out.append(decl.getProperty()).append(": ").append(decl.getValue()).append(";\n");
            }
        }
        indentLevel--;
        indent();
        out.append("}\n\n");
    }

    private boolean hasStylesheetChild(ElementNode el) {
        for (WebASTNode child : el.getChildren()) if (child instanceof StylesheetNode) return true;
        return false;
    }

    private boolean isVoidElement(String tag) {
        return tag.equals("br") || tag.equals("img") || tag.equals("input") || tag.equals("link");
    }

    private boolean isBooleanAttr(String name) {
        return name.equals("required") || name.equals("disabled") || name.equals("checked");
    }

    private boolean isInlineContainer(ElementNode el) {
        String tag = el.getTagName();
        return tag.equals("a") || tag.equals("span") || tag.equals("button") || tag.equals("title");
    }

    private String escapeAttr(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void indent() {
        out.append("    ".repeat(Math.max(0, indentLevel)));
    }
}