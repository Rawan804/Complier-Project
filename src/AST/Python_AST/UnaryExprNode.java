package AST.Python_AST;

public class UnaryExprNode extends ExprNode {

    private String operator;
    private ExprNode operand;

    public UnaryExprNode(int line,
                         String operator,
                         ExprNode operand) {
        super(line);
        this.operator = operator;
        this.operand = operand;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public ExprNode getOperand() {
        return operand;
    }

    public void setOperand(ExprNode operand) {
        this.operand = operand;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "UnaryExprNode");
        System.out.println(indent + "  Operator: " + operator);

        if (operand != null) {
            System.out.println(indent + "  Operand:");
            operand.print(indent + "    ");
        }
    }
}