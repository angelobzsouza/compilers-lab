/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

import lexer.Token;

public class CompositeSignalFactor extends SignalFactor {
	
	public CompositeSignalFactor(Token op, Expr right) {
		this.right = right;
		this.op = op;
	}
	
	@Override
	public Type getType() {	
		if (op == null) {
			return right.getType();
		}
		if (op != null && right.getType() == Type.intType) {
			return right.getType();
		} 
		return Type.undefinedType;
	}

	public void genJava(PW pw){
		if (op != null){
			pw.print(op.toString());
		} 
		right.genJava(pw);
	};
	
	Token op;
	Expr right;
}
