package AST.cssast;

public class SelectorGroupNode extends AST {
    public SelectorGroupNode(int lineNumber) {
        super("SelectorGroup", lineNumber);
    }

    public void addSelector(SelectorNode selector) {
        addChild(selector);
    }
}
