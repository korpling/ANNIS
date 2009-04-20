package importer.paula.paula10.importer.mapperV1;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;


import org.apache.log4j.Logger;

import de.corpling.salt.SaltFactory;
import de.corpling.salt.SaltGraph;
import de.corpling.salt.SaltProject;
import de.corpling.salt.SaltConcrete.SCorpDocRelation;
import de.corpling.salt.SaltConcrete.SCorpus;
import de.corpling.salt.SaltConcrete.SCorpusRelation;
import de.corpling.salt.SaltConcrete.SDocument;
import de.corpling.salt.SaltConcrete.SDominanceRelation;
import de.corpling.salt.SaltConcrete.SPointingRelation;
import de.corpling.salt.SaltConcrete.SSpanRelation;
import de.corpling.salt.SaltConcrete.SStructure;
import de.corpling.salt.SaltConcrete.STextualDataSource;
import de.corpling.salt.SaltConcrete.STextualRelation;
import de.corpling.salt.SaltConcrete.SToken;
import de.corpling.salt.SaltConcrete.SaltConcreteFactory;
import de.corpling.salt.model.ModelFactory;
import de.corpling.salt.model.SAnnotation;
import de.corpling.salt.model.SElement;
import de.corpling.salt.model.SProcessingAnnotation;
import de.corpling.salt.model.SRelation;

import importer.paula.paula10.importer.mapper.AbstractMapper;
import importer.paula.paula10.importer.paulaReader_1_0.*;
import importer.paula.paula10.importer.paulaReader_1_0.reader.PAULAReader;
import importer.paula.paula10.util.xPointer.XPtrInterpreter;
import importer.paula.paula10.util.xPointer.XPtrRef;

/**
 * Die Klasse MapperV1 bildet das Quelldatenmodell PAULA 1.0 auf das Zieldatenmodell 
 * relANNIS 2.0 ab. Zum Einlesen der Quelldaten wird das Package paulaReader_1_0 verwendet.
 * Der Mapper MapperV1 bildet diese Daten dann auf die Klassen des Packages relANNIS_2_0
 * ab, die ihrerseits das Interne KorpusModel bilden und die Abbildung der Daten auf das
 * relANNIS 2.0 Datenmodell erstellen. 
 * <br/><br/>
 * 
 * Dieser Prozess wird ï¿½ber die Methode Mapper.map() angestoï¿½en.
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public class MapperV1 extends AbstractMapper implements PAULAMapperInterface
{

//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"MapperV1";		//Name dieses Tools
	private static final String VERSION= 	"1.0";			//Version dieses Tools
	
//	private static final boolean MODE_SE_NEW= true;
	
	//Pfad und Dateiname fï¿½r Settingfiles
	private static final String FILE_TYPED_KORP=	"typed_corp.xml";				//default name der Korpusstrukturdatei
//	private static final boolean DEBUG=			false;				//DEBUG-Schalter
//	private static final boolean DEBUG_TOK_DATA= false;				//spezieller DEBUG-Schalter fï¿½r TokData
	private static final boolean DEBUG_SE=		false;				//spezieller DEBUG-Schalter fï¿½r StructEdge
	private static final boolean DEBUG_COMPLEX_ANNO_DATA=	false;	//spezieller DEBUG-Schalter fï¿½r ComplexAnnoData
//	private static final boolean DEBUG_COLLECTION_DN=	false;		//spezieller DEBUG-Schalter fï¿½r CollectionDN
	private static final boolean DEBUG_METASTRUCT_DATA= false;		//spezieller DEBUG-Schalter fï¿½r MetaStructData
	private static final boolean DEBUG_METAANNO_DATA= false;		//spezieller DEBUG-Schalter fï¿½r MetaAnnoData
//	private static final boolean DEBUG_KSDESC=	false;				//spezieller DEBUG-Schalter fï¿½r das Berechnen des Korpuspfades 
//	private static final boolean DEBUG_STRUCT=	false;				//spezieller DEBUG-Schalter fï¿½r den StructData-Connector
	private static final boolean DEBUG_ANNO_DATA=	false;			//spezieller DEBUG-Schalter fï¿½r den AnnoData-Connector
	private static final boolean DEBUG_POINTING_REL_DATA=	false;	//spezieller DEBUG-Schalter fï¿½r den ponting-relation-Connector
//	private static final boolean DEBUG_MULTI_FEAT_DATA=		false;	//spezieller DEBUG-Schalter fï¿½r den multiFeatDataConnector
//	private static final boolean DEBUG_AUDIO_DATA=			true;	//spezieller DEBUG-Schalter fï¿½r den audioDataConnector
	
	//Schlï¿½sselworte fï¿½r Readertypen
//	private static final String KW_CTYPE_METASTRUCTDATA=	"MetaStructData";	//MetaAnnotationsstruktur (anno.xml)
//	private static final String KW_CTYPE_METAANNODATA=		"MetaAnnoData";		//Metaannotationen (Dateien, die sich auf anno.xml beziehen)
//	private static final String KW_CTYPE_PRIMDATA=			"PrimData";			//Primï¿½rdaten
//	private static final String KW_CTYPE_TOKDATA=			"TokData";			//Tokendaten
//	private static final String KW_CTYPE_STRUCTDATA=		"StructData";		//Strukturdaten
//	private static final String KW_CTYPE_STRUCTEDGEDATA=	"StructEdgeData";	//Kanten-Strukturdaten
//	private static final String KW_CTYPE_ANNODATA=			"AnnoData";			//Annotationsdaten
	
	//Standardwerte
//	private static final long STD_ANNO_NAME_EXT=	0;			//Standardwert fï¿½r die Annotationsnnamenerweiterung
//	private static final long STD_CAD_NAME_EXT=		0;			//Standardwert fï¿½r die ComplexAnnotationsnnamenerweiterung
//	private static final long STD_COLANNO_NAME_EXT=	0;			//Standardwert fï¿½r die CollectionAnnotationsnamenerweiterung
//	private static final long STD_PR_NAME_EXT=		0;			//Standardwert fï¿½r die pointing-relation-Namenerweiterung
	
//	private static final String KW_STRUCTEDGE_TYPE_ATT=	"EDGE_TYPE";	//Name unter dem das Attribut rel.type im relANNIS Modell als Annotation gefï¿½hrt werden soll 
	
	//Schlï¿½sselworte
	private static final String KW_NAME_SEP=	"#";		//Seperator fï¿½r Knotennamen (Knotentyp#Knotenname)
//	private static final String KW_PATH_SEP=	"/";		//Seperaor fï¿½r Korpuspfade	
//	private static final String KW_TYPE_DNDOC=	"doc";		//Knotentypname fï¿½r Dokumentknoten
//	private static final String KW_TYPE_DNCOL=	"col";		//Knotentypname fï¿½r Collectionknoten
	
	//passt hier nicht so gut hin
//	private static final String KW_ANNO_VALUE=	"value";			//Schlï¿½sselwort unter dem das PAULA-Attribut Value als Annotation gespeichert werden soll
//	private static final String KW_ANNO_TAR=	"target";			//Schlï¿½sselwort unter dem das PAULA-Attribut Target als Annotation gespeichert werden soll
//	private static final String KW_ANNO_DESC=	"description";		//Schlï¿½sselwort unter dem das PAULA-Attribut Target als Annotation gespeichert werden soll
//	private static final String KW_ANNO_EXP=	"example";			//Schlï¿½sselwort unter dem das PAULA-Attribut Target als Annotation gespeichert werden soll
//	private static final String KW_REL_TYPE_NAME= "RELATION_TYPE";	//Schlï¿½sselwort unter der die Annotation fï¿½r relStructDN gespeichert
//	private static final String KW_TYPE_FILE=	"FILE";				//Schlï¿½sselwort unter fï¿½r den Typ "Datei" einer Collection				
	
	//einige Statistik-Counter
//	private long SC_SDN= 0;						//Statistik-Counter fï¿½r StructDN
//	private long SC_SDN_REF_EDGE= 0;			//Statistik-Counter fï¿½r Kanten vone einem StructDN zu den Referenzknoten
//	private long SC_SEDN= 0;					//Statistik-Counter fï¿½r StructEdgeDN
	private long SC_SEDN_REF_EDGE= 0;			//Statistik-Counter fï¿½r Kanten vone einem StructEdgeDN zu den Referenzknoten
	
	//private CorpusGraphMgr kGraphMgr= null;			//interner Korpusgraph, in den die Knoten eingefï¿½gt werden
	
//	private Long annoNameExt= STD_ANNO_NAME_EXT;			//Namenszusatz fï¿½r Annotationsknoten, da diese meist keine ID besitzen
//	private Long cadNameExt=  STD_CAD_NAME_EXT;				//Namenszusatz fï¿½r ComplexAnnotationsknoten, da diese meist keine ID besitzen
//	private Long pointingRelNameExt= STD_PR_NAME_EXT;		//Namenszusatz fï¿½r Pointing relations, da diese meist keine ID besitzen
//	private Long colAnnoNameExt= STD_COLANNO_NAME_EXT;		//Namenszusatz fï¿½r CollectionAnnotationsknoten, da diese meist keine ID besitzen
	//private CollectionDN currFileColDN= null;				//aktueller Collectionknoten, der ein PAULA-Dokument darstellt
	/**
	 * folder in wich the mapper can write some information like the documents as graph
	 */
//	private File infoFolder= null;
	
	/**
	 * gibt an, ob der Graph STï¿½ck fï¿½r Stï¿½ck als Dot-Files ausgegeben werden soll
	 */
//	private boolean toDot= false;
	// ----------------------------- StructEdge -----------------------------
	/**
	 * Diese Tabelle speichert alle StructEdge-Objekte, die von der Methode 
	 * structEdgeDataConnector() empfangen werden. Die eintzelnen Attribue werden in
	 * einem TmpStruceEdgeDN zwischengespeichert. Eine Liste mehrerer solcher Objekte wird
	 * dann einem paula::const-Elementknoten zugeordnet.
	 * Diese Tabelle hï¿½lt besitzt eine Reihenfolge und merkt sich in welcher Abfolge
	 * die Elemente eingefï¿½gt wurden.
	 */
	Map<String, Vector<TmpStructEdgeDN>> tmpSETable = null;
	Collection<String> tmpStructIDList= null;
	Collection<String> seenStructIDs= null;
	
	
	/**
	 * Zuordnung von Collectionnamen nach der PAULA-Notation und Collectionnamen des Korpusgraphen.
	 * Tabelle: PAULA-Namen : Korpusgraphnamen
	 */
//	private Hashtable<String, String> colNamingTable= null;	
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_OK=					"OK";
//	private static final String MSG_CREATE_INTGRAPH=	"create internal graph model...";
//	private static final String MSG_READ_PFILES=		"reading paula files..."; 
//	private static final String MSG_CREATE_KOPRPP=		"creating korpus pre- and post-order...";
//	private static final String MSG_PREPARE_GRAPH=		"preparing internal korpus graph for inserting nodes...";
//	private static final String MSG_DOT_WRITING=		"writing korpus graph to dot file...";
//	private static final String MSG_CLOSING_GRAPH=		"closing korpus graph...";
	//	 *************************************** Fehlermeldungen ***************************************
//	private static final String ERR_NOT_IMPLEMENTED=		MSG_ERR + "This methode is not yet been implemented.";
//	private static final String ERR_NO_PDFILE=				MSG_ERR + "The given pdFile does not exist. This might be an internal error. Not existing file: ";
	private static final String ERR_NO_TYPE_FILE=			MSG_ERR + "There is no type file for korpus in the following folder. You have to analyze the korpus first by PAULAAnalyzer. Untyped korpus: ";
//	private static final String ERR_TYPE_NOT_SUPPORTED=		MSG_ERR + "Sorry the given analyze type is not yet supported in "+ TOOLNAME+ " v" +VERSION+". Analyze type: ";
	private static final String ERR_XPTR_NOT_A_TEXT=		MSG_ERR + "An XPointer of the parsed document does not refer to a xml-textelement. Incorrect pointer: ";
	private static final String ERR_TOO_MANY_REFS=			MSG_ERR + "There are too many references for a token node element: ";
	private static final String ERR_WRONG_LEFT_RIGHT=		MSG_ERR + "The left or right border is not set correctly of XPointer: ";
	private static final String ERR_NO_PRIMDN=				MSG_ERR + "No primary data node found for token element: ";
	private static final String ERR_WRONG_REF_KIND_ELEM=	MSG_ERR + "The XPointer references in current file are incorrect. There only have to be element pointers and the following is not one of them: ";
//	private static final String ERR_CANNOT_FIND_REFNODE=	MSG_ERR + "Connot find a node with the following name: ";
//	private static final String ERR_CANNOT_FIND_REFEDGE=	MSG_ERR + "Connot find an edge with the following name: ";
//	private static final String ERR_NODENAME_NOT_IN_GRAPH=	MSG_ERR + "The given rel-Node does not exist in Graph.";
//	private static final String ERR_CYCLE_IN_SE_DOC=		MSG_ERR + "Cannot import the data from document, because there is a cycle in it. Cycle list: ";
	private static final String ERR_FCT_DEPRECATED=			MSG_ERR + "This method isnï¿½t supported, it is deprecated.";
//	private static final String ERR_CAD_NO_SRC=				MSG_ERR + "There is no source Href given in methode: ";
//	private static final String ERR_META_STRUCT_FILE=		MSG_ERR + "This corpus contains two meta-struct-data files (anno.xml).";
//	private static final String ERR_METASTRUCT_FILE=		MSG_ERR + "There is an error in the mta-struct-document. One link can reference only one Element or a sequence of elements: ";
//	private static final String ERR_ID_NOT_IN_NTABLE=		MSG_ERR + "The given reference cannot be explored, thereï¿½s an error in document: ";
//	private static final String ERR_XPTR_NO_ELEMENT=		MSG_ERR + "The given reference is not an element or an element-range pointer: ";
//	private static final String ERR_NO_RELS=				MSG_ERR + "Thereï¿½s an error in parsed document. The following struct node has no rel-node: ";
//	private static final String ERR_STRUCTID_NOT_EXIST=		MSG_ERR + "Thereï¿½s an error in parsed document. The following struct-id wich is referenced does not exists: ";
//	private static final String ERR_NULL_STRUCTEDGE=		MSG_ERR + "The searched edge does not exist in internal table: ";
	//	 ============================================== statische Methoden ==============================================
	
	
	//	 ============================================== Konstruktoren ==============================================

	Logger logger= Logger.getLogger(MapperV1.class);
	/**
	 * Initialisiert ein Mapper Objekt und setzt den logger zur Nachrichtenausgabe.
	 * 
	 */
	public MapperV1() throws Exception
	{
		super(Logger.getLogger(MapperV1.class));
	}
		
	/**
	 * the current project in which shall be imported 
	 */
	private SaltProject saltProject= null;
	public void setSaltProject(SaltProject saltProject)
	{
		if (saltProject== null)
			throw new NullPointerException(MSG_ERR + "Cannot import into an empty saltproject.");
		this.saltProject= saltProject;
	}
	public SaltProject getSalProject()
	{
		return(this.saltProject);
	}
	
	/**
	 * Current saltGraph in which this importer imports actually.
	 */
	protected SaltGraph saltGraph= null;

	
	/**
	 * Entry point for importing
	 * @param srcFolder
	 * @param saltModel
	 */
	public void map(File srcFolder)
	{
		try
		{
//			if (this.importReceiver== null)
//				throw new NullPointerException(MSG_ERR+ "Cannot start mapping, because the import receiver isn´t set.");
			if (!srcFolder.exists())
				throw new NullPointerException(MSG_ERR+ "The given source folder dosn´t exists: "+ srcFolder);
			if (!srcFolder.isDirectory())
				throw new NullPointerException(MSG_ERR+ "The given source folder isn´t a folder: "+ srcFolder);
			
			//Korpus-Typ-Datei erzeugen
			File typedFile= new File(srcFolder.getCanonicalPath() + "/" + FILE_TYPED_KORP);
			// es existiert keine typ-Datei des Korpus
			if (!typedFile.exists()) throw new Exception(ERR_NO_TYPE_FILE + srcFolder.getCanonicalPath());
			
			//Corpusgraph im SaltModel erstelln
			if (this.saltProject== null)
				throw new NullPointerException(MSG_ERR + "Cannot start importing, please set current saltProject first.");
			this.saltGraph= SaltFactory.eINSTANCE.createSaltGraph();
			this.saltProject.addSGraph(this.saltGraph);
			

			
			this.srcFolder= srcFolder;
			
		} catch (Exception e)
		{ e.printStackTrace(); throw new RuntimeException(MSG_ERR+ "An error occurs: "+e.getMessage());}
	}
	
	private File srcFolder= null; 
	
	private PAULAConnector pConnector= null;
	public void importCorpusStructure() throws Exception
	{
		//PAULAConnector erstellen
		if (this.logger!= null) this.logger.info("initializing all useble connector and reader objects");
		this.pConnector= new PAULAConnector(srcFolder, this, this.logger);
		if (this.logger!= null) this.logger.info(MSG_OK);
		
		//das Mappen starten
		if (this.logger!= null) this.logger.info("start reading");
		this.pConnector.startReading();
		if (this.logger!= null) this.logger.info(MSG_OK);
	}
	
	private SDocument currSDocument= null;
	
	/**
	 * global naming table for all elements stores paulaId of one element and corresponding salt id
	 */
	private Map<String, String> elementNamingTable= null;
	
	/**
	 * stores paula-document-names and corresponding paula-elements in readed order
	 * importent for interpreting xpointer (ranges)
	 */
	private Map<String, Collection<String>> elementOrderTable= null;
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void importDocument(SDocument sDocument) 
	{
		if (this.logger!= null) this.logger.info("start reading document '"+sDocument.getId()+"'");
		this.currSDocument= sDocument;
		
		//init naming table
		this.elementNamingTable= new Hashtable<String, String>();
		//init element-order table
		this.elementOrderTable= new Hashtable<String, Collection<String>>();
		
		try {
			this.pConnector.readDocumentContent(sDocument.getId().toString());
		} catch (Exception e) 
		{
			e.printStackTrace();
			throw new NullPointerException(e.getMessage());
		}
		if (this.logger!= null) this.logger.info(MSG_OK);
	}
	
// ------------------------------ Methoden der Middleware ------------------------------
	
	// ------------------------------ Methoden aus dem PAULAMapperInterface------------------------------
	/**
	 * stores the path element of parent corpus
	 */
	protected Stack<String> parentCorpusPath= new Stack<String>();
	/**
	 * in this version, there can be only one root
	 */
	protected String rootCorpusPath= null;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein neuer Korpus bzw. Subkorpus aus dem PAULA-
	 * Datenmodell resultiert.
	 * @param corpusPath String - der Pfad der bisherigen Korpora, wenn der neu zu erstellende Korpus ein Subkorpus ist
	 * @param corpusName String - Name des neu zu erzeugenden Korpus-Objekt
	 */
	public void startCorpusData(	String parentPath,
									String corpusName) throws Exception
	{
		this.logger.info("start reading corpus '" + corpusName + "'");
		
		//creating corpus element
		SCorpus sCorpus= SaltConcreteFactory.eINSTANCE.createSCorpus();
		sCorpus.setId(corpusName);
		this.saltGraph.addSElement(sCorpus);
		
		//creating relation
		if (this.parentCorpusPath.size()> 0)
		{
			SCorpusRelation corpRel= SaltConcreteFactory.eINSTANCE.createSCorpusRelation();
			corpRel.setSSourceElement(this.saltGraph.getSElementById(this.parentCorpusPath.peek()));
			corpRel.setSDestinationElement(this.saltGraph.getSElementById(corpusName));
			this.saltGraph.addSRelation(corpRel);
		}
		//set name of corpus graph to name of first corpus
		else
			this.saltGraph.setId(corpusName);
		this.parentCorpusPath.push(corpusName);		
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein neuer Korpus bzw. Subkorpus aus dem PAULA-
	 * Datenmodell resultiert.
	 * @param corpusPath String - der Pfad der bisherigen Korpora, wenn der neu zu erstellende Korpus ein Subkorpus ist
	 * @param corpusName String - Name des neu zu erzeugenden Korpus-Objekt
	 */
	public void endCorpusData(	String parentPath,
								String corpusName) throws Exception
	{
		this.parentCorpusPath.pop();
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein neues Dokument aus dem PAULA-
	 * Datenmodell resultiert.
	 * @param corpusPath String - der Pfad der bisherigen Korpora
	 * @param docName String - Name des neu zu erzeugenden Dokument-Objekt
	 * @throws Exception
	 */
	public void startDocumentData(	String corpusPath,
									String docName) throws Exception
	{
		this.logger.info("start reading corpus '" + docName + "'");
		if (corpusPath== null)
			throw new NullPointerException("Cannot start reading document, because the path of parent corpus is empty.");
		
		SDocument sDocument= SaltConcreteFactory.eINSTANCE.createSDocument();
		sDocument.setId(docName);
		this.saltGraph.addSElement(sDocument);
		
		SCorpDocRelation corpDocRel= SaltConcreteFactory.eINSTANCE.createSCorpDocRelation();
		corpDocRel.setSSourceElement(this.saltGraph.getSElementById(this.parentCorpusPath.peek()));
		corpDocRel.setSDestinationElement(this.saltGraph.getSElementById(docName));
		this.saltGraph.addSRelation(corpDocRel);
		
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein Dokument (nicht xml-Dokument) fertig 
	 * eingelesen wurde.
	 * @param corpusPath String - der Pfad der bisherigen Korpora
	 * @param docName String - Name des neu zu erzeugenden Dokument-Objekt
	 * @throws Exception
	 */
	public void endDocumentData(	String corpusPath,
									String docName) throws Exception
	{
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines MetaStructDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabeio werden alle Daten zu einem
	 * gelesenen Textelementes an die Mapper-Klasse ï¿½bergeben. 
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID des Textelementes
	 * @param slType String - Der Typ der TructList in diesem Document
	 * @param structID String - ID der ï¿½bergeordneten Struktur des rel-Elementes (struct-Element)
	 * @param relID String - ID dieses rel-Elementes
	 * @param href String -	Verweisziel dieses rel-Elementes
	 */
	public void metaStructDataConnector(	String corpusPath,
											File paulaFile,
											String paulaId,
											String slType,
											String structID,
											String relID,
											String href) throws Exception
	{
		if (DEBUG_METASTRUCT_DATA)
			System.out.println(	MSG_STD +"metaStructDataConnector with data:\t"+
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", slType: " + slType +
								", structID: " + structID + ", relID: "+ relID+
								", href: "+ href);
		/*
		//alle XPointer aus dem Href extrahieren, ein XPtr darf nur auf ein oder meherer Documente zeigen
		XPtrInterpreter xPtrInterpreter= new XPtrInterpreter();
		xPtrInterpreter.setXPtr(href);
		Vector<XPtrRef> xPtrRefs= xPtrInterpreter.getResult();
		Vector<CollectionDN> colDNs= new Vector<CollectionDN>();
		//alle einzelnen Referenzen ermitteln und Collection zu jeder erstellen
		for (XPtrRef xPtrRef: xPtrRefs)
		{
			//Fehler, wenn XPtr kein einfaches Element
			if (!xPtrRef.getType().equals(XPtrRef.POINTERTYPE.XMLFILE))
				throw new Exception(ERR_METASTRUCT_FILE + href);
			if (DEBUG_METASTRUCT_DATA) System.out.println("referenced document in meta-struct-file: "+xPtrRef.getID());
			String uniqueName= corpusPath + KW_PATH_SEP + xPtrRef.getID() + KW_NAME_SEP + KW_TYPE_DNCOL;
			CollectionDN colDN= (CollectionDN) this.kGraphMgr.getDN(uniqueName);
			//wenn eine solche Collection noch nicht existiert, dann erzeugen
			if (colDN== null)
			{
				colDN= new CollectionDN(uniqueName, KW_TYPE_FILE, xPtrRef.getID());
				//Knoten in den Graphen einfï¿½gen
				this.kGraphMgr.addCollectionDN(colDN, null, this.kGraphMgr.getCurrKorpDN());
			}
			//Knoten in Zuordnungstabelle eintragen
			this.colNamingTable.put(relID, uniqueName);
			colDNs.add(colDN);
		}
		
		//Collectionknoten in ï¿½bergeordneten Knoten einfï¿½gen
		//Name des ï¿½bergeordneten Knoten
		String uniqueName= corpusPath + KW_PATH_SEP + structID + KW_NAME_SEP + KW_TYPE_DNCOL;
		CollectionDN colDN1= (CollectionDN) this.kGraphMgr.getDN(uniqueName);
		//wenn ï¿½bergeordnete Collection noch nicht existiert, dann erzeugen
		if (colDN1== null)
		{
			colDN1= new CollectionDN(uniqueName, KW_TYPE_FILE, structID);
			this.kGraphMgr.addCollectionDN(colDN1, null, this.kGraphMgr.getCurrKorpDN());
			//Knoten in Zuordnungstabelle eintragen
			this.colNamingTable.put(structID, uniqueName);
			
		}
		//Kante vom ï¿½bergeordneten Collectionknoten zu den einzelnen Collectionknoten ziehen
		for (CollectionDN colDN2 :colDNs)
		{
			this.kGraphMgr.setDNToColDN(colDN2, colDN1);
		}
		*/
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines MetaAnnoDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabeio werden alle Daten zu einem
	 * gelesenen Textelementes an die Mapper-Klasse ï¿½bergeben. 
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID des Textelementes
	 * @param paulaType String - Der Typ der Meta-Annotationsdaten dieses Dokumentes
	 * @param xmlBase String - Das Basisdokument fï¿½r Verweisziele dieses Dokumentes 
	 * @param featHref String -	Verweisziel eines feat-Elementes
	 * @param featVal String Value-Wert eines feat-Elementes
	 */
	public void metaAnnoDataConnector(	String corpusPath,
										File paulaFile,
										String paulaId,
										String paulaType,
										String xmlBase,
										String featHref,
										String featVal) throws Exception
	{
		if (DEBUG_METAANNO_DATA)
			System.out.println(	MSG_STD +"metaAnnoDataConnector with data:\t"+
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", featHref: "+ featHref+
								", featVal: "+ featVal);
		
		if ((paulaType== null) || (paulaType.equalsIgnoreCase("")))
		{
			this.logger.warn("Cannot add the given meta-annotation, because no annotation name is given.");
			return;
		}
		//creates a fullName for this meta annotation
		String fullName= paulaType;
		if (this.currSDocument.getSAnnotation(fullName)== null)
		{
			SAnnotation anno= null;
			anno= ModelFactory.eINSTANCE.createSAnnotation();
			anno.setName(fullName);
			anno.setValue(featVal);
			this.currSDocument.addSAnnotation(anno);
		}
		
		/*
		XPtrInterpreter xPtrInterpreter= new XPtrInterpreter();
		xPtrInterpreter.setInterpreter(xmlBase, featHref);
		Vector<XPtrRef> xPtrRefs= xPtrInterpreter.getResult();
		String colDNName= null;		//Name des Collectionknotens, auf den der ColAnnoKnoten verweist
		CollectionDN colDN= null;	//Der Collectionknoten, auf den dieser ColAnnoDN verweist
		//alle Verweisziele extrahieren
		for (XPtrRef xPtrRef: xPtrRefs)
		{
			colDNName= this.colNamingTable.get(xPtrRef.getID()); 
			if (DEBUG_METAANNO_DATA)
				System.out.println(MSG_STD + "this node references to collection node: "+ colDNName);
			//Fehler, wenn Name nicht in NameingTable vorhanden
			if (colDNName== null)
				throw new Exception(ERR_ID_NOT_IN_NTABLE + featHref);
			//CollectionDN, auf den sich dieser Knoten bezieht ermitteln
			colDN= (CollectionDN) this.kGraphMgr.getDN(colDNName);
			//eindeutigen Namen fï¿½r Knoten erzeugen
			String uniqueName= corpusPath + KW_PATH_SEP + paulaType + KW_NAME_SEP+ colAnnoNameExt;
			colAnnoNameExt++;
			//Attribut-Wert-Paare (eigentliche ANnotation erzeugen)
			Hashtable<String, String> attValPairs= new Hashtable<String, String>();
			attValPairs.put(paulaType, featVal);
			//MetaAnnotationsknoten erstellen  (eindeutiger Name fï¿½r Graph, CollectionDN, Attribut-Wert-Paare)
			ColAnnoDN colAnnoDN= new ColAnnoDN(uniqueName, colDN, attValPairs);
			//colAnnoDN in Baum eintragen
			this.kGraphMgr.addColAnnoDN(colAnnoDN, colDN, this.kGraphMgr.getCurrKorpDN());
		}
		*/
	}
	//TODO very dirty hack
	private static final String KW_DIRTY_NS=	"dirty:ns";
	
	//TODO dirty hack, ns has be to returned by readers
	private String extractNS(File paulaFile)
	{
		String ns= null;
		String[] fileNameParts= paulaFile.getName().split("[.]");
		ns= fileNameParts[0];
		return(ns);
	}
	
	/**
	 * Nimmt die Daten eines Readers fï¿½r Primï¿½rdaten entgegen und verarbeitet sie indem
	 * die entsprechenden Knoten in einem internen Graph erzeugt werden. Die ï¿½bergebenen
	 * Daten werden auf das relANNIS 2.0 Modell ï¿½ber das Package relANNIS_2_0 abgebildet.
	 * Diese Methode erzeugt einen Dokument- und einen Primï¿½rdatenknoten.
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param corpusPath String - Pfad durch den KOrpus in dem das aktuelle Dokument liegt
	 * @param paulaId String - PAULA-ID des Textelementes
	 * @param text String - Text des Textelementes
	*/
	public void primDataConnector(	String corpusPath, 
									File paulaFile,
									String paulaId, 
									String text) throws Exception
	{
		//create uniqueName
		String uniqueName= paulaFile.getName();
		//create element
		STextualDataSource sTextualDS= SaltConcreteFactory.eINSTANCE.createSTextualDataSource();
		//sTextualDS.setId(uniqueName);
		sTextualDS.setSText(text);
		this.currSDocument.getSDocumentGraph().addSElement(sTextualDS);
		
		//create entry in naming table
		this.elementNamingTable.put(uniqueName, sTextualDS.getId().toString());	
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines TokDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabeio werden alle Daten zu einem
	 * gelesenen Tokenelementes an die Mapper-Klasse ï¿½bergeben. 
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaId String - PAULA-ID dieses Tokenelementes
	 * @param paulaType String - Paula-Typ dieses Tokenelementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Tokenelement bezieht
	 * @param markID String - Mark-ID dieses Tokenelementes
	 * @param href String - Bezugselement, auf die sich dieses Tokenelementes bezieht
	 * @param markType String - Mark-Type dieses Tokenelementes
	 */
	public void tokDataConnector(	String corpusPath,
									File paulaFile,
									String paulaId, 
									String paulaType,
									String xmlBase,
									String markID,
									String href,
									String markType) throws Exception
	{
		logger.debug(	MSG_STD +"tokDataConnector with data:\t"+
						"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
						", paulaID: " + paulaId + ", paulaType: " + paulaType +
						", xmlBase: " + xmlBase + ", markID: "+ markID+
						", href: "+ href + ", markType: "+ markType);
		
		//create unique name for element
		String uniqueName= paulaFile.getName() +KW_NAME_SEP + markID;
		{
			//create entry in element order table (file: elements)
			if (this.elementOrderTable.get(paulaFile.getName())== null)
			{
				Collection<String> orderedElementSlot= new Vector<String>();
				this.elementOrderTable.put(paulaFile.getName(), orderedElementSlot);
			}	
			Collection<String> orderedElementSlot= this.elementOrderTable.get(paulaFile.getName());
			orderedElementSlot.add(uniqueName);
		}
		
		//create namespace
		String currNS= this.extractNS(paulaFile);

		//Objekt zum Interpretieren des XLinks in mark.href initialisieren
		XPtrInterpreter xPtrInter= new XPtrInterpreter();
		xPtrInter.setInterpreter(xmlBase, href);
		Vector<XPtrRef> xPtrRefs=  xPtrInter.getResult();
		int runs= 0;
		//suche den Primï¿½rdatenknoten zu diesem Tokendatenknoten
		STextualDataSource primDN= null;
		Long left= null;	//linke Textgrenze
		Long right= null;	//rechte Textgrenze
		for (XPtrRef xPtrRef: xPtrRefs)
		{
			runs++;
			//Fehler wenn es mehr als eine Referenz gibt
			if (runs > 1) throw new Exception(ERR_TOO_MANY_REFS + xPtrInter.getXPtr());
			//Wenn Xpointer auf einen Text referenziert
			else if (xPtrRef.getType()== XPtrRef.POINTERTYPE.TEXT)
			{
				String textNodeName= this.elementNamingTable.get(xPtrRef.getDoc());
				primDN= (STextualDataSource) this.currSDocument.getSDocumentGraph().getSElementById(textNodeName);
				try
				{
					left= new Long (xPtrRef.getLeft());
					right= new Long (xPtrRef.getRight());
					//linken und rechten Wert in korrrektes Format bringen
					left= left-1;
					right= left + right;
					if (left > right)
						throw new NullPointerException(MSG_ERR + "Cannot create token, because its left value is higher than its right value. Error in document "+ paulaFile.getName()+ ".");
				}
				catch (Exception e)
				{throw new Exception(ERR_WRONG_LEFT_RIGHT + xPtrInter.getXPtr());}
			}
			//Wenn XPointer nicht auf einen Text referenziert
			else 
				throw new Exception(ERR_XPTR_NOT_A_TEXT + "base: "+xPtrRef.getDoc() + ", element: " + xPtrInter.getXPtr() + ", type: "+ xPtrRef.getType());
		}
		//wenn kein Primï¿½rdatenknoten, dann Fehler
		if (primDN == null) throw new Exception(ERR_NO_PRIMDN + paulaFile.getName() + KW_NAME_SEP + markID );
		
		//create SToken object
		SToken sToken= SaltConcreteFactory.eINSTANCE.createSToken();
		//sToken.setId(markID);  //not possible, because these id´s are not unique for one document file+id is unique but long
		//TODO dirty hack to add namespaces
		{
			String ns= this.extractNS(paulaFile);
			if (ns!= null)
			{	
				SProcessingAnnotation spAnno= ModelFactory.eINSTANCE.createSProcessingAnnotation();
				spAnno.setFullName(KW_DIRTY_NS);
				spAnno.setValue(ns);
				sToken.addSProcessingAnnotation(spAnno);
			}
		}	
		//sToken.setId(uniqueName);
		this.currSDocument.getSDocumentGraph().addSElement(sToken);
		
		//create entry in naming table
		this.elementNamingTable.put(uniqueName, sToken.getId().toString());
		
		//create relation
		STextualRelation textRel= SaltConcreteFactory.eINSTANCE.createSTextualRelation();
		textRel.setSSourceElement(sToken);
		textRel.setSDestinationElement(primDN);
		textRel.setSLeftPos(left);
		textRel.setSRightPos(right);
		this.currSDocument.getSDocumentGraph().addSRelation(textRel);
	}
	
	/**
	 * Returns a list of all paula-element-ids refered by the given xpointer-expression.
	 * @param xmlBase
	 * @param href
	 */
	private Collection<String> getPAULAElementIds(String xmlBase, String href) throws Exception
	{
		Collection<String> refPaulaIds= null;
		refPaulaIds= new Vector<String>();
		XPtrInterpreter xPtrInter= new XPtrInterpreter();
		xPtrInter.setInterpreter(xmlBase, href);
		//gehe durch alle Knoten, auf die sich dieses Element bezieht
		Vector<XPtrRef> xPtrRefs=  xPtrInter.getResult();
		for (XPtrRef xPtrRef: xPtrRefs)
		{
			//Fehler, wenn XPointer-Reference vom falschen Typ
			if (xPtrRef.getType()!= XPtrRef.POINTERTYPE.ELEMENT)
				throw new NullPointerException(ERR_WRONG_REF_KIND_ELEM + href);
			
			//wenn XPointer-Bezugsknoten einen Bereich umfasst
			if (xPtrRef.isRange())
			{
				//erzeuge den Namen des linken Bezugsknotens
				String leftName= xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getLeft();
				//erzeuge den Namen des rechten Bezugsknotens
				String rightName= xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getRight();
				//extract all paula elements which are refered by this pointer
				{
					
					boolean start= false;
					for (String paulaElementId : this.elementOrderTable.get(xPtrInter.getDoc()))
					{
						//if true, first element was found
						if (paulaElementId.equalsIgnoreCase(leftName))
							start= true;
						//if start of range is reached
						if (start)
						{
							refPaulaIds.add(paulaElementId);
						}
						//if last element was found, break
						if (paulaElementId.equalsIgnoreCase(rightName))
							break;
					}
				}
//				String typeName= this.kGraphMgr.getDNType(this.kGraphMgr.getDN(leftName));
//				for (ICMAbstractDN absDN: this.kGraphMgr.getDNRangeByType(typeName, leftName, rightName))
//					refNodes.add((TextedDN) absDN); 
			}
			//wenn XPointer-Bezugsknoten einen einzelnen Knoten referenziert
			else
			{
				String paulaElementId= xPtrRef.getDoc() +KW_NAME_SEP + xPtrRef.getID();
				refPaulaIds.add(paulaElementId);
				//erzeuge den Namen des Bezugsknotens
//				String nodeName= corpusPath + KW_NAME_SEP + xPtrRef.getDoc() +KW_NAME_SEP + xPtrRef.getID();
//				TextedDN refNode= (TextedDN)this.kGraphMgr.getDN(nodeName);
//				if (refNode == null) throw new Exception(ERR_CANNOT_FIND_REFNODE + nodeName);
//				refNodes.add(refNode);
			}	
		}
		return(refPaulaIds);
	}
	
	public void markableDataConnector(	String corpusPath,
										File paulaFile,
										String paulaId, 
										String paulaType,
										String xmlBase,
										String markID,
										String href,
										String markType) throws Exception
	{
		logger.debug(	MSG_STD +"markableDataConnector with data:\n"+MSG_STD +
						"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
						", paulaID: " + paulaId + ", paulaType: " + paulaType +
						", xmlBase: " + xmlBase + ", markID: "+ markID+
						", href: "+ href +", markType: "+ markType);
		
		//create unique name for element
		String uniqueName= paulaFile.getName() +KW_NAME_SEP + markID;
		{
			//create entry in element order table (file: elements)
			if (this.elementOrderTable.get(paulaFile.getName())== null)
			{
				Collection<String> orderedElementSlot= new Vector<String>();
				this.elementOrderTable.put(paulaFile.getName(), orderedElementSlot);
			}	
			Collection<String> orderedElementSlot= this.elementOrderTable.get(paulaFile.getName());
			orderedElementSlot.add(uniqueName);
		}
		
		Collection<String> refPAULAElementIds= this.getPAULAElementIds(xmlBase, href);
		//create struct element
		SStructure sStruct= SaltConcreteFactory.eINSTANCE.createSStructure();
		this.currSDocument.getSDocumentGraph().addSElement(sStruct);
		//TODO dirty hack to add namespaces
		{
			String ns= this.extractNS(paulaFile);
			if (ns!= null)
			{	
				SProcessingAnnotation spAnno= ModelFactory.eINSTANCE.createSProcessingAnnotation();
				spAnno.setFullName(KW_DIRTY_NS);
				spAnno.setValue(ns);
				sStruct.addSProcessingAnnotation(spAnno);
			}
		}	
		//create entry in naming table
		this.elementNamingTable.put(uniqueName, sStruct.getId().toString());
		
		//create relations for all referenced tokens
		SSpanRelation spanRel= null;
		for (String refPAULAId: refPAULAElementIds)
		{
			spanRel= SaltConcreteFactory.eINSTANCE.createSSpanRelation();
			spanRel.setSSourceElement(sStruct);
			spanRel.setSDestinationElement(this.currSDocument.getSDocumentGraph().getSElementById(this.elementNamingTable.get(refPAULAId)));
			this.currSDocument.getSDocumentGraph().addSRelation(spanRel);
		}
	}
	
	/**
	 * Needed for storing dominance relations out of paula-struct-documents. 
	 * Pre-Storing is necassary, because of struct-elements can refer to other struct-elements
	 * which aren´t read at this time. Therefore the relations can be stored after reading all 
	 * elements.
	 * @author Florian Zipser
	 *
	 */
	private class DominanceRelationContainer
	{
		public String paulaId= null;
		public SDominanceRelation relation= null;
		public String xmlBase= null;
		public String href= null;
	}
	
	private Collection<DominanceRelationContainer> dominanceRelationContainers= null;
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines StructEdgeDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse ï¿½bergeben. 
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Strukturelementes
	 * @param paulaType String - Paula-Typ dieses Strukturelementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Strukturelement bezieht
	 * @param structID String - ID des StructListElementes dem dieses Struct-Element unterstellt ist
	 * @param relID String - ID dieses Struct-Elementes
	 * @param relHref String - Verweis auf untergeordnete Struktur- oder Tokenelemente 
	 * @param relType String - Kantenannotation dieses Struct-Elementes
	 * 
	 * @param href String - Bezugselement, auf die sich dieses Strukturelementes bezieht
	 * @param markType String - Mark-Type dieses Strukturelementes
	 */
	public void structEdgeDataConnector(	String 	corpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String 	structID,
											String	relID,
											String	relHref,
											String	relType) throws Exception
	{
		if (DEBUG_SE)
			System.out.println(	MSG_STD +"structEdgeDataConnector with data:\n"+MSG_STD +
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", structID: "+ structID+
								", relID: "+ relID +", relHref: "+ relHref + ", relType: "+ relType);
			//create unique name for element
			String uniqueNameStruct= paulaFile.getName() +KW_NAME_SEP + structID;
			String uniqueNameRel= paulaFile.getName() +KW_NAME_SEP + relID;
			{
				//create entry in element order table (file: elements)
				if (this.elementOrderTable.get(paulaFile.getName())== null)
				{
					Collection<String> orderedElementSlot= new Vector<String>();
					this.elementOrderTable.put(paulaFile.getName(), orderedElementSlot);
				}	
				//check if struct is already inserted
				Collection<String> orderedElementSlot= this.elementOrderTable.get(paulaFile.getName());
				if (!orderedElementSlot.contains(uniqueNameStruct))
				{	
					orderedElementSlot.add(uniqueNameStruct);
				}
				if (!orderedElementSlot.contains(uniqueNameRel))
				{	
					orderedElementSlot.add(uniqueNameRel);
				}
			}
			
			if (this.elementNamingTable.get(uniqueNameStruct)== null)
			{	
				//create struct element
				SStructure sStruct= SaltConcreteFactory.eINSTANCE.createSStructure();
				
				//sStruct.setId(structID); //not possible, because these id´s are not unique for one document file+id is unique but long
				this.currSDocument.getSDocumentGraph().addSElement(sStruct);
				
				//TODO dirty hack to add namespaces
				{
					String ns= this.extractNS(paulaFile);
					if (ns!= null)
					{	
						SProcessingAnnotation spAnno= ModelFactory.eINSTANCE.createSProcessingAnnotation();
						spAnno.setFullName(KW_DIRTY_NS);
						spAnno.setValue(ns);
						sStruct.addSProcessingAnnotation(spAnno);
					}
				}
				
				//create entry in naming table for struct		
				this.elementNamingTable.put(uniqueNameStruct, sStruct.getId().toString());
			}
			
			//pre creating relation
			SDominanceRelation domRel= SaltConcreteFactory.eINSTANCE.createSDominanceRelation();
			String saltDstName= this.elementNamingTable.get(uniqueNameStruct);
			domRel.setSSourceElement(this.currSDocument.getSDocumentGraph().getSElementById(saltDstName));
			if ((relType!= null) && (!relType.equalsIgnoreCase("")))
					domRel.setSType(relType);
			
			//creating new container list
			if (dominanceRelationContainers== null)
				dominanceRelationContainers= new Vector<DominanceRelationContainer>();
				
			//creating dominance relation container
			DominanceRelationContainer domCon= new DominanceRelationContainer();
			domCon.paulaId= uniqueNameRel;
			domCon.relation= domRel;
			domCon.xmlBase= xmlBase;
			domCon.href= relHref;
			this.dominanceRelationContainers.add(domCon);
			
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines AnnoDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse ï¿½bergeben. 
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param paulaType String - Paula-Typ dieses Annotationselementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
	 * @param featID String - feat-ID dieses Annotationselementes
	 * @param featHref String - Bezugselement, auf die sich dieses Annotationselementes bezieht
	 * @param featTar String - feat-Target dieses Annotationselementes
	 * @param featVal String - feat-Value dieses Annotationselementes
	 * @param featDesc String - feat-Description dieses Annotationselementes
	 * @param featExp String - feat-Example dieses Annotationselementes
	 */
	public void annoDataConnector(	String 	corpusPath,
									File 	paulaFile,
									String 	paulaId, 
									String 	paulaType,
									String 	xmlBase,
									String 	featID,
									String 	featHref,
									String 	featTar,
									String 	featVal,
									String 	featDesc,
									String 	featExp) throws Exception
	{

		if (DEBUG_ANNO_DATA)
			System.out.println(	MSG_STD + "annoDataConnector with data:\n"+MSG_STD +
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", featID: "+ featID+
								", featHref: "+ featHref +", featTar: "+ featTar+
								", featVal: "+ featVal + ", featDesc: "+ featDesc+
								", featExp: " +featExp);
		
		if ((paulaType== null)|| (paulaType.equalsIgnoreCase("")))
				this.logger.warn(MSG_ERR + "Can not work with the given annotation, because the type-value is empty.");		
		else if ((featVal== null) || (featVal.equalsIgnoreCase(""))) 
			this.logger.warn(MSG_ERR + "Can not work with the given annotation, because the value-value is empty.");	
		
		else
		{	
			Collection<String> paulaElementIds= this.getPAULAElementIds(xmlBase, featHref);
			SAnnotation sAnno= ModelFactory.eINSTANCE.createSAnnotation();
			
	//		annoTable.put(paulaType, featVal);
	//		//wenn es ein target-Attribut gibt
	//		if ((featTar!= null) && (!featTar.equalsIgnoreCase("")))
	//			annoTable.put(KW_ANNO_TAR, featTar);
	//		//wenn es ein description-Attribut gibt
	//		if ((featDesc!= null) && (!featDesc.equalsIgnoreCase("")))
	//			annoTable.put(KW_ANNO_DESC, featDesc);
	//		//wenn es ein example-Attribut gibt
	//		if ((featExp!= null) && (!featExp.equalsIgnoreCase("")))
	//			annoTable.put(KW_ANNO_EXP, featExp);
			
			if ((paulaType!= null) && (!paulaType.equalsIgnoreCase("")))
			{
				String ns= this.extractNS(paulaFile);
				if ((ns!= null) && (!ns.equalsIgnoreCase("")))
					sAnno.setNamespace(this.extractNS(paulaFile));
				sAnno.setName(paulaType);
				sAnno.setValue(featVal);
			}
			
			for (String paulaElementId: paulaElementIds)
			{
				if ((paulaElementId== null) || (paulaElementId.equalsIgnoreCase("")))
					throw new NullPointerException(MSG_ERR + "No element with xml-id:"+ paulaElementId+ " was found.");
				SElement refElement= this.currSDocument.getSDocumentGraph().getSElementById(this.elementNamingTable.get(paulaElementId));
				SRelation refRelation= this.currSDocument.getSDocumentGraph().getSRelationById(this.elementNamingTable.get(paulaElementId));
				if (refElement!= null)
				{
					try {
						refElement.addSAnnotation(sAnno);
					} catch (Exception e) 
					{
						this.logger.warn("Exception in paula file: "+paulaFile.getCanonicalPath()+" at element: "+featHref+". Original message is: "+e.getMessage());
					}
				}	
				else if(refRelation!= null)
				{
					refRelation.addSAnnotation(sAnno);
				}
				else
					{throw new NullPointerException(MSG_ERR + "No element with xml-id:"+ paulaElementId+ " was found.");}
			}
		}
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines ComplexAnnoDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse ï¿½bergeben. 
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param paulaType String - Paula-Typ dieses Annotationselementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
	 * @param featID String - feat-ID dieses Annotationselementes
	 * @param featHref String - Bezugselement, auf die sich dieses Annotationselementes bezieht
	 * @param featTar String - feat-Target dieses Annotationselementes
	 * @param featVal String - feat-Value dieses Annotationselementes
	 * @param featDesc String - feat-Description dieses Annotationselementes
	 * @param featExp String - feat-Example dieses Annotationselementes
	 */
	public void complexAnnoDataConnector(	String 	corpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String 	featID,
											String 	featHref,
											String 	featTar,
											String 	featVal,
											String 	featDesc,
											String 	featExp) throws Exception
	{
		if (DEBUG_COMPLEX_ANNO_DATA)
			System.out.println(	MSG_STD + "complexAnnoDataConnector with data:\n"+MSG_STD +
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", featID: "+ featID+
								", featHref: "+ featHref +", featTar: "+ featTar+
								", featVal: "+ featVal + ", featDesc: "+ featDesc+
								", featExp: " +featExp);
		throw new Exception(ERR_FCT_DEPRECATED);
	}
	
	/**
	 * Diese Methode bietet ein einfaches Interface zur Kommunikation eines MultiFeatDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. 
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param multiFeatListTYPE String - 	Typ/ Annotationsebene all der Annotation
	 * @param multiFeatListBASE String - 	Basisdokument, auf dass sich alle Referenzen beziehen
	 * @param multiFeatID String - 			Identifier eines feat-Satzes
	 * @param multiFeatHREF String - 		Referenzziel eines feat-Satzes
	 * @param multiFeatID String - 			Identifier einer einzelnen Annotation
	 * @param multiFeatNAME String - 		Name einer einzelnen Annotation
	 * @param multiFeatVALUE String - 		Annotationswert einer einzelnen Annotation
	 */
	public void multiFeatDataConnector(	String 	corpusPath,
										File 	paulaFile,
										String 	paulaId, 
										String 	multiFeatListTYPE,
										String 	multiFeatListBASE,
										String 	multiFeatID,
										String 	multiFeatHREF,
										String 	featID,
										String 	featNAME,
										String 	featVALUE) throws Exception
	{
		/*
		if (DEBUG_MULTI_FEAT_DATA)
			System.out.println(	MSG_STD + "multiFeatDataConnector with data:\n"+MSG_STD +
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", multiFeatListTYPE: " + multiFeatListTYPE+
								", multiFeatListBASE: " + multiFeatListBASE + ", multiFeatID: "+ multiFeatID+
								", multiFeatHREF: "+ multiFeatHREF + ", featID: "+ featID+
								", featNAME: "+ featNAME + ", featVALUE: "+ featVALUE);
		this.annoDataConnector(	corpusPath, paulaFile, paulaId, 
								featNAME, multiFeatListBASE, 
								featID, multiFeatHREF, null, featVALUE, 
								null, null);		
		*/				
								
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines AudioDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse ï¿½bergeben. Wenn AudioRef= null wird kein AudioDN erzeugt.
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param paulaType String - Paula-Typ dieses Annotationselementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
	 * @param featID String - feat-ID dieses Annotationselementes
	 * @param featHref String - Bezugselement, auf die sich dieses Annotationselementes bezieht
	 * @param featTar String - feat-Target dieses Annotationselementes
	 * @param featDesc String - feat-Description dieses Annotationselementes
	 * @param featExp String - feat-Example dieses Annotationselementes
	 * @param audioRef File - File-Referenz auf die Audio-Datei
	 */
	public void audioDataConnector(	String 	corpusPath,
									File 	paulaFile,
									String 	paulaId, 
									String 	paulaType,
									String 	xmlBase,
									String 	featID,
									String 	featHref,
									String 	featTar,
									String 	featDesc,
									String 	featExp,
									File 	audioRef) throws Exception
	{
		/*
		if (DEBUG_AUDIO_DATA)
			System.out.println(	MSG_STD + "audioDataConnector with data:\n"+MSG_STD +
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", featID: "+ featID+
								", featHref: "+ featHref +", featTar: "+ featTar+
								", featDesc: "+ featDesc+ ", featExp: " +featExp+
								", audio file ref: "+ audioRef);
		
		if (audioRef!= null)
		{
			boolean areNodes= false;	//gibt an, ob die Referenzen auf Knoten verweisen
			Vector<TextedDN> refNodes= null;
			
			//Erzeuge Zuordnungstabelle fï¿½r Annotationen
			Hashtable<String, String> annoTable= new Hashtable<String, String>();
			//wenn es ein target-Attribut gibt
			if ((featTar!= null) && (!featTar.equalsIgnoreCase("")))
				annoTable.put(KW_ANNO_TAR, featTar);
			//wenn es ein description-Attribut gibt
			if ((featDesc!= null) && (!featDesc.equalsIgnoreCase("")))
				annoTable.put(KW_ANNO_DESC, featDesc);
			//wenn es ein example-Attribut gibt
			if ((featExp!= null) && (!featExp.equalsIgnoreCase("")))
				annoTable.put(KW_ANNO_EXP, featExp);
			
			try
			{
				//alle Knoten ermitteln, auf die diese Annotation verweist
				refNodes= this.extractXPtr(corpusPath, xmlBase, featHref);
				areNodes= true;
			}
			catch (Exception e)
				{ areNodes= false; }
			//Verweisziele sind Knoten
			if (areNodes)
			{
				//Name des zu erzeugenden AudioDN erstellen
				//Wenn Element keinen ID-wert besitzt, Namenserweiterung erstellen
				String nameID= null;
				if ((featID== null) || (featID.equalsIgnoreCase("")))
				{
					nameID= this.annoNameExt.toString();
					this.annoNameExt++;
				}
				else nameID= featID; 
				String uniqueName= corpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + nameID;
				if (DEBUG) System.out.println(MSG_STD + "name for audioDN: "+ uniqueName);
				
				//erzeuge den Namen desAnnotationslevels
				String parts[] =paulaFile.getName().split("[.]");
				String annoLevelName= parts[0];
				//neuen Annotationsknoten erzeugen (eindeutiger Knotenname fï¿½r Graphen, zu speichernder Name, referenzierte KNoten, Attribut-Wert-Paare, (Datei-)Collection zu der dieser Knoten gehï¿½rt)
				AudioDN audioDN= new AudioDN(uniqueName, refNodes, annoTable, annoLevelName, audioRef, this.currFileColDN);
				//Konvertieren der TextedDN in IKMAbstractDN
				Vector<ICMAbstractDN> refAbsNodes= new Vector<ICMAbstractDN>();
				for (TextedDN textedDN: refNodes)
				{
					refAbsNodes.add((ICMAbstractDN) textedDN);
				}
				//Knoten in Graphen einfï¿½gen
				this.kGraphMgr.addAnnoDN(audioDN, refAbsNodes);
			}
			//Verweisziele sind mï¿½glicherweise Kanten
			else
			{
				try
				{
					Collection<ICMAbstractEdge> edges= this.extractXPtrAsEdge(corpusPath, xmlBase, featHref);
					for (Edge edge: edges)
					{
						edge.addLabel(annoTable);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Exception("Error in document '"+this.currFileColDN.getName()+"': "+e.getMessage());
				}	
			}
		}
		*/
	}
	
	/**
	 * Diese Methode bietet ein einfaches Interface zur Kommunikation eines ComplexAnnoDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. 
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param paulaType String - Paula-Typ dieses Annotationselementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
	 * @param featVal String - Wert, den diese Kante haben kann
	 * @param srcHref String - Quelle von der aus diese Nicht-Dominanz-Kante zu ziehen ist
	 * @param dstHref String - Ziel zu dem diese Nicht-Dominanz-Kante zu ziehen ist
	 */
	public void pointingRelDataConnector(	String 	corpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String	featVal,
											String 	srcHref,
											String 	dstHref) throws Exception
	{
		if (DEBUG_POINTING_REL_DATA)
			System.out.println(	MSG_STD + "pointingRelDataConnector with data:\n"+MSG_STD +
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", featVal: "+ featVal+
								", srcHref: "+ srcHref + ", dstHref: "+ dstHref);
		
		if (((srcHref== null) || (srcHref.equalsIgnoreCase(""))) ||
			((dstHref== null) || (dstHref.equalsIgnoreCase(""))))
		{
			this.logger.warn("Cannot create pointing relation of file ("+paulaFile.getName()+"), because source or destination is empty.");
		}
		else
		{	
			Collection<String> paulaSrcElementIds= this.getPAULAElementIds(xmlBase, srcHref);
			Collection<String> paulaDstElementIds= this.getPAULAElementIds(xmlBase, dstHref);
			if ((paulaSrcElementIds== null) || (paulaSrcElementIds.size()== 0))
				throw new NullPointerException(MSG_ERR + "The source of pointing relation in file: "+paulaFile.getName() +" is not set.");
			if ((paulaDstElementIds== null) || (paulaDstElementIds.size()== 0))
				throw new NullPointerException(MSG_ERR + "The destination of pointing relation in file: "+paulaFile.getName() +" is not set.");
			//if there are more than one sources or destinations create cross product
			for (String paulaSrcElementId: paulaSrcElementIds)
			{
				for (String paulaDstElementId: paulaDstElementIds)
				{
					String saltSrcName= this.elementNamingTable.get(paulaSrcElementId);
					String saltDstName= this.elementNamingTable.get(paulaDstElementId);
					if ((saltSrcName== null) || (saltSrcName.equalsIgnoreCase("")))
						throw new NullPointerException(MSG_ERR + "The requestet source of relation (xml-id: "+paulaSrcElementId+") of file '"+paulaFile.getName()+"' does not exists.");
					SPointingRelation pRel= SaltConcreteFactory.eINSTANCE.createSPointingRelation();
					//SDominanceRelation pRel= SaltConcreteFactory.eINSTANCE.createSDominanceRelation();
					if ((saltDstName== null) || (saltDstName.equalsIgnoreCase("")))
						throw new NullPointerException(MSG_ERR + "The requestet destination of relation (xml-id: "+paulaDstElementId+") of file '"+paulaFile.getName()+"' does not exists.");
					pRel.setSType(paulaType);
					pRel.setSSourceElement(this.currSDocument.getSDocumentGraph().getSElementById(saltSrcName));
					pRel.setSDestinationElement(this.currSDocument.getSDocumentGraph().getSElementById(saltDstName));
					this.currSDocument.getSDocumentGraph().addSRelation(pRel);
				}
			}
		}
	}
	
	/**
	 * Diese Methode bietet ein einfaches Interface zur Kommunikation eines ComplexAnnoDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse ï¿½bergeben. 
	 * @param corpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param paulaType String - Paula-Typ dieses Annotationselementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
	 * @param featVal String - Wert, den diese Kante haben kann
	 * @param srcHref String - Quelle von der aus diese Nicht-Dominanz-Kante zu ziehen ist
	 * @param dstHref String - Ziel zu dem diese Nicht-Dominanz-Kante zu ziehen ist
	 */
	public void complexAnnoDataConnector(	String 	corpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String	featVal,
											String 	srcHref,
											String 	dstHref) throws Exception
	{
		/*
		if (DEBUG_COMPLEX_ANNO_DATA)
			System.out.println(	MSG_STD + "complexAnnoDataConnector with data:\n"+MSG_STD +
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", featVal: "+ featVal+
								", srcHref: "+ srcHref + ", dstHref: "+ dstHref);
		//Fehler wenn kein Quellknoten gegeben
		if ((srcHref== null) || (srcHref.equalsIgnoreCase("")))
			throw new Exception(ERR_CAD_NO_SRC + "complexAnnoDataConnector()");
		//Quellknoten ermitteln, auf die diese Annotation verweist
		ICMAbstractDN srcNode= (ICMAbstractDN)this.extractXPtr(corpusPath, xmlBase, srcHref).firstElement();
		//Zielknoten ermitteln, auf die diese Annotation verweist sofern ein Ziel angegeben wurde
		ICMAbstractDN dstNode= null;
		if (dstHref!= null)
			dstNode= (ICMAbstractDN)this.extractXPtr(corpusPath, xmlBase, dstHref).firstElement();
		
		//-- structRelDN erzeugen
		//eindeutigen Namen fï¿½r den StructRelDN erzeugen
		String uniqueName= corpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + cadNameExt;
		//Namensextension erhï¿½hen
		cadNameExt++;
		//neuen StructRelDN erzeugen (eindeutiger Name im Graphen, (Datei-)Collection zu der der Knoten gehï¿½rt, Quellknoten von dem aus die Nixht-Dominanzkante geht, Zielknoten zu dem die Nixht-Dominanzkante geht)
		StructRelDN structRelDN= new StructRelDN(uniqueName, MapperV1.createNS(paulaFile.getName()), paulaType, this.currFileColDN, srcNode, dstNode); 
		
		if (DEBUG_COMPLEX_ANNO_DATA)
		{
			if (dstNode!= null)
				System.out.println(MSG_STD + "non dominance edge for node: '"+ structRelDN.getName() +"' from '"+srcNode.getName()+"' to '"+dstNode.getName()+"'.");
			else 
				System.out.println(MSG_STD + "non dominance edge '"+ structRelDN.getName() +"' from '"+srcNode.getName()+"' to 'no node given'.");
		}
		
		//srcNode und dstNode in Liste schreiben und neuen Knoten in Graphen eintragen
		Vector<ICMAbstractDN> refNodes = new Vector<ICMAbstractDN>();
		refNodes.add(srcNode);
		if (dstNode!= null) refNodes.add(dstNode);
		//structRelDN in Graphen einfï¿½gen
		this.kGraphMgr.addStructRelDN(structRelDN, refNodes);
		//ggf. Kante von structRelDN zu aktuellem colDN
		if (this.currFileColDN!= null) this.kGraphMgr.setDNToColDN(structRelDN, this.currFileColDN);
		//-- annoDN erzeugen
		//eindeutigen Namen fï¿½r Annotationsknoten erstellen
		uniqueName= corpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + paulaType + KW_NAME_SEP +this.annoNameExt;
		this.annoNameExt++;
		//Referenzknoten als TextedNodes
		Vector<TextedDN> textedDNs= new Vector<TextedDN>(); 
		textedDNs.add((TextedDN)structRelDN);
		
		//Attribut-Wert-Paare erstellen
		Hashtable<String, String> attValPairs= new Hashtable<String, String>(); 
		attValPairs.put(KW_REL_TYPE_NAME, paulaType);
		//Attributwert  featVal einfï¿½gen, wenn dieser existiert
		if ((featVal != null) && (!featVal.equalsIgnoreCase("")))
			attValPairs.put(KW_ANNO_VALUE, featVal);
		//neuen Annotationsknoten erzeugen (eindeutiger Knotenname fï¿½r Graphen, zu speichernder Name, referenzierte KNoten, Attribut-Wert-Paare, (Datei-)Collection zu der dieser Knoten gehï¿½rt)
		AnnoDN annoDN= new AnnoDN(uniqueName, textedDNs, attValPairs, paulaType, this.currFileColDN);
		//Referenzknoten als IKMAbstractDN
		refNodes= new Vector<ICMAbstractDN>();
		refNodes.add(structRelDN);
		//AnnoKnoten in Graphen schreiben
		this.kGraphMgr.addAnnoDN(annoDN, refNodes);
		*/
	}
	
	/**
	 * Diese Methode bietet ein Interface, damit ein spezifischer Reader dem Mapper-Objekt
	 * mitteilen kann wenn das parsen einer PAULA-Datei gestartet wurde. Zur Identifikation
	 * welches Objekt dieses Event erzeugt hat, muss es als Parameter mitgegeben werden.
	 * @param paulaReader PAULAReader - Reader, der dieses Event erzeugt hat.
	 * @param paulaFile File - geparste Datei
	 * @param corpusPath String - Korpuspfad des aktuellen Dokumentes/Korpus 
	 */
	public void startDocument(	PAULAReader paulaReader, 
								File paulaFile,
								String corpusPath) throws Exception
	{
		
	}
	
	/**
	 * Diese Methode bietet ein Interface, damit ein spezifischer Reader dem Mapper-Objekt
	 * mitteilen kann wenn das parsen einer PAULA-Datei beendet wurde. Zur Identifikation
	 * welches Objekt dieses Event erzeugt hat, muss es als Parameter mitgegeben werden.
	 * @param paulaReader PAULAReader - Reader, der dieses Event erzeugt hat.
	 * @param corpusPath String - Korpuspfad des aktuellen Dokumentes/Korpus
	 */
	public void endDocument(	PAULAReader paulaReader,
								File paulaFile,
								String corpusPath) throws Exception
	{
		
		//storing dominance relations in graph
		if (dominanceRelationContainers!= null)
		{
			
			for (DominanceRelationContainer domCon: dominanceRelationContainers)
			{
				Collection<String> refPAULAElementIds= this.getPAULAElementIds(domCon.xmlBase, domCon.href);
				for (String refPAULAId: refPAULAElementIds)
				{
					SElement dstElement= this.currSDocument.getSDocumentGraph().getSElementById(this.elementNamingTable.get(refPAULAId));
					if (dstElement== null)
						throw new NullPointerException(MSG_ERR + "No paula element with name: "+ refPAULAId + " was found.");
					domCon.relation.setSDestinationElement(dstElement);
					this.currSDocument.getSDocumentGraph().addSRelation(domCon.relation);
					//create entry in naming table for struct
					if (this.elementNamingTable.get(domCon.paulaId)== null)
					{
						this.elementNamingTable.put(domCon.paulaId, domCon.relation.getId().toString());
					}
				}
			}
			dominanceRelationContainers= null;
		}
		
	}
	// ------------------------------ Ende Methoden aus dem PAULAMapperInterface------------------------------
// ------------------------------ Ende Methoden der Middleware ------------------------------
	
	/**
	 * Gibt Informationen ï¿½ber dieses Objekt als String zurï¿½ck. 
	 * @return String - Informationen ï¿½ber dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		 retStr= "this method isnï¿½t implemented";
		return(retStr);
	}
}
