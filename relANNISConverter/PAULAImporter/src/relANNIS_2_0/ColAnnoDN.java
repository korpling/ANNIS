package relANNIS_2_0;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.TupleWriter;
import internalCorpusModel.ICMColAnnoDN;

public class ColAnnoDN extends ICMColAnnoDN
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"ColAnnoDN";		//Name dieses Tools
	private static final boolean DEBUG= 	false;			//DEBUG-Schalter
		
	// statische Attribute für Factory
	protected static Logger logger= null;				//logger für log4j
	protected static DBConnector dbConnector= null;		//DB-Verbindungsobjekt
	protected static String absRelName=null;		//speichert den abstrakten Namen für die Relation, in die dieser Knoten geschrieben wird (Anno)
	protected static boolean factoryInit= false;		//gibt an, ob die Factory initialisiert wurde
	
	//private static KorpusGraphMgr kGraphMgr= null;		//KorpusGraph Manager, der diesen Knoten einfügt
	
	private Long relID= null;	
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_INIT=			MSG_STD + "Factory for "+TOOLNAME+" is initialized.";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DBCON=		MSG_ERR + "Cannot initialize the Factory, the given db-connector-object is empty.";
	private static final String ERR_EMPTY_KGRAPHMGR=	MSG_ERR + "Cannot initialize the Factory, the given korpus-graph-object is empty.";
	private static final String ERR_FACTORY_NOT_INIT=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the factory is not initilaized. Call "+TOOLNAME+".initFactory() first.";
	private static final String ERR_EMPTY_ABSRELNAME=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the abstract relation name is empty.";
	private static final String ERR_NO_COLDNS=			MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause there is no colDN-object node given as reference .";
	
//	 ============================================== statische Metthoden ==============================================
	
	/**
	 * Initialisiert die Factory zum erzeugen von ColAnnoDN-Objekten.
	 * Gesetzt wird hier:<br/>
	 * <ul>
	 *  <li>der Graph manager, in den dieser Knoten geschrieben werden soll</li>
	 * 	<li>der logger für log4j</li>
	 * 	<li>abstrakter Name der Relation, in die der Knoten geschrieben werden soll</li>
	 *  <li>das Datenbankverbindungsobjekt</li>
	 * <ul/>
	 * @param kGraphMgr KorpusGraph - der Graph, in den dieser Knoten geschrieben werden soll
	 * @param dbConnector DBConnector - DB-Verbindungsobjekt
	 * @param absRelName String - abstrakter Name der Relation, in die der Knoten geschrieben werden soll 
	 * @param logger Logger - Logger für log4j
	 */
	public static void initFactory(	CorpusGraphMgr kGraphMgr,
									DBConnector dbConnector, 
									String absRelName,
									Logger logger) throws Exception
	{
		ColAnnoDN.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn gültig
		if (kGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		//ColAnnoDN.kGraphMgr= kGraphMgr;
		
		//prüfen ob DB-Verbinder gültig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		ColAnnoDN.dbConnector= dbConnector;
		
		//abstrakten relNamen setzen
		if ((absRelName== null)||(absRelName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		ColAnnoDN.absRelName= absRelName;
		
		ColAnnoDN.factoryInit= true;
	}

//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt einen neuen Knoten vom Typ IKMAnnoDN. Dieser Knoten reräsentiert einen 
	 * Strukturdatenknoten. 
	 * @param uniqueName String - eindeutiger Name (Name des Knotens)
	 * @param name String - Name des des Strukturknotens
	 * @param refNodes Vector<TextedDN>- Knoten auf die sich dieser Knoten bezieht
	 * @param attValPairs Hashtable<String, String> - Tabelle der Annotationen, Zu dem gegebenen Attributwert in String1, wird ein Value in der DB abgelegt (String 2)
	 * @param annoLevelName String -Name des Annotationslevels
	 * @param colDN CollectionDN - Knoten vom Typ CollectionDN, auf den dieser ColAnnoDN Knoten verweist 
	 */
	public ColAnnoDN(	String uniqueName,
						CollectionDN colDN,			
						Hashtable<String, String> attValPairs) throws Exception
	{
		//ruft den Superkonstruktor mit Dummydaten auf
		super(uniqueName, attValPairs);

		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!ColAnnoDN.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		if (DEBUG) System.out.println(MSG_STD + "name: " + uniqueName);
		if (colDN== null)
			throw new Exception(ERR_NO_COLDNS);
		//schreibe dieses Tupel auf den Stream
		this.toWriter(colDN, attValPairs);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Schreibt diesen Knoten auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt. 
	 * zu schreibende Werte für die META_ATTRIBUTE-Tabelle: <br/>
	 * <ul>
	 * 	<li>col_ref		numeric(38)					NOT NULL,</li>
	 * 	<li>name		character varying(150)		NOT NULL,</li>
	 * 	<li>value		character varying(150),</li>
	 * 	</ul>
	 * @param colDN CollectionDN - ein Knoten vom Typ CollectionDN, auf den dieser ColAnnoDN Knoten verweist
	 * @param attValPairs Hashtable<String, String> - Attribut-Wert-Paare, die die Annotation(en) bilden
	 */
	private void toWriter(	CollectionDN colDN,
							Hashtable<String, String> attValPairs) throws Exception
	{
		//ermittle TupleWriter für die Tabelle meta_attribute
		TupleWriter colAnnoWriter = ColAnnoDN.dbConnector.getTWriter(ColAnnoDN.absRelName);
		//gehe alle Attribut-Wert-Paare durch
		Enumeration<String> keys= attValPairs.keys();
		while (keys.hasMoreElements())
		{
			String attName= keys.nextElement();
			String valName= attValPairs.get(attName);
			//erstelle Tupel
			Vector<String> tuple= new Vector<String>();
			//referenzID des CollectionDN eintragen
			tuple.add(colDN.getRelID().toString());
			//Attributname eintragen
			tuple.add(attName);
			//Attributwert eintragen
			tuple.add(valName);
			colAnnoWriter.addTuple(tuple);
		}
	}
//	 ============================================== öffentliche Methoden ==============================================
// -------------------------- Methoden von RelationalDN --------------------------	
	/**
	 * Gibt die relationale ID dieses Knotens zurück, sofern es eine gibt. Es wird
	 * null zurückgegeben, wenn keine relationale ID vorhanden.
	 * @return relationale ID
	 */
	public Long getRelID()
		{ return(this.relID); }
// -------------------------- Ende Methoden von RelationalDN --------------------------
	
}
