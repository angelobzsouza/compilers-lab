/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

import java.io.PrintWriter;

public class PW {

	public PW( PrintWriter out ) {
		this.out = out;
		currentIndent = 0;
	}

	public void add() {
		currentIndent += step;
	}

	public void sub() {
		if ( currentIndent < step ) {
			System.out.println("Internal compiler error: step (" + step + ") is greater then currentIndent (" + currentIndent + ") in method sub of class PW");
		}
		currentIndent -= step;
	}

	public void set( int indent ) {
		currentIndent = indent;
	}

	public void printIdent( String s ) {
		out.print( space.substring(0, currentIndent) );
		out.print(s);
	}

	public void printlnIdent( String s ) {
		out.print( space.substring(0, currentIndent) );
		out.println(s);
	}

	public void print( String s ) {
		out.print(s);
	}

	public void println( String s ) {
		out.println(s);
	}

	public void println() {
		out.println("");
	}

	int currentIndent = 0;
	public int step = 3;
	private PrintWriter out;
	static final private String space = "                                                                                                        ";
}


