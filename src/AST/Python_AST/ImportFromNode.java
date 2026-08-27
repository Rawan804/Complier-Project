package AST.Python_AST;




import java.util.ArrayList;
import java.util.List;

public class ImportFromNode extends StmtNode {

    private ExprNode module;
    private List<ImportItem> items;

    public ImportFromNode(int line,
                          ExprNode module,
                          List<ImportItem> items) {
        super(line);
        this.module = module;
        this.items = (items != null) ? items : new ArrayList<>();
    }

    public ExprNode getModule() {
        return module;
    }

    public List<ImportItem> getItems() {
        return items;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "ImportFromNode");

        System.out.println(indent + "  From:");
        module.print(indent + "    ");

        System.out.println(indent + "  Import:");

        for (ImportItem i : items) {
            i.getName().print(indent + "    ");

            if (i.getAlias() != null) {
                System.out.println(indent + "    AS " + i.getAlias());
            }
        }
    }
}