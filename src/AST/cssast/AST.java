package AST.cssast;

import AST.webast.WebASTNode;
import java.util.ArrayList;
import java.util.List;

public abstract class AST extends WebASTNode {
    protected String nodeName;
    protected int lineNumber;
    protected List<AST> children;

    public AST(String nodeName, int lineNumber) {
        this.nodeName = nodeName;
        this.lineNumber = lineNumber;
        this.children = new ArrayList<>();
    }

    public void addChild(AST child) {
        if (child != null) {
            children.add(child);
        }
    }

    public List<AST> getChildren() {
        return children;
    }

    public String getNodeName() {
        return nodeName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    // Polymorphic print method
    public void print(int indent) {
        String prefix = " ".repeat(indent * 2);
        System.out.println(prefix + nodeName + " (line: " + lineNumber + ")");
        for (AST child : children) {
            child.print(indent + 1);
        }
    }
}
