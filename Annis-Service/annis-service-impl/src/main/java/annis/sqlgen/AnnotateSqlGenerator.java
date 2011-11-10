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
package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.FACTS_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;

import annis.dao.AnnisNodeRowMapper;
import annis.dao.AnnotationRowMapper;
import annis.dao.DocumentNameMapRow;
import annis.dao.EdgeRowMapper;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.ql.parser.QueryData;

/**
 * 
 * @author thomas
 */
public class AnnotateSqlGenerator 
	extends QueryDataSqlGenerator<List<AnnotationGraph>> 
	implements SelectClauseSqlGenerator, FromClauseSqlGenerator, 
	WhereClauseSqlGenerator, OrderByClauseSqlGenerator
{
	
	private AnnotateInnerQuerySqlGenerator innerQuerySqlGenerator;
	private TableJoinsInFromClauseSqlGenerator 
		tableJoinsInFromClauseSqlGenerator;
	private TableAccessStrategy factsTas;
	private boolean optimizeOverlap;

	public static class AnnotateQueryData {
		private int offset;
		private int limit;
		private int left;
		private int right;

		public AnnotateQueryData(int offset, int limit, int left, 
				int right) {
			super();
			this.offset = offset;
			this.limit = limit;
			this.left = left;
			this.right = right;
		}

		public int getOffset() {
			return offset;
		}
		public int getLimit() {
			return limit;
		}
		public int getLeft() {
			return left;
		}
		public int getRight() {
			return right;
		}
		
		public boolean isPaged() {
			return offset != 0 || limit != 0;
		}
		
		@Override
		public String toString() {
			List<String> fields = new ArrayList<String>();
			if (limit > 0)
				fields.add("limit = " + limit);
			if (offset > 0)
				fields.add("offset = " + offset);
			if (left > 0)
				fields.add("left = " + left);
			if (right > 0)
				fields.add("right = " + right);
			return StringUtils.join(fields, ", ");
		}
	}

	// old
	
	public enum IslandPolicies
	{
		context, none
	}

	private static final Logger log = 
			Logger.getLogger(AnnotateSqlGenerator.class);
	private String matchedNodesViewName;
	private AnnotationRowMapper nodeAnnotationRowMapper;
	private AnnotationRowMapper edgeAnnotationRowMapper;
	private EdgeRowMapper edgeRowMapper;
	private AnnisNodeRowMapper annisNodeRowMapper;
	private String defaultIslandsPolicy;

	public AnnotateSqlGenerator()
	{
		// FIXME: totally ugly, but the query has fixed column names 
		// (and needs its own column aliasing)
		// TableAccessStrategyFactory wants a corpus selection 
		// strategy
		// solution: build AnnisNodes with API and refactor 
		// SqlGenerator to accept GROUP BY nodes
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
		
		Map<String, String> componentColumns = new HashMap<String, String>();
	    componentColumns.put("id", "component_id");
	    componentColumns.put("name", "edge_name");
	    componentColumns.put("namespace", "edge_namespace");
	    componentColumns.put("type", "edge_type");
		
		edgeColumns.put("name", "edge_name");
		edgeColumns.put("namespace", "edge_namespace");

		Map<String, Map<String, String>> columnAliases = new HashMap<String, Map<String, String>>();
		columnAliases.put(TableAccessStrategy.NODE_TABLE, nodeColumns);
		columnAliases.put(TableAccessStrategy.NODE_ANNOTATION_TABLE, nodeAnnotationColumns);
		columnAliases.put(TableAccessStrategy.EDGE_ANNOTATION_TABLE, edgeAnnotationColumns);
		columnAliases.put(TableAccessStrategy.RANK_TABLE, edgeColumns);
	    columnAliases.put(COMPONENT_TABLE, componentColumns);

	    factsTas = new TableAccessStrategy(null);
		factsTas.setColumnAliases(columnAliases);

		edgeRowMapper = new EdgeRowMapper();
		edgeRowMapper.setTableAccessStrategy(factsTas);

		annisNodeRowMapper = new AnnisNodeRowMapper();
		annisNodeRowMapper.setTableAccessStrategy(factsTas);

		nodeAnnotationRowMapper = new AnnotationRowMapper(TableAccessStrategy.NODE_ANNOTATION_TABLE);
		nodeAnnotationRowMapper.setTableAccessStrategy(factsTas);

		edgeAnnotationRowMapper = new AnnotationRowMapper(TableAccessStrategy.EDGE_ANNOTATION_TABLE);
		edgeAnnotationRowMapper.setTableAccessStrategy(factsTas);
	}

	@Deprecated
	public String explain(JdbcTemplate jdbcTemplate, 
			List<Long> corpusList,
			int nodeCount, long offset, long limit, int left, 
			int right,
			boolean analyze, Map<Long, Properties> corpusProperties)
	{
		ParameterizedSingleColumnRowMapper<String> planRowMapper = 
				new ParameterizedSingleColumnRowMapper<String>();

		List<String> plan = jdbcTemplate.query(
				(analyze ? "EXPLAIN ANALYZE " : "EXPLAIN ")
						+ "\n"
						+ getContextQuery(corpusList, left, right, 
								limit,
								offset, nodeCount, corpusProperties),
				planRowMapper);
		return StringUtils.join(plan, "\n");
	}

	@Deprecated
	public List<AnnotationGraph> queryAnnotationGraph(
			JdbcTemplate jdbcTemplate, List<Long> corpusList, 
			int nodeCount,
			long offset, long limit, int left, int right,
			Map<Long, Properties> corpusProperties)
	{
		return (List<AnnotationGraph>) jdbcTemplate.query(
				getContextQuery(corpusList, left, right, limit, 
						offset, nodeCount, corpusProperties), this);
	}

	@Deprecated
	public List<AnnotationGraph> queryAnnotationGraph(
			JdbcTemplate jdbcTemplate, long textID)
	{
		return (List<AnnotationGraph>) 
				jdbcTemplate.query(getTextQuery(textID), this);
	}

	private IslandPolicies getMostRestrictivePolicy(
			List<Long> corpora, Map<Long, Properties> props)
	{
		IslandPolicies[] all = IslandPolicies.values();
		IslandPolicies result = all[all.length - 1];

		for (Long l : corpora)
		{
			if (props.get(l) != null)
			{
				IslandPolicies newPolicy = 
						IslandPolicies.valueOf(props.get(l)
						.getProperty("islands-policy", 
								defaultIslandsPolicy));
				if (newPolicy.ordinal() < result.ordinal())
				{
					result = newPolicy;
				}
			}
		}
		return result;
	}

	@Deprecated
	public String getContextQuery(List<Long> corpusList, int left, 
			int right, long limit, long offset, int nodeCount,
			Map<Long, Properties> corpusProperties)
	{

		IslandPolicies islandsPolicy = getMostRestrictivePolicy(
				corpusList,	corpusProperties);

		// key for annotation graph matches
		StringBuilder keySb = new StringBuilder();
		keySb.append("ARRAY[matches.id1");
		for (int i = 2; i <= nodeCount; ++i)
		{
			keySb.append(",");
			keySb.append("matches.id");
			keySb.append(i);
		}
		keySb.append("] AS key");
		String key = keySb.toString();

		// sql for matches
		StringBuilder matchSb = new StringBuilder();
		matchSb.append("SELECT * FROM ");
		matchSb.append(matchedNodesViewName);
		matchSb.append(" ORDER BY ");
		matchSb.append("id1");
		for (int i = 2; i <= nodeCount; ++i)
		{
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
		sb.append("SELECT DISTINCT \n");
		sb.append("\t");
		sb.append(key);
		sb.append(", facts.*");
		sb.append(", corpus.path_name as path, corpus.path_name[1] as document_name \n");
		sb.append("FROM\n");
		sb.append("\t(");
		sb.append(matchSql);
		sb.append(") AS matches,\n");
		sb.append("\t");
		sb.append(FACTS_TABLE);
		sb.append(" AS facts,\n");
		sb.append("corpus\n");
		
		sb.append("WHERE\n");
		if (corpusList != null)
		{
			sb.append("\tfacts.toplevel_corpus IN (");
			sb.append(corpusList.isEmpty() ? "NULL" 
					: StringUtils.join(corpusList, ","));
			sb.append(") AND\n");
		}
		sb.append("\t(\n");

		if (islandsPolicy == IslandPolicies.context)
		{
			for (int i = 1; i <= nodeCount; ++i)
			{
				if (i > 1)
				{
					sb.append("\n\t\tOR\n");
				}

				sb.append("\t\t(\n" + "\t\t\tfacts.text_ref = matches.text_ref");
				sb.append(i);

				String rangeStart = "matches.left_token" + i + " - " 
						+ left;
				String rangeEnd = "matches.right_token" + i + " + " 
						+ right;

				sb.append("\n" + "\t\t\tAND\n" + "\t\t\t(\n");
				sb.append(contextRangeSubquery(rangeStart, rangeEnd, 
						"\t\t\t\t"));
				sb.append("\t\t\t)\n");

				sb.append("\n" + "\t\t)");
			}
		} else if (islandsPolicy == IslandPolicies.none)
		{
			sb.append("\t\tfacts.text_ref IN(");
			for (int i = 1; i <= nodeCount; i++)
			{
				if (i > 1)
				{
					sb.append(",");
				}
				sb.append("matches.text_ref");
				sb.append(i);
			}
			sb.append(")\n\t\tAND\n\t\t(\n");

			StringBuilder rangeStart = new StringBuilder();
			StringBuilder rangeEnd = new StringBuilder();

			rangeStart.append("ANY(ARRAY[");
			rangeEnd.append("ANY(ARRAY[");

			for (int i = 1; i <= nodeCount; i++)
			{
				if (i > 1)
				{
					rangeStart.append(",");
					rangeEnd.append(",");
				}
				rangeStart.append("matches.left_token");
				rangeStart.append(i);
				rangeStart.append(" - ");
				rangeStart.append(left);

				rangeEnd.append("matches.right_token");
				rangeEnd.append(i);
				rangeEnd.append(" + ");
				rangeEnd.append(right);
			}

			rangeStart.append("])");
			rangeEnd.append("])");

			sb.append(contextRangeSubquery(rangeStart.toString(),
					rangeEnd.toString(), "\t\t\t"));

			sb.append("\t\t)\n");
		}
		sb.append("\n\t)\n");
		sb.append("\tAND corpus.id = corpus_ref\n");
		sb.append("\nORDER BY key, facts.pre");
		return sb.toString();
	}

	@Deprecated
	private StringBuilder contextRangeSubquery(String rangeStart,
			String rangeEnd, String indent)
	{
		StringBuilder sb = new StringBuilder();

		// left token inside context range
		sb.append(indent);
		sb.append("(facts.left_token >= ");
		sb.append(rangeStart);
		sb.append(" AND facts.left_token <= ");
		sb.append(rangeEnd);
		sb.append(")\n");
		// right token inside context range
		sb.append(indent);
		sb.append("OR(facts.right_token >= ");
		sb.append(rangeStart);
		sb.append(" AND facts.right_token <= ");
		sb.append(rangeEnd);
		sb.append(")\n");
		// context range completly covered
		sb.append(indent);
		sb.append("OR(facts.left_token <= ");
		sb.append(rangeStart);
		sb.append(" AND facts.right_token >= ");
		sb.append(rangeEnd);
		sb.append(")\n");

		return sb;
	}

	public String getTextQuery(long textID)
	{
		String template = "SELECT DISTINCT \n"
				+ "\tARRAY[-1::bigint] AS key, facts.*, c.path_name as path, c.path_name[1] as document_name\n" + "FROM\n"
				+ "\tfacts AS facts, corpus as c\n" + "WHERE\n"
				+ "\tfacts.text_ref = :text_id AND facts.corpus_ref = c.id\n" + "ORDER BY facts.pre";
		String sql = template.replace(":text_id", String.valueOf(textID));
		return sql;
	}

	@Override
	public List<AnnotationGraph> extractData(ResultSet resultSet)
			throws SQLException, DataAccessException
	{
		// function result
		List<AnnotationGraph> graphs = 
				new LinkedList<AnnotationGraph>();

		// fn: match group -> annotation graph

		Map<List<Long>, AnnotationGraph> graphByMatchGroup = 
				new HashMap<List<Long>, AnnotationGraph>();

		// fn: node id -> node
		Map<Long, AnnisNode> nodeById = new HashMap<Long, AnnisNode>();

		// fn: edge pre order value -> edge
		Map<Long, Edge> edgeByPre = new HashMap<Long, Edge>();

		int rowNum = 0;
		while (resultSet.next())
		{
			// process result by match group
			// match group is identified by the ids of the matched 
			// nodes
			Array sqlKey = resultSet.getArray("key");
			Validate.isTrue(!resultSet.wasNull(),
					"Match group identifier must not be null");
			Validate.isTrue(sqlKey.getBaseType() == Types.BIGINT,
					"Key in database must be from the type \"bigint\" but was \""
							+ sqlKey.getBaseTypeName() + "\"");

			Long[] keyArray = (Long[]) sqlKey.getArray();
			List<Long> key = Arrays.asList(keyArray);
			// for (Long bd : keyArray)
			// {
			// key.add(bd == null ? null : bd.longValue());
			// }

			if (!graphByMatchGroup.containsKey(key))
			{
				log.debug("starting annotation graph for match: " 
						+ key);
				AnnotationGraph graph = new AnnotationGraph();
				graphs.add(graph);
				graphByMatchGroup.put(key, graph);

				// clear mapping functions for this graph
				// assumes that the result set is sorted by key, pre
				nodeById.clear();
				edgeByPre.clear();

				// set the matched keys
				for (Long l : key)
				{
					if (l != null)
					{
						graph.addMatchedNodeId(l);
					}
				}
			}

			AnnotationGraph graph = graphByMatchGroup.get(key);

			graph.setDocumentName(new DocumentNameMapRow().mapRow(
					resultSet, rowNum));
			
			Array path = resultSet.getArray("path");
			graph.setPath((String []) path.getArray());

			// get node data
			AnnisNode node = annisNodeRowMapper.mapRow(resultSet, 
					rowNum);

			// add node to graph if it is new, else get known copy
			long id = node.getId();
			if (!nodeById.containsKey(id))
			{
				log.debug("new node: " + id);
				nodeById.put(id, node);
				graph.addNode(node);
			} else
			{
				node = nodeById.get(id);
			}

			// we now have the id of the node and the general key, 
			// so we can
			// add the matched node index to the graph (if matched)
			long matchIndex = 1;
			// node.setMatchedNodeInQuery(null);
			for (Long l : key)
			{
				if (l != null)
				{
					if (id == l)
					{
						node.setMatchedNodeInQuery(matchIndex);
						break;
					}
					matchIndex++;
				}
			}

			// get edge data
			Edge edge = edgeRowMapper.mapRow(resultSet, rowNum);

			// add edge to graph if it is new, else get known copy
			long pre = edge.getPre();
			if (!edgeByPre.containsKey(pre))
			{
				// fix source references in edge
				edge.setDestination(node);
				fixNodes(edge, edgeByPre, nodeById);

				// add edge to src and dst nodes
				node.addIncomingEdge(edge);
				AnnisNode source = edge.getSource();
				if (source != null)
				{
					source.addOutgoingEdge(edge);
				}

				log.debug("new edge: " + edge);
				edgeByPre.put(pre, edge);
				graph.addEdge(edge);
			} else
			{
				edge = edgeByPre.get(pre);
			}

			// add annotation data
			Annotation nodeAnnotation = 
					nodeAnnotationRowMapper.mapRow(resultSet, rowNum);
			if (nodeAnnotation != null)
			{
				node.addNodeAnnotation(nodeAnnotation);
			}
			Annotation edgeAnnotation = 
					edgeAnnotationRowMapper.mapRow(resultSet, rowNum);
			if (edgeAnnotation != null)
			{
				edge.addAnnotation(edgeAnnotation);
			}

			rowNum++;
		}

		return graphs;
	}

	protected void fixNodes(Edge edge, Map<Long, Edge> edgeByPre,
			Map<Long, AnnisNode> nodeById)
	{
		// pull source node from parent edge
		AnnisNode source = edge.getSource();
		if (source == null)
		{
			return;
		}
		long pre = source.getId();
		Edge parentEdge = edgeByPre.get(pre);
		AnnisNode parent = parentEdge != null 
				? parentEdge.getDestination() : null;
		// log.debug("looking for node with rank.pre = 
		// " + pre + "; found: " + parent);
		edge.setSource(parent);

		// pull destination node from mapping function
		long destinationId = edge.getDestination().getId();
		edge.setDestination(nodeById.get(destinationId));
	}

	public String getMatchedNodesViewName()
	{
		return matchedNodesViewName;
	}

	public void setMatchedNodesViewName(String matchedNodesViewName)
	{
		this.matchedNodesViewName = matchedNodesViewName;
	}

	public String getDefaultIslandsPolicy()
	{
		return defaultIslandsPolicy;
	}

	public void setDefaultIslandsPolicy(String defaultIslandsPolicy)
	{
		this.defaultIslandsPolicy = defaultIslandsPolicy;
	}

// new
	
	@Override
	public String selectClause(QueryData queryData, 
			List<AnnisNode> alternative, String indent) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("DISTINCT\n");
		sb.append(indent + TABSTOP + "ARRAY[");
		
		// solutions key
		List<String> ids = new ArrayList<String>();
		for (int i = 1; i <= alternative.size(); ++i)
			ids.add("solutions.id" + i);
		sb.append(StringUtils.join(ids, ", "));
		sb.append("] AS key,\n");
		
		List<String> fields = new ArrayList<String>();
		addSelectClauseAttribute(fields, FACTS_TABLE, "fid");
		addSelectClauseAttribute(fields, NODE_TABLE, "id");
		addSelectClauseAttribute(fields, NODE_TABLE, "text_ref");
		addSelectClauseAttribute(fields, NODE_TABLE, "corpus_ref");
		addSelectClauseAttribute(fields, NODE_TABLE, "toplevel_corpus");
		addSelectClauseAttribute(fields, NODE_TABLE, "namespace");
		addSelectClauseAttribute(fields, NODE_TABLE, "name");
		addSelectClauseAttribute(fields, NODE_TABLE, "left");
		addSelectClauseAttribute(fields, NODE_TABLE, "right");
		addSelectClauseAttribute(fields, NODE_TABLE, "token_index");
		addSelectClauseAttribute(fields, NODE_TABLE, "is_token");
		addSelectClauseAttribute(fields, NODE_TABLE, "continuous");
		addSelectClauseAttribute(fields, NODE_TABLE, "span");
		addSelectClauseAttribute(fields, NODE_TABLE, "left_token");
		addSelectClauseAttribute(fields, NODE_TABLE, "right_token");
		addSelectClauseAttribute(fields, RANK_TABLE, "pre");
		addSelectClauseAttribute(fields, RANK_TABLE, "post");
		addSelectClauseAttribute(fields, RANK_TABLE, "parent");
		addSelectClauseAttribute(fields, RANK_TABLE, "root");
		addSelectClauseAttribute(fields, RANK_TABLE, "level");
		addSelectClauseAttribute(fields, COMPONENT_TABLE, "id");
		addSelectClauseAttribute(fields, COMPONENT_TABLE, "type");
		addSelectClauseAttribute(fields, COMPONENT_TABLE, "name");
		addSelectClauseAttribute(fields, COMPONENT_TABLE, "namespace");
		addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "namespace");
		addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "name");
		addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "value");
		addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "namespace");
		addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "name");
		addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "value");
		addSelectClauseAttribute(fields, FACTS_TABLE, "sample");
		
//		old order
//		addSelectClauseAttribute(fields, NODE_TABLE, "id");
//		addSelectClauseAttribute(fields, NODE_TABLE, "namespace");
//		addSelectClauseAttribute(fields, NODE_TABLE, "name");
//		addSelectClauseAttribute(fields, NODE_TABLE, "text_ref");
//		addSelectClauseAttribute(fields, NODE_TABLE, "left");
//		addSelectClauseAttribute(fields, NODE_TABLE, "right");
//		addSelectClauseAttribute(fields, NODE_TABLE, "span");
//		addSelectClauseAttribute(fields, NODE_TABLE, "token_index");
//		addSelectClauseAttribute(fields, NODE_TABLE, "left_token");
//		addSelectClauseAttribute(fields, NODE_TABLE, "right_token");
//		addSelectClauseAttribute(fields, NODE_TABLE, "corpus_ref");
//
//		addSelectClauseAttribute(fields, RANK_TABLE, "pre");
//		addSelectClauseAttribute(fields, RANK_TABLE, "post");
//		addSelectClauseAttribute(fields, RANK_TABLE, "parent");
//		addSelectClauseAttribute(fields, RANK_TABLE, "level");
//
//		addSelectClauseAttribute(fields, COMPONENT_TABLE, "id");
//		addSelectClauseAttribute(fields, COMPONENT_TABLE, "name");
//		addSelectClauseAttribute(fields, COMPONENT_TABLE, "namespace");
//		addSelectClauseAttribute(fields, COMPONENT_TABLE, "type");
//
//		addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "namespace");
//		addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "name");
//		addSelectClauseAttribute(fields, NODE_ANNOTATION_TABLE, "value");
//
//		addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "namespace");
//		addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "name");
//		addSelectClauseAttribute(fields, EDGE_ANNOTATION_TABLE, "value");

		sb.append(indent + TABSTOP);
		sb.append(StringUtils.join(fields, ",\n" + indent + TABSTOP));
		sb.append(",\n" + indent + TABSTOP);

		// corpus.path_name
		sb.append("corpus.path_name AS path,\n" + indent + TABSTOP);
		sb.append("corpus.path_name[1] AS document_name");
		
		return sb.toString();
	}

	private void addSelectClauseAttribute(List<String> fields, 
			String table, String column) {
		TableAccessStrategy tas = tables(null);
		fields.add(tas.aliasedColumn(table, column) + " AS " 
				+ factsTas.columnName(table, column));
	}

	@Override
	public Set<String> whereConditions(QueryData queryData,	
				List<AnnisNode> alternative, String indent) {
		TableAccessStrategy tables = tables(null);
		
		StringBuffer sb = new StringBuffer();
		
		// restrict node table to corpus list
		List<Long> corpusList = queryData.getCorpusList();
		if (corpusList != null && ! corpusList.isEmpty() ) {
			indent(sb, indent);
			sb.append(tables.aliasedColumn(NODE_TABLE, "toplevel_corpus"));
			sb.append(" IN (");
			sb.append(StringUtils.join(corpusList, ", "));
			sb.append(") AND\n");
		}
		
		// island policies
		HashMap<Long, Properties> corpusProperties = 
				queryData.getCorpusConfiguration();
		IslandPolicies islandsPolicy = 
				getMostRestrictivePolicy(corpusList, corpusProperties);
		if (islandsPolicy == IslandPolicies.context) {
			indent(sb, indent + TABSTOP);
			sb.append("(\n");
			List<String> overlapForOneSpan = new ArrayList<String>();
			for (int i = 1; i <= alternative.size(); ++i) {
				
				StringBuffer sb2 = new StringBuffer();

				indent(sb2, indent + TABSTOP + TABSTOP);
				sb2.append(tables.aliasedColumn(NODE_TABLE, "text_ref") 
						+ " = solutions.text" + i + " AND\n");
				indent(sb2, indent + TABSTOP + TABSTOP);

				String rangeMin = "solutions.min" + i;
				String rangeMax = "solutions.max" + i;

				sb2.append(overlapForOneRange(indent + TABSTOP,
						rangeMin, rangeMax, tables));
				overlapForOneSpan.add(sb2.toString());
			}
			sb.append(StringUtils.join(overlapForOneSpan, 
					"\n" + indent + TABSTOP + ") OR (\n" + indent));
			sb.append("\n");
			indent(sb, indent + TABSTOP);
			sb.append(")");
		} else {
			indent(sb, indent);
			sb.append(tables.aliasedColumn(NODE_TABLE, "text_ref"));
			sb.append(" IN (");
			List<String> solutionTexts = new ArrayList<String>();
			for (int i = 1; i <= alternative.size(); ++i) {
				solutionTexts.add("solutions.text" + i);
			}
			sb.append(StringUtils.join(solutionTexts, ", "));
			sb.append(") AND\n");
			indent(sb, indent + TABSTOP);
			
			StringBuilder minSb = new StringBuilder();
			StringBuilder maxSb = new StringBuilder();
			minSb.append("ANY(ARRAY[");
			maxSb.append("ANY(ARRAY[");
			List<String> mins = new ArrayList<String>();
			List<String> maxs = new ArrayList<String>();
			for (int i = 1; i <= alternative.size(); ++i) {
				mins.add("solutions.min" + i);
				maxs.add("solutions.max" + i);
			}
			minSb.append(StringUtils.join(mins, ", "));
			maxSb.append(StringUtils.join(maxs, ", "));
			minSb.append("])");
			maxSb.append("])");
			String rangeMin = minSb.toString();
			String rangeMax = maxSb.toString();
			sb.append(overlapForOneRange(indent, rangeMin, rangeMax, tables));
		}
		
		// corpus constriction
		sb.append(" AND\n");
		indent(sb, indent + TABSTOP);
		sb.append(tables.aliasedColumn(TableAccessStrategy.CORPUS_TABLE, "id"));
		sb.append(" = ");
		sb.append(tables.aliasedColumn(NODE_TABLE, "corpus_ref"));
		
		HashSet<String> conditions = new HashSet<String>();
		conditions.add(sb.toString());
		return conditions;
	}

	private String overlapForOneRange(String indent,
			String rangeMin, String rangeMax, TableAccessStrategy tables) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("(\n");
		indent(sb, indent + TABSTOP + TABSTOP);
		if (optimizeOverlap) {
			sb.append("(");
			sb.append(tables.aliasedColumn(
					NODE_TABLE, "left_token") + " >= " + rangeMin);
			sb.append(" AND ");
			sb.append(tables.aliasedColumn(
					NODE_TABLE, "right_token") + " <= " + rangeMax);
			sb.append(") OR\n");
			indent(sb, indent + TABSTOP + TABSTOP);
			sb.append("(");
			sb.append(tables.aliasedColumn(
					NODE_TABLE, "left_token") + " <= " + rangeMin);
			sb.append(" AND ");
			sb.append(rangeMin + " <= " + tables.aliasedColumn(NODE_TABLE, "right_token"));
			sb.append(") OR\n");
			indent(sb, indent + TABSTOP + TABSTOP);
			sb.append("(");
			sb.append(tables.aliasedColumn(
					NODE_TABLE, "left_token") + " <= " + rangeMax);
			sb.append(" AND ");
			sb.append(rangeMax + " <= " + tables.aliasedColumn(NODE_TABLE, "right_token"));
			sb.append(")");
		} else {
			sb.append(tables.aliasedColumn(
					NODE_TABLE, "left_token") + " <= " + rangeMax);
			sb.append(" AND ");
			sb.append(tables.aliasedColumn(
					NODE_TABLE, "right_token") + " >= " + rangeMin);
		}
		sb.append("\n");
		indent(sb, indent + TABSTOP);
		sb.append(")");
		
		String overlapForOneRange = sb.toString();
		return overlapForOneRange;
	}

	@Override
	public String orderByClause(QueryData queryData, 
			List<AnnisNode> alternative, String indent) {
		return "key, " + tables(null).aliasedColumn(RANK_TABLE, "pre");
	}
	
	@Override
	public String fromClause(QueryData queryData, 
			List<AnnisNode> alternative, String indent) {
		StringBuffer sb = new StringBuffer();
		
		indent(sb, indent);
		sb.append("(\n");
		indent(sb, indent);
		int indentBy = indent.length() / 2 + 2;
		sb.append(innerQuerySqlGenerator.toSql(queryData, indentBy));
		indent(sb, indent + TABSTOP);
		sb.append(") AS solutions,\n");
		
		indent(sb, indent + TABSTOP);
		// really ugly
		sb.append(
				tableJoinsInFromClauseSqlGenerator
				.fromClauseForNode(null, true));
		sb.append(",\n");
		// sb.append("facts AS facts");
		
		indent(sb, indent + TABSTOP);
		sb.append(TableAccessStrategy.CORPUS_TABLE);
		
		return sb.toString();
	}

	public AnnotateInnerQuerySqlGenerator getInnerQuerySqlGenerator() {
		return innerQuerySqlGenerator;
	}

	public void setInnerQuerySqlGenerator(
			AnnotateInnerQuerySqlGenerator innerQuerySqlGenerator) {
		this.innerQuerySqlGenerator = innerQuerySqlGenerator;
	}

	public TableJoinsInFromClauseSqlGenerator getTableJoinsInFromClauseSqlGenerator() {
		return tableJoinsInFromClauseSqlGenerator;
	}

	public void setTableJoinsInFromClauseSqlGenerator(
			TableJoinsInFromClauseSqlGenerator tableJoinsInFromClauseSqlGenerator) {
		this.tableJoinsInFromClauseSqlGenerator = tableJoinsInFromClauseSqlGenerator;
	}

	public boolean isOptimizeOverlap() {
		return optimizeOverlap;
	}

	public void setOptimizeOverlap(boolean optimizeOverlap) {
		this.optimizeOverlap = optimizeOverlap;
	}

}