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

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.sqlgen.TableAccessStrategy;

public class AnnotationGraphDaoHelper implements ResultSetExtractor {

	private Logger log = Logger.getLogger(this.getClass());
	
	public static class MatchGroupRowMapper implements ParameterizedRowMapper<String> {

		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getString("key");
		}
		
	}

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
		Map<String, String> nodeAnnotationColumns = new HashMap<String, String>();
		nodeAnnotationColumns.put("node_ref", "id");
		nodeAnnotationColumns.put("namespace", "anno_namespace");
		nodeAnnotationColumns.put("name", "anno_name");
		nodeAnnotationColumns.put("value", "anno_value");
		
		Map<String, String> edgeAnnotationColumns = new HashMap<String, String>();
		nodeAnnotationColumns.put("rank_ref", "pre");
		edgeAnnotationColumns.put("namespace", "edge_anno_namespace");
		edgeAnnotationColumns.put("name", "edge_anno_name");
		edgeAnnotationColumns.put("value", "edge_anno_value");
		
		Map<String, String> edgeColumns = new HashMap<String, String>();
		edgeColumns.put("node_ref", "id");
		edgeColumns.put("name", "edge_name");
		edgeColumns.put("namespace", "edge_name");
		
		Map<String, Map<String, String>> columnAliases = new HashMap<String, Map<String, String>>();
		columnAliases.put(TableAccessStrategy.NODE_ANNOTATION_TABLE, nodeAnnotationColumns);
		columnAliases.put(TableAccessStrategy.EDGE_ANNOTATION_TABLE, edgeAnnotationColumns);
		columnAliases.put(TableAccessStrategy.EDGE_TABLE, edgeColumns);
		
		TableAccessStrategy tableAccessStrategy = new TableAccessStrategy(null, null, null, columnAliases);

		annisNodeRowMapper = new AnnisNodeRowMapper();
		annisNodeRowMapper.setTableAccessStrategy(tableAccessStrategy);
		
		edgeRowMapper = new EdgeRowMapper();
		edgeRowMapper.setTableAccessStrategy(tableAccessStrategy);
		
		nodeAnnotationRowMapper = new AnnotationRowMapper(TableAccessStrategy.NODE_ANNOTATION_TABLE);
		nodeAnnotationRowMapper.setTableAccessStrategy(tableAccessStrategy);
		
		edgeAnnotationRowMapper = new AnnotationRowMapper(TableAccessStrategy.EDGE_ANNOTATION_TABLE);
		edgeAnnotationRowMapper.setTableAccessStrategy(tableAccessStrategy);
	}
	
	// create SQL query for a list of matches with context left, right
	public String createSqlQuery(List<Match> matches, int left, int right) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT DISTINCT\n"); 
		sb.append("\ttokens.key, edge.pre, edge.post, edge.parent, edge.level, edge.zshg AS component, edge.edge_type, edge.edge_name, edge.anno_namespace AS edge_anno_namespace, edge.anno_name AS edge_anno_name, edge.anno_value AS edge_anno_value, node.*\n");
		sb.append("FROM\n");
		
		sb.append(tokenRelation(matches, left, right));
		sb.append("\n");
		
		sb.append("\tJOIN struct_annotation AS node ON (tokens.text_ref = node.text_ref AND ");
		sb.append("(tokens.min <= node.left_token AND tokens.max >= node.right_token OR node.left_token <= tokens.min AND tokens.min <= node.right_token OR node.left_token <= tokens.max AND tokens.max <= node.right_token))\n");
		sb.append("\tJOIN rank_annotations AS edge ON (edge.node_ref = node.id)\n");           
		sb.append("ORDER BY tokens.key, edge.pre");
		
		return sb.toString();
	}
	
	// FIXME: there is only one graph and no matched nodes. {-1} as key only needed because of algorithm in extractData
	// create SQL query for a text id
	public String createSqlQuery(long textId) {
		String template = "SELECT DISTINCT\n"
			+ "\t'-1' AS key, edge.pre, edge.post, edge.parent, edge.level, edge.zshg AS component, edge.edge_type, edge.edge_name, edge.anno_namespace AS edge_anno_namespace, edge.anno_name AS edge_anno_name, edge.anno_value AS edge_anno_value, node.*\n"
			+ "FROM\n"
			+ "\tstruct_annotation AS node JOIN rank_annotations AS edge ON (edge.node_ref = node.id)\n"
			+ "WHERE\n" + "\tnode.text_ref = :text_id\n"
			+ "ORDER BY edge.pre";
		String sql = template.replace(":text_id", String.valueOf(textId));
		return sql;
	}
	
	public List<Match> slice(List<Match> matches, int offset, int length) {
		if (length == 0)
			return matches;
		
		if (offset >= matches.size())
			return new ArrayList<Match>();
		
		int toIndex = offset + length;		
		if (toIndex > matches.size())
			toIndex = matches.size();
		return matches.subList(offset, toIndex);
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
	
	@Deprecated
	protected String tokenRelation(List<Match> matches, int left, int right) {
		
		List<String> tokenRel = new ArrayList<String>();
		for (List<Span> match : matches) {
			
			StringBuffer sb = new StringBuffer();

			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			int textRef = match.get(0).getTextRef();
			
			for (Span node : match) {
				min = Math.min(min, node.getTokenLeft());
				max = Math.max(max, node.getTokenRight());
			}
			
			sb.append("\t\tSELECT '");
	
			List<Long> ids = new ArrayList<Long>();
			for (Span node : match)
				ids.add(node.getStructId());
			sb.append(StringUtils.join(ids, ","));
			sb.append("'::varchar AS key, ");
			
			sb.append(textRef);
			sb.append(" AS text_ref, ");
			
			sb.append(min - left);
			sb.append(" AS min, ");
			
			sb.append(max + right);
			sb.append(" AS max");
			tokenRel.add(sb.toString());
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("\t(\n");
		sb.append(StringUtils.join(tokenRel, " UNION\n"));
		sb.append("\n");
		sb.append("\t) AS tokens");

		return sb.toString();
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

}
