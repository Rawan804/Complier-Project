package AST.Python_AST;

import java.util.ArrayList;
import java.util.List;

public class ForStmtNode extends StmtNode {

    private List<ExprNode> targets = new ArrayList<>();
    private ExprNode iterable;
    private BlockStmtNode body;

    public ForStmtNode(int line,
                       List<ExprNode> targets,
                       ExprNode iterable,
                       BlockStmtNode body) {
        super(line);
        this.targets = targets;
        this.iterable = iterable;
        this.body = body;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "ForStmtNode");

        System.out.println(indent + "  Targets:");
        for (ExprNode t : targets) {
            t.print(indent + "    ");
        }

        System.out.println(indent + "  Iterable:");
        iterable.print(indent + "    ");

        System.out.println(indent + "  Body:");
        body.print(indent + "    ");
    }
    // في ForStmtNode.java — أضف:
    public List<ExprNode> getTargets()  { return targets; }
    public ExprNode getIterable()       { return iterable; }
    public BlockStmtNode getBody()      { return body; }
}