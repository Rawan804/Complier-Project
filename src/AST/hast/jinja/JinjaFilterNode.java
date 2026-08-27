package AST.hast.jinja;

import AST.hast.ASTNode;

public class JinjaFilterNode extends ASTNode {
    private final String filterName;

    public JinjaFilterNode(String filterName, int line, int column) {
        super(line, column);  // ✅ غير
        this.filterName = filterName;
    }

    public String getFilterName() { return filterName; }

    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("| " + filterName);
    }

    @Override
    public String getType() { return "JinjaFilterNode"; }
}