package relANNIS_2_0;

import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.IDTupleWriter;
import relANNIS_2_0.relANNISDAO.TupleWriter;
import internalCorpusModel.ICMPrimDN;

public class PrimDN extends ICMPrimDN implements RelationalDN
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PrimDN";		//Name dieses Tools
	// statische Attribute für Factory
	protected static Logger logger= null;				//logger für log4j
	protected static DBConnector dbConnector= null;		//DB-Verbindungsobjekt
	protected static String absRelName=null;			//speichert den abstrakten Namen für die Relation, in die dieser Knoten geschrieben wird
	protected static boolean factoryInit= false;		//gibt an, ob die Factory initialisiert wurde
	
	//private static KorpusGraphMgr kGraphMgr= null;		//KorpusGraph Manager, der diesen Knoten einfügt
	private String name= null;							//Name des Primärtextes
	private Long relID= null;							//relationale ID dieses Knotens
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_INIT=			MSG_STD + "Factory for "+TOOLNAME+" is initialized.";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DBCON=		MSG_ERR + "Cannot initialize the Factory, the given db-connector-object is empty.";
	private static final String ERR_EMPTY_KGRAPHMGR=	MSG_ERR + "Cannot initialize the Factory, the given korpus-graph-object is empty.";
	private static final String ERR_FACTORY_NOT_INIT=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the factory is not initilaized. Call "+TOOLNAME+".initFactory() first.";
	private static final String ERR_EMPTY_ABSRELNAME=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the abstract relation name is empty.";
//	 ============================================== statische Metthoden ==============================================
	/**
	 * Initialisiert die Factory zum erzeugen von KorpDN-Objekten.
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
		PrimDN.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn gültig
		if (kGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		//PrimDN.kGraphMgr= kGraphMgr;
		
		//prüfen ob DB-Verbinder gültig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		PrimDN.dbConnector= dbConnector;
		
		//abstrakten relNamen setzen
		if ((absRelName== null)||(absRelName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		PrimDN.absRelName= absRelName;
		
		PrimDN.factoryInit= true;
	}

//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt einen neuen Knoten vom Typ IKMPrimDN. Dieser Knoten reräsentiert einen 
	 * Primärdatenknoten. Es wird der Name des Primärdatenknotens und dessen Text gesetzt.
	 * @param uniqueName String - eindeutiger Name (Name des Knotens) 
	 * @param name String - Name des Primärdatenknotens 
	 * @param text String - Textwert des Knotens
	 * @param colDN CollectionDN - ein Knoten vom Typ CollectionDN, auf den dieser PrimDN Knoten verweist, wird keiner übergeben, wird ein NULL-Wert in der DN eingetragen
	 */
	public PrimDN(	String uniqueName,
					String name,
					String text,
					CollectionDN colDN) throws Exception
	{
		super(uniqueName, text, 0, text.length());
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!DocDN.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		this.name= name;
		this.toWriter(colDN);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Schreibt diesen Knoten auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt. 
	 * zu schreibende Werte: <br/>
	 * <ul>
	 * 	<li>id 				numeric(38) NOT NULL,</li>
	 * 	<li>name			character varying(150)	NOT NULL,</li>
	 * 	<li>"text" 			text,</li>
	 * 	<li>col_ref			numeric(38),</li>
	 * </ul>
	 * @param colDN CollectionDN - ein Knoten vom Typ CollectionDN, auf den dieser PrimDN Knoten verweist, wird keiner übergeben, wird ein NULL-Wert in der DN eingetragen
	 */
	private void toWriter(CollectionDN colDN) throws Exception
	{
		TupleWriter tWriter= PrimDN.dbConnector.getTWriter(PrimDN.absRelName);
		Vector<String> tuple= new Vector<String>();
		tuple.add(this.name);
		tuple.add(this.getText());
		//Verweis zum ColDN erzeugen, wenn einer existiert
		if (colDN!= null) tuple.add(colDN.getRelID().toString());
		else tuple.add(dbConnector.getDBNULL());
		this.relID= ((IDTupleWriter)tWriter).addTuple2(tuple);
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Gibt die relationale ID dieses Knotens zurück, sofern es eine gibt. Es wird
	 * null zurückgegeben, wenn keine relationale ID vorhanden.
	 * @return relationale ID
	 */
	public Long getRelID()
		{ return(this.relID); }
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		 retStr= "this method isn´t implemented";
		return(retStr);
	}
}