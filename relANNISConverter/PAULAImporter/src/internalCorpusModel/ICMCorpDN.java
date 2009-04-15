package internalCorpusModel;

import java.util.Collection;
import java.util.Vector;

public abstract class ICMCorpDN extends internalCorpusModel.ICMAbstractDN 
{
	
	/**
	 * Farbe dieses Knotentyps als DOT-Eintrag
	 */
	protected static final String color= "black";
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"IKMKorpDN";		//Name dieses Tools
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
	{ return("LEVEL_KORPUS"); }
//	 ============================================== Konstruktoren ==============================================
	public ICMCorpDN(String name) throws Exception
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
	{ 
		String retStr= "";
		retStr= "<" + this.getName() + ">[fontcolor= white]";
		Collection<String> valueNames= null;	//Liste der Wertenamen eines Knoten
		
		valueNames= this.getAttNames();
		if (valueNames!= null)
		{
			retStr= retStr + "[shape= record, ";
			//Knoten farbig ausgeben
			if (color!= null) retStr= retStr + "color= "+color+ ",style = filled, fontcolor= white, ";	
			retStr= retStr + "label= \"{"+this.getName();
			//durch alle Values gehen
			for(String valueName :valueNames)
			{
				retStr= retStr +"|" + valueName + " = " + this.getAttValue(valueName);
			}
			retStr= retStr + "}\"]";
		}
		else
			//Knoten farbig ausgeben
			if (color!= null) retStr= retStr + "[color= "+color+ ", style = filled]";
		return(retStr);
	}
}
