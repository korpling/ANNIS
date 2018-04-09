/*
 * Copyright 2014 SFB 632.
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
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annis.dao.AbstractDao;
import annis.dao.QueryDao;
import annis.service.objects.AnnisCorpus;

/**
 * Contains common functions used in the different adminstration DAOs
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public abstract class AbstractAdminstrationDao extends AbstractDao
{

  private final static Logger log = LoggerFactory.getLogger(
    AbstractAdminstrationDao.class);

  private String externalFilesPath;
  
  private QueryDao queryDao;


  protected File getRealDataDir()
  {
    File dataDir;
    if (getExternalFilesPath() == null || getExternalFilesPath().isEmpty())
    {
      // use the default directory
      dataDir = new File(System.getProperty("user.home"), ".annis/data/");
    }
    else
    {
      dataDir = new File(getExternalFilesPath());
    }
    return dataDir;
  }
  
  /**
   * Checks, if there already exists a top level corpus.
   *
   * @param topLevelCorpusName The name of the corpus, which is checked.
   * @return Is false, if the no top level coprpus exists.
   */
  protected boolean existConflictingTopLevelCorpus(String topLevelCorpusName)
  {
    List<AnnisCorpus> existing = queryDao.listCorpora(Arrays.asList(topLevelCorpusName));
    return !existing.isEmpty();
  }
  
  // tables in the staging area have their names prefixed with "_"
  protected String tableInStagingArea(String table)
  {
    return "_" + table;
  }

  public String getExternalFilesPath()
  {
    return externalFilesPath;
  }

  public void setExternalFilesPath(String externalFilesPath)
  {
    this.externalFilesPath = externalFilesPath;
  }

  
  public QueryDao getQueryDao()
  {
    return queryDao;
  }

  public void setQueryDao(QueryDao queryDao)
  {
    this.queryDao = queryDao;
  }
  

}
