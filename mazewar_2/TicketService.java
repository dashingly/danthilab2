import java.net.*;
import java.io.*;
import java.util.*;

public class TicketService {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
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
        SequenceNumber = 0;
        
        while (listening) {
        	new TicketServiceThread(serverSocket.accept()).start();
        }

        serverSocket.close();
	}
	
	// Sequence Number
	private static int SequenceNumber;
	
	// This is simple synchronized (critical section) counter.
	public static synchronized int getSeqs()
	{
		SequenceNumber++;
		return SequenceNumber;
	}

}
