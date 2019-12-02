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

	public void genJava (PW pw) {
        pw.printIdent(type.getCname()+" ");
        for (int i = 0; i < idList.size(); i++) {
            pw.print(idList.get(i).getName()+" ");
            if (i < idList.size() - 1) {
                pw.print(", ");
            }
        }

        if (expr != null) {
            pw.print("= ");
            expr.genJava(pw);
        }

        pw.println(";");

    }
	
	private ArrayList<Variable> idList;
	private Type type;
	private Expr expr;
}