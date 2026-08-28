package AST.Python_AST;

import java.util.ArrayList;
import java.util.List;
public class LambdaExprNode extends ExprNode {

    private List<ExprNode> params;
    private ExprNode body;

    public LambdaExprNode(int line,
                          List<ExprNode> params,
                          ExprNode body) {
        super(line);
        this.params = (params != null) ? params : new ArrayList<>();
        this.body = body;
    }

    public List<ExprNode> getParams() {
        return params;
    }

    public void setParams(List<ExprNode> params) {
        this.params = params;
    }

    public ExprNode getBody() {
        return body;
    }

    public void setBody(ExprNode body) {
        this.body = body;
    }

    @Override
    public void print(String indent) {

        System.out.println(indent + "LambdaExprNode");

        System.out.println(indent + "  Params:");
        for (ExprNode p : params) {
            p.print(indent + "    ");
        }

        System.out.println(indent + "  Body:");
        if (body != null) {
            body.print(indent + "    ");
        }}}