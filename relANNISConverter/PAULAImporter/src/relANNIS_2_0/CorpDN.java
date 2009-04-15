package relANNIS_2_0;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.IDTupleWriter;
import relANNIS_2_0.relANNISDAO.TupleWriter;

import internalCorpusModel.*;

public class CorpDN extends ICMCorpDN implements RelationalDN
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"CorpDN";		//Name dieses Tools
	
	// statische Attribute für Factory
	protected static Logger logger= null;				//logger für log4j
	protected static DBConnector dbConnector= null;		//DB-Verbindungsobjekt
	protected static String absRelName=null;			//speichert den abstrakten Namen für die Relation, in die dieser Knoten geschrieben wird
	protected static String absRelNameStruct= null;		//speichert den abstrakten Namen für die Relation, in die dieser Knoten geschrieben wird (für den Struktureintrag)
	protected static boolean factoryInit= false;		//gibt an, ob die Factory initialisiert wurde
	
	/**
	 * number of objects of this type which where created in this run.
	 */
	//protected static long numOfObjects= 0;
	private static CorpusGraphMgr kGraphMgr= null;		//KorpusGraph Manager, der diesen Knoten einfügt
	
	/**
	 * aktuelle relationale ID, die vergeben werden kann.
	 */
	private static Long currRelID= null;
	
	/**
	 * speichert alle bisher verwendeten Corp_id Werte
	 */
	private static Collection<Long> usedCorpIDs= null;
	
	private String name= null;							//Name des Primärtextes
	private Long relID= null;							//relationale ID dieses Knotens
	private Long corpID= null;							//global und für immer eindeutige Korpus-ID
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_INIT=			MSG_STD + "Factory for "+TOOLNAME+" is initialized.";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DBCON=		MSG_ERR + "Cannot initialize the Factory, the given db-connector-object is empty.";
	private static final String ERR_EMPTY_KGRAPHMGR=	MSG_ERR + "Cannot initialize the Factory, the given korpus-graph-object is empty.";
	private static final String ERR_FACTORY_NOT_INIT=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the factory is not initilaized. Call "+TOOLNAME+".initFactory() first.";
	private static final String ERR_EMPTY_ABSRELNAME=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the abstract relation name is empty.";
	private static final String ERR_CREATING_CORP_ID=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause there is a NumberFormatException while creating timestamp: ";
//	 ============================================== statische Methoden ==============================================
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
									String absRelNameStruct,
									String absRelName,
									Logger logger) throws Exception
	{
		DocDN.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn gültig
		if (kGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		CorpDN.kGraphMgr= kGraphMgr;
		
		//prüfen ob DB-Verbinder gültig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		CorpDN.dbConnector= dbConnector;
		
		//abstrakten relNamen setzen
		if ((absRelNameStruct== null)||(absRelNameStruct.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		CorpDN.absRelNameStruct= absRelNameStruct;
		
		//abstrakten relNamen setzen
		if ((absRelName== null)||(absRelName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		CorpDN.absRelName= absRelName;
		
		usedCorpIDs= new HashSet<Long>();
		
		CorpDN.factoryInit= true;
	}
	
	/**
	 * Gibt eine neue relationale ID für diesen Knotentyp zurück.
	 * @return neue relationale ID
	 * @throws Exception
	 */
	protected static long getNewRelId() throws Exception
	{
		Long relID= null;
		//wenn currRelID== null, dann ist diese noch nicht initialisiert
		if (currRelID== null)
		{
			//anfangsWert aus DB laden
			currRelID= dbConnector.getNewID(absRelName, "id");
		}
		relID= currRelID;
		currRelID++;
		return(relID);
	}
	
	/**
	 * This method creates a new and probably unique global Id for the given
	 * corpus. This ID is created by timestamp and includes date-hour-minute-second.
	 * So it does not consider milli-seconds.
	 * @return new and probably unique global corpus id
	 * @throws Exception
	 */
	protected static Long getCorpIDbyTime() throws Exception
	{
		Long id= null;
		
		GregorianCalendar calendar= new GregorianCalendar();
		
		//Datum berechnen
		String yearStr= ((Integer)calendar.get(GregorianCalendar.YEAR)).toString();
		String monthStr= ((Integer)(calendar.get(GregorianCalendar.MONTH)+1)).toString();
		if (monthStr.length() == 1)
			monthStr= "0"+monthStr; 
		String dayStr= ((Integer)calendar.get(GregorianCalendar.DAY_OF_MONTH)).toString();
		if (dayStr.length() == 1)
			dayStr= "0"+dayStr;
		String dateStr= yearStr + monthStr + dayStr;
		
		//Uhrzeit berechnen
		String hourStr= ((Integer)calendar.get(GregorianCalendar.HOUR_OF_DAY)).toString();
		if (hourStr.length() == 1)
			hourStr= "0"+hourStr;
		String minStr= ((Integer)calendar.get(GregorianCalendar.MINUTE)).toString();
		if (minStr.length() == 1)
			minStr= "0"+minStr;
		String secStr= ((Integer)calendar.get(GregorianCalendar.SECOND)).toString();
		if (secStr.length() == 1)
			secStr= "0"+secStr;
		String mSecStr= ((Integer)calendar.get(GregorianCalendar.MILLISECOND)).toString();
		if (mSecStr.length() == 1)
			mSecStr= "0"+mSecStr;
		String timeStr= hourStr + minStr + secStr+mSecStr;
		
		//ID berechnen
		try
		{id= new Long(dateStr + timeStr);}
		catch (NumberFormatException e)
		{ 	throw new Exception(ERR_CREATING_CORP_ID + dateStr + timeStr); }
			
		return(id);
	}
	
//	 ============================================== Konstruktoren ==============================================	
	/**
	 * Initialisiert ein Korpusknoten-Objekt.
	 * @param uniqueName String - eindeutiger Name (Name des Knotens)
	 * @param name String - Name des Korpus in der Datenbank 
	 */
	public CorpDN(	String uniqueName,	
					String name) throws Exception
	{
		super(uniqueName);
		this.name= name;
		if (!CorpDN.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		
		//neue Korpus-Id berechnen
		Long newCorpId= CorpDN.getCorpIDbyTime();
		//wenn Zeitstempel nicht eindeutig genug, solange suchen bis ein eindeutiger vorhanden ist
		while (usedCorpIDs.contains(newCorpId))
		{
			newCorpId++;
		}
		CorpDN.usedCorpIDs.add(newCorpId);
		this.corpID= newCorpId;
		
		//old 14.7.08 
		this.relID= CorpDN.getNewRelId();
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Schreibt diesen Knoten auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt. Diese erstellt einen Wurzelknoten für alle
	 * Struktur und Token-objekte.
	 */
	/*
	public void toWriterStruct() throws Exception
	{
		TupleWriter tWriter= CorpDN.dbConnector.getTWriter(CorpDN.absRelNameStruct);
		Vector<String> tuple= new Vector<String>();
		//füge tuple in Writer ein
		//Tupel erstellen
		//text_ref-Wert setzen
		tuple.add(dbConnector.getDBNULL());
		//doc_id-Wert setzen
		tuple.add(dbConnector.getDBNULL());
		//name-Wert setzen
		tuple.add(dbConnector.getDBNULL());
		//left-Wert setzen
		tuple.add(dbConnector.getDBNULL());
		//right-Wert setzen
		tuple.add(dbConnector.getDBNULL());
		//cont-Wert setzen
		tuple.add("false");
		//text-Wert setzen
		tuple.add(dbConnector.getDBNULL());
		//tuple auf writer schreiben
		tWriter.addTuple(tuple);
	}*/
	
	/**
	 * Schreibt diesen Knoten auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt. 
	 * @param pre long - PreWert dieses Knotens
	 * @param post long - PostWert dieses Knotens
	 */
	public void toWriter(Long pre, Long post) throws Exception
	{
		TupleWriter tWriter= CorpDN.dbConnector.getTWriter(CorpDN.absRelName);
		Vector<String> tuple= new Vector<String>();
		tuple.add(this.relID.toString());
		tuple.add(this.corpID.toString());
		tuple.add(this.name);
		tuple.add(pre.toString());
		tuple.add(post.toString());
		tWriter.addTuple(tuple);
		//geändert 26-06-08
		//this.relID= ((IDTupleWriter)tWriter).addTuple2(tuple);
	}
	
	/**
	 * Gibt die relationale ID dieses Knotens zurück, sofern es eine gibt. Es wird
	 * null zurückgegeben, wenn keine relationale ID vorhanden.
	 * @return relationale ID
	 */
	public Long getRelID()
		{ return(this.relID); }
	
	/**
	 * Returns the global unique id for this corpus.
	 * @return global unique id
	 */
	public Long getCorpID()
		{ return(this.corpID);}
	
	/**
	 * Returns the 'real' name of this corpus. 
	 * Attention: This is not the unique id of the node in graph. 
	 * @return global unique id
	 */
	public String getCorpName()
		{ return(this.name);}
	
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
