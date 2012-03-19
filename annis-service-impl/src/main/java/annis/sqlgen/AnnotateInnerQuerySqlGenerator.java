package annis.sqlgen;

import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.sqlgen.AnnotateSqlGenerator.AnnotateQueryData;

public class AnnotateInnerQuerySqlGenerator
  extends AbstractUnionSqlGenerator<Object>
  implements SelectClauseSqlGenerator<QueryData>,
  OrderByClauseSqlGenerator<QueryData>,
  LimitOffsetClauseSqlGenerator<QueryData>
{

  // sort solutions
  private boolean sortSolutions;

  // annotation graph key generation
  private SolutionKey<?> solutionKey;
  
  @Override
  public Object extractData(ResultSet rs) throws SQLException,
    DataAccessException
  {
    throw new NotImplementedException(
      "BUG: inner query result is evaluated by outer query");
  }

  @Override
  public String toSql(QueryData queryData, int indentBy)
  {
    StringBuilder sb = new StringBuilder();
    
    String indent = computeIndent(indentBy);
    
    indent(sb, indent);
    sb.append("SELECT row_number() OVER () as n, inn.*");
    indent(sb, indent);
    sb.append("FROM (\n");
    
    sb.append(super.toSql(queryData, indentBy+1));
    sb.append("\n");
    indent(sb, indent);
    sb.append(") AS inn\n");
    
    return sb.toString();
  }
  
  

  @Override
  public String selectClause(QueryData queryData, List<QueryNode> alternative,
    String indent)
  {
    AnnotateQueryData annotateQueryData = getAnnotateQueryData(queryData);

    List<String> selectClauseForNode = new ArrayList<String>();
    for (int i = 1; i <= alternative.size(); ++i)
    {
      QueryNode node = alternative.get(i - 1);
      TableAccessStrategy tables = tables(node);

      List<String> fields = new ArrayList<String>();
      fields.addAll(solutionKey.generateInnerQueryColumns(tables, i));
      fields.add(tables.aliasedColumn(NODE_TABLE, "text_ref") + " AS text" + i);
      fields.add(tables.aliasedColumn(NODE_TABLE, "left_token") + " - "
        + annotateQueryData.getLeft() + " AS min" + i);
      fields.add(tables.aliasedColumn(NODE_TABLE, "right_token") + " + "
        + annotateQueryData.getRight() + " AS max" + i);
      fields.add(tables.aliasedColumn(NODE_TABLE, "corpus_ref") + " AS corpus" + i);
      fields.add(tables.aliasedColumn(NODE_TABLE, "name") + " AS name" + i);
      
      selectClauseForNode.add("\n" + indent + TABSTOP + StringUtils.join(fields, ", "));
      
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
    // don't use ORDER BY clause if there's no LIMIT clause; saves a sort
    AnnotateQueryData annotateQueryData = getAnnotateQueryData(queryData);
    if (annotateQueryData.isPaged())
    {
      super.appendOrderByClause(sb, queryData, alternative, indent);
    }
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

  @Override
  public String limitOffsetClause(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    AnnotateQueryData annotateQueryData = getAnnotateQueryData(queryData);

    StringBuilder sb = new StringBuilder();
    if (annotateQueryData.isPaged())
    {
      sb.append("LIMIT ").append(annotateQueryData.getLimit());
      sb.append(" OFFSET ").append(annotateQueryData.getOffset());
    }
    return sb.toString();
  }

  private AnnotateQueryData getAnnotateQueryData(QueryData queryData)
  {
    // find required information, assume defaults if necessary
    AnnotateQueryData annotateQueryData = null;
    for (Object o : queryData.getExtensions())
    {
      if (o instanceof AnnotateQueryData)
      {
        annotateQueryData = (AnnotateQueryData) o;
      }
    }
    if (annotateQueryData == null)
    {
      annotateQueryData = new AnnotateQueryData(0, 0, 5, 5);
    }
    return annotateQueryData;
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