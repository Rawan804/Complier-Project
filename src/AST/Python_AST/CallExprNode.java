package AST.Python_AST;


import java.util.ArrayList;
import java.util.List;

public class CallExprNode extends ExprNode {

    private ExprNode function;
    private List<ExprNode> arguments;

    public CallExprNode(int line,
                        ExprNode function,
                        List<ExprNode> arguments) {

        super(line);

        this.function = function;

        this.arguments =
                (arguments != null)
                        ? arguments
                        : new ArrayList<>();
    }

    public ExprNode getFunction() {
        return function;
    }

    public void setFunction(ExprNode function) {
        this.function = function;
    }

    public List<ExprNode> getArguments() {
        return arguments;
    }

    public void setArguments(List<ExprNode> arguments) {
        this.arguments = arguments;
    }

    public void addArgument(ExprNode arg) {
        if (arg != null) {
            arguments.add(arg);
        }
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "CallExprNode");

        if (function != null) {
            System.out.println(indent + "  Function:");
            function.print(indent + "    ");
        }

        System.out.println(indent + "  Arguments:");

        for (ExprNode arg : arguments) {
            arg.print(indent + "    ");
        }
    }
}