import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.Thread;

// This Class listens to each client connected to the MazeServer
// and processes each message by enqueuing it
public class MazeClientListenerThread extends Thread {

	public MazeClientListenerThread(Socket socket) {
		super("MazeClientListenerThread");
		this.socket = socket;
		System.out.println("Created new MazeClientListenerThread to handle a client connections");
	}
	
	public void run() {
		// Just listens and enqueues
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			MazePacket packetFromClient;
			
			while (( packetFromClient = (MazePacket) fromClient.readObject()) != null) {
				/* process request */
				Client curClient; 
				if (packetFromClient.type == MazePacket.CLIENT_EVENT) {
					// Make sure client exists locally and obtain the client
					if (MazeClientListener.clientHandler.clientSet.containsKey(packetFromClient.ClientName))
					{
						curClient = MazeClientListener.clientHandler.clientSet.get(packetFromClient.ClientName);
					}
					else
					{
						System.err.println("ERROR: Client with name " + packetFromClient.ClientName + " is not known on this machine.");
						continue;
					}
					/* Instead of executing commands, just queue them.	*/
					MazeClientListener.add2q(packetFromClient.seqs, packetFromClient);
					
					
				} else {
						// if code comes here, there is an error in the packet
						System.err.println("ERROR: Unknown packet!!");
						System.exit(-1);
				}
			} 
			
			/* cleanup when client exits */
			fromClient.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	/* Internals ******************************************************/   
	// Turns debug messages on/off
	private static boolean DEBUG = false;
	// Socket connected to other client
	private Socket socket = null;

	// Set of remote clients
	public final Map<String, Client> clientSet = new HashMap<String, Client>();
	
	
	

	
}
