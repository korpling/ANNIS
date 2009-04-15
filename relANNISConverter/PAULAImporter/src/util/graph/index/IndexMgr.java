package util.graph.index;

/**
 * Dieses Interface bietet alle Methoden eines InterfaceManagers. Dieser kümmert sich um 
 * die Verwaltung verschiedener Indizes und identifiziert diese über ihren eindeutigen 
 * Namen.
 * @author Florian Zipser
 * @version 1.0
 */
public interface IndexMgr 
{
	/**
	 * Fügt den übergebenen Index unter dem übergebenen Namen dem IndexMgr hinzu.
	 * @param name String - Name des Indexes
	 * @param idx Index - der einzutragende Index
	 * @exception Fehler, wenn der Index leer ist oder bereits existiert
	 */
	public void addIndex(String name, Index idx) throws Exception;
	
	/**
	 * Fügt den übergebenen Index unter dem Namen des Indexes, der über die Index.getName()
	 * Methode ermittelt wird, dem IndexMgr hinzu.
	 * @param idx Index - der einzutragende Index
	 * @exception Fehler, wenn der Index leer ist oder bereits existiert
	 */
	public void addIndex(Index idx) throws Exception;
	
	/**
	 * Gibt einen Index zurück, der über den gegebenen Namen identifiziert wird.
	 * @param name String - Name des gesuchten Index
	 * @return Index, der über den gegebenen Namen identifiziert wurde
	 * @throws Exception
	 */
	public Index getIndex(String name) throws Exception;
	
	/**
	 * Gibt zurück, ob dieser IndexMgr einen Index mit dem übergebenen Namen verwaltet
	 * @param name String - Name des zu suchenden Index
	 * @return true, wenn ein Index mit dem übergebenen Namen von diesem IndexMgr verwaltet wird
	 * @throws Exception
	 */
	public boolean hasIndex(String name) throws Exception;
	
	/**
	 * Entfernt einen Index mit dem übergebenen Namen aus diesem IndexMgr.
	 * @param name String - Name des zu entfernenden Index
	 * @return true, wenn der Index entfernt werden konnte, false sonst
	 * @throws Exception
	 */
	public boolean removeIndex(String name) throws Exception;
	
	/**
	 * Entfernt den übergebenen entry aus allen von diesem IndexMgr verwalteten Indizes.
	 * @param entry Object - aus allen Indizes zu entfernender Eintrag
	 * @throws Exception
	 */
	public boolean removeEntry(Object entry)throws Exception;
	
	/**
	 * In allen Indizes, die einen Slot zu der übergebenen ID enthalten wird der 
	 * entsprechende Slot entfernt.
	 * @param id Object - id des Slots in allen Indizes
	 * @throws Exception
	 */
	public boolean removeSlot(Object id)throws Exception;
	
	
}
