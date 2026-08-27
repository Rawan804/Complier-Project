package AST.hast;

import AST.webast.WebASTNode;
import java.util.*;

public class ElementNode extends ASTNode {
    private String tagName;
    private Map<String, AttributeNode> attributes;
    private List<WebASTNode> children;
    private boolean isSelfClosing;

    public ElementNode(String tagName, int line, int column) {
        super(line, column);
        this.tagName = tagName;
        this.attributes = new LinkedHashMap<>();
        this.children = new ArrayList<>();
        this.isSelfClosing = false;
    }

    public String getTagName() { return tagName; }
    public Map<String, AttributeNode> getAttributes() { return attributes; }
    public List<WebASTNode> getChildren() { return children; }
    public boolean isSelfClosing() { return isSelfClosing; }
    public void setSelfClosing(boolean selfClosing) { isSelfClosing = selfClosing; }

    public void addAttribute(AttributeNode attr) {
        if (attr != null) attributes.put(attr.getName(), attr);
    }

    public void addChild(WebASTNode child) {
        if (child != null) children.add(child);
    }

    @Override
    public String getType() {
        return "ElementNode:" + tagName;
    }

    @Override
    public void print(int indent) {
        // طباعة المسافات البادئة
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        // طباعة opening tag
        System.out.print("<" + tagName);

        // طباعة attributes
        for (AttributeNode attr : attributes.values()) {
            System.out.print(" " + attr.getName() + "=\"" + attr.getValue() + "\"");
        }

        // إغلاق tag
        if (isSelfClosing || (children.isEmpty() && tagName.equals("br") || tagName.equals("img") || tagName.equals("input"))) {
            System.out.println("/> [" + line + ":" + column + "]");
        } else {
            System.out.println("> [" + line + ":" + column + "]");

            // طباعة children
            for (WebASTNode child : children) {
                if (child != null) {
                    child.print(indent + 1);
                }
            }

            // طباعة closing tag
            for (int i = 0; i < indent; i++) {
                System.out.print("  ");
            }
            System.out.println("</" + tagName + ">");
        }
    }
}