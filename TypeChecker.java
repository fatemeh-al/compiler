package toorla.visitor;

import sun.awt.Symbol;
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
import toorla.symbolTable.exceptions.ItemNotFoundException;
import toorla.types.Type;
import toorla.symbolTable.symbolTableItem.varItems.*;
import toorla.symbolTable.symbolTableItem.*;
import toorla.types.UndefinedType;
import toorla.types.singleType.BoolType;
import toorla.types.singleType.IntType;
import toorla.types.singleType.StringType;
import toorla.types.singleType.UserDefinedType;

public class TypeChecker implements Visitor<Type> {

    private boolean inLoop;

    public TypeChecker(){
        this.inLoop = false;
    }

    public boolean subTypeChecker(Type child, Type parent)
    {
        return true;
    }

    @Override
    public Type visit(Program program) {
        return null;
    }

    @Override
    public Type visit(PrintLine printLine) {
        Type argType = printLine.getArg().accept(this);
        if(!(argType.toString().equals("(IntType)") || argType.toString().equals("(StringType)")
                || argType.toString().equals("(ArrayType,(IntType))") || argType.toString().equals("(UndefinedType)")))
            System.out.println("Error:Line:" + printLine.line + ":;");
        return null;
    }

    @Override
    public Type visit(Assign assign) {
        Type lhs = assign.getLvalue().accept(this);
        if(!lhs.getLvalue())
            System.out.println("Error:Line:" + assign.line + ":;");
        Type rhs = assign.getRvalue().accept(this);
        if(!subTypeChecker(rhs, lhs))
            System.out.println("Error:Line:" + assign.line + ":;");
        return null;
    }

    @Override
    public Type visit(Block block) {
        SymbolTable.push(new SymbolTable(SymbolTable.top()));
        for(Statement stat: block.body)
            stat.accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Type visit(Conditional conditional) {
        Type condType = conditional.getCondition().accept(this);
        if(!(condType.toString().equals("(BoolType)") || condType.toString().equals("(UndefinedType)")))
            System.out.println("Error:Line:" + conditional.line + ":;");
        SymbolTable.push(new SymbolTable(SymbolTable.top()));
        conditional.getThenStatement().accept(this);
        SymbolTable.pop();
        SymbolTable.push(new SymbolTable(SymbolTable.top()));
        conditional.getThenStatement().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Type visit(While whileStat) {
        this.inLoop = true;
        Type condType = whileStat.expr.accept(this);
        if(!(condType.toString().equals("(BoolType)") || condType.toString().equals("(UndefinedType)")))
            System.out.println("Error:Line:" + whileStat.line + ":;");
        SymbolTable.push(new SymbolTable(SymbolTable.top()));
        whileStat.body.accept(this);
        SymbolTable.pop();
        this.inLoop = false;
        return null;
    }

    @Override
    public Type visit(Break breakStat) {
        if(!this.inLoop)
            System.out.println("Error:Line:" + breakStat.line + ":;");
        return null;
    }

    @Override
    public Type visit(Continue continueStat) {
        if(!this.inLoop)
            System.out.println("Error:Line:" + continueStat.line + ":;");
        return null;
    }

    @Override
    public Type visit(Skip skip) {
        return null;
    }

    @Override
    public Type visit(Return returnStat) {
        Type retType = returnStat.getReturnedExpr().accept(this);
        try{
            VarSymbolTableItem retItem = (VarSymbolTableItem)SymbolTable.top().get("$ret");
            Type methodRetType = retItem.getVarType();
            if(!subTypeChecker(retType, methodRetType))
                System.out.println("Error:Line:" + returnStat.line + ":;");
        }catch(ItemNotFoundException e) {
        }
        return null;
    }

    @Override
    public Type visit(Plus plusExpr) {
        Type lhs = plusExpr.getLhs().accept(this);
        Type rhs = plusExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return lhs;
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + plusExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Minus minusExpr) {
        Type lhs = minusExpr.getLhs().accept(this);
        Type rhs = minusExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return lhs;
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + minusExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Times timesExpr) {
        Type lhs = timesExpr.getLhs().accept(this);
        Type rhs = timesExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return lhs;
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + timesExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Division divisionExpr) {
        Type lhs = divisionExpr.getLhs().accept(this);
        Type rhs = divisionExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return lhs;
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + divisionExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Modulo moduloExpr){
        Type lhs = moduloExpr.getLhs().accept(this);
        Type rhs = moduloExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return lhs;
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + moduloExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(GreaterThan gtExpr) {
        Type lhs = gtExpr.getLhs().accept(this);
        Type rhs = gtExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return lhs;
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + gtExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(LessThan ltExpr) {
        Type lhs = ltExpr.getLhs().accept(this);
        Type rhs = ltExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return lhs;
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + ltExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Neg negExpr) {
        Type expr = negExpr.getExpr().accept(this);
        if(expr.toString().equals("(IntType)"))
            return expr;
        else if(expr.toString().equals("(UndefinedType)"))
            return expr;
        else {
            System.out.println("Error:Line:" + negExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(And andExpr) {
        Type lhs = andExpr.getLhs().accept(this);
        Type rhs = andExpr.getRhs().accept(this);
        if(lhs.toString().equals("(BoolType)") && rhs.toString().equals("(BoolType)"))
            return lhs;
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(BoolType)"))
                || (lhs.toString().equals("(BoolType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + andExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Or orExpr) {
        Type lhs = orExpr.getLhs().accept(this);
        Type rhs = orExpr.getRhs().accept(this);
        if(lhs.toString().equals("(BoolType)") && rhs.toString().equals("(BoolType)"))
            return lhs;
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(BoolType)"))
                || (lhs.toString().equals("(BoolType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + orExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Not notExpr) {
        Type expr = notExpr.getExpr().accept(this);
        if(expr.toString().equals("(BoolType)"))
            return expr;
        else if(expr.toString().equals("(UndefinedType)"))
            return expr;
        else {
            System.out.println("Error:Line:" + notExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Equals equalsExpr) {
        Type lhs = equalsExpr.getLhs().accept(this);
        Type rhs = equalsExpr.getRhs().accept(this);
        if(lhs.toString().equals(rhs.toString()))
            return lhs;
        else if(lhs.toString().equals("(UndefinedType)") || rhs.toString().equals("(UndefinedType)"))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + equalsExpr.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(NotEquals notEquals) {
        Type lhs = notEquals.getLhs().accept(this);
        Type rhs = notEquals.getRhs().accept(this);
        if(lhs.toString().equals(rhs.toString()))
            return lhs;
        else if(lhs.toString().equals("(UndefinedType)") || rhs.toString().equals("(UndefinedType)"))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + notEquals.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(IntValue intValue) {
        return new IntType();
    }

    @Override
    public Type visit(BoolValue booleanValue) {
        return new BoolType();
    }

    @Override
    public Type visit(StringValue stringValue) {
        return new StringType();
    }

    @Override
    public Type visit(IncStatement incStatement) {
        Type operand = incStatement.getOperand().accept(this);
        if((operand.toString().equals("(IntType)") || operand.toString().equals("(UndefinedType)"))){//Type is Correct
            if(operand.getLvalue())
                return operand;
            else
                System.out.println("Error:Line:" + incStatement.line + ":;");
        }
        else{//Type is wrong
            if(operand.getLvalue())
                System.out.println("Error:Line:" + incStatement.line + ":;");
            else {
                System.out.println("Error:Line:" + incStatement.line + ":;");
                System.out.println("Error:Line:" + incStatement.line + ":;");
            }
        }
        return new UndefinedType();
    }

    @Override
    public Type visit(DecStatement decStatement) {
        Type operand = decStatement.getOperand().accept(this);
        if((operand.toString().equals("(IntType)") || operand.toString().equals("(UndefinedType)"))){//Type is Correct
            if(operand.getLvalue())
                return operand;
            else
                System.out.println("Error:Line:" + decStatement.line + ":;");
        }
        else{//Type is wrong
            if(operand.getLvalue())
                System.out.println("Error:Line:" + decStatement.line + ":;");
            else {
                System.out.println("Error:Line:" + decStatement.line + ":;");
                System.out.println("Error:Line:" + decStatement.line + ":;");
            }
        }
        return new UndefinedType();
    }

    @Override
    public Type visit(MethodCall methodCall) {
        return null;
    }

    @Override
    public Type visit(Identifier identifier) {
        return null;
    }

    @Override
    public Type visit(Self self) {
        return null;
    }

    @Override
    public Type visit(NewArray newArray) {
        return null;
    }

    @Override
    public Type visit(NewClassInstance newClassInstance) {
        return null;
    }

    @Override
    public Type visit(FieldCall fieldCall) {
        return null;
    }

    @Override
    public Type visit(ArrayCall arrayCall) {
        return null;
    }

    @Override
    public Type visit(LocalVarDef localVarDef) {
        return null;
    }

    @Override
    public Type visit(ClassDeclaration classDeclaration) {
        return null;
    }

    @Override
    public Type visit(EntryClassDeclaration entryClassDeclaration) {
        return null;
    }

    @Override
    public Type visit(FieldDeclaration fieldDeclaration) {
        return null;
    }

    @Override
    public Type visit(ParameterDeclaration parameterDeclaration) {
        return null;
    }

    @Override
    public Type visit(MethodDeclaration methodDeclaration) {
        return null;
    }

    @Override
    public Type visit(LocalVarsDefinitions localVarsDefinitions) {
        return null;
    }
}
