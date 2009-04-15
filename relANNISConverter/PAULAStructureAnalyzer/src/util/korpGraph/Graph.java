package util.korpGraph;

import java.io.File;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Die Klasse Graph stellt einen Hauptspeichergraphen dar. Dies wesentlichen Bestandteile sind
 * Knoten und Kanten. Diese Klasse kann allg. Graphen repräsentieren. <br/>
 * Funktionen: <br/>
 * <ul>
 * 	<li>Innerhalb eines Graphen kann traversiert werden, hierfür gibt es einen aktuellen Knoten </li>
 * 	<li>Ein Graph kann als.dot ausgegeben werden</li>
 * 	<li>Ein nezer Knoten kann eingefügt werden</li>
 * 	<li>Eine neue Kante zwischen zwei Knoten kann gezogen werden</li>
 * 	<li>Ein Knoten hat einen eindeutigen Namen</li>
 * 	<li>Ein Knoten kann gelöscht werden</li>
 *  <li>Eine Kante kann gelöscht werden</li>
 * </ul>
 * 
 * @author Florian Zipser
 * @version 2.0
 *
 */
public class Graph 
{
//	 ============================================== private Variablen ==============================================
	private static final boolean DEBUG= false;		//DEBUG-Schalter
	private static final String TOOLNAME= "Graph";	//Name der Klasse
	protected static final String DOT_ENDING= "dot";	//Name der Klasse
	protected static final String NS_SEP=	"::";		//Namensraumseperator
	
	protected static final String KW_PRE=	"PRE";	//Name des Attributes für den pre-Wert
	protected static final String KW_POST=	"POST";	//Name des Attributes für den post-Wert
	
	protected String ns=		Graph.getNS();				//das zu benutzende Namensraumprefix
	protected String preName= this.ns + NS_SEP + KW_PRE;	//verwendeter Attributname für die Pre-Order
	protected String postName= this.ns + NS_SEP + KW_POST;	//verwendeter Attributname für die Post-Order
	
	private Logger logger= null;						//log4j zur Nachrichtenausgabe
	
	protected Hashtable<String, Node> nodes= null;		//Tabelle der Knoten key= name, value= Knotenobjekt
	protected Node currNode= null;						//aktueller Knoten
	protected Hashtable<String, Vector<String>>outEdges= null;	//ausgehende Kanten des ganzen Graphen key= Knoten mit ausgehenden Kanten, value= liste der Knoten, zu denen  die Kanten gehen
	protected Hashtable<String, Vector<String>>inEdges= null;		//eingehende Kanten des ganzen Graphen key= Knoten mit eingehenden Kanten, value= liste der Knoten, von denen aus die Kanten gehen
	protected PrintStream stdOut= System.out;									//Standardausgabe für DEBUG-MSGs
	
	protected Node root= null;							//Pointer auf Wurzel, wenn Graph ein Baum
	protected Vector<Node> dagNodes= null;				//Pointer auf alle "Wurzeln" in einem DAG 
	
	TraversalObject travObj= null;						//wird nur für das Callback der Methode depthFirst() benötigt
	
	//----------------------- Flags zur Initialisierung
	protected boolean isDirected= false;					//gibt an, ob der Graph gerichtet ist
	protected boolean isOrdered= false;					//gibt an, ob der Graph eine Knotenreihenfolge hat
	
	protected enum trFaNu {TRUE, FALSE, NULL};
	//----------------------- Flags, die Aufrufe sparen sollen
	protected trFaNu isTree= trFaNu.NULL;		//gibt an ob Graph ein Tree ist
	protected trFaNu isDAG= trFaNu.NULL;		//gibt an ob Graph ein DAG ist
	protected trFaNu hasCycle= trFaNu.NULL;	//gibt an ob Graph einen Zyklus hat
	
	//----------------------- für die mthode isTree()
	Vector<String> visitedNodes= null;					//Liste bereits besuchter Knoten
	//	 *************************************** Meldungen ***************************************
	protected static final String MSG_TODO=			TOOLNAME+">\tstill to be done";
	private static final String MSG_STD=			TOOLNAME+"> ";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	protected static final String MSG_NO_ROOT=		TOOLNAME+">\tGraph is no tree because it has no root.";
	protected static final String MSG_CYCLE=			TOOLNAME+">\tGraph is no tree because it has min one cycle.";
	protected static final String MSG_NOT_ALL_NODES=	TOOLNAME+">\tGraph is no tree because it is not coherent.";
	protected static final String MSG_INIT=			TOOLNAME+">\tGraph initialized.";
	protected static final String MSG_START_FCT=	MSG_STD + "start of method: ";
	//	 *************************************** Fehlermeldungen ***************************************
	protected static final String ERR_NODE_EXIST= 			MSG_ERR+ "A node with the given name already exists: ";
	protected static final String ERR_NODE_NOT_EXIST=		MSG_ERR+ "A node with this name doesn´t exists: ";
	protected static final String ERR_NODE_NAME_FAILURE= 	MSG_ERR+ "Node name is empty.";
	//private static final String ERR_EMPTY_FILENAME= 		MSG_ERR+  + "File name was empty.";
	protected static final String ERR_UNKNOWN= 				MSG_ERR+ "Unknown error .";
	protected static final String ERR_NO_PP= 				MSG_ERR+ "Graph is no tree or dag, so no pre- and post-order is possible.";
	protected static final String ERR_NO_DOT= 				MSG_ERR+ "No dot-file name was given.";
	private static final String ERR_TOO_MUCH_ROOTS=			MSG_ERR+ "Cannot compute rank relation, there are too much root nodes. This might be an internal error, root nodes: ";
	private static final String ERR_NO_TREE_DAG=			MSG_ERR+ "The graph is neither a tree nor a dag.";
	
//	 ============================================== statische Methoden ==============================================
	
	/**
	 * Gibt den von diesem Tool verwendeten Namensraum zurück.
	 */
	public static String getNS()
		{ return(TOOLNAME); }
	
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt ein Graph-Objekt
	 * @param isDirected boolean - true, wenn Graph gerichtet ist, false sonst
	 * @param isOrdered boolean - true, wenn Graph eine Knotenreihenfolge hat, false sonst  
	 */
	public Graph(boolean isDirected, boolean isOrdered)
	{
		this.isDirected= isDirected;
		this.isOrdered= isOrdered;
		this.nodes= new Hashtable<String, Node>();
		this.outEdges= new Hashtable<String,Vector<String>>();
		this.inEdges= new Hashtable<String,Vector<String>>();
		//Liste der "Wurzeln" für einen DAG initialisieren
		this.dagNodes= new Vector<Node>();
	}
	
	/**
	 * Erzeugt ein Graph-Objekt. Mit Nachrichtenstrom.
	 * @param isDirected boolean - true, wenn Graph gerichtet ist, false sonst
	 * @param isOrdered boolean - true, wenn Graph eine Knotenreihenfolge hat, false sonst  
	 * @param logger Logger - Log4j zur Nachrichtenausgabe
	 */
	public Graph(boolean isDirected, boolean isOrdered, Logger logger)
	{
		this(isDirected, isOrdered);
		this.logger= logger;
		
		if (this.logger != null) this.logger.debug(MSG_INIT);
	}
	
	/**
	 * Erzeugt ein Graph-Objekt. Mit Nachrichtenstrom.
	 * @param isDirected boolean - true, wenn Graph gerichtet ist, false sonst
	 * @param isOrdered boolean - true, wenn Graph eine Knotenreihenfolge hat, false sonst  
	 * @param prefix String - das Namensraumprefix, dass für den Namensraum dieses Objekts verwendet werden soll (bspw. bei Pre- und Postorder), ist dieses null oder leer, wird das Standard-Prefix: graph verwendet
	 * @param logger Logger - log4j Nachrichtenstrom  
	 */
	public Graph(boolean isDirected, boolean isOrdered, String prefix, Logger logger)
	{
		this(isDirected, isOrdered);
		if ((prefix != null) && (prefix.equalsIgnoreCase(""))) 
		{
			this.ns= prefix;
			this.preName= this.ns + NS_SEP + KW_PRE;
			this.postName= this.ns + NS_SEP + KW_POST;
		}
		
		this.logger= logger;
		
		if (this.logger != null) this.logger.info(MSG_INIT);
	}
//	 ============================================== private Methoden ==============================================
	
	/**
	 * Prüft einen Knotennamen auf Gültigkeit. Dazu gehört, dass der Name nicht leer sein darf.
	 * Und je nach Flag exists ob er existieren darf oder nicht. Gibt entsprechende Fehlermeldeungen.
	 * @param name String - Name des zu prüfenden Knotens
	 * @param exists boolean - bei true, muss Knoten existieren, sonst nicht
	 * @return true, wenn alles ok, false sonst
	 */
	private boolean checkNode(String name, boolean exists) throws Exception
	{
		//Prüfe Namen
		if ((name == null) ||(name.equalsIgnoreCase(""))) throw new Exception(ERR_NODE_NAME_FAILURE);
		
		//Prüfe ob Knoten bereits vorhanden ist
		boolean isThere = this.nodes.containsKey(name);
		
		//wenn Knoten existieren soll 
		if (exists) 
		{	
			if (isThere) return(true);
			else throw new Exception(ERR_NODE_NOT_EXIST);
		}
		//wenn Knoten nicht existieren soll
		else
		{
			if (isThere) throw new Exception(ERR_NODE_EXIST);
			else return(true);
		}
	}
	
	/**
	 * Prüft ob dieser Graph eine mögliche eindeutige Wurzel hat. Eine Wurzel zeichnet sich
	 * dadurch aus, dass sie keine eingehenden Kanten besitzt. Gibt es eine solche nicht oder 
	 * gibt es mehrere wird null zurückgegeben
	 * @return Wurzel, wenn es eine gibt, null sonst
	 */
	private Node posRootNode()
	{
		//suche Wurzel, für gerichteten Fall, kommt der Knoten in Frage der keine eingehende Kante hat. Gibt es mehrere, ist es kein Baum
		Vector<String> posRoot= null;
		boolean hasRoot= false;
		
		if (this.isDirected)
		{
			//Vector<String> nodeList= nodes.keySet().;
			posRoot= new Vector<String>();	//mögliche Wurzeln
			//gehe durch alle Knoten
			Enumeration<String> nodeNames= this.nodes.keys();
			String nodeName= "";		//für die Iteration
			while(nodeNames.hasMoreElements())
			{
				nodeName= nodeNames.nextElement();
				if (DEBUG) System.out.println("check node: '" + nodeName + "' for incoming edges");
				//wenn Knotenname nicht in Liste der eingehenden Kanten
				if (!this.inEdges.containsKey(nodeName))
				{
					if (DEBUG) System.out.println("no incoming edge");
					//wenn es bereits eine mögliche Wurzel gibt, ist Graph kein Baum 
					if(posRoot.size() == 1) 
					{
						hasRoot= false;
						break;
					}
					//es gibt noch keine mögliche Wurzel
					else 
					{
						posRoot.add(nodeName);
						hasRoot= true;
					}
				}
			}
		}
		if (hasRoot) return(this.nodes.get(posRoot.firstElement()));
		else return(null);
	}
	
	/**
	 * Stellt den rekursiven Aufruf zum Traversieren des Baumes dar. Gibt zurück, ob der Graph ein Baum ist.
	 * Bedient sich dabei von der globalen Variable visitedNodes.
	 * @param nodeList List zu besuchender Knoten
	 * @return true, wenn Graph ein Baum
	 * @throws Exception
	 */
	private boolean isTreeRek(Vector<String> nodeList) throws Exception
	{
		if (DEBUG) stdOut.println(MSG_START_FCT + "isTreeRek(" + nodeList + ")");
		boolean retVal= true;
		Vector<String> tmpOut= null;	//temporär für ausgehende Knoten	
		boolean copyOk= true;			//Flag, das angibt ob das Kopieren klar ging
		Vector<String> tmpNodeList= null;		//temporäre Knotenliste
		
		if ((nodeList != null) && (nodeList.size() > 0))
		{
			tmpNodeList= this.cloneStringVector(nodeList);
			for(String node: tmpNodeList)
			{
				
				if (DEBUG) stdOut.println("list of nodes to visit:\t" + nodeList);
				if (DEBUG) stdOut.println("visited nodes:\t" + this.visitedNodes);
				if (DEBUG) stdOut.println("actual node:\t" + node);
				if (DEBUG) stdOut.println("----------------------");
				
				//wenn Knoten bereits gesehen wurde --> Zyklus
				if (this.visitedNodes.contains(node)) 
				{
					if (this.logger != null) this.logger.debug(MSG_CYCLE);
					return(false);
				}
				//füge aktuellen Knoten zu Liste gesehener hinzu
				else this.visitedNodes.add(node);
				//entferne aktuellen Knoten aus aktueller Liste
				nodeList.remove(node);
				tmpOut= null;
				if (this.outEdges.get(node) != null) tmpOut= this.cloneStringVector(this.outEdges.get(node));
				//füge erreichbare Knoten vorne an die aktuelle Liste
				if (tmpOut != null) 
				{
					copyOk= nodeList.addAll(0, tmpOut);
					if (!copyOk) throw new Exception(ERR_UNKNOWN);
					//else retVal = isTreeRek(nodeList);
					else retVal = isTreeRek(tmpOut);
				}
				//wenn Graph kein Baum, nicht weitermachen
				if (retVal== false) break;
			}
		}
		return(retVal);
	}
	
	/**
	 * Vergibt rekursiv die Pr- und Poste-Order auf die einzelnen Knoten
	 * @param node Node - Knoten der nummeriert werden soll 
	 * @param ppOrder long - aktueller Pre- und Post-Order-Wert
	 * @return long - Pre- und Post-Order nach diesem Knoten und all seinen Unterknoten
	 * @throws Exception
	 */
	private long ppOrderRek(Node node, long ppOrder) throws Exception
	{
		if (DEBUG) stdOut.println("current node (name/ppOrder):\t" + node + ", " + ppOrder);
		
		Vector<Long> ppOrderVec= null;	//long-Vector für die PPOrder (bei DAGs)
		
		// wenn Graph-Struktur ein Baum, jeder Knoten hat nur einen Pre und Postwert
		if(this.isTree== trFaNu.TRUE) node.setValue(this.preName, ppOrder);
		// wenn Graph-Struktur ein DAG, jeder Knoten hat mehrere Pre und Postwerte
		else if (this.isDAG== trFaNu.TRUE)
		{
			//wenn Knoten bereits eine Pre-Order Wert hat
			if(node.hasValue(this.preName)) 
			{
				ppOrderVec = (Vector<Long>)node.getValue(this.preName);
				ppOrderVec.add(ppOrder);
			}
			//wenn Knoten noch keinen Pre-Order Wert hat
			else
			{
				ppOrderVec= new Vector<Long>();
				ppOrderVec.add(ppOrder);
				System.out.println("Nodename: "+ node.getName() + ", PreName: "+ this.preName);
				node.setValue(this.ns, KW_PRE, ppOrderVec);
				//node.setValue(this.preName, ppOrderVec);
			}
		}	
		ppOrder++;
		if (this.outEdges.get(node.getName()) != null)
		{
			for(String nodeName: this.outEdges.get(node.getName()))
			{
				ppOrder= ppOrderRek(this.nodes.get(nodeName), ppOrder);
			}
		}
		if (DEBUG) stdOut.println("current node (name/ppOrder):\t" + node + ", " + ppOrder);
		
		//Post-Order-Vergabe
		//wenn Graph ein Baum
		if (this.isTree== trFaNu.TRUE) node.setValue(this.postName, ppOrder);
		else if (this.isDAG== trFaNu.TRUE)
		{
			//wenn Knoten bereits eine Post-Order Wert hat
			if(node.hasValue(this.postName)) 
			{
				ppOrderVec = (Vector<Long>)node.getValue(this.postName);
				ppOrderVec.add(ppOrder);
			}
			//wenn Knoten noch keinen Post-Order Wert hat
			else
			{
				ppOrderVec= new Vector<Long>();
				ppOrderVec.add(ppOrder);
				//node.setValue(this.postName, ppOrderVec);
				node.setValue(this.ns, KW_POST, ppOrderVec);
			}
		}
		ppOrder++;
		return(ppOrder);
	}

	/**
	 * Prüft ob dieser Graph, ausgehend von dem übergebenen Knoten einen Zyklus besiztzt. Ist der
	 * rekursive Aufruf der Methode hasCycle. 
	 * @param node String - Startknoten, von dem aus an der Graph traversiert wird
	 * @param seenNodes Vector<String> - auf dem Pfad bereits gesehene Knoten
	 * @return true, wenn Grah einen Zyklus besitzt.
	 */
	private boolean hasCycleRec(String node, Vector<String> seenNodes) 
	{
		boolean retVal= false;			//Rückgabewert
		Vector<String> nodesToVisit= null;	//Liste der zu besuchenden Knoten
		Vector<String> visitedNodes= this.cloneStringVector(seenNodes);
		
		if (DEBUG) stdOut.println(TOOLNAME + ">\thasCycleRec:\t" + node +", "+ visitedNodes);
		
		//wenn noch kein Knoten gesehen wurde
		if (visitedNodes== null) visitedNodes= new Vector<String>(); 
		
		//prüfen ob Knoten bereits besucht wurde
		if (visitedNodes.contains(node)) retVal= true;
		//Knoten wurde noch nicht besucht
		else 
		{
			if (DEBUG) stdOut.println(TOOLNAME + ">\tnode hasn`t been seen:\t"+node);
			//Startknoten in Liste besuchter Knoten schreiben
			visitedNodes.add(node);
			nodesToVisit= this.cloneStringVector(this.outEdges.get(node));
			//wenn es keine Nachfahren gibt
			if ((nodesToVisit == null) || (nodesToVisit.size()== 0)) retVal= false;
			else
			{
				if (DEBUG) stdOut.println(TOOLNAME + ">\tnodes to expand:\t"+ nodesToVisit);
				//gehe durch alle seine Nachfahren
				for (String toNode: nodesToVisit) 
					retVal= hasCycleRec(toNode, visitedNodes);
			}
		}
		return(retVal);
	}
	
	/**
	 * Gibt eine Kopie eines Vectors zurück, der mit STrings gefüllt ist.
	 * @param vec Vector<String> - zu kopierender Vector
	 * @return neuer String-Vector
 	 */
	private Vector<String> cloneStringVector(Vector<String> vec)
	{
		Vector<String> retVec= null;
		
		// wenn übergebener Vector nicht leer ist
		if (vec != null)
		{
			retVec= new Vector<String>();
			//Durch jeden Eintrag des übergebenen Vectors gehen
			for(String elem: vec)
				retVec.add(elem);
		}
		return(retVec);
	}
//	 ============================================== protected Methoden ==============================================
	/**
	 * Wandelt eine String-Enumeration in einen neuen String-Vector um.
	 * @param enumeration - Umzuwandelnde enumeration
	 * @return neuer Vector mit den gleichen einträgen in der gleichen Reihenfolge 
	 */
	protected Vector<String> enumToVector(Enumeration<String> enumeration)
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
	
	/**
	 * Schreibt den Graphen in eine Doz-Datei. Ruft dabei die Methoden nodesToDOT und edgesTODot auf.
	 * Kann überschrieben werden um eigene Ausgaben zu realisieren.
	 * @param fileName String -Name der Ausgabedatei
	 */
	protected void printDOT(String fileName) throws Exception
	{
		//	Stream zur Ausgabe erzeugen
		File outFile= new File(fileName);
		PrintStream oStream= new PrintStream(outFile);
		
		//Kopf in den Stream schreiben
		if (this.isDirected) oStream.println("digraph G {");
		if (this.isOrdered) oStream.println("ordering= out;");
		
		Vector<String> allNodes= this.enumToVector(this.nodes.keys());
		
		//alle Knoten auf den Stream schreiben
		for (String nodeName: allNodes)
			oStream.println(this.nodesToDOT(this.nodes.get(nodeName)) + ";");
		
		//Kanten in Stream schreiben
		Vector<String> tmpOut= null;
		for (String nodeName: allNodes)
		{
			tmpOut= this.outEdges.get(nodeName);
			//wenn Knoten ausgehende Kanten hat
			if (tmpOut != null)
			{
				for(String out: tmpOut)
				{
					oStream.println(edgesToDOT(nodeName, out) + ";");
				}
			}
		}

		//Fuß ausgeben
		oStream.println("}");
		
		//Stream schließen und flushen
		oStream.flush();
		oStream.close();
	}
	
	/**
	 * Gibt einen String zurück, indem die Knoten nach DOT-Format formatiert sind (ohne Simikolon)
	 * @param node zu schreibender Knoten
	 */
	protected String nodesToDOT(Node node) throws Exception
	{
		String retStr= "";
		retStr= "<" + node.getName() + ">";
		Vector<String> valueNames= null;	//Liste der Wertenamen eines Knoten
		
		valueNames= node.getValuesNames();
		if (valueNames!= null)
		{
			retStr= retStr + "[shape= record, label= \"{"+node.getName();
			//durch alle Values gehen
			for(String valueName :valueNames)
			{
				retStr= retStr +"|" + valueName + " = " + node.getValue(valueName);
			}
			retStr= retStr + "}\"]";
		}
		return(retStr);
	}
	
	/**
	 * Gibt einen String zurück, indem die Knoten nach DOT-Format formatiert sind (ohne Simikolon)
	 * @param from Knoten von dem die Kante ausgeht
	 * @param to Knoten zu dem die Kante geht
	 */
	protected String edgesToDOT(String from, String to) throws Exception
	{
		String retStr= "";
		
		retStr= "<" + from + "> -> <" + to +">";
		
		return(retStr);
	}
	
	/**
	 * Funktion sollte von jeder Funktion der Klasse Graph aufgerufen werden. Diese Funktion
	 * setzt alle auskunftgebenden Flags dieses Graphen zurück wenn eine die Graph-Struktur ändernde
	 * Funktion aufgerufen wird. Wird eine nicht verändernde Funktion aufgerufen, bleiben die Flags 
	 * unangestastet. Diese Funktion soll Ausführungen sparen.   
	 * @param changes boolean - true, wenn es sich um eine ändernde Funktion handelt (Kante/ Knoten einfügen, löschen etc)
	 */
	protected void changeOperation(boolean changes)
	{
		//aufrufende Funktion ist ändernd
		if (changes)
		{
			this.root= null;
			this.isTree= trFaNu.NULL;
			this.isDAG= trFaNu.NULL;
			this.hasCycle= trFaNu.NULL;
		}
		//aufrufende Funktion ist nichtändernd
		else {}
	}
	
	/**
	 * Rekursiver Aufruf für die Methode depthFirst
	 * @param currNode Node - aktueller Knoten
	 * @param father - Vater des aktuellen Knotens
	 * @param order - Reihenfolge in der der aktuelle Knoten in der Kinderliste des Vaters vorkommt (beginnend bei null)
	 * @throws Exception
	 */
	protected void depthFirstRec(	Node currNode, 
									Node father,
									long order) throws Exception
	{
		this.travObj.nodeReached(currNode, father, order);
		
		//durch alle Kinder dieses Knotens gehen
		Vector<String> childs= this.outEdges.get(currNode.getName());
		//wenn Knoten Kinder hat
		if (childs != null)
		{
			//gehe durch alle Kinder des aktuellen Knoten gehen
			int i= 0;
			for(String childName: childs)
			{
				this.depthFirstRec(this.getNode(childName), currNode, i);
				i++;
			}
		}
		this.travObj.nodeLeft(currNode, father, order);
	}
//	 ============================================== öffentliche Methoden ==============================================

	/**
	 * Fügt den gegebenen Knoten dem Graphen hinzu. Es wird keine Kante gezogen. 
	 * Der aktuelle Knoten wird auf diesen gesetzt. Gibt einen Fehler, wenn bereits ein Knoten
	 * mit diesem Namen vorhanden ist.
	 * @param node - Node neu hinzuzufügender Knoten
	 */
	public void addNode(Node node) throws Exception
	{
		this.changeOperation(true); //gibt an ob Funktion die Graph-Struktur ändert
		if (this.logger != null) this.logger.debug("util.graph:\tnode '"+node.getName()+"' inserted");
		//Fehler, wenn Knoten mit diesem Namen bereits vorhanden
		if (this.nodes.containsKey(node.getName())) throw new Exception(ERR_NODE_EXIST + node.getName());
		this.nodes.put(node.getName(), node);
		this.currNode= node;
		this.root= null;
	}
	
	/**
	 * Gibt den aktuellen Knoten zurück.
	 * @return aktueller Knoten, null wenn Graph leer
	 */
	public Node getCurrNode()
	{
		this.changeOperation(false); //gibt an ob Funktion die Graph-Struktur ändert
		return(this.currNode); 
	}
	
	/**
	 * Sucht den Knoten mit dem übergebenen Namen und gibt diesen zurück. Ist der Knoten nicht 
	 * vorhanden, wird null zurückgegeben
	 * @param name String - Name des zu suchenden Knotens
	 * @return Node, Knoten, der auf diesen Namen passt
	 */
	public Node getNode(String name)
		{ return(this.nodes.get(name)); }
	
	/**
	 * Erzeugt eine Kante zwischen zwei Knoten. DIe Knoten werden über ihre eindeutigen 
	 * Namen identifiziert. Fehler, wenn Namen leer oder Knoten nicht existieren
	 * @param from String - Name des Knotens von dem aus die Kante geht
	 * @param to String - Name des Knotens zu dem die Kante geht
	 */
	public void createEdge(String from, String to) throws Exception
	{
		this.changeOperation(true); //gibt an ob Funktion die Graph-Struktur ändert
		if (this.logger != null) this.logger.debug("util.graph:\tcreate edge ("+from + ", "+ to +")");
		//Prüfe Knoten auf Namen und Existenz
		checkNode(from, true);
		checkNode(to, true);
		
		Vector<String> listOfEdges= null;
		//ausgehende Kante eintragen
		listOfEdges= this.outEdges.get(from);
		if (listOfEdges!= null)
		{
			//wenn Kante bereits vorhanden (gilt dann auch für inEdges)
			if (listOfEdges.contains(to)) return;
			listOfEdges.add(to); 
		}
		//wenn Knotenname noch keine eingehenden Kanten hat
		else
		{
			listOfEdges= new Vector<String>();
			listOfEdges.add(to);
			this.outEdges.put(from, listOfEdges);
		}
		//eingehende Kante eintragen
		listOfEdges= this.inEdges.get(to);
		//wenn Knotenname bereits eingehende Kanten hat
		if (listOfEdges!= null)
		{ 
			listOfEdges.add(from); 
		}
		//wenn Knotenname noch keine eingehenden Kanten hat
		else
		{
			listOfEdges= new Vector<String>();
			listOfEdges.add(from);
			this.inEdges.put(to, listOfEdges);
		}
		this.root= null;
	}
	
	/**
	 * Erzeugt eine Kante zwischen zwei Knoten. Fehler, wenn Knoten leer oder Knoten nicht existieren
	 * @param from Node - Knoten von dem aus die Kante geht
	 * @param to Node - Knoten zu dem die Kante geht
	 */
	public void createEdge(Node from, Node to) throws Exception
		{ this.createEdge(from.getName(), to.getName()); }
	
	
	/**
	 * Löscht eine Kante zwischen zwei Knoten. Fehler wenn Namen nicht vorhanden.
	 * @param from String - Name des Knotens von dem aus die Kante geht
	 * @param to String - Name des Knotens zu dem die Kante geht
	 */
	public void delEdge(String from, String to) throws Exception
	{
		this.changeOperation(true); //gibt an ob Funktion die Graph-Struktur ändert
		//Prüfe Knoten auf Namen und Existenz
		if ((checkNode(from, true)) && (checkNode(to, true)))
		{
			Vector<String> listOfNodes= null;
			//ausgehende Kante löschen
			listOfNodes= this.outEdges.get(from);
			listOfNodes.remove(to);
			//eingehende Kante löschen
			listOfNodes= this.outEdges.get(to);
			listOfNodes.remove(from);
		}
	}
	
	/**
	 * Gibt alle Knoten zurück, zu denen der übergebene Knoten ausgehende Kanten hat.
	 * @param node Node - der Knoten, ven dem die Knoten ausgehen sollen
	 * @return Knoten zu denen Kanten vom übergebenen Knoten gehen
	 */
	public Vector<Node> getOutEdges(Node node) throws Exception
	{
		if (node == null) throw new Exception(ERR_NODE_NAME_FAILURE);
		Vector<Node> retVec= null;
		
		Vector<String> outNames= this.outEdges.get(node.getName());
		//wenn Knoten ausgehende Kanten hat
		if (outNames != null)
		{
			retVec= new Vector<Node>(); 
			//durch alle ausgehenden Knoten gehen
			for (String outName: outNames)
			{
				retVec.add(this.getNode(outName));
			}
		}
		
		return(retVec);
	}
	
	/**
	 * Gibt die Anzahl der Knoten im Graph zurück.
	 * @return Anzahl der Knoten im Graph
	 */
	public int getSize()
	{ 
		this.changeOperation(false); //gibt an ob Funktion die Graph-Struktur ändert
		return(this.nodes.size()); 
	}
	
	/**
	 * Gibt die Wurzel zurück, wenn Graph ein Baum ist. Null sonst
	 * @return Node wenn Graph ein Baum ist, null sonst
	 */
	public Node getRoot() throws Exception
	{
		this.changeOperation(false); //gibt an ob Funktion die Graph-Struktur ändert
		if (this.root == null) this.isTree();
		return(root);
	}
	
	/**
	 * Gibt die "Wurzeln" eines DAG zurück. wenn es sich bei diesem Graphen um einen DAG
	 * handelt. 
	 * @return "Wurzeln" des DAG, wenn Graph ein DAG sonst null
	 */
	public Vector<Node> getDAGRoots()
	{
		//gibt an ob Funktion die Graph-Struktur ändert
		this.changeOperation(false); 
		Vector<Node> roots= new Vector<Node>();
		
		if(this.isDAG== Graph.trFaNu.TRUE) 
		{
			Vector<String> allNodes= this.enumToVector(this.nodes.keys());
			//prüfen ob es Knoten ohne eingehende Kanten gibt
			//alle Knoten durchgehen
			for (String node: allNodes)
			{
				//wenn Knoten keine eingehende Kante hat schreibe in Knoten in roots 
				if(!this.inEdges.containsKey(node))	roots.add(this.getNode(node));
			}
		}
		if (roots.isEmpty()) return(null);
		else return(roots);
	}
	
	/**
	 * Schreibt diesen Graphen in eine Dot-Datei. Wenn die Datei bereits existiert, wird sie
	 * überschrieben.
	 * @param fileName -String Name der DOT Datei
	 */
	public void prinToDot(String fileName) throws Exception
	{
		this.changeOperation(false); //gibt an ob Funktion die Graph-Struktur ändert
		//prüfe Dateinamen		
		if ((fileName == null) ||(fileName.equalsIgnoreCase(""))) throw new Exception(ERR_NO_DOT); 
		//Datei hat keine dot-Endung
		String parts[] = fileName.split("[.]");
		//Datei hat gar keine Endung oder keine DOT-Endung
		if ((parts.length == 1) || (!parts[parts.length-1].equalsIgnoreCase(DOT_ENDING)));
			fileName= fileName + "." + DOT_ENDING;
			
		this.printDOT(fileName);
	}
	
	/**
	 * Invertiert die Kanten eines gerichteten Baumes. Ist der Baum nicht gerichtet passiert 
	 * nichts. Ansonsten gilt für jede Kante (a, b) im Baum aus (a, b) wird (b, a).
	 */
	public void invertEdges()
	{
		if (this.isDirected)
		{
			Hashtable<String, Vector<String>> tempEdges= this.inEdges;
			this.inEdges= this.outEdges;
			this.outEdges= tempEdges;
		}
	}
	
	/**
	 * Macht eine Tiefensuche beginnend bei der Wurzel, so es denn eine gibt. 
	 * Über ein Rollback wird
	 * für jedee Traversion eine Methode aufgerufen, der der aktuelle Knoten und dessen 
	 * Vater übergeben wird. Funktioniert nur für Bäume und für DAGs mit genau einer Wurzel.
	 * Der Graph muss gerichtet sein. Wenn trvObj null, passiert nichts.
	 * @param travObj TraversalObject - Object, dass über ein Callback benachrichtigt werden soll wenn ein neuer Knoten erreicht wurde 
	 */
	public void depthFirst(TraversalObject travObj) throws Exception
		{ this.depthFirst(this.getRoot(), travObj); }
	
	
	/**
	 * Macht eine Tiefensuche beginnend bei dem übergebenen Knoten. Ist dieser Knoten 
	 * null wird bei der Wurzel begonnen, so es denn eine gibt. Über ein Rollback wird
	 * für jedee Traversion eine Methode aufgerufen, der der aktuelle Knoten und dessen 
	 * Vater übergeben wird. Funktioniert nur für Bäume und für DAGs mit genau einer Wurzel.
	 * Der Graph muss gerichtet sein. Wenn trvObj null, passiert nichts.
	 * @param startNode Node - Knoten bei dem die Traversierung begonnen werden soll
	 * @param travObj TraversalObject - Object, dass über ein Callback benachrichtigt werden soll wenn ein neuer Knoten erreicht wurde 
	 */
	public void depthFirst(Node startNode, TraversalObject travObj) throws Exception
	{
		if (travObj== null) return;
		this.travObj= travObj;
		
		//suche Wurzel des Graphen
		if (startNode == null)
		{
			Node root= null;
			//wenn Graph ein Baum ist
			if (this.isTree()) 
			{
				root= (Node)this.getRoot();
			}
			//wenn Graph ein DAG ist
			else if (this.isDAG()) 
			{
				Vector<Node> rootList= this.getDAGRoots();
				//Fehler, wenn mehr als eine Wurzel vorhanden
				if (rootList.size() > 1)
				{
					String rootNames= "";
					for (Node node :rootList)
						{ rootNames= rootNames + ", " + node.getName(); }
					throw new Exception(ERR_TOO_MUCH_ROOTS + rootNames);
					
				}
				root= rootList.firstElement(); 
			}
			else throw new Exception(ERR_NO_TREE_DAG);
			startNode= root;
		}
		
		this.depthFirstRec(startNode, null, 0);
		//travObj zurücksetzen
		this.travObj= null;
	}
	
	/**
	 * Gibt zurück, ob es sich bei diesem Graphen um einen Baum handelt. Baum hat eindeutige Wurzel,
	 * und es existiert genau ein Pfad von Knoten u zu Knoten v.
	 * @return true, wenn Graph ein Baum ist
	 */
	public boolean isTree() throws Exception
	{
		this.changeOperation(false);	//gibt an ob Funktion die Graph-Struktur ändert
		
		boolean isTree= false;			//Ist Graph Baum
		Node posRoot= null;				//Wurzel - Startknoten
		posRoot= this.posRootNode();
		
		//wenn letzte Berechnung noch aktuell ist
		if (this.isTree == trFaNu.TRUE) isTree= true;
		if (this.isTree == trFaNu.FALSE) isTree= false;
		//wenn letzte Berechnung nicht mehr aktuell ist
		else if (this.isTree == trFaNu.NULL) 
		{
			if (posRoot != null)
			{
				if (DEBUG) stdOut.println("possible root node:\t" + posRoot.getName());
	
				this.visitedNodes= new Vector<String>();
				//Wurzel zu den gesehenen Knoten hinzufügen
				this.visitedNodes.add(posRoot.getName());
				
				isTree= this.isTreeRek(this.cloneStringVector(this.outEdges.get(posRoot.getName())));
				//prüfen ob auch alle Knoten besucht wurden, gdw.  |visitedNodes|= |alle Knoten|
				if ((isTree== true) && (visitedNodes.size() != nodes.size())) 
				{
					if (logger != null) logger.debug(MSG_NOT_ALL_NODES);
					isTree= false;
				}
			}
			//Graph hat keine Wurzel
			else 
			{	
				isTree= false;
				if (logger != null) logger.debug(MSG_NO_ROOT);
			}
			//Wurzel setzen
			if (isTree) this.root= posRoot;
		}
		//Berechnung eintragen
		if(isTree) this.isTree = trFaNu.TRUE;
		else  this.isTree = trFaNu.FALSE;
		
		return(isTree);
	}
	
	/**
	 * Prüft ob dieser Graph ein directed acyclic graph (DAG)ist. Gibt true zurück wenn Graph ein
	 * DAG ist sonst false. G ist ein DAG, wenn G gerichtet und azyklisch ist. Basiert auf der Annahme, dass:<br/>
	 * Sei G= (V,E) der Graph. Für alle v aus V: es gibt ein u aus V: (u,v) aus E => G besitzt einen zyklus<br/>
	 * Das heißt, wenn es nur Knoten mit eingehenden Kanten gibt ist G kein DAG.
	 * @return true wenn Graph ein DAG ist sonst false
	 * @throws Exception
	 */
	public boolean isDAG() throws Exception
	{
		this.changeOperation(false);	//gibt an ob Funktion die Graph-Struktur ändert
		
		boolean retVal= false;	//Rückgabewert
		Vector<String> allNodes= null; 	//Liste aller Knoten von G
		Vector<String> outNodes= null;	//Knoten ohne eingehende Kanten
		
		//wenn letzte Berechnung noch aktuell ist
		if (this.isDAG == trFaNu.TRUE) retVal= true;
		if (this.isDAG == trFaNu.FALSE) retVal= false;
		else
		{
			//alle Knoten in Vector schreiben
			allNodes= this.enumToVector(this.nodes.keys());
			//wenn G Knoten hat und gerichtet ist
			if ((allNodes != null) && (this.isDirected))
			{
				outNodes= new Vector<String>();
				//prüfen ob es Knoten ohne eingehende Kanten gibt
				//alle Knoten durchgehen
				for (String node: allNodes)
				{
					//wenn Knoten keine eingehende Kante hat schreibe in outNodes 
					if(!this.inEdges.containsKey(node)) outNodes.add(node);
				}
				//wenn G keine Knoten ohne eingehende Kanten hat, ist G kein DAG
				if (outNodes.size()== 0) retVal= false;
				// G hat Knoten ohne eingehende Kanten
				else
				{
					//prüfen auf Zyklus
					for(String toNode: outNodes)
					{	
						if (this.hasCycleRec(toNode, null)) 
						{
							retVal= false;
							break;
						}
						else retVal= true;
					}
				}
			}
			else retVal= false;
		}	
		//Berechnung eintragen
		if(retVal) this.isDAG = trFaNu.TRUE;
		else  this.isDAG = trFaNu.FALSE;
		
		return(retVal);
	}
	
	/**
	 * Gibt zurück, ob dieser Graph einen Zyklus besitzt 
	 * @return true, wenn Graph Zyklus hat, false sonst
	 */
	public boolean hasCycle() throws Exception
	{
		this.changeOperation(false);	//gibt an ob Funktion die Graph-Struktur ändert
		
		boolean retVal= false;
		
		
		//wenn letzte Berechnung noch aktuell ist
		if (this.hasCycle == trFaNu.TRUE) retVal= true;
		if (this.hasCycle == trFaNu.FALSE) retVal= false;
		//wenn letzte Berechnung nicht mehr aktuell ist
		else
		{
			retVal= !this.isDAG();
		}
		//Berechnung eintragen
		if(retVal) this.hasCycle = trFaNu.TRUE;
		else  this.hasCycle = trFaNu.FALSE;
		
		return(retVal);
	}
	
	/**
	 * Berechnet die Pre- und Post-Order für jeden Knoten. Dies funktioniert nur, wenn der Graph ein 
	 * gerichteter Baum ist, oder der Graph ein DAG mit einem Knoten ohne ausgehende Kanten
	 * ist. Dabei wird jedem Knoten der Wert Graph::pre und Graph::post gegeben.
	 */
	public void computePPOrder() throws Exception
	{
		this.changeOperation(false);	//gibt an ob Funktion die Graph-Struktur ändert
		
		// Prüfen, welcher Struktur der Graph entspricht
		if (!this.isTree())
			this.isDAG();
		
		//wenn Graph gerichtet ist
		if (this.isDirected== true)
		{
			//wenn Graph ein Baum ist
			if (isTree== trFaNu.TRUE)
			{
				ppOrderRek(this.root, 0);
			}
			//wenn Graph ein DAG ist
			if (this.isDAG == trFaNu.TRUE)
			{
				//für alle Knoten, die keine eingehenden Kanten haben
				Vector<String> allNodes= this.cloneStringVector(this.enumToVector(this.nodes.keys()));
				long ppOrder= 0;
				//prüfen ob es Knoten ohne eingehende Kanten gibt
				//alle Knoten durchgehen
				for (String node: allNodes)
				{
					//wenn Knoten keine eingehende Kante hat schreibe in outNodes 
					if(!this.inEdges.containsKey(node)) 
					{
						ppOrder= ppOrderRek(this.nodes.get(node), ppOrder);
						System.out.println("KNOTEN: "+ node);
					}
				}
			}
			//wenn Graph weder Baum noch DAG, kann keine PP-Order vergeben werden
			else  throw new Exception(ERR_NO_PP);
		}
	}
	
	/**
	 * Gibt zurück ob der Graph zusammenhängend ist.
	 * @return true wenn Graph zusammenhängend ist, false sonst
	 */
	public boolean isCoherent()
	{
		this.changeOperation(false);	//gibt an ob Funktion die Graph-Struktur ändert
		
		boolean isCoherent= false;
		
		
			stdOut.println(MSG_TODO);
	
		return(isCoherent);
	}
	
	
}
