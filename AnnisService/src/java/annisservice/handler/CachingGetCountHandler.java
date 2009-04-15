package annisservice.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import annisservice.MemoryUsage;

import de.deutschdiachrondigital.dddquery.sql.Match;


public abstract class CachingGetCountHandler extends GetCountHandler {

	private Logger log = Logger.getLogger(this.getClass());
	
	// should results be cached?
	private boolean cacheEnabled;
	
	// result cache
	private Map<Map<String, Object>, List<Match>> cache;
	
	public CachingGetCountHandler() {
		super();
		cache = Collections.synchronizedMap(new HashMap<Map<String,Object>, List<Match>>());
		
		MemoryUsage.print();
	}
	
	@Override
	public List<Match> matchQuery(List<Long> corpusList, String annisQuery) {
		
		if ( ! isCacheEnabled() )
			return super.matchQuery(corpusList, annisQuery);
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(CORPUS_LIST, corpusList);
		args.put(ANNIS_QUERY, annisQuery);

		if (cache.containsKey(args)) {
			log.debug("result was found in cache");
			return cache.get(args);
		} else {
			log.debug("result was NOT found in cache");

			List<Match> matches = super.matchQuery(corpusList, annisQuery);
			cache.put(args, matches);
	
			MemoryUsage.print();

			return matches;
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
