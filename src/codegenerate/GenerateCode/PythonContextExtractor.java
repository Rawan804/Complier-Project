package codegenerate.GenerateCode;
import AST.Python_AST.*;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PythonContextExtractor {

    private final List<String> log = new ArrayList<>();

    public List<String> getLog() {
        return log;
    }

    public Map<String, Object> extract(ProgramNode program) {
        Map<String, Object> context = new LinkedHashMap<>();

        for (StmtNode stmt : program.getStatements()) {
            if (!(stmt instanceof AssignStmtNode)) {
                continue;
            }
            AssignStmtNode assign = (AssignStmtNode) stmt;
            ExprNode target = assign.getTarget();

            if (!(target instanceof IdentifierNode)) {
                log.add("SKIP: target غير مباشر (مو identifier) تم تجاوزه بمرحلة استخراج الـ context.");
                continue;
            }

            String varName = ((IdentifierNode) target).getName();
            Object value = evalLiteral(assign.getValue());
            context.put(varName, value);
            log.add("OK: تم استخراج المتغيّر '" + varName + "' من AST وإضافته لـ Context Data.");
        }

        return context;
    }
    private Object evalLiteral(ExprNode expr) {
        if (expr == null) return null;

        if (expr instanceof NumberNode) {
            return parseNumber(((NumberNode) expr).getValue());
        }

        if (expr instanceof StringNode) {
            return stripPythonStringLiteral(((StringNode) expr).getValue());
        }

        if (expr instanceof BooleanNode) {
            return ((BooleanNode) expr).getValue();
        }

        if (expr instanceof NullNode) {
            return null;
        }

        if (expr instanceof ListNode) {
            List<Object> items = new ArrayList<>();
            for (ExprNode item : ((ListNode) expr).getItems()) {
                items.add(evalLiteral(item));
            }
            return items;
        }

        if (expr instanceof TupleNode) {
            List<Object> items = new ArrayList<>();
            for (ExprNode item : ((TupleNode) expr).getItems()) {
                items.add(evalLiteral(item));
            }
            return items;
        }

        if (expr instanceof SetNode) {
            List<Object> items = new ArrayList<>();
            for (ExprNode item : ((SetNode) expr).getItems()) {
                items.add(evalLiteral(item));
            }
            return items;
        }

        if (expr instanceof DictNode) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (DictEntry entry : ((DictNode) expr).getEntries()) {
                Object keyValue = evalLiteral(entry.getKey());
                String key = keyValue == null ? "null" : String.valueOf(keyValue);
                map.put(key, evalLiteral(entry.getValue()));
            }
            return map;
        }
        log.add("WARN: قيمة غير ثابتة (" + expr.getClass().getSimpleName()
                + ") تم تعويضها بقيمة placeholder بدل تقييم خاطئ.");
        return "<dynamic:" + expr.getClass().getSimpleName() + ">";
    }
    private String stripPythonStringLiteral(String raw) {
        if (raw == null) return null;
        String s = raw.trim();

        if (s.length() >= 6 &&
                ((s.startsWith("\"\"\"") && s.endsWith("\"\"\"")) ||
                        (s.startsWith("'''") && s.endsWith("'''")))) {
            return s.substring(3, s.length() - 3);
        }

        if (s.length() >= 2 &&
                ((s.startsWith("\"") && s.endsWith("\"")) ||
                        (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private Object parseNumber(String raw) {
        try {
            if (raw.contains(".")) return Double.parseDouble(raw);
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            log.add("WARN: تعذر تحويل الرقم '" + raw + "' فتم إبقاؤه كنص.");
            return raw;
        }
    }
}