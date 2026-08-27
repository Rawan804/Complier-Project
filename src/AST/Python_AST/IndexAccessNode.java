package AST.Python_AST;

public class IndexAccessNode extends ExprNode {

    private ExprNode target;
    private ExprNode index;

    public IndexAccessNode(int line,
                           ExprNode target,
                           ExprNode index) {
        super(line);
        this.target = target;
        this.index = index;
    }

    public ExprNode getTarget() {
        return target;
    }

    public void setTarget(ExprNode target) {
        this.target = target;
    }

    public ExprNode getIndex() {
        return index;
    }

    public void setIndex(ExprNode index) {
        this.index = index;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "IndexAccessNode");

        if (target != null) {
            System.out.println(indent + "  Target:");
            target.print(indent + "    ");
        }

        if (index != null) {
            System.out.println(indent + "  Index:");
            index.print(indent + "    ");
        }
    }
}