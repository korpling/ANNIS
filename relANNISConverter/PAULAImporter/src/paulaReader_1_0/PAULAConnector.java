package paulaReader_1_0;

import java.io.File;

import org.apache.log4j.Logger;

import paulaReader_1_0.reader.CorpStructDataReader;
import paulaReader_1_0.reader.PAULAReader;
import paulaReader_1_0.util.PAULAReaderMgr;
import paulaReader_1_0.util.TypeFileReader;
import util.graph.Graph;
import util.graph.Node;


/**
 * The class PAULAConnector is the entry point to discover a paula corpus structure. It
 * explores subcorpora, documents and paula files in the given corpus. The given corpus
 * have to be analyzed be PAULAAnalyzer, so it has to contain an analyze file.<br/>
 * The PAULAConnector delegates the paula files to the specific paula-file-reader-objects.
 * All, the specific paula-file-reader and the PAULAConnector starts an call-back on the
 * methods of PAULAMapperInterface. So all events in dependence with a corpus like
 * creating a corpus, subcorpus, document, primary data, token data etc. will invoke the
 * methods of the object which implements the PAULAMapperInterface.<br/>
 * All files and corpus structure have to fulfill the PAULA 1.0 standard.  
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public class PAULAConnector implements PAULAFileConnector
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PAULAConnector";		//Name dieses Tools
	private static final String PAULA_VERSION=	"paula 1.0";		//untgerstützte PAULA-Version
	
	/**
	 * Name of the typed corpus file, which contains all informations about the corpus
	 * structure and the type of all paula documents.
	 */
	private static final String FILE_TYPED_KORP=	"typed_corp.xml";				//default name der Korpusstrukturdatei

	
	/**
	 * The paula corpus wich should be read.
	 */
	protected File paulaCorpus= null;
	/**
	 * Object which implements the PAULAMapperInterface, the mapper would be invoked
	 * by call back.
	 */
	protected PAULAMapperInterface mapper= null;
	/**
	 * Logger for log4j
	 */
	protected Logger logger= null;
	/**
	 * The first inserted corpus graph in the corpus structure graph.
	 */
	protected Node rootNode= null;
	
	/**
	 * The Manager-object, which manages all usable PAULAReaders.
	 */
	protected PAULAReaderMgr paulaReaderMgr= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_CORP=		MSG_ERR + "Cannot create '"+TOOLNAME+"' object, because the given 'paulaCorpus' is empty.";
	private static final String ERR_CORP_NO_DIR=	MSG_ERR + "Cannot create '"+TOOLNAME+"' object, because the given 'paulaCorpus' isn´t a directory: ";
	private static final String ERR_NO_MAPPER=		MSG_ERR + "Cannot create '"+TOOLNAME+"' object, because the given 'mapper' is empty.";
	private static final String ERR_CORP_NOT_EXIST=	MSG_ERR + "Cannot create '"+TOOLNAME+"' object, because the given 'paulaCorpus' does not exist: ";
	private static final String ERR_NO_TYPE_FILE=			MSG_ERR + "There is no type file for korpus in the following folder. You have to analyze the korpus first by PAULAAnalyzer. Untyped korpus: ";
	private static final String ERR_TYPE_NOT_SUPPORTED=		MSG_ERR + "Sorry the given analyze type is not yet supported in "+ TOOLNAME+ ". Analyze type: ";
	private static final String ERR_EMPTY_CTYPE=			MSG_ERR + "Cannot search for PAULAReader, because the given classification type is empty.";
	private static final String ERR_EMPTY_PFILE=			MSG_ERR + "Cannot search for PAULAReader, because the given paula file type is empty.";
	private static final String ERR_EMPTY_CPATH=			MSG_ERR + "Cannot search for PAULAReader, because the given corpus path is empty.";
//	 ============================================== statische Methoden ==============================================
	/**
	 * Returns the supported PAULA version.
	 * @return the supported PAULA version
	 */
	public static String getPAULAVersion()
		{ return(PAULA_VERSION); }
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Creates an object of type PAULAConnector. The PAULAConnector reads a paula corpus
	 * and calls the methods of the PAULAMapperInterface
	 */
	public PAULAConnector(	File paulaCorpus, 
							PAULAMapperInterface mapper,
							Logger logger) throws Exception
	{
		if (paulaCorpus== null) throw new Exception(ERR_NO_CORP);
		//wenn Datei nicht existiert
		if (!paulaCorpus.exists()) throw new Exception(ERR_CORP_NOT_EXIST + paulaCorpus.getCanonicalPath());
		//prüft ob der PAULACorpus ein Verzeichniss ist
		if (!paulaCorpus.isDirectory()) throw new Exception(ERR_CORP_NO_DIR + paulaCorpus.getCanonicalPath());
		if (mapper== null) throw new Exception(ERR_NO_MAPPER);
		
		//Paula Korpus setzen
		this.paulaCorpus= paulaCorpus;
		
		//Mapper setzen
		this.mapper= mapper;
		
		//logger setzen
		this.logger= logger;
	}

//	 ============================================== private Methoden ==============================================
	/**
	 * Searches for the typed corpus file and reads it.
	 * @param corpusPath File - the corpus as directory wich has to be parsed
	 * @return a graph which contains the structure of the corpus
	 */
	private Graph readCorpFile(File corpusPath) throws Exception
	{
		//Korpus-Typ-Datei erzeugen
		File typedFile= new File(corpusPath.getCanonicalPath() + "/" + FILE_TYPED_KORP);
		// es existiert keine typ-Datei des Korpus
		if (!typedFile.exists()) throw new Exception(ERR_NO_TYPE_FILE + corpusPath.getCanonicalPath());
		
		TypeFileReader typeFileReader= new TypeFileReader(this.logger);
		Graph graph= typeFileReader.parse(typedFile);
		this.rootNode= typeFileReader.getRoot();
		return(graph);
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Starts reading the paula corpus and invokes methods of the mapper wich implements
	 * PAULAMapperInterface.
	 */
	public void startReading() throws Exception
	{
		//PAULAReader-Manager erstellen
		this.paulaReaderMgr= new PAULAReaderMgr(this.logger);
		//Graph für die Kopusstruktur erstellen
		Graph graph= this.readCorpFile(this.paulaCorpus);
		//lese getypte Datei aus
		CorpStructDataReader cSReader= new CorpStructDataReader(this.mapper, this, this.logger);
		//getypte Datei parsen
		cSReader.parse(graph, this.rootNode);
		//KOrpusstrukturgraph ausgeben
		graph.prinToDot("readedGraph");
	}
	
	/**
	 * Invokes the reading of a paulaFile with the correct PAULAReader-object. This correct
	 * object is searched before by the Connector. 
	 * @param cType String - classification type of the paulaFile (computed by PAULAAnalyzer) 
	 * @param paulaFile String - name and path of the paula file
	 * @param corpusPath String - the current path of already read corpora and documents
	 * @throws Exception
	 */
	public void paulaFileConnector(	String cType, 
									String paulaFile, 
									String corpusPath) throws Exception
	{
		if ((cType== null) || (cType.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_CTYPE);
		if ((paulaFile== null) || (paulaFile.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_PFILE);
		if ((corpusPath== null) || (corpusPath.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_CPATH);
		
		File pFile= new File(paulaFile);
		PAULAReader pReader= this.paulaReaderMgr.getPAULAReader(cType, pFile, corpusPath, this.mapper);
		//prüfe ob ein Reader gefunden wurde
		if (pReader== null)
			throw new Exception(ERR_TYPE_NOT_SUPPORTED + cType);
		if (this.logger!= null) this.logger.debug(MSG_STD+ " parsing paula document: " 
				+ pFile.getCanonicalPath()
				+" with specific paula reader: " 
				+ pReader.getReaderName() + " v" +pReader.getReaderVersion());
		pReader.parse(pFile);
	}
	
	public String toString()
	{
		String retStr= "";
		retStr= MSG_STD +" supports version " + getPAULAVersion();
		return(retStr);
	}
}
