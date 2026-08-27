package AST.Python_AST;



import java.util.ArrayList;
import java.util.List;

public class DictNode extends ExprNode {

    private List<DictEntry> entries;

    public DictNode(int line, List<DictEntry> entries) {
        super(line);
        this.entries = (entries != null) ? entries : new ArrayList<>();
    }

    public List<DictEntry> getEntries() {
        return entries;
    }

    public void addEntry(DictEntry entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "DictNode");

        for (DictEntry e : entries) {
            System.out.println(indent + "  Key:");
            e.getKey().print(indent + "    ");

            System.out.println(indent + "  Value:");
            e.getValue().print(indent + "    ");
        }}}