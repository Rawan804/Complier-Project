package AST.Python_AST;

import java.util.ArrayList;
import java.util.List;

public class WithStmtNode extends StmtNode {

    public static class WithItem {
        public ExprNode context;
        public String alias;

        public WithItem(ExprNode context, String alias) {
            this.context = context;
            this.alias = alias;
        }

        public ExprNode getContext() { return context; }
        public String getAlias()    { return alias; }
    }

    private List<WithItem> items = new ArrayList<>();
    private BlockStmtNode body;

    public WithStmtNode(int line, BlockStmtNode body) {
        super(line);
        this.body = body;
    }

    public void addItem(ExprNode ctx, String alias) {
        items.add(new WithItem(ctx, alias));
    }

    public List<WithItem> getItems() { return items; }
    public BlockStmtNode getBody()   { return body; }

    @Override
    public void print(String indent) {

        System.out.println(indent + "WithStmtNode");

        for (WithItem i : items) {
            System.out.println(indent + "  Context:");
            i.context.print(indent + "    ");
        }

        System.out.println(indent + "  Body:");
        body.print(indent + "    ");
    }
}