package annis.sqlgen;

import java.sql.ResultSet;
import java.util.List;

public interface SolutionKey<KeyType>
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
  public abstract List<String> generateInnerQueryColumns(
      TableAccessStrategy tableAccessStrategy, int index);

  /**
   * Generate the key(s) for an annotation graph.
   * 
   * @param tableAccessStrategy TODO
   * @param size TODO
   * @return A list of column aliases that are used in the SELECT clause of
   *         the outer ANNOTATE query.
   */
  public abstract List<String> generateOuterQueryColumns(
      TableAccessStrategy tableAccessStrategy, int size);

  /**
   * Retrieve (and validate) the annotation graph key from the current row
   * of the JDBC result set.
   *
   * @param resultSet The JDBC result set returned by an ANNOTATE query.
   */
  public abstract KeyType retrieveKey(ResultSet resultSet);

  /**
   * Has the key changed from the last row to this one.
   *
   * @return True, if the key has changed from the last row to this one.
   */
  public abstract boolean isNewKey();

  /**
   * Retrieve the search term index for which a given node is a match. 
   * A node is a match for a given search term if its name is part of the
   * current row's key.
   * 
   * @param name A node name
   * @return The index of the search term for which the node is a match 
   *         (starting with 1) or {@code null} if the node is not a match.
   */
  public abstract Integer getMatchedNodeIndex(Object id);
  
  /**
   * Returns the name of the key columns.
   * @param size The number of matched nodes in a solution.
   */
  public abstract List<String> getKeyColumns(int size);
  
  /**
   * Returns the node ID of the current row in a result set.
   */
  public abstract Object getNodeId(ResultSet resultSet, TableAccessStrategy tableAccessStrategy);
  
  public abstract int getKeySize();
 
  /**
   * Returns the String representation of the key that can be used
   * in the Salt model for the
   * {@link annis.model.AnnisConstants.FEAT_MACHTEDIDS} property.
   */
  public abstract String getCurrentKeyAsString();
  
}