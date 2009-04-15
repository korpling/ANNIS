package util.graph;

import util.graph.Graph.TRAVERSAL_MODE;


/**
 * Dieses Interface muss implementiert werden, wenn die Methode DepthFirst() der Klasse
 * Graph genutzt wird. 
 * @author Florian Zipser
 * @version 1.0
 */
public interface XTraversalObject extends TraversalObject
{
	/**
	 * Diese Funktion wird von der Methode traversalGraph() aufgerufen, die die
	 * Knoten des Graphen traversiert. Dabei können verschiedene Verfahren angewandt 
	 * werden (wie z.B. DEPTH_FIRST, BOTTOM_UP ...), wodurch die Knoten des graphen in
	 * unterschiedlichen Reihenfolgen traversiert werden. 
	 * Die Methode traversalGraph() erzeugt ein Callback und ruft im ihr übergebenen
	 * Objekt die Methode checkConstraint() auf. Diese Methode soll dann anhand des 
	 * übergebenen Knotens entscheiden, ob weiter traversiert werden soll.
	 * @param tMode TRAVERSAL_MODE - Modus der Traversion
	 * @param edge Edge - Kante über die dieser Knoten erreicht wurde
	 * @param currNode Node - aktueller zu prüfender Knoten
	 * @return true, wenn weiter traversiert werden soll, false sonst
	 */
	public boolean checkConstraint(	TRAVERSAL_MODE tMode, 
									Edge edge, 
									Node currNode) throws Exception;
}
