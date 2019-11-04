/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

import lexer.Token;

public class CompositeTerm extends Term {
	
	public CompositeTerm(Expr left, Token op, Expr right) {
		this.left = left;
		this.right = right;
		this.op = op;
	}
	
	@Override
	public Type getType() {
		
		Type rightType = right.getType();
		Type leftType = left.getType();
		
		if (this.op == Token.AND && leftType == Type.booleanType && rightType == leftType) {
			return leftType;
		} 
		
		if (this.op != Token.AND && leftType == Type.intType && rightType == leftType) {
			return leftType;
		}
		
		return Type.undefinedType;
	} 

	Expr right; 
	Expr left;
	Token op;
	
}