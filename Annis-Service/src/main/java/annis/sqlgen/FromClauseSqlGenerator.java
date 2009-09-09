package annis.sqlgen;

import annis.model.AnnisNode;

public interface FromClauseSqlGenerator {

	String fromClause(AnnisNode node);
	
}
