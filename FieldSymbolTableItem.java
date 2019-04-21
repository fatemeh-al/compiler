package toorla.symbolTable.symbolTableItem.varItems;

import toorla.symbolTable.symbolTableItem.*;
import toorla.types.AnonymousType;
import toorla.types.Type;

public class FieldSymbolTableItem extends SymbolTableItem {

    private Type fieldType;
    private int index;
    public FieldSymbolTableItem(String name , int index ){
        this.name=name;
        this.fieldType = new AnonymousType();
        this.index = index;
    }


    @Override
    public String getKey() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public Type getFieldType() {
        return fieldType;
    }

    public void setFieldType(Type varType) {
        this.fieldType = varType;
    }

}
