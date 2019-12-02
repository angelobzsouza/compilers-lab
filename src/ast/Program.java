/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/

package ast;

import java.util.*;
import comp.CompilationError;

public class Program {

	public Program(ArrayList<TypeCianetoClass> classList, ArrayList<MetaobjectAnnotation> metaobjectCallList, 
			       ArrayList<CompilationError> compilationErrorList) {
		this.classList = classList;
		this.metaobjectCallList = metaobjectCallList;
		this.compilationErrorList = compilationErrorList;
	}



	public void genJava(PW pw) {
        pw.printlnIdent("import java.util.*;");

        pw.printlnIdent("public class "+this.mainJavaClassName+" {");
        pw.add();

				// pw.printlnIdent("private static class ReadInput {");
				// pw.add();

        // pw.printlnIdent("public static String readString () {");
				// pw.add();
        // pw.printlnIdent("Scanner scanner = new Scanner(System.in);");
        // pw.printlnIdent("return scanner.nextLine();");
				// pw.sub();
        // pw.printlnIdent("}");

        // pw.printlnIdent("public static int readInt () {");
				// pw.add();
        // pw.printlnIdent("Scanner scanner = new Scanner(System.in);");
        // pw.printlnIdent("return scanner.nextInt();");
				// pw.sub();
        // pw.printlnIdent("}");

				// pw.sub();
				// pw.printlnIdent("}");

        classList.stream().forEach((currentClass) -> {
            currentClass.genJava(pw);
        });
        pw.printlnIdent("public static void main (String []args) {");
        pw.add(); 
        pw.printlnIdent("new Program().run();");
        pw.sub();
        pw.printlnIdent("}");
        pw.sub();
        pw.printlnIdent("}");
	}

	
	public ArrayList<TypeCianetoClass> getClassList() {
		return classList;
	}

	public ArrayList<MetaobjectAnnotation> getMetaobjectCallList() {
		return metaobjectCallList;
	}
	
	public boolean hasCompilationErrors() {
		return compilationErrorList != null && compilationErrorList.size() > 0 ;
	}

	public ArrayList<CompilationError> getCompilationErrorList() {
		return compilationErrorList;
	}

	public void setMainJavaClassName(String mainJavaClassName) {
		this.mainJavaClassName = mainJavaClassName;
	}

	/**
	the name of the main Java class when the
	code is generated to Java. This name is equal
	to the file name (without extension)
	*/
	private String mainJavaClassName;
	private ArrayList<TypeCianetoClass> classList;
	private ArrayList<MetaobjectAnnotation> metaobjectCallList;	
	ArrayList<CompilationError> compilationErrorList;

}