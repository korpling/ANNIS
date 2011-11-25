/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.dao;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.sql.ResultSet;
import java.sql.SQLException;

import annis.model.AnnisNode;

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
		annisNode.setSpannedText(stringValue(resultSet, "span"));
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