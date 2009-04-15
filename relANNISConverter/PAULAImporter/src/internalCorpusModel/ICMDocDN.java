package internalCorpusModel;

public abstract class ICMDocDN extends internalCorpusModel.ICMAbstractDN 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMKorpDN";		//Name dieses Tools
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "gray52";
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt das die Ebene zurück auf der sich dieser Knoten im Internen Korpus Model
	 * befindet.
	 * @return Ebene des interenen Korpus Models
	 */
	public static java.lang.String getDNLevel() throws Exception
	{ return("LEVEL_DOCUMENT"); }
//	 ============================================== Konstruktoren ==============================================
	public ICMDocDN(String name) throws Exception
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
		 retStr= "this method isn´t implemented";
		return(retStr);
	}
	
	/**
	 * Schreibt diesen Knoten als DOT-Eintrag mit roter Knotenumrandung.
	 * @return Knoten als DOT-Eintrag
	 */
	public String toDOT() throws Exception
	{ return(this.toDOT(color)); }
}
