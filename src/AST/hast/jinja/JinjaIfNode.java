package AST.hast.jinja;

import AST.hast.ASTNode;  // ✅ غير import
import java.util.ArrayList;
import java.util.List;

public class JinjaIfNode extends ASTNode {  // ✅ غير extends
    private String condition;
    private List<ASTNode> thenBlock;
    private List<ASTNode> elseBlock;

    public JinjaIfNode(String condition, int line, int column) {
        super(line, column);  // ✅ غير
        this.condition = condition;
        this.thenBlock = new ArrayList<>();
        this.elseBlock = new ArrayList<>();
    }

    public String getCondition() { return condition; }

    public void addThenChild(ASTNode node) {
        if (node != null) thenBlock.add(node);
    }

    public void addElseChild(ASTNode node) {
        if (node != null) elseBlock.add(node);
    }

    public List<ASTNode> getThenBlock() { return thenBlock; }
    public List<ASTNode> getElseBlock() { return elseBlock; }

    @Override
    public void print(int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("{% if " + condition + " %} [" + line + ":" + column + "]");
        for (ASTNode child : thenBlock) {
            child.print(indent + 1);
        }
        if (!elseBlock.isEmpty()) {
            for (int i = 0; i < indent; i++) System.out.print("  ");
            System.out.println("{% else %}");
            for (ASTNode child : elseBlock) {
                child.print(indent + 1);
            }
        }
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println("{% endif %}");
    }
    public void addChild(ASTNode node) {
        addThenChild(node);
    }

    @Override
    public String getType() { return "JinjaIfNode"; }
}