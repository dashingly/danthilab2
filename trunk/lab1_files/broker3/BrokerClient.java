import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class BrokerClient {

	// Debug Variable
	static boolean DEBUG = true;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		// Name service connection
		Socket namingServiceSocket = null;
		ObjectOutputStream NS_out = null;
		ObjectInputStream NS_in = null;
		
		// Broker connection
		Socket brokerSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		// First, obtain hostname and port number of Name Service
		try {
			/* variables for hostname/port */
			String BrokerLookup_hostname = "localhost";
			int BrokerLookup_port = 4444;
			
			if(args.length == 2 ) {
				BrokerLookup_hostname = args[0];
				BrokerLookup_port = Integer.parseInt(args[1]);
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
		
		// Listen to User Input
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;

		System.out.println("Enter queries or x for exit:");
		System.out.print(">");
		while ((userInput = stdIn.readLine()) != null
				&& userInput.toLowerCase().indexOf("x") == -1) {
			
			// tokenize each line
			StringTokenizer st = new StringTokenizer(userInput, " ");
			String op;
			String stock_exchange;
			try {
				op = st.nextToken().trim();
			} catch (Exception ex) {
				System.out.println( "Incorrect Use" );
				System.out.print(">");
				continue;
			}
			
			if(op.equals("local")) {
				// 1 - Read the exchange field from user input
				try {
					stock_exchange = st.nextToken().trim();
				} catch (Exception ex) {
					System.out.println( "Incorrect Use. Use: local [BROKER]" );
					System.out.print(">");
					continue;
				}
				// 2 - Make a new LOOKUP_REQUEST packet to send to the Name Service
				BrokerPacket packetToNaming = new BrokerPacket();
				packetToNaming.type = BrokerPacket.LOOKUP_REQUEST;
				packetToNaming.exchange = stock_exchange;
				NS_out.writeObject(packetToNaming);
				if(DEBUG) {
					System.out.println ("[CLIENT DEBUG] 1. Successfully sent IP lookup request for " + packetToNaming.exchange);
				}
				
				// 3 - Name Service Reply
				BrokerPacket packetFromNaming;
				packetFromNaming = (BrokerPacket) NS_in.readObject();
				if (packetFromNaming.type == BrokerPacket.LOOKUP_REPLY)
					if(DEBUG) {
						System.out.println ("[CLIENT DEBUG] 2. Successfully obtained broker IP: " + packetFromNaming.locations[0].broker_host + " " + packetFromNaming.locations[0].broker_port);
					}
				else if (packetFromNaming.type == BrokerPacket.BROKER_ERROR){
					System.err.println("ERROR: Couldn't successfully obtain broker IP");
					System.exit(1);
				}
				
				// 4 - If the BrokerSocket is already open, close it
				if (brokerSocket != null) {
					// Say bye to other broker
					BrokerPacket packetBye = new BrokerPacket();
					packetBye.type = BrokerPacket.BROKER_BYE;
					out.writeObject(packetBye);
					
					// close, close, close
					brokerSocket.close();
					out.close();
					in.close();
					if(DEBUG) {
						System.out.println ("[CLIENT DEBUG] 2.5 Closing existing connection with Broker");
					}
				}
				
				// 5 - Open connection with Broker
				try {
					String hostname = packetFromNaming.locations[0].broker_host;
					int port = packetFromNaming.locations[0].broker_port;
					
					brokerSocket = new Socket(hostname, port);
					
					out = new ObjectOutputStream(brokerSocket.getOutputStream());
					in = new ObjectInputStream(brokerSocket.getInputStream());
					
					System.out.println (stock_exchange + " as local");
					
					if(DEBUG) {
						System.out.println ("[CLIENT DEBUG] 3. Successfully opened connection with Broker");
					}
				} catch (UnknownHostException e) {
					System.err.println("ERROR: Don't know where to connect!!");
					System.exit(1);
				} catch (IOException e) {
					System.err.println("ERROR: Couldn't get I/O for the connection.");
					System.exit(1);
				}
				
				System.out.print(">");
				continue;
				
			} else { 
				// Check that a local broker has been set
				if (brokerSocket == null) {
					System.out.println( "Please scpecify a local broker. Use: local [BROKER]" );
					System.out.print(">");
					continue;
				} else {
					/* make a new request packet */
					BrokerPacket packetToServer = new BrokerPacket();
					packetToServer.type = BrokerPacket.BROKER_REQUEST;
					packetToServer.symbol = op;
					if (DEBUG) { 
						System.out.println("[CLIENT DEBUG] Quote to broker: " + packetToServer.symbol);
					}
					out.writeObject(packetToServer);
				}
			}

			/* print server reply */
			BrokerPacket packetFromServer;
			packetFromServer = (BrokerPacket) in.readObject();

			if (packetFromServer.type == BrokerPacket.BROKER_QUOTE)
				System.out.println("Quote from broker: " + packetFromServer.quote);
			else if (packetFromServer.type == BrokerPacket.BROKER_ERROR){
				System.out.println(packetFromServer.symbol + " invalid");
			}

			/* re-print console prompt */
			System.out.print(">");
		}

		/* tell server that i'm quitting */
		BrokerPacket packetByeServer = new BrokerPacket();
		packetByeServer.type = BrokerPacket.BROKER_BYE;
		out.writeObject(packetByeServer);
		
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

}
