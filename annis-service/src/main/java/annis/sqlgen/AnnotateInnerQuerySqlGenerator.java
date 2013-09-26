  package annis.sqlgen;

import annis.sqlgen.extensions.AnnotateQueryData;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import org.apache.commons.lang3.Validate;

public class AnnotateInnerQuerySqlGenerator extends AbstractUnionSqlGenerator<Object>
  implements SelectClauseSqlGenerator<QueryData>,
  OrderByClauseSqlGenerator<QueryData>
{

  // sort solutions
  private boolean sortSolutions;
  // annotation graph key generation
  private SolutionKey<?> solutionKey;

  @Override
  public Object extractData(ResultSet rs) throws SQLException,
    DataAccessException
  {
    throw new UnsupportedOperationException(
      "BUG: inner query result is evaluated by outer query");
  }

  @Override
  public String toSql(QueryData queryData, String indent)
  {
    StringBuilder sb = new StringBuilder();

    sb.append(indent).append("SELECT row_number() OVER () as n, inn.*\n");
    sb.append(indent).append("FROM (\n");

    sb.append(super.toSql(queryData, indent + TABSTOP)).append("\n");
    sb.append(indent).append(") AS inn\n");

    return sb.toString();
  }

  @Override
  public String selectClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    List<AnnotateQueryData> extensions =
      queryData.getExtensions(AnnotateQueryData.class);
    AnnotateQueryData annotateQueryData = null;

    if (extensions.isEmpty())
    {
      annotateQueryData = new AnnotateQueryData(5, 5);
    }
    else
    {

      annotateQueryData = extensions.get(0);
    }

    List<String> selectClauseForNode = new ArrayList<String>();
    for (int i = 1; i <= alternative.size(); ++i)
    {
      QueryNode node = alternative.get(i - 1);
      TableAccessStrategy tables = tables(node);

      List<String> fields = new ArrayList<String>();
      fields.addAll(solutionKey.generateInnerQueryColumns(tables, i));
      fields.add(tables.aliasedColumn(NODE_TABLE, "text_ref") + " AS text" + i);
      
      // only set the context directly if we are doing a context search on the 
      // token but not any of the other segmentation layers
      if(annotateQueryData.getSegmentationLayer() == null)
      {
        fields.add(tables.aliasedColumn(NODE_TABLE, "left_token") + " - "
          + annotateQueryData.getLeft() + " AS min" + i);
        fields.add(tables.aliasedColumn(NODE_TABLE, "right_token") + " + "
          + annotateQueryData.getRight() + " AS max" + i);
      }
      else
      {
        fields.add(tables.aliasedColumn(NODE_TABLE, "left_token") 
          + " AS min" + i);
        fields.add(tables.aliasedColumn(NODE_TABLE, "right_token") 
          + " AS max" + i);
      }
      fields.add(tables.aliasedColumn(NODE_TABLE, "corpus_ref") + " AS corpus"
        + i);
      fields.add(tables.aliasedColumn(NODE_TABLE, "name") + " AS name" + i);

      selectClauseForNode.add("\n" + indent + TABSTOP + StringUtils.join(fields,
        ", "));
    }

    return "DISTINCT" + StringUtils.join(selectClauseForNode, ", ");
  }

  @Override
  protected void appendOrderByClause(StringBuffer sb, QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    // only use ORDER BY clause if result has to be sorted
    if (!sortSolutions)
    {
      return;
    }
    super.appendOrderByClause(sb, queryData, alternative, indent);
  }

  @Override
  public String orderByClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    List<String> ids = new ArrayList<String>();
    for (int i = 1; i <= queryData.getMaxWidth(); ++i)
    {
      ids.add("id" + i);
    }
    return StringUtils.join(ids, ", ");
  }

  public boolean isSortSolutions()
  {
    return sortSolutions;
  }

  public void setSortSolutions(boolean sortSolutions)
  {
    this.sortSolutions = sortSolutions;
  }

  public SolutionKey<?> getSolutionKey()
  {
    return solutionKey;
  }

  public void setSolutionKey(SolutionKey<?> solutionKey)
  {
    this.solutionKey = solutionKey;
  }
}