package annis.service.internal;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import annis.dao.AnnisDao;
import annis.dao.Match;
import annis.exceptions.AnnisException;
import de.deutschdiachrondigital.dddquery.DddQueryMapper;

public abstract class GetCountHandler extends AnnisServiceHandler<Integer> {

	private Logger log = Logger.getLogger(this.getClass());
	
	private AnnisDao dddQueryDao;
	
	public GetCountHandler() {
		super("GET COUNT");
	}
	
	public final String CORPUS_LIST = "corpusList";
	public final String ANNIS_QUERY = "annisQuery";
	
	@SuppressWarnings("unchecked")
	@Override
	protected Integer getResult(Map<String, Object> args) {
		List<Long> corpusList = (List<Long>) args.get(CORPUS_LIST);
		String annisQuery = (String) args.get(ANNIS_QUERY);
		
		List<Match> matches = matchQuery(corpusList, annisQuery);

		int count = matches.size();
		log.info("Found " + count + " matches");

		return count;
	}

	public List<Match> matchQuery(List<Long> corpusList, String annisQuery) {
		if (corpusList.isEmpty())
			throw new AnnisException("no corpus given");

		String dddQuery = dddQueryMapper().translate(annisQuery);
		log.debug("translated dddquery is: " + dddQuery);
		List<Match> matches = dddQueryDao.findMatches(null, dddQuery);
		return matches;
	}
	
	protected abstract DddQueryMapper dddQueryMapper();

}
