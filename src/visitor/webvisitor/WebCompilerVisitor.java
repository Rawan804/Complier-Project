package visitor.webvisitor;

import AST.hast.ASTNode;
import org.antlr.v4.runtime.tree.ParseTree;
import AST.hast.DocumentNode;
import AST.hast.ElementNode;
import AST.hast.AttributeNode;
import AST.hast.TextNode;

import AST.hast.jinja.JinjaExprNode;
import AST.hast.jinja.JinjaForNode;
import AST.hast.jinja.JinjaIfNode;

import AST.cssast.*;
import AST.webast.WebASTNode;
import LexerandParser.webantlr.WebParser;
import LexerandParser.webantlr.WebParserBaseVisitor;
import SymbolTable.webSymboltable.WebSymbolTable;

import org.antlr.v4.runtime.ParserRuleContext;


import java.util.*;


public class WebCompilerVisitor extends WebParserBaseVisitor<WebASTNode> {


    private final WebSymbolTable symbolTable;
    // يخزن السلكتور الحالي أثناء زيارة CSS
    private String currentSelector = "";

    public WebSymbolTable getSymbolTable() {
        return symbolTable;
    }

    public WebCompilerVisitor(WebSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }



    @Override
    public WebASTNode visitHtmlNode(WebParser.HtmlNodeContext ctx) {
        symbolTable.enterHtmlScope("html");
        DocumentNode docNode = new DocumentNode(ctx.start.getLine(), ctx.start.getCharPositionInLine());

        if (ctx.doctype() != null) visit(ctx.doctype());

        if (ctx.head() != null || ctx.body() != null) {
            registerElement("html");
            ElementNode htmlElem = new ElementNode("html", ctx.start.getLine(), ctx.start.getCharPositionInLine());
            if (ctx.head() != null) htmlElem.addChild(visit(ctx.head()));
            if (ctx.body() != null) htmlElem.addChild(visit(ctx.body()));
            docNode.setHtmlElement(htmlElem);
            docNode.addNode(htmlElem);
        }

        symbolTable.exitHtmlScope();
        return docNode;
    }



    @Override
    public WebASTNode visitDoctypeNode(WebParser.DoctypeNodeContext ctx) {
        return null;
    }

    @Override
    public WebASTNode visitHeadNode(WebParser.HeadNodeContext ctx) {
        symbolTable.enterHtmlScope("head");
        registerElement("head");
        ElementNode headNode = new ElementNode("head", ctx.start.getLine(), ctx.start.getCharPositionInLine());

        if (ctx.attribute() != null) {
            for (WebParser.AttributeContext attrCtx : ctx.attribute()) {
                AttributeNode attr = (AttributeNode) visit(attrCtx);
                if (attr != null) headNode.addAttribute(attr);
            }
        }
        if (ctx.title() != null)  headNode.addChild(visit(ctx.title()));
        if (ctx.link()  != null) {
            for (WebParser.LinkContext lCtx : ctx.link())
                headNode.addChild(visit(lCtx));
        }
        if (ctx.style() != null) {
            for (WebParser.StyleContext styleCtx : ctx.style()) {
                headNode.addChild(visit(styleCtx));
            }        }

        symbolTable.exitHtmlScope();
        return headNode;
    }
    @Override
    public WebASTNode visitStyleNode(WebParser.StyleNodeContext ctx) {
        registerElement("style");
        ElementNode styleNode =
                new ElementNode("style",
                        ctx.start.getLine(),
                        ctx.start.getCharPositionInLine());

        if (ctx.stylesheet() != null) {
            styleNode.addChild(visit(ctx.stylesheet()));
        }

        return styleNode;
    }

    @Override
    public WebASTNode visitTitleNode(WebParser.TitleNodeContext ctx) {
        registerElement("title");
        ElementNode titleNode = new ElementNode("title", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        if (ctx.inlineContent() != null) {
            for (WebParser.InlineContentContext c : ctx.inlineContent()) {
                WebASTNode child = visit(c);
                if (child != null) titleNode.addChild(child);
            }
        }
        return titleNode;
    }

    @Override
    public WebASTNode visitBodyNode(WebParser.BodyNodeContext ctx) {
        symbolTable.enterHtmlScope("body");
        registerElement("body");
        ElementNode bodyNode = new ElementNode("body", ctx.start.getLine(), ctx.start.getCharPositionInLine());

        if (ctx.attribute() != null) {
            for (WebParser.AttributeContext attrCtx : ctx.attribute()) {
                AttributeNode attr = (AttributeNode) visit(attrCtx);
                if (attr != null) bodyNode.addAttribute(attr);
            }
        }
        if (ctx.content() != null) {
            for (WebParser.ContentContext c : ctx.content()) {
                WebASTNode child = visit(c);
                if (child != null) bodyNode.addChild(child);
            }
        }

        symbolTable.exitHtmlScope();
        return bodyNode;
    }

    @Override
    public WebASTNode visitDivNode(WebParser.DivNodeContext ctx) {
        return createElementNode("div", ctx.attribute(), ctx.content(),
                ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public WebASTNode visitPNode(WebParser.PNodeContext ctx) {
        return createInlineElementNode("p", ctx.attribute(), ctx.inlineContent(),
                ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override public WebASTNode visitH1Node(WebParser.H1NodeContext ctx) { return createInlineElementNode("h1", ctx.attribute(), ctx.inlineContent(), ctx.start.getLine(), ctx.start.getCharPositionInLine()); }
    @Override public WebASTNode visitH2Node(WebParser.H2NodeContext ctx) { return createInlineElementNode("h2", ctx.attribute(), ctx.inlineContent(), ctx.start.getLine(), ctx.start.getCharPositionInLine()); }
    @Override public WebASTNode visitH3Node(WebParser.H3NodeContext ctx) { return createInlineElementNode("h3", ctx.attribute(), ctx.inlineContent(), ctx.start.getLine(), ctx.start.getCharPositionInLine()); }
    @Override public WebASTNode visitH4Node(WebParser.H4NodeContext ctx) { return createInlineElementNode("h4", ctx.attribute(), ctx.inlineContent(), ctx.start.getLine(), ctx.start.getCharPositionInLine()); }
    @Override public WebASTNode visitH5Node(WebParser.H5NodeContext ctx) { return createInlineElementNode("h5", ctx.attribute(), ctx.inlineContent(), ctx.start.getLine(), ctx.start.getCharPositionInLine()); }
    @Override public WebASTNode visitH6Node(WebParser.H6NodeContext ctx) { return createInlineElementNode("h6", ctx.attribute(), ctx.inlineContent(), ctx.start.getLine(), ctx.start.getCharPositionInLine()); }

    @Override
    public WebASTNode visitSpanNode(WebParser.SpanNodeContext ctx) {
        return createInlineElementNode("span", ctx.attribute(), ctx.inlineContent(),
                ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public WebASTNode visitUlNode(WebParser.UlNodeContext ctx) {
        registerElement("ul");
        ElementNode ulNode = new ElementNode("ul", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        if (ctx.attribute() != null) {
            for (WebParser.AttributeContext attrCtx : ctx.attribute()) {
                AttributeNode attr = (AttributeNode) visit(attrCtx);
                if (attr != null) ulNode.addAttribute(attr);
            }
        }
        for (int i = 0; i < ctx.getChildCount(); i++) {
            WebASTNode child = visit(ctx.getChild(i));
            if (child != null && !(child instanceof AttributeNode)) ulNode.addChild(child);
        }
        return ulNode;
    }

    @Override
    public WebASTNode visitLiNode(WebParser.LiNodeContext ctx) {
        registerElement("li");
        ElementNode liNode = new ElementNode("li", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        if (ctx.attribute() != null) {
            for (WebParser.AttributeContext attrCtx : ctx.attribute()) {
                AttributeNode attr = (AttributeNode) visit(attrCtx);
                if (attr != null) liNode.addAttribute(attr);
            }
        }
        if (ctx.content() != null) {
            for (WebParser.ContentContext c : ctx.content()) {
                WebASTNode child = visit(c);
                if (child != null) liNode.addChild(child);
            }
        }
        return liNode;
    }

    @Override
    public WebASTNode visitANode(WebParser.ANodeContext ctx) {
        registerElement("a");
        ElementNode aNode = new ElementNode("a", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        addAttributes(aNode, ctx.attribute());
        if (ctx.content() != null) {
            for (WebParser.ContentContext c : ctx.content()) {
                WebASTNode child = visit(c);
                if (child != null) aNode.addChild(child);
            }
        }
        return aNode;
    }

    @Override
    public WebASTNode visitImgNode(WebParser.ImgNodeContext ctx) {
        registerElement("img");
        ElementNode imgNode = new ElementNode("img", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        imgNode.setSelfClosing(true);
        addAttributes(imgNode, ctx.attribute());
        return imgNode;
    }

    @Override
    public WebASTNode visitBrNode(WebParser.BrNodeContext ctx) {
        registerElement("br");
        ElementNode brNode = new ElementNode("br", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        brNode.setSelfClosing(true);
        return brNode;
    }

    @Override
    public WebASTNode visitLinkNode(WebParser.LinkNodeContext ctx) {
        registerElement("link");
        ElementNode linkNode = new ElementNode("link", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        linkNode.setSelfClosing(true);
        addAttributes(linkNode, ctx.attribute());
        return linkNode;
    }

    @Override
    public WebASTNode visitFormNode(WebParser.FormNodeContext ctx) {
        registerElement("form");
        ElementNode formNode = new ElementNode("form", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        addAttributes(formNode, ctx.attribute());
        if (ctx.content() != null) {
            for (WebParser.ContentContext c : ctx.content()) {
                WebASTNode child = visit(c);
                if (child != null) formNode.addChild(child);
            }
        }
        return formNode;
    }

    @Override
    public WebASTNode visitButtonNode(WebParser.ButtonNodeContext ctx) {
        registerElement("button");
        ElementNode buttonNode = new ElementNode("button", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        addAttributes(buttonNode, ctx.attribute());
        if (ctx.content() != null) {
            for (WebParser.ContentContext c : ctx.content()) {
                WebASTNode child = visit(c);
                if (child != null) buttonNode.addChild(child);
            }
        }
        return buttonNode;
    }

    @Override
    public WebASTNode visitInputNode(WebParser.InputNodeContext ctx) {
        registerElement("input");
        ElementNode inputNode = new ElementNode("input", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        inputNode.setSelfClosing(true);
        addAttributes(inputNode, ctx.attribute());
        return inputNode;
    }

    @Override
    public WebASTNode visitTextareaNode(WebParser.TextareaNodeContext ctx) {
        registerElement("textarea");
        ElementNode textareaNode = new ElementNode("textarea", ctx.start.getLine(), ctx.start.getCharPositionInLine());
        addAttributes(textareaNode, ctx.attribute());
        if (ctx.content() != null) {
            for (WebParser.ContentContext c : ctx.content()) {
                WebASTNode child = visit(c);
                if (child != null) textareaNode.addChild(child);
            }
        }
        return textareaNode;
    }

    @Override
    public WebASTNode visitStyleAttribute(WebParser.StyleAttributeContext ctx) {
        String value = stripQuotes(ctx.STRING().getText());
        symbolTable.defineHtml("style", "Value_of_style", value);
        return new AttributeNode("style", value, ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public WebASTNode visitValuedAttribute(WebParser.ValuedAttributeContext ctx) {
        String name = getAttrNameText(ctx.attrName());
        WebParser.AttrValueContext valCtx = ctx.attrValue();
        int line = ctx.start.getLine();
        int col = ctx.start.getCharPositionInLine();

        if (valCtx instanceof WebParser.JinjaValContext jinjaVal) {
            WebParser.JinjaExprNodeContext exprCtx = (WebParser.JinjaExprNodeContext) jinjaVal.jinjaExpr();
            String expr = exprCtx.jinjaExpression().getText().trim();
            processJinjaExpression(expr);
            registerHtmlAttribute(name, expr);
            return new AttributeNode(name, expr, true, line, col);
        }

        String rawValue = stripQuotes(valCtx.getText());

        // ⬇️ جديد: نتحقق يدويًا إذا القيمة كاملها عبارة عن {{ ... }}
        // (حالة src="{{ product.image }}" التي ابتلعتها STRING بالكامل)
        java.util.regex.Matcher jinjaMatch =
                java.util.regex.Pattern.compile("^\\{\\{\\s*(.*?)\\s*\\}\\}$").matcher(rawValue);

        if (jinjaMatch.matches()) {
            String expr = jinjaMatch.group(1).trim();
            processJinjaExpression(expr);
            registerHtmlAttribute(name, expr);
            return new AttributeNode(name, expr, true, line, col);
        }

        symbolTable.defineHtml(name, "Value_of_" + name, rawValue);
        registerHtmlAttribute(name, rawValue);
        return new AttributeNode(name, rawValue, line, col);
    }

    @Override
    public WebASTNode visitBooleanAttribute(WebParser.BooleanAttributeContext ctx) {
        String name = getAttrNameText(ctx.attrName());
        return new AttributeNode(name, "true", ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public WebASTNode visitPlainText(WebParser.PlainTextContext ctx) {
        return new TextNode(ctx.TEXT().getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }

    @Override
    public WebASTNode visitJinjaExprNode(WebParser.JinjaExprNodeContext ctx) {

        String expr = ctx.jinjaExpression().getText().trim();

        // نسجّل الـ properties فقط للمتغيرات المعرّفة مسبقاً
        // الـ semantic check يصير في WebSemanticAnalyzer
        processJinjaExpression(expr);

        return new JinjaExprNode(
                expr,
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine()
        );
    }

    /**
     * يسجّل properties للمتغيرات المعرّفة فقط - ما يعرّف متغيرات جديدة
     */
    private void processJinjaExpression(String expr) {
        String[] parts = expr.split("[\\s,()=|+\\-*/<>!]+");

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            if (part.startsWith("'") || part.startsWith("\"")) continue;
            if (part.matches("-?\\d+(\\.\\d+)?")) continue;

            if (part.contains(".")) {
                String[] dotParts = part.split("\\.");
                String baseVar = dotParts[0];
                // نسجّل الـ properties فقط إذا المتغير معرّف
                if (symbolTable.jinjaLookup(baseVar)) {
                    for (int i = 1; i < dotParts.length; i++) {
                        symbolTable.registerJinjaProperty(baseVar, dotParts[i]);
                    }
                }
            }
        }
    }

    @Override
    public WebASTNode visitJinjaForNode(WebParser.JinjaForNodeContext ctx) {

        String variable = ctx.JINJA_ID().getText();
        String iterable = ctx.jinjaValue().getText();

        symbolTable.enterJinjaScope();
        symbolTable.defineJinja(variable, "loop_variable", iterable);

        JinjaForNode node = new JinjaForNode(
                variable,
                iterable,
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine()
        );

        for (WebParser.ContentContext c : ctx.content()) {
            WebASTNode child = visit(c);
            if (child != null && child instanceof ASTNode astChild) {
                node.addChild(astChild);
            }
        }

        symbolTable.exitJinjaScope();

        return node;
    }
    @Override
    public WebASTNode visitJinjaIfNode(WebParser.JinjaIfNodeContext ctx) {

        String condition = ctx.condition().getText();

        symbolTable.enterJinjaScope();

        JinjaIfNode node = new JinjaIfNode(
                condition,
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine()
        );

        if (ctx.ifBody() != null) {
            WebParser.IfBodyNodeContext ifBody = (WebParser.IfBodyNodeContext) ctx.ifBody();
            for (WebParser.ContentContext c : ifBody.content()) {
                WebASTNode child = visit(c);
                if (child != null && child instanceof ASTNode astChild) {
                    node.addThenChild(astChild);
                }
            }
        }

        if (ctx.elseBody() != null) {
            WebParser.ElseBodyNodeContext elseBody = (WebParser.ElseBodyNodeContext) ctx.elseBody();
            for (WebParser.ContentContext c : elseBody.content()) {
                WebASTNode child = visit(c);
                if (child != null && child instanceof ASTNode astChild) {
                    node.addElseChild(astChild);
                }
            }
        }

        symbolTable.exitJinjaScope();

        return node;
    }
    private String extractBaseVar(String expr) {
        if (expr.contains(".")) {
            return expr.split("\\.")[0];
        }
        return expr;
    }

    // ============================================================
    //  CSS VISIT METHODS
    // ============================================================

    @Override
    public WebASTNode visitStylesheetNode(WebParser.StylesheetNodeContext ctx) {
        StylesheetNode node = new StylesheetNode(ctx.getStart().getLine());
        for (WebParser.RuleSetContext rCtx : ctx.ruleSet()) {
            node.addRuleSet((RuleSetNode) visit(rCtx));
        }
        return node;
    }

    @Override
    public WebASTNode visitRuleSetNode(WebParser.RuleSetNodeContext ctx) {
        if (ctx.selectorGroup() != null) currentSelector = ctx.selectorGroup().getText();

        RuleSetNode node = new RuleSetNode(ctx.getStart().getLine());
        if (ctx.selectorGroup() != null) {
            node.setSelectorText(getOriginalText(ctx.selectorGroup()));            node.setSelectorGroup((SelectorGroupNode) visit(ctx.selectorGroup()));
        }
        for (WebParser.DeclarationContext dCtx : ctx.declaration()) {
            node.addDeclaration((DeclarationNode) visit(dCtx));
        }
        return node;
    }

    @Override
    public WebASTNode visitSelectorGroupNode(WebParser.SelectorGroupNodeContext ctx) {
        SelectorGroupNode node = new SelectorGroupNode(ctx.getStart().getLine());
        for (WebParser.SelectorContext sCtx : ctx.selector()) {
            node.addSelector((SelectorNode) visit(sCtx));
        }
        return node;
    }

    @Override
    public WebASTNode visitSelectorNode(WebParser.SelectorNodeContext ctx) {
        SelectorNode node = new SelectorNode(ctx.getStart().getLine());
        for (WebParser.SimpleSelectorContext sCtx : ctx.simpleSelector()) {
            WebASTNode child = visit(sCtx);
            if (child instanceof SimpleSelectorNode) node.setSimpleSelector((SimpleSelectorNode) child);
        }
        return node;
    }

    @Override
    public WebASTNode visitIdSelectorNode(WebParser.IdSelectorNodeContext ctx) {
        SimpleSelectorNode simple = new SimpleSelectorNode(ctx.getStart().getLine());
        simple.addModifier(new SelectorModifierNode(SelectorModifierNode.Type.ID, ctx.getText(), ctx.getStart().getLine()));
        return simple;
    }

    @Override
    public WebASTNode visitClassSelectorNode(WebParser.ClassSelectorNodeContext ctx) {
        SimpleSelectorNode simple = new SimpleSelectorNode(ctx.getStart().getLine());
        simple.addModifier(new SelectorModifierNode(SelectorModifierNode.Type.CLASS, ctx.getText(), ctx.getStart().getLine()));
        return simple;
    }

    @Override
    public WebASTNode visitElementSelectorNode(WebParser.ElementSelectorNodeContext ctx) {
        SimpleSelectorNode simple = new SimpleSelectorNode(ctx.getStart().getLine());
        simple.setElementName(new ElementNameNode(ctx.getText(), ctx.getStart().getLine()));
        return simple;
    }

    @Override
    public WebASTNode visitStarSelectorNode(WebParser.StarSelectorNodeContext ctx) {
        SimpleSelectorNode simple = new SimpleSelectorNode(ctx.getStart().getLine());
        simple.addModifier(new SelectorModifierNode(SelectorModifierNode.Type.PSEUDO, "*", ctx.getStart().getLine()));
        return simple;
    }

    @Override
    public WebASTNode visitPseudoSelectorNode(WebParser.PseudoSelectorNodeContext ctx) {
        SimpleSelectorNode simple = new SimpleSelectorNode(ctx.getStart().getLine());
        simple.addModifier(new SelectorModifierNode(SelectorModifierNode.Type.PSEUDO, ctx.getText(), ctx.getStart().getLine()));
        return simple;
    }

    @Override
    public WebASTNode visitNotSelectorNode(WebParser.NotSelectorNodeContext ctx) {
        return visit(ctx.notSelector());
    }

    @Override
    public WebASTNode visitNotSelectorNodeAlt(WebParser.NotSelectorNodeAltContext ctx) {
        SimpleSelectorNode simple = new SimpleSelectorNode(ctx.getStart().getLine());
        simple.addModifier(new SelectorModifierNode(SelectorModifierNode.Type.PSEUDO, ctx.getText(), ctx.getStart().getLine()));
        return simple;
    }

    // Declaration delegators
    @Override public WebASTNode visitColorDeclNode(WebParser.ColorDeclNodeContext ctx)                       { return visit(ctx.colorDecl()); }
    @Override public WebASTNode visitBackgroundColorDeclNode(WebParser.BackgroundColorDeclNodeContext ctx)   { return visit(ctx.backgroundColorDecl()); }
    @Override public WebASTNode visitWidthDeclNode(WebParser.WidthDeclNodeContext ctx)                       { return visit(ctx.widthDecl()); }
    @Override public WebASTNode visitHeightDeclNode(WebParser.HeightDeclNodeContext ctx)                     { return visit(ctx.heightDecl()); }
    @Override public WebASTNode visitMarginDeclNode(WebParser.MarginDeclNodeContext ctx)                     { return visit(ctx.marginDecl()); }
    @Override public WebASTNode visitPaddingDeclNode(WebParser.PaddingDeclNodeContext ctx)                   { return visit(ctx.paddingDecl()); }
    @Override public WebASTNode visitBorderDeclNode(WebParser.BorderDeclNodeContext ctx)                     { return visit(ctx.borderDecl()); }
    @Override public WebASTNode visitFontSizeDeclNode(WebParser.FontSizeDeclNodeContext ctx)                 { return visit(ctx.fontSizeDecl()); }
    @Override public WebASTNode visitDisplayDeclNode(WebParser.DisplayDeclNodeContext ctx)                   { return visit(ctx.displayDecl()); }
    @Override public WebASTNode visitPositionDeclNode(WebParser.PositionDeclNodeContext ctx)                 { return visit(ctx.positionDecl()); }
    @Override public WebASTNode visitFontWeightDeclNode(WebParser.FontWeightDeclNodeContext ctx)             { return visit(ctx.fontWeightDecl()); }
    @Override public WebASTNode visitTextAlignDeclNode(WebParser.TextAlignDeclNodeContext ctx)               { return visit(ctx.textAlignDecl()); }
    @Override public WebASTNode visitOpacityDeclNode(WebParser.OpacityDeclNodeContext ctx)                   { return visit(ctx.opacityDecl()); }
    @Override public WebASTNode visitGridTemplateColumnsDeclNode(WebParser.GridTemplateColumnsDeclNodeContext ctx) { return visit(ctx.gridTemplateColumnsDecl()); }
    @Override public WebASTNode visitGridGapDeclNode(WebParser.GridGapDeclNodeContext ctx)                   { return visit(ctx.gridGapDecl()); }

    private WebASTNode handleDeclaration(ParserRuleContext ctx, String property) {

        String value = extractValue(ctx);

        symbolTable.defineCss(currentSelector, property, value);

        return new DeclarationNode(
                property,
                value,
                ctx.getStart().getLine()
        );
    }

    @Override public WebASTNode visitColorDeclNodeAlt(WebParser.ColorDeclNodeAltContext ctx)                                 { return handleDeclaration(ctx, "color"); }
    @Override public WebASTNode visitBackgroundColorDeclNodeAlt(WebParser.BackgroundColorDeclNodeAltContext ctx)             { return handleDeclaration(ctx, "background-color"); }
    @Override public WebASTNode visitWidthDeclNodeAlt(WebParser.WidthDeclNodeAltContext ctx)                                 { return handleDeclaration(ctx, "width"); }
    @Override public WebASTNode visitHeightDeclNodeAlt(WebParser.HeightDeclNodeAltContext ctx)                               { return handleDeclaration(ctx, "height"); }
    @Override public WebASTNode visitMarginDeclNodeAlt(WebParser.MarginDeclNodeAltContext ctx)                               { return handleDeclaration(ctx, "margin"); }
    @Override public WebASTNode visitPaddingDeclNodeAlt(WebParser.PaddingDeclNodeAltContext ctx)                             { return handleDeclaration(ctx, "padding"); }
    @Override public WebASTNode visitBorderDeclNodeAlt(WebParser.BorderDeclNodeAltContext ctx)                               { return handleDeclaration(ctx, "border"); }
    @Override public WebASTNode visitDisplayDeclNodeAlt(WebParser.DisplayDeclNodeAltContext ctx)                             { return handleDeclaration(ctx, "display"); }
    @Override public WebASTNode visitPositionDeclNodeAlt(WebParser.PositionDeclNodeAltContext ctx)                           { return handleDeclaration(ctx, "position"); }
    @Override public WebASTNode visitFontSizeDeclNodeAlt(WebParser.FontSizeDeclNodeAltContext ctx)                           { return handleDeclaration(ctx, "font-size"); }
    @Override public WebASTNode visitFontWeightDeclNodeAlt(WebParser.FontWeightDeclNodeAltContext ctx)                       { return handleDeclaration(ctx, "font-weight"); }
    @Override public WebASTNode visitTextAlignDeclNodeAlt(WebParser.TextAlignDeclNodeAltContext ctx)                         { return handleDeclaration(ctx, "text-align"); }
    @Override public WebASTNode visitOpacityDeclNodeAlt(WebParser.OpacityDeclNodeAltContext ctx)                             { return handleDeclaration(ctx, "opacity"); }
    @Override public WebASTNode visitGridTemplateColumnsDeclNodeAlt(WebParser.GridTemplateColumnsDeclNodeAltContext ctx)     { return handleDeclaration(ctx, "grid-template-columns"); }
    @Override public WebASTNode visitGridGapDeclNodeAlt(WebParser.GridGapDeclNodeAltContext ctx)                             { return handleDeclaration(ctx, "gap"); }


    // ============================================================
    //  HELPER METHODS
    // ============================================================

    private void addAttributes(ElementNode node, List<WebParser.AttributeContext> attributes) {
        if (attributes == null) return;
        for (WebParser.AttributeContext attrCtx : attributes) {
            AttributeNode attr = (AttributeNode) visit(attrCtx);
            if (attr != null) node.addAttribute(attr);
        }
    }

    private String getAttrNameText(WebParser.AttrNameContext ctx) {
        return ctx.getText();
    }

    private String stripQuotes(String text) {
        if (text.length() >= 2 && (text.startsWith("\"") || text.startsWith("'"))) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private void registerElement(String tagName) {
        symbolTable.defineHtml(tagName, "Element", tagName);
    }

    private void registerHtmlAttribute(String name, String value) {
        switch (name) {
            case "href" -> symbolTable.defineHtml("href", "Attribute_href", value);
            case "action" -> symbolTable.defineHtml("action", "Form_Action", value);
            case "method" -> symbolTable.defineHtml("method", "Form_Method", value);
            case "type" -> symbolTable.defineHtml("type", "Input_Type", value);
            case "id" -> symbolTable.defineHtml(value, "id", value);
            case "class" -> {
                for (String cls : value.split("\\s+")) {
                    if (!cls.isEmpty()) {
                        symbolTable.defineHtml(cls, "class", cls);
                    }
                }
            }
            case "name" -> symbolTable.defineHtml(value, "name", value);
            case "src" -> symbolTable.defineHtml("src", "Attribute_src", value);
            case "placeholder" -> symbolTable.defineHtml("placeholder", "Attribute_placeholder", value);
            case "rel" -> symbolTable.defineHtml("rel", "Attribute_rel", value);
            case "required" -> symbolTable.defineHtml("required", "Attribute_required", "true");
            default -> { }
        }
    }

    private ElementNode createElementNode(String tagName,
                                          List<WebParser.AttributeContext> attributes,
                                          List<WebParser.ContentContext> contents,
                                          int line, int col) {
        registerElement(tagName);
        ElementNode node = new ElementNode(tagName, line, col);
        if (attributes != null) {
            for (WebParser.AttributeContext attrCtx : attributes) {
                AttributeNode attr = (AttributeNode) visit(attrCtx);
                if (attr != null) node.addAttribute(attr);
            }
        }
        if (contents != null) {
            for (WebParser.ContentContext contentCtx : contents) {
                WebASTNode child = visit(contentCtx);
                if (child != null) node.addChild(child);
            }
        }
        return node;
    }

    private ElementNode createInlineElementNode(String tagName,
                                                List<WebParser.AttributeContext> attributes,
                                                List<WebParser.InlineContentContext> contents,
                                                int line, int col) {
        registerElement(tagName);
        ElementNode node = new ElementNode(tagName, line, col);
        if (attributes != null) {
            for (WebParser.AttributeContext attrCtx : attributes) {
                AttributeNode attr = (AttributeNode) visit(attrCtx);
                if (attr != null) node.addAttribute(attr);
            }
        }
        if (contents != null) {
            for (WebParser.InlineContentContext contentCtx : contents) {
                WebASTNode child = visit(contentCtx);
                if (child != null) node.addChild(child);
            }
        }
        return node;
    }

    private String extractValue(ParserRuleContext ctx) {

        StringBuilder sb = new StringBuilder();
        boolean afterColon = false;

        for (int i = 0; i < ctx.getChildCount(); i++) {

            ParseTree child = ctx.getChild(i);
            String childText = child.getText();

            if (childText.equals(":")) {
                afterColon = true;
                continue;
            }
            if (childText.equals(";")) {
                break;
            }

            if (afterColon) {
                appendTokensWithSpaces(child, sb);
            }
        }

        return sb.toString().trim();
    }

    // ⬇️ جديد: يمشي بكل ورقة (Terminal Node) بعمق الشجرة ويحط مسافة بينهم
    private void appendTokensWithSpaces(ParseTree node, StringBuilder sb) {
        if (node.getChildCount() == 0) {
            // ورقة (terminal token) - نضيفها مباشرة
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                sb.append(' ');
            }
            sb.append(node.getText());
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                appendTokensWithSpaces(node.getChild(i), sb);
            }
        }

    }
    private String getOriginalText(ParserRuleContext ctx) {
        if (ctx == null || ctx.start == null || ctx.stop == null) return "";

        org.antlr.v4.runtime.CharStream input = ctx.start.getInputStream();
        return input.getText(
                org.antlr.v4.runtime.misc.Interval.of(
                        ctx.start.getStartIndex(),
                        ctx.stop.getStopIndex()
                )
        );
    }
}