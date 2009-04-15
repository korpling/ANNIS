package internalCorpusModel;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.CorpDN;

import util.graph.Edge;

public abstract class ICMGraphMgr 
{
	/**
	 * Stack mit dem aktueller Korpusknoten in der Korpusebene
	 */
	protected Stack<ICMCorpDN> currKorpDN= null;
	
	/**
	 * Wurzelkorpusknoten
	 */
	public ICMCorpDN rootKorpDN= null;
	
	/**
	 * Stack mit dem aktueller Dokumentknoten in der Dokumentenebene
	 */
	protected Stack<ICMDocDN> currDocDN= null;
	
	/**
	 * Stack mit dem aktueller Collectionknoten in der Collectionenebene
	 */
	protected Stack<ICMCollectionDN> currColDN= null;
	
	/**
	 * Der interne Graph zum Speichern der Knoten
	 */
	protected ICMGraph korpGraph= null;
	
	/**
	 * Logger für log4j
	 */
	protected Logger logger= null;
	// Indizes
 
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMGraph";		//Name dieses Tools
	private static final boolean DEBUG=		false;			//DEBUG-Schalter
	//Indizes auf die Knoten
	Hashtable<String, Vector<ICMAbstractDN>> nodeType_idx= null;				//ein Index über den Typ der Knoten (korpDN, docDN, etc.)
	
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START_FCT=		MSG_STD + "start of method ";
	private static final String MSG_END_FCT=		MSG_STD + "end of method ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_NODE=			MSG_ERR + "The given node for the following methode is empty: ";
	private static final String ERR_NODE_NOT_EXIST=		MSG_ERR + "The given node can not be inserted, because of a needed node does not exists. ";
	private static final String ERR_EMPTY_NODETYPE=		MSG_ERR + "The given node type is empty. A list of nodes to the given type cannot be found.";
	private static final String ERR_EMPTY_NODELIST=		MSG_ERR + "A list of nodes to the given type cannot be found: ";
	private static final String ERR_NO_REF_NODES=		MSG_ERR + "Cannot insert an IKMStructDN - object, because there are no reference nodes given.";
	private static final String ERR_NO_EDGES=			MSG_ERR + "Cannot insert an IKMStructDN - object, because there are no edges given.";
	private static final String ERR_TWO_DOCDN=			MSG_ERR + "Cannot insert an IKMStructDN - object, because the reference nodes refreneces two different IKMDocDN object.";
	private static final String ERR_EMPTY_TYPENAME=		MSG_ERR + "The given type name is empty.";
	private static final String ERR_EMPTY_FROM=			MSG_ERR + "The given node name from is empty.";
	private static final String ERR_EMPTY_TO=			MSG_ERR + "The given node name to is empty.";
	private static final String ERR_NO_SLOT_2_TYPE=		MSG_ERR + "There is no slot found in type-index for the given type:";
	private static final String ERR_NO_NODES_IN_SLOT=	MSG_ERR + "There are no nodes in slot of type-index: ";
	private static final String ERR_EMPTY_CNODE=		MSG_ERR + "The given contextnode is empty. Error in method: ";
	private static final String ERR_NO_CNODE=			MSG_ERR + "Cannot insert an edge from cNode to colDN, because the given cNode is empty.";
	private static final String ERR_NO_COLDN=			MSG_ERR + "Cannot insert an edge from cNode to colDN, because the given colDN is empty.";
	private static final String ERR_WRONG_EDGE_SRC=		MSG_ERR + "Cannot insert the given IKMStructDN, because one of its edges has a source wich is not the given IKMSTructDN-objeckt.";
	private static final String ERR_NO_ROOTCORP=		MSG_ERR + "Cannot return the root corpus, because the stack is not initialized.";
//	 ============================================== statische Methoden ==============================================
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initailaisert dieses IKMGraph-Objekt.
	 */
	public ICMGraphMgr(Logger logger) throws Exception
	{
		this.logger= logger;
		this.init();
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Initailaisert dieses IKMGRaph-Objekt. Dabei werden alle Objekte initialisiert.
	 */
	private void init() throws Exception
	{
		//internen Graph initialisieren (gerichtet, hat Ordnung)
		this.korpGraph= new ICMGraph(this.logger);
		//aktuellen Korpusknotenstack initialisieren
		this.currKorpDN= new Stack<ICMCorpDN>();
		//aktuellen Dokumentknotenstack initialisieren
		this.currDocDN= new Stack<ICMDocDN>();
		//aktuellen Dokumentknotenstack initialisieren
		this.currColDN= new Stack<ICMCollectionDN>();
		//Knotentyp-Index initialisieren
		this.nodeType_idx= new Hashtable<String, Vector<ICMAbstractDN>>();
	}
	
	/**
	 * Fügt einen neuen Knoten vom Typ IKMAbstractDN in den internen Graphen ein.
	 * @param newDN IKMAbstractDN - neu einzufügender Datenknoten
	 */
	private void addDN(ICMAbstractDN newDN) throws Exception
	{
		if (DEBUG) System.out.println(MSG_STD + "insert: "+newDN.getName());
		if (newDN == null) throw new Exception(ERR_EMPTY_NODE + "'newDN' in addDN()");
		this.korpGraph.addNode(newDN);
	}
	
	/**
	 * Erzeugt eine Kante zwischen dem Knoten src und dem Knoten dst in dem internen Graph-Objekt.
	 * @param src IKMAbstractDN - Knoten von dem die Kante ausgeht
	 * @param dst IKMAbstractDN - Knoten zu dem die Kante führt
	 */
	private void createEdge(ICMAbstractDN src,  ICMAbstractDN dst) throws Exception
	{
		if (src == null) throw new Exception(ERR_EMPTY_NODE + "'src' in createEdge()");
		if (dst == null) throw new Exception(ERR_EMPTY_NODE + "'dst' in createEdge()");
		this.korpGraph.createEdge(src, dst);
	}
	
	/**
	 * Fügt einen übergebnen Knoten in den Knotentypindex ein. Der Typindex bildet sich
	 * durch den übergebenen Knotentyp.
	 * @param node IKMAbstractDN - in den Index einzufügender Knoten
	 * @param nodeType String - Typ des Knotens, nach dem indizioert werden soll  
	 */
	private void setToTypeIdx(	ICMAbstractDN node,
								String nodeType) throws Exception
	{
		Vector<ICMAbstractDN> nodeSlot= null;
		nodeSlot= this.nodeType_idx.get(nodeType);
		//wenn es noch keinen Slot gibt
		if (nodeSlot == null) 
		{	
			nodeSlot= new Vector<ICMAbstractDN>();
			this.nodeType_idx.put(nodeType, nodeSlot);
		}
		nodeSlot.add(node);
	}
	
	/**
	 * Gibt eine Liste von Knoten zurück, die in einem Slot im Knotentypindex stehen.
	 * Der Knotentypindex bezieht sich auf den Typ eines Knotens nach dem internen 
	 * Korpus Modell.
	 * @param nodeType String - Typ zu dem alle Knoten zurückgegeben werden sollen
	 * @return Liste von Knoten, die zu einem bestimmten Typ gehören
	 */
	protected Vector<ICMAbstractDN> getFromTypeIdx(String nodeType) throws Exception
	{
		if ((nodeType== null) || (nodeType.equalsIgnoreCase("")))
			throw new Exception(ERR_EMPTY_NODETYPE);
		Vector<ICMAbstractDN> nodeList= this.nodeType_idx.get(nodeType);
		if (nodeList== null) throw new Exception(ERR_EMPTY_NODELIST+ nodeType);
		return(nodeList);
	}
//	 ============================================== öffentliche Methoden ==============================================
// ----------------------------- alle Kanten -----------------------------
	/**
	 * Gibt alle Kanten in diesem Graphen als Kantenobjekte zurück. 
	 * @return alle Kanten dieses Graphen, null wenn Graph keine Kanten enthält
	 */
	public Collection<ICMAbstractEdge> getEdges() throws Exception
	{ 
		Collection<Edge> edges= this.korpGraph.getEdges();
		if ((edges == null) || (edges.isEmpty())) return(null);
		Collection<ICMAbstractEdge> absEdges= new Vector<ICMAbstractEdge>();
		
		for (Edge edge: edges)
		{
			try
			{
				absEdges.add((ICMAbstractEdge) edge);
			}
			catch (Exception e)
			{  }
		}
		
		return(absEdges); 
	}
	
	/**
	 * Gibt eine Kante zurück, die den übergebenen Namen trägt.
	 * @param edgeName String - Name der gesuchten Kante
	 * @return Kante zu dem gesuchten Namen bzw. null, wenn keine Kante mit diesem Namen vorhanden
	 */
	public ICMAbstractEdge getEdge(String edgeName) throws Exception
		{	return((ICMAbstractEdge) this.korpGraph.getEdge(edgeName)); }
	
	/**
	 * Gibt eine Kante zurück, die den übergebenen Namen trägt.
	 * @param fromNode String - Name des Knoten von dem die gesuchte Kante ausgeht
	 * @param toNode String - Name des Knoten zu dem die gesuchte Kante führt
	 * @return Kante zu dem gesuchten Namen bzw. null, wenn keine Kante mit diesem Namen vorhanden
	 */
	public ICMAbstractEdge getEdge(String fromNode, String toNode) throws Exception
	{	return((ICMAbstractEdge) this.korpGraph.getEdge(fromNode, toNode)); }
// ----------------------------- alle Kanten -----------------------------
// ----------------------------- alle Knoten ----------------------------- 
	/**
	 * Gibt einen Knoten zurück, der den übergebenen Namen trägt.
	 * @param nodeName String - Name des gesuchten Knoten
	 * @return Knoten zu dem gesuchten Namen bzw. null, wenn kein Knoten mit diesem Namen vorhanden
	 */
	public ICMAbstractDN getDN(String nodeName) throws Exception
		{ return((ICMAbstractDN)this.korpGraph.getNode(nodeName)); }
	
	/**
	 * Gibt den Knotentyp eines Knotens zurück, sofern dieser ermittelt werden kann.
	 * @param node IKMAbstractDN - Knoten dessen Typ ermittelt werden soll
	 * @return Knotentyp des übergebenen Knotens als String, wenn dieser nicht ermittelt werden kann, wird null zurückgegeben
	 */
	public String getDNType(ICMAbstractDN node) throws Exception
	{
		String retType= null;
		ICMAbstractDN tmpNode;
		//prüfe auf IKMKOrpDN
		try{ 
			tmpNode= (ICMCorpDN) node; 
			retType= ICMCorpDN.getDNLevel();
		} 
		catch (ClassCastException e) {}
		//prüfe auf IKMDocDN
		try{ 
			tmpNode= (ICMDocDN) node; 
			retType= ICMDocDN.getDNLevel();
		} 
		catch (ClassCastException e) {}
		//prüfe auf IKMPrimDN
		try{ 
			tmpNode= (ICMPrimDN) node; 
			retType= ICMPrimDN.getDNLevel();
		} 
		catch (ClassCastException e) {}
		//prüfe auf IKMTokDN
		try{ 
			tmpNode= (ICMTokDN) node; 
			retType= ICMTokDN.getDNLevel();
		} 
		catch (ClassCastException e) {}
		//prüfe auf IKMStructDN
		try{ 
			tmpNode= (ICMStructDN) node; 
			retType= ICMStructDN.getDNLevel();
		} 
		catch (ClassCastException e) {}
		//prüfe auf IKMAnnoDN
		try{ 
			tmpNode= (ICMAnnoDN) node; 
			retType= ICMAnnoDN.getDNLevel();
		} 
		catch (ClassCastException e) {}
		return(retType);
	}
	
	/**
	 * Sucht einen Knotenbereich, bei dem alle Knoten den gleichen Typ haben wie
	 * der übergebene Typ-Name. Es werden alle Knoten in dem entsprechenden Slot des
	 * übergebenen Typen zwischen dem from und dem toKnoten (beide inklusive) gesucht
	 * und zurückgegeben.
	 * @param typeName String -Name des Knotentyps
	 * @param fromName String - Name des Knotens, bei dem die Suche beginnt
	 * @param toName String - Name des Knotens, bei dem die Suche endet
	 * @return Liste der Knoten, die im angegebenen Bereich liegen
	 */
	public Vector<ICMAbstractDN> getDNRangeByType(	String typeName,
													String fromName,
													String toName) throws Exception
	{
		//wenn Dateiname nicht korrekt
		if ((typeName == null) || (typeName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_TYPENAME);
		//wenn from nicht korrekt
		if ((fromName == null) || (fromName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_FROM);
		//wenn to nicht korrekt
		if ((toName == null) || (toName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_TO);
		Vector<ICMAbstractDN> nodeSlot= this.getFromTypeIdx(typeName);
		if (nodeSlot== null) throw new Exception(ERR_NO_SLOT_2_TYPE + typeName);
		if (nodeSlot.isEmpty()) throw new Exception(ERR_NO_NODES_IN_SLOT + typeName);
		
		Vector<ICMAbstractDN> retNodes= new Vector<ICMAbstractDN>();
		boolean foundFrom= false;	//gibt an, wenn from gefunden wurde
		for (ICMAbstractDN currNode: nodeSlot)
		{
			//ersten Knoten gefunden
			if (currNode.getName().equalsIgnoreCase(fromName)) foundFrom= true;
			//Knoten in returnListe eintragen, solange from Knoten nicht gefunden
			if (foundFrom) retNodes.add(currNode);
			//letrzten Knoten gefunden
			if (currNode.getName().equalsIgnoreCase(toName)) break;
		}
		return(retNodes);
	}
// ----------------------------- Ende alle Knoten -----------------------------
// ----------------------------- Start IKMKorpDN -----------------------------
	/**
	 * Übernimmt den gegebenen Knoten als neuen und aktuellen Korpusknoten. Es wird ein
	 * Eintrag auf der Korpusebene erzeugt. Wenn korpDN2 nicht leer ist, wird korpDN1 korpDN2 
	 * unterstellt.
	 * @param korpDN1 IKMKorpusDN - Der in die Korpusebene einzufügende Korpusknoten
	 * @param korpDN2 IKMKorpusDN - Korpusknoten dem der übergebene neue Korpusknoten unterstellt werden soll
	 */
	public void addCorpDN(ICMCorpDN korpDN1, ICMCorpDN korpDN2) throws Exception
	{
		if (korpDN1 == null) throw new Exception(ERR_EMPTY_NODE + "'korpDN1' in addKorpDN()");
		//neuen Knoten in Graph einfügen
		this.addDN(korpDN1);
		//Korpusknoten auf Stack der aktuellen Korpusknoten legen
		this.currKorpDN.push(korpDN1);
		//ersten Korpus als Wurzelkorpus eintragen
		if (this.rootKorpDN== null) this.rootKorpDN= korpDN1;
		//wenn korpDN2 leer ist
		if (korpDN2!=null)
		{
			//erzeuge Kante von korpDN1 zu korpDN2
			this.createEdge(korpDN2, korpDN1);
		}	
		//Knoten in den Knotentypindex eintragen
		this.setToTypeIdx(korpDN1, ICMCorpDN.getDNLevel());
	}
	/**
	 * Übernimmt den gegebenen Knoten als neuen und aktuellen Korpusknoten. Es wird ein
	 * Eintrag auf der Korpusebene erzeugt. Der neue Korpusknoten wird dem aktuellen
	 * Korpusknoten unterstellt, sofern einer existiert.
	 * @param korpDN IKMKorpusDN - Der in die Korpusebene einzufügende Korpusknoten
	 */
	public void addCorpDN(ICMCorpDN korpDN) throws Exception
	{
		//wenn noch kein Korpusknoten existiert
		if (this.currKorpDN.isEmpty()) this.addCorpDN(korpDN, null);
		else this.addCorpDN(korpDN, this.currKorpDN.peek());
	}
	
	/**
	 * Setzt den aktuellen Korpus auf den Vorgänger zurück und liefert den bisher
	 * aktuellen Korpus Knoten zurück.
	 * @return den bisher aktuellen Korpusknoten
	 * @throws Exception
	 */
	public ICMCorpDN leaveCorpDN() throws Exception
		{ return(this.currKorpDN.pop()); }
	
	/**
	 * Gibt den aktuellen Korpusknoten der Korpusebene zurück. 
	 * Wenn noch kein Korpusknoten im Graphen erzeugt wurde, wird null zurückgegeben.
	 * @return aktueller Korpusknoten oder null
	 * @throws Exception
	 */
	public ICMCorpDN getCurrKorpDN() throws Exception
	{ 
		if (this.currKorpDN.empty())
			return(null);
		return(this.currKorpDN.peek()); 
	}
	
	/**
	 * Gibt den Korpusknoten zu einem übergebenen Knoten. Wenn kein IKMKorpDN zu dem
	 * übergebenen Knoten existiert wird null zurückgegeben. 
	 * @param node IKMAbstractDN - Knoten zu dem der IKMDocDN gesucht werden soll.
	 * @return IKMKorpDN-Objekt, dass zu dem übergebenen Knoten passt
	 * @throws Exception
	 */
	public ICMCorpDN getCorpDN(ICMAbstractDN node) throws Exception
	{
		ICMCorpDN korpDN= null;
		Vector<ICMAbstractDN> parentNodes= null;
		//Dokumentknoten zu dem Kontextknoten ermitteln
		ICMDocDN docDN= this.getDocDN(node);
		//wenn Kontextknoten keinen Dokumentknoten hat
		if (docDN== null)
			parentNodes= this.korpGraph.getDominanceParents(node);
		else
			parentNodes= this.korpGraph.getDominanceParents(docDN);
		if (parentNodes != null)
		{
			for (ICMAbstractDN pNode: parentNodes)
				try {korpDN= (ICMCorpDN)pNode;}
				catch (ClassCastException e) {}
		}
		return(korpDN);	
	}
	
	/**
	 * This method returns the root of all corpora (Super-corpus).
	 * @return the root corpus
	 * @throws Exception
	 */
	public ICMCorpDN getRootCorpDN() throws Exception
	{
		if (this.currKorpDN== null) 
			throw new Exception(ERR_NO_ROOTCORP);
		if (this.currKorpDN.empty()) 
			return(null);
		return(this.currKorpDN.firstElement());
	}
// ----------------------------- Ende IKMKorpDN -----------------------------
// ----------------------------- Start IKMCollectionDN -----------------------------
	/**
	 * Übernimmt den gegebenen Knoten als neuen und aktuellen Collectionknoten. Es wird ein
	 * Eintrag auf der Collectionenebene erzeugt. Der übergebene Collectionknoten wird 
	 * dem aktuellen Korpusknoten und dem aktuellen Collectionknoten angehangen.
	 * @param colDN IKMCollectionDN - Der in die Collectionebene einzufügende Collectionknoten
	 */
	public void addCollectionDN(ICMCollectionDN colDN) throws Exception
	{
		this.addCollectionDN(colDN, this.getCurrKorpDN());
	}
	
	/**
	 * Übernimmt den gegebenen Knoten als neuen und aktuellen Collectionknoten. Es wird ein
	 * Eintrag auf der Collectionenebene erzeugt. Der übergebene Collectionknoten wird 
	 * dem übergebenen Korpusknoten und dem aktuellen Collectionknoten angehangen.
	 * @param colDN IKMCollectionDN - Der in die Collectionebene einzufügende Collectionknoten
	 * @param korpDN IKMKorpDN - Der Korpusknoten, dem der übergebne Collectionknoten unterstellt ist
	 */
	public void addCollectionDN(	ICMCollectionDN colDN, 
									ICMCorpDN korpDN) throws Exception
	{
		this.addCollectionDN(colDN, this.getCurrColDN(), korpDN);
	}
	
	/**
	 * Übernimmt den gegebenen Knoten als neuen und aktuellen Collectionknoten. Es wird ein
	 * Eintrag auf der Collectionenebene erzeugt. Der übergebene Collectionknoten wird 
	 * dem übergebenen Korpusknoten und dem übergebenen Collectionknoten angehangen, wenn col2 != null.
	 * @param colDN1 IKMCollectionDN - Der in die Collectionebene einzufügende Collectionknoten
	 * @param colDN2 IKMCollectionDN - Der Collectionknoten dem der neue Collectionknoten unterstellt werden soll
	 * @param korpDN IKMKorpDN - Der Korpusknoten, dem der übergebne Collectionknoten unterstellt ist
	 */
	public void addCollectionDN(	ICMCollectionDN colDN1, 
									ICMCollectionDN colDN2,
									ICMCorpDN korpDN) throws Exception
	{
		if (colDN1 == null) throw new Exception(ERR_EMPTY_NODE + "'colNode1' in addCollectionDN()");
		if (korpDN == null) throw new Exception(ERR_EMPTY_NODE + "'korpNode' in addCollectionDN()");
		//neuen Knoten in Graph einfügen
		this.addDN(colDN1);
		//Collectionknoten auf Stack der aktuellen Dokumentknoten legen
		this.currColDN.push(colDN1);
		//Kante zwischen Korpusknoten und Documentknoten bilden
		this.createEdge(korpDN, colDN1);
		//Kante zwischen Collectionknoten colDN1 und Collectionknoten colDN2 ziehen, wenn colDN2 vorhanden
		if (colDN2 != null) this.createEdge(colDN1, colDN2);
		//Dokumentknoten in den Knotentypindex eintragen
		this.setToTypeIdx(colDN1, ICMCollectionDN.getDNLevel());
	}	
	
	/**
	 * Gibt den aktuellen Collectionknoten der Collectionebene zurück. 
	 * Wenn noch kein Collectionknoten im Graphen erzeugt wurde, wird null zurückgegeben.
	 * @return aktueller Collectionknoten oder null
	 * @throws Exception
	 */
	public ICMCollectionDN getCurrColDN() throws Exception
	{ 
		if (this.currColDN.empty())
			return(null);
		return(this.currColDN.peek()); 
	}
	
	/**
	 * Hängt den übergebenen Kontextknoten vom Typ IKMAbstractDN an einen Knoten vom Typ
	 * IKMCollectionDN an. 
	 * @param cNode IKMAbstractDN - Kontextknoten der an den Collection-Lnoten gehangen werden soll
	 * @param colDN IKMCollectionDN - Collectionknoten an den der Kontextknoten gehangen werden soll
	 * @throws Exception Fehler, wenn einer der Knoten leer ist
	 */
	public void setDNToColDN(	ICMAbstractDN cNode, 
								ICMCollectionDN colDN) throws Exception
	{
		if (cNode== null) throw new Exception(ERR_NO_CNODE);
		if (colDN== null) throw new Exception(ERR_NO_COLDN);
		//Kante vom Collection-Knoten zum Kontextknoten ziehen
		this.createEdge(colDN, cNode);
	}
// ----------------------------- Ende IKMCollectionDN -----------------------------
// ----------------------------- Start IKMColAnnoDN -----------------------------
	/**
	 * Übernimmt den gegebenen Knoten als neue Annotation für den übergebenen Collection-knoten.
	 * Es wird ein Eintrag auf der Collection-Annotations-enebene erzeugt. Der übergebene
	 * Collection-Annotations-Knoten wird dem übergebenen Korpus-Knoten unterstellt.
	 * @param colAnnoDN IKMCollectionDN - Der in die Collectionebene einzufügende Collectionknoten
	 * @param colDN IKMCollectionDN - Der Collectionknoten dem der neue Collectionknoten unterstellt werden soll
	 * @param korpDN IKMKorpDN - Der Korpusknoten, dem der übergebne Collectionknoten unterstellt ist
	 */
	public void addColAnnoDN(	ICMColAnnoDN colAnnoDN, 
								ICMCollectionDN colDN,
								ICMCorpDN korpDN) throws Exception
	{
		if (colAnnoDN == null) throw new Exception(ERR_EMPTY_NODE + "'colAbboNode' in addCollectionDN()");
		if (colDN == null) throw new Exception(ERR_EMPTY_NODE + "'colNode' in addCollectionDN()");
		if (korpDN == null) throw new Exception(ERR_EMPTY_NODE + "'korpNode' in addCollectionDN()");
		//neuen Knoten in Graph einfügen
		this.addDN(colAnnoDN);
		//Kante zwischen Korpusknoten und Collection-Annotations-Knoten bilden
		this.createEdge(korpDN, colAnnoDN);
		//Kante zwischen Collection-Annotations-Knoten und Collection-Knoten bilden
		this.createEdge(colAnnoDN, colDN);
		//Dokumentknoten in den Knotentypindex eintragen
		this.setToTypeIdx(colAnnoDN, ICMColAnnoDN.getDNLevel());
	}
// ----------------------------- Ende IKMColAnnoDN -----------------------------
// ----------------------------- Start IKMDocDN -----------------------------
	/**
	 * Übernimmt den gegebenen Knoten als neuen und aktuellen Dokumentenknoten. Es wird ein
	 * Eintrag auf der Dokumentenebene erzeugt. Der übergebene Dokumentknoten wird 
	 * dem übergebenen Korpusknoten angehangen.
	 * @param docDN IKMDocDN - Der in die Dokumentenebene einzufügende Dokumentknoten
	 * @param korpDN IKMKorpDN - Der Korpusknoten, dem der übergebne Dokumentknoten unterstellt ist
	 */
	public void addDocDN(ICMDocDN docDN, ICMCorpDN korpDN) throws Exception
	{
		if (docDN == null) throw new Exception(ERR_EMPTY_NODE + "'docNode' in addDocDN()");
		if (korpDN == null) throw new Exception(ERR_EMPTY_NODE + "'korpNode' in addDocDN()");
		//neuen Knoten in Graph einfügen
		this.addDN(docDN);
		//Dokumentknoten auf Stack der aktuellen Dokumentknoten legen
		this.currDocDN.push(docDN);
		//Kante zwischen Korpusknoten und Documentknoten bilden
		this.createEdge(korpDN, docDN);
		//Dokumentknoten in den Knotentypindex eintragen
		this.setToTypeIdx(docDN, ICMDocDN.getDNLevel());
	}
	
	/**
	 * Übernimmt den gegebenen Knoten als neuen und aktuellen Dokumentenknoten. Es wird ein
	 * Eintrag auf der Dokumentenebene erzeugt. Der übergebene Knoten wird dem aktuellen
	 * Korpusknoten angehangen.
	 * @param docDN IKMDocDN - Der in die Dokumentenebene einzufügende Dokumentknoten
	 */
	public void addDocDN(ICMDocDN docDN) throws Exception
	{
		this.addDocDN(docDN, this.currKorpDN.peek());
	}
	
	/**
	 * Gibt den aktuellen Dokumentknoten der Dokumentebene zurück. 
	 * Wenn noch kein Dokumentknoten im Graphen erzeugt wurde, wird null zurückgegeben.
	 * @return aktueller Dokumentknoten oder null
	 * @throws Exception
	 */
	public ICMDocDN getCurrDocDN() throws Exception
	{ 
		if (this.currDocDN.empty())
			return(null);
		return(this.currDocDN.peek()); 
	}
	
	/**
	 * Gibt den Dokumentknoten zu einem übergebenen Knoten. Wenn kein IKMDoDN zu dem
	 * übergebenen Knoten existiert wird null zurückgegeben. Achtung es gibt im internen
	 * KOrpusmodell nicht zu jedem Knoten einen eindeutigen IKMDocDN (bspw. zu einem
	 * IKMKorpDN). 
	 * @param node IKMAbstractDN - Knoten zu dem der IKMDocDN gesucht werden soll.
	 * @return IKMDocDN-Objekt, dass zu dem übergebenen Knoten passt
	 * @throws Exception
	 */
	public ICMDocDN getDocDN(ICMAbstractDN node) throws Exception
	{
		Vector<ICMAbstractDN> parentNodes= this.korpGraph.getDominanceParents(node);
		ICMDocDN docDN= null;
		if (parentNodes != null)
		{
			for (ICMAbstractDN pNode: parentNodes)
				try {docDN= (ICMDocDN)pNode;}
				catch (ClassCastException e) {}
		}		
		return(docDN);
	}
// ----------------------------- Ende IKMDocDN -----------------------------
// ----------------------------- Start IKMPrimDN -----------------------------
	/**
	 * Fügt einen IKMPrimDN (Primärdatenknoten) in den Graphen ein. Dieser wird
	 * dem aktuellen Dokumentknoten angehangen, sofern einer existiert.
	 * @param primDN IKMPrimDN - Primärdatenknoten, der in den Graph eingefügt werden soll
	 * @exception Fehler, wenn kein aktueller Dokumentknoten existiert
	 */
	public void addPrimDN(ICMPrimDN primDN) throws Exception
	{
		if (primDN == null) throw new Exception(ERR_EMPTY_NODE + "'primNode' in addPrimDN()");
		//Prüfen ob es ein Dokumentknoten gibt, dem der Primärknoten angehängt werden kann
		if ((this.currDocDN== null) ||(this.currDocDN.isEmpty()))
			throw new Exception(ERR_NODE_NOT_EXIST + "Given node: "+ primDN.getName() +"\tneeded node: IKMDocDN");
		//neuen Knoten in Graph einfügen
		this.addDN(primDN);
		//Kante zwischen PrimKnoten und Documentknoten bilden
		this.createEdge(this.currDocDN.peek(), primDN);
		//Primärdatenknoten in den Knotentypindex eintragen
		this.setToTypeIdx(primDN, ICMPrimDN.getDNLevel());
	}
// ----------------------------- Ende IKMPrimDN -----------------------------
// ----------------------------- Start IKMTokDN -----------------------------
	/**
	 * Fügt einen IKMTokDN (Tokendatenknoten) in den Graphen ein. Dieser wird
	 * dem aktuellen Dokumentknoten angehangen, sofern einer existiert.
	 * @param tokDN IKMTokDN - Tokendatenknten, der in den Graph eingefügt werden soll
	 * @param primDN IKMPrimDN  - Primärdatenknoten, auf den sich dieser Tokendatenknoten bezieht
	 * @exception Fehler, wenn kein aktueller Dokumentknoten existiert
	 */
	public void addTokDN(ICMTokDN tokDN, ICMPrimDN primDN) throws Exception
	{
		if (tokDN == null) throw new Exception(ERR_EMPTY_NODE + "'tokNode' in addTokDN()");
		//Prüfen ob es ein Dokumentknoten gibt, dem der Dokumentknoten angehängt werden kann
		if ((this.currDocDN== null) ||(this.currDocDN.isEmpty()))
			throw new Exception(ERR_NODE_NOT_EXIST + "Given node: "+ tokDN.getName() +"\tneeded node: IKMDocDN");
		//neuen Knoten in Graph einfügen
		this.addDN(tokDN);
		//Dokumentknoten zu Primärdatenknoten finden
		ICMDocDN docDN= this.getDocDN(primDN);
		//Kante zwischen tokKnoten und Documentknoten bilden
		this.createEdge(docDN, tokDN);
		//Tokenknoten in den Knotentypindex eintragen
		this.setToTypeIdx(tokDN, ICMTokDN.getDNLevel());
	}
	
	/**
	 * Ermittelt die Liste an IKMTokDN-Objekten (Tokenknoten) auf die der übergebene
	 * Kontextknoten verweist. Die Liste an Tokenknoten, auf die verweisen wird wird 
	 * zurückgegeben.
	 * @param cNode IKMAbstractDN - Kontextknoten, dessen Verweistokenknoten ermittelt werden
	 * @return Liste an Tokenknoten, auf die der Kontextknotenverweist
	 * @exception Fehler, wenn Kontextknoten leer oder nicht vom Typ IKMStructDN ist.
	 */
	public Vector<ICMTokDN> getIKMTokDNs(ICMAbstractDN cNode) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "getIKMTokDNs()");
		if (cNode== null)	throw new Exception(ERR_EMPTY_CNODE + "getIKMTokDNs()");
		Vector<ICMTokDN> retNodes= null;
		retNodes= this.getIKMTokDNsRec(cNode);
		if (DEBUG) System.out.println(MSG_END_FCT + "getIKMTokDNs()");
		return(retNodes);
	}
	
	/**
	 * Sucht rekursiv den Baum durch, bis die tokenebene (IKMTokDN) gefunden wird. Alle
	 * TokDN die gefunden werden werden zurückgegeben.
 	 * @param cNode IKMAbstractDN - Kontextknoten, für den alle Token-Verweisziele gefunden werden
	 * @return Liste an Tokenknoten, auf die der Kontextknotenverweist, null wenn Knoten leer ist oder keine Kinderknoten hat und kein Tokenknoten ist
	 * @throws Exception Fehler, wenn Knoten auf keine Tokenknoten verweist.
	 */
	protected Vector<ICMTokDN> getIKMTokDNsRec(ICMAbstractDN cNode) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "getIKMTokDNsRec()");
		Vector<ICMTokDN> retNodes= null;
		//wenn es einen Kontextknoten gibt
		if (cNode!= null)
		{
			retNodes= new Vector<ICMTokDN>();
			//Knoten ist ein Tokenknoten
			if (ICMTokDN.class.isInstance(cNode))
				retNodes.add((ICMTokDN)cNode);
			else 
			{
				Vector<ICMAbstractDN> childs=  this.korpGraph.getDominanceChilds(cNode);
				if ((childs!= null) ||(!childs.isEmpty()))
				{
					//alle Kinderknoten dieses Knotens durchgehen
					for (ICMAbstractDN child: childs)
					{
						Vector<ICMTokDN> tokNodes= this.getIKMTokDNsRec(child); 
						//Rückgabewert der Kindknoten in Rückgabeliste einfügen
						if ((tokNodes!= null)&&(!tokNodes.isEmpty())) 
							retNodes.addAll(tokNodes);

					}
				}
			}
		}	
		if (DEBUG) System.out.println(MSG_END_FCT + "getIKMTokDNsRec()");
		return(retNodes);
	}
// ----------------------------- Ende IKMTokDN -----------------------------
// ----------------------------- Start IKMStructDN -----------------------------
	/**
	 * Fügt einen IKMStructDN (Strukturdatenknoten) in den Graphen ein. Dieser wird
	 * dem aktuellen Dokumentknoten angehangen, sofern einer existiert.
	 * @param structDN IKMTokDN - Tokendatenknten, der in den Graph eingefügt werden soll
	 * @exception Fehler, wenn kein aktueller Dokumentknoten existiert
	 */
	public void addStructDN(	ICMStructDN structDN) throws Exception
	{
		if (structDN == null) throw new Exception(ERR_EMPTY_NODE + "'structNode' in addTokDN()");
		//Prüfen ob es ein Dokumentknoten gibt, dem der Dokumentknoten angehängt werden kann
		if ((this.currDocDN== null) ||(this.currDocDN.isEmpty()))
			throw new Exception(ERR_NODE_NOT_EXIST + "Given node: "+ structDN.getName() +"\tneeded node: IKMDocDN");
		//neuen Knoten in Graph einfügen
		this.addDN(structDN);
		ICMDocDN docDN= this.getCurrDocDN();//Dokumentknoten zu den Referenzknoten
		
		//Kante zwischen structKnoten und Documentknoten bilden
		this.createEdge(docDN, structDN);
		//Structknoten in den Knotentypindex eintragen
		this.setToTypeIdx(structDN, ICMStructDN.getDNLevel());
	}
	
	/**
	 * Fügt einen IKMStructDN (Strukturdatenknoten) in den Graphen ein. Dieser wird
	 * dem aktuellen Dokumentknoten angehangen, sofern einer existiert.
	 * @param structDN IKMTokDN - Tokendatenknten, der in den Graph eingefügt werden soll
	 * @param refDN Vector<IKMAbstractDN>  - Referenzknoten, auf die sich dieser STrukturknoten bezieht
	 * @exception Fehler, wenn kein aktueller Dokumentknoten existiert
	 */
	public void addStructDN(	ICMStructDN structDN, 
								Vector<ICMAbstractDN> refDN) throws Exception
	{
		if (structDN == null) throw new Exception(ERR_EMPTY_NODE + "'structNode' in addTokDN()");
		//Prüfen ob es ein Dokumentknoten gibt, dem der Dokumentknoten angehängt werden kann
		if ((this.currDocDN== null) ||(this.currDocDN.isEmpty()))
			throw new Exception(ERR_NODE_NOT_EXIST + "Given node: "+ structDN.getName() +"\tneeded node: IKMDocDN");
		if ((refDN== null) || (refDN.isEmpty())) throw new Exception(ERR_NO_REF_NODES);
		//neuen Knoten in Graph einfügen
		this.addDN(structDN);
		ICMDocDN docDN= null;//Dokumentknoten zu den Referenzknoten
		//Kanten zu den refernzierten Knoten erzeugen
		for (ICMAbstractDN refNode: refDN)
		{	
			this.createEdge(structDN, refNode);
			//Dokumentknoten zu refknoten finden
			if (docDN== null) docDN= this.getDocDN(refNode);
			else if (docDN != this.getDocDN(refNode)) throw new Exception(ERR_TWO_DOCDN);
		}
		//Kante zwischen structKnoten und Documentknoten bilden
		this.createEdge(docDN, structDN);
		//Structknoten in den Knotentypindex eintragen
		this.setToTypeIdx(structDN, ICMStructDN.getDNLevel());
	}
	
	/**
	 * Fügt einen IKMStructDN (Strukturdatenknoten) in den Graphen ein. Dieser wird
	 * dem aktuellen Dokumentknoten angehangen, sofern einer existiert. Außerdem
	 * werden alle übergebenen Kanten eingefügt.
	 * @param structDN IKMTokDN - Strukturdatenknoten, der in den Graph eingefügt werden soll
	 * @param edges Collection<Edge> - Kanten, die von dem gegebenen IKMStructDN ausgehen und zu dessen untergeordneten Knoten führen
	 * @exception Fehler, wenn kein aktueller Dokumentknoten existiert
	 */
	public void addStructDN2(	ICMStructDN structDN, 
								Collection<Edge> edges) throws Exception
	{
		if (structDN == null) throw new Exception(ERR_EMPTY_NODE + "'structNode' in addTokDN()");
		//Prüfen ob es ein Dokumentknoten gibt, dem der Dokumentknoten angehängt werden kann
		if ((this.currDocDN== null) ||(this.currDocDN.isEmpty()))
			throw new Exception(ERR_NODE_NOT_EXIST + "Given node: "+ structDN.getName() +"\tneeded node: IKMDocDN");
		if ((edges== null) || (edges.isEmpty())) throw new Exception(ERR_NO_EDGES);
		//neuen Knoten in Graph einfügen
		this.addDN(structDN);
		ICMDocDN docDN= null;//Dokumentknoten zu den Referenzknoten
		//Kanten zu den refernzierten Knoten erzeugen
		for (Edge edge: edges)
		{	
			//wenn Quelle der Kante nicht StructDN ist --> Fehler
			if (edge.getFromNode()!= structDN) throw new Exception(ERR_WRONG_EDGE_SRC);
			this.korpGraph.createEdge(edge);
			
			if (docDN== null) docDN= this.getDocDN((ICMAbstractDN)edge.getToNode());
			else if (docDN != this.getDocDN((ICMAbstractDN)edge.getToNode())) throw new Exception(ERR_TWO_DOCDN);
		}
		//Kante zwischen structKnoten und Documentknoten bilden
		this.createEdge(docDN, structDN);
		//Structknoten in den Knotentypindex eintragen
		this.setToTypeIdx(structDN, ICMStructDN.getDNLevel());
	}
// ----------------------------- Ende IKMStructDN -----------------------------
// ----------------------------- Start IKMAnnoDN -----------------------------
	/**
	 * Fügt einen IKMAnnoDN (Annotationsdatenknoten) in den Graphen ein. Dieser wird
	 * dem aktuellen Dokumentknoten angehangen, sofern einer existiert.
	 * @param annoDN IKMTokDN - Tokendatenknten, der in den Graph eingefügt werden soll
	 * @param refDN Vector<IKMAbstractDN>  - Referenzknoten, auf die sich dieser STrukturknoten bezieht
	 * @exception Fehler, wenn kein aktueller Dokumentknoten existiert
	 */
	public void addAnnoDN(	ICMAnnoDN annoDN, 
							Vector<ICMAbstractDN> refDN) throws Exception
	{
		if (annoDN == null) throw new Exception(ERR_EMPTY_NODE + "'annoNode' in addTokDN()");
		//Prüfen ob es ein Dokumentknoten gibt, dem der Dokumentknoten angehängt werden kann
		if ((this.currDocDN== null) ||(this.currDocDN.isEmpty()))
			throw new Exception(ERR_NODE_NOT_EXIST + "Given node: "+ annoDN.getName() +"\tneeded node: IKMDocDN");
		if ((refDN== null) || (refDN.isEmpty())) throw new Exception(ERR_NO_REF_NODES);
		
		//neuen Knoten in Graph einfügen
		this.addDN(annoDN);
		ICMDocDN docDN= null;//Dokumentknoten zu den Referenzknoten
		//Kanten zu den refernzierten Knoten erzeugen
		for (ICMAbstractDN refNode: refDN)
		{	
			this.createEdge(annoDN, refNode);
			//Dokumentknoten zu refknoten finden
			if (docDN== null) docDN= this.getDocDN(refNode);
			else if (docDN != this.getDocDN(refNode)) throw new Exception(ERR_TWO_DOCDN);
		}
		//Kante zwischen tokKnoten und Documentknoten bilden
		this.createEdge(docDN, annoDN);
		//Tokenknoten in den Knotentypindex eintragen
		this.setToTypeIdx(annoDN, ICMStructDN.getDNLevel());
	}
// ----------------------------- Ende IKMAnnoDN -----------------------------
	/**
	 * Schreibt den internen Graphen als Dot in die übergebene Datei.
	 * @param fileName
	 */
	public void printGraph(String fileName) throws Exception 
	{ this.korpGraph.prinToDot(fileName); }
	
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
//	 ============================================== main Methode ==============================================	


}
