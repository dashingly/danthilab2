import java.util.Hashtable;

// subclass for nasdaq
public class NASDAQCache extends Cache{
	

	private static NASDAQCache singleCache = null;
	//private Hashtable<String, Long>  cache = null;
	
	private NASDAQCache(){
		cache = new Hashtable<String, Long>();
	}
	
	public static NASDAQCache getInstance() {
		if (singleCache == null) {
			singleCache = new NASDAQCache();
		}
		return singleCache;
	}

}
