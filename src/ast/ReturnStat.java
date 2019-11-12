/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;
import java.util.ArrayList;


public class ReturnStat extends Statement {
	
	public ReturnStat(Expr expr) {
		this.expr = expr;
	}

	public void genJava(PW pw){
		pw.print("return ");
        pw.sub();
        expr.genJava(pw);
        pw.add();
        pw.println(";");
	};

	private Expr expr;
}
