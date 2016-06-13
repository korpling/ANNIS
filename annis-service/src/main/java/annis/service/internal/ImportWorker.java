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

import annis.administration.CorpusAdministration;
import annis.administration.ImportStatus;
import annis.service.objects.ImportJob;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Queues;
import java.util.concurrent.BlockingQueue;
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

  private final BlockingQueue<ImportJob> importQueue = Queues.newLinkedBlockingDeque();

  private ImportJob currentJob;

  private final Cache<String, ImportJob> finishedJobs = CacheBuilder.newBuilder().
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


    Appender<ILoggingEvent> appender = new AppenderBase<ILoggingEvent>()
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

  private void importSingleCorpusFile(ImportJob job)
  {
    currentJob.setStatus(ImportJob.Status.RUNNING);
    corpusAdmin.sendImportStatusMail(currentJob.getStatusEmail(), 
          job.getCaption(), ImportJob.Status.RUNNING, null);
    
   
    boolean success = true;
    
    // do the actual import
    if(job.getImportRootDirectory() != null)
    {
      ImportStatus importStats = corpusAdmin.importCorporaSave(
        job.isOverwrite(), job.getAlias(), job.getStatusEmail(), true, job.getImportRootDirectory().getAbsolutePath());
    
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
