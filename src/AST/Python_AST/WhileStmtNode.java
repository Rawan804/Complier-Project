package AST.Python_AST;

public class WhileStmtNode extends StmtNode {

    private ExprNode condition;
    private BlockStmtNode body;
    private BlockStmtNode elseBody;

    public WhileStmtNode(int line,
                         ExprNode condition,
                         BlockStmtNode body) {
        super(line);
        this.condition = condition;
        this.body = body;
    }

    public void setElseBody(BlockStmtNode elseBody) {
        this.elseBody = elseBody;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "WhileStmtNode");

        System.out.println(indent + "  Condition:");
        condition.print(indent + "    ");

        System.out.println(indent + "  Body:");
        body.print(indent + "    ");

        if (elseBody != null) {
            System.out.println(indent + "  Else:");
            elseBody.print(indent + "    ");
        }
    }

    public ExprNode getCondition()      { return condition; }
    public BlockStmtNode getBody()      { return body; }
    public BlockStmtNode getElseBody()  { return elseBody; }
}