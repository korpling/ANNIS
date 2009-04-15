package util.depGraph;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Die Klasse stammt aus der Version PAULAAnalyzer.
 * Die Klasse Node stellt einen Knoten innerhalb eines Graphen dar. Ein Knoten verfügt über 
 * die Eigenschaften Name (Name eines Knoten), eine Liste an frei definierbaren Werten und
 * einer Liste ausgehender Kanten zu anderen Knoten. Durch diese Liste der Kanten, ist die 
 * Klasse Node im Prinzip die gesamte Graphstruktur.<br/>
 * <ul>
 * 	<li>name - ein Knotenname muss gegeben sein</li>
 *  <li>values - freidefinierbare Werte, realisiert durch Hashtable. Jeder Wert muss einen Namen haben (Key) und einen wert (Value)</li>
 *  <li>edges - Kanten zu anderen Knoten</li>
 * </ul>
 * @author Florian Zipser
 * @version 1.0
 */
public class Node 
{
//	 ============================================== private Variablen ==============================================
	//private static final boolean DEBUG= true;		//DEBUG-Schalter
	private static final String TOOLNAME= "Node";	//Name der Klasse
	private static final String NS_SEP= "::";		//Namensraumseperator
	
	private String name= "";						//Name eines Knoten
	private Hashtable<String,Object> values= null;	//freidefinierbare Werte
	//private Vector<Node> edges= null;				//Liste der ausgehenden Kanten
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_ERR=			"ERROR ("+ TOOLNAME+"): ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_NAME			= MSG_ERR+ "No name or an empty name was given for nodename.";
	//private static final String ERR_NO_VALUE_NAME	= MSG_ERR+ "No name or an empty name was given for value name.";
	private static final String ERR_TWO_VALUE_NAME	= MSG_ERR+ "Name for value allready exists for this node: ";
	private static final String ERR_NAME_NOT_EXIST	= MSG_ERR+ "Name does not exist for this node: ";
	private static final String ERR_SEP_USED		= MSG_ERR+ "In the name of the given node/attribute is an illegal sequence: '"+NS_SEP+"'. Name of node/attribute:  ";
	//private static final String ERR_EMPTY_NODE		= "ERROR ("+ CLASSNAME+"): The given node is empty.";
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt den Namen dieses Tools zurück.
	 * @return String - Name dieses Tools
	 */
	public static String getToolName()
		{ return(TOOLNAME); }
	
	/**
	 * Prüft ob ein Name ein gültiger Value-Name ist.
	 * @param name String - zu prüfender Name
	 * @return true, wenn Name gültig, false sonst
	 * @exception Fehler, wenn Name ungültig
	 */
	public static boolean checkName(String name) throws Exception
	{
		if ((name == null) ||(name.equalsIgnoreCase(""))) throw new Exception(ERR_NO_NAME);
		if (name.contains(NS_SEP)) throw new Exception(ERR_SEP_USED + name);
		
		return(true);
	}
	
	/**
	 * Prüft ob ein Namensraum ein gültiger Value-Name ist.
	 * @param ns String - zu prüfender Namensraum
	 * @exception Fehler, wenn Name ungültig
	 */
	public static boolean checkNS(String ns) throws Exception
	{
		boolean retVal= false;
		//Fehler, wenn Seperator enthalten
		if (ns.contains(NS_SEP)) throw new Exception(ERR_SEP_USED);
		
		if ((ns == null) ||(ns.equalsIgnoreCase(""))) retVal= false;
		else retVal= true;
		
		return(retVal);
	}
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt ein Node-Objekt. Es muss unbedingt ein nichtleerer Name zur Instatiierung 
	 * angegeben werden.Der Name darf die 
	 * Zeichenfolge "::" nicht enthalten.
	 * @param name String - Name des Knoten  
	 */
	public Node(String name) throws Exception
	{
		this.init(name);
	}
	
	/**
	 * Erzeugt ein Node-Objekt. Es muss unbedingt ein nichtleerer Name zur Instatiierung 
	 * angegeben werden. Außerdem ist die Angabe eines Namespace möglich. Der Name darf die 
	 * Zeichenfolge "::" nicht enthalten.
	 * @param name String - Name des Knoten  
	 */
	public Node(String ns, String name) throws Exception
	{
		//Namespace in Ordnung
		if (checkNS(ns)) 
		{
			if (checkName(name)) 
			{
				String vName= ns + NS_SEP + name;	//Wertname mit Namespace
				this.name= vName;
				
				//valueTabelle und Kantenliste instantiieren
				this.values= new Hashtable<String, Object>();
			}
		}
		//kein Namespace gegeben
		else init(name);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Erzeugt ein Node-Objekt. Es muss unbedingt ein nichtleerer Name zur Instatiierung 
	 * angegeben werden.Der Name darf die 
	 * Zeichenfolge "::" nicht enthalten.
	 * @param name String - Name des Knoten
	 */
	private void init(String name) throws Exception
	{
		if (checkName(name))
		{
			this.name = name;
		
			//valueTabelle und Kantenliste instantiieren
			this.values= new Hashtable<String, Object>();
			//this.edges= new Vector<Node>();
		}
	}
	
	/**
	 * Kopiert den Inhalt einer String-Enumeration in einen String-Vector und gibt diesen zurück
	 * @param enumeration Enumeration<String> - zu kopierende Enumeration
	 * @return Vector<String> - Vector mit dem gleichen Inhalt wie die Enumeration
	 */
	private Vector<String> enumToVec(Enumeration<String> enumeration)
	{
		Vector<String> retVec= null;
		
		if (enumeration != null)
		{
			retVec= new Vector<String>();
			while(enumeration.hasMoreElements())
				retVec.add(enumeration.nextElement());
		}
		
		return(retVec);
	}
	
//	 ============================================== öffentliche Methoden ==============================================
	
	/**
	 * Setzt einen Value für dieses Knotenobjekt. Der Value kann anhand des Namens wieder
	 * ausgelesen werden. Der Name darf nicht leer sein und muss eindeutig für einen Knoten 
	 * sein.
	 * @param name String - Name des Wertes (darf nicht leer sein und muss eindeutig sein)
	 * @param value Object - Wert dieses Values 
	 */
	public void setValue(String name, Object value) throws Exception
	{
		if (checkName(name))
		{
			if (this.values.containsKey(name)) throw new Exception(ERR_TWO_VALUE_NAME+ name);
			this.values.put(name, value);
		}
	}
	
	/**
	 * Setzt einen Value für dieses Knotenobjekt. Der Value kann anhand des Namen wieder
	 * ausgelesen werden. Der Name darf nicht leer sein und muss eindeutig für einen Knoten 
	 * sein. Zusätzlich kann ein Namensraum angegeben werden, zu dem dieser Wert gehört. 
	 * @param ns String - Namensraum, aus dem der Wert stammt, wenn ns= null, wird der Namenraum nicht verwendet
	 * @param name String - Name des Wertes (darf nicht leer sein und muss eindeutig sein), darf keinen "::" enthalten
	 * @param value Object - wert dieses Values 
	 */
	public void setValue(String ns, String name, Object value) throws Exception
	{
		//prüft den Value-Namen auf Gültigkeit
		if (checkName(name))
		{
			//prüft den Namespace-Namen auf Gültigkeit
			if (checkNS(ns))
			{
				String vName= ns + NS_SEP + name;	//Wertname mit Namespace
				if (this.values.containsKey(vName)) throw new Exception(ERR_TWO_VALUE_NAME+ vName);
				this.values.put(vName, value);
			}
			//wenn NS-Name ungültig --> ignorieren
			else this.setValue(name, value);
		}
	}
	
	/**
	 * Ändert einen Value dieses Knotenobjekt. Der Value kann anhand des Namen wieder
	 * ausgelesen werden. Der Name darf nicht leer sein und muss eindeutig für einen Knoten 
	 * sein. Zusätzlich kann ein Namensraum angegeben werden, zu dem dieser Wert gehört. 
	 * @param name String - Name des Wertes (darf nicht leer sein und muss eindeutig sein), darf keinen "::" enthalten
	 * @param value Object - wert dieses Values 
	 */
	public void changeValue(String name, Object value) throws Exception
	{
		if (checkName(name))
		{
			if (this.values.containsKey(name))
				this.values.remove(name);
			this.setValue(name, value);
		}
	}
	
	/**
	 * Ändert einen Value dieses Knotenobjekt. Der Value kann anhand des Namen wieder
	 * ausgelesen werden. Der Name darf nicht leer sein und muss eindeutig für einen Knoten 
	 * sein. Zusätzlich kann ein Namensraum angegeben werden, zu dem dieser Wert gehört. 
	 * @param ns String - Namensraum, aus dem der Wert stammt, wenn ns= null, wird der Namenraum nicht verwendet
	 * @param name String - Name des Wertes (darf nicht leer sein und muss eindeutig sein), darf keinen "::" enthalten
	 * @param value Object - wert dieses Values 
	 */
	public void changeValue(String ns, String name, Object value) throws Exception
	{
		//prüft den Value-Namen auf Gültigkeit
		if (checkName(name))
		{
			//prüft den Namespace-Namen auf Gültigkeit
			if (checkNS(ns))
			{
				String vName= ns + NS_SEP + name;	//Wertname mit Namespace
				if (!this.values.containsKey(vName)) throw new Exception(ERR_NAME_NOT_EXIST+ vName);
				this.values.put(vName, value);
			}
			//wenn NS-Name ungültig --> ignorieren
			else this.setValue(name, value);
		}
	}
	
	/**
	 * Gibt einen Wert zum angegebenen Namen zurück. Schmeißt einen Fehler, wenn Name nicht vorhanden
	 * @param name des gesuchten Wertes
	 * @return gesuchter Wert wenn vorhanden
	 * @throws Exception wenn KNoten diesen Wert nicht hat
	 */
	public Object getValue(String name) throws Exception
	{
		if (!this.values.containsKey(name)) throw new Exception(ERR_NAME_NOT_EXIST + name);
		return(this.values.get(name));
	}
	
	/**
	 * Gibt einen Wert zum angegebenen Namen und dessen Namespave zurück. Schmeißt einen Fehler, wenn Name nicht vorhanden
	 * @param ns String - Namensraum, aus dem der Wert stammt, wenn ns= null, wird der Namenraum nicht verwendet
	 * @param name des gesuchten Wertes
	 * @return gesuchter Wert wenn vorhanden
	 * @throws Exception wenn KNoten diesen Wert nicht hat
	 */
	public Object getValue(String ns, String name) throws Exception
	{
		//Namensraum ist gültig
		if (checkNS(ns))
		{
			String vName= ns + NS_SEP + name;	//Wertname mit Namespace
			if (!this.values.containsKey(vName)) throw new Exception(ERR_NAME_NOT_EXIST + vName);
			return(this.values.get(vName));
		}
		//	wenn NS-Name ungültig --> ignorieren
		else return(this.getValue(name));
	}
	
	/**
	 * Gibt zurück, ob ein Knoten über einen Valuenamen verfügt
	 * @param ns String - Namensraum, aus dem der Wert stammt, wenn ns= null, wird der Namenraum nicht verwendet
	 * @param name des gesuchten Wertes
	 * @return true, wenn dieser Valuename vorhanden ist, false sonst
	 */
	public boolean hasValue(String ns, String name)
	{ 
		String vName= ns + NS_SEP + name;	//Wertname mit Namespace
		return(this.values.containsKey(vName)); 
	}
	
	/**
	 * Gibt zurück, ob ein Knoten über einen Valuenamen verfügt
	 * @param name String - Name des Values
	 * @return true, wenn dieser Valuename vorhanden ist, false sonst
	 */
	public boolean hasValue(String name)
		{ return(this.values.containsKey(name)); }
	
	/**
	 * Gibt einen Vector aller Attributnamen zurück
	 * @return Namen der Values
	 * @throws Exception
	 */
	public Vector<String> getValuesNames() throws Exception
	{
		Vector<String> retVector= null;
		if (this.values.size() > 0)
		{
			retVector= new Vector<String>();
			Enumeration<String> valueNames= this.values.keys();
			while(valueNames.hasMoreElements())
				retVector.add(valueNames.nextElement());
		}
		return(retVector);
	}
	
	/**
	 * Gibt den Namen dieses Knoten-Objektes zurück.
	 * @return name des Knotenobjektes
	 **/
	public String getName()
		{ return(this.name); }
	
	/**
	 * Gibt den Namen dieses Knoten-Objektes zurück.
	 * @return name des Knotenobjektes
	 **/
	public String toString()
	{ 
		String retStr= "";
		retStr= retStr + "nodename = "+this.getName() + "\n";
		
		Vector<String> valueNames= this.enumToVec(this.values.keys());
		//wenn Werte exisitieren
		if ((valueNames != null) && (valueNames.size() > 0))
		{
			for(String vName: valueNames)
			{
				retStr= retStr + "\t" + vName + " = "+this.values.get(vName) + "\n";
			}
		}
		return(retStr); 
	}
	
	/**
	 * Erzeugt eine Kopie dieses Node-Objektes und gibt dieses zurück.
	 * @return Node - Kopie dieses Knoten-Objektes
	 */
	public Node cloneNode() throws Exception
	{
		Node newNode= new Node(this.name);
		Vector<String> nodes= this.enumToVec(this.values.keys());
		
		for (String node: nodes)
			newNode.setValue(node, this.getValue(name));
	
		return(newNode);
	}
	
	/**
	 * Setzt eine ausgehende Kante auf den übergebenen Knoten. Gibt einen Fehler, 
	 * wenn der Knoten leer ist. Dabei wird die Reihenfolge beachtet.
	 * @param node Node - Knoten auf den die Kante zeigt.
	 */
	/**
	public void setEdge(Node node) throws Exception
	{
		if (node == null) throw new Exception(ERR_EMPTY_NODE);
		this.edges.add(node);
	}
	
	/**
	 * Gibt alle ausgehenden Kanten zurück.
	 * @return Liste der ausgehenden Kanten
	 */
	/**
	public Vector<Node> getEdges()
		{ return(this.edges); }
	
	/**
	 * Löscht eine Kante zu dem angegebenen Knoten. Wirft Fehler, wenn Kante nicht existierte.
	 * @param nodeName String - Name des Knotens, zu dem die Kante gelöscht werden soll
	 * @return true, wenn Kante gelöscht werden konnte, false wenn Kante nicht vorhanden war
	 */
	/**
	public boolean delEdge(String nodeName) throws Exception
	{
		//Prüfe Namen des Knotens
		if ((nodeName == null) ||(nodeName.equalsIgnoreCase(""))) throw new Exception(ERR_NO_NAME);

		boolean found= false;	//Flag, ob passende Kante gefunden wurde
		for (Node toNode : this.edges)
		{
			//passende Kante gefunden
			if (toNode.getName().equalsIgnoreCase(nodeName))
			{
				found= true;
				this.edges.remove(toNode);
			}
		}
		return(found);
	}
	**/
}
