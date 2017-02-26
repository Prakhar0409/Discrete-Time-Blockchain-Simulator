import java.sql.Timestamp;

public class Event implements Comparable<Event>{

	//eventType is receiveBlock = 1, generateBlock = 2, receiveTransaction = 3, generateTransaction = 4.
	private int eventType;
	private Block eventBlock=null;
	private int creatorNum; //Used only in case of block events handling
	private int senderNum; 	//id of the node which forwards this transaction
	private int receiverNum;
	private Transaction eventTransaction = null;
	private Timestamp eventTimestamp ;
	private boolean executed = false;

	//constructors to create various types of events
	Event(int eventType, Block eventBlock, Timestamp eventTimestamp, int creatorNum){
		this.eventType = eventType;
		this.eventBlock = eventBlock;
		this.eventTimestamp = eventTimestamp;
		this.creatorNum = creatorNum;
	}

	Event(int eventType, Block eventBlock, Timestamp eventTimestamp, int receiverNum, int senderNum){
		this.eventType = eventType;
		this.eventBlock = eventBlock;
		this.eventTimestamp = eventTimestamp;
		this.receiverNum = receiverNum;
		this.senderNum = senderNum;
	}

	Event(int eventType, Transaction eventTransaction, Timestamp eventTimestamp){
		this.eventType = eventType;
		this.eventTransaction = eventTransaction;
		this.eventTimestamp = eventTimestamp;
	}

	Event(int eventType, Transaction eventTransaction, Timestamp eventTimestamp, int receiverNum, int senderNum){
		this.eventType = eventType;
		this.eventTransaction = eventTransaction;
		this.eventTimestamp = eventTimestamp;
		this.receiverNum = receiverNum;
		this.senderNum = senderNum;
	}

	//Creating a function to compare events
	public int compareTo(Event otherEvent){
		if(this.eventTimestamp.before(otherEvent.getEventTimestamp())){
			return -1;
		}
		else if(this.eventTimestamp.after(otherEvent.getEventTimestamp())){
			return 1;
		}
		else{
			return 0;
		}
	}

	//function to update senderNum
	public void updateSender(int newSenderNum){
		this.senderNum = newSenderNum;
	}

	//function to update receiverNum
	public void updateReceiver(int newReceiverNum){
		this.receiverNum = newReceiverNum;
	}

	//function to retrieve event timestamp
	public Timestamp getEventTimestamp(){
		return eventTimestamp;
	}

	//function to retrieve event type
	public int getEventType(){
		return eventType;
	}

	//function to retrieve event status i.e whether that is being already executed or not
	public boolean getEventStatus(){
		return executed;
	}

	//function to retrieve event block corresponding to it
	public Block getEventBlock(){
		return eventBlock;
	}

	//function to retrieve event transaction corresponding to it
	public Transaction getEventTransaction(){
		return eventTransaction;
	}

	public void changeEventStatus(){
		this.executed = true;
	}

	//return id of the node to whom it is forwarded
	public int getReceiverNum(){
		return receiverNum;
	}

	//num of the node which forwarded this
	public int getSenderNum(){
		return senderNum;
	}

	//id number of the creator of the block is returned
	public int getCreatorNum(){
		return this.creatorNum;
	}
}