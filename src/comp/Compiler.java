/* 
Angelo Bezerra de Souza RA: 726496
Igor Inácio de Carvalho Silva RA: 725804
*/
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
import ast.*;
import lexer.Lexer;
import lexer.Token;

public class Compiler {

	public Compiler() { }

	// compile must receive an input with an character less than
	// p_input.lenght
	public Program compile(char[] input, PrintWriter outError) {

		ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
		signalError = new ErrorSignaller(outError, compilationErrorList);
		symbolTable = new SymbolTable();
		lexer = new Lexer(input, signalError);
		signalError.setLexer(lexer);

		Program program = null;
		next();
		program = program(compilationErrorList);
		return program;
	}

	private Program program(ArrayList<CompilationError> compilationErrorList) {
		ArrayList<MetaobjectAnnotation> metaobjectCallList = new ArrayList<>();
		ArrayList<TypeCianetoClass> CianetoClassList = new ArrayList<>();
		Program program = new Program(CianetoClassList, metaobjectCallList, compilationErrorList);
		boolean thereWasAnError = false;
		haveRun = false;
		while ( lexer.token == Token.CLASS ||
				(lexer.token == Token.ID && lexer.getStringValue().equals("open") ) ||
				lexer.token == Token.ANNOT ) {
			try {
				while ( lexer.token == Token.ANNOT ) {
					metaobjectAnnotation(metaobjectCallList);
				}
				CianetoClassList.add(classDec());
			}
			catch( CompilerError e) {
				// if there was an exception, there is a compilation error
				thereWasAnError = true;
				while ( lexer.token != Token.CLASS && lexer.token != Token.EOF ) {
					try {
						next();
					}
					catch ( RuntimeException ee ) {
						e.printStackTrace();
						return program;
					}
				}
			}
			catch ( RuntimeException e ) {
				e.printStackTrace();
				thereWasAnError = true;
			}

		}
		if ( !thereWasAnError && lexer.token != Token.EOF ) {
			try {
				error("End of file expected");
			}
			catch( CompilerError e) {
			}
		}

		if ( !thereWasAnError && haveRun == false) {
			this.signalError.showError("Every program must have a class named 'Program' with a public parameterless method called 'run'", true);

		}

		return program;
	}

	/**  parses a metaobject annotation as <code>{@literal @}cep(...)</code> in <br>
     * <code>
     * {@literal @}cep(5, "'class' expected") <br>
     * class Program <br>
     *     func run { } <br>
     * end <br>
     * </code>
     *

	 */
	@SuppressWarnings("incomplete-switch")
	private void metaobjectAnnotation(ArrayList<MetaobjectAnnotation> metaobjectAnnotationList) {
		String name = lexer.getMetaobjectName();
		int lineNumber = lexer.getLineNumber();
		next();
		ArrayList<Object> metaobjectParamList = new ArrayList<>();
		boolean getNextToken = false;
		if ( lexer.token == Token.LEFTPAR ) {
			// metaobject call with parameters
			next();
			while ( lexer.token == Token.LITERALINT || lexer.token == Token.LITERALSTRING ||
					lexer.token == Token.ID ) {
				switch ( lexer.token ) {
				case LITERALINT:
					metaobjectParamList.add(lexer.getNumberValue());
					break;
				case LITERALSTRING:
					metaobjectParamList.add(lexer.getLiteralStringValue());
					break;
				case ID:
					metaobjectParamList.add(lexer.getStringValue());
				}
				next();
				if ( lexer.token == Token.COMMA )
					next();
				else
					break;
			}
			if ( lexer.token != Token.RIGHTPAR )
				error("')' expected after annotation with parameters");
			else {
				getNextToken = true;
			}
		}
		switch ( name ) {
		case "nce":
			if ( metaobjectParamList.size() != 0 )
				error("Annotation 'nce' does not take parameters");
			break;
		case "cep":
			if ( metaobjectParamList.size() != 3 && metaobjectParamList.size() != 4 )
				error("Annotation 'cep' takes three or four parameters");
			if ( !( metaobjectParamList.get(0) instanceof Integer)  ) {
				error("The first parameter of annotation 'cep' should be an integer number");
			}
			else {
				int ln = (Integer ) metaobjectParamList.get(0);
				metaobjectParamList.set(0, ln + lineNumber);
			}
			if ( !( metaobjectParamList.get(1) instanceof String) ||  !( metaobjectParamList.get(2) instanceof String) )
				error("The second and third parameters of annotation 'cep' should be literal strings");
			if ( metaobjectParamList.size() >= 4 && !( metaobjectParamList.get(3) instanceof String) )
				error("The fourth parameter of annotation 'cep' should be a literal string");
			break;
		case "annot":
			if ( metaobjectParamList.size() < 2  ) {
				error("Annotation 'annot' takes at least two parameters");
			}
			for ( Object p : metaobjectParamList ) {
				if ( !(p instanceof String) ) {
					error("Annotation 'annot' takes only String parameters");
				}
			}
			if ( ! ((String ) metaobjectParamList.get(0)).equalsIgnoreCase("check") )  {
				error("Annotation 'annot' should have \"check\" as its first parameter");
			}
			break;
		default:
			error("Annotation '" + name + "' is illegal");
		}
		metaobjectAnnotationList.add(new MetaobjectAnnotation(name, metaobjectParamList));
		if ( getNextToken ) next();
	}

	private TypeCianetoClass classDec() {
		boolean isInheritable = false;
		String superclassName = null;
		TypeCianetoClass superclass = null;
		
		if ( lexer.token == Token.ID && lexer.getStringValue().equals("open") ) {
			isInheritable = true;
			next();
		}
		if ( lexer.token != Token.CLASS ) {
			error("'class' expected");
		}	
		next();
		
		if ( lexer.token != Token.ID ) {
			error("Identifier expected");
		}
		
		//semantic
		String className = lexer.getStringValue();
		
		if (symbolTable.getInGlobal(className) != null) {
			error("Class " + className + " has already been declared");
		}

		next();
		
		if ( lexer.token == Token.EXTENDS ) {
		
			next();	
		
			if ( lexer.token != Token.ID ) {
				error("Class expected");
			}
			
			//semantic

			superclassName = lexer.getStringValue();
			superclass = (TypeCianetoClass) symbolTable.getInGlobal(superclassName);
		
			if (superclass != null) {
				//verify if the superclass is inheritable
				if (superclass.getInheritable() == false) {
					error("Superclass '" + superclassName + "' is not inheritable");
				}
			}	
			else {
				error("The superclass '" + superclassName + "' does not exist");
			}
			
			TypeCianetoClass aux = superclass;
			
			do {
				//get all superclass member
				ArrayList<Member> memberlist = aux.getMembers();
				for(int i = 0; i < memberlist.size(); i++) {
					Member m = memberlist.get(i);
					//if a method is public, put in superclass table
					if(m instanceof MethodDec &&((MethodDec) m).getQualifier().isPublic()) {
						symbolTable.putInSuperClassTable(((MethodDec)m).getMethodName(), m);
					}
				}
				//class can have a super class, and super class can have a super class..
				aux = aux.getSuperClass();
			} while(aux != null);
			next();
		}

		TypeCianetoClass typeCianetoClass = new TypeCianetoClass(className, superclass, isInheritable); 
		
		currentClass = typeCianetoClass;
       
		symbolTable.putInGlobal(className, typeCianetoClass);
		
		//return all members os class
		ArrayList<Member> ml = memberList();
		currentClass.setMemberList(ml);
		
		if (ml == null || lexer.token != Token.END) {
			error(" Class member OR 'end' expected");
		}

		if (currentClass.getName().equals("Program")) {
			boolean flag  = false;
			for (Member m : ml) {
				if (m instanceof MethodDec && ((MethodDec) m).getMethodName().equals("run")) {
					flag = true;
				}
			}
			
			if (flag == false) {
				error("Method 'run' was not found in class 'Program'");
			}
		}
		
		next();
		symbolTable.removeSuperTableIdent();
		symbolTable.removeClassIdent();
		return typeCianetoClass;
	}

	private ArrayList<Member> memberList() {
		ArrayList<Member> ml = new ArrayList<>();
		ArrayList<Variable> fieldList = null;
		
		while ( true ) {
			Qualifier q = qualifier();
			
			if ( lexer.token == Token.VAR) {
				fieldList = fieldDec(q);
				ml.addAll(fieldList);
			}
			else if ( lexer.token == Token.FUNC ) {
				ml.add(methodDec(q));
			}
			else {
				break;
			}
		}

		return ml;
	}

	private void error(String msg) {
		this.signalError.showError(msg);
	}

	private void next() {
		lexer.nextToken();
	}

	private void check(Token shouldBe, String msg) {
		if ( lexer.token != shouldBe ) {
			error(msg);
		}
	}

	private MethodDec methodDec(Qualifier q) {
		haveReturn = false;
		String id = null;
		ArrayList<Variable> paramList = new ArrayList<>();
		Type returnType = null;
		ArrayList<Statement> statList = new ArrayList<>();
		
		next();
		if ( lexer.token == Token.ID ) {
			id = lexer.getStringValue();
			next();

		} else if ( lexer.token == Token.IDCOLON ) {	
			id = lexer.getStringValue();
			next();
			paramList = parameterList();
			for (Variable p : paramList) {
				if (symbolTable.getInLocal(p.getName()) != null) {
					error("variavel" + p.getName() + "' ja foi declarada");
				}
				symbolTable.putInLocal(p.getName(), p);
			}
		} else {
			error("An identifier or identifer: was expected after 'func'");
		}
		
		Object obj = symbolTable.getInClass(id);
		
		if (obj != null) {
			error("Method '"+ id + "' is being redeclared");
		}
		
		if ( lexer.token == Token.MINUS_GT ) {
			next();
			returnType = type();
		}
		
		MethodDec method = (MethodDec) symbolTable.getInSuperClassTable(id);
		if(q.override()) {
			if(method == null) {
				error("The method '" + id +"' doesn't exist in superclass or the signature is different");	
			}else if(method.getQualifier().getToken1() == Token.FINAL) {
				error("The method in superclass is final, so it can not be override");
				
			}else if(paramList.size() != method.getParamList().size()) {
				error("Method '"+ id +"' of the subclass '"+ currentClass.getName() +"' has a signature different from the same method of superclass '"+ currentClass.getSuperClass().getName() +"'");
			
			}else if(returnType != method.getReturnType()) {
				error("Method '"+ method.getMethodName() +"' of subclass '"+ currentClass.getName() +"' has a signature different from method inherited from superclass '"+ currentClass.getSuperClass().getName() +"'");
			}
			for(int i = 0; i < method.getParamList().size(); i++) {
				if(paramList.get(i).getType() != method.getParamList().get(i).getType()) {
					error("Method '"+ id +"' of the subclass '"+ currentClass.getName() +"' has a signature different from the same method of superclass '"+ currentClass.getSuperClass().getName() +"'");
				}
			}
		} else if (method != null) {
			error("'override' expected before overridden method");
		}
		if ( lexer.token != Token.LEFTCURBRACKET ) {
			error("'{' expected");
		}
		
		MethodDec m = new MethodDec(id, paramList, returnType, q);
		
		if (currentClass.getName().equals("Program")) {
			if (m.getMethodName().equals("run") || m.getMethodName().equals("run:")) {
				if (m.getQualifier().getToken1() == Token.PUBLIC) {
					if (m.getParamList().isEmpty() == false || m.getReturnType() != null) {
						error("Method 'run:' of class 'Program' cannot take parameters");
					} else {
						haveRun = true;
					}
				} else {
					error("Method 'run' of class 'Program' cannot be private");
				}
			}
		} 
			
		next();
		currentMethod = m;
		symbolTable.putInClass(id, m);
		statList = statementList();	
		currentMethod.setStatList(statList);
	
		if ( lexer.token != Token.RIGHTCURBRACKET ) {
			error("'}' expected");
		}
		
		if(returnType != null){
			if(haveReturn == false){
				error("Missing 'return' statement in method '" + currentMethod.getMethodName() + "'");
			}
		}
        
		next();
		symbolTable.removeLocalIdent();
        
		return m;
	}

	private ArrayList<Statement> statementList() {	
		ArrayList<Statement> listStmt = new ArrayList<>();
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END && lexer.token != Token.UNTIL ) {
			listStmt.add(statement());
		}	
		return listStmt;
	}

	private Statement statement() {
		Statement stmt = null;
		boolean checkSemiColon = true;
		switch ( lexer.token ) {
		case IF:
			stmt =  ifStat();
			checkSemiColon = false;
			break;
		case WHILE:
			stmt =  whileStat();
			checkSemiColon = false;
			break;
		case RETURN:
			stmt =  returnStat();
			break;
		case BREAK:
			stmt =  breakStat();
			break;
		case SEMICOLON:
			break;
		case REPEAT:
			stmt =  repeatStat();
			break;
		case VAR:
			stmt =  localDec();
			break;
		case ASSERT:
			stmt = assertStat();
			break;
		default:
			if ( lexer.token == Token.ID && lexer.getStringValue().equals("Out") ) {
				stmt = writeStat();
			} else  {
				AssignExpr ae = assignExpr();
			} 
		}
				
		if ( checkSemiColon ) {
			if (lexer.token != Token.SEMICOLON) {
				this.signalError.showError("';' expected", true);
			}
			next();
		}
		
		return stmt;
	}

	private AssignExpr assignExpr() {	
		AssignExpr a = null;
		isReadExpr = false;
		isMethod = null;
		isValid = false;
		Expr left = expr();
		if (left == null) {
			error("Expression expected");
		}

		Expr right = null;
		if (lexer.token == Token.ASSIGN) {
			next();
			right = expr();
			if (right == null) {
				error("Expression expected");			
			}

			if (!checkType(left.getType(), right.getType())) {
				error("Type error: the type of the expression of the right-hand side is a basic type and the type of the variable of the left-hand side is a class");
			}else if(isValid == false) {
				error("'operator expected' or 'variable expected at the left-hand side of a assignment'");
			}
		} else {
			if (isMethod == null && isReadExpr == false) {
				error("Expression expected");
			} else {
				if (isMethod != null && isMethod.getReturnType() != null) {
					error("Method '" + isMethod.getMethodName() + "' returns a value that is not used");
				}
			}
		}

		return new AssignExpr(left, right);
	}

	private LocalDec localDec() {
		next();
		ArrayList<Variable> idList = new ArrayList<>();
		Type t = type();
		Expr e = null;
		
		check(Token.ID, "Identifier expected");	
		while ( lexer.token == Token.ID ) {
			Variable v = new Variable(lexer.getStringValue(), t);
			
			if (symbolTable.getInLocal(v.getName()) != null) {
                error("Variable '" + v.getName() + "' is being redeclared");
            }

            symbolTable.putInLocal(v.getName(), v);
            idList.add(v);
			next();
								
			if ( lexer.token == Token.COMMA ) {
				next();
				check(Token.ID, "Missing identifier");
			}
			else {
				break;
			}
		}
		
		if ( lexer.token == Token.ASSIGN ) {
			next();
			e = expr();
			if (e == null) {
				error("Assign expression expected");
			}
		}		
		
		return new LocalDec(t, idList, e);
	}

	private RepeatStat repeatStat() {
		next();
		isLoop = true;
		ArrayList<Statement> statList = statementList();
		isLoop = false;
		check(Token.UNTIL, "'}' not expected before 'until'");
		next();
		
		Expr e = expr();
		
		if (e == null) {
			error("Repeat expression expected");
		}
		
		if (e.getType() != Type.booleanType) {
			error("boolean expression expected in a repeat-until statement");
		}
		
		return new RepeatStat(statList, e);
	}

	private BreakStat breakStat() {
		if(isLoop == false) {
			error("'break' statement found outside a 'while' or 'repeat-until' statement");
		}
		next();
		return new BreakStat();
	}

	private ReturnStat returnStat() {
		next();
		Expr e = expr();
		if (e == null) {
			error("Return expression expected");
		}
		if (currentMethod.getReturnType() == null) {
			error("Illegal 'return' statement. Method returns 'void'");
		}
		if (checkType(currentMethod.getReturnType(), e.getType()) == false) {
			error("Type error: type of the expression returned is not subclass of the method return type");
		}
		
		haveReturn = true;
		
		return new ReturnStat(e);
	}

	private WhileStat whileStat() {
		next();
		boolean loopInsideLoop = false;
		Expr e = expr();
		if(isLoop) {
			loopInsideLoop = true;
		}
		if (e == null) {
			error("While expression expected");
		}
		
		if (e.getType() != Type.booleanType) {
			error("non-boolean expression in 'while' command");
		}
		
		check(Token.LEFTCURBRACKET, "'{' expected after the 'while' expression");
		next();
		isLoop = true;
		ArrayList<Statement> statList = statementList();
		if(!loopInsideLoop) {
			isLoop = false;
		}
		check(Token.RIGHTCURBRACKET, "'}' was expected after the 'while' statement");
		
		next();
			
		return new WhileStat(e, statList);
	}

	private IfStat ifStat() {
		next();
		Expr e = expr();
		if (e == null) {
			error("If expression expected");
		}
		if (e.getType() != Type.booleanType) {
			error("Expression expected OR Unknown sequence of symbols");
		}
		
		ArrayList<Statement> ifPart = new ArrayList<>();
		ArrayList<Statement> elsePart = new ArrayList<>();
		
		check(Token.LEFTCURBRACKET, "'{' expected after the 'if' expression");
		next();
		
		while ( lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END && lexer.token != Token.ELSE ) {
			ifPart.add(statement());
		}
		
		check(Token.RIGHTCURBRACKET, "'}' was expected after the 'if' statement");
		next();
		
		if ( lexer.token == Token.ELSE ) {
			next();
			check(Token.LEFTCURBRACKET, "'{' expected after 'else'");
			next();
			while ( lexer.token != Token.RIGHTCURBRACKET ) {
				elsePart.add(statement());
			}
			check(Token.RIGHTCURBRACKET, "'}' was expected after the 'else' statement");
			next();
		}
		return new IfStat(e, ifPart, elsePart);
	}

	private WriteStat writeStat() {
		next();
			check(Token.DOT, "a '.' was expected after 'Out'");
		next();
		check(Token.IDCOLON, "'print:' or 'println:' was expected after 'Out.'");
		String printName = lexer.getStringValue();
		if(!printName.equals("print:") && !printName.equals("println:")) {
			this.error("'print:' or 'println:' was expected after 'Out.'");
		}
		next();
		Expr e = expr();
		if (e == null) {
			error("Write expression expected");
		}
		if (e.getType() != Type.intType && e.getType() != Type.stringType) {
			error("Attempt to print a boolean expression");
		}
		return new WriteStat(e);
	}

	private Expr expr() {
		
		Expr left = simpleExpr();

		while (lexer.token == Token.EQ || lexer.token == Token.LT || lexer.token == Token.GT || 
			lexer.token == Token.LE || lexer.token == Token.GE || lexer.token == Token.NEQ) {
	
			Token op = lexer.token;		
			
			next();
			
			Expr right = simpleExpr();
			
			Type old = left.getType();
			
			left = new CompositeExpr(left, op, right);
		
			if (left.getType() == Type.undefinedType) { 
				error("Type error: cannot compare types " + old.getName() + " and " + right.getType().getName());
			}
			
		}
		
		return left;
	}

	private Expr simpleExpr() {
		Expr left = sumSubExpr();
		// Review
		while (lexer.getStringValue() == "++") {
			Token op = lexer.token;
			next();	
			
			Expr right = sumSubExpr();
			
			Type old = left.getType();

			left = new CompositeSimpleExpr(left, op, right);
	
			if (left.getType() == Type.undefinedType) {
				error("Type error: cannot concat types " + old.getName() + " and " + right.getType().getName());
			}
			
		}
		return left;
	}

	private ArrayList<Variable> fieldDec(Qualifier q) {
		next();
		Type type = type();
		ArrayList<Variable> fieldList = new ArrayList<>();
		

        while (true) {				
            if (lexer.token != Token.ID) {
                error("Missing identifier");
            }
            
            if(q.getToken1() != Token.PRIVATE) {
                error("Attempt to declare public instance variable '" + lexer.getStringValue() + "'");
            }

            Variable v = new Variable(lexer.getStringValue(), type, q);
            
            if (symbolTable.getInClass(v.getName()) != null) {
                error("Variable '" + v.getName() + "' is being redeclared");
            }
            
            symbolTable.putInClass(v.getName(), v);
            fieldList.add(v);
            next();
            if ( lexer.token == Token.COMMA ) {
                next();
            } else {
                break;
            } 
        }
        
        // Semicolon is optional
        if (lexer.token == Token.SEMICOLON) {
            next();
        }
		return fieldList;
	}

	private Type type() {
		Type type = null;
		
		if (lexer.token == Token.INT) {
			type = Type.intType;
			next();
		} else if ( lexer.token == Token.BOOLEAN) {
			type = Type.booleanType;
			next();
		} else if ( lexer.token == Token.STRING ) {
			type = Type.stringType;
			next();
		} else if ( lexer.token == Token.ID ) {
			if (symbolTable.getInGlobal(lexer.getStringValue()) == null){
	            error("Type '"+ lexer.getStringValue() +"' was not found");
	        }
			TypeCianetoClass c = (TypeCianetoClass) symbolTable.getInGlobal(lexer.getStringValue());
			if (c != null) {
				type = c;
			} else {
				error("Class '" + lexer.getStringValue() + "' does not exist");
			}
			next();
		} else {
			this.error("Type expected");
		}

		return type;
	}

	private Qualifier qualifier() {
		Token q1 = lexer.token;
		Token q2 = null;
		Token q3 = null;

		if ( lexer.token == Token.PRIVATE ) {
			next();
		}
		else if ( lexer.token == Token.PUBLIC ) {
			next();
		}
		else if ( lexer.token == Token.OVERRIDE ) {
			next();
			if ( lexer.token == Token.PUBLIC ) {
				q2 = Token.PUBLIC;
				next();
			}
		}
		else if ( lexer.token == Token.FINAL ) {
			next();
			if ( lexer.token == Token.PUBLIC ) {	
				next();
				q2 = Token.PUBLIC;
			}
			else if ( lexer.token == Token.OVERRIDE ) {
				q2 = Token.OVERRIDE;
				next();
				if ( lexer.token == Token.PUBLIC ) {
					next();
				}
				q3 = Token.PUBLIC;
			}
		} else if(q1 == Token.VAR){
			q1 = Token.PRIVATE;
			
		}else {
			q1 = Token.PUBLIC;
		}

		return new Qualifier (q1, q2, q3);
	}

	/**
	 * change this method to 'private'.
	 * uncomment it
	 * implement the methods it calls
	 */
	private Statement assertStat() {
		int lineNumber = lexer.getLineNumber();
		next();
		Expr e = expr();
		if (e == null) {
			error("Assert expression expected");
		}
		if (e.getType() != Type.booleanType) {
			error("Assert expression must be boolean");
		}
		if ( lexer.token != Token.COMMA ) {
			this.error("',' expected after the expression of the 'assert' statement");
		}
		next();
		if ( lexer.token != Token.LITERALSTRING ) {
			this.error("A literal string expected after the ',' of the 'assert' statement");
		}
		String message = lexer.getLiteralStringValue();
		next();
		return new AssertStat(e, message);
	}

	private LiteralInt literalInt() {

		LiteralInt e = null;

		// the number value is stored in lexer.getToken().value as an object of
		// Integer.
		// Method intValue returns that value as an value of type int.
		int value = lexer.getNumberValue();
		next();
		return new LiteralInt(value);
	}

	private static boolean startExpr(Token token) {

		return token == Token.FALSE || token == Token.TRUE
				|| token == Token.NOT || token == Token.SELF
				|| token == Token.LITERALINT || token == Token.SUPER
				|| token == Token.LEFTPAR || token == Token.NULL
				|| token == Token.ID || token == Token.LITERALSTRING;

	}

	private boolean checkType(Type type1, Type type2) {
		if (type1 == null || type2 == null)
			return false;
		if (type1 == type2) {
			return true;
		} 
		if ((type1 instanceof TypeCianetoClass && type2 == Type.nullType) || (type2 instanceof TypeCianetoClass && type1 == Type.nullType)) {
			return true;
		}
		if (type1.getClass() == type2.getClass()) {
			
			if (type1 instanceof TypeCianetoClass) {
				TypeCianetoClass c = (TypeCianetoClass) type1;
				if (c.isSubclass((TypeCianetoClass) type2)) {
					return true;
				} else {
					return false;
				}
			}
		}
		if ((type1 == Type.stringType && type2 == Type.nullType) || (type2 == Type.stringType && type1 == Type.nullType)) {
			return true;
		}
		
		return false;
	}

	private Expr sumSubExpr( ) {	
		Expr left = term();
		
		while (lexer.token == Token.PLUS || lexer.token == Token.MINUS || lexer.token == Token.OR) {
			Token op = lexer.token;
			next();
			Expr right = term();
			Type old = left.getType();
			left = new CompositeSumSubExpr(left, op, right);
			if (left.getType() == Type.undefinedType) {
				error("type boolean does not support operation '" + op.toString() +"'");
			}
			
		}
		return left;
	}

	private Expr term() {
		
		Expr left = signalFactor();
				
		while (lexer.token == Token.MULT || lexer.token == Token.DIV || lexer.token == Token.AND) {
			Token op = lexer.token;
			next();
			Expr right = signalFactor();
			Type old = left.getType();
			left = new CompositeTerm(left, op, right);
			
			if (left.getType() == Type.undefinedType) {
				error("Type error: cannot use operator " + op.toString() + " with "  + old.getName() + " and " + right.getType().getName());
			}
			
		}
		
		return left;
	}

	private SignalFactor signalFactor() {
		Token op = null;
	
		if (lexer.token == Token.PLUS || lexer.token == Token.MINUS) {
			op = lexer.token;
			next();
		}
		
		Factor right = factor();
		
		if (right == null) {
			error("Expression expected");
        }
		
		CompositeSignalFactor c = new CompositeSignalFactor(op, right);
		
		if (c.getType() == Type.undefinedType) {
			error("Operator '" + op.toString() + "' does not accepts " + right.getType().getName() + " expressions");
		}
		
		return c;
	}

	private Factor factor() {
		Expr e;
		ArrayList<Expr> eList;
		if (lexer.token == Token.LEFTPAR) {
			next();
			e = expr();
			if (lexer.token != Token.RIGHTPAR) 
				error("')' expected");
			if (e == null)
				return null;
			next();
			return new ExprFactor(e);
		} 
		if (lexer.token == Token.NOT) {
			next();
			Factor f = factor();
			if (f.getType() != Type.booleanType) {
				error("Operator '!' does not accepts '" + f.getType().getName() + "' values");
			}
			return f;
		} 
		if (lexer.token == Token.NULL) {
			next();
			return new NullExpr(); 
		} 
		if (lexer.token == Token.LITERALINT || lexer.token == Token.LITERALSTRING || lexer.token == Token.TRUE || lexer.token == Token.FALSE)
			return basicValue();
		
		if (lexer.token == Token.SUPER) {
			return superFunc();
		} else if (lexer.token == Token.ID) { 
			return auxId();
		} else if (lexer.token == Token.SELF){
			return auxSelf();
		} else if (lexer.getStringValue() == "In") {
			return readExpr();
		}
		return null;
	}

	private BasicValue basicValue() {
		
		if (lexer.token == Token.LITERALSTRING) {
			String s = lexer.getLiteralStringValue();
			next();
			return new BasicValue(s);
		}
		
		if (lexer.token == Token.LITERALINT) {
			Integer i = lexer.getNumberValue();
			next();
			return new BasicValue(i);
		}
		
		if (lexer.token == Token.TRUE) {
			boolean b = true;
			next();
			return new BasicValue(b);
		}			
		if (lexer.token == Token.FALSE) {
			boolean b = false;
			next();
			return new BasicValue(b);
		}
		
		error("Basic Value expected");
		return null;
			
	}

	private Factor superFunc() {
		Type type = Type.undefinedType;
		
		if(lexer.token == Token.SUPER){
			if (currentClass.getSuperClass() == null) {
				error("'super' used in class '" + currentClass.getName() + "' that does not have a superclass");
            }

			next();
			if(lexer.token == Token.DOT) {
				next();
				if(lexer.token == Token.ID) {
					String memberName = lexer.getStringValue();
					next();
					Variable f = searchFields(currentClass.getSuperClass(), memberName);
				
                    // Search variable or method in super class
					if (f != null) {
						type = f.getType();
					} else {
						MethodDec m = searchMethod(currentClass.getSuperClass(), memberName);
						if (m != null) {	
							if (m.getParamList().isEmpty() == false) {
								error("Method '" + memberName + "' has parameters");
							}
							type = m.getReturnType();
							isMethod = m;
						} else {
							error("Method '" + memberName + "' was not found in superclass '" + currentClass.getName() + "' or its superclasses");
						}
					}			
				} else if(lexer.token == Token.IDCOLON) {
					String methodName = lexer.getStringValue();
					next();
					
					MethodDec m = searchMethod(currentClass.getSuperClass(), methodName);

					if (m == null) {
						error("Superclass of '" + currentClass.getName() + "' does not have a method called '" + methodName + "'");
					}
					ArrayList<Expr> exprList = exprList();					
					checkParameters(exprList, m.getParamList(), methodName);
					type = m.getReturnType();
					isMethod = m;
				} else {
					error("id or idcolon expected");
				}
			} else {
				error("dot expected");
			}
		}
		if (type != Type.undefinedType) {
			return new PrimaryExpr(type);
		} else {
			return null;
		}
	}

    // Code review
	private Factor auxId() {
		Type type = Type.undefinedType;
		if(lexer.token == Token.ID) {
			String s = lexer.getStringValue();
			next();
			if (lexer.token == Token.DOT) {
				next();								
				if (lexer.token == Token.NEW) {
					
					next();
					TypeCianetoClass c = (TypeCianetoClass) symbolTable.getInGlobal(s);
					if (c == null) {
						error("Class '" + s + "' was not found");
					}
					return new ObjectCreation(c);
					
				} else if (lexer.token == Token.ID ) {
					String memberName = lexer.getStringValue();
					next();
					Variable v = (Variable)symbolTable.getInClass(s);	
					
					if (v == null) {
						error("Variable '" + s + "' not declared");
					}
					TypeCianetoClass c = (TypeCianetoClass) symbolTable.getInGlobal(v.getType().getName());
					if (c != null) {
						Variable f = searchFields(c, memberName);
						if (f != null) {
							type = f.getType();
						} else {
							MethodDec m = searchMethod(c, memberName);
							if (m != null) {	
								if (m.getParamList().isEmpty() == false) {
									error("Method '" + memberName + "' has parameters");
								}
								type = m.getReturnType();
								isMethod = m;
							} else {
								error("Method '" + memberName + "' was not found in class '" + v.getType().getName() + "' or its superclasses");
							}
						}				
					} else {
						error("Type of '" + s + "' does not have members");
					}
				} else if (lexer.token == Token.IDCOLON) {
					
					String methodName = lexer.getStringValue();
					next();	
					Variable v = (Variable) symbolTable.getInClass(s);	
					
					if (v == null) {
						error("'" + s + "' not declared");
					}
					
					TypeCianetoClass c = (TypeCianetoClass) symbolTable.getInGlobal(v.getType().getName());
					
					if (c != null) {
						MethodDec m = searchMethod(c, methodName);
						if (m == null) {
							error("Method '" + methodName + "' was not found in class '" + v.getType().getName() + "' or its superclasses");
						}						
						ArrayList<Expr> exprList = exprList();					
						checkParameters(exprList, m.getParamList(), methodName);
						
						type = m.getReturnType();
						isMethod = m;	
					} else {
						error("Type of '" + s + "' does not have members");
					}
				} else {
					error("id or idcolon expected");
				}
			} else {
				Object obj = (Object)symbolTable.getInClass(s);
				if (obj instanceof Variable) {
				
					Variable v = (Variable) symbolTable.getInClass(s);		
					Variable v1 = (Variable) symbolTable.getInLocal(s);	
					if (v == null) {
						error("Variable '" + s + "' not declared");
					}else if (v.getQualifier().getToken1() != Token.PUBLIC){
						error("self."+ s +" expected");
					}	
					if(v1 == null) {
						error("Variable '" + s + "' not declared");
					}
					
					type = v.getType();
					isValid = true;
				} else {
					error("Variable '" + s + "' was not declared");
				}	
			}
		} 
		if (type != Type.undefinedType) {
			return new PrimaryExpr(type);
		} else {
			return null;
		}
	}

	//new
	private Factor auxSelf() {
		Type type = Type.undefinedType;
		ArrayList<Expr> exprList = new ArrayList<>();
		MethodDec method = null;
		Member member = null;
		String methodName = null;
		if(lexer.token == Token.SELF) {
			next();
			if(lexer.token == Token.DOT) {
				next();
				if(lexer.token == Token.ID) {
					String memberName = lexer.getStringValue();
					member = (Member) symbolTable.getInClass(memberName);
					next();
					if(lexer.token == Token.DOT) {
						Object obj = symbolTable.getInClass(memberName);
						if(obj == null || (obj instanceof Variable) == false) {
							error("Variable '" + memberName + "' does not exist or was not declared");
						}
						Variable v = (Variable) obj;
						next();
						if(lexer.token == Token.ID) {
							methodName = lexer.getStringValue();
							next();
						
						}else if(lexer.token == Token.IDCOLON) {
							methodName = lexer.getStringValue();
							next();
							exprList = exprList();	
						}else{
							error("id or idcolon expected");
						}
						TypeCianetoClass classe = (TypeCianetoClass) symbolTable.getInGlobal(v.getType().getName());
						method = searchMethod(classe, methodName);
						
						if(method == null) {
							error("The method '" + memberName +"' doesn't exist in superclass or the signature is different");
						}else if(exprList.size() != method.getParamList().size()) {
							error("The signature of the method is different from the signature of the superclass");
						}
						for(int i = 0; i < method.getParamList().size(); i++) {
							if(exprList.get(i).getType() != method.getParamList().get(i).getType()) {
								error("The signature of the method is different from the signature of the superclass");
							}
						}
						type = method.getReturnType();
						isMethod = method;
					}else{	
						Variable f = searchFields(currentClass, memberName);
				
						if (f != null) {
							type = f.getType();
							isValid = true;
						} else {
							MethodDec m = searchMethod(currentClass, memberName);
							if (m != null) {	
								if (m.getParamList().isEmpty() == false) {
									error("Method '" + memberName + "' has parameters");
								}
								type = m.getReturnType();
								isMethod = m;

							} else {
								error("Method '" + memberName + "' was not found in class '" + currentClass.getName() + "' or its superclasses");
							}
						}				
					}
				} else if(lexer.token == Token.IDCOLON) {
					
					methodName = lexer.getStringValue();		
					next();
					MethodDec m = null;
					m = (MethodDec)symbolTable.getInClass(methodName); 
					if (m == null) {
						m = searchMethod(currentClass.getSuperClass(), methodName);
					}
					if (m == null) {
						error("Superclasses or class '" + currentClass.getName() + "' does not have the method '" + methodName + "'");
					}
					exprList = exprList();
					checkParameters(exprList, m.getParamList(), methodName);
					type = m.getReturnType();
					isMethod = m;

				}else {
					error("id or idcolon expected");
				}
			} else {
				type = currentClass;
			}
		}
		
		if (type != Type.undefinedType) {
			return new PrimaryExpr(type);
		} else {
			return null;
		}
	}

	private ReadExpr readExpr() {
		next();
		if(lexer.token == Token.DOT) {
			next();
			if (lexer.token == Token.ID && (lexer.getStringValue().equals("readInt") || lexer.getStringValue().equals("readString"))) {
				isReadExpr = true;
				if (lexer.getStringValue().equals("readInt")) {
					next();
					return new ReadExpr(Type.intType);
				} else {
					next();
					return new ReadExpr(Type.stringType);
				}
			} else {
				error("readInt or readString expected");
			}
		}else {
			error("dot expected");
		}
		return null;
	}

	private Variable searchFields(TypeCianetoClass c, String fieldName) {
		Member field = (Member) symbolTable.getInClass(fieldName);
		if (field != null && field instanceof Variable) {
			return (Variable) field;
		}
		
		while (c != null) {	
			ArrayList<Variable> memberList = c.getFields();
			for (Variable f: memberList) {
				if (f.getName().equals(fieldName)) {
					return f;
				}
			}
			c = c.getSuperClass();	
		}
		return null;
	}

	private MethodDec searchMethod(TypeCianetoClass c, String methodName) {	
		Member method = (Member) symbolTable.getInClass(methodName);
		if (method != null && method instanceof MethodDec && c == currentClass)
			return (MethodDec) method;
		while (c != null) {
			ArrayList<MethodDec> memberList = c.getMethods();
			for (MethodDec m: memberList) {	
				if (m.getMethodName().equals(methodName)) {
					return m;
				}
			}
			c = c.getSuperClass();	
		}
		return null;
	}

	private ArrayList<Expr> exprList() {
		ArrayList<Expr> exprList = new ArrayList<>();
		Expr e = null;
		e = expr();
		if (e == null)
			error("Expression expected");
			
		exprList.add(e);
		while (lexer.token == Token.COMMA) {
			next();
			e = expr();
			if (e == null)
				error("Expression expected");
			exprList.add(e);
		}

		return exprList;
	}

	private void checkParameters(ArrayList<Expr> list1, ArrayList<Variable> list2, String methodName){	
		if (list1.size() != list2.size()) {
			error("The number of given parameters are incorrect in '" + methodName + "' method");
		}
		
		for (int i = 0; i < list1.size(); i++) {
            if (!checkType(list2.get(i).getType(), list1.get(i).getType())) {
                error("Type error: the type of the real parameter is not subclass of the type of the formal parameter");
            }
        }
		
	}

	private ArrayList <Variable> parameterList() {
		ArrayList <Variable> paramList = new ArrayList<>();
		while (true) {	
			Type t = type();
						
			if (lexer.token != Token.ID) {
				error("Identifier expected");
			}
			
			paramList.add(new Variable(lexer.getStringValue(), t));
			next();
			
			if (lexer.token == Token.COMMA) {
				next();
			}
			else {
				break;
			}
		}
		
		return paramList;
	}


	private SymbolTable symbolTable;
	private Lexer lexer;
	private ErrorSignaller signalError;
	private Boolean haveRun;
	private TypeCianetoClass currentClass;
	private boolean haveReturn;
	private MethodDec currentMethod;
	private boolean isReadExpr;
	private MethodDec isMethod;
	private boolean isValid;
	private boolean  isLoop;
}
