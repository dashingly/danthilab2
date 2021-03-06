// File: ClientEcho.java
// Author: TM

// Currently dummy class to echo events locally from client to maze
// This way, we can easily extend this entity accross different machines

// Just copied and pasted the import list from MazeImpl.java
import java.lang.Thread;
import java.lang.Runnable;
import java.io.*;
import java.util.*;
import java.net.*;


public class MazeClientHandler implements Serializable, ClientListener, Runnable{
	
	// Constructor
	public MazeClientHandler(String hostname, int port, GUIClient client, MazeImpl mazeStr) {
		this.hostname = hostname;
		this.port = port;
		// Add the ClientEcho object as a reference
		assert(client != null);
		this.theGUIClient = client;
		assert(selfName != null);
		
		// Set the reference to the maze
		this.maze = mazeStr;
		
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
		System.out.println("ClientHandler thread running");
		
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
			//Client to address
			Client curClient; 
			// Maximum number of clients
			int MaxNumClients = 1000;
			// Conter taking track of number of clients logged
			int numClientLogged = 0;
			
			while (active) {
				if(( packetFromServer = (MazePacket) inStream.readObject()) != null) {
					
					/*
					 * Two cases:
					 * 1. Client being added 	Check to make sure client is not in the list.
					 * 2. Client event 			Check to make sure client is in our clientSet.
					 * 
					 * There is a problem with adding clients:
					 * 		Given seeds solves all problems, except for initialization of clients - they have to initialize in the same order, 
					 * 		otherwise they will be mapped to the same location.
					 */
					if (packetFromServer.type == MazePacket.ADD_CLIENT) {
						// Set the client limit set by the server
						MaxNumClients = packetFromServer.MaxNumClient;
						// Incement the number of clients logged
						numClientLogged ++;
						// The client set is contained withing this class: check if it isn't a duplicate
						if (this.clientSet.containsKey(packetFromServer.ClientName))
						{
							System.out.println("ERROR: Client with name " + packetFromServer.ClientName + " already exists locally.");
							continue;
						}
						else
						{
							//Check if this is a local client we are adding
							if ((packetFromServer.ClientName).equals(theGUIClient.getName()))
							{
								clientSet.put(theGUIClient.getName(), theGUIClient);
								maze.addClient(theGUIClient);
								if (DEBUG) {
									System.out.println("CLIENT DEBUG: Server indicates to add the GUI client " + packetFromServer.ClientName);
								}
							}
							else
							{
								RemoteClient remClient = new RemoteClient(packetFromServer.ClientName);
								clientSet.put(remClient.getName(), remClient);
								maze.addClient(remClient);
								if (DEBUG) {
									System.out.println("CLIENT DEBUG: Server indicates to add new remote client " + packetFromServer.ClientName);
								}
							}
						}
						if (DEBUG) {
							System.out.println("CLIENT DEBUG: numClientLogged = " + numClientLogged + ", MaxNumClients = " + MaxNumClients);
						}
						// If all of the clients are connected, the Maze can launch.
						if (numClientLogged==MaxNumClients) {
							maze.waiting = false;
							if (DEBUG) {
								System.out.println("CLIENT DEBUG: The Mazewar game can start!");
							}
						}
					} else if (packetFromServer.type == MazePacket.UPDATE_PROJECTILES) {
						// Update all projectiles on the map
						maze.moveAllProjectiles();
					} else if (packetFromServer.type == MazePacket.CLIENT_EVENT) {
						// Make sure client exists locally and obtain the client
						if (this.clientSet.containsKey(packetFromServer.ClientName))
						{
							curClient = this.clientSet.get(packetFromServer.ClientName);
						}
						else
						{
							System.err.println("ERROR: Client with name " + packetFromServer.ClientName + " is not known on this machine.");
							continue;
						}
						switch (packetFromServer.ce) {
							case MOVE_FORWARD:
								maze.moveClientForward(curClient);
								if(DEBUG) 
									System.out.println("CLIENT DEBUG: Server indicates client " + packetFromServer.ClientName + " is moving forward");
								break;
							case MOVE_BACKWARD:
								maze.moveClientBackward(curClient);
								if(DEBUG) 
									System.out.println("CLIENT DEBUG: Server indicates client " + packetFromServer.ClientName + " is moving backwards");
								break;
							case TURN_LEFT:
								maze.rotateClientLeft(curClient);
								if(DEBUG) 
									System.out.println("CLIENT DEBUG: Server indicates client " + packetFromServer.ClientName + " is turning left");
								break;
							case TURN_RIGHT:
								maze.rotateClientRight(curClient);
								if(DEBUG) 
									System.out.println("CLIENT DEBUG: Server indicates client " + packetFromServer.ClientName + " is turning right");
								break;
							case FIRE:
								maze.clientFire(curClient);
								if(DEBUG) 
									System.out.println("CLIENT DEBUG: Server indicates client " + packetFromServer.ClientName + " is firing");
								break;
							default:
								if(DEBUG) 
									System.out.println("CLIENT DEBUG: Unknown event from server " + packetFromServer.ClientName);
						}
					} else {
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
	/*
	 * Seems to me it runs under GUI-thread and not MazeClientHandler-thread.
	 */
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
			// Set the header
			if (ce.getEvent()== ADD)			packetToServer.type = MazePacket.ADD_CLIENT;
			else								packetToServer.type = MazePacket.CLIENT_EVENT;
			
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
					case ADD:
						System.out.println("CLIENT DEBUG: GUI client is being added.");
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
	private GUIClient theGUIClient = null;
	// Reference to the maze with all the clients.
	private static MazeImpl maze;
	private String selfName;
	
	
	// Set of remote clients
	public final Map<String, Client> clientSet = new HashMap<String, Client>();
	
	// Thread
	private final Thread thread;
	// Flag to say whether the control thread is active
	private static boolean active = false;
	
	// Turns debug messages on/off
	private static boolean DEBUG = true;
	
	// Need to copy those locally: the clientevent class protects those
	private static final int MOVE_FORWARD 	= 0;
	private static final int MOVE_BACKWARD 	= 1;
	private static final int TURN_LEFT 		= 2;
	private static final int TURN_RIGHT 	= 3;
	private static final int FIRE 			= 4;
	private static final int ADD 			= 7;
	

}