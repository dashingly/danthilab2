import java.net.*;
import java.io.*;
import java.util.*;


public class TicketServiceThread extends Thread{

	// Debug Variable
	static boolean DEBUG = true;
	
	// Static objects - ip_lookup, NumClients, and the outputStreamSet
	static Hashtable<String, ClientIP>  ip_lookup = new Hashtable<String, ClientIP>();
	static int NumClients = 0;
	static Set outputStreamSet = new HashSet();
	
	// Socket (local)
	private Socket socket = null;
	
	public TicketServiceThread(Socket socket) {
		super("TicketServiceThread");
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
				if(packetFromClient.type == MazePacket.GET_SEQs) {
					
					
					// 4 - Send the reply message for the client
					MazePacket packetToClient = new MazePacket();
					packetToClient.type = MazePacket.SEQs;
					packetToClient.ClientName = packetFromClient.ClientName;
					packetToClient.seqs = TicketService.getSeqs();
					toClient.writeObject(packetToClient);
					
					if (DEBUG)	System.out.println("Sending seq # " + packetToClient.seqs + " to client " + packetToClient.ClientName);
					
					
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
