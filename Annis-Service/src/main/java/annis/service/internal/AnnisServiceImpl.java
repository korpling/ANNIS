package annis.service.internal;

import annis.WekaDaoHelper;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import annis.dao.AnnisDao;
import annis.dao.AnnotationGraphDaoHelper;
import annis.dao.Match;
import annis.exceptions.AnnisBinaryNotFoundException;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.externalFiles.ExternalFileMgr;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.service.AnnisService;
import annis.service.AnnisServiceException;
import annis.service.ifaces.AnnisAttributeSet;
import annis.service.ifaces.AnnisBinary;
import annis.service.ifaces.AnnisContingencyTable;
import annis.service.ifaces.AnnisCorpusSet;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import annis.service.objects.AnnisAttributeSetImpl;
import annis.service.objects.AnnisCorpusSetImpl;
import annis.service.objects.AnnisResultImpl;
import annis.service.objects.AnnisResultSetImpl;
import de.deutschdiachrondigital.dddquery.DddQueryMapper;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

// TODO: Exceptions aufräumen
// TODO: TestCase fehlt
public class AnnisServiceImpl implements AnnisService {
	private static final long serialVersionUID = 1970615866336637980L;

	private Logger log = Logger.getLogger(this.getClass());

	private DddQueryMapper dddQueryMapper;
	private DddQueryParser dddQueryParser;
	private AnnisDao annisDao;
  private WekaDaoHelper wekaDaoHelper;
	private ExternalFileMgr externalFileMgr;

	/**
	 * Log the successful initialization of this bean.
	 * 
	 * <p>
	 * XXX: This should be a private method annotated with <tt>@PostConstruct</tt>, but
	 * that doesn't seem to work.  As a work-around, the method is called
	 * by Spring as an init-method.
	 */
	public void sayHello() {
		// log a message after successful startup
		log.info("AnnisService loaded.");
	}
	
	public void ping() throws RemoteException {
		
	}
	
	private String translate(String annisQuery) {
		return dddQueryMapper.translate(annisQuery);
	}

	public int getCount(List<Long> corpusList, String annisQuery) throws RemoteException, AnnisQLSemanticsException {
		return annisDao.countMatches(corpusList, translate(annisQuery));
	}
	
	public AnnisResultSet getResultSet(List<Long> corpusList, String annisQuery, int limit, int offset, int contextLeft, int contextRight) 
	throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException {
		List<AnnotationGraph> annotationGraphs = annisDao.retrieveAnnotationGraph(corpusList, translate(annisQuery), offset, limit, contextLeft, contextRight);
		AnnisResultSetImpl annisResultSet = new AnnisResultSetImpl();
		for (AnnotationGraph annotationGraph : annotationGraphs) {
			annisResultSet.add(new AnnisResultImpl(annotationGraph));
		}
		return annisResultSet;
	}
	
	
	public AnnisResultSet getResultSet1(List<Long> corpusList, String annisQuery, int limit, int offset, int contextLeft, int contextRight) 
		throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException
	{
		List<Match> matches = annisDao.findMatches(corpusList, translate(annisQuery));
		List<Match> slice = new AnnotationGraphDaoHelper().slice(matches, offset, limit); // ugly
		List<AnnotationGraph> annotationGraphs = annisDao.retrieveAnnotationGraph(slice, contextLeft, contextRight);
		AnnisResultSetImpl annisResultSet = new AnnisResultSetImpl();
		for (AnnotationGraph annotationGraph : annotationGraphs) {
			annisResultSet.add(new AnnisResultImpl(annotationGraph));
		}
		return annisResultSet;
	}
	
	public AnnisCorpusSet getCorpusSet() throws RemoteException {
		return new AnnisCorpusSetImpl(annisDao.listCorpora());
	}

	public AnnisAttributeSet getNodeAttributeSet(List<Long> corpusList,	boolean fetchValues) throws RemoteException {
		return new AnnisAttributeSetImpl(annisDao.listNodeAnnotations(corpusList, fetchValues));
	}
	
	public String getPaula(Long textId) throws RemoteException {
		AnnotationGraph graph = annisDao.retrieveAnnotationGraph(textId);
		if (graph != null)
			return new AnnisResultImpl(graph).getPaula();
		throw new AnnisServiceException("no text found with id = " + textId);
	}
	
	public AnnisResult getAnnisResult(Long textId) throws RemoteException {
		AnnotationGraph graph = annisDao.retrieveAnnotationGraph(textId);
		if (graph != null)
			return new AnnisResultImpl(graph);
		throw new AnnisServiceException("no text found with id = " + textId);
	}

	public boolean isValidQuery(String annisQuery) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException {
		try {
			dddQueryParser.parse(translate(annisQuery));
			return true;
		} catch (AnnisException e) {
			return false;
		}
	}

	// TODO: test getBinary
	public AnnisBinary getBinary(Long id) throws AnnisBinaryNotFoundException {
		log.debug("Retrieving binary file with id = " + id);
		
		try {
			return externalFileMgr.getBinary(id);
		} catch (Exception e) {
			throw new AnnisBinaryNotFoundException(e.getMessage());
		}
	}

	// XXX: not implemented
	public AnnisContingencyTable getContingencyTable(List<Long> corpusList,
			String annisQL, Map<String, String> attributesMap, boolean desc,
			int limit, int offset) throws RemoteException,
			AnnisQLSemanticsException, AnnisQLSyntaxException,
			AnnisCorpusAccessException {
		throw new AnnisException("not implemented");
	}

	public List<Annotation> getMetadata(long corpusId) throws RemoteException, AnnisServiceException {
		return annisDao.listCorpusAnnotations(corpusId);
	}

  @Override
  public String getWeka(List<Long> corpusList, String annisQL) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException
  {
    StringBuilder out = new StringBuilder();
    List<Match> matches = annisDao.findMatches(corpusList, translate(annisQL));
		if ( ! matches.isEmpty() ) {
			List<AnnisNode> annotatedNodes = annisDao.annotateMatches(matches);
      out.append(wekaDaoHelper.exportAsWeka(annotatedNodes, matches));
      out.append("\n");
		} else
			out.append("(empty)\n");

    return out.toString();
  }
	
	///// Getter / Setter
	
	public ExternalFileMgr getExternalFileMgr() {
		return externalFileMgr;
	}

	public void setExternalFileMgr(ExternalFileMgr externalFileMgr) {
		this.externalFileMgr = externalFileMgr;
	}

	public AnnisDao getAnnisDao() {
		return annisDao;
	}

	public void setAnnisDao(AnnisDao annisDao) {
		this.annisDao = annisDao;
	}

	public DddQueryMapper getDddQueryMapper() {
		return dddQueryMapper;
	}

	public void setDddQueryMapper(DddQueryMapper dddQueryMapper) {
		this.dddQueryMapper = dddQueryMapper;
	}

	public DddQueryParser getDddQueryParser() {
		return dddQueryParser;
	}

	public void setDddQueryParser(DddQueryParser dddQueryParser) {
		this.dddQueryParser = dddQueryParser;
	}

  public WekaDaoHelper getWekaDaoHelper()
  {
    return wekaDaoHelper;
  }

  public void setWekaDaoHelper(WekaDaoHelper wekaDaoHelper)
  {
    this.wekaDaoHelper = wekaDaoHelper;
  }

  
}
