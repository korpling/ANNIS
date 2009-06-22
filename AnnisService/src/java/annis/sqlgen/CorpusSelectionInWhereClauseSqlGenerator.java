package annis.sqlgen;

import java.util.ArrayList;
import java.util.List;

import annis.model.AnnisNode;
import annis.model.Annotation;


public class CorpusSelectionInWhereClauseSqlGenerator 
	extends BaseNodeSqlGenerator
	implements WhereClauseSqlGenerator {
	
	private SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy;
	
	public List<String> whereConditions(AnnisNode node, List<Long> corpusList, List<Annotation> metaData) {
		List<String> conditions = new ArrayList<String>();
		
		if ( subQueryCorpusSelectionStrategy.hasCorpusSelection(corpusList, metaData) ) {
			String condition = 
				tables(node).aliasedColumn(TableAccessStrategy.NODE_TABLE, "corpus_ref") +
				" IN (" +
				subQueryCorpusSelectionStrategy.buildSubQuery(corpusList, metaData) +
				")";
			conditions.add(condition);
		}
			
		return conditions;
	}

	///// Getters / Setters

	public SubQueryCorpusSelectionStrategy getSubQueryCorpusSelectionStrategy() {
		return subQueryCorpusSelectionStrategy;
	}

	public void setSubQueryCorpusSelectionStrategy(
			SubQueryCorpusSelectionStrategy subQueryCorpusSelectionStrategy) {
		this.subQueryCorpusSelectionStrategy = subQueryCorpusSelectionStrategy;
	}

}
