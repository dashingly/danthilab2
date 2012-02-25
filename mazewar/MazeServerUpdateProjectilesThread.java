import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.Thread;

// This class generates an UpdateProjectilesThread command and broadcasts it to all clients every 200ms
public class MazeServerUpdateProjectilesThread extends Thread {

	private Socket socket = null;
	private Hashtable<String, Long>  cache = new Hashtable<String, Long>();
	
	public MazeServerUpdateProjectilesThread() {
		super("s");
		if (DEBUG)
			System.out.println("SERVER DEBUG: Created new Thread to generate updateProjectiles events");
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
		
		active = true;
		
		while (active) {
			// Create an updateProjectiles message for the clients
			MazePacket packetUpdateProjectiles = new MazePacket();
			packetUpdateProjectiles.type = MazePacket.UPDATE_PROJECTILES;
			MazeServer.addToServerInQueue(packetUpdateProjectiles);
			
			// Sleep 
			try {
					sleep(200);
			} catch(Exception e) {
					// Shouldn't happen.
			}
		}
	}

	
	/* Internals ******************************************************/   
	// Activates the MazeServerUpdateProjectiles
	private static boolean active = false;
	// Turns debug messages on/off
	private static boolean DEBUG = true;
}
