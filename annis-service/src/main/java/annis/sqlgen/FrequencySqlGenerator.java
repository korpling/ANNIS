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

import annis.service.objects.FrequencyTable;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.sqlgen.extensions.FrequencyTableQueryData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public abstract class FrequencySqlGenerator extends AbstractSqlGenerator
  implements WhereClauseSqlGenerator<QueryData>, SelectClauseSqlGenerator<QueryData>,
  GroupByClauseSqlGenerator<QueryData>, FromClauseSqlGenerator<QueryData>,
  SqlGeneratorAndExtractor<QueryData, FrequencyTable>
{
  
  private SolutionSqlGenerator solutionSqlGenerator;

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
  public abstract Set<String> whereConditions(QueryData queryData, List<QueryNode> alternative, String indent);
  
  @Override
  public abstract String selectClause(QueryData queryData, List<QueryNode> alternative, String indent);

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
  public abstract String fromClause(QueryData queryData, List<QueryNode> alternative, String indent);

  public SolutionSqlGenerator getSolutionSqlGenerator()
  {
    return solutionSqlGenerator;
  }

  public void setSolutionSqlGenerator(SolutionSqlGenerator solutionSqlGenerator)
  {
    this.solutionSqlGenerator = solutionSqlGenerator;
  }

 
}
