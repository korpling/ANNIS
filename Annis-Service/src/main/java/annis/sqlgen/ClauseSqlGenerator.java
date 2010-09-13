package annis.sqlgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import annis.model.AnnisNode;

public class ClauseSqlGenerator
{

  private List<SelectClauseSqlGenerator> selectClauseSqlGenerators;
  private List<FromClauseSqlGenerator> fromClauseSqlGenerators;
  private List<WhereClauseSqlGenerator> whereClauseSqlGenerators;

  public String toSql(List<AnnisNode> nodes, int maxWidth, List<Long> corpusList, List<Long> documents)
  {
    Assert.notEmpty(nodes, "empty node list");
    Assert.isTrue(maxWidth >= nodes.size(), "maxWidth < nodes.size()");

    StringBuffer sb = new StringBuffer();
    appendSelectClause(sb, nodes, maxWidth);
    appendFromClause(sb, nodes);
    appendWhereClause(sb, nodes, corpusList, documents);

    return sb.toString();
  }

  ///// SELECT clause generation
  void appendSelectClause(StringBuffer sb, List<AnnisNode> nodes, int maxWidth)
  {
    // create SELECT clause
    sb.append("SELECT ");
    for (SelectClauseSqlGenerator generator : selectClauseSqlGenerators)
    {
      sb.append(generator.selectClause(nodes, maxWidth));
    }
    sb.append("\n");
  }

  ///// FROM clause generation
  void appendFromClause(StringBuffer sb, List<AnnisNode> nodes)
  {
    sb.append("FROM");
    List<String> fromTables = new ArrayList<String>();
    for (FromClauseSqlGenerator generator : fromClauseSqlGenerators)
    {
      for (AnnisNode node : nodes)
      {
        fromTables.add(generator.fromClause(node));
      }
    }
    sb.append("\n\t");
    sb.append(StringUtils.join(fromTables, ",\n\t"));
    sb.append("\n");
  }

  ///// WHERE clause generation
  void appendWhereClause(StringBuffer sb, List<AnnisNode> nodes, List<Long> corpusList, List<Long> documents)
  {

    // treat each condition as mutable string to remove last AND
    List<StringBuffer> whereClause = new ArrayList<StringBuffer>();
    for (AnnisNode node : nodes)
    {
      // append node comment
      whereClause.add(new StringBuffer("-- " + node));
      // append node conditions
      for (WhereClauseSqlGenerator generator : whereClauseSqlGenerators)
      {
        List<String> conditions = generator.whereConditions(node, corpusList, documents);
        if (conditions != null)
        {
          for (String constraint : conditions)
          {
            whereClause.add(new StringBuffer(constraint));
          }
        }
      }
    }

    // get common where clauses
    for(WhereClauseSqlGenerator generator : whereClauseSqlGenerators)
    {
      List<String> conditions = generator.commonWhereConditions(nodes, corpusList, documents);
      if(conditions != null)
      {
        for (String constraint : conditions)
        {
          whereClause.add(new StringBuffer(constraint));
        }
      }
    }
    

    // append AND to each condition in WHERE clause, skip comments, remember last condition
    StringBuffer lastConstraint = null;
    for (StringBuffer constraint : whereClause)
    {
      if (!constraint.toString().startsWith("--"))
      {
        constraint.append(" AND");
        lastConstraint = constraint;
      }
    }

    // no node with condition, just print out the node comments
    if (lastConstraint == null)
    {
      for (StringBuffer constraint : whereClause)
      {
        sb.append("\t");
        sb.append(constraint);
        sb.append("\n");
      }

      // at least one condition, prepend WHERE and remove AND from last condition
    }
    else
    {
      sb.append("WHERE");
      lastConstraint.setLength(lastConstraint.length() - " AND".length());
      for (StringBuffer constraint : whereClause)
      {
        sb.append("\n\t");
        sb.append(constraint);
      }
      sb.append("\n");
    }
  }

  ///// Getter / Setter
  public List<SelectClauseSqlGenerator> getSelectClauseSqlGenerators()
  {
    return selectClauseSqlGenerators;
  }

  public void setSelectClauseSqlGenerators(
    List<SelectClauseSqlGenerator> selectClauseSqlGenerators)
  {
    this.selectClauseSqlGenerators = selectClauseSqlGenerators;
  }

  public List<FromClauseSqlGenerator> getFromClauseSqlGenerators()
  {
    return fromClauseSqlGenerators;
  }

  public void setFromClauseSqlGenerators(
    List<FromClauseSqlGenerator> fromClauseSqlGenerators)
  {
    this.fromClauseSqlGenerators = fromClauseSqlGenerators;
  }

  public List<WhereClauseSqlGenerator> getWhereClauseSqlGenerators()
  {
    return whereClauseSqlGenerators;
  }

  public void setWhereClauseSqlGenerators(
    List<WhereClauseSqlGenerator> whereClauseSqlGenerators)
  {
    this.whereClauseSqlGenerators = whereClauseSqlGenerators;
  }
}
