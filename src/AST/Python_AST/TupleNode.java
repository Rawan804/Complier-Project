package AST.Python_AST;

import java.util.ArrayList;
import java.util.List;

public class TupleNode extends ExprNode {

    private List<ExprNode> items;

    public TupleNode(int line, List<ExprNode> items) {
        super(line);
        this.items = (items != null) ? items : new ArrayList<>();
    }

    public List<ExprNode> getItems() {
        return items;
    }

    public void setItems(List<ExprNode> items) {
        this.items = items;
    }

    public void addItem(ExprNode item) {
        if (item != null) {
            items.add(item);
        }
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "TupleNode");

        for (ExprNode item : items) {
            item.print(indent + "  ");
        }
    }
}