package importer.paula.paula10.importer.paulaReader_1_0.util;
import java.io.File;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import importer.paula.paula10.importer.reader.AbstractReader;
import importer.paula.paula10.importer.util.graph.Graph;
import importer.paula.paula10.importer.util.graph.Node;

/**
 * Liest eine Korpus-Typ Datei vom Typ (typed_korp.dtd) aus und erzeugt daraus die 
 * entsprechenden PDDesc-Objekte. Die PDDesc-Objekte werden in der Reihenfolge 
 * in die Liste geschrieben, in der sie in der Datei stehen.
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class TypeFileReader extends AbstractReader 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PDDescReader";		//Name dieses Tools
	
	private static final String KW_YES=		"yes";		//Schlüsselwort für ja
	private static final String KW_NO=		"no";		//Schlüsselwort für nein

	private static final String KW_PATH_SEP=	"/";		//Seperetor between corpus in corpus path
	private static final String KW_NODE_NAME= "NODE_NAME";	//Name for attribute, wich contains the name of a node.
	private static final String KW_FILE_TYPE= "FILE_ANA_TYPE";	//Name for attribute, wich contains the type of analysis result
	private static final String KW_FILE_DTD= "FILE_DTD";	//Name for attribute, wich contains the DTD of file
	private static final String KW_FILE_PATH= "FILE_PATH";	//Name for attribute, wich contains the path of file
	/**
	 * Name for attribute, wich contains the type information of a node.
	 */
	private static final String KW_NODE_TYPE= 	"NODE_TYPE";
	private static final String TYPE_CORP= 		"CORPUS";		//node type is corpus
	private static final String TYPE_DOC= 		"DOCUMENT";		//node type is document
	private static final String TYPE_FILE= 		"PAULA_FILE";	//node type is paula file
	
	//Elementname und Attribute zum Elementknoten korpus
	private static final String TAG_CORP=			"corpus";	//erstes Tag dieses Dokumentes
	private static final String ATT_CORP_NAME=		"name";		//Nmae des Attributes Korpus.name
	
	//Elementname und Attribute zum Elementknoten korpus
	private static final String TAG_DOC=			"document";	//erstes Tag dieses Dokumentes
	private static final String ATT_DOC_NAME=		"name";		//Nmae des Attributes Korpus.name
	
	//Elementname und Attribute zum Elementknoten anno
	private static final String TAG_ANNO=			"anno";		//Name des Tags für den Bereich der Annotationsdaten

	//Elementname und Attribute zum Elementknoten anno
	private static final String TAG_META=			"meat";		//Name des Tags für den Bereich der Metadaten

	//Elementname und Attribute zum Elementknoten file
	private static final String TAG_FILE=			"file";				//einzelner Eintrag
	private static final String ATT_FILE_NAME=		"name";				//Name des Attributes name
	private static final String ATT_FILE_TYPE=	"analyze_type";		//Name des Attributes analyze_type
	private static final String ATT_FILE_IMP=		"import";			//Name des Attributes import
	private static final String ATT_FILE_PATH=		"path";				//Name des Attributes path
	private static final String ATT_FILE_DTD=		"dtd";				//Name des Attributes DTD
	
	protected Locator locator= null;
	
	/**
	 * A graph object wich qould be filled by parsing the type file.
	 */
	protected Graph graph= null;
	
	/**
	 * A stack wich has all already read corpus nodes. The last read corpus node is always
	 * on top.
	 */
	protected Stack<Node> corpNodeStack= null;
	
	/**
	 * A node objects wich points to the current document node.
	 */
	protected Node currDocNode= null;
	/**
	 * the first inserted corpus Node.
	 */
	protected Node rootNode= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_XML_ERR=		MSG_ERR + "There´s an error in the xml-file: ";
	private static final String MSG_XML_ERR_END=	" This error occurs in line, col: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_KORP_NAME=	MSG_ERR + "The Korpus has no name.";
	private static final String ERR_EMPTY_GRAPH=		MSG_ERR + "Cannot create a '"+TOOLNAME+"'-object, because of the given graph is empty.";
	private static final String ERR_FILE=				MSG_ERR + "There is an error in the read file: ";
	private static final String ERR_NO_CORP_NAME=		ERR_FILE + "There is an corpus element-node wich has no name.";
	private static final String ERR_NO_DOC_NAME=		ERR_FILE + "There is an document element-node wich has no name.";
	private static final String ERR_NO_CORP_NODE=		ERR_FILE + "There is no corpus node above the current document node: ";
	private static final String ERR_EMPTY_FILE_NAME=	ERR_FILE + "The name of the file is empty.";
	private static final String ERR_EMPTY_FILE_TYPE=	ERR_FILE + "The type of the file is empty: ";
	private static final String ERR_EMPTY_FILE_PATH=	ERR_FILE + "The path of the file is empty: ";
	private static final String ERR_EMPTY_FILE_DTD=		ERR_FILE + "The dtd of the file is empty: ";
	private static final String ERR_NO_DOC_NODE=		ERR_FILE + "There is no document node above the current file node: ";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Instanziiert ein PDFileReader-Objekt. Die PDDesc-Objekte werden während des Parsens 
	 * der XML-Datei in ide übergebene Liste geschrieben. Ist diese Liste nicht 
	 * Vorinitialisiert, wird ein Fehler geworfen.
	 * @param logger Logger - Logger für log4j
	 * @exception Fehler, wenn Liste nicht initeilisiert ist
	 */
	/*
	public TypeFileReader(Graph graph, Logger logger) throws Exception
	{
		super(logger);
		this.graph= new Graph(true, true, this.logger);
	}
	*/
	public TypeFileReader(Logger logger) throws Exception
	{
		super(logger);
		this.init();
	}
//	 ============================================== private Methoden ==============================================
	
	/**
	 * Initializes this object. 
	 */
	private void init() throws Exception
	{
		this.graph= new Graph(true, true, this.logger);
		this.corpNodeStack= new Stack<Node>();
	}
	
	/**
	 * Computes a corpus path from the corpus stack and returns it as String. The
	 * different corpus and document elements are seperated by KW_SEP. 
	 * @return the current corpus path, or empty String, if there is no corpus on stack
	 */
	private String getCorpPath() throws Exception
	{
		String corpPath= "";
		for (Node node: this.corpNodeStack)
		{
			corpPath= corpPath + (String)node.getAttValue(KW_NODE_NAME) + KW_PATH_SEP;
		}
		if (this.currDocNode!= null) corpPath= corpPath + this.currDocNode.getAttValue(KW_NODE_NAME) + KW_PATH_SEP;
		return(corpPath);
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Starts parsing of the given type file.
	 * @param xmlFile File - type file wich has to be parsed 
	 */
	public Graph parse(File xmlFile) throws Exception
	{ 
		//parse aktuelles Dokument
		this.setReader(this).parse(xmlFile.getCanonicalPath());
		return(this.graph);
	}
	
	/**
	 * Gives back the root node of the graph wich is returned by parse. 
	 * @return
	 */
	public Node getRoot()
		{ return(this.rootNode); }
	
	//-------------------------------------- SAX -Methoden -------------------------
	public void setDocumentLocator(Locator locator)
	{
		this.locator= locator;
	}
	
	public void startElement(	String uri,
					            String localName,
					            String qName,
					            Attributes attributes) throws SAXException
	{
		try
		{
			//Tag CORP gelesen
			if (qName.equalsIgnoreCase(TAG_CORP))
			{
				String corpName= "";
				for (int i= 0; i < attributes.getLength(); i++)
				{
					//Korpusnamen belegen
					if (attributes.getQName(i).equalsIgnoreCase(ATT_CORP_NAME))
						corpName= attributes.getValue(i);
				}
				if (corpName.equalsIgnoreCase("")) throw new Exception(ERR_NO_CORP_NAME);
				Node corpNode= new Node(this.getCorpPath() + corpName);
				//ersten Knoten als Wurzel markieren
				if (this.rootNode== null) this.rootNode= corpNode;
				//Typ hinzufügen
				corpNode.setAtt(KW_NODE_TYPE, TYPE_CORP);
				//Namen des Korpus hinzufügen
				corpNode.setAtt(KW_NODE_NAME, corpName);
				//Knoten in Graphen einfügen
				this.graph.addNode(corpNode);
				//Kante ziehen, wenn ein Knoten auf dem Stack
				Node parent= null;
				if (!this.corpNodeStack.empty()) 
					parent= this.corpNodeStack.peek();
				if (parent!= null)
					this.graph.createEdge(parent, corpNode);
				//Knoten in den Stack eintragen
				this.corpNodeStack.push(corpNode);
			}
			//Tag DOCUMENT gelesen
			else if (qName.equalsIgnoreCase(TAG_DOC))
			{
				String docName= "";
				for (int i= 0; i < attributes.getLength(); i++)
				{
					//Korpusnamen belegen
					if (attributes.getQName(i).equalsIgnoreCase(ATT_DOC_NAME))
						docName= attributes.getValue(i);
				}
				if (docName.equalsIgnoreCase("")) throw new Exception(ERR_NO_DOC_NAME);
				Node docNode= new Node(this.getCorpPath() + docName);
				//Typ hinzufügen
				docNode.setAtt(KW_NODE_TYPE, TYPE_DOC);
				//Namen des Korpus hinzufügen
				docNode.setAtt(KW_NODE_NAME, docName);
				//Knoten in Graphen einfügen
				this.graph.addNode(docNode);
				//Kante ziehen, wenn ein Knoten auf dem Stack
				Node parent= this.corpNodeStack.peek();
				if (parent== null) throw new Exception(ERR_NO_CORP_NODE + docName);
				this.graph.createEdge(parent, docNode);
				this.currDocNode= docNode;
			}
			//Tag ANNOS gelesen
			else if (qName.equalsIgnoreCase(TAG_ANNO))
			{
				
			}
			//Tag META gelesen
			else if (qName.equalsIgnoreCase(TAG_META))
			{
				
			}
			//Tag FILE gelesen
			else if (qName.equalsIgnoreCase(TAG_FILE))
			{
				String fileName= "";
				String fileType= "";
				String fileImp= "";
				String filePath= "";
				String fileDTD= "";
				for (int i= 0; i < attributes.getLength(); i++)
				{
					//Korpusnamen belegen
					if (attributes.getQName(i).equalsIgnoreCase(ATT_FILE_NAME))
						fileName= attributes.getValue(i);
					//Analysetyp belegen
					else if (attributes.getQName(i).equalsIgnoreCase(ATT_FILE_TYPE))
						fileType= attributes.getValue(i);
					//Importflag belegen
					else if (attributes.getQName(i).equalsIgnoreCase(ATT_FILE_IMP))
						fileImp= attributes.getValue(i);
					//Dateipfad belegen
					else if (attributes.getQName(i).equalsIgnoreCase(ATT_FILE_PATH))
						filePath= attributes.getValue(i);
					//DTD belegen
					else if (attributes.getQName(i).equalsIgnoreCase(ATT_FILE_DTD))
						fileDTD= attributes.getValue(i);
				}
				//Knoten erzeugen, wenn Datei eingelesen werden soll
				if (fileImp.equalsIgnoreCase(KW_YES))
				{
					//Fehler, wenn ein Datum leer
					if (fileName.equalsIgnoreCase("")) throw new Exception(ERR_EMPTY_FILE_NAME);
					if (fileType.equalsIgnoreCase("")) throw new Exception(ERR_EMPTY_FILE_TYPE+ fileName);
					if (filePath.equalsIgnoreCase("")) throw new Exception(ERR_EMPTY_FILE_PATH+ fileName);
					if (fileDTD.equalsIgnoreCase("")) throw new Exception(ERR_EMPTY_FILE_DTD+ fileName);
					//Dateiknoten erzeugen
					Node fileNode= new Node(this.getCorpPath() + fileName);
					//Typ hinzufügen
					fileNode.setAtt(KW_NODE_TYPE, TYPE_FILE);
					//Namen des Korpus hinzufügen
					fileNode.setAtt(KW_NODE_NAME, fileName);
					//Analysetyp des Knotens hinzufügen
					fileNode.setAtt(KW_FILE_TYPE, fileType);
					//Pfad des Knotens hinzufügen
					fileNode.setAtt(KW_FILE_PATH, filePath);
					//DTD des Knotens hinzufügen
					fileNode.setAtt(KW_FILE_DTD, fileDTD);
					//Knoten in Graph einfügen
					this.graph.addNode(fileNode);
					if (this.currDocNode== null) throw new Exception(ERR_NO_DOC_NODE + fileName);
					//Kante zum Dokumentknoten ziehen
					this.graph.createEdge(this.currDocNode, fileNode);
				}
			}
		}
		catch (Exception e)
		{ throw new SAXException(e.getMessage()); }
	}
	
	public void endElement(	String uri,
            				String localName,
            				String qName) throws SAXException
     {
		//Tag CORP gelesen
		if (qName.equalsIgnoreCase(TAG_CORP))
		{
			//Knoten vom Korpusstack löschen
			this.corpNodeStack.pop();
		}
		//Tag DOCUMENT gelesen
		else if (qName.equalsIgnoreCase(TAG_DOC))
		{
			this.currDocNode= null;
		}
	 }
	//-------------------------------------- Ende SAX -Methoden -------------------------
//	 ============================================== main Methode ==============================================	
}
