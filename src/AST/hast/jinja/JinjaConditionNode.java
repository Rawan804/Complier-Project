package AST.hast.jinja;

import AST.hast.ASTNode;

public class JinjaConditionNode extends ASTNode {
    private final String left;
    private final String operator;
    private final String right;

    public JinjaConditionNode(String left, String operator, String right, int line, int column) {
        super(line, column);  // ✅ غير من super("TYPE", line, column) إلى super(line, column)
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public String getLeft() { return left; }
    public String getOperator() { return operator; }
    public String getRight() { return right; }

    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("CONDITION: " + left + " " + operator + " " + right);
    }

    @Override
    public String getType() { return "JinjaConditionNode"; }
}