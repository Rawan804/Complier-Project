package semantic.python_semantic;

import AST.Python_AST.*;
import SymbolTable.python_symbol_table.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
public class SemanticAnalyzer {

    private final SymbolTable symbolTable;
    private final List<SemanticError> errors = new ArrayList<>();
    private boolean insideFunction = false;
    private boolean insideLoop = false;
    private String currentFunction = null;
    private final HashSet<String> visitedVariables = new HashSet<>();
    private static final String TYPE_INT = "int";
    private static final String TYPE_FLOAT = "float";
    private static final String TYPE_STRING = "str";
    private static final String TYPE_BOOL = "bool";
    private static final String TYPE_ANY = "any";
    private static final String TYPE_UNKNOWN = "unknown";

    public SemanticAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public List<SemanticError> getErrors() { return errors; }

    private void error(String msg, int line) {
        errors.add(new SemanticError(msg, line));
    }


    private String getActualType(SymbolRow row) {
        if (row == null) return TYPE_UNKNOWN;
        Object inferred = row.getAttribute("inferredType");
        if (inferred != null) {
            String infStr = inferred.toString().toLowerCase();
            if (infStr.contains("int")) return TYPE_INT;
            if (infStr.contains("string") || infStr.contains("str")) return TYPE_STRING;
            if (infStr.contains("float")) return TYPE_FLOAT;
            if (infStr.contains("bool")) return TYPE_BOOL;
            if (infStr.contains("list")) return "list";
            if (infStr.contains("dict")) return "dict";
            if (infStr.contains("tuple")) return "tuple";

            return infStr;
        }
        return TYPE_ANY;
    }

    private boolean canAssign(String targetType, String valueType) {
        if (targetType.equals(TYPE_ANY) || valueType.equals(TYPE_ANY)) return true;
        if (targetType.equals(TYPE_FLOAT) && valueType.equals(TYPE_INT)) return true;
        return targetType.equals(valueType);
    }
    public void analyze(ProgramNode program) {
        for (StmtNode stmt : program.getStatements())
            analyzeStmt(stmt);
    }
    private void analyzeStmt(StmtNode stmt) {
        if (stmt instanceof AssignStmtNode) analyzeAssign((AssignStmtNode) stmt);
        else if (stmt instanceof FunctionDefNode) analyzeFunc((FunctionDefNode) stmt);
        else if (stmt instanceof ReturnStmtNode) analyzeReturn((ReturnStmtNode) stmt);
        else if (stmt instanceof IfStmtNode) analyzeIf((IfStmtNode) stmt);
        else if (stmt instanceof ForStmtNode) analyzeFor((ForStmtNode) stmt);
        else if (stmt instanceof WhileStmtNode) analyzeWhile((WhileStmtNode) stmt);
        else if (stmt instanceof BreakStmtNode) analyzeBreak((BreakStmtNode) stmt);
        else if (stmt instanceof ContinueStmtNode) analyzeContinue((ContinueStmtNode) stmt);
        else if (stmt instanceof ExprStmtNode) analyzeExpr(((ExprStmtNode) stmt).getExpression());
        else if (stmt instanceof BlockStmtNode) analyzeBlock((BlockStmtNode) stmt);
    }

    private void analyzeBlock(BlockStmtNode block) {
        for (StmtNode stmt : block.getStatements())
            analyzeStmt(stmt);
    }

    // ══════════════════════════════════════════════
    // ASSIGN — التحقق وتحديث الأنواع تتابعياً
    // ══════════════════════════════════════════════
    private void analyzeAssign(AssignStmtNode node) {
        String exprType = analyzeExpr(node.getValue());

        if (node.getTarget() instanceof IdentifierNode) {
            String varName = ((IdentifierNode) node.getTarget()).getName();
            SymbolRow row = symbolTable.lookup(varName);

            if (row != null) {
                if (!visitedVariables.contains(varName)) {
                    row.setAttribute("inferredType", exprType);
                    visitedVariables.add(varName);
                } else {
                    String currentVarType = getActualType(row);
                    if (!canAssign(currentVarType, exprType)) {
                        error("Type mismatch: Cannot assign '" + exprType + "' to a variable of type '" + currentVarType + "'", node.getLine());
                    }
                }
            } else {
                // هنا الفحص الجديد عند الإسناد:
                if (symbolTable.existsAnywhereInProgram(varName)) {
                    error("Scope Error: Cannot assign to variable '" + varName + "' because it is outside the current scope", node.getLine());
                } else {
                    // إذا كان متغير جديد تماماً في السكوب الحالي (قم بتسجيله بالـ SymbolTable إذا لزم الأمر حسب منطق الـ Builder لديك)
                    // symbolTable.insert(varName, ...);
                }
            }
            symbolTable.recordUsage(varName, node.getLine());
        } else {
            analyzeExpr(node.getTarget());
        }
    }
    // ══════════════════════════════════════════════
    // FUNCTION
    private void analyzeFunc(FunctionDefNode node) {
        // ---- الفحص الجديد: كشف تكرار اسم الدالة في نفس النطاق ----
        // نبحث في السكوب الحالي (الذي نحن فيه الآن قبل الدخول للدالة)
        SymbolRow existingSymbol = symbolTable.lookupCurrentScope(node.getName());

        // إذا وجدنا رمزاً بنفس الاسم، وكان هذا الرمز يمثل دالة تم فحصها وزيارتها مسبقاً
        if (existingSymbol != null && "function".equals(existingSymbol.getType()) && visitedVariables.contains(node.getName())) {
            error("Duplicate Function Error: Function '" + node.getName() + "' is already defined in this scope", node.getLine());
        }

        // تسجيل اسم الدالة الحالية في الـ visited لتجنب تكرار الخطأ مع نفسها وللتحقق من القادم
        visitedVariables.add(node.getName());
        boolean prevInsideFunction = insideFunction;
        String prevFunction = currentFunction;
        insideFunction = true;
        currentFunction = node.getName();
        // استعادة السكوب المحفوظ مسبقاً بالـ Visitor
        symbolTable.reActivateScope("function:" + node.getName());
        if (node.getBody() != null)
            analyzeBlock(node.getBody());
        symbolTable.deactivateCurrentScope();
        insideFunction = prevInsideFunction;
        currentFunction = prevFunction;
    }

    // ══════════════════════════════════════════════
    // RETURN
    // ══════════════════════════════════════════════
    private void analyzeReturn(ReturnStmtNode node) {
        if (!insideFunction)
            error("'return' outside function", node.getLine());
        if (node.getValue() != null)
            analyzeExpr(node.getValue());
    }

    // ══════════════════════════════════════════════
    // IF
    // ══════════════════════════════════════════════
    private void analyzeIf(IfStmtNode node) {
        analyzeExpr(node.getIfCondition());
        analyzeBlock(node.getIfBody());

        for (IfStmtNode.IfBranch branch : node.getElifBranches()) {
            analyzeExpr(branch.condition);
            analyzeBlock(branch.body);
        }

        if (node.getElseBody() != null)
            analyzeBlock(node.getElseBody());
    }
    private void analyzeFor(ForStmtNode node) {
        String iterableType = analyzeExpr(node.getIterable());
        if (iterableType.equals(TYPE_INT) || iterableType.equals(TYPE_FLOAT) || iterableType.equals(TYPE_BOOL)) {
            error("Type Error: '" + iterableType + "' object is not iterable", node.getLine());
        }
        if (node.getTargets() instanceof IdentifierNode) {
            visitedVariables.add(((IdentifierNode) node.getTargets()).getName());
        }

        boolean prev = insideLoop;
        insideLoop = true;
        analyzeBlock(node.getBody());
        insideLoop = prev;
    }

    private void analyzeWhile(WhileStmtNode node) {
        analyzeExpr(node.getCondition());
        boolean prev = insideLoop;
        insideLoop = true;
        analyzeBlock(node.getBody());
        insideLoop = prev;
    }

    private void analyzeBreak(BreakStmtNode node) {
        if (!insideLoop)
            error("'break' outside loop", node.getLine());
    }

    private void analyzeContinue(ContinueStmtNode node) {
        if (!insideLoop)
            error("'continue' outside loop", node.getLine());
    }
    private String analyzeExpr(ExprNode expr) {
        if (expr == null) return TYPE_UNKNOWN;

        if (expr instanceof NumberNode) return TYPE_INT;
        if (expr instanceof StringNode) return TYPE_STRING;
        if (expr instanceof BooleanNode) return TYPE_BOOL;

        if (expr instanceof IdentifierNode) {
            return analyzeIdentifier((IdentifierNode) expr);
        }
        else if (expr instanceof FuncCallNode) {
            analyzeFuncCall((FuncCallNode) expr);
            return TYPE_ANY;
        }
        else if (expr instanceof BinaryExprNode) {
            return analyzeBinary((BinaryExprNode) expr);
        }
        else if (expr instanceof UnaryExprNode) {
            UnaryExprNode unary = (UnaryExprNode) expr;
            String operandType = analyzeExpr(unary.getOperand());

            if (unary.getOperator().equals("-") || unary.getOperator().equals("+")) {
                if (operandType.equals(TYPE_STRING) || operandType.equals("list") || operandType.equals("dict")) {
                    error("Type Error: Bad operand type for unary " + unary.getOperator() + ": '" + operandType + "'", unary.getLine());
                }
            }
            return operandType;
        }
        else if (expr instanceof IndexAccessNode) {
            analyzeIndex((IndexAccessNode) expr);
            return TYPE_ANY;
        }
        else if (expr instanceof MemberAccessNode) {
            analyzeExpr(((MemberAccessNode) expr).getObject());
            return TYPE_ANY;
        }
        else if (expr instanceof ListNode) {
            analyzeList((ListNode) expr);
            return "list";
        }
        else if (expr instanceof DictNode) {
            analyzeDict((DictNode) expr);
            return "dict";
        }
        else if (expr instanceof TupleNode) {
            analyzeTuple((TupleNode) expr);
            return "tuple";
        }
        else if (expr instanceof KwArgNode) {
            return analyzeExpr(((KwArgNode) expr).getValue());
        }

        return TYPE_ANY;
    }



    private String analyzeIdentifier(IdentifierNode node) {
        String name = node.getName();
        if (isBuiltin(name)) return TYPE_ANY;
        SymbolRow row = symbolTable.lookup(name);

        if (row == null) {
            if (symbolTable.existsAnywhereInProgram(name)) {
                error("Scope Error: Variable '" + name + "' is defined in another scope and cannot be accessed from here", node.getLine());
                return TYPE_UNKNOWN;
            }
            error("Variable '" + name + "' is not defined", node.getLine());
            return TYPE_UNKNOWN;
        }
        return getActualType(row);
    }

    private void analyzeFuncCall(FuncCallNode node) {
        if (node.getTarget() instanceof IdentifierNode) {
            String name = ((IdentifierNode) node.getTarget()).getName();
            if (!isBuiltin(name)) {
                SymbolRow row = symbolTable.lookupInAllScopes(name);
                if (row == null)
                    error("Function '" + name + "' is not defined", node.getLine());
            }
        } else {
            analyzeExpr(node.getTarget());
        }
        for (ExprNode arg : node.getArgs())
            analyzeExpr(arg);
    }

    private String analyzeBinary(BinaryExprNode node) {
        String leftType = analyzeExpr(node.getLeft());
        String rightType = analyzeExpr(node.getRight());

        // فحص تضارب أنواع العمليات (Operational Type Mismatch)
        if (node.getOperator().equals("-") || node.getOperator().equals("*") || node.getOperator().equals("/")) {
            if (leftType.equals(TYPE_STRING) || rightType.equals(TYPE_STRING)) {
                error("Unsupported operand type(s) for " + node.getOperator() + ": '" + leftType + "' and '" + rightType + "'", node.getLine());
                return TYPE_ANY;
            }
        }

        // فحص القسمة على صفر
        if (node.getOperator().equals("/")) {
            ExprNode right = node.getRight();
            if (right instanceof NumberNode) {
                double val = Double.parseDouble(((NumberNode) right).getValue());
                if (val == 0)
                    error("Division by zero", node.getLine());
            }
        }

        return leftType;
    }

    private void analyzeIndex(IndexAccessNode node) {
        String targetType = analyzeExpr(node.getTarget());
        String indexType = analyzeExpr(node.getIndex());

        // 1. فحص هل المستهدف يدعم الـ Indexing أصلاً؟
        if (targetType.equals(TYPE_INT) || targetType.equals(TYPE_FLOAT) || targetType.equals(TYPE_BOOL)) {
            error("Type Error: '" + targetType + "' object is not subscriptable", node.getLine());
        }

        // 2. فحص إضافي: إذا كان المستهدف list أو str، يجب أن يكون الـ index عبارة عن int
        if ((targetType.equals("list") || targetType.equals(TYPE_STRING)) && !indexType.equals(TYPE_INT) && !indexType.equals(TYPE_ANY)) {
            error("Type Error: List/String indices must be integers, not '" + indexType + "'", node.getLine());
        }
    }

    private void analyzeList(ListNode node) {
        for (ExprNode item : node.getItems()) analyzeExpr(item);
    }

    private void analyzeDict(DictNode node) {
        for (DictEntry e : node.getEntries()) {
            analyzeExpr(e.getKey());
            analyzeExpr(e.getValue());
        }
    }

    private void analyzeTuple(TupleNode node) {
        for (ExprNode item : node.getItems()) analyzeExpr(item);
    }

    private boolean isBuiltin(String name) {
        return switch (name) {
            case "print", "len", "range", "int", "str", "float",
                 "list", "dict", "set", "tuple", "bool",
                 "True", "False", "None", "__name__",
                 "append", "get", "run" -> true;
            default -> false;
        };
    }
}