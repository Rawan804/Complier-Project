package AST.Python_AST;



public class BooleanNode extends ExprNode {

    private boolean value;

    public BooleanNode(int line, boolean value) {
        super(line);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "BooleanNode");
        System.out.println(indent + "  Value: " + value);
    }
}