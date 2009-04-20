package importer.paula.paula10.importer.paulaReader_1_0.reader;

import java.util.Vector;

import org.apache.log4j.Logger;

import importer.paula.paula10.importer.paulaReader_1_0.PAULAFileConnector;
import importer.paula.paula10.importer.paulaReader_1_0.PAULAMapperInterface;

import importer.paula.paula10.importer.util.graph.Edge;
import importer.paula.paula10.importer.util.graph.Graph;
import importer.paula.paula10.importer.util.graph.Node;
import importer.paula.paula10.importer.util.graph.TraversalObject;
import importer.paula.paula10.importer.util.graph.Graph.TRAVERSAL_MODE;

/**
 * This reader is the only wich is not derived from PAULAReader. This reader reads the
 * corpus structre by a given graph object. It also calls back the methods of 
 * PAULAMapperInterface. It just reads the beginning and the end of a corpus and
 * a document.
 * 
 * @author Florian Zipser
 *
 */
public class CorpStructDataReader implements TraversalObject
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"CorpStructDataReader";		//Name dieses Tools
	private static final String PAULA_VERSION=	"paula 1.0";		//untgerstützte PAULA-Version
	
	private static final String KW_PATH_SEP=	"/";	//corpus path seperator
	
	private static final String KW_NODE_NAME= "NODE_NAME";	//Name for attribute, wich contains the name of a node.
	private static final String KW_FILE_TYPE= "FILE_ANA_TYPE";	//Name for attribute, wich contains the type of analysis result
	//private static final String KW_FILE_DTD= "FILE_DTD";	//Name for attribute, wich contains the DTD of file
	private static final String KW_FILE_PATH= "FILE_PATH";	//Name for attribute, wich contains the path of file
	/**
	 * Name for attribute, wich contains the type information of a node.
	 */
	private static final String KW_NODE_TYPE= 	"NODE_TYPE";
	private static final String TYPE_CORP= 		"CORPUS";		//node type is corpus
	private static final String TYPE_DOC= 		"DOCUMENT";		//node type is document
	private static final String TYPE_FILE= 		"PAULA_FILE";	//node type is paula file
	
	/**
	 * An object wich would be called by call back.
	 */
	protected PAULAMapperInterface mapper= null;
	
	/**
	 * the connector for call back for invoking parsing paula files.
	 */
	protected PAULAFileConnector pCon= null;
	/**
	 * Logger for log4j.
	 */
	protected Logger logger= null;
	
	/**
	 * A List of corpus and document names wich represent a corpus path.
	 */
	protected Vector<String> corpusPath= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_MAPPER=			MSG_ERR + "Cannot create a '"+TOOLNAME+"'-object, because the given mapper is empty.";
	private static final String ERR_EMPTY_PCONNECTOR=		MSG_ERR + "Cannot create a '"+TOOLNAME+"'-object, because the given pCon (PAULAFileConnector) is empty.";
//	 ============================================== statische Methoden ==============================================
	/**
	 * Returns the supported PAULA version.
	 * @return
	 */
	public static String getPAULAVersion()
		{ return(PAULA_VERSION); }
	
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Creates a new reader object and sets the messages to the given logger.
	 * @param mapper PAULAMapperInterface - the object wich should be called by call back  
	 * @param logger Logger - a logger for log4j
	 */
	public CorpStructDataReader(	PAULAMapperInterface mapper,
									PAULAFileConnector pCon,
									Logger logger) throws Exception
	{
		if (mapper== null) throw new Exception(ERR_EMPTY_MAPPER);
		if (pCon== null) throw new Exception(ERR_EMPTY_PCONNECTOR);
		this.pCon= pCon;
		this.mapper= mapper;
		this.logger= logger;
		this.init();
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Initialize this object
	 */
	private void init()
	{
		this.corpusPath= new Vector<String>();
	}
	
	/**
	 * Returns the current corpus path as String. The path is seperated by KW_PATH_SEP.
	 */
	private String getCorpusPath()
	{
		String corpPath= "";
		for (String corp: this.corpusPath)
		{
			corpPath= corpPath + KW_PATH_SEP + corp;
		}
		return(corpPath);
	}
//	 ============================================== öffentl. Methoden ==============================================
	/**
	 * name of current document
	 */
	private String currDocName= null;
	/**
	 * This methode is called during depthFirst Traversal ob the corpus structure graph.
	 */
	public void nodeReached(	TRAVERSAL_MODE tMode, 
								Node currNode, 
								Edge edge,
								Node fromNode, 
								long order) throws Exception
	{
		//Knoten ist Korpusknoten
		if (((String)currNode.getAttValue(KW_NODE_TYPE)).equalsIgnoreCase(TYPE_CORP))
		{
			String corpName= (String) currNode.getAttValue(KW_NODE_NAME);
			this.mapper.startCorpusData(this.getCorpusPath(), corpName);
			this.corpusPath.add(corpName);
		}
		//Knoten ist Dokumentknoten
		else if (((String)currNode.getAttValue(KW_NODE_TYPE)).equalsIgnoreCase(TYPE_DOC))
		{
			String docName= (String) currNode.getAttValue(KW_NODE_NAME);
			this.mapper.startDocumentData(this.getCorpusPath(), docName);
			this.corpusPath.add(docName);
			this.currDocName= docName;
		}
		//Knoten ist Fileknoten
		else if (((String)currNode.getAttValue(KW_NODE_TYPE)).equalsIgnoreCase(TYPE_FILE))
		{
			//Name und Ort der Paula-Datei
			String pFile= (String) currNode.getAttValue(KW_FILE_PATH);
			//Typ der Paula-Datei
			String cType= (String) currNode.getAttValue(KW_FILE_TYPE);
			//this.pCon.paulaFileConnector(cType, pFile, this.getCorpusPath());
			this.pCon.paulaFileConnector(cType, pFile, this.currDocName);
		}
	}

	/**
	* This methode is called during depthFirst Traversal ob the corpus structure graph.
	*/
	public void nodeLeft(	TRAVERSAL_MODE tMode, 
							Node currNode, 
							Edge edge,
							Node fromNode, 
							long order) throws Exception
	{
		//Knoten ist Korpusknoten
		if (((String)currNode.getAttValue(KW_NODE_TYPE)).equalsIgnoreCase(TYPE_CORP))
		{
			String corpName= (String) currNode.getAttValue(KW_NODE_NAME);
			this.mapper.endCorpusData(this.getCorpusPath(), corpName);
			//letztes Element des KorpusPfades löschen
			this.corpusPath.remove(this.corpusPath.lastElement());
		}
		//Knoten ist Dokumentknoten
		else if (((String)currNode.getAttValue(KW_NODE_TYPE)).equalsIgnoreCase(TYPE_DOC))
		{
			String docName= (String) currNode.getAttValue(KW_NODE_NAME);
			this.mapper.endDocumentData(this.getCorpusPath(), docName);
			//letztes Element des KorpusPfades löschen
			this.corpusPath.remove(this.corpusPath.lastElement());
		}
		//Knoten ist Fileknoten
		else if (((String)currNode.getAttValue(KW_NODE_TYPE)).equalsIgnoreCase(TYPE_FILE))
		{
		}
	}
	
	/**
	 * Traverses the given graph by depth First and calls all methods of corpus- and
	 * document-objects which is given by PAULAMapperInterface.
	 * @param graph Graph - the graph wich have to be traversed
	 * @param rootNode Node - the root node, from wich the Traversal has to start 
	 */
	public void parse(Graph graph, Node rootNode) throws Exception
	{ 
		//starte das Traversieren
		graph.traverseGraph(Graph.TRAVERSAL_MODE.DEPTH_FIRST, rootNode, this);
	}
}
