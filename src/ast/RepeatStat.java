/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;
import java.util.ArrayList;

public class RepeatStat extends Statement {
	
	public RepeatStat(ArrayList<Statement> statList, Expr e) {
		this.statList = statList;
		this.e = e;
	}
	
	private ArrayList<Statement> statList;
	private Expr e;
	
}
