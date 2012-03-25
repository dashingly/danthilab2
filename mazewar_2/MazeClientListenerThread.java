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
					switch (packetFromClient.ce) {
						case MOVE_FORWARD:
							MazeClientListener.clientHandler.maze.moveClientForward(curClient);
							if(DEBUG) 
								System.out.println("[CLIENT DEBUG] Server indicates client " + packetFromClient.ClientName + " is moving forward");
							break;
						case MOVE_BACKWARD:
							MazeClientListener.clientHandler.maze.moveClientBackward(curClient);
							if(DEBUG) 
								System.out.println("[CLIENT DEBUG] Server indicates client " + packetFromClient.ClientName + " is moving backwards");
							break;
						case TURN_LEFT:
							MazeClientListener.clientHandler.maze.rotateClientLeft(curClient);
							if(DEBUG) 
								System.out.println("[CLIENT DEBUG] Server indicates client " + packetFromClient.ClientName + " is turning left");
							break;
						case TURN_RIGHT:
							MazeClientListener.clientHandler.maze.rotateClientRight(curClient);
							if(DEBUG) 
								System.out.println("[CLIENT DEBUG] Server indicates client " + packetFromClient.ClientName + " is turning right");
							break;
						case FIRE:
							MazeClientListener.clientHandler.maze.clientFire(curClient);
							if(DEBUG) 
								System.out.println("[CLIENT DEBUG] Server indicates client " + packetFromClient.ClientName + " is firing");
							break;
						default:
							if(DEBUG) 
								System.out.println("[CLIENT DEBUG] Unknown event from server " + packetFromClient.ClientName);
					}
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
	private static boolean DEBUG = true;
	// Socket connected to other client
	private Socket socket = null;

	// Set of remote clients
	public final Map<String, Client> clientSet = new HashMap<String, Client>();
	
	
	// Need to copy those locally: the clientevent class protects those
	private static final int MOVE_FORWARD 	= 0;
	private static final int MOVE_BACKWARD 	= 1;
	private static final int TURN_LEFT 		= 2;
	private static final int TURN_RIGHT 	= 3;
	private static final int FIRE 			= 4;
	private static final int ADD 			= 7;
	
}
