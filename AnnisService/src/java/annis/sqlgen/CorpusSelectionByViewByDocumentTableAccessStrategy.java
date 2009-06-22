package annis.sqlgen;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.ql.parser.QueryData;


public class CorpusSelectionByViewByDocumentTableAccessStrategy 
	extends TableAccessStrategy 
	implements SqlSessionModifier {

	// dependencies
	private SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy;
	private ParameterizedSingleColumnRowMapper<Long> corpusIdRowMapper;
	
	// configuration
	private String nodeTableViewName;

	public CorpusSelectionByViewByDocumentTableAccessStrategy() {
		super();
		corpusIdRowMapper = new ParameterizedSingleColumnRowMapper<Long>();
	}
	
	public CorpusSelectionByViewByDocumentTableAccessStrategy(AnnisNode node) {
		super(node);
	}
	
	public void modifySqlSession(SimpleJdbcTemplate simpleJdbcTemplate, QueryData queryData) {
		
		// turn top-level corpus id into list of documents
		List<Long> corpusList = queryData.getCorpusList();
		List<Annotation> metaData = queryData.getMetaData();
		
		String viewDefinition = "CREATE VIEW :view_name AS SELECT * FROM :node_table"
			.replaceAll(":view_name", nodeTableViewName)
			.replaceAll(":node_table", originalNodeTable());
		
		if (subQueryCorpusSelectionStrategy.hasCorpusSelection(corpusList, metaData)) {
			String documentSql = subQueryCorpusSelectionStrategy.buildSubQuery(corpusList, metaData);
			List<Long> documentIds = simpleJdbcTemplate.query(documentSql, getCorpusIdRowMapper());
			viewDefinition += " WHERE :corpus_ref IN ( :documentIds )"
				.replaceAll(":documentIds", StringUtils.join(documentIds, ", "))
				.replaceAll(":corpus_ref", column(originalNodeTable(), columnName(NODE_TABLE, "corpus_ref")));
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

	public SubQueryCorpusSelectionStrategy getSubQueryCorpusSelectionStrategy() {
		return subQueryCorpusSelectionStrategy;
	}

	public void setSubQueryCorpusSelectionStrategy(
			SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy) {
		this.subQueryCorpusSelectionStrategy = subQueryCorpusSelectionStrategy;
	}
	

	public ParameterizedSingleColumnRowMapper<Long> getCorpusIdRowMapper() {
		return corpusIdRowMapper;
	}

	public void setCorpusIdRowMapper(ParameterizedSingleColumnRowMapper<Long> corpusIdRowMapper) {
		this.corpusIdRowMapper = corpusIdRowMapper;
	}

}
