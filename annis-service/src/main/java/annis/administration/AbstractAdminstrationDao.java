/*
 * Copyright 2014 SFB 632.
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

import annis.dao.AnnisDao;
import annis.utils.DynamicDataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Contains common functions used in the different adminstration DAOs
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public abstract class AbstractAdminstrationDao
{

  private final static Logger log = LoggerFactory.getLogger(
    AbstractAdminstrationDao.class);

  private JdbcTemplate jdbcTemplate;
  private String externalFilesPath;
  // script path
  private String scriptPath;
  private StatementController statementController;
  private DynamicDataSource dataSource;
  private AnnisDao annisDao;

  protected boolean lockRepositoryMetadataTable(boolean waitForOtherTasks)
  {
    try
    {
      log.info("Locking repository_metadata table to ensure no other import is running");
      jdbcTemplate.execute(
        "LOCK TABLE repository_metadata IN EXCLUSIVE MODE" + (waitForOtherTasks ? ""
          : " NOWAIT"));
      return true;
    }
    catch (DataAccessException ex)
    {
      return false;
    }
  }

  protected File getRealDataDir()
  {
    File dataDir;
    if (getExternalFilesPath() == null || getExternalFilesPath().isEmpty())
    {
      // use the default directory
      dataDir = new File(System.getProperty("user.home"), ".annis/data/");
    }
    else
    {
      dataDir = new File(getExternalFilesPath());
    }
    return dataDir;
  }

  protected MapSqlParameterSource makeArgs()
  {
    return new MapSqlParameterSource();
  }

  /**
   * executes an SQL script from $ANNIS_HOME/scripts, substituting the
   * parameters found in args
   *
   * @param script
   * @param args
   * @return
   */
  protected PreparedStatement executeSqlFromScript(String script,
    MapSqlParameterSource args)
  {
    File fScript = new File(scriptPath, script);
    if (fScript.canRead() && fScript.isFile())
    {
      Resource resource = new FileSystemResource(fScript);
      log.debug("executing SQL script: " + resource.getFilename());
      String sql = readSqlFromResource(resource, args);
      CancelableStatements cancelableStats
        = new CancelableStatements(
          sql, statementController);

      // register the statement, so we could try to interrupt it in the gui.
      if (statementController != null)
      {
        statementController.registerStatement(cancelableStats.statement);
      }
      else
      {
        log.debug("statement controller is not initialized");
      }

      jdbcTemplate.execute(cancelableStats, cancelableStats);
      return cancelableStats.statement;
    }
    else
    {
      log.debug("SQL script " + fScript.getName() + " does not exist");
      return null;
    }
  }
  
    // reads the content from a resource into a string
  @SuppressWarnings("unchecked")
  private String readSqlFromResource(Resource resource,
    MapSqlParameterSource args)
  {
    // XXX: uses raw type, what are the parameters to Map in MapSqlParameterSource?
    Map<String, Object> parameters = args != null ? args.getValues()
      : new HashMap();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
      new FileInputStream(
        resource.
        getFile()), "UTF-8"));)
    {
      StringBuilder sqlBuf = new StringBuilder();

      for (String line = reader.readLine(); line != null; line
        = reader.readLine())
      {
        sqlBuf.append(line).append("\n");
      }
      String sql = sqlBuf.toString();
      for (Map.Entry<String, Object> placeHolderEntry : parameters.entrySet())
      {
        String key = placeHolderEntry.getKey();
        String value = placeHolderEntry.getValue().toString();
        log.debug("substitution for parameter '" + key + "' in SQL script: "
          + value);
        sql = sql.replaceAll(key, Matcher.quoteReplacement(value));
      }
      return sql;
      }
      catch (IOException e)
      {
        log.error("Couldn't read SQL script from resource file.", e);
        throw new FileAccessException(
          "Couldn't read SQL script from resource file.", e);
      }
  }
  
  /**
   * Checks, if there already exists a top level corpus.
   *
   * @param topLevelCorpusName The name of the corpus, which is checked.
   * @return Is false, if the no top level coprpus exists.
   */
  protected boolean existConflictingTopLevelCorpus(String topLevelCorpusName)
  {
    String sql = "SELECT count(name) as amount FROM corpus WHERE name='"
      + topLevelCorpusName + "'";
    Integer numberOfCorpora = getJdbcTemplate().query(sql,
      new ResultSetExtractor<Integer>()
      {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException,
        DataAccessException
        {
          if (rs.next())
          {
            return rs.getInt("amount");
          }
          else
          {
            return 0;
          }
        }
      });

    return numberOfCorpora > 0;
  }
  
  // tables in the staging area have their names prefixed with "_"
  protected String tableInStagingArea(String table)
  {
    return "_" + table;
  }

  public JdbcTemplate getJdbcTemplate()
  {
    return jdbcTemplate;
  }

  public DynamicDataSource getDataSource()
  {
    return dataSource;
  }

  
  public void setDataSource(DynamicDataSource dataSource)
  {
    this.dataSource = dataSource;
    jdbcTemplate = new JdbcTemplate(dataSource);
  }


  public String getExternalFilesPath()
  {
    return externalFilesPath;
  }

  public void setExternalFilesPath(String externalFilesPath)
  {
    this.externalFilesPath = externalFilesPath;
  }

  public String getScriptPath()
  {
    return scriptPath;
  }

  public void setScriptPath(String scriptPath)
  {
    this.scriptPath = scriptPath;
  }
  
  
  public void registerGUICancelThread(StatementController statementCon)
  {
    this.statementController = statementCon;
  }
  
  /**
   * Registers a {@link PreparedStatement} to the {@link StatementController}.
   */
  private class CancelableStatements implements PreparedStatementCreator,
    PreparedStatementCallback<Void>
  {

    String sqlQuery;

    PreparedStatement statement;

    public CancelableStatements(String sql,
      StatementController statementController)
    {
      sqlQuery = sql;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection con) throws
      SQLException
    {
      if (statementController != null && statementController.isCancelled())
      {
        throw new SQLException("process was cancelled");
      }

      statement = con.prepareCall(sqlQuery);
      if (statementController != null)
      {
        statementController.registerStatement(statement);
      }
      return statement;
    }

    @Override
    public Void doInPreparedStatement(PreparedStatement ps) throws SQLException,
      DataAccessException
    {
      ps.execute();
      return null;
    }
  }
  
  /**
   * Closes all open idle connections. The current data source
   * must have superuser rights.
   * 
   * This can be used if a another database action needs full access to a database,
   * e.g. when deleting and then creating it
   * @param databasename
   */
  protected void closeAllConnections(String databasename)
  {
    String sql
      = "SELECT pg_terminate_backend(pg_stat_activity.pid)\n"
      + "FROM pg_stat_activity\n"
      + "WHERE pg_stat_activity.datname = ?\n"
      + "  AND pid <> pg_backend_pid();";
    try(Connection conn = getDataSource().getConnection())
    {
      DatabaseMetaData meta = conn.getMetaData();
      
      if(meta.getDatabaseMajorVersion() == 9 
        && meta.getDatabaseMinorVersion() <= 1)
      {
        sql
          = "SELECT pg_terminate_backend(pg_stat_activity.procpid)\n"
          + "FROM pg_stat_activity\n"
          + "WHERE pg_stat_activity.datname = ?\n"
          + "  AND procpid <> pg_backend_pid();";
      }
    }
    catch(SQLException ex)
    {
      log.warn("Could not get the PostgreSQL version", ex);
    }
    
    getJdbcTemplate().queryForRowSet(sql, databasename);

  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }
  

}
