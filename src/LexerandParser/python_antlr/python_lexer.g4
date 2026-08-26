lexer grammar python_lexer;

@header {
    import java.util.Deque;
    import java.util.ArrayDeque;
    import java.util.LinkedList;
    import org.antlr.v4.runtime.Token;
    import org.antlr.v4.runtime.CommonToken;
}
@members {
    private Deque<Integer> indents = new ArrayDeque<>();
    private LinkedList<Token> tokens = new LinkedList<>();
    private int opened = 0;
    private Token lastToken = null;

    @Override
    public Token nextToken() {
        if (!tokens.isEmpty()) {
            return tokens.poll();
        }

        Token next = super.nextToken();
        if (next.getType() == EOF) {

            if (lastToken != null && lastToken.getType() != NEWLINE) {
                tokens.add(new CommonToken(NEWLINE, "\n"));
            }

            while (!indents.isEmpty()) {
                indents.pop();
                tokens.add(new CommonToken(DEDENT, "<DEDENT>"));
            }

            tokens.add(next);
            return tokens.poll();
        }

        lastToken = next;
        return next;
    }

    private int getIndentationCount(String spaces) {
        int count = 0;
        for (char ch : spaces.toCharArray()) {
            if (ch == ' ') count++;
            else if (ch == '\t') count += 4;
        }
        return count;
    }
}



IF      : 'if';
ELIF    : 'elif';
ELSE    : 'else';
WHILE   : 'while';
FOR     : 'for';
DEF     : 'def';
RETURN  : 'return';
PRINT   : 'print';
IN      : 'in';
NOT     : 'not';
AND     : 'and';
OR      : 'or';
TRUE  : 'True';
FALSE : 'False';
NONE  : 'None';

IMPORT  : 'import';
FROM    : 'from';
BREAK   : 'break';
CONTINUE: 'continue';
AS      : 'as';
IS      : 'is';
LAMBDA  : 'lambda';
TRY     : 'try';
EXCEPT  : 'except';
FINALLY : 'finally';
WITH    : 'with';
CLASS   : 'class';
RANGE   : 'range';

GLOBAL   : 'global';
NONLOCAL : 'nonlocal';
PASS     : 'pass';
/* ============================
   SYMBOLS
   ============================ */

OPEN_B  : '(' { opened++; };
CLOSE_B : ')' { opened--; };
LBRACE  : '{' { opened++; };
RBRACE  : '}' { opened--; };
LSB     : '[' { opened++; };
RSB     : ']' { opened--; };

COL     : ':';
COMMA   : ',';
DOT      :'.';
ASSIGN   :'=';
AT       :'@';

/* ============================
   OPERATORS (MATCH PARSER)
   ============================ */

PLUS        : '+';
MINUS       : '-';
MUL         : '*';
DIV         : '/';
MOD         : '%';
PLUS_ASSIGN  : '+=';
MINUS_ASSIGN : '-=';
MUL_ASSIGN   : '*=';
DIV_ASSIGN   : '/=';
EQ          : '==';
NOTEQ       : '!=';
SMALLERTHAN : '<';
GREATERTHAN : '>';
SMALLOREQ   : '<=';
GREATEROREQ : '>=';

/* ============================
   IDENTIFIERS
   ============================ */

IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_]*;

/* ============================
   NUMBERS
   ============================ */

NUMBER : [0-9]+ ('.' [0-9]+)?;

/* ============================
   STRINGS
   ============================ */

STRING
    : '"' (~["\\\r\n])* '"'
    | '\'' (~['\\\r\n])* '\''
    ;

/* ============================
   INDENTATION
   ============================ */

NEWLINE
    :   ('\r'? '\n') SPACES?
        {
            String text = getText();
            String spaces = text.replaceAll("[\r\n]+", "");
            int nextChar = _input.LA(1);

            // إذا داخل () أو [] أو {} → تجاهل الـNEWLINE
            if (opened > 0) {
                skip();
            }
            // إذا السطر القادم فارغ → تجاهل
            else if (nextChar == '\r' || nextChar == '\n') {
                skip();
            }
            // إذا السطر القادم تعليق → تجاهل
            else if (nextChar == '#') {
                skip();
            }
            else {
                int newIndent = getIndentationCount(spaces);
                int currentIndent = indents.isEmpty() ? 0 : indents.peek();

                tokens.add(new CommonToken(NEWLINE, "\n"));

                if (newIndent > currentIndent) {
                    indents.push(newIndent);
                    tokens.add(new CommonToken(INDENT, "<INDENT>"));
                } else if (newIndent < currentIndent) {
                    while (!indents.isEmpty() && indents.peek() > newIndent) {
                        indents.pop();
                        tokens.add(new CommonToken(DEDENT, "<DEDENT>"));
                    }
                }
            }
        }
    ;


fragment SPACES : [ \t]+ ;

WS      : [ \t]+ -> skip;
COMMENT : '#' ~[\r\n]* -> skip;

INDENT  : ;
DEDENT  : ;
