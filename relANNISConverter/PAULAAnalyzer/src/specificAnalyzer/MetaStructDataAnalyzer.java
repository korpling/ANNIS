package specificAnalyzer;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import paulaAnalyzer.AnalyzeContainer;

public class MetaStructDataAnalyzer extends AbstractAnalyzer 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"MetaStructAnalyzer";	//Name dieses Tools
	private static final String VERSION= 	"1.0";					//Version dieses Tools
	private static final boolean DEBUG=	false;					//Debug-Schalter
	
	private static final String anaType= "MetaStructData";			//Name des Analyse Typs
	private static final String comment= "analyzed by: "+ TOOLNAME + " this document is the main structure document";			//Kommentar zu diesem Analyse Typ
	
	private static final String[] analyzableDTDs=	{"paula_struct.dtd", "sfb632_struct.dtd"};			//DTD von der die zu analysierende Datei sein muss
	private static final String subID= "[.]*annoSet[.]*";			//Teilstring, den die PAULA-ID enthalten muss um analysiert werden zu können
	
	private AnalyzeContainer aCon= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START_FCT=		MSG_STD + "start method: ";
	private static final String MSG_END_FCT=		MSG_STD + "end method: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_DTD_NOT_EXIST=	MSG_ERR + "the given dtd file does not exist.";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt ein Objekt vom Typ MetaStructAnalyzer. Dieses kann Dateien vom Typ 
	 * MetaStructData bestimmen.
	 */
	public MetaStructDataAnalyzer() throws Exception
	{
		super(TOOLNAME, VERSION);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Prüft ob die PAULA-ID den entsprechenden Teilstring enthält.
	 * @param paulaID String - Wert der PAULA-ID
	 * @param subStr String - zu suchender Substring
	 */
	private boolean checkPAULAID(String paulaID, String subStr)
	{
		Pattern pattern= Pattern.compile(subStr, Pattern.CASE_INSENSITIVE);
		Matcher matcher= pattern.matcher(paulaID);
		if (matcher.find()) return(true);
		else return(false);
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * @see AbstractAnalyzer#canAnalyze(String, String, String)
	 */
	public boolean canAnalyze(	File dtd, 
								String paulaID, 
								String paulaType) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "canAnalyze("+aCon+")");
		
		if (dtd == null)throw new Exception(ERR_EMPTY_DTD + paulaID);
		if (!dtd.exists())  throw new Exception(ERR_DTD_NOT_EXIST);
		if ((paulaID == null) || (paulaID.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_PAULAID);
		
		boolean retVal= false;
		//prüfe ob DTD zu den analysierbaren gehört
		if (!super.canAnalyzeValue(dtd.getName(), analyzableDTDs)) retVal= false;
		//prüfe ob ID zu den analysierbaren gehört
		else if (!checkPAULAID(paulaID, subID)) retVal= false;
		else retVal= true;
		
		if (DEBUG) System.out.println(MSG_END_FCT + "canAnalyze()-->"+retVal);
		
		return(retVal);
	}
	
	/**
	 * Prüft ob dieser spezielle Analyser eine Datei mit den gegebenen Eigenschaften 
	 * analysieren kann.
	 * @see AbstractAnalyzer#canAnalyze(String, String, String)
	 */
	public boolean canAnalyze(AnalyzeContainer aCon) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "canAnalyze("+aCon+")");
		boolean retVal= this.canAnalyze(aCon.getDTD(), aCon.getPAULAID(), aCon.getPAULAType());
		if (DEBUG) System.out.println(MSG_END_FCT + "canAnalyze("+aCon+")");
		return(retVal);
	}
	
	/**
	 * Startet den AnalyseProzess und übergibt das zu beschreibende Container-Objekt.
	 * @param aCon AnalyzeContainer - ein Container-Objekt, in dem Dateiinformationen stehen und das Analyseergebniss geschrieben werden kann
	 * @exception Fehler, wenn Methode nicht überschrieben wurde
	 */
	public void startAnalyze(AnalyzeContainer aCon) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "startAnalyze("+aCon+")");
		this.aCon= aCon;
		if (DEBUG) System.out.println(MSG_END_FCT + "startAnalyze()");
	}
	
	/**
	 * Wird aufgerufen, wenn der AnalyseProzess zu Ende ist, diese Methode gibt das vorher
	 * übergebene Objekt um die Eigenschaften Typ und Kommentar erweitert zurück.
	 * @exception Fehler, wenn Methode nicht überschrieben wurde
	 */
	public AnalyzeContainer getResult() throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "getResukt()");
		this.aCon.setAnaType(anaType);
		this.aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.META_STRUCT_DATA);
		this.aCon.setComment(comment);
		
		if (DEBUG) System.out.println(MSG_END_FCT + "getResukt()");
		
		return(this.aCon);
	}
}
