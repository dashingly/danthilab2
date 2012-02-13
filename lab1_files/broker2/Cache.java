import java.util.Hashtable;

// employs a Singleton Pattern to ensure only a single instance is present in the system
public class Cache {

	private static Cache singleCache = null;
	private Hashtable<String, Long>  cache = null;
	
	private Cache(){
		cache = new Hashtable<String, Long>();
	}
	
	public static Hashtable<String, Long> getInstance() {
		if (singleCache == null) {
			singleCache = new Cache();
		}
		return singleCache.cache;
	}
}
