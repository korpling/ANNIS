package relANNIS_2_0;

/**
 * Dieses Interface bietet Methoden an, die für Datenknoten die sich auf einen Textwert 
 * beziehen bestimmt sind. So kann zu einem Textwert dessen text, linke Grenze, rechte 
 * Grenze und Kontinuität zurückgegeben werden.
 * @author Florian Zipser
 * @version 1.0
 */
public interface TextedDN 
{
	/**
	 * Gibt einen Primärdatenknoten zurück, auf den sich dieser bezieht.
	 * @return Primärdatenlknoten
	 */
	public PrimDN getPrimDN() throws Exception;
	
	/**
	 * Gibt die linke Textgrenze eines in diesem Knoten gespeicherten Textes zurück.
	 * @return linke Textgrenze zu diesem Text
	 */
	public Long getLeft() throws Exception;
	
	/**
	 * Gibt die rechte Textgrenze eines in diesem Knoten gespeicherten Textes zurück.
	 * @return rechte Textgrenze zu diesem Text
	 */
	public Long getRight() throws Exception;
	
	/**
	 * Gibt zurück, ob der Text in diesem Textknoten kontinuierlich ist oder nicht.
	 * @return true, wenn Text kontinuierlich ist, false sonst
	 */
	public boolean getCont()throws Exception;
}
