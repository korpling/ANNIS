/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * Abstract base class for a complete SQL statement.
 *
 * A SQL statement consists of a mandatory SELECT and FROM clauses and optional
 * WHERE, GROUP BY, ORDER BY and LIMIT/OFFSET clauses. The individual clauses
 * are generated using helper classes which are specified by properties.
 *
 * @author Viktor Rosenfeld <rosenfel@informatik.hu-berlin.de>
 *
 * @param <T> Type into which the JDBC result set is transformed.
 */
public abstract class AbstractSqlGenerator
  extends TableAccessStrategyFactory
  implements SqlGenerator<QueryData>
{

  // generators for different SQL statement clauses
  private WithClauseSqlGenerator<QueryData> withClauseSqlGenerator;
  private SelectClauseSqlGenerator<QueryData> selectClauseSqlGenerator;
  private List<FromClauseSqlGenerator<QueryData>> fromClauseSqlGenerators;
  private List<WhereClauseSqlGenerator<QueryData>> whereClauseSqlGenerators;
  private GroupByClauseSqlGenerator<QueryData> groupByClauseSqlGenerator;
  private OrderByClauseSqlGenerator<QueryData> orderByClauseSqlGenerator;
  private LimitOffsetClauseSqlGenerator<QueryData> limitOffsetClauseSqlGenerator;
  // controls indentation
  public final static String TABSTOP = "  ";

  @Override
  public String toSql(QueryData queryData)
  {
    String result = toSql(queryData, "");
    return result;
  }

  @Override
  public String toSql(QueryData queryData, String indent)
  {
    Assert.notEmpty(queryData.getAlternatives(), "BUG: no alternatives");

    // push alternative down
    List<QueryNode> alternative = queryData.getAlternatives().get(0);;
    // find the first alternative which has the maximum width in order to make sure
    // getMaxWidth() and alternative.size() are always the same
    for(List<QueryNode> a :  queryData.getAlternatives())
    {
      if(a.size() == queryData.getMaxWidth())
      {
        alternative = a;
        break;
      }
    }
    StringBuffer sb = new StringBuffer();
    sb.append(indent);
    sb.append(createSqlForAlternative(queryData, alternative, indent));
    appendOrderByClause(sb, queryData, alternative, indent);
    appendLimitOffsetClause(sb, queryData, alternative, indent);
    return sb.toString();
  }

  protected String createSqlForAlternative(QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    StringBuffer sb = new StringBuffer();
    appendWithClause(sb, queryData, alternative, indent);
    appendSelectClause(sb, queryData, alternative, indent);
    appendFromClause(sb, queryData, alternative, indent);
    appendWhereClause(sb, queryData, alternative, indent);
    appendGroupByClause(sb, queryData, alternative, indent);
    return sb.toString();
  }

  protected String computeIndent(int indentBy)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indentBy; ++i)
    {
      sb.append(TABSTOP);
    }
    return sb.toString();
  }

  private void appendWithClause(StringBuffer sb, QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    if (withClauseSqlGenerator != null)
    {
      List<String> clauses = withClauseSqlGenerator.withClauses(queryData,
        alternative, indent + TABSTOP);

      if (!clauses.isEmpty())
      {
        sb.append(indent).append("WITH\n");
        sb.append(StringUtils.join(clauses, ",\n"));
        sb.append("\n");
      }
    }
  }

  private void appendSelectClause(StringBuffer sb, QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    sb.append(indent);
    sb.append("SELECT ");
    sb.append(selectClauseSqlGenerator.selectClause(queryData, alternative,
      indent));
    sb.append("\n");
  }

  private void appendFromClause(StringBuffer sb, QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    sb.append(indent);
    sb.append("FROM");
    List<String> fromTables = new ArrayList<>();
    for (FromClauseSqlGenerator<QueryData> generator : fromClauseSqlGenerators)
    {
      fromTables.add(generator.fromClause(queryData, alternative, indent));
    }
    sb.append("\n");
    sb.append(indent).append(TABSTOP);
    sb.append(StringUtils.join(fromTables, ",\n" + indent + TABSTOP));
    sb.append("\n");
  }

  private void appendWhereClause(StringBuffer sb, QueryData queryData,
    List<QueryNode> alternative, String indent)
  {

    // check if the WHERE clause generators are really used
    if (whereClauseSqlGenerators == null || whereClauseSqlGenerators.isEmpty())
    {
      return;
    }

    // treat each condition as mutable string to remove last AND
    List<StringBuffer> conditions = new ArrayList<>();

    for (WhereClauseSqlGenerator<QueryData> generator : whereClauseSqlGenerators)
    {
      Set<String> whereConditions = generator.whereConditions(queryData,
        alternative, indent);
      for (String constraint : whereConditions)
      {
        conditions.add(new StringBuffer(constraint));
      }
    }

    // sort conditions, group by accessed table alias
    Collections.sort(conditions, new Comparator<StringBuffer>()
    {

      @Override
      public int compare(StringBuffer o1, StringBuffer o2)
      {
        if (o1 == null && o2 == null)
        {
          return 0;
        }
        else if (o1 == null && o2 != null)
        {
          return -1;
        }
        else if (o1 != null && o2 == null)
        {
          return 1;
        }
        else if(o1 != null && o2 != null)
        {
          return o1.toString().compareTo(o2.toString());
        }
        throw new IllegalArgumentException("Could not compare " + o1 + " with " + o2);
      }
    });

    // no conditions in WHERE clause? break out
    if (conditions.isEmpty())
    {
      return;
    }

    // append WHERE clause to query
    sb.append(indent);
    sb.append("WHERE");
    sb.append("\n");
    sb.append(indent).append(TABSTOP);
    sb.append(StringUtils.join(conditions, " AND\n" + indent + TABSTOP));
    sb.append("\n");
  }

  private void appendGroupByClause(StringBuffer sb, QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    if (groupByClauseSqlGenerator != null)
    {
      String atts = groupByClauseSqlGenerator.groupByAttributes(queryData,
        alternative);
      if(atts != null && !atts.isEmpty())
      {
        sb.append(indent);
        sb.append("GROUP BY ");
        sb.append(atts);
        sb.append("\n");
      }
    }
  }

  protected void appendOrderByClause(StringBuffer sb, QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    if (orderByClauseSqlGenerator != null)
    {
      sb.append(indent);
      sb.append("ORDER BY ");
      sb.append(orderByClauseSqlGenerator.orderByClause(queryData, alternative,
        indent));
      sb.append("\n");
    }
  }

  protected void appendLimitOffsetClause(StringBuffer sb, QueryData queryData,
    List<QueryNode> alternative, String indent)
  {
    if (limitOffsetClauseSqlGenerator != null)
    {
      sb.append(limitOffsetClauseSqlGenerator.limitOffsetClause(queryData,
        alternative, indent));
      sb.append("\n");
    }
  }

  ///// Getter / Setter
  public List<FromClauseSqlGenerator<QueryData>> getFromClauseSqlGenerators()
  {
    return fromClauseSqlGenerators;
  }

  public void setFromClauseSqlGenerators(
    List<FromClauseSqlGenerator<QueryData>> fromClauseSqlGenerators)
  {
    this.fromClauseSqlGenerators = fromClauseSqlGenerators;
  }

  public List<WhereClauseSqlGenerator<QueryData>> getWhereClauseSqlGenerators()
  {
    return whereClauseSqlGenerators;
  }

  public void setWhereClauseSqlGenerators(
    List<WhereClauseSqlGenerator<QueryData>> whereClauseSqlGenerators)
  {
    this.whereClauseSqlGenerators = whereClauseSqlGenerators;
  }

  public GroupByClauseSqlGenerator<QueryData> getGroupByClauseSqlGenerator()
  {
    return groupByClauseSqlGenerator;
  }

  public void setGroupByClauseSqlGenerator(
    GroupByClauseSqlGenerator<QueryData> groupByClauseSqlGenerator)
  {
    this.groupByClauseSqlGenerator = groupByClauseSqlGenerator;
  }

  public WithClauseSqlGenerator<QueryData> getWithClauseSqlGenerator()
  {
    return withClauseSqlGenerator;
  }

  public void setWithClauseSqlGenerator(
    WithClauseSqlGenerator<QueryData> withClauseSqlGenerator)
  {
    this.withClauseSqlGenerator = withClauseSqlGenerator;
  }

  public SelectClauseSqlGenerator<QueryData> getSelectClauseSqlGenerator()
  {
    return selectClauseSqlGenerator;
  }

  public void setSelectClauseSqlGenerator(
    SelectClauseSqlGenerator<QueryData> selectClauseSqlGenerator)
  {
    this.selectClauseSqlGenerator = selectClauseSqlGenerator;
  }

  public OrderByClauseSqlGenerator<QueryData> getOrderByClauseSqlGenerator()
  {
    return orderByClauseSqlGenerator;
  }

  public void setOrderByClauseSqlGenerator(
    OrderByClauseSqlGenerator<QueryData> orderByClauseSqlGenerator)
  {
    this.orderByClauseSqlGenerator = orderByClauseSqlGenerator;
  }

  public LimitOffsetClauseSqlGenerator<QueryData> getLimitOffsetClauseSqlGenerator()
  {
    return limitOffsetClauseSqlGenerator;
  }

  public void setLimitOffsetClauseSqlGenerator(
    LimitOffsetClauseSqlGenerator<QueryData> limitOffsetClauseSqlGenerator)
  {
    this.limitOffsetClauseSqlGenerator = limitOffsetClauseSqlGenerator;
  }
}