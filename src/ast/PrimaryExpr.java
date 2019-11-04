/*Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;
import java.util.ArrayList;

public class PrimaryExpr extends Factor {
	
	public PrimaryExpr(Type type) {
		this.type = type;
	}
		
	public Type getType() {
		return this.type;
	}
	
	private Type type;
	private String statType;
}
