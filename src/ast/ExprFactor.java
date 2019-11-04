/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

public class ExprFactor extends Factor {

	public ExprFactor(Expr expr) {
		this.expr = expr;
	}
	
	public Type getType() {
		return expr.getType();
	}

	private Expr expr;
}
