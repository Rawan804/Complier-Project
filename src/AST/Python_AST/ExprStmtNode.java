package AST.Python_AST;



public class ExprStmtNode extends StmtNode {

    private ExprNode expression;

    public ExprStmtNode(int line, ExprNode expression) {
        super(line);
        this.expression = expression;
    }

    public ExprNode getExpression() {
        return expression;
    }

    public void setExpression(ExprNode expression) {
        this.expression = expression;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "ExprStmtNode");

        if (expression != null) {
            expression.print(indent + "  ");
        }
    }
}