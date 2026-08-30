package AST.webast;

public abstract class WebASTNode {

    protected int line;
    protected int column;

    public int getLine() { return line; }
    public int getColumn() { return column; }

    public abstract void print(int indent);
}