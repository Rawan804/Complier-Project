package AST.Python_AST;

public class IdentifierNode extends ExprNode {

    private String name;

    public IdentifierNode(int line, String name) {
        super(line);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "IdentifierNode");
        System.out.println(indent + "  Name: " + name);
    }
}