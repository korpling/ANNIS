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
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.zip.ZipEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@Component
public class ImportWorker extends Thread
{

  private final static Logger log = LoggerFactory.getLogger(ImportWorker.class);

  private AdministrationDao adminDao;

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
        importSingleCorpus(currentJob);
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
        if (currentJob != null && event.getLevel().isGreaterOrEqual(Level.INFO))
        {
          currentJob.getMessages().add(event.getFormattedMessage());
        }
      }
    };
    ch.qos.logback.classic.Logger rootLogger = lc.getLogger(
      Logger.ROOT_LOGGER_NAME);
    rootLogger.addAppender(appender);
    appender.start();
  }

  private void importSingleCorpus(ImportJob job)
  {
    currentJob.setStatus(ImportJob.Status.RUNNING);
    
    // unzip
    File outDir = Files.createTempDir();
    outDir.deleteOnExit();

    File rootDir = null;
    
    List<File> createdFiles = new LinkedList<File>();
    List<File> createdDirs = new LinkedList<File>();
    
    Enumeration<? extends ZipEntry> zipEnum = job.getInZip().entries();
    while (zipEnum.hasMoreElements())
    {
      ZipEntry e = zipEnum.nextElement();
      File outFile = new File(outDir, e.getName().replaceAll("\\/", "/"));
      outFile.deleteOnExit();
      
      if(e.isDirectory())
      {
        if(!outFile.mkdirs())
        {
          log.warn("Could not create temporary directory " + outFile.getAbsolutePath());
        }
        createdDirs.add(0, outFile);
      } // end if directory
      else
      {
        if("corpus.tab".equals(outFile.getName()) || "corpus.relannis".equals(outFile.getName()))
        {
          rootDir = outFile.getParentFile();
        }

        FileOutputStream outStream = null;
        try
        {
          outStream = new FileOutputStream(outFile);
          ByteStreams.copy(job.getInZip().getInputStream(e), outStream);
          createdFiles.add(0, outFile);
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

    if(rootDir != null)
    {
      if(adminDao.importCorpus(rootDir.getAbsolutePath(), job.isOverwrite()))
      {
        currentJob.setStatus(ImportJob.Status.SUCCESS);
      }
      else
      {
        currentJob.setStatus(ImportJob.Status.ERROR);
      }
      finishedJobs.put(currentJob.getUuid(), currentJob);
    }
    
    // cleanup
    for(File f : createdFiles)
    {
      if(!f.delete())
      {
        log.warn("Could not delete temporary file " + f.getAbsolutePath());
      }
    }
    for(File f : createdDirs)
    {
      if(!f.delete())
      {
        log.warn("Could not delete temporary directory " + f.getAbsolutePath());
      }
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

  public AdministrationDao getAdminDao()
  {
    return adminDao;
  }

  public void setAdminDao(AdministrationDao adminDao)
  {
    this.adminDao = adminDao;
  }

  public ImportJob getCurrentJob()
  {
    return currentJob;
  }
  
  
}
