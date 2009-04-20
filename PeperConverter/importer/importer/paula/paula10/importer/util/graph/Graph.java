package importer.paula.paula10.importer.util.graph;

import importer.paula.paula10.importer.util.graph.index.EdgeIndex;
import importer.paula.paula10.importer.util.graph.index.EdgeIndexImpl;
import importer.paula.paula10.importer.util.graph.index.Index;
import importer.paula.paula10.importer.util.graph.index.IndexMgr;
import importer.paula.paula10.importer.util.graph.index.IndexMgrImpl;
import importer.paula.paula10.importer.util.graph.index.NodeIndex;
import importer.paula.paula10.importer.util.graph.index.NodeIndexImpl;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
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
 * Dieser Graph ist um eine Indexverwaltung erweitert, das bedeutet es können 
 * eigenständig Indizes angelegt werden und dem internen Index-Manager des
 * Graphen übergeben werden. Der Index-Manager kümmert sich dann um das entfernen
 * von Knoten bzw. Kanten aus dem benutzerdefinierten index. Ein 
 * benutzerdefinierter Index muss vom Interface Index bzw. NodeIndex oder EdgeIndex
 * oder auch einer der diese Interfaces implementierenden Klassen abgeleitet sein.
 * 
 * @author Florian Zipser
 * @version 2.0
 *
 */
public class Graph 
{
	public enum TRAVERSAL_MODE {DEPTH_FIRST, BOTTOM_UP};
//	 ============================================== private Variablen ==============================================
	private static final boolean DEBUG= false;			//DEBUG-Schalter
	private static final boolean DEBUG_REM_EDGE= false;	//spezieller DEBUG-Schalter für das löschen von Kanten
	
	private static final String TOOLNAME= "Graph";	//Name der Klasse
	protected static final String DOT_ENDING= "dot";	//Name der Klasse
	protected static final String NS_SEP=	"::";		//Namensraumseperator
	
	protected static final String KW_PRE=	"PRE";	//Name des Attributes für den pre-Wert
	protected static final String KW_POST=	"POST";	//Name des Attributes für den post-Wert
	
	//interne Indexnamen
	/**
	 * Name des Indexes für die Knotennamen
	 */
	protected static final String IDX_NODENAME=	"idx_nodename";
	/**
	 * Name des Indexes für die Kantennamen
	 */
	protected static final String IDX_EDGENAME=	"idx_edgename";
	/**
	 * Name des Indexes für die ausgehenden Kanten( id= Knotenname, value= 
	 * alle von diesen Knoten ausgehende Kanten)
	 */
	protected static final String IDX_OUTEDGES=	"idx_outedges";
	/**
	 * Name des Indexes für die eingehenden Kanten (id= Knotenname, value= 
	 * alle in diesen Knoten eingehenden Kanten)
	 */
	protected static final String IDX_INEDGES=	"idx_inedges";
	
	/**
	 * Name dieses Graphobjektes
	 */
	protected String name=	null;	
	protected String ns=		Graph.getNS();				//das zu benutzende Namensraumprefix
	protected String preName= this.ns + NS_SEP + KW_PRE;	//verwendeter Attributname für die Pre-Order
	protected String postName= this.ns + NS_SEP + KW_POST;	//verwendeter Attributname für die Post-Order
	
	protected Logger logger= null;						//log4j zur Nachrichtenausgabe
	
	/**
	 * Der Indexmanager, der alle Indizes, also interne als auch extern hinzugefügte verwaltet
	 */
	private IndexMgr idxMgr= null;
	
	/**
	 * Tabelle der Knoten key= name, value= Knotenobjekt, hier werden die Knoten gespeichert
	 */
	//protected Map<String, Node> nodes= null;
	
	/**
	 * Liste aller Kanten zwischen den Knoten. Hier werden die Kanten gespeichert.
	 */
	protected Collection<Edge> edges= null;
	/**
	 * Index, der allen Kantennamen eine Kante zuordnet ACHTUNG: nicht jede Kante muss einen Namen besitzen
	 */
	//idx-old: protected Map<String, Edge> edgeNameIdx= null;
	protected Node currNode= null;						//aktueller Knoten
	
	//old
	//protected Hashtable<String, Vector<String>>outEdges= null;	//ausgehende Kanten des ganzen Graphen key= Knoten mit ausgehenden Kanten, value= liste der Knoten, zu denen  die Kanten gehen
	//protected Hashtable<String, Vector<String>>inEdges= null;		//eingehende Kanten des ganzen Graphen key= Knoten mit eingehenden Kanten, value= liste der Knoten, von denen aus die Kanten gehen
	/**
	 * ausgehende Kanten des ganzen Graphen key= Knoten mit ausgehenden Kanten, value= liste der Kanten
	 */
	//idx-oldprotected Map<String, Vector<Edge>> outEdges= null;
	/**
	 * eingehende Kanten des ganzen Graphen key= Knoten mit ausgehenden Kanten, value= liste der Kanten
	 */
	//idx-oldprotected Map<String, Vector<Edge>> inEdges= null;
	
	protected PrintStream stdOut= System.out;									//Standardausgabe für DEBUG-MSGs
	
	protected Node root= null;							//Pointer auf Wurzel, wenn Graph ein Baum
	protected Collection<Node> dagNodes= null;				//Pointer auf alle "Wurzeln" in einem DAG 
	
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
	Vector<Node> visitedNodes= null;					//Liste bereits besuchter Knoten
	//	 *************************************** Meldungen ***************************************
	protected static final String MSG_TODO=			TOOLNAME+">\tstill to be done";
	private static final String MSG_STD=			TOOLNAME+"> ";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	protected static final String MSG_NO_ROOT=		TOOLNAME+">\tGraph is no tree because it has no root.";
	protected static final String MSG_CYCLE=			TOOLNAME+">\tGraph is no tree because it has min one cycle.";
	protected static final String MSG_NOT_ALL_NODES=	TOOLNAME+">\tGraph is no tree because it is not coherent.";
	protected static final String MSG_INIT=			TOOLNAME+">\tGraph initialized.";
	protected static final String MSG_START_FCT=	MSG_STD + "start of method: ";
	protected static final String MSG_END_FCT=	MSG_STD + "end of method: ";
	//	 *************************************** Fehlermeldungen ***************************************
	protected static final String ERR_NODE_EXIST= 			MSG_ERR+ "A node with the given name already exists: ";
	protected static final String ERR_NODE_NOT_EXIST=		MSG_ERR+ "A node with this name doesn´t exists: ";
	protected static final String ERR_NODE_NAME_FAILURE= 	MSG_ERR+ "Node name is empty.";
	//protected static final String ERR_EMPTY_FILENAME= 		MSG_ERR+  + "File name was empty.";
	protected static final String ERR_UNKNOWN= 				MSG_ERR+ "Unknown error .";
	protected static final String ERR_NO_PP= 				MSG_ERR+ "Graph is no tree or dag, so no pre- and post-order is possible.";
	protected static final String ERR_NO_DOT= 				MSG_ERR+ "No dot-file name was given.";
	protected static final String ERR_TOO_MUCH_ROOTS=		MSG_ERR+ "Cannot compute rank relation, there are too much root nodes. This might be an internal error, root nodes: ";
	protected static final String ERR_NO_TREE_DAG=			MSG_ERR+ "The graph is neither a tree nor a dag.";
	protected static final String ERR_NO_TRVOBJ=			MSG_ERR+ "Cannot traverse graph, because there is no TraversalObject-obkject given.";
	protected static final String ERR_NO_STARTNODE=			MSG_ERR+ "Cannot traverse graph, because there is no startNode given.";
	protected static final String ERR_EMPTY_EDGE=			MSG_ERR+ "Cannot insert the given edge, beaceuse the edge is empty.";	 
	protected static final String ERR_NO_FROMNODE=			MSG_ERR+ "Cannot insert the given edge, the fromNode (node from wich the edge comes) is empty.";
	protected static final String ERR_NO_TONODE=			MSG_ERR+ "Cannot insert the given edge, the toNode (node to wich the edge goes) is empty.";
	protected static final String ERR_NO_SRC_NODE=			MSG_ERR+ "Cannot create edge, because the given source-node doesn´t exist in this graph: ";
	protected static final String ERR_NO_DST_NODE=			MSG_ERR+ "Cannot create edge, because the given destination-node doesn´t exist in this graph: ";
//	 ============================================== statische Methoden ==============================================
	
	/**
	 * Gibt den von diesem Tool verwendeten Namensraum zurück.
	 * @return der von diesem Tool verwendete Namensraum
	 */
	public static String getNS()
		{ return(TOOLNAME); }
	
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt ein Graph-Objekt
	 * @param isDirected boolean - true, wenn Graph gerichtet ist, false sonst
	 * @param isOrdered boolean - true, wenn Graph eine Knotenreihenfolge hat, false sonst  
	 */
	public Graph(boolean isDirected, boolean isOrdered) throws Exception
	{
		this.isDirected= isDirected;
		this.isOrdered= isOrdered;
		
		//IndexManager erzeugen
		idxMgr= new IndexMgrImpl();
		
		this.createIndexes();

		//Tabelle der Knoten initialisieren
		//old-idx: this.nodes= new Hashtable<String, Node>();
		
		//Listen mit Kanten auf Startwert setzen
		//idx-old this.initEdges();
		//Liste der "Wurzeln" für einen DAG initialisieren
		this.dagNodes= new Vector<Node>();
	}
	
	/**
	 * Erzeugt ein Graph-Objekt
	 * @param isDirected boolean - true, wenn Graph gerichtet ist, false sonst
	 * @param isOrdered boolean - true, wenn Graph eine Knotenreihenfolge hat, false sonst  
	 */
	public Graph(	String name,
					boolean isDirected, 
					boolean isOrdered) throws Exception
	{
		this(isDirected, isOrdered);
		this.name= name;
	}
	
	/**
	 * Erzeugt ein Graph-Objekt. Mit Nachrichtenstrom.
	 * @param isDirected boolean - true, wenn Graph gerichtet ist, false sonst
	 * @param isOrdered boolean - true, wenn Graph eine Knotenreihenfolge hat, false sonst  
	 * @param logger Logger - Log4j zur Nachrichtenausgabe
	 */
	public Graph(boolean isDirected, boolean isOrdered, Logger logger) throws Exception
	{
		this(isDirected, isOrdered);
		this.logger= logger;
		
		if (this.logger != null) this.logger.info(MSG_INIT);
	}
	
	/**
	 * Erzeugt ein Graph-Objekt. Mit Nachrichtenstrom.
	 * @param isDirected boolean - true, wenn Graph gerichtet ist, false sonst
	 * @param isOrdered boolean - true, wenn Graph eine Knotenreihenfolge hat, false sonst  
	 * @param prefix String - das Namensraumprefix, dass für den Namensraum dieses Objekts verwendet werden soll (bspw. bei Pre- und Postorder), ist dieses null oder leer, wird das Standard-Prefix: graph verwendet
	 * @param logger Logger - log4j Nachrichtenstrom  
	 */
	public Graph(boolean isDirected, boolean isOrdered, String prefix, Logger logger) throws Exception
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
	 * Erzeugt alle internen Indizes:
	 * <ul>
	 * 	<li>Knotennamenindex</li>
	 * </ul>
	 */
	protected void createIndexes() throws Exception
	{
		Index idx= null;
		
		//Knotennamenindex erzeugen
		idx= new NodeIndexImpl(IDX_NODENAME);
		this.idxMgr.addIndex(idx);
		
		//Liste der Kanten initialisieren
		this.edges= new Vector<Edge>();
		
		//Kantenindizes erstellen
		//Kantennamenindex erstellen
		idx= new EdgeIndexImpl(IDX_EDGENAME);
		this.idxMgr.addIndex(idx);
		//Index der ausgehenden Kanten erstellen
		idx= new EdgeIndexImpl(IDX_OUTEDGES);
		this.idxMgr.addIndex(idx);
		//Index der eingehenden Kanten erstellen
		idx= new EdgeIndexImpl(IDX_INEDGES);
		this.idxMgr.addIndex(idx);
	}
	
	/**
	 * Setzt alle Objekte, die Kanten speichern auf den Startwert.
	 */
	private void initEdges()
	{
		//idx-old:
		/*
		//Liste der Kanten initialisieren
		this.edges= new Vector<Edge>();
		//Index, der Kantennamen mit Namen verbindet initialisieren
		this.edgeNameIdx= new Hashtable<String,Edge>(); 
		//Index, der einen Knotennamen mit ausgehenden Kantenverbindet initialisieren
		this.outEdges= new Hashtable<String,Vector<Edge>>();
		//Index, der einen Knotennamen mit eingeheneden Kantenverbindet initialisieren
		this.inEdges= new Hashtable<String,Vector<Edge>>();
		*/
	}
	
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
		//old-idx: boolean isThere = this.nodes.containsKey(name);
		boolean isThere = this.idxMgr.getIndex(IDX_NODENAME).hasId(name);
		
		//wenn Knoten existieren soll 
		if (exists) 
		{	
			if (isThere) return(true);
			else throw new Exception(ERR_NODE_NOT_EXIST + name);
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
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
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
	}*/
	
	/**
	 * Stellt den rekursiven Aufruf zum Traversieren des Baumes dar. Gibt zurück, ob der Graph ein Baum ist.
	 * Bedient sich dabei von der globalen Variable visitedNodes.
	 * @param nodeList List zu besuchender Knoten
	 * @return true, wenn Graph ein Baum
	 * @throws Exception
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
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
				if (this.outEdges.get(node) != null) 
				{
					tmpOut= new Vector<String>();
					for(Edge edge : this.outEdges.get(node))
						tmpOut.add(edge.getToNode().getName());
					//old
					// tmpOut= this.cloneEdgeVector(this.outEdges.get(node));
				}
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
	}*/
	
	/**
	 * Vergibt rekursiv die Pr- und Poste-Order auf die einzelnen Knoten
	 * @param node Node - Knoten der nummeriert werden soll 
	 * @param ppOrder long - aktueller Pre- und Post-Order-Wert
	 * @return long - Pre- und Post-Order nach diesem Knoten und all seinen Unterknoten
	 * @throws Exception
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
	private long ppOrderRek(Node node, long ppOrder) throws Exception
	{
		if (DEBUG) stdOut.println("current node (name/ppOrder):\t" + node + ", " + ppOrder);
		
		Vector<Long> ppOrderVec= null;	//long-Vector für die PPOrder (bei DAGs)
		
		// wenn Graph-Struktur ein Baum, jeder Knoten hat nur einen Pre und Postwert
		if(this.isTree== trFaNu.TRUE) node.setValue(KW_PRE, ppOrder);
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
			for(Edge edge: this.getOutEdges(node))
				ppOrder= ppOrderRek(this.nodes.get(edge.getToNode().getName()), ppOrder);
			//old
			/*
			for(String nodeName: this.outEdges.get(node.getName()))
			{
				ppOrder= ppOrderRek(this.nodes.get(nodeName), ppOrder);
			}*/
/*
		}
		if (DEBUG) stdOut.println("current node (name/ppOrder):\t" + node + ", " + ppOrder);
		
		//Post-Order-Vergabe
		//wenn Graph ein Baum
		if (this.isTree== trFaNu.TRUE) node.setValue(KW_POST, ppOrder);
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
	}*/

	/**
	 * Prüft ob dieser Graph, ausgehend von dem übergebenen Knoten einen Zyklus besiztzt. Ist der
	 * rekursive Aufruf der Methode hasCycle. 
	 * @param node String - Startknoten, von dem aus an der Graph traversiert wird
	 * @param seenNodes Vector<String> - auf dem Pfad bereits gesehene Knoten
	 * @return true, wenn Grah einen Zyklus besitzt.
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
	private boolean hasCycleRec(String node, Vector<String> seenNodes) throws Exception
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
			nodesToVisit= new Vector<String>();
			for (Edge edge: this.getOutEdges(node))
				nodesToVisit.add(edge.getToNode().getName());
			//old
			//nodesToVisit= this.cloneStringVector(this.outEdges.get(node));
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
	}*/
	
	/**
	 * Gibt eine Kopie eines Vectors zurück, der mit Strings gefüllt ist.
	 * @param vec Vector<String> - zu kopierender Vector
	 * @return neuer String-Vector
 	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
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
	}*/
	
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
	 * Schreibt den Graphen in eine Dot-Datei. Ruft dabei die Methoden nodesToDOT und edgesTODot auf.
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
		
		//Vector<String> allNodes= this.enumToVector(this.nodes.keys());
		//old-idx: Collection<String> allNodes= this.nodes.keySet();
		Collection<String> allNodes= ((NodeIndex)this.idxMgr.getIndex(IDX_NODENAME)).getNodeIds();
		
		//alle Knoten auf den Stream schreiben
		for (String nodeName: allNodes)
		{
			//old-idx: oStream.println(this.nodesToDOT(this.nodes.get(nodeName)) + ";");
			for (Node node: ((NodeIndex)this.idxMgr.getIndex(IDX_NODENAME)).getEntry(nodeName))
				oStream.println(this.nodesToDOT(node) + ";");
		}
		//Kanten in Stream schreiben
		Collection<Edge> tmpOut= null;
		for (String nodeName: allNodes)
		{
			//idx-old: tmpOut= this.outEdges.get(nodeName);
			tmpOut= ((EdgeIndex)this.idxMgr.getIndex(IDX_OUTEDGES)).getEntry(nodeName);
			//wenn Knoten ausgehende Kanten hat
			if (tmpOut != null)
			{
				for(Edge out: tmpOut)
				{
					oStream.println(edgesToDOT(nodeName, out.getToNode().getName()) + ";");
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
		return(node.toDOT());
		/*
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
		*/
	}
	
	/**
	 * Gibt einen String zurück, indem die Knoten nach DOT-Format formatiert sind (ohne Simikolon)
	 * @param from Knoten von dem die Kante ausgeht
	 * @param to Knoten zu dem die Kante geht
	 */
	protected String edgesToDOT(String from, String to) throws Exception
	{
		String retStr= "";
		
		Collection<Edge> outEdges= this.getOutEdges(from);
		for (Edge edge: outEdges)
			if (edge.getToNode().getName().equalsIgnoreCase(to))
				{retStr= edge.toDOT();}
		
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
	
// ====================================== Start: Graph-Traversierung ====================================== 
	
	/**
	 * Diese Methode traversiert auf unterschiedliche Methoden den Graphen und ruft dabei
	 * über ein Callback die Funktionen eines TraversalObjects bzw. XTraversalObjects auf.
	 * Es können verschiedene Traversionsmethoden realisiert werden, abhängig von der 
	 * Traversionsmethode tMode ist die Reihenfolge der besuchten Knoten. Diese Methode
	 * beginnt bei mehereren Startknoten
	 * Die unterstützten Traversionsmethoden sind:<br/>
	 * <ul>
	 * 	<li>DepthFirst - childNode= aktueller Knoten</li>
	 *	<li>BottomUp - fatherNode= aktueller Knoten</li>
	 * </ul>
	 * @param tMode TRAVERSAL_MODE - der Modus/ die Art der Traversion
	 * @param startNodes Vector<Node> - Liste von Knoten bei denen begonnen werden soll
	 * @param travObj TraversalObject - Das über ein Callback aufzurufende TraversalObjekt
	 */
	public void traverseGraph(	TRAVERSAL_MODE tMode, 
								Collection<Node> startNodes, 
								TraversalObject travObj) throws Exception
	{
		if (travObj== null) throw new Exception(ERR_NO_TRVOBJ);
		if ((startNodes== null) || (startNodes.isEmpty())) 
			throw new Exception(ERR_NO_STARTNODE);
		this.travObj= travObj;
		
		//wenn sMode= BottomUp
		if (tMode== TRAVERSAL_MODE.BOTTOM_UP)
		{
			this.visitedNodes= new Vector<Node>();
			//mit allen Knoten BottomUp traversieren
			for (Node startNode: startNodes)
				this.bottomUpRec(startNode, null, null, 0);
			this.visitedNodes= null;
		}
		//wenn sMode= DepthFirst
		else if (tMode== TRAVERSAL_MODE.DEPTH_FIRST)
		{
			this.visitedNodes= new Vector<Node>();
			//mit allen Knoten BottomUp traversieren
			for (Node startNode: startNodes)
				this.depthFirstRec(startNode, null, null, 0);
			this.visitedNodes= null;
		}
	}
	
	/**
	 * Diese Methode traversiert auf unterschiedliche Methoden den Graphen und ruft dabei
	 * über ein Callback die Funktionen eines TraversalObjects bzw. XTraversalObjects auf.
	 * Es können verschiedene Traversionsmethoden realisiert werden, abhängig von der 
	 * Traversionsmethode tMode ist die Reihenfolge der besuchten Knoten. 
	 * Die unterstützten Traversionsmethoden sind:<br/>
	 * <ul>
	 * 	<li>DepthFirst - childNode= aktueller Knoten</li>
	 *	<li>BottomUp - fatherNode= aktueller Knoten</li>
	 * </ul>
	 * @param tMode TRAVERSAL_MODE - der Modus/ die Art der Traversion
	 * @param startNode Node - der Knoten bei dem begonnen werden soll
	 * @param travObj TraversalObject - Das über ein Callback aufzurufende TraversalObjekt
	 */
	public void traverseGraph(	TRAVERSAL_MODE tMode, 
								Node startNode, 
								TraversalObject travObj) throws Exception
	{
		if (travObj== null) throw new Exception(ERR_NO_TRVOBJ);
		if (startNode== null) throw new Exception(ERR_NO_STARTNODE);
		this.travObj= travObj;
		
		//wenn sMode= depthFirst
		if (tMode== TRAVERSAL_MODE.DEPTH_FIRST)
		{
			//old
			//this.depthFirst(startNode, travObj);
			this.depthFirstRec(startNode, null, null, 0);
		}
		//wenn sMode= BottomUp
		else if (tMode== TRAVERSAL_MODE.BOTTOM_UP)
		{
			this.bottomUpRec(startNode, null, null, 0);
		}
	}
	
	/**
	 * Rekursiver Aufruf für die Methode depthFirst
	 * @param currNode Node - aktueller Knoten
	 * @param edge Edge - Kante, die die beiden Knoten verbindet
	 * @param father - Vater des aktuellen Knotens
	 * @param order - Reihenfolge in der der aktuelle Knoten in der Kinderliste des Vaters vorkommt (beginnend bei null)
	 * @throws Exception
	 */
	protected void depthFirstRec(	Node currNode,
									Edge edge,
									Node father,
									long order) throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_START_FCT + "depthFirstRec()");
		
		this.travObj.nodeReached(TRAVERSAL_MODE.DEPTH_FIRST, currNode, edge, father, order);
		
		//durch alle Kinder dieses Knotens gehen
		Collection<Edge> childEdges= this.getOutEdges(currNode);
		//wenn Knoten Kinder hat
		if (childEdges != null)
		{
			//gehe durch alle Kinder des aktuellen Knoten
			int i= 0;
			for(Edge childEdge: childEdges)
			{
				Node childNode= childEdge.getToNode();
				try{
					//versuche Cast in XTraversalObject
					if (((XTraversalObject)this.travObj).checkConstraint(TRAVERSAL_MODE.DEPTH_FIRST, childEdge, childNode))
					{
						this.depthFirstRec(childNode, childEdge, currNode, i);
						i++;
					}
				}
				//Object ist nur TraversalObject
				catch (ClassCastException e)
				{
					this.depthFirstRec(childNode, childEdge, currNode, i);
					i++;
				}
			}
		}
		this.travObj.nodeLeft(TRAVERSAL_MODE.DEPTH_FIRST, currNode, edge, father,  order);
		
		if (this.logger!= null) this.logger.debug(MSG_END_FCT + "depthFirstRec()");
	}
	
	/**
	 * Macht eine Tiefensuche beginnend bei der Wurzel, so es denn eine gibt. 
	 * Über ein Rollback wird
	 * für jedee Traversion eine Methode aufgerufen, der der aktuelle Knoten und dessen 
	 * Vater übergeben wird. Funktioniert nur für Bäume und für DAGs mit genau einer Wurzel.
	 * Der Graph muss gerichtet sein. Wenn trvObj null, passiert nichts.
	 * @param travObj TraversalObject - Object, dass über ein Callback benachrichtigt werden soll wenn ein neuer Knoten erreicht wurde 
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
	public void depthFirst(TraversalObject travObj) throws Exception
		{ this.depthFirst(this.getRoot(), travObj); }
	*/
	
	/**
	 * Macht eine Tiefensuche beginnend bei dem übergebenen Knoten. Ist dieser Knoten 
	 * null wird bei der Wurzel begonnen, so es denn eine gibt. Über ein Rollback wird
	 * für jedee Traversion eine Methode aufgerufen, der der aktuelle Knoten und dessen 
	 * Vater übergeben wird. Funktioniert nur für Bäume und für DAGs mit genau einer Wurzel.
	 * Der Graph muss gerichtet sein. Wenn trvObj null, passiert nichts.
	 * @param startNode Node - Knoten bei dem die Traversierung begonnen werden soll
	 * @param travObj TraversalObject - Object, dass über ein Callback benachrichtigt werden soll wenn ein neuer Knoten erreicht wurde 
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	//TODO es muss geprüft werden, inwiefern diese Methode noch gebraucht wird
	/*
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
		
		this.depthFirstRec(startNode, null, null, 0);
		//travObj zurücksetzen
		this.travObj= null;
	}
	*/
	
	/**
	 * Rekursiver Aufruf für die Methode bottomUp
	 * @param currNode Node - aktueller Knoten
	 * @param edge Edge - Kante, die die beiden Knoten verbindet
	 * @param child - Kindknoten des aktuellen Knotens
	 * @param order - Reihenfolge in der der aktuelle Knoten in der Kinderliste des Vaters vorkommt (beginnend bei null)
	 * @throws Exception
	 */
	protected void bottomUpRec(	Node currNode,
								Edge edge,
								Node child,
								long order) throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_START_FCT + "depthFirstRec()");
		
		//wenn dieser Knoten schon einmal besichtigt wurde, dann abbruch
		if ((this.visitedNodes!= null) && (this.visitedNodes.contains(currNode)));
		//sonst weiter traversieren
		else
		{
			if (this.visitedNodes!= null) this.visitedNodes.add(currNode);
			this.travObj.nodeReached(TRAVERSAL_MODE.BOTTOM_UP, currNode, edge, child, order);
			
			//durch alle Väter dieses Knotens gehen
			Collection<Edge> parentEdges= this.getInEdges(currNode);
			//wenn Knoten Kinder hat
			if (parentEdges != null)
			{
				//gehe durch alle Väter des aktuellen Knoten
				int i= 0;
				for(Edge parentEdge: parentEdges)
				{
					Node parentNode= parentEdge.getFromNode();
					try{
						//versuche Cast in XTraversalObject
						if (((XTraversalObject)this.travObj).checkConstraint(TRAVERSAL_MODE.BOTTOM_UP, parentEdge, parentNode))
						{
							this.bottomUpRec(parentNode, parentEdge, currNode, i);
							i++;
						}
					}
					//Object ist nur TraversalObject
					catch (ClassCastException e)
					{
						this.bottomUpRec(parentNode, parentEdge, currNode, i);
						i++;
					}
				}
			}
			this.travObj.nodeLeft(TRAVERSAL_MODE.BOTTOM_UP, currNode, edge, child,  order);
		}	
		if (this.logger!= null) this.logger.debug(MSG_END_FCT + "depthFirstRec()");
	}
	
// ====================================== Start: Graph-Traversierung ======================================	

	/**
	 * Gibt den Namen dieses Graphobjektes zurück.
	 * @return Name dieses Graphobjektes
	 */
	public String getName() throws Exception
		{ return(this.name); }
// ====================================== Start: Kanten zurückgeben ======================================

	/**
	 * Gibt eine Liste von zu dem übergebenden Knoten eingehenden Kanten zurück.
	 * Die in dieser Liste enthaltenen Kanten haben den übergebenen Kontextknoten also
	 * als Zielknoten.
	 * @param Node cNode - Kontextknoten den alle zurückgegebenen Kanten zum Ziel haben
	 * @return Liste an Kanten, die den Kontextknoten zum Ziel haben
	 */
	public Collection<Edge> getInEdges(Node cNode) throws Exception
	{	
		return(((EdgeIndex)this.idxMgr.getIndex(IDX_INEDGES)).getEntry(cNode.getName()));
		//idx-old: return(this.inEdges.get(cNode.getName())); 
	}
	
	/**
	 * Gibt eine Liste von zu dem übergebenden Knoten eingehenden Kanten zurück.
	 * Die in dieser Liste enthaltenen Kanten haben den übergebenen Kontextknoten also
	 * als Zielknoten.
	 * @param cNodeName String - Kontextknotenname den alle zurückgegebenen Kanten zum Ziel haben
	 * @return Liste an Kanten, die den Kontextknoten zum Ziel haben
	 */
	public Collection<Edge> getInEdges(String cNodeName) throws Exception
	{	return(this.getInEdges(this.getNode(cNodeName))); }
	
	/**
	 * Gibt eine Liste von zu dem übergebenden Knoten eingehenden Kanten zurück.
	 * Die in dieser Liste enthaltenen Kanten haben den übergebenen Kontextknoten also
	 * als Quelleknoten.
	 * @param Node cNode - Kontextknoten den alle zurückgegebenen Kanten zum Quelle haben
	 * @return Liste an Knaten, die den Kontextknoten zum Quelle haben
	 */
	public Collection<Edge> getOutEdges(Node cNode) throws Exception
	{	
		return(((EdgeIndex)this.idxMgr.getIndex(IDX_OUTEDGES)).getEntry(cNode.getName()));
		//idx-old: return(this.outEdges.get(cNode.getName())); 
	}
	
	/**
	 * Gibt eine Liste von zu dem übergebenden Knoten eingehenden Kanten zurück.
	 * Die in dieser Liste enthaltenen Kanten haben den übergebenen Kontextknoten also
	 * als Quelleknoten.
	 * @param String cNodeName - Kontextknotenname den alle zurückgegebenen Kanten zum Quelle haben
	 * @return Liste an Kanten, die den Kontextknoten zum Quelle haben
	 */
	public Collection<Edge> getOutEdges(String cNodeName) throws Exception
	{	return(this.getOutEdges(this.getNode(cNodeName))); }
	
// ====================================== Ende: Kanten zurückgeben ======================================
	
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
		if (this.logger != null) this.logger.debug(MSG_STD+"node '"+node.getName()+"' inserted");
		//Fehler, wenn Knoten mit diesem Namen bereits vorhanden
		//idx-old: if (this.nodes.containsKey(node.getName())) throw new Exception(ERR_NODE_EXIST + node.getName());
		if (this.idxMgr.getIndex(IDX_NODENAME).hasEntry(node.getName())) 
			throw new Exception(ERR_NODE_EXIST + node.getName());
		//idx-old: this.nodes.put(node.getName(), node);
		this.idxMgr.getIndex(IDX_NODENAME).addEntry(node.getName(), node);
		this.currNode= node;
		this.root= null;
	}
	
	/**
	 * Entfernt den Knoten mit dem übergebnen Namen aus dem Graphen.
	 * @param nodeName String- Name des zu entfernenden Knoten
	 * @throws Exception Fehler, wenn Knoten nicht im Graphen oder Knoten nicht entfernt werden kann
	 */
	public void removeNode(String nodeName) throws Exception
	{
		Node node= this.getNode(nodeName);
		this.removeNode(node);
	}
	
	/**
	 * Entfernt den übergebnen Knoten aus dem Graphen.
	 * @param node Node - der zu entfernende Knoten
	 * @throws Exception Fehler, wenn Knoten nicht im Graphen oder Knoten nicht entfernt werden kann, ist kNoten null geschieht nichts
	 */
	public void removeNode(Node node) throws Exception
	{
		if (node!= null)
		{
			this.changeOperation(true); //gibt an ob Funktion die Graph-Struktur ändert
			if (!this.idxMgr.getIndex(IDX_NODENAME).hasEntry(node)) throw new Exception(ERR_NODE_NOT_EXIST + node.getName());
			//Knoten aus allen Indizes löschen
			this.idxMgr.removeEntry(node);
			//alle Kanten, die mit diesem Knoten zu tun haben löschen
			Collection<Edge> depEdges= new Vector<Edge>();	//Liste der Abhängigkeitskanten
			for (Edge edge: edges)
			{
				//wenn Kante etwas mit Knoten zu tun hat, Kante zu Abhängigkeitskanten hinzufügen 
				if ((edge.getFromNode()== node) || (edge.getToNode()== node))
					depEdges.add(edge);
			}
			if ((depEdges!= null) && (depEdges.size()> 0))
			{
				//alle Abhängigkeitskanten löschen
				for (Edge edge: depEdges)
				{
					this.removeEdge(edge);
				}
			}
		}
	}
	
	/**
	 * Gibt den aktuellen Knoten zurück.
	 * @return aktueller Knoten, null wenn Graph leer
	 */
	//TODO muss an das löschen von Knoten angepasst werden 
	/*
	public Node getCurrNode()
	{
		this.changeOperation(false); //gibt an ob Funktion die Graph-Struktur ändert
		return(this.currNode); 
	}*/
	
	/**
	 * Sucht den Knoten mit dem übergebenen Namen und gibt diesen zurück. Ist der Knoten nicht 
	 * vorhanden, wird null zurückgegeben
	 * @param name String - Name des zu suchenden Knotens
	 * @return Node, Knoten, der auf diesen Namen passt, oder null wenn kein Knoten existiert
	 */
	public Node getNode(String name) throws Exception
	{ 
		//idx-old: return(this.nodes.get(name));
		//Index gibt eine Collection zurück, die nur ein Element enthalten kann, leider verfügt Collection nicht über getFirst()
		Node retNode= null;
		Collection<Node> nodes= ((NodeIndex)this.idxMgr.getIndex(IDX_NODENAME)).getEntry(name);
		if ((nodes!= null) && (nodes.size() > 0))
		{
			for (Node node: nodes)
				retNode= node;
		}
		return(retNode);
	}
	
	/**
	 * Gibt alle Kanten in diesem Graphen als Kantenobjekte zurück. 
	 * @return alle Kanten dieses Graphen, null wenn Graph keine Kanten enthält
	 */
	public Collection<Edge> getEdges()
	{ 
		if ((this.edges== null) || (this.edges.isEmpty())) return(null);
		return(this.edges); 
	}
	
	/**
	 * Sucht einen Kante anhand des übergebenen Namen. Existiert eine solche Kante nicht,
	 * wird null zurückgegeben,
	 * @param name String - Name der zu suchenden Kante
	 */
	public Edge getEdge(String name) throws Exception
	{
		Edge retEdge= null;
		
		Collection<Edge> edges= ((EdgeIndex)this.idxMgr.getIndex(IDX_EDGENAME)).getEntry(name);
		if ((edges!= null ) && (edges.size()> 0))
			for (Edge edge: edges)
				retEdge= edge;
		return(retEdge);
		//idx-old: return(edgeNameIdx.get(name));
	}
	
	/**
	 * Sucht eine Kante, die vom Knoten from zum Knoten to führt und gibt diese zurück.
	 * @param from Node - Knoten, von dem die Kante ausgehen soll
	 * @param to Node - Knoten, zu dem die Kante führen soll
	 * @return Kante, die vom Knoten form zum Knoten to führt, null wenn Kante nicht existiert
	 */
	public Edge getEdge(Node from, Node to) throws Exception
	{
		Edge retEdge= null;
		Collection<Edge> edges= this.getOutEdges(from);
		for (Edge edge: edges)
		{
			if (edge.getToNode()== to)
			{
				retEdge= edge;
				break;
			}
		}
		return(retEdge);
	}
	
	/**
	 * Sucht eine Kante, die vom Knoten from zum Knoten to führt und gibt diese zurück.
	 * @param fromName String - Name des Knotens, von dem die Kante ausgehen soll
	 * @param toName String - Name des Knotens, zu dem die Kante führen soll
	 * @return Kante, die vom Knoten form zum Knoten to führt, null wenn Kante nicht existiert
	 */
	public Edge getEdge(String fromName, String toName) throws Exception
		{	return(this.getEdge(this.getNode(fromName), this.getNode(toName))); }
	
	/**
	 * Erzeugt eine Kante zwischen zwei Knoten. Die Knoten werden über ihre eindeutigen 
	 * Namen identifiziert. Fehler, wenn Namen leer oder Knoten nicht existieren. Es
	 * können auch Multikanten erzeugt werden. Das Kantenlabel wird nicht gesetzt.
	 * @param fromName String - Name des Knoten von dem aus die Kante geht
	 * @param toName String- Name des Knoten zu dem die Kante geht
	 * @return die erzeugte Kante wird anschließend zurückgegeben
	 */
	public Edge createEdge(String fromName, String toName) throws Exception
	{
		return(this.createEdge(this.getNode(fromName), this.getNode(toName), null));
	}
	
	/**
	 * Erzeugt eine Kante zwischen zwei Knoten. Die Knoten werden über ihre eindeutigen 
	 * Namen identifiziert. Fehler, wenn Namen leer oder Knoten nicht existieren. Es
	 * können auch Multikanten erzeugt werden. Das Kantenlabel wird nicht gesetzt.
	 * @param fromNode Node - Knoten von dem aus die Kante geht
	 * @param toNode Node- Knoten zu dem die Kante geht
	 * @return die erzeugte Kante wird anschließend zurückgegeben
	 */
	public Edge createEdge(Node fromNode, Node toNode) throws Exception
	{
		return(this.createEdge(fromNode, toNode, null));
	}
	
	/**
	 * Erzeugt eine Kante zwischen zwei Knoten. Fehler, wenn Namen leer oder Knoten nicht existieren. Es
	 * können auch Multikanten erzeugt werden
	 * @param fromNode Node - Knoten von dem aus die Kante geht
	 * @param toNode Node- Knoten zu dem die Kante geht
	 * @param labels Hashtable<String, String> - Tabelle der Labels (Attribut-Wert-Paare)
	 * @return die erzeugte Kante wird anschließend zurückgegeben
	 */
	public Edge createEdge(Node fromNode, Node toNode, Hashtable<String, String>labels) throws Exception
	{
		this.changeOperation(true); //gibt an ob Funktion die Graph-Struktur ändert
		if (this.logger != null) this.logger.debug(MSG_STD + "create edge ("+fromNode.getName() + ", "+ toNode.getName() +")");
		//Prüfe Knoten auf Namen und Existenz
		if (!checkNode(fromNode.getName(), true)) throw new Exception(ERR_NO_SRC_NODE + fromNode.getName());
		if (!checkNode(toNode.getName(), true)) throw new Exception(ERR_NO_DST_NODE + fromNode.getName());
		
		//neue Kante erstellen
		Edge edge= new Edge(fromNode, toNode, labels);
		//erzeugte Kante zurückgeben
		return(createEdge(edge));
	}
	
	/**
	 * Fügt das übergebene Kantenobjekt in diesen Graphen ein und setzt dabei alle 
	 * nötigen Indizes.
	 * @param edge Edge - Das einzufügende Kantenobjekt
	 */
	public Edge createEdge(Edge edge) throws Exception
	{
		if (edge== null) throw new Exception(ERR_EMPTY_EDGE);
		if (edge.fromNode== null) throw new Exception(ERR_NO_FROMNODE);
		if (edge.toNode== null) throw new Exception(ERR_NO_TONODE);
		
		//neu erstellte Kante in Liste der Kanten einfügen
		this.edges.add(edge);
		
		//Kante in Namensindex eintragen
		if ((edge.getName()!= null) && (!edge.getName().equalsIgnoreCase("")))
		{
			((EdgeIndex)this.idxMgr.getIndex(IDX_EDGENAME)).addEntry(edge.name, edge);
		}
		//Kante in outEdges eintragen
		((EdgeIndex)this.idxMgr.getIndex(IDX_OUTEDGES)).addEntry(edge.getFromNode().getName(), edge);
		//Kante in inEdges eintragen
		((EdgeIndex)this.idxMgr.getIndex(IDX_INEDGES)).addEntry(edge.getToNode().getName(), edge);
		return(edge);
		
		
		/*
		if (edge== null) throw new Exception(ERR_EMPTY_EDGE);
		if (edge.fromNode== null) throw new Exception(ERR_NO_FROMNODE);
		if (edge.toNode== null) throw new Exception(ERR_NO_TONODE);
		
		//neu erstellte Kante in Liste der Kanten einfügen
		this.edges.add(edge);
		//Kante in Namensindex eintragen
		if ((edge.getName()!= null) && (!edge.getName().equalsIgnoreCase("")))
			((EdgeIndex)this.idxMgr.getIndex(IDX_EDGENAME)).addEntry(edge.name, edge);
			//idx-old: this.edgeNameIdx.put(edge.name, edge);
		//idx-old: Vector<Edge> listOfEdges= null;
		//Kante in outEdges eintragen
		((EdgeIndex)this.idxMgr.getIndex(IDX_OUTEDGES)).addEntry(edge.getFromNode(), edge);
		/*
		//idx-old: 
		listOfEdges= getOutEdges(edge.fromNode);
		if (listOfEdges == null)
		{
			listOfEdges= new Vector<Edge>();
			listOfEdges.add(edge);
			this.outEdges.put(edge.fromNode.getName(), listOfEdges);
		}
		else
			listOfEdges.add(edge);
		*/
		//Kante in inEdges eintragen
		//((EdgeIndex)this.idxMgr.getIndex(IDX_INEDGES)).addEntry(edge.getToNode(), edge);
		/*
		//idx-old:
		listOfEdges= getInEdges(edge.toNode);
		if (listOfEdges == null)
		{
			listOfEdges= new Vector<Edge>();
			listOfEdges.add(edge);
			this.inEdges.put(edge.toNode.getName(), listOfEdges);
		}
		else
			listOfEdges.add(edge);
		*/
		//eingefügte Kante zurückgeben
		//return(edge);
	}
	
	/**
	 * Erzeugt eine Kante zwischen zwei Knoten. Fehler, wenn Knoten leer oder Knoten nicht existieren
	 * @param from Node - Knoten von dem aus die Kante geht
	 * @param to Node - Knoten zu dem die Kante geht
	 */
	//old
	/*
	public void createEdge(Node from, Node to) throws Exception
		{ this.createEdge(from.getName(), to.getName()); }
	*/
	
	/**
	 * Entfernt eine Kante zwischen zwei Knoten. Fehler wenn Namen nicht vorhanden.
	 * @param from String - Name des Knotens von dem aus die Kante geht
	 * @param to String - Name des Knotens zu dem die Kante geht
	 */
	public void removeEdge(String from, String to) throws Exception
	{
		this.removeEdge(this.getNode(from), this.getNode(to));
	}
	
	/**
	 * Entfernt eine Kante zwischen zwei Knoten. Gibt es mehrere Kanten zwischen zwei Knoten,
	 * werden alle gelöscht
	 * @param fromNode Node - Knoten von dem aus die Kante geht
	 * @param toNode Node - Knoten zu dem die Kante geht
	 */
	public void removeEdge(Node fromNode, Node toNode) throws Exception
	{
		Collection<Edge> listOfEdges= this.getOutEdges(fromNode.getName());
		for (Edge edge : listOfEdges)
		{
			if (edge.getToNode().equals(toNode))
				this.removeEdge(edge);
		}
	}
	
	/**
	 * Entfernt die übergebene Kante zwischen zwei Knoten. Tut nichts, 
	 * wenn übergebene Kante leer ist. 
	 * @param edge Egde - Die zu löschende Kante
	 */
	public void removeEdge(Edge edge) throws Exception
	{
		if (edge!= null)
		{
			if (DEBUG_REM_EDGE) 
				System.out.println(MSG_STD + "removing edge: "+ edge.getName());
			//gibt an ob Funktion die Graph-Struktur ändert
			this.changeOperation(true); 
			//Kante aus Liste der Kanten löschen
			this.edges.remove(edge);
			//lösche Kante aus allen Indizes
			this.idxMgr.removeEntry(edge);
			/*
			//Kante aus Namensindex löschen, wenn Kante einen Namen hat
			if (edge.getName()!= null) 
				((EdgeIndex)this.idxMgr.getIndex(IDX_EDGENAME)).removeEntry(edge);	
			//Kante aus ausgehenden Kanten löschen
			((EdgeIndex)this.idxMgr.getIndex(IDX_OUTEDGES)).removeEntry(edge);
			//Kante aus ausgehenden Kanten löschen
			((EdgeIndex)this.idxMgr.getIndex(IDX_INEDGES)).removeEntry(edge);
			*/
			/*
			if (DEBUG_REM_EDGE) 
				System.out.println(MSG_STD + "removing edge: "+ edge.getName());
			//gibt an ob Funktion die Graph-Struktur ändert
			this.changeOperation(true); 
			//Kante aus Liste der Kanten löschen
			this.edges.remove(edge);
			System.out.println("NUN HIER 1");
			Collection<Edge> listOfEdges= null;
			//Kante aus Namensindex löschen, wenn Kante einen Namen hat
			if (edge.getName()!= null) 
				((EdgeIndex)this.idxMgr.getIndex(IDX_EDGENAME)).removeEntry(edge.getName());
				//idx-old: this.edgeNameIdx.remove(edge.getName());
			System.out.println("NUN HIER 2");
			
			//Kante aus ausgehenden Kanten löschen
			this.idxMgr.getIndex(IDX_OUTEDGES).removeEntry(edge);
			//Kante aus ausgehenden Kanten löschen
			this.idxMgr.getIndex(IDX_INEDGES).removeEntry(edge);
			/*
			System.out.println("NUN HIER 2");
			//Kante aus Tabelle der eingehenden Kanten löschen
			listOfEdges= this.getInEdges(edge.getToNode());
			listOfEdges.remove(edge);
			System.out.println("NUN HIER 3");
			//Kante aus Tabelle der ausgehenden Kanten löschen
			listOfEdges= this.getOutEdges(edge.getFromNode());
			listOfEdges.remove(edge);
			*/
		}
	}
	
	/**
	 * Entfernt alle Kanten innerhalb dieses Graphen.
	 * @param edge Egde - Die zu löschende Kante
	 */
	public void removeEdges() throws Exception
	{
		//alle Kanten aus EdgeName-Index löschen
		this.idxMgr.getIndex(IDX_EDGENAME).removeAll();
		//alle Kanten aus OUTEges-Index löschen
		this.idxMgr.getIndex(IDX_OUTEDGES).removeAll();
		//alle Kanten aus INEdges-Index löschen
		this.idxMgr.getIndex(IDX_INEDGES).removeAll();
		//idx-old: this.initEdges();
	}
	
	/**
	 * Gibt alle Knoten zurück, zu denen der übergebene Knoten ausgehende Kanten hat.
	 * @param node Node - der Knoten, ven dem die Knoten ausgehen sollen
	 * @return Knoten zu denen Kanten vom übergebenen Knoten gehen
	 */
	//old
	/*
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
	*/

	/**
	 * Gibt alle Knoten zurück, die eine Kante zu dem übergebenen Knoten besitzen.
	 * @param node Node - der Knoten, ven dem die Knoten ausgehen sollen
	 * @return Knoten zu denen Kanten vom übergebenen Knoten gehen
	 */
	//old
	/*
	public Vector<Node> getInEdges(Node node) throws Exception
	{
		if (node == null) throw new Exception(ERR_NODE_NAME_FAILURE);
		Vector<Node> retVec= null;
		
		Vector<String> inNames= this.inEdges.get(node.getName());
		//wenn Knoten ausgehende Kanten hat
		if (inNames != null)
		{
			retVec= new Vector<Node>(); 
			//durch alle ausgehenden Knoten gehen
			for (String inName: inNames)
			{
				retVec.add(this.getNode(inName));
			}
		}
		
		return(retVec);
	}
	*/
	
	/**
	 * Gibt die Anzahl der Knoten im Graph zurück.
	 * @return Anzahl der Knoten im Graph
	 */
	public long getNumOfNodes() throws Exception
	{ 
		this.changeOperation(false); //gibt an ob Funktion die Graph-Struktur ändert
		return(this.idxMgr.getIndex(IDX_NODENAME).getNumOfEntries()); 
	}
	
	/**
	 * Gibt die Anzahl der Knoten im Graph zurück.
	 * @return Anzahl der Knoten im Graph
	 */
	public int getNumOfEdges()
	{
		this.changeOperation(false); //gibt an ob Funktion die Graph-Struktur ändert
		return(this.edges.size());
	}
	
	/**
	 * Gibt die Wurzel zurück, wenn Graph ein Baum ist. Null sonst
	 * @return Node wenn Graph ein Baum ist, null sonst
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
	public Node getRoot() throws Exception
	{
		this.changeOperation(false); //gibt an ob Funktion die Graph-Struktur ändert
		if (this.root == null) this.isTree();
		return(root);
	}*/
	
	/**
	 * Gibt die "Wurzeln" eines DAG zurück. wenn es sich bei diesem Graphen um einen DAG
	 * handelt. 
	 * @return "Wurzeln" des DAG, wenn Graph ein DAG sonst null
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
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
	}*/
	
	/**
	 * Schreibt diesen Graphen in eine Dot-Datei. Wenn die Datei bereits existiert, wird sie
	 * überschrieben.
	 * @param fileName -String Name der DOT Datei
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
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
	//TODO: muss geändert werden, zwar kann der Name IDX_INEDGES mit IDX_OUTEDGES getauscht werden, aber alle fromKnoten der Kanten müssen mit allen toKnoten der Kanten vertauscht werden!!!
	//old
	/*
	public void invertEdges()
	{
		if (this.isDirected)
		{
			Map<String, Vector<Edge>> tempEdges= this.inEdges;
			this.inEdges= this.outEdges;
			this.outEdges= tempEdges;
		}
	}*/
	
	/**
	 * Gibt zurück, ob es sich bei diesem Graphen um einen Baum handelt. Ein Baum hat 
	 * eindeutige Wurzel, und es existiert genau ein Pfad von Knoten u zu Knoten v.
	 * @return true, wenn Graph ein Baum ist
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
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
				
				Vector<String> nodesToVisit = new Vector<String>();
				for (Edge edge: this.getOutEdges(posRoot))
					nodesToVisit.add(edge.getToNode().getName());
				isTree= this.isTreeRek(nodesToVisit);
				
				//old
				//isTree= this.isTreeRek(this.cloneEdgeVector(this.outEdges.get(posRoot.getName())));
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
	*/
	/**
	 * Prüft ob dieser Graph ein directed acyclic graph (DAG)ist. Gibt true zurück wenn Graph ein
	 * DAG ist sonst false. G ist ein DAG, wenn G gerichtet und azyklisch ist. Basiert auf der Annahme, dass:<br/>
	 * Sei G= (V,E) der Graph. Für alle v aus V: es gibt ein u aus V: (u,v) aus E => G besitzt einen zyklus<br/>
	 * Das heißt, wenn es nur Knoten mit eingehenden Kanten gibt ist G kein DAG.
	 * @return true wenn Graph ein DAG ist sonst false
	 * @throws Exception
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
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
	}*/
	
	/**
	 * Gibt zurück, ob dieser Graph einen Zyklus besitzt 
	 * @return true, wenn Graph Zyklus hat, false sonst
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
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
	}*/
	
	/**
	 * Berechnet die Pre- und Post-Order für jeden Knoten. Dies funktioniert nur, wenn der Graph ein 
	 * gerichteter Baum ist, oder der Graph ein DAG mit einem Knoten ohne ausgehende Kanten
	 * ist. Dabei wird jedem Knoten der Wert Graph::pre und Graph::post gegeben.
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
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
	}*/
	
	/**
	 * Gibt zurück ob der Graph zusammenhängend ist.
	 * @return true wenn Graph zusammenhängend ist, false sonst
	 */
	//TODO muss auf TraversalObjekt und an Kantenannotation angepasst werden
	/*
	 * 
	public boolean isCoherent()
	{
		this.changeOperation(false);	//gibt an ob Funktion die Graph-Struktur ändert
		
		boolean isCoherent= false;
		
		
			stdOut.println(MSG_TODO);
	
		return(isCoherent);
	}*/
	
// -------------------------- Indexverwaltung --------------------------
	
	/**
	 * Fügt dem internen Index-Manager einen benutzerdefinierten Index hinzu.
	 * Der Index-Manager kümmert sich automatisch um das updaten des Index,
	 * wenn ein Knoten oder eine Kante gelöscht wird und diese im 
	 * benutzerdefinierten Index referenziert werden.
	 * @param index Index - der Index, der von dem internen Index-Manager verwaltet werden soll 
	 */
	public void addIndex(Index index) throws Exception
	{
		this.idxMgr.addIndex(index);
	}
	
	/**
	 * Entfernt einen benutzerdefinierten Index aus dem internen Index-Manager.
	 * @param idxName String - Name des zu entfernen Indexes
	 * @throws Exception
	 */
	public void removeIndex(String idxName) throws Exception
	{
		this.idxMgr.removeIndex(idxName);
	}
	
	/**
	 * Gibt zurück, ob ein  benutzerdefinierten Index in diesem Graphen existiert.
	 * Dieser wird über den übergebenen Namen identifiziert.
	 * @param idxName String - Name des zu entfernen Indexes
	 * @return true, wenn ein Index mit dem übergebenen Namen existiert
	 * @throws Exception
	 */
	public boolean hasIndex(String idxName) throws Exception
	{
		return(this.idxMgr.hasIndex(idxName));
	}
	
	/**
	 * Gibt einen benutzerdefuinierten Index zurück. Dieser wird über den 
	 * übergebenen Namen identifiziert.
	 * @param idxName String - Name des zu entfernen Indexes
	 * @throws Exception
	 */
	public Index getIndex(String idxName) throws Exception
	{
		return(this.idxMgr.getIndex(idxName));
	}
	
// -------------------------- Ende Indexverwaltung --------------------------
}
