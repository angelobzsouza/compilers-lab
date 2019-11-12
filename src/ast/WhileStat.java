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

	public void genJava(PW pw){
 		pw.print("while (");
        
        expr.genJava(pw);
        
        pw.println("){");
        pw.add();
        
        statList.stream().forEach((stat) -> {
            stat.genJava(pw);
        });
        
        pw.sub();
        pw.println("}");

	};
	
	private Expr expr;
	private ArrayList<Statement> statList;

}
