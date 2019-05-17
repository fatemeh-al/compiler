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
import toorla.utilities.graph.Graph;
import toorla.utilities.graph.GraphDoesNotContainNodeException;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TypeChecker implements Visitor<Type> {

    private boolean inLoop;
    private int numOfErrors;
    private Graph<String> inheritanceGraph;
    private String currentClass;

    public TypeChecker(Graph<String> graph){
        this.inLoop = false;
        this.numOfErrors = 0;
        this.inheritanceGraph = graph;
    }

    public boolean loopChecker(Graph<String> inheritance_Graph,String child){
        Set<String> visited=new HashSet<>();
        int i=0;
        try {
            String newChild=child;
            while(!newChild.equals("Any") && !(visited.contains(newChild))){
                visited.add(newChild);
                newChild=inheritanceGraph.getParentsOfNode(newChild).iterator().next();
            }
            if(newChild.equals(child))
                return true;
            else if(child.equals("Any"))
                return false;
            else
                return false;
        }catch (GraphDoesNotContainNodeException e){}
        return false;

    }
    public boolean parentChecker(Graph<String> inheritance_Graph,String child,String parent,Set<String> visited){
        try{
            if(inheritanceGraph.getParentsOfNode(child).contains(parent)) {
                return true;
            }
            else {
                while(!child.equals("Any") && !visited.contains(child)) {
                    try {
                        visited.add(child);
                        Collection<String> parents=inheritanceGraph.getParentsOfNode(child);
                        return parentChecker(inheritanceGraph,parents.iterator().next(),parent,visited);
                    } catch (GraphDoesNotContainNodeException e) {}
                }
            }
        }catch (GraphDoesNotContainNodeException e){}
        return false;
    }

    public boolean subTypeChecker(Type child, Type parent) {
        if(child.toString().equals("(UndefinedType)") || parent.toString().equals("(UndefinedType)"))
            return true;
        if(child.toString().equals(parent.toString()))
            return true;
        if(child.toString().startsWith("(UserDefined") && parent.toString().startsWith("(UserDefined")){
            String childName=((UserDefinedType)child).getClassDeclaration().getName().getName();
            String parentName=((UserDefinedType)parent).getClassDeclaration().getName().getName();
            Set<String> empty=new HashSet<>();
            if(parentChecker(inheritanceGraph,childName,parentName,empty))
                return true;
            else
                return false;
        }
        return false;
    }

    @Override
    public Type visit(Program program) {
        for(ClassDeclaration cd: program.getClasses()){
            if (loopChecker(inheritanceGraph,cd.getName().getName())) {
                this.numOfErrors++;
                System.out.println("Error:" + cd.getName().line + ":class " + cd.getName().getName() + " has inheritance loop;");
            }
            cd.accept(this);
        }
        if(this.numOfErrors == 0)
            System.out.println("No error detected;");
        return null;
    }

    @Override
    public Type visit(PrintLine printLine) {
        Type argType = printLine.getArg().accept(this);
        if(!(argType.toString().equals("(IntType)") || argType.toString().equals("(StringType)")
                || argType.toString().equals("(ArrayType,(IntType))") || argType.toString().equals("(UndefinedType)"))) {
            System.out.println("Error:Line:" + printLine.line + ":Type of parameter of print built-in function must be integer , string or array of integer;");
            this.numOfErrors++;
        }
        return null;
    }

    @Override
    public Type visit(Assign assign) {
        Type lhs = assign.getLvalue().accept(this);
        if(!lhs.getLvalue()) {
            System.out.println("Error:Line:" + assign.line + ":Left hand side expression is not assignable;");
            this.numOfErrors++;
        }
        Type rhs = assign.getRvalue().accept(this);
        if(!subTypeChecker(rhs, lhs)) {
            System.out.println("Error:Line:" + assign.line + ":Right hand side is not subType of left hand side;");
            //NOT SURE ABOUT THE ERROR MESSAGE
            this.numOfErrors++;
        }
        return null;
    }

    @Override
    public Type visit(Block block) {
        SymbolTable.push(new SymbolTable(SymbolTable.top()));
        for(int i = 0; i < block.body.size(); i++)
            block.body.get(i).accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Type visit(Conditional conditional) {
        Type condType = conditional.getCondition().accept(this);
        if(!(condType.toString().equals("(BoolType)") || condType.toString().equals("(UndefinedType)"))) {
            System.out.println("Error:Line:" + conditional.getCondition().line + ":Condition type must be bool in Conditional statements;");
            this.numOfErrors++;
        }
        SymbolTable.push(new SymbolTable(SymbolTable.top()));
        conditional.getThenStatement().accept(this);
        SymbolTable.pop();
        SymbolTable.push(new SymbolTable(SymbolTable.top()));
        conditional.getElseStatement().accept(this);
        SymbolTable.pop();
        return null;
    }

    @Override
    public Type visit(While whileStat) {
        this.inLoop = true;
        Type condType = whileStat.expr.accept(this);
        if(!(condType.toString().equals("(BoolType)") || condType.toString().equals("(UndefinedType)"))) {
            System.out.println("Error:Line:" + whileStat.expr.line + ":Condition type must be bool in Loop statements;");
            this.numOfErrors++;
        }
        SymbolTable.push(new SymbolTable(SymbolTable.top()));
        whileStat.body.accept(this);
        SymbolTable.pop();
        this.inLoop = false;
        return null;
    }

    @Override
    public Type visit(Break breakStat) {
        if(!this.inLoop) {
            System.out.println("Error:Line:" + breakStat.line + ":Invalid use of Break, Break must be used as loop statement;");
            this.numOfErrors++;
        }
        return null;
    }

    @Override
    public Type visit(Continue continueStat) {
        if(!this.inLoop) {
            System.out.println("Error:Line:" + continueStat.line + ":Invalid use of Continue, Continue must be used as loop statement;");
            this.numOfErrors++;
        }
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
            VarSymbolTableItem retItem = (VarSymbolTableItem)SymbolTable.top().get("var_$ret");
            Type methodRetType = retItem.getVarType();
            String returnString="";
            if(methodRetType.toString().equals("(IntType)"))
                returnString="int";
            else if(methodRetType.toString().equals("(boolType)"))
                returnString="boolean";
            else if(methodRetType.toString().equals("(StringType)"))
                returnString="string";
            else if(methodRetType.toString().startsWith("(ArrayType,")) {
                if(methodRetType.toString().contains("(IntType)"))
                    returnString="array of int";
                if(methodRetType.toString().contains("(boolType)"))
                    returnString="array of boolean";
                if(methodRetType.toString().contains("(StringType)"))
                    returnString="array of string";
                if(methodRetType.toString().contains("UserDefined"))
                    returnString="array of " + methodRetType.toString().substring(24,methodRetType.toString().indexOf(")"));
            }
            else if(methodRetType.toString().startsWith("(UserDefined,"))
                returnString=methodRetType.toString().substring(13,methodRetType.toString().indexOf(")"));
            else
                returnString="";
            if(!subTypeChecker(retType, methodRetType)) {
                System.out.println("Error:Line:" + returnStat.line + ":Expression returned by this method must be " + returnString +";");
                this.numOfErrors++;
            }
        }catch(ItemNotFoundException e) {
        }
        return null;
    }

    @Override
    public Type visit(Plus plusExpr) {
        Type lhs = plusExpr.getLhs().accept(this);
        Type rhs = plusExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return new IntType();
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + plusExpr.line + ":Unsupported operand types for " + plusExpr.toString() + ";");
            this.numOfErrors++;
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Minus minusExpr) {
        Type lhs = minusExpr.getLhs().accept(this);
        Type rhs = minusExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return new IntType();
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + minusExpr.line + ":Unsupported operand types for " + minusExpr.toString() + ";");
            this.numOfErrors++;
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Times timesExpr) {
        Type lhs = timesExpr.getLhs().accept(this);
        Type rhs = timesExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return new IntType();
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + timesExpr.line + ":Unsupported operand types for " + timesExpr.toString() + ";");
            this.numOfErrors++;
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Division divisionExpr) {
        Type lhs = divisionExpr.getLhs().accept(this);
        Type rhs = divisionExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return new IntType();
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + divisionExpr.line + ":Unsupported operand types for " + divisionExpr.toString() + ";");
            this.numOfErrors++;
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Modulo moduloExpr){
        Type lhs = moduloExpr.getLhs().accept(this);
        Type rhs = moduloExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return new IntType();
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + moduloExpr.line + ":Unsupported operand types for " + moduloExpr.toString() + ";");
            this.numOfErrors++;
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(GreaterThan gtExpr) {
        Type lhs = gtExpr.getLhs().accept(this);
        Type rhs = gtExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return new BoolType();
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + gtExpr.line + ":Unsupported operand types for " + gtExpr.toString() + ";");
            this.numOfErrors++;
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(LessThan ltExpr) {
        Type lhs = ltExpr.getLhs().accept(this);
        Type rhs = ltExpr.getRhs().accept(this);
        if(lhs.toString().equals("(IntType)") && rhs.toString().equals("(IntType)"))
            return new BoolType();
        else if((lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(IntType)"))
                || (lhs.toString().equals("(IntType)") && rhs.toString().equals("(UndefinedType)"))
                || (lhs.toString().equals("(UndefinedType)") && rhs.toString().equals("(UndefinedType)")))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + ltExpr.line + ":Unsupported operand types for " + ltExpr.toString() + ";");
            this.numOfErrors++;
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Neg negExpr) {
        Type expr = negExpr.getExpr().accept(this);
        if(expr.toString().equals("(IntType)"))
            return new IntType();
        else if(expr.toString().equals("(UndefinedType)"))
            return expr;
        else {
            System.out.println("Error:Line:" + negExpr.line + ":Unsupported operand types for " + negExpr.toString() + ";");
            this.numOfErrors++;
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
            System.out.println("Error:Line:" + andExpr.line + ":Unsupported operand types for " + andExpr.toString() + ";");
            this.numOfErrors++;
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
            System.out.println("Error:Line:" + orExpr.line + ":Unsupported operand types for " + orExpr.toString() + ";");
            this.numOfErrors++;
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Not notExpr) {
        Type expr = notExpr.getExpr().accept(this);
        if(expr.toString().equals("(BoolType)"))
            return new BoolType();
        else if(expr.toString().equals("(UndefinedType)"))
            return expr;
        else {
            System.out.println("Error:Line:" + notExpr.line + ":Unsupported operand types for " + notExpr.toString() + ";");
            this.numOfErrors++;
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(Equals equalsExpr) {
        Type lhs = equalsExpr.getLhs().accept(this);
        Type rhs = equalsExpr.getRhs().accept(this);
        if(lhs.toString().equals(rhs.toString()))
            return new BoolType();
        else if(lhs.toString().equals("(UndefinedType)") || rhs.toString().equals("(UndefinedType)"))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + equalsExpr.line + ":Unsupported operand types for " + equalsExpr.toString() + ";");
            this.numOfErrors++;
            return new UndefinedType();
        }
    }

    @Override
    public Type visit(NotEquals notEquals) {
        Type lhs = notEquals.getLhs().accept(this);
        Type rhs = notEquals.getRhs().accept(this);
        if(lhs.toString().equals(rhs.toString()))
            return new BoolType();
        else if(lhs.toString().equals("(UndefinedType)") || rhs.toString().equals("(UndefinedType)"))
            return new UndefinedType();
        else {
            System.out.println("Error:Line:" + notEquals.line + ":Unsupported operand types for " + notEquals.toString() + ";");
            this.numOfErrors++;
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
            else {
                System.out.println("Error:Line:" + incStatement.line + ":Operand of Inc must be a valid lvalue;");
                this.numOfErrors++;
            }
        }
        else{//Type is wrong
            this.numOfErrors++;
            System.out.println("Error:Line:" + incStatement.line + ":Unsupported operand types for " + incStatement.toString() + ";");
            if(!operand.getLvalue())
                System.out.println("Error:Line:" + incStatement.line + ":Operand of Inc must be a valid lvalue;");
        }
        return new UndefinedType();
    }

    @Override
    public Type visit(DecStatement decStatement) {
        Type operand = decStatement.getOperand().accept(this);
        if((operand.toString().equals("(IntType)") || operand.toString().equals("(UndefinedType)"))){//Type is Correct
            if(operand.getLvalue())
                return operand;
            else {
                System.out.println("Error:Line:" + decStatement.line + ":Operand of Dec must be a valid lvalue;");
                this.numOfErrors++;
            }
        }
        else{//Type is wrong
            this.numOfErrors++;
            System.out.println("Error:Line:" + decStatement.line + ":Unsupported operand types for " + decStatement.toString() + ";");
            if(!operand.getLvalue())
                System.out.println("Error:Line:" + decStatement.line + ":Operand of Dec must be a valid lvalue;");
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
        initialType.setLvalue();
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
        if(!(lengthType.toString().equals("(IntType)") || lengthType.toString().equals("(UndefinedType)"))) {
            System.out.println("Error:Line:" + newArray.line + ":Size of an array must be of type integer;");
            this.numOfErrors++;
        }
        return new ArrayType(arrayType);
    }

    @Override
    public Type visit(ArrayCall arrayCall) {
        Type instanceType = arrayCall.getInstance().accept(this);
        Type arraySingle = new UndefinedType();
        if(instanceType.toString().startsWith("(ArrayType"))
            arraySingle = ((ArrayType)instanceType).getSingleType();
        else if(!instanceType.toString().equals("(UndefinedType)")) {
            System.out.println("Error:Line:" + arrayCall.line + ":Instance of array call is not an array;");
            this.numOfErrors++;
        }
        Type indexType = arrayCall.getIndex().accept(this);
        if(!indexType.toString().equals("(IntType)") && !indexType.toString().equals("(UndefinedType)")) {
            System.out.println("Error:Line:" + arrayCall.line + ":Array index must be of type integer;");
            this.numOfErrors++;
        }
        arraySingle.setLvalue();
        return arraySingle;
    }

    @Override
    public Type visit(ParameterDeclaration parameterDeclaration) {
        Type parameterType = parameterDeclaration.getType();
        parameterType.setLvalue();
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
            Type identifierType = item.getVarType();
            identifierType.setLvalue();
            return identifierType;
        }catch(ItemNotFoundException e){
            System.out.println("Error:Line:" + identifier.line + ":Variable " + identifier.getName() + " is not declared yet is this Scope;");
            this.numOfErrors++;
            Type undefinedIdentifier = new UndefinedType();
            undefinedIdentifier.setLvalue();
            return undefinedIdentifier;
        }
    }

    @Override
    public Type visit(Self self) {
        return new UserDefinedType(new ClassDeclaration(new Identifier(this.currentClass)));
    }

    @Override
    public Type visit(NewClassInstance newClassInstance) {
        String className = newClassInstance.getClassName().getName();
        try {
            SymbolTableItem item = SymbolTable.root.get("class_" + className);
            Type classType = new UserDefinedType(new ClassDeclaration(newClassInstance.getClassName()));
            return classType;
        }catch(ItemNotFoundException e){
            System.out.println("Error:Line:" + newClassInstance.line + ":There is no class with name " + className + ";");
            this.numOfErrors++;
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
                        System.out.println("Error:Line:" + methodCall.line + ":There is no Method with name " + methodName + " with such parameters in class " + className + ";");
                        this.numOfErrors++;
                        return new UndefinedType();
                    }
                    for(int i = 0; i < args.size(); i++){
                        Type singleArgType = args.get(i).accept(this);
                        if(!subTypeChecker(singleArgType, argTypes.get(i))){
                            System.out.println("Error:Line:" + methodCall.line + ":There is no Method with name " + methodName + " with such parameters in class " + className + ";");
                            this.numOfErrors++;
                            return new UndefinedType();
                        }
                    }
                    AccessModifier access = methodItem.getAccessModifier();
                    if(access.toString().equals("(ACCESS_MODIFIER_PRIVATE)") && !methodCall.getInstance().toString().equals("(Self)"))
                    {
                        System.out.println("Error:Line:" + methodCall.line + ":Illegal access to Method " + methodName + " of an object of Class " + className + ";");
                        this.numOfErrors++;
                        return new UndefinedType();
                    }
                    return methodItem.getReturnType();
                }catch(ItemNotFoundException e2){
                    System.out.println("Error:Line:" + methodCall.line + ":There is no Method with name " + methodName +" with such parameters in class " + className + ";");
                    this.numOfErrors++;
                }
            }catch(ItemNotFoundException e1){
                System.out.println("Error:Line:" + methodCall.line + ":There is no class with name " + className + ";");
                this.numOfErrors++;
            }
        }
        else if(!instanceType.toString().equals("(UndefinedType)"))
            System.out.println("Error:Line:" + methodCall.line + ":Unsupported operand types for " + methodCall.toString() + ";");
        return new UndefinedType();
    }

    @Override
    public Type visit(FieldCall fieldCall) {
        Type instanceType = fieldCall.getInstance().accept(this);
        String fieldName = fieldCall.getField().getName();
        Type returnUndefined = new UndefinedType();
        returnUndefined.setLvalue();
        if(instanceType.toString().startsWith("(UserDefined")){
            String className = ((UserDefinedType)instanceType).getClassDeclaration().getName().getName();
            try{
                ClassSymbolTableItem classItem = (ClassSymbolTableItem) SymbolTable.root.get("class_" + className);
                SymbolTable classSymbolTable = classItem.getSymbolTable();
                try{
                    FieldSymbolTableItem fieldItem  = (FieldSymbolTableItem)classSymbolTable.get("var_" + fieldName);
                    AccessModifier access = fieldItem.getAccessModifier();
                    if(access.toString().equals("(ACCESS_MODIFIER_PRIVATE)") && !fieldCall.getInstance().toString().equals("(Self)")){
                        System.out.println("Error:Line:" + fieldCall.line + ":Illegal access to Field " + fieldName + " of an object of Class " + className + ";");
                        this.numOfErrors++;
                        return returnUndefined;
                    }
                    Type fieldType = fieldItem.getVarType();
                    fieldType.setLvalue();
                    return fieldType;
                }catch(ItemNotFoundException e2){
                    System.out.println("Error:Line:" + fieldCall.line + ":There is no Field with name " + fieldName + " with in class " + className + ";");
                    this.numOfErrors++;
                }
            }catch(ItemNotFoundException e1){
                System.out.println("Error:Line:" + fieldCall.line + ":There is no class with name " + className + ";");
                this.numOfErrors++;
            }
        }
        else if(instanceType.toString().startsWith("(ArrayType")){
            if(!fieldName.equals("length")){
                System.out.println("Error:Line:" + fieldCall.line + ":Not able to call a field on arrays except length;");
                this.numOfErrors++;
                return new UndefinedType();
            }
            return new IntType();
        }
        else if(!instanceType.toString().equals("(UndefinedType)"))
            System.out.println("Error:Line:" + fieldCall.line + ":Unsupported operand types for " + fieldCall.toString() + ";");
        return returnUndefined;
    }

    @Override
    public Type visit(MethodDeclaration methodDeclaration) {
        if(methodDeclaration.getName().getName().equals("main") && (!methodDeclaration.getReturnType().toString().equals("(IntType)")
                || methodDeclaration.getArgs().size() != 0
                || !methodDeclaration.getAccessModifier().toString().equals("(ACCESS_MODIFIER_PUBLIC)"))) {
            System.out.println("Error:Line:" + methodDeclaration.getName().line + ":Main method definition is not valid;");
            this.numOfErrors++;
        }
        SymbolTable.push(new SymbolTable(SymbolTable.top()));
        //Set return type
        VarSymbolTableItem returnItem = new VarSymbolTableItem();
        Type returnType = methodDeclaration.getReturnType();
        if(returnType.toString().startsWith("(UserDefined")) {
            String retClassName = returnType.toString().substring(13, returnType.toString().indexOf(")"));
            try{
                SymbolTableItem retClassItem = SymbolTable.root.get("class_" + retClassName);
            }catch(ItemNotFoundException e1){
                System.out.println("Error:Line:" + methodDeclaration.getName().line + ":There is no class with name " + retClassName + ";");
                this.numOfErrors++;
                returnType = new UndefinedType();
            }
        }
        returnItem.setVarType(returnType);
        returnItem.setName("$ret");
        try{
            SymbolTable.top().put(returnItem);
        }catch(ItemAlreadyExistsException e2){
        }
        //Add parameters to Symbol Table
        for(ParameterDeclaration arg: methodDeclaration.getArgs()){
            VarSymbolTableItem argItem = new VarSymbolTableItem();
            argItem.setVarType(arg.getType());
            if(arg.getType().toString().startsWith("(UserDefined")) {
                String argCLassName = arg.getType().toString().substring(13, arg.getType().toString().indexOf(")"));
                try{
                    SymbolTableItem argClassItem = SymbolTable.root.get("class_" + argCLassName);
                }catch(ItemNotFoundException e4){
                    System.out.println("Error:Line:" + methodDeclaration.getName().line + ":There is no class with name " + argCLassName + ";");
                    this.numOfErrors++;
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
            String parentName = classDeclaration.getParentName().getName();
            if(parentName != null) {
                try {
                    SymbolTableItem parentItem = SymbolTable.root.get("class_" + parentName);
                } catch (ItemNotFoundException e1) {
                    this.numOfErrors++;
                    System.out.println("Error:Line:" + classDeclaration.getName().line + ":There is no class with name " + parentName + ";");
                    try{
                        ClassSymbolTableItem anyItem = (ClassSymbolTableItem) SymbolTable.root.get("class_Any");
                        classSym.setPreSymbolTable(anyItem.getSymbolTable());
                    }catch (ItemNotFoundException e4){
                    }
                }
            }
            SymbolTable.push(classSym);
            this.currentClass = classDeclaration.getName().getName();
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
            String parentName = entryClassDeclaration.getParentName().getName();
            if(parentName != null) {
                try {
                    SymbolTableItem parentItem = SymbolTable.root.get("class_" + parentName);
                } catch (ItemNotFoundException e1) {
                    this.numOfErrors++;
                    System.out.println("Error:Line:" + entryClassDeclaration.getName().line + ":There is no class with name " + parentName + ";");
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
            }catch(ItemNotFoundException e3){
                this.numOfErrors++;
                System.out.println("Error:Line:" + entryClassDeclaration.getName().line + ":No method main declared in entry class;");
            }
            this.currentClass = entryClassDeclaration.getName().getName();
            for(ClassMemberDeclaration member: entryClassDeclaration.getClassMembers())
                member.accept(this);
        }catch(ItemNotFoundException e2){
        }
        return null;
    }
}



