//*********************************************** CAuthor ******************************************
package importer.paula.paula10.util.toolDescriptor;

/**
 * Die Klasse CAuthor ist eine Struktur zum speichern des Namens und der eMail-Adresse eines 
 * Programmautors.
 * @author Flo
 * @version 1.0	
 */
public class CAuthor 
{
//	 ============================================== private Variablen ==============================================
	
	private String name= "";		//Name des Autors
	private String eMail= "";		//eMail-Adresse des Autors
	
	//	 *************************************** Meldungen ***************************************
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Instanziiert ein CAuthor Objekt
	 */
	public CAuthor()
	{}
	
	/**
	 * Instanziiert ein CAuthor Objekt und setzt den Namen und die eMail Adresse auf die
	 * übergebenen Parameter. 
	 * @param name
	 * @param eMail
	 */
	public CAuthor(String name, String eMail)
	{
		this.name= name;
		this.eMail= eMail;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	
	/**
	 * Setzt den Namen des Autors auf den übergebenen.
	 * @param name - String - Name des Autors
	 */
	public void setName(String name)
		{ this.name = name;	}
	
	/**
	 * Setzt die eMail-Adresse des Autors auf die übergebenen.
	 * @param eMail - String - eMail des Autors
	 */
	public void setEMail(String eMail)
		{ this.eMail = eMail; }
	
	/**
	 * Gibt den Namen des Autors zurück.
	 * @return Name des Autors
	 */
	public String getName()
		{ return(this.name); }
	
	/**
	 * Gibt die eMail-Adresse des Autors zurück.
	 * @return eMail-Adresse des Autors
	 */
	public String getEMail()
		{ return(this.eMail); }
	
	public CAuthor clone()
	{
		CAuthor author = new CAuthor(this.name, this.eMail);
		return(author);
	}
	
	/**
	 * Gibt dieses CAuthor-Objekt als String zurück.
	 * @return CAuthor als String
	 */
	public String toString()
	{
		String retStr= "";
		retStr= "name:\t" + this.name + "\teMail:\t" + this.eMail;
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	
}
