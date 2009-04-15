package relANNIS_2_0;

import internalCorpusModel.ICMAbstractDN;
import internalCorpusModel.ICMDominanceEdge;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.TupleWriter;

public class ConstEdge extends ICMDominanceEdge implements RelationalEdge
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"ConstEdge";		//Name dieses Tools
	private static final String VERSION=	"1.0";				//Version des ineternen KorpusModels
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "blue";
	/**
	 * Typ dieser Kante
	 */
	private String edgeType= null;
	
	protected static Logger logger= null;				//logger für log4j
	protected static DBConnector dbConnector= null;		//DB-Verbindungsobjekt
	/**
	 * speichert den abstrakten Namen für die Relation, 
	 * in der die Struktur der Kante gespeichert wird (rank)
	 */
	protected static String absRelNameStruct=null;			
	/**
	 * speichert den abstrakten Namen für die Relation, 
	 * in der die Annotation der Kante gespeichert wird (rank-anno)
	 */
	protected static String absRelNameAnno=null;			
	protected static boolean factoryInit= false;		//gibt an, ob die Factory initialisiert wurde
	
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_INIT=			MSG_STD + "Factory for "+TOOLNAME+" is initialized.";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DBCON=		MSG_ERR + "Cannot initialize the Factory, the given db-connector-object is empty.";
	private static final String ERR_EMPTY_KGRAPHMGR=	MSG_ERR + "Cannot initialize the Factory, the given korpus-graph-object is empty.";
	private static final String ERR_FACTORY_NOT_INIT=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, because the factory is not initilaized. Call "+TOOLNAME+".initFactory() first.";
	private static final String ERR_EMPTY_ABSRELNAME=	MSG_ERR + "Cannot create "+TOOLNAME+"-object, beacause the abstract relation name is empty.";

//	 ============================================== statische Methoden ==============================================
	/**
	 * Initialisiert die Factory zum Erzeugen von ConstEdge-Objekten.
	 * Gesetzt wird hier:<br/>
	 * <ul>
	 *  <li>der Graph manager, in den dieser Knoten geschrieben werden soll</li>
	 * 	<li>der logger für log4j</li>
	 * 	<li>abstrakter Name der Relation, in die der Knoten geschrieben werden soll</li>
	 *  <li>das Datenbankverbindungsobjekt</li>
	 * <ul/>
	 * @param kGraphMgr KorpusGraph - der Graph, in den dieser Knoten geschrieben werden soll
	 * @param dbConnector DBConnector - DB-Verbindungsobjekt
	 * @param absRelNameStruct String - abstrakter Name der Relation, in die die Struktur dieser Kante geschrieben werden soll
	 * @param absRelNameAnno String - abstrakter Name der Relation, in die die Annotation dieser Kante geschrieben werden soll 
	 * @param logger Logger - Logger für log4j
	 */
	public static void initFactory(	CorpusGraphMgr kGraphMgr,
									DBConnector dbConnector, 
									String absRelNameStruct,
									String absRelNameAnno,
									Logger logger) throws Exception
	{
		ConstEdge.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn gültig
		if (kGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		//ConstEdge.kGraphMgr= kGraphMgr;
		
		//prüfen ob DB-Verbinder gültig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		ConstEdge.dbConnector= dbConnector;
		
		//abstrakten relNamen setzen
		if ((absRelNameStruct== null)||(absRelNameStruct.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		ConstEdge.absRelNameStruct= absRelNameStruct;
		if ((absRelNameAnno== null)||(absRelNameAnno.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		ConstEdge.absRelNameAnno= absRelNameAnno;
		
		ConstEdge.factoryInit= true;
	}
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei einen Namen für diese Kante. Außerdem 
	 * werden die beiden übergebenen Knoten als Quelle bzw. Ziel gesetzt. 
	 * Weiter kann auch ein oder mehrere Kantenlabels vergeben werden. Diese Kantenlabels
	 * bilden Attribut-Wert-Paare in Form einer Tabelle
	 * @param name String - Name dieser Kante
	 * @param fromIKMAbstractDN IKMAbstractDN - Quellknoten, von dem aus die Kante geht
	 * @param toIKMAbstractDN IKMAbstractDN -  Zielknoten, zu dem die Kante geht
	 * @param labels Hashtable<String, String> - Tabelle der Attribut-Wert-Paare
	 */
	public ConstEdge(	String name,
						StructDN fromIKMAbstractDN, 
						ICMAbstractDN toIKMAbstractDN,
						Hashtable<String, String> labels) throws Exception
	{
		super(name, fromIKMAbstractDN, toIKMAbstractDN, labels);
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!ConstEdge.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
	}
	
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei einen Namen für diese Kante. Außerdem 
	 * werden die beiden übergebenen Knoten als Quelle bzw. Ziel gesetzt. 
	 * Weiter kann auch ein oder mehrere Kantenlabels vergeben werden. Diese Kantenlabels
	 * bilden Attribut-Wert-Paare in Form einer Tabelle
	 * @param name String - Name dieser Kante
	 * @param fromIKMAbstractDN IKMAbstractDN - Quellknoten, von dem aus die Kante geht
	 * @param toIKMAbstractDN IKMAbstractDN -  Zielknoten, zu dem die Kante geht
	 * @param edgeType String - Typ dieser Kante
	 */
	public ConstEdge(	String name,
						StructDN fromIKMAbstractDN, 
						ICMAbstractDN toIKMAbstractDN,
						String edgeType) throws Exception
	{
		super(name, fromIKMAbstractDN, toIKMAbstractDN);
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!ConstEdge.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		this.edgeType= edgeType;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Schreibt diese Kante auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt.
	 * 
	 * @param pre String - Pre-Wert für den Quellknoten dieser Kante
	 * @param post String - Post-Wert für den Quellknoten dieser Kante 
	 * @param fatherPre String - Zielknoten für diese Kante
	 */
	public void toWriter(	String pre, 
							String post, 
							String fatherPre) throws Exception
	{
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!ConstEdge.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		Vector<String> tuple= null;
		
		//Tupel in KantenStrukturRelation schreiben
		//ermittle TupleWriter für die Tabelle meta_attribute
		TupleWriter structWriter = ConstEdge.dbConnector.getTWriter(ConstEdge.absRelNameStruct);
		tuple= new Vector<String>();
		tuple.add(pre.toString());
		tuple.add(post.toString());
		tuple.add(((RelationalDN)this.getToNode()).getRelID().toString());
		tuple.add(fatherPre.toString());
		//Flag setzen, dass besagt ob diese Kante eine Dominanzkante ist
		tuple.add(dbConnector.getDBTRUE());
		//tuple in Writer schreiben
		structWriter.addTuple(tuple);
		
		//Tupel in KantenAnnotationsRelation schreiben
		//wenn es keine Annotationen und keinen Typ gibt, dann keinen Eintrag vornehmen
		if (((this.edgeType== null) || (this.edgeType.equalsIgnoreCase(""))) &&
				((this.labels== null) || (this.labels.isEmpty())));
		//wenn es keine Annotationen gibt, dann nur einen Eintrag vornehmen
		else if ((this.getLabel()== null) || (this.getLabel().isEmpty()))
		{
			TupleWriter annoWriter = ConstEdge.dbConnector.getTWriter(ConstEdge.absRelNameAnno);
			tuple= new Vector<String>();
			//Referenz auf Struktureintrag
			tuple.add(pre.toString());
			//Typwert
			if ((this.edgeType== null) || (this.edgeType.equalsIgnoreCase("")))
				tuple.add(dbConnector.getDBNULL());
			else tuple.add(this.edgeType);
			//Annotationsname
			tuple.add(dbConnector.getDBNULL());
			//Annotationswert
			tuple.add(dbConnector.getDBNULL());
			//tuple in Writer schreiben
			annoWriter.addTuple(tuple);
		}
		else
		{	
			//wenn es Annotationen gibt
			for (String key: this.getLabel().keySet())
			{
				TupleWriter annoWriter = ConstEdge.dbConnector.getTWriter(ConstEdge.absRelNameAnno);
				tuple= new Vector<String>();
				//Referenz auf Strukturintrag
				tuple.add(pre.toString());
				//Typwert
				tuple.add(this.edgeType);
				//Annotationsname
				tuple.add(key);
				//Annotationswert
				tuple.add(this.getLabel().get(key));
				//tuple in Writer schreiben
				annoWriter.addTuple(tuple);
			}
		}
	}
	
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "toolname: "+ TOOLNAME + ", version: "+ VERSION;
		try
		{
			retStr= retStr+ ", object-name: "+ this.getName();
		}
		catch (Exception e)
		{
			retStr= retStr+ "null";
		}
		return(retStr);
	}
	
	/**
	 * Schreibt diesen Knoten als DOT-Eintrag mit roter Knotenumrandung.
	 * @return Knoten als DOT-Eintrag
	 */
	public String toDOT() throws Exception
	{ 
		String retStr= "";
		
		retStr= "<" + this.getFromNode().getName() + "> -> <" + this.getToNode().getName() +">";
		
		//Farbe setzen
		retStr= retStr + "[color= " + color +"]"; 
		
		return(retStr);
	}
}
