/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.test;

import au.com.bytecode.opencsv.CSVReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CsvResultSetProvider
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CsvResultSetProvider.class);

  @Mock
  protected ResultSet rs;
  private CSVReader reader;
  private String[] current;
  private Map<String, Integer> header;
  boolean wasNull;

  public CsvResultSetProvider(InputStream csvStream) throws SQLException
  {
    MockitoAnnotations.initMocks((CsvResultSetProvider) this);

    header = new HashMap<>();
    reader = new CSVReader(new InputStreamReader(csvStream), ';', '"');
    try
    {
      String[] firstLine = reader.readNext();
      for (int i = 0; firstLine != null && i < firstLine.length; i++)
      {
        header.put(firstLine[i], i);
      }
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }

    wasNull = false;

    // mock all needed methods of the ResultSet

    when(rs.next()).thenAnswer(new Answer<Boolean>()
    {

      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable
      {
        try
        {
          current = reader.readNext();
          return current != null && current.length > 0;
        }
        catch (IOException ex)
        {
          return false;
        }
      }
    });

    when(rs.wasNull()).thenAnswer(new Answer<Boolean>()
    {

      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable
      {
        return wasNull;
      }
    });
    
    when(rs.findColumn(anyString())).thenAnswer(new Answer<Integer>()
    {

      @Override
      public Integer answer(InvocationOnMock invocation) throws Throwable
      {
        String arg = (String) invocation.getArguments()[0];
        return getColumnByName(arg)+1;
      }
      
    });

    // getter with column position as argument
    when(rs.getString(anyInt())).thenAnswer(new Answer<String>()
    {

      @Override
      public String answer(InvocationOnMock invocation) throws Throwable
      {
        int idx = (Integer) invocation.getArguments()[0];
        return getStringValue(idx-1);
      }
    });

    when(rs.getLong(anyInt())).thenAnswer(new Answer<Long>()
    {

      @Override
      public Long answer(InvocationOnMock invocation) throws Throwable
      {
        int idx = (Integer) invocation.getArguments()[0];
        return getLongValue(idx-1);
      }
    });

    when(rs.getInt(anyInt())).thenAnswer(new Answer<Integer>()
    {

      @Override
      public Integer answer(InvocationOnMock invocation) throws Throwable
      {
        int idx = (Integer) invocation.getArguments()[0];
        return getIntValue(idx-1);
      }
    });

    when(rs.getBoolean(anyInt())).thenAnswer(new Answer<Boolean>()
    {

      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable
      {
        int idx = (Integer) invocation.getArguments()[0];
        return getBooleanValue(idx-1);
      }
    });

    when(rs.getArray(anyInt())).thenAnswer(new Answer<Array>()
    {

      @Override
      public Array answer(InvocationOnMock invocation) throws Throwable
      {
        int idx = (Integer) invocation.getArguments()[0];
        return new DummySQLArray(getArrayValue(
          idx-1, Types.VARCHAR), Types.VARCHAR,
          "VARCHAR");

      }
    });

    when(rs.getObject(anyInt())).thenAnswer(new Answer<Object>()
    {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable
      {
        int idx = (Integer) invocation.getArguments()[0];
        return getObjectValue(idx-1);
      }
    });

    // getter with column name as argument
    when(rs.getString(anyString())).thenAnswer(new Answer<String>()
    {

      @Override
      public String answer(InvocationOnMock invocation) throws Throwable
      {
        return getStringValue(getColumnByName(
          (String) invocation.getArguments()[0]));
      }
    });

    when(rs.getLong(anyString())).thenAnswer(new Answer<Long>()
    {

      @Override
      public Long answer(InvocationOnMock invocation) throws Throwable
      {
        return getLongValue(getColumnByName(
          (String) invocation.getArguments()[0]));
      }
    });

    when(rs.getInt(anyString())).thenAnswer(new Answer<Integer>()
    {

      @Override
      public Integer answer(InvocationOnMock invocation) throws Throwable
      {
        return getIntValue(getColumnByName(
          (String) invocation.getArguments()[0]));
      }
    });

    when(rs.getBoolean(anyString())).thenAnswer(new Answer<Boolean>()
    {

      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable
      {
        return getBooleanValue(getColumnByName(
          (String) invocation.getArguments()[0]));
      }
    });

    when(rs.getArray(anyString())).thenAnswer(new Answer<Array>()
    {

      @Override
      public Array answer(InvocationOnMock invocation) throws Throwable
      {
        // HACK: we don't know how to get the type, use our knowledge
        if ("key".equalsIgnoreCase((String) invocation.getArguments()[0]))
        {
          return new DummySQLArray(getArrayValue(getColumnByName(
            (String) invocation.getArguments()[0]), Types.BIGINT), Types.BIGINT,
            "BIGINT");
        }
        else
        {
          return new DummySQLArray(getArrayValue(getColumnByName(
            (String) invocation.getArguments()[0]), Types.VARCHAR),
            Types.VARCHAR, "VARCHAR");
        }
      }
    });

    when(rs.getObject(anyString())).thenAnswer(new Answer<Object>()
    {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable
      {
        return getObjectValue(getColumnByName(
          (String) invocation.getArguments()[0]));
      }
    });

  }

  public int getColumnByName(String name)
  {
    if (header.containsKey(name))
    {
      return header.get(name);
    }
    return -1;
  }
  
  public Object getObjectValue(int column)
  {
    String rawString = getStringValue(column);
    try
    {
      return Long.parseLong(rawString);
    }
    catch(NumberFormatException ex)
    {
      // ignore
    }
    
    return rawString;
  }

  public String getStringValue(int column)
  {
    if (current != null && column >= 0)
    {
      String val = current[column];
      if (!"".equals(val))
      {
        wasNull = false;
        return val;
      }
    }
    wasNull = true;
    return null;
  }

  public long getLongValue(int column)
  {
    String str = getStringValue(column);
    if (str != null)
    {
      try
      {
        Long l = Long.parseLong(str);
        return l;
      }
      catch (NumberFormatException ex)
      {
      }
    }

    return 0l;
  }

  public int getIntValue(int column)
  {
    String str = getStringValue(column);
    if (str != null)
    {
      try
      {
        Integer l = Integer.parseInt(str);
        return l;
      }
      catch (NumberFormatException ex)
      {
      }
    }

    return 0;
  }

  public boolean getBooleanValue(int column)
  {
    String str = getStringValue(column);
    if (str != null)
    {
      try
      {
        if ("t".equalsIgnoreCase(str) || "true".equalsIgnoreCase(str))
        {
          return true;
        }
      }
      catch (NumberFormatException ex)
      {
      }
    }

    return false;
  }

  public Object[] getArrayValue(int column, int sqlType)
  {
    String str = getStringValue(column);
    if (StringUtils.startsWith(str, "{") && StringUtils.endsWith(str, "}"))
    {
      String stripped = str.substring(1, str.length() - 1);
      String[] split = stripped.split(",");

      if (sqlType == Types.BIGINT)
      {
        Long[] result = new Long[split.length];
        for (int i = 0; i < result.length; i++)
        {
          try
          {
            result[i] = Long.parseLong(split[i]);
          }
          catch (NumberFormatException ex)
          {
            log.error(null, ex);
          }
        }
        return result;
      }
      else
      {
        // just return the string if requested so
        return split;
      }
    }

    return null;
  }

  public ResultSet getResultSet()
  {
    return rs;
  }

  public static class DummySQLArray implements Array
  {

    private Object[] base;
    private int sqlType;
    private String sqlTypeName;

    public DummySQLArray(Object[] base, int sqlType, String sqlTypeName)
    {
      this.base = base;
      this.sqlType = sqlType;
      this.sqlTypeName = sqlTypeName;
    }

    @Override
    public String getBaseTypeName() throws SQLException
    {
      return sqlTypeName;
    }

    @Override
    public int getBaseType() throws SQLException
    {
      return sqlType;
    }

    @Override
    public Object getArray() throws SQLException
    {
      return base;
    }

    @Override
    public Object getArray(Map<String, Class<?>> arg0) throws SQLException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getArray(long arg0, int arg1) throws SQLException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getArray(long arg0, int arg1,
      Map<String, Class<?>> arg2) throws SQLException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResultSet getResultSet() throws SQLException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResultSet getResultSet(Map<String, Class<?>> arg0) throws
      SQLException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResultSet getResultSet(long arg0, int arg1) throws SQLException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResultSet getResultSet(long arg0, int arg1,
      Map<String, Class<?>> arg2) throws SQLException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void free() throws SQLException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
}
