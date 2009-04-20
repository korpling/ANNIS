package importer.paula.paula10.importer.util.graph;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * Die Klasse Node stellt einen Knoten innerhalb eines Graphen dar. Ein Knoten verfügt über 
 * die Eigenschaften Name (Name eines Knoten) und eine Liste an frei definierbaren Werten.
 * <br/>
 * <ul>
 * 	<li>name - ein Knotenname muss gegeben sein</li>
 *  <li>values - freidefinierbare Werte, realisiert durch Hashtable. Jeder Wert muss einen Namen haben (Key) und einen wert (Value)</li>
 * </ul>
 * @author Florian Zipser
 * @version 1.0
 */
public class Node 
{
//	 ============================================== private Variablen ==============================================
	//private static final boolean DEBUG= true;		//DEBUG-Schalter
	private static final boolean DEBUG_FIN= false;	//spezieller DEBUG-Schalter für das Löschen eines Knotens (finalize()) 
	private static final String TOOLNAME= "Node";	//Name der Klasse
	private static final String NS_SEP= "::";		//Namensraumseperator
	
	private String name= "";						//Name eines Knoten
	private Map<String,Object> values= null;	//freidefinierbare Werte
	//private Vector<Node> edges= null;				//Liste der ausgehenden Kanten
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_ERR=			"ERROR ("+ TOOLNAME+"): ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_NAME			= MSG_ERR+ "No name or an empty name was given for nodename.";
	//private static final String ERR_NO_VALUE_NAME	= MSG_ERR+ "No name or an empty name was given for value name.";
	private static final String ERR_TWO_VALUE_NAME	= MSG_ERR+ "Attribute name allready exists for this node: ";
	private static final String ERR_NAME_NOT_EXIST	= MSG_ERR+ "Attribute name does not exist for this node: ";
	private static final String ERR_SEP_USED		= MSG_ERR+ "In the name of the given node/attribute is an illegal sequence: '"+NS_SEP+"'. Name of node/attribute:  ";
	private static final String ERR_EMPTY_ATT_VALUE = MSG_ERR+ "The given attribute value is empty.";
	//private static final String ERR_EMPTY_NODE		= "ERROR ("+ CLASSNAME+"): The given node is empty.";
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt den Namen dieses Tools zurück.
	 * @return String - Name dieses Tools
	 */
	public static String getToolName()
		{ return(TOOLNAME); }
	
	/**
	 * Prüft ob ein Name ein übergebener Name einen gültigen Knoten oder Attributnamen
	 * darstellt.
	 * @param name String - zu prüfender Name
	 * @return true, wenn Name gültig, false sonst
	 * @exception Fehler, wenn Name ungültig
	 */
	public static boolean checkName(String name) throws Exception
	{
		if ((name == null) ||(name.equalsIgnoreCase(""))) throw new Exception(ERR_NO_NAME);
		if (name.contains(NS_SEP)) return(false);
		else return(true);
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
		{ this.init(name);}
	
	/**
	 * Erzeugt ein Node-Objekt. Es muss unbedingt ein nichtleerer Name zur Instatiierung 
	 * angegeben werden. Außerdem ist die Angabe eines Namespace möglich. Der Name darf die 
	 * Zeichenfolge "::" nicht enthalten.
	 * @param ns String - Namensraum dieses Knotens
	 * @param name String - Name des Knoten  
	 */
	/*
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
	}*/
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
		}
	}
	
//	 ============================================== öffentliche Methoden ==============================================
	
	/**
	 * Setzt einen Value für dieses Knotenobjekt. Der Value kann anhand des Namens wieder
	 * ausgelesen werden. Der Name darf nicht leer sein und muss eindeutig für einen Knoten 
	 * sein.
	 * @param name String - Name des Attributes (darf nicht leer sein und muss eindeutig sein)
	 * @param value Object - Wert dieses Values 
	 */
	public void setAtt(String name, Object value) throws Exception
	{
		if (value== null) throw new Exception(ERR_EMPTY_ATT_VALUE);
		if (checkName(name))
		{
			if (this.values.containsKey(name)) throw new Exception(ERR_TWO_VALUE_NAME+ name);
			this.values.put(name, value);
		}
		else throw new Exception(ERR_SEP_USED);
	}
	
	/**
	 * Setzt einen Value für dieses Knotenobjekt. Der Value kann anhand des Namen wieder
	 * ausgelesen werden. Der Name darf nicht leer sein und muss eindeutig für einen Knoten 
	 * sein. Zusätzlich kann ein Namensraum angegeben werden, zu dem dieser Wert gehört. 
	 * @param ns String - Namensraum, aus dem der Wert stammt, wenn ns= null, wird der Namenraum nicht verwendet
	 * @param name String - Name des Attributes (darf nicht leer sein und muss eindeutig sein), darf keinen "::" enthalten
	 * @param value Object - wert dieses Values 
	 */
	public void setAtt(String ns, String name, Object value) throws Exception
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
			else this.setAtt(name, value);
		}
		else throw new Exception(ERR_SEP_USED);
	}
	
	/**
	 * Ändert einen Value dieses Knotenobjekt. Der Value kann anhand des Namen wieder
	 * ausgelesen werden. Der Name darf nicht leer sein und muss eindeutig für einen Knoten 
	 * sein. Zusätzlich kann ein Namensraum angegeben werden, zu dem dieser Wert gehört. 
	 * @param name String - Name des Wertes (darf nicht leer sein und muss eindeutig sein), darf keinen "::" enthalten
	 * @param value Object - wert dieses Values 
	 */
	public void changeAttValue(String name, Object value) throws Exception
	{
		if (checkName(name))
		{
			if (this.values.containsKey(name))
				this.values.remove(name);
			this.setAtt(name, value);
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
	public void changeAttValue(String ns, String name, Object value) throws Exception
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
			else this.setAtt(name, value);
		}
	}
	
	/**
	 * Gibt einen Wert zum angegebenen Namen zurück. Schmeißt einen Fehler, wenn Name nicht vorhanden
	 * @param name des gesuchten Wertes
	 * @return gesuchter Wert wenn vorhanden
	 * @throws Exception wenn KNoten diesen Wert nicht hat
	 */
	public Object getAttValue(String name) throws Exception
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
	public Object getAttValue(String ns, String name) throws Exception
	{
		//Namensraum ist gültig
		if (checkNS(ns))
		{
			String vName= ns + NS_SEP + name;	//Wertname mit Namespace
			if (!this.values.containsKey(vName)) throw new Exception(ERR_NAME_NOT_EXIST + vName);
			return(this.values.get(vName));
		}
		//	wenn NS-Name ungültig --> ignorieren
		else return(this.getAttValue(name));
	}
	
	/**
	 * Gibt zurück, ob ein Knoten über einen Valuenamen verfügt
	 * @param ns String - Namensraum, aus dem der Wert stammt, wenn ns= null, wird der Namenraum nicht verwendet
	 * @param name des gesuchten Wertes
	 * @return true, wenn dieser Valuename vorhanden ist, false sonst
	 */
	public boolean hasAttName(String ns, String name)
	{ 
		String vName= ns + NS_SEP + name;	//Wertname mit Namespace
		return(this.values.containsKey(vName)); 
	}
	
	/**
	 * Gibt zurück, ob ein Knoten über einen Valuenamen verfügt
	 * @param name String - Name des Values
	 * @return true, wenn dieser Valuename vorhanden ist, false sonst
	 */
	public boolean hasAttName(String name)
		{ return(this.values.containsKey(name)); }
	
	/**
	 * Gibt eine Liste aller Attributnamen zurück.
	 * @return Namen der Attributnamen bzw. null, wenn keine vorhanden
	 * @throws Exception
	 */
	public Collection<String> getAttNames() throws Exception
	{
		Collection<String> retVector= null;
		if (this.values.size() > 0)
		{
			retVector= new Vector<String>();
			Collection<String> attNames= this.values.keySet();
			for (String attName: attNames)
				retVector.add(attName);
		}
		return(retVector);
	}
	
	/**
	 * Entfernt ein Attribut, aus der Attributliste dieses Knotens. 
	 * @param name String - Name des Attributes
	 * @exception Fehler, wenn der Attributname nicht zu der Liste der Attribute des Knotens gehört
	 */
	public void removeAtt(String name) throws Exception
	{
		if (!this.hasAttName(name)) throw new Exception(ERR_NAME_NOT_EXIST);
		this.values.remove(name);
	}
	
	/**
	 * Entfernt ein Attribut, aus der Attributliste dieses Knotens. 
	 * @param ns String - Namensraum des Attributes
	 * @param name String - Name des Attributes
	 * @exception Fehler, wenn der Attributname nicht zu der Liste der Attribute des Knotens gehört
	 */
	public void removeAtt(String ns, String name) throws Exception
	{
		if (!this.hasAttName(ns, name)) throw new Exception(ERR_NAME_NOT_EXIST);
		this.values.remove(ns + NS_SEP + name);
	}
	
	/**
	 * Entfernt alle Attribute aus der Attributliste dieses Knotens.
	 */
	public void removeAttributes()
		{ this.values=  new Hashtable<String, Object>(); }
	
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
		retStr= retStr + "nodename = "+this.getName() + "\t";
		
		Collection<String> valueNames= this.values.keySet();
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
	 * Gibt einen String zurück, indem dieser Knoten nach dem DOT-Format formatiert.
	 * @return Knoteninformationen im Dot-Format 
	 */
	public String toDOT() throws Exception
	{ return(this.toDOT(null)); }
	
	/**
	 * Gibt einen String zurück, indem dieser Knoten nach dem DOT-Format formatiert.
	 * @param color String - Farbe in der der Knoten angezeigt werden soll.
	 * @return Knoteninformationen im Dot-Format 
	 */
	public String toDOT(String color) throws Exception
	{
		String retStr= "";
		retStr= "<" + this.getName() + ">";
		Collection<String> valueNames= null;	//Liste der Wertenamen eines Knoten
		
		valueNames= this.getAttNames();
		if (valueNames!= null)
		{
			retStr= retStr + "[shape= record, ";
			//Knoten farbig ausgeben
			if (color!= null) retStr= retStr + "color= "+color+ ",style = filled, ";	
			retStr= retStr + "label= \"{"+this.getName();
			//durch alle Values gehen
			for(String valueName :valueNames)
			{
				retStr= retStr +"|" + valueName + " = " + this.getAttValue(valueName);
			}
			retStr= retStr + "}\"]";
		}
		else
			//Knoten farbig ausgeben
			if (color!= null) retStr= retStr + "[color= "+color+ ", style = filled]";
		return(retStr);
	}
	
	/**
	 * Erzeugt eine Kopie dieses Node-Objektes und gibt dieses zurück.
	 * @return Node - Kopie dieses Knoten-Objektes
	 */
	public Node cloneNode() throws Exception
	{
		Node newNode= new Node(this.getName());
		
		//Attribute übertragen
		if (this.getAttNames()!= null)
			for (String attName: this.getAttNames())//attNames)
			{
				if (attName.contains(NS_SEP))
				{
					String parts[]= attName.split(NS_SEP);
					String ns= parts[0];
					String name= parts[1];
					newNode.setAtt(ns, name, this.getAttValue(ns, name));
				}
				else newNode.setAtt(attName, this.getAttValue(attName));
			}
		//System.out.println(newNode);
		return(newNode);
	}
	
	protected void finalize() throws Throwable
	{
		if (DEBUG_FIN)
		{
			System.out.println("node: " +this.getName()+ " deleted...");
		}
	}
}
