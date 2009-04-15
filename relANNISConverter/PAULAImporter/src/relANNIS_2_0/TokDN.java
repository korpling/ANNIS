package relANNIS_2_0;

import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.IDTupleWriter;
import relANNIS_2_0.relANNISDAO.TupleWriter;
import internalCorpusModel.ICMTokDN;

public class TokDN extends ICMTokDN implements RelationalDN, TextedDN
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"TokDN";		//Name dieses Tools
	private static final String KW_NODE_TYPE=	"STRUCT";		//Name für den Knotentyp im relANNIS Modell
	
	// statische Attribute für Factory
	protected static Logger logger= null;				//logger für log4j
	protected static DBConnector dbConnector= null;		//DB-Verbindungsobjekt
	protected static String absRelName=null;			//speichert den abstrakten Namen für die Relation, in die dieser Knoten geschrieben wird
	protected static boolean factoryInit= false;		//gibt an, ob die Factory initialisiert wurde
	
	private static CorpusGraphMgr kGraphMgr= null;		//KorpusGraph Manager, der diesen Knoten einfügt
	
	/**
	 * Keyword for adressing the attribute namespace
	 */
	private static String KW_NS= "ns"; 
	
	
	private Long relID= null;							//relationale ID dieses Knotens
	private PrimDN primDN= null;						//Primärdatenknoten, auf den sich dieser Tokenknoten bezieht
	/**
	 * Typ dieses Knotens (welchen Strukturtypen stellt dieser Knoten dar)
	 */
	protected String type= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_INIT=			MSG_STD + "Factory for "+TOOLNAME+" is initialized.";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DBCON=		MSG_ERR + "Cannot initialize the Factory, the given db-connector-object is empty.";
	private static final String ERR_EMPTY_KGRAPHMGR=	MSG_ERR + "Cannot initialize the Factory, the given korpus-graph-object is empty.";
	private static final String ERR_FACTORY_NOT_INIT=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the factory is not initilaized. Call "+TOOLNAME+".initFactory() first.";
	private static final String ERR_EMPTY_ABSRELNAME=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the abstract relation name is empty.";
	private static final String ERR_EMPTY_PRIMDN=		MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the given primqry data node is empty.";
	private static final String ERR_EMPTY_TYPE=			MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the given type is empty.";
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
		TokDN.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn gültig
		if (kGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		TokDN.kGraphMgr= kGraphMgr;
		
		//prüfen ob DB-Verbinder gültig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		TokDN.dbConnector= dbConnector;
		
		//abstrakten relNamen setzen
		if ((absRelName== null)||(absRelName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		TokDN.absRelName= absRelName;
		
		TokDN.factoryInit= true;
	}

//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt einen neuen Knoten vom Typ IKMTokDN. Dieser Knoten reräsentiert einen 
	 * Primärdatenknoten. Es wird der Name des Primärdatenknotens und dessen Text gesetzt.
	 * @param uniqueName String - eindeutiger Name (Name des Knotens)
	 * @param name String - Name des Tokenknotens
	 * @param ns String - Namensraum des type-Attributes
	 * @param type String - Typ dieses Knotens (welchen Strukturtypen stellt dieser Knoten dar), darf nicht leer sein
	 * @param primDN PrimDN - Primärdatenknoten, auf den dieses Objekt verweist
	 * @param colDN CollectionDN - ein Knoten vom Typ CollectionDN, auf den dieser PrimDN Knoten verweist, wird keiner übergeben, wird ein NULL-Wert in der DN eingetragen
	 * @param left long - linke Textgrenze im Bezugstext
	 * @param right long - rechte Textgrenze im Bezugstext
	 * @param text String - Textwert des Knotens 
	 */
	public TokDN(	String uniqueName,
					String name,
					String ns,
					String type,
					PrimDN primDN,
					CollectionDN colDN,
					long left,
					long right,
					long order) throws Exception
	{
		//ruft den Superkonstruktor auf
		super(uniqueName, primDN.getTextInterval(left, right), left, right, order);

		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!TokDN.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		
		//Fehler, wenn Typ leer ist
		if ((type== null) || (type.equalsIgnoreCase("")))
			throw new Exception(ERR_EMPTY_TYPE);
		this.type= type;
		
		//Namespace dieses Typ-Attributes setzen
		this.setAtt(TOOLNAME, KW_NS, ns);
		
		//Fehler, wenn primDN nicht leer ist
		if (primDN== null) throw new Exception(ERR_EMPTY_PRIMDN);
		this.primDN= primDN;
		this.toWriter(primDN, colDN);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Schreibt diesen Knoten auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt. 
	 * zu schreibende Werte: <br/>
	 * <ul>
	 * 	<li>id 			numeric(38)	NOT NULL,</li>
	 *	<li>text_ref 	numeric(38),</li>
	 *	<li>col_ref		numeric(38),</li>
	 *	<li>doc_id		numeric(38),</li>			
	 *	<li>ns 		character varying(100) NOT NULL,</li>
	 *	<li>name 		character varying(100) NOT NULL,</li>
	 *	<li>"left" 		numeric(38),</li>
	 *	<li>"right" 	numeric(38),</li>
	 *	<li>"order"		number(38)</li>
	 *	<li>cont 		boolean,</li>
	 *	<li>"text"		text,</li>
	 *	
	 * </ul>
	 * @param primDN PrimDN - Primärdatenknoten, auf den sich dieser Knoten bezieht
	 * @param colDN CollectionDN - ein Knoten vom Typ CollectionDN, auf den dieser PrimDN Knoten verweist, wird keiner übergeben, wird ein NULL-Wert in der DN eingetragen
	 */
	private void toWriter(PrimDN primDN, CollectionDN colDN) throws Exception
	{
		TupleWriter tWriter= TokDN.dbConnector.getTWriter(TokDN.absRelName);
		Vector<String> tuple= new Vector<String>();
		//Tupel erstellen
		//text_ref-Wert setzen
		tuple.add(primDN.getRelID().toString());
		//Verweis zum ColDN erzeugen, wenn einer existiert
		if (colDN!= null) tuple.add(colDN.getRelID().toString());
		else tuple.add(dbConnector.getDBNULL());
		//doc_ref-Wert setzen
		tuple.add(((DocDN)TokDN.kGraphMgr.getDocDN(primDN)).getRelID().toString());
		//Namensraum des Typ-Attributes setzen
		tuple.add((String)this.getAttValue(TOOLNAME, KW_NS));
		//name-Wert setzen
		tuple.add(KW_NODE_TYPE);
		//type-Wert setzen
		tuple.add(this.type);
		//left-Wert setzen
		tuple.add(((Long)left).toString());
		//right-Wert setzen
		tuple.add(((Long)right).toString());
		//order-Wert setzen
		tuple.add(((Long)pos).toString());
		//cont-Wert setzen
		tuple.add(dbConnector.getDBTRUE());
		//text-Wert setzen
		tuple.add(this.text);
		this.relID= ((IDTupleWriter)tWriter).addTuple2(tuple);
	}
//	 ============================================== öffentliche Methoden ==============================================
	// -------------------------- Methoden von TextedDN --------------------------	
	/**
	 * Gibt den in diesem Knoten gespeicherten Text zurück.
	 * @return Text zu diesem Knoten
	 */
	public PrimDN getPrimDN() throws Exception
	{return(this.primDN);}
	
	/**
	 * Gibt die linke Textgrenze eines in diesem Knoten gespeicherten Textes zurück.
	 * @return linke Textgrenze zu diesem Text
	 */
	public Long getLeft() throws Exception
	{	return(this.left);}
	
	/**
	 * Gibt die rechte Textgrenze eines in diesem Knoten gespeicherten Textes zurück.
	 * @return rechte Textgrenze zu diesem Text
	 */
	public Long getRight() throws Exception
	{	return(this.right);}
	
	/**
	 * Gibt zurück, ob der Text in diesem Textknoten kontinuierlich ist oder nicht.
	 * Ein Token hat immer einen kontinuierlichen Textbereich, daher wird immer true 
	 * zurückgegeben.
	 * @return true, wenn Text kontinuierlich ist, false sonst
	 */
	public boolean getCont()
	{	return(true); }
// -------------------------- Ende Methoden von TextedDN --------------------------
	
	
	
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
