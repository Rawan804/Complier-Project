package server;

import java.nio.file.*;

public class AppPyWatcher implements Runnable {

    private final Path appPy;
    private final RegenerationService regenerationService;
    private volatile boolean running = true;

    public AppPyWatcher(Path appPy, RegenerationService regenerationService) {
        this.appPy = appPy;
        this.regenerationService = regenerationService;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            Path dir = appPy.getParent();
            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

            System.out.println("[WATCHER] Monitoring app.py for manual edits → auto regenerate");

            while (running) {
                WatchKey key = watcher.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;

                    Path changed = (Path) event.context();
                    if (!"app.py".equals(changed.toString())) continue;

                    if (regenerationService.isSuppressWatcher()) {
                        continue; // Java server wrote app.py — لا نعيد التوليد مرتين
                    }

                    Thread.sleep(400); // debounce
                    System.out.println("[WATCHER] app.py changed manually → regenerating...");
                    try {
                        regenerationService.regenerate("manual app.py edit");
                        regenerationService.syncProducts();
                    } catch (Exception ex) {
                        System.err.println("[WATCHER] Regeneration failed: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            System.err.println("[WATCHER] Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
