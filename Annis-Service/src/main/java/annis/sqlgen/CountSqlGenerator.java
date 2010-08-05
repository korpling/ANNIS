package annis.sqlgen;

import annis.ql.parser.QueryData;
import de.deutschdiachrondigital.dddquery.node.Start;
import java.util.List;


public class CountSqlGenerator extends SqlGenerator
{

  @Override
  public String toSql(QueryData queryData, List<Long> corpusList)
  {
    StringBuffer sql = new StringBuffer();

    sql.append("SELECT count(*) FROM ");
    sql.append("(\n");
    sql.append(super.toSql(queryData, corpusList));
    sql.append(") AS solutions");

    return sql.toString();
  }
}
