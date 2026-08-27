package AST.hast.jinja;

public class JinjaStringValueNode extends JinjaValueNode {
    public JinjaStringValueNode(String string, int line, int column) {
        super(string, line, column);
    }

    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("JINJA_STRING: " + getValue() + " [" + line + ":" + column + "]");
    }
}