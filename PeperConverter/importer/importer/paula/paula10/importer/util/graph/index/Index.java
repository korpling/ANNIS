package importer.paula.paula10.importer.util.graph.index;

import java.util.Collection;

/**
 * Dieses Interface benennt alle Methoden, die ein Objekt vom Typ Index benötigt, um
 * von einem IndexManager angesprochen werden zu können. Dieses Interface kann von
 * anderen Klassen implementiert werden und somit einen Index bereitstellen. 
 * Vornehmlich wird dieses Interface und ein IndexMgr für die Klasse Graph
 * geschrieben. 
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public interface Index 
{
	/**
	 * Diese Methode gibt den Namen dieses Indizes zurück.
	 * @return Name dieses Indizes
	 */
	public String getIdxName();
	
	/**
	 * Gibt alle Ids zurück, die in diesem Index vorhanden sind
	 * @return Name dieses Indizes
	 */
	public Collection<Object> getIds();
	
	/**
	 * Diese Methode fügt diesem Index einen neuen Eintrag hinzu. Der Eintrag wird
	 * unter dem übergebenen Identifier abgelegt und kann anschließend über diesen 
	 * identifiziert werden.
	 * @param id Object - eindeutiger Identifier, der den übergebenen Eintrag identifiziert
	 * @param entry Object - abzulegender Eintrag  
	 */
	public void addEntry(Object id, Object entry) throws Exception;
	
	/**
	 * Diese Methode gibt zurück, ob es in diesem Index mindestens ein Objekt gibt,
	 * das der übergebenen id zugeordnet ist.
	 * @param id Object - eindeutiger Identifier, der den übergebenen Eintrag identifiziert
	 * @return true, wenne s mindestens einen Eintrag zu der übergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasId(Object id) throws Exception;
	
	/**
	 * Diese Methode gibt zurück, ob der übergebene Eintrag in diesem Index eingetragen ist. 
	 * @param entry Object - eindeutiger Identifier, der den übergebenen Eintrag identifiziert
	 * @return true, wenne s mindestens einen Eintrag zu der übergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasEntry(Object entry) throws Exception;
	
	/**
	 * Diese Methode entfernt alle Einträge, die mit der übergebenen id identifiziert werden.
	 * Alle Einträge zu einer id werden als slot bezeichnet.
	 * @param id Object -  eindeutiger Identifier, der den zu löschenden Eintrag identifiziert
	 */
	public boolean removeSlot(Object id) throws Exception;
	
	/**
	 * Diese Methode entfernt den übergebenen Eintrag aus dem Index. Ist der Slot nach dem
	 * entfernen dieses Eintrages leer, wird der ganze slot entfernt.
	 */
	public boolean removeEntry(Object entry) throws Exception;
	
	/**
	 * Diese Methode entfernt alle Einträge aus dem Index.
	 * @return true, wenn alle Einträge gelöscht werden konnten
	 * @throws Exception
	 */
	public boolean removeAll() throws Exception;
	
	/**
	 * Diese Methode gibt eine Liste von Einträgen, die von der übergebenen id 
	 * identifiziert werden zurück. 
	 * @return Objekt, passend zu dem übergebenen Eintrag
	 */
	public Collection<Object> getEntry(Object id) throws Exception;
	
	/**
	 * Gibt die Anzahl der in diesem Index enthaltenen Slots, bzw. verschiedenen Id-Werte
	 * zurück. 
	 * @return Anzahl der Slots
	 * @throws Exception
	 */
	public long getNumOfIds() throws Exception;
	
	/**
	 * Gibt die Anzahl der in diesem Index enthaltenen Einträge zurück.
	 * @return Anzahl der Einträge
	 * @throws Exception
	 */
	public long getNumOfEntries() throws Exception;
}
