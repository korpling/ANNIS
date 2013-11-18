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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import annis.AnnisRunnerException;
import annis.exceptions.AnnisException;
import annis.service.objects.ImportJob;
import java.util.LinkedList;
import javax.mail.internet.InternetAddress;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CorpusAdministration
{

  private AdministrationDao administrationDao;
  private SchemeFixer schemeFixer;
  private String statusMailSender;

  private static final Logger log = LoggerFactory.getLogger(
    CorpusAdministration.class);

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
        throw new AnnisRunnerException(
          "Corpus does not exist (or is not a top-level corpus): "
          + id);
      }
    }
    log.info("Deleting corpora: " + ids);
    administrationDao.deleteCorpora(ids, true);
    log.info("Finished deleting corpora: " + ids);
  }

  public void initializeDatabase(String host, String port, String database,
    String user, String password, String defaultDatabase, String superUser,
    String superPassword, boolean useSSL)
  {

    log.info("initializing database");
    administrationDao.initializeDatabase(host, port, database, user, password,
      defaultDatabase, superUser, superPassword, useSSL);

    // write database information to property file
    writeDatabasePropertiesFile(host, port, database, user, password, useSSL);
  }

  /**
   * Imports several corpora and catches a possible thrown
   * {@link DefaultAdministrationDao.ConflictingCorpusException} when the
   * overwrite flag is set to false.
   *
   * @param paths Valid pathes to corpora.
   * @param aliasName An common alias name for all imported corpora or null
   * @param statusEmailAdress If not null an e-mail will be sent to this address 
   * whenever the import status changes.
   * @param overwrite If set to false, a conflicting corpus is not silently
   * reimported.
   * @param waitForOtherTasks If true wait for other imports to finish, 
   * if false abort the import.
   * @return True if all corpora where imported successfully.
   */
  public boolean importCorporaSave(boolean overwrite, 
    String aliasName,
    String statusEmailAdress, boolean waitForOtherTasks, List<String> paths)
  {
    boolean result = true;
    
    // check if database scheme is ok
    if(schemeFixer != null)
    {
      schemeFixer.checkAndFix();
    }
    
    // import each corpus
    for (String path : paths)
    {
      try
      {
        log.info("Importing corpus from: " + path);
        if(administrationDao.importCorpus(path, aliasName,
          overwrite, waitForOtherTasks))
        {
          log.info("Finished import from: " + path);
          sendStatusMail(statusEmailAdress, path, ImportJob.Status.SUCCESS, null);
        }
        else
        {
          result = false;
          sendStatusMail(statusEmailAdress, path, ImportJob.Status.ERROR, null);
        }
      }
      catch (DefaultAdministrationDao.ConflictingCorpusException ex)
      {
        result = false;
        log.error(ex.getMessage());
        sendStatusMail(statusEmailAdress, path, ImportJob.Status.ERROR, ex.getMessage());
      }
      catch(Throwable ex)
      {
        result = false;
        log.error("Error on importing corpus", ex);
        sendStatusMail(statusEmailAdress, path, ImportJob.Status.ERROR, ex.getMessage());
      }
    }
    
    return result;
  }
  
  public void sendStatusMail(String adress, String corpusPath, 
    ImportJob.Status status, String additionalInfo)
  {
    if(adress == null || corpusPath == null)
    {
      return;
    }
    
    // check valid properties
    if(statusMailSender == null || statusMailSender.isEmpty())
    {
      log.warn("Could not send status mail because \"annis.mail-sender\" "
        + "property was not configured in conf/annis-service-properties.");
      return;
    }
    
    try
    {
      SimpleEmail mail = new SimpleEmail();
      List<InternetAddress> to = new LinkedList<InternetAddress>();
      to.add(new InternetAddress(adress));

      StringBuilder sbMsg = new StringBuilder();
      sbMsg.append("Dear Sir or Madam,\n");
      sbMsg.append("\n");
      sbMsg.append("this is the requested status update to the ANNIS corpus import "
        + "you have started. Please note that this message is automated and "
        + "if you have any question regarding the import you have to ask the "
        + "administrator of the ANNIS instance directly.\n\n");
      
      mail.setTo(to);
      if(status == ImportJob.Status.SUCCESS)
      {
        mail.setSubject("ANNIS import finished successfully (" + corpusPath + ")");
        sbMsg.append("Status:\nThe corpus \"").append(corpusPath)
          .append("\" was successfully imported and can be used from now on.\n");
      }
      else if(status == ImportJob.Status.ERROR)
      {
        mail.setSubject("ANNIS import *failed* (" + corpusPath + ")");
        sbMsg.append("Status:\nUnfortunally the corpus \"").append(corpusPath).append(
          "\" could not be imported successfully. "
          + "You may ask the administrator of the ANNIS installation for "
          + "assistance why the corpus import failed.\n");
      }
      else if(status == ImportJob.Status.RUNNING)
      {
        mail.setSubject("ANNIS import started (" + corpusPath + ")");
        sbMsg.append("Status:\nThe import of the corpus \"").append(corpusPath)
          .append("\" was started.\n");
      }
      else if(status == ImportJob.Status.WAITING)
      {
        mail.setSubject("ANNIS import was scheduled (" + corpusPath + ")");
        sbMsg.append("Status:\nThe import of the corpus \"").append(corpusPath)
          .append("\" was scheduled and is currently waiting for other imports to "
          + "finish. As soon as the previous imports are finished this import "
          + "job will be executed.\n");
      }
      else
      {
        // we don't know how to handle this, just don't send a message
        return;
      }
      if(additionalInfo != null && !additionalInfo.isEmpty())
      {
        sbMsg.append("Addtional information:\n");
        sbMsg.append(additionalInfo).append("\n");
      }
      
      sbMsg.append("\n\nSincerely yours,\n\nthe ANNIS import service.");
      mail.setMsg(sbMsg.toString());
      mail.setHostName("localhost");
      mail.setFrom(statusMailSender);
      
      mail.send();
      log.info("Send status ({}) mail to {}.", new String[] {status.name(), adress});
      
    }
    catch(Throwable ex)
    {
      log.warn("Could not send mail: " + ex.getMessage());
    }
  }

  public boolean checkDatabaseSchemaVersion()
  {
    try
    {
      administrationDao.checkDatabaseSchemaVersion();
    }
    catch (AnnisException ex)
    {
      return false;
    }
    return true;
  }

  /**
   * Imports several corpora.
   *
   * @param overwrite if false, a conflicting top level corpus is silently
   * skipped.
   * @param aliasName An common alias name for all imported corpora or null
   * @param statusEmailAdress If not null the email adress of the user who 
   * started the import.
   * @param waitForOtherTasks If true wait for other imports to finish, 
   * if false abort the import.
   * @param paths the paths to the corpora
   * @return True if all corpora where imported successfully.
   */
  public boolean importCorporaSave(boolean overwrite, 
    String aliasName,
    String statusEmailAdress, 
    boolean waitForOtherTasks, String... paths)
  {
    return importCorporaSave(overwrite, null, statusEmailAdress,
      waitForOtherTasks,
      Arrays.asList(paths));
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
    String database, String user, String password, boolean useSSL)
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
      writer.write("datasource.ssl=" + (useSSL ? "true" : "false") + "\n");
    }
    catch (IOException e)
    {
      log.error("Couldn't write database properties file", e);
      throw new FileAccessException(e);
    }
    finally
    {
      if (writer != null)
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

  public String getStatusMailSender()
  {
    return statusMailSender;
  }

  public void setStatusMailSender(String statusMailSender)
  {
    this.statusMailSender = statusMailSender;
  }

  public SchemeFixer getSchemeFixer()
  {
    return schemeFixer;
  }

  public void setSchemeFixer(SchemeFixer schemeFixer)
  {
    this.schemeFixer = schemeFixer;
  }
  
  
  
  
}
