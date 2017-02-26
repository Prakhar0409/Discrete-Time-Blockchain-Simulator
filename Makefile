all:
	javac Transaction.java
	javac Node.java
	javac Event.java
	javac Block.java
	javac TreeNode.java
	javac Utilities.java
	javac Simulator.java	

run3:
	java Simulator 3

clean:
	rm *.class