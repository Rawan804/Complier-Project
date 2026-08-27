parser grammar WebParser;

@header {
package LexerandParser.webantlr;
}

options { tokenVocab = WebLexer; }


// ============================================================
//  HTML + JINJA RULES
// ============================================================

doctype
    : OPEN DOC DOCTYPE HTML CLOSE
    #doctypeNode
    ;

html
    : doctype
      OPEN HTML CLOSE
        head?
        body?
      OPEN SLASH HTML CLOSE
    #htmlNode
    ;

head
    : OPEN HEAD attribute* CLOSE
        title?
        link*
        style*
      OPEN SLASH HEAD CLOSE
    #headNode
    ;

title
    : OPEN TITLE attribute* CLOSE
        inlineContent*
      OPEN SLASH TITLE CLOSE
    #titleNode
    ;

body
    : OPEN BODY attribute* CLOSE
        content*
      OPEN SLASH BODY CLOSE
    #bodyNode
    ;

content
    : element        #elementContent
    | jinjaExpr      #jinjaExprContent
    | jinjaStmt      #jinjaStmtContent
    | textContent    #textContentContent
    ;

element
    : div
    | h1 | h2 | h3 | h4 | h5 | h6
    | p
    | span
    | a
    | ul
    | li
    | form
    | button
    | input
    | textarea
    | img
    | br
    | link
    ;

textContent
    : TEXT        #plainText
    ;

inlineContent
    : textContent
    | jinjaExpr
    | jinjaStmt
    ;

div
    : OPEN DIV attribute* CLOSE
        content*
      OPEN SLASH DIV CLOSE
    #divNode
    ;

ul
    : OPEN UL attribute* CLOSE
        (li | content | jinjaStmt)*
      OPEN SLASH UL CLOSE
    #ulNode
    ;

li
    : OPEN LI attribute* CLOSE
        content*
      OPEN SLASH LI CLOSE
    #liNode
    ;

form
    : OPEN FORM attribute* CLOSE
        content*
      OPEN SLASH FORM CLOSE
    #formNode
    ;

h1 : OPEN H1 attribute* CLOSE inlineContent* OPEN SLASH H1 CLOSE #h1Node ;
h2 : OPEN H2 attribute* CLOSE inlineContent* OPEN SLASH H2 CLOSE #h2Node ;
h3 : OPEN H3 attribute* CLOSE inlineContent* OPEN SLASH H3 CLOSE #h3Node ;
h4 : OPEN H4 attribute* CLOSE inlineContent* OPEN SLASH H4 CLOSE #h4Node ;
h5 : OPEN H5 attribute* CLOSE inlineContent* OPEN SLASH H5 CLOSE #h5Node ;
h6 : OPEN H6 attribute* CLOSE inlineContent* OPEN SLASH H6 CLOSE #h6Node ;

p
    : OPEN P attribute* CLOSE inlineContent* OPEN SLASH P CLOSE
    #pNode
    ;

span
    : OPEN SPAN attribute* CLOSE inlineContent* OPEN SLASH SPAN CLOSE
    #spanNode
    ;

a
    : OPEN A attribute* CLOSE content* OPEN SLASH A CLOSE
    #aNode
    ;

button
    : OPEN BUTTON_KW attribute* CLOSE content* OPEN SLASH BUTTON_KW CLOSE
    #buttonNode
    ;

input
    : OPEN INPUT attribute* SLASH? CLOSE
    #inputNode
    ;

textarea
    : OPEN TEXTAREA attribute* CLOSE
        content*
      OPEN SLASH TEXTAREA CLOSE
    #textareaNode
    ;
style
    : OPEN STYLE CLOSE
      stylesheet
      STYLE_END
          #styleNode

    ;
img
    : OPEN IMG attribute*  SLASH CLOSE
    #imgNode
    ;

br
    : OPEN BR SLASH? CLOSE
    #brNode
    ;

link
    : OPEN LINK attribute* SLASH? CLOSE
    #linkNode
    ;

attrName
    : ATR | REL_KW | HREF_KW | SRC | TYPE_KW | NAME
    | PLACEHOLDER | METHOD_KW | ACTION_KW | REQUIRED
    ;

attrValue
    : STRING       #stringVal
    | jinjaExpr    #jinjaVal
    | TYPE         #typeVal
    | METHOD       #methodVal
    | REL          #relVal
    ;

attribute
    : STYLE ATTR_EQ STRING           #styleAttribute
    | attrName                       #booleanAttribute
    | attrName ATTR_EQ attrValue     #valuedAttribute
    ;

jinjaExpr
    : JINJA_EXPR_OPEN jinjaExpression JINJA_EXPR_CLOSE
    #jinjaExprNode
    ;

jinjaExpression
    : JINJA_RAW_EXPR
    #rawJinjaExpressionNode
    ;

jinjaValue
    : JINJA_ID (DOT JINJA_ID)*    #jinjaIdValue
    | NUMBER                       #jinjaNumberValue
    | STRING_J                     #jinjaStringValue
    ;

jinjaFilter
    : JINJA_ID
    #jinjaFilterNode
    ;

jinjaStmt
    : jinjaIf
    | jinjaFor
    ;

jinjaIf
    : JINJA_STMT_OPEN IF condition JINJA_STMT_CLOSE
        ifBody
      elseBody?
      JINJA_STMT_OPEN ENDIF JINJA_STMT_CLOSE
    #jinjaIfNode
    ;

ifBody
    : content*
    #ifBodyNode
    ;

elseBody
    : JINJA_STMT_OPEN ELSE JINJA_STMT_CLOSE content*
    #elseBodyNode
    ;

condition
    : jinjaValue (OP jinjaValue)?
    #conditionNode
    ;

jinjaFor
    : JINJA_STMT_OPEN FOR JINJA_ID IN jinjaValue JINJA_STMT_CLOSE
        content*
      JINJA_STMT_OPEN ENDFOR JINJA_STMT_CLOSE
    #jinjaForNode
    ;


// ============================================================
//  CSS RULES
// ============================================================

stylesheet
    : ruleSet*
    #StylesheetNode
    ;

ruleSet
    : selectorGroup LBRACE declaration* RBRACE
    #RuleSetNode
    ;

selectorGroup
    : selector (COMMA selector)*
    #SelectorGroupNode
    ;

selector
    : simpleSelector+
    #SelectorNode
    ;

simpleSelector
    : CSS_ELEMENT    #ElementSelectorNode
    | CSS_ID         #IdSelectorNode
    | CSS_CLASS      #ClassSelectorNode
    | CSS_STAR       #StarSelectorNode
    | CSS_PSEUDO     #PseudoSelectorNode
    | notSelector    #NotSelectorNode
    ;

notSelector
    : CSS_NOT LPAREN selector RPAREN
    #NotSelectorNodeAlt
    ;

declaration
    : colorDecl               #ColorDeclNode
    | backgroundColorDecl     #BackgroundColorDeclNode
    | widthDecl               #WidthDeclNode
    | heightDecl              #HeightDeclNode
    | marginDecl              #MarginDeclNode
    | paddingDecl             #PaddingDeclNode
    | borderDecl              #BorderDeclNode
    | displayDecl             #DisplayDeclNode
    | positionDecl            #PositionDeclNode
    | fontSizeDecl            #FontSizeDeclNode
    | fontWeightDecl          #FontWeightDeclNode
    | textAlignDecl           #TextAlignDeclNode
    | opacityDecl             #OpacityDeclNode
    | gridTemplateColumnsDecl #GridTemplateColumnsDeclNode
    | gridGapDecl             #GridGapDeclNode
    ;

colorDecl
    : CSS_PROP_COLOR COLON (CSS_COLOR | CSS_ELEMENT) SEMICOLON
    #ColorDeclNodeAlt
    ;

backgroundColorDecl
    : CSS_PROP_BG_COLOR COLON (CSS_COLOR | NONE_KW | CSS_ELEMENT) SEMICOLON
    #BackgroundColorDeclNodeAlt
    ;

widthDecl
    : CSS_PROP_WIDTH COLON sizeValue SEMICOLON
    #WidthDeclNodeAlt
    ;

heightDecl
    : CSS_PROP_HEIGHT COLON sizeValue SEMICOLON
    #HeightDeclNodeAlt
    ;

marginDecl
    : CSS_PROP_MARGIN COLON sizeValue SEMICOLON
    #MarginDeclNodeAlt
    ;

paddingDecl
    : CSS_PROP_PADDING COLON sizeValue SEMICOLON
    #PaddingDeclNodeAlt
    ;

borderDecl
    : CSS_PROP_BORDER COLON borderValue SEMICOLON
    #BorderDeclNodeAlt
    ;

displayDecl
    : CSS_PROP_DISPLAY COLON (CSS_VALUE_DISPLAY | NONE_KW | CSS_ELEMENT) SEMICOLON
    #DisplayDeclNodeAlt
    ;

positionDecl
    : CSS_PROP_POSITION COLON (CSS_VALUE_POSITION | CSS_ELEMENT) SEMICOLON
    #PositionDeclNodeAlt
    ;

fontSizeDecl
    : CSS_PROP_FONT_SIZE COLON (CSS_VALUE_GENERAL )+ SEMICOLON
    #FontSizeDeclNodeAlt
    ;

fontWeightDecl
    : CSS_PROP_FONT_WEIGHT COLON (CSS_VALUE_FONT_WEIGHT | CSS_VALUE_GENERAL ) SEMICOLON
    #FontWeightDeclNodeAlt
    ;

textAlignDecl
    : CSS_PROP_TEXT_ALIGN COLON (CSS_VALUE_TEXT_ALIGN | CSS_ELEMENT) SEMICOLON
    #TextAlignDeclNodeAlt
    ;

opacityDecl
    : CSS_PROP_OPACITY COLON (CSS_VALUE_GENERAL | CSS_ELEMENT) SEMICOLON
    #OpacityDeclNodeAlt
    ;

gridTemplateColumnsDecl
    : CSS_PROP_GRID_TEMPLATE_COLUMNS COLON gridValue SEMICOLON
    #GridTemplateColumnsDeclNodeAlt
    ;

gridGapDecl
    : CSS_PROP_GRID_GAP COLON sizeValue SEMICOLON
    #GridGapDeclNodeAlt
    ;

gridValue
    : CSS_VALUE_GRID_FUNCTION LPAREN CSS_VALUE_GENERAL COMMA (CSS_VALUE_GENERAL | CSS_VALUE_GRID_UNIT) RPAREN
    #GridFunctionNode
    | (CSS_VALUE_GENERAL | CSS_VALUE_GRID_UNIT | AUTO_KW)+
    #GridValueListNode
    ;

sizeValue
    : (CSS_VALUE_GENERAL | AUTO_KW | CSS_ELEMENT)+
    #SizeValueNode
    ;

borderValue
    : NONE_KW
    #NoneBorderNode
    | (CSS_VALUE_GENERAL | CSS_VALUE_BORDER_STYLE | CSS_COLOR )+
    #BorderValueListNode
    ;
