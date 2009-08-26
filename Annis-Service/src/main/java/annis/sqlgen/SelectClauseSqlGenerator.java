package annis.sqlgen;

import java.util.List;

import annis.model.AnnisNode;

public interface SelectClauseSqlGenerator {

	String selectClause(List<AnnisNode> nodes, int maxWidth);
	
}
