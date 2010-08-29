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

	private String factsViewName;
  private String nodeViewName;
  private String factsContextViewName;

	private Logger log = Logger.getLogger(this.getClass());

	private SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy;

	public CorpusSelectionByViewOnlyToplevelTableAccessStrategy() {
		super();
	}
	
	public CorpusSelectionByViewOnlyToplevelTableAccessStrategy(AnnisNode node) {
		super(node);
	}
	
	public void modifySqlSession(SimpleJdbcTemplate simpleJdbcTemplate, QueryData queryData) {
		
    String factsViewDefinition = "CREATE TEMPORARY VIEW :view_name AS SELECT * FROM :node_table"
			.replaceAll(":view_name", factsViewName)
			.replaceAll(":node_table", nodeTableAlias());

    String factsContextViewDefinition = "CREATE TEMPORARY VIEW :view_name AS SELECT * FROM :node_table"
			.replaceAll(":view_name", factsContextViewName)
			.replaceAll(":node_table", contextTableAlias());

    String nodeViewDefinition = "CREATE TEMPORARY VIEW :view_name AS SELECT * FROM :node_table"
			.replaceAll(":view_name", nodeViewName)
			.replaceAll(":node_table", realNodeTableAlias());
		
		List<Long> corpusList = queryData.getCorpusList();
		
		if ( ! corpusList.isEmpty() ) {
			factsViewDefinition += corpusConstraint(corpusList,
        column(nodeTableAlias(), columnName(NODE_TABLE, "toplevel_corpus")));

      factsContextViewDefinition += corpusConstraint(corpusList,
        column(contextTableAlias(), columnName(CONTEXT, "toplevel_corpus")));

      nodeViewDefinition += corpusConstraint(corpusList, 
        column(realNodeTableAlias(), columnName(REAL_NODE_TABLE, "toplevel_corpus")));
      
			List<Annotation> metaData = queryData.getMetaData();
			if ( ! metaData.isEmpty() ) {
				String documentsWithMetaDataSql = subQueryCorpusSelectionStrategy.buildSubQuery(corpusList, metaData);
				List<Long> documents = simpleJdbcTemplate.query(documentsWithMetaDataSql, ParameterizedSingleColumnRowMapper.newInstance(Long.class));

        factsViewDefinition += metaDataConstraint(documents,
          column(nodeTableAlias(), columnName(NODE_TABLE, "corpus_ref")));

        factsContextViewDefinition += metaDataConstraint(documents,
          column(contextTableAlias(), columnName(NODE_TABLE, "corpus_ref")));

        nodeViewDefinition += metaDataConstraint(documents,
          column(realNodeTableAlias(), columnName(REAL_NODE_TABLE, "corpus_ref")));

			}
		}

    log.debug("SQL for facts view:\n" + factsViewDefinition);
    log.debug("SQL for facts_context view:\n" + factsContextViewDefinition);
		log.debug("SQL for node view:\n" + nodeViewDefinition);

		simpleJdbcTemplate.update(factsViewDefinition);
    simpleJdbcTemplate.update(factsContextViewDefinition);
    simpleJdbcTemplate.update(nodeViewDefinition);
	}

  private String corpusConstraint(List<Long> corpusList, String toplevelColumnName)
  {
    return " WHERE :toplevel IN ( :corpusList )"
				.replaceAll(":toplevel", toplevelColumnName)
				.replaceAll(":corpusList", StringUtils.join(corpusList, ", "));
  }

  private String metaDataConstraint(List<Long> documents, String corpusRefColumnName)
  {
    return " AND :corpus IN ( :documentList )"
					.replaceAll(":corpus", corpusRefColumnName)
					.replaceAll(":documentList", documents.isEmpty() ? "NULL" : StringUtils.join(documents, ", "));
  }

	/** return the original node table alias */
	private String nodeTableAlias() {
		return super.tableName(NODE_TABLE);
	}

  private String realNodeTableAlias() {
		return super.tableName(REAL_NODE_TABLE);
	}

  private String contextTableAlias()
  {
    return super.tableName(CONTEXT);
  }

	
	@Override
	public String tableName(String table) {
		String alias = super.tableName(table);
		String originalNodeTable = nodeTableAlias();
		if (alias.equals(originalNodeTable))
				return factsViewName;
		else
			return alias;
	}
	
	///// Getter / Setter

	public String getFactsViewName() {
		return factsViewName;
	}

	public void setFactsViewName(String factsViewName) {
		this.factsViewName = factsViewName;
	}

	
	public SubQueryCorpusSelectionStrategy getSubQueryCorpusSelectionStrategy() {
		return subQueryCorpusSelectionStrategy;
	}

	public void setSubQueryCorpusSelectionStrategy(
			SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy) {
		this.subQueryCorpusSelectionStrategy = subQueryCorpusSelectionStrategy;
	}

  public String getNodeViewName()
  {
    return nodeViewName;
  }

  public void setNodeViewName(String nodeViewName)
  {
    this.nodeViewName = nodeViewName;
  }

  public String getFactsContextViewName()
  {
    return factsContextViewName;
  }

  public void setFactsContextViewName(String factsContextViewName)
  {
    this.factsContextViewName = factsContextViewName;
  }

  
  
}
