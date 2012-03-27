import java.net.*;
import java.io.*;
import java.util.*;

public class TicketService implements Runnable{

	public TicketService(int nSPort) 
	{
		// TODO Auto-generated constructor stub
		TS_port = nSPort + 1;
		
		// Start the Ticket Service
		thread = new Thread(this);
		
		thread.start();
	}

	// Sequence Number
	private static int 	SequenceNumber = 0;
	private static int 	TS_port = 0;
	private final 		Thread thread;
	
	// This is simple synchronized (critical section) counter.
	public static synchronized int getSeqs()
	{
		SequenceNumber++;
		return SequenceNumber;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		ServerSocket serverSocket = null;
		boolean listening = true;

		System.out.println("[TICKET SERVICE] Up and Running");
		
        try {
        	if(TS_port > 0) {
        		serverSocket = new ServerSocket(TS_port);
        	} else {
        		System.err.println("[TICKET SERVICE] ERROR: Invalid port!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("[TICKET SERVICE] ERROR: Could not listen on port!");
            System.exit(-1);
        }
        SequenceNumber = 0;
        
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

}
