package relANNIS_2_0;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.ExtFileTupleWriter;
import relANNIS_2_0.relANNISDAO.IDTupleWriter;
import relANNIS_2_0.relANNISDAO.TupleWriter;
import internalCorpusModel.ICMAnnoDN;

public class AudioDN extends ICMAnnoDN
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"AudioDN";		//Name dieses Tools
	private static final boolean DEBUG= 	false;			//DEBUG-Schalter
		
	// statische Attribute für Factory
	protected static Logger logger= null;				//logger für log4j
	protected static DBConnector dbConnector= null;		//DB-Verbindungsobjekt
	protected static String absRelNameAudio=null;		//speichert den abstrakten Namen für die Relation, in der die Audio Daten als BLOB gespeichert werden
	protected static String absRelNameANNO=null;		//speichert den abstrakten Namen für die Relation, in die dieser Knoten geschrieben wird (Anno)
	protected static String absRelNameATT=null;			//speichert den abstrakten Namen für die Relation, in die dieser Knoten geschrieben wird (Anno_attribute)
	protected static boolean factoryInit= false;		//gibt an, ob die Factory initialisiert wurde
	
	/**
	 * Mime-Type für Audio-Dateien
	 */
	protected static String KW_MIME_AUDIO=	"audio/wav";			
	private static CorpusGraphMgr cGraphMgr= null;		//KorpusGraph Manager, der diesen Knoten einfügt
	
	private Long relID= null;	
	private File audioRef= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_INIT=			MSG_STD + "Factory for "+TOOLNAME+" is initialized.";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DBCON=		MSG_ERR + "Cannot initialize the Factory, the given db-connector-object is empty.";
	private static final String ERR_EMPTY_KGRAPHMGR=	MSG_ERR + "Cannot initialize the Factory, the given korpus-graph-object is empty.";
	private static final String ERR_FACTORY_NOT_INIT=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the factory is not initilaized. Call "+TOOLNAME+".initFactory() first.";
	private static final String ERR_EMPTY_ABSRELNAME=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the abstract relation name is empty.";
	private static final String ERR_NO_REFNODES=		MSG_ERR + "Cannot create "+TOOLNAME+"-object, because there are no reference nodes given.";
	private static final String ERR_NO_AUDIO_REF=		MSG_ERR + "Cannot create "+TOOLNAME+"-object, because there is no audio file given.";
//	 ============================================== statische Metthoden ==============================================
	
	/**
	 * Initialisiert die Factory zum erzeugen von AudioDN-Objekten.
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
	public static void initFactory(	CorpusGraphMgr cGraphMgr,
									DBConnector dbConnector, 
									String absRelNameAnno,
									String absRelNameAtt,
									String absRelNameAudio,
									Logger logger) throws Exception
	{
		AudioDN.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn gültig
		if (cGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		AudioDN.cGraphMgr= cGraphMgr;
		
		//prüfen ob DB-Verbinder gültig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		AudioDN.dbConnector= dbConnector;
		
		//abstrakten relNamen setzen
		if ((absRelNameAnno== null)||(absRelNameAnno.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		AudioDN.absRelNameANNO= absRelNameAnno;
		//abstrakten relNamen setzen
		if ((absRelNameAtt== null)||(absRelNameAtt.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		AudioDN.absRelNameATT= absRelNameAtt;
		
		//abstrakten relNamen setzen
		if ((absRelNameAudio== null)||(absRelNameAudio.equalsIgnoreCase(""))) 
				throw new Exception(ERR_EMPTY_ABSRELNAME);
		AudioDN.absRelNameAudio= absRelNameAudio;
		
		AudioDN.factoryInit= true;
	}

//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt einen neuen Knoten vom Typ IKMAudioDN. Dieser Knoten reräsentiert einen 
	 * Strukturdatenknoten. 
	 * @param uniqueName String - eindeutiger Name (Name des Knotens)
	 * @param name String - Name des des Strukturknotens
	 * @param refNodes Vector<TextedDN>- Knoten auf die sich dieser Knoten bezieht
	 * @param attValPairs Hashtable<String, String> - Tabelle der Annotationen, Zu dem gegebenen Attributwert in String1, wird ein Value in der DB abgelegt (String 2)
	 * @param annoLevelName String -Name des Annotationslevels
	 * @param audioRef File - Refrenz auf die Audio-Datei
	 * @param colDN CollectionDN - ein Knoten vom Typ CollectionDN, auf den dieser PrimDN Knoten verweist, wird keiner übergeben, wird ein NULL-Wert in der DN eingetragen 
	 */
	public AudioDN(	String uniqueName,
					Vector<TextedDN> refNodes,
					Hashtable<String, String> attValPairs,
					String annoLevelName,
					File audioRef,
					CollectionDN colDN) throws Exception
	{
		//ruft den Superkonstruktor mit Dummydaten auf
		super(uniqueName, attValPairs);

		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!AudioDN.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		if ((refNodes== null) || (refNodes.isEmpty())) throw new Exception(ERR_NO_REFNODES);
		if (DEBUG) System.out.println(MSG_STD + "name: " + uniqueName + ", refs: "+ refNodes + ", annotations: "+ attValPairs);
		if ((audioRef!= null) && (!audioRef.exists()))
			throw new Exception(ERR_NO_AUDIO_REF);
		this.audioRef= audioRef;
		//schreibe dieses Tupel auf den Stream
		this.toWriter(refNodes,attValPairs, annoLevelName, colDN);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Schreibt diesen Knoten auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt. 
	 * zu schreibende Werte für die ANNO-Tabelle: <br/>
	 * <ul>
	 * 	<li>id 				numeric(38)	NOT NULL,</li>
	 *	<li>struct_ref		numeric(38)	NOT NULL,</li>
	 *	<li>col_ref			numeric(38),</li>
	 * 	<li>anno_level		character varying(150),</li>
	 * </ul>
	 * zu schreibende Werte für die ANNO_ATTRIBUTE-Tabelle: <br/>
	 * <ul>
	 * 	<li>anno_ref	numeric(38)						NOT NULL,</li>
	 *	<li>name		character varying(150)		NOT NULL,</li>
	 *	<li>value		character varying(150),</li>
	 * </ul>
	 * @param refNodes Vector<TextedDN> - Knoten auf die dieser AudioDN referenziert
	 * @param attValPairs Hashtable<String, String> - Attribut-Wert-Paare, die die Annotation(en) bilden
	 * @param annoLevelName String -  Annotationsebene für dieses Annotationselement
	 * @param colDN CollectionDN - ein Knoten vom Typ CollectionDN, auf den dieser PrimDN Knoten verweist, wird keiner übergeben, wird ein NULL-Wert in der DN eingetragen
	 */
	private void toWriter(	Vector<TextedDN> refNodes,
							Hashtable<String, String> attValPairs,
							String annoLevelName,
							CollectionDN colDN) throws Exception
	{
		//erzeuge anno-Knoten
		IDTupleWriter annoWriter = (IDTupleWriter)AudioDN.dbConnector.getTWriter(AudioDN.absRelNameANNO);
		//erzeuge alle anno_attribute-Knoten
		TupleWriter annoAttWriter = AudioDN.dbConnector.getTWriter(AudioDN.absRelNameATT);
		Vector<String> tuple= null;
		Vector<String> attTuple= null;//Tupel für die Annotationen zu einem AnnoKnoten
		//für jeden Referenzknoten einen Annotationsknoten in die Tabelle schreiben
		for (TextedDN refDN: refNodes)
		{
			tuple= new Vector<String>();
			//struct_ref
			tuple.add(((RelationalDN)refDN).getRelID().toString());
			//Verweis zum ColDN erzeugen, wenn einer existiert
			if (colDN != null) tuple.add(colDN.getRelID().toString());
			else tuple.add(dbConnector.getDBNULL());
			tuple.add(annoLevelName);
			this.relID= annoWriter.addTuple2(tuple);
			//für diesen Annoknoten alle Annotationen schreiben
			String attName=null;	//Name eines Attributes
			Enumeration<String> attNames= attValPairs.keys();
			while(attNames.hasMoreElements())
			{
				attName= attNames.nextElement();
				attTuple= new Vector<String>();
				//Tupel erzeugen
				attTuple.add(relID.toString());
				attTuple.add(attName);
				attTuple.add(attValPairs.get(attName));
				annoAttWriter.addTuple(attTuple);
			}
			//wenn AudioReferenz nicht leer, Annotationsknoten für Audio-Referenz anlegen
			if (this.audioRef!= null)
			{
				//old 14.7.08
				/*
				//Tupel in die BLOB-Tabelle einfügen
				Long blobId= 0l;
				BLOBTupleWriter bWriter= (BLOBTupleWriter)AudioDN.dbConnector.getTWriter(AudioDN.absRelNameAudio);
				tuple= new Vector<String>();
				tuple.add(this.audioRef.getName());
				tuple.add(KW_MIME_AUDIO);
				blobId= bWriter.addTuple2(tuple, this.audioRef, 0);
				*/
				
				//Referenz auf die ExtFileDir
				CorpDN corpDN= AudioDN.cGraphMgr.getRootCorpDN();
				String subDir= corpDN.getCorpID().toString();
				ExtFileTupleWriter eFWriter= (ExtFileTupleWriter)AudioDN.dbConnector.getTWriter(AudioDN.absRelNameAudio);
				attTuple= new Vector<String>();
				//Tupel erzeugen
				attTuple.add(relID.toString());
				attTuple.add("AUDIO");
				eFWriter.addTuple(attTuple, this.audioRef, 3, subDir, true);
				//annoAttWriter.addTuple(attTuple);
			}
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
