package annis.sqlgen;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.dao.SqlSessionModifier;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.ql.parser.QueryData;


public class CorpusSelectionByViewOnlyToplevelTableAccessStrategy 
	extends TableAccessStrategy 
	implements SqlSessionModifier {

	private String nodeTableViewName;

	public CorpusSelectionByViewOnlyToplevelTableAccessStrategy() {
		super();
	}
	
	public CorpusSelectionByViewOnlyToplevelTableAccessStrategy(AnnisNode node) {
		super(node);
	}
	
	public void modifySqlSession(SimpleJdbcTemplate simpleJdbcTemplate, QueryData queryData) {
		String viewDefinition = "CREATE VIEW :view_name AS SELECT * FROM :node_table"
			.replaceAll(":view_name", nodeTableViewName)
			.replaceAll(":node_table", originalNodeTable());
		
		List<Long> corpusList = queryData.getCorpusList();
		List<Annotation> metaData = queryData.getMetaData();
		
		if ( ! corpusList.isEmpty() ) {
			viewDefinition += " WHERE :corpus_ref IN ( :corpusSelectionSubQuery )"
				.replaceAll(":corpus_ref", column(originalNodeTable(), columnName(NODE_TABLE, "toplevel_corpus")))
				.replaceAll(":corpusSelectionSubQuery", StringUtils.join(corpusList, ", "));
		}
			
		simpleJdbcTemplate.update(viewDefinition);
	}

	// return the original node table alias
	private String originalNodeTable() {
		return super.tableName(NODE_TABLE);
	}
	
	@Override
	public String tableName(String table) {
		String alias = super.tableName(table);
		if (alias.equals(originalNodeTable()))
				return nodeTableViewName;
		else
			return alias;
	}
	
	///// Getter / Setter

	public String getNodeTableViewName() {
		return nodeTableViewName;
	}

	public void setNodeTableViewName(String nodeTableViewName) {
		this.nodeTableViewName = nodeTableViewName;
	}

}
