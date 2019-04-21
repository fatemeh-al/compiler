package toorla.symbolTable.symbolTableItem;

import java.util.ArrayList;

import toorla.ast.declaration.classDecs.classMembersDecs.AccessModifier;
import toorla.types.Type;
import toorla.symbolTable.*;

public class MethodSymbolTableItem extends SymbolTableItem {

    private Type returnType;
    private AccessModifier accessModifier;
    private ArrayList<Type> argTypes = new ArrayList<>();
    private SymbolTable methodSymbolTable;

    public MethodSymbolTableItem(String name, ArrayList<Type> argTypes, Type returnType)
    {
        this.name = name;
        this.argTypes = argTypes;
        this.returnType = returnType;
    }

    public void setAccessModifier(AccessModifier accessModifier)
    {
        this.accessModifier = accessModifier;
    }

    public AccessModifier getAccessModifier()
    {
        return this.accessModifier;
    }

    public void setMethodSymbolTable( SymbolTable symTable )
    {
        this.methodSymbolTable = symTable;
    }

    public SymbolTable getMethodSymbolTable()
    {
        return this.methodSymbolTable;
    }

    @Override
    public String getKey()
    {
        return name;
    } //"Method_" + name

}
