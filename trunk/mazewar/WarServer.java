/*
*	Created by Daniil Shevelev and Thierry Moreau for ECE419
*/

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * The entry point and glue code for the game.  It also contains some helpful
 * global utility methods.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: Mazewar.java 371 2004-02-10 21:55:32Z geoffw $
 */

public class WarServer{

        /**
         * The default width of the {@link Maze}.
         */
        private final int mazeWidth = 20;

        /**
         * The default height of the {@link Maze}.
         */
        private final int mazeHeight = 10;

        /**
         * The default random seed for the {@link Maze}.
         * All implementations of the same protocol must use 
         * the same seed value, or your mazes will be different.
         */
        private final int mazeSeed = 42;

        /**
         * The {@link Maze} that the game uses.
         */
        private Maze maze = null;

        /**
         * The {@link GUIClient} for the game.
         */
        private GUIClient guiClient = null;

        /**
         * The panel that displays the {@link Maze}.
         */
        private OverheadMazePanel overheadPanel = null;
        
        /**
         * Static method for performing cleanup before exiting the game.
         */
        public static void quit() {
                // Put any network clean-up code you might have here.
                // (inform other implementations on the network that you have 
                //  left, etc.)
                

                System.exit(0);
        }
       
        /** 
         * The place where all the pieces are put together. 
         */
        public WarServer() {
                
        		System.out.println("WarServer started!");
                
                // Create the maze
                maze = new MazeImpl(new Point(mazeWidth, mazeHeight), mazeSeed);
                assert(maze != null);
                
                //Initialize server queue
                squeue = new HashMap<Integer, Object>();
                
                //TODO: How do we simultaneously "push" part of the queue to all clients and then remove that part of the queue.
                // Throw up a dialog to get the GUIClient name.
                           
                
        }
        
        public Hashtable<Integer, Object> squeue; 

        
        /**
         * Server for the game. The goal of the server is to order the actions of different remote clients.
         * @param args Port number to listen on.
         */
        public static void main(String[] args) throws IOException{
        	//Create maze object
        	WarServer serve = new WarServer();
        	
        	//Start listening for incomming connections
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

        	/*
        	 * If there is a new client: 
        	 * 1. Spawn a thread.
        	 * 		a) Add client's actions to global server queue;
        	 * 		b) Broadcast the queue after some delay.        	 * 
        	 */
        	while (listening) {
        		new ClientHandler(serverSocket.accept(), serve).start();
        	}

        	serverSocket.close();
        }

}
