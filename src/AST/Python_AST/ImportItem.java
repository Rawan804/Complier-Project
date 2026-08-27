package AST.Python_AST;

public class ImportItem {

    private ExprNode name; // dottedName
    private String alias;

    public ImportItem(ExprNode name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public ExprNode getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }
}