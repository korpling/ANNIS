/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.sqlgen;

import annis.model.AnnisNode;
import annis.ql.parser.QueryData;
import java.util.List;
import org.springframework.util.Assert;

/**
 *
 * @author thomas
 */
public abstract class QueryDataSqlGenerator<T> extends BaseSqlGenerator<QueryData, T>
{
  @Override
  public String toSql(QueryData queryData)
  {
    return toSql(queryData, 0);
  }

  @Override
  public String toSql(QueryData queryData, int indentBy)
  {
    Assert.notEmpty(queryData.getAlternatives(), "BUG: no alternatives");

    // push alternative down
    List<AnnisNode> alternative = queryData.getAlternatives().get(0);

    String indent = computeIndent(indentBy);
    StringBuffer sb = new StringBuffer();
    indent(sb, indent);
    sb.append(createSqlForAlternative(queryData, alternative, indent));
    appendOrderByClause(sb, queryData, alternative, indent);
    appendLimitOffsetClause(sb, queryData, alternative, indent);
    return sb.toString();
  }
}
