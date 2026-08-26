package AST.Python_AST;



import java.util.ArrayList;
import java.util.List;

public class BlockStmtNode extends StmtNode {
    private List<StmtNode> statements;

    public BlockStmtNode(int line) {
        super(line);
        this.statements = new ArrayList<>();
    }

    public BlockStmtNode(int line, List<StmtNode> statements) {
        super(line);
        this.statements = (statements != null) ? statements : new ArrayList<>();
    }

    public List<StmtNode> getStatements() { return statements; }

    public void addStatement(StmtNode stmt) {
        if (stmt != null) statements.add(stmt);
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "BlockStmtNode");
        for (StmtNode s : statements) {
            s.print(indent + "  ");
        }
    }
}