import compiler.CompilerMain;

import java.nio.file.Path;


public class RegenOnce {
    public static void main(String[] args) throws Exception {
        Path root = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        System.exit(CompilerMain.run(root));
    }
}
