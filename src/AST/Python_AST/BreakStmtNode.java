package AST.Python_AST;



public class BreakStmtNode extends StmtNode {

    public BreakStmtNode(int line) {
        super(line);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "BreakStmtNode");
    }
}