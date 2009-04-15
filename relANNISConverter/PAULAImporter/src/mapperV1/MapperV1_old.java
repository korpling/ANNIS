//package mapperV1;
//
//import internalKorpusModel.IKMAbstractDN;
//import internalKorpusModel.IKMAbstractEdge;
//
//import java.io.File;
//import java.lang.reflect.Constructor;
//import java.util.Collection;
//import java.util.Hashtable;
//import java.util.Map;
//import java.util.Vector;
//
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//
//import org.apache.log4j.Logger;
//import org.xml.sax.XMLReader;
//
//import relANNIS_2_0.*;
//import util.graph.Edge;
//import util.xPointer.XPtrInterpreter;
//import util.xPointer.XPtrRef;
//import paulaReader_1_0.*;
//import paulaReader_1_0.reader.PAULAReader;
//
///**
// * Die Klasse MapperV1 bildet das Quelldatenmodell PAULA 1.0 auf das Zieldatenmodell 
// * relANNIS 2.0 ab. Zum Einlesen der Quelldaten wird das Package paulaReader_1_0 verwendet.
// * Der Mapper MapperV1 bildet diese Daten dann auf die Klassen des Packages relANNIS_2_0
// * ab, die ihrerseits das Interne KorpusModel bilden und die Abbildung der Daten auf das
// * relANNIS 2.0 Datenmodell erstellen. 
// * <br/><br/>
// * 
// * Dieser Prozess wird über die Methode Mapper.map() angestoßen.
// * 
// * @author Florian Zipser
// * @version 1.0
// */
//public class MapperV1 extends mapper.AbstractMapper implements PAULAMapperInterface
//{
//	/**
//	 * Speichert Rel-Elemente für StructEdgeDatenKnoten zwischen.
//	 * @author Florian Zipser
//	 * @version 1.0
//	 */
//	private class TempStructEdgeDN
//	{
//		String korpusPath= null;
//		File paulaFile= null;
//		String paulaId= null;
//		String paulaType= null;
//		String xmlBase= null;
//		String structID= null;
//		String relID= null;
//		String relHref= null;
//		String relType= null;
//		
//		public TempStructEdgeDN(	String korpusPath, 
//									File paulaFile, 
//									String paulaId, 
//									String paulaType,  
//									String xmlBase, 
//									String structID,
//									String relID,
//									String relHref,
//									String relType) throws Exception
//		{
//			this.korpusPath= korpusPath;
//			this.paulaFile= paulaFile;
//			this.paulaId= paulaId;
//			this.paulaType= paulaType;
//			this.xmlBase= xmlBase;
//			this.structID= structID;
//			this.relID= relID;
//			this.relHref= relHref;
//			this.relType= relType;
//		}
//		
//		public String toString()
//		{
//			String retStr= null;
//			retStr= "relId: "+ this.relID;
//			return(retStr);
//		}
//	}
//	
//	/**
//	 * Speichert Struct-Elemente für StructEdgeDatenKnoten zwischen.
//	 * @author Florian Zipser
//	 * @version 1.0
//	 */
//	private class TempStructEdgeDN2
//	{
//		String 	korpusPath= null;
//		File 	paulaFile= null;
//		String 	paulaId= null;
//		String 	paulaType= null;
//		String 	xmlBase= null;
//		String 	structID= null;
//		Vector<String> refNodes= null;
//		
//		public TempStructEdgeDN2(	String 	korpusPath,
//									File 	paulaFile,
//									String 	paulaId, 
//									String 	paulaType,
//									String 	xmlBase,
//									String 	structID) throws Exception
//		{
//			this.korpusPath= korpusPath;
//			this.paulaFile= paulaFile;
//			this.paulaId= paulaId;
//			this.paulaType= paulaType;
//			this.xmlBase= xmlBase;
//			this.structID= structID;
//		}
//		
//		public void setRefNodes(Vector<String> refNodes)
//			{ this.refNodes= refNodes; }
//		
//		public String toString()
//		{
//			String retStr= null;
//			retStr= "structId: "+ this.structID;
//			return(retStr);
//		}
//	}
//	
//	/**
//	 * Speichert TokenDatenKnoten zwischen
//	 * @author Florian Zipser
//	 * @version 1.0
//	 */
//	private class TempTokDN implements Comparable<TempTokDN>
//	{
//		String uniqueName= null;
//		String paulaType= null;
//		String markID= null;
//		PrimDN primDN= null;
//		CollectionDN colDN= null; 
//		Long left= null;
//		Long right= null;
//		
//		public TempTokDN(String uniqueName, String paulaType, String markID, PrimDN primDN, CollectionDN colDN, Long left, Long  right)
//		{
//			this.uniqueName= uniqueName;
//			this.paulaType= paulaType;
//			this.markID= markID;
//			this.primDN= primDN;
//			this.colDN= colDN;
//			this.left= left;
//			this.right= right;
//		}
//		
//		/**
//		 * Vergleicht zwei Daten für das Interface Comparable
//		 * Wenn "this < argument" dann muss die Methode irgendetwas < 0 zurückgeben
//    	 * Wenn "this = argument" dann muss die Methode 0 (irgendetwas = 0) zurückgeben
//    	 * Wenn "this > argument" dann muss die Methode irgendetwas > 0 zurückgeben     
//		 */
//		public int compareTo(TempTokDN argument) 
//		{
//			if(this.left < argument.left)
//	            return -1;
//	        if( this.left > argument.left)
//	            return 1;     
//	        return 0; 
//		}
//	}
//
//	/**
//	 * Diese Klasse dient dem Speichern von Informationen zu PAULAReadern.
//	 * @author Florian Zipser
//	 *
//	 */
//	private class ReaderInfo
//	{
//		String name= null;			//Name des Verwendeten Readers
//		String readerCType= null;	//CTyp zu der dieser Reader gehört 
//		String className= null;		//Javaklasse dieses Readertyps 
//		Double priority= null;		//Gibt die Priorität an, mit der der Reader zum Zug kommt
//		
//		public ReaderInfo(String name, String readerCType, String className, Double prio)
//		{
//			this.name= name;
//			this.readerCType= readerCType;
//			this.className= className;
//			this.priority= prio;
//		}
//		
//		/**
//		 * Erzeugt einen PAULAReader. Dieser ist ein Objekt der Klasse className. 
//		 * @return neuer PAULAReader zu der hier gespeicherten Klasse
//		 */
//		public PAULAReader getReader(	PAULAMapperInterface pMI,
//										String korpusPath,						
//										File paulaFile,
//										Logger logger) throws Exception
//		{	
//			//Erzeugen des entsprechenden Readers
//			PAULAReader reader= null;
//			//if (Class.forName(className)<PAULAReader>)
//			Class readerClass= Class.forName(this.className);
//			//KonstruktorParameter zusammenbauen
//			Class[] paramTypes = new Class[]{PAULAMapperInterface.class,java.lang.String.class, File.class, Logger.class}; 
//			//suche passenden Konstruktor
//			Constructor<PAULAReader> readerConst= readerClass.getConstructor(paramTypes);
//			reader= readerConst.newInstance(pMI, korpusPath, paulaFile, logger);
//			return(reader);
//		}
//	}
////	 ============================================== private Variablen ==============================================
//	private static final String TOOLNAME= 	"MapperV1";		//Name dieses Tools
//	private static final String VERSION= 	"1.0";			//Version dieses Tools
//	
//	private static final boolean MODE_SE_NEW= true;
//	
//	//Pfad und Dateiname für Settingfiles
//	private static final String FILE_TYPED_KORP=	"typed_corp.xml";				//default name der Korpusstrukturdatei
//	private static final boolean DEBUG=			false;				//DEBUG-Schalter
//	private static final boolean DEBUG_TOK_DATA= false;				//spezieller DEBUG-Schalter für TokData
//	private static final boolean DEBUG_SE=		false;				//spezieller DEBUG-Schalter für StructEdge
//	private static final boolean DEBUG_COMPLEX_ANNO_DATA=	false;	//spezieller DEBUG-Schalter für ComplexAnnoData
//	private static final boolean DEBUG_COLLECTION_DN=	false;		//spezieller DEBUG-Schalter für CollectionDN
//	private static final boolean DEBUG_METASTRUCT_DATA= false;		//spezieller DEBUG-Schalter für MetaStructData
//	private static final boolean DEBUG_METAANNO_DATA= false;		//spezieller DEBUG-Schalter für MetaAnnoData
//	private static final boolean DEBUG_KSDESC=	false;				//spezieller DEBUG-Schalter für das Berechnen des Korpuspfades 
//	private static final boolean DEBUG_STRUCT=	false;				//spezieller DEBUG-Schalter für den StructData-Connector
//	
//	//Schlüsselworte für Readertypen
//	private static final String KW_CTYPE_METASTRUCTDATA=	"MetaStructData";	//MetaAnnotationsstruktur (anno.xml)
//	private static final String KW_CTYPE_METAANNODATA=		"MetaAnnoData";		//Metaannotationen (Dateien, die sich auf anno.xml beziehen)
//	private static final String KW_CTYPE_PRIMDATA=			"PrimData";			//Primärdaten
//	private static final String KW_CTYPE_TOKDATA=			"TokData";			//Tokendaten
//	private static final String KW_CTYPE_STRUCTDATA=		"StructData";		//Strukturdaten
//	private static final String KW_CTYPE_STRUCTEDGEDATA=	"StructEdgeData";	//Kanten-Strukturdaten
//	private static final String KW_CTYPE_ANNODATA=			"AnnoData";			//Annotationsdaten
//	
//	//Standardwerte
//	private static final long STD_ANNO_NAME_EXT=	0;			//Standardwert für die Annotationsnnamenerweiterung
//	private static final long STD_CAD_NAME_EXT=		0;			//Standardwert für die ComplexAnnotationsnnamenerweiterung
//	private static final long STD_COLANNO_NAME_EXT=	0;			//Standardwert für die CollectionAnnotationsnamenerweiterung
//	
//	private static final String KW_STRUCTEDGE_TYPE_ATT=	"EDGE_TYPE";	//Name unter dem das Attribut rel.type im relANNIS Modell als Annotation geführt werden soll 
//	
//	//Schlüsselworte
//	private static final String KW_NAME_SEP=	"#";		//Seperator für Knotennamen (Knotentyp#Knotenname)
//	private static final String KW_PATH_SEP=	"/";		//Seperaor für Korpuspfade	
//	private static final String KW_TYPE_DNDOC=	"doc";		//Knotentypname für Dokumentknoten
//	private static final String KW_TYPE_DNCOL=	"col";		//Knotentypname für Collectionknoten
//	
//	//passt hier nicht so gut hin
//	private static final String KW_ANNO_VALUE=	"value";			//Schlüsselwort unter dem das PAULA-Attribut Value als Annotation gespeichert werden soll
//	private static final String KW_ANNO_TAR=	"target";			//Schlüsselwort unter dem das PAULA-Attribut Target als Annotation gespeichert werden soll
//	private static final String KW_ANNO_DESC=	"description";		//Schlüsselwort unter dem das PAULA-Attribut Target als Annotation gespeichert werden soll
//	private static final String KW_ANNO_EXP=	"example";			//Schlüsselwort unter dem das PAULA-Attribut Target als Annotation gespeichert werden soll
//	private static final String KW_REL_TYPE_NAME= "RELATION_TYPE";	//Schlüsselwort unter der die Annotation für relStructDN gespeichert
//	private static final String KW_TYPE_FILE=	"FILE";				//Schlüsselwort unter für den Typ "Datei" einer Collection				
//	
//	//einige Statistik-Counter
//	private long SC_SDN= 0;						//Statistik-Counter für StructDN
//	private long SC_SDN_REF_EDGE= 0;			//Statistik-Counter für Kanten vone einem StructDN zu den Referenzknoten
//	private long SC_SEDN= 0;					//Statistik-Counter für StructEdgeDN
//	private long SC_SEDN_REF_EDGE= 0;			//Statistik-Counter für Kanten vone einem StructEdgeDN zu den Referenzknoten
//	
//	private KorpusGraphMgr kGraphMgr= null;			//interner Korpusgraph, in den die Knoten eingefügt werden
//	
//	private Long annoNameExt= STD_ANNO_NAME_EXT;			//Namenszusatz für Annotationsknoten, da diese meist keine ID besitzen
//	private Long cadNameExt=  STD_CAD_NAME_EXT;				//Namenszusatz für ComplexAnnotationsknoten, da diese meist keine ID besitzen
//	private Long colAnnoNameExt= STD_COLANNO_NAME_EXT;		//Namenszusatz für CollectionAnnotationsknoten, da diese meist keine ID besitzen
//	private ReaderInfo currReaderInfo= null;				//aktuell benutzter PAULA-Reader, wird gespeichert um Events entgegen zu nehmen
//	private Vector<ReaderInfo> readerInfoList= null;		//Liste aller benutzbaren PAULAReader
//	private Vector<TempTokDN> TempTokDNList= null;			//Liste, die die Daten für die TokenDN zwischenspeichert
//	private Vector<TempStructEdgeDN> TempSEDNList= null;	//Liste, die die Daten für die StructEdgeDN zwischenspeichert (rel-Elemente)
//	private Vector<TempStructEdgeDN2> TempSEDN2List= null;	//Liste, die die Daten für die StructEdgeDN zwischenspeichert (struct-Elemente)
//	private Vector<String> TempSERefDN=	null;				//Liste mit Namen von StructEdgeNodes (rel-Elemente) die Zwischengespeichert werden
//	private CollectionDN currFileColDN= null;				//aktueller Collectionknoten, der ein PAULA-Dokument darstellt
//	
//	// ----------------------------- StructEdge -----------------------------
//	/**
//	 * Diese Tabelle speichert alle StructEdge-Objekte, die von der Methode 
//	 * structEdgeDataConnector() empfangen werden. Die eintzelnen Attribue werden in
//	 * einem TmpStruceEdgeDN zwischengespeichert. Eine Liste mehrerer solcher Objekte wird
//	 * dann einem paula::const-Elementknoten zugeordnet.
//	 * Diese Tabelle hält besitzt eine Reihenfolge und merkt sich in welcher Abfolge
//	 * die Elemente eingefügt wurden.
//	 */
//	Map<String, Vector<TmpStructEdgeDN>> tmpSETable = null;
//	Collection<String> tmpStructIDList= null;
//	Collection<String> seenStructIDs= null;
//	
//	
//	/**
//	 * Zuordnung von Collectionnamen nach der PAULA-Notation und Collectionnamen des Korpusgraphen.
//	 * Tabelle: PAULA-Namen : Korpusgraphnamen
//	 */
//	private Hashtable<String, String> colNamingTable= null;	
//	//	 *************************************** Meldungen ***************************************
//	private static final String MSG_STD=			TOOLNAME + ">\t";
//	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
//	private static final String MSG_OK=					"OK";
//	private static final String MSG_CREATE_INTGRAPH=	"create internal graph model...";
//	private static final String MSG_READ_PFILES=		"reading paula files..."; 
//	private static final String MSG_CREATE_KOPRPP=		"creating korpus pre- and post-order...";
//	private static final String MSG_PREPARE_GRAPH=		"preparing internal korpus graph for inserting nodes...";
//	private static final String MSG_DOT_WRITING=		"writing korpus graph to dot file...";
//	private static final String MSG_CLOSING_GRAPH=		"closing korpus graph...";
//	//	 *************************************** Fehlermeldungen ***************************************
//	private static final String ERR_NOT_IMPLEMENTED=		MSG_ERR + "This methode is not yet been implemented.";
//	private static final String ERR_NO_PDFILE=				MSG_ERR + "The given pdFile does not exist. This might be an internal error. Not existing file: ";
//	private static final String ERR_NO_TYPE_FILE=			MSG_ERR + "There is no type file for korpus in the following folder. You have to analyze the korpus first by PAULAAnalyzer. Untyped korpus: ";
//	private static final String ERR_TYPE_NOT_SUPPORTED=		MSG_ERR + "Sorry the given analyze type is not yet supported in "+ TOOLNAME+ " v" +VERSION+". Analyze type: ";
//	private static final String ERR_XPTR_NOT_A_TEXT=		MSG_ERR + "An XPointer of the parsed document does not refer to a xml-textelement. Incorrect pointer: ";
//	private static final String ERR_TOO_MANY_REFS=			MSG_ERR + "There are too many references for a token node element: ";
//	private static final String ERR_WRONG_LEFT_RIGHT=		MSG_ERR + "The left or right border is not set correctly of XPointer: ";
//	private static final String ERR_NO_PRIMDN=				MSG_ERR + "No primary data node found for token element: ";
//	private static final String ERR_WRONG_REF_KIND_ELEM=	MSG_ERR + "The XPointer references in current file are incorrect. There only have to be element pointers and the following is not one of them: ";
//	private static final String ERR_CANNOT_FIND_REFNODE=	MSG_ERR + "Connot find a node with the following name: ";
//	private static final String ERR_CANNOT_FIND_REFEDGE=	MSG_ERR + "Connot find an edge with the following name: ";
//	private static final String ERR_INCORRECT_ANNO=			MSG_ERR + "Can not work with the given annotation, because the type-value or the value-value is empty.";
//	private static final String ERR_NODENAME_NOT_IN_GRAPH=	MSG_ERR + "The given rel-Node does not exist in Graph.";
//	private static final String ERR_CYCLE_IN_SE_DOC=		MSG_ERR + "Cannot import the data from document, because there is a cycle in it. Cycle list: ";
//	private static final String ERR_FCT_DEPRECATED=			MSG_ERR + "This method isn´t supported, it is deprecated.";
//	private static final String ERR_CAD_NO_SRC=				MSG_ERR + "There is no source Href given in methode: ";
//	private static final String ERR_META_STRUCT_FILE=		MSG_ERR + "This corpus contains two meta-struct-data files (anno.xml).";
//	private static final String ERR_METASTRUCT_FILE=		MSG_ERR + "There is an error in the mta-struct-document. One link can reference only one Element or a sequence of elements: ";
//	private static final String ERR_ID_NOT_IN_NTABLE=		MSG_ERR + "The given reference cannot be explored, there´s an error in document: ";
//	private static final String ERR_XPTR_NO_ELEMENT=		MSG_ERR + "The given reference is not an element or an element-range pointer: ";
//	private static final String ERR_NO_RELS=				MSG_ERR + "There´s an error in parsed document. The following struct node has no rel-node: ";
//	private static final String ERR_STRUCTID_NOT_EXIST=		MSG_ERR + "There´s an error in parsed document. The following struct-id wich is referenced does not exists: ";
//	private static final String ERR_NULL_STRUCTEDGE=		MSG_ERR + "The searched edge does not exist in internal table: ";
//	//	 ============================================== Konstruktoren ==============================================
//	/**
//	 * Initialisiert ein Mapper Objekt und setzt den logger zur Nachrichtenausgabe.
//	 * @param logger Logger - Logger zur Nachrichtenausgabe
//	 */
//	public MapperV1(Logger logger) throws Exception
//	{
//		super(logger);
//		fillReaderInfoList();
//		TempTokDNList= new Vector<TempTokDN>();
//	}
////	 ============================================== private Methoden ==============================================
//	/**
//	 * Erstellt eine Liste mit Informationen zu allen Verfügbaren PAULAReadern.
//	 * Diese Liste ist this.readerInfoList.
//	 */
//	private void fillReaderInfoList() throws Exception
//	{
//		readerInfoList= new Vector<ReaderInfo>();
//		ReaderInfo readerInfo= null;
//		
//		//PrimDataReader(Name, CType, ClassName, Priorität)
//		readerInfo= new ReaderInfo("PrimDataReader", "PrimData", "paulaReader_1_0.reader.PrimDataReader", 1.0);
//		this.readerInfoList.add(readerInfo);
//		
//		//TokDataReader(Name, CType, ClassName, Priorität)
//		readerInfo= new ReaderInfo("TokDataReader", "TokData", "paulaReader_1_0.reader.TokDataReader", 1.0);
//		this.readerInfoList.add(readerInfo);
//		
//		//StructDataReader(Name, CType, ClassName, Priorität)
//		readerInfo= new ReaderInfo("StructDataReader", "StructData", "paulaReader_1_0.reader.StructDataReader", 1.0);
//		this.readerInfoList.add(readerInfo);
//		
//		//StructEdgeDataReader(Name, CType, ClassName, Priorität)
//		readerInfo= new ReaderInfo("StructEdgeDataReader", "StructEdgeData", "paulaReader_1_0.reader.StructEdgeDataReader", 1.0);
//		this.readerInfoList.add(readerInfo);
//		
//		//AnnoDataReader(Name, CType, ClassName, Priorität)
//		readerInfo= new ReaderInfo("AnnoDataReader", "AnnoData", "paulaReader_1_0.reader.AnnoDataReader", 1.0);
//		this.readerInfoList.add(readerInfo);	
//		
//		//ComplexAnnoDataReader(Name, CType, ClassName, Priorität)
//		readerInfo= new ReaderInfo("ComplexAnnoDataReader", "ComplexAnnoData", "paulaReader_1_0.reader.ComplexAnnoDataReader", 1.0);
//		this.readerInfoList.add(readerInfo);
//		
//		//MetaStructDataReader(Name, CType, ClassName, Priorität)
//		readerInfo= new ReaderInfo("MetaStructDataReader", "MetaStructData", "paulaReader_1_0.reader.MetaStructDataReader", 1.0);
//		this.readerInfoList.add(readerInfo);
//		
//		//MetaAnnoDataReader(Name, CType, ClassName, Priorität)
//		readerInfo= new ReaderInfo("MetaAnnoDataReader", "MetaAnnoData", "paulaReader_1_0.reader.MetaAnnoDataReader", 1.0);
//		this.readerInfoList.add(readerInfo);
//	}
//	
//	
//	/**
//	 * Diese Methode sucht alle Knoten aus dem internen Graphen, auf die der hier 
//	 * übergebene XPointer verweist zurück.
//	 * @param korpusPath String - Der aktuelle KorpusPfad, indem sich die Knoten befinden
//	 * @param xmlBase String - Das XML-Basisdokuments des XPointers
//	 * @param href String - der eigentliche XPointer
//	 * @return alle Knoten, auf die dieser XPointer verweist 
//	 */
//	private Vector<TextedDN> extractXPtr(	String korpusPath, 
//											String xmlBase, 
//											String href) throws Exception
//	{
//		//Objekt zum Interpretieren des XLinks in mark.href initialisieren
//		XPtrInterpreter xPtrInter= new XPtrInterpreter();
//		xPtrInter.setInterpreter(xmlBase, href);
//		//gehe durch alle Knoten, auf die sich dieses Element bezieht
//		Vector<XPtrRef> xPtrRefs=  xPtrInter.getResult();
//		Vector<TextedDN> refNodes= new Vector<TextedDN>();
//		for (XPtrRef xPtrRef: xPtrRefs)
//		{
//			//Fehler, wenn XPointer-Reference vom falschen Typ
//			if (xPtrRef.getType()!= XPtrRef.POINTERTYPE.ELEMENT)
//				throw new Exception(ERR_WRONG_REF_KIND_ELEM + href);
//			
//			//wenn XPointer-Bezugsknoten einen Bereich umfasst
//			if (xPtrRef.isRange())
//			{
//				//erzeuge den Namen des linken Bezugsknotens
//				String leftName= korpusPath + KW_NAME_SEP + xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getLeft();
//				//erzeuge den Namen des rechten Bezugsknotens
//				String rightName= korpusPath + KW_NAME_SEP + xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getRight();
//				String typeName= this.kGraphMgr.getDNType(this.kGraphMgr.getDN(leftName));
//				for (IKMAbstractDN absDN: this.kGraphMgr.getDNRangeByType(typeName, leftName, rightName))
//					refNodes.add((TextedDN) absDN); 
//			}
//			//wenn XPointer-Bezugsknoten einen einzelnen Knoten referenziert
//			else
//			{
//				//erzeuge den Namen des Bezugsknotens
//				String nodeName= korpusPath + KW_NAME_SEP + xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getID();
//				TextedDN refNode= (TextedDN)this.kGraphMgr.getDN(nodeName);
//				if (refNode == null) throw new Exception(ERR_CANNOT_FIND_REFNODE + nodeName);
//				refNodes.add(refNode);
//			}
//		}
//		return(refNodes);
//	}
//	
//	private Collection<IKMAbstractEdge> extractXPtrAsEdge(	String korpusPath, 
//															String xmlBase, 
//															String href) throws Exception
//	{
//		//Objekt zum Interpretieren des XLinks in mark.href initialisieren
//		XPtrInterpreter xPtrInter= new XPtrInterpreter();
//		xPtrInter.setInterpreter(xmlBase, href);
//		//gehe durch alle Knoten, auf die sich dieses Element bezieht
//		Vector<XPtrRef> xPtrRefs=  xPtrInter.getResult();
//		Vector<IKMAbstractEdge> refEdges= new Vector<IKMAbstractEdge>();
//		for (XPtrRef xPtrRef: xPtrRefs)
//		{
//			//Fehler, wenn XPointer-Reference vom falschen Typ
//			if (xPtrRef.getType()!= XPtrRef.POINTERTYPE.ELEMENT)
//				throw new Exception(ERR_WRONG_REF_KIND_ELEM + href);
//			
//			//wenn XPointer-Bezugsknoten einen Bereich umfasst
//			if (xPtrRef.isRange())
//			{
//				//TODO es muss ein Index angelegt werden um Bereiche von Kanten zu ermitteln
//				throw new Exception(ERR_NOT_IMPLEMENTED);
//				/*
//				//erzeuge den Namen des linken Bezugsknotens
//				String leftName= korpusPath + KW_NAME_SEP + xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getLeft();
//				//erzeuge den Namen des rechten Bezugsknotens
//				String rightName= korpusPath + KW_NAME_SEP + xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getRight();
//				String typeName= this.kGraphMgr.getDNType(this.kGraphMgr.getDN(leftName));
//				for (IKMAbstractDN absDN: this.kGraphMgr.getDNRangeByType(typeName, leftName, rightName))
//					refNodes.add((TextedDN) absDN); 
//					*/
//			}
//			//wenn XPointer-Bezugsknoten einen einzelnen Knoten referenziert
//			else
//			{
//				//System.out.println("alle Kanten: "+ this.kGraphMgr.getEdges());
//				//erzeuge den Namen des Bezugsknotens
//				String edgeName= korpusPath + KW_NAME_SEP + xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getID();
//				IKMAbstractEdge refEdge= this.kGraphMgr.getEdge(edgeName);
//				if (refEdge == null) throw new Exception(ERR_CANNOT_FIND_REFEDGE + edgeName);
//				refEdges.add(refEdge);
//			}
//		}
//		return(refEdges);	
//	}
//	
//	/**
//	 * Nimmt alle Resettings vor, die beim parsen einer neuen PAULA-Datei erledigt
//	 * werden müssen. Einige Werte werden auf ihren Standardwert zurückgesetzt.
//	 */
//	private void resetEveryPAULAFile()
//	{
//		//Statistikcounter initialisiseren
//		this.SC_SDN= 0;
//		this.SC_SDN_REF_EDGE= 0;
//		this.SC_SEDN= 0;
//		this.SC_SEDN_REF_EDGE= 0;
//		
//		this.annoNameExt= STD_ANNO_NAME_EXT;
//		this.cadNameExt= STD_CAD_NAME_EXT;
//		TempTokDNList= new Vector<TempTokDN>();
//	}
//	
////	 ============================================== protected Methoden ==============================================
//	/**
//	 * Liest aus einer XML-Datei die Informationen aus, die die Analyse eines 
//	 * PAULA-Verzeichnisses ergab. Aus diesen Informationen wird ein KSDesc
//	 * Objekt erzeugt und zurückgegeben. Dieses Objekt kann dann im Mapping-Schritt
//	 * benutzt werden.
//	 * @param typedFile File - Name der XML-Datei
//	 * @throws Exception
//	 */
//	protected KSDesc createKSDesc(File typedFile) throws Exception
//	{
//		if (!typedFile.exists()) throw new Exception(ERR_NO_PDFILE + typedFile.getName());
//		
//		KSDescReader ksDescReader= new KSDescReader();
//		
//		SAXParser parser;
//        XMLReader xmlReader;
//        
//        final SAXParserFactory factory= SAXParserFactory.newInstance();
//        parser= factory.newSAXParser();
//        xmlReader= parser.getXMLReader();
//
//        //contentHandler erzeugen und setzen
//        xmlReader.setContentHandler(ksDescReader);
//        //LexicalHandler setzen, damit DTD ausgelsen werden kann
//        xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", ksDescReader);
//		xmlReader.parse(typedFile.getCanonicalPath());
//		return(ksDescReader.getRoot());
//	}
//	
//	/**
//	 * Traversiert die Struktur eines gegebenen KSDesc-Objektes rekursiv.
//	 * @param ksDesc KSDesc - KSDesc-Objekt von dem an traversiert werden soll
//	 * @param father KSDesc - KSDesc-Objekt das den superkorpus des ksDesc-Objektes darstellt
//	 * @param korpusPath String - Speichert beim Traversieren den durchwanderten Pfad durch die verschiedenen Korpora (/SuperKorpus/Subkorpus/...) 
//	 */
//	protected void traverseKS(	KSDesc ksDesc, 
//								KSDesc father,
//								String korpusPath) throws Exception
//	{
//		if (DEBUG_KSDESC)
//			System.out.println(MSG_STD + "old korpus path: "+ korpusPath);
//		this.createKorpNode(korpusPath, ksDesc.getName());
//		//korpusPath erweitern
//		//wenn korpusPath!= null, dann erweitern 
//		if (korpusPath!= null)
//			korpusPath= korpusPath + KW_PATH_SEP + ksDesc.getName();
//		//wenn korpusPath== null, dann initialisieren
//		else
//			korpusPath= ksDesc.getName();
//		if (DEBUG_KSDESC)
//			System.out.println(MSG_STD + "new korpus path: "+ korpusPath);
//		// wenn es Subkorpora gibt
//		if ((ksDesc.getKSList()!= null) && (!ksDesc.getKSList().isEmpty()))
//		{
//			for (KSDesc subKSDesc: ksDesc.getKSList())
//				this.traverseKS(subKSDesc, ksDesc, korpusPath);
//		}
//		
//		//wenn es Dokumente in diesem Korpus gibt
//		if ((ksDesc.getPDDescList()!= null) && (!ksDesc.getPDDescList().isEmpty()))
//		{
//			//Annotationsstrukturdatei
//			PDDesc metaStructDataFile= null;
//			//Liste der Annotationsdateien
//			Vector<PDDesc> metaAnnoDataFiles= new Vector<PDDesc>();
//			//Liste der Annotationsdateien
//			Vector<PDDesc> annoDataFiles= new Vector<PDDesc>();
//			//gehe durch alle PAULA-Dateien und sortiere diese nach MetaStructData-, MetaAnnoData- und AnnoData-Dateien
//			for(PDDesc pdDesc: ksDesc.getPDDescList())
//			{
//				//Annotationsstrukturdatei ermitteln
//				if (pdDesc.getPDType().equalsIgnoreCase(KW_CTYPE_METASTRUCTDATA))
//				{
//					if (metaStructDataFile!= null) throw new Exception(ERR_META_STRUCT_FILE);
//					else metaStructDataFile= pdDesc;
//				}
//				//Metaannotationen ermitteln
//				else if (pdDesc.getPDType().equalsIgnoreCase(KW_CTYPE_METAANNODATA))
//					metaAnnoDataFiles.add(pdDesc);
//				//einfache Annotationsdateien
//				else annoDataFiles.add(pdDesc);
//			}
//			
//			//Annotationsstruktur-Datei einlesen
//			if (metaStructDataFile!= null)
//			{
//				this.resetEveryPAULAFile();
//				colNamingTable= new Hashtable<String, String>();
//				//starte spezifischen Reader
//				if (this.logger!= null) this.logger.info("reading annotation structure file: "+metaStructDataFile.getFileName());
//				this.parsePAULAFile(metaStructDataFile, korpusPath);
//			}
//			
//			//Metaannotationsdateien einlesen
//			if (this.logger!= null) this.logger.info("reading meta annotation files:");	
//			for(PDDesc pdDesc: metaAnnoDataFiles)
//			{
//				if (this.logger!= null) this.logger.info("reading paula file '"+pdDesc.getFile().getCanonicalPath()+"'...");
//				this.resetEveryPAULAFile();
//				//starte spezifischen Reader
//				this.parsePAULAFile(pdDesc, korpusPath);
//			}
//			
//			//Annotationsdateien einlesen
//			if (this.logger!= null) this.logger.info("reading annotation files:");
//			for(PDDesc pdDesc: annoDataFiles)
//			{
//				if (this.logger!= null) this.logger.info("reading paula file '"+pdDesc.getFile().getCanonicalPath()+"'...");
//				//eindeutigen Namen für die Collection erzeugen
//				String uniqueName= korpusPath + KW_PATH_SEP + pdDesc.getFile().getName() + KW_NAME_SEP + KW_TYPE_DNCOL;
//				//einen Collection Knoten für diese Datei erzeugen
//				CollectionDN colDN= (CollectionDN) this.kGraphMgr.getDN(uniqueName);
//				//wenn übergeordnete Collection noch nicht existiert, dann erzeugen
//				if (colDN== null)
//				{
//					colDN= new CollectionDN(uniqueName, KW_TYPE_FILE, pdDesc.getFile().getName());
//					this.kGraphMgr.addCollectionDN(colDN, null, this.kGraphMgr.getCurrKorpDN());
//				}
//				if (DEBUG_COLLECTION_DN) 
//					System.out.println("created collection node with name: "+ uniqueName);
//				//erzeugten Knoten als aktuellen setzen
//				this.currFileColDN= colDN; 
//				
//				//nimmt ein paar resettings vor, die für jede neue Datei zurückgesetzt werden müssen
//				this.resetEveryPAULAFile();
//				//starte spezifischen Reader
//				this.parsePAULAFile(pdDesc, korpusPath);
//				if (this.logger!= null) this.logger.info("OK");
//			}
//		}
//		this.leaveKorpDN();
//	}
//	
//	/**
//	 * Parst ein übergebenes PAULA-Dokument mit einem dafür vorgesehenen Reader. Dieser 
//	 * wird mit der Methode searchPAULAReader() ermittelt.
//	 * @param pdDesc PDDesc - Beschreibungsobjekt für das aktuelle Dokument
//	 * @param korpusPath String - Pfad durch den KOrpus in dem das aktuelle Dokument liegt
//	 * @throws Exception
//	 */
//	protected void parsePAULAFile(	PDDesc pdDesc,
//									String korpusPath) throws Exception
//	{
//		//suche spezifischen Reader
//		PAULAReader paulaReader= this.searchPAULAReader(pdDesc, korpusPath);
//		//prüfe ob ein Reader gefunden wurde
//		if (paulaReader== null)
//			throw new Exception(ERR_TYPE_NOT_SUPPORTED + pdDesc.getPDType());
//		if (this.logger!= null) this.logger.debug(MSG_STD+ " parsing paula document: " 
//				+ pdDesc.getFullFileName()
//				+" with specific paula reader: " 
//				+ paulaReader.getReaderName() + " v" +paulaReader.getReaderVersion());
//		paulaReader.parse(pdDesc.getFile());
//	}
//	
//	/**
//	 * Sucht einen zu dem Paula-Dokument-Type (PDDesc.getPDType()) passenden Reader und 
//	 * gibt diesen zurück.
//	 * @param pdDesc PDDesc - Beschreibungsobjekt für das aktuelle Dokument
//	 * @param korpusPath String - Pfad durch den KOrpus in dem das aktuelle Dokument liegt
//	 * @return zu dem Dokument Typ passender Reader
//	 * @throws Exception
//	 */
//	protected PAULAReader searchPAULAReader(	PDDesc pdDesc,
//												String korpusPath) throws Exception
//	{
//		this.currReaderInfo= null;
//		Double prio= null;
//		for (ReaderInfo readerInfo : this.readerInfoList)
//		{
//			//wenn passender Reader gefunden wurde
//			if (pdDesc.getPDType().equalsIgnoreCase(readerInfo.readerCType))
//			{	
//				//wenn die Priorität besser ist als die bereits gefundene, sofern eine gefunden wurde
//				//prio ist besser, wenn sie kleiner ist
//				if ((prio == null) || ((prio!= null) && (prio > readerInfo.priority)))
//				{
//					this.currReaderInfo= readerInfo;
//					prio= readerInfo.priority;
//				}
//			}	
//		}
//		//prüfe ob ein Reader gefunden wurde
//		if (this.currReaderInfo== null)
//			throw new Exception(ERR_TYPE_NOT_SUPPORTED + pdDesc.getPDType());
//		return(this.currReaderInfo.getReader(this, korpusPath, pdDesc.getFile(), this.logger));
//	}
////	 ============================================== öffentliche Methoden ==============================================
//	/**
//	 * Bildet das PAULA 1.0 Datenmodell auf das relANNIS 2.0 Modell ab. Dabei werden die
//	 * Packages paulaReader_1_0 und relANNIS_2_0 verwendet. Benötigt wird das analysierte
//	 * Quellkorpus srcFolder und ein Verzeichnis, in das die erstellten Dateien geschrieben
//	 * werden können.
//	 * @param srcFolder File - Quellverzeichnis, aus dem das zu mappende Korpus stammt
//	 * @param tmpFolder File - temporäres Verzeichnis zum Zwischenspeichern
//	 */
//	public void map(File srcFolder, File tmpFolder) throws Exception
//	{ 
//		//Korpus-Typ-Datei erzeugen
//		File typedFile= new File(srcFolder.getCanonicalPath() + "/" + FILE_TYPED_KORP);
//		// es existiert keine typ-Datei des Korpus
//		if (!typedFile.exists()) throw new Exception(ERR_NO_TYPE_FILE + srcFolder.getCanonicalPath());
//		// Liste der einzulesenden Dateien erstellen
//		KSDesc ksDesc= this.createKSDesc(typedFile);
//		
//		if (this.logger!= null) this.logger.info(MSG_CREATE_INTGRAPH);
//		this.kGraphMgr= new KorpusGraphMgr(this.logger);
//		if (this.logger!= null) this.logger.info(MSG_OK);
//		
//		//bereitet den KorpusGraphMgr zum Einfügen von Daten vor 
//		if (this.logger!= null) this.logger.info(MSG_PREPARE_GRAPH);
//		this.kGraphMgr.start();
//		if (this.logger!= null) this.logger.info(MSG_OK);
//		
//		//PAULA-Dateien einlesen
//		if (this.logger!= null) this.logger.info(MSG_READ_PFILES);
//		this.traverseKS(ksDesc, null, null);
//		if (this.logger!= null) this.logger.info(MSG_OK);
//				
//		if (this.logger!= null) this.logger.info(MSG_CREATE_KOPRPP);
//		//Pre und Post-order für die Korpusebene generieren
//		this.kGraphMgr.computeKorpPPOrder();
//		if (this.logger!= null) this.logger.info(MSG_OK);
//		
//		//schließe den KorpusGraphen
//		if (this.logger!= null) this.logger.info(MSG_CLOSING_GRAPH);
//		this.kGraphMgr.close();
//		if (this.logger!= null) this.logger.info(MSG_OK);
//		
//		//Ausgabe des erzeugten Korpusgraphen
//		if (this.logger!= null) this.logger.info(MSG_DOT_WRITING);
//		this.kGraphMgr.printGraph(tmpFolder.getCanonicalPath() + "/kGraph");
//		if (this.logger!= null) this.logger.info(MSG_OK);
//	}
//	
//// ------------------------------ Methoden der Middleware ------------------------------
//	/**
//	 * Fügt einen Korpusknoten in den Korpusgraphen ein.
//	 * @param korpusPath String - Pfad durch den KOrpus in dem das aktuelle Dokument liegt
//	 * @param name String - Name des einzufügenden Korpusknotens
//	 */
//	public void createKorpNode(	String korpusPath,
//								String name) throws Exception
//	{
//		KorpDN korpDN= null;
//		if (korpusPath!= null)
//			korpDN= new KorpDN(korpusPath + KW_PATH_SEP + name, name);
//		else 
//			korpDN= new KorpDN(name, name);
//		this.kGraphMgr.addKorpDN(korpDN);
//	}
//	
//	/**
//	 * Setzt den aktuellen Korpus auf den Vorgänger zurück und liefert den bisher
//	 * aktuellen Korpus Knoten zurück.
//	 * @return den bisher aktuellen Korpusknoten
//	 * @throws Exception
//	 */
//	public KorpDN leaveKorpDN() throws Exception
//	{
//		KorpDN korpDN= (KorpDN)this.kGraphMgr.leaveKorpDN(); 
//		this.kGraphMgr.finishKorpus(korpDN);
//		return(korpDN);
//	}
//	// ------------------------------ Methoden aus dem PAULAMapperInterface------------------------------
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
//	 * Ereigniss wird aufgerufen, wenn ein neuer Korpus bzw. Subkorpus aus dem PAULA-
//	 * Datenmodell resultiert.
//	 * @param corpusPath String - der Pfad der bisherigen Korpora, wenn der neu zu erstellende Korpus ein Subkorpus ist
//	 * @param corpusName String - Name des neu zu erzeugenden Korpus-Objekt
//	 */
//	public void corpusDataConnector(	String corpusPath,
//										String corpusName) throws Exception
//	{
//		throw new Exception(ERR_NOT_IMPLEMENTED);
//	}
//	
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
//	 * Ereigniss wird aufgerufen, wenn ein neues Dokument aus dem PAULA-
//	 * Datenmodell resultiert.
//	 * @param corpusPath String - der Pfad der bisherigen Korpora
//	 * @param corpusName String - Name des neu zu erzeugenden Dokument-Objekt
//	 * @throws Exception
//	 */
//	public void documentDataConnector(	String corpusPath,
//										String docName) throws Exception
//	{
//		throw new Exception(ERR_NOT_IMPLEMENTED);
//	}
//	
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation eines MetaStructDataReaders mit einer
//	 * dieses Interface implementierenden Mapperklasse. Dabeio werden alle Daten zu einem
//	 * gelesenen Textelementes an die Mapper-Klasse übergeben. 
//	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
//	 * @param paulaFile File - geparste PAULA-Datei
//	 * @param paulaId String - PAULA-ID des Textelementes
//	 * @param slType String - Der Typ der TructList in diesem Document
//	 * @param structID String - ID der übergeordneten Struktur des rel-Elementes (struct-Element)
//	 * @parem relID String - ID dieses rel-Elementes
//	 * @param href String -	Verweisziel dieses rel-Elementes
//	 */
//	public void metaStructDataConnector(	String korpusPath,
//											File paulaFile,
//											String paulaId,
//											String slType,
//											String structID,
//											String relID,
//											String href) throws Exception
//	{
//		if (DEBUG_METASTRUCT_DATA)
//			System.out.println(	MSG_STD +"metaStructDataConnector with data:\t"+
//								"\tkorpusPath: "+ korpusPath+ ", paulaFile: "+ paulaFile.getName()+
//								", paulaID: " + paulaId + ", slType: " + slType +
//								", structID: " + structID + ", relID: "+ relID+
//								", href: "+ href);
//		
//		//alle XPointer aus dem Href extrahieren, ein XPtr darf nur auf ein oder meherer Documente zeigen
//		XPtrInterpreter xPtrInterpreter= new XPtrInterpreter();
//		xPtrInterpreter.setXPtr(href);
//		Vector<XPtrRef> xPtrRefs= xPtrInterpreter.getResult();
//		Vector<CollectionDN> colDNs= new Vector<CollectionDN>();
//		//alle einzelnen Referenzen ermitteln und Collection zu jeder erstellen
//		for (XPtrRef xPtrRef: xPtrRefs)
//		{
//			//Fehler, wenn XPtr kein einfaches Element
//			if (!xPtrRef.getType().equals(XPtrRef.POINTERTYPE.XMLFILE))
//				throw new Exception(ERR_METASTRUCT_FILE + href);
//			if (DEBUG_METASTRUCT_DATA) System.out.println("referenced document in meta-struct-file: "+xPtrRef.getID());
//			String uniqueName= korpusPath + KW_PATH_SEP + xPtrRef.getID() + KW_NAME_SEP + KW_TYPE_DNCOL;
//			CollectionDN colDN= (CollectionDN) this.kGraphMgr.getDN(uniqueName);
//			//wenn eine solche Collection noch nicht existiert, dann erzeugen
//			if (colDN== null)
//			{
//				colDN= new CollectionDN(uniqueName, KW_TYPE_FILE, xPtrRef.getID());
//				//Knoten in den Graphen einfügen
//				this.kGraphMgr.addCollectionDN(colDN, null, this.kGraphMgr.getCurrKorpDN());
//			}
//			//Knoten in Zuordnungstabelle eintragen
//			this.colNamingTable.put(relID, uniqueName);
//			colDNs.add(colDN);
//		}
//		
//		//Collectionknoten in übergeordneten Knoten einfügen
//		//Name des übergeordneten Knoten
//		String uniqueName= korpusPath + KW_PATH_SEP + structID + KW_NAME_SEP + KW_TYPE_DNCOL;
//		CollectionDN colDN1= (CollectionDN) this.kGraphMgr.getDN(uniqueName);
//		//wenn übergeordnete Collection noch nicht existiert, dann erzeugen
//		if (colDN1== null)
//		{
//			colDN1= new CollectionDN(uniqueName, KW_TYPE_FILE, structID);
//			this.kGraphMgr.addCollectionDN(colDN1, null, this.kGraphMgr.getCurrKorpDN());
//			//Knoten in Zuordnungstabelle eintragen
//			this.colNamingTable.put(structID, uniqueName);
//			
//		}
//		//Kante vom übergeordneten Collectionknoten zu den einzelnen Collectionknoten ziehen
//		for (CollectionDN colDN2 :colDNs)
//		{
//			this.kGraphMgr.setDNToColDN(colDN2, colDN1);
//		}
//	}
//	
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation eines MetaAnnoDataReaders mit einer
//	 * dieses Interface implementierenden Mapperklasse. Dabeio werden alle Daten zu einem
//	 * gelesenen Textelementes an die Mapper-Klasse übergeben. 
//	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
//	 * @param paulaFile File - geparste PAULA-Datei
//	 * @param paulaId String - PAULA-ID des Textelementes
//	 * @param paulaType String - Der Typ der Meta-Annotationsdaten dieses Dokumentes
//	 * @param xmlBase String - Das Basisdokument für Verweisziele dieses Dokumentes 
//	 * @param featHref String -	Verweisziel eines feat-Elementes
//	 * @param featVal String Value-Wert eines feat-Elementes
//	 */
//	public void metaAnnoDataConnector(	String korpusPath,
//										File paulaFile,
//										String paulaId,
//										String paulaType,
//										String xmlBase,
//										String featHref,
//										String featVal) throws Exception
//	{
//		if (DEBUG_METAANNO_DATA)
//			System.out.println(	MSG_STD +"metaAnnoDataConnector with data:\t"+
//								"\tkorpusPath: "+ korpusPath+ ", paulaFile: "+ paulaFile.getName()+
//								", paulaID: " + paulaId + ", paulaType: " + paulaType +
//								", xmlBase: " + xmlBase + ", featHref: "+ featHref+
//								", featVal: "+ featVal);
//		XPtrInterpreter xPtrInterpreter= new XPtrInterpreter();
//		xPtrInterpreter.setInterpreter(xmlBase, featHref);
//		Vector<XPtrRef> xPtrRefs= xPtrInterpreter.getResult();
//		String colDNName= null;		//Name des Collectionknotens, auf den der ColAnnoKnoten verweist
//		CollectionDN colDN= null;	//Der Collectionknoten, auf den dieser ColAnnoDN verweist
//		//alle Verweisziele extrahieren
//		for (XPtrRef xPtrRef: xPtrRefs)
//		{
//			colDNName= this.colNamingTable.get(xPtrRef.getID()); 
//			if (DEBUG_METAANNO_DATA)
//				System.out.println(MSG_STD + "this node references to collection node: "+ colDNName);
//			//Fehler, wenn Name nicht in NameingTable vorhanden
//			if (colDNName== null)
//				throw new Exception(ERR_ID_NOT_IN_NTABLE + featHref);
//			//CollectionDN, auf den sich dieser Knoten bezieht ermitteln
//			colDN= (CollectionDN) this.kGraphMgr.getDN(colDNName);
//			//eindeutigen Namen für Knoten erzeugen
//			String uniqueName= korpusPath + KW_PATH_SEP + paulaType + KW_NAME_SEP+ colAnnoNameExt;
//			colAnnoNameExt++;
//			//Attribut-Wert-Paare (eigentliche ANnotation erzeugen)
//			Hashtable<String, String> attValPairs= new Hashtable<String, String>();
//			attValPairs.put(paulaType, featVal);
//			//MetaAnnotationsknoten erstellen  (eindeutiger Name für Graph, CollectionDN, Attribut-Wert-Paare)
//			ColAnnoDN colAnnoDN= new ColAnnoDN(uniqueName, colDN, attValPairs);
//			//colAnnoDN in Baum eintragen
//			this.kGraphMgr.addColAnnoDN(colAnnoDN, colDN, this.kGraphMgr.getCurrKorpDN());
//		}
//	}
//	
//	/**
//	 * Nimmt die Daten eines Readers für Primärdaten entgegen und verarbeitet sie indem
//	 * die entsprechenden Knoten in einem internen Graph erzeugt werden. Die übergebenen
//	 * Daten werden auf das relANNIS 2.0 Modell über das Package relANNIS_2_0 abgebildet.
//	 * Diese Methode erzeugt einen Dokument- und einen Primärdatenknoten.
//	 * @param paulaFile File - geparste PAULA-Datei
//	 * @param korpusPath String - Pfad durch den KOrpus in dem das aktuelle Dokument liegt
//	 * @param paulaId String - PAULA-ID des Textelementes
//	 * @param text String - Text des Textelementes
//	*/
//	public void primDataConnector(	String korpusPath, 
//									File paulaFile,
//									String paulaId, 
//									String text) throws Exception
//	{
//		//Dokumentknoten für diesen Primärdatenknoten erzeugen und einfügen(Knotenname: korpusPath#doc#Knotenname)
//		DocDN docDN= new DocDN(paulaId, korpusPath + KW_NAME_SEP + KW_TYPE_DNDOC + KW_NAME_SEP + paulaFile.getName());
//		this.kGraphMgr.addDocDN(docDN);
//		
//		//Primärdatenknoten erzeugen 
//		PrimDN primDN= new PrimDN(korpusPath + KW_NAME_SEP + paulaFile.getName(), paulaId, text, this.currFileColDN);
//		//PrimDN-knoten in Graph einfügen (Kante zum DocDN und KorpDN wird automatisch erzeugt)
//		this.kGraphMgr.addPrimDN(primDN);
//		//Kante von PrimDN zu aktuellem CollectionDN erzeugen, wenn es einen gibt
//		if (this.currFileColDN!= null) this.kGraphMgr.setDNToColDN(primDN, this.currFileColDN);
//	}
//	
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation eines TokDataReaders mit einer
//	 * dieses Interface implementierenden Mapperklasse. Dabeio werden alle Daten zu einem
//	 * gelesenen Tokenelementes an die Mapper-Klasse übergeben. 
//	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
//	 * @param paulaId String - PAULA-ID dieses Tokenelementes
//	 * @param paulaType String - Paula-Typ dieses Tokenelementes
//	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Tokenelement bezieht
//	 * @param markID String - Mark-ID dieses Tokenelementes
//	 * @param hrefs Vector<String> - Bezugselement, auf die sich dieses Tokenelementes bezieht
//	 * @param markType String - Mark-Type dieses Tokenelementes
//	 */
//	public void tokDataConnector(	String korpusPath,
//									File paulaFile,
//									String paulaId, 
//									String paulaType,
//									String xmlBase,
//									String markID,
//									String href,
//									String markType) throws Exception
//	{
//		if (DEBUG_TOK_DATA)
//			System.out.println(	MSG_STD +"tokDataConnector with data:\t"+
//								"\tkorpusPath: "+ korpusPath+ ", paulaFile: "+ paulaFile.getName()+
//								", paulaID: " + paulaId + ", paulaType: " + paulaType +
//								", xmlBase: " + xmlBase + ", markID: "+ markID+
//								", href: "+ href + ", markType: "+ markType);
//		//Objekt zum Interpretieren des XLinks in mark.href initialisieren
//		XPtrInterpreter xPtrInter= new XPtrInterpreter();
//		xPtrInter.setInterpreter(xmlBase, href);
//		Vector<XPtrRef> xPtrRefs=  xPtrInter.getResult();
//		int runs= 0;
//		//suche den Primärdatenknoten zu diesem Tokendatenknoten
//		PrimDN primDN= null;
//		Long left= null;	//linke Textgrenze
//		Long right= null;	//rechte Textgrenze
//		for (XPtrRef xPtrRef: xPtrRefs)
//		{
//			runs++;
//			//Fehler wenn es mehr als eine Referenz gibt
//			if (runs > 1) throw new Exception(ERR_TOO_MANY_REFS + xPtrInter.getXPtr());
//			//Wenn Xpointer auf einen Text referenziert
//			else if (xPtrRef.getType()== XPtrRef.POINTERTYPE.TEXT)
//			{
//				String textNodeName= korpusPath +KW_NAME_SEP+ xPtrRef.getDoc();
//				primDN= (PrimDN)this.kGraphMgr.getDN(textNodeName);
//				try
//				{
//					left= new Long (xPtrRef.getLeft());
//					right= new Long (xPtrRef.getRight());
//					//linken und rechten Wert in korrrektes Format bringen
//					left= left-1;
//					right= left + right;
//				}
//				catch (Exception e)
//				{throw new Exception(ERR_WRONG_LEFT_RIGHT + xPtrInter.getXPtr());}
//			}
//			//Wenn XPointer nicht auf einen Text referenziert
//			else 
//				throw new Exception(ERR_XPTR_NOT_A_TEXT + "base: "+xPtrRef.getDoc() + ", element: " + xPtrInter.getXPtr() + ", type: "+ xPtrRef.getType());
//		}
//		//wenn kein Primärdatenknoten, dann Fehler
//		if (primDN == null) throw new Exception(ERR_NO_PRIMDN + paulaFile.getName() + KW_NAME_SEP + markID );
//		//tokDN erstellen
//		//Namen für den Knoten erstellen
//		String uniqueName= korpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + markID;
//		//Objekt erstellen zum Zwischenspeichern der Tokenknoten
//		TempTokDN TempTokDN= new TempTokDN(uniqueName, paulaType, markID, primDN, this.currFileColDN, left, right);
//		// und in die Liste eintragen
//		this.TempTokDNList.add(TempTokDN);
//	}
//	
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation eines StructDataReaders mit einer
//	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
//	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. 
//	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
//	 * @param paulaFile File - geparste PAULA-Datei
//	 * @param paulaId String - PAULA-ID dieses Strukturelementes
//	 * @param paulaType String - Paula-Typ dieses Strukturelementes
//	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Strukturelement bezieht
//	 * @param markID String - Mark-ID dieses Strukturelementes
//	 * @param href String - Bezugselement, auf die sich dieses Strukturelementes bezieht
//	 * @param markType String - Mark-Type dieses Strukturelementes
//	 */
//	public void structDataConnector(	String korpusPath,
//										File paulaFile,
//										String paulaId, 
//										String paulaType,
//										String xmlBase,
//										String markID,
//										String href,
//										String markType) throws Exception
//	{
//		if (DEBUG_STRUCT)
//			System.out.println(	MSG_STD +"structDataConnector with data:\n"+MSG_STD +
//								"\tkorpusPath: "+ korpusPath+ ", paulaFile: "+ paulaFile.getName()+
//								", paulaID: " + paulaId + ", paulaType: " + paulaType +
//								", xmlBase: " + xmlBase + ", markID: "+ markID+
//								", href: "+ href +", markType: "+ markType);
//		
//		//alle Knoten ermitteln, auf die diese Annotation verweist
//		Vector<TextedDN> refNodes= this.extractXPtr(korpusPath, xmlBase, href);
//		//Name des zu erzeugenden StructDN erstellen
//		String uniqueName= korpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + markID;	
//		//erzeuge Strukurknoten mit (eindeutiger Name für Graphen, Name für DB, aktuelle Collection, referenzierte Knoten)
//		StructDN structDN= new StructDN(uniqueName, markID, paulaType, this.currFileColDN, refNodes);
//		//Konvertieren der TextedDN in IKMAbstractDN
//		//Referenzknoten konvertieren zum einfügen in den Graphen
//		Vector<IKMAbstractDN> refAbsNodes= new Vector<IKMAbstractDN>();
//		for (TextedDN textedDN: refNodes)
//			refAbsNodes.add((IKMAbstractDN) textedDN);
//		//Knoten in Graphen einfügen
//		this.kGraphMgr.addStructDN(structDN, refAbsNodes);
//		//Kante von StructDN zu ColDN
//		if (this.currFileColDN!= null) this.kGraphMgr.setDNToColDN(structDN, this.currFileColDN);
//
//	}
//	
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation eines StructEdgeDataReaders mit einer
//	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
//	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. 
//	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
//	 * @param paulaFile File - geparste PAULA-Datei
//	 * @param paulaId String - PAULA-ID dieses Strukturelementes
//	 * @param paulaType String - Paula-Typ dieses Strukturelementes
//	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Strukturelement bezieht
//	 * @param strucID String - ID des StructListElementes dem dieses Struct-Element unterstellt ist
//	 * @param relID String - ID dieses Struct-Elementes
//	 * @param relHref String - Verweis auf untergeordnete Struktur- oder Tokenelemente 
//	 * @param relType String - Kantenannotation dieses Struct-Elementes
//	 * 
//	 * @param href String - Bezugselement, auf die sich dieses Strukturelementes bezieht
//	 * @param markType String - Mark-Type dieses Strukturelementes
//	 */
//	public void structEdgeDataConnector(	String 	korpusPath,
//											File 	paulaFile,
//											String 	paulaId, 
//											String 	paulaType,
//											String 	xmlBase,
//											String 	structID,
//											String	relID,
//											String	relHref,
//											String	relType) throws Exception
//	{
//		if (DEBUG_SE)
//			System.out.println(	MSG_STD +"structDataConnector with data:\n"+MSG_STD +
//								"\tkorpusPath: "+ korpusPath+ ", paulaFile: "+ paulaFile.getName()+
//								", paulaID: " + paulaId + ", paulaType: " + paulaType +
//								", xmlBase: " + xmlBase + ", structID: "+ structID+
//								", relID: "+ relID +", relHref: "+ relHref + ", relType: "+ relType);
//		
//		if(!this.tmpStructIDList.contains(structID)) 
//			this.tmpStructIDList.add(structID);
//		
//		//Objekt zum temporären Speichern erstellen
//		TmpStructEdgeDN tmpSEDN= new TmpStructEdgeDN(	korpusPath, paulaFile, paulaId, 
//														paulaType, xmlBase, structID, 
//														relID, relHref, relType);
//		Vector<TmpStructEdgeDN> seDNList= this.tmpSETable.get(structID);
//		if (seDNList== null)
//			seDNList= new Vector<TmpStructEdgeDN>();
//		seDNList.add(tmpSEDN);
//		this.tmpSETable.put(structID, seDNList);
//	}
//	
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation eines StructEdgeDataReaders mit einer
//	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
//	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. Diese Methode ist für
//	 * das verarbeiten von STRUCT-Elementen zuständig.
//	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
//	 * @param paulaFile File - geparste PAULA-Datei
//	 * @param paulaId String - PAULA-ID dieses Strukturelementes
//	 * @param paulaType String - Paula-Typ dieses Strukturelementes
//	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Strukturelement bezieht
//	 * @param structID String - ID dieses StructListElementes
//	 */
//	/*
//	public void structEdgeDataConnector1(	String 	korpusPath,
//											File 	paulaFile,
//											String 	paulaId, 
//											String 	paulaType,
//											String 	xmlBase,
//											String 	structID) throws Exception
//	{
//		//Knoten muss in Warteliste geschrieben werden und wird am Ende des Parsens der aktuellen Datei als Knoten in den KorpusGraph geschrieben
//		//wenn für den im Schritt zuvor gelesenen struct-Knoten bereits alle rel-Elemente im Graphen stehen, kann dieser eingefügt werden
//		//wenn es einen Knoten in der Warteliste gibt, dann dessen refNodes setzen
//		if (!this.TempSEDN2List.isEmpty())
//		{
//			//setze referenzknoten des letzten Elementes in der Liste
//			this.TempSEDN2List.lastElement().setRefNodes(this.TempSERefDN);
//			this.TempSERefDN= new Vector<String>();
//		}
//		//versuche den Knoten aus dem vorherigen Schritt dirket in den Graphen zu schreiben
//		//wenn es vorherigen Schritt gibt
//		if (!this.TempSEDN2List.isEmpty())
//		{
//			try
//			{
//				TempStructEdgeDN2 lastDN= this.TempSEDN2List.lastElement();
//				//versuche vorherigen Knoten in Korpusgraph zu schreiben
//				this.InsertStructEdge1(lastDN.korpusPath, lastDN.paulaFile, lastDN.paulaId, lastDN.paulaType, lastDN.xmlBase, lastDN.structID, lastDN.refNodes);
//				//wenn das Schreiben klappt, lösche vorherigen Knoten aus TempListe
//				this.TempSEDN2List.remove(lastDN);
//			}
//			catch (Exception e)
//			{}
//		}
//		//den aktuellen Knoten auf die Warteliste setzen
//		this.TempSEDN2List.add(new TempStructEdgeDN2(korpusPath, paulaFile, paulaId, paulaType, xmlBase, structID));
//	}*/
//	
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation eines StructEdgeDataReaders mit einer
//	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
//	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. Diese Methode ist für
//	 * das verarbeiten von REL-Elementen zuständig.
//	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
//	 * @param paulaFile File - geparste PAULA-Datei
//	 * @param paulaId String - PAULA-ID dieses Strukturelementes
//	 * @param paulaType String - Paula-Typ dieses Strukturelementes
//	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Strukturelement bezieht
//	 * @param strucID String - ID des StructListElementes dem dieses Struct-Element unterstellt ist
//	 * @param relID String - ID dieses Struct-Elementes
//	 * @param relHref String - Verweis auf untergeordnete Struktur- oder Tokenelemente 
//	 * @param relType String - Kantenannotation dieses Struct-Elementes
//	 * 
//	 * @param href String - Bezugselement, auf die sich dieses Strukturelementes bezieht
//	 * @param markType String - Mark-Type dieses Strukturelementes
//	 */
//	/*
//	public void structEdgeDataConnector2(	String 	korpusPath,
//											File 	paulaFile,
//											String 	paulaId, 
//											String 	paulaType,
//											String 	xmlBase,
//											String 	structID,
//											String	relID,
//											String	relHref,
//											String	relType) throws Exception
//	{
//		//erstelle Liste mit aktuellen Referenzknoten für die Methode structEdgeDataConnector1
//		//Name des zu erzeugenden StructDN erstellen
//		String uniqueName= korpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + relID;
//		this.TempSERefDN.add(uniqueName);
//		//wenn Referenzknoten ermittelt werden können, dann kann Struct-Knoten eingefügt werden, sonst warten
//		try
//		{
//			this.extractXPtr(korpusPath, xmlBase, relHref);
//			//Knoten in Korpusgraphen schreiben
//			this.InsertStructEdge2(korpusPath, paulaFile, paulaId, paulaType, xmlBase, structID, relID, relHref, relType);
//		}
//		catch (Exception e)
//		{
//			//Knoten muss in Warteliste geschrieben werden
//			this.TempSEDNList.add(new TempStructEdgeDN(korpusPath, paulaFile, paulaId, paulaType, xmlBase, structID, relID, relHref, relType));
//		}
//	}*/
//	
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation eines AnnoDataReaders mit einer
//	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
//	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. 
//	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
//	 * @param paulaFile File - geparste PAULA-Datei
//	 * @param paulaId String - PAULA-ID dieses Annotationselementes
//	 * @param paulaType String - Paula-Typ dieses Annotationselementes
//	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
//	 * @param featID String - feat-ID dieses Annotationselementes
//	 * @param featHref String - Bezugselement, auf die sich dieses Annotationselementes bezieht
//	 * @param featTar String - feat-Target dieses Annotationselementes
//	 * @param featVal String - feat-Value dieses Annotationselementes
//	 * @param featDesc String - feat-Description dieses Annotationselementes
//	 * @param featExp String - feat-Example dieses Annotationselementes
//	 */
//	public void annoDataConnector(	String 	korpusPath,
//									File 	paulaFile,
//									String 	paulaId, 
//									String 	paulaType,
//									String 	xmlBase,
//									String 	featID,
//									String 	featHref,
//									String 	featTar,
//									String 	featVal,
//									String 	featDesc,
//									String 	featExp) throws Exception
//	{
//		if (DEBUG)
//			System.out.println(	MSG_STD + "annoDataConnector with data:\n"+MSG_STD +
//								"\tkorpusPath: "+ korpusPath+ ", paulaFile: "+ paulaFile.getName()+
//								", paulaID: " + paulaId + ", paulaType: " + paulaType +
//								", xmlBase: " + xmlBase + ", featID: "+ featID+
//								", featHref: "+ featHref +", featTar: "+ featTar+
//								", featVal: "+ featVal + ", featDesc: "+ featDesc+
//								", featExp: " +featExp);
//		boolean areNodes= false;	//gibt an, ob die Referenzen auf Knoten verweisen
//		Vector<TextedDN> refNodes= null;
//		
//		//Erzeuge Zuordnungstabelle für Annotationen
//		Hashtable<String, String> annoTable= new Hashtable<String, String>();
//		//type und value einfügen
//		if ((paulaType== null)|| (paulaType.equalsIgnoreCase("")) ||
//			(featVal== null)|| (featVal.equalsIgnoreCase(""))) throw new Exception(ERR_INCORRECT_ANNO);
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
//		
//		try
//		{
//			//alle Knoten ermitteln, auf die diese Annotation verweist
//			refNodes= this.extractXPtr(korpusPath, xmlBase, featHref);
//			areNodes= true;
//		}
//		catch (Exception e)
//			{ areNodes= false; }
//		//Verweisziele sind Knoten
//		if (areNodes)
//		{
//			//Name des zu erzeugenden AnnoDN erstellen
//			//Wenn Element keinen ID-wert besitzt, Namenserweiterung erstellen
//			String nameID= null;
//			if ((featID== null) || (featID.equalsIgnoreCase("")))
//			{
//				nameID= this.annoNameExt.toString();
//				this.annoNameExt++;
//			}
//			else nameID= featID; 
//			String uniqueName= korpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + nameID;
//			if (DEBUG) System.out.println(MSG_STD + "name for annoDN: "+ uniqueName);
//			
//			//erzeuge den Namen desAnnotationslevels
//			String parts[] =paulaFile.getName().split("[.]");
//			String annoLevelName= parts[0];
//			//neuen Annotationsknoten erzeugen (eindeutiger Knotenname für Graphen, zu speichernder Name, referenzierte KNoten, Attribut-Wert-Paare, (Datei-)Collection zu der dieser Knoten gehört)
//			AnnoDN annoDN= new AnnoDN(uniqueName, refNodes, annoTable, annoLevelName, this.currFileColDN);
//			//Konvertieren der TextedDN in IKMAbstractDN
//			Vector<IKMAbstractDN> refAbsNodes= new Vector<IKMAbstractDN>();
//			for (TextedDN textedDN: refNodes)
//				refAbsNodes.add((IKMAbstractDN) textedDN);
//			//Knoten in Graphen einfügen
//			this.kGraphMgr.addAnnoDN(annoDN, refAbsNodes);
//		}
//		//Verweisziele sind möglicherweise Kanten
//		else
//		{
//			Collection<IKMAbstractEdge> edges= this.extractXPtrAsEdge(korpusPath, xmlBase, featHref);
//			for (Edge edge: edges)
//			{
//				edge.addLabel(annoTable);
//			}
//		}
//		
//		
//	}
//	
//	/**
//	 * Diese Methode bietet ein Interface zur Kommunikation eines ComplexAnnoDataReaders mit einer
//	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
//	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. 
//	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
//	 * @param paulaFile File - geparste PAULA-Datei
//	 * @param paulaId String - PAULA-ID dieses Annotationselementes
//	 * @param paulaType String - Paula-Typ dieses Annotationselementes
//	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
//	 * @param featID String - feat-ID dieses Annotationselementes
//	 * @param featHref String - Bezugselement, auf die sich dieses Annotationselementes bezieht
//	 * @param featTar String - feat-Target dieses Annotationselementes
//	 * @param featVal String - feat-Value dieses Annotationselementes
//	 * @param featDesc String - feat-Description dieses Annotationselementes
//	 * @param featExp String - feat-Example dieses Annotationselementes
//	 */
//	public void complexAnnoDataConnector(	String 	korpusPath,
//											File 	paulaFile,
//											String 	paulaId, 
//											String 	paulaType,
//											String 	xmlBase,
//											String 	featID,
//											String 	featHref,
//											String 	featTar,
//											String 	featVal,
//											String 	featDesc,
//											String 	featExp) throws Exception
//	{
//		if (DEBUG_COMPLEX_ANNO_DATA)
//			System.out.println(	MSG_STD + "complexAnnoDataConnector with data:\n"+MSG_STD +
//								"\tkorpusPath: "+ korpusPath+ ", paulaFile: "+ paulaFile.getName()+
//								", paulaID: " + paulaId + ", paulaType: " + paulaType +
//								", xmlBase: " + xmlBase + ", featID: "+ featID+
//								", featHref: "+ featHref +", featTar: "+ featTar+
//								", featVal: "+ featVal + ", featDesc: "+ featDesc+
//								", featExp: " +featExp);
//		throw new Exception(ERR_FCT_DEPRECATED);
//	}
//	
//	/**
//	 * Diese Methode bietet ein einfaches Interface zur Kommunikation eines ComplexAnnoDataReaders mit einer
//	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
//	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. 
//	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
//	 * @param paulaFile File - geparste PAULA-Datei
//	 * @param paulaId String - PAULA-ID dieses Annotationselementes
//	 * @param paulaType String - Paula-Typ dieses Annotationselementes
//	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
//	 * @param featVal String - Wert, den diese Kante haben kann
//	 * @param srcHref String - Quelle von der aus diese Nicht-Dominanz-Kante zu ziehen ist
//	 * @param dstHref String - Ziel zu dem diese Nicht-Dominanz-Kante zu ziehen ist
//	 */
//	public void complexAnnoDataConnector(	String 	korpusPath,
//											File 	paulaFile,
//											String 	paulaId, 
//											String 	paulaType,
//											String 	xmlBase,
//											String	featVal,
//											String 	srcHref,
//											String 	dstHref) throws Exception
//	{
//		if (DEBUG_COMPLEX_ANNO_DATA)
//			System.out.println(	MSG_STD + "complexAnnoDataConnector with data:\n"+MSG_STD +
//								"\tkorpusPath: "+ korpusPath+ ", paulaFile: "+ paulaFile.getName()+
//								", paulaID: " + paulaId + ", paulaType: " + paulaType +
//								", xmlBase: " + xmlBase + ", featVal: "+ featVal+
//								", srcHref: "+ srcHref + ", dstHref: "+ dstHref);
//		//Fehler wenn kein Quellknoten gegeben
//		if ((srcHref== null) || (srcHref.equalsIgnoreCase("")))
//			throw new Exception(ERR_CAD_NO_SRC + "complexAnnoDataConnector()");
//		//Quellknoten ermitteln, auf die diese Annotation verweist
//		IKMAbstractDN srcNode= (IKMAbstractDN)this.extractXPtr(korpusPath, xmlBase, srcHref).firstElement();
//		//Zielknoten ermitteln, auf die diese Annotation verweist sofern ein Ziel angegeben wurde
//		IKMAbstractDN dstNode= null;
//		if (dstHref!= null)
//			dstNode= (IKMAbstractDN)this.extractXPtr(korpusPath, xmlBase, dstHref).firstElement();
//		
//		//-- structRelDN erzeugen
//		//eindeutigen Namen für den StructRelDN erzeugen
//		String uniqueName= korpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + cadNameExt;
//		//Namensextension erhöhen
//		cadNameExt++;
//		//neuen StructRelDN erzeugen (eindeutiger Name im Graphen, (Datei-)Collection zu der der Knoten gehört, Quellknoten von dem aus die Nixht-Dominanzkante geht, Zielknoten zu dem die Nixht-Dominanzkante geht)
//		StructRelDN structRelDN= new StructRelDN(uniqueName, paulaType, this.currFileColDN, srcNode, dstNode); 
//		
//		if (DEBUG_COMPLEX_ANNO_DATA)
//		{
//			if (dstNode!= null)
//				System.out.println(MSG_STD + "non dominance edge for node: '"+ structRelDN.getName() +"' from '"+srcNode.getName()+"' to '"+dstNode.getName()+"'.");
//			else 
//				System.out.println(MSG_STD + "non dominance edge '"+ structRelDN.getName() +"' from '"+srcNode.getName()+"' to 'no node given'.");
//		}
//		
//		//srcNode und dstNode in Liste schreiben und neuen Knoten in Graphen eintragen
//		Vector<IKMAbstractDN> refNodes = new Vector<IKMAbstractDN>();
//		refNodes.add(srcNode);
//		if (dstNode!= null) refNodes.add(dstNode);
//		//structRelDN in Graphen einfügen
//		this.kGraphMgr.addStructRelDN(structRelDN, refNodes);
//		//ggf. Kante von structRelDN zu aktuellem colDN
//		if (this.currFileColDN!= null) this.kGraphMgr.setDNToColDN(structRelDN, this.currFileColDN);
//		//-- annoDN erzeugen
//		//eindeutigen Namen für Annotationsknoten erstellen
//		uniqueName= korpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + paulaType + KW_NAME_SEP +this.annoNameExt;
//		this.annoNameExt++;
//		//Referenzknoten als TextedNodes
//		Vector<TextedDN> textedDNs= new Vector<TextedDN>(); 
//		textedDNs.add((TextedDN)structRelDN);
//		
//		//Attribut-Wert-Paare erstellen
//		Hashtable<String, String> attValPairs= new Hashtable<String, String>(); 
//		attValPairs.put(KW_REL_TYPE_NAME, paulaType);
//		//Attributwert  featVal einfügen, wenn dieser existiert
//		if ((featVal != null) && (!featVal.equalsIgnoreCase("")))
//			attValPairs.put(KW_ANNO_VALUE, featVal);
//		//neuen Annotationsknoten erzeugen (eindeutiger Knotenname für Graphen, zu speichernder Name, referenzierte KNoten, Attribut-Wert-Paare, (Datei-)Collection zu der dieser Knoten gehört)
//		AnnoDN annoDN= new AnnoDN(uniqueName, textedDNs, attValPairs, paulaType, this.currFileColDN);
//		//Referenzknoten als IKMAbstractDN
//		refNodes= new Vector<IKMAbstractDN>();
//		refNodes.add(structRelDN);
//		//AnnoKnoten in Graphen schreiben
//		this.kGraphMgr.addAnnoDN(annoDN, refNodes);
//	}
//	
//	/**
//	 * Diese Methode bietet ein Interface, damit ein spezifischer Reader dem Mapper-Objekt
//	 * mitteilen kann wenn das parsen einer PAULA-Datei gestartet wurde. Zur Identifikation
//	 * welches Objekt dieses Event erzeugt hat, muss es als Parameter mitgegeben werden.
//	 * @param paulaReader PAULAReader - Reader, der dieses Event erzeugt hat.
//	 */
//	public void startDocument(PAULAReader paulaReader) throws Exception
//	{
//		//vom Typ STRUCTEDGE
//		if (this.currReaderInfo.readerCType.equalsIgnoreCase(KW_CTYPE_STRUCTEDGEDATA))
//		{
//			//old
//			if (MODE_SE_NEW)
//			{
//				// Tabelle zum temporären Speichern der StructEdge-Elemente initialisierne
//				this.tmpSETable= new Hashtable<String, Vector<TmpStructEdgeDN>>();
//				this.tmpStructIDList= new Vector<String>();
//			}
//			else
//			{
//				//Liste der StructEdgeDN (rel-Elemente) initialisieren
//				this.TempSEDNList= new Vector<TempStructEdgeDN>();
//				//Liste der StructEdgeDN (struct-Elemente) initialisieren
//				this.TempSEDN2List= new Vector<TempStructEdgeDN2>();
//				//Liste der refNodes (rel-Elemente), die zu einem struct-Element gehören initialisieren 
//				this.TempSERefDN= new Vector<String>();
//				//gibt an, dass im Schritt zuvor kein StructEdgeNode gelöscht wurde
//			}
//		}
//	}
//	
//	/**
//	 * Diese Methode erzeugt StructDN-Objecte aus den temporären Objekten, die aus den
//	 * StructEdgeDateien erzeugt wurden. Dabei werden u.U. mehrere TmpStructEdgeDN-Objekte in
//	 * StructDN-Objekte umgewandelt und es werden annotierte Kanten erzeugt. Diese Methode
//	 * ruft sich selbst rekursiv auf und bricht ab, wenn ein Zyklus entdeckt wird. 
//	 * @param structID String - ID des struct-Elementes aus PAULA, das gerade eingefügt werden soll
//	 * @return den daraus erzeugten StructDN-Knoten
//	 * @throws Exception
//	 */
//	private StructDN insertStructEdgeDN(String structID) throws Exception
//	{
//		//System.out.println("bearbeite: "+ structID);
//		Vector<TmpStructEdgeDN> seDNs= this.tmpSETable.get(structID);
//		if ((seDNs== null) || (seDNs.isEmpty())) 
//		{
//			if (!this.tmpStructIDList.contains(structID))
//				throw new Exception(ERR_STRUCTID_NOT_EXIST + structID);
//			else
//				throw new Exception(ERR_NO_RELS + structID);
//		}
//		String korpusPath= seDNs.firstElement().korpusPath;
//		String paulaFileName= seDNs.firstElement().paulaFile.getName();
//		String uniqueName= korpusPath + KW_NAME_SEP + paulaFileName +KW_NAME_SEP + structID;
//		String nodeType= seDNs.firstElement().paulaType;
//		//Tabelle, die zu jedem Referenzknoten die Attribute ID und Kantentyp speichert
//		Map<String, String[]> edgeAttTable= new Hashtable<String, String[]>(); 
//		//System.out.println("uniqueName" + uniqueName);
//		//wenn es bereits einen Knoten mit diesem Namen gibt
//		if (this.kGraphMgr.getDN(uniqueName)!= null) 
//		{
//			//System.out.println("springe raus");
//			return((StructDN)this.kGraphMgr.getDN(uniqueName));
//		}
//		//wenn es noch keinen Knoten mit diesem Namen gibt
//		else
//		{
//			if (this.seenStructIDs.contains(structID))
//				throw new Exception(ERR_CYCLE_IN_SE_DOC + this.seenStructIDs);
//			this.seenStructIDs.add(structID);
//			//leere Liste für die Referenzknoten erstellen
//			Vector<IKMAbstractDN> refNodes= new Vector<IKMAbstractDN>();
//			//iteriere durch alle TmpStructEdgeDN-Objekte zu structID
//			for (TmpStructEdgeDN seDN: seDNs)
//			{
//				//System.out.println("Referenz: "+seDN.relHref);
//				//Prüfe die Referenzen jedes seDN-Elementes
//				XPtrInterpreter xPtrInterpreter= new XPtrInterpreter();
//				xPtrInterpreter.setInterpreter(seDN.xmlBase, seDN.relHref);
//				Collection<XPtrRef> xPtrRefs= xPtrInterpreter.getResult();
//				for (XPtrRef xPtrRef: xPtrRefs)
//				{
//					//Fehler, wenn Referenztyp nicht ELEMENT
//					if(xPtrRef.getType()!= XPtrRef.POINTERTYPE.ELEMENT) throw new Exception(ERR_XPTR_NO_ELEMENT);
//					//wenn Basis-Dokument das gerade gelesene ist
//					if (xPtrRef.getDoc().equalsIgnoreCase(paulaFileName))
//					{
//						//System.out.println("Referenz in diesem Dokument");
//						//wenn Referenz auf ein Einzelelement verweist
//						if (!xPtrRef.isRange())
//						{
//							refNodes.add(insertStructEdgeDN(xPtrRef.getID()));
//							String uniqueNameR= korpusPath + KW_NAME_SEP + paulaFileName +KW_NAME_SEP + xPtrRef.getID();
//							//Annotationen der Kante Zwischenspeichern, KantenID Kantentyp
//							edgeAttTable.put(uniqueNameR, new String[]{seDN.relID, seDN.relType});
//						}
//						//wenn Referenz auf Bereich von Elementen verweist
//						else 
//						{
//							boolean start= false;
//							//ermittle Bereich b_se aus L_const beginnend von ref.left bis left.right
//							for (String newStructID : this.tmpStructIDList)
//							{
//								if (newStructID.equalsIgnoreCase(xPtrRef.getLeft()))
//									start= true;
//								if (start) 
//								{
//									StructDN structDN= insertStructEdgeDN(newStructID);
//									refNodes.add(structDN);
//									//Annotationen der Kante Zwischenspeichern, KantenID Kantentyp
//									edgeAttTable.put(structDN.getName(), new String[]{seDN.relID, seDN.relType});
//								}
//								if (newStructID.equalsIgnoreCase(xPtrRef.getRight()))
//									break;
//							}
//						}
//					}
//					//wenn Basis-Dokument nicht das gerade gelesene ist
//					else
//					{
//						//System.out.println("Referenz nicht aus aktuellem Dokument");
//						//ermittle alle Referenzknoten durch entsprechende Methode
//						Collection<TextedDN> textedRefDNs= this.extractXPtr(seDN.korpusPath, seDN.xmlBase, seDN.relHref);
//						//schreibe alle ermittelten Referenzknoten in Liste ref_list
//						for (TextedDN textedRefDN: textedRefDNs)
//						{
//							refNodes.add((IKMAbstractDN) textedRefDN);
//							//Annotationen der Kante Zwischenspeichern, KantenID Kantentyp
//							edgeAttTable.put(((IKMAbstractDN) textedRefDN).getName(), new String[]{seDN.relID, seDN.relType});
//						}
//					}
//				}
//			}
//			//wenn refNodes== leer -->Fehler
//			if ((refNodes== null) || (refNodes.isEmpty())) 
//				throw new Exception(ERR_NO_RELS + structID);
//			
//			//alle refNodes in TextedDN casten
//			Vector<TextedDN> tRefNodes= new Vector<TextedDN>(); 
//			for (IKMAbstractDN aDN: refNodes)
//				tRefNodes.add((TextedDN)aDN);
//			//StructDN Knoten erzeugen
//			StructDN structDN= new StructDN(uniqueName, structID, nodeType, this.currFileColDN, tRefNodes);
//			
//			//Kanten von diesem structKnoten zu dessen Referenzknoten erzeugen
//			Collection<Edge> edges= new Vector<Edge>();
//			ConstEdge edge= null;
//			for (IKMAbstractDN refNode: refNodes)
//			{
//				//Name der Kante extrahieren
//				if (edgeAttTable.get(refNode.getName())== null)
//				{
//					//System.out.println("edgeAttTable: "+edgeAttTable);
//					//System.out.println("StructID: "+structID);
//					throw new Exception(ERR_NULL_STRUCTEDGE+ refNode.getName());
//					
//				}
//				String edgeID= edgeAttTable.get(refNode.getName())[0];
//				String uniqueNameE= korpusPath + KW_NAME_SEP + paulaFileName +KW_NAME_SEP + edgeID;
//				//System.out.println("EdgeType: "+ edgeAttTable.get(refNode.getName())[1]);
//				//Kantentyp extrahieren
//				String edgeType= edgeAttTable.get(refNode.getName())[1];
//				edge= new ConstEdge(uniqueNameE, structDN, refNode, edgeType);
//				edges.add(edge);
//			}	
//			//System.out.println("füge ein: "+ structDN.getName());
//			//StructDNKnoten in den Graphen einfügen
//			this.kGraphMgr.addStructDN2(structDN, edges);
//			return(structDN);
//			
//		}
//	}
//	
//	/**
//	 * Diese Methode bietet ein Interface, damit ein spezifischer Reader dem Mapper-Objekt
//	 * mitteilen kann wenn das parsen einer PAULA-Datei beendet wurde. Zur Identifikation
//	 * welches Objekt dieses Event erzeugt hat, muss es als Parameter mitgegeben werden.
//	 * @param paulaReader PAULAReader - Reader, der dieses Event erzeugt hat.
//	 */
//	public void endDocument(PAULAReader paulaReader) throws Exception
//	{
//		//vom Typ PrimData
//		if (this.currReaderInfo.readerCType.equalsIgnoreCase(KW_CTYPE_PRIMDATA));
//		//vom Typ TokData
//		else if (this.currReaderInfo.readerCType.equalsIgnoreCase(KW_CTYPE_TOKDATA))
//		{
//			java.util.Collections.sort(this.TempTokDNList);
//			long counter= 0;	//Zähler, der die Reihenfolgenummer speichert
//			//Liste der Informationen der Tokendaten durchgehen und an den Graph hängen
//			for (TempTokDN TempTokDN: this.TempTokDNList)
//			{
//				//neuen Tokenknoten erzeugen (eindeutiger Name im Graph, ..., Primärdatenknoten auf den dieser verweist, (Datei-)Collectionknoten, ...)
//				TokDN tokDN= new TokDN(TempTokDN.uniqueName, TempTokDN.markID, TempTokDN.paulaType, TempTokDN.primDN, TempTokDN.colDN, TempTokDN.left, TempTokDN.right, counter);
//				//Knoten in Graph einfügen
//				this.kGraphMgr.addTokDN(tokDN, TempTokDN.primDN);
//				counter++;
//			}
//			
//		}
//		//vom Typ StructData
//		else if (this.currReaderInfo.readerCType.equalsIgnoreCase(KW_CTYPE_STRUCTDATA));
//		//vom Typ AnnoData
//		else if (this.currReaderInfo.readerCType.equalsIgnoreCase(KW_CTYPE_ANNODATA));
//		//vom Typ STRUCTEDGE
//		else if (this.currReaderInfo.readerCType.equalsIgnoreCase(KW_CTYPE_STRUCTEDGEDATA))
//		{
//			if (MODE_SE_NEW)
//			{
//				this.seenStructIDs= new Vector<String>();
//				for (String structID: this.tmpStructIDList)
//					insertStructEdgeDN(structID);
//				
//				// Tabelle zum temporären Speichern der StructEdge-Elemente löschen
//				this.tmpSETable= null;
//				this.tmpStructIDList= null;
//				this.seenStructIDs=null;
//			}
//			else
//			{
//				/*
//				//wenn es einen Knoten in der Warteliste gibt, dann dessen refNodes setzen
//				if (!this.TempSEDN2List.isEmpty())
//				{
//					//setze Referenzknoten des letzten Elementes in der Liste
//					this.TempSEDN2List.lastElement().setRefNodes(this.TempSERefDN);
//					this.TempSERefDN= new Vector<String>();
//				}
//				//alle übriggebliebenen Knoten in den Korpusgraphen einfügen
//				boolean abort= false;		//gibt an ob folgende Schleife abgebrochen werden soll
//				boolean listsEmpty= false;	//gibt an, wann beide Listen geleert sind
//				int list2length= this.TempSEDN2List.size();				//mißt die Veränderung der Listen um Zyklen zu finden
//				int runs= 0;		//zählt die Durchläufe, wenn DEBUG-Schalter gesetzt 	
//				while ((!abort) && (!listsEmpty))
//				{
//					if (DEBUG_SE) System.out.println(MSG_STD + "Durchlauf Nr.: "+ runs);
//					if (DEBUG_SE) runs++;
//					
//					//gehe Liste der rel-Nodes durch und versuche diese zu schreiben
//					Vector<TempStructEdgeDN> nodesToDelete= new Vector<TempStructEdgeDN>(); 
//					for(TempStructEdgeDN seDN: this.TempSEDNList)
//					{
//						//DEBUG
//						if (DEBUG_SE) System.out.print(MSG_STD +"Einfügen von(rel): "+seDN.relID);
//						
//						//versuche rel-Knoten in den KorpusGraphen zu schreiben
//						try
//						{
//							this.InsertStructEdge2(seDN.korpusPath, seDN.paulaFile, seDN.paulaId, seDN.paulaType, seDN.xmlBase, seDN.structID, seDN.relID, seDN.relHref, seDN.relType);
//							//aktuellen Knoten zum löschen in Löschliste schreiben
//							nodesToDelete.add(seDN);
//							//DEBUG
//							if (DEBUG_SE) System.out.println("...eingefügt");
//						}
//						catch (Exception e)
//						{
//							//DEBUG
//							if (DEBUG_SE) System.out.println("...nicht eingefügt");
//						}
//					}
//					//Knoten aus Löschliste löschen
//					if (!nodesToDelete.isEmpty())
//						this.TempSEDNList.removeAll(nodesToDelete);
//					
//					//gehe Liste der rel-Nodes durch und versuche diese zu schreiben
//					Vector<TempStructEdgeDN2> nodesToDelete2= new Vector<TempStructEdgeDN2>();
//					for(TempStructEdgeDN2 seDN: this.TempSEDN2List)
//					{
//						//DEBUG
//						if (DEBUG_SE) System.out.print(MSG_STD +"Einfügen von(struct): "+seDN.structID);
//						
//						//versuche rel-Knoten in den KorpusGraphen zu schreiben
//						try
//						{
//							this.InsertStructEdge1(seDN.korpusPath, seDN.paulaFile, seDN.paulaId, seDN.paulaType, seDN.xmlBase, seDN.structID, seDN.refNodes);
//							//eingefügten Knoten aus Liste löschen
//							nodesToDelete2.add(seDN);
//							
//							//DEBUG
//							if (DEBUG_SE) System.out.println("...eingefügt");
//						}
//						catch (Exception e)
//						{
//							//DEBUG
//							if (DEBUG_SE) System.out.println("...nicht eingefügt");
//						}
//					}
//					//Knoten aus Löschliste löschen
//					if (!nodesToDelete2.isEmpty())
//						this.TempSEDN2List.removeAll(nodesToDelete2);
//					//DEBUG
//					if (DEBUG_SE) System.out.println(MSG_STD + "alte Länge/neue Länge: "+ list2length + " / "+ this.TempSEDN2List.size());
//					
//					//wenn beide Listen leer sind, listsEmpty = true
//					if ((this.TempSEDNList.isEmpty()) && (this.TempSEDN2List.isEmpty()))
//						listsEmpty= true;
//					//wenn Zyklus entdeckt wurde
//					else if (this.TempSEDN2List.size()== list2length)
//						abort= true;
//					list2length= this.TempSEDN2List.size();
//				}
//				if (abort)
//					throw new Exception(ERR_CYCLE_IN_SE_DOC + "Size of list: "+ list2length);
//				if (this.logger!= null) 
//				{
//					this.logger.debug(MSG_STD + "reading document results: " + paulaReader.getEvaluation());
//					this.logger.debug(MSG_STD + "reading document made: " 	+ this.SC_SDN+ " structDN-objects,\t"
//																			+ this.SC_SDN_REF_EDGE+ " structDN-edges,\t"
//																			+ this.SC_SEDN+ " structEdgeDN-objects,\t"
//																			+ this.SC_SEDN_REF_EDGE+ " structEdgeDN-edges");
//				}
//				//Listen neu Initialisieren
//				this.TempSEDNList= null;
//				this.TempSEDNList= null;
//				this.TempSEDN2List= null;
//				*/
//			}
//		}
//	}
//	// ------------------------------ Ende Methoden aus dem PAULAMapperInterface------------------------------
//// ------------------------------ Ende Methoden der Middleware ------------------------------
//	
//	/**
//	 * Gibt Informationen über dieses Objekt als String zurück. 
//	 * @return String - Informationen über dieses Objekt
//	 */
//	public String toString()
//	{	
//		String retStr= "";
//		 retStr= "this method isn´t implemented";
//		return(retStr);
//	}
////	 ============================================== main Methode ==============================================	
//
//
//}
