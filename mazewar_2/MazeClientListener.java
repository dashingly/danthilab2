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

	public MazeClientListener(String my_hostname, int my_port) {
		this.my_hostname = my_hostname;
		this.my_port = my_port;
		
		System.out.println("Created new MazeClientListenerThread to listen to incoming client connections");
		
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
				System.out.println("CLIENT DEBUG: Client " + my_hostname + " listening");
		
		try {
			while (listening) {
				new MazeServerHandlerThread(listeningSocket.accept()).start();
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

}
