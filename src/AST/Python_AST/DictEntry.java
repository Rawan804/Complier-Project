package AST.Python_AST;



public class DictEntry {

    private ExprNode key;
    private ExprNode value;

    public DictEntry(ExprNode key, ExprNode value) {
        this.key = key;
        this.value = value;
    }

    public ExprNode getKey() {
        return key;
    }

    public ExprNode getValue() {
        return value;
    }
}