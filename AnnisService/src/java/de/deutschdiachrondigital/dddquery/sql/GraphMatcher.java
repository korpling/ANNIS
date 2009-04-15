package de.deutschdiachrondigital.dddquery.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.helper.QueryExecution;
import de.deutschdiachrondigital.dddquery.helper.ResultSetConverter;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import de.deutschdiachrondigital.dddquery.sql.model.Graph;

// TODO: doc
public class GraphMatcher {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public static class DefaultResultSetConverter implements ResultSetConverter<List<Match>> {

		private Logger log = Logger.getLogger(this.getClass());
		
		public List<Match> convertResultSet(ResultSet resultSet) {
			List<Match> assignments = new ArrayList<Match>();
			
			try {
				ResultSetMetaData metaData = resultSet.getMetaData();
				int columns = metaData.getColumnCount() / 4;
				while (resultSet.next()) {
					Match assignment = new Match();
					for (int i = 0; i < columns; ++i) {
						Integer structId = resultSet.getInt((i * 4 + 1));
						if (resultSet.wasNull())
							break;
						int textRef = resultSet.getInt((i * 4 + 2));
						int tokenLeft = resultSet.getInt((i * 4 + 3));
						int tokenRight = resultSet.getInt((i * 4 + 4));
						
						assignment.add(new Node(structId, textRef, tokenLeft, tokenRight));
					}
					assignments.add(assignment);
					
					log.debug("found matching assignment: " + assignment);
				}
			} catch (SQLException e) {
				log.warn("an exception occured", e);
				throw new RuntimeException(e);
			}
			return assignments;
		}
		
	}
	
	public interface MatchFilter {
		public void init();
		public boolean filterMatch(Match match);
	}
	
	public static class MultipleTextsFilter implements MatchFilter {

		private Logger log = Logger.getLogger(this.getClass());
		
		public void init() {
			// no init necessary, since each match is individually filtered
		}
		
		public boolean filterMatch(Match match) {
			// every match has at least one node
			int textRef = match.get(0).getTextRef();
			
			// check all nodes and return true if a node is found with a different text ref
			for (Node node : match)
				if (textRef != node.getTextRef()) {
					log.debug("multiple texts referenced in match, expected <" + textRef + ">, was <" + node.getTextRef() + ">");
					return true;
				}
		
			// all text refs are the same
			return false;
		}
		
	}
	
	public static class DuplicateMatchFilter implements MatchFilter {

		private Logger log = Logger.getLogger(this.getClass());
		
		private class Key {
			private long textRef;
			private long tokenLeft;
			private long tokenRight;
			
			public Key(Match match) {
				textRef = match.get(0).getTextRef();
				tokenLeft = Integer.MAX_VALUE;
				tokenRight = Integer.MIN_VALUE;
				for (Node node : match) {
					tokenLeft = Math.min(tokenLeft, node.getTokenLeft());
					tokenRight = Math.max(tokenRight, node.getTokenRight());
				}
			}
			
			@Override
			public boolean equals(Object obj) {
				if ( ! (obj instanceof Key) )
					return false;
				
				Key k = (Key) obj;
			
				return new EqualsBuilder()
					.append(this.textRef, k.textRef)
					.append(this.tokenRight, k.tokenRight)
					.append(this.tokenLeft, k.tokenLeft)
					.isEquals();
			}
			
			@Override
			public int hashCode() {
				return new HashCodeBuilder()
					.append(textRef)
					.append(tokenLeft)
					.append(tokenRight)
					.toHashCode();
			}
		}
		
		private Set<Key> seen = new HashSet<Key>();
		
		public void init() {
			seen.clear();
		}
		
		public boolean filterMatch(Match match) {
			Key key = new Key(match);
			
			if (seen.contains(key)) {
				log.debug("already seen match that references the tokens " + key.tokenRight + " to " + key.tokenLeft + " of text " + key.textRef);
				return true;
			}
			
			seen.add(key);
			return false;
		}
		
	}
	
	private List<DepthFirstAdapter> preProcessors;
	private DddQueryParser parser;
	private GraphTranslator graphTranslator;
	private SqlGenerator sqlGenerator;
	private QueryExecution queryExecution;
	private ResultSetConverter<List<Match>> resultSetConverter = new DefaultResultSetConverter();
	private List<MatchFilter> matchFilters;
	
	public List<Match> matchGraph(List<Long> corpora, String dddQuery) {
		log.info("searching for nodes that match the query: " + dddQuery);
		
		Start start = parser.parseDddQuery(dddQuery);
		for (DepthFirstAdapter preProcessor : preProcessors) {
			log.debug("applying pre processor to parse tree: " + preProcessor.getClass().getSimpleName());
			
			start.apply(preProcessor);
		}
		
		Graph graph = graphTranslator.translate(start);
		String sqlQuery = sqlGenerator.translate(corpora, graph);
		ResultSet resultSet = queryExecution.executeQuery(sqlQuery);
		List<Match> matches = resultSetConverter.convertResultSet(resultSet);
		
		for (MatchFilter filter : matchFilters) {
			filter.init();
			List<Match> matchCopy = new ArrayList<Match>(matches);
			for (Match match : matchCopy)
				if (filter.filterMatch(match)) {
					log.debug("removing match " + match);
					matches.remove(match);
				}
		}
		
		log.info("done, matches found: " + matches.size());

		return matches;
	}
	
	public List<DepthFirstAdapter> getPreProcessors() {
		return preProcessors;
	}

	public void setPreProcessors(List<DepthFirstAdapter> preProcessors) {
		this.preProcessors = preProcessors;
	}

	public DddQueryParser getParser() {
		return parser;
	}

	public void setParser(DddQueryParser parser) {
		this.parser = parser;
	}

	public GraphTranslator getGraphTranslator() {
		return graphTranslator;
	}

	public void setGraphTranslator(GraphTranslator translator) {
		this.graphTranslator = translator;
	}

	public SqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}

	public void setSqlGenerator(SqlGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	public QueryExecution getQueryExecution() {
		return queryExecution;
	}

	public void setQueryExecution(QueryExecution queryExecution) {
		this.queryExecution = queryExecution;
	}

	public ResultSetConverter<List<Match>> getResultSetConverter() {
		return resultSetConverter;
	}

	public void setResultSetConverter(ResultSetConverter<List<Match>> resultSetConverter) {
		this.resultSetConverter = resultSetConverter;
	}

	public List<MatchFilter> getMatchFilters() {
		return matchFilters;
	}

	public void setMatchFilters(List<MatchFilter> matchFilters) {
		this.matchFilters = matchFilters;
	}
	
}
