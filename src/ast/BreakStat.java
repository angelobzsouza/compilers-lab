/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

public class BreakStat extends Statement {
    public void genJava(PW pw){
        pw.printIdent("break;");
        pw.sub();
    };
}
