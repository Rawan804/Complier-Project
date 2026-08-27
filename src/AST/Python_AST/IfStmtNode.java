package AST.Python_AST;



import java.util.ArrayList;
import java.util.List;

public class IfStmtNode extends StmtNode {

    public static class IfBranch {
        public ExprNode condition;
        public BlockStmtNode body;

        public IfBranch(ExprNode condition, BlockStmtNode body) {
            this.condition = condition;
            this.body = body;
        }
    }

    private List<IfBranch> elifBranches = new ArrayList<>();
    private ExprNode ifCondition;
    private BlockStmtNode ifBody;
    private BlockStmtNode elseBody;

    public IfStmtNode(int line,
                      ExprNode ifCondition,
                      BlockStmtNode ifBody) {
        super(line);
        this.ifCondition = ifCondition;
        this.ifBody = ifBody;
    }

    public void addElif(ExprNode cond, BlockStmtNode body) {
        elifBranches.add(new IfBranch(cond, body));
    }

    public void setElse(BlockStmtNode elseBody) {
        this.elseBody = elseBody;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "IfStmtNode");

        System.out.println(indent + "  IF:");
        System.out.println(indent + "    Condition:");
        ifCondition.print(indent + "      ");
        System.out.println(indent + "    Body:");
        ifBody.print(indent + "      ");

        for (IfBranch b : elifBranches) {
            System.out.println(indent + "  ELIF:");
            b.condition.print(indent + "    ");
            b.body.print(indent + "    ");
        }

        if (elseBody != null) {
            System.out.println(indent + "  ELSE:");
            elseBody.print(indent + "    ");
        }
    }
    // في IfStmtNode.java — أضف:
    public ExprNode getIfCondition()         { return ifCondition; }
    public BlockStmtNode getIfBody()         { return ifBody; }
    public BlockStmtNode getElseBody()       { return elseBody; }
    public List<IfBranch> getElifBranches() { return elifBranches; }
}