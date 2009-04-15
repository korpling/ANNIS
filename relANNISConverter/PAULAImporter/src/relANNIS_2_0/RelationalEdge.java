package relANNIS_2_0;

/**
 * Alle dieses Interface implementierenden Klassen können in die rank-Tabelle des 
 * relANNIS-Modells geschrieben werden.
 * @author Florian Zipser
 * @version 1.0
 */
public interface RelationalEdge 
{
	/**
	 * Schreibt diese Kante auf einen TupleWriter. Dieser wird über das 
	 * dbConnector-Objekt ermittelt.
	 * @param pre Long - Pre-Wert für den Quellknoten dieser Kante
	 * @param post Long - Post-Wert für den Quellknoten dieser Kante 
	 * @param father Long - Zielknoten für diese Kante
	 */
	public void toWriter(String pre, String post, String fatherPre) throws Exception;
}
