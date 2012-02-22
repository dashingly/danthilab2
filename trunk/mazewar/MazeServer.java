import java.net.*;
import java.io.*;
import java.lang.Object;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
 
/* inline class to describe client */
class ClientLocation implements Serializable {
	public Socket socket;
	public String name;
	
	/* constructor */
	public ClientLocation (Socket socket, String name) {
		this.socket = socket;
		this.name = name;
	}
}

public class MazeServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		ServerSocket serverSocket = null;
        boolean listening = true;

        try {
        	if(args.length == 1) {
        		serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }
		
		// Initialize the queue
		ServerInQueue = new LinkedList<MazePacket>();
		
		// Intialize the set of clients
		clientSet= new HashSet<ClientLocation>();
		
		// Initialize the sequence number
		SequenceNumber = 1;
		
		// Initialize the number of clients
		NumClients = 0;
		
		// Run the maze server replier thread
		new MazeServerReplierThread().start();
		
		if (DEBUG)
				System.out.println("SERVER DEBUG: Server listening");
        
        while (listening) {
        	new MazeServerHandlerThread(serverSocket.accept()).start();
        }

        serverSocket.close();
	}

	// Adds the client to the clientSet
	public static synchronized boolean addClient(MazePacket packetFromClient, Socket socket) {
			assert(packetFromClient != null);
			String ClientName = packetFromClient.ClientName;
			ClientLocation client = new ClientLocation(socket, ClientName);
			clientSet.add(client);
			
			if (DEBUG)
				System.out.println("SERVER DEBUG: New Client added: " + ClientName);
			return true;
	}

	// Gets the size of clientSet
	public static synchronized int getNumClient() {
			return clientSet.size();
	}
	
	// Handles the synchronized add to the ServerInQueue
	public static synchronized boolean addToServerInQueue(MazePacket packetFromClient) {
			assert(packetFromClient != null);
			String ClientName = packetFromClient.ClientName;
			
			if (DEBUG) {
				switch (packetFromClient.ce) {
					case MOVE_FORWARD:
						System.out.println("SERVER DEBUG: Client " + ClientName + " is moving forward");
						break;
					case MOVE_BACKWARD:
						System.out.println("SERVER DEBUG: Client " + ClientName + " is moving backwards");
						break;
					case TURN_LEFT:
						System.out.println("SERVER DEBUG: Client " + ClientName + " is turning left");
						break;
					case TURN_RIGHT:
						System.out.println("SERVER DEBUG: Client " + ClientName + " is turning right");
						break;
					case FIRE:
						System.out.println("SERVER DEBUG: Client " + ClientName + " is firing");
						break;
					default:
						System.out.println("SERVER DEBUG: Unknown event from client " + ClientName);
				}
			}
			
			try {
				// Enqueuing command
				if (DEBUG)
					System.out.println("SERVER DEBUG: Enqueuing command into the MazeServerQueue issued by " + packetFromClient.ClientName);
				ServerInQueue.add(packetFromClient);
			} catch (IllegalStateException e) {
				System.err.println("ERROR: Could not add to ServerInQueue due to capacity retrictions!");
				System.exit(-1);
			} catch (ClassCastException e) {
				System.err.println("ERROR: Could not add the class of the specified element to ServerInQueue!");
				System.exit(-1);
			} catch (IllegalArgumentException e) {
				System.err.println("ERROR: Could not add the specified element to ServerInQueue!");
				System.exit(-1);
			} 
			return true;
	}

	// Handles the synchronized remove from the ServerInQueue
	public static synchronized MazePacket removeFromServerInQueue() {
			Object o = null;
			try {
				o = ServerInQueue.remove();
			} catch (RuntimeException e) {
				System.err.println("ERROR: Could not remove from ServerInQueue!");
				System.exit(-1);
			}
			assert(o instanceof MazePacket);
			return (MazePacket) o;
	}

	// Handles the synchronized peek from the ServerInQueue
	public static synchronized boolean peekFromServerInQueue() {
			Object o = null;
			try {
				o = ServerInQueue.peek();
			} catch (RuntimeException e) {
				System.err.println("ERROR: Could not remove from ServerInQueue!");
				System.exit(-1);
			}
			if (o instanceof MazePacket)
				return true;
			return false;
	}

	// Returns the current SequenceNumber and increments it (not in use right now)
	public static synchronized int getUniqueSequenceNumber() {
			int returnNumber = SequenceNumber;
			SequenceNumber ++;
			return returnNumber;
	}

	
	/* Internals ******************************************************/ 
	// Incoming Queue
	private static Queue ServerInQueue;
	// Sequence Number
	private static int SequenceNumber;
	// Number of Clients
	private static int NumClients;
	// Set of Clients (public, no need for synchronization) 
	public static Set<ClientLocation> clientSet;
	
	// Turns debug messages on/off
	private static boolean DEBUG = true;
	
	// Need to copy those locally: the clientevent class protects those
	private static final int MOVE_FORWARD = 0;
	private static final int MOVE_BACKWARD = 1;
	private static final int TURN_LEFT = 2;
	private static final int TURN_RIGHT = 3;
	private static final int FIRE = 4;

}
