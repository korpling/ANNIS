package annis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import annis.dao.Match;
import annis.dao.Span;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnisNode.TextMatching;

public class WekaDaoHelper implements ResultSetExtractor {
	
	private String nullValue;
	
	public WekaDaoHelper() {
		setNullValue("");
	}

	// FIXME: eigentlich könnte findMatches schon alle Informationen liefern
	public String createSqlQuery(List<Match> matches) {
		SortedSet<Long> ids = new TreeSet<Long>();
		
		for (Match match : matches) {
			for (Span span : match) {
				ids.add(span.getStructId());
			}
		}
		
		String template = "SELECT node.id, node.token_index, node.span, node_annotation.* FROM node, node_annotation WHERE node.id IN ( :ids ) and node_annotation.node_ref = node.id";
		
		String sql = template
			.replace(":ids", StringUtils.join(ids, ", "));
		
		return sql;
	}

	public List<AnnisNode> extractData(ResultSet resultSet) throws SQLException,
			DataAccessException {
		Map<Long, AnnisNode> nodesById = new HashMap<Long, AnnisNode>();
		
		while (resultSet.next()) {
			long id = resultSet.getLong("node_ref");
			if ( ! nodesById.containsKey(id) )
				nodesById.put(id, new AnnisNode(id));
			AnnisNode annisNode = nodesById.get(id);
			
			String span = resultSet.getString("span");
			Long tokenIndex = resultSet.getLong("token_index");
			if (resultSet.wasNull()) {
				tokenIndex = null;
			} else {
				annisNode.setTokenIndex(tokenIndex);
				annisNode.setSpannedText(span);
			}
			
			String namespace = resultSet.getString("namespace");
			String name = resultSet.getString("name");
			String value = resultSet.getString("value");
			annisNode.addNodeAnnotation(
					new Annotation(namespace, name, value, TextMatching.EXACT));
		}
		
		return new ArrayList<AnnisNode>(nodesById.values());
	}

	public String exportAsWeka(List<AnnisNode> annotatedNodes, List<Match> matches) {
		StringBuffer sb = new StringBuffer();
		
		// fn: id -> AnnisNode
		Map<Long, AnnisNode> nodeById = new HashMap<Long, AnnisNode>();
		for (AnnisNode node : annotatedNodes) {
			nodeById.put(node.getId(), node);
		}
		
		// header: relation name (unused)
		sb.append("@relation name\n");
		sb.append("\n");
		
		// column names for each node variable
		SortedMap<Integer, SortedSet<String>> columnsByNodePos = new TreeMap<Integer, SortedSet<String>>();
		for (int i = 0; i < matches.size(); ++i) {
			Match match = matches.get(i);
			for (int j = 0; j < match.size(); ++j) {
				Span span = match.get(j);
				AnnisNode annisNode = nodeById.get(span.getStructId());
				if (columnsByNodePos.get(j) == null)
					columnsByNodePos.put(j, new TreeSet<String>());
				for (Annotation annotation : annisNode.getNodeAnnotations())
					columnsByNodePos.get(j).add(annotation.getQualifiedName());
			}
		}
		
		int count = columnsByNodePos.keySet().size();
		for (int j = 0; j < count; ++j) {
			sb.append("@attribute " + fullColumnName(j + 1, "id") + " string\n");
			sb.append("@attribute " + fullColumnName(j + 1, "token") + " string\n");
			SortedSet<String> annotationNames = columnsByNodePos.get(j);
			for (String name : annotationNames) {
				sb.append("@attribute " + fullColumnName(j + 1, name) + " string\n");
			}
		}
		sb.append("\n@data\n\n");
		
		// values
		for (Match match : matches) {
			List<String> line = new ArrayList<String>();
			int k = 0;
			for (; k < match.size(); ++k) {
				Span span = match.get(k);
				AnnisNode annisNode = nodeById.get(span.getStructId());
				Map<String, String> valueByName = new HashMap<String, String>();
				for (Annotation annotation : annisNode.getNodeAnnotations()) {
					valueByName.put(annotation.getQualifiedName(), annotation.getValue());
				}
				line.add("'" + annisNode.getId() + "'");
				line.add("'" + (annisNode.isToken() ? annisNode.getSpannedText() : "NULL") + "'");
				for (String name : columnsByNodePos.get(k)) {
					if (valueByName.containsKey(name))
						line.add("'" + valueByName.get(name) + "'");
					else
						line.add("'NULL'");
				}
			}
			for (int l = k; l < count; ++l) {
				line.add("'NULL'");
				for (int m = 0; m <= columnsByNodePos.get(l).size(); ++m) {
					line.add("'NULL'");
				}
			}
			sb.append(StringUtils.join(line, ","));
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	private String fullColumnName(int i, String name) {
		return "#" + i + "_" + name;
	}
	
	public String getNullValue() {
		return nullValue;
	}

	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}


}