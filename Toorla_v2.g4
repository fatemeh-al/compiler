grammar Toorla;

@header
{
    import toorla.ast.expression.*;
    import toorla.ast.expression.binaryExpression.*;
    import toorla.ast.expression.unaryExpression.*;
    import toorla.ast.expression.value.*;
    import toorla.ast.declaration.*;
    import toorla.ast.declaration.classDecs.*;
    import toorla.ast.declaration.classDecs.classMembersDecs.*;
    import toorla.ast.declaration.localVarDecs.*;
    import toorla.ast.statement.*;
    import toorla.ast.statement.localVarStats.*;
    import toorla.ast.statement.returnStatement.*;
    import toorla.ast.*;
    import toorla.types.*;
    import toorla.types.arrayType.*;
    import toorla.types.singleType.*;
    import java.util.ArrayList;
    import java.util.List;
}

@members
{
    int currentLine = 0;
}

program returns[Program root] locals[boolean setLine]:
    { $root = new Program(); $setLine = true; }
    (newClass=classDef
        {   $root.addClass($newClass.newClass);
            if($setLine)
            {
                $root.line = $newClass.newClass.line;
                $root.col = $newClass.newClass.col;
                $setLine = false;
            }
        }
    )*
    EOF
;

id returns[Identifier identifier]:
    a=ID { $identifier=new Identifier($a.text); $identifier.line = $a.line; $identifier.col = $a.pos; }
;

classDef returns[ClassDeclaration newClass]:
    (key1=ENTRY CLASS name=id INHERITS parent=id COLON { $newClass = new EntryClassDeclaration($name.identifier, $parent.identifier); $newClass.line = $key1.line; $newClass.col = $key1.pos; }
        | key2=ENTRY CLASS name=id COLON { $newClass = new EntryClassDeclaration($name.identifier); $newClass.line = $key2.line; $newClass.col = $key2.pos;}
        | key3=CLASS name=id INHERITS parent=id COLON { $newClass = new ClassDeclaration($name.identifier, $parent.identifier); $newClass.line = $key3.line; $newClass.col = $key3.pos; }
        | key4=CLASS name=id COLON { $newClass = new ClassDeclaration($name.identifier); $newClass.line = $key4.line; $newClass.col = $key4.pos; }
    )
    ( newField=fieldDef[$newClass] { $newClass.addFieldDeclaration($newField.field); }
        | newMethod=methodDef {$newClass.addMethodDeclaration($newMethod.method); })*
    END
;

accessModifier returns[AccessModifier access, int line, int col]:
    key1=PUBLIC { $access = AccessModifier.ACCESS_MODIFIER_PUBLIC; $line = $key1.line; $col = $key1.pos; }
    | key2=PRIVATE { $access = AccessModifier.ACCESS_MODIFIER_PRIVATE; $line = $key2.line; $col = $key2.pos; }
;

fieldDef[ClassDeclaration newClass] returns[FieldDeclaration field] locals[ArrayList<FieldDeclaration> inners]:
    { $inners = new ArrayList<>(); }
    (
        a=accessModifier FIELD
        ( innerField=id COMMA { $inners.add(new FieldDeclaration($innerField.identifier)); })*
        name=id t=type SEMICOLON
        { $field = new FieldDeclaration($name.identifier, $t.t, $a.access); $field.line = $a.line; $field.col = $a.col; }
    |
        key=FIELD
        ( innerField=id COMMA {$inners.add(new FieldDeclaration($innerField.identifier)); })*
        name=id t=type SEMICOLON
        { $field =new FieldDeclaration($name.identifier, $t.t); $field.line = $key.line; $field.col = $key.pos; }
    )
    {
        for(FieldDeclaration inner : $inners)
        {
            inner.setAccessModifier($field.getAccessModifier());
            inner.setType($field.getType());
            $newClass.addFieldDeclaration(inner);
        }
    }
;

type returns[Type t]:
    type_a=primitiveType {$t=$type_a.t;}
    | type_b=primitiveType LBRACKET RBRACKET{$t=new ArrayType($type_b.t);  }
    | name=id{$t=new UserDefinedType(new ClassDeclaration($name.identifier)); }
    | name2=id LBRACKET RBRACKET{$t=new ArrayType(new UserDefinedType(new ClassDeclaration($name2.identifier))); }
;

primitiveType returns[SingleType t]:
    key=INT {$t=new IntType(); }
    | key1=BOOL {$t=new BoolType(); }
    | key2=STRING {$t=new StringType(); }
;

methodDef returns[MethodDeclaration method]:
    (a=accessModifier FUNCTION name=id { $method=new MethodDeclaration($name.identifier); $method.setAccessModifier($a.access); $method.line = $a.line; $method.col = $a.col; }
        | key=FUNCTION name=id { $method = new MethodDeclaration($name.identifier); $method.line = $key.line; $method.col = $key.pos; }
        | key=FUNCTION name=id { $method = new MethodDeclaration($name.identifier); $method.line = $key.line; $method.col = $key.pos; }
    )
    (LPAREN RPAREN
        | LPAREN (argName=id COLON argtype=type COMMA { $method.addArg(new ParameterDeclaration($argName.identifier, $argtype.t)); } )*
        newArg=id COLON argType=type RPAREN {$method.addArg(new ParameterDeclaration($newArg.identifier, $argType.t)); }
    )
    RETURNS returnType=type { $method.setReturnType($returnType.t); }
    COLON (newStat=statement {$method.addStatement($newStat.stat); } )*
    END
;

statement returns[Statement stat]:
    m=matchedStat { $stat = $m.stat; }
    | u = unmatchedStat { $stat = $u.stat; }
;

matchedStat returns[Statement stat] locals[ArrayList<Conditional> elifs]:
    { $elifs = new ArrayList<>(); }
    key=IF LPAREN e=expression RPAREN then=matchedStat { $elifs.add(new Conditional($e.exp, $then.stat)); }
    (ELIF LPAREN exp=expression RPAREN thenn=matchedStat { $elifs.add(new Conditional($exp.exp, $thenn.stat)); })*
    ELSE elseStat=matchedStat
    {   $elifs.get($elifs.size() - 1).setElseStatement($elseStat.stat);
        for(int i = $elifs.size() - 2; i >= 0; i--)
            $elifs.get(i).setElseStatement($elifs.get(i + 1));
        $stat = $elifs.get(0);
        $stat.line = $key.line; $stat.col = $key.pos;
    }
    | s1=assignstatement { $stat= $s1.assign; }
    | s2=matchedWhileStatement { $stat= $s2.newWhile; }
    | s3=printstatement { $stat= $s3.print; }
    | s4=blockstatement { $stat= $s4.block; }
    | s5=incstatement { $stat= $s5.inc; }
    | s6=varstatement{ $stat= $s6.defs; }
    | s7=returnstatement { $stat= $s7.r; }
    | s8=skipstatement { $stat= $s8.skip; }
    | s9=decstatement { $stat=$s9.dec; }
    | s10=breakstatement {$stat=$s10.newBreak; }
    | s11=continuestatement {$stat=$s11.newContinue; }
;

unmatchedStat returns[Statement stat] locals[ArrayList<Conditional> elifs]:
    key1=IF LPAREN condition=expression RPAREN then=statement
    { $stat = new Conditional($condition.exp, $then.stat); $stat.line = $key1.line; $stat.col = $key1.pos;}
    | { $elifs = new ArrayList<>(); }
    key2=IF LPAREN e=expression RPAREN then2=matchedStat { $elifs.add(new Conditional($e.exp, $then2.stat)); }
    (ELIF LPAREN exp=expression RPAREN elifthen=matchedStat { $elifs.add(new Conditional($exp.exp, $elifthen.stat)); })*
    ELSE elze=unmatchedStat
    {
        $elifs.get($elifs.size() - 1).setElseStatement($elze.stat);
        for(int i = $elifs.size() - 2; i >= 0; i--)
            $elifs.get(i).setElseStatement($elifs.get(i + 1));
        $stat = $elifs.get(0);
        $stat.line = $key2.line; $stat.col = $key2.pos;
    }
    | { $elifs = new ArrayList<>(); }
    key3=IF LPAREN e2=expression RPAREN then3=matchedStat { $elifs.add(new Conditional($e2.exp, $then3.stat)); }
    (ELIF LPAREN e3=expression RPAREN elifthen2=matchedStat { $elifs.add(new Conditional($e3.exp, $elifthen2.stat)); } )*
    ELIF LPAREN e4=expression RPAREN elifthen3=statement
    {
         $elifs.add(new Conditional($e4.exp, $elifthen3.stat));
         for(int i = $elifs.size() - 2; i >= 0; i--)
              $elifs.get(i).setElseStatement($elifs.get(i + 1));
         $stat = $elifs.get(0);
         $stat.line = $key3.line; $stat.col = $key3.pos;
    }
    | openWhile=unmatchedWhileStatement
    { $stat = $openWhile.newWhile; }
;

breakstatement returns[Break newBreak]:
    key=BREAK SEMICOLON { $newBreak=new Break(); $newBreak.line = $key.line; $newBreak.col = $key.pos; }
;

continuestatement returns[Continue newContinue]:
    key=CONTINUE SEMICOLON{ $newContinue=new Continue(); $newContinue.line = $key.line; $newContinue.col = $key.pos; }
;

blockstatement returns [Block block]:
    { $block = new Block(); }
    key=BEGIN (s=statement { $block.addStatement($s.stat); })*
    END { $block.line = $key.line; $block.col = $key.pos; }
;

//can be deleted
whilestatement returns[While newWhile]:
    key=WHILE LPAREN exp=expression RPAREN stat=statement
    { $newWhile = new While($exp.exp, $stat.stat); $newWhile.line = $key.line; $newWhile.col = $key.pos;}
;

matchedWhileStatement returns[While newWhile]:
    key=WHILE LPAREN exp=expression RPAREN stat=matchedStat
    { $newWhile = new While($exp.exp, $stat.stat); $newWhile.line = $key.line; $newWhile.col = $key.pos;}
;

unmatchedWhileStatement returns[While newWhile]:
    key=WHILE LPAREN exp=expression RPAREN stat=unmatchedStat
    { $newWhile = new While($exp.exp, $stat.stat); $newWhile.line = $key.line; $newWhile.col = $key.pos;}
;

printstatement returns[PrintLine print]:
    key=PRINT LPAREN exp=expression RPAREN SEMICOLON
    { $print = new PrintLine($exp.exp); $print.line = $key.line; $print.col = $key.pos; }
;

skipstatement returns[Skip skip]:
    semi=SEMICOLON
    { $skip = new Skip(); $skip.line = $semi.line; $skip.col = $semi.pos; }
;

returnstatement returns[Return r]:
    ret=RETURN exp=expression SEMICOLON
    { $r = new Return($exp.exp); $r.line = $ret.line; $r.col = $ret.pos; }
;

assignstatement returns[Assign assign]:
    lexp=expression ASSIGN rexp=expression SEMICOLON
    { $assign = new Assign($lexp.exp, $rexp.exp); $assign.line = $lexp.exp.line; $assign.col = $lexp.exp.col; }
;

varstatement returns[LocalVarsDefinitions defs]:
    { $defs = new LocalVarsDefinitions(); }
    v=VAR (newVar=id ASSIGN e=expression COMMA {$defs.addVarDefinition(new LocalVarDef($newVar.identifier, $e.exp)); })*
    varName=id ASSIGN exp=expression SEMICOLON
    {
        $defs.addVarDefinition(new LocalVarDef($varName.identifier, $exp.exp));
        for(LocalVarDef definition : $defs.getVarDefinitions())
        {
            definition.line = $v.line;
            definition.col = $v.pos;
        }
        $defs.line = $v.line;
        $defs.col = $v.pos;
    }
;

incstatement returns[IncStatement inc]:
    exp=expression PLUSONE SEMICOLON
    { $inc = new IncStatement($exp.exp); $inc.line = $exp.exp.line; $inc.col = $exp.exp.col; }
;

decstatement returns[DecStatement dec]:
    exp=expression MINUSONE SEMICOLON
    { $dec = new DecStatement($exp.exp); $dec.line = $exp.exp.line; $dec.col = $exp.exp.col; }
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
    { $exp = $ortemp.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    |
    { $exp = $lhs; }
;

andExpression returns[Expression exp]:
    equal=equalExpression andexp=andExpressionTemp[$equal.exp]
    { $exp = $andexp.exp; }
;

andExpressionTemp[Expression lhs] returns[Expression exp] locals[And andexp]:
    AND rhs=equalExpression { $andexp= new And($lhs, $rhs.exp); } andtemp=andExpressionTemp[$andexp]
    { $exp = $andtemp.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    |
    { $exp = $lhs; }
;

equalExpression returns[Expression exp]:
    comp=compExpression equalexp=equalExpressionTemp[$comp.exp]
    { $exp = $equalexp.exp; }
;

equalExpressionTemp[Expression lhs] returns[Expression exp] locals[Equals equalexp, NotEquals nequalexp]:
    EQUAL rhsEq=compExpression { $equalexp= new Equals($lhs, $rhsEq.exp); } eqTemp=equalExpressionTemp[$equalexp]
    { $exp = $eqTemp.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    | NOTEQUAL rhsNeq=compExpression {$nequalexp = new NotEquals($lhs, $rhsNeq.exp); } neqTemp=equalExpressionTemp[$nequalexp]
    { $exp = $neqTemp.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    |
    { $exp = $lhs; $exp.line = $lhs.line; $exp.col = $lhs.col; }
;

compExpression returns[Expression exp]:
    add=addExpression compExp=compExpressionTemp[$add.exp]
    { $exp = $compExp.exp; }
;

compExpressionTemp[Expression lhs] returns[Expression exp] locals[LessThan less, GreaterThan greater]:
    LESSTHAN rhs=addExpression { $less = new LessThan($lhs, $rhs.exp); } comp=compExpressionTemp[$less]
    { $exp = $comp.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    | GREATERTHAN rhs2=addExpression { $greater = new GreaterThan($lhs, $rhs2.exp); } comp2=compExpressionTemp[$greater]
    { $exp = $comp2.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    |
    { $exp = $lhs; }
;

addExpression returns[Expression exp]:
    mult=multExpression add=addExpressionTemp[$mult.exp]
    { $exp = $add.exp; }
;

addExpressionTemp[Expression lhs] returns[Expression exp] locals[Plus plus, Minus minus]:
    PLUS rhs=multExpression {$plus = new Plus($lhs, $rhs.exp); } add=addExpressionTemp[$plus]
    { $exp = $add.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    | MINUS rhs2=multExpression {$minus = new Minus($lhs, $rhs2.exp); } add2=addExpressionTemp[$minus]
    { $exp = $add2.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    |
    { $exp = $lhs; }
;

multExpression returns[Expression exp]:
    unary=unaryExpression mult=multExpressionTemp[$unary.exp]
    { $exp = $mult.exp; }
;

multExpressionTemp[Expression lhs] returns[Expression exp] locals[Times t, Division d, Modulo m]:
    MULTIPLY rhs=unaryExpression {$t = new Times($lhs, $rhs.exp); } mult=multExpressionTemp[$t]
    { $exp = $mult.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    | DIVIDE rhs2=unaryExpression {$d = new Division($lhs, $rhs2.exp); } mult2=multExpressionTemp[$d]
    { $exp = $mult2.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    | MODULO rhs3=unaryExpression {$m = new Modulo($lhs, $rhs3.exp); } mult3=multExpressionTemp[$m]
    { $exp = $mult3.exp; $exp.line = $lhs.line; $exp.col = $lhs.col; }
    |
    { $exp = $lhs; }
;

unaryExpression returns[Expression exp]:
    n=NOT u=unaryExpression
    { $exp = new Not($u.exp); $exp.line = $n.line; $exp.col = $n.pos; }
    | m=MINUS u2=unaryExpression
    { $exp = new Neg($u2.exp); $exp.line = $m.line; $exp.col = $m.pos; }
    | call=callExpression
    { $exp = $call.exp; }
;

callExpression returns[Expression exp]:
    other=otherExpression call=callExpressionTemp[$other.exp]
    { $exp = $call.exp; }
;

callExpressionTemp[Expression instance] returns[Expression exp] locals[ArrayCall a, MethodCall m, FieldCall f]:
    LBRACKET index=expression RBRACKET {$a = new ArrayCall($instance, $index.exp); } call=callExpressionTemp[$a]
        { $exp = $call.exp; $exp.line = $instance.line; $exp.col = $instance.col; }
    | DOT name=id LPAREN RPAREN { $m = new MethodCall($instance, $name.identifier); } call2=callExpressionTemp[$m]
        { $exp = $call2.exp; $exp.line = $instance.line; $exp.col = $instance.col; }
    | DOT name2=id LPAREN { $m = new MethodCall($instance, $name2.identifier); } (args=expression COMMA { $m.addArg($args.exp); } )* arg=expression { $m.addArg($arg.exp); } RPAREN call3=callExpressionTemp[$m]
        { $exp = $call3.exp; $exp.line = $instance.line; $exp.col = $instance.col; }
    | DOT name4=id { $f = new FieldCall($instance, $name4.identifier); } call4=callExpressionTemp[$f]
        { $exp = $call4.exp; $exp.line = $instance.line; $exp.col = $instance.col; }
    |
        { $exp = $instance; }
;

otherExpression returns[Expression exp] locals[MethodCall m]:
    a=INTLIT { $exp = new IntValue($a.int); $exp.line = $a.line; $exp.col = $a.pos; }
    | b=STRINGLIT { $exp = new StringValue($b.text); $exp.line = $b.line; $exp.col = $b.pos; }
    | f=FALSE { $exp = new BoolValue(false); $exp.line = $f.line; $exp.col = $f.pos; }
    | t=TRUE { $exp = new BoolValue(true); $exp.line = $t.line; $exp.col = $t.pos; }
    | s=SELF { $exp = new Self(); }
    | n=NEW className=id LPAREN RPAREN { $exp = new NewClassInstance($className.identifier); $exp.line = $n.line; $exp.col = $n.pos;}
    | n1=NEW INT LBRACKET len=expression RBRACKET { $exp = new NewArray(new IntType(), $len.exp); $exp.line = $n1.line; $exp.col = $n1.pos; }
    | n2=NEW STRING LBRACKET len2=expression RBRACKET { $exp = new NewArray(new StringType(), $len2.exp); $exp.line = $n2.line; $exp.col = $n2.pos;}
    | n3=NEW BOOL LBRACKET len3=expression RBRACKET { $exp = new NewArray(new BoolType(), $len3.exp); $exp.line = $n3.line; $exp.col = $n3.pos;}
    | n4=NEW name=id LBRACKET len4=expression RBRACKET { $exp = new NewArray(new UserDefinedType(new ClassDeclaration($name.identifier)), $len4.exp); $exp.line = $n4.line; $exp.col = $n4.pos;}
    | var=id { $exp = $var.identifier; $exp.line = $var.identifier.line; $exp.col = $var.identifier.col;}
    | name2=id LPAREN RPAREN { $exp = new MethodCall(new Self(), $name2.identifier); $exp.line = $name2.identifier.line; $exp.col = $name2.identifier.col;}
    | name3=id LPAREN { $m = new MethodCall(new Self(), $name3.identifier); } (e=expression COMMA { $m.addArg($e.exp); } )* e2=expression RPAREN { $m.addArg($e2.exp); $exp = $m; $exp.line = $name3.identifier.line; $exp.col = $name3.identifier.col;}
    | name4=id LBRACKET e2=expression RBRACKET { $exp = new ArrayCall($name4.identifier, $e2.exp); $exp.line = $name4.identifier.line; $exp.col = $name4.identifier.col;}
    | LPAREN exp2=expression RPAREN { $exp = $exp2.exp; $exp.line = $exp2.exp.line; $exp.col = $exp2.exp.col;}
;

INTLIT: [1-9][0-9]* | [0];

WS: [ \t] -> skip;

NEWLINE: [\n\r] -> skip;

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




