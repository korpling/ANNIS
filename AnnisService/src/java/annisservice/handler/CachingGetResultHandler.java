package annisservice.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import annisservice.MemoryUsage;
import annisservice.ifaces.AnnisResultSet;


public abstract class CachingGetResultHandler extends GetResultHandler {

	private Logger log = Logger.getLogger(this.getClass());
	
	// should results be cached?
	private boolean cacheEnabled;
	
	// result cache
	private Map<Map<String, Object>, AnnisResultSet> cache;
	
	public CachingGetResultHandler() {
		super();
		cache = Collections.synchronizedMap(new HashMap<Map<String,Object>, AnnisResultSet>());
		MemoryUsage.print();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected AnnisResultSet getResult(Map<String, Object> args) {
		
		if ( ! isCacheEnabled() )
			return super.getResult(args);
		
		if (cache.containsKey(args)) {
			AnnisResultSet annisResultSet = cache.get(args);
			log.debug(annisResultSet.size() + " results found in cache");
			return annisResultSet;
		} else {
			log.debug("result was NOT found in cache");

			AnnisResultSet results = super.getResult(args);
			cache.put(args, results);

			MemoryUsage.print();
			
			return results;
		}
	}

	///// Getter / Setter

	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}
	
}
