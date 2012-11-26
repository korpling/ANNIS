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

import annis.dao.objects.FrequencyTable;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import static annis.sqlgen.TableAccessStrategy.ANNOTATION_POOL_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import annis.sqlgen.extensions.FrequencyTableQueryData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
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
      String[] tupel = new String[meta.getColumnCount()-2];
      
      for(int i=1; i <= tupel.length; i++)
      {
        String colName = meta.getColumnName(i);
        tupel[i-1] = colName;
      } // end for each column (except last "count" column) 
      
      result.addEntry(new FrequencyTable.Entry(tupel, count));
      
    } // end for complete result
    
    return result;
  }
  
  @Override
  public Set<String> whereConditions(QueryData queryData, List<QueryNode> alternative, String indent)
  {
    return new HashSet<String>();
//    throw new UnsupportedOperationException("Not supported yet.");
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
    for(FrequencyTableQueryData.Entry e : ext)
    {
      if(e.getType() == FrequencyTableQueryData.Type.annotation)
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
    for(FrequencyTableQueryData.Entry e : ext)
    {
      sb.append(indent).append(TABSTOP);
      sb.append(TableAccessStrategy.tableName(tas.getTableAliases(), NODE_TABLE));
      sb.append(" AS v").append(i).append("\n");
      
      if(e.getType() == FrequencyTableQueryData.Type.annotation)
      {
        sb.append(indent).append(TABSTOP);
        // TODO: this only works for annopool scheme
        sb.append(TableAccessStrategy.tableName(tas.getTableAliases(), ANNOTATION_POOL_TABLE));
        sb.append(" AS a").append(i).append("\n");
      }

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
