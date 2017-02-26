import java.sql.Timestamp;
import java.util.Random;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.HashMap;
import java.io.IOException;
import java.io.PrintWriter;

public class Simulator{
	public static void main(String[] args){
		try{

			PrintWriter transactionHistory = new PrintWriter("transactionHistory.txt", "UTF-8");
			PrintWriter blockChainHistory = new PrintWriter("blockChainHistory.txt", "UTF-8");
			int numPeers = Integer.parseInt(args[0]);
			double minPropDelay = 10;
			double maxPropDelay = 500;
			double qDelayParameter = 12.0/1024.0;
			ArrayList<Node> nodeList = new ArrayList<Node>();

			//Genesys Block
			Timestamp genesisTime = new Timestamp(System.currentTimeMillis());
			Block genesisBlock = new Block("genesis", genesisTime);

			//Generating numPeers number of nodes with randomly choosing fast and slow property
			//type true for fast nodes and false for lazy nodes
			Boolean[] nodeTypes = new Boolean[numPeers];
			Random randType = new Random(System.nanoTime());
			for(int i=0; i<numPeers; i++){
				String nodeID = "Node_"+i;
				boolean type = (randType.nextInt()%2==0);
				nodeTypes[i] = type;
				Timestamp creationTime = new Timestamp(System.currentTimeMillis());
				Node newNode = new Node(nodeID, type, creationTime, genesisBlock);
				nodeList.add(i,newNode);
			}

			//to create a connencted graph with each node connected to a random number of other nodes
			Boolean[][] connectionArray = new Boolean[numPeers][numPeers];
			for(int i = 0; i<numPeers; i++){
				for(int j = 0; j<numPeers; j++){
					connectionArray[i][j]=false;
				}
			}
			Random connRand = new Random(System.nanoTime());
			int n1Num = connRand.nextInt(numPeers);
			int numNode = 0;
			Boolean[] tempConnection = new Boolean[numPeers];
			for(int i = 0; i<numPeers; i++){
				tempConnection[i] = false;
			}
			
			tempConnection[n1Num] = true;
			int newNum = connRand.nextInt(numPeers);
			while(tempConnection[newNum]){
				newNum = connRand.nextInt(numPeers);
			}
			tempConnection[newNum] = true;
			connectionArray[n1Num][newNum] = true;
			connectionArray[newNum][n1Num] = true;
			numNode++;

			while (numNode <= numPeers){
				newNum = connRand.nextInt(numPeers);
				while(tempConnection[newNum]){
					newNum = connRand.nextInt(numPeers);
				}
				int oldNum = connRand.nextInt(numPeers);
				while(!tempConnection[oldNum]){
					oldNum = connRand.nextInt(numPeers);
				}

				connectionArray[oldNum][newNum] = true;
				connectionArray[newNum][oldNum] = true;
				numNode++;
			}

			int maxRemainingEdges = ((numPeers-1)*(numPeers-2))/2;
			int remainingEdges = connRand.nextInt()%maxRemainingEdges;
			while(remainingEdges>0){
				int i = connRand.nextInt(numPeers);
				int j = connRand.nextInt(numPeers);
				if(!connectionArray[i][j]){
					connectionArray[i][j] = true;
					connectionArray[j][i] = true;
					remainingEdges--;
				}
			}

			//Creating a 2D array to store Propagation delay between each pair of nodes
			Double[][] propagationDelay  = new Double[numPeers][numPeers];
			Random randProp = new Random(System.nanoTime());
			for(int i=0; i<numPeers; i++){			
				for(int j=0; j<numPeers; j++){
					if(i<=j){					
						boolean makeConnection = connectionArray[i][j];
						if(makeConnection){
							nodeList.get(i).addNode(nodeList.get(j));
							double propDelay = minPropDelay + randProp.nextDouble()*(maxPropDelay - minPropDelay);
							propagationDelay[i][j] = propDelay;
						}
						
					}
					else{
						//To mantain the symmetry of the propagation delay
						if(propagationDelay[j][i]!= null){
							nodeList.get(i).addNode(nodeList.get(j));
							propagationDelay[i][j] = propagationDelay[j][i];	
						}					
					}
					
				}
				// System.out.println("Number of connected nodes of "+i+ "is" + );
			}

			//Creating a array to store the bottle neck link between each pair of nodes
			Double[][] bottleNeck = new Double[numPeers][numPeers];
			for(int i=0; i<numPeers; i++){
				for(int j=0; j<numPeers; j++){
					if(connectionArray[i][j]){
						if(nodeList.get(i).getType() && nodeList.get(j).getType())
							bottleNeck[i][j] = 100.0;
						else
							bottleNeck[i][j] = 5.0;
					}								
				}
			}

			//Assigning mean to generate T_k later for each node from an exponential distribution
			Double[] cpuPower = new Double[numPeers];
			Random randCpu = new Random(System.nanoTime());
			for(int i=0; i<numPeers; i++){
				double cpuMean = 10 + randCpu.nextDouble()*1;
				cpuPower[i] = 1/cpuMean;
			}


			//Assigning mean to generate transaction later for each node from an exponential distribution
			Double[] txnMean = new Double[numPeers];
			Random randMean = new Random(System.nanoTime());
			for(int i=0; i<numPeers; i++){
				double tempTxnMean = 50 + randMean.nextDouble()*50;
				txnMean[i] = 1/tempTxnMean;
			}

			//Priortiy Queue of events to be executed and finished
			PriorityQueue<Event> pendingEvents = new PriorityQueue<Event>();
			PriorityQueue<Event> finishedEvents = new PriorityQueue<Event>();

			long simTime = 1000*1000;
			Timestamp currTime = new Timestamp(System.currentTimeMillis());
			// Timestamp startTime = currTime;
			long currTimeOffset = currTime.getTime();
			Timestamp maxTime = new Timestamp(currTimeOffset + (long)(Math.random()*simTime));

			//Every node here tries to generate a block on the genesis block
			for(int i=0; i<numPeers; i++){
				Random randBlock = new Random(System.nanoTime());
				double  nextTimeOffset = randBlock.nextDouble();
				while(nextTimeOffset == 0.0){
					nextTimeOffset = randBlock.nextDouble();
				}
				double nextTimeGap = -1*Math.log(nextTimeOffset)/cpuPower[i];
				Timestamp nextBlockTime = new Timestamp(currTimeOffset + (long)nextTimeGap*1000);
				//register a new block generation event
				Block newBlock = nodeList.get(i).generateBlock(genesisBlock, nextBlockTime);
				Event newEvent = new Event(2, newBlock, nextBlockTime, i);
				nodeList.get(i).nextBlockTime = nextBlockTime;
				pendingEvents.add(newEvent);
			}

			//To generate initial set of transactions to start the simulator
			for(int i=0; i<numPeers; i++){

				// long nextTxnLong = (long)(10000*txnMean[i]);
				Random randNext = new Random(System.nanoTime());
				double nextTimeOffset = randNext.nextDouble();
				while (nextTimeOffset == 0.0){
					nextTimeOffset = randNext.nextFloat();
				}
				double nextTimeGap = -1*Math.log(nextTimeOffset)/txnMean[i];
				Timestamp nextTxnTime = new Timestamp(currTimeOffset + (long)nextTimeGap*100);
				nodeList.get(i).nextTxnTime = nextTxnTime;
				Random receiveRand = new Random(System.nanoTime());
				int rcvNum = receiveRand.nextInt(numPeers);
				while(rcvNum == i){
					rcvNum = receiveRand.nextInt(numPeers);
				}
				String receiverID = nodeList.get(rcvNum).getUID();
				float receivedAmount = 0 ;
				Transaction newTransaction = nodeList.get(i).generateTxn(receiverID, receivedAmount, nextTxnTime);
				//register generate transcation event
				Event newEvent = new Event(4, newTransaction, nextTxnTime);
				pendingEvents.add(newEvent);
			}

			//Timestamp of the next event to be executed
			Timestamp nextEventTime = pendingEvents.peek().getEventTimestamp();
			Iterator<Event> eventItr = pendingEvents.iterator();
			while(nextEventTime.before(maxTime)){			
				if(eventItr.hasNext()){
					Event nextEvent = pendingEvents.poll();
					finishedEvents.add(nextEvent);

					if(nextEvent.getEventType()==1){
						//Code to execute receive Block event
						int currentNum = nextEvent.getReceiverNum();
						int creatorNum = nextEvent.getCreatorNum();
						Node currentNode = nodeList.get(currentNum);

						Block tmpBlock = nextEvent.getEventBlock();
	//					Block currentBlock = new Block(tmpBlock);

						Block currentBlock = nextEvent.getEventBlock();
						String currentBlockID = currentBlock.getBlockID();
						if(!currentNode.checkForwarded(currentBlockID)){

							nodeList.get(currentNum).addForwarded(currentBlockID);
							boolean addBlockSuccess = nodeList.get(currentNum).addBlock(currentBlock);
							
							if(addBlockSuccess){
								//check if any pending blocks can be added
								nodeList.get(currentNum).addPendingBlocks();
							}
							int currentDepth = nodeList.get(currentNum).probParentBlock.getDepth();
							int blockDepth = currentBlock.getDepth();						

							if(blockDepth > currentDepth){
								//updating the probable parent block
								nodeList.get(currentNum).probParentBlock = currentBlock;
								nodeList.get(creatorNum).calculateBTC();

								//to Generate the next transaction for the sending node
								Random randNext = new Random(System.nanoTime());
								double nextTimeOffset = randNext.nextDouble();
								while (nextTimeOffset == 0.0){
									nextTimeOffset = randNext.nextDouble();
								}
								double nextTimeGap = -1*Math.log(nextTimeOffset)/cpuPower[currentNum];
								Timestamp newBlockTime = new Timestamp(nextEventTime.getTime() + (long)nextTimeGap*1000);
								nodeList.get(currentNum).nextBlockTime = newBlockTime;
								Block newBlock = nodeList.get(currentNum).generateBlock(currentBlock, newBlockTime);
								Event newEvent = new Event(2, newBlock, newBlockTime, currentNum);
								pendingEvents.add(newEvent);			
							}

							for(int i=0; i<numPeers; i++){
								Node nextNode = currentNode.getNode(i);
								if(nextNode == null){
									break;
								}
								else{									
									int nextNodeNum = Integer.parseInt(nextNode.getUID().split("_")[1]);
									Random queingRandom = new Random(System.nanoTime());
									float qDelayP1 = queingRandom.nextFloat();
									while (qDelayP1 == 0.0){
										qDelayP1 = queingRandom.nextFloat();
									}
									long qDelay = (long)((-1*Math.log(qDelayP1)*bottleNeck[currentNum][nextNodeNum])/qDelayParameter);
									long pDelay = Math.round(propagationDelay[currentNum][nextNodeNum]);
									long msgDelay = 0;
									if(bottleNeck[creatorNum][nextNodeNum]!=null){
										msgDelay = Math.round(1000.0/bottleNeck[creatorNum][nextNodeNum]);
									}
									Timestamp receiveTime = new Timestamp(nextEventTime.getTime()+ qDelay + pDelay + msgDelay);									
									Event newEvent = new Event(1, currentBlock, receiveTime, nextNodeNum, currentNum);
									pendingEvents.add(newEvent);
								}							
							}
							//Timestamp of the next event to be executed
							blockChainHistory.println("Block received "+currentBlockID+" at depth "+ currentBlock.getDepth() +" by "+ currentNum);
							nextEventTime = pendingEvents.peek().getEventTimestamp();
						}
					}
					else if(nextEvent.getEventType()==2){
						//Code to execute generate Block
						int creatorNum = nextEvent.getCreatorNum();
						Node currentNode = nodeList.get(creatorNum);
						Block currentBlock = nextEvent.getEventBlock();
						Timestamp nextBlockTime = currentNode.nextBlockTime;
										
						if(!(nextBlockTime.after(nextEventTime) || nextBlockTime.before(nextEventTime))){ //Only execute this if the node still decides to execute it
	
	//						System.out.println("heere");
							//mining fee transaction 
							Transaction mfee = new Transaction(currentNode.getUID()+"_mining_fee","god",currentNode.getUID(),50,new Timestamp(System.currentTimeMillis()));
							currentBlock.addTxn(mfee);
							
							//change block to include transactions
							Block parent = currentNode.probParentBlock;
							for(int i=0;i<currentNode.allTxns.size();i++){
								boolean alreadyIncluded = false;
								Transaction tmpTxn = currentNode.allTxns.get(i);
								
								//check block validity
								if(!currentNode.checkValid(tmpTxn)){
									continue;	//continue if invalid. It can turn valid after some time.
								}
								while(parent!=null){
									if(parent.txnList.contains(tmpTxn)){
										System.out.println("Txn: "+tmpTxn.getTxnID()+" failed");
										alreadyIncluded = true;
										break;
									}
									parent = parent.getParentBlock();
								}
								if(!alreadyIncluded){
									currentBlock.addTxn(tmpTxn);
								}
							}
							//end of adding pending transaction to the new block
	//						System.out.println("ttttttheere");

							nodeList.get(creatorNum).addForwarded(currentBlock.getBlockID());
							boolean addBlockSuccess = nodeList.get(creatorNum).addBlock(currentBlock);
							nodeList.get(creatorNum).probParentBlock = currentBlock;
							nodeList.get(creatorNum).calculateBTC();
							if(addBlockSuccess){
								blockChainHistory.println("Node "+creatorNum+" created Block "+currentBlock.getBlockID()+ " at Depth "+ currentBlock.getDepth() + " ON "+currentBlock.getParentBlockID());
								for(int i=0; i<numPeers; i++){
									Node nextNode = currentNode.getNode(i);
									if(nextNode == null){
										break;
									}
									else{									
										int nextNodeNum = Integer.parseInt(nextNode.getUID().split("_")[1]);
										Random queingRandom = new Random(System.nanoTime());
										float qDelayP1 = queingRandom.nextFloat();
										while (qDelayP1 == 0.0){
											qDelayP1 = queingRandom.nextFloat();
										}
										long qDelay = (long)((-1*Math.log(qDelayP1)*bottleNeck[creatorNum][nextNodeNum])/qDelayParameter);
										long pDelay = Math.round(propagationDelay[creatorNum][nextNodeNum]);
										long msgDelay = 0;
										if(bottleNeck[creatorNum][nextNodeNum]!=null){
											msgDelay = Math.round(1000.0/bottleNeck[creatorNum][nextNodeNum]);
										}
										Timestamp receiveTime = new Timestamp(nextEventTime.getTime()+ qDelay + pDelay+msgDelay);									
										Event newEvent = new Event(1, currentBlock, receiveTime, nextNodeNum, creatorNum);
										pendingEvents.add(newEvent);

									}							
								}						
							}
							//to Generate the next transaction for the sending node
							Random randNext = new Random(System.nanoTime());
							double nextTimeOffset = randNext.nextDouble();
							while (nextTimeOffset == 0.0){
								nextTimeOffset = randNext.nextDouble();
							}
							double nextTimeGap = -1*Math.log(nextTimeOffset)/cpuPower[creatorNum];
							Timestamp newBlockTime = new Timestamp(nextEventTime.getTime() + (long)nextTimeGap*1000);

							Block newBlock = nodeList.get(creatorNum).generateBlock(currentBlock, newBlockTime);
							Event newEvent = new Event(2, newBlock, newBlockTime, creatorNum);
							pendingEvents.add(newEvent);
						}
						//Updating the time to execute next evnet
						nextEventTime = pendingEvents.peek().getEventTimestamp();									
					}
					else if(nextEvent.getEventType()==3){

						//Code to execute receive Transaction					
						int receiverNum = nextEvent.getReceiverNum();
						int senderNum = nextEvent.getSenderNum();
						Node tempSenderNode = nodeList.get(receiverNum);
						Transaction newTxn = nextEvent.getEventTransaction();
						String newTxnID = newTxn.getTxnID();
						if(!(tempSenderNode.checkForwarded(newTxnID))){//Only execute if it has not already forwarded the same transaction earlier
													
							//add transactions to allTxns list 
							Node currNode = nodeList.get(receiverNum);
							if(!currNode.allTxns.contains(newTxn)){
								currNode.allTxns.add(newTxn);
							}
							//end
							
							int txnReceiverNum = Integer.parseInt((newTxn.getReceiverID()).split("_")[1]);
							transactionHistory.print("Transaction Id "+ newTxnID+" Money receiver :"+txnReceiverNum+" "+"Message Receiver :"+receiverNum);
							if(txnReceiverNum == receiverNum){ //checking the transaction is meant for that node or not
								boolean addReceiveSuccess = nodeList.get(receiverNum).addTxn(newTxn);
								transactionHistory.print(" Money Added!!");						
							}

							transactionHistory.println();
							nodeList.get(receiverNum).addForwarded(newTxnID);
							for(int i=0; i<numPeers; i++){
								Node nextNode = tempSenderNode.getNode(i);							
								if(nextNode == null){
									break;
								}							
								else{	
									int nextNodeNum = Integer.parseInt(nextNode.getUID().split("_")[1]);
									if (nextNodeNum != senderNum){

										Random queingRandom = new Random(System.nanoTime());
										double qDelayP1 = queingRandom.nextDouble();
										while (qDelayP1 == 0.0){
											qDelayP1 = queingRandom.nextFloat();
										}
										long qDelay = (long)((-1*Math.log(qDelayP1)*bottleNeck[nextNodeNum][receiverNum])/qDelayParameter);
										// System.out.println(qDelay);
										long pDelay = Math.round(propagationDelay[receiverNum][nextNodeNum]);
										Timestamp receiveTime = new Timestamp(nextEventTime.getTime()+ qDelay + pDelay);
										Event newEvent = new Event(3, newTxn, receiveTime, nextNodeNum, receiverNum);
										pendingEvents.add(newEvent);
									}
								}
							}				
							
							//Timestamp of the next event to be executed
							nextEventTime = nextEvent.getEventTimestamp();
						}
						
					}
					else if(nextEvent.getEventType()==4){

						//Code to handle generate Transaction event
						Transaction newTxn = nextEvent.getEventTransaction();
						String senderID = newTxn.getSenderID();
						int senderNum = Integer.parseInt(senderID.split("_")[1]);

						//Adding a temporary node to enhance efficiency
						Node tempSenderNode = nodeList.get(senderNum);

						//random to generate an amount for the transaction
						Random updateRand = new Random(System.nanoTime());
						float newAmount = updateRand.nextFloat()*tempSenderNode.getCurrOwned();
						newTxn.updateAmount(newAmount);
						// System.out.print("b: "+tempSenderNode.getCurrOwned()+" ");

						
						//add transactions to allTxns list 
						Node currNode = nodeList.get(senderNum);
						if(!currNode.allTxns.contains(newTxn)){
							currNode.allTxns.add(newTxn);
						}
						//end
						
						//Adding the transaction at the sender end.
						boolean addTxnSuccess = nodeList.get(senderNum).addTxn(newTxn);
						nodeList.get(senderNum).addForwarded(newTxn.getTxnID());
						if(addTxnSuccess){			//proceeding only when the transaction is successfully added
							if (newAmount!=0){
								transactionHistory.println(senderID + " sents " + newTxn.getAmount()+ " to " + newTxn.getReceiverID()+" a: "+ nodeList.get(senderNum).getCurrOwned());
								for(int i=0; i<numPeers; i++){
									Node nextNode = tempSenderNode.getNode(i);
									if(nextNode == null){
										break;
									}
									else{
										int nextNodeNum = Integer.parseInt(nextNode.getUID().split("_")[1]);

										Random queingRandom = new Random(System.nanoTime());
										float qDelayP1 = queingRandom.nextFloat();
										while (qDelayP1 == 0.0){
											qDelayP1 = queingRandom.nextFloat();
										}
										long qDelay = (long)((-1*Math.log(qDelayP1)*bottleNeck[senderNum][nextNodeNum])/qDelayParameter);
										long pDelay = Math.round(propagationDelay[senderNum][nextNodeNum]);
										Timestamp receiveTime = new Timestamp(nextEventTime.getTime()+ qDelay + pDelay);
										Event newEvent = new Event(3, newTxn, receiveTime, nextNodeNum, senderNum);
										pendingEvents.add(newEvent);

									}								
								}
							}						

							//to Generate the next transaction for the sending node
							Random randNext = new Random();
							double nextTimeOffset = randNext.nextDouble();
							while (nextTimeOffset == 0.0){
								nextTimeOffset = randNext.nextFloat();
							}
							double nextTimeGap = -1*Math.log(nextTimeOffset)/txnMean[senderNum];
							Timestamp nextTxnTime = new Timestamp(nextEventTime.getTime() + (long)nextTimeGap*1000);

							nodeList.get(senderNum).nextTxnTime = nextTxnTime;
							// System.out.println(nextTxnTime);
							Random receiveRand = new Random(System.nanoTime());
							int rcvNum = receiveRand.nextInt(numPeers);
							while(rcvNum == senderNum){
								rcvNum = receiveRand.nextInt(numPeers);
							}

							String receiverID = nodeList.get(rcvNum).getUID();
							float receivedAmount = 0;

							Transaction newTransaction = nodeList.get(senderNum).generateTxn(receiverID, receivedAmount, nextTxnTime);
							Event newEvent = new Event(4, newTransaction, nextTxnTime);
							pendingEvents.add(newEvent);

							//Updating the time to execute next evnet
							nextEventTime = pendingEvents.peek().getEventTimestamp();
							// System.out.println(nextEventTime);
						}
						else{
							System.out.println("Add Transaction Failed!!");
						}	
					}
					else{
						System.out.println("Error: Wrong Eventtype Detected.");
					}
				}
				else{

				}
			}

			double sum = 0;
			for(int i=0; i<numPeers; i++){
				float value = nodeList.get(i).getCurrOwned();
				sum = sum + value;
				transactionHistory.println(value);
			}
			transactionHistory.println("Total :"+sum);

			transactionHistory.close();
			blockChainHistory.close();

			for(int i=0; i<numPeers; i++){
				HashMap<String, Block> tempBlockChain = nodeList.get(i).blockChain;
				String root = "genesis";
				String fileName = "file_"+i+".txt";
				String vizFileNmae = "viz_"+i+".csv";
				try{
					PrintWriter writer = new PrintWriter(fileName,"UTF-8");
					writer.println("Node "+i+", Details:");
					writer.println("Type : "+(nodeTypes[i]?"fast":"slow"));
					writer.println("CPU power : "+cpuPower[i]);
					writer.println("Connected to :");
					for(int j=0; j<numPeers; j++){
						if(connectionArray[i][j]){
							writer.println("NID: "+ nodeList.get(j).getUID() +", PD: "+ propagationDelay[i][j] +", BS: "+ bottleNeck[i][j]);
						}
					}
					writer.println("\nStored Tree:");

					printTree(writer ,root, tempBlockChain);
					writer.close();
				}
				catch (IOException e){
					e.printStackTrace();
				}			
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public static void printTree(PrintWriter writer,String root, HashMap<String, Block> blockChain){		
		Block rootBlock = blockChain.get(root);
		if(rootBlock != null){
			ArrayList<String> childList = blockChain.get(root).getChildList();
			int childListSize = childList.size();
			int i = 0;
			while(i<childListSize){
				String newRoot = childList.get(i);
				printTree(writer, newRoot, blockChain);
				i++;
			}
			Block parent = blockChain.get(root).getParentBlock();
			if(parent != null){
				writer.println(root+","+parent.getBlockID());
			}					
		}
	}
}