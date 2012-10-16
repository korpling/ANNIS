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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class FrequencySqlGenerator extends AbstractSqlGenerator<FrequencyTable>
{

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
  
}
