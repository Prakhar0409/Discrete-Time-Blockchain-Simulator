all:
	javac Transaction.java
	javac Node.java
	javac Event.java
	javac Block.java
	javac TreeNode.java
	javac Utilities.java
	javac Simulator.java	
clean:
	rm *.class