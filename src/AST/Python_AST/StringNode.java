package AST.Python_AST;

public class StringNode extends ExprNode {

    private String value;

    public StringNode(int line, String value) {
        super(line);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "StringNode");
        System.out.println(indent + "  Value: " + value);
    }
}