all:
	javac *.java
clean:
	/bin/rm *.class
run:
	java ChessGame
test:
	java ChessGame unittest
