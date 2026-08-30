package codegenerate.GenerateCode;

import AST.Python_AST.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RenderTemplateCallFinder {


    public static class RenderTemplateCall {
        public String routeName;
        public String templateFile;
        public Map<String, String> contextVars = new LinkedHashMap<>();


        @Override
        public String toString() {
            return "route '" + routeName + "' -> template '" + templateFile + "' مع context: " + contextVars;
        }
    }


    public List<RenderTemplateCall> find(ProgramNode program) {
        List<RenderTemplateCall> results = new ArrayList<>();

        for (StmtNode stmt : program.getStatements()) {
            if (stmt instanceof FunctionDefNode) {
                FunctionDefNode route = (FunctionDefNode) stmt;
                collectFromStmt(route.getBody(), route.getName(), results);
            }
        }
        return results;
    }


    private void collectFromStmt(StmtNode stmt, String routeName, List<RenderTemplateCall> out) {
        if (stmt == null) return;

        if (stmt instanceof BlockStmtNode) {
            for (StmtNode s : ((BlockStmtNode) stmt).getStatements()) collectFromStmt(s, routeName, out);

        } else if (stmt instanceof ReturnStmtNode) {
            collectFromExpr(((ReturnStmtNode) stmt).getValue(), routeName, out);

        } else if (stmt instanceof ExprStmtNode) {
            collectFromExpr(((ExprStmtNode) stmt).getExpression(), routeName, out);

        } else if (stmt instanceof AssignStmtNode) {
            collectFromExpr(((AssignStmtNode) stmt).getValue(), routeName, out);

        } else if (stmt instanceof IfStmtNode) {
            IfStmtNode ifs = (IfStmtNode) stmt;
            collectFromStmt(ifs.getIfBody(), routeName, out);
            for (IfStmtNode.IfBranch branch : ifs.getElifBranches()) {
                collectFromStmt(branch.body, routeName, out);
            }
            if (ifs.getElseBody() != null) collectFromStmt(ifs.getElseBody(), routeName, out);

        } else if (stmt instanceof ForStmtNode) {
            collectFromStmt(((ForStmtNode) stmt).getBody(), routeName, out);

        } else if (stmt instanceof WhileStmtNode) {
            WhileStmtNode w = (WhileStmtNode) stmt;
            collectFromStmt(w.getBody(), routeName, out);
            if (w.getElseBody() != null) collectFromStmt(w.getElseBody(), routeName, out);

        } else if (stmt instanceof WithStmtNode) {
            collectFromStmt(((WithStmtNode) stmt).getBody(), routeName, out);

        } else if (stmt instanceof TryStmtNode) {
            TryStmtNode t = (TryStmtNode) stmt;
            collectFromStmt(t.getTryBody(), routeName, out);
            for (TryStmtNode.ExceptBlock e : t.getExceptBlocks()) collectFromStmt(e.getBody(), routeName, out);
            if (t.getElseBody() != null) collectFromStmt(t.getElseBody(), routeName, out);
            if (t.getFinallyBody() != null) collectFromStmt(t.getFinallyBody(), routeName, out);
        }

    }


    private void collectFromExpr(ExprNode expr, String routeName, List<RenderTemplateCall> out) {
        if (expr == null) return;

        if (expr instanceof FuncCallNode) {
            FuncCallNode call = (FuncCallNode) expr;

            if (isRenderTemplateCall(call)) {
                out.add(buildCall(call, routeName));
            }

            for (ExprNode arg : call.getArgs()) collectFromExpr(arg, routeName, out);

        } else if (expr instanceof CallExprNode) {
            for (ExprNode arg : ((CallExprNode) expr).getArguments()) collectFromExpr(arg, routeName, out);

        } else if (expr instanceof BinaryExprNode) {
            BinaryExprNode b = (BinaryExprNode) expr;
            collectFromExpr(b.getLeft(), routeName, out);
            collectFromExpr(b.getRight(), routeName, out);

        } else if (expr instanceof UnaryExprNode) {
            collectFromExpr(((UnaryExprNode) expr).getOperand(), routeName, out);
        }

    }

    private boolean isRenderTemplateCall(FuncCallNode call) {
        ExprNode target = call.getTarget();
        return target instanceof IdentifierNode
                && "render_template".equals(((IdentifierNode) target).getName());
    }

    private RenderTemplateCall buildCall(FuncCallNode call, String routeName) {
        RenderTemplateCall result = new RenderTemplateCall();
        result.routeName = routeName;

        for (ExprNode arg : call.getArgs()) {
            if (arg instanceof StringNode && result.templateFile == null) {
                result.templateFile = stripQuotes(((StringNode) arg).getValue());

            } else if (arg instanceof KwArgNode) {
                KwArgNode kw = (KwArgNode) arg;
                result.contextVars.put(kw.getName(), describeSource(kw.getValue()));
            }
        }
        return result;
    }

    private String describeSource(ExprNode expr) {
        if (expr instanceof IdentifierNode) {
            return ((IdentifierNode) expr).getName();
        }
        return "<expr:" + expr.getClass().getSimpleName() + ">";
    }

    private String stripQuotes(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.length() >= 6 &&
                ((s.startsWith("\"\"\"") && s.endsWith("\"\"\"")) ||
                        (s.startsWith("'''") && s.endsWith("'''")))) {
            return s.substring(3, s.length() - 3);
        }
        if (s.length() >= 2 &&
                ((s.startsWith("\"") && s.endsWith("\"")) ||
                        (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}