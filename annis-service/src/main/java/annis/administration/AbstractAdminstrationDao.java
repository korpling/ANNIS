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

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import annis.dao.AbstractDao;
import annis.dao.QueryDao;

/**
 * Contains common functions used in the different adminstration DAOs
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public abstract class AbstractAdminstrationDao extends AbstractDao
{

  private final static Logger log = LoggerFactory.getLogger(
    AbstractAdminstrationDao.class);

  private String externalFilesPath;
  
  private QueryDao queryDao;

  protected boolean lockRepositoryMetadataTable(boolean waitForOtherTasks)
  {
    try
    {
      log.info("Locking repository_metadata table to ensure no other import is running");
      getJdbcTemplate().execute(
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

  /**
   * Checks, if there already exists a top level corpus.
   *
   * @param topLevelCorpusName The name of the corpus, which is checked.
   * @return Is false, if the no top level coprpus exists.
   */
  protected boolean existConflictingTopLevelCorpus(String topLevelCorpusName)
  {
    String sql = "SELECT count(name) as amount FROM corpus WHERE top_level=true AND name='"
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

  public String getExternalFilesPath()
  {
    return externalFilesPath;
  }

  public void setExternalFilesPath(String externalFilesPath)
  {
    this.externalFilesPath = externalFilesPath;
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

  public QueryDao getQueryDao()
  {
    return queryDao;
  }

  public void setQueryDao(QueryDao queryDao)
  {
    this.queryDao = queryDao;
  }
  

}
