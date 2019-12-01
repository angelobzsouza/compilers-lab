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

	public void genJava (PW pw){

        pw.printIdent("");
        left.genJava(pw);
        
        if (right != null) {
            pw.printIdent(" = ");
            right.genJava(pw);
        }
        
        pw.printlnIdent(";");
    }

	private Expr right;
	private Expr left;
}
