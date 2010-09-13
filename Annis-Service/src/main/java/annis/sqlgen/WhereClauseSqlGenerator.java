package annis.sqlgen;

import java.util.List;

import annis.model.AnnisNode;
import annis.model.Annotation;

public interface WhereClauseSqlGenerator {

	List<String> whereConditions(AnnisNode node, List<Long> corpusList, List<Long> documents);

  List<String> commonWhereConditions(List<AnnisNode> nodes, List<Long> corpusList,List<Long> documents);

}
