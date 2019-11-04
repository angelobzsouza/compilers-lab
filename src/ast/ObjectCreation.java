/*Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/

package ast;

public class ObjectCreation extends Factor {
	
	public ObjectCreation(TypeCianetoClass c) {
		this.type = c;
	}
	
	@Override
	public Type getType() {
		return type;
	}

	private Type type;
}
