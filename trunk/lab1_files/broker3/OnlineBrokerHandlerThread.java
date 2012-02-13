import java.net.*;
import java.io.*;
import java.util.*;


public class OnlineBrokerHandlerThread extends Thread{

	// Debug Variable
	static boolean DEBUG = true;
	
	private Socket socket = null;
	private String exchange_Name = "";
	private Hashtable<String, Long>  cache;
	private Socket namingServiceSocket = null;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	
	public OnlineBrokerHandlerThread(Socket socket, String exchange_Name, Hashtable<String, Long>  cache,
			Socket namingServiceSocket, ObjectOutputStream out, ObjectInputStream in) {
		super("OnlineBrokerHandlerThread");
		this.socket = socket;
		this.exchange_Name = exchange_Name;
		this.cache = cache;
		this.namingServiceSocket = namingServiceSocket;
		this.out = out;
		this.in = in;
		
		if (DEBUG) {
			System.out.println("[BROKER_T DEBUG] Created new " + exchange_Name + " thread to handle client");
		}
	}
	
	public void run() {

		boolean gotByePacket = false;
		Socket brokerSocket = null;
	    ObjectOutputStream Broker_out = null;
	    ObjectInputStream Broker_in = null;
		
		//parse the nasdaq file, and build a cache
		//buildCache();
		
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			BrokerPacket packetFromClient;
			
			/* stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
			
			
			while (( packetFromClient = (BrokerPacket) fromClient.readObject()) != null) {
				/* create a packet to send reply back to client */
				BrokerPacket packetToClient = new BrokerPacket();
				
				
				/* process request */
				if(packetFromClient.type == BrokerPacket.BROKER_REQUEST) {
					// Look up the quote from the cache 
					packetToClient.symbol = packetFromClient.symbol;
					// Check if the KEY is in the cache
					if(!cache.containsKey(packetFromClient.symbol.toLowerCase())){
						
						if(DEBUG) {
							System.out.println ("[BROKER_T DEBUG] Symbol " + packetFromClient.symbol + " could not be found. Proceeding to forwarding.");
						}
						
						// Forward it to another broker
						// 1 - lookup the address of the other broker first
						BrokerPacket packetToNaming = new BrokerPacket();
						packetToNaming.type = BrokerPacket.LOOKUP_REQUEST;

						packetToNaming.exchange = (exchange_Name.equals("nasdaq"))? "tse" :"nasdaq";
						out.writeObject(packetToNaming);
						if(DEBUG) {
							System.out.println ("[BROKER_T DEBUG]   1. Successfully sent IP lookup request for " + packetToNaming.exchange);
						}
						
						// 2 - Name Service Reply
						BrokerPacket packetFromNaming;
						packetFromNaming = (BrokerPacket) in.readObject();
						if (packetFromNaming.type == BrokerPacket.LOOKUP_REPLY)
							if(DEBUG) {
								System.out.println ("[BROKER_T DEBUG]   2. Successfully obtained broker IP: " + packetFromNaming.locations[0].broker_host + " " + packetFromNaming.locations[0].broker_port);
							}
						else if (packetFromNaming.type == BrokerPacket.BROKER_ERROR){
							System.err.println("ERROR: Couldn't successfully obtain broker IP");
							System.exit(1);
						}
						
						// 3 - Open connection with the other Broker
						try {
							String hostname = packetFromNaming.locations[0].broker_host;
							int port = packetFromNaming.locations[0].broker_port;
							
							brokerSocket = new Socket(hostname, port);
							
							Broker_out = new ObjectOutputStream(brokerSocket.getOutputStream());
							Broker_in = new ObjectInputStream(brokerSocket.getInputStream());
							
							if(DEBUG) {
								System.out.println ("[BROKER_T DEBUG]   3. Successfully opened connection with Broker");
							}
						} catch (UnknownHostException e) {
							System.err.println("ERROR: Don't know where to connect!!");
							System.exit(1);
						} catch (IOException e) {
							System.err.println("ERROR: Couldn't get I/O for the connection.");
							System.exit(1);
						}
						
						// now forward the packet
						BrokerPacket packetToOtherBroker = new BrokerPacket();
						packetToOtherBroker.type = BrokerPacket.BROKER_FORWARD;
						packetToOtherBroker.symbol = packetFromClient.symbol;
						Broker_out.writeObject(packetToOtherBroker);
						if(DEBUG) {
							System.out.println ("[BROKER_T DEBUG]   4. Successfully forwarded the query to the " + packetToNaming.exchange + " broker to lookup : " + packetToOtherBroker.symbol);
						}
						
						
						// don't care what the packet is about, just pass it back to the client.
						// as the packetfowarding if loop took care of that from the other broker
						BrokerPacket packetFromOtherBroker = (BrokerPacket) Broker_in.readObject(); 
						if(DEBUG) {
							System.out.println ("[BROKER_T DEBUG]   5. Got the packet back from other broker");
						}
						packetToClient = packetFromOtherBroker;

						// Say bye to other broker
						BrokerPacket packetBye = new BrokerPacket();
						packetBye.type = BrokerPacket.BROKER_BYE;
						Broker_out.writeObject(packetBye);
						
						// close, close, close
						Broker_out.close();
						Broker_in.close();
						brokerSocket.close();
						
					} else {
						packetToClient.type = BrokerPacket.BROKER_QUOTE;
						packetToClient.quote = (Long)cache.get(packetFromClient.symbol.toLowerCase());
						
						if (DEBUG) {
							System.out.println ("[BROKER_T DEBUG] QUOTE REQUEST - CACHE CONTENT DUMP");
							for (Enumeration e = cache.keys(); e.hasMoreElements();)
							{
								String index_symbol = (String) e.nextElement(); 
								System.out.println ("[BROKER_T DEBUG]   " + index_symbol + ": " + cache.get(index_symbol));
							}
						}
					}
					
					if (DEBUG) {
						System.out.println("[BROKER_T DEBUG] Sending to Client: " + packetToClient.symbol);
					}
				
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
				else if(packetFromClient.type == BrokerPacket.EXCHANGE_ADD){
					packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
					packetToClient.symbol = packetFromClient.symbol;
					
					// Look up the symbol from the cache first (always use lower case)
					// If already exsits, return error
					if(cache.containsKey(packetFromClient.symbol.toLowerCase())){
						packetToClient.type = BrokerPacket.BROKER_ERROR;
						packetToClient.error_code = BrokerPacket.ERROR_SYMBOL_EXISTS;
						if (DEBUG) {
							System.out.println ("[BROKER_T DEBUG] ERROR_SYMBOL_EXISTS");
						}
					}
					else{
						// Add the symbol to the cache
						cache.put(packetFromClient.symbol.toLowerCase(), Long.parseLong("0"));
						
						if (DEBUG) {
							System.out.println ("[BROKER_T DEBUG] SYMBOL ADD - CACHE CONTENT DUMP");
							for (Enumeration e = cache.keys(); e.hasMoreElements();)
							{
								String index_symbol = (String) e.nextElement(); 
								System.out.println ("[BROKER_T DEBUG]   " + index_symbol + ": " + cache.get(index_symbol));
							}
						}
					}
					
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					// Update file
					updateFile();
					
					/* wait for next packet */
					continue;
					
				}
				else if(packetFromClient.type == BrokerPacket.EXCHANGE_REMOVE){
					packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
					packetToClient.symbol = packetFromClient.symbol;
					
					// look up the symbol from the cache first
					// if doesn't exist, return error
					if(!cache.containsKey(packetFromClient.symbol.toLowerCase())){
						packetToClient.type = BrokerPacket.BROKER_ERROR;
						packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
						if (DEBUG) {
							System.out.println ("[BROKER_T DEBUG] ERROR_INVALID_SYMBOL");
						}
					}
					else{
						cache.remove(packetFromClient.symbol.toLowerCase());
						if (DEBUG) {
							System.out.println ("[BROKER_T DEBUG] REMOVE SYMBOL - POST-CACHE CONTENT DUMP");
							for (Enumeration e = cache.keys(); e.hasMoreElements();)
							{
								String index_symbol = (String) e.nextElement(); 
								System.out.println ("[BROKER_T DEBUG]   " + index_symbol + ": " + cache.get(index_symbol));
							}
						}
					}
					
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					// Update file
					updateFile();
					
					/* wait for next packet */
					continue;
				}
				else if(packetFromClient.type == BrokerPacket.EXCHANGE_UPDATE){
					packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
					packetToClient.symbol = packetFromClient.symbol;
					packetToClient.quote = packetFromClient.quote;
					
					// look up the symbol from the cache first
					// if doesn't exsit, return error
					if(!cache.containsKey(packetFromClient.symbol.toLowerCase())){
						packetToClient.type = BrokerPacket.BROKER_ERROR;
						packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
						if (DEBUG) {
							System.out.println ("[BROKER_T DEBUG] ERROR_INVALID_SYMBOL");
						}
					}
					else{
						if(packetFromClient.quote >=1 && packetFromClient.quote <=300){
							cache.put(packetFromClient.symbol.toLowerCase(), packetFromClient.quote);
							if (DEBUG) {
								System.out.println ("[BROKER_T DEBUG] QUOTE UPDATE - CACHE CONTENT DUMP");
								for (Enumeration e = cache.keys(); e.hasMoreElements();)
								{
									String index_symbol = (String) e.nextElement(); 
									System.out.println ("[BROKER_T DEBUG]   " + index_symbol + ": " + cache.get(index_symbol));
								}
							}
						}
						else{
							packetToClient.type = BrokerPacket.BROKER_ERROR;
							packetToClient.error_code = BrokerPacket.ERROR_OUT_OF_RANGE;
							if (DEBUG) {
								System.out.println ("[BROKER_T DEBUG] ERROR_OUT_OF_RANGE");
							}
						}
					}
					
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					// Update file
					updateFile();
					
					/* wait for next packet */
					continue;
				}
				else if(packetFromClient.type == BrokerPacket.BROKER_FORWARD){

					BrokerPacket forwardReply = new BrokerPacket();
					forwardReply.symbol = packetFromClient.symbol;
					// if not in cache, return error
					if(!cache.containsKey(packetFromClient.symbol.toLowerCase())){
						forwardReply.type = BrokerPacket.BROKER_ERROR;
						forwardReply.symbol = packetFromClient.symbol;
						forwardReply.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
						if (DEBUG) {
							System.out.println ("[BROKER_T DEBUG] ERROR_INVALID_SYMBOL");
						}
					}
					else{ // reply back with quote
						forwardReply.type = BrokerPacket.BROKER_QUOTE;
						forwardReply.quote = (Long)cache.get(packetFromClient.symbol.toLowerCase());
						if (DEBUG) {
							System.out.println ("[BROKER_T DEBUG] FORWARDED REQUEST - CACHE CONTENT DUMP");
							for (Enumeration e = cache.keys(); e.hasMoreElements();)
							{
								String index_symbol = (String) e.nextElement(); 
								System.out.println ("[BROKER_T DEBUG]   " + index_symbol + ": " + cache.get(index_symbol));
							}
						}
					}
					
					/* send reply back to client */
					toClient.writeObject(forwardReply);
					
					continue;
				}
				
				/* Sending an ECHO_NULL || ECHO_BYE means quit */
				else if (packetFromClient.type == BrokerPacket.BROKER_NULL || packetFromClient.type == BrokerPacket.BROKER_BYE) {
					gotByePacket = true;
					packetToClient = new BrokerPacket();
					packetToClient.type = BrokerPacket.BROKER_BYE;
					toClient.writeObject(packetToClient);
 					
					// Update the nasdaq file
					if (DEBUG) {
						System.out.println ("[BROKER_T DEBUG] Terminating " + exchange_Name + " Thread");
					}
					updateFile();
					break;
				}
				
				/* if code comes here, there is an error in the packet */
				System.err.println("ERROR: Unknown BROKER_* packet!!");
				System.exit(-1);
			}
			
			
			/* cleanup when client exits */
			fromClient.close();
			toClient.close();
			socket.close();

		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}
	
	private void buildCache() {
		try {
			FileInputStream fstream = new FileInputStream(exchange_Name);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;

			while ((line = br.readLine()) != null) {
				parseLine(line);
			}
			in.close();
		} catch (Exception e) {
			/* just print the error stack and exit. */
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void parseLine(String line) {
		
		// check if the line is blank
		if (line.trim().length() != 0) {
			StringTokenizer st;
			String symbol, inputQuote;
			Long quote;

			st = new StringTokenizer(line, " ");
			
			// remove leading and trailing whitespace from each field
			symbol = st.nextToken().trim();
			inputQuote = st.nextToken().trim();
			// convert quote to Long object
			quote = Long.parseLong(inputQuote);

			// add each (symbol, quote) pair to the cache
			cache.put(symbol, quote);
		}
	}
	
	private void updateFile(){
		
		if (DEBUG) {
			System.out.println("[BROKER_T DEBUG] Updating File Contents" );
		}
			 try{
			    // Create file 
			    FileWriter fstream = new FileWriter(exchange_Name);
			    BufferedWriter out = new BufferedWriter(fstream);
			    
			  //  out.write("Hello Java");
			    Enumeration keys = cache.keys();
			    while (keys.hasMoreElements()) {
			         String key = (String)keys.nextElement();
			         String newline = key + " " +  cache.get(key) + "\n";
			         out.write(newline);
			     }
			    //Close the output stream
			    out.close();
		}catch (Exception e){//Catch exception if any
			      System.err.println("Error: " + e.getMessage());
			    }
}
}
