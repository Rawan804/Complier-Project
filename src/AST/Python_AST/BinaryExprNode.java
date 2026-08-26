package AST.Python_AST;



public class BinaryExprNode extends ExprNode {

    private ExprNode left;
    private String operator;
    private ExprNode right;

    public BinaryExprNode(int line,
                          ExprNode left,
                          String operator,
                          ExprNode right) {
        super(line);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ExprNode getLeft() {
        return left;
    }

    public void setLeft(ExprNode left) {
        this.left = left;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public ExprNode getRight() {
        return right;
    }

    public void setRight(ExprNode right) {
        this.right = right;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "BinaryExprNode");
        System.out.println(indent + "  Operator: " + operator);

        if (left != null) {
            System.out.println(indent + "  Left:");
            left.print(indent + "    ");
        }

        if (right != null) {
            System.out.println(indent + "  Right:");
            right.print(indent + "    ");
        }
    }
}