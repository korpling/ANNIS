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
		return 	"SELECT id, name, text, tokens " +
				"FROM corpus_stats";
	}
	
	public AnnisCorpus mapRow(ResultSet rs, int rowNum) throws SQLException {
		AnnisCorpusImpl corpus = new AnnisCorpusImpl();
		corpus.setId(rs.getLong("id"));
		corpus.setName(rs.getString("name"));
		corpus.setTextCount(rs.getInt("text"));
		corpus.setTokenCount(rs.getInt("tokens"));
		return corpus;
	}
	
}