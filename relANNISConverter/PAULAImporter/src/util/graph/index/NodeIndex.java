package util.graph.index;

import java.util.Collection;

import util.graph.Node;

/**
 * Dieses Interface definiert einen Index, der auf den Typen Node als Indexinhalt
 * zugeschnitten ist.
 * @author Florian Zipser
 *
 */
public interface NodeIndex extends Index 
{
	/**
	 * Gibt alle Ids zurück, die in diesem Index vorhanden sind
	 * @return Collection<String> - alle Ids dieses Indexes
	 */
	public Collection<String> getNodeIds();
	
	/**
	 * Diese Methode fügt diesem Index einen neuen Eintrag hinzu. Der Eintrag wird
	 * unter dem übergebenen Identifier abgelegt und kann anschließend über diesen 
	 * identifiziert werden.
	 * @param id Object - eindeutiger Identifier, der den übergebenen Eintrag identifiziert
	 * @param entry Object - abzulegender Eintrag  
	 */
	public void addEntry(String id, Node entry) throws Exception;
	
	/**
	 * Diese Methode gibt zurück, ob es in diesem Index mindestens ein Objekt gibt,
	 * das der übergebenen id zugeordnet ist.
	 * @param id Object - eindeutiger Identifier, der den übergebenen Eintrag identifiziert
	 * @return true, wenne s mindestens einen Eintrag zu der übergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasId(String id) throws Exception;
	
	/**
	 * Diese Methode gibt zurück, ob der übergebene Eintrag in diesem Index eingetragen ist. 
	 	 * @param entry Node - Knoten, für den geprüft werden soll, ob sie in diesem Index enthalten ist
	 * @return true, wenne s mindestens einen Eintrag zu der übergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasEntry(Node entry) throws Exception;
	
	/**
	 * Diese Methode entfernt alle Einträge, die mit der übergebenen id identifiziert werden.
	 * Alle Einträge zu einer id werden als slot bezeichnet.
	 * @param id Object -  eindeutiger Identifier, der den zu löschenden Eintrag identifiziert
	 */
	public boolean removeSlot(String id) throws Exception;
	
	/**
	 * Diese Methode entfernt den übergebenen Eintrag aus dem Index. Ist der Slot nach dem
	 * entfernen dieses Eintrages leer, wird der ganze slot entfernt.
	 */
	public boolean removeEntry(Node entry) throws Exception;
	
	/**
	 * Diese Methode gibt eine Liste von Einträgen, die von der übergebenen id 
	 * identifiziert werden zurück. 
	 * @param id String - eindeutiger Identifizierer des gesuchten Knoten(s) 
	 * @return Collection<Node>, passend zu dem übergebenen Identifizierer
	 */
	public Collection<Node> getEntry(String id) throws Exception;
}
