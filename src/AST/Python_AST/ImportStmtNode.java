package AST.Python_AST;



import java.util.ArrayList;
import java.util.List;

public class ImportStmtNode extends StmtNode {

    private List<ImportItem> items;

    public ImportStmtNode(int line, List<ImportItem> items) {
        super(line);
        this.items = (items != null) ? items : new ArrayList<>();
    }

    public List<ImportItem> getItems() {
        return items;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "ImportStmtNode");

        for (ImportItem i : items) {
            System.out.println(indent + "  Import:");
            i.getName().print(indent + "    ");

            if (i.getAlias() != null) {
                System.out.println(indent + "    AS " + i.getAlias());
            }
        }
    }
}