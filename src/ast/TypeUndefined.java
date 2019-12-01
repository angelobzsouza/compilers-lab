/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

public class TypeUndefined extends Type {
    
   public TypeUndefined() { super("undefined"); }
   
   public String getCname() {
      return "int";
   }
   @Override
   public void genJava(PW pw){}
   
}
