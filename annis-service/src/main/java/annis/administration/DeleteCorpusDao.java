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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class DeleteCorpusDao extends AbstractAdminstrationDao
{
  
  private final static Logger log = LoggerFactory.getLogger(AdministrationDao.class);

  
  
  /**
   * Deletes a top level corpus, when it is already exists.
   * @param corpusName
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW,
    isolation = Isolation.READ_COMMITTED)
  public void checkAndRemoveTopLevelCorpus(String corpusName)
  {
    if (existConflictingTopLevelCorpus(corpusName))
    {
      log.info("delete conflicting corpus: {}", corpusName);
      List<String> corpusNames = new LinkedList<>();
      corpusNames.add(corpusName);
      deleteCorpora(getQueryDao().mapCorpusNamesToIds(corpusNames), false);
    }
  }
  
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW,
    isolation = Isolation.READ_COMMITTED)
  public void deleteCorpora(List<Long> ids, boolean acquireLock)
  {
    if (acquireLock && !lockRepositoryMetadataTable(false))
    {
      log.error("Another import is currently running");
      return;
    }
    
    if(ids == null || ids.isEmpty())
    {
      return;
    }

    File dataDir = getRealDataDir();

    for (long l : ids)
    {
      log.info("deleting external data files");

      List<String> filesToDelete = getJdbcTemplate().queryForList(
        "SELECT filename FROM media_files AS m, corpus AS top, corpus AS child\n"
        + "WHERE\n"
        + "  m.corpus_ref = child.id AND\n"
        + "  top.id = ? AND\n"
        + "  child.pre >= top.pre AND child.post <= top.post", String.class, l);
      for (String fileName : filesToDelete)
      {
        File f = new File(dataDir, fileName);
        if (f.exists())
        {
          if (!f.delete())
          {
            log.warn("Could not delete {}", f.getAbsolutePath());
          }
        }
      }

      log.info("dropping tables");

      log.debug("dropping facts table for corpus " + l);
      getJdbcTemplate().execute("DROP TABLE IF EXISTS facts_" + l);
      getJdbcTemplate().execute("DROP TABLE IF EXISTS facts_edge_" + l);
      getJdbcTemplate().execute("DROP TABLE IF EXISTS facts_node_" + l);
      log.debug("dropping annotation_pool table for corpus " + l);
      getJdbcTemplate().execute("DROP TABLE IF EXISTS annotation_pool_" + l);
      log.debug("dropping annotations table for corpus " + l);
      getJdbcTemplate().execute("DROP TABLE IF EXISTS annotations_" + l);
    }

    log.info("recursivly deleting corpora: " + ids);

    executeSqlFromScript("delete_corpus.sql", makeArgs().addValue(":ids",
      StringUtils.join(ids, ", ")));
  }

}
