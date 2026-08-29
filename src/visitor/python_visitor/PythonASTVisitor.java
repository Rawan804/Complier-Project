package visitor.python_visitor;

import AST.Python_AST.*;
import LexerandParser.puthon_antlr.python_parser;
import LexerandParser.puthon_antlr.python_parserBaseVisitor;
import SymbolTable.python_symbol_table.SymbolRow;
import SymbolTable.python_symbol_table.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class PythonASTVisitor extends python_parserBaseVisitor<Node> {

    private final SymbolTable symbolTable = new SymbolTable();

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    @Override
    public Node visitProgram(python_parser.ProgramContext ctx) {
        ProgramNode program = new ProgramNode();
        for (python_parser.StmtContext stmtCtx : ctx.stmt()) {
            StmtNode stmt = (StmtNode) visit(stmtCtx);
            if (stmt != null) program.addStatement(stmt);
        }
        return program;
    }

    @Override public Node visitSIf(python_parser.SIfContext ctx)
    {
        return visit(ctx.ifStmt()); }
    @Override public Node visitSWhile(python_parser.SWhileContext ctx)
    {
        return visit(ctx.whileStmt()); }
    @Override public Node visitSFor(python_parser.SForContext ctx)
    {
        return visit(ctx.forStmt()); }
    @Override public Node visitSTry(python_parser.STryContext ctx)
    { return visit(ctx.tryStmt()); }
    @Override public Node visitSWith(python_parser.SWithContext ctx)
    { return visit(ctx.withStmt()); }
    @Override public Node visitSFunc(python_parser.SFuncContext ctx)
    { return visit(ctx.funcDef()); }
    @Override public Node visitSClass(python_parser.SClassContext ctx)
    { return visit(ctx.classDef()); }
    @Override public Node visitSDecorated(python_parser.SDecoratedContext ctx)
    { return visit(ctx.decorated()); }
    @Override public Node visitSAugAssign(python_parser.SAugAssignContext ctx)
    { return visit(ctx.augAssign()); }
    @Override public Node visitSAssign(python_parser.SAssignContext ctx)
    { return visit(ctx.assign()); }
    @Override public Node visitSGlobal(python_parser.SGlobalContext ctx)
    { return visit(ctx.globalStmt()); }
    @Override public Node visitSNonlocal(python_parser.SNonlocalContext ctx)
    { return visit(ctx.nonlocalStmt()); }

    @Override
    public Node visitGlobalStatement(python_parser.GlobalStatementContext ctx) {
        int line = ctx.getStart().getLine();
        for (org.antlr.v4.runtime.tree.TerminalNode idNode
                : ctx.getTokens(python_parser.IDENTIFIER)) {
            String name = idNode.getText();

            SymbolRow row = symbolTable.lookupCurrentScope(name);
            if (row == null) {
                row = symbolTable.insert(name);
                row.setLine(line);
            }
            row.setType("global");
            row.setAttribute("global", true);


            SymbolRow globalRow = symbolTable.lookupGlobal(name);
            if (globalRow != null) {
                globalRow.setAttribute("sharedAsGlobal", true);
            }
        }
        return null;
    }

    @Override
    public Node visitNonlocalStatement(python_parser.NonlocalStatementContext ctx) {
        int line = ctx.getStart().getLine();
        for (org.antlr.v4.runtime.tree.TerminalNode idNode
                : ctx.getTokens(python_parser.IDENTIFIER)) {
            String name = idNode.getText();
            SymbolRow row = symbolTable.lookupCurrentScope(name);
            if (row == null) {
                row = symbolTable.insert(name);
                row.setLine(line);
            }
            row.setType("nonlocal");
            row.setAttribute("nonlocal", true);
        }
        return null;
    }

    @Override
    public Node visitSPass(python_parser.SPassContext ctx) {
        return new PassStmtNode(ctx.getStart().getLine());
    }

    @Override
    public Node visitSBreak(python_parser.SBreakContext ctx) {
        return new BreakStmtNode(ctx.getStart().getLine());
    }

    @Override
    public Node visitSContinue(python_parser.SContinueContext ctx) {
        return new ContinueStmtNode(ctx.getStart().getLine());
    }
    // IMPORT
    @Override
    public Node visitSImport(python_parser.SImportContext ctx) {
        return visit(ctx.importStmt());
    }

    @Override
    public Node visitSExpr(python_parser.SExprContext ctx) {
        ExprNode expr = (ExprNode) visit(ctx.expr());
        return new ExprStmtNode(ctx.getStart().getLine(), expr);
    }

    @Override
    public Node visitImportDirect(python_parser.ImportDirectContext ctx) {
        int line = ctx.getStart().getLine();
        List<ImportItem> items = new ArrayList<>();
        for (python_parser.ImportItemContext itemCtx : ctx.importItem()) {
            ImportItem item = buildImportItem(itemCtx, line);
            if (item != null) items.add(item);
        }
        return new ImportStmtNode(line, items);
    }

    @Override
    public Node visitImportFrom(python_parser.ImportFromContext ctx) {
        int line = ctx.getStart().getLine();
        ExprNode moduleNode = (ExprNode) visit(ctx.dottedName());


        if (ctx.dottedName() != null) {
            String moduleName = ctx.dottedName().getText();
            SymbolRow modRow = symbolTable.lookup(moduleName);
            if (modRow == null) {
                modRow = symbolTable.insert(moduleName);
            }
            modRow.setType("module");
            modRow.setLine(line);
        }

        List<ImportItem> items = new ArrayList<>();
        for (python_parser.ImportItemContext itemCtx : ctx.importItem()) {
            ImportItem item = buildImportItem(itemCtx, line);
            if (item != null) items.add(item);
        }
        return new ImportFromNode(line, moduleNode, items);
    }

    private ImportItem buildImportItem(python_parser.ImportItemContext ctx, int line) {
        if (!(ctx instanceof python_parser.ImportAliasContext)) return null;
        python_parser.ImportAliasContext aliasCtx = (python_parser.ImportAliasContext) ctx;

        String nameText  = aliasCtx.name.getText();
        int    nameLine  = aliasCtx.name.getLine();
        String aliasText = (aliasCtx.alias != null) ? aliasCtx.alias.getText() : null;

        ExprNode nameNode    = new IdentifierNode(nameLine, nameText);
        String   insertName  = (aliasText != null) ? aliasText : nameText;

        SymbolRow row = symbolTable.insert(insertName);
        row.setType("import");
        row.setLine(nameLine);
        if (aliasText != null) {
            row.setAttribute("originalName", nameText);
        }
        return new ImportItem(nameNode, aliasText);
    }
    // RETURN
    @Override
    public Node visitSReturn(python_parser.SReturnContext ctx) {
        int line = ctx.getStart().getLine();
        if (ctx.args() == null) return new ReturnStmtNode(line);

        ExprNode returnValue = null;
        if (ctx.args() instanceof python_parser.ArgListContext) {
            python_parser.ArgListContext argList = (python_parser.ArgListContext) ctx.args();
            if (argList.arg().size() == 1) {
                returnValue = (ExprNode) visit(argList.arg(0));
            } else {
                List<ExprNode> items = new ArrayList<>();
                for (python_parser.ArgContext a : argList.arg())
                    items.add((ExprNode) visit(a));
                returnValue = new TupleNode(line, items);
            }
        }
        return new ReturnStmtNode(line, returnValue);
    }
    // BLOCK
    @Override
    public Node visitBlockIndented(python_parser.BlockIndentedContext ctx) {
        BlockStmtNode block = new BlockStmtNode(ctx.getStart().getLine());
        for (python_parser.StmtContext stmtCtx : ctx.stmt()) {
            StmtNode stmt = (StmtNode) visit(stmtCtx);
            if (stmt != null) block.addStatement(stmt);
        }
        return block;
    }

    @Override
    public Node visitBlockInline(python_parser.BlockInlineContext ctx) {
        BlockStmtNode block = new BlockStmtNode(ctx.getStart().getLine());
        StmtNode stmt = (StmtNode) visit(ctx.stmt());
        if (stmt != null) block.addStatement(stmt);
        return block;
    }
    // DOTTED NAME
    @Override
    public Node visitDottedId(python_parser.DottedIdContext ctx) {
        ExprNode current = new IdentifierNode(
                ctx.IDENTIFIER(0).getSymbol().getLine(),
                ctx.IDENTIFIER(0).getText());
        for (int i = 1; i < ctx.IDENTIFIER().size(); i++) {
            current = new MemberAccessNode(
                    ctx.IDENTIFIER(i).getSymbol().getLine(),
                    current,
                    ctx.IDENTIFIER(i).getText());
        }
        return current;
    }
    // IF
    @Override
    public Node visitIfStatement(python_parser.IfStatementContext ctx) {
        int line = ctx.getStart().getLine();

        ExprNode      ifCondNode = (ExprNode)      visit(ctx.ifCond);
        BlockStmtNode ifBodyNode = (BlockStmtNode) visit(ctx.ifBody);
        IfStmtNode    ifStmtNode = new IfStmtNode(line, ifCondNode, ifBodyNode);

        for (int i = 0; i < ctx.elifCond.size(); i++) {
            ExprNode      elifCond = (ExprNode)      visit(ctx.elifCond.get(i));
            BlockStmtNode elifBody = (BlockStmtNode) visit(ctx.elifBody.get(i));
            if (elifCond != null && elifBody != null)
                ifStmtNode.addElif(elifCond, elifBody);
        }

        if (ctx.elseBody != null)
            ifStmtNode.setElse((BlockStmtNode) visit(ctx.elseBody));

        return ifStmtNode;
    }
    // WHILE — بدون allocate/free
    @Override
    public Node visitWhileStatement(python_parser.WhileStatementContext ctx) {
        int line = ctx.getStart().getLine();
        ExprNode      condNode = (ExprNode)      visit(ctx.cond);
        BlockStmtNode bodyNode = (BlockStmtNode) visit(ctx.body);
        WhileStmtNode whileNode = new WhileStmtNode(line, condNode, bodyNode);
        if (ctx.elseBody != null)
            whileNode.setElseBody((BlockStmtNode) visit(ctx.elseBody));
        return whileNode;
    }

    @Override
    public Node visitForStatement(python_parser.ForStatementContext ctx) {
        int line = ctx.getStart().getLine();
        List<ExprNode> targetNodes = new ArrayList<>();

        for (org.antlr.v4.runtime.Token tok : ctx.targets) {
            String name = tok.getText();
            targetNodes.add(new IdentifierNode(tok.getLine(), name));


            SymbolRow row = symbolTable.lookup(name);
            if (row == null) {
                row = symbolTable.insert(name);
            }
            row.setType("variable");
            row.setLine(tok.getLine());
            row.setAttribute("source", "for-loop");
        }

        ExprNode      iterableNode = (ExprNode)      visit(ctx.iter);
        BlockStmtNode bodyNode     = (BlockStmtNode) visit(ctx.body);
        return new ForStmtNode(line, targetNodes, iterableNode, bodyNode);
    }
    // WITH
    @Override
    public Node visitWithStatement(python_parser.WithStatementContext ctx) {
        int line = ctx.getStart().getLine();
        BlockStmtNode withBody    = (BlockStmtNode) visit(ctx.body);
        WithStmtNode  withStmtNode = new WithStmtNode(line, withBody);

        for (python_parser.WithItemContext itemCtx : ctx.withItem()) {
            if (!(itemCtx instanceof python_parser.WithContextContext)) continue;
            python_parser.WithContextContext withCtx = (python_parser.WithContextContext) itemCtx;
            ExprNode ctxNode   = (ExprNode) visit(withCtx.ctx);
            String   aliasText = null;

            if (withCtx.alias != null) {
                aliasText = withCtx.alias.getText();
                SymbolRow row = symbolTable.insert(aliasText);
                row.setType("variable");
                row.setLine(withCtx.alias.getLine());
                row.setAttribute("source", "with-alias");
            }
            withStmtNode.addItem(ctxNode, aliasText);
        }
        return withStmtNode;
    }
    // TRY
    @Override
    public Node visitTryStatement(python_parser.TryStatementContext ctx) {
        int line = ctx.getStart().getLine();
        BlockStmtNode tryBodyNode  = (BlockStmtNode) visit(ctx.tryBody);
        TryStmtNode   tryStmtNode  = new TryStmtNode(line, tryBodyNode);

        int numExcepts = ctx.excBody != null ? ctx.excBody.size() : 0;

        for (int i = 0; i < numExcepts; i++) {
            ExprNode typeNode  = null;
            String   aliasText = null;
            if (i == 0 && ctx.excType != null) {
                typeNode = (ExprNode) visit(ctx.excType);
            }
            if (i == 0 && ctx.excAlias != null) {
                aliasText = ctx.excAlias.getText();
                SymbolRow row = symbolTable.insert(aliasText);
                row.setType("exception");
                row.setLine(ctx.excAlias.getLine());
                if (typeNode != null)
                    row.setAttribute("exceptionType", typeNode.toString());
            }

            BlockStmtNode exceptBody = (BlockStmtNode) visit(ctx.excBody.get(i));
            tryStmtNode.addExcept(typeNode, aliasText, exceptBody);
        }

        if (ctx.elseBody    != null) tryStmtNode.setElseBody((BlockStmtNode)    visit(ctx.elseBody));
        if (ctx.finallyBody != null) tryStmtNode.setFinallyBody((BlockStmtNode) visit(ctx.finallyBody));

        return tryStmtNode;
    }
    // FUNCTION DEFINITION
    @Override
    public Node visitFuncDefinition(python_parser.FuncDefinitionContext ctx) {
        int    line         = ctx.getStart().getLine();
        String functionName = ctx.name != null ? ctx.name.getText() : "anonymous";
        SymbolRow funcRow = symbolTable.insert(functionName);
        funcRow.setType("function");
        funcRow.setLine(line);
        symbolTable.allocate("function:" + functionName);

        List<ParamNode> paramNodes = new ArrayList<>();
        if (ctx.params() instanceof python_parser.ParamListContext) {
            for (python_parser.ParamContext p : ((python_parser.ParamListContext) ctx.params()).param()) {
                ParamNode pn = (ParamNode) visit(p);
                if (pn != null) paramNodes.add(pn);
            }
        }

        BlockStmtNode bodyNode = ctx.body != null ? (BlockStmtNode) visit(ctx.body) : null;
        funcRow.setAttribute("paramCount", paramNodes.size());

        symbolTable.free();

        return new FunctionDefNode(line, functionName, paramNodes, bodyNode, new ArrayList<>());
    }
    // PARAMETERS

    @Override
    public Node visitParamList(python_parser.ParamListContext ctx) { return null; }

    @Override
    public Node visitParamDef(python_parser.ParamDefContext ctx) {
        int    line      = ctx.getStart().getLine();
        String paramName = ctx.name.getText();
        ExprNode defaultVal = ctx.def != null ? (ExprNode) visit(ctx.def) : null;

        SymbolRow row = symbolTable.insert(paramName);
        row.setType("parameter");
        row.setLine(line);
        if (defaultVal != null) row.setAttribute("hasDefault", true);

        return new ParamNode(line, paramName, defaultVal);
    }

    // DECORATORS

    @Override
    public Node visitDecoratorExpr(python_parser.DecoratorExprContext ctx) {
        int line = ctx.getStart().getLine();
        ExprNode       nameNode      = (ExprNode) visit(ctx.name);
        List<ExprNode> decoratorArgs = new ArrayList<>();

        if (ctx.args() instanceof python_parser.ArgListContext) {
            for (python_parser.ArgContext a : ((python_parser.ArgListContext) ctx.args()).arg()) {
                Node n = visit(a);
                if (n instanceof ExprNode) decoratorArgs.add((ExprNode) n);
            }
        }
        return new DecoratorNode(line, nameNode, decoratorArgs);
    }

    @Override
    public Node visitDecoratedFunc(python_parser.DecoratedFuncContext ctx) {
        FunctionDefNode funcNode = (FunctionDefNode) visit(ctx.funcDef());
        if (ctx.decorator() != null) {
            for (python_parser.DecoratorContext d : ctx.decorator()) {
                DecoratorNode dn = (DecoratorNode) visit(d);
                if (dn != null && funcNode != null) funcNode.addDecorator(dn);
            }
        }
        return funcNode;
    }
    // CLASS DEFINITION
    @Override
    public Node visitClassDefinition(python_parser.ClassDefinitionContext ctx) {
        int    line      = ctx.getStart().getLine();
        String className = ctx.name.getText();

        SymbolRow classRow = symbolTable.insert(className);
        classRow.setType("class");
        classRow.setLine(line);

        symbolTable.allocate("class:" + className);

        List<ExprNode> bases = new ArrayList<>();
        if (ctx.args() instanceof python_parser.ArgListContext) {
            for (python_parser.ArgContext a : ((python_parser.ArgListContext) ctx.args()).arg()) {
                Node n = visit(a);
                if (n instanceof ExprNode) bases.add((ExprNode) n);
            }
        }

        BlockStmtNode bodyNode = (BlockStmtNode) visit(ctx.body);
        classRow.setAttribute("baseCount", bases.size());

        symbolTable.free();

        return new ClassDefNode(line, className, bases, bodyNode);
    }
    // ASSIGNMENTS

    @Override
    public Node visitAssignStmt(python_parser.AssignStmtContext ctx) {
        int      line       = ctx.getStart().getLine();
        ExprNode targetNode = (ExprNode) visit(ctx.target);
        ExprNode valueNode  = (ExprNode) visit(ctx.value);
        insertPostfixTarget(ctx.target, line, valueNode);
        return new AssignStmtNode(line, targetNode, valueNode);
    }

    @Override
    public Node visitAugAssignStmt(python_parser.AugAssignStmtContext ctx) {
        int      line       = ctx.getStart().getLine();
        ExprNode targetNode = (ExprNode) visit(ctx.target);
        ExprNode valueNode  = (ExprNode) visit(ctx.value);
        String   operator   = ctx.op.getText();
        insertPostfixTarget(ctx.target, line, null);
        return new AugAssignStmtNode(line, targetNode, operator, valueNode);
    }

    private void insertPostfixTarget(python_parser.PostfixContext targetCtx,
                                     int line, ExprNode valueNode) {
        if (!(targetCtx instanceof python_parser.AtomIdContext)) return;

        String varName = ((python_parser.AtomIdContext) targetCtx).IDENTIFIER().getText();

        SymbolRow row = symbolTable.lookupCurrentScope(varName);
        if (row == null) {

            row = symbolTable.lookup(varName);
        }
        if (row == null) {
            row = symbolTable.insert(varName);
            row.setType("variable");
            row.setLine(line);
        }

        if (valueNode != null) {
            String inferredType = inferType(valueNode);
            if (inferredType != null) row.setAttribute("inferredType", inferredType);
        }
    }

    private String inferType(ExprNode node) {
        if (node instanceof NumberNode)  return "number";
        if (node instanceof StringNode)  return "string";
        if (node instanceof BooleanNode) return "bool";
        if (node instanceof NullNode)    return "None";
        if (node instanceof ListNode)    return "list";
        if (node instanceof DictNode)    return "dict";
        if (node instanceof SetNode)     return "set";
        if (node instanceof TupleNode)   return "tuple";
        if (node instanceof LambdaExprNode) return "lambda";
        return null;
    }
    // EXPRESSIONS: Math, Logic, Comparison

    @Override
    public Node visitAddSub(python_parser.AddSubContext ctx) {
        return new BinaryExprNode(ctx.getStart().getLine(),
                (ExprNode) visit(ctx.left), ctx.op.getText(), (ExprNode) visit(ctx.right));
    }

    @Override
    public Node visitMulDiv(python_parser.MulDivContext ctx) {
        return new BinaryExprNode(ctx.getStart().getLine(),
                (ExprNode) visit(ctx.left), ctx.op.getText(), (ExprNode) visit(ctx.right));
    }

    @Override
    public Node visitLogicOr(python_parser.LogicOrContext ctx) {
        return new BinaryExprNode(ctx.getStart().getLine(),
                (ExprNode) visit(ctx.orExpr()), "or", (ExprNode) visit(ctx.andExpr()));
    }

    @Override
    public Node visitLogicAnd(python_parser.LogicAndContext ctx) {
        return new BinaryExprNode(ctx.getStart().getLine(),
                (ExprNode) visit(ctx.andExpr()), "and", (ExprNode) visit(ctx.cmpExpr()));
    }

    @Override
    public Node visitCompare(python_parser.CompareContext ctx) {
        return new BinaryExprNode(ctx.getStart().getLine(),
                (ExprNode) visit(ctx.left), ctx.cmpOp().getText(), (ExprNode) visit(ctx.right));
    }

    @Override
    public Node visitUnaryOp(python_parser.UnaryOpContext ctx) {
        return new UnaryExprNode(ctx.getStart().getLine(),
                ctx.op.getText(), (ExprNode) visit(ctx.operand));
    }

    @Override
    public Node visitTernary(python_parser.TernaryContext ctx) {
        int line = ctx.getStart().getLine();
        ExprNode cond  = (ExprNode) visit(ctx.cond);
        ExprNode tTrue = (ExprNode) visit(ctx.trueExpr);
        ExprNode tFalse= (ExprNode) visit(ctx.falseExpr);
        return new BinaryExprNode(line, cond, "ternary_if",
                new BinaryExprNode(line, tTrue, "ternary_else", tFalse));
    }


    // POSTFIX & MEMBER ACCESS

    @Override
    public Node visitAtomId(python_parser.AtomIdContext ctx) {
        int    line = ctx.getStart().getLine();
        String name = ctx.IDENTIFIER().getText();

        symbolTable.recordUsage(name, line);
        return new IdentifierNode(line, name);
    }

    @Override
    public Node visitMemberAccess(python_parser.MemberAccessContext ctx) {
        return new MemberAccessNode(ctx.getStart().getLine(),
                (ExprNode) visit(ctx.postfix()), ctx.field.getText());
    }

    @Override
    public Node visitIndexAccess(python_parser.IndexAccessContext ctx) {
        return new IndexAccessNode(ctx.getStart().getLine(),
                (ExprNode) visit(ctx.postfix()), (ExprNode) visit(ctx.index));
    }

    @Override
    public Node visitFuncCall(python_parser.FuncCallContext ctx) {
        int line = ctx.getStart().getLine();
        ExprNode targetNode = (ExprNode) visit(ctx.postfix());

        List<ExprNode> args = new ArrayList<>();
        if (ctx.args() instanceof python_parser.ArgListContext) {
            for (python_parser.ArgContext a :
                    ((python_parser.ArgListContext) ctx.args()).arg())
                args.add((ExprNode) visit(a));
        }

        if (targetNode instanceof IdentifierNode) {
            String funcName = ((IdentifierNode) targetNode).getName();
            SymbolRow row = symbolTable.lookup(funcName);
            if (row != null) {
                row.setAttribute("called", true);
            }
        }

        return new FuncCallNode(line, targetNode, args);
    }

    @Override
    public Node visitPrimLambda(python_parser.PrimLambdaContext ctx) {
        int line = ctx.getStart().getLine();
        symbolTable.allocate("lambda@" + line);

        List<ExprNode> lambdaParams = new ArrayList<>();
        if (ctx.params() instanceof python_parser.ParamListContext) {
            for (python_parser.ParamContext p : ((python_parser.ParamListContext) ctx.params()).param())
                lambdaParams.add((ExprNode) visit(p));
        }
        ExprNode bodyNode = (ExprNode) visit(ctx.body);

        symbolTable.free();
        return new LambdaExprNode(line, lambdaParams, bodyNode);
    }
    // ARGS

    @Override
    public Node visitKwArg(python_parser.KwArgContext ctx) {
        return new KwArgNode(ctx.getStart().getLine(),
                ctx.name.getText(), (ExprNode) visit(ctx.value));
    }

    @Override
    public Node visitPosArg(python_parser.PosArgContext ctx) {
        return visit(ctx.value);
    }


    // COLLECTIONS

    @Override
    public Node visitListLit(python_parser.ListLitContext ctx) {
        int line = ctx.getStart().getLine();
        List<ExprNode> items = new ArrayList<>();
        if (ctx.items instanceof python_parser.ArgListContext)
            for (python_parser.ArgContext a : ((python_parser.ArgListContext) ctx.items).arg())
                items.add((ExprNode) visit(a));
        return new ListNode(line, items);
    }

    @Override
    public Node visitEmptyTuple(python_parser.EmptyTupleContext ctx) {
        return new TupleNode(ctx.getStart().getLine(), new ArrayList<>());
    }

    @Override
    public Node visitTupleLit(python_parser.TupleLitContext ctx) {
        int line = ctx.getStart().getLine();
        List<ExprNode> items = new ArrayList<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            org.antlr.v4.runtime.tree.ParseTree child = ctx.getChild(i);
            if (child instanceof python_parser.ArgContext) {
                Node n = visit(child);
                if (n instanceof ExprNode) items.add((ExprNode) n);
            }
        }
        return new TupleNode(line, items);
    }

    @Override
    public Node visitDictLit(python_parser.DictLitContext ctx) {
        int line = ctx.getStart().getLine();
        List<DictEntry> entries = new ArrayList<>();
        if (ctx.pairs instanceof python_parser.DictItemListContext) {
            for (python_parser.DictItemContext item
                    : ((python_parser.DictItemListContext) ctx.pairs).dictItem()) {
                if (item instanceof python_parser.DictPairContext) {
                    python_parser.DictPairContext pair = (python_parser.DictPairContext) item;
                    entries.add(new DictEntry((ExprNode) visit(pair.key),
                            (ExprNode) visit(pair.value)));
                }
            }
        }
        return new DictNode(line, entries);
    }

    @Override
    public Node visitDictPair(python_parser.DictPairContext ctx) { return null; }

    @Override
    public Node visitSetLit(python_parser.SetLitContext ctx) {
        int line = ctx.getStart().getLine();
        List<ExprNode> items = new ArrayList<>();
        if (ctx.items instanceof python_parser.ArgListContext)
            for (python_parser.ArgContext a : ((python_parser.ArgListContext) ctx.items).arg())
                items.add((ExprNode) visit(a));
        return new SetNode(line, items);
    }

    @Override
    public Node visitRangeLit(python_parser.RangeLitContext ctx) {
        int line = ctx.getStart().getLine();
        List<ExprNode> args = new ArrayList<>();
        if (ctx.args() instanceof python_parser.ArgListContext)
            for (python_parser.ArgContext a : ((python_parser.ArgListContext) ctx.args()).arg())
                args.add((ExprNode) visit(a));
        return new RangeNode(line, args);
    }


    // LITERALS

    @Override public Node visitLitNum(python_parser.LitNumContext ctx)   { return new NumberNode(ctx.getStart().getLine(),  ctx.NUMBER().getText()); }
    @Override public Node visitLitStr(python_parser.LitStrContext ctx)   { return new StringNode(ctx.getStart().getLine(),  ctx.STRING().getText()); }
    @Override public Node visitLitTrue(python_parser.LitTrueContext ctx) { return new BooleanNode(ctx.getStart().getLine(), true); }
    @Override public Node visitLitFalse(python_parser.LitFalseContext ctx){ return new BooleanNode(ctx.getStart().getLine(), false); }
    @Override public Node visitLitNull(python_parser.LitNullContext ctx)  { return new NullNode(ctx.getStart().getLine()); }



    @Override
    public Node visitPrimGrouped(python_parser.PrimGroupedContext ctx) {
        return visit(ctx.inner);
    }

    @Override public Node visitPrimLiteral(python_parser.PrimLiteralContext ctx)     {
        return visit(ctx.literal()); }
    @Override public Node visitPrimPostfix(python_parser.PrimPostfixContext ctx)     { return visit(ctx.postfix()); }
    @Override public Node visitPrimCollection(python_parser.PrimCollectionContext ctx){ return visit(ctx.collection()); }
    @Override public Node visitExprPassthrough(python_parser.ExprPassthroughContext ctx) { return visit(ctx.orExpr()); }
    @Override public Node visitOrPassthrough(python_parser.OrPassthroughContext ctx)    { return visit(ctx.andExpr()); }
    @Override public Node visitAndPassthrough(python_parser.AndPassthroughContext ctx)  { return visit(ctx.cmpExpr()); }
    @Override public Node visitCmpPassthrough(python_parser.CmpPassthroughContext ctx)  { return visit(ctx.addExpr()); }
    @Override public Node visitAddPassthrough(python_parser.AddPassthroughContext ctx)  { return visit(ctx.mulExpr()); }
    @Override public Node visitMulPassthrough(python_parser.MulPassthroughContext ctx)  { return visit(ctx.unary()); }
    @Override public Node visitUnaryPassthrough(python_parser.UnaryPassthroughContext ctx){ return visit(ctx.primary()); }
}