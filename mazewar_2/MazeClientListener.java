// File: MazeClientListener.java
// Author: TM

// Just copied and pasted the import list from MazeImpl.java
import java.lang.Thread;
import java.lang.Runnable;
import java.io.*;
import java.util.*;
import java.net.*;

// Listens to other client connections
public class MazeClientListener implements Runnable {

	public MazeClientListener(String my_hostname, int my_port, MazeClientHandler clientHandler) {
		this.my_hostname = my_hostname;
		this.my_port = my_port;
		this.clientHandler = clientHandler;
		
		//Initialize the queue
		incomingQ = new Hashtable<Integer, MazePacket>();
		
		System.out.println("Created new MazeClientListener in instance to listen to incoming client connections");
		
		// Start the MazeClientListener
		thread = new Thread(this);
		thread.start();
	}
	
	public void run() {
		
		ServerSocket listeningSocket = null;
		
		// Open socket for listening purposes
		try {
			listeningSocket = new ServerSocket(my_port);
		} catch (IOException e) {
			System.err.println("ERROR: Could not listen on port!");
			System.exit(-1);
		}
		
		boolean listening = true;
		
		if (DEBUG)
				System.out.println("[CLIENT DEBUG] Client " + my_hostname + " listening");
		
		try {
			while (listening) {
				new MazeClientListenerThread(listeningSocket.accept()).start();
			}
			listeningSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static synchronized void add2q(int seq, MazePacket pack)
	{
		/* First add the packet */
		/*
		HashMap<Integer,MazePacket> temp;
		temp = MazeClientHandler.getIncomingQ();
		temp.put(seq, pack);
		MazeClientHandler.setIncomingQ(temp);
		*/
		incomingQ.put(seq, pack);
		
		System.out.println("[CLIENT LISTENER] Current event before while is " + currentEvent);
		
		/* Check if we can dequeue something */
		while (incomingQ.containsKey(currentEvent))
		{
			// Dequeue
			MazePacket p = incomingQ.get(currentEvent);
			Client curClient = MazeClientListener.clientHandler.clientSet.get(p.ClientName);
			
			
			switch (p.ce) {
			case MOVE_FORWARD:
				MazeClientHandler.maze.moveClientForward(curClient);
				if(DEBUG) 
					System.out.println("[CLIENT DEBUG] Server indicates client " + p.ClientName + " is moving forward");
				break;
			case MOVE_BACKWARD:
				MazeClientHandler.maze.moveClientBackward(curClient);
				if(DEBUG) 
					System.out.println("[CLIENT DEBUG] Server indicates client " + p.ClientName + " is moving backwards");
				break;
			case TURN_LEFT:
				MazeClientHandler.maze.rotateClientLeft(curClient);
				if(DEBUG) 
					System.out.println("[CLIENT DEBUG] Server indicates client " + p.ClientName + " is turning left");
				break;
			case TURN_RIGHT:
				MazeClientHandler.maze.rotateClientRight(curClient);
				if(DEBUG) 
					System.out.println("[CLIENT DEBUG] Server indicates client " + p.ClientName + " is turning right");
				break;
			case FIRE:
				MazeClientHandler.maze.clientFire(curClient);
				if(DEBUG) 
					System.out.println("[CLIENT DEBUG] Server indicates client " + p.ClientName + " is firing");
				break;
			default:
				if(DEBUG) 
					System.out.println("[CLIENT DEBUG] Unknown event from server " + p.ClientName);
			}
			// Remove current event from the queue
			incomingQ.remove(currentEvent);
			// Increment counter
			increment();
			//currentEvent++;
			System.out.println("[CLIENT LISTENER] Current event is " + currentEvent);
		}
	}
	
	public static synchronized void increment()
	{
		currentEvent++;
	}
	
	

	
	/* Internals ******************************************************/    
	// Local Client Info
	private static String my_hostname = null;
	private static int my_port = 0;
	// Thread
	private final Thread thread;
	// Turns debug messages on/off
	private static boolean DEBUG = true;
	
	// Reference to the clientHandler (to access the clientSet, and the maze)
	public static MazeClientHandler clientHandler;
	
	// Counter to keep track of the position within incoming queue
	private static int currentEvent = 1;
	
	/* Queue */
	/*
	 *  We can use HashMap as incoming queue. All we have to do is use SEQ# as identifier.
	 *  Making it public for other threads to see. 
	 */
	public static Hashtable<Integer,MazePacket> incomingQ;
	
	// Need to copy those locally: the ClientEvent class protects those
	private static final int MOVE_FORWARD 	= 0;
	private static final int MOVE_BACKWARD 	= 1;
	private static final int TURN_LEFT 		= 2;
	private static final int TURN_RIGHT 	= 3;
	private static final int FIRE 			= 4;
	private static final int ADD 			= 7;
}
