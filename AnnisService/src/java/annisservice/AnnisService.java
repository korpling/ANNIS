package annisservice;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import annisservice.exceptions.AnnisBinaryNotFoundException;
import annisservice.exceptions.AnnisCorpusAccessException;
import annisservice.exceptions.AnnisQLSemanticsException;
import annisservice.exceptions.AnnisQLSyntaxException;
import annisservice.ifaces.AnnisAttributeSet;
import annisservice.ifaces.AnnisBinary;
import annisservice.ifaces.AnnisContingencyTable;
import annisservice.ifaces.AnnisCorpusSet;
import annisservice.ifaces.AnnisResultSet;

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
	
	/**
	 * Check this query if it is a valid AQL query.
	 * @param annisQL The query.
	 * @return true if valid, false if not (but mostly an exception will be thrown describing the error)
	 * @throws RemoteException
	 * @throws AnnisQLSemanticsException
	 * @throws AnnisQLSyntaxException
	 */
	public boolean isValidQuery(String annisQL) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException;
	
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
	public String getPaula(Long textId) throws RemoteException;
	
	/**
	 * Get an Annis Binary object identified by its id.
	 * 
	 * @param id
	 * @return AnnisBinary
	 */
	public AnnisBinary getBinary(Long id) throws RemoteException, AnnisBinaryNotFoundException;
	
	public List<List<String>> getWeka(List<Long> corpusList, String annisQl) throws RemoteException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException;
	
	/**
	 * 
	 * Ping remote Service. For internal purposes.
	 * 
	 * @throws RemoteException
	 */
	public void ping() throws RemoteException;
	
}