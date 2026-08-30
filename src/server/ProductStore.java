package server;

import AST.Python_AST.ProgramNode;
import LexerandParser.puthon_antlr.python_lexer;
import LexerandParser.puthon_antlr.python_parser;
import codegenerate.GenerateCode.PythonContextExtractor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import visitor.python_visitor.PythonASTVisitor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductStore {

    private static final Pattern PRODUCTS_BLOCK =
            Pattern.compile("(?ms)^products\\s*=\\s*\\[");

    private final Path appPy;
    private List<Map<String, Object>> products = new ArrayList<>();

    public ProductStore(Path appPy) {
        this.appPy = appPy.toAbsolutePath().normalize();
    }

    public Path getAppPyPath() {
        return appPy;
    }

    public List<Map<String, Object>> getProducts() {
        return products;
    }

    public void reloadFromAppPy() throws Exception {
        products = new ArrayList<>();
        if (!Files.exists(appPy)) return;

        try (var is = Files.newInputStream(appPy)) {
            CharStream input = CharStreams.fromStream(is);
            python_lexer lexer = new python_lexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            python_parser parser = new python_parser(tokens);
            ParseTree tree = parser.prog();

            PythonASTVisitor visitor = new PythonASTVisitor();
            ProgramNode ast = (ProgramNode) visitor.visit(tree);

            PythonContextExtractor extractor = new PythonContextExtractor();
            Map<String, Object> context = extractor.extract(ast);

            Object raw = context.get("products");
            if (raw instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map) {
                        Map<String, Object> copy = new LinkedHashMap<>();
                        for (Map.Entry<?, ?> e : map.entrySet()) {
                            copy.put(String.valueOf(e.getKey()), e.getValue());
                        }
                        products.add(copy);
                    }
                }
            }
        }
    }

    public Map<String, Object> addProduct(String name, String price, String description, String image)
            throws Exception {
        reloadFromAppPy();

        long nextId = 1;
        for (Map<String, Object> p : products) {
            Object id = p.get("id");
            if (id instanceof Number n) {
                nextId = Math.max(nextId, n.longValue() + 1);
            }
        }

        Map<String, Object> product = new LinkedHashMap<>();
        product.put("id", nextId);
        product.put("name", name != null ? name.trim() : "");
        product.put("price", price != null ? price.trim() : "0");
        product.put("description", description != null ? description.trim() : "");
        product.put("image", (image == null || image.isBlank()) ? "default.jpg" : image.trim());

        products.add(product);
        writeProductsToAppPy();
        System.out.println("[PYTHON] Saved new product to app.py → id=" + nextId + " name=" + product.get("name"));
        return product;
    }

    public boolean deleteProduct(long id) throws Exception {
        reloadFromAppPy();
        boolean removed = products.removeIf(p -> {
            Object pid = p.get("id");
            if (pid instanceof Number n) return n.longValue() == id;
            return String.valueOf(pid).equals(String.valueOf(id));
        });
        if (removed) {
            writeProductsToAppPy();
            System.out.println("[PYTHON] Deleted product from app.py → id=" + id);
        } else {
            System.out.println("[PYTHON] Product id=" + id + " not found in app.py");
        }
        return removed;
    }

    public void writeProductsToAppPy() throws Exception {
        String content = Files.readString(appPy, StandardCharsets.UTF_8);

        Matcher matcher = PRODUCTS_BLOCK.matcher(content);
        if (!matcher.find()) {
            throw new IllegalStateException("لم يُعثر على products = [...] في app.py");
        }

        int bracketStart = matcher.end() - 1;
        int bracketEnd = findMatchingBracket(content, bracketStart);

        String before = content.substring(0, matcher.start());
        String after = content.substring(bracketEnd + 1);
        String newBlock = toPythonList(products);

        Files.writeString(appPy, before + newBlock + after, StandardCharsets.UTF_8);
    }

    private static int findMatchingBracket(String s, int openIdx) {
        int depth = 0;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        throw new IllegalStateException("Unbalanced brackets in products list");
    }

    private static String toPythonList(List<Map<String, Object>> products) {
        StringBuilder sb = new StringBuilder("products = [\n");
        for (Map<String, Object> p : products) {
            sb.append("    {\n");
            appendField(sb, "id", p.get("id"));
            appendField(sb, "name", p.get("name"));
            appendField(sb, "price", p.get("price"));
            appendField(sb, "description", p.get("description"));
            appendField(sb, "image", p.get("image"));
            sb.append("    },\n");
        }
        sb.append("]\n");
        return sb.toString();
    }

    private static void appendField(StringBuilder sb, String key, Object value) {
        sb.append("        \"").append(key).append("\": ");
        if (value instanceof Number) {
            sb.append(value);
        } else {
            sb.append("\"").append(escapePython(String.valueOf(value))).append("\"");
        }
        sb.append(",\n");
    }

    private static String escapePython(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
