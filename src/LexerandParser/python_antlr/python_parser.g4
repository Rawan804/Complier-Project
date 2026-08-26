parser grammar python_parser;
options { tokenVocab=python_lexer; }

prog : (NEWLINE | stmt)* EOF                                         # program ;

stmt
    : ifStmt                                                         # sIf
    | whileStmt                                                      # sWhile
    | forStmt                                                        # sFor
    | tryStmt                                                        # sTry
    | withStmt                                                       # sWith
    | funcDef                                                        # sFunc
    | classDef                                                       # sClass
    | decorated                                                      # sDecorated
    | augAssign                                                      # sAugAssign
    | assign                                                         # sAssign
    | globalStmt                                                     # sGlobal
    | nonlocalStmt                                                   # sNonlocal
    | passStmt                                                       # sPass
    | RETURN args?                                                   # sReturn
    | BREAK                                                          # sBreak
    | CONTINUE                                                       # sContinue
    | importStmt                                                     # sImport
    | expr                                                           # sExpr
    ;


importStmt
    : IMPORT importItem (COMMA importItem)*                          # importDirect
    | FROM dottedName IMPORT importItem (COMMA importItem)*         # importFrom
    ;

importItem : name=IDENTIFIER (AS alias=IDENTIFIER)?                 # importAlias ;

globalStmt   : GLOBAL   IDENTIFIER (COMMA IDENTIFIER)*              # globalStatement ;
nonlocalStmt : NONLOCAL IDENTIFIER (COMMA IDENTIFIER)*              # nonlocalStatement ;
passStmt     : PASS                                                  # passStatement ;


ifStmt
    : IF ifCond=expr COL ifBody=block
      (ELIF elifCond+=expr COL elifBody+=block)*
      (ELSE COL elseBody=block)?                                     # ifStatement
    ;


whileStmt
    : WHILE cond=expr COL body=block
      (ELSE COL elseBody=block)?                                     # whileStatement
    ;

forStmt
    : FOR targets+=IDENTIFIER (COMMA targets+=IDENTIFIER)* IN iter=expr
      COL body=block                                                 # forStatement
    ;


tryStmt
    : TRY COL tryBody=block
      (EXCEPT excType=expr? (AS excAlias=IDENTIFIER)? COL excBody+=block)+
      (ELSE    COL elseBody=block)?
      (FINALLY COL finallyBody=block)?                               # tryStatement
    ;

withStmt : WITH withItem (COMMA withItem)* COL body=block           # withStatement ;
withItem : ctx=expr (AS alias=IDENTIFIER)?                          # withContext ;


funcDef
    : DEF name=IDENTIFIER OPEN_B params? CLOSE_B COL body=block    # funcDefinition
    ;

classDef
    : CLASS name=IDENTIFIER (OPEN_B args? CLOSE_B)? COL body=block # classDefinition
    ;

decorated : decorator+ NEWLINE* funcDef                             # decoratedFunc ;
decorator : AT name=dottedName (OPEN_B args? CLOSE_B)?             # decoratorExpr ;

params : param (COMMA param)* COMMA?                                # paramList ;
param  : name=IDENTIFIER (ASSIGN def=expr)?                         # paramDef ;


block
    : NEWLINE+ INDENT (NEWLINE | stmt)+ DEDENT                      # blockIndented
    | stmt                                                           # blockInline
    ;


assign
    : target=postfix ASSIGN value=expr                              # assignStmt
    ;

augAssign
    : target=postfix
      op=(PLUS_ASSIGN | MINUS_ASSIGN | MUL_ASSIGN | DIV_ASSIGN)
      value=expr                                                     # augAssignStmt
    ;

expr
    : trueExpr=orExpr IF cond=orExpr ELSE falseExpr=expr            # ternary
    | orExpr                                                         # exprPassthrough
    ;

orExpr
    : orExpr OR andExpr                                              # logicOr
    | andExpr                                                        # orPassthrough
    ;

andExpr
    : andExpr AND cmpExpr                                            # logicAnd
    | cmpExpr                                                        # andPassthrough
    ;

cmpExpr
    : left=addExpr op=cmpOp right=addExpr                           # compare
    | addExpr                                                        # cmpPassthrough
    ;

addExpr
    : left=addExpr op=(PLUS | MINUS) right=mulExpr                  # addSub
    | mulExpr                                                        # addPassthrough
    ;

mulExpr
    : left=mulExpr op=(MUL | DIV | MOD) right=unary                 # mulDiv
    | unary                                                          # mulPassthrough
    ;

unary
    : op=(NOT | PLUS | MINUS) operand=unary                         # unaryOp
    | primary                                                        # unaryPassthrough
    ;

primary
    : literal                                                        # primLiteral
    | collection                                                     # primCollection
    | LAMBDA params? COL body=expr                                   # primLambda
    | OPEN_B inner=expr CLOSE_B                                      # primGrouped
    | postfix                                                        # primPostfix
    ;

postfix
    : postfix DOT field=IDENTIFIER                                   # memberAccess
    | postfix OPEN_B args? CLOSE_B                                   # funcCall
    | postfix LSB index=expr RSB                                     # indexAccess
    | IDENTIFIER                                                     # atomId
    ;

cmpOp
    : GREATERTHAN                                                    # opGt
    | SMALLERTHAN                                                    # opLt
    | GREATEROREQ                                                    # opGte
    | SMALLOREQ                                                      # opLte
    | EQ                                                             # opEq
    | NOTEQ                                                          # opNeq
    | IN                                                             # opIn
    | NOT IN                                                         # opNotIn
    | IS                                                             # opIs
    | IS NOT                                                         # opIsNot
    ;

collection
    : LSB items=args? RSB                                            # listLit
    | OPEN_B CLOSE_B                                                 # emptyTuple
    | OPEN_B arg COMMA (arg (COMMA arg)*)? COMMA? CLOSE_B           # tupleLit
    | LBRACE pairs=dictItems? RBRACE                                 # dictLit
    | LBRACE items=args RBRACE                                       # setLit
    | RANGE OPEN_B args? CLOSE_B                                     # rangeLit
    ;

dictItems : dictItem (COMMA dictItem)* COMMA?                       # dictItemList ;
dictItem  : key=expr COL value=expr                                  # dictPair ;

args : arg (COMMA arg)* COMMA?                                       # argList ;
arg
    : name=IDENTIFIER ASSIGN value=expr                             # kwArg
    | value=expr                                                     # posArg
    ;

literal
    : NUMBER                                                         # litNum
    | STRING                                                         # litStr
    | TRUE                                                           # litTrue
    | FALSE                                                          # litFalse
    | NONE                                                           # litNull
    ;

dottedName : IDENTIFIER (DOT IDENTIFIER)*                           # dottedId ;
