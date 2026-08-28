package AST.Python_AST;



public class ReturnStmtNode extends StmtNode {

    private ExprNode value;

    public ReturnStmtNode(int line) {
        super(line);
    }

    public ReturnStmtNode(int line, ExprNode value) {
        super(line);
        this.value = value;
    }

    public ExprNode getValue() {
        return value;
    }

    public void setValue(ExprNode value) {
        this.value = value;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "ReturnStmtNode");

        if (value != null) {

            System.out.println(indent + "  Value:");

            value.print(indent + "    ");
        }
    }
}