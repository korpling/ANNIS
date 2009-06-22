package annis.dao;

import java.util.List;

import annis.model.Annotation;
import annis.sqlgen.NodeSqlAdapter;

public interface CorpusSelectionStrategy {

	boolean usesViews();
	
	void registerNodeAdapter(NodeSqlAdapter adapter);
	
	String createViewSql();
	
	String whereClauseForNode(String docRefColumn);
	
	String viewName(String table);
	
	void addMetaAnnotations(List<Annotation> annotations);

}