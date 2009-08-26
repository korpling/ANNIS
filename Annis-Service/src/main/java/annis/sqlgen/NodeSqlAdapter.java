package annis.sqlgen;

import java.util.List;


public interface NodeSqlAdapter {
	
	String selectClause();
	
	String selectClauseNullValues();
	
	String fromClause();
	
	List<String> whereClause();

}