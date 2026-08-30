package AST.Python_AST;

public class ParamNode extends Node {

    private String name;
    private ExprNode defaultValue; // ممكن يكون null

    public ParamNode(int line, String name, ExprNode defaultValue) {
        super(line);
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public ExprNode getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "ParamNode");
        System.out.println(indent + "  Name: " + name);

        if (defaultValue != null) {
            System.out.println(indent + "  Default:");
            defaultValue.print(indent + "    ");
        }
    }
}