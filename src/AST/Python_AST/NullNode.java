package AST.Python_AST;

public class NullNode extends ExprNode {

    public NullNode(int line) {
        super(line);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "NullNode");
    }
}