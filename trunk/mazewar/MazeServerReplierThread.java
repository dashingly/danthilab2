import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.Thread;

// This class dequeues each message and broadcasts it to each of the 
// Clients
public class MazeServerReplierThread extends Thread {

	private Socket socket = null;
	private Hashtable<String, Long>  cache = new Hashtable<String, Long>();
	
	public MazeServerReplierThread() {
		super("MazeServerReplierThread");
		active = true;
		if (DEBUG)
			System.out.println("SERVER DEBUG: Created new Thread to broadcast to all clients");
	}
	
	public void run() {
		// First wait until there are enough clients connected to start the game
		while (MazeServer.getNumClient() < MazeServer.ClientNum) {
			// Sleep 
			try {
				sleep(100);
			} catch(Exception e) {
				// Shouldn't happen.
			}
		}
		// Now we're ready to open all sockets
		if (DEBUG)
			System.out.println("SERVER DEBUG: All clients have connected themselves to the server: ");
		outputStreamSet = new HashSet();
		Iterator csi = MazeServer.clientSet.iterator();
		while (csi.hasNext()) {
			Object o = csi.next();
			assert(o instanceof ClientLocation);
			ClientLocation cl = (ClientLocation)o;
			try {
				/* stream to read from client */
				ObjectOutputStream toClient = new ObjectOutputStream(cl.socket.getOutputStream());
				outputStreamSet.add(toClient);
				if (DEBUG)
					System.out.println("SERVER DEBUG: \t Client " + cl.name);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Send the client add start sequence
		while(MazeServer.peekFromClientAddQueue()) {
			MazePacket packetToClient = (MazePacket) MazeServer.removeFromClientAddQueue();
			if (DEBUG)
				System.out.println("SERVER DEBUG: Sending start sequence: " + packetToClient.ClientName);
			assert (packetToClient != null);
			// Now that we've retrieved a MazePacket from the ServerInQueue, we can broadcast it to everyone
			Iterator ossi = outputStreamSet.iterator();
			while (ossi.hasNext()) {
				Object o = ossi.next();
				assert(o instanceof ObjectOutputStream);
				ObjectOutputStream toClient = (ObjectOutputStream)o;
				try {
					/* stream to read from client */
					toClient.writeObject(packetToClient);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// Just dequeues and broadcasts
		while (active) {
			if(MazeServer.peekFromServerInQueue()) {
				MazePacket packetToClient = (MazePacket) MazeServer.removeFromServerInQueue();
				if (DEBUG)
					System.out.println("SERVER DEBUG: Dequeuing command from the MazeServerQueue issued by " + packetToClient.ClientName);
				assert (packetToClient != null);
				// Now that we've retrieved a MazePacket from the ServerInQueue, we can broadcast it to everyone
				Iterator ossi = outputStreamSet.iterator();
				while (ossi.hasNext()) {
					Object o = ossi.next();
					assert(o instanceof ObjectOutputStream);
					ObjectOutputStream toClient = (ObjectOutputStream)o;
					try {
						/* stream to read from client */
						toClient.writeObject(packetToClient);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			// Sleep 
			try {
					sleep(100);
			} catch(Exception e) {
					// Shouldn't happen.
			}
		}
	}

	
	/* Internals ******************************************************/   
	// Turns debug messages on/off
	private static boolean DEBUG = true;
	// Activates the MazeServerReplierThread
	private static boolean active = false;
	// Set of ObjectOutputStream (warning! ensure that all clients have been added first)
	private static Set outputStreamSet;
}
