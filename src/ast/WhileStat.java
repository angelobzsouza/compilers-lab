/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;
import java.util.ArrayList;

public class WhileStat extends Statement {
	
	public WhileStat(Expr expr, ArrayList<Statement> statList) {
		this.expr = expr;
		this.statList = statList;
	}
	
	private Expr expr;
	private ArrayList<Statement> statList;

}
