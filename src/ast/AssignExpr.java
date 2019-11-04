/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/

package ast;

public class AssignExpr extends Statement {
	public AssignExpr(Expr left, Expr right) {
		this.right = right;
		this.left = left;
	}	
	private Expr right;
	private Expr left;
}
