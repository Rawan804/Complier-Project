package AST.Python_AST;



import java.util.ArrayList;
import java.util.List;

public class DecoratorNode extends Node {

    private ExprNode name; // dottedName
    private List<ExprNode> args;

    public DecoratorNode(int line,
                         ExprNode name,
                         List<ExprNode> args) {
        super(line);
        this.name = name;
        this.args = (args != null) ? args : new ArrayList<>();
    }

    public ExprNode getName() {
        return name;
    }

    public List<ExprNode> getArgs() {
        return args;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "DecoratorNode");

        System.out.println(indent + "  Name:");
        name.print(indent + "    ");

        if (!args.isEmpty()) {
            System.out.println(indent + "  Args:");
            for (ExprNode a : args) {
                a.print(indent + "    ");
            }}}}