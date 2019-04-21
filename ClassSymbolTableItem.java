package toorla.symbolTable.symbolTableItem;

import toorla.symbolTable.*;
import toorla.ast.declaration.classDecs.*;

public class ClassSymbolTableItem extends SymbolTableItem {

    private SymbolTable classSym;
    private ClassDeclaration classDeclaration;

    public ClassSymbolTableItem( ClassDeclaration classDeclaration )
    {
        this.name = classDeclaration.getName().getName();
        this.classDeclaration = classDeclaration;
    }

    @Override

    public String getKey() { return name; } //"Class_" + name

    public void setClassSymbolTable( SymbolTable classSym )
    {
        this.classSym = classSym;
    }

    public SymbolTable getClassSymbolTable()
    {
        return classSym;
    }

    public void setClassDeclaration( ClassDeclaration classDeclaration )
    {
        this.classDeclaration = classDeclaration;
    }

    public ClassDeclaration getClassDeclaration()
    {
        return classDeclaration;
    }

}
