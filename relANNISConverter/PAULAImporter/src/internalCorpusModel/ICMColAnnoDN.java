package internalCorpusModel;

import java.util.Hashtable;

/**
 * Ein IKMColAnnoDN stellt eine Annotation einer ganzen Collection dar. Diese
 * COllection muss ein Subtyp der Klasse IKMCollectionDN sein.
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class ICMColAnnoDN extends ICMAbstractDN 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMColAnnoDN";		//Name dieses Tools
	private static final String VERSION=	"1.0";				//Version des ineternen KorpusModels
	
	Hashtable<String, String> attValPairs= null;			//Attribut-Werte-Paare, die die ANnotations darstellen
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "dodgerblue";
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_ANNOS=			MSG_ERR + "Cannot create "+TOOLNAME+"-object, because there are no annotations given.";
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt das die Ebene zurück auf der sich dieser Knoten im Internen Korpus Model
	 * befindet.
	 * @return Ebene des interenen Korpus Models
	 */
	public static java.lang.String getDNLevel() throws Exception
	{ return("LEVEL_COLANNODATA"); }
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt einen neuen Knoten vom Typ IKMCollectionDN. Dieser Knoten reräsentiert einen 
	 * Strukturdatenknoten. 
	 * @param name String - Name des Knotens
	 * @param attValPairs - Attribut-Werte-Paare, die die ANnotations darstellen
	 */
	public ICMColAnnoDN(	String name,
							Hashtable<String, String> attValPairs) throws Exception
	{
		super(name);
		this.attValPairs= attValPairs;
		if ((attValPairs== null) || (attValPairs.isEmpty())) throw new Exception(ERR_NO_ANNOS);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== protected Methoden ==============================================
	protected Hashtable<String, String> getAttValPairs()
		{ return(this.attValPairs); }
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
