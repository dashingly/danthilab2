import java.net.*;
import java.io.*;
import java.util.*;


public class OnlineBrokerHandlerThread extends Thread{

	// Debug Variable
	static boolean DEBUG = true;
	
	private Socket socket = null;
	static Hashtable<String, Long>  cache = Cache.getInstance();
	
	public OnlineBrokerHandlerThread(Socket socket) {
		super("OnlineBrokerHandlerThread");
		this.socket = socket;
		System.out.println("Created new Thread to handle client");
	}
	
	public void run() {

		boolean gotByePacket = false;
		
		
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
						packetToClient.type = BrokerPacket.BROKER_ERROR;
						packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
					} else {
						packetToClient.type = BrokerPacket.BROKER_QUOTE;
						packetToClient.quote = (Long)cache.get(packetFromClient.symbol.toLowerCase());
						// This check could be redundant - leave it for now... (can replace with an assertion)
						if( packetToClient.quote == null){
							packetToClient.type = BrokerPacket.BROKER_ERROR;
							packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
						}
					}
					
					System.out.println("From Client: " + packetFromClient.symbol);
				
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
						packetToClient.error_code = BrokerPacket.ERROR_SYMBOL_EXISTS;
						if (DEBUG) {
							System.out.println ("[DEBUG] ERROR_SYMBOL_EXISTS");
						}
					}
					else{
						// Add the symbol to the cache
						cache.put(packetFromClient.symbol.toLowerCase(), Long.parseLong("0"));
						// Add the symbol to the update list
						
						if (DEBUG) {
							System.out.println ("[DEBUG] CACHE CONTENT DUMP");
							for (Enumeration e = cache.keys(); e.hasMoreElements();)
							{
								String index_symbol = (String) e.nextElement(); 
								System.out.println ("[DEBUG]   " + index_symbol + ": " + cache.get(index_symbol));
							}
						}
					}
					
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
					
				}
				else if(packetFromClient.type == BrokerPacket.EXCHANGE_REMOVE){
					packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
					packetToClient.symbol = packetFromClient.symbol;
					
					// look up the symbol from the cache first
					// if doesn't exist, return error
					if(!cache.containsKey(packetFromClient.symbol.toLowerCase())){
						packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
						if (DEBUG) {
							System.out.println ("[DEBUG] ERROR_INVALID_SYMBOL");
						}
					}
					else{
						cache.remove(packetFromClient.symbol.toLowerCase());
						if (DEBUG) {
							System.out.println ("[DEBUG] CACHE CONTENT DUMP");
							for (Enumeration e = cache.keys(); e.hasMoreElements();)
							{
								String index_symbol = (String) e.nextElement(); 
								System.out.println ("[DEBUG]   " + index_symbol + ": " + cache.get(index_symbol));
							}
						}
					}
					
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
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
						packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
						if (DEBUG) {
							System.out.println ("[DEBUG] ERROR_INVALID_SYMBOL");
						}
					}
					else{
						if(packetFromClient.quote >=1 && packetFromClient.quote <=300){
							cache.put(packetFromClient.symbol.toLowerCase(), packetFromClient.quote);
							if (DEBUG) {
								System.out.println ("[DEBUG] CACHE CONTENT DUMP");
								for (Enumeration e = cache.keys(); e.hasMoreElements();)
								{
									String index_symbol = (String) e.nextElement(); 
									System.out.println ("[DEBUG]   " + index_symbol + ": " + cache.get(index_symbol));
								}
							}
						}
						else{
							packetToClient.error_code = BrokerPacket.ERROR_OUT_OF_RANGE;
							if (DEBUG) {
								System.out.println ("[DEBUG] ERROR_OUT_OF_RANGE");
							}
						}
					}
					
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
					
				
				/* Sending an ECHO_NULL || ECHO_BYE means quit */
				else if (packetFromClient.type == BrokerPacket.BROKER_NULL || packetFromClient.type == BrokerPacket.BROKER_BYE) {
					System.out.println("I am here");
					gotByePacket = true;
					//packetToClient = new BrokerPacket();
					//packetToClient.type = BrokerPacket.BROKER_BYE;
					//toClient.writeObject(packetToClient);
 						
					// Update the nasdaq file
					System.out.println("I am here");
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
	
	
	// utility function that helps update the file
	private void updateFile(){
		System.out.println( "Updating File Contents" );
	
		try{
		    // Create file 
		    FileWriter fstream = new FileWriter("nasdaq");
		    BufferedWriter out = new BufferedWriter(fstream);
			    
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
