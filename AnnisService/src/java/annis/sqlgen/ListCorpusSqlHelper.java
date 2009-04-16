/**
 * 
 */
package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import annis.service.ifaces.AnnisCorpus;
import annis.service.objects.AnnisCorpusImpl;

public class ListCorpusSqlHelper implements ParameterizedRowMapper<AnnisCorpus> {

	public String createSqlQuery() {
		return 	"SELECT id, name, text as n_texts, n_tokens " +
				"FROM corpus, corpus_stats " +
				"WHERE corpus_stats.corpus_ref = corpus.id and corpus.top_level = 'y'";
	}
	
	public AnnisCorpus mapRow(ResultSet rs, int rowNum) throws SQLException {
		AnnisCorpusImpl corpus = new AnnisCorpusImpl();
		corpus.setId(rs.getLong("id"));
		corpus.setName(rs.getString("name"));
		corpus.setTextCount(rs.getInt("n_texts"));
		corpus.setTokenCount(rs.getInt("n_tokens"));
		return corpus;
	}
	
}