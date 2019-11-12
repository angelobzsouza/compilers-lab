/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;
import lexer.Token;
import java.util.ArrayList;

public class LocalDec extends Statement {
	
	public LocalDec(Type type, ArrayList<Variable> idList, Expr e) {
		this.type = type;
		this.idList = idList;
		this.expr = e;	
	}

	public void genJava(PW pw){};
	// public void genJava (PW pw) {
    //     pw.set(0);
        
    //     if (type != null) {
    //         pw.print(type.getName() + " ");
    //     } else {
    //         pw.print("void ");
    //     }
        
    //     pw.print(type.getName() + "(");
        
    //     for (int i = 0; i < idList.size(); ) {
    //         idList.get(i).getName().genJava(pw);
            
    //         if (++i < idList.size()) {
    //             pw.print(", ");
    //         }
    //     }
        
    //     pw.out.print("){");
        
    //     if (statList.size() > 0) {
    //         pw.println();
    //         pw.add();
    //     }
        
    //     statList.stream().forEach((stat) -> {
    //         stat.genJava(pw);
    //     });
        
    //     pw.println("}");
    //     pw.println();
    // }
	
	private ArrayList<Variable> idList;
	private Type type;
	private Expr expr;
}
