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
}
