package AST.Python_AST;

import java.util.ArrayList;
import java.util.List;


public class RangeNode extends ExprNode {
    private List<ExprNode> args;

    public RangeNode(int line, List<ExprNode> args) {
        super(line);
        this.args = (args != null) ? args : new ArrayList<>();
    }

    public List<ExprNode> getArgs() { return args; }

    @Override
    public void print(String indent) {
        System.out.println(indent + "RangeNode");
        System.out.println(indent + "  Args:");
        for (ExprNode a : args) a.print(indent + "    ");
    }
}