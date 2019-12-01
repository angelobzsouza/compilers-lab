/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

import java.util.*;
import lexer.Token;

public class TypeCianetoClass extends Type {

	public TypeCianetoClass(String name, TypeCianetoClass superclass, boolean isInheritable) {
		super(name);
		this.name = name;
		this.superclass = superclass;
		this.isInheritable = isInheritable;
		this.memberList = new ArrayList<>();
	}

	public TypeCianetoClass getSuperClass() {
		return this.superclass;
	}
	
	public boolean getInheritable() {
		return this.isInheritable;
	}
	
	public ArrayList<MethodDec> getMethods() {		
		ArrayList<MethodDec> methods = new ArrayList<>();
			
		for (Member m: memberList) {
			if (m instanceof MethodDec) {
				MethodDec method = (MethodDec) m;
				if (method.getQualifier().getToken1() == Token.PUBLIC) {
					methods.add(method);
            	}
			}
		}
		return methods;
	}

	public String getClassName() {
		return this.name;
	}
	
	public ArrayList<Variable> getFields() {

		ArrayList<Variable> fields = new ArrayList<>();
					
		for (Member f: memberList) {
		
			if (f instanceof Variable) {
				Variable field = (Variable) f;
				if (field.getQualifier().getToken1() == Token.PUBLIC){
					fields.add(field);
				}			
			}
		}
		return fields;
	}
	
	public boolean isSubclass(TypeCianetoClass c2) {
	
		TypeCianetoClass c = c2;

		while (c != null && c != this) {
			c = c.getSuperClass();
		}
		
		if (c != null && c == this) {
			return true;
		}
		return false;
	}
	
	public void setMemberList(ArrayList<Member> memberList) {
		this.memberList = memberList;
	}
	
	public ArrayList<Member> getMembers() {
		return this.memberList;
	}

   @Override
   public String getCname() {
      return getName();
   }
	@Override
	public void genJava (PW pw) {
			pw.print("public class "+name+" ");
			pw.println(" {");
			pw.add();
			memberList.stream().forEach((member) -> {
					member.genJava(pw);
			});
			pw.sub();
			pw.printlnIdent("}");
	}
 
	private String name;
	private TypeCianetoClass superclass;
	private ArrayList<Member> memberList;
	private boolean isInheritable;
}