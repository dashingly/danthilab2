import java.net.*;
import java.io.*;
import java.util.*;

public class OnlineBrokerHandlerThread extends Thread{

	private Socket socket = null;
	private Hashtable<String, Long>  cache = new Hashtable<String, Long>();
	
	public OnlineBrokerHandlerThread(Socket socket) {
		super("OnlineBrokerHandlerThread");
		this.socket = socket;
		System.out.println("Created new Thread to handle client");
	}
	
	public void run() {

		boolean gotByePacket = false;
		
		//parse the nasdaq file, and build a cache
		buildCache();
		
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			BrokerPacket packetFromClient;
			
			/* stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
			
			
			while (( packetFromClient = (BrokerPacket) fromClient.readObject()) != null) {
				/* create a packet to send reply back to client */
				BrokerPacket packetToClient = new BrokerPacket();
				packetToClient.type = BrokerPacket.BROKER_QUOTE;
				
				/* process request */
				if(packetFromClient.type == BrokerPacket.BROKER_REQUEST) {
					
					//look up the quote from the cache
					packetToClient.symbol = packetFromClient.symbol;
					packetToClient.quote = (Long)cache.get(packetFromClient.symbol.toLowerCase());
					if( packetToClient.quote == null) 
						packetToClient.quote = Long.parseLong("0");
					
					System.out.println("From Client: " + packetFromClient.symbol);
				
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
				
				/* Sending an ECHO_NULL || ECHO_BYE means quit */
				if (packetFromClient.type == BrokerPacket.BROKER_NULL || packetFromClient.type == BrokerPacket.BROKER_BYE) {
					gotByePacket = true;
					packetToClient = new BrokerPacket();
					packetToClient.type = BrokerPacket.BROKER_BYE;
					toClient.writeObject(packetToClient);
					break;
				}
				
				/* if code comes here, there is an error in the packet */
				System.err.println("ERROR: Unknown BROKER_* packet!!");
				System.exit(-1);
			}
			
			/* cleanup when client exits */
			fromClient.close();
			toClient.close();
			socket.close();

		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}
	
        // build the cache out of the stock file
	private void buildCache() {
		try {
			FileInputStream fstream = new FileInputStream("nasdaq");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;

			while ((line = br.readLine()) != null) {
				parseLine(line);
			}
			in.close();
		} catch (Exception e) {
			/* just print the error stack and exit. */
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	// helper function to parse each line of the file
	private void parseLine(String line) {
		
		// check if the line is blank
		if (line.trim().length() != 0) {
			StringTokenizer st;
			String symbol, inputQuote;
			Long quote;

			st = new StringTokenizer(line, " ");
			
			// remove leading and trailing whitespace from each field
			symbol = st.nextToken().trim();
			inputQuote = st.nextToken().trim();
			// convert quote to Long object
			quote = Long.parseLong(inputQuote);

			// add each (symbol, quote) pair to the cache
			cache.put(symbol, quote);
		}
	}

}
