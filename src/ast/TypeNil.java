/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

public class TypeNil extends Type {

	public TypeNil() {
		super("nil");
	}

	public String getCname() {
		return "nil";
	}
	
	@Override
	public void genJava(PW pw){}

}
