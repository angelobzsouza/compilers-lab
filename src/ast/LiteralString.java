/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

public class LiteralString extends Expr {
    
    public LiteralString( String literalString ) { 
        this.literalString = literalString;
    }
  
    public Type getType() {
        return Type.stringType;
    }

    public void genJava(PW pw){};
    
    private String literalString;
}
