import java.sql.Timestamp;
public class Transaction{

	private String uTxnID;
	private String senderID;
	private String receiverID;
	private float amount;
	private Timestamp txnTime;
	private boolean isValid = true;

	//Default constructor
	Transaction(String uTxnID, String senderID, String receiverID, float amount, Timestamp txnTime){
		this.uTxnID = uTxnID;
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.amount = amount;
		this.txnTime = txnTime;
	}

	Transaction(String uTxnID, String creatorID, Timestamp txnTime){
		this.uTxnID = uTxnID;
		this.senderID = "Created";
		this.receiverID = creatorID;
		this.amount = 50;
		this.txnTime = txnTime;
	}

	//function to return unique transaction id
	public String getTxnID(){
		return uTxnID;
	}

	//to return transaction amount
	public float getAmount(){
		return amount;
	}

	//to return senderID
	public String getSenderID(){
		return senderID;
	}

	//to return recieverID
	public String getReceiverID(){
		return receiverID;
	}

	//to return time of transaction
	public Timestamp getTxnTime(){
		return txnTime;
	}

	//setting the transaction to false
	public void setFalse(){
		this.isValid = false;
	}

	//to return transaction status
	public  boolean getTxnStatus(){
		return isValid;
	}

	//to update the amount of transaction
	public void updateAmount(float newAmount){
		this.amount = newAmount;
	}

}