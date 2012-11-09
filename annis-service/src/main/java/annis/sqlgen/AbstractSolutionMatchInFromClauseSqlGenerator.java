package annis.sqlgen;

import java.util.List;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;

public abstract class AbstractSolutionMatchInFromClauseSqlGenerator<T> extends
    AbstractSqlGenerator<T> implements FromClauseSqlGenerator<QueryData>
{

  private SqlGenerator<QueryData, ?> findSqlGenerator;

  @Override
  public String fromClause(QueryData queryData, List<QueryNode> alternative,
      String indent)
  {
    StringBuffer sb = new StringBuffer();
    
    sb.append(indent).append("(\n");
    
    sb.append(indent).append(TABSTOP);
    sb.append(findSqlGenerator.toSql(queryData, indent + TABSTOP));
    sb.append(indent).append(TABSTOP).append(") AS solutions");
    
    return sb.toString();
  }

  public SqlGenerator<QueryData, ?> getFindSqlGenerator() {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(SqlGenerator<QueryData, ?> findSqlGenerator) {
    this.findSqlGenerator = findSqlGenerator;
  }

}
