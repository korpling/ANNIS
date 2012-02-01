package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.util.ArrayList;
import java.util.List;

//FIXME: key and key_names are hard-coded
public class NodeNameAndIdPostgreSqlArraySolutionKey extends PostgreSqlArraySolutionKey<String>
{
  
  public NodeNameAndIdPostgreSqlArraySolutionKey()
  {
    super();
    setIdColumnName("name");
    setKeyColumnName("key_names");
  }
  
  @Override
  public List<String> generateInnerQueryColumns(
      TableAccessStrategy tableAccessStrategy, int index)
  {
    List<String> columns = new ArrayList<String>();
    columns.add(tableAccessStrategy.aliasedColumn(NODE_TABLE, "id")
        + " AS " + "id" + index);
    columns.add(tableAccessStrategy.aliasedColumn(NODE_TABLE, "name")
        + " AS " + "name" + index);
    return columns;
  }

  @Override
  public List<String> generateOuterQueryColumns(
      TableAccessStrategy tableAccessStrategy, int size)
  {
    String idAlias = tableAccessStrategy.aliasedColumn("solutions", "id");
    String nameAlias = tableAccessStrategy.aliasedColumn("solutions", "name");
    List<String> columns = new ArrayList<String>();
    columns.add(createKeyArray(idAlias, "key", size));
    columns.add(createKeyArray(nameAlias, "key_names", size));
    return columns;
  }

}
