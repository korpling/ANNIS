package internalCorpusModel;

import java.util.Hashtable;

/**
 * Diese Klasse stellt eine Nicht-Dominanzkante in einem Korpusgraphen dar.
 * @author Florian Zipser
 * @version 1.0
 *
 */
public abstract class ICMNonDominanceEdge extends ICMAbstractEdge
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMNonDominanceEdge";		//Name dieses Tools
	private static final String VERSION=	"1.0";				//Version des ineternen KorpusModels
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "green";
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	//private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== statische Methoden ==============================================
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei die beiden übergebenen Knoten als Quelle bzw.
	 * Ziel.
	 * @param fromNode Node - Quellknoten, von dem aus die Kante geht
	 * @param toNode Node -  Zielknoten, zu dem die Kante geht
	 */
	public ICMNonDominanceEdge(	ICMAbstractDN fromNode, 
								ICMAbstractDN toNode) throws Exception
	{
		super(fromNode, toNode);
	}
	
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei einen Namen für diese Kante. Außerdem 
	 * werden die beiden übergebenen Knoten als Quelle bzw. Ziel gesetzt. 
	 * @param name String - Name dieser Kante
	 * @param fromNode Node - Quellknoten, von dem aus die Kante geht
	 * @param toNode Node -  Zielknoten, zu dem die Kante geht
	 */
	public ICMNonDominanceEdge(	String name,
								ICMAbstractDN fromNode, 
								ICMAbstractDN toNode) throws Exception
	{
		super(name, fromNode, toNode);
	}
	
	/**
	 * Erzeugt ein Edge-Objekt und setzt dabei die beiden übergebenen Knoten als Quelle bzw.
	 * Ziel. Weiter kann auch ein oder mehrere Kantenlabels vergeben werden. Diese Kantenlabels
	 * bilden Attribut-Wert-Paare in Form einer Tabelle
	 * @param fromNode Node - Quellknoten, von dem aus die Kante geht
	 * @param toNode Node -  Zielknoten, zu dem die Kante geht
	 * @param labels Hashtable<String, String> - Tabelle der Attribut-Wert-Paare
	 */
	public ICMNonDominanceEdge(	ICMAbstractDN fromNode, 
								ICMAbstractDN toNode,
								Hashtable<String, String> labels) throws Exception
	{
		super(fromNode, toNode, labels);
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
	public ICMNonDominanceEdge(	String name,
								ICMAbstractDN fromIKMAbstractDN, 
								ICMAbstractDN toIKMAbstractDN,
								Hashtable<String, String> labels) throws Exception
	{
		super(name, fromIKMAbstractDN, toIKMAbstractDN, labels);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	
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
		retStr= retStr + "color= " + color; 
		
		return(retStr);
	}
}
