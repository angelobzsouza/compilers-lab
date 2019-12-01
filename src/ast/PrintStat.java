/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/

package ast;

import java.util.ArrayList;

public class PrintStat extends Statement {
	
	public PrintStat(ArrayList<Expr> exprList) {
		this.exprList = exprList;
	}

	public void genJava(PW pw){
		pw.printIdent("System.out.println(");
		exprList.stream().forEach((expr) -> {
			expr.genJava(pw);
		});
		pw.println(");");
	};

	private ArrayList<Expr> exprList;

}