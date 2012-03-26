import java.net.*;
import java.io.*;
import java.util.*;

public class TicketService{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		ServerSocket serverSocket = null;
		boolean listening = true;

		System.out.println("[Ticket Service] Up and Running");
		
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
	/*
	
	public static void main(String[] args) throws IOException{
		ServerSocket serverSocket = null;
		boolean listening = true;

        try {
        	TS_port = MazeNamingService.NS_port;
        	serverSocket = new ServerSocket();
        	
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }
        SequenceNumber = 0;
        
        System.out.println("[TICKET SERVER] Up and running.");
        
        while (listening) {
        	try {
				new TicketServiceThread(serverSocket.accept()).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	// Sequence Number
	private static int SequenceNumber = 0;
	
	private static int TS_port = 0;
	
	// This is simple synchronized (critical section) counter.
	public static synchronized int getSeqs()
	{
		SequenceNumber++;
		return SequenceNumber;
	}

}
