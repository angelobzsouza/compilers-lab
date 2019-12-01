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
		pw.printIdent(this.qualifier+" "+this.returnType+" "+this.id+" (");
		paramList.stream().forEach((param) -> {
				param.genJava(pw);
		});
		pw.printlnIdent(") {");
		pw.add();
		statList.stream().forEach((stat) -> {
			stat.genJava(pw);
		});
		pw.sub();
		pw.printlnIdent("}");
	}
	
	private String id;
	private ArrayList<Variable> paramList;
	private Type returnType = Type.nullType;
	private ArrayList<Statement> statList;
	private Qualifier qualifier;
	
}
