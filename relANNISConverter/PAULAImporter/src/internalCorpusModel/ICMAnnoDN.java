package internalCorpusModel;

import java.util.Hashtable;

public class ICMAnnoDN extends ICMAbstractDN 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMAnnoDN";		//Name dieses Tools
	private static final String VERSION=	"1.0";				//Version des ineternen KorpusModels
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "lightblue";
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	//private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt das die Ebene zurück auf der sich dieser Knoten im Internen Korpus Model
	 * befindet.
	 * @return Ebene des interenen Korpus Models
	 */
	public static java.lang.String getDNLevel() throws Exception
	{ return("LEVEL_ANNOSDATA"); }
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt einen neuen Knoten vom Typ IKMStructDN. Dieser Knoten reräsentiert einen 
	 * Strukturdatenknoten. 
	 * @param name String - Name des Knotens
	 * @param attValPairs Hashtable<String, String> - Annotationsdaten als Attribut-Werte-Paare
	 */
	public ICMAnnoDN(	String name,
						Hashtable<String, String> attValPairs) throws Exception
	{
		super(name);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "toolname: "+ TOOLNAME + ", version: "+ VERSION+ ", object-name: "+ this.getName();
		return(retStr);
	}
	
	/**
	 * Schreibt diesen Knoten als DOT-Eintrag mit roter Knotenumrandung.
	 * @return Knoten als DOT-Eintrag
	 */
	public String toDOT() throws Exception
	{ return(this.toDOT(color)); }
}
