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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.postgresql.Driver;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.annotation.Transactional;
import annis.AnnisRunnerException;
import annis.exceptions.AnnisException;
import java.util.logging.Level;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class CorpusAdministration
{

  private AdministrationDao administrationDao;
  private static final Logger log = LoggerFactory.getLogger(CorpusAdministration.class);

  public CorpusAdministration()
  {
  }

  @Transactional(readOnly = false)
  public void deleteCorpora(List<Long> ids)
  {
    // check if corpus exists
    List<Long> corpora = administrationDao.listToplevelCorpora();
    for (long id : ids)
    {
      if (!corpora.contains(id))
      {
        throw new AnnisRunnerException("Corpus does not exist (or is not a top-level corpus): "
          + id);
      }
    }
    log.info("Deleting corpora: " + ids);
    administrationDao.deleteCorpora(ids);
    log.info("Finished deleting corpora: " + ids);
  }

  public void initializeDatabase(String host, String port, String database,
    String user, String password, String defaultDatabase, String superUser,
    String superPassword)
  {

    log.info("initializing database");
    administrationDao.initializeDatabase(host, port, database, user, password,
      defaultDatabase, superUser, superPassword);

    // write database information to property file
    writeDatabasePropertiesFile(host, port, database, user, password);
  }

  public void importCorpora(List<String> paths)
  {

    // import each corpus
    for (String path : paths)
    {
      log.info("Importing corpus from: " + path);
      administrationDao.importCorpus(path);
      log.info("Finished import from: " + path);

    }
  }

  public boolean checkDatabaseSchemaVersion()
  {
    try
    {
      administrationDao.checkDatabaseSchemaVersion();
    }
    catch(AnnisException ex)
    {
      return false;
    }
    return true;
  }

  public void importCorpora(String... paths)
  {
    importCorpora(Arrays.asList(paths));
  }

  public List<Map<String, Object>> listCorpusStats()
  {
    return administrationDao.listCorpusStats();
  }

  public List<String> listUsedIndexes()
  {
    return administrationDao.listUsedIndexes();
  }

  public List<String> listUnusedIndexes()
  {
    return administrationDao.listUnusedIndexes();
  }

  ///// Helper
  protected void writeDatabasePropertiesFile(String host, String port,
    String database, String user, String password)
  {
    File file = new File(System.getProperty("annis.home") + "/conf",
      "database.properties");
    BufferedWriter writer = null;
    try
    {
      writer = new BufferedWriter(new FileWriterWithEncoding(file, "UTF-8"));
      writer.write("# database configuration\n");
      writer.write("datasource.driver=org.postgresql.Driver\n");
      writer.write("datasource.url=jdbc:postgresql://" + host + ":" + port + "/"
        + database + "\n");
      writer.write("datasource.username=" + user + "\n");
      writer.write("datasource.password=" + password + "\n");
    }
    catch (IOException e)
    {
      log.error("Couldn't write database properties file", e);
      throw new FileAccessException(e);
    }
    finally
    {
      if(writer != null)
      {
        try
        {
          writer.close();
        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
      }
    }
    log.info("Wrote database configuration to " + file.getAbsolutePath());
  }

  ///// Getter / Setter
  public AdministrationDao getAdministrationDao()
  {
    return administrationDao;
  }

  public void setAdministrationDao(AdministrationDao administrationDao)
  {
    this.administrationDao = administrationDao;
  }
}
