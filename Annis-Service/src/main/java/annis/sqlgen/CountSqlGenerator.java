package annis.sqlgen;

import de.deutschdiachrondigital.dddquery.node.Start;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class CountSqlGenerator extends SqlGenerator implements ResultSetExtractor
{

  @Override
  public String toSql(Start statement, List<Long> corpusList)
  {
    StringBuffer sql = new StringBuffer();

    sql.append("SELECT count(*) FROM ");
    sql.append("(\n");
    sql.append(super.toSql(statement, corpusList));
    sql.append(") AS solutions");

    return sql.toString();
  }

  @Override
  public Object extractData(ResultSet rs) throws SQLException, DataAccessException
  {
    int sum = 0;
    while (rs.next())
    {
      sum += rs.getInt(1);
    }
    return sum;
  }
}
