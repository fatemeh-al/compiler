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

import java.io.FileWriter;

public class CodeGenerator extends Visitor<Void>{

    private FileWriter writer;

    public Void writeInCurrentFile(String code){
        try{
            this.writer.write(code);
            //CHECK IF IT GOES TO A NEW LINE OR NOT
        }catch(Exception e){}
        return null;
    }

    public Void visit(Plus plusExpr) {
        return null;
    }

    public Void visit(Minus minusExpr) {
        return null;
    }

    public Void visit(Times timesExpr) {
        return null;
    }

    public Void visit(Division divExpr) {
        return null;
    }

    public Void visit(Modulo moduloExpr) {
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
        return null;
    }

    public Void visit(LocalVarsDefinitions localVarsDefinitions) {
        return null;
    }

    public Void visit(FieldDeclaration fieldDeclaration) {
        return null;
    }

    public Void visit(MethodDeclaration methodDeclaration) {
        //push SymbolTable
        //.method
        //.limit stack
        //.limit locals
        //statements
        //.end method
        return null;
    }

    public Void visit(ClassDeclaration classDeclaration) {
        try{
            this.writer = new FileWriter("artifact/" + classDeclaration.getName().getName() + ".j");
            //.class
            //.super
            //push SymbolTable
            //fields and methods
            //pop SymbolTable
            this.writer.close();
        }catch(Exception e){}
        return null;
    }

    public Void visit(EntryClassDeclaration entryClassDeclaration) {
        try{
            this.writer = new FileWriter("artifact/" + entryClassDeclaration.getName().getName() + ".j");
            //.class
            //.super
            //push SymbolTable
            //fields and methods
            //pop SymbolTable
            this.writer.close();
        }catch(Exception e){}
        return null;
    }

    public Void visit(Program program) {
        //create Runner class
        //Check if you should push Any symbolTable from queue or not
        //visit classes
        return null;
    }
}
