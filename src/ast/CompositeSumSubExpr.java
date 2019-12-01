/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

import lexer.Token;

public class CompositeSumSubExpr extends SumSubExpr {
	
	public CompositeSumSubExpr(Expr left, Token op, Expr right) {
		this.left = left;
		this.right = right;
		this.op = op;
	}
	
	@Override
	public Type getType() {
		
		Type rightType = right.getType();
		Type leftType = left.getType();
		
		if ((op == Token.PLUS || op == Token.MINUS) && leftType == Type.intType && rightType == leftType) {
			return leftType;
		}
		
		if (op == Token.OR && left.getType() == Type.booleanType && right.getType() == left.getType()) {
			return Type.booleanType;
		}
		
		return Type.undefinedType;
	}
	public void genJava(PW pw){
		left.genJava(pw);
		pw.printIdent(op.toString());
		right.genJava(pw);
	};
	Expr right;   
	Expr left;
	Token op;

}
