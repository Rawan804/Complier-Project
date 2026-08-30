package codegenerate.GenerateCode;


import AST.Python_AST.*;

import java.util.List;
public class PythonUnparser {
    private final StringBuilder out = new StringBuilder();
    private int indentLevel = 0;

    public String generate(ProgramNode program) {
        out.setLength(0);
        indentLevel = 0;
        for (StmtNode stmt : program.getStatements()) {
            writeStmt(stmt);
        }
        return out.toString();
    }
    private void indent() {
        for (int i = 0; i < indentLevel; i++) out.append("    ");
    }

    private void line(String text) {
        indent();
        out.append(text).append("\n");
    }

    private void writeIndentedBlock(BlockStmtNode block) {
        indentLevel++;
        if (block == null || block.getStatements().isEmpty()) {
            line("pass");
        } else {
            for (StmtNode s : block.getStatements()) writeStmt(s);
        }
        indentLevel--;
    }

    // ══════════════════════════════════════════════
    // STATEMENTS
    // ══════════════════════════════════════════════
    private void writeStmt(StmtNode stmt) {
        if (stmt == null) return;

        if (stmt instanceof AssignStmtNode) {
            AssignStmtNode a = (AssignStmtNode) stmt;
            line(exprStr(a.getTarget()) + " = " + exprStr(a.getValue()));

        } else if (stmt instanceof AugAssignStmtNode) {
            AugAssignStmtNode a = (AugAssignStmtNode) stmt;
            line(exprStr(a.getTarget()) + " " + a.getOperator() + " " + exprStr(a.getValue()));

        } else if (stmt instanceof FunctionDefNode) {
            writeFunctionDef((FunctionDefNode) stmt);

        } else if (stmt instanceof ReturnStmtNode) {
            ReturnStmtNode r = (ReturnStmtNode) stmt;
            line(r.getValue() != null ? "return " + exprStr(r.getValue()) : "return");

        } else if (stmt instanceof IfStmtNode) {
            writeIf((IfStmtNode) stmt);

        } else if (stmt instanceof ForStmtNode) {
            writeFor((ForStmtNode) stmt);

        } else if (stmt instanceof WhileStmtNode) {
            writeWhile((WhileStmtNode) stmt);

        } else if (stmt instanceof BreakStmtNode) {
            line("break");

        } else if (stmt instanceof ContinueStmtNode) {
            line("continue");

        } else if (stmt instanceof PassStmtNode) {
            line("pass");

        } else if (stmt instanceof ExprStmtNode) {
            line(exprStr(((ExprStmtNode) stmt).getExpression()));

        } else if (stmt instanceof BlockStmtNode) {
            for (StmtNode s : ((BlockStmtNode) stmt).getStatements()) writeStmt(s);

        } else if (stmt instanceof ClassDefNode) {
            writeClassDef((ClassDefNode) stmt);

        } else if (stmt instanceof GlobalStmtNode) {
            line("global " + String.join(", ", ((GlobalStmtNode) stmt).getNames()));

        } else if (stmt instanceof NonlocalStmtNode) {
            line("nonlocal " + String.join(", ", ((NonlocalStmtNode) stmt).getNames()));

        } else if (stmt instanceof ImportStmtNode) {
            writeImport((ImportStmtNode) stmt);

        } else if (stmt instanceof ImportFromNode) {
            writeImportFrom((ImportFromNode) stmt);

        } else if (stmt instanceof WithStmtNode) {
            writeWith((WithStmtNode) stmt);

        } else if (stmt instanceof TryStmtNode) {
            writeTry((TryStmtNode) stmt);

        } else {
            line("pass  # TODO: unparse " + stmt.getClass().getSimpleName());
        }
    }

    private void writeFunctionDef(FunctionDefNode node) {
        for (DecoratorNode d : node.getDecorators()) {
            line("@" + exprStr(d.getName()) + (d.getArgs().isEmpty() ? "" : "(" + joinExprs(d.getArgs()) + ")"));
        }

        StringBuilder params = new StringBuilder();
        List<ParamNode> paramList = node.getParams();
        for (int i = 0; i < paramList.size(); i++) {
            params.append(writeParam(paramList.get(i)));
            if (i < paramList.size() - 1) params.append(", ");
        }
        line("def " + node.getName() + "(" + params + "):");
        writeIndentedBlock(node.getBody());
    }

    private String writeParam(ParamNode p) {
        if (p.getDefaultValue() != null) return p.getName() + "=" + exprStr(p.getDefaultValue());
        return p.getName();
    }

    private void writeClassDef(ClassDefNode node) {
        List<ExprNode> bases = node.getBases();
        String header = bases.isEmpty()
                ? "class " + node.getName() + ":"
                : "class " + node.getName() + "(" + joinExprs(bases) + "):";
        line(header);
        writeIndentedBlock(node.getBody());
    }

    private void writeIf(IfStmtNode node) {
        line("if " + exprStr(node.getIfCondition()) + ":");
        writeIndentedBlock(node.getIfBody());

        for (IfStmtNode.IfBranch branch : node.getElifBranches()) {
            line("elif " + exprStr(branch.condition) + ":");
            writeIndentedBlock(branch.body);
        }

        if (node.getElseBody() != null) {
            line("else:");
            writeIndentedBlock(node.getElseBody());
        }
    }

    private void writeFor(ForStmtNode node) {
        line("for " + joinExprs(node.getTargets()) + " in " + exprStr(node.getIterable()) + ":");
        writeIndentedBlock(node.getBody());
    }

    private void writeWhile(WhileStmtNode node) {
        line("while " + exprStr(node.getCondition()) + ":");
        writeIndentedBlock(node.getBody());
        if (node.getElseBody() != null) {
            line("else:");
            writeIndentedBlock(node.getElseBody());
        }
    }

    private void writeImport(ImportStmtNode node) {
        StringBuilder sb = new StringBuilder("import ");
        List<ImportItem> items = node.getItems();
        for (int i = 0; i < items.size(); i++) {
            sb.append(importItemStr(items.get(i)));
            if (i < items.size() - 1) sb.append(", ");
        }
        line(sb.toString());
    }

    private void writeImportFrom(ImportFromNode node) {
        StringBuilder sb = new StringBuilder("from " + exprStr(node.getModule()) + " import ");
        List<ImportItem> items = node.getItems();
        for (int i = 0; i < items.size(); i++) {
            sb.append(importItemStr(items.get(i)));
            if (i < items.size() - 1) sb.append(", ");
        }
        line(sb.toString());
    }

    private String importItemStr(ImportItem item) {
        String base = exprStr(item.getName());
        return item.getAlias() != null ? base + " as " + item.getAlias() : base;
    }

    // ⚠️ يحتاج getItems()/getBody() على WithStmtNode وgetContext()/getAlias() على WithItem
    private void writeWith(WithStmtNode node) {
        StringBuilder sb = new StringBuilder("with ");
        List<WithStmtNode.WithItem> items = node.getItems();
        for (int i = 0; i < items.size(); i++) {
            WithStmtNode.WithItem it = items.get(i);
            sb.append(exprStr(it.getContext()));
            if (it.getAlias() != null) sb.append(" as ").append(it.getAlias());
            if (i < items.size() - 1) sb.append(", ");
        }
        sb.append(":");
        line(sb.toString());
        writeIndentedBlock(node.getBody());
    }

    // ⚠️ يحتاج getters على TryStmtNode وExceptBlock
    private void writeTry(TryStmtNode node) {
        line("try:");
        writeIndentedBlock(node.getTryBody());

        for (TryStmtNode.ExceptBlock e : node.getExceptBlocks()) {
            StringBuilder sb = new StringBuilder("except");
            if (e.getType() != null) sb.append(" ").append(exprStr(e.getType()));
            if (e.getAlias() != null) sb.append(" as ").append(e.getAlias());
            sb.append(":");
            line(sb.toString());
            writeIndentedBlock(e.getBody());
        }

        if (node.getElseBody() != null) {
            line("else:");
            writeIndentedBlock(node.getElseBody());
        }
        if (node.getFinallyBody() != null) {
            line("finally:");
            writeIndentedBlock(node.getFinallyBody());
        }
    }

    private String exprStr(ExprNode expr) {
        if (expr == null) return "";

        if (expr instanceof NumberNode)  return ((NumberNode) expr).getValue();
        if (expr instanceof StringNode)  return ((StringNode) expr).getValue();
        if (expr instanceof BooleanNode) return ((BooleanNode) expr).getValue() ? "True" : "False";
        if (expr instanceof NullNode)    return "None";
        if (expr instanceof IdentifierNode) return ((IdentifierNode) expr).getName();

        if (expr instanceof BinaryExprNode) {
            BinaryExprNode b = (BinaryExprNode) expr;
            // الـ Visitor الحالي عم يمثل الـ ternary كـ BinaryExprNode متسلسل
            // بعمليات "ternary_if"/"ternary_else" (شوفي visitTernary) —
            // منرجعها لصياغة Python الصحيحة هون:
            if (b.getOperator().equals("ternary_if") && b.getRight() instanceof BinaryExprNode
                    && ((BinaryExprNode) b.getRight()).getOperator().equals("ternary_else")) {
                BinaryExprNode elseNode = (BinaryExprNode) b.getRight();
                return exprStr(elseNode.getLeft()) + " if " + exprStr(b.getLeft())
                        + " else " + exprStr(elseNode.getRight());
            }
            return exprStr(b.getLeft()) + " " + b.getOperator() + " " + exprStr(b.getRight());
        }

        if (expr instanceof TernaryExprNode) {
            TernaryExprNode t = (TernaryExprNode) expr;
            return exprStr(t.getTrueExpr()) + " if " + exprStr(t.getCondition())
                    + " else " + exprStr(t.getFalseExpr());
        }

        if (expr instanceof UnaryExprNode) {
            UnaryExprNode u = (UnaryExprNode) expr;
            String op = u.getOperator();
            return op + (op.equals("not") ? " " : "") + exprStr(u.getOperand());
        }

        if (expr instanceof FuncCallNode) {
            FuncCallNode f = (FuncCallNode) expr;
            return exprStr(f.getTarget()) + "(" + joinExprs(f.getArgs()) + ")";
        }

        if (expr instanceof CallExprNode) {
            CallExprNode c = (CallExprNode) expr;
            return exprStr(c.getFunction()) + "(" + joinExprs(c.getArguments()) + ")";
        }

        if (expr instanceof IndexAccessNode) {
            IndexAccessNode ix = (IndexAccessNode) expr;
            return exprStr(ix.getTarget()) + "[" + exprStr(ix.getIndex()) + "]";
        }

        if (expr instanceof MemberAccessNode) {
            MemberAccessNode m = (MemberAccessNode) expr;
            return exprStr(m.getObject()) + "." + m.getMember();
        }

        if (expr instanceof ListNode) {
            return "[" + joinExprs(((ListNode) expr).getItems()) + "]";
        }

        if (expr instanceof SetNode) {
            List<ExprNode> items = ((SetNode) expr).getItems();
            return items.isEmpty() ? "set()" : "{" + joinExprs(items) + "}";
        }

        if (expr instanceof TupleNode) {
            List<ExprNode> items = ((TupleNode) expr).getItems();
            if (items.isEmpty()) return "()";
            if (items.size() == 1) return "(" + exprStr(items.get(0)) + ",)";
            return "(" + joinExprs(items) + ")";
        }

        if (expr instanceof DictNode) {
            StringBuilder sb = new StringBuilder("{");
            List<DictEntry> entries = ((DictNode) expr).getEntries();
            for (int i = 0; i < entries.size(); i++) {
                DictEntry e = entries.get(i);
                sb.append(exprStr(e.getKey())).append(": ").append(exprStr(e.getValue()));
                if (i < entries.size() - 1) sb.append(", ");
            }
            return sb.append("}").toString();
        }

        if (expr instanceof RangeNode) {
            return "range(" + joinExprs(((RangeNode) expr).getArgs()) + ")";
        }

        if (expr instanceof KwArgNode) {
            KwArgNode k = (KwArgNode) expr;
            return k.getName() + "=" + exprStr(k.getValue());
        }

        if (expr instanceof LambdaExprNode) {
            // ⚠️ LambdaExprNode.getParams() هون فعلياً List<ExprNode> (حسب صنفك الحالي)
            // مش List<ParamNode> — لهيك منستعمل exprStr() العادية بدل writeParam().
            // ملاحظة: هاد بيطبع بس اسم الباراميتر (identifier)، بدون قيمة افتراضية،
            // لأنه النوع الحالي ما بيحمل معلومة الـ default value لباراميترات اللامبدا.
            LambdaExprNode l = (LambdaExprNode) expr;
            return "lambda " + joinExprs(l.getParams()) + ": " + exprStr(l.getBody());
        }

        return "<?" + expr.getClass().getSimpleName() + "?>";
    }

    private String joinExprs(List<ExprNode> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(exprStr(items.get(i)));
            if (i < items.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}