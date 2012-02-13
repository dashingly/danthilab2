import java.util.Hashtable;

// subclass for tse
public class TSECache extends Cache{


	private static TSECache singleCache = null;
	//private Hashtable<String, Long>  cache = null;
	
	private TSECache(){
		cache = new Hashtable<String, Long>();
	}
	
	public static TSECache getInstance() {
		if (singleCache == null) {
			singleCache = new TSECache();
		}
		return singleCache;
	}
}
