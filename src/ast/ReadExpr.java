/*Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

public class ReadExpr extends Factor{

	public ReadExpr(Type readType) {
		this.readType = readType;
	}

	public Type getType() {

		return this.readType;
	}

	public void genJava(PW pw){
		//scanner??
	};
	
	private Type readType;
}
