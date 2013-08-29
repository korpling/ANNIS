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
package annis.administration;

import annis.exceptions.AnnisException;
import annis.security.AnnisUserConfig;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 *
 * @author thomas
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public interface AdministrationDao
{

  public List<Long> listToplevelCorpora();

  public void deleteCorpora(List<Long> ids, boolean acquireLock);

  public void initializeDatabase(String host, String port, String database,
    String user, String password, String defaultDatabase, String superUser,
    String superPassword, boolean useSSL);

  /**
   * Reads relAnnis files from several directories.
   *
   * @param path Specifies the path to the corpora, which should be imported.
   * @param overwrite If set to true conflicting top level corpora are deleted.
   * @param waitForOtherTasks If true wait for other tasks to finish, if false abort.
   * 
   * @return true if successful
   */
  public boolean importCorpus(String path, boolean overwrite, boolean waitForOtherTasks);
  
  public List<Map<String, Object>> listCorpusStats();

  public List<String> listUsedIndexes();

  public List<String> listUnusedIndexes();

  public String getDatabaseSchemaVersion();

  public boolean checkDatabaseSchemaVersion() throws AnnisException;

  public PreparedStatement executeSqlFromScript(String script);

  public PreparedStatement executeSqlFromScript(String script,
    MapSqlParameterSource args);

  public AnnisUserConfig retrieveUserConfig(String userName);

  public void registerGUICancelThread(StatementController statementCon);

  /**
   * Provides a interface to cancel {@link PreparedStatement} via a gui.
   */
  public interface StatementController
  {

    /**
     * Registers a sql statement.
     *
     * @param statement The statement which maybe get cancelled.
     */
    public void registerStatement(PreparedStatement statement);

    /**
     * Interrupts a sql statement via the JDBC-Driver.
     *
     * <p>It relies on the actual implementation of the JDBC-Driver, if this
     * method has an effect.</p>
     *
     * <p>If the {@link PreparedStatement#cancel()} It also set the internal
     * isCancelled-flat to true, so {@link #isCancelled()} will always return
     * true. This behaviour is inspired by the method interrupt method of the
     * {@link Thread#interrupt()} method. The implementation of the
     * {@link AdministrationDao} interface should poll against this flag and do
     * not execute further sql-statements.</p>
     *
     */
    public void cancelStatements();

    /**
     * Returns true when {@link #cancelStatements()} has been executed at most
     * once.
     *
     * @return the value True signals, that no sql statements should be executed
     * anymore.
     */
    public boolean isCancelled();
  }

  public void storeUserConfig(AnnisUserConfig config);
}
