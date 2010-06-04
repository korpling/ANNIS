package annis.sqlgen;

import java.util.List;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import annis.dao.SqlSessionModifier;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.ql.parser.QueryData;


public class CorpusSelectionByViewTableAccessStrategy 
	extends TableAccessStrategy 
	implements SqlSessionModifier {

	private SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy;
	private String nodeTableViewName;

	public CorpusSelectionByViewTableAccessStrategy() {
		super();
	}
	
	public CorpusSelectionByViewTableAccessStrategy(AnnisNode node) {
		super(node);
	}
	
	public void modifySqlSession(SimpleJdbcTemplate simpleJdbcTemplate, QueryData queryData) {
		String viewDefinition = "CREATE TEMPORARY VIEW :view_name AS SELECT * FROM :node_table"
			.replaceAll(":view_name", nodeTableViewName)
			.replaceAll(":node_table", originalNodeTable());
		
		List<Long> corpusList = queryData.getCorpusList();
		List<Annotation> metaData = queryData.getMetaData();
		
		if (subQueryCorpusSelectionStrategy.hasCorpusSelection(corpusList, metaData)) {
			viewDefinition += " WHERE :corpus_ref IN ( :corpusSelectionSubQuery )"
				.replaceAll(":corpus_ref", column(originalNodeTable(), columnName(NODE_TABLE, "corpus_ref")))
				.replaceAll(":corpusSelectionSubQuery", subQueryCorpusSelectionStrategy.buildSubQuery(corpusList, metaData));
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

}
