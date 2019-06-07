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
import toorla.symbolTable.symbolTableItem.ClassSymbolTableItem;
import toorla.symbolTable.symbolTableItem.MethodSymbolTableItem;
import toorla.symbolTable.symbolTableItem.varItems.VarSymbolTableItem;
import toorla.typeChecker.ExpressionTypeExtractor;
import toorla.types.Type;
import toorla.types.arrayType.ArrayType;
import toorla.types.singleType.*;
import toorla.utilities.graph.Graph;
import toorla.utilities.stack.Stack;

import java.io.File;
import java.io.FileWriter;

public class CodeGenerator extends Visitor<Void>{
    private ExpressionTypeExtractor expressionTypeExtractor;
    private static Stack<String> breaks = new Stack<>();
    private static Stack<String> continues = new Stack<>();
    private FileWriter writer;
    private int labelNum;
    private int definedVars;
    private boolean isLeft;
    private String currentClass;
    private String putFieldCommand;

    //Every comment is important. check if all of them are done

    public CodeGenerator(Graph<String> classHierarchy){
        this.isLeft = false;
        this.labelNum = 0;
        this.expressionTypeExtractor = new ExpressionTypeExtractor(classHierarchy);
    }

    public Void writeInCurrentFile(String code){
        try{
            this.writer.write(code + "\n");
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

    public Void visit(Neg negExpr) {
        negExpr.getExpr().accept(this);
        this.writeInCurrentFile("ineg");
        return null;
    }

    public Void visit(And andExpr) {
        //p?q:false
        andExpr.getLhs().accept(this);
        this.labelNum++;
        String previousLabel = "Label" + this.labelNum;
        this.writeInCurrentFile("ifeq " + previousLabel);
        andExpr.getRhs().accept(this);
        this.labelNum++;
        this.writeInCurrentFile("goto Label" + this.labelNum);
        this.writeInCurrentFile(previousLabel + ": ldc 0");
        this.writeInCurrentFile("Label" + this.labelNum + ":");
        return null;
    }

    public Void visit(Or orExpr) {
        //p?true:q
        orExpr.getLhs().accept(this);
        this.labelNum++;
        String previousLabel = "Label" + this.labelNum;
        this.writeInCurrentFile("ifeq " + previousLabel);
        this.writeInCurrentFile("ldc 1");
        this.labelNum++;
        this.writeInCurrentFile("goto Label" + this.labelNum);
        this.writeInCurrentFile(previousLabel + ":");
        orExpr.getRhs().accept(this);
        this.writeInCurrentFile("Label" + this.labelNum + ":");
        return null;
    }

    public Void visit(IntValue intValue) {
        this.writeInCurrentFile("ldc " + intValue.getConstant());
        return null;
    }

    public Void visit(BoolValue booleanValue) {
        if(booleanValue.isConstant())
            this.writeInCurrentFile("ldc 1");//iconst_1
        else
            this.writeInCurrentFile("ldc 0");//iconst_0
        return null;
    }

    public Void visit(StringValue stringValue) {
        this.writeInCurrentFile("ldc " + stringValue.getConstant());
        return null;
    }

    public Void visit(Self self) {
        this.writeInCurrentFile("aload_0");
        return null;
    }

    public Void visit(Equals equalsExpr) { //CHECK THIS NODE
        equalsExpr.getLhs().accept(this);
        equalsExpr.getRhs().accept(this);
        Type Lhs_t=equalsExpr.getLhs().accept(expressionTypeExtractor);
        if(Lhs_t instanceof StringType || (Lhs_t instanceof UserDefinedType)){
            this.writeInCurrentFile("invokevirtual  java/lang/String/equals(Ljava/lang/Object;)Z");
        }
        if((Lhs_t instanceof IntType) || (Lhs_t instanceof BoolType)){
            labelNum++;
            int first=labelNum;
            this.writeInCurrentFile("if_icmpeq "+"Label"+first);
            this.writeInCurrentFile("ldc 0");
            labelNum++;
            int second=labelNum;
            this.writeInCurrentFile("goto "+"Label"+second);
            this.writeInCurrentFile("Label"+first+":");
            this.writeInCurrentFile("ldc 1");
            this.writeInCurrentFile("Label"+second+":");
        }
        if(Lhs_t instanceof ArrayType){
            if(Lhs_t instanceof ArrayType){
                this.writeInCurrentFile(" invokestatic  java/util/Arrays/equals("+Lhs_t.getSymbol()+Lhs_t.getSymbol()+")Z");
            }
        }
        //moghayaseye 2 ta userdefinded type?shayad aslan method equal tarif nashode bashe barash
        return null;
    }

    public Void visit(GreaterThan gtExpr) {//CHECK THIS NODE
        gtExpr.getLhs().accept(this);
        gtExpr.getRhs().accept(this);
        this.labelNum++;
        int first = this.labelNum;
        this.writeInCurrentFile("if_icmple "+"Label"+first );
        this.writeInCurrentFile("ldc 1");
        this.labelNum++;
        int second = this.labelNum;
        this.writeInCurrentFile("goto "+"Label"+second);
        this.writeInCurrentFile("Label"+first+":");
        this.writeInCurrentFile("ldc 0");
        this.writeInCurrentFile("Label"+second+":");
        return null;
    }

    public Void visit(LessThan lessThanExpr) { //CHECK THIS NODE
        lessThanExpr.getLhs().accept(this);
        lessThanExpr.getRhs().accept(this);
        this.labelNum++;
        int first = this.labelNum;
        this.writeInCurrentFile("if_icmpgt "+"Label"+first );
        this.writeInCurrentFile("ldc 1");
        this.labelNum++;
        int second = this.labelNum;
        this.writeInCurrentFile("goto "+"Label"+second);
        this.writeInCurrentFile("Label"+first+":");
        this.writeInCurrentFile("ldc 0");
        this.writeInCurrentFile("Label"+second+":");
        return null;
    }

    public Void visit(Not notExpr) { //CHECK THIS NODE
        notExpr.getExpr().accept(this);
        this.labelNum++;
        int first = this.labelNum;
        this.writeInCurrentFile("ifeq " + "Label"+first);
        this.writeInCurrentFile("ldc 0");
        this.labelNum++;
        int second = this.labelNum;
        this.writeInCurrentFile("goto Label"+second);
        this.writeInCurrentFile("Label"+first+":");
        this.writeInCurrentFile("ldc 1");
        this.writeInCurrentFile("Label"+second+":");
        return null;
    }

    public Void visit(NotEquals notEquals) { //CHECK THIS NODE
        Not notnode=new Not(new Equals(notEquals.getLhs(),notEquals.getRhs()));
        notnode.accept(this);
        //moghayaseye 2 ta userdefind type?shayad aslan method equal tarif nashode bashe barash?!
        return null;
    }

    public Void visit(Identifier identifier) {
        try{
            VarSymbolTableItem varItem = (VarSymbolTableItem)SymbolTable.top().get("var_" + identifier.getName());
            Type varType = varItem.getType();
            int index = varItem.getDefinitionNumber();
            if(index > this.definedVars){ //lazeme inja while bezanam ya ba ye if hal mishe?
                varItem = (VarSymbolTableItem)SymbolTable.top().getInParentScopes("var_" + identifier.getName());
                index = varItem.getDefinitionNumber();
                varType = varItem.getType();
            }
            if(varItem.mustBeUsedAfterDef()){ //It is a variable. not a field
                if(this.isLeft){ //It should be stored in
                    if(varType instanceof IntType)
                        this.writeInCurrentFile("istore " + index);
                    else
                        this.writeInCurrentFile("astore " + index);
                }
                else{ //It should be loaded into stack
                    if(varType instanceof IntType)
                        this.writeInCurrentFile("iload " + index);
                    else
                        this.writeInCurrentFile("aload " + index);
                }
            }
            else{ // It is a field. not a variable
                if(this.isLeft) { //It should be stored in
                    this.writeInCurrentFile("putfield " +"class_" + this.currentClass + "/" + identifier.getName() + " " + varType.getSymbol());
                }
                else { //It should be loaded into stack
                    this.writeInCurrentFile("aload 0");
                    this.writeInCurrentFile("getfield " + "class_" + this.currentClass + "/" + identifier.getName() + " " + varType.getSymbol());
                }
            }
        }catch(Exception e){}
        return null;
    }

    public Void visit(NewArray newArray) {
        newArray.getLength().accept(this);
        Type arrayType = newArray.getType();
        if(arrayType instanceof IntType)
            this.writeInCurrentFile("newarray int");
        else if(arrayType instanceof BoolType)
            this.writeInCurrentFile("newarray boolean");
        else if(arrayType instanceof StringType)
            this.writeInCurrentFile("anewarray java/lang/String");//java/lang/String   ya   java/lang/String;
        else if(arrayType instanceof UserDefinedType)
            this.writeInCurrentFile("anewarray " + ((UserDefinedType)arrayType).getClassName());
        return null;
    }

    public Void visit(NewClassInstance newClassInstance) {
        this.writeInCurrentFile("new " + "class_" + newClassInstance.getClassName().getName());
        this.writeInCurrentFile("dup");
        this.writeInCurrentFile("invokespecial " + "class_"+ newClassInstance.getClassName().getName()+"/<init>()V");
        return null;
    }

    public Void visit(FieldCall fieldCall) {
        if(this.isLeft) {
            this.isLeft = false;
            fieldCall.getInstance().accept(this);
            this.isLeft = true;
        }
        else
            fieldCall.getInstance().accept(this);
        Type instanceType = fieldCall.getInstance().accept(expressionTypeExtractor);
        String className = currentClass;
        VarSymbolTableItem fieldItem = null;
        if(fieldCall.getInstance() instanceof  Self){
            try{
                fieldItem = (VarSymbolTableItem)SymbolTable.top().get("var_" + fieldCall.getField().getName());
            }catch(Exception e1){}
        }
        else if(instanceType instanceof UserDefinedType){
            className = ((UserDefinedType)instanceType).getClassName();
            try {
                ClassSymbolTableItem classItem = (ClassSymbolTableItem) SymbolTable.root.get("class_" + className);
                SymbolTable classSymbolTable = classItem.getSymbolTable();
                fieldItem = (VarSymbolTableItem) classSymbolTable.get("var_" + fieldCall.getField().getName());
            }catch(Exception e2){}
        }
        if(this.isLeft){
            this.putFieldCommand = "putfield " + "class_" + className + "/" + fieldCall.getField().getName() + " " + fieldItem.getType().getSymbol();
            this.isLeft = false;
        }
        else {
            if(instanceType instanceof ArrayType)
                this.writeInCurrentFile("arraylength"); // darim asan in dastooro?
            else
                this.writeInCurrentFile("getfield " + "class_" + className + "/" + fieldCall.getField().getName() + " " + fieldItem.getType().getSymbol());
        }
        return null;
    }

    public Void visit(MethodCall methodCall) {
        Type instanceType = methodCall.getInstance().accept(expressionTypeExtractor);
        MethodSymbolTableItem methodItem = null;
        String className = currentClass;
        if(methodCall.getInstance() instanceof Self){
            try{
                methodItem = (MethodSymbolTableItem)SymbolTable.top().get("method_" + methodCall.getMethodName().getName());
            }catch(Exception e1){}
        }
        else if(instanceType instanceof  UserDefinedType){
            className = ((UserDefinedType)instanceType).getClassName();
            try {
                ClassSymbolTableItem classItem = (ClassSymbolTableItem) SymbolTable.root.get("class_" + className);
                SymbolTable classSymbolTable = classItem.getSymbolTable();
                methodItem = (MethodSymbolTableItem) classSymbolTable.get("method_"+methodCall.getMethodName().getName());
            }catch(Exception e2){}
        }
        //else ke nadare?
        methodCall.getInstance().accept(this);
        for(Expression arg: methodCall.getArgs())
            arg.accept(this);
        String descriptor = methodCall.getMethodName().getName() + "(";
        for(Type argType: methodItem.getArgumentsTypes())
            descriptor += argType.getSymbol();
        descriptor = descriptor + ")" + methodItem.getReturnType().getSymbol();
        this.writeInCurrentFile("invokevirtual "+ "class_" + className+"/" + descriptor);
        return null;
    }

    public Void visit(ArrayCall arrayCall) {
        if(this.isLeft) {
            this.isLeft = false;
            arrayCall.getInstance().accept(this);
            arrayCall.getIndex().accept(this);
            this.isLeft = true;
        }
        else{
            arrayCall.getInstance().accept(this);
            arrayCall.getIndex().accept(this);
        }
        Type arrayType=arrayCall.getInstance().accept(expressionTypeExtractor);
        //  System.out.println(arrayType);
        if(!this.isLeft){
            SingleType type=((ArrayType) arrayType).getSingleType();
            if(new IntType().equals(type)|| new BoolType().equals(type))
                this.writeInCurrentFile("iaload");
            else{
                this.writeInCurrentFile("aaload");
            }
        }
        return null;
    }

    // Statement
    public Void visit(PrintLine printStat) {
        this.writeInCurrentFile("getstatic java/lang/System/out Ljava/io/PrintStream;");
        Type printType=printStat.getArg().accept(expressionTypeExtractor);
        printStat.getArg().accept(this);
        if(printType instanceof StringType){
            this.writeInCurrentFile("invokevirtual  java/io/PrintStream.println(Ljava/lang/String;)V");
        }
        if(printType instanceof IntType){
            this.writeInCurrentFile( "invokevirtual java/io/PrintStream.println(I)V");
        }
        if(printType instanceof ArrayType){
            this.writeInCurrentFile("invokestatic java/util/Arrays/toString("+printType.getSymbol()+")Ljava/lang/String;");
            this.writeInCurrentFile("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V");

        }

        return null;
    }

    public Void visit(Conditional conditional) { //CHECK THIS NODE
        this.writeInCurrentFile("; start if");
        SymbolTable.pushFromQueue();
        conditional.getCondition().accept(this);
        labelNum++;
        int first=labelNum;
        this.writeInCurrentFile("ifeq "+"Label"+first);
        conditional.getThenStatement().accept(this);
        SymbolTable.pop();
        SymbolTable.pushFromQueue();
        labelNum++;
        int second=labelNum;
        this.writeInCurrentFile("goto "+"Label"+second);
        this.writeInCurrentFile("Label"+first+":");
        conditional.getElseStatement().accept(this);
        this.writeInCurrentFile("Label"+second+":");
        SymbolTable.pop();
        this.writeInCurrentFile("; end if");
        return null;
    }

    public Void visit(While whileStat) { //CHECK THIS NODE
        this.writeInCurrentFile("; while start");
        SymbolTable.pushFromQueue();
        labelNum++;
        int first=labelNum;
        writeInCurrentFile("Label"+first+":");
        whileStat.expr.accept(this);
        labelNum++;
        int second=labelNum;
        this.writeInCurrentFile("ifeq "+ "Label"+ second);
        breaks.push("Label"+second);
        continues.push("Label"+first);
        whileStat.expr.accept(this);
        this.writeInCurrentFile("goto "+"Label"+first);
        this.writeInCurrentFile("Label"+second+":");
        SymbolTable.pop();
        breaks.pop();
        continues.pop();
        this.writeInCurrentFile("; end while");
        return null;
    }

    public Void visit(Break breakStat) { //CHECK THIS NODE
        String breakLabel=breaks.pop();
        breaks.push(breakLabel);
        this.writeInCurrentFile("goto "+breakLabel);
        return null;
    }

    public Void visit(Continue continueStat) { //CHECK THIS NODE
        String continueLabel=continues.pop();
        continues.push(continueLabel);
        this.writeInCurrentFile("goto "+continueLabel);
        return null;
    }

    public Void visit(Skip skip) {
        return null;
    }

    public Void visit(LocalVarDef localVarDef) {
        SymbolTable.define();
        this.definedVars++;
        localVarDef.getInitialValue().accept(this);
        this.isLeft = true;
        localVarDef.getLocalVarName().accept(this);
        this.isLeft = false;
        return null;
    }

    public Void visit(Block block) {
        SymbolTable.pushFromQueue();
        for(Statement stat: block.body) {
            this.writeInCurrentFile("; a statement in block");
            stat.accept(this);
        }
        SymbolTable.pop();
        return null;
    }

    public Void visit(Return returnStat) {
        returnStat.getReturnedExpr().accept(this);
        Type returnType = returnStat.getReturnedExpr().accept(expressionTypeExtractor);
        if((returnType instanceof IntType) || (returnType instanceof BoolType))
            this.writeInCurrentFile("ireturn");
        else
            this.writeInCurrentFile("areturn");
        return null;
    }

    public Void visit(Assign assignStat) {
        if(assignStat.getLvalue() instanceof ArrayCall){
            this.isLeft=true;
            assignStat.getLvalue().accept(this);
            this.isLeft=false;
            assignStat.getRvalue().accept(this);
            Type rType=assignStat.getRvalue().accept(expressionTypeExtractor);
            if((rType instanceof IntType) || (rType instanceof  BoolType))
                this.writeInCurrentFile("iastore");
            else
                this.writeInCurrentFile("aastore");
        }
        else if(assignStat.getLvalue() instanceof FieldCall){
            this.isLeft = true;
            assignStat.getLvalue().accept(this);
            this.isLeft = false;
            assignStat.getRvalue().accept(this);
            this.writeInCurrentFile(this.putFieldCommand);
        }

        else{//identifier
            try {
                VarSymbolTableItem varItem = (VarSymbolTableItem) SymbolTable.top().get("var_" + ((Identifier) (assignStat.getLvalue())).getName());
                if (!varItem.mustBeUsedAfterDef())
                    this.writeInCurrentFile("aload 0");
            }
            catch (Exception e){}
            assignStat.getRvalue().accept(this);
            this.isLeft=true;
            assignStat.getLvalue().accept(this);
            this.isLeft=false;
        }
        return null;
    }

    public Void visit(IncStatement incStatement) {
        Assign converter=new Assign(incStatement.getOperand(),new Plus(incStatement.getOperand(),new IntValue(1)));
        converter.accept(this);
        return null;
    }

    public Void visit(DecStatement decStatement) {
        Assign converter=new Assign(decStatement.getOperand(),new Minus(decStatement.getOperand(),new IntValue(1)));
        converter.accept(this);
        return null;
    }

    // declarations
    public Void visit(ParameterDeclaration parameterDeclaration) {
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
        SymbolTable.reset();
        String descriptor = ".method ";
        if(methodDeclaration.getAccessModifier().toString().equals("(ACCESS_MODIFIER_PUBLIC)"))
            descriptor += "public ";
        else
            descriptor += "private ";
        descriptor += (methodDeclaration.getName().getName() + "(");
        for(ParameterDeclaration arg: methodDeclaration.getArgs())
            descriptor += arg.getType().getSymbol();
        descriptor += (")" + methodDeclaration.getReturnType().getSymbol());
        this.writeInCurrentFile(descriptor);
        this.writeInCurrentFile(".limit stack 1000");
        this.writeInCurrentFile(".limit locals 100");
        SymbolTable.pushFromQueue();
        this.definedVars = 0;
        for(Statement stat: methodDeclaration.getBody()) {
            this.writeInCurrentFile("; a new statement");
            stat.accept(this);
        }
        this.writeInCurrentFile(".end method");
        SymbolTable.pop();
        return null;
    }

    public Void visit(ClassDeclaration classDeclaration) {
        try{
            File file = new File("artifact/class_" + classDeclaration.getName().getName() + ".j");
            file.createNewFile();
            this.writer = new FileWriter(file);
            this.writeInCurrentFile(".class public " + "class_" + classDeclaration.getName().getName());
            String parentName = classDeclaration.getParentName().getName();
            if(parentName == null)
                this.writeInCurrentFile(".super java/lang/Object");
            else
                this.writeInCurrentFile(".super class_" +classDeclaration.getParentName().getName());
            SymbolTable.pushFromQueue();
            this.currentClass = classDeclaration.getName().getName();
            for(ClassMemberDeclaration cmd: classDeclaration.getClassMembers())
                if(cmd instanceof FieldDeclaration)
                    cmd.accept(this);
            for(ClassMemberDeclaration cmd: classDeclaration.getClassMembers())
                if(cmd instanceof MethodDeclaration)
                    cmd.accept(this);
            this.writeInCurrentFile(".method public <init>()V");
            this.writeInCurrentFile("aload_0");
            this.writeInCurrentFile("invokespecial java/lang/Object/<init>()V");
            this.writeInCurrentFile("return");
            this.writeInCurrentFile(".end method");
            SymbolTable.pop();
            this.writer.close();
        }catch(Exception e){}
        return null;
    }

    public Void visit(EntryClassDeclaration entryClassDeclaration) {
        try{

            File file = new File("artifact/class_" + entryClassDeclaration.getName().getName() + ".j");
            file.createNewFile();
            this.writer = new FileWriter(file);
            this.writeInCurrentFile(".class public class_" + entryClassDeclaration.getName().getName());
            String parentName = entryClassDeclaration.getParentName().getName();
            if(parentName == null)
                this.writeInCurrentFile(".super java/lang/Object");
            else
                this.writeInCurrentFile(".super class_" + entryClassDeclaration.getParentName().getName());
            SymbolTable.pushFromQueue();
            this.currentClass = entryClassDeclaration.getName().getName();
            for(ClassMemberDeclaration cmd: entryClassDeclaration.getClassMembers())
                if(cmd instanceof FieldDeclaration)
                    cmd.accept(this);
            for(ClassMemberDeclaration cmd: entryClassDeclaration.getClassMembers())
                if(cmd instanceof MethodDeclaration)
                    cmd.accept(this);
            this.writeInCurrentFile(".method public <init>()V");
            this.writeInCurrentFile("aload_0");
            this.writeInCurrentFile("invokespecial java/lang/Object/<init>()V");
            this.writeInCurrentFile("return");
            this.writeInCurrentFile(".end method");
            SymbolTable.pop();
            this.writer.close();
        }catch(Exception e){}
        return null;
    }

    public Void visit(Program program) {
        String entryClassName="";
        SymbolTable.pushFromQueue();
        for( ClassDeclaration classDeclaration : program.getClasses() ) {
            if (classDeclaration instanceof EntryClassDeclaration)
                entryClassName=classDeclaration.getName().getName();
            classDeclaration.accept(this);
        }
        try {
            File file = new File("artifact/Runner.j");
            file.createNewFile();
            this.writer = new FileWriter(file);
            this.writeInCurrentFile(".class public Runner");
            this.writeInCurrentFile(".super java/lang/Object");
            this.writeInCurrentFile(".method public <init>()V");
            this.writeInCurrentFile("aload_0");
            this.writeInCurrentFile("invokespecial java/lang/Object/<init>()V");
            this.writeInCurrentFile("return");
            this.writeInCurrentFile(".end method");
            this.writeInCurrentFile(".method public static main([Ljava/lang/String;)V");
            this.writeInCurrentFile(".limit stack 1000");
            this.writeInCurrentFile(".limit locals 100");
            this.writeInCurrentFile("new " + "class_" + entryClassName);
            this.writeInCurrentFile("dup");
            this.writeInCurrentFile("invokespecial " + "class_" + entryClassName+"/<init>()V");
            this.writeInCurrentFile("invokevirtual "+ "class_" + entryClassName+"/main()I");
            this.writeInCurrentFile("istore 1");
            this.writeInCurrentFile("return");
            this.writeInCurrentFile(".end method");
            this.writer.close();
        }
        catch(Exception e){}
        SymbolTable.pop();
        return null;
    }
}

//meghdar dehi avaliye: field hayi ke string hastan bayad meghdare "" begiran
//node hayi ke neveshte shode check shan
//Equals va not Equals hanooz nesfe an
//array type single type ezafe kardam
