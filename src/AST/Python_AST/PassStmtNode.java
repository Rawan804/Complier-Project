package AST.Python_AST;



public class PassStmtNode extends StmtNode {

    public PassStmtNode(int line) {
        super(line);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "PassStmtNode");
    }
}