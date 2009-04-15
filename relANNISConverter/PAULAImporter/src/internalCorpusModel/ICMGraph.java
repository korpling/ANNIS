package internalCorpusModel;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import relANNIS_2_0.DominanceEdge;

import util.graph.Edge;
import util.graph.Graph;
import util.graph.XTraversalObject;

public class ICMGraph extends Graph
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMGraph";		//Name dieses Tools
	private static final boolean DEBUG=		true;			//DEBUG-Schalter
	
	//für das Traversieren über depthFirst()
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt ein Graph-Objekt. Mit Nachrichtenstrom.
	 * @param logger Logger - Log4j zur Nachrichtenausgabe
	 */
	public ICMGraph(Logger logger) throws Exception
	{
		//Graph mit isDirected= true, idOrdered= true, logger erzeugen
		super(true, true, logger);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== protected Methoden ==============================================	
	/**
	 * Rekursiver Aufruf für die Methode depthFirst
	 * @param currNode Node - aktueller Knoten
	 * @param father - Vater des aktuellen Knotens
	 * @param order - Reihenfolge in der der aktuelle Knoten in der Kinderliste des Vaters vorkommt (beginnend bei null)
	 * @throws Exception
	 */
	/*
	protected void depthFirstRec(	IKMAbstractDN currNode, 
									IKMAbstractDN father,
									long order) throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_START_FCT + "depthFirstRec()");
		
		this.travObj.nodeReached(currNode, father, order);
		
		//durch alle Kinder dieses Knotens gehen
		Vector<String> childs= this.outEdges.get(currNode.getName());
		//wenn Knoten Kinder hat
		if (childs != null)
		{
			//gehe durch alle Kinder des aktuellen Knoten
			int i= 0;
			for(String childName: childs)
			{
				IKMAbstractDN childNode= (IKMAbstractDN)this.getNode(childName);
				if (this.travObj.checkConstraint(childNode))
				{
					this.depthFirstRec(childNode, currNode, i);
					i++;
				}
			}
		}
		this.travObj.nodeLeft(currNode, father, order);
		
		if (this.logger!= null) this.logger.debug(MSG_END_FCT + "depthFirstRec()");
	}
	*/
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Traversiert den Graphen bei dem übergebenen Knoten beginnenend, dessen Kinder 
	 * abwärts. Also entlang der Tiefensuche. Während des Traversierens werden die 
	 * Callback-Methoden des XTraversalObjects aufgerufen
	 * @param startNode IKMAbstractDN - Knoten bei dem begonnen werden soll
	 * @param travObj XTraversalObject - Objekt für die Callbacks
	 */
	public void depthFirst(	ICMAbstractDN startNode,
							XTraversalObject travObj) throws Exception
	{
		
		this.traverseGraph(TRAVERSAL_MODE.DEPTH_FIRST, startNode, travObj);
		//this.travObj= travObj;	
		//depthFirstRec(startNode, null, 0);
	}
	
	/**
	 * Gibt eine Liste aller Elternknoten des übergebenen Knozens zurück. Wenn es keine
	 * Elternknoten gibt, wird null zurückgegeben.
	 * @param cNode ICMAbstractDN - Kontextknoten
	 * @return Liste der Elternknoten
	 * @exception Fehler, wenn Kontextknoten nicht existiert 
	 */
//	public Vector<ICMAbstractDN> getParents(ICMAbstractDN cNode) throws Exception
//	{
//		Collection<Edge> parentEdges= this.getInEdges(cNode);
//		Vector<ICMAbstractDN> retNodes= null;
//		if ((parentEdges != null) && (!parentEdges.isEmpty()))
//		{
//			retNodes= new Vector<ICMAbstractDN>();
//			for (Edge edge: parentEdges)
//				retNodes.add((ICMAbstractDN) edge.getFromNode());
//		}
//		return(retNodes);
//	}
	
	/**
	 * Gibt eine Liste aller Elternknoten des übergebenen Knozens zurück, die durch eine
	 * Dominanzkante erreicht werden können. Wenn es keine
	 * Elternknoten gibt, wird null zurückgegeben.
	 * @param cNode ICMAbstractDN - Kontextknoten
	 * @return Liste der Elternknoten
	 * @exception Fehler, wenn Kontextknoten nicht existiert 
	 */
	public Vector<ICMAbstractDN> getDominanceParents(ICMAbstractDN cNode) throws Exception
	{
		Collection<Edge> parentEdges= this.getInEdges(cNode);
		Vector<ICMAbstractDN> retNodes= null;
		if ((parentEdges != null) && (!parentEdges.isEmpty()))
		{
			retNodes= new Vector<ICMAbstractDN>();
			for (Edge edge: parentEdges)
			{
				//muss geändert werden in if (ICMDominanceEdge.class.isInstance(edge))
				if (!ICMNonDominanceEdge.class.isInstance(edge))
					retNodes.add((ICMAbstractDN) edge.getFromNode());
			}
		}
		return(retNodes);
	}
	
	/**
	 * Gibt eine Liste aller Kinderknoten des übergebenen Knozens zurück. Wenn es keine
	 * Kinderknoten gibt, wird null zurückgegeben.
	 * @param cNode IKMAbstractDN - Kontextknoten
	 * @return Liste der Kinderknoten
	 * @exception Fehler, wenn Kontextknoten nicht existiert 
	 */
//	public Vector<ICMAbstractDN> getChilds(ICMAbstractDN cNode) throws Exception
//	{
//		Collection<Edge> childEdges= this.getOutEdges(cNode);
//		Vector<ICMAbstractDN> retNodes= null;
//		if ((childEdges != null) && (!childEdges.isEmpty()))
//		{
//			retNodes= new Vector<ICMAbstractDN>();
//			for (Edge edge: childEdges)
//				retNodes.add((ICMAbstractDN) edge.getToNode());
//		}
//		return(retNodes);
//	}
	
	/**
	 * Gibt eine Liste aller Kinderknoten des übergebenen Knozens zurück, die durch eine
	 * Dominanzkante erreicht werden können. Wenn es keine
	 * Kinderknoten gibt, wird null zurückgegeben.
	 * @param cNode IKMAbstractDN - Kontextknoten
	 * @return Liste der Kinderknoten
	 * @exception Fehler, wenn Kontextknoten nicht existiert 
	 */
	public Vector<ICMAbstractDN> getDominanceChilds(ICMAbstractDN cNode) throws Exception
	{
		Collection<Edge> childEdges= this.getOutEdges(cNode);
		Vector<ICMAbstractDN> retNodes= null;
		if ((childEdges != null) && (!childEdges.isEmpty()))
		{
			retNodes= new Vector<ICMAbstractDN>();
			//muss geändert werden in if (ICMDominanceEdge.class.isInstance(edge))
			for (Edge edge: childEdges)
				if (!ICMNonDominanceEdge.class.isInstance(edge))
					retNodes.add((ICMAbstractDN) edge.getToNode());
		}
		return(retNodes);
	}
	
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
}
