package annisservice.handler;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import annisservice.AnnisResultSetBuilder;
import annisservice.ifaces.AnnisResultSet;
import de.deutschdiachrondigital.dddquery.helper.QueryExecution;
import de.deutschdiachrondigital.dddquery.sql.AnnotationRetriever;
import de.deutschdiachrondigital.dddquery.sql.Match;

public abstract class GetResultHandler extends AnnisServiceHandler<AnnisResultSet> {

	private Logger log = Logger.getLogger(this.getClass());
	
	// to retrieve annotations we first have to retrieve match list
	@Autowired protected GetCountHandler getCountHandler;
	
	public GetResultHandler() {
		super("GET RESULT");
	}
	
	public final String CORPUS_LIST = "corpusList";
	public final String ANNIS_QUERY = "annisQuery";
	public final String CONTEXT_LEFT = "contextLeft";
	public final String CONTEXT_RIGHT = "contextRight";
	public final String LIMIT = "limit";
	public final String OFFSET = "offset";
	
	@SuppressWarnings("unchecked")
	@Override
	protected AnnisResultSet getResult(Map<String, Object> args) {
		List<Long> corpusList = (List<Long>) args.get(CORPUS_LIST);
		String annisQuery = (String) args.get(ANNIS_QUERY);
		int contextLeft = (Integer) args.get(CONTEXT_LEFT);
		int contextRight = (Integer) args.get(CONTEXT_RIGHT);
		int limit = (Integer) args.get(LIMIT);
		int offset = (Integer) args.get(OFFSET);
		
		List<Match> match = getCountHandler.matchQuery(corpusList, annisQuery);

		AnnisResultSet annisResultSet = annisResultSetBuilder().buildResultSet(
				annotationRetriever().retrieveAnnotations(match, contextLeft, contextRight, limit, offset));
		
		log.info("Retrieved " + annisResultSet.size() + " results.");
		
		return annisResultSet;
	}
	
	protected abstract AnnotationRetriever annotationRetriever();
	protected abstract AnnisResultSetBuilder annisResultSetBuilder();
	protected abstract QueryExecution queryExecution();

}
