package server;

import compiler.CompilerMain;

import java.nio.file.Path;

public class RegenerationService {

    private final Path projectRoot;
    private final ProductStore productStore;
    private volatile boolean suppressWatcher;

    public RegenerationService(Path projectRoot, ProductStore productStore) {
        this.projectRoot = projectRoot;
        this.productStore = productStore;
    }

    public boolean isSuppressWatcher() {
        return suppressWatcher;
    }

    public synchronized int regenerate(String reason) throws Exception {
        System.out.println("\n[REGEN] " + reason);
        suppressWatcher = true;
        try {
            return CompilerMain.run(projectRoot);
        } finally {
            Thread.sleep(800);
            suppressWatcher = false;
        }
    }

    public void syncProducts() throws Exception {
        productStore.reloadFromAppPy();
    }
}
