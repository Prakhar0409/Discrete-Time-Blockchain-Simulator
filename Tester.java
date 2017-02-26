import java.sql.Timestamp;
import java.util.Random;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.HashMap;


public class Tester {
	
	public static void main(String[] args){
		Timestamp tp = new Timestamp(System.currentTimeMillis());
		Block b = new Block("genesis", tp);
		Node n1 = new Node("A",true,tp,b);
//		Node n2 = new Node("B",true,tp,b);
		n1.addBlock(b);
		
//		n1.testPrintAll();
//		n2.testPrintAll();
//		n1.testChange();
//		n1.testPrintAll();
//		n2.testPrintAll();
	}
	
}
