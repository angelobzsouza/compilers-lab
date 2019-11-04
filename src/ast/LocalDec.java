/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;
import lexer.Token;
import java.util.ArrayList;

public class LocalDec extends Statement {
	
	public LocalDec(Type type, ArrayList<Variable> idList, Expr e) {
		this.type = type;
		this.idList = idList;
		this.expr = e;	
	}
	
	private ArrayList<Variable> idList;
	private Type type;
	private Expr expr;
}
