/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

public class ReturnStat extends Statement {
	
	public ReturnStat(Expr expr) {
		this.expr = expr;
	}

	private Expr expr;
}
