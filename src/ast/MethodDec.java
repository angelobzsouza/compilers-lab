/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;
import java.util.ArrayList;
import lexer.Token;

public class MethodDec extends Member {
	
	public MethodDec(String id, ArrayList<Variable> paramList, Type returnType, Qualifier qualifier) {
		this.id = id;
		this.paramList = paramList;
		this.returnType = returnType;
		this.qualifier = qualifier;
	}
	
	public String getMethodName() {
		return this.id;
	}
	
	public void setStatList(ArrayList<Statement> statList) {
		this.statList = statList;
	}
	
	public ArrayList<Variable> getParamList() {
		return this.paramList;
	}
	
	public Qualifier getQualifier() {
		return this.qualifier;
	}
	
	public Type getReturnType() {
		return this.returnType;
	}

	@Override
	public void genJava (PW pw) {
		if (this.qualifier != null)
		{
			pw.printIdent(this.qualifier.getToken1()+" ");

			if (this.qualifier.getToken2() != null) {
				pw.print(this.qualifier.getToken2()+" ");
			}
			if (this.qualifier.getToken3() != null) {
				pw.print(this.qualifier.getToken3()+" ");
			}
			if (this.returnType != null) {
				this.returnType.genJava(pw);
			}
		}
		pw.print(this.id+" (");

		paramList.stream().forEach((param) -> {
				param.genJava(pw);
		});
		pw.printlnIdent(") {");
		pw.add();
		
		statList.stream().forEach((stat) -> {
			if (stat != null)
				stat.genJava(pw);
		});
		pw.sub();
		pw.printlnIdent("}");
	}
	
	private String id;
	private ArrayList<Variable> paramList;
	private Type returnType = Type.nilType;
	private ArrayList<Statement> statList;
	private Qualifier qualifier;
	
}
