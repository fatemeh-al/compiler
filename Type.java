package toorla.types;

abstract public class Type {
    protected boolean isLvalue;
    public Type() { this.isLvalue = false; }
    public Void setLvalue() {
        this.isLvalue = true;
        return null;
    }
    public boolean getLvalue() { return this.isLvalue; }
    public abstract String toString();
}
