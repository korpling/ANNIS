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

import annis.service.ifaces.AnnisBinaryMetaData;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

import annis.WekaHelper;
import annis.dao.AnnisDao;
import annis.dao.AnnotatedMatch;
import annis.exceptions.AnnisBinaryNotFoundException;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.externalFiles.ExternalFileMgr;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.AnnisService;
import annis.service.AnnisServiceException;
import annis.service.ifaces.AnnisAttributeSet;
import annis.service.ifaces.AnnisBinary;
import annis.service.ifaces.AnnisCorpusSet;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import annis.service.objects.AnnisAttributeSetImpl;
import annis.service.objects.AnnisCorpusSetImpl;
import annis.service.objects.AnnisResultImpl;
import annis.service.objects.AnnisResultSetImpl;
import annis.sqlgen.AnnotateSqlGenerator.AnnotateQueryData;

// TODO: Exceptions aufräumen
// TODO: TestCase fehlt
public class AnnisServiceImpl implements AnnisService
{

  private static final long serialVersionUID = 1970615866336637980L;
  private Logger log = Logger.getLogger(this.getClass());
  private AnnisDao annisDao;
  private ExternalFileMgr externalFileMgr;
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


	private QueryData analyzeQuery(String annisQuery, List<Long> corpusList) {
		QueryData queryData = annisDao.parseAQL(annisQuery, corpusList);
		return queryData;
	}

  @Override
  public int getCount(List<Long> corpusList, String annisQuery) throws RemoteException, AnnisQLSemanticsException
  {
		QueryData queryData = analyzeQuery(annisQuery, corpusList);
		return annisDao.count(queryData);
  }

  @Override
  public AnnisResultSet getResultSet(List<Long> corpusList, String annisQuery, int limit, int offset, int contextLeft, int contextRight)
    throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException
  {
    contextLeft = Math.min(maxContext, contextLeft);
    contextRight = Math.min(maxContext, contextRight);

	QueryData queryData = analyzeQuery(annisQuery, corpusList);
	queryData.addExtension(new AnnotateQueryData(offset, limit, contextLeft, contextRight));
    List<AnnotationGraph> annotationGraphs = annisDao.annotate(queryData);
    AnnisResultSetImpl annisResultSet = new AnnisResultSetImpl();
    for(AnnotationGraph annotationGraph : annotationGraphs)
    {
      annisResultSet.add(new AnnisResultImpl(annotationGraph));
    }
    return annisResultSet;
  }

  @Override
  public AnnisCorpusSet getCorpusSet() throws RemoteException
  {
    return new AnnisCorpusSetImpl(annisDao.listCorpora());
  }

  @Override
  public AnnisAttributeSet getAttributeSet(List<Long> corpusList,
    boolean fetchValues, boolean onlyMostFrequentValues) throws RemoteException
  {
    return new AnnisAttributeSetImpl(annisDao.listAnnotations(corpusList,
      fetchValues, onlyMostFrequentValues));
  }

  @Override
  public String getPaula(Long textId) throws RemoteException
  {
    AnnotationGraph graph = annisDao.retrieveAnnotationGraph(textId);
    if(graph != null)
    {
      return new AnnisResultImpl(graph).getPaula();
    }
    throw new AnnisServiceException("no text found with id = " + textId);
  }

  @Override
  public AnnisResult getAnnisResult(Long textId) throws RemoteException
  {
    AnnotationGraph graph = annisDao.retrieveAnnotationGraph(textId);
    if(graph != null)
    {
      return new AnnisResultImpl(graph);
    }
    throw new AnnisServiceException("no text found with id = " + textId);
  }

  @Override
  public boolean isValidQuery(String annisQuery) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException
  {
    annisDao.parseAQL(annisQuery, null);
    return true;
  }

 
  public AnnisBinary getBinary(Long id) throws AnnisBinaryNotFoundException
  {
    log.debug("Retrieving binary file with id = " + id);

    try
    {
      return externalFileMgr.getBinary(id);
    }
    catch(Exception e)
    {
      throw new AnnisBinaryNotFoundException(e.getMessage());
    }
  }

  @Override
  public List<Annotation> getMetadata(long corpusId) throws RemoteException, AnnisServiceException
  {
    return annisDao.listCorpusAnnotations(corpusId);
  }

  @Override
  public List<ResolverEntry> getResolverEntries(SingleResolverRequest[] request)
    throws RemoteException
  {
    return annisDao.getResolverEntries(request);
  }


  @Override
  public String getWeka(List<Long> corpusList, String annisQL) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException
  {
	  QueryData queryData = analyzeQuery(annisQL, corpusList) ;
    List<AnnotatedMatch> matches = annisDao.matrix(queryData);
    if(matches.isEmpty())
    {
      return "(empty)";
    }
    else
    {
      return wekaHelper.exportAsArff(matches);
    }
  }

  ///// Getter / Setter
  public ExternalFileMgr getExternalFileMgr()
  {
    return externalFileMgr;
  }

  public void setExternalFileMgr(ExternalFileMgr externalFileMgr)
  {
    this.externalFileMgr = externalFileMgr;
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

  @Override
  public AnnisBinary getBinary(String corpusName, int offset, int length)
    throws RemoteException
  {
    return annisDao.getBinary(corpusName, offset, length);
  }

  @Override
  public AnnisBinaryMetaData getBinaryMeta(String corpusName)
  {
    return annisDao.getBinary(corpusName, 1, 1);    
  }

  @Override
  public AnnisBinary getBinary(long id) throws RemoteException,
    AnnisBinaryNotFoundException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
