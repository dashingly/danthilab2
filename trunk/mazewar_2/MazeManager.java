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


public class MazeManager implements Serializable, ClientListener, Runnable{
	
	// Constructor
	public MazeManager(String NS_hostname, int NS_port, String my_hostname, int my_port, GUIClient client, MazeImpl mazeStr) {
		this.NS_hostname = NS_hostname;
		this.NS_port = NS_port;
		this.my_hostname = my_hostname;
		this.my_port = my_port;
		// Add the ClientEcho object as a reference
		assert(client != null);
		this.theGUIClient = client;
		assert(selfName != null);
		
		// Set the reference to the maze
		this.maze = mazeStr;
		
		clientSet= new HashSet<ClientLocation>();
		
		// Start the MazeClientHandler
		thread = new Thread(this);
		active = true;
		thread.start();
	}
	
	public void run() {
		/*
		 * Stage 1:
		 * - Get all the client info using Naming Service
		 * - Establish connections to clients and save sockets into variable for later use by ClientUpdate function.
		 * - Start threads for incoming connections from the clients.
		 * - Establish connection to Ticketing Service and store socket in variable 
		 * Stage 2:
		 * - Monitor incoming queue - once there are no gaps execute events. Can use "current" counter and check for it's existence in the hash-map.
		 * - Do not have to bother with removing past events. Can simply increment counter and move on.
		 */
		
		/*
		 * 		STAGE 1
		 */
		// Counter taking track of number of clients logged
		int numClientLogged = 0;
		
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
				clientLocationSet.put(item.client_name, item);
				if(DEBUG) {
					System.out.println ("[CLIENT DEBUG] Successfully registered client " + item.client_name + " - IP: " + item.client_host + " " + item.client_port);
				}
				numClientLogged ++;
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
						if(DEBUG) {
							System.out.println ("[CLIENT DEBUG] Successfully added client " + addPacketFromNaming.locations[0].client_name + " - IP: " + addPacketFromNaming.locations[0].client_host + " " + addPacketFromNaming.locations[0].client_port);
							
							System.out.println ("[CLIENT DEBUG] " + addPacketFromNaming.locations[0].client_name + " " + theGUIClient.getName());
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
		 * 		STAGE 2
		 */
		
		// Check the queue
		// If the "cunnertEvent" number exists in the "incomingQ" hash map, then execute the event with that SEQ# 
	}
	
	// Function that handles the GUIclient's updates
	/*
	 * Seems to me it runs under GUI-thread and not MazeClientHandler-thread.
	 */
	public void clientUpdate(Client c, ClientEvent ce) {
			if (DEBUG) {
				System.out.println("CLIENT DEBUG: MazeClientHandler Listener notified");
			}
			/* 
			 * The plan is to:
			 * 1. Get a ticket for event.
			 * 2. Add event to both incoming and outgoing queues using SEQ#. 
			 * 
			 */
			
			/*
			 * Get the ticket. Would be nice if we did not have to reopen connection every time.
			 */
			
			/*
			 * Once we have SEQ# add to 
			 */
			
			
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
	
	/* New variables */
	// Store all the client sockets here so that we can send out local events to all clients from inside ClientUpdate method.
	public static Set<ClientLocation> clientS;
	// Store socket to ticket service so we can request SEQ# from inside ClientUpdate method.
	public static Socket ticketS;
	// Counter to keep track of incoming queue
	public static int currentEvent;
	// We can use HashMap as incoming queue. All we have to do is use SEQ# as identifier.
	public static HashMap<Integer,ClientEvent> incomingQ;



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