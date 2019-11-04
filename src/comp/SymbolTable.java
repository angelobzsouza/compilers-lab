/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package comp;

import java.util.*;

public class SymbolTable {

	public SymbolTable() {
    globalTable = new Hashtable();
    localTable  = new Hashtable();
    classTable = new Hashtable();
    superClassTable =  new Hashtable();
  }
    
  public Object putInGlobal( String key, Object value ) {
    return globalTable.put(key, value);
  }

  public Object putInLocal( String key, Object value ) {
    return localTable.put(key, value);
  }
  
  public Object putInClass( String key, Object value ) {
    return classTable.put(key, value);
  }

  public Object getInClass( Object key ) {
    Object result;
    if ((result = localTable.get(key)) != null) {
        return result;
    }

    else return classTable.get(key);
  }
     
  public Object putInSuperClassTable( String key, Object value ) {
    return superClassTable.put(key, value);
  }
  
  public Object getInLocal( Object key ) {
    return localTable.get(key);
  }
  
  public Object getInGlobal( Object key ) {
    return globalTable.get(key);
  }

  public Object getInSuperClassTable( Object key ) {
    return superClassTable.get(key);
  }

  public Object get( String key ) {
    Object result;
    if ( (result = localTable.get(key)) != null ) {
        return result;
    }
    else {
        return globalTable.get(key);
    }
  }

  public void removeLocalIdent() {
    localTable.clear();
  }
  
  public void removeSuperTableIdent() {
    superClassTable.clear();
  }
  
  public void removeClassIdent() {
    classTable.clear();
  }
   
  private Hashtable<String, Object> globalTable, localTable, classTable, superClassTable;
}
