import java.net.*;
import java.io.*;
import java.util.*;


public class MazeNamingServiceHandlerThread extends Thread{

	// Debug Variable
	static boolean DEBUG = true;
	
	// Static objects - ip_lookup, NumClients, and the outputStreamSet
	static Hashtable<String, ClientIP>  ip_lookup = new Hashtable<String, ClientIP>();
	static int NumClients = 0;
	static Set outputStreamSet = new HashSet();
	
	// Socket (local)
	private Socket socket = null;
	
	public MazeNamingServiceHandlerThread(Socket socket) {
		super("MazeNamingServiceHandlerThread");
		this.socket = socket;
		this.ip_lookup = ip_lookup;
		
		if (DEBUG) {
			System.out.println("[NAME_SERVICE DEBUG] Created new thread to handle client");
		}
	}
	
	public void run() {

		boolean gotByePacket = false;
		
		try {
			// Stream to read from client
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			MazePacket packetFromClient;
			
			// Stream to write back to client
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
			// Add this stream to the outputStreamSet
			outputStreamSet.add(toClient);
			
			
			while((packetFromClient = (MazePacket) fromClient.readObject()) != null) {
				/* NS_REGISTER */
				if(packetFromClient.type == MazePacket.NS_REGISTER) {
					// 1 - Look up the ClientName from the ip_lookup cache first (always use lower )
					//     If already exsits, remove previous and add current
					if(ip_lookup.containsKey(packetFromClient.ClientName)){
						if (DEBUG) {
							System.out.println ("[NAME_SERVICE DEBUG] Broker IP already registered - overwriting...");
						}
						ip_lookup.remove(packetFromClient.ClientName);
					}
					
					// 2 - Add the ip to the ip_lookup cache
					packetFromClient.locations[0].client_number = NumClients;
					ip_lookup.put(packetFromClient.ClientName, packetFromClient.locations[0]);
					NumClients ++;
					if (DEBUG) {
						System.out.println ("[NAME_SERVICE DEBUG] ADDING NEW ENTRY - IP_LOOKUP CACHE CONTENT DUMP - NumClients = " + NumClients);
						for (Enumeration e = ip_lookup.keys(); e.hasMoreElements();)
						{
							String index_ClientName = (String) e.nextElement(); 
							System.out.println ("[NAME_SERVICE DEBUG]    " + index_ClientName + " - " + ip_lookup.get(index_ClientName) );
						}
					}
					
					// 3 - Prepare the location array sent in the reply message for the client
					ClientIP location[] = new ClientIP[NumClients];
					for (Enumeration e = ip_lookup.keys(); e.hasMoreElements();)
					{
						String index_ClientName = (String) e.nextElement(); 
						location[ip_lookup.get(index_ClientName).client_number] = ip_lookup.get(index_ClientName);
					}
					
					// 4 - Send the reply message for the client
					MazePacket packetToClient = new MazePacket();
					packetToClient.type = MazePacket.NS_REPLY;
					packetToClient.ClientName = packetFromClient.ClientName;
					packetToClient.locations = location;
					packetToClient.NumClients = NumClients;
					toClient.writeObject(packetToClient);
					
					// 5 - Now prepare to notify everyone else about the new client added
					MazePacket packetToBroadcast = new MazePacket();
					packetToBroadcast.type = MazePacket.NS_ADD;
					packetToBroadcast.ClientName = packetFromClient.ClientName;
					packetToBroadcast.locations = packetFromClient.locations;
					
					// 6 - Broadcast
					Iterator ossi = outputStreamSet.iterator();
					while (ossi.hasNext()) {
						Object o = ossi.next();
						assert(o instanceof ObjectOutputStream);
						ObjectOutputStream toBroadcast = (ObjectOutputStream)o;
						try {
							/* stream to read from client */
							toBroadcast.writeObject(packetToBroadcast);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
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
