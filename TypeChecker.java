package toorla.visitor;

import toorla.ast.Program;
import toorla.ast.declaration.classDecs.ClassDeclaration;
import toorla.ast.declaration.classDecs.EntryClassDeclaration;
import toorla.ast.declaration.classDecs.classMembersDecs.AccessModifier;
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
import toorla.symbolTable.exceptions.ItemAlreadyExistsException;
import toorla.symbolTable.exceptions.ItemNotFoundException;
import toorla.types.Type;
import toorla.symbolTable.symbolTableItem.varItems.*;
import toorla.symbolTable.symbolTableItem.*;
import toorla.types.UndefinedType;
import toorla.types.arrayType.ArrayType;
import toorla.types.singleType.*;

import java.util.ArrayList;
import java.util.List;

public class TypeChecker implements Visitor<Type> {

    private boolean inLoop;

    public TypeChecker(){
        this.inLoop = false;
    }

    public boolean subTypeChecker(Type child, Type parent) {
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
    public Type visit(FieldDeclaration fieldDeclaration) {
        return null;
    }

    @Override
    public Type visit(LocalVarsDefinitions localVarsDefinitions) {
        for(LocalVarDef def: localVarsDefinitions.getVarDefinitions())
            def.accept(this);
        return null;
    }

    @Override
    public Type visit(LocalVarDef localVarDef) {
        Type initialType = localVarDef.getInitialValue().accept(this);
        VarSymbolTableItem localVar = new VarSymbolTableItem();
        localVar.setName(localVarDef.getLocalVarName().getName());
        localVar.setVarType(initialType);
        try{
            SymbolTable.top().put(localVar);
        }catch(ItemAlreadyExistsException e){
        }
        return null;
    }

    @Override
    public Type visit(NewArray newArray) {
        Type lengthType = newArray.getLength().accept(this);
        SingleType arrayType = newArray.getType();
        if(!(lengthType.toString().equals("(IntType)") || lengthType.toString().equals("(UndefinedType)")))
            System.out.println("Error:Line:" + newArray.line + ":;");
        return new ArrayType(arrayType);
    }

    @Override
    public Type visit(ArrayCall arrayCall) {
        Type instanceType = arrayCall.getInstance().accept(this);
        Type arraySingle = new UndefinedType();
        if(instanceType.toString().startsWith("(ArrayType"))
            arraySingle = ((ArrayType)instanceType).getSingleType();
        else if(!instanceType.toString().equals("(UndefinedType)"))
            System.out.println("Error:Line:" + arrayCall.line + ":;");
        Type indexType = arrayCall.getIndex().accept(this);
        if(!indexType.toString().equals("(IntType)") && !indexType.toString().equals("(UndefinedType)"))
            System.out.println("Error:Line:" + arrayCall.line + ":;");
        arraySingle.setLvalue();
        return arraySingle;
    }

    @Override
    public Type visit(ParameterDeclaration parameterDeclaration) {
        Type parameterType = parameterDeclaration.getType();
        try{
            VarSymbolTableItem parameter = new VarSymbolTableItem();
            parameter.setName(parameterDeclaration.getIdentifier().getName());
            parameter.setVarType(parameterType);
            SymbolTable.top().put(parameter);
        }catch(ItemAlreadyExistsException e){
        }
        return null;
    }

    @Override
    public Type visit(Identifier identifier) {
        String name = identifier.getName();
        try {
            VarSymbolTableItem item = (VarSymbolTableItem)(SymbolTable.top().get("var_" + name));
            return item.getVarType();
        }catch(ItemNotFoundException e){
            System.out.println("Error:Line:" + identifier.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Self self) {
        return null;
    }

    @Override
    public Type visit(NewClassInstance newClassInstance) {
        String className = newClassInstance.getClassName().getName();
        try {
            SymbolTableItem item = SymbolTable.root.get("class_" + className);
            Type classType = new UserDefinedType(new ClassDeclaration(newClassInstance.getClassName()));
            return classType;
        }catch(ItemNotFoundException e){
            System.out.println("Error:Line:" + newClassInstance.line + ":;");
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(MethodCall methodCall) {
        Type instanceType = methodCall.getInstance().accept(this);
        String methodName = methodCall.getMethodName().getName();
        ArrayList<Expression> args = methodCall.getArgs();
        if(instanceType.toString().startsWith("(UserDefined")){
            String className = ((UserDefinedType)instanceType).getClassDeclaration().getName().getName();
            try{
                ClassSymbolTableItem classItem = (ClassSymbolTableItem) SymbolTable.root.get("class_" + className);
                SymbolTable classSymbolTable = classItem.getSymbolTable();
                try{
                    MethodSymbolTableItem methodItem = (MethodSymbolTableItem) classSymbolTable.get("method_" + methodName);
                    List<Type> argTypes = methodItem.getArgumentsTypes();
                    if (argTypes.size() != args.size()){
                        System.out.println("Error:Line:" + methodCall.line + ":;");
                        return new UndefinedType();
                    }
                    for(int i = 0; i < args.size(); i++){
                        Type singleArgType = args.get(i).accept(this);
                        if(!subTypeChecker(singleArgType, argTypes.get(i))){
                            System.out.println("Error:Line:" + methodCall.line + ":;");
                            return new UndefinedType();
                        }
                    }
                    AccessModifier access = methodItem.getAccessModifier();
                    if(access.toString().equals("(private)") && !methodCall.getInstance().toString().equals("(Self)"))
                    {
                        System.out.println("Error:Line:" + methodCall.line + ":;");
                        return new UndefinedType();
                    }
                    return methodItem.getReturnType();
                }catch(ItemNotFoundException e2){
                    System.out.println("Error:Line:" + methodCall.line + ":;");
                }
            }catch(ItemNotFoundException e1){
                System.out.println("Error:Line:" + methodCall.line + ":;");
            }
        }
        return new UndefinedType();
    }

    @Override
    public Type visit(FieldCall fieldCall) {
        Type instanceType = fieldCall.getInstance().accept(this);
        String fieldName = fieldCall.getField().getName();
        if(instanceType.toString().startsWith("(UserDefined")){
            String className = ((UserDefinedType)instanceType).getClassDeclaration().getName().getName();
            try{
                ClassSymbolTableItem classItem = (ClassSymbolTableItem) SymbolTable.root.get("class_" + className);
                SymbolTable classSymbolTable = classItem.getSymbolTable();
                try{
                    FieldSymbolTableItem fieldItem  = (FieldSymbolTableItem)classSymbolTable.get("var_" + fieldName);
                    AccessModifier access = fieldItem.getAccessModifier();
                    if(access.toString().equals("(private)") && !fieldCall.toString().equals("(Self)")){
                        System.out.println("Error:Line:" + fieldCall.line + ":;");
                        return new UndefinedType();
                    }
                    return fieldItem.getVarType();
                }catch(ItemNotFoundException e2){
                    System.out.println("Error:Line:" + fieldCall.line + ":;");
                }
            }catch(ItemNotFoundException e1){
                System.out.println("Error:Line:" + fieldCall.line + ":;");
            }
        }
        else if(instanceType.toString().startsWith("(ArrayType")){
            if(!fieldName.equals("length")){
                System.out.println("Error:Line:" + fieldCall.line + ":;");
                return new UndefinedType();
            }
            return new IntType();
        }
        return new UndefinedType();
    }

    @Override
    public Type visit(MethodDeclaration methodDeclaration) {
        SymbolTable.push(new SymbolTable(SymbolTable.top()));
        VarSymbolTableItem returnItem = new VarSymbolTableItem();
        Type returnType = methodDeclaration.getReturnType();
        if(returnType.toString().startsWith("(UserDefinedType")) {
            String retClassName = returnType.toString().substring(16, returnType.toString().indexOf(")"));
            System.out.println("RETCLASS RO IN PEYDA KARDAM   :" + retClassName);
            try{
                SymbolTableItem retClassItem = SymbolTable.root.get("class_" + retClassName);
            }catch(ItemNotFoundException e1){
                System.out.println("Error:Line:" + methodDeclaration.getName().line + ":;");
                returnType = new UndefinedType();
            }
        }
        returnItem.setVarType(returnType);
        returnItem.setName("$ret");
        try{
            SymbolTable.top().put(returnItem);
        }catch(ItemAlreadyExistsException e2){
        }
        for(ParameterDeclaration arg: methodDeclaration.getArgs()){
            VarSymbolTableItem argItem = new VarSymbolTableItem();
            argItem.setVarType(arg.getType());
            if(arg.getType().toString().startsWith("(UserDefinedType")) {
                String argCLassName = arg.getType().toString().substring(16, arg.getType().toString().indexOf(")"));
                try{
                    SymbolTableItem argClassItem = SymbolTable.root.get("class_" + argCLassName);
                }catch(ItemNotFoundException e4){
                    System.out.println("Error:Line:" + methodDeclaration.getName().line + ":;");
                    argItem.setVarType(new UndefinedType());
                }
            }
            argItem.setName(arg.getIdentifier().getName());
            try{
                SymbolTable.top().put(argItem);
            }catch(ItemAlreadyExistsException e3){
            }
        }
        for(Statement stat: methodDeclaration.getBody())
            stat.accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Type visit(ClassDeclaration classDeclaration) {
        try{
            ClassSymbolTableItem classItem = (ClassSymbolTableItem)SymbolTable.root.get("class_" + classDeclaration.getName().getName());
            SymbolTable classSym = classItem.getSymbolTable();
            if(classDeclaration.getParentName().getName() != null) {
                try {
                    SymbolTableItem parentItem = SymbolTable.root.get("class_" + classDeclaration.getParentName().getName());
                } catch (ItemNotFoundException e1) {
                    System.out.println("Error:Line:" + classDeclaration.getName().line + ":;");
                    try{
                        ClassSymbolTableItem anyItem = (ClassSymbolTableItem) SymbolTable.root.get("class_Any");
                        classSym.setPreSymbolTable(anyItem.getSymbolTable());
                    }catch (ItemNotFoundException e4){
                    }
                }
            }
            SymbolTable.push(classSym);
            for(ClassMemberDeclaration member: classDeclaration.getClassMembers())
                member.accept(this);
        }catch(ItemNotFoundException e2){
        }
        return null;
    }

    @Override
    public Type visit(EntryClassDeclaration entryClassDeclaration) {
        try{
            ClassSymbolTableItem classItem = (ClassSymbolTableItem)SymbolTable.root.get("class_" + entryClassDeclaration.getName().getName());
            SymbolTable classSym = classItem.getSymbolTable();
            if(entryClassDeclaration.getParentName().getName() != null) {
                try {
                    SymbolTableItem parentItem = SymbolTable.root.get("class_" + entryClassDeclaration.getParentName().getName());
                } catch (ItemNotFoundException e1) {
                    System.out.println("Error:Line:" + entryClassDeclaration.getName().line + ":;");
                    try{
                        ClassSymbolTableItem anyItem = (ClassSymbolTableItem) SymbolTable.root.get("class_Any");
                        classSym.setPreSymbolTable(anyItem.getSymbolTable());
                    }catch (ItemNotFoundException e4){
                    }
                }
            }
            SymbolTable.push(classSym);
            try{
                MethodSymbolTableItem mainItem = (MethodSymbolTableItem)(SymbolTable.top().get("method_main"));
                if(!mainItem.getReturnType().toString().equals("(IntType)")
                        || mainItem.getArgumentsTypes().size() != 0
                        || !mainItem.getAccessModifier().toString().equals("(public)"))
                    System.out.println("Error:Line:" + entryClassDeclaration.getName().line + ":;");
            }catch(ItemNotFoundException e3){
                System.out.println("Error:Line:" + entryClassDeclaration.getName().line + ":;");
            }
            for(ClassMemberDeclaration member: entryClassDeclaration.getClassMembers())
                member.accept(this);
        }catch(ItemNotFoundException e2){
        }
        return null;
    }
}


//Tooye newClassInstance ke mikham begiram, bayad havasam bashe pedar dare ya ne?
//az to stringe AccessModifier motmaen nistam tooye methodCall va tooye fieldCall
//bebin classa voojood dashte bashan to methodDeclaration ba subString
