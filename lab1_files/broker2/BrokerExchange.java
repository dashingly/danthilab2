import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class BrokerExchange {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		Socket brokerSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		try {
			/* variables for hostname/port */
			String hostname = "localhost";
			int port = 4444;
			
			if(args.length == 2 ) {
				hostname = args[0];
				port = Integer.parseInt(args[1]);
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
			brokerSocket = new Socket(hostname, port);

			out = new ObjectOutputStream(brokerSocket.getOutputStream());
			in = new ObjectInputStream(brokerSocket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}
		
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;

		System.out.println("Enter command or x for exit:");
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
			
			// deal with different operations
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
					System.out.println( packetFromServer.symbol + " added." );
					break;
				case BrokerPacket.EXCHANGE_REMOVE:
					System.out.println( packetFromServer.symbol + " removed." );
					break;
				case BrokerPacket.EXCHANGE_UPDATE:
					System.out.println( packetFromServer.symbol + " updated to " + packetFromServer.quote + ".");
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

		out.close();
		in.close();
		stdIn.close();
		brokerSocket.close();

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
