package specificAnalyzer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import paulaAnalyzer.AnalyzeContainer;

/**
 * Die Klasse AudioDataAnalyzer kann Dateien vom Typ annoData die auf Audio-
 * Dateien verweisen klassifizieren.
 * @author Florian Zipser
 * @version 1.0
 */
public class AudioDataAnalyzer extends AbstractAnalyzer 
{
//		 ============================================== private Variablen ==============================================
		private static final String TOOLNAME= 	"AudioDataAnalyzer";	//Name dieses Tools
		private static final String VERSION= 	"1.0";					//Version dieses Tools
		private static final boolean DEBUG=	false;					//Debug-Schalter 
		
		private static final String KW_ATT_VAL_AUDIO= "audioFile";			//Attributwert, der ein Dokument als Tok typisiert
		
		private boolean canAnalyze= false;		//gibt an, ob ein Dokument mit diesem ANalyser richtig analysiert werden kann (nach dem lesen)
		
		private static final String anaType= "AudioData";			//Name des Analyse Typs
		private static final String comment= "analyzed by: "+ TOOLNAME + " this document contains annotations over token and structural nodes.";			//Kommentar zu diesem Analyse Typ
		
		private static final String[] analyzableDTDs=	{"paula_feat.dtd", "sfb632_feat.dtd"};			//DTD von der die zu analysierende Datei sein muss
		private AnalyzeContainer aCon= null;
		//	 *************************************** Meldungen ***************************************
		private static final String MSG_STD=			TOOLNAME + ">\t";
		private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
		private static final String MSG_START_FCT=		MSG_STD + "start method: ";
		private static final String MSG_END_FCT=		MSG_STD + "end method: ";
		//	 *************************************** Fehlermeldungen ***************************************
		private static final String ERR_NO_ACON=		MSG_ERR + "No analyze-container-object was given.";
		private static final String ERR_CANNOT_ANALYZE=	MSG_ERR + "Cannot analyze this document: ";
//		 ============================================== Konstruktoren ==============================================
//		 ============================================== private Methoden ==============================================
//		 ============================================== öffentliche Methoden ==============================================
		/**
		 * Erzeugt ein Objekt vom Typ MetaStructAnalyzer. Dieses kann Dateien vom Typ 
		 * MetaStructData bestimmen.
		 */
		public AudioDataAnalyzer() throws Exception
		{
			super(TOOLNAME, VERSION);
		}
//		 ============================================== private Methoden ==============================================
//		 ============================================== öffentliche Methoden ==============================================
		/**
		 * Prüft ob dieser spezielle Analyser eine Datei mit den gegebenen Eigenschaften 
		 * analysieren kann.
		 */
		public boolean canAnalyze(AnalyzeContainer aCon) throws Exception
		{
			if (DEBUG) System.out.println(MSG_START_FCT + "canAnalyze("+aCon+")");
			boolean retVal= false;
			if (aCon == null) throw new Exception(ERR_NO_ACON);
			
			//wenn DTD = paula_feat.dtd oder DTD= sfb632_feat.dtd
			if (this.canAnalyzeValue(aCon.getDTD().getName(), analyzableDTDs))
				retVal= true;
			
			if (DEBUG) System.out.println(MSG_END_FCT + "canAnalyze()--> "+ retVal);
			return(retVal);
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
			
			this.aCon.setAnaType(anaType);
			this.aCon.setAbsAnaType(AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA);
			//wenn dieser der richtige Analyser war
			if (this.canAnalyze)
				this.aCon.setComment(comment);
			else this.aCon.setAbborded();
			if (DEBUG) System.out.println(MSG_END_FCT + "getResult()");
			this.canAnalyze= false;
			return(this.aCon);
		}
// -------------------------- Start SAX-Methoden --------------------------
		public void startElement(	String uri,
						            String localName,
						            String qName,
						            Attributes attributes) throws SAXException
		{
			try
			{
				//Element FEATLIST gefunden
				if (this.canAnalyzeValue(qName, TAG_FEAT_FEATLIST))
				{
					for (int i= 0; i < attributes.getLength(); i++)
					{
						//Attribut FEATLIST.TYPE gefunden
						if (this.canAnalyzeValue(attributes.getQName(i), ATT_MARK_MARK_TYPE))
						{
							//TYPE= AUDIO
							if (attributes.getValue(i).equalsIgnoreCase(KW_ATT_VAL_AUDIO))
							{
								this.canAnalyze= true;
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				if (DEBUG) e.printStackTrace();
				throw new SAXException(ERR_STD_XML + e.getMessage());
			}
		}
// -------------------------- Ende SAX-Methoden --------------------------
}