all:
	javac -encoding cp1252 src/ast/*.java src/comp/*.java src/lexer/*.java

report-student-tests:
	java -classpath ./src comp.Comp ./student-made-tests

test:
	java -classpath ./src comp.Comp ./

clean:
	rm src/ast/*.class src/comp/*.class src/lexer/*.class
	