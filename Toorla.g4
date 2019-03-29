grammar Toorla;

@header
{
    import toorla.ast.expression.*;
    import toorla.ast.expression.binaryExpression.*;
    import toorla.ast.expression.unaryExpression.*;
    import toorla.ast.expression.value.*;
    import toorla.ast.declaration.*;
    import toorla.ast.statement.*;
    import toorla.ast.*;
}


program: (classDef)* EOF;

classDef: ENTRY? CLASS ID (INHERITS ID)? COLON (fieldDef | methodDef)* END;

accessModifier: PUBLIC | PRIVATE;

fieldDef: accessModifier? FIELD (ID COMMA)* ID type SEMICOLON;

primitiveType: INT | BOOL | STRING;

type: primitiveType | primitiveType LBRACKET RBRACKET | ID | ID LBRACKET RBRACKET;

methodDef: accessModifier? FUNCTION ID (LPAREN RPAREN | LPAREN (ID COLON type COMMA)* ID COLON type RPAREN) RETURNS type COLON statements END;

statements: (statement)*;

statement: assignstatement | whilestatement | ifstatement | printstatement | blockstatement | incOrDecstatement | varstatement | returnstatement;

loopstatement: statement | blockstatementInLoop | ifstatementInLoop | breakstatement | continuestatement;

breakstatement: BREAK SEMICOLON;

continuestatement: CONTINUE SEMICOLON;

blockstatement: BEGIN statements END;

blockstatementInLoop: BEGIN (statement | loopstatement)* END;

whilestatement: WHILE LPAREN expression RPAREN loopstatement;

ifstatement: IF LPAREN expression RPAREN statement (ELIF LPAREN expression RPAREN statement)* (ELSE statement)?;

ifstatementInLoop: IF LPAREN expression RPAREN loopstatement (ELIF LPAREN expression RPAREN loopstatement)* (ELSE loopstatement)?;

printstatement: PRINT LPAREN expression RPAREN SEMICOLON;

returnstatement: RETURN expression SEMICOLON;

expression: orExpression;

orExpression: andExpression orExpressionTemp;

orExpressionTemp: OR andExpression orExpressionTemp |;

andExpression: equalExpression andExpressionTemp;

andExpressionTemp: AND equalExpression andExpressionTemp |;

equalExpression: compExpression equalExpressionTemp;

equalExpressionTemp: (EQUAL | NOTEQUAL) compExpression equalExpressionTemp |;

compExpression: addExpression compExpressionTemp;

compExpressionTemp: (LESSTHAN | GREATERTHAN) addExpression compExpressionTemp |;

addExpression: multExpression addExpressionTemp;

addExpressionTemp: (PLUS | MINUS) multExpression addExpressionTemp |;

multExpression: unaryExpression multExpressionTemp;

multExpressionTemp: (MULTIPLY | DIVIDE | MODULU) unaryExpression multExpressionTemp |;

unaryExpression: (NOT | MINUS) unaryExpression | memExpression;

memExpression: methodExpression memExpressionTemp;

//not sure about this line. probably wrong. the correct one may be this:
//memExpressionTemp: (LBRACKET expression RBRACKET) memExpressionTemp |;
memExpressionTemp: LBRACKET expression RBRACKET |;

methodExpression: otherExpression methodExpressionTemp;

methodExpressionTemp: DOT (ID LPAREN RPAREN | ID LPAREN (expression COMMA)* expression RPAREN | 'length') methodExpressionTemp |;

otherExpression: INTLIT | STRINGLIT | SELF | TRUE | FALSE | NEW ID LPAREN RPAREN | NEW (INT | STRING | BOOL | ID) LBRACKET INTLIT RBRACKET | ID | ID LBRACKET expression RBRACKET | LPAREN expression RPAREN;

singleComment: SINGLECOMMENT (~[\r\n])* -> skip;

//not sure about this line too
multilineComment: LMULTICOMMENT (~(RMULTICOMMENT))* RMULTICOMMENT -> skip;

assignstatement: expression ASSIGN expression SEMICOLON;

varstatement: VAR (ID ASSIGN expression COMMA)* ID ASSIGN expression SEMICOLON;

incOrDecstatement: expression (PLUSONE | MINUSONE) SEMICOLON;

INTLIT: [1-9][0-9]* | [0];

WS: [ \t\n] -> skip;

BOOL: 'bool';

INT: 'int';

STRING: 'string';

CLASS: 'class';

FUNCTION: 'function';

PRINT: 'print';

IF: 'if';

PRIVATE: 'private';

FIELD: 'field';

SELF: 'self';

FALSE: 'false';

TRUE: 'true';

WHILE: 'while';

ELSE: 'else';

NEW: 'new';

RETURN: 'return';

ELIF: 'elif';

RETURNS: 'returns';

BREAK: 'break';

CONTINUE: 'continue';

ENTRY: 'entry';

BEGIN: 'begin';

END: 'end';

PUBLIC: 'public';

VAR: 'var';

INHERITS: 'inherits';

fragment DIGIT: [0-9];

fragment LETTER: [a-z] | [A-Z] | [_];

ID: (LETTER)(LETTER | DIGIT)*;

STRINGLIT: '"' (~["\n])* '"';

LPAAREN: '(';

RPAREN: ')';

COLON: ':';

COMMA: ',';

DOT: '.';

SEMICOLON: ';';

SINGLECOMMENT: '//';

LMULTICOMMENT: '/*';

RMULTICOMMENT: '*/';

LBRACKET: '[';

RBRACKET: ']';

ASSIGN: '=';

PLUS: '+';

MINUS: '-';

MULTIPLY: '*';

DIVIDE: '/';

MODULU: '%';

EQUAL: '==';

NOTEQUAL: '<>';

LESSTHAN: '<';

GREATERTHAN: '>';

AND: '&&';

OR: '||';

NOT: '!';

PLUSONE: '++';

MINUSONE: '--';





