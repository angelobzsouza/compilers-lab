/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/

package ast;
import lexer.*;

public class CompositeExpr extends Expr {

	public Type getType() {
		Type rightType = right.getType();
		Type leftType = left.getType();
		if	(this.op == Token.GT || this.op == Token.GE || this.op == Token.LT || this.op == Token.LE) {
			if (leftType == Type.intType && rightType == leftType) {
				return Type.booleanType;
			}
		}
		else if (this.op == Token.EQ || this.op == Token.NEQ) {
			
			if (leftType == rightType) {
				return Type.booleanType;
			}
			
			if ((leftType == Type.stringType && rightType == Type.nullType) ||(rightType == Type.stringType && leftType == Type.nullType)) {
				return Type.booleanType;
			}
			
			if ((leftType instanceof TypeCianetoClass && rightType == Type.nullType) || (rightType instanceof TypeCianetoClass && leftType == Type.nullType)) {
				return Type.booleanType;
			}
			
			if (leftType instanceof TypeCianetoClass && rightType instanceof TypeCianetoClass) {
				TypeCianetoClass c1 = (TypeCianetoClass) leftType;
				TypeCianetoClass c2 = (TypeCianetoClass) rightType;
				if (c1.isSubclass(c2) || c2.isSubclass(c1)) {
					return Type.booleanType;
				}
			}
		}
		return Type.undefinedType;
	}
	  
	public CompositeExpr(Expr left, Token op, Expr right) {
		this.left = left;
		this.right = right;
		this.op = op;
	}

	public void genJava( PW pw ) {
        pw.print("(");
        left.genJava(pw);
        pw.print(" " + op.toString() + " ");
        right.genJava(pw);
        pw.print(")");
    }
	Expr right;
	Expr left;
	Token op;
}
