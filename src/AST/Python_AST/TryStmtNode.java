package AST.Python_AST;

import java.util.ArrayList;
import java.util.List;

public class TryStmtNode extends StmtNode {

    public static class ExceptBlock {
        private ExprNode type;
        private String alias;
        private BlockStmtNode body;

        public ExceptBlock(ExprNode type, String alias, BlockStmtNode body) {
            this.type = type;
            this.alias = alias;
            this.body = body;
        }

        public ExprNode getType()      { return type; }
        public String getAlias()       { return alias; }
        public BlockStmtNode getBody() { return body; }
    }

    private BlockStmtNode tryBody;
    private List<ExceptBlock> exceptBlocks = new ArrayList<>();
    private BlockStmtNode elseBody;
    private BlockStmtNode finallyBody;

    public TryStmtNode(int line, BlockStmtNode tryBody) {
        super(line);
        this.tryBody = tryBody;
    }

    public void addExcept(ExprNode type, String alias, BlockStmtNode body) {
        exceptBlocks.add(new ExceptBlock(type, alias, body));
    }

    public void setElseBody(BlockStmtNode elseBody) {
        this.elseBody = elseBody;
    }

    public void setFinallyBody(BlockStmtNode finallyBody) {
        this.finallyBody = finallyBody;
    }

    public BlockStmtNode getTryBody()          { return tryBody; }
    public List<ExceptBlock> getExceptBlocks() { return exceptBlocks; }
    public BlockStmtNode getElseBody()         { return elseBody; }
    public BlockStmtNode getFinallyBody()      { return finallyBody; }

    @Override
    public void print(String indent) {

        System.out.println(indent + "TryStmtNode");

        System.out.println(indent + "  Try:");
        tryBody.print(indent + "    ");

        for (ExceptBlock e : exceptBlocks) {
            System.out.println(indent + "  Except:");

            if (e.type != null) {
                System.out.println(indent + "    Type:");
                e.type.print(indent + "      ");
            }

            if (e.alias != null) {
                System.out.println(indent + "    Alias: " + e.alias);
            }

            if (e.body != null) {
                System.out.println(indent + "    Body:");
                e.body.print(indent + "      ");
            }
        }

        if (elseBody != null) {
            System.out.println(indent + "  Else:");
            elseBody.print(indent + "    ");
        }

        if (finallyBody != null) {
            System.out.println(indent + "  Finally:");
            finallyBody.print(indent + "    ");
        }
    }
}