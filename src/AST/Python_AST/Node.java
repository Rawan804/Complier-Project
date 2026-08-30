package AST.Python_AST;

public abstract class Node {

    // رقم السطر في الملف المصدر
    protected int line;

    public Node(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    // كل عقدة ستطبع نفسها بطريقتها الخاصة
    public abstract void print(String indent);
}