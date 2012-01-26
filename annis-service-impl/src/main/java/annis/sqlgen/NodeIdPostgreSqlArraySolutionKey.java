package annis.sqlgen;

import java.sql.Types;

public class NodeIdPostgreSqlArraySolutionKey extends PostgreSqlArraySolutionKey<Integer>
    
{

  public NodeIdPostgreSqlArraySolutionKey()
  {
    super("id", "key", Types.BIGINT);
  }

}
