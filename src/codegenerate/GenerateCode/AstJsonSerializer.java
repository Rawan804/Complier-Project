package codegenerate.GenerateCode;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public final class AstJsonSerializer {

    private AstJsonSerializer() {}

    public static String toJson(Object node) {
        StringBuilder sb = new StringBuilder();
        write(node, sb, 0);
        return sb.toString();
    }
    public static String prettyPrint(String json) {
        StringBuilder out = new StringBuilder();
        int indent = 0;
        boolean inString = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (inString) {
                out.append(c);
                if (c == '\\' && i + 1 < json.length()) {
                    out.append(json.charAt(++i));
                    continue;
                }
                if (c == '"') inString = false;
                continue;
            }

            switch (c) {
                case '"':
                    inString = true;
                    out.append(c);
                    break;

                case '{':
                case '[': {
                    char next = (i + 1 < json.length()) ? json.charAt(i + 1) : 0;
                    out.append(c);
                    if (next == '}' || next == ']') {
                    } else {
                        indent++;
                        out.append('\n').append(indentUnit(indent));
                    }
                    break;
                }

                case '}':
                case ']': {
                    char prevChar = (out.length() > 0) ? out.charAt(out.length() - 1) : 0;
                    if (prevChar == '{' || prevChar == '[') {
                        out.append(c);
                    } else {
                        indent--;
                        out.append('\n').append(indentUnit(indent)).append(c);
                    }
                    break;
                }

                case ',':
                    out.append(c).append('\n').append(indentUnit(indent));
                    break;

                case ':':
                    out.append(c).append(' ');
                    break;

                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    private static String indentUnit(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) sb.append("  ");
        return sb.toString();
    }

    private static void write(Object value, StringBuilder sb, int depth) {
        if (depth > 60) {
            sb.append("\"<max-depth>\"");
            return;
        }

        if (value == null) {
            sb.append("null");
            return;
        }

        if (value instanceof String) {
            writeString((String) value, sb);
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            sb.append(value.toString());
            return;
        }

        if (value instanceof Enum) {
            writeString(((Enum<?>) value).name(), sb);
            return;
        }

        if (value instanceof List) {
            List<?> list = (List<?>) value;
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                write(list.get(i), sb, depth + 1);
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            return;
        }

        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            sb.append("{");
            int i = 0;
            int n = map.size();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                writeString(String.valueOf(e.getKey()), sb);
                sb.append(":");
                write(e.getValue(), sb, depth + 1);
                if (++i < n) sb.append(",");
            }
            sb.append("}");
            return;
        }
        Class<?> clazz = value.getClass();
        Method[] methods = clazz.getMethods();

        sb.append("{");
        writeString("_type", sb);
        sb.append(":");
        writeString(clazz.getSimpleName(), sb);

        for (Method m : methods) {
            if (!isSimpleGetter(m)) continue;
            String fieldName = getterToFieldName(m.getName());
            if (fieldName.equals("class")) continue;

            Object fieldValue;
            try {
                fieldValue = m.invoke(value);
            } catch (Exception ex) {
                fieldValue = "<error: " + ex.getMessage() + ">";
            }

            sb.append(",");
            writeString(fieldName, sb);
            sb.append(":");
            write(fieldValue, sb, depth + 1);
        }

        sb.append("}");
    }

    private static boolean isSimpleGetter(Method m) {
        if (m.getParameterCount() != 0) return false;
        String name = m.getName();
        if (name.equals("getClass")) return false;
        return (name.startsWith("get") && name.length() > 3)
                || (name.startsWith("is") && name.length() > 2);
    }

    private static String getterToFieldName(String getterName) {
        String stripped = getterName.startsWith("get")
                ? getterName.substring(3)
                : getterName.substring(2); // isXxx()
        if (stripped.isEmpty()) return stripped;
        return Character.toLowerCase(stripped.charAt(0)) + stripped.substring(1);
    }

    private static void writeString(String s, StringBuilder sb) {
        sb.append("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
    }
}