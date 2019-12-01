/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;
import lexer.Token;

public class Variable extends Member {

	public Variable(String name, Type type) {
		this.name = name;
		this.type = type;
		this.qualifier = new Qualifier(Token.PUBLIC, null, null);
	}
	
	public Variable(String name, Type type, Qualifier q) {
		this.name = name;
		this.type = type;
		this.qualifier = q;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public Qualifier getQualifier() {
		return this.qualifier;
	}

	@Override
	public void genJava (PW pw) {
		pw.printlnIdent(qualifier+" "+type+" "+name+";");
	}

	private Qualifier qualifier;
	private String name;
	private Type type;
}
