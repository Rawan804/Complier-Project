package AST.Python_AST;

public class NumberNode extends ExprNode {

    private String value;

    public NumberNode(int line, String value) {
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
        System.out.println(indent + "NumberNode");
        System.out.println(indent + "  Value: " + value);
    }
}