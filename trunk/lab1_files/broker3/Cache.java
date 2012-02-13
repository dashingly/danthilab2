import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.StringTokenizer;


public abstract class Cache {
	 Hashtable<String, Long>  cache = null;
	 
	 public void buildCache(String broker){
		//System.out.println("in building cache");
			try {
				FileInputStream fstream = new FileInputStream(broker);
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
