package AST.Python_AST;


import java.util.ArrayList;
import java.util.List;
public class ClassDefNode extends StmtNode {

    private String name;
    private List<ExprNode> bases;
    private BlockStmtNode body;

    public ClassDefNode(
            int line,
            String name,
            List<ExprNode> bases,
            BlockStmtNode body) {

        super(line);

        this.name = name;
        this.bases = (bases != null)
                ? bases
                : new ArrayList<>();

        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<ExprNode> getBases() {
        return bases;
    }

    public BlockStmtNode getBody() {
        return body;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "ClassDefNode");
        System.out.println(indent + "  Name: " + name);

        if (!bases.isEmpty()) {
            System.out.println(indent + "  Bases:");

            for (ExprNode b : bases) {
                b.print(indent + "    ");
            }
        }

        System.out.println(indent + "  Body:");

        if (body != null) {
            body.print(indent + "    ");
        }
    }
}