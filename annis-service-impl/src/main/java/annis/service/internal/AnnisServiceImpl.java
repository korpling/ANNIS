/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service.internal;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import annis.WekaHelper;
import annis.dao.AnnisDao;
import annis.dao.AnnotatedMatch;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.ql.parser.QueryData;
import annis.service.AnnisService;

// TODO: Exceptions aufräumen
// TODO: TestCase fehlt
public class AnnisServiceImpl implements AnnisService
{

  private static final long serialVersionUID = 1970615866336637980L;
  private Logger log = Logger.getLogger(this.getClass());
  private static Logger queryLog = Logger.getLogger("QueryLog");
  private AnnisDao annisDao;
  private WekaHelper wekaHelper;
  private int maxContext = 10;

  /**
   * Log the successful initialization of this bean.
   *
   * <p>
   * XXX: This should be a private method annotated with <tt>@PostConstruct</tt>, but
   * that doesn't seem to work.  As a work-around, the method is called
   * by Spring as an init-method.
   */
  public void sayHello()
  {
    // log a message after successful startup
    log.info("AnnisService loaded.");
  }

  @Override
  public void ping() throws RemoteException
  {
  }

  private QueryData analyzeQuery(String annisQuery, List<Long> corpusList)
  {
    QueryData queryData = annisDao.parseAQL(annisQuery, corpusList);
    return queryData;
  }


  private void logQuery(String queryFunction, String annisQuery,
      List<Long> corpusList, long runtime)
  {
    logQuery(queryFunction, annisQuery, corpusList, runtime, null);
  }

  private void logQuery(String queryFunction, String annisQuery,
      List<Long> corpusList, long runtime, String options)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("function: ");
    sb.append(queryFunction);
    sb.append(", ");
    sb.append("query: ");
    sb.append(annisQuery);
    sb.append(", ");
    sb.append("corpus: ");
    sb.append(annisDao.mapCorpusIdsToNames(corpusList));
    sb.append(", ");
    sb.append("runtime: ");
    sb.append(runtime);
    sb.append(" ms");
    if (options != null && !options.isEmpty())
    {
      sb.append(", ");
      sb.append(options);
    }
    String message = sb.toString();
    queryLog.info(message);
  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  public WekaHelper getWekaHelper()
  {
    return wekaHelper;
  }

  public void setWekaHelper(WekaHelper wekaHelper)
  {
    this.wekaHelper = wekaHelper;
  }

  public int getMaxContext()
  {
    return maxContext;
  }

  public void setMaxContext(int maxContext)
  {
    this.maxContext = maxContext;
  }
}
