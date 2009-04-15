package paulaReader_1_0;

/**
 * This interface describes only methods for Communication between a PAULAConnector and
 * a Reader for the PAULA-structure. It describes a call back for parsing specific 
 * PAULA-Files.
 * @author Florian Zipser
 *
 */
public interface PAULAFileConnector 
{
	/**
	 * Invokes the reading of a paulaFile with the correct PAULAReader-object. T
	 * @param cType String - classification type of the paulaFile (computed by PAULAAnalyzer) 
	 * @param paulaFile String - name and path of the paula file
	 * @param corpusPath String - the current path of already read corpora and documents
	 * @throws Exception
	 */
	public void paulaFileConnector(	String cType, 
									String paulaFile, 
									String corpusPath) throws Exception;
}
