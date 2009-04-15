package annisservice;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.deutschdiachrondigital.dddquery.helper.AnnisQlTranslator;

import annisservice.exceptions.AnnisBinaryNotFoundException;
import annisservice.exceptions.AnnisCorpusAccessException;
import annisservice.exceptions.AnnisQLSemanticsException;
import annisservice.exceptions.AnnisQLSyntaxException;
import annisservice.exceptions.AnnisServiceException;
import annisservice.extFiles.ExternalFileMgr;
import annisservice.handler.GetCorpusHandler;
import annisservice.handler.GetCountHandler;
import annisservice.handler.GetNodeAttributeSetHandler;
import annisservice.handler.GetNodeAttributeSetWithValuesHandler;
import annisservice.handler.GetPaulaHandler;
import annisservice.handler.GetResultHandler;
import annisservice.handler.GetWekaHandler;
import annisservice.ifaces.AnnisAttributeSet;
import annisservice.ifaces.AnnisBinary;
import annisservice.ifaces.AnnisContingencyTable;
import annisservice.ifaces.AnnisCorpusSet;
import annisservice.ifaces.AnnisResultSet;

public class AnnisServiceImpl implements AnnisService {

	private Logger log = Logger.getLogger(this.getClass());

	// XXX: is this necessary?  the class is not transported by RMI, is it?
	private static final long serialVersionUID = 1970615866336637980L;
	
	// handlers for the methods exported by the AnnisService interface
	@Autowired private GetCorpusHandler getCorpusHandler;
	@Autowired private GetNodeAttributeSetHandler getNodeAttributeSetHandler;
	@Autowired private GetNodeAttributeSetWithValuesHandler getNodeAttributeSetWithValuesHandler;
	@Autowired private GetCountHandler getCountHandler;
	@Autowired private GetResultHandler getResultHandler;
	@Autowired private GetPaulaHandler getPaulaHandler;
	@Autowired private GetWekaHandler getWekaHandler;
	@Autowired private AnnisQlTranslator dddQueryMapper;
	
	// answers getBinary() queries
	@Autowired private ExternalFileMgr externalFileMgr;

	/**
	 * Log the successful initialization of this bean.
	 * 
	 * <p>
	 * XXX: This should be a private method annotated with <tt>@PostConstruct</tt>, but
	 * that doesn't seem to work.  As a work-around, the method is called
	 * by Spring as an init-method.
	 */
	@PostConstruct
	public void sayHello() {
		// log a message after successful startup
		log.info("AnnisService loaded.");
	}
	
	// XXX: what is this good for?
	public void ping() throws RemoteException {
		
	}

	public int getCount(List<Long> corpusList, String annisQuery) throws RemoteException, AnnisQLSemanticsException {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(getCountHandler.CORPUS_LIST, corpusList);
		args.put(getCountHandler.ANNIS_QUERY, annisQuery);
		return getCountHandler.handleRequest(args);
	}

	public boolean isValidQuery(String annisQL) throws RemoteException,
			AnnisQLSemanticsException, AnnisQLSyntaxException {
		
		dddQueryMapper.translate(annisQL);
		
		return true;
	}
	
	public List<List<String>> getWeka(List<Long> corpusList, String annisQuery)
	throws RemoteException, AnnisQLSemanticsException,
	AnnisQLSyntaxException, AnnisCorpusAccessException {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(getWekaHandler.CORPUS_LIST, corpusList);
		args.put(getWekaHandler.ANNIS_QUERY, annisQuery);
		return getWekaHandler.handleRequest(args);
		
	}

	public AnnisResultSet getResultSet(List<Long> corpusList, String annisQuery, int limit, int offset, int contextLeft, int contextRight) 
		throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException
	{
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(getResultHandler.CORPUS_LIST, corpusList);
		args.put(getResultHandler.ANNIS_QUERY, annisQuery);
		args.put(getResultHandler.CONTEXT_LEFT, contextLeft);
		args.put(getResultHandler.CONTEXT_RIGHT, contextRight);
		args.put(getResultHandler.LIMIT, limit);
		args.put(getResultHandler.OFFSET, offset);
		return getResultHandler.handleRequest(args);
	}
	
	@SuppressWarnings("all")	// shut up the warning about the cast to Object[]
	public AnnisCorpusSet getCorpusSet() throws RemoteException {
		return getCorpusHandler.handleRequest(null);
	}
	
	public AnnisAttributeSet getNodeAttributeSet(List<Long> corpusList,	boolean fetchValues) throws RemoteException {
		Map<String, Object> args = new HashMap<String, Object>();

		if (fetchValues) {
			GetNodeAttributeSetWithValuesHandler handler = getNodeAttributeSetWithValuesHandler;
			args.put(handler.CORPUS_LIST, corpusList);
			return handler.handleRequest(args);
		} else {
			GetNodeAttributeSetHandler handler = getNodeAttributeSetHandler;
			args.put(handler.CORPUS_LIST, corpusList);
			return handler.handleRequest(args);
		}
	}
	
	public String getPaula(Long textId) throws RemoteException {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(getPaulaHandler.TEXT_ID, textId);
		return getPaulaHandler.handleRequest(args);
	}

	public AnnisBinary getBinary(Long id) throws AnnisBinaryNotFoundException {
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
		throw new AnnisServiceException("not implemented");
	}

}
