package annis.sqlgen;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.dao.SqlSessionModifier;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.ql.parser.QueryData;
import org.apache.log4j.Logger;


public class CorpusSelectionByViewOnlyToplevelTableAccessStrategy 
	extends TableAccessStrategy 
	implements SqlSessionModifier {

	private String nodeTableViewName;
	private Logger log = Logger.getLogger(this.getClass());

	private SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy;

	public CorpusSelectionByViewOnlyToplevelTableAccessStrategy() {
		super();
	}
	
	public CorpusSelectionByViewOnlyToplevelTableAccessStrategy(AnnisNode node) {
		super(node);
	}
	
	public void modifySqlSession(SimpleJdbcTemplate simpleJdbcTemplate, QueryData queryData) {
		String viewDefinition = "CREATE TEMPORARY VIEW :view_name AS SELECT * FROM :node_table"
			.replaceAll(":view_name", nodeTableViewName)
			.replaceAll(":node_table", originalNodeTable());
		
		List<Long> corpusList = queryData.getCorpusList();
		
		if ( ! corpusList.isEmpty() ) {
			viewDefinition += " WHERE :toplevel IN ( :corpusList )"
				.replaceAll(":toplevel", column(originalNodeTable(), columnName(NODE_TABLE, "toplevel_corpus")))
				.replaceAll(":corpusList", StringUtils.join(corpusList, ", "));

			List<Annotation> metaData = queryData.getMetaData();
			if ( ! metaData.isEmpty() ) {
				String documentsWithMetaDataSql = subQueryCorpusSelectionStrategy.buildSubQuery(corpusList, metaData);
				List<Long> documents = simpleJdbcTemplate.query(documentsWithMetaDataSql, ParameterizedSingleColumnRowMapper.newInstance(Long.class));
				viewDefinition += " AND :corpus IN ( :documentList )"
					.replaceAll(":corpus", column(originalNodeTable(), columnName(NODE_TABLE, "corpus_ref")))
					.replaceAll(":documentList", documents.isEmpty() ? "NULL" : StringUtils.join(documents, ", "));
			}
		}

    log.info("SQL for view:\n" + viewDefinition);
			
		simpleJdbcTemplate.update(viewDefinition);
	}

	// return the original node table alias
	private String originalNodeTable() {
		return super.tableName(NODE_TABLE);
	}
	
	@Override
	public String tableName(String table) {
		String alias = super.tableName(table);
		String originalNodeTable = originalNodeTable();
		if (alias.equals(originalNodeTable))
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

}
