/*Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;
import java.util.ArrayList;

public class PrimaryExpr extends Factor {
	
	public PrimaryExpr(Type type, String name, MethodDec m) {
		this.type = type;
		this.name = name;
		this.m = m;
	}
		
	public Type getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public void setIsNegationAsTrue () {
		isNegation = true;
	}

	public void genJava(PW pw){
		if (isNegation) {
			pw.print("!");
		}

		if (m != null) {
			pw.print(name+"."+m.getMethodName()+"() ");
		}
		else {
			pw.print(name);
		}
	};
	
	private Type type;
	private String name = "";
	private MethodDec m = null;
	private boolean isNegation = false;
}
