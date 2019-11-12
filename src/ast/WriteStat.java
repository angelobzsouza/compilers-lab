/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/

package ast;

public class WriteStat extends Statement {
	
	public WriteStat(Expr expr) {
		this.expr = expr;
	}

	public void genJava(PW pw){
		pw.print("System.out.println(");
		expr.genJava(pw);
		pw.println(");");
	};

	private Expr expr;

}