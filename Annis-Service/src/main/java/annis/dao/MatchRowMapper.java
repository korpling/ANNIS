/**
 * 
 */
package annis.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class MatchRowMapper implements ParameterizedRowMapper<Match> {
	public Match mapRow(ResultSet rs, int rowNum) throws SQLException {
		Match match = new Match();

		// 4 columns per node (id, text_ref, left_token, right_token)
		ResultSetMetaData metaData = rs.getMetaData();
		int columns = metaData.getColumnCount() / 4;
		
		for (int i = 0; i < columns; ++i) {
			int id = rs.getInt((i * 4 + 1));
			if (rs.wasNull())
				// no more matches in this row if an id was NULL
				break;

			int textRef = rs.getInt((i * 4 + 2));
			int leftToken = rs.getInt((i * 4 + 3));
			int rightToken = rs.getInt((i * 4 + 4));
			
			match.add(new Span(id, textRef, leftToken, rightToken));
		}
		
//		for (int i = 0; i < columns; ++i) {
//			int id = rs.getInt(i + 1);
//			if (rs.wasNull())
//				break;
//			match.add(new Span(id, id, id, id));
//		}
		
		return match;
	}
}