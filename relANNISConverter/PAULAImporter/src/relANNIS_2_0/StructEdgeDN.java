package relANNIS_2_0;

import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.IDTupleWriter;
import relANNIS_2_0.relANNISDAO.TupleWriter;
import internalCorpusModel.ICMAbstractDN;
import internalCorpusModel.ICMStructDN;

/**
 * Dise Klasse bietet einen Knotentyp der in dem relANNIS-Modell den Namen STRUCTEDGE trägt. 
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class StructEdgeDN extends ICMStructDN implements RelationalDN, TextedDN
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"StructEdgeDN";		//Name dieses Tools
	private static final boolean DEBUG= 	false;			//DEBUG-Schalter
	
	private static final String KW_DUMMY=		"DUMMY";			//ein Dummy-Text
	private static final String KW_NODE_TYPE=	"STRUCTEDGE";		//Name für den Knotentyp im relANNIS Modell
	// statische Attribute für Factory
	protected static Logger logger= null;				//logger für log4j
	protected static DBConnector dbConnector= null;		//DB-Verbindungsobjekt
	protected static String absRelName=null;			//speichert den abstrakten Namen für die Relation, in die dieser Knoten geschrieben wird
	protected static boolean factoryInit= false;		//gibt an, ob die Factory initialisiert wurde
	
	protected static CorpusGraphMgr kGraphMgr= null;		//KorpusGraph Manager, der diesen Knoten einfügt
	
	/**
	 * Keyword for adressing the attribute namespace
	 */
	private static String KW_NS= "ns"; 
	
	protected Long relID= null;							//relationale ID dieses Knotens
	protected PrimDN primDN= null;						//Primärdatenknoten, auf den sich dieser Tokenknoten bezieht
	protected boolean isCont= false;					//gibt an, ob diesrr Knoten auf eine kontinuierliche Menge an Tokenknoten verweist
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_INIT=			MSG_STD + "Factory for "+TOOLNAME+" is initialized.";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DBCON=		MSG_ERR + "Cannot initialize the Factory, the given db-connector-object is empty.";
	private static final String ERR_EMPTY_KGRAPHMGR=	MSG_ERR + "Cannot initialize the Factory, the given korpus-graph-object is empty.";
	private static final String ERR_FACTORY_NOT_INIT=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the factory is not initilaized. Call "+TOOLNAME+".initFactory() first.";
	private static final String ERR_EMPTY_ABSRELNAME=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the abstract relation name is empty.";
	//private static final String ERR_EMPTY_PRIMDN=		MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the given primqry data node is empty.";
	private static final String ERR_TWO_PRIMDN=			MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause this object refers two primary data nodes: ";
	private static final String ERR_NO_REFNODES=		MSG_ERR + "Cannot create "+TOOLNAME+"-object, because there are no reference nodes given.";
//	 ============================================== statische Metthoden ==============================================
	/**
	 * Initialisiert die Factory zum erzeugen von StructDN-Objekten.
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
		StructEdgeDN.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn gültig
		if (kGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		StructEdgeDN.kGraphMgr= kGraphMgr;
		
		//prüfen ob DB-Verbinder gültig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		StructEdgeDN.dbConnector= dbConnector;
		
		//abstrakten relNamen setzen
		if ((absRelName== null)||(absRelName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		StructEdgeDN.absRelName= absRelName;
		
		StructEdgeDN.factoryInit= true;
	}

//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt einen neuen Knoten vom Typ IKMStructDN. Dieser Knoten reräsentiert einen 
	 * Strukturdatenknoten. 
	 * @param uniqueName String - eindeutiger Name (Name des Knotens)
	 * @param name String - Name des des Strukturknotens
	 * @param ns String - Namensraum des type-Attributes
	 * @param colDN CollectionDN - ein Knoten vom Typ CollectionDN, auf den dieser PrimDN Knoten verweist, wird keiner übergeben, wird ein NULL-Wert in der DN eingetragen
	 * @param refNodes Vector<TextedDN>- Knoten auf die sich dieser Knoten bezieht
	 */
	public StructEdgeDN(	String uniqueName,
							String name,
							String ns,
							CollectionDN colDN,
							Vector<TextedDN> refNodes) throws Exception
	{
		//ruft den Superkonstruktor mit Dummydaten auf
		super(uniqueName, KW_DUMMY, 0, 1);

		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!StructEdgeDN.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		if ((refNodes== null) || (refNodes.isEmpty())) throw new Exception(ERR_NO_REFNODES);
		
		if (DEBUG) System.out.println(MSG_STD + "name: " + uniqueName + ", refs: "+ refNodes);
		this.computeVals(refNodes);
		
		//Namespace dieses Typ-Attributes setzen
		this.setAtt(TOOLNAME, KW_NS, ns);
		//schreibe dieses Tupel auf den Stream
		this.toWriter(colDN);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Errechnet aus den Knoten auf den sich dieser bezieht die Werte linke Grenze,
	 * rechte Grenze, kontinuität und Text aus.
	 * @param refNodes Vector<TextedDN>- Knoten auf die sich dieser Knoten bezieht
	 */
	private void computeVals(Vector<TextedDN> refNodes) throws Exception
	{
		Long minLeft= null;
		Long maxRight= null;
		for (TextedDN refNode: refNodes)
		{
			if (minLeft== null) minLeft= refNode.getLeft();
			else if (minLeft > refNode.getLeft()) minLeft= refNode.getLeft();
			if (maxRight== null) maxRight= refNode.getRight();
			else if (maxRight< refNode.getRight()) maxRight= refNode.getRight();
			if (this.primDN== null) this.primDN= refNode.getPrimDN();
			else if (this.primDN!= refNode.getPrimDN())
				throw new Exception(ERR_TWO_PRIMDN + this.primDN.getName() + ", " + refNode.getPrimDN().getName());
		}
		this.left= minLeft;
		this.right= maxRight;
		//Text neu setzen, da hier nur ein DUMMY steht
		this.text= this.getPrimDN().getTextInterval(this.left, this.right);
		
		//Casten der TextedDN in IKMAbstractDN
		Vector<ICMAbstractDN> absNodes= new Vector<ICMAbstractDN>();
		for (TextedDN textedDN: refNodes)
			absNodes.add((ICMAbstractDN) textedDN);
		
		//der Continuitätswert ermitteln
		this.isCont= kGraphMgr.isContinuous(absNodes);
	}
	
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
	 *	<li>type 		character varying(100) NOT NULL,</li>
	 *	<li>"left" 		numeric(38),</li>
	 *	<li>"right" 	numeric(38),</li>
	 *	<li>"order"		numeric(38),</li>
	 *	<li>cont 		boolean,</li>
	 *	<li>"text"		text,</li>
	 * </ul>
	 * @param colDN CollectionDN - ein Knoten vom Typ CollectionDN, auf den dieser PrimDN Knoten verweist, wird keiner übergeben, wird ein NULL-Wert in der DN eingetragen
	 */
	private void toWriter(CollectionDN colDN) throws Exception
	{
		TupleWriter tWriter= StructEdgeDN.dbConnector.getTWriter(StructEdgeDN.absRelName);
		Vector<String> tuple= new Vector<String>();
		//Tupel erstellen
		//text_ref-Wert setzen
		tuple.add(primDN.getRelID().toString());
		//Verweis zum ColDN erzeugen, wenn einer existiert
		if (colDN!= null) tuple.add(colDN.getRelID().toString());
		else tuple.add(dbConnector.getDBNULL());
		//doc_id-Wert setzen
		tuple.add(((DocDN)StructEdgeDN.kGraphMgr.getDocDN(primDN)).getRelID().toString());
		//Namensraum des Typ-Attributes setzen
		tuple.add((String)this.getAttValue(TOOLNAME, KW_NS));
		//name-Wert setzen
		tuple.add(KW_NODE_TYPE);
		//type-Wert setzen
		tuple.add(KW_NODE_TYPE);
		//left-Wert setzen
		//tuple.add(dbConnector.getDBNULL());
		//wenn left-Wert wirklich errechnet werden soll
		tuple.add(((Long)this.left).toString());
		//right-Wert setzen
		//tuple.add(dbConnector.getDBNULL());
		//wenn right-Wert wirklich errechnet werden soll
		tuple.add(((Long)this.right).toString());
		//order-Wert setzen
		tuple.add(dbConnector.getDBNULL());
		//cont-Wert setzen
		tuple.add(Boolean.toString(this.isCont));
		//text-Wert setzen
		//tuple.add(this.text);
		tuple.add(dbConnector.getDBNULL());
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
