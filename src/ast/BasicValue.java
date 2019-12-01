/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

public class BasicValue extends Factor {
	public BasicValue(String stringValue) {
		this.stringValue = stringValue;
		this.type = Type.stringType;
	}
	public BasicValue(boolean boolValue) {
		this.boolValue = boolValue;
		this.type =  Type.booleanType;
	}
	public BasicValue(Integer intValue) {
		this.intValue = intValue;
		this.type = Type.intType;
	}
	public Type getType() {
		return this.type;
	}

	public void genJava(PW pw){
		if(type == Type.booleanType){
			pw.print(String.valueOf(boolValue));
		}
		else if(type == Type.intType ){
			pw.print(String.valueOf(intValue));
		}
		else if(type == Type.stringType){
			pw.print(stringValue);
		}
	}
	
	private Type type;
	private Integer intValue;
	private boolean boolValue;
	private String stringValue;
}
