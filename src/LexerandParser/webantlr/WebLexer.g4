lexer grammar WebLexer;

@header {
package LexerandParser.webantlr;
}

// ============================================================
//  DEFAULT MODE — HTML / Jinja content (outside tags)
// ============================================================
@members {
    boolean insideStyle = false;
}
OPEN            : '<'  -> pushMode(HTML_TAG_MODE);
JINJA_EXPR_OPEN : '{{' -> pushMode(JINJA_EXPR_MODE);
JINJA_STMT_OPEN : '{%' -> pushMode(JINJA_STMT_MODE);
JINJA_COMMENT   : '{#' .*? '#}' -> skip;
WS_CONTENT      : [ \t\r\n]+ -> skip;
HTML_COMMENT    : '<!--' .*? '-->' -> skip;

TEXT : (
       ~[<{]
     | '$' {_input.LA(1) != '{' && _input.LA(2) != '{'}?
     )+ ;

ENTITY   : '&' [a-zA-Z#]+ ';';
JINJA_ID : [a-zA-Z_][a-zA-Z0-9_]* ;
DOT      : '.';
NUMBER   : [0-9]+;
STRING_J : '"' (~["\\] | '\\' .)* '"';


// ============================================================
//  HTML_TAG_MODE — Inside <tag ...>
//  EQ مسماة ATTR_EQ لتجنب التعارض مع EQ البايثون (==)
// ============================================================

mode HTML_TAG_MODE;

CLOSE
    : '>'
      {
          if (insideStyle) {
              insideStyle = false;
              popMode();
              pushMode(CSS_MODE);
          } else {
              popMode();
          }
      }
    ;SLASH       : '/';
ATTR_EQ     : '=';
HTML        : 'html';
HEAD        : 'head';
BODY        : 'body';
ACTION_KW   : 'action';
TITLE       : 'title';
DIV         : 'div';
P           : 'p';
H1 : 'h1'; H2 : 'h2'; H3 : 'h3'; H4 : 'h4'; H5 : 'h5'; H6 : 'h6';
SPAN        : 'span';
IMG         : 'img';
UL          : 'ul';
FORM        : 'form';
INPUT       : 'input';
TYPE_KW     : 'type';
TYPE        : '"text"' | '"number"' | '"submit"' | '"password"';
NAME        : 'name';
LI          : 'li';
SRC         : 'src';
BR          : 'br';
REQUIRED    : 'required';
HREF_KW     : 'href';
A           : 'a';
DOCTYPE     : 'DOCTYPE';
DOC         : '!';
REL_KW      : 'rel';
REL         : '"stylesheet"' | '"icon"';
TEXTAREA    : 'textarea';
LINK        : 'link';
BUTTON_KW   : 'button';
BUTTON      : '"submit"' | '"button"';
METHOD_KW   : 'method';
METHOD      : '"get"' | '"post"' ;
STYLE
    : 'style'
      { insideStyle = true; }
    ;PLACEHOLDER : 'placeholder';

ATR    : [a-zA-Z_][a-zA-Z0-9_\-]* ;
STRING : '"' (~["\\\r\n] | '\\' .)* '"'
       | '\'' (~['\\\r\n] | '\\' .)* '\''
       ;
WS_TAG : [ \t\r\n]+ -> skip;


// ============================================================
//  JINJA_EXPR_MODE — Inside {{ ... }}
// ============================================================

mode JINJA_EXPR_MODE;

JINJA_EXPR_CLOSE : '}}' -> popMode;

// أي محتوى داخل {{ ... }} (دوال url_for، concatenation، filters، إلخ)
JINJA_RAW_EXPR   : ( ~'}' | '}' ~'}' )+ ;

WS_JINJA : [ \t\r\n]+ -> skip;


// ============================================================
//  JINJA_STMT_MODE — Inside {% ... %}
// ============================================================

mode JINJA_STMT_MODE;

JINJA_STMT_CLOSE : '%}' -> popMode;

FOR    : 'for';
ENDFOR : 'endfor';
IN     : 'in';
IF     : 'if';
ELSE   : 'else';
ENDIF  : 'endif';

OP : '==' | '!=' | '>=' | '<=' | '>' | '<';

STMT_ID  : [a-zA-Z_][a-zA-Z0-9_]* -> type(JINJA_ID);
STMT_DOT : '.'                      -> type(DOT);
STMT_NUM : [0-9]+                   -> type(NUMBER);
STMT_STR : '"' (~["\\] | '\\' .)* '"' -> type(STRING_J);

WS_JINJA_STMT : [ \t\r\n]+ -> skip;


// ============================================================
//  CSS_MODE — CSS stylesheet content
//  يتم تفعيله برمجياً: lexer.pushMode(WebLexer.CSS_MODE)
// ============================================================

mode CSS_MODE;
STYLE_END
    : '</style>'
      -> popMode;

LBRACE    : '{';
RBRACE    : '}';
COLON     : ':';
SEMICOLON : ';';
COMMA     : ',';
LPAREN    : '(';
RPAREN    : ')';

CSS_PROP_COLOR                 : 'color';
CSS_PROP_BG_COLOR              : 'background-color';
CSS_PROP_WIDTH                 : 'width';
CSS_PROP_HEIGHT                : 'height';
CSS_PROP_MARGIN                : 'margin';
CSS_PROP_PADDING               : 'padding';
CSS_PROP_BORDER                : 'border';
CSS_PROP_FONT_SIZE             : 'font-size';
CSS_PROP_FONT_WEIGHT           : 'font-weight';
CSS_PROP_TEXT_ALIGN            : 'text-align';
CSS_PROP_DISPLAY               : 'display';
CSS_PROP_POSITION              : 'position';
CSS_PROP_OPACITY               : 'opacity';
CSS_PROP_GRID_TEMPLATE_COLUMNS : 'grid-template-columns';
CSS_PROP_GRID_TEMPLATE_ROWS    : 'grid-template-rows';
CSS_PROP_GRID_GAP              : 'gap';

CSS_VALUE_BORDER_STYLE  : 'solid' | 'dashed' | 'dotted' | 'double' | 'inset' | 'outset';
CSS_VALUE_DISPLAY       : 'block' | 'inline' | 'inline-block' | 'flex' | 'grid';
CSS_VALUE_POSITION      : 'static' | 'relative' | 'absolute' | 'fixed' | 'sticky';
CSS_VALUE_TEXT_ALIGN    : 'left' | 'right' | 'center' | 'justify';
CSS_VALUE_FONT_WEIGHT   : 'normal' | 'bold' | 'bolder' | 'lighter' | [1-9] '00';
CSS_VALUE_GRID_FUNCTION : 'repeat';
CSS_VALUE_GRID_UNIT     : 'fr';

NONE_KW : 'none';
AUTO_KW : 'auto';

CSS_COLOR
    : '#' [a-fA-F0-9]+
    | 'white' | 'black' | 'red' | 'blue' | 'green'
    | 'darkred' | 'darkgreen'
    | 'transparent' | 'gray'
    ;

CSS_VALUE_GENERAL
    : [0-9]+ ('.' [0-9]+)? ('px' | 'em' | 'rem' | '%' | 'vw' | 'vh' | 'fr')?
    ;

CSS_NOT     : ':not';
CSS_ID      : '#' [a-zA-Z_][a-zA-Z0-9_-]*;
CSS_CLASS   : '.' [a-zA-Z_][a-zA-Z0-9_-]*;
CSS_PSEUDO  : ':' [a-zA-Z_-]+;
CSS_STAR    : '*';
CSS_ELEMENT : [a-zA-Z_][a-zA-Z0-9_-]*;

CSS_COMMENT : '/*' .*? '*/' -> skip;
WS          : [ \t\r\n]+    -> skip;
ANY         : .             -> skip;


