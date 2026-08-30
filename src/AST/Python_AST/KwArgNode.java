package AST.Python_AST;
public class KwArgNode extends ExprNode {
    private String name;
    private ExprNode value;

    public KwArgNode(int line, String name, ExprNode value) {
        super(line);
        this.name = name;
        this.value = value;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ExprNode getValue() { return value; }
    public void setValue(ExprNode value) { this.value = value; }

    @Override
    public void print(String indent) {
        System.out.println(indent + "KwArgNode");
        System.out.println(indent + "  Name: " + name);
        System.out.println(indent + "  Value:");
        if (value != null) value.print(indent + "    ");
    }
}