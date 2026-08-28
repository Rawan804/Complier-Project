package AST.Python_AST;

public class TernaryExprNode extends ExprNode {

    private ExprNode condition;
    private ExprNode trueExpr;
    private ExprNode falseExpr;

    public TernaryExprNode(int line,
                           ExprNode condition,
                           ExprNode trueExpr,
                           ExprNode falseExpr) {

        super(line);
        this.condition = condition;
        this.trueExpr = trueExpr;
        this.falseExpr = falseExpr;
    }

    public ExprNode getCondition() {
        return condition;
    }

    public void setCondition(ExprNode condition) {
        this.condition = condition;
    }

    public ExprNode getTrueExpr() {
        return trueExpr;
    }

    public void setTrueExpr(ExprNode trueExpr) {
        this.trueExpr = trueExpr;
    }

    public ExprNode getFalseExpr() {
        return falseExpr;
    }

    public void setFalseExpr(ExprNode falseExpr) {
        this.falseExpr = falseExpr;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "TernaryExprNode");

        System.out.println(indent + "  Condition:");
        if (condition != null) condition.print(indent + "    ");

        System.out.println(indent + "  True:");
        if (trueExpr != null) trueExpr.print(indent + "    ");

        System.out.println(indent + "  False:");
        if (falseExpr != null) falseExpr.print(indent + "    ");
    }
}