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

import annis.AnnisRunnerException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.postgresql.Driver;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author thomas
 */
public abstract class CorpusAdministration
{

  private Logger log = Logger.getLogger(this.getClass());

  public CorpusAdministration()
  {
  }

  protected DataSource createDataSource(String host, String port,
    String database,
    String user, String password)
  {
    String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

    // DriverManagerDataSource is deprecated
    // return new DriverManagerDataSource("org.postgresql.Driver", url, user, password);

    // why is this better?
    // XXX: how to construct the datasource?    
    return new SimpleDriverDataSource(new Driver(), url, user, password);
  }

  @Transactional(readOnly = false)
  public void deleteCorpora(List<Long> ids)
  {
    // check if corpus exists
    List<Long> corpora = getAdministrationDao().listToplevelCorpora();
    for (long id : ids)
    {
      if (!corpora.contains(id))
      {
        throw new AnnisRunnerException("Corpus does not exist (or is not a top-level corpus): "
          + id);
      }
    }
    log.info("Deleting corpora: " + ids);
    getAdministrationDao().deleteCorpora(ids);
    log.info("Finished deleting corpora: " + ids);
  }

  public void initializeDatabase(String host, String port, String database,
    String user, String password, String defaultDatabase, String superUser,
    String superPassword)
  {
    log.info("initializing database with schema "
      + getSchemeType().getDescription());

    // connect as super user to the default database to create new user and database
    getAdministrationDao().setDataSource(createDataSource(host, port,
      defaultDatabase,
      superUser, superPassword));

    getAdministrationDao().dropDatabase(database);
    getAdministrationDao().dropUser(user);
    getAdministrationDao().createUser(user, password);
    getAdministrationDao().createDatabase(database);

    // switch to new database, but still as super user to install stored procedure compute_rank_level
    getAdministrationDao().setDataSource(createDataSource(host, port, database,
      superUser, superPassword));
    getAdministrationDao().installPlPgSql();
    getAdministrationDao().createFunctionUniqueToplevelCorpusName();

    // switch to new database as new user for the rest
    getAdministrationDao().setDataSource(createDataSource(host, port, database,
      user,
      password));

    getAdministrationDao().createSchema(getSchemeType());
    getAdministrationDao().populateResolverTable();

    // write database information to property file
    writeDatabasePropertiesFile(host, port, database, user, password);
  }
  
  @Transactional(readOnly = false)
  public void importCorpora(boolean temporaryStagingArea,
    List<String> paths)
  {
    // import each corpus
    for (String path : paths)
    {
      log.info("Importing corpus from: " + path);
      getAdministrationDao().createStagingArea(temporaryStagingArea);
      getAdministrationDao().bulkImport(path);
      getAdministrationDao().computeTopLevelCorpus();
      long corpusID = getAdministrationDao().updateIds();
      getAdministrationDao().importBinaryData(path);
      getAdministrationDao().createStagingAreaIndexes();
      getAdministrationDao().analyzeStagingTables();
      //			// finish transaction here to debug computation of left|right-token
      //if (true) return;
      getAdministrationDao().computeLeftTokenRightToken();
      //      if (true) return;
      getAdministrationDao().computeRealRoot();
      getAdministrationDao().computeLevel();
      getAdministrationDao().computeCorpusStatistics();
      getAdministrationDao().updateCorpusStatsId(corpusID);
      getAdministrationDao().applyConstraints();
      getAdministrationDao().analyzeStagingTables();
      getAdministrationDao().insertCorpus();
      getAdministrationDao().computeCorpusPath(corpusID);
      getAdministrationDao().createAnnotations(corpusID);

      // create the new facts table partition
      getAdministrationDao().createFacts(corpusID, getSchemeType());

      if (temporaryStagingArea)
      {
        getAdministrationDao().dropStagingArea();
      }
      getAdministrationDao().analyzeFacts(corpusID);
      log.info("Finished import from: " + path);
    }
  }

  
  public void importCorpora(boolean temporaryStagingArea, String... paths)
  {
    importCorpora(temporaryStagingArea, Arrays.asList(paths));
  }

  public List<Map<String, Object>> listCorpusStats()
  {
    return getAdministrationDao().listCorpusStats();
  }

  public List<Map<String, Object>> listTableStats()
  {
    return getAdministrationDao().listTableStats();
  }

  public List<String> listUnusedIndexes()
  {
    return getAdministrationDao().listUnusedIndexes();
  }

  public List<String> listUsedIndexes()
  {
    return getAdministrationDao().listUsedIndexes();
  }

  ///// Helper
  protected void writeDatabasePropertiesFile(String host, String port,
    String database, String user, String password)
  {
    File file = new File(System.getProperty("annis.home") + "/conf",
      "database.properties");
    try
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      writer.write("# database configuration\n");
      writer.write("datasource.driver=org.postgresql.Driver\n");
      writer.write("datasource.url=jdbc:postgresql://" + host + ":" + port + "/"
        + database + "\n");
      writer.write("datasource.username=" + user + "\n");
      writer.write("datasource.password=" + password + "\n");
      writer.close();
    }
    catch (IOException e)
    {
      log.error("Couldn't write database properties file", e);
      throw new FileAccessException(e);
    }
    log.info("Wrote database configuration to " + file.getAbsolutePath());
  }

  ///// Getter / Setter
  public abstract SpringAnnisAdministrationDao getAdministrationDao();

  public abstract void setAdministrationDao(
    SpringAnnisAdministrationDao administrationDao);

  public abstract SchemeType getSchemeType();
}
