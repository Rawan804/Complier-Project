package AST.hast;

public class TextNode extends ASTNode {
    private String text;

    public TextNode(String text, int line, int column) {
        super(line, column);
        this.text = text;
    }

    public String getText() { return text; }

    @Override
    public String getType() { return "TextNode"; }

    // ✅ أضف print
    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        String displayText = text.length() > 50 ? text.substring(0, 47) + "..." : text;
        System.out.println("TEXT: \"" + displayText + "\" [" + line + ":" + column + "]");
    }
}