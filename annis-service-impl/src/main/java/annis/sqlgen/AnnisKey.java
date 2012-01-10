package annis.sqlgen;

import java.util.ArrayList;
import java.util.List;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

public class AnnisKey
{

  /**
   * Generate list of column aliases that are used to identify a node
   * in a matching solution in the inner query of an ANNOTATE function query.
   *  
   * @param tableAccessStrategy TODO
   * @param index TODO
   * @return  A list of column aliases that are used in the SELECT clause of
   *          the inner query.
   */
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

  /**
   * Generate the key(s) for an annotation graph.
   * 
   * @param tableAccessStrategy TODO
   * @param size TODO
   * @return A list of column aliases that are used in the SELECT clause of
   *         the outer ANNOTATE query.
   */
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

  // create a key array
  // example: ARRAY[solutions.id1, solutions.id2, solutions.id3] AS key
  private String createKeyArray(String column, String alias, int size)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("ARRAY[");
    sb.append(column);
    sb.append(1);
    for (int i = 2; i <= size; ++i) {
      sb.append(", ");
      sb.append(column);
      sb.append(i);
    }
    sb.append("]");
    sb.append(" AS ");
    sb.append(alias);
    String s = sb.toString();
    return s;
  }

  
  
}
