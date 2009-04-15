package util.depGraph;

/**
 * Die Klasse stammt aus der Version PAULAAnalyzer.
 * Dieses Interface muss implementiert werden, wenn die Methode DepthFirst() der Klasse
 * Graph genutzt wird. 
 * @author Florian Zipser
 * @version 1.0
 */
public interface TraversalObject 
{
	/**
	 * Diese Funktion wird von der Methode Graph.depthFirst() aufgerufen um ein Callback
	 * zu realisieren. Damit kann eine andere Klasse verständigt werden, wenn die 
	 * Tiefensuche einen neuen Knoten erreicht hat.
	 * @param currNode Node - aktuell erreichter Knoten
	 * @param father Node - Vater des aktuellen Knoten
	 * @param order long - Reihenfolge des aktuellen Knotens in der Ordnung der Nachfolger des Vaterknotens (beginnend bei 0)
	 */
	public void nodeReached(Node currNode, Node father, long order) throws Exception;
	
	/**
	 * Diese Funktion wird von der Methode Graph.depthFirst() aufgerufen um ein Callback
	 * zu realisieren. Damit kann eine andere Klasse verständigt werden, wenn die 
	 * Tiefensuche einen neuen Knoten erreicht hat. Die Methode wird aufgerufen, wenn der
	 * Knoten wieder verlassen wird (parallel zur Postorder);
	 * @param currNode Node - aktuell erreichter Knoten
	 * @param father Node - Vater des aktuellen Knoten
	 * @param order long - Reihenfolge des aktuellen Knotens in der Ordnung der Nachfolger des Vaterknotens (beginnend bei 0)
	 */
	public void nodeLeft(Node currNode, Node father, long order) throws Exception;
}
