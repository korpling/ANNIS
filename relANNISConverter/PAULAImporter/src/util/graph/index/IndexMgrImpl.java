package util.graph.index;

import java.util.Hashtable;
import java.util.Map;

/**
 * Diese Klasse implementiert das Interface Index und stellt somit eine Indexverwaltung 
 * zur Verfügung.
 * @author Florian Zipser
 *
 */
public class IndexMgrImpl implements IndexMgr 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IndexMgrImpl";		//Name dieses Tools
	//private static final boolean DEBUG= false;
	
	/**
	 * Diese Tabelle ist der Kern des implementierten Index.
	 */
	protected Map<String, Index> idxTable= null;	 
	
	/**
	 * Anzahl der gesamten Einträge dieses Indizes
	 */
	protected Long numOfIndexes= null;
	
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_NAME_ADD=				MSG_ERR + "Cannot add the given index, because its name is empty.";
	private static final String ERR_EMPTY_IDX_ADD=				MSG_ERR + "Cannot add the given index, because its empty.";
	private static final String ERR_IDX_ALREADY_ADDED=			MSG_ERR + "Cannot add the given index, because an index with the given name has already been added: ";
	private static final String ERR_EMPTY_NAME=					MSG_ERR + "The given name for index is empty.";
	private static final String ERR_EMPTY_NAME_REM=				MSG_ERR + "Cannot remove index, because the given name fpr index is empty.";
	private static final String ERR_EMPTY_ENTRY_REM=			MSG_ERR + "Cannot remove entry from indizes, because the given entry is empty.";
	private static final String ERR_EMPTY_ID_REM=				MSG_ERR + "Cannot remove id from indizes, because the given id is empty.";
//	 ============================================== Konstruktoren ==============================================

	public IndexMgrImpl() throws Exception
	{
		this.idxTable= new Hashtable<String, Index>();
	}
//	 ============================================== öffentliche Methoden ==============================================	
	/**
	 * Fügt den übergebenen Index unter dem Namen des Indexes, der über die Index.getName()
	 * Methode ermittelt wird, dem IndexMgr hinzu.
	 * @param idx Index - der einzutragende Index
	 * @exception Fehler, wenn der Index leer ist oder bereits existiert
	 */
	public void addIndex(Index idx) throws Exception
	{
		this.addIndex(idx.getIdxName(), idx);
	}
	
	/**
	 * Fügt den übergebenen Index unter dem übergebenen Namen dem IndexMgr hinzu.
	 * @param name String - Name des Indexes
	 * @param idx Index - der einzutragende Index
	 * @exception Fehler, wenn der Index leer ist oder bereits existiert
	 */
	public void addIndex(String name, Index idx) throws Exception 
	{
		if ((name== null) || (name.equalsIgnoreCase("")))
			throw new Exception(ERR_EMPTY_NAME_ADD);
		if (idx== null)
			throw new Exception(ERR_EMPTY_IDX_ADD);
		if (this.hasIndex(name))
			throw new Exception(ERR_IDX_ALREADY_ADDED + name);
		
		this.idxTable.put(name, idx);
	}

	/**
	 * Gibt zurück, ob dieser IndexMgr einen Index mit dem übergebenen Namen verwaltet
	 * @param name String - Name des zu suchenden Index
	 * @return true, wenn ein Index mit dem übergebenen Namen von diesem IndexMgr verwaltet wird
	 * @throws Exception
	 */
	public boolean hasIndex(String name) throws Exception 
	{
		if ((name== null) || (name.equalsIgnoreCase("")))
			throw new Exception(ERR_EMPTY_NAME);
		return(this.idxTable.containsKey(name));
	}
	
	/**
	 * Gibt einen Index zurück, der über den gegebenen Namen identifiziert wird.
	 * @param name String - Name des gesuchten Index
	 * @return Index, der über den gegebenen Namen identifiziert wurde
	 * @throws Exception
	 */
	public Index getIndex(String name) throws Exception 
	{
		if ((name== null) || (name.equalsIgnoreCase("")))
			throw new Exception(ERR_EMPTY_NAME);
		return(this.idxTable.get(name));
	}

	/**
	 * Entfernt einen Index mit dem übergebenen Namen aus diesem IndexMgr.
	 * @param name String - Name des zu entfernenden Index
	 * @return true, wenn der Index entfernt werden konnte, false sonst
	 * @throws Exception
	 */
	public boolean removeIndex(String name) throws Exception 
	{
		boolean retVal= false;
		if ((name== null) || (name.equalsIgnoreCase("")))
			throw new Exception(ERR_EMPTY_NAME_REM);
		if (this.hasIndex(name))
		{
			this.idxTable.remove(name);
			retVal= true;
		}
		return(retVal);
	}
	
	/**
	 * Entfernt den übergebenen entry aus allen von diesem IndexMgr verwalteten Indizes.
	 * @param entry Object - aus allen Indizes zu entfernender Eintrag
	 * @throws Exception
	 */
	public boolean removeEntry(Object entry)throws Exception
	{
		boolean retVal= false; 
		if (entry== null) 
			throw new Exception(ERR_EMPTY_ENTRY_REM);
		for (String idxName: this.idxTable.keySet())
		{
			this.idxTable.get(idxName).removeEntry(entry);
		}
		return(retVal);
	}
	
	/**
	 * In allen Indizes, die einen Slot zu der übergebenen ID enthalten wird der 
	 * entsprechende Slot entfernt.
	 * @param id Object - id des Slots in allen Indizes
	 * @throws Exception
	 */
	public boolean removeSlot(Object id)throws Exception
	{
		boolean retVal= false;
		if (id== null) 
			throw new Exception(ERR_EMPTY_ID_REM);
		for (String idxName: this.idxTable.keySet())
		{
			this.idxTable.get(idxName).removeSlot(id);
		}
		return(retVal);
	}

	

}
