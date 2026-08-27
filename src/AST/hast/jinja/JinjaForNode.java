package AST.hast.jinja;

import AST.hast.ASTNode;
import AST.webast.WebASTNode;
import java.util.ArrayList;
import java.util.List;

public class JinjaForNode extends ASTNode {
    private String variable;
    private String iterable;
    private List<WebASTNode> children;

    public JinjaForNode(String variable, String iterable, int line, int column) {
        super(line, column);
        this.variable = variable;
        this.iterable = iterable;
        this.children = new ArrayList<>();
    }

    public String getVariable() { return variable; }
    public String getIterable() { return iterable; }

    public void addChild(WebASTNode child) {
        if (child != null) children.add(child);
    }

    public List<WebASTNode> getChildren() { return children; }

    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("{% for " + variable + " in " + iterable + " %} [" + line + ":" + column + "]");
        for (WebASTNode child : children) {
            child.print(indent + 1);
        }
    }

    @Override
    public String getType() { return "JinjaForNode"; }
}