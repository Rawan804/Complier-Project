package AST.cssast;

public class RuleSetNode extends AST {
    private String selectorText = "";

    public RuleSetNode(int lineNumber) {
        super("RuleSet", lineNumber);
    }

    public void setSelectorText(String selectorText) {
        this.selectorText = selectorText;
    }

    public String getSelectorText() {
        return selectorText;
    }

    public void setSelectorGroup(SelectorGroupNode selectorGroup) {
        addChild(selectorGroup);
    }

    public void addDeclaration(DeclarationNode declaration) {
        addChild(declaration);
    }
}
