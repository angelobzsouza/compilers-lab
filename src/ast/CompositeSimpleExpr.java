/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

import lexer.Token;

public class CompositeSimpleExpr extends SimpleExpr {

	public CompositeSimpleExpr(Expr left, Token op, Expr right) {
		this.left = left;
		this.right = right;
		this.op = op;
	}
	
	@Override
	public Type getType() {
		Type rightType = right.getType();
		Type leftType = left.getType();
	
		if ( (rightType == Type.intType || rightType == Type.stringType) && (leftType == Type.intType || leftType == Type.stringType)) {
			return Type.stringType;
		}
		
		return Type.undefinedType;
	}
	public void genJava(PW pw){};
	Expr right; 
	Expr left;
	Token op;

}
