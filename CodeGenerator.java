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
import toorla.types.singleType.BoolType;
import toorla.types.singleType.IntType;
import toorla.types.singleType.StringType;
import toorla.types.singleType.UserDefinedType;
import toorla.utilities.graph.Graph;
import toorla.utilities.stack.Stack;

import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Queue;

public class CodeGenerator extends Visitor<Void>{
    private ExpressionTypeExtractor expressionTypeExtractor;
    private static Stack<String> breaks = new Stack<>();
    private static Stack<String> continues = new Stack<>();
    private FileWriter writer;
    private int labelNum;
    private boolean isLeft;
    private String currentClass;

    //Every comment is important. check if all of them are done

    public CodeGenerator(Graph<String> classHierarchy){
        this.isLeft = false;
        this.labelNum = 0;
        this.expressionTypeExtractor = new ExpressionTypeExtractor(classHierarchy);
    }

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

    public Void visit(Neg negExpr) {
        negExpr.accept(this);
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
        //bipush dorost tar nist??
        return null;
    }

    public Void visit(Self self) {
        this.writeInCurrentFile("aload_0");
        return null;
    }

    public Void visit(Equals equalsExpr) {
        equalsExpr.getLhs().accept(this);
        equalsExpr.getRhs().accept(this);
        Type Lhs_t=equalsExpr.getLhs().accept(expressionTypeExtractor);
        if(Lhs_t instanceof StringType){
            this.writeInCurrentFile("invokevirtual  java/lang/String.equals:(Ljava/lang/Object;)Z");
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
            //JAVA/UTILL/ARRAY?!->nemidoonam chejoori estefade mishe:(
        }
        //moghayaseye 2 ta userdefinded type?shayad aslan method equal tarif nashode bashe barash
        return null;
    }

    public Void visit(GreaterThan gtExpr) {
        gtExpr.getLhs().accept(this);
        gtExpr.getRhs().accept(this);
        int first=this.labelNum++;
        this.writeInCurrentFile("if_icmple"+"Label"+first );
        this.writeInCurrentFile("ldc 1");
        int second=this.labelNum++;
        this.writeInCurrentFile("goto "+"Label"+second);
        this.writeInCurrentFile("Label"+first+":");
        this.writeInCurrentFile("ldc 0");
        this.writeInCurrentFile("Label"+second+":");
        return null;
    }

    public Void visit(LessThan lessThanExpr) {
        lessThanExpr.getLhs().accept(this);
        lessThanExpr.getRhs().accept(this);
        int first=this.labelNum++;
        this.writeInCurrentFile("if_icmpgt"+"Label"+first );
        this.writeInCurrentFile("ldc 1");
        int second=this.labelNum++;
        this.writeInCurrentFile("goto "+"Label"+second);
        this.writeInCurrentFile("Label"+first+":");
        this.writeInCurrentFile("ldc 0");
        this.writeInCurrentFile("Label"+second+":");
        return null;
    }

    public Void visit(Not notExpr) { 
        notExpr.accept(this);
        int first=this.labelNum++;
        this.writeInCurrentFile("ifeq " + "Label"+first);
        this.writeInCurrentFile("ldc 0");
        int second=this.labelNum++;
        this.writeInCurrentFile("goto Label"+second);
        this.writeInCurrentFile("Label+"+first+":");
        this.writeInCurrentFile("ldc 1");
        this.writeInCurrentFile("Label"+second+":");
        return null;
    }

    public Void visit(NotEquals notEquals) {
        notEquals.getLhs().accept(this);
        notEquals.getRhs().accept(this);
        Type Lhs_t=notEquals.getLhs().accept(expressionTypeExtractor);
        if(Lhs_t instanceof StringType){
            this.writeInCurrentFile("invokevirtual  java/lang/String.equals:(Ljava/lang/Object;)Z");
        }
        if((Lhs_t instanceof IntType) || (Lhs_t instanceof BoolType)){
            labelNum++;
            int first=labelNum;
            this.writeInCurrentFile("if_icmpneq "+"Label"+first);
            this.writeInCurrentFile("ldc 0");
            labelNum++;
            int second=labelNum;
            this.writeInCurrentFile("goto "+"Label"+second);
            this.writeInCurrentFile("Label"+first+":");
            this.writeInCurrentFile("ldc 1");
            this.writeInCurrentFile("Label"+second+":");
        }
        if(Lhs_t instanceof ArrayType){
            //JAVA/UTILL/ARRAY?!->nemidoonam chejoori estefade mishe:(
        }
        //moghayaseye 2 ta userdefind type?shayad aslan method equal tarif nashode bashe barash?!
        return null;
    }

    public Void visit(Identifier identifier) {
        try{
            VarSymbolTableItem varItem = (VarSymbolTableItem)SymbolTable.top().get("var_" + identifier.getName());
            Type varType = varItem.getType();
            if(varItem.mustBeUsedAfterDef()){
                //It is a variable. not a field
                int index = varItem.getDefinitionNumber();
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
                    this.writeInCurrentFile("putfield " + this.currentClass + "/" + identifier.getName() + " " + varType.getSymbol());
                }
                else { //It should be loaded into stack
                    this.writeInCurrentFile("aload 0");
                    this.writeInCurrentFile("getfield " + this.currentClass + "/" + identifier.getName() + " " + varType.getSymbol());
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
            this.writeInCurrentFile("anewarray " + arrayType.getSymbol());
        return null;
    }

    public Void visit(NewClassInstance newClassInstance) {
        this.writeInCurrentFile("new " + newClassInstance.getClassName().getName());
        this.writeInCurrentFile("dup");
        this.writeInCurrentFile("invokespecial " + newClassInstance.getClassName().getName()+"/<init>()V");
        return null;
    }

    public Void visit(FieldCall fieldCall) {
        fieldCall.getInstance().accept(this);
        if(!isLeft){
            fieldCall.getField().accept(this);
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
            }catch(Exception e){}
        }
        else{
            className = instanceType.getSymbol();
            try {
                //Inja ro check kon ke esme classo dorost begire va betoone SymbolTablesh ro biyare
                ClassSymbolTableItem classItem = (ClassSymbolTableItem) SymbolTable.root.get(className);
                SymbolTable classSymbolTable = classItem.getSymbolTable();
                methodItem = (MethodSymbolTableItem) classSymbolTable.get("method_"+methodCall.getMethodName().getName());
            }catch(Exception e2){}
        }
        methodCall.getInstance().accept(this);
        for(Expression arg: methodCall.getArgs())
            arg.accept(this);
        String descriptor = methodCall.getMethodName().getName() + "(";
        for(Type argType: methodItem.getArgumentsTypes())
            descriptor += argType.getSymbol();
        descriptor = descriptor + ")" + methodItem.getReturnType().getSymbol();
        this.writeInCurrentFile("invokevirtual "+className+"/" + descriptor);
        return null;
    }

    public Void visit(ArrayCall arrayCall) {
        arrayCall.getInstance().accept(this);
        arrayCall.getIndex().accept(this);
        Type arrayType=arrayCall.getInstance().accept(expressionTypeExtractor);
        if(!isLeft){
            if((arrayType instanceof IntType) ||(arrayType instanceof BoolType))
                this.writeInCurrentFile("iaload");
            else
                this.writeInCurrentFile("aaload");
        }

        return null;
    }

    // Statement
    public Void visit(PrintLine printStat) {
        this.writeInCurrentFile("getstatic java/lang/System/out Ljava/io/PrintStream;");
        Type printType=printStat.getArg().accept(expressionTypeExtractor);
        printStat.getArg().accept(this);
        if(printType instanceof StringType){
            this.writeInCurrentFile("invokevirtual  java/io/PrintStream.println:(Ljava/lang/String;)V");
        }
        if(printType instanceof IntType){
            this.writeInCurrentFile( "invokevirtual java/io/PrintStream.println:(I)V)");
        }
        if(printType instanceof ArrayType){
            //java.util.arrays?
        }
        return null;
    }

    public Void visit(Conditional conditional) { 
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
        this.writeInCurrentFile("ifeq "+second);
        conditional.getElseStatement().accept(this);
        this.writeInCurrentFile("Label"+second+":");
        SymbolTable.pop();
        return null;
    }

    public Void visit(While whileStat) { 
        SymbolTable.pushFromQueue();
        labelNum++;
        int first=labelNum;
        writeInCurrentFile("Label"+first+":");
        whileStat.expr.accept(this);
        labelNum++;
        int second=labelNum;
        this.writeInCurrentFile("ifeq "+second);
        breaks.push("Label"+Integer.toString(second));
        continues.push("Label"+Integer.toString(first));
        whileStat.expr.accept(this);
        this.writeInCurrentFile("goto "+"Label"+first);
        this.writeInCurrentFile("Label"+second+":");
        SymbolTable.pop();
        //fek konam bayad az stack break va continue pop beshe
        return null;
    }

    public Void visit(Break breakStat) {
        String breakLabel=breaks.pop();
        this.writeInCurrentFile("goto "+breakLabel);
        return null;
    }

    public Void visit(Continue continueStat) {
        String continueLabel=continues.pop();
        this.writeInCurrentFile("goto "+continueLabel);
        return null;
    }

    public Void visit(Skip skip) {
        return null;
    }

    public Void visit(LocalVarDef localVarDef) {
        localVarDef.getInitialValue().accept(this);
        this.isLeft = true;
        localVarDef.getLocalVarName().accept(this);
        this.isLeft = false;
        return null;
    }

    public Void visit(Block block) {
        SymbolTable.pushFromQueue();
        for(Statement stat: block.body)
            stat.accept(this);
        SymbolTable.pop();
        return null;
    }

    public Void visit(Return returnStat) {
        returnStat.getReturnedExpr().accept(this);
        Type returnType = returnStat.accept(expressionTypeExtractor);
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
            this.isLeft=true;
            assignStat.getLvalue().accept(this);
            this.isLeft=false;
            assignStat.getRvalue().accept(this);
            this.isLeft=true;
            ((FieldCall) assignStat.getLvalue()).getField().accept(this);
            this.isLeft=false;
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
        //Method <init> bayad ezafe konam??? ---> ARE. YADET NARE
        try{
            this.writer = new FileWriter("artifact/class_" + classDeclaration.getName().getName() + ".j");
            this.writeInCurrentFile(".class public " + "class_" + classDeclaration.getName().getName());
            if(classDeclaration.getParentName().getName().equals("Any"))
                this.writeInCurrentFile(".super java/lang/Object");
            else
                this.writeInCurrentFile(".super class_" +classDeclaration.getParentName().getName());
            SymbolTable.pushFromQueue();
            this.currentClass = classDeclaration.getName().getName();
            for(ClassMemberDeclaration cmd: classDeclaration.getClassMembers())
                cmd.accept(this);
            SymbolTable.pop();
            this.writer.close();
        }catch(Exception e){}
        return null;
    }

    public Void visit(EntryClassDeclaration entryClassDeclaration) {
        //Method <init> bayad ezafe konam???  ---> ARE. YADET NARE
        try{
            this.writer = new FileWriter("artifact/class_" + entryClassDeclaration.getName().getName() + ".j");
            this.writeInCurrentFile(".class public class_" + entryClassDeclaration.getName().getName());
            if(entryClassDeclaration.getParentName().getName().equals("Any"))
                this.writeInCurrentFile(".super java/lang/Object");
            else
                this.writeInCurrentFile(".super class_" + entryClassDeclaration.getParentName().getName());
            SymbolTable.pushFromQueue();
            this.currentClass = entryClassDeclaration.getName().getName();
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

//be nazaram assign ghesmate fieldCall ghalate
//khode fieldCall ham ehtemalan ghalate
//classe runner ro bayad ezafe konim
//method init ro be har class bayad ezafe konim
//equals va not equals va print ghesmate array bayad check she
//tak take comment ha check shan anjam dade bashim




