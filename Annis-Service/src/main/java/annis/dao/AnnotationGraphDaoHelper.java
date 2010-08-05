package annis.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.ql.parser.QueryAnalysis;
import annis.sqlgen.SqlGenerator;
import annis.sqlgen.TableAccessStrategy;
import org.springframework.jdbc.core.JdbcTemplate;

@Deprecated
public class AnnotationGraphDaoHelper implements ResultSetExtractor {

	private Logger log = Logger.getLogger(this.getClass());

	public static class MatchGroupRowMapper implements ParameterizedRowMapper<String> {

		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getString("key");
		}
		
	}

  private String nodeTableViewName;
  private String matchedNodesViewName;
	
	// for inline query generation
	// FIXME: tests for dependencies missing
	private DddQueryParser dddQueryParser;
	private SqlGenerator sqlGenerator;
	private QueryAnalysis queryAnalysis;
	
	// row mappers for column data to model classes
	private AnnisNodeRowMapper annisNodeRowMapper;
	private EdgeRowMapper edgeRowMapper;
	private AnnotationRowMapper nodeAnnotationRowMapper;
	private AnnotationRowMapper edgeAnnotationRowMapper;
	private MatchGroupRowMapper matchGroupRowMapper;
	
	public AnnotationGraphDaoHelper() {
		matchGroupRowMapper = new MatchGroupRowMapper();
		
		// FIXME: totally ugly, but the query has fixed column names (and needs its own column aliasing)
		// TableAccessStrategyFactory wants a corpus selection strategy
		// solution: build AnnisNodes with API and refactor SqlGenerator to accept GROUP BY nodes
		Map<String, String> nodeColumns = new HashMap<String, String>();
		nodeColumns.put("namespace", "node_namespace");
		nodeColumns.put("name", "node_name");
		
		Map<String, String> nodeAnnotationColumns = new HashMap<String, String>();
		nodeAnnotationColumns.put("node_ref", "id");
		nodeAnnotationColumns.put("namespace", "node_annotation_namespace");
		nodeAnnotationColumns.put("name", "node_annotation_name");
		nodeAnnotationColumns.put("value", "node_annotation_value");
		
		Map<String, String> edgeAnnotationColumns = new HashMap<String, String>();
		nodeAnnotationColumns.put("rank_ref", "pre");
		edgeAnnotationColumns.put("namespace", "edge_annotation_namespace");
		edgeAnnotationColumns.put("name", "edge_annotation_name");
		edgeAnnotationColumns.put("value", "edge_annotation_value");
		
		Map<String, String> edgeColumns = new HashMap<String, String>();
		edgeColumns.put("node_ref", "id");
		edgeColumns.put("name", "edge_name");
		edgeColumns.put("namespace", "edge_name");
		
		Map<String, Map<String, String>> columnAliases = new HashMap<String, Map<String, String>>();
		columnAliases.put(TableAccessStrategy.NODE_TABLE, nodeColumns);
		columnAliases.put(TableAccessStrategy.NODE_ANNOTATION_TABLE, nodeAnnotationColumns);
		columnAliases.put(TableAccessStrategy.EDGE_ANNOTATION_TABLE, edgeAnnotationColumns);
		columnAliases.put(TableAccessStrategy.RANK_TABLE, edgeColumns);
		
		TableAccessStrategy tableAccessStrategy = new TableAccessStrategy(null);
		tableAccessStrategy.setColumnAliases(columnAliases);

		annisNodeRowMapper = new AnnisNodeRowMapper();
		annisNodeRowMapper.setTableAccessStrategy(tableAccessStrategy);
		
		edgeRowMapper = new EdgeRowMapper();
		edgeRowMapper.setTableAccessStrategy(tableAccessStrategy);
		
		nodeAnnotationRowMapper = new AnnotationRowMapper(TableAccessStrategy.NODE_ANNOTATION_TABLE);
		nodeAnnotationRowMapper.setTableAccessStrategy(tableAccessStrategy);
		
		edgeAnnotationRowMapper = new AnnotationRowMapper(TableAccessStrategy.EDGE_ANNOTATION_TABLE);
		edgeAnnotationRowMapper.setTableAccessStrategy(tableAccessStrategy);
	}
	
	// FIXME: there is only one graph and no matched nodes. {-1} as key only needed because of algorithm in extractData
	// create SQL query for a text id
	public String createSqlQuery(long textId) {
		String template = "SELECT DISTINCT\n"
			+ "\t'-1' AS key, facts.*\n"
			+ "FROM\n"
			+ "\tfacts AS facts\n"
			+ "WHERE\n" + "\tfacts.text_ref = :text_id\n"
			+ "ORDER BY facts.pre";
		String sql = template.replace(":text_id", String.valueOf(textId));
		return sql;
	}
	
	// kinda ugly
	public String createSqlQuery(List<Long> corpusList, int nodeCount, long offset, long limit, int left, int right) {
			
		// key for annotation graph matches
		StringBuffer keySb = new StringBuffer();
		keySb.append("(matches.id1");
		for (int i = 2; i <= nodeCount; ++i) {
			keySb.append(" || ',' || ");
			keySb.append("matches.id");
			keySb.append(i);
		}
		keySb.append(") AS key");
		String key = keySb.toString();
		
		// sql for matches
		StringBuffer matchSb = new StringBuffer();
		matchSb.append("SELECT * FROM ");
    matchSb.append(matchedNodesViewName);
		matchSb.append(" ORDER BY ");
		matchSb.append("id1");
		for (int i = 2; i <= nodeCount; ++i) {
			matchSb.append(", ");
			matchSb.append("id");
			matchSb.append(i);
		}
		matchSb.append(" OFFSET ");
		matchSb.append(offset);
		matchSb.append(" LIMIT ");
		matchSb.append(limit);
		String matchSql = matchSb.toString();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT\n");
		sb.append("\t");
		sb.append(key);
		sb.append(", facts.*\n");
		sb.append("FROM\n");
		sb.append("\t(");
		sb.append(matchSql);
		sb.append(") AS matches,\n");
		sb.append("\t");
    sb.append(nodeTableViewName);
    sb.append(" AS facts\n");
		sb.append("WHERE\n");
		sb.append("\t(facts.text_ref = matches.text_ref1 AND ((facts.left_token >= matches.left_token1 - " + left + " AND facts.right_token <= matches.right_token1 + " + right + ") OR (facts.left_token <= matches.left_token1 - " + left + " AND matches.left_token1 - " + left + " <= facts.right_token) OR (facts.left_token <= matches.right_token1 + " + right + " AND matches.right_token1 + " + right + " <= facts.right_token)))");
		for (int i = 2; i <= nodeCount; ++i) {
			sb.append(" OR\n");
			sb.append("\t(facts.text_ref = matches.text_ref");
			sb.append(i);
			sb.append(" AND ((facts.left_token >= matches.left_token");
			sb.append(i);
			sb.append(" - ");
			sb.append(left);
			sb.append(" AND facts.right_token <= matches.right_token");
			sb.append(i);
			sb.append(" + ");
			sb.append(right);
			sb.append(") OR (facts.left_token <= matches.left_token");
			sb.append(i);
			sb.append(" - ");
			sb.append(left);
			sb.append(" AND matches.left_token");
			sb.append(i);
			sb.append(" - ");
			sb.append(left);
			sb.append(" <= facts.right_token) OR (facts.left_token <= matches.right_token");
			sb.append(i);
			sb.append(" + ");
			sb.append(right);
			sb.append(" AND matches.right_token");
			sb.append(i);
			sb.append(" + ");
			sb.append(right);
			sb.append(" <= facts.right_token)))");
		}
		sb.append("\nORDER BY key, facts.pre");
		
    log.debug("SQL query with context:\n" + sb.toString());

		return sb.toString();
	}


  public List<AnnotationGraph> queryAnnotationGraph(JdbcTemplate jdbcTemplate, int nodeCount, List<Long> corpusList, String dddQuery, long offset, long limit, int left, int right)
  {
    return (List<AnnotationGraph>) jdbcTemplate.query(createSqlQuery(corpusList, nodeCount, offset, limit, left, right), this);
  }

	public List<AnnotationGraph> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
		
		// fn: match group -> annotation graph
		Map<String, AnnotationGraph> graphByMatchGroup = new HashMap<String, AnnotationGraph>();
		
		// fn: node id -> node
		Map<Long, AnnisNode> nodeById = new HashMap<Long, AnnisNode>();
		
		// fn: edge pre order value -> edge
		Map<Long, Edge> edgeByPre = new HashMap<Long, Edge>();
		
		// funktion result
		List<AnnotationGraph> graphs = new ArrayList<AnnotationGraph>();
		
		int rowNum = 0;
		while (resultSet.next()) {
			// process result by match group
			// match group is identified by the ids of the matched nodes
			String key = matchGroupRowMapper.mapRow(resultSet, rowNum);
			Validate.notNull(key, "Match group identifier must not be null");
			if ( ! graphByMatchGroup.containsKey(key) ) {
				log.debug("starting annotation graph for match: " + key);
				AnnotationGraph graph = new AnnotationGraph();
				graphs.add(graph);
				graphByMatchGroup.put(key, graph);
				
				// add matched node ids to the graph
				for (String id : key.split(","))
					graph.addMatchedNodeId(Long.parseLong(id));
				
				// clear mapping functions for this graph
				// assumes that the result set is sorted by key, pre
				nodeById.clear();
				edgeByPre.clear();
			}
			AnnotationGraph graph = graphByMatchGroup.get(key);
			
			// get node data
			AnnisNode node = annisNodeRowMapper.mapRow(resultSet, rowNum);
			
			// add node to graph if it is new, else get known copy
			long id = node.getId();
			if ( ! nodeById.containsKey(id) ) {
				log.debug("new node: " + id);
				nodeById.put(id, node);
				graph.addNode(node);
			} else {
				node = nodeById.get(id);
			}
			
			// get edge data
			Edge edge = edgeRowMapper.mapRow(resultSet, rowNum);

			// add edge to graph if it is new, else get known copy
			long pre = edge.getPre();
			if ( ! edgeByPre.containsKey(pre) ) {
				// fix source references in edge
				edge.setDestination(node);
				fixNodes(edge, edgeByPre, nodeById);

				// add edge to src and dst nodes
				node.addIncomingEdge(edge);
				AnnisNode source = edge.getSource();
				if (source != null)
					source.addOutgoingEdge(edge);
				
				log.debug("new edge: " + edge);
				edgeByPre.put(pre, edge);
				graph.addEdge(edge);
			} else {
				edge = edgeByPre.get(pre);
			}
			
			// add annotation data
			Annotation nodeAnnotation = nodeAnnotationRowMapper.mapRow(resultSet, rowNum);
			if (nodeAnnotation != null)
				node.addNodeAnnotation(nodeAnnotation);
			Annotation edgeAnnotation = edgeAnnotationRowMapper.mapRow(resultSet, rowNum);
			if (edgeAnnotation != null)
				edge.addAnnotation(edgeAnnotation);
		}
		
//		ArrayList<AnnotationGraph> graphs = new ArrayList<AnnotationGraph>(graphByMatchGroup.values());


		return graphs;
	}
	
	///// Helper
	
	protected void fixNodes(Edge edge, Map<Long, Edge> edgeByPre, Map<Long, AnnisNode> nodeById) {
		// pull source node from parent edge
		AnnisNode source = edge.getSource();
		if (source == null)
			return;
		long pre = source.getId();
		Edge parentEdge = edgeByPre.get(pre);
		AnnisNode parent = parentEdge != null ? parentEdge.getDestination() : null;
//		log.debug("looking for node with rank.pre = " + pre + "; found: " + parent);
		edge.setSource(parent);
		
		// pull destination node from mapping function
		long destinationId = edge.getDestination().getId();
		edge.setDestination(nodeById.get(destinationId));
	}
	
	///// Getter / Setter
	
	public AnnisNodeRowMapper getAnnisNodeRowMapper() {
		return annisNodeRowMapper;
	}

	public void setAnnisNodeRowMapper(AnnisNodeRowMapper annisNodeRowMapper) {
		this.annisNodeRowMapper = annisNodeRowMapper;
	}

	public EdgeRowMapper getEdgeRowMapper() {
		return edgeRowMapper;
	}

	public void setEdgeRowMapper(EdgeRowMapper edgeRowMapper) {
		this.edgeRowMapper = edgeRowMapper;
	}

	public AnnotationRowMapper getNodeAnnotationRowMapper() {
		return nodeAnnotationRowMapper;
	}

	public void setNodeAnnotationRowMapper(
			AnnotationRowMapper nodeAnnotationRowMapper) {
		this.nodeAnnotationRowMapper = nodeAnnotationRowMapper;
	}

	public AnnotationRowMapper getEdgeAnnotationRowMapper() {
		return edgeAnnotationRowMapper;
	}

	public void setEdgeAnnotationRowMapper(
			AnnotationRowMapper edgeAnnotationRowMapper) {
		this.edgeAnnotationRowMapper = edgeAnnotationRowMapper;
	}

	public MatchGroupRowMapper getMatchGroupRowMapper() {
		return matchGroupRowMapper;
	}

	public void setMatchGroupRowMapper(MatchGroupRowMapper matchGroupRowMapper) {
		this.matchGroupRowMapper = matchGroupRowMapper;
	}

	public SqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}

	public void setSqlGenerator(SqlGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	public DddQueryParser getDddQueryParser() {
		return dddQueryParser;
	}

	public void setDddQueryParser(DddQueryParser dddQueryParser) {
		this.dddQueryParser = dddQueryParser;
	}

	public QueryAnalysis getQueryAnalysis() {
		return queryAnalysis;
	}

	public void setQueryAnalysis(QueryAnalysis queryAnalysis) {
		this.queryAnalysis = queryAnalysis;
	}

  public String getNodeTableViewName()
  {
    return nodeTableViewName;
  }

  public void setNodeTableViewName(String nodeTableViewName)
  {
    this.nodeTableViewName = nodeTableViewName;
  }

  public String getMatchedNodesViewName()
  {
    return matchedNodesViewName;
  }

  public void setMatchedNodesViewName(String matchedNodesViewName)
  {
    this.matchedNodesViewName = matchedNodesViewName;
  }

  


}
