package AST.Python_AST;
import java.util.List;

public class FuncCallNode extends ExprNode {
    private final ExprNode target;
    private final List<ExprNode> args;

    public FuncCallNode(int line, ExprNode target, List<ExprNode> args) {
        super(line);
        this.target = target;
        this.args   = args;
    }

    public ExprNode       getTarget() { return target; }
    public List<ExprNode> getArgs()   { return args; }

    @Override
    public void print(String indent) {
        System.out.println(indent + "FuncCallNode");
        System.out.println(indent + "  Target:");
        if (target != null)
            target.print(indent + "    ");
        if (!args.isEmpty()) {
            System.out.println(indent + "  Args:");
            for (ExprNode arg : args)
                if (arg != null)
                    arg.print(indent + "    ");
        }
    }
}