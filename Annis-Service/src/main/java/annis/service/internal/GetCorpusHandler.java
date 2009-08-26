package annis.service.internal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import annis.service.ifaces.AnnisCorpusSet;
import annis.service.objects.AnnisCorpusImpl;
import annis.service.objects.AnnisCorpusSetImpl;

public class GetCorpusHandler extends AnnisServiceHandler<AnnisCorpusSet> {

	private Logger log = Logger.getLogger(this.getClass());
	
	public GetCorpusHandler() {
		super("GET CORPUS");
	}
	
	@Override
	protected AnnisCorpusSet getResult(Map<String, Object> args) {
		
		String sql = 
			"SELECT id, timestamp_id, name, text as n_texts, n_tokens " +
			"FROM corpus, corpus_stats " +
			"WHERE corpus_stats.corpus_ref = corpus.id and corpus.top_level = 'y'";
		
		ParameterizedRowMapper<AnnisCorpusImpl> rowMapper = new ParameterizedRowMapper<AnnisCorpusImpl>() {

			public AnnisCorpusImpl mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				AnnisCorpusImpl corpus = new AnnisCorpusImpl();
				corpus.setId(rs.getLong("id"));
				corpus.setName(rs.getString("name"));
				corpus.setTextCount(rs.getInt("n_texts"));
				corpus.setTokenCount(rs.getInt("n_tokens"));
				
				log.debug("found corpus #" + corpus.getId() + ": " + corpus.getName() + " (" + corpus.getTextCount() + " texts, " + corpus.getTokenCount() + " tokens)");
				
				return corpus;
			}
			
		};
		
		AnnisCorpusSet result = new AnnisCorpusSetImpl();
		result.addAll(getSimpleJdbcTemplate().query(sql, rowMapper));
		
		log.info("Found " + result.size() + " corpora.");

		return result;
	}
	
}
