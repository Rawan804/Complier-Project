package AST.Python_AST;



public class ContinueStmtNode extends StmtNode {

    public ContinueStmtNode(int line) {
        super(line);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "ContinueStmtNode");
    }
}