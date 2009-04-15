package annisservice.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.UncategorizedSQLException;

import de.deutschdiachrondigital.dddquery.helper.QueryExecution;
import de.deutschdiachrondigital.dddquery.sql.Match;
import de.deutschdiachrondigital.dddquery.sql.Node;

public class GetWekaHandler extends AnnisServiceHandler<List<List<String>>> {

	private Logger log = Logger.getLogger(this.getClass());
	
	// to retrieve annotations we first have to retrieve match list
	@Autowired protected GetCountHandler getCountHandler;
	private QueryExecution queryExecution;
	
	public GetWekaHandler() {
		super("GET WEKA");
	}
	
	public final String CORPUS_LIST = "corpusList";
	public final String ANNIS_QUERY = "annisQuery";
	
	@SuppressWarnings("unchecked")
	@Override
	protected List<List<String>> getResult(Map<String, Object> args) {
		List<Long> corpusList = (List<Long>) args.get(CORPUS_LIST);
		String annisQuery = (String) args.get(ANNIS_QUERY);
		
		log.debug("matching query");
		List<Match> matches = getCountHandler.matchQuery(corpusList, annisQuery);

		// what IDs are in the matches?
		Set<Long> ids = new HashSet<Long>();
		for (Match match : matches) {
			for (Node node : match) {
				ids.add(node.getStructId());
			}
		}
		
		if (ids.isEmpty()) {
			return null;
		}
		
		// get annotations for nodes with that id
		log.debug("retrieving annotations of matches");
		String template = "SELECT node.id, node.token_index, node.span, node_annotation.* FROM struct AS node, annotation AS node_annotation WHERE node.id IN ( :ids ) and node_annotation.struct_ref = node.id";
		String sql = template.replace(":ids", StringUtils.join(ids, ", "));
		log.debug(sql);
		ResultSet rs = queryExecution.executeQuery(sql);
		
		// get node annotations from result set
		Map<Long, Node> nodes = new HashMap<Long, Node>();
		try {
			while (rs.next()) {
				long id = rs.getLong("struct_ref");
				String ns = rs.getString("ns");
				String name = rs.getString("attribute");
				String value = rs.getString("value");
				
				if (ns != null)
					name = ns + ":" + name;

				log.debug("#" + id + ": " + name + "=" + value);

				if ( ! nodes.containsKey(id) )
					nodes.put(id, new Node(id));
				
				Node node = nodes.get(id);
	
				String span = rs.getString("span");
				Long tokenIndex = rs.getLong("token_index");
				if (rs.wasNull()) {
					tokenIndex = null;
				} else {
					node.setTokenIndex(tokenIndex);
					node.setSpan(span);
				}
				
				node.addAnnotation(name, value);
			}
		} catch (SQLException e) {
			throw new UncategorizedSQLException("get weka", e.getMessage(), e);
		}
		
		// build weka header
		Map<Integer, Set<String>> headerPerMatchColumn = new HashMap<Integer, Set<String>>();
		for (Match match : matches) {
			int i = 1;
			for (Node node : match) {
				if ( ! headerPerMatchColumn.containsKey(i) )
					headerPerMatchColumn.put(i, new HashSet<String>());
				Set<String> annotations = headerPerMatchColumn.get(i);
				long structId = node.getStructId();
				Node node2 = nodes.get(structId);
				if (node2 != null) {
					Map<String, String> annotations2 = node2.getAnnotations();
					Set<String> keySet = annotations2.keySet();
					for (String annotation : keySet)
						annotations.add(annotation);
				}
				++i;
			}
		}
		List<String> header = new ArrayList<String>();
		for (int i : headerPerMatchColumn.keySet()) {
			header.add("#" + i + "_id");
			header.add("#" + i + "_token");
			for (String annotation : headerPerMatchColumn.get(i)) {
				header.add("#" + i + "_" + annotation);
			}
		}
		
		List<List<String>> weka = new ArrayList<List<String>>();
		weka.add(header);
		
		// create lines in weka table
		for (Match match : matches) {
			List<String> line = new ArrayList<String>();
			int i = 1;
			for (Node node : match) {
				line.add(String.valueOf(node.getStructId()));
				Node node2 = nodes.get(node.getStructId());
				if (node2 != null) {
					line.add(node2.getTokenIndex() != null ? node2.getSpan() : "NULL");
					for (String annotation : headerPerMatchColumn.get(i)) {
						if (node2.getAnnotations().containsKey(annotation))
							line.add(node2.getAnnotations().get(annotation));
						else
							line.add("NULL");
					}
				} else {
					line.add("NULL");
				}
				++i;
			}
			weka.add(line);
		}
		
		return weka;		
	}

	public QueryExecution getQueryExecution() {
		return queryExecution;
	}

	public void setQueryExecution(QueryExecution queryExecution) {
		this.queryExecution = queryExecution;
	}

}
