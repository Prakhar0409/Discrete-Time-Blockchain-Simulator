# Discrete Event Simulator for Bitcoin Network

## Get it up and Running
### Compiling
* Change directory to the source code directory.
* To compile 
> javac Simulator.java
### Run
* To run the Simlator program type the following:
> java Simulator <num_peers>

where, num_peers : number of nodes in the bitcoin P2P network to run during simulation

## I/O Format:
The code generate 3 type of output files:
>blockChainHistory.txt 
> transactionHistory.txt
> file_i.txt

#### blockChainHistory.txt 
It contains information about all the blockchain events that happend in the simulator
		this events includes:
			1) Creation of block
			2) Receiving of block and forwarding of block

#### transactionHistory.txt 
It contains information about transactionevents happend in the simulator like
			1) Creation of transaction
			2) Receiving and forwarding of transactions

#### file_i.txt
For each node in the network we are creating a separate output file named file_i.txt for i-th node
		 these file includes information like
		 	1) Node uniqe ID
		 	2) Type (slow or fast)
		 	3) CPU power of that node
		 	4) List of nodes that it is connected to
		 	5) For all such connection:
		 		a) Node uID
		 		b) Propagation Delay
		 		c) Bottleneck Speed