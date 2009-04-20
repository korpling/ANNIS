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
import de.corpling.salt.SaltConcrete.SSpanRelation;
import de.corpling.salt.SaltConcrete.SStructure;
import de.corpling.salt.SaltConcrete.STextualDataSource;
import de.corpling.salt.SaltConcrete.STextualRelation;
import de.corpling.salt.SaltConcrete.SToken;
import de.corpling.salt.SaltConcrete.SaltConcreteFactory;

import importer.paula.paula10.analyzer.paulaAnalyzer.PAULAAnalyzer;
import importer.paula.paula10.importer.mapper.AbstractMapper;
import importer.paula.paula10.importer.paulaReader_1_0.*;
import importer.paula.paula10.importer.paulaReader_1_0.reader.PAULAReader;
import importer.paula.paula10.structureAnalyzer.structureAnalyzer.PAULAStructAnalyzer;
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
public class CopyOfMapperV1 extends AbstractMapper implements PAULAMapperInterface
{
	/**
	 * Speichert Rel-Elemente fï¿½r StructEdgeDatenKnoten zwischen.
	 * @author Florian Zipser
	 * @version 1.0
	 */
	private class TempStructEdgeDN
	{
		String corpusPath= null;
		File paulaFile= null;
		String paulaId= null;
		String paulaType= null;
		String xmlBase= null;
		String structID= null;
		String relID= null;
		String relHref= null;
		String relType= null;
		
		public TempStructEdgeDN(	String corpusPath, 
									File paulaFile, 
									String paulaId, 
									String paulaType,  
									String xmlBase, 
									String structID,
									String relID,
									String relHref,
									String relType) throws Exception
		{
			this.corpusPath= corpusPath;
			this.paulaFile= paulaFile;
			this.paulaId= paulaId;
			this.paulaType= paulaType;
			this.xmlBase= xmlBase;
			this.structID= structID;
			this.relID= relID;
			this.relHref= relHref;
			this.relType= relType;
		}
		
		public String toString()
		{
			String retStr= null;
			retStr= "relId: "+ this.relID;
			return(retStr);
		}
	}
	
	/**
	 * Speichert Struct-Elemente fï¿½r StructEdgeDatenKnoten zwischen.
	 * @author Florian Zipser
	 * @version 1.0
	 */
	private class TempStructEdgeDN2
	{
		String 	corpusPath= null;
		File 	paulaFile= null;
		String 	paulaId= null;
		String 	paulaType= null;
		String 	xmlBase= null;
		String 	structID= null;
		Vector<String> refNodes= null;
		
		public TempStructEdgeDN2(	String 	corpusPath,
									File 	paulaFile,
									String 	paulaId, 
									String 	paulaType,
									String 	xmlBase,
									String 	structID) throws Exception
		{
			this.corpusPath= corpusPath;
			this.paulaFile= paulaFile;
			this.paulaId= paulaId;
			this.paulaType= paulaType;
			this.xmlBase= xmlBase;
			this.structID= structID;
		}
		
		public void setRefNodes(Vector<String> refNodes)
			{ this.refNodes= refNodes; }
		
		public String toString()
		{
			String retStr= null;
			retStr= "structId: "+ this.structID;
			return(retStr);
		}
	}
	
	/**
	 * Speichert TokenDatenKnoten zwischen
	 * @author Florian Zipser
	 * @version 1.0
	 */
	/*
	private class TempTokDN implements Comparable<TempTokDN>
	{
		String uniqueName= null;
		String ns= null;
		String paulaType= null;
		String markID= null;
		PrimDN primDN= null;
		CollectionDN colDN= null; 
		Long left= null;
		Long right= null;
		
		public TempTokDN(String uniqueName, String ns, String paulaType, String markID, PrimDN primDN, CollectionDN colDN, Long left, Long  right)
		{
			this.ns= ns;
			this.uniqueName= uniqueName;
			this.paulaType= paulaType;
			this.markID= markID;
			this.primDN= primDN;
			this.colDN= colDN;
			this.left= left;
			this.right= right;
		}
		*/
		/**
		 * Vergleicht zwei Daten fï¿½r das Interface Comparable
		 * Wenn "this < argument" dann muss die Methode irgendetwas < 0 zurï¿½ckgeben
    	 * Wenn "this = argument" dann muss die Methode 0 (irgendetwas = 0) zurï¿½ckgeben
    	 * Wenn "this > argument" dann muss die Methode irgendetwas > 0 zurï¿½ckgeben     
		 */
	/*
		public int compareTo(TempTokDN argument) 
		{
			if(this.left < argument.left)
	            return -1;
	        if( this.left > argument.left)
	            return 1;     
	        return 0; 
		}
	}*/

//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"MapperV1";		//Name dieses Tools
	private static final String VERSION= 	"1.0";			//Version dieses Tools
	
	private static final boolean MODE_SE_NEW= true;
	
	//Pfad und Dateiname fï¿½r Settingfiles
	private static final String FILE_TYPED_KORP=	"typed_corp.xml";				//default name der Korpusstrukturdatei
	private static final boolean DEBUG=			false;				//DEBUG-Schalter
	private static final boolean DEBUG_TOK_DATA= false;				//spezieller DEBUG-Schalter fï¿½r TokData
	private static final boolean DEBUG_SE=		false;				//spezieller DEBUG-Schalter fï¿½r StructEdge
	private static final boolean DEBUG_COMPLEX_ANNO_DATA=	false;	//spezieller DEBUG-Schalter fï¿½r ComplexAnnoData
	private static final boolean DEBUG_COLLECTION_DN=	false;		//spezieller DEBUG-Schalter fï¿½r CollectionDN
	private static final boolean DEBUG_METASTRUCT_DATA= false;		//spezieller DEBUG-Schalter fï¿½r MetaStructData
	private static final boolean DEBUG_METAANNO_DATA= false;		//spezieller DEBUG-Schalter fï¿½r MetaAnnoData
	private static final boolean DEBUG_KSDESC=	false;				//spezieller DEBUG-Schalter fï¿½r das Berechnen des Korpuspfades 
	private static final boolean DEBUG_STRUCT=	false;				//spezieller DEBUG-Schalter fï¿½r den StructData-Connector
	private static final boolean DEBUG_ANNO_DATA=	false;			//spezieller DEBUG-Schalter fï¿½r den AnnoData-Connector
	private static final boolean DEBUG_POINTING_REL_DATA=	false;	//spezieller DEBUG-Schalter fï¿½r den ponting-relation-Connector
	private static final boolean DEBUG_MULTI_FEAT_DATA=		false;	//spezieller DEBUG-Schalter fï¿½r den multiFeatDataConnector
	private static final boolean DEBUG_AUDIO_DATA=			true;	//spezieller DEBUG-Schalter fï¿½r den audioDataConnector
	
	//Schlï¿½sselworte fï¿½r Readertypen
	private static final String KW_CTYPE_METASTRUCTDATA=	"MetaStructData";	//MetaAnnotationsstruktur (anno.xml)
	private static final String KW_CTYPE_METAANNODATA=		"MetaAnnoData";		//Metaannotationen (Dateien, die sich auf anno.xml beziehen)
	private static final String KW_CTYPE_PRIMDATA=			"PrimData";			//Primï¿½rdaten
	private static final String KW_CTYPE_TOKDATA=			"TokData";			//Tokendaten
	private static final String KW_CTYPE_STRUCTDATA=		"StructData";		//Strukturdaten
	private static final String KW_CTYPE_STRUCTEDGEDATA=	"StructEdgeData";	//Kanten-Strukturdaten
	private static final String KW_CTYPE_ANNODATA=			"AnnoData";			//Annotationsdaten
	
	//Standardwerte
	private static final long STD_ANNO_NAME_EXT=	0;			//Standardwert fï¿½r die Annotationsnnamenerweiterung
	private static final long STD_CAD_NAME_EXT=		0;			//Standardwert fï¿½r die ComplexAnnotationsnnamenerweiterung
	private static final long STD_COLANNO_NAME_EXT=	0;			//Standardwert fï¿½r die CollectionAnnotationsnamenerweiterung
	private static final long STD_PR_NAME_EXT=		0;			//Standardwert fï¿½r die pointing-relation-Namenerweiterung
	
	private static final String KW_STRUCTEDGE_TYPE_ATT=	"EDGE_TYPE";	//Name unter dem das Attribut rel.type im relANNIS Modell als Annotation gefï¿½hrt werden soll 
	
	//Schlï¿½sselworte
	private static final String KW_NAME_SEP=	"#";		//Seperator fï¿½r Knotennamen (Knotentyp#Knotenname)
	private static final String KW_PATH_SEP=	"/";		//Seperaor fï¿½r Korpuspfade	
	private static final String KW_TYPE_DNDOC=	"doc";		//Knotentypname fï¿½r Dokumentknoten
	private static final String KW_TYPE_DNCOL=	"col";		//Knotentypname fï¿½r Collectionknoten
	
	//passt hier nicht so gut hin
	private static final String KW_ANNO_VALUE=	"value";			//Schlï¿½sselwort unter dem das PAULA-Attribut Value als Annotation gespeichert werden soll
	private static final String KW_ANNO_TAR=	"target";			//Schlï¿½sselwort unter dem das PAULA-Attribut Target als Annotation gespeichert werden soll
	private static final String KW_ANNO_DESC=	"description";		//Schlï¿½sselwort unter dem das PAULA-Attribut Target als Annotation gespeichert werden soll
	private static final String KW_ANNO_EXP=	"example";			//Schlï¿½sselwort unter dem das PAULA-Attribut Target als Annotation gespeichert werden soll
	private static final String KW_REL_TYPE_NAME= "RELATION_TYPE";	//Schlï¿½sselwort unter der die Annotation fï¿½r relStructDN gespeichert
	private static final String KW_TYPE_FILE=	"FILE";				//Schlï¿½sselwort unter fï¿½r den Typ "Datei" einer Collection				
	
	//einige Statistik-Counter
	private long SC_SDN= 0;						//Statistik-Counter fï¿½r StructDN
	private long SC_SDN_REF_EDGE= 0;			//Statistik-Counter fï¿½r Kanten vone einem StructDN zu den Referenzknoten
	private long SC_SEDN= 0;					//Statistik-Counter fï¿½r StructEdgeDN
	private long SC_SEDN_REF_EDGE= 0;			//Statistik-Counter fï¿½r Kanten vone einem StructEdgeDN zu den Referenzknoten
	
	//private CorpusGraphMgr kGraphMgr= null;			//interner Korpusgraph, in den die Knoten eingefï¿½gt werden
	
	private Long annoNameExt= STD_ANNO_NAME_EXT;			//Namenszusatz fï¿½r Annotationsknoten, da diese meist keine ID besitzen
	private Long cadNameExt=  STD_CAD_NAME_EXT;				//Namenszusatz fï¿½r ComplexAnnotationsknoten, da diese meist keine ID besitzen
	private Long pointingRelNameExt= STD_PR_NAME_EXT;		//Namenszusatz fï¿½r Pointing relations, da diese meist keine ID besitzen
	private Long colAnnoNameExt= STD_COLANNO_NAME_EXT;		//Namenszusatz fï¿½r CollectionAnnotationsknoten, da diese meist keine ID besitzen
	//private ReaderInfo currReaderInfo= null;				//aktuell benutzter PAULA-Reader, wird gespeichert um Events entgegen zu nehmen
	//private Vector<ReaderInfo> readerInfoList= null;		//Liste aller benutzbaren PAULAReader
	//private Vector<TempTokDN> TempTokDNList= null;			//Liste, die die Daten fï¿½r die TokenDN zwischenspeichert
	private Vector<TempStructEdgeDN> TempSEDNList= null;	//Liste, die die Daten fï¿½r die StructEdgeDN zwischenspeichert (rel-Elemente)
	private Vector<TempStructEdgeDN2> TempSEDN2List= null;	//Liste, die die Daten fï¿½r die StructEdgeDN zwischenspeichert (struct-Elemente)
	private Vector<String> TempSERefDN=	null;				//Liste mit Namen von StructEdgeNodes (rel-Elemente) die Zwischengespeichert werden
	//private CollectionDN currFileColDN= null;				//aktueller Collectionknoten, der ein PAULA-Dokument darstellt
	/**
	 * folder in wich the mapper can write some information like the documents as graph
	 */
	private File infoFolder= null;
	
	/**
	 * gibt an, ob der Graph STï¿½ck fï¿½r Stï¿½ck als Dot-Files ausgegeben werden soll
	 */
	private boolean toDot= false;
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
	private Hashtable<String, String> colNamingTable= null;	
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_OK=					"OK";
	private static final String MSG_CREATE_INTGRAPH=	"create internal graph model...";
	private static final String MSG_READ_PFILES=		"reading paula files..."; 
	private static final String MSG_CREATE_KOPRPP=		"creating korpus pre- and post-order...";
	private static final String MSG_PREPARE_GRAPH=		"preparing internal korpus graph for inserting nodes...";
	private static final String MSG_DOT_WRITING=		"writing korpus graph to dot file...";
	private static final String MSG_CLOSING_GRAPH=		"closing korpus graph...";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NOT_IMPLEMENTED=		MSG_ERR + "This methode is not yet been implemented.";
	private static final String ERR_NO_PDFILE=				MSG_ERR + "The given pdFile does not exist. This might be an internal error. Not existing file: ";
	private static final String ERR_NO_TYPE_FILE=			MSG_ERR + "There is no type file for korpus in the following folder. You have to analyze the korpus first by PAULAAnalyzer. Untyped korpus: ";
	private static final String ERR_TYPE_NOT_SUPPORTED=		MSG_ERR + "Sorry the given analyze type is not yet supported in "+ TOOLNAME+ " v" +VERSION+". Analyze type: ";
	private static final String ERR_XPTR_NOT_A_TEXT=		MSG_ERR + "An XPointer of the parsed document does not refer to a xml-textelement. Incorrect pointer: ";
	private static final String ERR_TOO_MANY_REFS=			MSG_ERR + "There are too many references for a token node element: ";
	private static final String ERR_WRONG_LEFT_RIGHT=		MSG_ERR + "The left or right border is not set correctly of XPointer: ";
	private static final String ERR_NO_PRIMDN=				MSG_ERR + "No primary data node found for token element: ";
	private static final String ERR_WRONG_REF_KIND_ELEM=	MSG_ERR + "The XPointer references in current file are incorrect. There only have to be element pointers and the following is not one of them: ";
	private static final String ERR_CANNOT_FIND_REFNODE=	MSG_ERR + "Connot find a node with the following name: ";
	private static final String ERR_CANNOT_FIND_REFEDGE=	MSG_ERR + "Connot find an edge with the following name: ";
	private static final String ERR_INCORRECT_ANNO=			MSG_ERR + "Can not work with the given annotation, because the type-value or the value-value is empty.";
	private static final String ERR_NODENAME_NOT_IN_GRAPH=	MSG_ERR + "The given rel-Node does not exist in Graph.";
	private static final String ERR_CYCLE_IN_SE_DOC=		MSG_ERR + "Cannot import the data from document, because there is a cycle in it. Cycle list: ";
	private static final String ERR_FCT_DEPRECATED=			MSG_ERR + "This method isnï¿½t supported, it is deprecated.";
	private static final String ERR_CAD_NO_SRC=				MSG_ERR + "There is no source Href given in methode: ";
	private static final String ERR_META_STRUCT_FILE=		MSG_ERR + "This corpus contains two meta-struct-data files (anno.xml).";
	private static final String ERR_METASTRUCT_FILE=		MSG_ERR + "There is an error in the mta-struct-document. One link can reference only one Element or a sequence of elements: ";
	private static final String ERR_ID_NOT_IN_NTABLE=		MSG_ERR + "The given reference cannot be explored, thereï¿½s an error in document: ";
	private static final String ERR_XPTR_NO_ELEMENT=		MSG_ERR + "The given reference is not an element or an element-range pointer: ";
	private static final String ERR_NO_RELS=				MSG_ERR + "Thereï¿½s an error in parsed document. The following struct node has no rel-node: ";
	private static final String ERR_STRUCTID_NOT_EXIST=		MSG_ERR + "Thereï¿½s an error in parsed document. The following struct-id wich is referenced does not exists: ";
	private static final String ERR_NULL_STRUCTEDGE=		MSG_ERR + "The searched edge does not exist in internal table: ";
	//	 ============================================== statische Methoden ==============================================
	private static String createNS(String paulaFileName)
	{
		String parts[] =paulaFileName.split("[.]");
		return(parts[0].trim());
	}
	
	//	 ============================================== Konstruktoren ==============================================

	Logger logger= Logger.getLogger(CopyOfMapperV1.class);
	/**
	 * Initialisiert ein Mapper Objekt und setzt den logger zur Nachrichtenausgabe.
	 * 
	 */
	public CopyOfMapperV1() throws Exception
	{
		super(Logger.getLogger(CopyOfMapperV1.class));
		//fillReaderInfoList();
		//TempTokDNList= new Vector<TempTokDN>();
	}
	
	/**
	 * Initialisiert ein Mapper Objekt und setzt den logger zur Nachrichtenausgabe.
	 * @param logger Logger - Logger zur Nachrichtenausgabe
	 */
	public CopyOfMapperV1(Logger logger) throws Exception
	{
		super(logger);
		//fillReaderInfoList();
		//TempTokDNList= new Vector<TempTokDN>();
	}
//	 ============================================== private Methoden ==============================================
	
	/**
	 * Diese Methode sucht alle Knoten aus dem internen Graphen, auf die der hier 
	 * ï¿½bergebene XPointer verweist zurï¿½ck.
	 * @param corpusPath String - Der aktuelle KorpusPfad, indem sich die Knoten befinden
	 * @param xmlBase String - Das XML-Basisdokuments des XPointers
	 * @param href String - der eigentliche XPointer
	 * @return alle Knoten, auf die dieser XPointer verweist 
	 */
	/*
	private Vector<TextedDN> extractXPtr(	String corpusPath, 
											String xmlBase, 
											String href) throws Exception
	{
		return(null);
		/*
		//Objekt zum Interpretieren des XLinks in mark.href initialisieren
		XPtrInterpreter xPtrInter= new XPtrInterpreter();
		xPtrInter.setInterpreter(xmlBase, href);
		//gehe durch alle Knoten, auf die sich dieses Element bezieht
		Vector<XPtrRef> xPtrRefs=  xPtrInter.getResult();
		Vector<TextedDN> refNodes= new Vector<TextedDN>();
		for (XPtrRef xPtrRef: xPtrRefs)
		{
			//Fehler, wenn XPointer-Reference vom falschen Typ
			if (xPtrRef.getType()!= XPtrRef.POINTERTYPE.ELEMENT)
				throw new Exception(ERR_WRONG_REF_KIND_ELEM + href);
			
			//wenn XPointer-Bezugsknoten einen Bereich umfasst
			if (xPtrRef.isRange())
			{
				//erzeuge den Namen des linken Bezugsknotens
				String leftName= corpusPath + KW_NAME_SEP + xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getLeft();
				//erzeuge den Namen des rechten Bezugsknotens
				String rightName= corpusPath + KW_NAME_SEP + xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getRight();
				String typeName= this.kGraphMgr.getDNType(this.kGraphMgr.getDN(leftName));
				for (ICMAbstractDN absDN: this.kGraphMgr.getDNRangeByType(typeName, leftName, rightName))
					refNodes.add((TextedDN) absDN); 
			}
			//wenn XPointer-Bezugsknoten einen einzelnen Knoten referenziert
			else
			{
				//erzeuge den Namen des Bezugsknotens
				String nodeName= corpusPath + KW_NAME_SEP + xPtrRef.getDoc() +KW_NAME_SEP + xPtrRef.getID();
				TextedDN refNode= (TextedDN)this.kGraphMgr.getDN(nodeName);
				if (refNode == null) throw new Exception(ERR_CANNOT_FIND_REFNODE + nodeName);
				refNodes.add(refNode);
			}
		}
		return(refNodes);
		
	}*/
	/*
	private Collection<ICMAbstractEdge> extractXPtrAsEdge(	String corpusPath, 
															String xmlBase, 
															String href) throws Exception
	{
		return(null);
		/*
		//Objekt zum Interpretieren des XLinks in mark.href initialisieren
		XPtrInterpreter xPtrInter= new XPtrInterpreter();
		xPtrInter.setInterpreter(xmlBase, href);
		//gehe durch alle Knoten, auf die sich dieses Element bezieht
		Vector<XPtrRef> xPtrRefs=  xPtrInter.getResult();
		Vector<ICMAbstractEdge> refEdges= new Vector<ICMAbstractEdge>();
		for (XPtrRef xPtrRef: xPtrRefs)
		{
			//Fehler, wenn XPointer-Reference vom falschen Typ
			if (xPtrRef.getType()!= XPtrRef.POINTERTYPE.ELEMENT)
				throw new Exception(ERR_WRONG_REF_KIND_ELEM + href);
			
			//wenn XPointer-Bezugsknoten einen Bereich umfasst
			if (xPtrRef.isRange())
			{
				//TODO es muss ein Index angelegt werden um Bereiche von Kanten zu ermitteln
				throw new Exception(ERR_NOT_IMPLEMENTED);
				
			}
			//wenn XPointer-Bezugsknoten einen einzelnen Knoten referenziert
			else
			{
				//System.out.println("alle Kanten: "+ this.kGraphMgr.getEdges());
				//erzeuge den Namen des Bezugsknotens
				String edgeName= corpusPath + KW_NAME_SEP + xPtrInter.getDoc() +KW_NAME_SEP + xPtrRef.getID();
				ICMAbstractEdge refEdge= this.kGraphMgr.getEdge(edgeName);
				if (refEdge == null) throw new Exception(ERR_CANNOT_FIND_REFEDGE + edgeName);
				refEdges.add(refEdge);
			}
		}
		return(refEdges);
			
	}*/
	
	/**
	 * Nimmt alle Resettings vor, die beim parsen einer neuen PAULA-Datei erledigt
	 * werden mï¿½ssen. Einige Werte werden auf ihren Standardwert zurï¿½ckgesetzt.
	 */
	private void resetEveryPAULAFile()
	{
		//Statistikcounter initialisiseren
		this.SC_SDN= 0;
		this.SC_SDN_REF_EDGE= 0;
		this.SC_SEDN= 0;
		this.SC_SEDN_REF_EDGE= 0;
		
		this.annoNameExt= STD_ANNO_NAME_EXT;
		this.cadNameExt= STD_CAD_NAME_EXT;
		this.pointingRelNameExt= STD_PR_NAME_EXT; 
		//TempTokDNList= new Vector<TempTokDN>();
	}
	
//	 ============================================== protected Methoden ==============================================
		
	
//	 ============================================== ï¿½ffentliche Methoden ==============================================
	/**
	 * Prepares this Mapper-object for mapping all readed data.
	 */
	private void prepare4Map()
	{
		//Namenszuordnungstabelle fï¿½r meta-Annotationsdaten erstellen
		colNamingTable= new Hashtable<String, String>();
		
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
	
//	/**
//	 * The factory for producing all salt model elements.
//	 */
//	protected SaltFactory saltFactory= new SaltFactoryImpl(); 
	
	/**
	 * Current saltGraph in which this importer imports actually.
	 */
	protected SaltGraph saltGraph= null;

//----------- Start: import receiver	
//	protected ImportReceiver importReceiver= null;
//	/**
//	 * Sets the import receiver, to commit that a corpus is read.
//	 * @param importReceiver
//	 */
//	public void setImportReceiver(ImportReceiver importReceiver)
//	{
//		this.importReceiver= importReceiver;
//	}
//----------- End: import receiver
	
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
	private File dstFolder= null;
	private File tmpFolder= null;
	/**
	 * Bildet das PAULA 1.0 Datenmodell auf das relANNIS 2.0 Modell ab. Dabei werden die
	 * Packages paulaReader_1_0 und relANNIS_2_0 verwendet. Benï¿½tigt wird das analysierte
	 * Quellkorpus srcFolder und ein Verzeichnis, in das die erstellten Dateien geschrieben
	 * werden kï¿½nnen.
	 * @param srcFolder File - Quellverzeichnis, aus dem das zu mappende Korpus stammt
	 * @param dstFolder File - Zielverzeichniss, in das die Output-Dateien geschrieben werden
	 * @param tmpFolder File - temporï¿½res Verzeichnis zum Zwischenspeichern
	 * @param toDot boolean - Gibt an, ob eine Dot-Datei dieses Graphens erstellt werden soll
	 */
	public void map(	File srcFolder, 
						File dstFolder,
						File tmpFolder,
						boolean toDot) throws Exception
	{ 
		//Korpus-Typ-Datei erzeugen
		File typedFile= new File(srcFolder.getCanonicalPath() + "/" + FILE_TYPED_KORP);
		// es existiert keine typ-Datei des Korpus
		if (!typedFile.exists()) throw new Exception(ERR_NO_TYPE_FILE + srcFolder.getCanonicalPath());
		
		//toDot setzen
		this.toDot= toDot;
		
		//globalen info-Ordner setzen
		this.infoFolder= tmpFolder;
		
		//internen Korpusgraph erstellen
		if (this.logger!= null) this.logger.info(MSG_CREATE_INTGRAPH);
		//this.kGraphMgr= new CorpusGraphMgr(dstFolder, this.logger);
		if (this.logger!= null) this.logger.info(MSG_OK);
		
		//bereitet den KorpusGraphMgr zum Einfï¿½gen von Daten vor 
		if (this.logger!= null) this.logger.info(MSG_PREPARE_GRAPH);
		//this.kGraphMgr.start();
		if (this.logger!= null) this.logger.info(MSG_OK);
		
		//Mapper fï¿½r Mapping vorbereiten
		if (this.logger!= null) this.logger.info("preparing mapper for mapping");
		this.prepare4Map();
		if (this.logger!= null) this.logger.info(MSG_OK);
		
		//PAULAConnector erstellen
		if (this.logger!= null) this.logger.info("initializing all useble connector and reader objects");
		PAULAConnector pConnector= new PAULAConnector(this.srcFolder, this, this.logger);
		if (this.logger!= null) this.logger.info(MSG_OK);
		
		//das Mappen starten
		if (this.logger!= null) this.logger.info("start reading");
		pConnector.startReading();
		if (this.logger!= null) this.logger.info(MSG_OK);
		
		
//		//Ausgabe des erzeugten Korpusgraphen
//		if (this.toDot)
//		{
//			if (this.logger!= null) this.logger.info(MSG_DOT_WRITING);
//			//this.kGraphMgr.printGraph(infoFolder.getCanonicalPath() + "/kGraph");
//			if (this.logger!= null) this.logger.info(MSG_OK);
//		}
		this.srcFolder= srcFolder;
		this.dstFolder= dstFolder;
		this.tmpFolder= tmpFolder;
	}
	
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
		
//		CorpusElement corpusElement= this.saltFactory.createCorpusElement();
//		corpusElement.setName(corpusName);
//		ElementPath parentCPath=null;
//		if (this.parentCorpusPath.size()> 0)
//			parentCPath= this.parentCorpusPath.peek(); 
//		this.parentCorpusPath.push(this.corpusGraph.addCorpus(parentCPath, corpusElement));
//		//this.parentCorpusPath= this.corpusGraph.addCorpus(parentCorpusPath, corpusElement);
//		//initialize root corpus
//		if (rootCorpusPath== null)
//			//this.rootCorpusPath= this.parentCorpusPath;
//			this.parentCorpusPath.peek();
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
//		ElementPath corpusPath= this.parentCorpusPath.pop();
//		//commit, that corpus tree is finished
//		if (this.parentCorpusPath.size()== 0)
//		{
//			this.importReceiver.commit(corpusPath);
//		}
	}
	
//	protected ElementPath documentPath= null;
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
//		this.importReceiver.commit(this.documentPath);
		//String fullDocName= corpusPath + KW_PATH_SEP + docName;
		
		
		/*
		//aktuellen Dokumentknoten und alle seine Kinder schreiben entfernen
		if (this.logger!= null) this.logger.info("closing...");
		this.kGraphMgr.closeDocDN();
		
		if (this.logger!= null) this.logger.info("COMPLETED");
		
		//Ausgabe des erzeugten Korpusgraphen
		if (this.toDot)
		{
			if (this.logger!= null) this.logger.info("printing...");
			String dotFileName= infoFolder.getCanonicalPath() + "/"+corpusPath.replace("/", "-");
			this.kGraphMgr.printGraph(dotFileName);
		}
		*/
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
		/*
		if (DEBUG_METASTRUCT_DATA)
			System.out.println(	MSG_STD +"metaStructDataConnector with data:\t"+
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", slType: " + slType +
								", structID: " + structID + ", relID: "+ relID+
								", href: "+ href);
		
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
		/*
		if (DEBUG_METAANNO_DATA)
			System.out.println(	MSG_STD +"metaAnnoDataConnector with data:\t"+
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", featHref: "+ featHref+
								", featVal: "+ featVal);
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
		//String uniqueName= corpusPath + KW_NAME_SEP + paulaFile.getName();
		String uniqueName= paulaFile.getName();
		//create element
		STextualDataSource sTextualDS= SaltConcreteFactory.eINSTANCE.createSTextualDataSource();
		//sTextualDS.setId(uniqueName);
		sTextualDS.setSText(text);
		this.currSDocument.getSDocumentGraph().addSElement(sTextualDS);
		
		//create entry in naming table
		this.elementNamingTable.put(uniqueName, sTextualDS.getId().toString());
		
		/*
		//Primï¿½rdatenknoten erzeugen 
		PrimDN primDN= new PrimDN(corpusPath + KW_NAME_SEP + paulaFile.getName(), paulaId, text, this.currFileColDN);
		//PrimDN-knoten in Graph einfï¿½gen (Kante zum DocDN und KorpDN wird automatisch erzeugt)
		this.kGraphMgr.addPrimDN(primDN);
		//Kante von PrimDN zu aktuellem CollectionDN erzeugen, wenn es einen gibt
		if (this.currFileColDN!= null) this.kGraphMgr.setDNToColDN(primDN, this.currFileColDN);
		*/
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
		if (DEBUG_TOK_DATA)
			System.out.println(	MSG_STD +"tokDataConnector with data:\t"+
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
		//sToken.setId(uniqueName);
		this.currSDocument.getSDocumentGraph().addSElement(sToken);
		
		//create entry in naming table
		this.elementNamingTable.put(uniqueName, sToken.getId().toString());
		
		//create relation
		STextualRelation textRel= SaltConcreteFactory.eINSTANCE.createSTextualRelation();
		textRel.setSSourceElement(sToken);
		textRel.setSDestinationElement(primDN);
		textRel.setSLeftPos(left);
		this.currSDocument.getSDocumentGraph().addSRelation(textRel);
		textRel.setSRightPos(right);
		
		//tokDN erstellen
		//Namen fï¿½r den Knoten erstellen
//		String uniqueName= corpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + markID;
//		//Namespace extrahieren
//		String parts[] =paulaFile.getName().split("[.]");
//		String ns= parts[0];
//		//Objekt erstellen zum Zwischenspeichern der Tokenknoten
//		TempTokDN TempTokDN= new TempTokDN(uniqueName, ns, paulaType, markID, primDN, this.currFileColDN, left, right);
//		// und in die Liste eintragen
//		this.TempTokDNList.add(TempTokDN);
		
		/*
		if (DEBUG_TOK_DATA)
			System.out.println(	MSG_STD +"tokDataConnector with data:\t"+
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", markID: "+ markID+
								", href: "+ href + ", markType: "+ markType);
		//Objekt zum Interpretieren des XLinks in mark.href initialisieren
		XPtrInterpreter xPtrInter= new XPtrInterpreter();
		xPtrInter.setInterpreter(xmlBase, href);
		Vector<XPtrRef> xPtrRefs=  xPtrInter.getResult();
		int runs= 0;
		//suche den Primï¿½rdatenknoten zu diesem Tokendatenknoten
		PrimDN primDN= null;
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
				String textNodeName= corpusPath +KW_NAME_SEP+ xPtrRef.getDoc();
				primDN= (PrimDN)this.kGraphMgr.getDN(textNodeName);
				try
				{
					left= new Long (xPtrRef.getLeft());
					right= new Long (xPtrRef.getRight());
					//linken und rechten Wert in korrrektes Format bringen
					left= left-1;
					right= left + right;
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
		//tokDN erstellen
		//Namen fï¿½r den Knoten erstellen
		String uniqueName= corpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + markID;
		//Namespace extrahieren
		String parts[] =paulaFile.getName().split("[.]");
		String ns= parts[0];
		//Objekt erstellen zum Zwischenspeichern der Tokenknoten
		TempTokDN TempTokDN= new TempTokDN(uniqueName, ns, paulaType, markID, primDN, this.currFileColDN, left, right);
		// und in die Liste eintragen
		this.TempTokDNList.add(TempTokDN);
		*/
	}
	
	/**
	 * Returns a list of all paula-element-ids refered by the given xpointer-expression.
	 * @param xmlBase
	 * @param href
	 */
	private Collection<String> getPAULAElementIds(String xmlBase, String href) throws Exception
	{
		Collection<String> refPaulaIds= null;
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
				
//				System.out.println("range in: "+ leftName + ", to: "+ rightName);
				//extract all paula elements which are refered by this pointer
				{
					refPaulaIds= new Vector<String>();
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
		if (DEBUG_STRUCT)
			System.out.println(	MSG_STD +"markableDataConnector with data:\n"+MSG_STD +
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
		/*
		if (DEBUG_STRUCT)
		System.out.println(	MSG_STD +"markableDataConnector with data:\n"+MSG_STD +
			"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
			", paulaID: " + paulaId + ", paulaType: " + paulaType +
			", xmlBase: " + xmlBase + ", markID: "+ markID+
			", href: "+ href +", markType: "+ markType);
		
		//alle Knoten ermitteln, auf die diese Annotation verweist
		Vector<TextedDN> refNodes= null;
		try
		{
			refNodes= this.extractXPtr(corpusPath, xmlBase, href);
		}
		catch (Exception e)
		{
			throw new Exception("Error in document '"+this.currFileColDN.getName()+"' "+e.getMessage());
		}
		//Name des zu erzeugenden StructDN erstellen
		String uniqueName= corpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + markID;	
		//erzeuge Strukurknoten mit (eindeutiger Name fï¿½r Graphen, Name fï¿½r DB, aktuelle Collection, referenzierte Knoten)
		StructDN structDN= new StructDN(uniqueName, markID, MapperV1.createNS(paulaFile.getName()), paulaType, this.currFileColDN, refNodes);
		//Konvertieren der TextedDN in IKMAbstractDN
		//Vector<ICMAbstractDN> refAbsNodes= new Vector<ICMAbstractDN>();
		//Knoten in Graphen einfï¿½gen
		this.kGraphMgr.addStructDN(structDN);
		//Referenzknoten konvertieren zum einfï¿½gen in den Graphen
		CoverageRelationEdge cREdge= null; 
		for (TextedDN textedDN: refNodes)
		{
			cREdge= new CoverageRelationEdge(structDN, (ICMAbstractDN) textedDN);
			this.kGraphMgr.addNonDominanceEdge(cREdge);
			//refAbsNodes.add((ICMAbstractDN) textedDN);
		}
		//Kante von StructDN zu ColDN
		if (this.currFileColDN!= null) this.kGraphMgr.setDNToColDN(structDN, this.currFileColDN);
		*/
	}
	
	
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
		/*
		if (DEBUG_SE)
			System.out.println(	MSG_STD +"structEdgeDataConnector with data:\n"+MSG_STD +
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", structID: "+ structID+
								", relID: "+ relID +", relHref: "+ relHref + ", relType: "+ relType);
		
		if(!this.tmpStructIDList.contains(structID)) 
			this.tmpStructIDList.add(structID);
		
		//Objekt zum temporï¿½ren Speichern erstellen
		TmpStructEdgeDN tmpSEDN= new TmpStructEdgeDN(	corpusPath, paulaFile, paulaId, 
														paulaType, xmlBase, structID, 
														relID, relHref, relType);
		Vector<TmpStructEdgeDN> seDNList= this.tmpSETable.get(structID);
		if (seDNList== null)
			seDNList= new Vector<TmpStructEdgeDN>();
		seDNList.add(tmpSEDN);
		this.tmpSETable.put(structID, seDNList);
		*/
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
		/*
		if (DEBUG_ANNO_DATA)
			System.out.println(	MSG_STD + "annoDataConnector with data:\n"+MSG_STD +
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", featID: "+ featID+
								", featHref: "+ featHref +", featTar: "+ featTar+
								", featVal: "+ featVal + ", featDesc: "+ featDesc+
								", featExp: " +featExp);
		boolean areNodes= false;	//gibt an, ob die Referenzen auf Knoten verweisen
		Vector<TextedDN> refNodes= null;
		
		//Erzeuge Zuordnungstabelle fï¿½r Annotationen
		Hashtable<String, String> annoTable= new Hashtable<String, String>();
		//type und value einfï¿½gen
		if ((paulaType== null)|| (paulaType.equalsIgnoreCase("")) ||
			(featVal== null)|| (featVal.equalsIgnoreCase(""))) throw new Exception(ERR_INCORRECT_ANNO);
		annoTable.put(paulaType, featVal);
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
			{ areNodes= false;}
		//Verweisziele sind Knoten
		if (areNodes)
		{
			//Name des zu erzeugenden AnnoDN erstellen
			//Wenn Element keinen ID-wert besitzt, Namenserweiterung erstellen
			String nameID= null;
			if ((featID== null) || (featID.equalsIgnoreCase("")))
			{
				nameID= this.annoNameExt.toString();
				this.annoNameExt++;
			}
			else nameID= featID; 
			String uniqueName= corpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + nameID;
			if (DEBUG) System.out.println(MSG_STD + "name for annoDN: "+ uniqueName);
			
			//erzeuge den Namen desAnnotationslevels
			String parts[] =paulaFile.getName().split("[.]");
			String annoLevelName= parts[0];
			//neuen Annotationsknoten erzeugen (eindeutiger Knotenname fï¿½r Graphen, zu speichernder Name, referenzierte KNoten, Attribut-Wert-Paare, (Datei-)Collection zu der dieser Knoten gehï¿½rt)
			AnnoDN annoDN= new AnnoDN(uniqueName, refNodes, annoTable, annoLevelName, this.currFileColDN);
			//Konvertieren der TextedDN in IKMAbstractDN
			Vector<ICMAbstractDN> refAbsNodes= new Vector<ICMAbstractDN>();
			for (TextedDN textedDN: refNodes)
			{
				refAbsNodes.add((ICMAbstractDN) textedDN);
			}
			//Knoten in Graphen einfï¿½gen
			this.kGraphMgr.addAnnoDN(annoDN, refAbsNodes);
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
		*/
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
		/*
		if (DEBUG_POINTING_REL_DATA)
			System.out.println(	MSG_STD + "pointingRelDataConnector with data:\n"+MSG_STD +
								"\tcorpusPath: "+ corpusPath+ ", paulaFile: "+ paulaFile.getName()+
								", paulaID: " + paulaId + ", paulaType: " + paulaType +
								", xmlBase: " + xmlBase + ", featVal: "+ featVal+
								", srcHref: "+ srcHref + ", dstHref: "+ dstHref);
		//suche Bezugsknoten von denen die Pointing relation ausgehen soll
		Vector<TextedDN> srcDNs= this.extractXPtr(corpusPath, xmlBase, srcHref);
		PointingRelationEdge edge= null;
		String edgeName="PR";
		String edgeValue=paulaType;
		//wenn es Ziele fï¿½r die Pointing relation gibt, Kante zwischen src und dst einfï¿½gen
		if ((dstHref!= null) && (!dstHref.equalsIgnoreCase("")))
		{
			//suche Bezugsknoten zu denen die Pointing relation gehen soll
			Vector<TextedDN> dstDNs= this.extractXPtr(corpusPath, xmlBase, dstHref);
			//eindeutiger Name der Kante
			String uniqueName= null;
			//gehe Quellbezugsknoten durch
			for(TextedDN srcDN: srcDNs)
			{
				for(TextedDN dstDN: dstDNs)
				{
					uniqueName= corpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + pointingRelNameExt;
					pointingRelNameExt++;
					
					edge= new PointingRelationEdge(uniqueName, (StructDN)srcDN, (ICMAbstractDN)dstDN, edgeName, edgeValue);
					this.kGraphMgr.addNonDominanceEdge(edge);
				}
			}
			
		}
		//wenn es keine Ziele fï¿½r die Pointing relation gibt, Selbstkante einfï¿½gen
		else 
		{
			//eindeutiger Name der Kante
			String uniqueName= null;
			//Tabelle der Labels der Kante
			for(TextedDN srcDN: srcDNs)
			{
				uniqueName= corpusPath + KW_NAME_SEP + paulaFile.getName() +KW_NAME_SEP + pointingRelNameExt;
				pointingRelNameExt++;
				edge= new PointingRelationEdge(uniqueName, (ICMAbstractDN)srcDN, (ICMAbstractDN)srcDN, edgeName, edgeValue);
				this.kGraphMgr.addNonDominanceEdge(edge);
			}
		}
		*/
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
		/*
		//Collection-Knoten erzeugen
		String uniqueName= paulaFile.getName();
		CollectionDN colDN= (CollectionDN) this.kGraphMgr.getDN(uniqueName);
		//wenn ï¿½bergeordnete Collection noch nicht existiert, dann erzeugen
		if (colDN== null)
		{
			colDN= new CollectionDN(uniqueName, KW_TYPE_FILE, paulaFile.getName());
			this.kGraphMgr.addCollectionDN(colDN, null, this.kGraphMgr.getCurrKorpDN());
		}
		if (DEBUG_COLLECTION_DN) 
			System.out.println("created collection node with name: "+ uniqueName);
		//erzeugten Knoten als aktuellen setzen
		this.currFileColDN= colDN; 
		
		//nimmt ein paar resettings vor, die fï¿½r jede neue Datei zurï¿½ckgesetzt werden mï¿½ssen
		this.resetEveryPAULAFile();
		
		
		//vom Typ STRUCTEDGE
		//old if (this.currReaderInfo.readerCType.equalsIgnoreCase(KW_CTYPE_STRUCTEDGEDATA))
		if (paulaReader.getPAULACType().equalsIgnoreCase(KW_CTYPE_STRUCTEDGEDATA))
		{
			//old
			if (MODE_SE_NEW)
			{
				// Tabelle zum temporï¿½ren Speichern der StructEdge-Elemente initialisierne
				this.tmpSETable= new Hashtable<String, Vector<TmpStructEdgeDN>>();
				this.tmpStructIDList= new Vector<String>();
			}
			else
			{
				//Liste der StructEdgeDN (rel-Elemente) initialisieren
				this.TempSEDNList= new Vector<TempStructEdgeDN>();
				//Liste der StructEdgeDN (struct-Elemente) initialisieren
				this.TempSEDN2List= new Vector<TempStructEdgeDN2>();
				//Liste der refNodes (rel-Elemente), die zu einem struct-Element gehï¿½ren initialisieren 
				this.TempSERefDN= new Vector<String>();
				//gibt an, dass im Schritt zuvor kein StructEdgeNode gelï¿½scht wurde
			}
		}
		*/
	}
	
	/**
	 * Diese Methode erzeugt StructDN-Objecte aus den temporï¿½ren Objekten, die aus den
	 * StructEdgeDateien erzeugt wurden. Dabei werden u.U. mehrere TmpStructEdgeDN-Objekte in
	 * StructDN-Objekte umgewandelt und es werden annotierte Kanten erzeugt. Diese Methode
	 * ruft sich selbst rekursiv auf und bricht ab, wenn ein Zyklus entdeckt wird. 
	 * @param structID String - ID des struct-Elementes aus PAULA, das gerade eingefï¿½gt werden soll
	 * @return den daraus erzeugten StructDN-Knoten
	 * @throws Exception
	 */
	/*
	private StructDN insertStructEdgeDN(String structID) throws Exception
	{
		return(null);
		/*
		//System.out.println("bearbeite: "+ structID);
		Vector<TmpStructEdgeDN> seDNs= this.tmpSETable.get(structID);
		if ((seDNs== null) || (seDNs.isEmpty())) 
		{
			if (!this.tmpStructIDList.contains(structID))
				throw new Exception(ERR_STRUCTID_NOT_EXIST + structID);
			else
				throw new Exception(ERR_NO_RELS + structID);
		}
		String corpusPath= seDNs.firstElement().corpusPath;
		String paulaFileName= seDNs.firstElement().paulaFile.getName();
		String uniqueName= corpusPath + KW_NAME_SEP + paulaFileName +KW_NAME_SEP + structID;
		String nodeType= seDNs.firstElement().paulaType;
		//Tabelle, die zu jedem Referenzknoten die Attribute ID und Kantentyp speichert
		Map<String, String[]> edgeAttTable= new Hashtable<String, String[]>(); 
		//System.out.println("uniqueName" + uniqueName);
		//wenn es bereits einen Knoten mit diesem Namen gibt
		if (this.kGraphMgr.getDN(uniqueName)!= null) 
		{
			//System.out.println("springe raus");
			return((StructDN)this.kGraphMgr.getDN(uniqueName));
		}
		//wenn es noch keinen Knoten mit diesem Namen gibt
		else
		{
			if (this.seenStructIDs.contains(structID))
				throw new Exception(ERR_CYCLE_IN_SE_DOC + this.seenStructIDs);
			this.seenStructIDs.add(structID);
			//leere Liste fï¿½r die Referenzknoten erstellen
			Vector<ICMAbstractDN> refNodes= new Vector<ICMAbstractDN>();
			//iteriere durch alle TmpStructEdgeDN-Objekte zu structID
			for (TmpStructEdgeDN seDN: seDNs)
			{
				//System.out.println("Referenz: "+seDN.relHref);
				//Prï¿½fe die Referenzen jedes seDN-Elementes
				XPtrInterpreter xPtrInterpreter= new XPtrInterpreter();
				xPtrInterpreter.setInterpreter(seDN.xmlBase, seDN.relHref);
				Collection<XPtrRef> xPtrRefs= null; 
				try
				{
					xPtrRefs= xPtrInterpreter.getResult();
				}
				catch (Exception e)
				{
					//e.printStackTrace();
					throw new Exception("Error in document '"+this.currFileColDN.getName()+"': "+e.getMessage());
				}	
				for (XPtrRef xPtrRef: xPtrRefs)
				{
					//Fehler, wenn Referenztyp nicht ELEMENT
					if(xPtrRef.getType()!= XPtrRef.POINTERTYPE.ELEMENT) throw new Exception(ERR_XPTR_NO_ELEMENT);
					//wenn Basis-Dokument das gerade gelesene ist
					if (xPtrRef.getDoc().equalsIgnoreCase(paulaFileName))
					{
						//System.out.println("Referenz in diesem Dokument");
						//wenn Referenz auf ein Einzelelement verweist
						if (!xPtrRef.isRange())
						{
							refNodes.add(insertStructEdgeDN(xPtrRef.getID()));
							String uniqueNameR= corpusPath + KW_NAME_SEP + paulaFileName +KW_NAME_SEP + xPtrRef.getID();
							//Annotationen der Kante Zwischenspeichern, KantenID Kantentyp
							edgeAttTable.put(uniqueNameR, new String[]{seDN.relID, seDN.relType});
						}
						//wenn Referenz auf Bereich von Elementen verweist
						else 
						{
							boolean start= false;
							//ermittle Bereich b_se aus L_const beginnend von ref.left bis left.right
							for (String newStructID : this.tmpStructIDList)
							{
								if (newStructID.equalsIgnoreCase(xPtrRef.getLeft()))
									start= true;
								if (start) 
								{
									StructDN structDN= insertStructEdgeDN(newStructID);
									refNodes.add(structDN);
									//Annotationen der Kante Zwischenspeichern, KantenID Kantentyp
									edgeAttTable.put(structDN.getName(), new String[]{seDN.relID, seDN.relType});
								}
								if (newStructID.equalsIgnoreCase(xPtrRef.getRight()))
									break;
							}
						}
					}
					//wenn Basis-Dokument nicht das gerade gelesene ist
					else
					{
						//System.out.println("Referenz nicht aus aktuellem Dokument");
						//ermittle alle Referenzknoten durch entsprechende Methode
						Collection<TextedDN> textedRefDNs= this.extractXPtr(seDN.corpusPath, seDN.xmlBase, seDN.relHref);
						//schreibe alle ermittelten Referenzknoten in Liste ref_list
						for (TextedDN textedRefDN: textedRefDNs)
						{
							refNodes.add((ICMAbstractDN) textedRefDN);
							//Annotationen der Kante Zwischenspeichern, KantenID Kantentyp
							edgeAttTable.put(((ICMAbstractDN) textedRefDN).getName(), new String[]{seDN.relID, seDN.relType});
						}
					}
				}
			}
			//wenn refNodes== leer -->Fehler
			if ((refNodes== null) || (refNodes.isEmpty())) 
				throw new Exception(ERR_NO_RELS + structID);
			
			//alle refNodes in TextedDN casten
			Vector<TextedDN> tRefNodes= new Vector<TextedDN>(); 
			for (ICMAbstractDN aDN: refNodes)
				tRefNodes.add((TextedDN)aDN);
			//StructDN Knoten erzeugen
			StructDN structDN= new StructDN(uniqueName, structID, MapperV1.createNS(paulaFileName), nodeType, this.currFileColDN, tRefNodes);
			
			//Kanten von diesem structKnoten zu dessen Referenzknoten erzeugen
			Collection<Edge> edges= new Vector<Edge>();
			ConstEdge edge= null;
			for (ICMAbstractDN refNode: refNodes)
			{
				//Name der Kante extrahieren
				if (edgeAttTable.get(refNode.getName())== null)
				{
					//System.out.println("edgeAttTable: "+edgeAttTable);
					//System.out.println("StructID: "+structID);
					throw new Exception(ERR_NULL_STRUCTEDGE+ refNode.getName());
					
				}
				String edgeID= edgeAttTable.get(refNode.getName())[0];
				String uniqueNameE= corpusPath + KW_NAME_SEP + paulaFileName +KW_NAME_SEP + edgeID;
				//System.out.println("EdgeType: "+ edgeAttTable.get(refNode.getName())[1]);
				//Kantentyp extrahieren
				String edgeType= edgeAttTable.get(refNode.getName())[1];
				edge= new ConstEdge(uniqueNameE, structDN, refNode, edgeType);
				edges.add(edge);
			}	
			//System.out.println("fï¿½ge ein: "+ structDN.getName());
			//StructDNKnoten in den Graphen einfï¿½gen
			this.kGraphMgr.addStructDN2(structDN, edges);
			return(structDN);
		}
		
	}*/
	
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
		/*
		if (DEBUG) System.out.println(MSG_STD +"endDocument");
		//vom Typ PrimData
		if (paulaReader.getPAULACType().equalsIgnoreCase(KW_CTYPE_PRIMDATA));
		//old if (paulaReader.getReaderName().equalsIgnoreCase(KW_CTYPE_PRIMDATA));
		//vom Typ TokData
		else if (paulaReader.getPAULACType().equalsIgnoreCase(KW_CTYPE_TOKDATA))
		//old else if (paulaReader.getReaderName().equalsIgnoreCase(KW_CTYPE_TOKDATA))
		{
			java.util.Collections.sort(this.TempTokDNList);
			long counter= 0;	//Zï¿½hler, der die Reihenfolgenummer speichert
			//Liste der Informationen der Tokendaten durchgehen und an den Graph hï¿½ngen
			for (TempTokDN TempTokDN: this.TempTokDNList)
			{
				//neuen Tokenknoten erzeugen (eindeutiger Name im Graph, ..., Primï¿½rdatenknoten auf den dieser verweist, (Datei-)Collectionknoten, ...)
				TokDN tokDN= new TokDN(TempTokDN.uniqueName, TempTokDN.markID, TempTokDN.ns, TempTokDN.paulaType, TempTokDN.primDN, TempTokDN.colDN, TempTokDN.left, TempTokDN.right, counter);
				//Knoten in Graph einfï¿½gen
				this.kGraphMgr.addTokDN(tokDN, TempTokDN.primDN);
				counter++;
			}	
		}
		//vom Typ STRUCTEDGE
		else if (paulaReader.getPAULACType().equalsIgnoreCase(KW_CTYPE_STRUCTEDGEDATA))
		//old else if (paulaReader.getReaderName().equalsIgnoreCase(KW_CTYPE_STRUCTEDGEDATA))
		{
			if (MODE_SE_NEW)
			{
				this.seenStructIDs= new Vector<String>();
				for (String structID: this.tmpStructIDList)
				{	
					insertStructEdgeDN(structID);
				}
				// Tabelle zum temporï¿½ren Speichern der StructEdge-Elemente lï¿½schen
				this.tmpSETable= null;
				this.tmpStructIDList= null;
				this.seenStructIDs=null;
			}
			else
			{
			}
		}
		*/
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
//	 ============================================== main Methode ==============================================	


}
