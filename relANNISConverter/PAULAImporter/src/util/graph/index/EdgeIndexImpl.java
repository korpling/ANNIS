package util.graph.index;


import java.util.Collection;
import java.util.Vector;

import util.graph.Edge;

/**
 * Diese Klasse implementiert das Interface EdgeIndex und stellt damit einen Index zur
 * Kantenverwaltung, also des Typs Edge zur Verfügung.
 * @author Administrator
 *
 */
public class EdgeIndexImpl extends IndexImpl implements EdgeIndex 
{
//	 ============================================== private Variablen ==============================================
	//private static final String TOOLNAME= 	"NodeIndexImpl";		//Name dieses Tools
	//private static final boolean DEBUG= false;
	
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	//private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== Konstruktoren ==============================================
	public EdgeIndexImpl(String name) throws Exception
	{
		super(name);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Gibt alle Ids zurück, die in diesem Index vorhanden sind
	 * @return Collection<String> - alle Ids dieses Indexes
	 */
	public Collection<String> getNodeIds()
	{
		Collection<String> nodeIds= new Vector<String>();
		for (Object obj: super.getIds())
		{
			nodeIds.add((String) obj);
		}
		return(nodeIds);
	}
	
	/**
	 * Diese Methode fügt diesem Index eine neue Kantehinzu. Der Eintrag wird
	 * unter dem übergebenen Identifier abgelegt und kann anschließend über diesen 
	 * identifiziert werden.
	 * @param id String - eindeutiger Identifier, der die übergebene Kante identifiziert
	 * @param entry Edge - abzulegende Kante  
	 */
	public void addEntry(String id, Edge entry) throws Exception
	{ 
		super.addEntry(id, entry);
	}
	
	/**
	 * Diese Methode gibt zurück, ob es in diesem Index mindestens einen Identifier gibt,
	 * das der übergebenen id zugeordnet ist.
	 * @param id Object - eindeutiger Identifier, der die übergebene Kante identifiziert
	 * @return true, wenn es mindestens eine Kante zu der übergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasId(String id) throws Exception
	{ return(super.hasId(id)); }
	
	/**
	 * Diese Methode gibt zurück, ob der übergebene Eintrag in diesem Index eingetragen ist. 
	 * @param entry Edge - Kante, für die geprüft werden soll, ob sie in diesem Index enthalten ist
	 * @return true, wenne s mindestens einen Eintrag zu der übergebenen is gibt
	 * @throws Exception
	 */
	public boolean hasEntry(Edge entry) throws Exception
	{ return(super.hasEntry(entry)); }
	
	/**
	 * Diese Methode entfernt alle Einträge, die mit der übergebenen id identifiziert werden.
	 * Alle Kanten zu einer id werden als slot bezeichnet.
	 * @param id String -  eindeutiger Identifier, der den zu löschenden Kante identifiziert
	 */
	public boolean removeSlot(String id) throws Exception
	{ return(super.removeSlot(id)); }
	
	/**
	 * Diese Methode entfernt die übergebene Kante aus dem Index. Ist der Slot nach dem
	 * entfernen dieses Eintrages leer, wird der ganze Slot entfernt.
	 */
	public boolean removeEntry(Edge entry) throws Exception
	{ return(super.removeEntry(entry)); }
	
	/**
	 * Diese Methode gibt eine Liste von Kanten, die von der übergebenen id 
	 * identifiziert werden zurück.
	 * @param id String - eindeutiger Identifizierer der gesuchten Kante(n) 
	 * @return Collection<Edge>, passend zu dem übergebenen Identifizierer
	 */
	public Collection<Edge> getEntry(String id) throws Exception
	{
		Collection<Edge> edges= null;
		Collection<Object> edgeObjs=  super.getEntry(id);
		if ((edgeObjs != null) && (edgeObjs.size()!= 0))
		{
			edges= new Vector<Edge>();
			for (Object obj: edgeObjs)
			{
				edges.add((Edge) obj);
			}
		}
		return(edges);
	}
	
	public String toString()
	{
		String retStr= null;
		
		retStr= "index-name: " + this.getIdxName() + ", content: " + this.idObjectTable;
		
		return(retStr);
	}
}
