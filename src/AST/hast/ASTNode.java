package AST.hast;

import AST.webast.WebASTNode;

public abstract class ASTNode extends WebASTNode {
    protected int line;
    protected int column;

    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() { return line; }
    public int getColumn() { return column; }

    // هذه دوال مجردة يجب تنفيذها في كل الكلاسات الموروثة
    public abstract String getType();
    public abstract void print(int indent);
}