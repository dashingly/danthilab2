// File: MazeClientHandler.java
// Author: TM

// Just copied and pasted the import list from MazeImpl.java
import java.lang.Thread;
import java.lang.Runnable;
import java.io.*;
import java.util.*;
import java.net.*;

/* inline class to describe client */
class ClientConnection implements Serializable {
	public Socket socket;
	public String name;
	
	/* constructor */
	public ClientConnection (Socket socket, String name) {
		this.socket = socket;
		this.name = name;
	}
}

public class MazeClientHandler implements Serializable, ClientListener, Runnable {
	
	// Constructor
	public MazeClientHandler(String NS_hostname, int NS_port, String my_hostname, int my_port, GUIClient client, MazeImpl mazeStr) {
		this.NS_hostname = NS_hostname;
		this.NS_port = NS_port;
		this.my_hostname = my_hostname;
		this.my_port = my_port;
		// Add the client object as a reference
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
		
		// Intialize the set of clients
		clientConnectionSet = new HashSet<ClientConnection>();
		
		// Ensure that no socket is currently open
		assert (NS_Socket == null);
		assert (NS_out == null);
		assert (NS_in == null);
		System.out.println("ClientHandler thread running");
		
		// 1 - Register to the naming service
		try {
			// Open socket
			NS_Socket = new Socket(NS_hostname, NS_port);
			// Open output stream
			NS_out = new ObjectOutputStream(NS_Socket.getOutputStream());
			// Create an NS_REGISTER message for the Server
			MazePacket packetToNamingService = new MazePacket();
			ClientIP my_location[] = new ClientIP[1];
			my_location[0] = new ClientIP(theGUIClient.getName(), my_hostname ,my_port);
			packetToNamingService.type = MazePacket.NS_REGISTER;
			packetToNamingService.ClientName = theGUIClient.getName();
			packetToNamingService.locations = my_location;
			// Send out the NS_REGISTER message
			NS_out.writeObject(packetToNamingService);
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("CLIENT DEBUG: Nameservice hostname: " + NS_hostname);
			System.out.println("CLIENT DEBUG: Nameservice port: " + NS_port);
			System.out.println(e.getMessage());
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}
		
		// 2 - Listen to the naming service reply
		try {
			// Open input stream
			NS_in = new ObjectInputStream(NS_Socket.getInputStream());
			// Packet received from the Name Service
			MazePacket packetFromNaming = (MazePacket) NS_in.readObject();
			// Process the packet
			assert (packetFromNaming.type == MazePacket.NS_REPLY);
			for (ClientIP item : packetFromNaming.locations) {
				if (item.client_name.compareTo(theGUIClient.getName())==0) {
					// Do nothing - do not add yourself
				} else {
					// Add the client
					addClient(item);
				}
				if(DEBUG) {
					System.out.println ("[CLIENT DEBUG] Successfully registered client " + item.client_name + " - IP: " + item.client_host + " " + item.client_port);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// 3 - Listen to additional joins from other clients
		try {
			MazePacket addPacketFromNaming;
			while (( addPacketFromNaming = (MazePacket) NS_in.readObject()) != null) {
				if (addPacketFromNaming.type == MazePacket.NS_ADD) {
					if (addPacketFromNaming.locations[0].client_name.compareTo(theGUIClient.getName())==0) {
						if(DEBUG) {
							System.out.println ("[CLIENT DEBUG] Received add command from local client, ignore.");
						}
					}
					else {
						// Add the client
						addClient(addPacketFromNaming.locations[0]);
						if(DEBUG) {
							System.out.println ("[CLIENT DEBUG] Successfully added client " + addPacketFromNaming.locations[0].client_name + " - IP: " + addPacketFromNaming.locations[0].client_host + " " + addPacketFromNaming.locations[0].client_port);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		
		
		/*
		try {
			//Client to address
			Client curClient; 
			
			while (active) {
				if(( packetFromServer = (MazePacket) inStream.readObject()) != null) {
					
					//
					// Two cases:
					// 1. Client being added 	Check to make sure client is not in the list.
					// 2. Client event 			Check to make sure client is in our clientSet.
					// 
					// There is a problem with adding clients:
					// 		Given seeds solves all problems, except for initialization of clients - they have to initialize in the same order, 
					// 		otherwise they will be mapped to the same location.
					//
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
							// if code comes here, there is an error in the packet
							System.err.println("ERROR: Unknown packet!!");
							System.exit(-1);
					}
				}
			}
			
			// cleanup when client exits 
			NS_out.close();
			NS_in.close();
			NS_Socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		*/
	}
	
	// Adds the client to the clientSet
	private void addClient(ClientIP clientIP) {
			assert(clientIP != null);
			// Add the ClientIP object to our clientLocationSet
			clientLocationSet.put(clientIP.client_name, clientIP);
			// Open socket
			Socket RemoteClient_Socket = null;
			try {
				RemoteClient_Socket = new Socket(clientIP.client_host, clientIP.client_port);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Create the ClientConnection object
			ClientConnection clientConnection = new ClientConnection(RemoteClient_Socket, clientIP.client_name);
			clientConnectionSet.add(clientConnection);
			numClientLogged ++;
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
			assert(NS_out != null);
			// Create an clientEvent message for the Server
			MazePacket packetToNamingService = new MazePacket();
			// Set the header
			if (ce.getEvent()== ADD)			packetToNamingService.type = MazePacket.ADD_CLIENT;
			else								packetToNamingService.type = MazePacket.CLIENT_EVENT;
			
			packetToNamingService.ClientName = theGUIClient.getName();
			packetToNamingService.ce = ce.getEvent();
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
				NS_out.writeObject(packetToNamingService);
			} catch (IOException e) {
				System.err.println("ERROR: Couldn't send the CLIENT_EVENT message.");
				System.exit(1);
			}
	}



	/* Internals ******************************************************/    
	// NameService Info
	private static String NS_hostname = null;
	private static int NS_port = 0;
	// Local Client Info
	private static String my_hostname = null;
	private static int my_port = 0;
	// Networking
	private static Socket NS_Socket = null;
	private static ObjectOutputStream NS_out = null;
	private static ObjectInputStream NS_in = null;
	
	// Reference to the GUIClient we are listening to
	private GUIClient theGUIClient = null;
	// Reference to the maze with all the clients.
	private static MazeImpl maze;
	private String selfName;
	
	
	// Set of remote clients
	public final Map<String, Client> clientSet = new HashMap<String, Client>();
	
	// Set of remote client locations
	public final Map<String, ClientIP> clientLocationSet = new HashMap<String, ClientIP>();
	
	// Set of remote client connections
	public static Set<ClientConnection> clientConnectionSet;
	
	// Counter taking track of number of clients logged
	int numClientLogged = 0;
	
	
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