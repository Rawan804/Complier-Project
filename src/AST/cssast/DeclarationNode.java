
package AST.cssast;

public class DeclarationNode extends AST {
    private String property;
    private String value;

    public DeclarationNode(String property, String value, int lineNumber) {
        super("Declaration", lineNumber);
        this.property = property;
        this.value = value;
    }

    public String getProperty() { return property; }
    public String getValue() { return value; }

    @Override
    public void print(int indent) {
        String prefix = " ".repeat(indent * 2);
        System.out.println(prefix + property + " : " + value);
    }
}