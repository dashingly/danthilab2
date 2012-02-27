import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.Thread;

// This Class listens to each client connected to the MazeServer
// and processes each message by enqueuing it
public class MazeServerHandlerThread extends Thread {

	private Socket socket = null;
	private Hashtable<String, Long>  cache = new Hashtable<String, Long>();
	
	public MazeServerHandlerThread(Socket socket) {
		super("MazeServerHandlerThread");
		assert(socket != null);
		this.socket = socket;
		System.out.println("Created new Thread to handle client");
	}
	
	public void run() {
		// Just listens and enqueues
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			MazePacket packetFromClient;
			
			while (( packetFromClient = (MazePacket) fromClient.readObject()) != null) {
				/* process request */
				switch (packetFromClient.type) {
					case MazePacket.ADD_CLIENT:
						// Add the client
						packetFromClient.MaxNumClient = MazeServer.ClientNum;
						MazeServer.addClient(packetFromClient, socket);
						MazeServer.addToClientAddQueue(packetFromClient);
						break;
					case MazePacket.CLIENT_EVENT:
						// Add the packet to the ServerInQueue
						MazeServer.addToServerInQueue(packetFromClient);
						break;
					default:
						/* if code comes here, there is an error in the packet */
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
	private static boolean DEBUG = true;
}
