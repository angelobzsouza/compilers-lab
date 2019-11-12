/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;
import java.util.ArrayList;

public class IfStat extends Statement {
	
	public IfStat(Expr expr, ArrayList<Statement> ifPart, ArrayList<Statement> elsePart) {
		this.expr = expr;
		this.ifPart = ifPart;
		this.elsePart = elsePart;
	}

    public void genJava (PW pw) {
        pw.print("if (");
        
        expr.genJava(pw);
        
        pw.println("){");
        pw.add();
        
        ifPart.stream().forEach((ifStat) -> {
            ifStat.genJava(pw);
        });
        
        pw.sub();
        pw.println("}");
        
        if (!elsePart.isEmpty()) {
            pw.println("else {");
            pw.add();
        
            elsePart.stream().forEach((elseStat) -> {
                elseStat.genJava(pw);
            });
            
            pw.sub();
            pw.println("}");
        }
    }
	
	private Expr expr;
	private PW pw;
	private ArrayList<Statement> ifPart;
	private ArrayList<Statement> elsePart;
}