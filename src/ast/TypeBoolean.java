/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

public class TypeBoolean extends Type {

   public TypeBoolean() {
      super("boolean");
   }

   @Override
   public String getCname() {
      return "int";
   }

}
