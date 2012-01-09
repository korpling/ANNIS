package annis.sqlgen;

import java.util.ArrayList;
import java.util.List;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

public class AnnisKey
{

  public List<String> generateInnerQueryColumns(
      TableAccessStrategy tableAccessStrategy, int index)
  {
    List<String> columns = new ArrayList<String>();
    columns.add(tableAccessStrategy.aliasedColumn(NODE_TABLE, "id") + index
        + " AS " + "id" + index);
    columns.add(tableAccessStrategy.aliasedColumn(NODE_TABLE, "name") + index
        + " AS " + "name" + index);
    return columns;
  }

}
