package AST.Python_AST;



import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends Node {

    private List<StmtNode> statements;

    public ProgramNode() {
        super(0);
        this.statements = new ArrayList<>();
    }

    public ProgramNode(List<StmtNode> statements) {
        super(0);
        this.statements = (statements != null)
                ? statements
                : new ArrayList<>();
    }

    public void addStatement(StmtNode stmt) {
        if (stmt != null) {
            statements.add(stmt);
        }
    }

    public List<StmtNode> getStatements() {
        return statements;
    }

    public void setStatements(List<StmtNode> statements) {
        this.statements = statements;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "ProgramNode");

        for (StmtNode stmt : statements) {
            if (stmt != null) {
                stmt.print(indent + "  ");
            }
        }
    }
}