package AST.Python_AST;

import java.util.ArrayList;
import java.util.List;

public class FunctionDefNode extends StmtNode {
    private String name;
    // parameters
    private List<ParamNode> params;
    // function body
    private BlockStmtNode body;
    // decorators (@...)
    private List<DecoratorNode> decorators;
    public FunctionDefNode(int line,String name,List<ParamNode> params, BlockStmtNode body, List<DecoratorNode> decorators) {
        super(line);
        this.name = name;
        this.params = (params != null) ? params : new ArrayList<>();
        this.body = body;
        this.decorators = (decorators != null) ? decorators : new ArrayList<>();
    }
    // =========================
    // Getters
    // =========================
    public String getName() {
        return name;
    }
    public List<ParamNode> getParams() {
        return params;
    }
    public BlockStmtNode getBody() {
        return body;
    }

    public List<DecoratorNode> getDecorators() {
        return decorators;
    }
    public void setBody(BlockStmtNode body) {
        this.body = body;
    }
    public void addParam(ParamNode param) {
        this.params.add(param);
    }
    public void addDecorator(DecoratorNode decorator) {
        this.decorators.add(decorator);
    }
    @Override
    public void print(String indent) {
        System.out.println(indent + "FunctionDefNode");
        // name
        System.out.println(indent + "  Name: " + name);
        // decorators
        if (!decorators.isEmpty()) {
            System.out.println(indent + "  Decorators:");
            for (DecoratorNode d : decorators) {
                d.print(indent + "    ");
            }
        }
        // params
        System.out.println(indent + "  Params:");
        for (ParamNode p : params) {
            p.print(indent + "    ");
        }
        // body
        System.out.println(indent + "  Body:");
        if (body != null) {
            body.print(indent + "    ");
        }
        }
        }