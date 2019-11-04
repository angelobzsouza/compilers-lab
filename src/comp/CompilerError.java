/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package comp;

public class CompilerError extends RuntimeException {
	public CompilerError(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getErrorMessage() { return errorMessage; }

	private String errorMessage;
	private static final long serialVersionUID = 1L;

}
