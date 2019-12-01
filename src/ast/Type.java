/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

abstract public class Type {

    public Type( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    abstract public String getCname();

    abstract  void genJava(PW pw);

    public static Type booleanType = new TypeBoolean();
    public static Type intType = new TypeInt();
    public static Type stringType = new TypeString();
    public static Type undefinedType = new TypeUndefined();
    public static Type nilType = new TypeNil();
    private String name;
}
