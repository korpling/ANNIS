package importer.paula.paula10.importer.util.graph.index;

import java.util.Collection;

import importer.paula.paula10.importer.util.graph.Edge;

/**
 * Dieses Interface definiert einen Index, der auf den Typen Edge als Indexinhalt
 * zugeschnitten ist.
 * @author Florian Zipser
 *
 */
public interface EdgeIndex extends Index 
{
	/**
	 * Gibt alle Ids zurück, die in diesem Index vorhanden sind
	 * @return Collection<String> - alle Ids dieses Indexes
	 */
	public Collection<String> getNodeIds();
	
	/**
	 * Diese Methode fügt diesem Index eine neue Kantehinzu. Der Eintrag wird
	 * unter dem übergebenen Identifier abgelegt und kann anschließend über diesen 
	 * identifiziert werden.
	 * @param id String - eindeutiger Identifier, der die übergebene Kante identifiziert
	 * @param entry Edge - abzulegende Kante  
	 */
	public void addEntry(String id, Edge entry) throws Exception;
	
	/**
	 * Diese Methode gibt zurück, ob es in diesem Index mindestens einen Identifier gibt,
	 * das der übergebenen id zugeordnet ist.
	 * @param id Object - eindeutiger Identifier, der die übergebene Kante identifiziert
	 * @return true, wenn es mindestens eine Kante zu der übergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasId(String id) throws Exception;
	
	/**
	 * Diese Methode gibt zurück, ob der übergebene Eintrag in diesem Index eingetragen ist. 
	 * @param entry Edge - Kante, für die geprüft werden soll, ob sie in diesem Index enthalten ist
	 * @return true, wenne s mindestens einen Eintrag zu der übergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasEntry(Edge entry) throws Exception;
	
	/**
	 * Diese Methode entfernt alle Einträge, die mit der übergebenen id identifiziert werden.
	 * Alle Kanten zu einer id werden als slot bezeichnet.
	 * @param id String -  eindeutiger Identifier, der den zu löschenden Kante identifiziert
	 */
	public boolean removeSlot(String id) throws Exception;
	
	/**
	 * Diese Methode entfernt die übergebene Kante aus dem Index. Ist der Slot nach dem
	 * entfernen dieses Eintrages leer, wird der ganze Slot entfernt.
	 */
	public boolean removeEntry(Edge entry) throws Exception;
	
	/**
	 * Diese Methode gibt eine Liste von Kanten, die von der übergebenen id 
	 * identifiziert werden zurück.
	 * @param id String - eindeutiger Identifizierer der gesuchten Kante(n) 
	 * @return Collection<Edge>, passend zu dem übergebenen Identifizierer
	 */
	public Collection<Edge> getEntry(String id) throws Exception;
}
