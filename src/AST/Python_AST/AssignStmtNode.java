package AST.Python_AST;



public class AssignStmtNode extends StmtNode {

    private ExprNode target;
    private ExprNode value;

    public AssignStmtNode(int line,
                          ExprNode target,
                          ExprNode value) {

        super(line);
        this.target = target;
        this.value = value;
    }

    public ExprNode getTarget() {
        return target;
    }

    public void setTarget(ExprNode target) {
        this.target = target;
    }

    public ExprNode getValue() {
        return value;
    }

    public void setValue(ExprNode value) {
        this.value = value;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "AssignStmtNode");

        System.out.println(indent + "  Target:");

        if (target != null) {
            target.print(indent + "    ");
        }

        System.out.println(indent + "  Value:");

        if (value != null) {
            value.print(indent + "    ");
        }
    }
}