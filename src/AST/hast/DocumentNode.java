package AST.hast;

import AST.webast.WebASTNode;
import java.util.ArrayList;
import java.util.List;

public class DocumentNode extends ASTNode {
    private String doctype;
    private ElementNode htmlElement;
    private List<WebASTNode> nodes;

    public DocumentNode(int line, int column) {
        super(line, column);
        this.nodes = new ArrayList<>();
    }

    public String getDoctype() { return doctype; }
    public void setDoctype(String doctype) { this.doctype = doctype; }

    public ElementNode getHtmlElement() { return htmlElement; }
    public void setHtmlElement(ElementNode htmlElement) { this.htmlElement = htmlElement; }

    public void addNode(WebASTNode node) {
        if (node != null) nodes.add(node);
    }
    public List<WebASTNode> getNodes() { return nodes; }

    @Override
    public String getType() { return "DocumentNode"; }

    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("DOCUMENT [" + line + ":" + column + "]");

        if (doctype != null) {
            for (int i = 0; i < indent + 1; i++) System.out.print("  ");
            System.out.println("DOCTYPE: " + doctype);
        }

        for (WebASTNode node : nodes) {
            if (node != null) node.print(indent + 1);
        }
    }
}