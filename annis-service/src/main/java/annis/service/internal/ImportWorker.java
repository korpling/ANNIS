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
package annis.service.internal;

import annis.service.objects.ImportJob;
import annis.administration.AdministrationDao;
import annis.administration.CorpusAdministration;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Queues;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Component
public class ImportWorker extends Thread
{

  private final static Logger log = LoggerFactory.getLogger(ImportWorker.class);

  private CorpusAdministration corpusAdmin;

  private BlockingQueue<ImportJob> importQueue = Queues.newLinkedBlockingDeque();

  private ImportJob currentJob;

  private Cache<String, ImportJob> finishedJobs = CacheBuilder.newBuilder().
    maximumSize(100).build();

  public ImportWorker()
  {
    addAppender();
  }

  @Override
  public void run()
  {
    while (isAlive())
    {
      try
      {
        currentJob = importQueue.take();
        importSingleCorpusFile(currentJob);
      }
      catch (InterruptedException ex)
      {
        log.error(null, ex);
        break;
      }
    }
  }

  private void addAppender()
  {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(lc);


    Appender appender = new AppenderBase<ILoggingEvent>()
    {
      @Override
      protected void append(ILoggingEvent event)
      {
        if (currentJob != null 
          && event.getLevel().isGreaterOrEqual(Level.INFO)
          && event.getLoggerName().equals("annis.administration.AdministrationDao"))
        {
          currentJob.getMessages().add(event.toString());
        }
      }
    };
    ch.qos.logback.classic.Logger rootLogger = lc.getLogger(
      Logger.ROOT_LOGGER_NAME);
    rootLogger.addAppender(appender);
    appender.start();
  }

  /**
   * Extract the zipped relANNIS corpus files to an output directory.
   * 
   * @param outDir The ouput directory.
   * @param zip ZIP-file to extract.
   * @return A list of root directories where the tab-files are located if found, null otherwise.
   */
  private List<File> unzipCorpus(File outDir, ZipFile zip)
  {
    List<File> rootDirs = new ArrayList<File>();

    Enumeration<? extends ZipEntry> zipEnum = zip.entries();
    while (zipEnum.hasMoreElements())
    {
      ZipEntry e = zipEnum.nextElement();
      File outFile = new File(outDir, e.getName().replaceAll("\\/", "/"));
      outFile.deleteOnExit();

      if (e.isDirectory())
      {
        if (!outFile.mkdirs())
        {
          log.warn("Could not create temporary directory " + outFile.
            getAbsolutePath());
        }
      } // end if directory
      else
      {
        if ("corpus.tab".equals(outFile.getName()) || "corpus.relannis".equals(
          outFile.getName()))
        {
          rootDirs.add(outFile.getParentFile());
        }

        FileOutputStream outStream = null;
        try
        {
          outStream = new FileOutputStream(outFile);
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
        finally
        {
          if (outStream != null)
          {
            try
            {
              outStream.close();
            }
            catch (IOException ex)
            {
              log.error(null, ex);
            }
          }
        }
      } // end else is file
    } // end for each entry in zip file

    return rootDirs;
  }

  private void importSingleCorpusFile(ImportJob job)
  {
    currentJob.setStatus(ImportJob.Status.RUNNING);
    corpusAdmin.sendStatusMail(currentJob.getStatusEmail(), 
          job.getCaption(), ImportJob.Status.RUNNING, null);
    
    
    File outDir = new File(System.getProperty("user.home"), ".annis/zip-imports/"
      + getSafeDirName(job.getCaption()));
    if(outDir.exists())
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

    // unzip
    List<File> rootDirectories = unzipCorpus(outDir, job.getInZip());

    boolean success = true;
    
    for(File rootDir : rootDirectories)
    {
      // do the actual import
      AdministrationDao.ImportStats importStats = corpusAdmin.importCorporaSave(
        job.isOverwrite(), job.getAlias(), job.getStatusEmail(), true, rootDir.getAbsolutePath());
      if (!importStats.getStatus())
      {
        success = false; 
      }
    }
    if(success)
    {
      currentJob.setStatus(ImportJob.Status.SUCCESS);
    }
    else
    {
      currentJob.setStatus(ImportJob.Status.ERROR);
    }
    finishedJobs.put(currentJob.getUuid(), currentJob);
  }
  
  /**
   * Returns a directory name for an import job that is safe to use as a file name.
   * @param job
   * @return 
   */
  private String getSafeDirName(String jobName)
  {
    if(jobName != null)
    {
      return jobName.replaceAll("[^0-9A-Za-z.-]", "_");
    }
    else
    {
      return UUID.randomUUID().toString();
    }
  }

  public ImportJob getFinishedJob(String uuid)
  {
    ImportJob job = finishedJobs.getIfPresent(uuid);
    finishedJobs.invalidate(uuid);
    return job;
  }

  public BlockingQueue<ImportJob> getImportQueue()
  {
    return importQueue;
  }

  public ImportJob getCurrentJob()
  {
    return currentJob;
  }

  public CorpusAdministration getCorpusAdmin()
  {
    return corpusAdmin;
  }

  public void setCorpusAdmin(CorpusAdministration corpusAdmin)
  {
    this.corpusAdmin = corpusAdmin;
  }
  
}
