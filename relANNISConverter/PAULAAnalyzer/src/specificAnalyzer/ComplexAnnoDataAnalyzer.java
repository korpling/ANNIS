package specificAnalyzer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import paulaAnalyzer.AnalyzeContainer;

/**
 * Die Klasse ComplexAnnoDataAnalyzer kann Dateien vom Typ ComplexAnnoData klassifizieren.
 * Das sind komplexere Annotationsdateien, in diesem Fall ausschlie�lich Anaphor-Antecedent
 * Strukturen und strukturell Vergleichbares.
 * @author Florian Zipser
 * @version 1.0
 */
public class ComplexAnnoDataAnalyzer extends AbstractAnalyzer 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"ComplexAnnoDataAnalyzer";	//Name dieses Tools
	private static final String VERSION= 	"1.0";					//Version dieses Tools
	private static final boolean DEBUG=	false;					//Debug-Schalter 
	
	// alles zum Tag feat
	private static final String TAG_FEAT= "feat";				//Tag-feat
	private static final String ATT_FEAT_TARGET= "target";		//Attribut feat.target
	
	private static final String anaType= "ComplexAnnoData";			//Name des Analyse Typs
	private static final String comment= "analyzed by: "+ TOOLNAME + " this document contains complex annotations over token and structural nodes. Complex annotations means merging of structure and annotation like anaphor-antecedent structures.";			//Kommentar zu diesem Analyse Typ
	
	private static final String[] analyzableDTDs=	{"paula_feat.dtd", "sfb632_feat.dtd"};			//DTD von der die zu analysierende Datei sein muss
	
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
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Erzeugt ein Objekt vom Typ MetaStructAnalyzer. Dieses kann Dateien vom Typ 
	 * MetaStructData bestimmen.
	 */
	public ComplexAnnoDataAnalyzer() throws Exception
	{
		super(TOOLNAME, VERSION);
		this.hasTargetLinks= false;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Pr�ft ob dieser spezielle Analyser eine Datei mit den gegebenen Eigenschaften 
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
	 * Startet den AnalyseProzess und �bergibt das zu beschreibende Container-Objekt.
	 * @param aCon AnalyzeContainer - ein Container-Objekt, in dem Dateiinformationen stehen und das Analyseergebniss geschrieben werden kann
	 * @exception Fehler, wenn Methode nicht �berschrieben wurde
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
	 * �bergebene Objekt um die Eigenschaften Typ und Kommentar erweitert zur�ck.
	 * @exception Fehler, wenn Methode nicht �berschrieben wurde
	 */
	public AnalyzeContainer getResult() throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "getResult()");
		
		AnalyzeContainer retCon= null;
		if (this.hasTargetLinks)
		{
			this.aCon.setAnaType(anaType);
			this.aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			this.aCon.setComment(comment);
		}
		else 
		{
			this.aCon.setAbborded();
		}
		if (DEBUG) System.out.println(MSG_END_FCT + "getResult(): "+aCon.getAnaType());
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
		
		//wenn Element feat ist
		if (qName.equalsIgnoreCase(TAG_FEAT))
		{
			String targetValue= null;
			//suche Attribut target
			for (int i=0; i < attributes.getLength(); i++)
			{
				if (attributes.getQName(i).equalsIgnoreCase(ATT_FEAT_TARGET))
					targetValue= attributes.getValue(i);
			}
			if (targetValue!= null)
			{
				String link= "#[a-zA-Z_0-9][0-9]*";
				//System.out.println("TargetVal: "+targetValue);
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
