package AST.hast.jinja;

public class JinjaNumberValueNode extends JinjaValueNode {
    public JinjaNumberValueNode(String number, int line, int column) {
        super(number, line, column);
    }

    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("JINJA_NUMBER: " + getValue() + " [" + line + ":" + column + "]");
    }
}