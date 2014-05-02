/*
 * Copyright 2013 SFB 632.
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
package annis.administration;

import com.google.common.base.Preconditions;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A helper class that allows you to fix the database scheme.
 * 
 * Currently it can
 * - create an corpus_alias table
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class SchemeFixer
{
  private final Logger log = LoggerFactory.getLogger(SchemeFixer.class);

  // use Spring's JDBC support
  private DataSource dataSource;
  private JdbcTemplate jdbcTemplate;

  /**
   *  Execute all fixes that are available.
   */
  public void checkAndFix()
  {
    corpusAlias();
  }
  
  protected void corpusAlias()
  {
    ResultSet result = null;
    Connection conn = null;
    try
    {
      conn = dataSource.getConnection();
      DatabaseMetaData dbMeta = conn.getMetaData();
      result = dbMeta.getColumns(null, null, "corpus_alias", null);
      
      Map<String, Integer> columnType = new HashMap<String,Integer>();
      
      while(result.next())
      {
        columnType.put(result.getString(4), result.getInt(5));
      }
      
      if(columnType.isEmpty())
      {
        // create the table
        log.info("Creating corpus_alias table");
        jdbcTemplate.execute(
          "CREATE TABLE corpus_alias\n"
          + "(\n"
          + "  alias text,\n"
          + "  corpus_ref bigint references corpus(id) ON DELETE CASCADE,\n"
          + "  PRIMARY KEY (alias, corpus_ref)\n"
          + ");");
      }
      else
      {
        // check if columns have correct type and name, if not throw an error
        Preconditions.checkState(Types.VARCHAR == columnType.get("alias"), "there must be an \"alias\" column of type \"text\"");
        Preconditions.checkState(Types.BIGINT == columnType.get("corpus_ref"), "there must be an \"corpus_ref\" column of type \"bigint\"");
      }
      
    }
    catch (SQLException ex)
    {
      log.error("Could not get the metadata for the database", ex);
    }
    finally
    {
      if(result != null)
      {
        try
        {
          result.close();
        }
        catch (SQLException ex1)
        {
          log.error("Could not close the result set", ex1);
        }
      }
      if(conn != null)
      {
        try
        {
          conn.close();
        }
        catch (SQLException ex1)
        {
          log.error("Could not close the result set", ex1);
        }
      }
    }
  }

  public JdbcTemplate getJdbcTemplate()
  {
    return jdbcTemplate;
  }

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate)
  {
    this.jdbcTemplate = jdbcTemplate;
  }

  public DataSource getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource)
  {
    this.dataSource = dataSource;
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }
  
  
  
}
