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
			}
			catch ( RuntimeException e ) {
				//e.printStackTrace();
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
			try {
				this.signalError.showError("Every program must have a class named 'Program' with a public parameterless method called 'run'", true);
			}
			catch( CompilerError e) {
			}
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

	private boolean isNull(Object obj){
		if(obj == null){
			return true;
		}
		return false;
	}

	private TypeCianetoClass classDec() {
	
		boolean isInheritable = false;
		String superclassName = null;
		TypeCianetoClass superclass = null;
		if (lexer.getStringValue().equals("open") && lexer.token == Token.ID) {
			isInheritable = true;
			next();
		}
		check(Token.CLASS, "'class' expected");
		next();
		check(Token.ID, "Identifier expected");

		String className = lexer.getStringValue();

		if (!isNull(symbolTable.getInGlobal(className))) {
			error("Class " + className + " has already been declared");
		}

		next();
		
		if ( lexer.token == Token.EXTENDS ) {
			next();	
			check(Token.ID, "Class expected");
			
			//semantic

			superclassName = lexer.getStringValue();
			superclass = (TypeCianetoClass) symbolTable.getInGlobal(superclassName);
		
			if (!isNull(superclass)) {
				//verify if the superclass is inheritable
				if (!superclass.getInheritable()) {
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
			} while(!isNull(aux));
			next();
		}


		TypeCianetoClass typeCianetoClass = new TypeCianetoClass(className, superclass, isInheritable); 
		
		currentClass = typeCianetoClass;
       
		symbolTable.putInGlobal(className, typeCianetoClass);
		
		//return all members os class
		ArrayList<Member> ml = memberList();
		currentClass.setMemberList(ml);

		if (isNull(ml)|| lexer.token != Token.END) {
			error(" Class member OR 'end' expected");
		}

		if (currentClass.getName().equals("Program")) {
			boolean flag  = false;
			for (Member m : ml) {
				if (m instanceof MethodDec && ((MethodDec) m).getMethodName().equals("run")) {
					flag = true;
				}
			}
			
			if (!flag) {
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
				try {
					ml.add(methodDec(q));
				} catch (Exception e) {
					e.printStackTrace();
				}
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
				if (!isNull(symbolTable.getInLocal(p.getName()))) {
					error("variavel" + p.getName() + "' ja foi declarada");
				}
				symbolTable.putInLocal(p.getName(), p);
			}
		} else {
			error("An identifier or identifer: was expected after 'func'");
		}
		
		Object obj = symbolTable.getInClass(id);

		if (!isNull(obj)) {
			error("Method '"+ id + "' is being redeclared");
		}
		
		if ( lexer.token == Token.MINUS_GT ) {
			next();
			returnType = type();
		}
		
		MethodDec method = (MethodDec) symbolTable.getInSuperClassTable(id);

		if(q.override()) {
 
			if(isNull(method)) {
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
		} else if (!isNull(method)) {
			error("'override' expected before overridden method");
		}
		check(Token.LEFTCURBRACKET,"'{' expected" );
		
		MethodDec m = new MethodDec(id, paramList, returnType, q);
		
		if (currentClass.getName().equals("Program")) {
			if (m.getMethodName().equals("run:") || m.getMethodName().equals("run")) {
				if (m.getQualifier().getToken1() == Token.PUBLIC) {
					if (!isNull(m.getReturnType()) || !m.getParamList().isEmpty()) {
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
	
		check(Token.RIGHTCURBRACKET,"'}' expected" );
		
		if(!isNull(returnType)){
			if(!haveReturn){
				error("Missing 'return' statement in method '" + currentMethod.getMethodName() + "'");
			}
		}
        
		next();
		symbolTable.removeLocalIdent();
        
		return m;
	}

	private ArrayList<Statement> statementList() {	
	
		ArrayList<Statement> listStmt = new ArrayList<>();
		while ( lexer.token != Token.UNTIL && lexer.token != Token.RIGHTCURBRACKET && lexer.token != Token.END ) {
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
		case OUT:
				stmt = printStat();
		break;
		default:
				stmt = assignExpr();
		}
		
		if ( checkSemiColon ) {
			check(Token.SEMICOLON, "';' expected");
			next();
		}
		
		return stmt;
	}

	private AssignExpr assignExpr() {
		AssignExpr a = null;
		isValid = false;
		isMethod = null;
		isReadExpr = false;
		Expr left = expr();
		if (isNull(left)) {
			error("Expression expected");
		}
    
		Expr right = null;
		if (lexer.token == Token.ASSIGN) {
			next();
			right = expr();
			if (isNull(right)) {
				error("Expression expected");			
			}
			if (!checkType(left.getType(), right.getType())) {
				error("Type error: the type of the expression of the right-hand side is a basic type and the type of the variable of the left-hand side is a class");
			}else if(!isValid) {
				error("'operator expected' or 'variable expected at the left-hand side of a assignment'");
			}
		} else {
			if (isNull(isMethod) && !isReadExpr) {
				error("Expression expected");
			} else {
				if (!isNull(isMethod) && !isNull(isMethod.getReturnType())) {
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

		do {
			Variable v = new Variable(lexer.getStringValue(), t);
			
			if (!isNull(symbolTable.getInLocal(v.getName()))) {
				error("Variable '" + v.getName() + "' is being redeclared");
			}

			symbolTable.putInLocal(v.getName(), v);
			idList.add(v);
			next();
								
			if ( lexer.token == Token.COMMA ) {
				next();
				check(Token.ID, "Missing identifier");
			}
		} while ( lexer.token == Token.ID );
		
		if ( lexer.token == Token.ASSIGN ) {
			next();
			e = expr();
			if (isNull(e)) {
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
		
		if (isNull(e)) {
			error("Repeat expression expected");
		}
		
		if (e.getType() != Type.booleanType) {
			error("boolean expression expected in a repeat-until statement");
		}
		
		return new RepeatStat(statList, e);
	}

	private BreakStat breakStat() {
		if(!isLoop) {
			error("'break' statement found outside a 'while' or 'repeat-until' statement");
		}
		next();
		return new BreakStat();
	}

	private ReturnStat returnStat() {
		next();
		Expr e = expr();
		if (isNull(e)) {
			error("Return expression expected");
		}
		if (isNull(currentMethod.getReturnType())) {
			error("Illegal 'return' statement. Method returns 'void'");
		}
		if (!checkType(currentMethod.getReturnType(), e.getType())) {
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
		if (isNull(e)) {
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
		if (isNull(e)) {
			error("If expression expected");
		}
		if (e.getType() != Type.booleanType) {
			error("Expression expected OR Unknown sequence of symbols");
		}
		
		ArrayList<Statement> ifPart = new ArrayList<>();
		ArrayList<Statement> elsePart = new ArrayList<>();
		
		check(Token.LEFTCURBRACKET, "'{' expected after the 'if' expression");
		next();
		
		while ( lexer.token != Token.END && lexer.token != Token.ELSE && lexer.token != Token.RIGHTCURBRACKET ) {
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

	private PrintStat printStat() {
		next();
		check(Token.DOT, "a '.' was expected after 'Out'");
		next();
		if(!lexer.getStringValue().equals("println:") && !lexer.getStringValue().equals("print:")) {
			error("'print:' or 'println:' was expected after 'Out.'");
		}
		next();

		ArrayList<Expr> exprList = new ArrayList<>();
		do {
			if (lexer.token == Token.COMMA) {
				next();
			}

			Expr e = expr();
			if (e.getType() != Type.stringType && e.getType() != Type.intType) {
				error("Attempt to print a boolean expression");
			}

			exprList.add(e);
		} while (lexer.token == Token.COMMA);
		
		if (exprList.isEmpty()) {
			error("Write expression expected");
		}
		
		return new PrintStat(exprList);
	}

	private Expr expr() {
		Expr left = simpleExpr();

		while (lexer.token == Token.GE || lexer.token == Token.EQ || lexer.token == Token.LT || lexer.token == Token.GT || 
			lexer.token == Token.LE || lexer.token == Token.NEQ) {
	
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
		while (lexer.getStringValue().equals("++")) {
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

				do {
						check(Token.ID, "Missing identifier");

            if(q.getToken1() != Token.PRIVATE) {
                error("Attempt to declare public instance variable '" + lexer.getStringValue() + "'");
            }

            Variable v = new Variable(lexer.getStringValue(), type, q);
            
            if (!isNull(symbolTable.getInClass(v.getName()))) {
                error("Variable '" + v.getName() + "' is being redeclared");
            }
            
            symbolTable.putInClass(v.getName(), v);
            fieldList.add(v);
            next();
				}	while (lexer.token == Token.COMMA);	
        
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
			if (isNull(symbolTable.getInGlobal(lexer.getStringValue()))){
				error("Type '"+ lexer.getStringValue() +"' was not found");
			}
			TypeCianetoClass c = (TypeCianetoClass) symbolTable.getInGlobal(lexer.getStringValue());
			if (!isNull(c)) {
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
		}
		else if(q1 == Token.VAR){
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
		if (isNull(e)) {
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
				|| token == Token.LEFTPAR || token == Token.NIL
				|| token == Token.LITERALINT || token == Token.SUPER
				|| token == Token.NOT || token == Token.SELF
				|| token == Token.ID || token == Token.LITERALSTRING;

	}

	private boolean checkType(Type type1, Type type2) {
		if (isNull(type1)|| isNull(type2)) {
			return false;
		}
		if (type1 == type2) {
			return true;
		} 
		if ((type1 instanceof TypeCianetoClass && type2 instanceof TypeNil) || (type2 instanceof TypeCianetoClass && type1 instanceof TypeNil)) {
			return true;
		}
		if (type1.getClass() == type2.getClass()) {			
			if (type1 instanceof TypeCianetoClass) {
				TypeCianetoClass c = (TypeCianetoClass) type1;
				return c.isSubclass((TypeCianetoClass) type2);
			}
		}

		return (type1 == Type.stringType && type2 == Type.nilType) || (type2 == Type.stringType && type1 == Type.nilType); 
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
		
		if (isNull(right)) {
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
			check(Token.RIGHTPAR,"')' expected");
	
			if (isNull(e))
				return null;
			next();
			return new ExprFactor(e);
		} 
		if (lexer.token == Token.NOT) {
			next();
			PrimaryExpr f = (PrimaryExpr) factor();
			if (f.getType() != Type.booleanType) {
				error("Operator '!' does not accepts '" + f.getType().getName() + "' values");
			}
			f.setIsNegationAsTrue();
			return f;
		} 
		if (lexer.token == Token.NIL) {
			next();
			return new NilExpr(); 
		} 
		if (lexer.token == Token.LITERALINT || lexer.token == Token.LITERALSTRING || lexer.token == Token.TRUE || lexer.token == Token.FALSE) {
			return basicValue();
		}
		
		if (lexer.token == Token.SUPER) {
			return superFunc();
		} else if (lexer.token == Token.SELF){
			return auxSelf();
		} else if (lexer.token == Token.IN) {
			return readExpr();
		} else if (lexer.token == Token.ID) { 
			return auxId();
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
			if (isNull(currentClass.getSuperClass())) {
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
					if (!isNull(f)) {
						type = f.getType();
					} else {
						MethodDec m = searchMethod(currentClass.getSuperClass(), memberName);
						if (!isNull(m)) {	
							if (!m.getParamList().isEmpty()) {
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

					if (isNull(m)) {
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
			return new PrimaryExpr(type, "", null);
		} else {
			return null;
		}
	}

	private Factor auxId() {
		Type type = Type.undefinedType;
		String name = lexer.getStringValue();
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
					
					if (isNull(v)) {
						error("Variable '" + s + "' not declared");
					}
					TypeCianetoClass c = (TypeCianetoClass) symbolTable.getInGlobal(v.getType().getName());
					if (!isNull(c)) {
						Variable f = searchFields(c, memberName);
						if (!isNull(f)) {
							type = f.getType();
						} else {
							MethodDec m = searchMethod(c, memberName);
							if (!isNull(m)) {
								if (!m.getParamList().isEmpty()) {
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
					
					if (isNull(v)) {
						error("'" + s + "' not declared");
					}
					
					TypeCianetoClass c = (TypeCianetoClass) symbolTable.getInGlobal(v.getType().getName());
					
					if (!isNull(c)) {
						MethodDec m = searchMethod(c, methodName);
						if (isNull(m)) {
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
					name = v.getName();
					Variable v1 = (Variable) symbolTable.getInLocal(s);	
					if (isNull(v)) {
						error("Variable '" + s + "' not declared");
					}else if (v.getQualifier().getToken1() != Token.PUBLIC){
						error("self."+ s +" expected");
					}	
					if(isNull(v1)) {
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
			return new PrimaryExpr(type, name, isMethod);
		} else {
			return null;
		}
	}

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
						if(isNull(obj) || !(obj instanceof Variable)) {
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
						
						if(isNull(method)) {
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

				
						if (!isNull(f)) {
							type = f.getType();
							isValid = true;
						} else {
							MethodDec m = searchMethod(currentClass, memberName);
							if (!isNull(m)) {	
								if (!m.getParamList().isEmpty()) {
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
					if (isNull(m)) {
						m = searchMethod(currentClass.getSuperClass(), methodName);
					}
					if (isNull(m)) {
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
			return new PrimaryExpr(type, "", null);
		} else {
			return null;
		}
	}

	private ReadExpr readExpr() {
		next();
		check(Token.DOT, "dot expected");
		next();
		if (lexer.getStringValue().equals("readInt") || lexer.getStringValue().equals("readString")) {
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
		return null;
	}

	private Variable searchFields(TypeCianetoClass c, String fieldName) {
		Member field = (Member) symbolTable.getInClass(fieldName);
		if (!isNull(field) && field instanceof Variable) {
			return (Variable) field;
		}
		
		while (!isNull(c)) {	
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
		if (!isNull(method) && method instanceof MethodDec && c == currentClass)
			return (MethodDec) method;
		while (!isNull(c)) {
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
		if (isNull(e))
			error("Expression expected");
			
		exprList.add(e);
		while (lexer.token == Token.COMMA) {
			next();
			e = expr();
			if (isNull(e))
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
			check(Token.ID, "Identifier expected");

			paramList.add(new Variable(lexer.getStringValue(), t));		

			lexer.nextToken();

			if (lexer.token == Token.COMMA) {
				next();
			}
			else {
				break;
			}
		}
		return paramList;
	}

  public void p(String s){
		System.out.println(s);
	}

  public void pt(){
		System.out.println(lexer.token);
	}

	private TypeCianetoClass currentClass;
	private ErrorSignaller signalError;
	private SymbolTable symbolTable;
	private MethodDec currentMethod;
	private MethodDec isMethod;
	private Lexer lexer;
	private boolean haveRun;
	private boolean haveReturn;
	private boolean isReadExpr;
	private boolean isValid;
	private boolean  isLoop;
}