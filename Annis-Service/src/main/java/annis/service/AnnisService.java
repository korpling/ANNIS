package annis.service;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import annis.exceptions.AnnisBinaryNotFoundException;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.model.Annotation;
import annis.service.ifaces.AnnisAttributeSet;
import annis.service.ifaces.AnnisBinary;
import annis.service.ifaces.AnnisContingencyTable;
import annis.service.ifaces.AnnisCorpusSet;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;

public interface AnnisService extends Remote {
	
	/**
	 * 
	 * @param corpusList A list of corpora (identified by their id) to run the query on.
	 * @param annisQL AnnisQL query to execute
	 * @return 
	 * @throws RemoteException
	 * @throws AnnisQLSemanticsException 
	 * @throws AnnisQLSyntaxException
	 * @throws AnnisCorpusAccessException
	 */
	public int getCount(List<Long> corpusList, String annisQL) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException;
	
	public AnnisContingencyTable getContingencyTable(List<Long> corpusList, String annisQL, Map<String, String> attributesMap, boolean desc, int limit, int offset) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException;
	
	/**
	 * 
	 * @return A set object that contains all available corpora.
	 * @throws RemoteException
	 */
	public AnnisCorpusSet getCorpusSet() throws RemoteException;
	
	/**
	 * 
	 * @param corpusList
	 * @param annisQL
	 * @param limit The maximum number of items of the returned result set.
	 * @param offset The first result to output. Offset 1 will output results from 2 to n result .
	 * @param contextLeft The number of token before the first match.
	 * @param contextRight The number of token before after the last match.
	 * @return
	 * @throws RemoteException
	 * @throws AnnisQLSemanticsException
	 * @throws AnnisQLSyntaxException
	 * @throws AnnisCorpusAccessException
	 */
	public AnnisResultSet getResultSet(List<Long> corpusList, String annisQL, int limit, int offset, int contextLeft, int contextRight) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException;

  /**
	 * Get result in WEKA format
	 * @param corpusList
	 * @param annisQL
	 * @return The WEKA result
	 * @throws RemoteException
	 * @throws AnnisQLSemanticsException
	 * @throws AnnisQLSyntaxException
	 * @throws AnnisCorpusAccessException
	 */
  public String getWeka(List<Long> corpusList, String annisQL) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException;

	/**
	 * 
	 * @param corpusList
	 * @param fetchValues Set to true if distinct values for the attributes should be retrieved as well. Attention: This may cause performance penalties.
	 * @return
	 * @throws RemoteException
	 */
	public AnnisAttributeSet getNodeAttributeSet(List<Long> corpusList, boolean fetchValues) throws RemoteException;
	
	/**
	 * 
	 * @param textId The id of the text to get the Paula XML from.
	 * @return
	 * @throws RemoteException
	 */
	@Deprecated
	public String getPaula(Long textId) throws RemoteException;
	
	/**
	 * 
	 * @param textId The id of the text to get the annotation graph from.
	 * @return
	 * @throws RemoteException
	 */
	public AnnisResult getAnnisResult(Long textId) throws RemoteException;
	
	/**
	 * Get an Annis Binary object identified by its id.
	 * 
	 * @param id
	 * @return AnnisBinary
	 */
	public AnnisBinary getBinary(Long id) throws RemoteException, AnnisBinaryNotFoundException;
	
	/**
	 * 
	 * Ping remote Service. For internal purposes.
	 * 
	 * @throws RemoteException
	 */
	public void ping() throws RemoteException;

  /**
   * Return true if this is a valid query or throw exception when invalid
   */
	public boolean isValidQuery(String annisQL) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException;

	public List<Annotation> getMetadata(long corpusId) throws RemoteException, AnnisServiceException;
	
}