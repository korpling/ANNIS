package relANNIS_2_0;

import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.IDTupleWriter;
import relANNIS_2_0.relANNISDAO.TupleWriter;

public class CollectionDN extends internalCorpusModel.ICMCollectionDN implements RelationalDN
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"CollectionDN";		//Name dieses Tools
	private static final boolean DEBUG= 	false;				//DEBUG-Schalter
	
	// statische Attribute für Factory
	protected static Logger logger= null;				//logger für log4j
	protected static DBConnector dbConnector= null;		//DB-Verbindungsobjekt
	/**
	 * speichert den abstrakten Namen für die Relation, in die dieser Knoten 
	 * geschrieben wird. 
	 */
	protected static String absRelName=null;
	protected static boolean factoryInit= false;		//gibt an, ob die Factory initialisiert wurde
	
	//private String name= null;							//Name des Primärtextes
	private static CorpusGraphMgr kGraphMgr= null;		//KorpusGraph Manager, der diesen Knoten einfügt
	private static Long classRelID= null;				//relationaleID als Zählvariable für neue Knoten
	
	private Long relID= null;								//relationale ID dieses Knotens(Knoten als Doc)
	private String type=		null;		//Typ dieser Collection 
	private String typeName=	null;		//Name dieser Collection
//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_INIT=			MSG_STD + "Factory for "+TOOLNAME+" is initialized.";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DBCON=		MSG_ERR + "Cannot initialize the Factory, the given db-connector-object is empty.";
	private static final String ERR_EMPTY_KGRAPHMGR=	MSG_ERR + "Cannot initialize the Factory, the given korpus-graph-object is empty.";
	private static final String ERR_EMPTY_RELID=		MSG_ERR + "Cannot initialize the Factory, the given relID is empty.";
	private static final String ERR_FACTORY_NOT_INIT=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the factory is not initilaized. Call "+TOOLNAME+".initFactory() first.";
	private static final String ERR_EMPTY_ABSRELNAME=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the abstract relation name is empty.";
	private static final String ERR_NO_TYPE=			MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause 'type' is empty.";
	private static final String ERR_NO_TYPENAME=		MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause 'typeName' is empty.";
	private static final String ERR_CLASSRELID_NOT_SET=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the classRelID is not set. Call 'setRelID()' first."; 
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
		DocDN.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn gültig
		if (kGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		CollectionDN.kGraphMgr= kGraphMgr;
		
		//prüfen ob DB-Verbinder gültig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		CollectionDN.dbConnector= dbConnector;
		
		//abstrakten relNamen für Dokumenttabelle setzen
		if ((absRelName== null)||(absRelName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		CollectionDN.absRelName= absRelName;
		
		CollectionDN.factoryInit= true;
	}
	
	/**
	 * Setzt die relationale ID für diese Klasse, die beim erzeugen eines Objektes immer um eins
	 * hochgezählt wird.
	 * @param relID Long - initiale relID, die innerhalb der Klasse hochgezählt wird 
	 */
	public static void setRelID(Long relID) throws Exception
	{
		if (relID== null) throw new Exception(ERR_EMPTY_RELID);
		classRelID= relID;
	}
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein Collectionknoten-Objekt.
	 * @param uniqueName String - eindeutiger Name (Name des Knotens)
	 * @param type String - Typ dieser Collection
	 * @param typeName String - Name dieser Collection 
	 */
	public CollectionDN(	String uniqueName,
							String type,
							String typeName) throws Exception
	{
		super(uniqueName);
		//Prüfen ob type und typeName vergeben sind
		if ((type== null)||(type.equalsIgnoreCase("")))
			throw new Exception(ERR_NO_TYPE);
		if ((typeName== null)||(typeName.equalsIgnoreCase("")))
			throw new Exception(ERR_NO_TYPENAME);
		this.type= type;
		this.typeName= typeName;
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!CollectionDN.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		//prüfen ob Klassen relID gesetzt wurde
		if (classRelID== null)
			throw new Exception(ERR_CLASSRELID_NOT_SET);
		this.relID= classRelID;
		classRelID++;
		
		this.toWriter();
		if (DEBUG) System.out.println(MSG_STD+"node created with name: "+ uniqueName);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Schreibt diesen Knoten auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt. 
	 * zu schreibende Werte: <br/>
	 * <ul>
	 * 	<li>id		numeric(38) NOT NULL</li>
	 *	<li>"type"	character varying(100) NOT NULL</li>
	 *	<li>name	character varying(100) NOT NULL</li>
	 *	<li>pre		numeric(38)	NOT NULL</li>
	 *	<li>post	numeric(38)	NOT NULL</li>
	 * </ul>
	 */
	public void toWriter() throws Exception
	{
		IDTupleWriter tWriter= (IDTupleWriter)CollectionDN.dbConnector.getTWriter(CollectionDN.absRelName);
		Vector<String> tuple= new Vector<String>();
		tuple.add(this.type);
		tuple.add(this.typeName);
		//tuple.add(pre.toString());
		//tuple.add(post.toString());
		this.relID= tWriter.addTuple2(tuple);
	}
	/*
	public void toWriter(Long pre, Long post) throws Exception
	{
		TupleWriter tWriter= CollectionDN.dbConnector.getTWriter(CollectionDN.absRelName);
		Vector<String> tuple= new Vector<String>();
		tuple.add(this.relID.toString());
		tuple.add(this.type);
		tuple.add(this.typeName);
		//tuple.add(pre.toString());
		//tuple.add(post.toString());
		tWriter.addTuple(tuple);
	}*/
	
	/**
	 * Gibt die relationale ID dieses Knotens zurück, sofern es eine gibt. Es wird null 
	 * zurückgegeben, wenn keine relationale 
	 * ID vorhanden.
	 * @return relationale ID
	 */
	public Long getRelID() throws Exception
	{ 
		return(this.relID); 
	}
	
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		 retStr= MSG_STD + "node name: "+ this.getName() + ", type: " + this.type + ", type name: "+ this.typeName;
		return(retStr);
	}
}
