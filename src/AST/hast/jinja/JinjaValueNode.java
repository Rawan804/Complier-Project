package AST.hast.jinja;

import AST.hast.ASTNode;

public class JinjaValueNode extends ASTNode {
    protected String value;
    protected String valueType;

    // ✅ Constructor الصحيح
    public JinjaValueNode(String value, int line, int column) {
        super(line, column);  // استخدم super(line, column) فقط
        this.value = value;
        this.valueType = "unknown";
    }

    // Constructor إضافي مع type
    public JinjaValueNode(String value, String valueType, int line, int column) {
        super(line, column);
        this.value = value;
        this.valueType = valueType;
    }

    public String getValue() { return value; }
    public String getValueType() { return valueType; }

    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("JINJA_VALUE: " + value + " (" + valueType + ") [" + line + ":" + column + "]");
    }

    @Override
    public String getType() {
        return "JinjaValueNode";
    }
}