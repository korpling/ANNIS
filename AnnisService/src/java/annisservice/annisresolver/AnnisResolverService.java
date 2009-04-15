package annisservice.annisresolver;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AnnisResolverService extends Remote
{
	/**
	 * 
	 * These are the available Visualization types
	 *
	 */
	public enum VisualizationType {KWIC, TREE, PARTITURE, RST, MMAX, AUDIO, VIDEO, IMAGE, NONE}
	
	/**
	 * Returns a visualization type for a special corpus and a special annotation level.
	 * If the annotation is null or empty, the other method will be called.
	 * @param corpusId String - a global unique id for the special corpus
	 * @param annoLevel String - a special annotation level 
	 * @return the visualization type
	 * @throws RemoteException if there is no annotation type for corpusId and and annoLevel 
	 */
	public VisualizationType getVizualizationType(	Long corpusId, 
													String annoLevel,
													String annotation) throws RemoteException;
	
	/**
	 * This method returns a visualization type. The visualization type is taken
	 * from data base and is identified by unique identifier for corpus (corpus_ID) 
	 * and annotation level (annoLevel).
	 * @param corpusID Long - unique identifier for a corpus
	 * @param annoLevel String - Name of the annotation level
	 * @return visualization type
	 * @throws Exception
	 */
	public VisualizationType getVizualizationType(	Long corpusId, 
													String levelName) throws RemoteException;
	
	/**
	 * This method returns the name of the tool which should visualize the
	 * computed Viz-type for the given annotation level and corpus.
	 * @param corpusID Long - unique identifier for a corpus
	 * @param annoLevel String - Name of the annotation level
	 * @return name of tool for visualization
	 * @throws Exception
	 */
	public String getVizualizationTool(	Long corpusID, 
										String annoLevel) throws RemoteException;
	
	/**
	 * 
	 * Ping remote Service. For internal purposes.
	 * 
	 * @throws RemoteException
	 */
	public void ping() throws RemoteException;
	
}