package AST.hast;

public class AttributeNode extends ASTNode {
    private String name;
    private String value;
    private boolean isJinjaExpr;

    public AttributeNode(String name, String value, int line, int column) {
        super(line, column);
        this.name = name;
        this.value = value;
        this.isJinjaExpr = false;
    }

    public AttributeNode(String name, String value, boolean isJinjaExpr, int line, int column) {
        super(line, column);
        this.name = name;
        this.value = value;
        this.isJinjaExpr = isJinjaExpr;
    }

    public String getName() { return name; }
    public String getValue() { return value; }
    public boolean isJinjaExpr() { return isJinjaExpr; }

    @Override
    public String getType() { return "AttributeNode"; }

    // ✅ أضف هذه الدالة المفقودة
    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("ATTRIBUTE: " + name + " = " + value + (isJinjaExpr ? " (jinja)" : "") + " [" + line + ":" + column + "]");
    }
}