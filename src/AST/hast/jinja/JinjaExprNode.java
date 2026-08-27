package AST.hast.jinja;

import AST.hast.ASTNode;

public class JinjaExprNode extends ASTNode {
    private String expression;

    public JinjaExprNode(String expression, int line, int column) {
        super(line, column);  // ✅ استخدم super(line, column)
        this.expression = expression;
    }

    public String getExpression() { return expression; }

    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("JINJA_EXPR: {{ " + expression + " }} [" + line + ":" + column + "]");
    }

    @Override
    public String getType() {
        return "JinjaExprNode";
    }
}