import java.net.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.io.*;

public class OnlineBroker {
	
	static Hashtable<String, Long> globalLookUpTable = Cache.getInstance();
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		ServerSocket serverSocket = null;
        boolean listening = true;
        
        buildCache();

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
        
        while (listening) {
        	new OnlineBrokerHandlerThread(serverSocket.accept()).start();
        }

        serverSocket.close();
	}
	private static void buildCache() {
		System.out.println("in building cache");
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
	
	private static void parseLine(String line) {
		
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
			globalLookUpTable.put(symbol, quote);
		}
	}
}
