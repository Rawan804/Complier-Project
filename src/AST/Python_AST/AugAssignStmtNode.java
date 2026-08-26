package AST.Python_AST;



public class AugAssignStmtNode extends StmtNode {

    private ExprNode target;
    private String operator;
    private ExprNode value;

    public AugAssignStmtNode(int line,
                             ExprNode target,
                             String operator,
                             ExprNode value) {
        super(line);
        this.target = target;
        this.operator = operator;
        this.value = value;
    }

    public ExprNode getTarget() {
        return target;
    }

    public void setTarget(ExprNode target) {
        this.target = target;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public ExprNode getValue() {
        return value;
    }

    public void setValue(ExprNode value) {
        this.value = value;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "AugAssignStmtNode");

        System.out.println(indent + "  Target:");
        if (target != null) {
            target.print(indent + "    ");
        }

        System.out.println(indent + "  Operator: " + operator);

        System.out.println(indent + "  Value:");
        if (value != null) {
            value.print(indent + "    ");
        }
    }
}