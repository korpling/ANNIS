/*
 * Copyright 2012 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.sqlgen;

import annis.exceptions.AnnisQLSemanticsException;
import annis.service.objects.FrequencyTable;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import static annis.sqlgen.TableAccessStrategy.ANNOTATION_POOL_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import annis.sqlgen.extensions.FrequencyTableQueryData;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencySqlGenerator extends AbstractSqlGenerator<FrequencyTable>
  implements WhereClauseSqlGenerator<QueryData>, SelectClauseSqlGenerator<QueryData>,
  GroupByClauseSqlGenerator<QueryData>, FromClauseSqlGenerator<QueryData>
{
  
  private SqlGenerator<QueryData, ?> innerQuerySqlGenerator;

  @Override
  public FrequencyTable extractData(ResultSet rs) throws SQLException, DataAccessException
  {
    FrequencyTable result = new FrequencyTable();
    
    ResultSetMetaData meta = rs.getMetaData();
    
    while(rs.next())
    {
      Validate.isTrue(meta.getColumnCount() > 1, 
        "frequency table extractor needs at least 2 columns");
      
      Validate.isTrue(
        "count".equalsIgnoreCase(meta.getColumnName(meta.getColumnCount())),
        "last column name must be \"count\"");
      
      long count = rs.getLong("count");      
      String[] tupel = new String[meta.getColumnCount()-1];
      
      for(int i=1; i <= tupel.length; i++)
      {
        String colVal = rs.getString(i);
        tupel[i-1] = colVal;
      } // end for each column (except last "count" column) 
      
      result.addEntry(new FrequencyTable.Entry(tupel, count));
      
    } // end for complete result
    
    return result;
  }
  
  @Override
  public Set<String> whereConditions(QueryData queryData, List<QueryNode> alternative, String indent)
  {
    Set<String> conditions = new LinkedHashSet<String>();
    
    FrequencyTableQueryData ext;
    List<FrequencyTableQueryData> freqQueryData = queryData.getExtensions(FrequencyTableQueryData.class);
    Validate.notNull(freqQueryData);
    Validate.notEmpty(freqQueryData);    
    ext = freqQueryData.get(0);
    
    ImmutableMap<String, QueryNode> idxNodeVariables = Maps.uniqueIndex(
      alternative.iterator(), new Function<QueryNode, String>()
    {
      @Override
      public String apply(QueryNode input)
      {
        return input.getVariable();
      }
    });
    
    int i=1;
    for(FrequencyTableEntry e : ext)
    {
      // TODO: use alias names!
      
      // general partition restriction
      conditions.add("v" + i + ".toplevel_corpus IN (" + StringUtils.join(
        queryData.getCorpusList(), ",") + ")" );
      // specificly join on top level corpus
      conditions.add("v" + i + ".toplevel_corpus = solutions.toplevel_corpus" );
      // join on node ID
      QueryNode referencedNode = idxNodeVariables.get(e.getReferencedNode());
      if(referencedNode == null)
      {
        throw new AnnisQLSemanticsException("No such node \"" 
          + e.getReferencedNode() + "\". "
          + "Your query contains " + alternative.size() + " node(s), make sure no node definition numbers are greater than this number");
      }
      conditions.add("v" + i + ".id = solutions.id" + referencedNode.getId());
      
      if(e.getType() == FrequencyTableEntryType.span)
      {
        conditions.add("v" + i + ".n_sample IS TRUE" );
      }
      else
      {
        // join on node ID and toplevel_corpus
        conditions.add("a" + i + ".id = v" + i + ".node_anno_ref");
        conditions.add("a" + i + ".toplevel_corpus = v" + i + ".toplevel_corpus");
        // filter by selected key
        conditions.add("a" + i + ".name = '" + e.getKey().replaceAll("'",
          "''") + "'");
        conditions.add("v" + i + ".n_na_sample IS TRUE" );
        // general partition restriction
        conditions.add("a" + i + ".toplevel_corpus IN (" + StringUtils.join(
          queryData.getCorpusList(), ",") + ")");
      }
      i++;
    }
    
    return conditions;
  }
  
  @Override
  public String selectClause(QueryData queryData, List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tas = tables(null);
    
    FrequencyTableQueryData ext;
    List<FrequencyTableQueryData> freqQueryData = queryData.getExtensions(FrequencyTableQueryData.class);
    Validate.notNull(freqQueryData);
    Validate.notEmpty(freqQueryData);    
    ext = freqQueryData.get(0);
    
    StringBuilder sb = new StringBuilder();
    int i=1;
    for(FrequencyTableEntry e : ext)
    {
      if(e.getType() == FrequencyTableEntryType.annotation)
      {
        sb.append("a").append(i).append(".").append(
          tas.columnName(ANNOTATION_POOL_TABLE,
          "val"));
      }
      else
      {
        sb.append("v").append(i).append(".").append(
          tas.columnName(NODE_TABLE,
          "span"));
      }
      sb.append(" AS value").append(i).append(", ");
      i++;
    }
    
    sb.append("count(*) AS \"count\"");
    
    return sb.toString();
  }

  @Override
  public String groupByAttributes(QueryData queryData, List<QueryNode> alternative)
  {
    FrequencyTableQueryData ext;
    List<FrequencyTableQueryData> freqQueryData = queryData.getExtensions(FrequencyTableQueryData.class);
    Validate.notNull(freqQueryData);
    Validate.notEmpty(freqQueryData);    
    ext = freqQueryData.get(0);
    
    StringBuilder sb = new StringBuilder();
    for(int i=1; i <= ext.size(); i++)
    {
      sb.append("value").append(i);
      if(i < ext.size())
      {
        sb.append(", ");
      }
    }
    
    return sb.toString();
  }

  @Override
  public String fromClause(QueryData queryData, List<QueryNode> alternative, String indent)
  {
    TableAccessStrategy tas = tables(null);
    
    FrequencyTableQueryData ext;
    List<FrequencyTableQueryData> freqQueryData = queryData.getExtensions(FrequencyTableQueryData.class);
    Validate.notNull(freqQueryData);
    Validate.notEmpty(freqQueryData);    
    ext = freqQueryData.get(0);
    
    StringBuilder sb = new StringBuilder();

    sb.append(indent).append("(\n");
    sb.append(indent);

    sb.append(innerQuerySqlGenerator.toSql(queryData, indent + TABSTOP));
    sb.append(indent).append(") AS solutions,\n");

    int i = 1;
    Iterator<FrequencyTableEntry> itEntry = ext.iterator();
    while(itEntry.hasNext())
    {
      FrequencyTableEntry e = itEntry.next();
      
      sb.append(indent).append(TABSTOP);
      
      sb.append(TableAccessStrategy.partitionTableName(
        tas.getTableAliases(), tas.getTablePartitioned(), 
        NODE_TABLE, queryData.getCorpusList()));
      sb.append(" AS v").append(i);
      
      if(e.getType() == FrequencyTableEntryType.annotation)
      {
        sb.append(",\n");
        sb.append(indent).append(TABSTOP);
        // TODO: this only works for annopool scheme
        sb.append(TableAccessStrategy.partitionTableName(
          tas.getTableAliases(), tas.getTablePartitioned(), 
          ANNOTATION_POOL_TABLE, queryData.getCorpusList()));
        sb.append(" AS a").append(i);
      }
      
      if(itEntry.hasNext())
      {
        sb.append(",");
      }
      
      sb.append("\n");

      i++;
    }
    return sb.toString();
  }

  public SqlGenerator<QueryData, ?> getInnerQuerySqlGenerator()
  {
    return innerQuerySqlGenerator;
  }

  public void setInnerQuerySqlGenerator(SqlGenerator<QueryData, ?> innerQuerySqlGenerator)
  {
    this.innerQuerySqlGenerator = innerQuerySqlGenerator;
  }
  
}
