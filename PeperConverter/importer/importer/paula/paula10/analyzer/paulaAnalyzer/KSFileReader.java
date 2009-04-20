package importer.paula.paula10.analyzer.paulaAnalyzer;

import java.io.File;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

import importer.paula.paula10.analyzer.util.depGraph.Graph;
import importer.paula.paula10.analyzer.util.depGraph.Node;

/**
 * Diese Klasse liest aus einer korpus-structure Datei (im XML-Format), die Baumstruktur aus,
 * die der Korpus Struktur entspricht. Die dabei erzeugte Baumstruktur wird in ein Objekt
 * vom Typ util.Graph geschrieben und zurückgegeben.
 * @author Florian Zipser
 * @version 1.0
 */
public class KSFileReader extends DefaultHandler2 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"KSFileReader";		//Name dieses Tools
	
	protected static final String KW_ACONLIST= "ACONLIST";			//Schlüsselwort	für eine Liste von AnalyzeContainern (Korpusgraphstruktur)
	
	protected static final String KW_PATH_SEP= "/";
	//alles zum Tag korpus
	private static final String TAG_CORP=	"corpus";			//Name des Tags Korpus
	private static final String ATT_CORP_NAME=	"name";			//Name des Attributes kopus.name
	
	//alles zum Tag Dokument
	private static final String TAG_DOC=	"document";			//Name des Tags Korpus
	private static final String ATT_DOC_NAME=	"name";			//Name des Attributes kopus.name
	
	//alles zum Tag file
	private static final String TAG_FILE=	"file";			//Name des Tags file
	private static final String ATT_FILE_NAME=	"name";		//Name des Attributes file.name
	private static final String ATT_FILE_PATH=	"path";		//Name des Attributes file.path
	
	//Keywords um einen Knoten zu bestimmen
	private static final String KW_NODE_TYPE=		"NODE_TYPE";
	private static final String KW_CORP_NODE=		"CORP";
	private static final String KW_CORP_NAME=		"CORP_NAME";
	private static final String KW_DOC_NODE=		"DOC";
	private static final String KW_DOC_NAME=		"DOC_NAME";
	
	
	protected Logger logger= null;			//log4j-Logger
	protected Graph ksTree= null;			//Baum, der die Korpusstruktur enthält
	protected Stack<Node> nodeStack= null;	//aktueller Knoten im  Baum der Korpusstruktur ist immer oben
	protected File envPath= null;			//Verzeichnis des Umschlages
	/**
	 * the current corpus path in a list
	 */
	protected Vector<String> corpPath= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START_FCT=		MSG_STD + "start method: ";
	private static final String MSG_END_FCT=		MSG_STD + "end method: ";
	private static final String MSG_INIT=			MSG_STD + TOOLNAME + "-object is initialized";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_XML_EXCEPT=			MSG_ERR + "An xml error has occured in file: ";
	private static final String ERR_EMPTY_ATT=			MSG_ERR + "The attribut file.name or file.path is empty.";
	private static final String ERR_EMPTY_KSFILE=		MSG_ERR + "The given korpus structure file is empty.";
	private static final String ERR_KSFILE_NOT_EXISTS=	MSG_ERR + "The given korpus structure file does not exists: ";
	private static final String ERR_NO_CORP_NAME=		MSG_ERR + "The given corpus element node has no value for attribute name.";
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt den Namen zurück, unter dem die Liste von AnalyzeContainern in einem Knoten 
	 * zu finden ist.
	 * @return Keywort für den Attributnamen der ACON-List in einem Knoten
	 */
	public static String getKWACONLIST()
		{ return(KW_ACONLIST); }

//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt eine Instanz des KSFileReader. 
	 * @param logger Logger - Ein logger für die Nachrichtenausgabe (log4j)
	 */
	public KSFileReader(Logger logger)
	{
		this.corpPath= new Vector<String>();
		
		this.logger= logger;	
		if (this.logger != null) this.logger.info(MSG_INIT);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * returns the current corpus path as String
	 * @return the corpus path as string.
	 */
	private String getCorpPath()
	{
		String path= "";
		for (String corp: this.corpPath)
		{
			path= path + KW_PATH_SEP + corp;
		}
		return(path);
	}
//	 ============================================== öffentliche Methoden ==============================================
	
	/**
	 * Liest eine Korpusstruktur aus einer gegebenen Datei ein und macht daraus eine 
	 * Baumdarstellung, die zurückgegeben wird. Ein Knoten hat dabei den Namen eines
	 * Korpus und enthält für jede enthaltene Datei ein AnalyzeContainer-Objekt.
	 * @param ksFile File - XML-Datei mit der Korpusstruktur
	 * @return Baumstruktur der Korpusstruktue
	 */
	public Graph getKSGraph(File ksFile) throws Exception
	{
		if (ksFile == null) throw new Exception(ERR_EMPTY_KSFILE);
		if (!ksFile.exists()) throw new Exception(ERR_KSFILE_NOT_EXISTS + ksFile.getCanonicalPath());
		this.envPath= ksFile.getParentFile();
		
		if (this.logger!= null) this.logger.debug(MSG_START_FCT + "getKSGRaph()");
		this.ksTree= null;
		this.nodeStack= new Stack<Node>();
		try
        {
        	SAXParser parser;
            XMLReader reader;
            
            final SAXParserFactory factory= SAXParserFactory.newInstance();
            parser= factory.newSAXParser();
            reader= parser.getXMLReader();

            //contentHandler erzeugen und setzen
            reader.setContentHandler(this);
            reader.setDTDHandler(this);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes",true);
            
            //neuen Graph für die Baumdarstellung erzeugen
            //isDirected, isOrdered, log4j-Logger
            this.ksTree= new Graph(true, true, this.logger);
            
            //TODO Ausgabe lieber Trace
            if (this.logger!= null) this.logger.debug(MSG_STD + "starting with parsing korpus-structure file: " + ksFile.getCanonicalPath()); 
            
            reader.parse(ksFile.getCanonicalPath());
            
            if (this.logger!= null) this.logger.debug(MSG_STD + "ending with parsing korpus-structure file: " + ksFile.getCanonicalPath());
        }
	 	catch (SAXParseException e1)
	 		{ throw new Exception(ERR_XML_EXCEPT+ e1.getMessage()); }
	 	if (this.logger!= null) this.logger.debug(MSG_END_FCT + "getKSGRaph()");
	 	return(this.ksTree);
	}
	
// ---------------------------- SAX-Methoden ----------------------------
	
	/**
	 * Für jeden gelesenen Korpus-Knoten wird ein neuer Knoten im ksTRee erstellt, für
	 * jeden file-Knoten wird die Liste von ACONS des aktuellen Korpus-Knoten erweitert.
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(String, String, String, Attributes) 
	 */
	public void startElement(	String uri,
					            String localName,
					            String qName,
					            Attributes attributes) throws SAXException
	{
		try
		{
			//wenn Elementknoten Korpus ist
			if (qName.equalsIgnoreCase(TAG_CORP))
			{
				String corpName= "";
				//gehe alle Attribute durch
				for (int i= 0; i < attributes.getLength(); i++)
				{
					//Attribut korpus.name gefunden
					if (attributes.getQName(i).equalsIgnoreCase(ATT_CORP_NAME))
						corpName= attributes.getValue(i);
				}
				if (corpName.equalsIgnoreCase("")) 
					throw new Exception();
				String nodeName= this.getCorpPath() + KW_PATH_SEP + corpName;
				//erzeuge neuen Knoten 
				Node newNode= new Node(nodeName);
				this.corpPath.add(corpName);
				//aktuelle ACON-Liste erzeugen
				Vector<AnalyzeContainer> currACONList= new Vector<AnalyzeContainer>();
				//aktuelle ACON-List setzen
				newNode.setValue(KW_ACONLIST, currACONList);
				newNode.setValue(KW_NODE_TYPE, KW_CORP_NODE);
				newNode.setValue(KW_CORP_NAME, corpName);
				//füge Knoten in Graph ein
				this.ksTree.addNode(newNode);
				//erzeuge Kante in Graph, wenn Knoten nicht der erste
				if (!nodeStack.empty()) this.ksTree.createEdge(nodeStack.peek(), newNode);
				//setze neuen Knoten oben auf den Stack
				this.nodeStack.push(newNode);
			}
			//wenn Elementknoten Dokument ist
			else if (qName.equalsIgnoreCase(TAG_DOC))
			{
				String docName= "";
				for (int i= 0; i < attributes.getLength(); i++)
				{
					//Attribut korpus.name gefunden
					if (attributes.getQName(i).equalsIgnoreCase(ATT_DOC_NAME))
						docName= attributes.getValue(i);
				}
				if (docName.equalsIgnoreCase("")) 
					throw new Exception();
				String nodeName= this.getCorpPath() + KW_PATH_SEP + docName;
				//erzeuge neuen Knoten 
				Node newNode= new Node(nodeName);
				this.corpPath.add(docName);
				//aktuelle ACON-Liste erzeugen
				Vector<AnalyzeContainer> currACONList= new Vector<AnalyzeContainer>();
				//aktuelle ACON-List setzen
				newNode.setValue(KW_ACONLIST, currACONList);
				newNode.setValue(KW_NODE_TYPE, KW_DOC_NODE);
				newNode.setValue(KW_DOC_NAME, docName);
				//füge Knoten in Graph ein
				this.ksTree.addNode(newNode);
				//erzeuge Kante in Graph, wenn Knoten nicht der erste
				if (!nodeStack.empty()) this.ksTree.createEdge(nodeStack.peek(), newNode);
				//setze neuen Knoten oben auf den Stack
				this.nodeStack.push(newNode);
			/*
				//gehe alle Attribute durch
				for (int i= 0; i < attributes.getLength(); i++)
				{
					//Attribut korpus.name gefunden
					if (attributes.getQName(i).equalsIgnoreCase(ATT_DOC_NAME))
					{
						//erzeuge neuen Knoten 
						Node newNode= new Node(attributes.getValue(i));
						//aktuelle ACON-Liste erzeugen
						Vector<AnalyzeContainer> currACONList= new Vector<AnalyzeContainer>();
						//aktuelle ACON-List setzen
						newNode.setValue(KW_ACONLIST, currACONList);
						newNode.setValue(KW_NODE_TYPE, KW_DOC_NODE);
						//füge Knoten in Graph ein
						this.ksTree.addNode(newNode);
						//erzeuge Kante in Graph, wenn Knoten nicht der erste
						if (!nodeStack.empty()) this.ksTree.createEdge(nodeStack.peek(), newNode);
						//setze neuen Knoten oben auf den Stack
						this.nodeStack.push(newNode);
					}
				}
			*/
			}
			//wenn Elementknoten File ist
			else if (qName.equalsIgnoreCase(TAG_FILE))
			{
				String fileName= "";
				String filePath= "";
				//gehe alle Attribute durch
				for (int i= 0; i < attributes.getLength(); i++)
				{
					//Attribut file.name gefunden
					if (attributes.getQName(i).equalsIgnoreCase(ATT_FILE_NAME)) 
						fileName= attributes.getValue(i); 
					//Attribut file.path gefunden
					else if (attributes.getQName(i).equalsIgnoreCase(ATT_FILE_PATH))
						filePath= attributes.getValue(i);
				}
				if ((fileName.equalsIgnoreCase("")) || (filePath.equalsIgnoreCase("")))
					throw new Exception(ERR_EMPTY_ATT);
				//neuen ACon erzeugen 
				//old File paulaFile= new File(this.envPath.getCanonicalPath() + "\\" +filePath);
				File paulaFile= new File(filePath);
				AnalyzeContainer newACON= new AnalyzeContainer(paulaFile); 
				//und in aktuelle Liste einfügen
				Vector<AnalyzeContainer> currACONList= (Vector<AnalyzeContainer>)((Node)this.nodeStack.peek()).getValue(KW_ACONLIST);
				currACONList.add(newACON);
			}
		}
		catch (Exception e)
		{ 
			e.printStackTrace();
			if(this.logger != null) this.logger.error(e.getMessage());
			throw new SAXException(e.getMessage()); 
		}
	}
	
	/**
	 * Löscht den obersten Knoten vom Stack wenn Element korpus. Wenn Element gleich file
	 * wird die aktuelle Liste von AnalyzeContainern an den aktuellen korpusKnoten angehangen.
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(String, String, String)
	 */
	public void endElement(	String uri,
				            String localName,
				            String qName) throws SAXException
    {
		//wenn Elementknoten Korpus ist
		if ((qName.equalsIgnoreCase(TAG_CORP)) || ((qName.equalsIgnoreCase(TAG_DOC))))
		{
			this.nodeStack.pop();
			this.corpPath.remove(this.corpPath.lastElement());
		}
    }
}
