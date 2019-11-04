/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package ast;

import java.util.ArrayList;

/** This class represents a metaobject annotation as <code>{@literal @}ce(...)</code> in <br>
 * <code>
 * @ce(5, "'class' expected") <br>
 * clas Program <br>
 *     public void run() { } <br>
 * end <br>
 * </code>
 *
   @author Jos�

 */
public class MetaobjectAnnotation {

	public MetaobjectAnnotation(String name, ArrayList<Object> paramList) {
		this.name = name;
		this.paramList = paramList;
	}

	public ArrayList<Object> getParamList() {
		return paramList;
	}

	public String getName() {
		return name;
	}

	private String name;
	private ArrayList<Object> paramList;
}
