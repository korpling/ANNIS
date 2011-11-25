/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.test;

import java.util.Map;
import au.com.bytecode.opencsv.CSVReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.*;

/**
 *
 * @author thomas
 */
public class CSVResultSetProvider
{

  @Mock
  protected ResultSet rs;
  private CSVReader reader;
  private String[] current;
  private Map<String, Integer> header;
  boolean wasNull;

  public CSVResultSetProvider(InputStream csvStream) throws SQLException
  {
    MockitoAnnotations.initMocks((CSVResultSetProvider) this);

    header = new HashMap<String, Integer>();
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
      Logger.getLogger(CSVResultSetProvider.class.getName()).
        log(Level.SEVERE, null, ex);
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

    // getter with column position as argument
    when(rs.getString(anyInt())).thenAnswer(new Answer<String>()
    {

      @Override
      public String answer(InvocationOnMock invocation) throws Throwable
      {
        return getStringValue((Integer) invocation.getArguments()[0]);
      }
    });

    when(rs.getLong(anyInt())).thenAnswer(new Answer<Long>()
    {

      @Override
      public Long answer(InvocationOnMock invocation) throws Throwable
      {
        return getLongValue((Integer) invocation.getArguments()[0]);
      }
    });

    when(rs.getInt(anyInt())).thenAnswer(new Answer<Integer>()
    {

      @Override
      public Integer answer(InvocationOnMock invocation) throws Throwable
      {
        return getIntValue((Integer) invocation.getArguments()[0]);
      }
    });

    when(rs.getBoolean(anyInt())).thenAnswer(new Answer<Boolean>()
    {

      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable
      {
        return getBooleanValue((Integer) invocation.getArguments()[0]);
      }
    });

    when(rs.getArray(anyInt())).thenAnswer(new Answer<Array>()
    {

      @Override
      public Array answer(InvocationOnMock invocation) throws Throwable
      {
        return new DummySQLArray(getArrayValue(
          (Integer) invocation.getArguments()[0], Types.VARCHAR), Types.VARCHAR,
          "VARCHAR");

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

  }

  public int getColumnByName(String name)
  {
    if (header.containsKey(name))
    {
      return header.get(name);
    }
    return -1;
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
            Logger.getLogger(CSVResultSetProvider.class.getName()).log(
              Level.SEVERE, null, ex);
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
