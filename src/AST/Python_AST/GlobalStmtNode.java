package AST.Python_AST;



import java.util.ArrayList;
import java.util.List;

public class GlobalStmtNode extends StmtNode {

    private List<String> names;

    public GlobalStmtNode(int line, List<String> names) {
        super(line);
        this.names = (names != null) ? names : new ArrayList<>();
    }

    public List<String> getNames() {
        return names;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "GlobalStmtNode");

        for (String name : names) {
            System.out.println(indent + "  Name: " + name);
        }
    }
}