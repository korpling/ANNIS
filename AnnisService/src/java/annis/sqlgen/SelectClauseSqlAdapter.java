package annis.sqlgen;

import java.util.List;

import annis.dao.CorpusSelectionStrategy;
import annis.model.AnnisNode;

public interface SelectClauseSqlAdapter {

	@SuppressWarnings("unchecked")
	String selectClause(List<AnnisNode> nodes,
			CorpusSelectionStrategy corpusSelectionStrategy);

}