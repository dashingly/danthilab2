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
	// Socket connected to other client
	private Socket socket = null;
}
