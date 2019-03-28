package toorla.visitor;

import toorla.ast.Program;
import toorla.ast.declaration.classDecs.ClassDeclaration;
import toorla.ast.declaration.classDecs.EntryClassDeclaration;
import toorla.ast.declaration.classDecs.classMembersDecs.FieldDeclaration;
import toorla.ast.declaration.classDecs.classMembersDecs.MethodDeclaration;
import toorla.ast.declaration.localVarDecs.ParameterDeclaration;
import toorla.ast.expression.*;
import toorla.ast.expression.binaryExpression.*;
import toorla.ast.expression.unaryExpression.Neg;
import toorla.ast.expression.unaryExpression.Not;
import toorla.ast.expression.value.BoolValue;
import toorla.ast.expression.value.IntValue;
import toorla.ast.expression.value.StringValue;
import toorla.ast.statement.*;
import toorla.ast.statement.localVarStats.LocalVarDef;
import toorla.ast.statement.localVarStats.LocalVarsDefinitions;
import toorla.ast.statement.returnStatement.Return;

import toorla.ast.declaration.classDecs.classMembersDecs.ClassMemberDeclaration;

import java.util.ArrayList;
import java.util.List;

public class TreePrinter implements Visitor<Void> {
    //TODO : Implement all visit methods in TreePrinter to print AST as required in phase1 document
    @Override
    public Void visit(PrintLine printStat) {
        System.out.print("( ");
        printStat.getArg().accept(this);
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(Assign assignStat) {
        System.out.print("(= " );
        assignStat.getLvalue().accept(this);
        System.out.print(" ");
        assignStat.getRvalue().accept(this);
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(Block block) {
        System.out.println("(");
        for(Statement stat:block.body) {
            stat.accept(this);
            System.out.print(" ");
        }
        System.out.println(")");
        return null;
    }

    @Override
   public Void visit(Conditional conditional) {
        Expression condition = conditional.getCondition();
        Statement thenStmt = conditional.getThenStatement();
        Statement elseStmt = conditional.getElseStatement();
        System.out.print("(if ");
        condition.accept(this);
        System.out.print(" ");
        thenStmt.accept(this);
        System.out.print(" ");
        if(elseStmt!=null){
            elseStmt.accept(this);
            System.out.print(" ");
        }
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(While whileStat) {
        System.out.print("(while ");
        whileStat.expr.accept(this);
        System.out.print(" ");
        whileStat.body.accept(this);
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(Return returnStat) {
        System.out.print("(return ");
        returnStat.getReturnedExpr().accept(this);
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(Break breakStat) {
        String string=breakStat.toString();
        System.out.println(string);
        return null;
    }

    @Override
    public Void visit(Continue continueStat) {
        String string=continueStat.toString();
        System.out.println(string);
        return null;
    }

    @Override
    public Void visit(Skip skip) {
        String string=skip.toString();
        System.out.println(string);
        return null;
    }

    @Override
    public Void visit(LocalVarDef localVarDef) {
        System.out.print("(var ");
        localVarDef.getLocalVarName().accept(this);
        System.out.print(" ");
        localVarDef.getInitialValue().accept(this);
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(IncStatement incStatement) {
        System.out.print("(++ ");
        incStatement.getOperand().accept(this);
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(DecStatement decStatement) {
        System.out.print("(-- ");
        decStatement.getOperand().accept(this);
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(Plus plusExpr) {
        System.out.print("(+ ");
        plusExpr.getLhs().accept(this);
        System.out.print(" ");
        plusExpr.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(Minus minusExpr) {
        System.out.print("(- ");
        minusExpr.getLhs().accept(this);
        System.out.print(" ");
        minusExpr.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(Times timesExpr) {
        System.out.print("(* ");
        timesExpr.getLhs().accept(this);
        System.out.print(" ");
        timesExpr.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(Division divExpr) {
        System.out.print("(/ ");
        divExpr.getLhs().accept(this);
        System.out.print(" ");
        divExpr.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(Modulo moduloExpr) {
        System.out.print("(% ");
        moduloExpr.getLhs().accept(this);
        System.out.print(" ");
        moduloExpr.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(Equals equalsExpr) {
        System.out.print("(== ");
        equalsExpr.getLhs().accept(this);
        System.out.print(" ");
        equalsExpr.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(GreaterThan gtExpr) {
        System.out.print("(> ");
        gtExpr.getLhs().accept(this);
        System.out.print(" ");
        gtExpr.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(LessThan lessThanExpr) {
        System.out.print("(< ");
        lessThanExpr.getLhs().accept(this);
        System.out.print(" ");
        lessThanExpr.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(And andExpr) {
        System.out.print("(&& ");
        andExpr.getLhs().accept(this);
        System.out.print(" ");
        andExpr.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(Or orExpr) {
        System.out.print("(|| ");
        orExpr.getLhs().accept(this);
        System.out.print(" ");
        orExpr.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(Neg negExpr) {
        System.out.print("(- ");
        negExpr.getExpr().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(Not notExpr) {
        System.out.print("(! ");
        notExpr.getExpr().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(MethodCall methodCall) {
        return null;
    }

    @Override
    public Void visit(Identifier identifier) {
        String string=identifier.toString();
        System.out.print(string);
        return null;
    }

    @Override
    public Void visit(Self self) {
        String string=self.toString();
        System.out.print(string);
        return null;
    }

    @Override
    public Void visit(IntValue intValue) {
        String string=intValue.toString();
        System.out.print(string);
        return null;
    }

    @Override
    public Void visit(NewArray newArray) {
        System.out.print("( new array of ");
        String string = newArray.getType().toString();
        System.out.print(string+" ");
        newArray.getLength().accept(this);
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(BoolValue booleanValue) {
        String string=booleanValue.toString();
        System.out.print(string);
        return null;
    }

    @Override
    public Void visit(StringValue stringValue) {
        String string=stringValue.toString();
        System.out.print(string);
        return null;
    }

    @Override
    public Void visit(NewClassInstance newClassInstance) {
        System.out.print("( new ");
        newClassInstance.getClassName().accept(this);
        System.out.print(" )");
        return null;
    }

    @Override
    public Void visit(FieldCall fieldCall) {
        System.out.print("(. ");
        fieldCall.getInstance().accept(this);
        System.out.print(" ");
        fieldCall.getField().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(ArrayCall arrayCall) {
        System.out.print("([] ");
        arrayCall.getInstance().accept(this);
        System.out.print(" ");
        arrayCall.getIndex().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(NotEquals notEquals) {
        System.out.print("(<> ");
        notEquals.getLhs().accept(this);
        System.out.print(" ");
        notEquals.getRhs().accept(this);
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(ClassDeclaration classDeclaration) {
        System.out.print("(class ");
        classDeclaration.getName().accept(this);
        System.out.print(" ");
        if(classDeclaration.getParentName()!=null) {
            classDeclaration.getParentName().accept(this);
            System.out.print(" ");
        }
        ArrayList<ClassMemberDeclaration> body = classDeclaration.getClassMembers();
        for(ClassMemberDeclaration stat:body) {
            stat.accept(this);
            System.out.print(" ");
        }
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(EntryClassDeclaration entryClassDeclaration) {
        System.out.print("(entry class ");
        entryClassDeclaration.getName().accept(this);
        System.out.print(" ");
        if(entryClassDeclaration.getParentName()!=null) {
            entryClassDeclaration.getParentName().accept(this);
            System.out.print(" ");
        }
        ArrayList<ClassMemberDeclaration> body = entryClassDeclaration.getClassMembers();
        for(ClassMemberDeclaration stat:body) {
            stat.accept(this);
            System.out.print(" ");
        }
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(FieldDeclaration fieldDeclaration) {
        System.out.print("( ");
        String string = fieldDeclaration.getAccessModifier().toString();
        System.out.print(string);
        System.out.print(" field ");
        fieldDeclaration.getIdentifier().accept(this);
        System.out.print(" ");
        if(fieldDeclaration.getType()!=null){
            System.out.print(fieldDeclaration.getType().toString());
        }
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(ParameterDeclaration parameterDeclaration) {
        System.out.print("( ");
        parameterDeclaration.getIdentifier().accept(this);
        System.out.print(" : ");
        System.out.print(parameterDeclaration.getType().toString());
        System.out.print(")");
        return null;
    }

    @Override
    public Void visit(MethodDeclaration methodDeclaration) {
        ArrayList<ParameterDeclaration> args = methodDeclaration.getArgs();
        ArrayList<Statement> body = methodDeclaration.getBody();
        System.out.print("( ");
        String string = methodDeclaration.getAccessModifier().toString();
        System.out.print(string);
        System.out.print(" method ");
        methodDeclaration.getName().accept(this);
        System.out.print(" ");
        for(ParameterDeclaration arg:args) {
            arg.accept(this);
            System.out.print(" ");
        }
        System.out.print(methodDeclaration.getReturnType().toString());
        System.out.println(" (");
        for(Statement stat:body) {
            stat.accept(this);
            System.out.print(" ");
        }
        System.out.println(")");
        System.out.println(")");
        return null;
    }

    @Override
    public Void visit(LocalVarsDefinitions localVarsDefinitions) {
        List<LocalVarDef> vars =localVarsDefinitions.getVarDefinitions();
        for(LocalVarDef var:vars) {
            var.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Program program) {
        System.out.print("(");
        List<ClassDeclaration> program_body = program.getClasses();
        for(ClassDeclaration program_class:program_body) {
            program_class.accept(this);
        }
        System.out.println(")");
        return null;
    }
}
