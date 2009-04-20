package importer.paula.paula10.analyzer.specificAnalyzer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import importer.paula.paula10.analyzer.paulaAnalyzer.AnalyzeContainer;

/**
 * Die Klasse MetaAnnoDataAnalyzer kann Dateien vom Typ MetaAnnoData klassifizieren.
 * Dieses sind Dateien, die Annotationen von anderen Dateien über die anno.xml vornehmen.
 * @author Florian Zipser
 * @version 1.0
 */
public class MetaAnnoDataAnalyzer extends AbstractAnalyzer 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"MetaAnnoDataAnalyzer";	//Name dieses Tools
	private static final String VERSION= 	"1.0";					//Version dieses Tools
	private static final boolean DEBUG=	false;					//Debug-Schalter 
		
	private static final String anaType= "MetaAnnoData";			//Name des Analyse Typs
	private static final String comment= "analyzed by: "+ TOOLNAME + " this document contains annotations over other files, refernced  through anno.xml.";			//Kommentar zu diesem Analyse Typ
	
	private static final String[] analyzableDTDs=	{"paula_feat.dtd", "sfb632_feat.dtd"};			//DTD von der die zu analysierende Datei sein muss
	
	// alles zum Tag featList
	private static final String TAG_FL= "featlist";				//Tag-featlist
	private static final String ATT_FL_BASE= "xml:base";		//Attribut featlist.base
	
	private AnalyzeContainer aCon= null;
	
	private boolean hasTargetLinks= false;				//gibt an, ob Elemente dieses Dokumentes Links im Target-Attributwert haben 
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START_FCT=		MSG_STD + "start method: ";
	private static final String MSG_END_FCT=		MSG_STD + "end method: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_ACON=		MSG_ERR + "No analyze-container-object was given.";
	private static final String ERR_CANNOT_ANALYZE=	MSG_ERR + "Cannot analyze this document: ";
//	 ============================================== Konstruktoren ==============================================
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Erzeugt ein Objekt vom Typ MetaStructAnalyzer. Dieses kann Dateien vom Typ 
	 * MetaStructData bestimmen.
	 */
	public MetaAnnoDataAnalyzer() throws Exception
	{
		super(TOOLNAME, VERSION);
		this.hasTargetLinks= false;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Prüft ob dieser spezielle Analyser eine Datei mit den gegebenen Eigenschaften 
	 * analysieren kann.
	 */
	public boolean canAnalyze(AnalyzeContainer aCon) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "canAnalyze("+aCon+")");
		if (aCon == null) throw new Exception(ERR_NO_ACON);
		
		//wenn DTD = paula_feat.dtd oder DTD= sfb632_feat.dtd
		if (this.canAnalyzeValue(aCon.getDTD().getName(), analyzableDTDs))
			return(true);
		
		if (DEBUG) System.out.println(MSG_END_FCT + "canAnalyze()--> false");
		return(false);
	}
	
	/**
	 * Startet den AnalyseProzess und übergibt das zu beschreibende Container-Objekt.
	 * @param aCon AnalyzeContainer - ein Container-Objekt, in dem Dateiinformationen stehen und das Analyseergebniss geschrieben werden kann
	 * @exception Fehler, wenn Methode nicht überschrieben wurde
	 */
	public void startAnalyze(AnalyzeContainer aCon) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "canAnalyze("+aCon+")");
		if (!this.canAnalyze(aCon)) throw new Exception(ERR_CANNOT_ANALYZE + aCon.getPAULAFile().getAbsolutePath());
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
		if (DEBUG) System.out.println(MSG_START_FCT + "getResult()");
		
		AnalyzeContainer retCon= null;
		if (this.hasTargetLinks)
		{
			this.aCon.setAnaType(anaType);
			this.aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.META_DATA);
			this.aCon.setComment(comment);
		}
		else 
		{
			this.aCon.setAbborded();
		}
		if (DEBUG) System.out.println(MSG_END_FCT + "getResult()");
		retCon= this.aCon;
		this.hasTargetLinks= false;
		return(retCon);
	}

// --------------------------- SAX-Methoden --------------------------- 

	public void startElement(	String uri,
            					String localName,
					            String qName,
					            Attributes attributes) throws SAXException
    {
		//wenn Element featList ist
		if (qName.equalsIgnoreCase(TAG_FL))
		{
			String targetValue= null;
			//suche Attribut target
			for (int i=0; i < attributes.getLength(); i++)
			{
				if (attributes.getQName(i).equalsIgnoreCase(ATT_FL_BASE))
					targetValue= attributes.getValue(i);
			}
			if (targetValue!= null)
			{
				String link= "[a-zA-Z_0-9]*.anno.xml";
				Pattern pattern= Pattern.compile(link, Pattern.CASE_INSENSITIVE);
				Matcher matcher= pattern.matcher(targetValue);
				if (matcher.find())
				{
					this.hasTargetLinks= true;
				}
			}	
		}
    }
}
