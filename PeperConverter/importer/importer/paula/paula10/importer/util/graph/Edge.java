package importer.paula.paula10.importer.util.graph;

import java.util.Hashtable;
import java.util.Map;

/**
 * Die Klasse Edge stellt eine Kante in einem Graphen dar. Konkret bedeutet dies, ein Objekt
 * diesen Typs verbindet zwei Objekte des Typs Node in einem Graph-Objekt. Ähnlich wie bei 
 * einem Knoten kann eine Kante einen Namen besitzen. Aber im Gegensatz zu Node-Objekten
 * ist der Kantenname hier optional.
 * @author Florian Zipser
 * @version 1.0
 */
public class Edge 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"Edge";		//Name dieses Tools
	private static final String VERSION= 	"1.0";	
	
	/**
	 * Name dieser Kante, sofern einer vergeben wird.
	 */
	protected String name= null;
	
	/**
	 * Knoten von dem diese Kante ausgeht.
	 */
	protected Node fromNode= null;
	/**
	 * Knoten zu dem diese Kante führt.
	 */
	protected Node toNode= null;
	/**
	 * Kantenlabels dieser Kante.
	 */
	protected Map<String, String> labels= null;
	
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_FROMNODE=		MSG_ERR + "Cannot create an edge-objekt, because the given fromNode is empty.";
	private static final String ERR_NO_TONODE=			MSG_ERR + "Cannot create an edge-objekt, because the given toNode is empty.";
	private static final String ERR_EMPTY_NAME=			MSG_ERR + "Cannot set the name of this edge, beacuse the given name was an empty string.";
	private static final String ERR_LABEL_NOT_EXIST=	MSG_ERR + "This edge does not contain a label with the given label name: ";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei die beiden übergebenen Knoten als Quelle bzw.
	 * Ziel.
	 * @param fromNode Node - Quellknoten, von dem aus die Kante geht
	 * @param toNode Node -  Zielknoten, zu dem die Kante geht
	 */
	public Edge(	Node fromNode, 
					Node toNode) throws Exception
	{
		if (fromNode== null) throw new Exception(ERR_NO_FROMNODE);
		else if (toNode== null) throw new Exception(ERR_NO_TONODE);
		
		this.fromNode= fromNode;
		this.toNode= toNode;
	}
	
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei einen Namen für diese Kante. Außerdem 
	 * werden die beiden übergebenen Knoten als Quelle bzw. Ziel gesetzt. 
	 * @param name String - Name dieser Kante
	 * @param fromNode Node - Quellknoten, von dem aus die Kante geht
	 * @param toNode Node -  Zielknoten, zu dem die Kante geht
	 */
	public Edge(	String name,
					Node fromNode, 
					Node toNode) throws Exception
	{
		this(fromNode, toNode);
		this.setName(name);
	}
	
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei die beiden übergebenen Knoten als Quelle bzw.
	 * Ziel. Weiter kann auch ein oder mehrere Kantenlabels vergeben werden. Diese Kantenlabels
	 * bilden Attribut-Wert-Paare in Form einer Tabelle
	 * @param fromNode Node - Quellknoten, von dem aus die Kante geht
	 * @param toNode Node -  Zielknoten, zu dem die Kante geht
	 * @param labels Hashtable<String, String> - Tabelle der Attribut-Wert-Paare
	 */
	public Edge(	Node fromNode, 
					Node toNode,
					Map<String, String> labels) throws Exception
	{
		this(fromNode, toNode);
		this.setLabel(labels);
	}
	
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei einen Namen für diese Kante. Außerdem 
	 * werden die beiden übergebenen Knoten als Quelle bzw. Ziel gesetzt. 
	 * Weiter kann auch ein oder mehrere Kantenlabels vergeben werden. Diese Kantenlabels
	 * bilden Attribut-Wert-Paare in Form einer Tabelle
	 * @param name String - Name dieser Kante
	 * @param fromNode Node - Quellknoten, von dem aus die Kante geht
	 * @param toNode Node -  Zielknoten, zu dem die Kante geht
	 * @param labels Hashtable<String, String> - Tabelle der Attribut-Wert-Paare
	 */
	public Edge(	String name,
					Node fromNode, 
					Node toNode,
					Map<String, String> labels) throws Exception
	{
		this(fromNode, toNode, labels);
		this.setName(name);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Setzt den Namen dieser Kante auf den übergebenen. Ein leerer String ist kein 
	 * gültiger Name und erzeugt einen Fehler. Wird null übergeben, wird der Name nicht
	 * gesetzt.
	 * @param name String - der zu setztende Name dieser Kante
	 * @exception Fehler, wenn Name der Kante ein leerer String ist
	 */
	public void setName(String name) throws Exception
	{
		if ((name== null) || (name.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_NAME);
		this.name= name;
	}
	
	/**
	 * Gibt den Namen dieser Kante zurück, sofern es einen Namen gibt. Gibt
	 * null zurück, wenn noch kein Name vergeben wurde.
	 * @return Der Name der Kante bzw. null wenn kein Name existiert. 
	 */
	public String getName() throws Exception
	{	return(this.name);	}
	
	/**
	 * Gibt den Quellknoten dieser Kante zurück.
	 * @return Quellknoten dieser Kante
	 */
	public Node getFromNode()throws Exception
	{ return(this.fromNode); }
	
	/**
	 * Gibt den Zielknoten dieser Kante zurück.
	 * @return Zielknoten dieser Kante
	 */
	public Node getToNode() throws Exception
	{ return(this.toNode); }
	
	/**
	 * Gibt den die Labels als Attribut-Wert-Paar-Tabelle dieser Kante zurück.
	 * @return Attribut-Wert-Paar-Tabelle der Labels, null, wenn keine Labels gesetzt wurden
	 */
	public Map<String, String> getLabel() throws Exception
	{ return(this.labels); }
	
	
	/**
	 * Setzt die Kantenlabel Attribut-Wert-Paar-Tabelle auf die übergebene
	 * @param labels Map<String, String>- Kantenlabel als Attribut-Wert-Paar-Tabelle
	 */
	public void setLabel(Map<String, String> labels) throws Exception
	{
		this.labels= labels;
	}
	
	/**
	 * Fügt die übergebenen Kantenlabels an die bereitsbestehenden. Wenn noch keine 
	 * Kantenlabels gesetzt wurden, so werden sie auf die übergebenen gesetzt.
	 * @param labels Map<String, String>- Kantenlabel als Attribut-Wert-Paar-Tabelle
	 */
	public void addLabel(Map<String, String> labels) throws Exception
	{
		//noch keine Lables vorhanden
		if ((this.labels==null) || (this.labels.isEmpty()))
			this.setLabel(labels);
		//es gibt bereits Labels
		else
		{
			this.labels.putAll(labels);
		}
	}
	
	/**
	 * Fügt das übergebene Kantenlabel an die bereitsbestehenden. Wenn noch keine 
	 * Kantenlabels gesetzt wurden, so werden sie auf die übergebenen gesetzt.
	 * @param labelName String - Name des Labels
	 * @param labelValue String Wert des Labels
	 */
	public void addLabel(String labelName, String labelValue) throws Exception
	{
		//noch keine Lables vorhanden
		if ((this.labels==null) || (this.labels.isEmpty()))
		{
			this.labels= new Hashtable<String, String>();
		}
		//es gibt bereits Labels
		this.labels.put(labelName, labelValue);
	}
	
	/**
	 * Prüft ob diese Kante ein Label mit dem übergebenen Namen besitzt.
	 * @param labelName String - Name des gesuchten Labels
	 * @return true, wenn diese Kante ein Label mit dem übergebenen Namen besitzt, false sonst
	 */
	public boolean hasLabel(String labelName) throws Exception
	{
		boolean retVal= false;
		if (this.labels!= null)
				retVal= this.labels.containsKey(labelName);
			
		return(retVal);
	}
	
	/**
	 * Entfernt alle Labels aus der internen Labeltabelle.
	 * @throws Exception
	 */
	public void removeLabels() throws Exception
		{ this.labels= null; }
	
	/**
	 * Entfernt das Label mit dem übergebenen Labelnamen aus der internen Labeltabelle.
	 * @param labelName String - Name des zu entfernenden Labels
	 * @throws Exception Fehler, wenn das Label mit dem angegebenen Namen nicht existiert
	 */
	public void removeLabel(String labelName) throws Exception
	{ 
		if (!this.hasLabel(labelName)) throw new Exception(ERR_LABEL_NOT_EXIST+ labelName);
		if (this.labels!= null) this.labels.remove(labelName);
	}
	
	/**
	 * Gibt einen String zurück, indem dieser Knoten nach dem DOT-Format formatiert.
	 * @return Knoteninformationen im Dot-Format 
	 */
	public String toDOT() throws Exception
	{
		String retStr= "";
		
		retStr= "<" + this.getFromNode().getName() + "> -> <" + this.getToNode().getName() +">";
		
		return(retStr);
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
			retStr= retStr+ ", edge-name: "+ this.getName();
		}
		catch (Exception e)
		{
			retStr= retStr+ "null";
		}
		try
		{
			retStr= retStr + ", " + this.getFromNode().getName() + "-->" + this.getToNode().getName();
		}
		catch (Exception e)
		{}
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	


}
