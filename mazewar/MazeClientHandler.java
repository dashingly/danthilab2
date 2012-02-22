// File: ClientEcho.java
// Author: TM

// Currently dummy class to echo events locally from client to maze
// This way, we can easily extend this entity accross different machines

// Just copied and pasted the import list from MazeImpl.java
import java.lang.Thread;
import java.lang.Runnable;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;  
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.HashMap;
import java.net.*;


public class MazeClientHandler implements Serializable, ClientListener, Runnable{


	//constructor
	public MazeClientHandler(String hostname, int port, GUIClient client) {
		this.hostname = hostname;
		this.port = port;
		// Add the ClientEcho object as a reference
		assert(client != null);
		this.theGUIClient = client;
		
		// Start the MazeClientHandler
		thread = new Thread(this);
		active = true;
		thread.start();
	}
	
	public void run() {
		// Ensure that no socket is currently open
		assert (clientSocket == null);
		assert (outStream == null);
		assert (inStream == null);
		System.out.println("CLIENT DEBUG: ClientHandler thread running");
		
		// Open the socket, and object streams
		try {
			// Open socket
			clientSocket = new Socket(hostname, port);
			
			// Open output stream
			outStream = new ObjectOutputStream(clientSocket.getOutputStream());
			// Create an addClient message for the Server
			MazePacket packetToServer = new MazePacket();
			packetToServer.type = MazePacket.ADD_CLIENT;
			packetToServer.ClientName = theGUIClient.getName();
			// Send out the add_client message
			outStream.writeObject(packetToServer);
			
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("CLIENT DEBUG: Server hostname: " + hostname);
			System.out.println("CLIENT DEBUG: Server port: " + port);
			System.out.println(e.getMessage());
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}
		
		try {
			// Open input stream
			inStream = new ObjectInputStream(clientSocket.getInputStream());
			// Packet received from the Server
			MazePacket packetFromServer;
			
			while (active) {
				if(( packetFromServer = (MazePacket) inStream.readObject()) != null) {
					/* process request */
					// THIERRY: This part is temporary, obviously we need to have it change the map
					switch (packetFromServer.type) {
						case MazePacket.CLIENT_EVENT:
							// Just print it
							if (DEBUG) {
								switch (packetFromServer.ce) {
									case MOVE_FORWARD:
										System.out.println("CLIENT DEBUG: Server indicates client " + packetFromServer.ClientName + " is moving forward");
										break;
									case MOVE_BACKWARD:
										System.out.println("CLIENT DEBUG: Server indicates client " + packetFromServer.ClientName + " is moving backwards");
										break;
									case TURN_LEFT:
										System.out.println("CLIENT DEBUG: Server indicates client " + packetFromServer.ClientName + " is turning left");
										break;
									case TURN_RIGHT:
										System.out.println("CLIENT DEBUG: Server indicates client " + packetFromServer.ClientName + " is turning right");
										break;
									case FIRE:
										System.out.println("CLIENT DEBUG: Server indicates client " + packetFromServer.ClientName + " is firing");
										break;
									default:
										System.out.println("CLIENT DEBUG: Unknown event from server " + packetFromServer.ClientName);
								}
							}
							break;
						default:
							/* if code comes here, there is an error in the packet */
							System.err.println("ERROR: Unknown packet!!");
							System.exit(-1);
					}
				}
			}
			/* cleanup when client exits */
			outStream.close();
			inStream.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// Function that handles the GUIclient's updates
	public void clientUpdate(Client c, ClientEvent ce) {
			if (DEBUG) {
				System.out.println("CLIENT DEBUG: MazeClientHandler Listener notified");
			}
			// Assert that the client we are listening is indeed theGUIClient
			assert (c == theGUIClient);
			// Make sure that we are connected to the server
			assert(outStream != null);
			// Create an clientEvent message for the Server
			MazePacket packetToServer = new MazePacket();
			packetToServer.type = MazePacket.CLIENT_EVENT;
			packetToServer.ClientName = theGUIClient.getName();
			packetToServer.ce = ce.getEvent();
			// Debug printouts
			if (DEBUG) {
				switch (ce.getEvent()) {
					case MOVE_FORWARD:
						System.out.println("CLIENT DEBUG: GUI client moves forward.");
						break;
					case MOVE_BACKWARD:
						System.out.println("CLIENT DEBUG: GUI client moves backward.");
						break;
					case TURN_LEFT:
						System.out.println("CLIENT DEBUG: GUI client turns left.");
						break;
					case TURN_RIGHT:
						System.out.println("CLIENT DEBUG: GUI client turns right.");
						break;
					case FIRE:
						System.out.println("CLIENT DEBUG: GUI client fires.");
						break;
				}
			}
			// Send out the add_client message
			try {
				outStream.writeObject(packetToServer);
			} catch (IOException e) {
				System.err.println("ERROR: Couldn't send the CLIENT_EVENT message.");
				System.exit(1);
			}
	}



	/* Internals ******************************************************/    
	// Server Info
	private static String hostname = null;
	private static int port = 0;
	// Networking
	private static Socket clientSocket = null;
	private static ObjectOutputStream outStream = null;
	private static ObjectInputStream inStream = null;
	// Reference to the GUIClient we are listening to
	private static GUIClient theGUIClient = null;
	// Thread
	private final Thread thread;
	// Flag to say whether the control thread is active
	private static boolean active = false;
	
	// Turns debug messages on/off
	private static boolean DEBUG = true;
	
	// Need to copy those locally: the clientevent class protects those
	private static final int MOVE_FORWARD = 0;
	private static final int MOVE_BACKWARD = 1;
	private static final int TURN_LEFT = 2;
	private static final int TURN_RIGHT = 3;
	private static final int FIRE = 4;
	

}