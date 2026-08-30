import compiler.CompilerMain;
import server.AppPyWatcher;
import server.ProductStore;
import server.RegenerationService;
import server.StaticSiteServer;

import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;


public class main {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        Path projectRoot = detectProjectRoot();
        int port = DEFAULT_PORT;

        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) || "-p".equals(args[i])) {
                if (i + 1 < args.length) {
                    port = Integer.parseInt(args[++i]);
                }
            } else if (!args[i].startsWith("-")) {
                projectRoot = Path.of(args[i]).toAbsolutePath().normalize();
            }
        }

        System.out.println("  Compiler Full 2 — Python + Jinja + Server");
        System.out.println("Project root: " + projectRoot);
        System.out.println();

        int exitCode = CompilerMain.run(projectRoot);
        if (exitCode != 0) {
            System.err.println("Initial generation failed.");
            System.exit(1);
        }

        Path appPy = projectRoot.resolve("app.py");
        Path outputDir = projectRoot.resolve("output");

        ProductStore store = new ProductStore(appPy);
        store.reloadFromAppPy();

        RegenerationService regen = new RegenerationService(projectRoot, store);
        StaticSiteServer server = StaticSiteServer.startOnAvailablePort(
                projectRoot, outputDir, store, regen, port
        );
        AppPyWatcher watcher = new AppPyWatcher(appPy, regen);

        Thread watcherThread = new Thread(watcher, "app-py-watcher");
        watcherThread.setDaemon(true);
        watcherThread.start();

        int activePort = server.getPort();
        System.out.println();
        System.out.println("=== READY ===");
        String siteUrl = "http://localhost:" + activePort;
        System.out.println("Open in browser: " + siteUrl);
        openBrowser(siteUrl);
        System.out.println();
        System.out.println("Add/Delete from web → saved to app.py → HTML regenerated");
        System.out.println("Edit app.py manually  → auto regenerate (watcher)");
        System.out.println("Press Ctrl+C to stop.");
        System.out.println();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SERVER] Shutting down...");
            server.stop();
            watcher.stop();
        }));

        Thread.currentThread().join();
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("[SERVER] Browser opened automatically.");
            }
        } catch (Exception e) {
            System.out.println("(Could not auto-open browser — open the URL manually)");
        }
    }

    private static Path detectProjectRoot() {
        Path cwd = Path.of(".").toAbsolutePath().normalize();
        if (Files.exists(cwd.resolve("app.py"))) return cwd;

        Path parent = cwd.getParent();
        if (parent != null && Files.exists(parent.resolve("app.py"))) return parent;

        Path grand = parent != null ? parent.getParent() : null;
        if (grand != null && Files.exists(grand.resolve("app.py"))) return grand;

        return cwd;
    }
}
