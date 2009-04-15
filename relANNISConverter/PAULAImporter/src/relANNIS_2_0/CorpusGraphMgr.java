package relANNIS_2_0;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import internalCorpusModel.*;

import org.apache.log4j.Logger;

import util.graph.Edge;
import util.graph.Node;
import util.graph.XTraversalObject;
import util.graph.Graph.TRAVERSAL_MODE;
import util.settingMgr.SettingMgr;

//Datenbankverbindung
import relANNIS_2_0.relANNISDAO.*;

public class CorpusGraphMgr extends ICMGraphMgr implements XTraversalObject
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"CorpusGraphMgr";		//Name dieses Tools
	private static final boolean DEBUG=		false;				//DEBUG-Schalter
	private static final boolean DEBUG_FINISH_KORP=		false;	//spezieller DEBUG-Schalter f�r das beeneden eines Korpus
	private static final boolean DEBUG_CLOSE=	false;			//spezieller DEBUG-Schalter f�r das beednden des Graphen
	private static final boolean DEBUG_CLOSE_DOCDN= false;		//spezieller DEBUG-Schalter f�r das beednden eines Dokumentknotens
	
	private static final boolean NEW_PP_RANK= true;				//gibt an, ob nach der neuen PP_Rank_computing Methode gearbeitet werden soll
	
	//Schl�sselworte
	private static final String KW_PRE_VAL= "PRE";				//Keywort f�r pre-wert
	private static final String KW_POST_VAL= "POST";			//Keywort f�r post-wert
	
	private static final String KW_RANK_PRE_VAL= "RANK:PRE";	//Pre-Wert f�r Rank
	private static final String KW_RANK_POST_VAL= "RANK:POST";	//Post-Wert f�r Rank
	
	private static final String FILE_SET= "./PAULAImporter/settings/dbSettings_local.xml";	//Name der Settingdatei
	
	//Schl�sselworte der abstrakten Relationsnamen
	private static final String KW_ABS_REL_KORP=		"korpDataRel";		//Korpusrelation
	private static final String KW_ABS_REL_DOC_STRUCT=	"structDataRel";	//Korpusrelation f�r Korpusstruktureintrag
	private static final String KW_ABS_REL_DOC_CORP=	"docCorpDataRel";		//Dokumentrelation
	private static final String KW_ABS_REL_DOC_DOC=		"docDataRel";		//Dokumentrelation
	//private static final String KW_ABS_REL_DOC_ROOT=	"structDataRel";	//Dokumentrelation
	private static final String KW_ABS_REL_PRIM=		"primDataRel";		//Prim�rdatenrelation
	private static final String KW_ABS_REL_TOK=			"structDataRel";	//Tokendatenrelation
	private static final String KW_ABS_REL_STRUCT=		"structDataRel";	//Tokendatenrelation
	private static final String KW_ABS_REL_STRUCTEDGE=	"structDataRel";	//Struktur mit Kantenannotation
	private static final String KW_ABS_REL_STRUCTREL=	"structDataRel";	//Struktur mit nicht Dominanz-Kante
	private static final String KW_ABS_REL_RANK=		"StructRankRel";	//Strukturverbindungsdatenrelation
	//private static final String KW_ABS_REL_RANK_ANNO=	"EdgeAnnoRel";		//Annotationen �ber die Strukturverbindungsdaten
	private static final String KW_ABS_REL_ANNO=		"AnnoDataRel";		//Annotationsverbindungsdatenrelation
	private static final String KW_ABS_REL_ANNO_ATT=	"AnnoAttDataRel";	//Annotationsdaten
	private static final String KW_ABS_REL_COL=			"ColDataRel";		//Collectiondaten
	private static final String KW_ABS_REL_COL_ANNO=	"ColAnnoDataRel";	//Collectiondaten
	private static final String KW_ABS_REL_COL_RANK=	"ColRankDataRel";
	private static final String KW_ABS_REL_AUDIO=		"ExtFileDataRel";		//Relation f�r die BLOB-Tupel
	
	/**
	 * abstrakter Name der Relation, in die die Strukturinformationen f�r ConstEdge-Kanten geschrieben werden.
	 */
	private static final String KW_ABS_REL_CONSTEDGE_STRUCT=	"StructRankRel";
	/**
	 * abstrakter Name der Relation, in die die Annotationen f�r ConstEdge-Kanten geschrieben werden.
	 */
	private static final String KW_ABS_REL_CONSTEDGE_ANNO=		"EdgeAnnoRel";
	
	//TODO, das ist hier ne bl�de Stelle um das fest zu verdrahten
	private static final String KW_REL_DOC_ID=		"doc_ref"; 		//Name des ID-Wertes der Relation korp_2_doc
	private static final String KW_REL_RANK_POST=	"post"; 		//Name des Post-Wertes der Relation rank
	private static final String KW_REL_COL_ID=		"id"; 			//Name des ID-Wertes der Relation collection
	
	/**
	 * speichert alle bereits gesehen NIcht-Dominanzkanten w�hrend des Abw�rtstraversierens
	 * bei der rank-Vergabe.
	 */
	private Collection<NonDominanceEdge> seenNonDEdges= null;
	
	/**
	 * speichert den Knoten, der Ursprung einer Nicht-Dominanzkante ist,
	 * wenn diese die zuletzt gelesene Kante ist, sonst null, wichtig
	 * f�r das Abw�rtstraversieren bei der rank-Vergabe
	 */
	private ICMAbstractDN nonDEdgeRead= null;
	
	/**
	 * Datanbankverbindungsobjekt
	 */
	protected DBConnector dbConnector= null;	
	
	private Long ppOrder_RANK= null;				//Pre- undPost-Order f�r das Traversieren und setzen der Rank-Tabelle
	private Long ppOrder_COL= null;				//Pre- undPost-Order f�r das Traversieren und setzen der Collection-Tabelle
	private Long ppOrder_CORP= null;				//Pre- undPost-Order f�r das Traversieren und setzen der Corpus-Tabelle
	
	private enum MODE_PPORDER {NON, CORPUS, RANK, COLLECTION};
	private MODE_PPORDER modPPOrder= MODE_PPORDER.NON;
	
	/**
	 * Speichert die Knoten, die als Wurzeln unterhalb der Dokumentebene liegen. 
	 */
	private Vector<ICMAbstractDN> rankRootDns= null;

	//private boolean korpPPOrder= false;		//gibt an, ob die Korpus Pre und Postorder berechnet werden soll
	//private boolean rankPPOrder= false;		//gibt an, ob Pre- und Postorder f�r rank-Tabelle berechnet werden soll
	private Long docDNID= null;				//aktuelle ID f�r DOCDN-Objekte
	private TupleWriter rankTWriter= null;	//TupleWriter f�r Strukturverbindungsdaten
	private TupleWriter colRankTWriter= null;	//TupleWriter f�r Strukturverbindungsdaten der Collectionknoten
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START_FCT=		MSG_STD + "start of methode: ";
	private static final String MSG_END_FCT=		MSG_STD + "end of methode: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_RANK_TWRITER=	MSG_ERR + "An internal failure occurs. The rank tuple writer is not set.";
	private static final String ERR_EMPTY_DOCDN_REM=	MSG_ERR + "Cannot close the given document-node, because the given document-node is empty.";
	private static final String ERR_EMPTY_CORPDN_REM=	MSG_ERR + "Cannot close the given document-node, because the given corpus-node is empty.";
	private static final String ERR_EMPTY_PR=			MSG_ERR + "Cannot insert the given pointing relation, because it is empty.";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein KorpusGraphMgr-Objekt. Weiter wird der �bergebene logger 
	 * gesetzt und es werden alle Einstellungen aus der 
	 * �bergebenen Datei gelesen und eine Verbindung zur Datenbank aufgebaut.
	 * @param dstFolder File - Name des Ausgabeverzeichnisses
	 * @param logger Logger - logger f�r log4j
	 * @param setFile String - Name einer Settingsdatei
	 */
	public CorpusGraphMgr(File dstFolder, Logger logger, String setFile) throws Exception
	{
		super(logger);
		this.init(setFile, dstFolder);
	}
	
	/**
	 * Initialisiert ein KorpusGraphMgr-Objekt. Weiter wird der �bergebene logger 
	 * gesetzt und es werden alle Einstellungen aus der 
	 * �bergebenen Datei gelesen und eine Verbindung zur Datenbank aufgebaut.
	 * @param dstFolder File - Name des Ausgabeverzeichnisses
	 * @param logger Logger - logger f�r log4j
	 */
	public CorpusGraphMgr(File dstFolder, Logger logger) throws Exception
	{
		super(logger);
		this.init(FILE_SET, dstFolder);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Initialisiert ein KorpusGraphMgr-Objekt. Weiter werden alle Einstellungen aus der 
	 * �bergebenen Datei gelesen und eine Verbindung zur Datenbank aufgebaut.
	 * @param setFile String - Name der Settingdatei, aus der die Einstellungen geladen werden sollen
	 * @param dstFolder File - Name des Ausgabeverzeichnisses 
	 * 
	 */
	private void init(String setFile, File dstFolder) throws Exception
	{
		//erstellt ein Datenbankverbindungsobjekt
		this.dbConnector= new DBConnector(dstFolder, this.logger);
		//liest alle Einstellungen aus einer Einstellungsdatei
		this.regSettings(setFile);
		//alle Knotentypen initialisieren
		CorpDN.initFactory(this, this.dbConnector,KW_ABS_REL_DOC_STRUCT,  KW_ABS_REL_KORP , this.logger);
		DocDN.initFactory(this, this.dbConnector, KW_ABS_REL_DOC_DOC, KW_ABS_REL_DOC_CORP, this.logger);
		PrimDN.initFactory(this, this.dbConnector, KW_ABS_REL_PRIM, this.logger);
		TokDN.initFactory(this, this.dbConnector, KW_ABS_REL_TOK, this.logger);
		StructDN.initFactory(this, this.dbConnector, KW_ABS_REL_STRUCT, this.logger);
		StructEdgeDN.initFactory(this, this.dbConnector, KW_ABS_REL_STRUCTEDGE, this.logger);
		StructRelDN.initFactory(this, this.dbConnector, KW_ABS_REL_STRUCTREL, this.logger);
		AnnoDN.initFactory(this, this.dbConnector, KW_ABS_REL_ANNO, KW_ABS_REL_ANNO_ATT, this.logger);
		CollectionDN.initFactory(this, this.dbConnector, KW_ABS_REL_COL, this.logger);
		ColAnnoDN.initFactory(this, this.dbConnector, KW_ABS_REL_COL_ANNO, this.logger);
		AudioDN.initFactory(this, this.dbConnector, KW_ABS_REL_ANNO, KW_ABS_REL_ANNO_ATT, KW_ABS_REL_AUDIO, this.logger);
		
		//Kantentypen initialisieren
		ConstEdge.initFactory(this, this.dbConnector, KW_ABS_REL_CONSTEDGE_STRUCT, KW_ABS_REL_CONSTEDGE_ANNO, this.logger);
		NonDominanceEdge.initFactory(this, this.dbConnector, KW_ABS_REL_CONSTEDGE_STRUCT, KW_ABS_REL_CONSTEDGE_ANNO, this.logger);
		PointingRelationEdge.initFactory(this, this.dbConnector, KW_ABS_REL_CONSTEDGE_STRUCT, KW_ABS_REL_CONSTEDGE_ANNO, this.logger);
		CoverageRelationEdge.initFactory(this, this.dbConnector, KW_ABS_REL_CONSTEDGE_STRUCT, this.logger);
	}
	
	/**
	 * Diese Methode registriert alle Objekte, die ihre Einstellungen aus dem Settingfile lesen
	 * bei dem SettingMgr.
	 * @param setFile - Name einer Settingdatei
	 */
	private void regSettings(String setFile) throws Exception
	{
		SettingMgr setMgr= new SettingMgr(setFile);
		//DBConnector registrieren
		setMgr.addSetListener(this.dbConnector);
		//Settings einlesen
		setMgr.start();
	}
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Bereitet die relANNIS - Datenbank auf das Einf�gen von Daten vor. 
	 */
	public void start() throws Exception
	{
		//Erzeugt ein Wurzelknotenelement in der Tabelle korpus
		//old ge�ndert 21.7.08 kein ALL-Knoten mehr in Korpus-Tabelle this.dbConnector.createCorpusAll(KW_ABS_REL_KORP);
		//Erzeugt ein Wurzelknotenelement in der Tabelle collection
		this.dbConnector.createColAll(KW_ABS_REL_COL, KW_ABS_REL_COL_RANK);
		CollectionDN.setRelID(this.dbConnector.getNewID(KW_ABS_REL_COL, KW_REL_COL_ID));
		//Erzeugt ein Wurzelknotenelement in der Tabelle struct
		//ge�ndert am 26-06-08
		//this.dbConnector.createStructAll(KW_ABS_REL_DOC_STRUCT, KW_ABS_REL_RANK);
	}
	
	/**
	 * Beendet die Datenbankverbindung und l�scht den internen Korpus. Au�erdem werden
	 * noch nicht in Streams geschriebenen Knoten geschrieben. 
	 * Liste der Schritte:<br/>
	 * <ul>
	 * 	<li>Dokumentknoten auf die Platte schreiben</li>
	 * 	<li>Datenbankverbindung schlie�en</li>
	 * </ul>
	 */
	public void close() throws Exception
	{
		//durch alle DocDN Objekte gehen und DocDN-Objekte schreiben
		//Vector<IKMAbstractDN> docNodeList= this.nodeType_idx.get(IKMDocDN.getDNLevel());
		Vector<ICMAbstractDN> docNodeList= super.getFromTypeIdx(ICMDocDN.getDNLevel());
		if (DEBUG_CLOSE) System.out.println("all nodes in index:");
		if (docNodeList != null)
		{
			for (ICMAbstractDN node: docNodeList)
			{
				DocDN docDN= (DocDN)node;
				//einen Eintrag in der Structtabelle
				//Documentknoten soll nicht mehr in dei struct-Tabelle geschrieben werden
				//ge�ndert am 06-05-08
				//docDN.toWriterStruct();
				//einen Eintrag in der Dokumenttabelle
				
				//ge�ndert am 26-06-08
				//docDN.toWriter();
				
				if (DEBUG_CLOSE) System.out.println("current document node: "+((DocDN)node).getName());
				//f�r diesen Dokumentknoten und alle untergeordneten Knoten alle Strukturverbindungsdaten schreiben
				//old before REM docDN this.createRankEntries(docDN);
			}
		}
		//Datenbankverbindung schlie�en
		this.dbConnector.close();
	}
	
	/**
	 * Schreibt die Strukturverbindungsdaten f�r den �bergebenen Dokumentenknoten und
	 * alle untergeordneten Knoten in die entsprechende Tabelle (rank).
	 * @param docDN DocDN - Dokumentenknoten, der die Wurzel darstellt.
	 * @throws Exception
	 */
	public void createRankEntries(DocDN docDN) throws Exception
	{
		this.logger.debug(MSG_START_FCT + "computeRankPPOrder()");
		//this.logger.info("creating PPOrder for document: "+ docDN.getName());
		this.rankTWriter= this.dbConnector.getTWriter(KW_ABS_REL_RANK);
		//es m�ssen DocDN, TokDN und StructDN durchsucht werden keine docDN
		
		//suche freien PPorder Wert
		if (this.ppOrder_RANK== null)
		{
			this.ppOrder_RANK= this.dbConnector.getMaxID(KW_ABS_REL_RANK, KW_REL_RANK_POST);
			//System.out.println("setze ppOrder aus DB: "+this.ppOrder_RANK);
		}
		//PP-Order soll f�r rank berechnet werden
		this.modPPOrder= MODE_PPORDER.RANK;
		if (NEW_PP_RANK)
		{
			/*
			//Diesen documentNode und alle seine untergeordneten Dokumente traversieren
			//alle Struct-Knoten ermitteln, die direkt unter dem DOCDN liegen und keine anderen V�ter haben
			//suche alle Tokenknoten
			Vector<IKMAbstractDN> tokDNs= this.getFromTypeIdx(TokDN.getDNLevel());
			Vector<Node> nodes= new Vector<Node>();
			for (IKMAbstractDN tokDN: tokDNs)
				nodes.add((Node)tokDN);

			//Liste der Wurzelknoten initialisieren
			this.rankRootDns= new Vector<IKMAbstractDN>();
			//suche per bottomUP alle Wurzelknoten unterhalb der Document -Ebene
			this.korpGraph.traverseGraph(TRAVERSAL_MODE.BOTTOM_UP, nodes, this);
			//gehe die ermittelten Wurzelknoten per depthFirst durch
			//System.out.println(MSG_STD + "alle m�glichen rank Wurzeln:");
			for (Node rootNode: this.rankRootDns)
			{
				System.out.println(MSG_STD + "traverse graph under node: "+ rootNode.getName());
				this.korpGraph.traverseGraph(TRAVERSAL_MODE.DEPTH_FIRST, rootNode, this);
				
			}
			*/
			//Diesen documentNode und alle seine untergeordneten Dokumente traversieren
			//alle Struct-Knoten ermitteln, die direkt unter dem DOCDN liegen und keine anderen V�ter haben
			//suche alle Tokenknoten
			Vector<ICMAbstractDN> tokDNs= this.getFromTypeIdx(TokDN.getDNLevel());
			Collection<TokDN> realTokDNs= new Vector<TokDN>();
			//selektiere alle Tokenknoten dieses Dokumentes aus
			for (ICMAbstractDN tokDN: tokDNs)
			{
				//wenn der Dokumentknoten des Tokenknoten mit dem aktuellen Dokumentknoten �bereinstimmt
				if (this.getDocDN(tokDN)== docDN)
					realTokDNs.add((TokDN)tokDN);
			}
			Vector<Node> nodes= new Vector<Node>();
			for (ICMAbstractDN tokDN: realTokDNs)
				nodes.add((Node)tokDN);

			//Liste der Wurzelknoten initialisieren
			this.rankRootDns= new Vector<ICMAbstractDN>();
			//suche per bottomUP alle Wurzelknoten unterhalb der Document -Ebene
			this.korpGraph.traverseGraph(TRAVERSAL_MODE.BOTTOM_UP, nodes, this);
			//gehe die ermittelten Wurzelknoten per depthFirst durch
			//System.out.println(MSG_STD + "alle m�glichen rank Wurzeln:");
			this.seenNonDEdges= new Vector<NonDominanceEdge>();
			this.nonDEdgeRead= null;
			//System.out.println("create PPOrder from: "+ this.ppOrder_RANK);
			for (Node rootNode: this.rankRootDns)
			{
				//System.out.println(MSG_STD + "traverse graph under node: "+ rootNode.getName());
				this.korpGraph.traverseGraph(TRAVERSAL_MODE.DEPTH_FIRST, rootNode, this);
				this.nonDEdgeRead= null;
			}
			//System.out.println("create PPOrder to: "+ this.ppOrder_RANK);
			this.rankRootDns= null;
		}	
		else 
		{
			//old
			this.korpGraph.depthFirst(docDN, this);
		}	
		//Post-Wert des ALL-Tupels der Tabelle rank aktualisieren
		//ge�ndert am 26-06-08
		//this.dbConnector.updateRankAll(KW_ABS_REL_RANK, KW_ABS_REL_STRUCT, this.ppOrder);
		//KorpusGraphMgrStatistics.printStatistics();
		this.modPPOrder= MODE_PPORDER.NON;
		if (this.logger!= null) this.logger.debug(MSG_END_FCT + "computeRankPPOrder()");
	}
	
	/**
	 * Soll alle Inhalte eines Korpus speichern und aus dem Hauptspeicher entfernen.
	 * Die durchzuf�hrenden Aktionen sind:<br/>
	 * <ul>
	 * 	<li>das Schreiben der CollectionDN</li>
	 * </ul>
	 * @param korpDN String - Der entsprechende Korpus, dessen Inhalte gespeichert und gel�scht werden sollen
	 *
	 */
	public void finishKorpus(CorpDN korpDN) throws Exception
	{
		if (DEBUG_FINISH_KORP) 
			System.out.println(MSG_STD + "finish korpus with name: "+korpDN.getName());
		//Pre- und Postorder der Collectionknoten in diesem Korpusknoten erstellen
		this.computeColPPOrder(korpDN);
	}
	
	/**
	 * Berechnet im internen Graphen die Pre- und Post-Order f�r Knoten der 
	 * Collectionebene, die Kinder des �bergebenen Korpusknotens sind.
	 * @param korpDN KorpDN - f�r die Collection-Ebene dieses Korpusknoten wird die PPOrder berechnet
	 */
	private void computeColPPOrder(CorpDN korpDN) throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_START_FCT + "computeColPPOrder()");
		this.modPPOrder= MODE_PPORDER.COLLECTION;
		
		//TupleWriter setzen
		this.colRankTWriter= this.dbConnector.getTWriter(KW_ABS_REL_COL_RANK);
		
		//alle Collectionknoten ermitteln
		Vector<ICMAbstractDN> colDNs= this.getFromTypeIdx(CollectionDN.getDNLevel());
		//alle Collectionknoten ermitteln, die keine CollectionDN als Vater haben
		Vector<CollectionDN> rootColDNs= new Vector<CollectionDN>();
		for (ICMAbstractDN colDN: colDNs)
		{
			//pr�fen ob Knoten zu dem aktuellen Korpus geh�rt
			if (this.getCorpDN(colDN)== korpDN)
			{
				boolean colDNParent= true;	//gibt an, ob dieser Knoten einen Vater hat, der ColDN ist
				for (ICMAbstractDN parent: this.getDominanceParents(colDN))
				{
					if (parent.getClass().equals(CollectionDN.class))
					{
						colDNParent= false;
						break;
					}
				}
				//alle V�ter von colDN ist nicht vom Typ CollectionDN
				if (colDNParent) 
				{
					if (DEBUG_FINISH_KORP) System.out.println(MSG_STD + "collection node wich is root node: "+colDN.getName());
					rootColDNs.add((CollectionDN)colDN);
				}
			}
		}
		
		
		//ppOrder-Wert aus der DB holen
		this.ppOrder_COL= this.dbConnector.getNewPPVal(KW_ABS_REL_COL_RANK);
		//System.out.println("holePPOrder f�r Col aus DB: "+this.ppOrder_COL);
		
		for (CollectionDN rootColDN : rootColDNs)
		{
			//Collectionbaum abw�rts traversieren
			this.korpGraph.depthFirst(rootColDN, this);
		}
		//Post-Wert des Collection-ALL-Tupels aktualisieren
		this.dbConnector.updateColAll(KW_ABS_REL_COL, KW_ABS_REL_COL_RANK, this.ppOrder_COL);
		
		this.modPPOrder= MODE_PPORDER.NON;
		if (this.logger!= null) this.logger.debug(MSG_END_FCT + "computeColPPOrder()");
	}
	
	
// ------------------------- Start XTraversalObject -------------------------
	/**
	 * Die Methode wird durch das Traversieren des IKMGraph aufgerufen und vergibt die 
	 * Pre-Order. 
	 * @param sMode SEARCH_MODE - Art der Traversierung
	 * @param currNode Node - aktueller Knoten, also der der gerade durch das Traversieren erreicht wurde (Veterknoten bei DEPTH_FIRST, Kinknoten bei BOTTOM_UP)
	 * @param edge Edge - Kante die die beiden Knoten verbindet
	 * @param fromNode Node - Knoten von dem aus der aktuelle Knoten erreicht wurde
	 * @param order long - Reihenfolge des Kindknotens in der Ordnung der Nachfolger des Vaterknotens (beginnend bei 0), ist currNode Veterknoten, so ist es die Reihenfolge  
	 */
	public void nodeReached(	TRAVERSAL_MODE tMode, 
								Node currNode, 
								Edge edge,
								Node fromNode, 
								long order) throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_START_FCT + "nodeReached("+currNode.getName()+")");
		// es soll die PP-Order f�r Korpusknoten erstellt werden
		if (this.modPPOrder.equals(MODE_PPORDER.CORPUS))
		{
			currNode.setAtt(KW_PRE_VAL, this.ppOrder_CORP);
			this.ppOrder_CORP++;
		}
		//es soll die PP-Order f�r Collectionknoten erstellt werden
		else if (this.modPPOrder.equals(MODE_PPORDER.COLLECTION))
		{
			Vector<Long> preVec= null;
			if (currNode.hasAttName(KW_PRE_VAL))
			{
				preVec= (Vector<Long>)currNode.getAttValue(KW_PRE_VAL);
				preVec.add(this.ppOrder_COL);
			}
			//wenn es noch keinen Pre-Eintrag gibt f�r B�ume und ersten Besuch im DAG
			else
			{
				preVec= new Vector<Long>();
				preVec.add(this.ppOrder_COL);
				currNode.setAtt(KW_PRE_VAL, preVec);
			}
			this.ppOrder_COL++;
		}
		// es soll die PP-Order f�r rank erstellt werden
		else if (this.modPPOrder.equals(MODE_PPORDER.RANK))
		{
			//System.out.println("node reached, arbeite mit pp: "+this.ppOrder);
			//BottomUp-Suche
			if (tMode== TRAVERSAL_MODE.BOTTOM_UP)
			{
				//alle Vaterknoten dieses Knotens ermitteln
				Vector<ICMAbstractDN> fatherDNs= this.getDominanceParents((ICMAbstractDN)currNode);
				boolean fatherOk= false;
				//gehe durch alle V�ter und pr�fe ob diese nicht mehr gerankt werden sollen
				for(ICMAbstractDN fatherDN: fatherDNs)
				{
					if ((ICMTokDN.class.isInstance(fatherDN)) || 
							(ICMStructDN.class.isInstance(fatherDN)))
					{
						fatherOk= true;
						break;
					}
				}
				//wenn dieser Knoten keine V�terknoten hat, die ebenfalls gerankt werden sollen 
				if (!fatherOk) this.rankRootDns.add((ICMAbstractDN)currNode);
			}
			//DepthFirst-Suche
			else if (tMode== TRAVERSAL_MODE.DEPTH_FIRST)
			{
				KorpusGraphMgrStatistics.graphTraversal(currNode.getClass().toString(),"nodeReached");
				//wenn es bereits einen Pre-Eintrag gibt, f�r DAG
				Vector<Long> preVec= null;
				if (currNode.hasAttName(KW_RANK_PRE_VAL))
				{
					preVec= (Vector<Long>)currNode.getAttValue(KW_RANK_PRE_VAL);
					preVec.add(this.ppOrder_RANK);
				}
				//wenn es noch keinen Pre-Eintrag gibt f�r B�ume und ersten Besuch im DAG
				else
				{
					preVec= new Vector<Long>();
					preVec.add(this.ppOrder_RANK);
					currNode.setAtt(KW_RANK_PRE_VAL, preVec);
				}
				this.ppOrder_RANK++;
				if (DEBUG) System.out.println("Knoten: "+currNode.getName());
			}
		}
		if (this.logger!= null) this.logger.debug(MSG_END_FCT + "nodeReached()");
	}
	
	/**
	 * Die Methode wird durch das Traversieren des IKMGraph aufgerufen und vergibt die 
	 * Post-Order. 
	 * @param tMode TRAVERSAL_MODE - Art der Traversierung
	 * @param currNode Node - aktueller Knoten, also der der gerade durch das Traversieren erreicht wurde (Veterknoten bei DEPTH_FIRST, Kinknoten bei BOTTOM_UP)
	 * @param edge Edge - Kante die die beiden Knoten verbindet
	 * @param fromNode Node - Knoten von dem aus der aktuelle Knoten erreicht wurde
	 * @param order long - Reihenfolge des Kindknotens in der Ordnung der Nachfolger des Vaterknotens (beginnend bei 0) 
	 */
	public void nodeLeft(	TRAVERSAL_MODE tMode, 
							Node currNode, 
							Edge edge,
							Node fromNode, 
							long order) throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_START_FCT + "nodeLeft("+currNode.getName()+")");
		// es soll die PP-Order f�r Korpusknoten erstellt werden
		if (this.modPPOrder.equals(MODE_PPORDER.CORPUS))
		{
			currNode.setAtt(KW_POST_VAL, this.ppOrder_CORP);
			this.ppOrder_CORP++;
			((CorpDN)currNode).toWriter((Long)currNode.getAttValue(KW_PRE_VAL), (Long)currNode.getAttValue(KW_POST_VAL));
		}
		//es soll die PP-Order f�r Collectionknoten erstellt werden
		else if (this.modPPOrder.equals(MODE_PPORDER.COLLECTION))
		{
			Vector<Long> postVec= null;
			if (currNode.hasAttName(KW_POST_VAL))
			{
				postVec= (Vector<Long>)currNode.getAttValue(KW_POST_VAL);
				postVec.add(this.ppOrder_COL);
			}
			//wenn es noch keinen Pre-Eintrag gibt f�r B�ume und ersten Besuch im DAG
			else
			{
				postVec= new Vector<Long>();
				postVec.add(this.ppOrder_COL);
				currNode.setAtt(KW_POST_VAL, postVec);
			}
			this.ppOrder_COL++;
			
			//rank-Eintrag in der Tabelle col_rank erstellen
			String pre= ((Vector<Long>)currNode.getAttValue(KW_PRE_VAL)).lastElement().toString();
			String post= ((Vector<Long>)currNode.getAttValue(KW_POST_VAL)).lastElement().toString();
			Vector<String> tuple= new Vector<String>();
			tuple.add(((RelationalDN)currNode).getRelID().toString());
			tuple.add(pre);
			tuple.add(post);
			//tuple in Writer schreiben
			this.colRankTWriter.addTuple(tuple);
		}
		// es soll die Post-Order f�r rank erstellt werden
		else if (this.modPPOrder.equals(MODE_PPORDER.RANK))
		{
			
			//suche ist DepthFirst und soll PPOrder f�r rank berechnen
			if (tMode== TRAVERSAL_MODE.DEPTH_FIRST)
			{
				KorpusGraphMgrStatistics.graphTraversal(currNode.getClass().toString(), "nodeLeft");
				if (DEBUG_CLOSE)
					System.out.println("create rank for node: "+currNode.getName());
				//wenn es bereits einen Pre-Eintrag gibt, f�r DAG
				Vector<Long> postVec= null;
				if (currNode.hasAttName(KW_RANK_POST_VAL))
				{
					postVec= (Vector<Long>)currNode.getAttValue(KW_RANK_POST_VAL);
					postVec.add(this.ppOrder_RANK);
				}
				//wenn es noch keinen Pre-Eintrag gibt f�r B�ume und ersten Besuch im DAG
				else
				{
					postVec= new Vector<Long>();
					postVec.add(this.ppOrder_RANK);
					currNode.setAtt(KW_RANK_POST_VAL, postVec);
				}
				this.ppOrder_RANK++;
				
				String pre= ((Vector<Long>)currNode.getAttValue(KW_RANK_PRE_VAL)).lastElement().toString();
				String post= ((Vector<Long>)currNode.getAttValue(KW_RANK_POST_VAL)).lastElement().toString();
				String fatherPre= dbConnector.getDBNULL();
				if (fromNode != null) 
					fatherPre= ((Vector<Long>)fromNode.getAttValue(KW_RANK_PRE_VAL)).lastElement().toString();
				else
				{
					//ge�ndert am 26-06-08
					//fatherPre= this.dbConnector.getStructAllPre(KW_ABS_REL_STRUCT, KW_ABS_REL_RANK).toString();
				}
				//TODO alle Kanten, die nach Rank geschrieben werden sollten mal das Interface RelationalEdge implementieren
				
//				if (fromNode!= null)
//					System.out.println("von: "+ fromNode.getName());
//				if (currNode!= null)
//					System.out.println(", nach: "+ currNode.getName());
//				if (edge== null) System.out.println("Problem");
//				else System.out.println(edge.getClass());
				
				//wenn Kante eine RelationalEdge-Kante ist, kann sie direkt geschrieben werden
//				System.out.println("Name: "+currNode.getName() + ", pre: "+ pre +", post: " +post);
				if (RelationalEdge.class.isInstance(edge))
				{
					((RelationalEdge)edge).toWriter(pre, post, fatherPre);
					//aus der Liste der Pre-Werte dieses Knotens muss der letzte Eintrag gel�scht werden
					((Vector<Long>)currNode.getAttValue(KW_RANK_PRE_VAL)).remove(((Vector<Long>)currNode.getAttValue(KW_RANK_PRE_VAL)).size()-1);
				}
				else
				{
					//System.out.println("schreibe in catch pre: "+ pre);
					if (DEBUG)
						System.out.println(	"name: " + currNode.getName() +
											", pre: " +currNode.getAttValue(KW_RANK_PRE_VAL)+ 
											", post: " +currNode.getAttValue(KW_RANK_POST_VAL));
					if (this.rankTWriter== null) throw new Exception(ERR_NO_RANK_TWRITER);
					String relID= null;
					// wenn Knoten vom Typ DocDN ist
					try
						{ relID= ((DocDN)currNode).getRelIDStruct().toString(); }
					//Knoten ist nicht vom Typ DocDN
					catch (Exception e)
						{relID= ((RelationalDN)currNode).getRelID().toString();}
					
					//Kante soll nur normal geschrieben werden, wenn der Vater nicht leer ist, denn sonst ist dieser Knoten ein EInstiegspunkt und damit selber der Vater
					if (fromNode != null)
					{
						//tuple f�r die Tabelle rank ertstellen
						Vector<String> tuple= new Vector<String>();
						tuple.add(pre);
						tuple.add(post);
						tuple.add(relID);
						tuple.add(fatherPre);
						//Flag setzen, dass besagt ob diese Kante eine Dominanzkante ist
						tuple.add(dbConnector.getDBTRUE());
						//tuple in Writer schreiben
						this.rankTWriter.addTuple(tuple);
						//throw new Exception("bl�de Dominanzkante");
					}
					//wenn Vater= null, dann muss diese Kante als Alibikante geschrieben werden, da es sein kann, dass sich andere Kanten darauf beziehen
					else if (fromNode== null)
					{
						Vector<String> tuple= new Vector<String>();
						tuple.add(pre);
						tuple.add(post);
						tuple.add(relID);
						tuple.add(dbConnector.getDBNULL());
						//Flag setzen, dass besagt ob diese Kante eine Dominanzkante ist
						tuple.add(dbConnector.getDBNULL());
						//tuple in Writer schreiben
						this.rankTWriter.addTuple(tuple);
					}
				}
			}
		}
		if (this.logger!= null) this.logger.debug(MSG_END_FCT + "nodeLeft()");
	}
	
	/**
	 * Pr�ft bei einem gegebenen Knoten, ob dieser ein bestimmtes Constraint erf�llt.
	 * <br/>
	 * Wenn die Pre- und Postorder f�r die Korpusebene errechnet werden soll, werden 
	 * folgende Knoten ber�cksichtigt:<br/>
	 * KorpDN
	 * <br/>
	 * Wenn die Pre- und Postorder f�r den rank errechner werden soll, werden folgende Knoten ber�cksichtigt:<br/>
	 * <ul>
	 * 	<li>DocDN</li>
	 * 	<li>TokDN</li>
	 * 	<li>StructDN</li>
	 *  <li>StructEdgeDN</li>
	 *  <li>StructRelDN</li>
	 * </ul>
	 * @param tMode TRAVERSAL_MODE - Modus der Traversion
	 * @param edge Edge - Kante �ber die dieser Knoten erreicht wurde
	 * @param currNode Node - aktueller zu pr�fender Knoten
	 * @return true, wenn weiter traversiert werden soll, false sonst
	 */
	public boolean checkConstraint(	TRAVERSAL_MODE tMode, 
									Edge edge, 
									Node currNode) throws Exception
	{
		boolean retVal= false;
		if (this.logger!= null) this.logger.debug(MSG_START_FCT + "checkConstraint()");
		// es soll die PP-Order f�r Korpusknoten erstellt werden
		if (this.modPPOrder.equals(MODE_PPORDER.CORPUS))
		{
			//pr�fen ob Knoten KorpDN ist
			if (currNode.getClass().equals(CorpDN.class))
				retVal= true ;
			else retVal= false;
		}
		//es soll die PP-Order f�r Collectionknoten erstellt werden
		else if (this.modPPOrder.equals(MODE_PPORDER.COLLECTION))
		{
			//pr�fen ob Knoten CollectionDN ist
			if (currNode.getClass().equals(CollectionDN.class))
				retVal= true;
			else retVal= false;
		}
		// es soll die PP-Order f�r rank erstellt werden
		else if (this.modPPOrder.equals(MODE_PPORDER.RANK))
		{
			
			if (tMode== TRAVERSAL_MODE.BOTTOM_UP)
			{
				if (PointingRelationEdge.class.isInstance(edge))
					retVal= false;
				else
				{
					//nicht weitersuchen, wenn aktueller Knoten kein IKMTokDN oder IKMStructDN ist
					if (	(ICMTokDN.class.isInstance(currNode)) || 
							(ICMStructDN.class.isInstance(currNode)))
						retVal= true;
					else retVal= false;
				}
			}
			else if (tMode== TRAVERSAL_MODE.DEPTH_FIRST)
			{
				//System.out.println("check context node: "+currNode.getName());
				//System.out.println("check context edge going to node: "+edge.getClass());
				//wenn als letztes eine Nicht-Dominanzkante gelesen wurde und deshalb nun f�lschlicherweise weitere Knoten gepr�ft werden
//				System.out.println("********* pr�fe nonEdge: "+ this.nonDEdgeRead);
//				System.out.println("********* habe Edge: "+ edge.getFromNode());
				if (this.nonDEdgeRead== edge.getFromNode())
				{
					retVal= false;
//					System.out.println("ABBORTED");
				}
				else
				{
					this.nonDEdgeRead= null;
					//nur traversieren, wenn Kante keine PointingRelation ist
					if (!PointingRelationEdge.class.isInstance(edge))
					{
						
						KorpusGraphMgrStatistics.graphTraversal(currNode.getClass().toString(), "checkConstraint");
						//System.out.println("checkConstraint for: "+currNode.getClass().toString());
						//es m�ssen DocDN, TokDN und StructDN durchsucht werden keine KorpDN
						//pr�fen ob Knoten TokDN ist
						if (currNode.getClass().equals(TokDN.class))
							retVal= true;
						//pr�fen ob Knoten StructDN ist
						else if (currNode.getClass().equals(StructDN.class))
							retVal= true;
						//pr�fen ob Knoten StructEdgeDN ist
						else if (currNode.getClass().equals(StructEdgeDN.class))
							retVal= true;
						//pr�fen ob Knoten StructRelDN ist
						else if (currNode.getClass().equals(StructRelDN.class))
							retVal= true;
						//System.out.println (MSG_END_FCT + "checkConstraint("+retVal+") for node: " +currNode.getClass());
					}
					else if (PointingRelationEdge.class.isInstance(edge))
					{
//						System.out.println("PointingRelation");
//						System.out.println("Kante: "+ edge);
//						System.out.println("from: "+ edge.getFromNode().getName());
//						System.out.println("to: "+ edge.getToNode().getName());
//						if (this.seenNonDEdges.contains(edge))
//							retVal= false;
//						else
						{
							//änderung seit 24.11.08
							/*
							this.seenNonDEdges.add((NonDominanceEdge)edge);
							this.nonDEdgeRead= (ICMAbstractDN)edge.getToNode();
							edge.addLabel("CGM:TRAVERSED", "true");
							retVal= true;
							
							//geändert 20.11.08
							this.nonDEdgeRead= null;
							*/
							this.seenNonDEdges.add((NonDominanceEdge)edge);
							this.nonDEdgeRead= (ICMAbstractDN)edge.getToNode();
							
							if ((edge.getLabel()!= null) && (edge.getLabel().get("CGM:TRAVERSED").equalsIgnoreCase("true")))
							{
								//System.out.println("hat ein label:  "+edge.getLabel().get("CGM:TRAVERSED"));
								retVal= false;
							}
							else 
							{
								Map<String, String> labels= new HashMap<String, String>();
								edge.addLabel("CGM:TRAVERSED", "true");
								edge.addLabel(labels);
								//System.out.println("no labels");
								retVal= true;
							}
							
							
							
							//geändert 20.11.08
							this.nonDEdgeRead= null;
						}
					}
				}
			}
		}
		//System.out.println("return from checkConstraint with: "+retVal);
		if (this.logger!= null) this.logger.debug(MSG_END_FCT + "checkConstraint("+retVal+")");
		return(retVal);
	}
// ------------------------- Ende XTraversalObject -------------------------
	
// ------------------------- Start Graphorganisation -------------------------	
	/**
	 * �bernimmt den gegebenen Knoten als neuen und aktuellen Dokumentenknoten. Es wird ein
	 * Eintrag auf der Dokumentenebene erzeugt. Der �bergebene Dokumentknoten wird 
	 * dem �bergebenen Korpusknoten angehangen.
	 * @param docDN DocDN - Der in die Dokumentenebene einzuf�gende Dokumentknoten
	 * @param korpDN KorpDN - Der Korpusknoten, dem der �bergebne Dokumentknoten unterstellt ist
	 */
	public void addDocDN(DocDN docDN, CorpDN korpDN) throws Exception
	{
		//Knoten ist erster Dokumentknoten
		if (this.docDNID== null)
			this.docDNID= this.dbConnector.getNewID(KW_ABS_REL_DOC_CORP, KW_REL_DOC_ID);
		//ge�ndert am 26-06-08
		//docDN.setRelID(this.docDNID);
		super.addDocDN(docDN, korpDN);
		//ge�ndert am 26-06-08
		//this.docDNID++;
	}
	
	/**
	 * �bernimmt den gegebenen Knoten als neuen und aktuellen Dokumentenknoten. Es wird ein
	 * Eintrag auf der Dokumentenebene erzeugt. Der �bergebene Knoten wird dem aktuellen
	 * Korpusknoten angehangen.
	 * @param docDN DocDN - Der in die Dokumentenebene einzuf�gende Dokumentknoten
	 */
	public void addDocDN(DocDN docDN) throws Exception
	{
		//Knoten ist erster Dokumentknoten
		if (this.docDNID== null)
			this.docDNID= this.dbConnector.getNewID(KW_ABS_REL_DOC_CORP, KW_REL_DOC_ID);
		
		//ge�ndert am 26-06-08
		//docDN.setRelID(this.docDNID);
		super.addDocDN(docDN);
		//ge�ndert am 26-06-08
		//this.docDNID++;
	}
	
	/**
	 * Closes the current document node under the current corpus node. The method also 
	 * begins writing all nodes under the document node (incl. document-node) onto
	 * the file-stream. For this it has to compute different values for nodes first
	 * (for example the pre and post order of all structural nodes). After writing
	 * the whole structure under document-node (excl. document-node) will be removed
	 * from main memory. 
	 * @throws Exception
	 */
	public void closeDocDN(String dotFileName) throws Exception
	{
		this.closeDocDN((DocDN)this.getCurrDocDN(), (CorpDN)this.getCurrKorpDN(), dotFileName);
	}
	
	/**
	 * Closes the given document node under the given corpus node. The method also 
	 * begins writing all nodes under the document node (incl. document-node) onto
	 * the file-stream. For this it has to compute different values for nodes first
	 * (for example the pre and post order of all structural nodes). After writing
	 * the whole structure under document-node (excl. document-node) will be removed
	 * from main memory. 
	 * @param docDN DocDN - the document-nodes, wich has to be written to stream and removed from main memory
	 * @param corpDN CorpDN - the corpus-node under wich the document nodes exists 
	 * @throws Exception
	 */
	public void closeDocDN(DocDN docDN, CorpDN corpDN, String dotFileName) throws Exception
	{
		//Fehler, wenn ein �bergebener Knoten leer ist
		if (docDN== null) throw new Exception(ERR_EMPTY_DOCDN_REM);
		if (corpDN== null) throw new Exception(ERR_EMPTY_CORPDN_REM);
		
		if (DEBUG_CLOSE_DOCDN)
		{
			System.out.println(MSG_STD + "closing document node: "+ docDN.getName() + " under corpus-node: " + corpDN.getName());
		}
		
		//Pre- und Postorder f�r die Strukturknoten schreiben
		this.createRankEntries(docDN);
		
		if ((dotFileName!= null) && (!dotFileName.equalsIgnoreCase("")))
			this.printGraph(dotFileName);
		
		//Knoten entfernen
		Collection<ICMAbstractDN> childNodes= this.getChilds(docDN);
		//alle Kindknoten des Dokumentknotens durchegehen
		for (ICMAbstractDN childNode: childNodes)
		{
			//System.out.println(MSG_STD + "removing node" + childNode.getName());
			this.korpGraph.removeNode(childNode);
		}
	}
	
	/**
	 * F�gt einen PrimDN (Prim�rdatenknoten) in den Graphen ein. Dieser wird
	 * dem aktuellen Dokumentknoten angehangen, sofern einer existiert.
	 * @param primDN PrimDN - Prim�rdatenknoten, der in den Graph eingef�gt werden soll
	 * @exception Fehler, wenn kein aktueller Dokumentknoten existiert
	 */
	public void addPrimDN(PrimDN primDN) throws Exception
	{
		super.addPrimDN(primDN);
		DocDN docDN= (DocDN)this.getDocDN(primDN);
		//setze PrimDN bei DocDN
		//docDN.setPrimDN(primDN);
	}
	
	/**
	 * F�gt einen einen StructDN als StructEdgeDN1 in den Korpusgraphen ein. Das bedeutet,
	 * dass Kanten zwischen diesem Knoten und den �bergebenen Referenzknoten gezogen werden.
	 * Weiter werden auch Kanten zu den Knoten auf die die Referenzknoten verweisen gezogen.
	 * @param structDN StructDN - Strukturdatenknoten, der in den Graph eingef�gt werden soll
	 * @param refNodes Vector<IKMAbstractDN> - Strukturdatenknoten auf die dieser Knoten zeigen soll
	 */
	public void addStructEdgeDN1(StructDN structDN, Vector<ICMAbstractDN> refNodes) throws Exception
	{
		//Enkelknoten suchen, also die Knoten auf die die Referenzknoten zeigen
		Vector<ICMAbstractDN> grandChildNodes= new Vector<ICMAbstractDN>();
		for (ICMAbstractDN refNode: refNodes)
		{
			//Enkelknoten ermitteln iund in Liste schreiben
			grandChildNodes.addAll(this.getChilds(refNode));
		}
		//Knoten als normalen Strukturknoten einf�gen
		this.addStructDN(structDN, refNodes);
		//Kante von diesem Knoten zu den Enkelknooten erzeugen
		for (ICMAbstractDN grandChildNode: grandChildNodes)
			this.korpGraph.createEdge(structDN, grandChildNode);
	}
	
	/**
	 * F�gt einen einen StructDN als StructRelDN1 in den Korpusgraphen ein. Das bedeutet,
	 * dass Kanten zwischen diesem Knoten und den �bergebenen Referenzknoten gezogen werden.
	 * Weiter werden auch Kanten zu den Knoten auf die die Referenzknoten verweisen gezogen.
	 * @param structDN StructDN - Strukturdatenknoten, der in den Graph eingef�gt werden soll
	 * @param refNodes Vector<IKMAbstractDN> - Strukturdatenknoten auf die dieser Knoten zeigen soll
	 */
	public void addStructRelDN(StructRelDN structDN, Vector<ICMAbstractDN> refNodes) throws Exception
	{
		//Methode StructDN aufrufen
		this.addStructDN(structDN, refNodes);
	}
	
	/**
	 * F�gt eine �bergebene Pointing relation in den Korpusgraph ein.
	 * @param pointingRelation NonDominanceEdge - einzuf�gende pointing relation
	 */
	public void addNonDominanceEdge(NonDominanceEdge edge) throws Exception
	{
		if (edge== null) throw new Exception(ERR_EMPTY_PR);
		this.korpGraph.createEdge(edge);
	}
	
	/**
	 * This method returns the root of all corpora (Super-corpus).
	 * @return the root corpus
	 * @throws Exception
	 */
	public CorpDN getRootCorpDN() throws Exception
	{
		CorpDN corpDN= (CorpDN)super.getRootCorpDN();
		if (corpDN== null)
			return(null);
		else return((CorpDN)super.getRootCorpDN());
	}
	
	/**
	 * Gibt den Korpus-Knoten zu dem �bergebenen Knotennamen zur�ck.
	 * @param nodeName String - Name des zu suchenden Knoten
	 * @return Knoten zu dem �bergebenen Namen
	 * @exception Fehler, wenn es keinen Knoten zu dem Namen gibt
	 */
	public CorpDN getCorpDN(String nodeName) throws Exception
		{ return((CorpDN)this.getDN(nodeName)); }
	
	/**
	 * Gibt den aktuellen Korpus-Knoten zur�ck.
	 * @return aktueller Korpus-Knoten
	 */
	public CorpDN getCurrCorpDN() throws Exception
		{ return((CorpDN)this.getCurrKorpDN()); }
	
	/**
	 * Berechnet im internen Graphen die Pre- und Post-Order f�r die Korpusebene.
	 */
	public void computeCorpPPOrder() throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_START_FCT + "computeKorpPPOrder()");
		//ppOrder aus der Datenbank ermitteln und setzen
		this.ppOrder_CORP= this.dbConnector.getNewPPVal(KW_ABS_REL_KORP);
		//System.out.println("holePPOrder f�r Corpus aus DB: "+this.ppOrder_CORP);
		this.modPPOrder= MODE_PPORDER.CORPUS;
		//this.korpPPOrder= true;
		this.korpGraph.depthFirst(this.rootKorpDN, this);
		//Post-Wert des ALL-Tupels aktualisieren
		this.dbConnector.updateKorpusAll(KW_ABS_REL_KORP, this.ppOrder_CORP);
		if (this.logger!= null) this.logger.debug(MSG_END_FCT + "computeKorpPPOrder()");
		this.modPPOrder= MODE_PPORDER.NON;
		//this.korpPPOrder= false;
	}
	
	/**
	 * Gibt alle Nachfahren des �bergebenen Kontextknotens zur�ck.
	 * @param currNode IKMAbstractDN - Kontextknoten zu dem die Nachfahren gesucht werden
	 * @return Liste aller Nachfahren des Kontextknotens
	 */
	public Vector<ICMAbstractDN> getChilds(ICMAbstractDN currNode) throws Exception
	{
		return(this.korpGraph.getDominanceChilds(currNode));
	}
	
	/**
	 * Gibt zur�ck, ob die Referenzknoten euf eine kontinuierliche Menge an Tokenknoten
	 * verweisen. Dies geschieht anhand des Order-Wertes der Tokenknoten. Die Tokenknoten
	 * werden rekursiv ermittelt.
	 * @param refNodes Vector<IKMAbstractDN> - Refernzknoten, derenn refernzierte Tokenknoten �berpr�ft werden
	 * @return true, wenn die refernzierten Tokenknoten kontinuierlich sind, false sonst
	 */
	public boolean isContinuous(Vector<ICMAbstractDN> refNodes) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "isContinuous()"); 
		boolean retVal= false;
		Vector<ICMTokDN> tokNodes= new Vector<ICMTokDN>();	//Liste aller Tokenknoten
		//gehe alle Knoten in der refNodes Liste durch
		for (ICMAbstractDN absDN : refNodes)
		{
			
			Vector<ICMTokDN> childs= this.getIKMTokDNs(absDN);
			if (!childs.isEmpty()) tokNodes.addAll(childs);
		}
		//es gibt keine Tokenknoten auf die verwiesen wird
		if (tokNodes.isEmpty()) retVal= true;
		//es gibt Tokenknoten, pr�fen ob diese Kontinuierlich sind
		else
		{
			//schreibe alle Positionen in einen Array
			long[] poses= new long[tokNodes.size()];
			int i= 0;
			for (ICMTokDN tokDN: tokNodes)
			{
				poses[i]= tokDN.getPos(); 
				i++;
			}
			//Sortiere alle Positionen
			Arrays.sort(poses);
			Long oldPos= null;
			boolean isCont= true;
			//Pr�fe ob es eine Position einen abstand von 1 zur vorderen hat, ist dem nicht so, sind die Token nicht kontinuierlich
			for (long pos: poses)
			{
				if (oldPos== null) oldPos= pos;
				else if (pos== oldPos +1) oldPos= pos;
				else 
				{
					isCont= false;
					break;
				}
			}
			retVal= isCont;
		}
			
		if (DEBUG) System.out.println(MSG_END_FCT + "isContinuous()");
		return(retVal);
	}
	
	/**
	 * Gibt alle Vorfahren des �bergebenen Kontextknotens zur�ck.
	 * @param currNode IKMAbstractDN - Kontextknoten zu dem die Nachfahren gesucht werden
	 * @return Liste aller Nachfahren des Kontextknotens
	 */
//	public Vector<ICMAbstractDN> getParents(ICMAbstractDN currNode) throws Exception
//	{
//		return(this.korpGraph.getParents(currNode));
//	}
	
	/**
	 * Gibt alle Vorfahren des �bergebenen Kontextknotens zur�ck, 
	 * die �ber eine Dominanz-Kante erreicht werden k�nnen.
	 * @param currNode IKMAbstractDN - Kontextknoten zu dem die Nachfahren gesucht werden
	 * @return Liste aller Nachfahren des Kontextknotens
	 */
	public Vector<ICMAbstractDN> getDominanceParents(ICMAbstractDN currNode) throws Exception
	{
		return(this.korpGraph.getDominanceParents(currNode));
	}
// ------------------------- Ende Graphorganisation -------------------------
	
	/**
	 * Gibt Informationen �ber dieses Objekt als String zur�ck. 
	 * @return String - Informationen �ber dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		 retStr= "this method isn�t implemented";
		return(retStr);
	}

}
