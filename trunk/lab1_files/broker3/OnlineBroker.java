import java.net.*;
import java.util.Hashtable;
import java.io.*;

public class OnlineBroker {

	// Debug Variable
	static boolean DEBUG = true;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// Sockets etc.
		ServerSocket serverSocket = null;
		Socket namingServiceSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		boolean listening = true;
		
		// Read in command arguments
		String BrokerLookup_hostname = "localhost";
		int BrokerLookup_port = 4444;
		int my_port = 4444;
		String exchange_Name = "";
		if(args.length == 4 ) {
			BrokerLookup_hostname = args[0];               // $1 = hostname of BrokerLookupServer
			BrokerLookup_port = Integer.parseInt(args[1]); // $2 = port where BrokerLookupServer is listening
			my_port = Integer.parseInt(args[2]);           // $3 = port where I will be listening
			exchange_Name = args[3];                       // $4 = my name ("nasdaq" or "tse")
		} else {
			System.err.println("ERROR: Invalid arguments!");
			System.exit(-1);
		}
		
		// Fist obtain the local host name
		InetAddress addr = InetAddress.getLocalHost();
		String hostname = addr.getHostName();
		
		// Second, register to Naming Service
		try {
			// 1 - Open Socket with the Naming Service
			namingServiceSocket = new Socket(BrokerLookup_hostname, BrokerLookup_port);
			out = new ObjectOutputStream(namingServiceSocket.getOutputStream());
			in = new ObjectInputStream(namingServiceSocket.getInputStream());
			if(DEBUG) {
				System.out.println ("[BROKER DEBUG] 1. Successfully obtained I/O for Name Service connection");
			}
			
			// 2 - Now let's register our IP to the naming service
			BrokerPacket packetToNaming = new BrokerPacket();
			BrokerLocation my_location[] = new BrokerLocation[1];
			my_location[0] = new BrokerLocation(hostname,my_port);
			packetToNaming.type = BrokerPacket.LOOKUP_REGISTER;
			packetToNaming.exchange = exchange_Name;
			packetToNaming.locations = my_location;
			out.writeObject(packetToNaming);
			if(DEBUG) {
				System.out.println ("[BROKER DEBUG] 2. Successfully sent LOOKUP_REGISTER request");
			}
			
			// 3 - Name Service Reply
			BrokerPacket packetFromNaming;
			packetFromNaming = (BrokerPacket) in.readObject();
			if (packetFromNaming.type == BrokerPacket.LOOKUP_REPLY)
				if(DEBUG) {
					System.out.println ("[BROKER DEBUG] 3. Successfully registered IP");
				}
			else if (packetFromNaming.type == BrokerPacket.BROKER_ERROR){
				System.err.println("ERROR: Couldn't successfully register IP");
				System.exit(1);
			}

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}
		
		
		// 4 - Then open Socket to listen to Client requests
		try {
			serverSocket = new ServerSocket(my_port);
			if(DEBUG) {
				System.out.println ("[BROKER DEBUG] 4. Successfully opened socket for listening to client requests");
			}
		} catch (IOException e) {
			System.err.println("ERROR: Could not listen on port!");
			System.exit(-1);
		}

		// Listen for client requests
		
		while (listening) {
			Hashtable<String, Long>  cache = getCacheforBroker(exchange_Name);
			new OnlineBrokerHandlerThread(serverSocket.accept(), exchange_Name, cache, 
					namingServiceSocket, out, in).start();
		}

		namingServiceSocket.close();
		serverSocket.close();
	}

	// little factory method....
	private static Hashtable<String, Long> getCacheforBroker(String broker){
		if(broker.toLowerCase().equals("nasdaq")){
			NASDAQCache.getInstance().buildCache(broker);
			return NASDAQCache.getInstance().cache;
		}
		else
			TSECache.getInstance().buildCache(broker);
			return TSECache.getInstance().cache;
	}
}
