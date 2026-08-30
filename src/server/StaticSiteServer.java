package server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaticSiteServer {

    private final Path outputDir;
    private final Path projectRoot;
    private final ProductStore productStore;
    private final RegenerationService regenerationService;
    private final int port;
    private HttpServer server;
    private final ExecutorService regenExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "html-regen");
        t.setDaemon(true);
        return t;
    });

    public StaticSiteServer(
            Path projectRoot,
            Path outputDir,
            ProductStore productStore,
            RegenerationService regenerationService,
            int port
    ) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.outputDir = outputDir.toAbsolutePath().normalize();
        this.productStore = productStore;
        this.regenerationService = regenerationService;
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        printEndpoints();
    }

    public int getPort() {
        return port;
    }

    public static StaticSiteServer startOnAvailablePort(
            Path projectRoot,
            Path outputDir,
            ProductStore productStore,
            RegenerationService regenerationService,
            int preferredPort
    ) throws IOException {
        IOException lastError = null;
        for (int p = preferredPort; p < preferredPort + 20; p++) {
            try {
                StaticSiteServer site = new StaticSiteServer(
                        projectRoot, outputDir, productStore, regenerationService, p
                );
                site.start();
                if (p != preferredPort) {
                    System.out.println("[SERVER] Port " + preferredPort
                            + " was busy — using " + p + " instead.");
                }
                return site;
            } catch (java.net.BindException ex) {
                lastError = ex;
                if (p == preferredPort) {
                    System.out.println("[SERVER] Port " + preferredPort
                            + " already in use — trying next port...");
                }
            }
        }
        throw new IOException(
                "Could not bind ports " + preferredPort + "–" + (preferredPort + 19),
                lastError
        );
    }

    private void printEndpoints() {
        System.out.println("[SERVER] http://localhost:" + port);
        System.out.println("  ⚠ Open this URL in browser (not output/index.html directly)");
        System.out.println("  POST /add          → save to app.py + regenerate HTML");
        System.out.println("  POST /delete/{id}  → save to app.py + regenerate HTML");
    }

    public void stop() {
        if (server != null) server.stop(0);
        regenExecutor.shutdownNow();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("POST".equals(method) && "/add".equals(path)) {
                handleAdd(exchange);
                return;
            }

            if ("POST".equals(method) && path.startsWith("/delete/")) {
                handleDelete(exchange, path);
                return;
            }

            if ("GET".equals(method)) {
                handleGet(exchange, path);
                return;
            }

            send(exchange, 405, "text/plain", "Method Not Allowed");
        } catch (Exception ex) {
            ex.printStackTrace();
            send(exchange, 500, "text/html; charset=UTF-8",
                    "<h1>Error</h1><p>" + escapeHtml(ex.getMessage()) + "</p>"
                            + "<p><a href='/'>Back</a></p>");
        }
    }

    private void handleAdd(HttpExchange exchange) throws Exception {
        Map<String, String> form = parseFormBody(exchange);

        if (form.get("name") == null || form.get("name").isBlank()) {
            send(exchange, 400, "text/html; charset=UTF-8",
                    "<h1>Missing product name</h1><p><a href='/add'>Back</a></p>");
            return;
        }

        var product = productStore.addProduct(
                form.get("name"),
                form.get("price"),
                form.get("description"),
                form.get("image")
        );
        ensureProductImage(String.valueOf(product.get("image")));

        sendUpdatingPage(exchange, "تم الحفظ في app.py — جاري تحديث HTML...");

        scheduleRegenerate("UI add product id=" + product.get("id"));
    }

    private void handleDelete(HttpExchange exchange, String path) throws Exception {
        String idPart = path.substring("/delete/".length()).replaceAll("[^0-9]", "");
        if (idPart.isEmpty()) {
            send(exchange, 400, "text/plain", "Invalid product id");
            return;
        }
        long id = Long.parseLong(idPart);

        boolean removed = productStore.deleteProduct(id);
        if (!removed) {
            send(exchange, 404, "text/html; charset=UTF-8",
                    "<h1>Product not found</h1><p><a href='/'>Back</a></p>");
            return;
        }

        sendUpdatingPage(exchange, "تم الحذف من app.py — جاري تحديث HTML...");
        scheduleRegenerate("UI delete product id=" + id);
    }

    private void scheduleRegenerate(String reason) {
        regenExecutor.submit(() -> {
            try {
                regenerationService.regenerate(reason);
                System.out.println("[SERVER] HTML updated after: " + reason);
            } catch (Exception e) {
                System.err.println("[SERVER] Regenerate failed (app.py already saved): " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void sendUpdatingPage(HttpExchange exchange, String message) throws IOException {
        String html = """
                <!DOCTYPE html>
                <html><head>
                <meta charset="UTF-8">
                <meta http-equiv="refresh" content="2;url=/">
                <title>Updating...</title>
                </head><body style="font-family:Arial;text-align:center;padding:40px;">
                <h2>%s</h2>
                <p>سيتم تحويلك للصفحة الرئيسية خلال ثانيتين...</p>
                <p><a href="/">اضغط هنا إذا لم يتم التحويل</a></p>
                </body></html>
                """.formatted(escapeHtml(message));
        send(exchange, 200, "text/html; charset=UTF-8", html);
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if ("/".equals(path) || "/index.html".equals(path)) {
            serveFile(exchange, outputDir.resolve("index.html"));
            return;
        }
        if ("/add".equals(path) || "/add_product.html".equals(path)) {
            serveFile(exchange, outputDir.resolve("add_product.html"));
            return;
        }

        String relative = path.startsWith("/") ? path.substring(1) : path;
        Path file = outputDir.resolve(relative).normalize();
        if (!file.startsWith(outputDir)) {
            send(exchange, 403, "text/plain", "Forbidden");
            return;
        }
        if (Files.exists(file) && Files.isRegularFile(file)) {
            serveFile(exchange, file);
        } else if (relative.startsWith("static/images/")) {
            Path fallback = outputDir.resolve("static/images/default.jpg");
            if (Files.exists(fallback)) {
                serveFile(exchange, fallback);
            } else {
                send(exchange, 404, "text/plain", "Image not found: " + path);
            }
        } else {
            send(exchange, 404, "text/plain", "Not Found: " + path);
        }
    }

    private void ensureProductImage(String imageName) throws IOException {
        if (imageName == null || imageName.isBlank()) {
            imageName = "default.jpg";
        }
        imageName = imageName.replace("\\", "/");
        if (imageName.contains("/")) {
            imageName = imageName.substring(imageName.lastIndexOf('/') + 1);
        }

        Path imagesDir = projectRoot.resolve("static/images");
        Path target = imagesDir.resolve(imageName).normalize();
        if (!target.startsWith(imagesDir.normalize())) {
            return;
        }
        if (Files.exists(target)) {
            return;
        }

        Files.createDirectories(imagesDir);
        Path defaultImg = imagesDir.resolve("default.jpg");
        if (Files.exists(defaultImg)) {
            Files.copy(defaultImg, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Path outputImg = outputDir.resolve("static/images").resolve(imageName);
            Files.createDirectories(outputImg.getParent());
            Files.copy(defaultImg, outputImg, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[SERVER] Created missing image: static/images/" + imageName);
        }
    }

    private void serveFile(HttpExchange exchange, Path file) throws IOException {
        if (!Files.exists(file)) {
            send(exchange, 404, "text/plain", "File not found: " + file.getFileName());
            return;
        }

        byte[] bytes = Files.readAllBytes(file);
        String contentType = detectContentType(file, bytes);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseFormBody(HttpExchange exchange) throws IOException {
        byte[] body = exchange.getRequestBody().readAllBytes();
        String raw = new String(body, StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        if (raw.isEmpty()) return map;
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) continue;
            String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
            String val = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            map.put(key, val);
        }
        return map;
    }

    private void send(HttpExchange exchange, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", type);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String detectContentType(Path file, byte[] bytes) {
        String name = file.getFileName().toString().toLowerCase();
        if (bytes.length >= 4) {
            if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
                return "image/png";
            }
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) {
                return "image/jpeg";
            }
            if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46) {
                return "image/gif";
            }
            if (bytes[0] == '<') {
                String head = new String(bytes, 0, Math.min(bytes.length, 256), StandardCharsets.UTF_8)
                        .trim().toLowerCase();
                if (head.startsWith("<svg") || head.startsWith("<?xml")) {
                    return "image/svg+xml";
                }
            }
        }

        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".webp")) return "image/webp";
        return "text/html; charset=UTF-8";
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
