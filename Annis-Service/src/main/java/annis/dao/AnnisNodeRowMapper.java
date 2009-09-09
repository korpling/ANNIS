package annis.dao;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.sql.ResultSet;
import java.sql.SQLException;

import annis.model.AnnisNode;
import annis.model.AnnisNode.TextMatching;

public class AnnisNodeRowMapper extends AbstractModelRowMapper<AnnisNode> {
	
	public AnnisNode mapRow(ResultSet resultSet, int rowNum)
			throws SQLException {
		AnnisNode annisNode = new AnnisNode(longValue(resultSet, "id"));
		
		annisNode.setCorpus(longValue(resultSet, "corpus_ref"));
		annisNode.setTextId(longValue(resultSet, "text_ref"));
		annisNode.setLeft(longValue(resultSet, "left"));
		annisNode.setRight(longValue(resultSet, "right"));
		annisNode.setNamespace(stringValue(resultSet, "namespace"));
		annisNode.setName(stringValue(resultSet, "name"));
		annisNode.setTokenIndex(longValue(resultSet, "token_index"));
		if (resultSet.wasNull())
			annisNode.setTokenIndex(null);
		annisNode.setSpannedText(stringValue(resultSet, "span"), TextMatching.EXACT);
		annisNode.setLeftToken(longValue(resultSet, "left_token"));
		annisNode.setRightToken(longValue(resultSet, "right_token"));
		
		return annisNode;
	}
	
	private String stringValue(ResultSet resultSet, String column) throws SQLException {
		return stringValue(resultSet, NODE_TABLE, column);
	}
	
	private long longValue(ResultSet resultSet, String column) throws SQLException {
		return longValue(resultSet, NODE_TABLE, column);
	}
	
}