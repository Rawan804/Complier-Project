package AST.cssast;

public class StylesheetNode extends AST {
    public StylesheetNode(int lineNumber) {
        super("Stylesheet", lineNumber);
    }

    public void addRuleSet(RuleSetNode ruleSet) {
        addChild(ruleSet);
    }
}
