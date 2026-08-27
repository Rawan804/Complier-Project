package AST.cssast;

public class ElementNameNode extends AST {
    public String name;

    public ElementNameNode(String name, int lineNumber) {
        super("ElementName", lineNumber);
        this.name = name;
    }

    @Override
    public void print(int indent) {
        String prefix = " ".repeat(indent * 2);
        System.out.println(prefix + nodeName + ": " + name + " (line: " + lineNumber + ")");
    }
}
