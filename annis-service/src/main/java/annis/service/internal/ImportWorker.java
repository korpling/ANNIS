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

import annis.administration.AdministrationDao;
import annis.utils.RelANNISHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Queues;
import com.google.common.io.Files;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.zip.ZipEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@Component
public class ImportWorker extends Thread
{
  private final static Logger log = LoggerFactory.getLogger(ImportWorker.class);
  
  @Autowired
  private AdministrationDao adminDao;
  private BlockingQueue<ImportJob> importQueue = Queues.newLinkedBlockingDeque();
  private ImportJob currentJob;
  private Cache<String, ImportJob> finishedJobs = CacheBuilder.newBuilder().maximumSize(100).build();
  
  final private String[] allTables = new String[] {
    "corpus", "corpus_annotation", "text", "node", "component", "rank",
    "node_annotation", "edge_annotation", "resolver_vis_map", "media_files",
    "example_queries"
  };

  public ImportWorker(AdministrationDao adminDao)
  {
    
    this.adminDao = adminDao;
  }

  @Override
  public void run()
  {
    while(isAlive())
    {
      try
      {
        currentJob =  importQueue.take();
        importSingleCorpus(currentJob);
      }
      catch (InterruptedException ex)
      {
        log.error(null, ex);
        break;
      }
    }
  }
  
  private void importSingleCorpus(ImportJob job)
  {
    // unzip
    File outDir = Files.createTempDir();
    for(String table : allTables)
    {
      ZipEntry entry = RelANNISHelper.getRelANNISEntry(job.getInZip(), table, "tab");
      if(entry != null)
      {
        
      }
    }
  }

  public BlockingQueue<ImportJob> getImportQueue()
  {
    return importQueue;
  }
  
  

  
}
