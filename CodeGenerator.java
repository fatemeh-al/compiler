package toorla.visitor;

import toorla.ast.Program;
import toorla.ast.declaration.classDecs.ClassDeclaration;
import toorla.ast.declaration.classDecs.EntryClassDeclaration;
import toorla.ast.declaration.classDecs.classMembersDecs.ClassMemberDeclaration;
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
import toorla.symbolTable.SymbolTable;
import toorla.types.Type;

import java.io.FileWriter;

public class CodeGenerator extends Visitor<Void>{

    private FileWriter writer;

    public Void writeInCurrentFile(String code){
        try{
            this.writer.write(code);
            //CHECK IF IT WRITES A NEW LINE OR NOT
        }catch(Exception e){}
        return null;
    }

    public Void visit(Plus plusExpr) {
        plusExpr.getLhs().accept(this);
        plusExpr.getRhs().accept(this);
        this.writeInCurrentFile("iadd");
        return null;
    }

    public Void visit(Minus minusExpr) {
        //Which one should be first?
        minusExpr.getLhs().accept(this);
        minusExpr.getRhs().accept(this);
        this.writeInCurrentFile("isub");
        return null;
    }

    public Void visit(Times timesExpr) {
        timesExpr.getLhs().accept(this);
        timesExpr.getRhs().accept(this);
        this.writeInCurrentFile("imul");
        return null;
    }

    public Void visit(Division divExpr) {
        //Which one should be first???
        divExpr.getLhs().accept(this);
        divExpr.getRhs().accept(this);
        this.writeInCurrentFile("idiv");
        return null;
    }

    public Void visit(Modulo moduloExpr) {
        //Which one should be first???
        moduloExpr.getLhs().accept(this);
        moduloExpr.getRhs().accept(this);
        this.writeInCurrentFile("irem");
        return null;
    }

    public Void visit(Equals equalsExpr) {
        return null;
    }

    public Void visit(GreaterThan gtExpr) {
        return null;
    }

    public Void visit(LessThan lessThanExpr) {
        return null;
    }

    public Void visit(And andExpr) {
        return null;
    }

    public Void visit(Or orExpr) {
        return null;
    }

    public Void visit(Neg negExpr) {
        return null;
    }

    public Void visit(Not notExpr) {
        return null;
    }

    public Void visit(MethodCall methodCall) {
        return null;
    }

    public Void visit(Identifier identifier) {
        return null;
    }

    public Void visit(Self self) {
        return null;
    }

    public Void visit(IntValue intValue) {
        return null;
    }

    public Void visit(NewArray newArray) {
        return null;
    }

    public Void visit(BoolValue booleanValue) {
        return null;
    }

    public Void visit(StringValue stringValue) {
        return null;
    }

    public Void visit(NewClassInstance newClassInstance) {
        return null;
    }

    public Void visit(FieldCall fieldCall) {
        return null;
    }

    public Void visit(ArrayCall arrayCall) {
        return null;
    }

    public Void visit(NotEquals notEquals) {
        return null;
    }

    // Statement
    public Void visit(PrintLine printStat) {
        return null;
    }

    public Void visit(Assign assignStat) {
        return null;
    }

    public Void visit(Block block) {
        return null;
    }

    public Void visit(Conditional conditional) {
        return null;
    }

    public Void visit(While whileStat) {
        return null;
    }

    public Void visit(Return returnStat) {
        return null;
    }

    public Void visit(Break breakStat) {
        return null;
    }

    public Void visit(Continue continueStat) {
        return null;
    }

    public Void visit(Skip skip) {
        return null;
    }

    public Void visit(LocalVarDef localVarDef) {
        return null;
    }

    public Void visit(IncStatement incStatement) {
        return null;
    }

    public Void visit(DecStatement decStatement) {
        return null;
    }

    // declarations
    public Void visit(ParameterDeclaration parameterDeclaration) {
        //Anything to do here???
        return null;
    }

    public Void visit(LocalVarsDefinitions localVarsDefinitions) {
        for(LocalVarDef varDef: localVarsDefinitions.getVarDefinitions())
            varDef.accept(this);
        return null;
    }

    public Void visit(FieldDeclaration fieldDeclaration) {
        String descriptor = ".field ";
        if(fieldDeclaration.getAccessModifier().toString().equals("(ACCESS_MODIFIER_PUBLIC)"))
            descriptor += "public ";
        else
            descriptor += "private ";
        descriptor += (fieldDeclaration.getIdentifier().getName() + " " + fieldDeclaration.getType().getSymbol());
        this.writeInCurrentFile(descriptor);
        return null;
    }

    public Void visit(MethodDeclaration methodDeclaration) {
        String descriptor = ".method ";
        if(methodDeclaration.getAccessModifier().toString().equals("(ACCESS_MODIFIER_PUBLIC)"))
            descriptor += "public ";
        else
            descriptor += "private ";
        descriptor += (methodDeclaration.getName().getName() + "(");
        for(ParameterDeclaration arg: methodDeclaration.getArgs())
            descriptor += arg.getType().getSymbol();
        descriptor += (methodDeclaration.getReturnType().getSymbol() + ")");
        this.writeInCurrentFile(descriptor);
        this.writeInCurrentFile(".limit stack 1000");
        this.writeInCurrentFile(".limit locals 100");
        SymbolTable.pushFromQueue();
        for(Statement stat: methodDeclaration.getBody())
            stat.accept(this);
        this.writeInCurrentFile(".end method");
        SymbolTable.pop();
        return null;
    }

    public Void visit(ClassDeclaration classDeclaration) {
        //Age bayad esme classa ba fileshoon yeki bashe, pas "class_" ro koja ezafe konam ke ruuner handle she?
        try{
            this.writer = new FileWriter("artifact/" + classDeclaration.getName().getName() + ".j");
            this.writeInCurrentFile(".class public " + "class_" + classDeclaration.getName().getName());
            if(classDeclaration.getParentName().getName().equals("Any"))
                this.writeInCurrentFile(".super java/lang/Object");
            else
                this.writeInCurrentFile(".super class_" +classDeclaration.getParentName().getName());
            SymbolTable.pushFromQueue();
            for(ClassMemberDeclaration cmd: classDeclaration.getClassMembers())
                cmd.accept(this);
            SymbolTable.pop();
            this.writer.close();
        }catch(Exception e){}
        return null;
    }

    public Void visit(EntryClassDeclaration entryClassDeclaration) {
        //Age bayad esme classa ba fileshoon yeki bashe, pas "class_" ro koja ezafe konam ke ruuner handle she?
        try{
            this.writer = new FileWriter("artifact/" + entryClassDeclaration.getName().getName() + ".j");
            this.writeInCurrentFile(".class public class_" + entryClassDeclaration.getName().getName());
            if(entryClassDeclaration.getParentName().getName().equals("Any"))
                this.writeInCurrentFile(".super java/lang/Object");
            else
                this.writeInCurrentFile(".super class_" + entryClassDeclaration.getParentName().getName());
            SymbolTable.pushFromQueue();
            for(ClassMemberDeclaration cmd: entryClassDeclaration.getClassMembers())
                cmd.accept(this);
            SymbolTable.pop();
            this.writer.close();
        }catch(Exception e){}
        return null;
    }

    public Void visit(Program program) {
        //create Runner class
        SymbolTable.pushFromQueue();
        for( ClassDeclaration classDeclaration : program.getClasses() )
            classDeclaration.accept(this);
        SymbolTable.pop();
        return null;
    }
}
