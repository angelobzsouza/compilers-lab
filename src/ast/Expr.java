/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

abstract public class Expr extends Statement {
    abstract public Type getType();
    abstract public void genJava(PW pw);
}