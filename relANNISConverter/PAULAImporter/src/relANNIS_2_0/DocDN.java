package relANNIS_2_0;

import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.IDTupleWriter;
import relANNIS_2_0.relANNISDAO.TupleWriter;

public class DocDN extends internalCorpusModel.ICMDocDN implements RelationalDN
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"DocDN";		//Name dieses Tools
	
	// statische Attribute für Factory
	protected static Logger logger= null;				//logger für log4j
	protected static DBConnector dbConnector= null;		//DB-Verbindungsobjekt
	/**
	 * speichert den abstrakten Namen für die Relation, in die dieser Knoten 
	 * geschrieben wird. Dies ist die Tabelle um einen Dokumenteintrag zu machen.
	 */
	protected static String absRelNameDoc=null;	
	
	/**
	 * speichert den abstrakten Namen für die Relation, in die dieser Knoten 
	 * geschrieben wird. Dies ist die Tabelle in der die Zuordnung von Document zu
	 * Corpus enthalten ist.
	 */
	protected static String absRelNameDoc2Corp=null;
	/**
	 * speichert den abstrakten Namen für die Relation, in die dieser Knoten 
	 * geschrieben wird. Dies ist die Tabelle einen Wurzeleintrag für alle Token-
	 * und Struktur-objekte zu machen.
	 */
	//protected static String absRelNameRoot=null;
	protected static boolean factoryInit= false;		//gibt an, ob die Factory initialisiert wurde
	
	private String name= null;							//Name des Primärtextes
	/**
	 * Verweis auf den aktuellen Korpusknoten
	 */
	protected CorpDN corpDN= null;
	//private static CorpusGraphMgr kGraphMgr= null;		//KorpusGraph Manager, der diesen Knoten einfügt
	
	private Long relID= null;								//relationale ID dieses Knotens(Knoten als Doc)
	private Long relIDStruct= null;								//relationale ID dieses Knotens(Knoten als Struct)
	//private PrimDN primDN= null;							//Primärdatenknoten, auf den dieser Knoten sich bezieht
//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_INIT=			MSG_STD + "Factory for "+TOOLNAME+" is initialized.";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DBCON=		MSG_ERR + "Cannot initialize the Factory, the given db-connector-object is empty.";
	private static final String ERR_EMPTY_KGRAPHMGR=	MSG_ERR + "Cannot initialize the Factory, the given korpus-graph-object is empty.";
	private static final String ERR_FACTORY_NOT_INIT=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the factory is not initilaized. Call "+TOOLNAME+".initFactory() first.";
	private static final String ERR_EMPTY_ABSRELNAME=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the abstract relation name is empty.";
	private static final String ERR_NO_RELID=			MSG_ERR + "Cannot write "+TOOLNAME+"-object, beacause the 'relID' isn´t set.";
	//private static final String ERR_NO_PRIMDN=			MSG_ERR + "Cannot write "+TOOLNAME+"-object, because the primDN is not set. Please call setPrimDN() first.";
	private static final String ERR_EMPTY_CORPDN=		MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the given corpus node is empty.";
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
									String absRelNameDoc,
									String absRelNameDoc2Corp,
									Logger logger) throws Exception
	{
		DocDN.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn gültig
		if (kGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		//DocDN.kGraphMgr= kGraphMgr;
		
		//prüfen ob DB-Verbinder gültig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		DocDN.dbConnector= dbConnector;
		
		//abstrakten relNamen für Dokumenttabelle setzen
		if ((absRelNameDoc== null)||(absRelNameDoc.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		DocDN.absRelNameDoc= absRelNameDoc;
		
		//abstrakten relNamen für Wurzelknotentabelle setzen
		if ((absRelNameDoc2Corp== null)||(absRelNameDoc2Corp.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		DocDN.absRelNameDoc2Corp= absRelNameDoc2Corp;
		
		DocDN.factoryInit= true;
	}
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein Korpusknoten-Objekt.
	 * @param name String - Name des des Primärtextes 
	 * @param uniqueName String - eindeutiger Name (Name des Knotens)
	 */
	public DocDN(	String name,
					String uniqueName,
					CorpDN corpDN) throws Exception
	{
		super(uniqueName);
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!DocDN.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		
		if (corpDN == null) throw new Exception(ERR_EMPTY_CORPDN);
		this.corpDN= corpDN;
		
		this.name= name;
		
		//Knoten auf TupleWriter schreiben
		this.toWriter();
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Setzt den Primärdatenknoten, auf den sich dieser Dokumentknoten bezieht.
	 * @param primDN PrimDN - Primärdatenknoten
	 */
	/*
	public void setPrimDN(PrimDN primDN)
		{ this.primDN= primDN; }
	*/
	
	/**
	 * Schreibt diesen Knoten auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt. Diese erstellt einen Wurzelknoten für alle
	 * Struktur und Token-objekte.
	 * zu schreibende Werte: <br/>
	 * <ul>
	 * 	<li>id 			numeric(38)	NOT NULL,</li>
	 *	<li>text_ref 	numeric(38),</li>
	 *	<li>col_ref		numeric(38),</li>
	 *	<li>doc_id		numeric(38),</li>			
	 *	<li>name 		character varying(100) NOT NULL,</li>
	 *	<li>"left" 		numeric(38),</li>
	 *	<li>"right" 	numeric(38),</li>
	 *	<li>"order"		numeric(38),</li>
	 *	<li>cont 		boolean,</li>
	 *	<li>"text"		text,</li>
	 * </ul>
	 * @deprecated DocDN soll nicht mehr in struct-Tabelle geschrieben werden, Änderung am 06-05-08
	 */
	/*
	public void toWriterStruct() throws Exception
	{
		if (this.primDN== null) throw new Exception(ERR_NO_PRIMDN);
		//Fehler wenn relID nicht gesetzt wurde
		if (this.relID== null) throw new Exception(ERR_NO_RELID);
		TupleWriter tWriter= DocDN.dbConnector.getTWriter(DocDN.absRelNameRoot);
		Vector<String> tuple= new Vector<String>();
		//füge tuple in Writer ein
		//Tupel erstellen
		//text_ref-Wert setzen
		tuple.add(primDN.getRelID().toString());
		//collection-Verweis setzen
		tuple.add(dbConnector.getDBNULL());
		//doc_id-Wert setzen
		tuple.add(this.getRelID().toString());
		//name-Wert setzen
		tuple.add("DOCUMENT");
		//left-Wert setzen
		tuple.add(((Long)this.primDN.getLeft()).toString());
		//right-Wert setzen
		tuple.add(((Long)this.primDN.getRight()).toString());
		//order-Wert setzen
		tuple.add(dbConnector.getDBNULL());
		//cont-Wert setzen
		tuple.add("false");
		//text-Wert setzen
		tuple.add(dbConnector.getDBNULL());
		//tuple auf writer schreiben
		this.relIDStruct= ((IDTupleWriter)tWriter).addTuple2(tuple);
	}*/
	
	/**
	 * Schreibt diesen Knoten auf die beiden entsprechenden TupleWriter. 
	 * Dieser wird über das dbConnector-Objekt ermittelt.
	 * Diese writeMethode macht einen Eintrag in Dokumenttabelle:
	 * <ul>
	 * 	<li>id				numeric(38)	NOT NULL</li
	 * 	<li>name			character varying(100) NOT NULL,</li>
	 * </ul>
	 * und in die Dcument-corpus-ZuordnungsTabelle
	 * <ul>
	 * 	<li>doc_ref				numeric(38)	NOT NULL,</li>
	 * 	<li>corpus_ref			numeric(38)	NOT NULL,</li>
	 * </ul>
	 */
	private void toWriter() throws Exception
	{
		IDTupleWriter tWriter= (IDTupleWriter)DocDN.dbConnector.getTWriter(DocDN.absRelNameDoc);
		Vector<String> tuple= new Vector<String>();
		//füge tuple in Writer ein
		tuple.add(this.name);
		this.relID= tWriter.addTuple2(tuple);
		this.toWriterDoc2Corp();
	}
	
	/**
	 * Schreibt diesen Knoten auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt. Diese writeMethode macht einen Eintrag in
	 * der Dokumenttabelle
	 */
	private void toWriterDoc2Corp() throws Exception
	{
		//Fehler wenn relID nicht gesetzt wurde
		if (this.relID== null) throw new Exception(ERR_NO_RELID);
		TupleWriter tWriter= DocDN.dbConnector.getTWriter(DocDN.absRelNameDoc2Corp);
		Vector<String> tuple= new Vector<String>();
		
		//geändert am 26-06-08
		// suche Korpusknoten, der Vater dieses Knotens ist
		//CorpDN korpDN= (CorpDN) kGraphMgr.getDominanceParents(this).firstElement();
		
		//füge tuple in Writer ein
		tuple.add(this.relID.toString());
		//tuple.add(korpDN.getRelID().toString());
		tuple.add(this.corpDN.getRelID().toString());
		tWriter.addTuple(tuple);
	}
	
	/**
	 * Setzt die relationale ID dieses Knotens auf den übergebenen ID-Wert. 
	 * @param relID long - relationale ID für dieses Objekt
	 */
	//public void setRelID(long relID)
	//	{ this.relID= relID;}
	
	/**
	 * Gibt die relationale ID dieses Knotens zurück, sofern es eine gibt (wenn dieser als
	 * Dokumentknoten betrachtet wird). Es wird null zurückgegeben, wenn keine relationale 
	 * ID vorhanden.
	 * @return relationale ID
	 */
	public Long getRelID()
		{ return(this.relID); }
	
	/**
	 * Gibt die relationale ID dieses Knotens zurück, sofern es eine gibt (wenn dieser als
	 * Structknoten betrachtet wird). Es wird null zurückgegeben, wenn keine relationale 
	 * ID vorhanden.
	 * @return relationale ID
	 */
	public Long getRelIDStruct()
		{ return(this.relIDStruct); }
	
	
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
