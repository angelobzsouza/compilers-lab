/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/

package ast;

public class NilExpr extends Factor {
	
	public NilExpr() {
		this.type = new TypeNil();
	}
		
	public Type getType() {
		return this.type;
	}

	public void genJava(PW pw){};
	
	private Type type;
}