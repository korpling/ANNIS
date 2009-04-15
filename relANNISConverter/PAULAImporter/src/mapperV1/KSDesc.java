package mapperV1;

import java.util.Vector;

/**
 * Die Klasse KSDesc (Korpus Structure Description) ist eine Objekt-Repräsentation einer 
 * Korpus-Typ-Datei. Sie enthält KSDesc Objekt und PDDesc-Objekte. 
 *  
 * @author Florian Zipser
 * @version 1.0
 */
public class KSDesc 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"KSDesc";		//Name dieses Tools
	
	/**
	 * Name dieses Korpus
	 */
	private String name= null;
	/**
	 * Liste von PDDesc-Objekten
	 */
	private Vector<PDDesc> pdDescList= null;
	
	/**
	 * Liste von KSDesc-Objekten
	 */
	private Vector<KSDesc> ksDescList= null;
	
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_KORP_NAME=		MSG_ERR + "The given Korpusname is empty.";
	private static final String ERR_KORPUS_EXISTS=	MSG_ERR + "The Korpus with the given Name already exists in this korpus: ";
//	 ============================================== Konstruktoren ==============================================
	public KSDesc(String name) throws Exception
	{
		if ((name== null) || (name.equalsIgnoreCase(""))) throw new Exception(ERR_KORP_NAME);
		this.name= name;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Setzt die interne Liste von PDDesc-Objekten auf die übergebene.
	 * @param pdDescList Vector<PDDesc> - Liste von PDDesc-Objekten
	 */
	public void setPDList(Vector<PDDesc> pdDescList)
	{
		this.pdDescList= pdDescList; 
	}
	
	/**
	 * Fügt ein neues KSDesc-Objekt hinten an die interne Liste der KSDesc-Obkjekte an.
	 * @param ksDesc KSDesc - neues anzufügendes Korpusobjekt
	 */
	public void addKorpus(KSDesc ksDesc) throws Exception
	{
		if (this.ksDescList == null) this.ksDescList= new Vector<KSDesc>();
		for (KSDesc oldKSDesc: this.ksDescList)
		{
			if (oldKSDesc.getName().equalsIgnoreCase(ksDesc.getName()))
				throw new Exception(ERR_KORPUS_EXISTS + oldKSDesc.getName());
		}
		this.ksDescList.add(ksDesc);
	}
	
	/**
	 * Gibt den Namen diese Korpus zurück.
	 * @return Name dieses Korpus
	 */
	public String getName()
	{return(this.name); }
	
	/**
	 * Gibt die in diesem Objekt entahletenen KSDesc-Objekte zurück.
	 * @return Liste von KSDesc-Objekten, die dieses Objekt enthält, bzw null, wenn die Liste leer
	 */
	public Vector<KSDesc> getKSList()
		{return(this.ksDescList);}
	
	/**
	 * Gibt die in diesem Objekt entahletenen PDDesc-Objekte zurück.
	 * @return Liste von PDDesc-Objekten, die dieses Objekt enthält, bzw null, wenn die Liste leer
	 */
	public Vector<PDDesc> getPDDescList()
		{return(this.pdDescList);}
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		 retStr= "this method isn´t implemented";
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	


}
