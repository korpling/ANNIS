package annis.sqlgen;

import java.sql.Types;

public class NodeNamePostgreSqlArraySolutionKey extends PostgreSqlArraySolutionKey<String>
    
{

  public NodeNamePostgreSqlArraySolutionKey()
  {
    super("name", "key_names", Types.VARCHAR);
  }

}
