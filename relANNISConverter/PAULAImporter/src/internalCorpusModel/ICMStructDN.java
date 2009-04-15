package internalCorpusModel;

public class ICMStructDN extends ICMAbstractDN 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMStructDN";		//Name dieses Tools
	private static final String VERSION=	"1.0";				//Version des ineternen KorpusModels
	
	protected String text= null;		//Textwert des Knotens
	protected Long left;				//linke Grenze des Knotens
	protected Long right;				//rechte Grenze des Knotens
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "palegreen";
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
	{ return("LEVEL_STRUCTDATA"); }
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt einen neuen Knoten vom Typ IKMStructDN. Dieser Knoten reräsentiert einen 
	 * Strukturdatenknoten. 
	 * @param name String - Name des Knotens
	 * @param text String - Textwert des Knotens 
	 * @param left long - linke Grenze des Textes
	 * @param right long - rechte Grenze des Textes
	 */
	public ICMStructDN(	String name,
						String text,
						long left,
						long right) throws Exception
	{
		super(name);
		this.text= text;
		this.left= left;
		this.right= right;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Gibt den Primärtext zurück, auf den sich dieser Knoten bezieht.
	 * @return Primärtext dieses Knotens
	 */
	public String getText() throws Exception
		{ return(this.text); }
	
	/**
	 * Gibt die linke Textgrenze des Textes zurück, auf den sich dieser Knoten bezieht.
	 * @return linke Textgrenze
	 */
	public Long getLeft() throws Exception
		{ return(this.left); }
	
	/**
	 * Gibt die rechte Textgrenze des Textes zurück, auf den sich dieser Knoten bezieht.
	 * @return rechte Textgrenze
	 */
	public Long getRight() throws Exception
		{ return(this.right); }
	
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
