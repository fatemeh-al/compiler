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
    import java.util.ArrayList;
    import java.util.List;
}


program returns[Program root]:
    { $root = new Program(); }
    (newClass=classDef { $root.addClass($newClass.class); } )*
    EOF
;

id returns[Identifier identifier] : a=ID
{
    $identifier=new Identifier($a.text);
}
;

classDef returns[Declaration class]:
    (ENTRY CLASS name=id INHERITS parent=id COLON { $class = new EntryClassDeclaration($name.identifier, $parent.identifier); }
    | ENTRY CLASS name=id COLON { $class = new EntryClassDeclaration($name.identifier); }
    | CLASS name=id INHERITS parent=id COLON { $class = new ClassDeclaration($name.identifier, $parent.identifier); }
    | CLASS name=id COLON { $class = new ClassDeclaration($name.identifier); }
    )
    ( newField=fieldDef[$class] { $class.addFieldDeclaration($newField.field); }
    | newMethod=methodDef {$class.addMethodDeclaration($newMethod.method); })*
    END
;

accessModifier returns[AccessModifier access]:
    PUBLIC { $access = AccessModifier.ACCESS_MODIFIER_PUBLIC; }
    |
    PRIVATE { $access = AccessModifier.ACCESS_MODIFIER_PRIVATE; }
;

fieldDef[Declaration class] returns[FieldDeclaration field] locals[ArrayList<FieldDeclaration> inners]:
    { $inners = new ArrayList<>(); }
    (
        a=accessModifier FIELD
        ( innerField=id COMMA { $inners.add(new FieldDeclaration($innerField.identifier)); })*
        name=id t=type SEMICOLON
        { $field = new FieldDeclaration($name.identifier, $t.t, $a.access); }
    |
        FIELD
        ( innerField=id COMMA {$inners.add(new FieldDeclaration($innerField.identifier)); })*
        name=id t=type SEMICOLON
        { $field =new FieldDeclaration($name.identifier, $t.t); }
    )
    {
        for(FieldDeclaration inner : $inners)
        {
            inner.setAccessModifier($field.getAccessModifier());
            inner.setType($field.getType());
            $class.addFieldDeclaration(inner);
        }
    }
;

type returns[Type t]:
    type_a=primitiveType {$t=$type_a.t;}
    | type_b=primitiveType LBRACKET RBRACKET{$t=new ArrayType(); $t.setSingleType($type_b.t);}
    | name=id{$t=new UserDefindType(new ClassDeclaration($name.identifier));}
    | name2=id LBRACKET RBRACKET{$t=new ArrayType(); $t.setSingleType(new UserDefinedType(new ClassDeclaration($name2.identifier)));}
;

primitiveType returns[Type t]:
    INT {$t=new IntType();}
    | BOOL {$t=new boolType();}
    | STRING {$t=new StringType();}
;

methodDef returns[MethodDeclaration method]:
    (a=accessModifier FUNCTION name=id { $method=new MethodDeclaration($name.identifier); $method.setAccessModifier($a.access); }
        |
    FUNCTION name=id { $method = new MethodDeclaration($name.identifier); }
    )
    (LPAREN RPAREN
        |
    LPAREN (argName=id COLON argtype=type COMMA { $method.addArg(new PerameterDeclaration($argName.identifier, $argtype.t)); } )*
    newArg=id COLON argType=type RPAREN {$method.addArg(new ParemeterDeclaration($newArg.identifier, $argType.t)); }
    )
    RETURNS returnType=type { $method.setReturnType($returnType.t); }
    COLON (newStat=statement {$method.addStatement($newStat.stat); } )*
    END
;

statement returns[Statement stat]:
    m=matchedStat { $stat = $m.stat; }
    | u = unmatchedStat { $stat = $u.stat; }
;

matchedStat returns[Statement stat]locals[ArrayList<Conditional> elifs]:
    { $elifs = new ArrayList<>(); }
    IF LPAREN e=expression RPAREN then=matchedStat
    (ELIF LPAREN exp=expression RPAREN thenn=matchedStat )*
    ELSE elseStat=matchedStat
    | s1=assignstatement { $stat= $s1.assign; }
    | s2=matchedWhileStatement { $stat= $s2.while; }
    | s3=printstatement { $stat= $s3.print; }
    | s4=blockstatement { $stat= $s4.block; }
    | s5=incstatement { $stat= $s5.inc; }
    | s6=varstatement{ $stat= $s6.defs; }
    | s7=returnstatement { $stat= $s7.r; }
    | s8=skipstatement { $stat= $s8.skip; }
    | s9=decstatement { $stat=$s9.dec; }
    | s10=breakstatement {$stat=$s10.break; }
    | s11=continuestatement {$stat=$s11.continue; }
;

unmatchedStat returns[Statement stat]:
    IF LPAREN expression RPAREN statement
    | IF LPAREN expression RPAREN matchedStat (ELIF LPAREN expression RPAREN matchedStat)* ELSE unmatchedStat
    | unmatchedWhileStatement
;

breakstatement returns[Break break]:
    BREAK SEMICOLON {$break=new Break()}
;

continuestatement returns[Continue continue]:
    CONTINUE SEMICOLON{$continue=new Continue()}
;

blockstatement returns [Block block]:
    { $block = new Block(); }
    BEGIN (s=statement { $block.addStatement($s.stat); })*
    END
;

//can be deleted
whilestatement returns[While while]:
    WHILE LPAREN exp=expression RPAREN stat=statement
    { $while = new While($exp.exp, $stat.stat); }
;

matchedWhileStatement returns[While while]:
    WHILE LPAREN exp=expression RPAREN stat=matchedStat
    { $while = new While($exp.exp, $stat.stat); }
;

unmatchedWhileStatement returns[While while]:
    WHILE LPAREN exp=expression RPAREN stat=unmatchedStat
    { $while = new While($exp.exp, $stat.stat); }
;

printstatement returns[PrintLine print]:
    PRINT LPAREN exp=expression RPAREN SEMICOLON
    { $print = new PrintLine($exp.exp); }
;

skipstatement returns[Skip skip]:
    SEMICOLON
    { $skip = new Skip(); }
;

returnstatement returns[Return r]:
    RETURN exp=expression SEMICOLON
    { $r = new Return($exp.exp); }
;

assignstatement returns[Assign assign]:
    lexp=expression ASSIGN rexp=expression SEMICOLON
    { $assign = new Assign($lexp.exp, $rexp.exp); }
;

varstatement returns[LocalVarsDefinitions defs]:
    { $defs = new LocalVarDefinitions(); }
    VAR (newVar=id ASSIGN e=expression COMMA {$defs.addVarDefinition(new LocalVarDef($newVar.identifier, $e.exp)); })*
    varName=id ASSIGN exp=expression SEMICOLON
    {$defs.addVarDefinition(new LocalVarDef($varName.identifier, $exp.exp)); }
;

incstatement returns[IncStatement inc]:
    exp=expression PLUSONE SEMICOLON
    { $inc = new IncStatement($exp.exp); }
;

decstatement returns[DecStatement dec]:
    exp=expression MINUSONE SEMICOLON
    { $dec = new DecStatement($exp.exp); }
;

expression returns[Expression exp]:
    orexp=orExpression
    { $exp = $orexp.exp; }
;

orExpression returns[Expression exp]:
    andexp=andExpression orexp=orExpressionTemp[$andexp.exp]
    { $exp = $orexp.exp; }
;

orExpressionTemp[Expression lhs] returns[Expression exp] locals[Or orExp]:
    OR rhs=andExpression { $orExp = new Or($lhs, $rhs.exp); } ortemp=orExpressionTemp[$orExp]
    { $exp = $ortemp.exp; }
    |
    { $exp = $lhs; }
;

andExpression returns[Expression exp]:
    equal=equalExpression andexp=andExpressionTemp[$equal.exp]
    { $exp = $andexp.exp; }
;

andExpressionTemp[Expression lhs] returns[Expression exp] locals[And andexp]:
    AND rhs=equalExpression { $andexp= new And($lhs, $rhs.exp); } andtemp=andExpressionTemp[$andexp]
    { $exp = $andtemp.exp; }
    |
    { $exp = $lhs; }
;

equalExpression returns[Expression exp]:
    comp=compExpression equalexp=equalExpressionTemp[$comp.exp]
    { $exp = $equalexp.exp; }
;

equalExpressionTemp[Expression lhs] returns[Expression exp] locals[Equals equalexp, NotEquals nequalexp]:
    EQUAL rhsEq=compExpression { $equalexp= new Equals($lhs, $rhsEq.exp); } eqTemp=equalExpressionTemp[$equalexp]
    { $exp = $eqTemp.exp; }
    | NOTEQUAL rhsNeq=compExpression {$nequalexp = new NotEquals($lhs, $rhsNeq.exp); } neqTemp=equalExpressionTemp[$nequalexp]
    { $exp = $neqTemp.exp; }
    |
    { $exp = $lhs; }
;

compExpression returns[Expression exp]:
    add=addExpression compExp=compExpressionTemp[$add.exp]
    { $exp = $compExp.exp; }
;

compExpressionTemp[Expression lhs] returns[Expression exp] locals[LessThan less, GreaterThan greater]:
    LESSTHAN rhs=addExpression { $less = new LessThan($lhs, $rhs.exp); } comp=compExpressionTemp[$less]
    { $exp = $comp.exp; }
    | GREATERTHAN rhs2=addExpression { $greater = new GreaterThan($lhs, $rhs2.exp); } comp2=compExpressionTemp[$greater]
    { $exp = $comp2.exp; }
    |
    { $exp = $lhs; }
;

addExpression returns[Expression exp]:
    mult=multExpression add=addExpressionTemp[$mult.exp]
    { $exp = $add.exp; }
;

addExpressionTemp[Expression lhs] returns[Expression exp] locals[Plus plus, Minus minus]:
    PLUS rhs=multExpression {$plus = new Plus($lhs, $rhs.exp); } add=addExpressionTemp[$plus]
    { $exp = $add.exp; }
    | MINUS rhs2=multExpression {$minus = new Minus($lhs, $rhs2.exp); } add2=addExpressionTemp[$minus]
    { $exp = $add2.exp; }
    |
    { $exp = $lhs; }
;

multExpression returns[Expression exp]:
    unary=unaryExpression mult=multExpressionTemp[$unary.exp]
    { $exp = $mult.exp; }
;

multExpressionTemp[Expression lhs] returns[Expression exp] locals[Times t, Division d, Modulo m]:
    MULTIPLY rhs=unaryExpression {$t = new Times($lhs, $rhs.exp); } mult=multExpressionTemp[$t]
    { $exp = $mult.exp; }
    | DIVIDE rhs2=unaryExpression {$d = new Division($lhs, $rhs2.exp); } mult2=multExpressionTemp[$d]
    { $exp = $mult2.exp; }
    | MODULO rhs3=unaryExpression {$m = new Modulo($lhs, $rhs3.exp); } mult3=multExpressionTemp[$m]
    { $exp = $mult3.exp; }
    |
    { $exp = $lhs; }
;

unaryExpression returns[Expression exp]:
    NOT u=unaryExpression
    { $exp = new Not($u.exp); }
    | MINUS u2=unaryExpression
    { $exp = new Neg($u2.exp); }
    | mem=memExpression
    { $exp = $mem.exp; }
;

memExpression returns[Expression exp]:
     methodCall=methodExpression mem=memExpressionTemp[$methodCall.exp]
     { $exp = $mem.exp; }
;

memExpressionTemp[Expression instance] returns[Expression exp] locals[ArrayCall arrayCall]:
    LBRACKET index=expression RBRACKET {$arrayCall = new ArrayCall($instance, $index.exp); } mem=memExpressionTemp[$arrayCall]
    { $exp = $mem.exp; }
    |
    { $exp = $instance; }
;

methodExpression returns[Expression exp]:
    other=otherExpression methodTemp=methodExpressionTemp[$other.exp]
    { $exp = $methodTemp.exp; }
;

methodExpressionTemp[Expression instance] returns[Expression exp] locals[MethodCall m, FieldCall f]:
    DOT name=id LPAREN RPAREN { $m = new MethodCall($instance, $name.identifier); } method=methodExpressionTemp[$m]
    { $exp = $method.exp; }
    | DOT name2=id LPAREN { $m = new MethodCall($instance, $name2.identifier); } (args=expression COMMA { $m.addArg($args.exp); } )* arg=expression { $m.addArg($arg.exp); } RPAREN method2=methodExpressionTemp[$m]
    { $exp = $method2.exp; }
    | DOT 'length' { $f = new FieldCall($instance, new Identifier('length')); } method3=methodExpressionTemp[$f]
    { $exp = $method3.exp; }
    | DOT name4=id { $f = new FieldCall($instance, $name4.identifier); } method4=methodExpressionTemp[$f]
    { $exp = $method4.exp; }
    |
    { $exp = $instance; }
;

otherExpression returns[Expression exp]:
    a=INTLIT { $exp = new IntValue($a.int); }
    | b=STRINGLIT { $exp = new StringValue($b.text); }
    | FALSE { $exp = new BoolValue(false); }
    | TRUE { $exp = new BoolValue(true); }
    | SELF { $exp = new Self(); }
    | NEW className=id LPAREN RPAREN { $exp = new NewClassInstance($className.identifier); }
    | NEW INT LBRACKET len=expression RBRACKET { $exp = new NewArray(new IntType(), $len.exp); }
    | NEW STRING LBRACKET len2=expression RBRACKET { $exp = new NewArray(new StringType(), $len2.exp); }
    | NEW BOOL LBRACKET len3=expression RBRACKET { $exp = new NewArray(new BoolType(), $len3.exp); }
    | NEW name=id LBRACKET len4=expression RBRACKET { $exp = new NewArray(new UserDefinedType(new ClassDeclaration($name.identifier)), $len4.exp); }
    | var=id { $exp = $var.identifier; }
    | LPAREN exp2=expression RPAREN { $exp = $exp2.exp; }
;

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

LPAREN: '(';

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

MODULO: '%';

EQUAL: '==';

NOTEQUAL: '<>';

LESSTHAN: '<';

GREATERTHAN: '>';

AND: '&&';

OR: '||';

NOT: '!';

PLUSONE: '++';

MINUSONE: '--';

SINGLE_COMMENT: SINGLECOMMENT(~[\r\n])* -> skip;

MULTILINE_COMMENT: LMULTICOMMENT .*? RMULTICOMMENT -> skip;




