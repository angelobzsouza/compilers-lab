/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

public class AssertStat extends Statement {

	public AssertStat(Expr expr, String string) {
		this.expr = expr;
		this.string = string;
	}
	public void genJava(PW pw){};
	
	private Expr expr;
	private String string;
	
}
