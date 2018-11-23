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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;

import annis.AnnisRunnerException;
import annis.CommonHelper;
import annis.exceptions.AnnisException;
import annis.service.objects.ImportJob;
import annis.utils.ANNISFormatHelper;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CorpusAdministration
{

  private AdministrationDao administrationDao;
  private DeleteCorpusDao deleteCorpusDao;

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
          + id, 51);
      }
    }
    log.info("Deleting corpora: " + ids);
    deleteCorpusDao.deleteCorpora(ids, true);
    log.info("Finished deleting corpora: " + ids);
  }

  public void cleanupData()
  {
    administrationDao.cleanupData();
  }

  public void initializeDatabase(String host, String port, String database,
    String user, String password, String defaultDatabase, String superUser,
    String superPassword, boolean useSSL, String pgSchema)
  {

    log.info("initializing database");
    administrationDao.initializeDatabase(host, port, database, user, password,
      defaultDatabase, superUser, superPassword, useSSL, pgSchema);

    // write database information to property file
    writeDatabasePropertiesFile(host, port, database, user, password, useSSL,
      pgSchema);

    // create tables and other stuff that is handled by the scheme fixer
    if (schemeFixer != null)
    {
      schemeFixer.setDatabaseSchema(pgSchema);
      schemeFixer.checkAndFix();
    }
  }

  /**
   * Imports several corpora and catches a possible thrown
   * {@link DefaultAdministrationDao.ConflictingCorpusException} when the
   * overwrite flag is set to false.
   *
   *
   * @param overwrite If set to false, a conflicting corpus is not silently
   * reimported.
   * @param aliasName An common alias name for all imported corpora or null
   * @param statusEmailAdress an email adress for informating the admin about
   * statuses
   * @param waitForOtherTasks If true wait for other imports to finish, if false
   * abort the import.
   * @param paths Valid pathes to corpora.
   * @return True if all corpora where imported successfully.
   */
  public ImportStatus importCorporaSave(boolean overwrite,
    String aliasName,
    String statusEmailAdress, boolean waitForOtherTasks, List<String> paths)
  {

    // init the import stats. From the beginning everything is ok
    ImportStatus importStats = new ImportStatsImpl();
    importStats.setStatus(true);

    // check if database scheme is ok
    if (schemeFixer != null)
    {
      schemeFixer.checkAndFix();
    }

    List<File> roots = new LinkedList<>();
    for (String path : paths)
    {
      File f = new File(path);

      if (f.isFile())
      {
        // might be a ZIP-file
        try (ZipFile zip = new ZipFile(f);)
        {

          // get the names of all corpora included in the ZIP file
          // in order to get a folder name
          Map<String, ZipEntry> corpora = ANNISFormatHelper.corporaInZipfile(zip);

          // unzip and add all resulting corpora to import list
          log.info("Unzipping " + f.getPath());
          File outDir = createZIPOutputDir(Joiner.on(", ").
            join(corpora.keySet()));
          roots.addAll(unzipCorpus(outDir, zip));

        }
        catch (ZipException ex)
        {
          log.error(
            "" + f.getAbsolutePath()
            + " might not be a valid ZIP file and will be ignored",
            ex);
        }
        catch (IOException ex)
        {
          log.error(
            "IOException when importing file " + f.getAbsolutePath()
            + ", will be ignored",
            ex);
        }
      }
      else
      {
        try
        {
          roots.addAll(ANNISFormatHelper.corporaInDirectory(f).values());
        }
        catch (IOException ex)
        {
          log.error("Could not find any corpus in " + f.getPath(), ex);
          importStats.setStatus(false);
          importStats.addException(f.getAbsolutePath(), ex);
        }
      }
    } // end for each given path

    // import each corpus separately
    boolean anyCorpusImported = false;
    for (File r : roots)
    {
      try
      {
        log.info("Importing corpus from: " + r.getPath());
        if (administrationDao.importCorpus(r.getPath(), aliasName,
          overwrite, waitForOtherTasks))
        {
          log.info("Finished import from: " + r.getPath());
          sendImportStatusMail(statusEmailAdress, r.getPath(),
            ImportJob.Status.SUCCESS, null);
          anyCorpusImported = true;
        }
        else
        {
          importStats.setStatus(false);
          sendImportStatusMail(statusEmailAdress, r.getPath(), ImportJob.Status.ERROR,
            null);
        }
      }

      catch (AdministrationDao.ConflictingCorpusException ex)
      {
        importStats.setStatus(false);
        importStats.addException(r.getPath(), ex);
        log.error("Error on conflicting top level corpus name for {}", r.
          getPath());
        sendImportStatusMail(statusEmailAdress, r.getPath(), ImportJob.Status.ERROR,
          ex.
          getMessage());
      }

      catch (org.springframework.transaction.CannotCreateTransactionException ex)
      {
        importStats.setStatus(false);
        importStats.addException(r.getPath(), ex);
        log.error("Postgres is not running or misconfigured");
      }

      catch (Throwable ex)
      {
        importStats.setStatus(false);
        importStats.addException(r.getPath(), ex);
        log.error("Error on importing corpus", ex);
        sendImportStatusMail(statusEmailAdress, r.getPath(), ImportJob.Status.ERROR,
          ex.getMessage());
      }
    } // end for each corpus
    
    return importStats;
  }

  /**
   * Extract the zipped ANNIS corpus files to an output directory.
   *
   * @param outDir The ouput directory.
   * @param zip ZIP-file to extract.
   * @return A list of root directories where the tab-files are located if
   * found, null otherwise.
   */
  private List<File> unzipCorpus(File outDir, ZipFile zip)
  {
    List<File> rootDirs = new ArrayList<>();

    Enumeration<? extends ZipEntry> zipEnum = zip.entries();
    while (zipEnum.hasMoreElements())
    {
      ZipEntry e = zipEnum.nextElement();
      File outFile = new File(outDir, e.getName().replaceAll("\\/", "/"));

      if (e.isDirectory())
      {
        if (!outFile.mkdirs())
        {
          log.warn("Could not create output directory " + outFile.
            getAbsolutePath());
        }
      } // end if directory
      else
      {
        if ("corpus.tab".equals(outFile.getName()) || "corpus.annis".equals(
          outFile.getName()))
        {
          rootDirs.add(outFile.getParentFile());
        }

        if (!outFile.getParentFile().isDirectory())
        {
          if (!outFile.getParentFile().mkdirs())
          {
            {
              log.warn(
                "Could not create output directory for file " + outFile.
                getAbsolutePath());
            }
          }
        }
        try (FileOutputStream outStream = new FileOutputStream(outFile);)
        {

          ByteStreams.copy(zip.getInputStream(e), outStream);
        }
        catch (FileNotFoundException ex)
        {
          log.error(null, ex);
        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
      } // end else is file
    } // end for each entry in zip file

    return rootDirs;
  }

  public File createZIPOutputDir(String corpusName)
  {
    File outDir = new File(System.getProperty("user.home"),
      ".annis/zip-imports/"
      + CommonHelper.getSafeFileName(corpusName));
    if (outDir.exists())
    {
      try
      {
        // delete old data inside the corpus directory
        FileUtils.deleteDirectory(outDir);
      }
      catch (IOException ex)
      {
        log.warn("Could not recursivly delete the output directory", ex);
      }
    }
    if (!outDir.mkdirs())
    {
      throw new IllegalStateException("Could not create directory "
        + outDir.getAbsolutePath());
    }
    return outDir;
  }

  public static class ImportStatsImpl implements ImportStatus
  {

    boolean status = true;

    private final static String SEPERATOR = "--------------------------\n";

    final Map<String, List<Throwable>> exceptions;

    public ImportStatsImpl()
    {
      exceptions = new HashMap<>();
    }

    @Override
    public boolean getStatus()
    {
      return status;
    }

    @Override
    public List<Throwable> getThrowables()
    {
      List<Throwable> allThrowables = new ArrayList<>();

      for (List<Throwable> l : exceptions.values())
      {
        allThrowables.addAll(l);
      }

      return allThrowables;
    }

    @Override
    public List<Throwable> getThrowable(String corpusName)
    {
      return exceptions.get(corpusName);
    }

    @Override
    public void addException(String corpusName, Throwable ex)
    {
      if (!exceptions.containsKey(corpusName))
      {
        exceptions.put(corpusName, new ArrayList<Throwable>());
      }

      exceptions.get(corpusName).add(ex);
    }

    @Override
    public void setStatus(boolean status)
    {
      this.status = status;
    }

    @Override
    public void add(ImportStatus importStats)
    {
      if (importStats == null)
      {
        return;
      }

      status &= importStats.getStatus();
      exceptions.putAll(importStats.getAllThrowable());
    }

    @Override
    public List<Exception> getExceptions()
    {
      List<Exception> exs = new ArrayList<>();

      if (exceptions != null)
      {
        for (List<Throwable> throwables : exceptions.values())
        {
          for (Throwable throwable : throwables)
          {
            if (throwable instanceof Exception)
            {
              exs.add((Exception) throwable);
            }
          }
        }
      }

      return exs;
    }

    @Override
    public Map<String, List<Throwable>> getAllThrowable()
    {
      return this.exceptions;
    }

    @Override
    public String printMessages()
    {
      StringBuilder txtMessages = new StringBuilder();
      for (Entry<String, List<Throwable>> e : exceptions.entrySet())
      {
        txtMessages.append(SEPERATOR);
        txtMessages.append("Error in corpus: ").append(e.getKey()).append("\n");
        txtMessages.append(SEPERATOR);

        for (Throwable th : e.getValue())
        {
          Exception exception = (Exception) th;
          txtMessages.append(exception.getLocalizedMessage()).append("\n");
        }
      }

      return txtMessages.toString();
    }

    @Override
    public String printDetails()
    {
      StringBuilder details = new StringBuilder();
      for (Entry<String, List<Throwable>> e : exceptions.entrySet())
      {
        details.append(SEPERATOR);
        details.append("Error in corpus: ").append(e.getKey()).append("\n");
        details.append(SEPERATOR);

        for (Throwable th : e.getValue())
        {
          details.append(th.getLocalizedMessage()).append("\n");
          StackTraceElement[] st = th.getStackTrace();

          for (int i = 0; i < st.length; i++)
          {
            details.append(st[i].toString());
            details.append("\n");
          }
        }
      }

      return details.toString();
    }

    @Override
    public String printType()
    {
      StringBuilder type = new StringBuilder();

      for (Entry<String, List<Throwable>> e : exceptions.entrySet())
      {
        String name = e.getKey().split("/")[e.getKey().split("/").length - 1];
        type.append("(").append(name).append(": ");

        for (Throwable th : e.getValue())
        {
          type.append(th.getClass().getSimpleName()).append(" ");
        }

        type.append(") ");
      }

      return type.toString();
    }
  }

  public void sendImportStatusMail(String adress, String corpusPath,
    ImportJob.Status status, String additionalInfo)
  {
    if (adress == null || corpusPath == null)
    {
      return;
    }

    // check valid properties
    if (statusMailSender == null || statusMailSender.isEmpty())
    {
      log.warn("Could not send status mail because \"annis.mail-sender\" "
        + "property was not configured in conf/annis-service-properties.");
      return;
    }

    try
    {
      SimpleEmail mail = new SimpleEmail();
      List<InternetAddress> to = new LinkedList<>();
      to.add(new InternetAddress(adress));

      StringBuilder sbMsg = new StringBuilder();
      sbMsg.append("Dear Sir or Madam,\n");
      sbMsg.append("\n");
      sbMsg.append(
        "this is the requested status update to the ANNIS corpus import "
        + "you have started. Please note that this message is automated and "
        + "if you have any question regarding the import you have to ask the "
        + "administrator of the ANNIS instance directly.\n\n");

      mail.setTo(to);
      if (status == ImportJob.Status.SUCCESS)
      {
        mail.setSubject(
          "ANNIS import finished successfully (" + corpusPath + ")");
        sbMsg.append("Status:\nThe corpus \"").append(corpusPath)
          .append("\" was successfully imported and can be used from now on.\n");
      }
      else if (status == ImportJob.Status.ERROR)
      {
        mail.setSubject("ANNIS import *failed* (" + corpusPath + ")");
        sbMsg.append("Status:\nUnfortunally the corpus \"").append(corpusPath).
          append(
            "\" could not be imported successfully. "
            + "You may ask the administrator of the ANNIS installation for "
            + "assistance why the corpus import failed.\n");
      }
      else if (status == ImportJob.Status.RUNNING)
      {
        mail.setSubject("ANNIS import started (" + corpusPath + ")");
        sbMsg.append("Status:\nThe import of the corpus \"").append(corpusPath)
          .append("\" was started.\n");
      }
      else if (status == ImportJob.Status.WAITING)
      {
        mail.setSubject("ANNIS import was scheduled (" + corpusPath + ")");
        sbMsg.append("Status:\nThe import of the corpus \"").append(corpusPath)
          .append(
            "\" was scheduled and is currently waiting for other imports to "
            + "finish. As soon as the previous imports are finished this import "
            + "job will be executed.\n");
      }
      else
      {
        // we don't know how to handle this, just don't send a message
        return;
      }
      if (additionalInfo != null && !additionalInfo.isEmpty())
      {
        sbMsg.append("Addtional information:\n");
        sbMsg.append(additionalInfo).append("\n");
      }

      sbMsg.append("\n\nSincerely yours,\n\nthe ANNIS import service.");
      mail.setMsg(sbMsg.toString());
      mail.setHostName("localhost");
      mail.setFrom(statusMailSender);

      mail.send();
      log.info("Send status ({}) mail to {}.", new String[]
      {
        status.name(), adress
      });

    }
    catch (AddressException | EmailException ex)
    {
      log.warn("Could not send mail: " + ex.getMessage());
    }
  }
  
  public void sendCopyStatusMail(String adress, String origDBFile,
    ImportJob.Status status, String additionalInfo)
  {
    if (adress == null || origDBFile == null)
    {
      return;
    }

    // check valid properties
    if (statusMailSender == null || statusMailSender.isEmpty())
    {
      log.warn("Could not send status mail because \"annis.mail-sender\" "
        + "property was not configured in conf/annis-service-properties.");
      return;
    }

    try
    {
      SimpleEmail mail = new SimpleEmail();
      List<InternetAddress> to = new LinkedList<>();
      to.add(new InternetAddress(adress));

      StringBuilder sbMsg = new StringBuilder();
      sbMsg.append("Dear Sir or Madam,\n");
      sbMsg.append("\n");
      sbMsg.append(
        "this is the requested status update to the ANNIS corpus import "
        + "you have started. Please note that this message is automated and "
        + "if you have any question regarding the import you have to ask the "
        + "administrator of the ANNIS instance directly.\n\n");

      mail.setTo(to);
      if (status == ImportJob.Status.SUCCESS)
      {
        mail.setSubject(
          "ANNIS copy finished successfully (" + origDBFile + ")");
        sbMsg.append("Status:\nThe corpora from \"").append(origDBFile)
          .append("\" were successfully imported and can be used from now on.\n");
      }
      else if (status == ImportJob.Status.ERROR)
      {
        mail.setSubject("ANNIS copy *failed* (" + origDBFile + ")");
        sbMsg.append("Status:\nUnfortunally the corpora from \"").append(origDBFile).
          append(
            "\" could not be imported successfully. "
            + "You may ask the administrator of the ANNIS installation for "
            + "assistance why the corpus import failed.\n");
      }
      else if (status == ImportJob.Status.RUNNING)
      {
        mail.setSubject("ANNIS copy started (" + origDBFile + ")");
        sbMsg.append("Status:\nThe import of the corpora from \"").append(origDBFile)
          .append("\" was started.\n");
      }
      else if (status == ImportJob.Status.WAITING)
      {
        mail.setSubject("ANNIS copy was scheduled (" + origDBFile + ")");
        sbMsg.append("Status:\nThe import of the corpora from \"").append(origDBFile)
          .append(
            "\" was scheduled and is currently waiting for other imports to "
            + "finish. As soon as the previous imports are finished this copy "
            + "job will be executed.\n");
      }
      else
      {
        // we don't know how to handle this, just don't send a message
        return;
      }
      if (additionalInfo != null && !additionalInfo.isEmpty())
      {
        sbMsg.append("Addtional information:\n");
        sbMsg.append(additionalInfo).append("\n");
      }

      sbMsg.append("\n\nSincerely yours,\n\nthe ANNIS import service.");
      mail.setMsg(sbMsg.toString());
      mail.setHostName("localhost");
      mail.setFrom(statusMailSender);

      mail.send();
      log.info("Send status ({}) mail to {}.", new String[]
      {
        status.name(), adress
      });

    }
    catch (AddressException | EmailException ex)
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
   * @param waitForOtherTasks If true wait for other imports to finish, if false
   * abort the import.
   * @param paths the paths to the corpora
   * @return True if all corpora where imported successfully.
   */
  public ImportStatus importCorporaSave(boolean overwrite,
    String aliasName,
    String statusEmailAdress, boolean waitForOtherTasks, String... paths)
  {
    return importCorporaSave(overwrite, aliasName,
      statusEmailAdress, waitForOtherTasks,
      Arrays.asList(paths));
  }

  public List<Map<String, Object>> listCorpusStats()
  {
    return administrationDao.listCorpusStats();
  }

  public List<Map<String, Object>> listCorpusStats(File databaseProperties)
  {
    return administrationDao.listCorpusStats(databaseProperties);
  }

  public List<String> listUsedIndexes()
  {
    return administrationDao.listUsedIndexes();
  }

  public List<String> listUnusedIndexes()
  {
    return administrationDao.listUnusedIndexes();
  }

  public boolean copyFromOtherInstance(File dbProperties,
    boolean overwrite, String mail)
  {
    if (dbProperties.isFile() && dbProperties.canRead())
    {
      // find the corpus paths
      List<Map<String, Object>> corpora = listCorpusStats(
        dbProperties);
      List<String> corpusPaths = new LinkedList<>();
      for (Map<String, Object> c : corpora)
      {
        String sourcePath = (String) c.get("source_path");
        if (sourcePath != null)
        {
          corpusPaths.add(sourcePath);
        }
      }

      if (corpusPaths.isEmpty())
      {
        log.warn("No corpora found");
        return true;
      }
      else
      {
        
        log.info("The following corpora will be imported:\n"
          + "---------------\n"
          + "{}\n"
          + "---------------\n",
          Joiner.on("\n").join(corpusPaths));
        sendCopyStatusMail(mail, dbProperties.getAbsolutePath(), ImportJob.Status.RUNNING,
          "The following corpora will be imported:\n"
          + "---------------\n"
          + Joiner.on("\n").join(corpusPaths) + "\n"
          + "---------------\n");


        // remember the corpus alias table
        Multimap<String, String> corpusAlias = administrationDao.listCorpusAlias(
          dbProperties);

        //import each corpus
        ImportStatus status = importCorporaSave(
          overwrite, null,
          null,
          false,
          corpusPaths);


        // report the successful or failure failed
        Set<String> successfullCorpora = new LinkedHashSet<>(corpusPaths);
        Set<String> failedCorpora = new LinkedHashSet<>(
          status.getAllThrowable().keySet());
        successfullCorpora.removeAll(failedCorpora);

        log.info("copying corpus aliases");
        for(Map.Entry<String, String> e : corpusAlias.entries())
        {
          administrationDao.addCorpusAlias(e.getValue(), e.getKey());
        }

        if (failedCorpora.isEmpty())
        {
          log.info("All corpora imported without errors:\n"
            + "---------------\n"
            + "{}\n"
            + "---------------\n",
            Joiner.on("\n").join(successfullCorpora));
          sendCopyStatusMail(mail, dbProperties.getAbsolutePath(),
            ImportJob.Status.SUCCESS,
            "All corpora imported without errors:\n"
            + "---------------\n"
            + Joiner.on("\n").join(corpusPaths) + "\n"
            + "---------------\n");
          return true;
        }
        else
        {

          log.error(
            "Errors occured during import, not all corpora have been imported.\n"
              + "---------------\n"
              + "Success:\n"
              + "{}\n"
              + "---------------\n"
              + "Failed:\n"
              + "{}\n"
              + "---------------\n",
            Joiner.on("\n").join(successfullCorpora),
            Joiner.on("\n").join(failedCorpora));
          sendCopyStatusMail(mail, dbProperties.getAbsolutePath(), ImportJob.Status.ERROR,
            
            "Errors occured during import, not all corpora have been imported.\n"
              + "---------------\n"
              + "Success:\n"
              +  Joiner.on("\n").join(successfullCorpora) + "\n"
              + "---------------\n"
              + "Failed:\n"
              + Joiner.on("\n").join(failedCorpora) + "\n"
              + "---------------\n");
        }
      }
    }
    else
    {
      log.error("Can not read the database configuration file {}", dbProperties.
        getAbsolutePath());
    }
    return false;
  }
  
  public void dumpTable(String tableName, File outputFile)
  {
    log.info("Dumping table {} to file {}", tableName, outputFile);
    administrationDao.dumpTableToResource(tableName, new FileSystemResource(outputFile));
    if(!outputFile.exists())
    {
      try
      {
        // when a table is empty to output file is generated, still create an empty
        // file so the user knows something happend
        outputFile.createNewFile();
      }
      catch (IOException ex)
      {
        log.error("Could not create (empty) output file", ex);
      }
    }
  }
  
  public void restoreTable(String tableName, File inputFile)
  {
    log.info("Restoring table {} from file {}", tableName, inputFile);
    administrationDao.restoreTableFromResource(tableName, new FileSystemResource(inputFile));
  }

  ///// Helper
  protected void writeDatabasePropertiesFile(String host, String port,
    String database, String user, String password, boolean useSSL, String schema)
  {
    File file = new File(System.getProperty("annis.home") + "/conf",
      "database.properties");
    try (BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(
      file, "UTF-8"));)
    {

      writer.write("# database configuration\n");
      writer.write("datasource.driver=org.postgresql.Driver\n");
      writer.write("datasource.url=jdbc:postgresql://" + host + ":" + port + "/"
        + database + "\n");
      writer.write("datasource.username=" + user + "\n");
      writer.write("datasource.password=" + password + "\n");
      writer.write("datasource.ssl=" + (useSSL ? "true" : "false") + "\n");
      if (schema != null)
      {
        writer.write("datasource.schema=" + schema + "\n");
      }
    }
    catch (IOException e)
    {
      log.error("Couldn't write database properties file", e);
      throw new FileAccessException(e);
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

  public DeleteCorpusDao getDeleteCorpusDao()
  {
    return deleteCorpusDao;
  }

  public void setDeleteCorpusDao(DeleteCorpusDao deleteCorpusDao)
  {
    this.deleteCorpusDao = deleteCorpusDao;
  }
  
  

}
