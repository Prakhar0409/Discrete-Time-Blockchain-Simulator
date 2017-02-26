import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Node{

	private boolean type; //type true for fast nodes and false for lazy nodes
	private String uID;
	private float currOwned;
	private Timestamp creationTime;
	private Block genesisBlock;

	//For the next set of events to executed by the node
	Timestamp nextTxnTime;
	Timestamp nextBlockTime;

	List<Transaction> allTxns = new ArrayList<Transaction>(); 
	
	LinkedList<Transaction> txnIncludePending  = new LinkedList<Transaction>();
	int numTxnIncludePending = 0;

	LinkedList<Transaction> totalTxnIncludePending = new LinkedList<Transaction>();
	int numTotalTxnIncludePending = 0;

	LinkedList<Block> blockIncludePending = new LinkedList<Block>();
	int numBlockIncludedPending = 0;

	//Varialbes to store information about longest chain received so far
	Block probParentBlock;
	// int currentDepth = 0;

	//Transaction details
	ArrayList<Transaction> receivedTxn = new ArrayList<Transaction>();
	int numReceivedTxn = 0;
	ArrayList<Transaction> sentTxn = new ArrayList<Transaction>();
	int numSentTxn = 0;

	//Connection Details
	LinkedList<Node> connectedNode = new LinkedList<Node>();
	int numConnection = 0;

	int numCreatedBlock = 0; //Number of block generated by this node so far

	//Tree to store all the blocks heard by the Node so far
	HashMap<String, Block> blockChain = new HashMap<String, Block>();

	//HashMap to store all the transactions forwarded by the node.
	HashMap<String, Boolean> forwardedMessage = new HashMap<String, Boolean>();

	//Default constructor
	Node(String uID, boolean type, Timestamp creationTime, Block genesisBlock){
		this.uID = uID;
		this.type = type;
		this.creationTime = creationTime;
		this.currOwned = 50;
		this.genesisBlock = genesisBlock;
		this.probParentBlock = genesisBlock;
		blockChain.put(genesisBlock.getBlockID(),genesisBlock);
	}

	//function to gerate block at a particular timestamp
	public Block generateBlock(Block parentBlock, Timestamp creationTime){
		
		String uBlockID = uID + "_B_" + numCreatedBlock;
		String txnID = "create_"+uID+"_"+creationTime;
		this.nextBlockTime = creationTime;
		Transaction createCoin = new Transaction(txnID, uID, creationTime);
		ArrayList<Transaction> txnList = new ArrayList<Transaction>();
		txnList.add(0,createCoin);
		this.probParentBlock = parentBlock;
		Block newBlock = new Block(uBlockID, creationTime, uID, parentBlock, txnList);
		return newBlock;
	}

	//Code to add a block in the node's block chain
	public boolean addBlock(Block newBlock){
		String parentBlockID = newBlock.getParentBlockID();
		String currentBlockID = newBlock.getBlockID();
		String creatorID = newBlock.getCreatorID();
		if(blockChain.containsKey(parentBlockID)){
			blockChain.put(currentBlockID, newBlock);
			if(!blockChain.get(parentBlockID).checkChild(currentBlockID)){
				blockChain.get(parentBlockID).putChild(currentBlockID);
				blockChain.put(currentBlockID, newBlock);
				if(this.uID.equals(creatorID)){
					numCreatedBlock++;
				}
				
				return true;
			}
		}else{
			if(!this.blockIncludePending.contains(newBlock)){
				this.blockIncludePending.add(newBlock);
			}
		}
		return false;
	}
	
	//adds pending blocks to the block chain
	public void addPendingBlocks(){
		int num_new = 1;
		while(num_new>0){
			num_new = 0;
			for(int i=0;i<this.blockIncludePending.size();i++){
				if(this.addBlock(this.blockIncludePending.get(i))){
					num_new++;
				}
			}
		}
	}

	public LinkedList<Transaction> getTxnIncludePending(){
		return this.txnIncludePending;
	}
	
	//function to generate a transaction
	Transaction generateTxn(String receiverID, float txnAmount, Timestamp txnTime){
		String txnID = uID + "_" + numSentTxn;
		Transaction newTxn = new Transaction(txnID, uID, receiverID, txnAmount, txnTime);
		return newTxn;
	}
	
	//function to add a new transaction to a node
	boolean addTxn(Transaction newTxn){
		if(newTxn.getSenderID().equals(this.uID)){			
			if(newTxn.getAmount()<=currOwned){
				//Add to sentTxn ArrayList
				sentTxn.add(numSentTxn, newTxn);
				currOwned = currOwned - newTxn.getAmount();
				numSentTxn++;
				this.numTotalTxnIncludePending++;
				this.totalTxnIncludePending.add(newTxn);
				return true;
			}
			else{
				return false;
			}
			
		}
		else if(newTxn.getReceiverID().equals(this.uID)){
			//Add to receivedTxn ArrayList
			receivedTxn.add(numReceivedTxn, newTxn);
			currOwned = currOwned + newTxn.getAmount();
			numReceivedTxn++;
			this.numTotalTxnIncludePending++;
			this.totalTxnIncludePending.add(newTxn);
			return true;
		}
		else{
			this.numTotalTxnIncludePending++;
			this.totalTxnIncludePending.add(newTxn);
			return true;
		}
	}

	//function to update pending include transaction list
	/*
	public void updateTxnIncludePending(LinkedList<Transaction> newList){
		txnIncludePending = new ArrayList<Transaction>();
		int newSize;
		if(newList != null)
			newSize = newList.size();
		else
			newSize = null;

		for(int j=0; j<numTotalTxnIncludedPending; j++){
			txnIncludePending.add(totalTxnIncludePending.get(i));
			numTxnIncludedPending++;
		}
					
		for(int j=0; j<numTotalTxnIncludedPending; j++){
			for(int i=0; i<newSize; i++){
				if(newList.get(i).getTxnID().equals(totalTxnIncludePending.get(j).getTxnID())){
					txnIncludePending.add(newList.get(i));
					numTxnIncludedPending++;
				}
			}
		}				
	}
	*/

	//to get the list 
	/*
	public LinkedList<Transaction> getTxnIncludePendingList(){		
		return this.txnIncludePending;
	}
	*/

	//Add Node to connected Nodes
	void addNode(Node newNode){
		connectedNode.add(newNode);
		numConnection++;
	}

	//to return current depth at which the node is working.
	// public int getCurrentDepth(){
	// 	return this.currentDepth;
	// }

	//userID return
	public String getUID(){
		return uID;
	}

	//type return
	public boolean getType(){
		return type;
	}

	//creationTime return
	public Timestamp getCreationTime(){
		return creationTime;
	}

	//userID return
	public float getCurrOwned(){
		return currOwned;
	}

	//to update the currently owned value
	public void updateCurrOwned(float newAmount){
		this.currOwned = newAmount;
	}

	//overwritting toString method for Node
	public String toString(){
		return "ID: "+this.uID+" type: "+ (this.type?"fast":"lazy") + " Creation time: "+this.creationTime  + " Balance: "+this.currOwned;
	}

	public Node getNode(int index){
		if(index >= numConnection){
			return null;
		}
		else{
			return connectedNode.get(index);
		}		
	}

	//Function to check given a transactionID whether that is already being forwarded or not
	public boolean checkForwarded(String newID){
		return (forwardedMessage.containsKey(newID));		
	}

	public void addForwarded(String newID){
		this.forwardedMessage.put(newID, true);
	}
}