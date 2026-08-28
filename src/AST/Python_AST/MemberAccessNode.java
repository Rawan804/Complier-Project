package AST.Python_AST;

public class MemberAccessNode extends ExprNode {

    private ExprNode object;
    private String member;

    public MemberAccessNode(int line,
                            ExprNode object,
                            String member) {
        super(line);
        this.object = object;
        this.member = member;
    }

    public ExprNode getObject() {
        return object;
    }

    public void setObject(ExprNode object) {
        this.object = object;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "MemberAccessNode");
        System.out.println(indent + "  Member: " + member);

        if (object != null) {
            System.out.println(indent + "  Object:");
            object.print(indent + "    ");
        }
    }
}