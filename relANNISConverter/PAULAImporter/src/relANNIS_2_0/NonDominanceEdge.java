package relANNIS_2_0;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.relANNISDAO.DBConnector;
import relANNIS_2_0.relANNISDAO.TupleWriter;
import internalCorpusModel.ICMAbstractDN;
import internalCorpusModel.ICMNonDominanceEdge;

/**
 * Diese Klasse stellt eine Nicht-Dominanzkante im Korpusmodell dar. Solche 
 * Nicht-Dominanzkanten können z.B. anaphor-antecedent Beziehungen von Knoten sein.
 * Derartige Beziehungen werden durch eine gerichtete Kante dargestellt, die eine
 * Kantenannnotation besitzt.
 * @author Administrator
 *
 */
public class NonDominanceEdge extends ICMNonDominanceEdge implements RelationalEdge 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"NonDominanceEdge";		//Name dieses Tools
	private static final String VERSION=	"1.0";				//Version des ineternen KorpusModels
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "green";
	
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
	private static final String ERR_NO_LABELS=			MSG_ERR + "Cannot write this edge to stream, because it needs a label and thre´s no labelvalue given for edge: ";
	
//	 ============================================== statische Methoden ==============================================
	/**
	 * Initialisiert die Factory zum erzeugen von NonDominanceEdge-Objekten.
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
		NonDominanceEdge.logger= logger;
		if (logger!= null) logger.debug(MSG_INIT);
		
		//KorpusGraph setzen wenn gültig
		if (kGraphMgr== null) throw new Exception(ERR_EMPTY_KGRAPHMGR);
		
		//prüfen ob DB-Verbinder gültig ist
		if (dbConnector== null) throw new Exception(ERR_EMPTY_DBCON);
		NonDominanceEdge.dbConnector= dbConnector;
		
		//abstrakten relNamen setzen
		if ((absRelNameStruct== null)||(absRelNameStruct.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		NonDominanceEdge.absRelNameStruct= absRelNameStruct;
		if ((absRelNameAnno== null)||(absRelNameAnno.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ABSRELNAME);
		NonDominanceEdge.absRelNameAnno= absRelNameAnno;
		
		NonDominanceEdge.factoryInit= true;
	}
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei einen Namen für diese Kante. Außerdem 
	 * werden die beiden übergebenen Knoten als Quelle bzw. Ziel gesetzt. 
	 * Weiter kann auch ein oder mehrere Kantenlabels vergeben werden. Diese Kantenlabels
	 * bilden Attribut-Wert-Paare in Form einer Tabelle
	 * @param fromIKMAbstractDN IKMAbstractDN - Quellknoten, von dem aus die Kante geht
	 * @param toIKMAbstractDN IKMAbstractDN -  Zielknoten, zu dem die Kante geht
	 */
	public NonDominanceEdge(	ICMAbstractDN fromIKMAbstractDN, 
								ICMAbstractDN toIKMAbstractDN) throws Exception
	{
		super(fromIKMAbstractDN, toIKMAbstractDN);
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!NonDominanceEdge.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
	}
	
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
	public NonDominanceEdge(	String name,
								ICMAbstractDN fromIKMAbstractDN, 
								ICMAbstractDN toIKMAbstractDN,
								Hashtable<String, String> labels) throws Exception
	{
		super(name, fromIKMAbstractDN, toIKMAbstractDN, labels);
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!NonDominanceEdge.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
	}
	
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei einen Namen für diese Kante. Außerdem 
	 * werden die beiden übergebenen Knoten als Quelle bzw. Ziel gesetzt. 
	 * Weiter kann auch ein oder mehrere Kantenlabels vergeben werden. Diese Kantenlabels
	 * bilden Attribut-Wert-Paare in Form einer Tabelle
	 * @param name String - Name dieser Kante
	 * @param fromIKMAbstractDN IKMAbstractDN - Quellknoten, von dem aus die Kante geht
	 * @param toIKMAbstractDN IKMAbstractDN -  Zielknoten, zu dem die Kante geht
	 */
	public NonDominanceEdge(	String name,
								ICMAbstractDN fromIKMAbstractDN, 
								ICMAbstractDN toIKMAbstractDN) throws Exception
	{
		super(name, fromIKMAbstractDN, toIKMAbstractDN);
		//Fehler, wenn initFactory() nicht gestartet wurde
		if (!NonDominanceEdge.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
	}
	
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Schreibt diese Kante auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt. Die zu füllende Tabelle:
	 * <ul>
	 * 	<li>pre					numeric(38)	NOT NULL,</li>
	 * 	<li>post				numeric(38)	NOT NULL,</li>
	 * 	<li>struct_ref			numeric(38)	NOT NULL,</li>
	 * 	<li>parent				numeric(38),</li>
	 * 	<li>dominance			boolean		NOT NULL,</li>
	 * </ul>
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
		if (!NonDominanceEdge.factoryInit) throw new Exception(ERR_FACTORY_NOT_INIT);
		Vector<String> tuple= null;
		
		//Tupel in KantenStrukturRelation schreiben
		//ermittle TupleWriter für die Tabelle meta_attribute
		TupleWriter structWriter = NonDominanceEdge.dbConnector.getTWriter(NonDominanceEdge.absRelNameStruct);
		tuple= new Vector<String>();
		tuple.add(pre.toString());
		tuple.add(post.toString());
		tuple.add(((RelationalDN)this.getToNode()).getRelID().toString());
		tuple.add(fatherPre.toString());
		//Flag setzen, dass besagt ob diese Kante eine Dominanzkante ist
		tuple.add(dbConnector.getDBFALSE());

		//tuple in Writer schreiben
		structWriter.addTuple(tuple);
		
		if ((this.labels== null) || (this.labels.size()== 0))
			throw new Exception(ERR_NO_LABELS + this.getName());
		//wenn es Annotationen gibt
		for (String key: this.getLabel().keySet())
		{
			TupleWriter annoWriter = NonDominanceEdge.dbConnector.getTWriter(NonDominanceEdge.absRelNameAnno);
			tuple= new Vector<String>();
			//Referenz auf Strukturintrag
			tuple.add(pre.toString());
			//Typwert
			tuple.add(dbConnector.getDBNULL());
			//Annotationsname
			tuple.add(key);
			//Annotationswert
			tuple.add(this.getLabel().get(key));
			//tuple in Writer schreiben
			annoWriter.addTuple(tuple);
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
