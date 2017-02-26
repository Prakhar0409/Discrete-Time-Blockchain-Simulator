import java.sql.Timestamp;
import java.util.ArrayList;

public class Block{

	private String uBlokckID;
	private Timestamp creationTime;
	private String creatorID;
	private Block parentBlock;
	private int depth;

	//This list contains all the transactions included in the block
	private ArrayList<String> childList = new ArrayList<String>();
	private int numChild = 0;
	public ArrayList<Transaction> txnList = new ArrayList<Transaction>();
	private int numTxns;

	Block(String uBlokckID, Timestamp creationTime, String creatorID, Block parentBlock, ArrayList<Transaction> txnList){
		this.uBlokckID = uBlokckID;
		this.creationTime = creationTime;
		this.creatorID = creatorID;
		this.parentBlock = parentBlock;
		this.depth = parentBlock.getDepth()+1;
		if(txnList!=null){
			this.txnList = txnList;
		}
		this.numTxns = txnList.size()-1;
	}

	Block(String uBlokckID, Timestamp creationTime){
		this.uBlokckID = uBlokckID;
		this.creationTime = creationTime;
		this.creatorID = "satoshi";
		this.parentBlock = null;
//		this.txnList = null;
		this.depth = 0;
	}

	//function to add txns to a block
	public void addTxn(Transaction newTxn){
		txnList.add(numTxns++,newTxn);
	}

	//returning a transaction from the block using correspondng transaction id
	public Transaction getTxn(String txnID){
		for(int i = 0; i<=numTxns; i++){
			if(txnList.get(i).getTxnID().equals(txnID)){
				return txnList.get(i);
			}
		}
		return null;
	}

	//to check whether a txn with particular id has been there in the list or not
	public boolean containsTxn(String txnID){
		for(int i = 0; i<=numTxns; i++){
			if(txnList.get(i).getTxnID().equals(txnID)){
				return true;
			}
		}
		return false;
	}

	//To store all list of the childIDs
	public ArrayList<String> getChildList(){
		return childList;
	}

	public void putChild(String newChildID){
		childList.add(numChild++, newChildID);
	}

	public boolean checkChild(String childID){
		for(int i=0; i<numChild; i++){
			if(childID.equals(childList.get(i))){
				return true;
			}
		}
		return false;
	}

	//to return block ID
	public String getBlockID(){
		return uBlokckID;
	}
	
	//to return parent Block
	public Block getParentBlock(){
		return parentBlock;
	}

	//to return block id of the parent node
	public String getParentBlockID(){
		return this.parentBlock.getBlockID();
	}

	//to return id of the creator of the block
	public String getCreatorID(){
		return this.creatorID;
	}
	
	//to set a new parent block
	public void setParentBlock(String newParentBlock){
		this.parentBlock = parentBlock;
	}

	//to return creation time of the block
	public Timestamp getCreationTime(){
		return creationTime;
	}

	//to get number of transaction in the Block
	public int getNumTxns(){
		return numTxns;
	}

	//to get the depth of the current block
	public int getDepth(){
		return this.depth;
	}

	//To check whether the block is genesys or not
	public boolean checkGenesis(){
		if(this.uBlokckID.equals("genesis")){
			return true;
		}
		return false;
	}	

	//Do not see any use of the follwing two function
	public void printBlock(String ident){
		System.out.println(ident+"Block UID:" + this.uBlokckID);
		System.out.println(ident+"Creation Time:" + this.creationTime);
		System.out.println(ident+"Creator ID:" + this.creatorID);
		System.out.println(ident+"Previous Block UID:" + this.parentBlock.getBlockID());
	}
	
	public boolean matchBlockID(String newID){
		if(this.uBlokckID.equals(newID)){
			return true;
		}
		return false;
	}
}