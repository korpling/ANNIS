package util.depGraph;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import paulaAnalyzer.AnalyzeContainer;
import paulaAnalyzer.PAULAAnalyzer;

/**
 * Diese Klasse realisiert einen Abh�ngigkeitsgraphen f�r PAULA-Dateien. Diese sind in 
 * Analyze-Container eingefasst und k�nnen innerhalb dieses Graphen gerichtete Kanten
 * zueinander haben.
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class FileDepGraph extends Graph 
{
//	 ============================================== private Variablen ==============================================
	private static final boolean DEBUG= false;
	
	private static final String TOOLNAME= 	"FileDepGraph";		//Name dieses Tools
	private static final String KW_ACON= 	"aCon";
	
	private Logger logger= Logger.getLogger(FileDepGraph.class);		//Logger f�r log4j
	
	Hashtable<Double, Vector<Node>> orderIdx= null;	//Index auf die Paula-Datei Reihenfolge
	Vector<Node> nonOrderNodes= null;				//Liste die alle Knoten enth�lt, die keinen AnalyzeContainer haben
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_ACON=			MSG_ERR + "No analyze-container-object was given.";
	private static final String ERR_NON_ORDERED_NODES=	MSG_ERR + "Cannot serialize, there are some nodes in graph without AnalyzeContainer-object: ";
	private static final String ERR_CYCLE_FOUND=		MSG_ERR + "Cannot serialize, there might be a cycle in the dependency graph. ";
	private static final String ERR_WRONG_ORDER_REF=	MSG_ERR + "One PAULA-file references to a file wich is on a higher level (order-level). There might be a problem with the setting file, or an error in the paula data.";
//	 ============================================== Konstruktoren ==============================================
	public FileDepGraph()
	{
		//Aufruf des Superkonstruktors mit (gerichtet, nicht geordert, logger)
		super(true, false);
		
		this.init();
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Initialisiert dieses Objekt
	 */
	private void init()
	{
		this.orderIdx= new Hashtable<Double, Vector<Node>>();
		this.nonOrderNodes= new Vector<Node>();
	}
	
	/**
	 * F�gt den �bergebenen Knoten in den Reihenfolgenindex ein.
	 * @param node Node - in den Index einzuf�gender Knoten
	 */
	private void fillOrderIdx(Node node) throws Exception
	{
		//pr�fen ob es einen AnalyzeContainer gibt
		if (!node.hasValue(KW_ACON))
		{
			this.nonOrderNodes.add(node);
		}
		//es gibt einen ANalyzeContainer
		else
		{
			//ggf. Knoten aus Liste der Knoten, die keinen ANalyzeContainer haben l�schen
			if (this.nonOrderNodes.contains(node)) this.nonOrderNodes.remove(node);
			Double order= ((AnalyzeContainer)node.getValue(KW_ACON)).getOrder();
			Vector<Node> nodeVec= this.orderIdx.get(order); 
			//pr�fe ob es im Index einen Vector f�r diese Priorit�t gibt
			if (nodeVec == null)
			{
				//wenn nein, erzeuige Vector
				nodeVec= new Vector<Node>();
				nodeVec.add(node);
				this.orderIdx.put(order, nodeVec);
			}
			//f�ge Objekt in den Vector
			else nodeVec.add(node);
		}
	}
	
	/**
	* Sortiert einen Slot von Knoten in dem Reihenfolgeindex und gibt den sortierten Slot 
	* zur�ck.
	* Algorithmus:<br/>
	* 	erzeuge Liste slot, in der alle Knoten dieses Slots enthalten sind
	* 	i:=0
	*	solange slot != leer
	*		nimm item Knoten k aus Liste 
	*		wenn k keine ausgehenden Kanten zu Knoten aus slot hat, 
	*			dann f�ge an anfang von List ordered
	*			inserted= true
	*		wenn k ausgehende Kante zu Knoten aus Liste ordered slot hat
	*			pr�fe ob alle Zielknoten z bereits in ordered
	*			wenn ja
	*				f�ge Knoten k direkt hinter letztem (in der Liste letztem) z ein
	*			sonst gehe zu n�chstem Knoten k (i= i+1)
	*		wenn j== i, i also in der letzen Runde nicht erh�ht wurde --> Fehler, endlosschleife, die auf einen Zyklus hinweist 
	*		j:= i;
	* @param order Double - der Reihenfolgewert, dessen Slot sortiert werden soll
	* @return sortierter Slot
	*/
	private Vector<Node> sortSlot(Double order) throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_STD + "start method sortSlot()");
		if (DEBUG ) System.out.println(MSG_STD + "start method sortSlot()");
		Vector<Node> sortedSlot= new Vector<Node>();
		//erzeuge Liste slot, in der alle Knoten dieses Slots enthalten sind
		Vector<Node> orderSlot= this.orderIdx.get(order);
		
		if (DEBUG ) 
		{
			System.out.println("Sortiere diesen Slot: ");
			for (Node currNode: orderSlot)
				System.out.print(((AnalyzeContainer)currNode.getValue(KW_ACON)).getPAULAID() + ", ");
			System.out.println();
		}
		
		int nodesInSlot= 0;	//Anzahl der Knoten in orderSlot (f�r die Abbruchbedingung)
		//solange slot != leer
		while(!orderSlot.isEmpty())
		{
			
			nodesInSlot= orderSlot.size();
			//gehe durch alle Knoten aus orderSlot
			for (int i= 0; i < orderSlot.size(); i++)
			{
				if (DEBUG)
				{
					System.out.print("sortedSlot: ");
					if (sortedSlot != null)
					{
						for (Node currNode: sortedSlot)
						{
							System.out.print(((AnalyzeContainer)currNode.getValue(KW_ACON)).getPAULAID() + ", ");
						}
					}
					System.out.println();
				}
				
				Node currNode= orderSlot.get(i);
				if (DEBUG) System.out.println("check node: "+((AnalyzeContainer)currNode.getValue(KW_ACON)).getPAULAID());
				
				//Pr�fe ob currNode eine ausgehende Kanten im gleichen Slot hat
				Vector<Node> outNodes= this.getOutEdges(currNode); 
				boolean sameSlot= false;
				Vector<Node> sameOrderSlot= new Vector<Node>();
				if (outNodes != null)
				{
					for (Node outNode: outNodes)
					{	
						double outNodeOrder= ((AnalyzeContainer)outNode.getValue(KW_ACON)).getOrder();
						double currOrder= ((AnalyzeContainer)currNode.getValue(KW_ACON)).getOrder();
						//wenn outNode und currNode gleiche Reihenfolge
						if (currOrder == outNodeOrder)
						{
							sameOrderSlot.add(outNode);
							sameSlot= true;
						}
						// wenn aktuelle Reihenfolge kleiner ist als die des Knotens auf den verwiesen wird, und der aktuelle keine Metadatei enth�lt
						else if ((currOrder < outNodeOrder) 
								&& ((AnalyzeContainer)currNode.getValue(KW_ACON)).getAbsAnaType() != AnalyzeContainer.ABS_ANA_TYPE.META_DATA)
							//dann Fehler in Datei
						{
							throw new Exception(ERR_WRONG_ORDER_REF);
						}
					}
				}
					
				//wenn currNode keine ausgehenden Kanten zu Knoten aus gleichem slot hat,
				if (!sameSlot)
				{
					// dann f�ge an Anfang von List ordered
					sortedSlot.insertElementAt(currNode, 0);
					//l�sche Knoten aus Liste orederSlot
					orderSlot.remove(currNode);
					i--;
				}
				// wenn k ausgehende Kante zu Knoten aus Liste ordered slot hat
				else
				{
					boolean nodeInList= true;
					int pos= 0;	//Poistion, an der der letzte Knoten in der Liste steht
					//gehe durch alle Knoten zu denen eine Kante von currNode ausgeht und die im gleichen Slot sind
					for (Node outNode: sameOrderSlot)
					{	
						//pr�fe ob alle Zielknoten outNodes bereits in ordered
						if (!sortedSlot.contains(outNode))
						{
							nodeInList= false;
						}
						//wenn nicht ermittle letzten Knoten auf den eine Kante geht
						if (sortedSlot.indexOf(outNode) > pos) pos= sortedSlot.indexOf(outNode); 
					}
					//wenn Knoten schon in Liste, dann 
					if (nodeInList)
					{
						// f�ge Knoten currNode direkt hinter letztem (in der Liste letztem) outNode ein
						sortedSlot.insertElementAt(currNode, pos+1);
						//l�sche Knoten aus Liste orederSlot
						orderSlot.remove(currNode);	
						i--;
					}
				}
			}
			//wenn kein Knoten in einem Durchlauf hinzugef�gt wurde, gab es einen Fehler --> wahrscheinlich ein Zyklus 
			if (nodesInSlot== orderSlot.size()) throw new Exception(ERR_CYCLE_FOUND);
		}
		return(sortedSlot);
	}
	
	/**
	 * Erzeugt einen vertr�glichen Knotennamen aus dem Dateinamen, dabei 
	 * wird ein Backslash erstezt.
	 * @param paulaFile File - die Datei, auf die sich der Knoten beziehen wird
	 * @return ein vertr�glicher, eindeutiger Name der Datei
	 */
	private String createNodeName(File paulaFile) throws Exception
	{
		String retStr= null;
		
		String originName= paulaFile.getCanonicalPath();
		retStr= originName.replace("\\", "_");
		
		return(retStr);
	}
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * F�gt ggf. mehrere neue Knoten in den Graphen ein. Existiert bereits ein Knoten f�r
	 * den AnalyzeContainer, wird dieser nur um den Container vervollst�ndigt. Wenn nicht 
	 * wird der Knoten neu erzeugt. F�r alle Eintr�ge in depFiles wird ein neuer Knoten 
	 * erzeugt, sofern nicht bereits einer existiert. Au�erdem werden Kanten zwischen dem
	 * Knoten der aCon enth�lt und den Knoten der depFiles erzeugt. Ein Knoten wird immer mit
	 * kannonoschen Pathname + Dateinamen eingef�gt.
	 * @param aCon AnalyzeContainer - AnalyzeContainer f�r einen Knoten
	 * @param depFiles  Vector<String> - Knoten von denen aCOn abh�ngig ist 
	 */
	public void addNode(AnalyzeContainer aCon, Vector<String> depFiles) throws Exception
	{
		if (aCon== null) throw new Exception (ERR_NO_ACON);
		
		//Erzeugung und Bereinigung des Knotennamens
		String currNodeName= this.createNodeName(aCon.getPAULAFile());
		
		Node currNode= this.getNode(currNodeName);
		//pr�fe ob es bereits einen Knoten zu dieser Datei gibt
		//neuen Knoten erzeugen
		if (currNode == null)
		{
			currNode= new Node(currNodeName);
			this.addNode(currNode);
		}
		//Knoten erweitern
		currNode.setValue(KW_ACON, aCon);
		
		//Knoten des aCon in Orderindex eintragen
		this.fillOrderIdx(currNode);
		
		//durch alle Dateien gehen, von denen der aCon abh�ngt, wenn aCon != META_STRUCT_DATA
		if ((aCon.getAbsAnaType()!= AnalyzeContainer.ABS_ANA_TYPE.META_STRUCT_DATA) &&(depFiles != null))
		{
			for (String depFileName: depFiles)
			{
				//pr�fen ob es bereits einen Knoten f�r diese Datei gibt
				File depFile= new File(depFileName);
				//Erzeugung und Bereinigung des Knotennamens
				String canFileName= this.createNodeName(depFile);
				
				Node depNode= this.getNode(canFileName);
				//neuen Knoten erzeugen
				if (depNode == null) 
				{	
					depNode= new Node(canFileName);
					this.addNode(depNode);
					this.fillOrderIdx(depNode);
				}
				//Kante zwischen currNode und depNode einf�gen
				this.createEdge(currNode, depNode);
			}
		}
	}
	
	/**
	 * Serialisiert die in diesem Graph existierenden Knoten anhand des Order-Wertes
	 * der Analyze-Container der Knoten und der Abh�ngigkeiten der Knoten untereinander. 
	 * Diese Knoten werden in einer Liste von AnalyzeContainern zur�ckgegeben. 
	 */
	public Vector<AnalyzeContainer> serialize() throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_STD + "start method serialize()");
		if (DEBUG) System.out.println(MSG_STD + "start method serialize()");
		
		if (this.nonOrderNodes.size() > 0) throw new Exception(ERR_NON_ORDERED_NODES + this.nonOrderNodes);
		Vector<AnalyzeContainer> serVec= null;	//serialisierte Liste
		
		Vector<Vector<Node>> tmpVec= new Vector<Vector<Node>>();
		//gehe alle Reihenfolgeebenen durch
		Enumeration<Double> orderVals= this.orderIdx.keys();
		while (orderVals.hasMoreElements())
		{
			double order= orderVals.nextElement();
			Vector<Node> sortedSlot= this.sortSlot(order);
			
			if (DEBUG ) 
			{
				System.out.print("serialized slot: ");
				for (Node currNode: sortedSlot)
					System.out.print(((AnalyzeContainer)currNode.getValue(KW_ACON)).getPAULAID() + ", ");
				System.out.println();
			}
			
			if (tmpVec.isEmpty()) tmpVec.add(sortedSlot);
			else
			{
				boolean inserted= false; //gibt an ob sortedSlot eingef�gt wurde
				for (int i= 0; i < tmpVec.size(); i++)
				{
					//wenn Listeneintrag gr��er ist als aktueller Vector,
					if (((AnalyzeContainer)((Node)tmpVec.elementAt(i).firstElement()).getValue(KW_ACON)).getOrder() > order)
					{
						//F�ge aktuellen Slot an dieser Stelle ein
						tmpVec.insertElementAt(sortedSlot, i);
						inserted= true;
						break;
					}
				}
				//sortedSlot wurde noch nicht eigef�gt
				if (!inserted) tmpVec.add(sortedSlot);
			}
		}
		if (!tmpVec.isEmpty())
		{
			serVec= new Vector<AnalyzeContainer>();	//serialisierte Liste initialisieren
			//sortiere sortierte Slots nach Reihenfolgelevel
			for(Vector<Node> sortedSlot : tmpVec)
			{
				for(Node node : sortedSlot)
				{
					serVec.add((AnalyzeContainer)node.getValue(KW_ACON));
				}
			}	
		}
		return(serVec);
	}
	
	/**
	 * Gibt Informationen �ber dieses Objekt als String zur�ck. 
	 * @return String - Informationen �ber dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "this method isn�t implemented";
		return(retStr);
	}
// ========================================= main Methode f�r Test =========================================	
	public static void main(String args[])
	{
		String MSG_START= "********************** start test for class FileDepGraph **********************";
		String MSG_END= "********************** test for class FileDepGraph ended **********************";
		
		System.out.println(MSG_START);
		try
		{
			//log4j einrichten
			Logger logger= Logger.getLogger(PAULAAnalyzer.class);	//log4j initialisieren
			DOMConfigurator.configure("./settings/log4j.xml");			//log-FileEinstellungen
			
			FileDepGraph fDGraph= new FileDepGraph(); 
			//abh�ngige Dateien kreieren
			
			//Priorit�tsebene 1
			File xmlFile= null;
			File dtdFile= null;
			Vector<String> depStr= new Vector<String>();
			
			
			//Prio 11
			xmlFile = new File("./test/fDGraph/prio11.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			AnalyzeContainer aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("text");
			aCon.setPAULAID("prio11");
			aCon.setOrder(1.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("primData");
			fDGraph.addNode(aCon, null);
			
			//Prio 21
			xmlFile = new File("./test/fDGraph/prio21.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("tok");
			aCon.setPAULAID("prio21");
			aCon.setOrder(2.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("tokData");
			depStr.clear();
			depStr.add("./test/fDGraph/prio11.xml");
			depStr.add("./test/fDGraph/prio22.xml");
			fDGraph.addNode(aCon, depStr);
			
			//Prio 12
			xmlFile = new File("./test/fDGraph/prio12.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("text");
			aCon.setPAULAID("prio12");
			aCon.setOrder(1.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("textData");
			depStr.clear();
			depStr.add("./test/fDGraph/prio11.xml");
			depStr.add("./test/fDGraph/prio13.xml");
			fDGraph.addNode(aCon, depStr);
			
			//Prio 22
			xmlFile = new File("./test/fDGraph/prio22.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("tok");
			aCon.setPAULAID("prio22");
			aCon.setOrder(2.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("tokData");
			fDGraph.addNode(aCon, null);
			
			//Prio 13
			xmlFile = new File("./test/fDGraph/prio13.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("text");
			aCon.setPAULAID("prio13");
			aCon.setOrder(1.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("textData");
			fDGraph.addNode(aCon, null);
			
			//Prio 23
			xmlFile = new File("./test/fDGraph/prio23.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("tok");
			aCon.setPAULAID("prio23");
			aCon.setOrder(2.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("tokData");
			depStr.clear();
			depStr.add("./test/fDGraph/prio12.xml");
			depStr.add("./test/fDGraph/prio22.xml");
			fDGraph.addNode(aCon, depStr);
			
			//Prio 24
			xmlFile = new File("./test/fDGraph/prio24.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("tok");
			aCon.setPAULAID("prio24");
			aCon.setOrder(2.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("tokData");
			depStr.clear();
			depStr.add("./test/fDGraph/prio13.xml");
			depStr.add("./test/fDGraph/prio23.xml");
			depStr.add("./test/fDGraph/prio22.xml");
			fDGraph.addNode(aCon, depStr);
			
			//Prio 25 -- soll Fehler produzieren
			/*
			xmlFile = new File("./test/fDGraph/prio25.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("tok");
			aCon.setPAULAID("prio25");
			aCon.setOrder(2.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("tokData");
			depStr.clear();
			depStr.add("./test/fDGraph/prio13.xml");
			depStr.add("./test/fDGraph/prio23.xml");
			depStr.add("./test/fDGraph/prio32.xml");
			fDGraph.addNode(aCon, depStr);
			*/
			//Prio 31
			xmlFile = new File("./test/fDGraph/prio31.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("struct");
			aCon.setPAULAID("prio31");
			aCon.setOrder(3.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("structData");
			depStr.clear();
			depStr.add("./test/fDGraph/prio32.xml");
			fDGraph.addNode(aCon, depStr);
			
			//Prio 32
			xmlFile = new File("./test/fDGraph/prio32.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("struct");
			aCon.setPAULAID("prio32");
			aCon.setOrder(3.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("structData");
			depStr.clear();
			depStr.add("./test/fDGraph/prio13.xml");
			depStr.add("./test/fDGraph/prio23.xml");
			fDGraph.addNode(aCon, depStr);
			
			//Prio 33
			xmlFile = new File("./test/fDGraph/prio33.xml");
			dtdFile= new File("./test/fDGraph/dtd.dtd");
			
			aCon= new AnalyzeContainer(xmlFile);
			aCon.setDTD(dtdFile);
			aCon.setPAULAType("struct");
			aCon.setPAULAID("prio33");
			aCon.setOrder(3.0);
			aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			aCon.setAnaType("structData");
			depStr.clear();
			depStr.add("./test/fDGraph/prio23.xml");
			depStr.add("./test/fDGraph/prio31.xml");
			depStr.add("./test/fDGraph/prio32.xml");
			fDGraph.addNode(aCon, depStr);
			
			
			System.out.println("Index of orders:");
			System.out.println(fDGraph.orderIdx);
			System.out.println("Liste der Knoten ohne aCon:" + fDGraph.nonOrderNodes);
			fDGraph.printDOT("./test/fdGraph/depGraph.dot");
			
			fDGraph.serialize();
			
			//Abh�ngige Dateien
		}
		catch (Exception e)
			{e.printStackTrace();}
		System.out.println(MSG_END);
	}
}
