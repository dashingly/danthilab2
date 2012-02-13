import java.net.*;
import java.io.*;
import java.util.*;


public class BrokerLookupServerHandlerThread extends Thread{

	// Debug Variable
	static boolean DEBUG = true;
	
	private Socket socket = null;
	private Hashtable<String, BrokerLocation>  ip_lookup = new Hashtable<String, BrokerLocation>();
	private int NumLocations = 0;
	
	public BrokerLookupServerHandlerThread(Socket socket, Hashtable<String, BrokerLocation>  ip_lookup) {
		super("BrokerLookupServerHandlerThread");
		this.socket = socket;
		this.ip_lookup = ip_lookup;
		
		if (DEBUG) {
			System.out.println("[LOOKUP_T DEBUG] Created new thread to handle client");
		}
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
				if(packetFromClient.type == BrokerPacket.LOOKUP_REGISTER) {
					packetToClient.type = BrokerPacket.LOOKUP_REPLY;
					packetToClient.exchange = packetFromClient.exchange;
					
					// Look up the exchange from the ip_lookup cache first (always use lower case)
					// If already exsits, remove previous and add current
					if(ip_lookup.containsKey(packetFromClient.exchange.toLowerCase())){
						if (DEBUG) {
							System.out.println ("[LOOKUP_T DEBUG] Broker IP already registered - overwriting...");
						}
						ip_lookup.remove(packetFromClient.exchange.toLowerCase());
					}
					// Add the ip to the ip_lookup cache
					ip_lookup.put(packetFromClient.exchange.toLowerCase(), packetFromClient.locations[0]);
					
					if (DEBUG) {
						System.out.println ("[LOOKUP_T DEBUG] ADDING NEW ENTRY - IP_LOOKUP CACHE CONTENT DUMP");
						for (Enumeration e = ip_lookup.keys(); e.hasMoreElements();)
						{
							String index_exchange = (String) e.nextElement(); 
							System.out.println ("[LOOKUP_T DEBUG]    " + index_exchange + ": " + ip_lookup.get(index_exchange) );
						}
					}
					// System.out.println("From Client: " + packetFromClient.exchange);
					
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
				else if(packetFromClient.type == BrokerPacket.LOOKUP_REQUEST) {
					packetToClient.exchange = packetFromClient.exchange;
					if (DEBUG) {
						System.out.println ("[LOOKUP_T DEBUG] ENTRY QUERY - IP_LOOKUP CACHE CONTENT DUMP");
						for (Enumeration e = ip_lookup.keys(); e.hasMoreElements();)
						{
							String index_exchange = (String) e.nextElement(); 
							System.out.println ("[LOOKUP_T DEBUG]    " + index_exchange + ": " + ip_lookup.get(index_exchange) );
						}
					}
					// Check if the exchange is in the ip_lookup
					if(!ip_lookup.containsKey(packetFromClient.exchange.toLowerCase())){
						packetToClient.type = BrokerPacket.BROKER_ERROR;
						packetToClient.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
						if (DEBUG) {
							System.out.println ("[LOOKUP_T DEBUG] IP for " + packetFromClient.exchange + " could not be found");
						}
					} else {
						// BrokerLocation object
						BrokerLocation location_lookup = (BrokerLocation) ip_lookup.get(packetFromClient.exchange.toLowerCase());
						
						BrokerLocation location[] = new BrokerLocation[1];
						location[0] = location_lookup;

						packetToClient.type = BrokerPacket.LOOKUP_REPLY;
						packetToClient.locations = location;
						if (DEBUG) {
							System.out.println ("[LOOKUP_T DEBUG] Sending IP for " + packetToClient.exchange + " : " + location[0].broker_host + ", " + location[0].broker_port);
						}
					}
					
					// System.out.println("From Client: " + packetFromClient.exchange);
					
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
				
				/* Sending an ECHO_NULL || ECHO_BYE means quit */
				else if (packetFromClient.type == BrokerPacket.BROKER_NULL || packetFromClient.type == BrokerPacket.BROKER_BYE) {
					gotByePacket = true;
					BrokerPacket packetBye = new BrokerPacket();
					packetBye.type = BrokerPacket.BROKER_BYE;
					toClient.writeObject(packetBye);
 					
					// Update the nasdaq file
					if (DEBUG) {
						System.out.println ("[BROKER_T DEBUG] Closing Thread");
					}
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
}
