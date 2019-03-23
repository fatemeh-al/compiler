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

IDENTIFIER: (LETTER)(LETTER | DIGIT)*;

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

SINGLEASSIGN: '=';

PLUS: '+';

MINUS: '-';

MULTIPLY: '*';

DIVIDE: '/';

MODULU: '%';

DOUBLEASSIGN: '==';

NOTEQUAL: '<>';

LESSTHAN: '<';

GREATERTHAN: '>';

AND: '&&';

OR: '||';

NOT: '!';

PLUSONE: '++';

MINUSONE: '--';





