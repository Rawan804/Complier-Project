package AST.cssast;

public class SimpleSelectorNode extends AST {
    public SimpleSelectorNode(int lineNumber) {
        super("SimpleSelector", lineNumber);
    }

    public void setElementName(ElementNameNode elementName) {
        addChild(elementName);
    }

    public void addModifier(SelectorModifierNode modifier) {
        addChild(modifier);
    }
}
