import java.net.*;
import java.io.*;
import java.util.*;

public class MazeNamingService  {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		ServerSocket serverSocket = null;
		boolean listening = true;
		
		

        try {
        	if(args.length == 1) {
        		NS_port = Integer.parseInt(args[0]);
        		serverSocket = new ServerSocket(NS_port);
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }
        
        /* Spawn Ticketing service */
        TicketService tik = new TicketService(NS_port);
        
        while (listening) {
        	new MazeNamingServiceHandlerThread(serverSocket.accept()).start();
        }

        serverSocket.close();
	}
	
	public static int NS_port = 0;

}
