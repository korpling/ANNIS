package importer.paula.paula10.importer.util.graph;

import importer.paula.paula10.importer.util.graph.Graph.TRAVERSAL_MODE;

/**
 * Dieses Interface muss implementiert werden, wenn die Methode DepthFirst() der Klasse
 * Graph genutzt wird. 
 * @author Florian Zipser
 * @version 1.0
 */
public interface TraversalObject 
{
	/**
	 * Diese Funktion wird von der Methode traversalGraph() aufgerufen, die die
	 * Knoten des Graphen traversiert. Dabei können verschiedene Verfahren angewandt 
	 * werden (wie z.B. DEPTH_FIRST, BOTTOM_UP ...), wodurch die Knoten des graphen in
	 * unterschiedlichen Reihenfolgen traversiert werden. 
	 * Die Methode traversalGraph() erzeugt ein Callback und ruft im ihr übergebenen
	 * Objekt die Methode nodeReached() auf. 
	 * @param sMode SEARCH_MODE - Art der Traversierung
	 * @param currNode Node - aktueller Knoten, also der der gerade durch das Traversieren erreicht wurde (Veterknoten bei DEPTH_FIRST, Kinknoten bei BOTTOM_UP)
	 * @param edge Edge - Kante die die beiden Knoten verbindet
	 * @param fromNode Node - Knoten von dem aus der aktuelle Knoten erreicht wurde
	 * @param order long - Reihenfolge des Kindknotens in der Ordnung der Nachfolger des Vaterknotens (beginnend bei 0), ist currNode Veterknoten, so ist es die Reihenfolge  
	 */
	public void nodeReached(	TRAVERSAL_MODE tMode, 
								Node currNode, 
								Edge edge,
								Node fromNode, 
								long order) throws Exception;
	
	/**
	 * Diese Funktion wird von der Methode traversalGraph() aufgerufen, die die
	 * Knoten des Graphen traversiert. Dabei können verschiedene Verfahren angewandt 
	 * werden (wie z.B. DEPTH_FIRST, BOTTOM_UP ...), wodurch die Knoten des Graphen in
	 * unterschiedlichen Reihenfolgen traversiert werden. 
	 * Die Methode traversalGraph() erzeugt ein Callback und ruft im ihr übergebenen
	 * Objekt die Methode nodeLeft() auf. 
	 * @param sMode SEARCH_MODE - Art der Traversierung
	 * @param currNode Node - aktueller Knoten, also der der gerade durch das Traversieren erreicht wurde (Veterknoten bei DEPTH_FIRST, Kinknoten bei BOTTOM_UP)
	 * @param edge Edge - Kante die die beiden Knoten verbindet
	 * @param fromNode Node - Knoten von dem aus der aktuelle Knoten erreicht wurde
	 * @param order long - Reihenfolge des Kindknotens in der Ordnung der Nachfolger des Vaterknotens (beginnend bei 0) 
	 */
	public void nodeLeft(	TRAVERSAL_MODE tMode, 
							Node currNode, 
							Edge edge,
							Node fromNode, 
							long order) throws Exception;
}
