import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class BrokerExchange {

	// Debug Variable
	static boolean DEBUG = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		// Name service connection
		Socket namingServiceSocket = null;
		ObjectOutputStream NS_out = null;
		ObjectInputStream NS_in = null;
		
		// Broker connection
		Socket brokerSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		/* variables for hostname/port of naming service */
		String BrokerLookup_hostname = "localhost";
		int BrokerLookup_port = 4444;
		String exchange_Name = "";
		
		// 1 - Read in arguments
		try {
			if(args.length == 3 ) {
				BrokerLookup_hostname = args[0];               // $1 = hostname of where BrokerLookupServer is located
				BrokerLookup_port = Integer.parseInt(args[1]); // $2 = port # where BrokerLookupServer is listening
				exchange_Name = args[2];                       // $3 = name of broker you are connecting to ("nasdaq" or "tse")
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
			namingServiceSocket = new Socket(BrokerLookup_hostname, BrokerLookup_port);

			NS_out = new ObjectOutputStream(namingServiceSocket.getOutputStream());
			NS_in = new ObjectInputStream(namingServiceSocket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}
		
		// 2 - Make a new LOOKUP_REQUEST packet to send to the Name Service
		BrokerPacket packetToNaming = new BrokerPacket();
		packetToNaming.type = BrokerPacket.LOOKUP_REQUEST;
		packetToNaming.exchange = exchange_Name;
		NS_out.writeObject(packetToNaming);
		if(DEBUG) {
			System.out.println ("[EXCHANGE DEBUG] 1. Successfully sent IP lookup request for " + packetToNaming.exchange);
		}
		
		// 3 - Name Service Reply
		BrokerPacket packetFromNaming;
		packetFromNaming = (BrokerPacket) NS_in.readObject();
		if (packetFromNaming.type == BrokerPacket.LOOKUP_REPLY)
			if(DEBUG) {
				System.out.println ("[EXCHANGE DEBUG] 2. Successfully obtained broker IP: " + packetFromNaming.locations[0].broker_host + " " + packetFromNaming.locations[0].broker_port);
			}
		else if (packetFromNaming.type == BrokerPacket.BROKER_ERROR){
			System.err.println("ERROR: Couldn't successfully obtain broker IP");
			System.exit(1);
		}
		
		// 4 - Open connection with Broker
		try {
			String hostname = packetFromNaming.locations[0].broker_host;
			int port = packetFromNaming.locations[0].broker_port;
			
			brokerSocket = new Socket(hostname, port);
			
			out = new ObjectOutputStream(brokerSocket.getOutputStream());
			in = new ObjectInputStream(brokerSocket.getInputStream());
			
			if(DEBUG) {
				System.out.println ("[EXCHANGE DEBUG] 3. Successfully opened connection with Broker");
			}
		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}
		
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;

		System.out.print(">");
		while ((userInput = stdIn.readLine()) != null
				&& userInput.toLowerCase().indexOf("x") == -1) {
			
			// tokenize each line
			StringTokenizer st;
			st = new StringTokenizer(userInput, " ");
			String op, symbol;
			try {
				op = st.nextToken().trim();
				symbol = st.nextToken().trim();
			} catch (Exception ex) {
				System.out.println( "Incorrect Use: add/remove/update [SYMBOL] [QUOTE]" );
				System.out.print(">");
				continue;
			}
			
			
			
			/* make a new request packet */
			BrokerPacket packetToServer = new BrokerPacket();
			
			
			if(op.equals("add"))
				packetToServer.type = BrokerPacket.EXCHANGE_ADD;
			else if(op.equals("remove"))
				packetToServer.type = BrokerPacket.EXCHANGE_REMOVE;
			else if(op.equals("update")){
				packetToServer.type = BrokerPacket.EXCHANGE_UPDATE;
				String quote;
				try {
					quote = st.nextToken().trim();
				} catch (Exception ex) {
					System.out.println( "Missing Quote. Use: update [SYMBOL] [QUOTE]" );
					System.out.print(">");
					continue;
				}
				
				if (checkIfNumber(quote) == false) {
					System.out.println( "Quote is not a number. Use: update [SYMBOL] [QUOTE]" );
					System.out.print(">");
					continue;
				} else {
					packetToServer.quote = Long.parseLong(quote);
				}
			}
			else {
				// FIXME: This could be more elegant
				// At least it works for now...
				System.out.println( "Invalid command. Use: add/remove/update [SYMBOL] [QUOTE]" );
				System.out.print(">");
				continue;
			}
			
			packetToServer.symbol = symbol;
			out.writeObject(packetToServer);

			/* print server reply */
		    BrokerPacket packetFromServer;
			packetFromServer = (BrokerPacket) in.readObject();

			// if error 
			if(packetFromServer.error_code < 0)
			{
				switch(packetFromServer.error_code){
				case BrokerPacket.ERROR_SYMBOL_EXISTS:
					System.out.println( packetFromServer.symbol + " exists." );
					break;
				case BrokerPacket.ERROR_OUT_OF_RANGE:
					System.out.println( packetFromServer.symbol + " out of range." );
					break;
				case BrokerPacket.ERROR_INVALID_SYMBOL:
					System.out.println( packetFromServer.symbol + " invalid." );
					break;
				}
			
			}
			else { // no error
				switch(packetToServer.type){
				case BrokerPacket.EXCHANGE_ADD:
					System.out.println( packetFromServer.symbol + " added" );
					break;
				case BrokerPacket.EXCHANGE_REMOVE:
					System.out.println( packetFromServer.symbol + " removed" );
					break;
				case BrokerPacket.EXCHANGE_UPDATE:
					System.out.println( packetFromServer.symbol + " updated to " + packetFromServer.quote);
					break;
				}
			}

			/* re-print console prompt */
			System.out.print(">");
		}

		/* tell server that i'm quitting */
		BrokerPacket packetToServer = new BrokerPacket();
		packetToServer.type = BrokerPacket.BROKER_BYE;
		out.writeObject(packetToServer);
		
		/* tell naming service that i'm quitting */
		BrokerPacket packetByeNaming = new BrokerPacket();
		packetByeNaming.type = BrokerPacket.BROKER_BYE;
		NS_out.writeObject(packetByeNaming);

		out.close();
		in.close();
		NS_out.close();
		NS_in.close();
		stdIn.close();
		brokerSocket.close();
		namingServiceSocket.close();

	}
	
	// Function used to validate if input String is a number
	public static boolean checkIfNumber(String in) {
		try {
			Integer.parseInt(in);
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;
    }

}
