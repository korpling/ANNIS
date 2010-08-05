package annis.sqlgen;

import annis.ql.parser.QueryData;
import java.util.List;


public class CountSqlGenerator extends SqlGenerator
{

  @Override
  public String toSql(QueryData queryData, List<Long> corpusList)
  {
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT count(*) FROM ");
    sql.append("(\n");
    sql.append(super.toSql(queryData, corpusList));
    sql.append(") AS solutions");

    return sql.toString();
  }
}
