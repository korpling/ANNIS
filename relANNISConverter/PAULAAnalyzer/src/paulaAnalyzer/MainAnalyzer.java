package paulaAnalyzer;

import java.io.File;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

import specificAnalyzer.AbstractAnalyzer;
import util.depGraph.FileDepGraph;

/**
 * Diese Klasse ist der Hauptanalyzer des Tools PAULAAnalyzer. Ein Objekt dieser Klasse parst
 * eine PAULA-Datei und gibt sie in entsprechenden H�ppchen an die specificAnalyzer weiter.
 * Zur Kommunikation mit dem PAULAAnalyzer, sowie den specificAnalyzern diesnt ein Objekt der
 * Klasse AnalyzeContainer
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public class MainAnalyzer extends DefaultHandler2 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"MainAnalyzer";		//Name dieses Tools
	
	//private static final String KW_XML_ENDING= ".xml";			//Endung einer XML-Datei
	//Elementname und Attribute des Elements paula
	private static final String[] TAG_ENV=		{"paula", "sfb632_standard"};	//Name des �u�ersten TAGs (Envelope)
	
	/**
	 * Logger
	 * @generated NOT
	 */
	private Logger logger= Logger.getLogger(MainAnalyzer.class);
	
	//Elementname und Attribute des Elements header
	private static final String[] TAG_HEAD=			{"header"};						//Name des Kopfelements	
	private static final String[] ATT_PAULA_ID=		{"paula_id", "sfb_id"};			//Name des Attributs der paula_id
	private static final String[] ATT_PAULA_TYPE= 	{"type"};						//Name des Attributs des PAULA-Typs
	
	/**
	 * path seperator for the file system
	 */
	private static final String KW_PATH_SEP=		"/";
	
	private Locator docLocator= null;				//Locator f�r die zu parsende XML-Datei
	private Vector<AnalyzerParams> aParams= null;	//Liste der specificAnalyzers
	private Vector<AnalyzerParams> matchingAnalyzer= null;	//Liste der aktuell passenden Analyzer
	
	private FileDepGraph depGraph= null;			//Abh�ngigkeitsgraph f�r PAULA-Dateien
	private Vector<String> depFiles= new Vector<String>();	//Liste der abh�ngigen Dateien
	
	//private Logger logger= null;					//logger f�r log4j
	private AnalyzeContainer currACon= null;		//aktueller AnalyzeContainer
	private AnalyzeContainer bestACon= null;		//bester ermittelter AnalyzeContainer
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_WARN=			"WARN(" +TOOLNAME+ "):\t";
	private static final String MSG_INIT=			MSG_STD + "object is initialized with: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String WARN_NO_ANALYZER=		MSG_WARN + "No matching specific analyzer was found for file: ";
	
	private static final String ERR_NO_APRAMS=			MSG_ERR + "No list with AnalyzerParams given.";
	private static final String ERR_EMPTY_ACON=			MSG_ERR + "The given AnalyzeContainer-object is empty.";
	private static final String ERR_XML_EXCEPT=			MSG_ERR + "An xml error has occured in file: ";
	private static final String ERR_NO_ANALYZER=		MSG_ERR + "No matching specific analyzer was found for file: ";
	private static final String ERR_NO_DEPGRAPH=		MSG_ERR + "Non initialized dependeny graph. The graph has to be initialized.";
	private static final String ERR_NO_ANALYZE_TYPE=	MSG_ERR + "The Analyzer gives back no type.";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein MainAnalyzer Objekt und legt die Liste aller vorhandenen Analyzer ab.
	 * @param aParams Vector<AnalyzerParams> - Liste aller verf�gbaren specificAnalyzer
	 * @param logger Logger - Logger f�r log4j 
	 */
	public MainAnalyzer(	Vector<AnalyzerParams> aParams) throws Exception
	{
		if ((aParams== null) || (aParams.isEmpty())) throw new Exception(ERR_NO_APRAMS);
		
		this.aParams= aParams;
		
		String debStr= MSG_INIT;
		for (AnalyzerParams aParam: aParams)
			debStr=  debStr + "\n" + aParam;
		this.logger.debug(debStr);
	}
//	 ============================================== protected Methoden ==============================================
	/**
	 * Vergleicht ob der zweite Stringwert gleich dem ersten ist. Der Vergleich ist
	 * Case-Insensitiv. Verglichen wird �ber String.equalsIgnoreCase()
	 * @param str1 String - erster Vergleichswert
	 * @param str2 String - zweiter Vergleichswert
	 * @return true, wenn Strings gleich, false sonst
	 */
	protected boolean compare(String str1, String str2)
		{ return(str1.equalsIgnoreCase(str2)); }
	
	/**
	 * Vergleicht ein String-Array und einen String. Ist der String in dem Array enthalten, 
	 * so wird true zur�ckgegeben. Dabei wird nicht case-sensitiv verglichen. F�r den 
	 * Vergleich wird die Methode compare(String str1, String str2) benutzt.
	 * @param str1 String - erster Vergleichsert
	 * @param list String[] - Liste mit Vergleichswerten
	 * @return true, wenn erster Vergleichswert in der liste vorhanden ist.
	 */
	protected boolean compare(String str1, String[] list)
	{
		for (String str2: list)
			if (compare(str1, str2)) return(true);
		return(false);
	}
	
	/**
	 * Ruft alle bisher aufgerufenen Sax-Methoden in dem �bergebenen spetiellen Analyzer 
	 * auf. Dies geschieht, damit dieser ggf. auf die Ereignisse reagieren kann.
	 * @param aParam AnalyzerParams - Parameter, der den Analyzer enth�lt
	 */
	protected void invokeSAXMethods(AnalyzerParams aParam)
	{
		//SAX-Methoden in dem specificAnalyzer aufrufen
		AbstractAnalyzer analyzer= aParam.getAnalyzer();
		//DocumentLocator setzen
		analyzer.setDocumentLocator(this.docLocator);	
	}
	
	/**
	 * Sucht in einem String mehrere Teilstrings mit der Endung .xml
	 * TODO diese Methode sollte in eine allgemeine XPointer-Klasse ausgelagert werden
	 * @param chkStr - String der zu �berpr�fende String
	 * @return Dateiname, wenn ein Dateiname gefunden wurde, null sonst
	 */
	protected Vector<String> searchXMLFiles(String chkStr)
	{
		Vector<String> retStr= new Vector<String>();
		
		//spezielle Behandlung, wenn XLink eine Sequenz von Dateien ist
		if ((chkStr.contains("(")) && (chkStr.contains(",")) && (chkStr.contains("(")))
		{
			chkStr= chkStr.replace("(", "");
			chkStr= chkStr.replace(")", "");
			String sep= ",";
			String[] parts= chkStr.split(sep);
			for (String part: parts)
			{
				retStr.add(this.searchXMLFile(part));
			}
		}
		//es gibt keine Sequenz
		else 
		{
			String depFile= this.searchXMLFile(chkStr);
			if ((depFile!= null) && (!depFile.equalsIgnoreCase("")))
				retStr.add(depFile);
		}
		/*if (!retStr.isEmpty())
		{
			System.out.println(chkStr);
			System.out.println(retStr);
		}*/
		return(retStr);
	}
	
	/**
	 * Sucht in einem String einen Teilstring mit der Endung .xml
	 * TODO diese Methode sollte in eine allgemeine XPointer-Klasse ausgelagert werden
	 * @param chkStr - String der zu �berpr�fende String
	 * @return Dateiname, wenn ein Dateiname gefunden wurde, null sonst
	 */
	protected String searchXMLFile(String chkStr)
	{
		String retStr= null;
		String sep= "#";
		//�bergebenen String bei Seperator # trennen
		String[] parts= chkStr.split(sep);
		//nur der erste Teil kann ein Dateiname sein
		if (parts.length > 0) chkStr= parts[0];
		
		String xmlEnding= "[.]*.xml";
		Pattern pattern= Pattern.compile(xmlEnding, Pattern.CASE_INSENSITIVE);
		Matcher matcher= pattern.matcher(chkStr);
		if (matcher.find()) retStr= chkStr;
		
		return(retStr);
	}
//	 ============================================== �ffentliche Methoden ==============================================
	/**
	 * Analysiert eine Datei in einem AnalyzeContainer mit den daf�r passenden specificAnalyzern.
	 * Der AnalyseContainer wird mit dem AnalyseTyp und einem Kommentar beschrieben und zur�ckgegeben.
	 * @param aCon AnalyzeContainer - Die zu analysierende Datei
	 */
	public AnalyzeContainer analyze(	AnalyzeContainer aCon, 
										FileDepGraph depGraph) throws Exception
	{
		if (aCon== null) throw new Exception(ERR_EMPTY_ACON);
		if (depGraph==  null) throw new Exception(ERR_NO_DEPGRAPH);
		//Abh�ngigkeitsgraphen f�r PAULA-Dateien setzen
		this.depGraph= depGraph;
		
		//besten ermittelten AnalyzeContainer zur�cksetzen
		this.bestACon= null;
		this.currACon= aCon;
		//Dateien, von deenen die aktuelle abh�ngt zur�cksetzen
		this.depFiles.clear();
		
		//paula_datei aus dem AnalyzeContainer parsen
        try
        {
        	SAXParser parser;
            XMLReader reader;
            
            final SAXParserFactory factory= SAXParserFactory.newInstance();
            parser= factory.newSAXParser();
            reader= parser.getXMLReader();

            //contentHandler erzeugen und setzen
            reader.setContentHandler(this);
            reader.setDTDHandler(this);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes",true);
            
            //TODO Ausgabe lieber Trace
            if (this.logger!= null) this.logger.debug("starting with parsing paula file: " + aCon.getPAULAFile().getName());
                
            //XML-konforme Datei durch reader parsen 
            reader.parse(aCon.getPAULAFile().getCanonicalPath());
            
            if (this.logger!= null) this.logger.debug("ending with parsing paula file: " + aCon.getPAULAFile().getName());
            
            //besten analyzeContainer mit seinen Abh�ngigkeiten in den Abh�ngigkeitsgraphen eintragen
            this.depGraph.addNode(this.bestACon, this.depFiles);
            
            return(this.bestACon);
        }
	 	catch (SAXParseException e1)
	 		{ throw new Exception(ERR_XML_EXCEPT+ e1.getMessage()); }
	}
	
	/**
	 * Gibt Informationen �ber dieses Objekt als String zur�ck. 
	 * @return String - Informationen �ber dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		
		return(retStr);
	}
//	 ============================================== Sax Methoden ==============================================	
	/**
	 * F�gt die DTD zu der aktuellen PAULA-Datei in das Analyze-Container Objekt.
	 */
	public void startDTD(String name, String publicId, String systemId) throws SAXException
	{
		try
		{	this.currACon.setDTD(new File(this.currACon.getPAULAFile().getParent() +"/" +systemId)); }
		catch (Exception e)
			{ throw new SAXException(e.getMessage()); }
	}
	
	/**
	 * Setzt den �bergebenen DocumentLocator, so dass er sp�ter weitergegeben werden kann.
	 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(Locator)
	 */
	public void setDocumentLocator(Locator locator)
		{ this.docLocator= locator; }
	
	/**
	 * 
	 */
	public void startElement(	String uri,
								String localName,
					            String qName,
					            Attributes attributes) throws SAXException
	{
		try
		{
			//verschiedene Elementknoten durchgehen
			//Elementknoten ist der PaulaUmschlag
			if (this.compare(qName, TAG_ENV))
			{
				//nichts bekannt
			}
			//Elementknoten ist das Kopfelement
			else if (this.compare(qName, TAG_HEAD))
			{
				//auslesen der Attribute des Header-Elementes
				for (int i= 0; i < attributes.getLength(); i++)
				{
					//Attribut ist PAULA-ID
					if (this.compare(attributes.getQName(i), ATT_PAULA_ID))
						this.currACon.setPAULAID(attributes.getValue(i));
					//Attribut ist PAULA-Type
					if (this.compare(attributes.getQName(i), ATT_PAULA_TYPE))
						this.currACon.setPAULAType(attributes.getValue(i));
				}
				
				//m�gliche specificAnalyzer ermitteln
				//Liste aktuell passender Analyzer
				this.matchingAnalyzer= new Vector<AnalyzerParams>();	
				for (AnalyzerParams aParam: this.aParams)
				{
					//Kopie des AnalyzeContainers erstellen
					AnalyzeContainer newACon= this.currACon.clone2();
					//wenn Analyzer auf Paula-Datei passt
					if (aParam.getAnalyzer().canAnalyze(newACon))
					{
						//Setzt die Reihenfolge, die durch den Analyzer vorgegeben wird
						newACon.setOrder(aParam.getOrder());
						aParam.getAnalyzer().startAnalyze(newACon);
						//Analyzer in die Liste der m�glichen schreiben
						this.matchingAnalyzer.add(aParam);
						this.invokeSAXMethods(aParam);
						if (this.logger!= null) this.logger.debug("possible specific analyzer: "+ aParam.getName());
					}
				}
				//wenn kein passender Analyzer gefunden wurde
				if (matchingAnalyzer.size() == 0)
				{
					//wenn geloggt werden kann dann warning ausgeben
					//TODO hier sollte eigentlich logger.warn hin
					if (this.logger!= null) this.logger.error(WARN_NO_ANALYZER + this.currACon.getPAULAFile().getCanonicalPath());
					
					throw new Exception(ERR_NO_ANALYZER + this.currACon.getPAULAFile().getCanonicalPath());
				}
			}
			//Elementknoten irgendein Element --> weiterreichen
			else 
			{
				//durch alle aktuellen Analyzer gehen
				for (AnalyzerParams aParam: this.matchingAnalyzer)
					aParam.getAnalyzer().startElement(uri, localName, qName, attributes);
			}
			
			
			//pr�fe Abh�ngigkeiten zu anderen Dateien (diese stehen in den Attributwerten)
			for(int i= 0; i < attributes.getLength(); i++)
			{
				//TODO: muss ge�ndert werden, Link-Attribute sollten nicht nach Namen, sondern nach NS bestimmt werden
				String currPath= this.currACon.getPAULAFile().getParentFile().getCanonicalPath() + KW_PATH_SEP;
				if (attributes.getQName(i).equalsIgnoreCase("xml:base"))
				{
					this.depFiles.add(currPath + attributes.getValue(i));
				}
				else if (attributes.getQName(i).equalsIgnoreCase("xlink:href"))
				{
					Vector<String> depFiles= searchXMLFiles(attributes.getValue(i));
					if ((depFiles != null) && (!depFiles.isEmpty()))
					{
						for (String depFile: depFiles)
						{
							if ((depFile!= null) && (!depFile.equalsIgnoreCase("")))
							{	
								//wenn Datei auf sich selber zeigt, dann Link nicht einf�gen
								if (this.currACon.getPAULAFile().getName().equalsIgnoreCase(depFile))
								{
									//System.out.println(this.currACon.getPAULAFile().getName() + "zeigt auf sich selbst");
								}
								else this.depFiles.add(currPath + depFile);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
			{
			e.printStackTrace();
			throw new SAXException(e.getMessage()); }
	}
		
	/**
	 * Wird aufgerufen wenn das Ende der zu analysierenden PAULA-Datei erreicht wurde. Aus
	 * allen m�glichen AnalyzeErgebnissen wird das beste ausgesucht und als einziges bestimmt.
	 * Dies geschieht aufgrund der Priorit�t, die ein Analyzer bekommen hat. Hat er der h�chst-
	 * priore Analyzer ein Ergebniss gefunden wird dies genommen. Wenn nicht, dann wird
	 * das Ergebniss der absteigenden Priorit�t nach gew�hlt.
	 * @see org.xml.sax.ext.DefaultHandler2#endDocument()
	 */
	public void endDocument() throws SAXException
	{
		try
		{
			//durch alle aktuellen Analyzer gehen
			for (AnalyzerParams aParam: this.matchingAnalyzer)
				aParam.getAnalyzer().endDocument();
			AnalyzerParams bestAnalyzer= null;		//bester Analyzer der gefunden wurde
			if (this.logger!= null) this.logger.debug(MSG_STD + "number of analyzers: " + this.matchingAnalyzer.size());
			//alle m�glichen Analyzer durchgehen
			for (AnalyzerParams aParam: this.matchingAnalyzer)
			{
				AnalyzeContainer aCon= aParam.getAnalyzer().getResult();
				//Analyzer ist interessant, wenn er ein Ergebniss hat
				if ((aCon.getStatus() == AnalyzeContainer.STATUS.TYPED)||
						(aCon.getStatus() == AnalyzeContainer.STATUS.COMMENTED))
				{
					//wenn bester Analyzer null oder schlechter (hei�t geringere Priorit�t)  
					if((bestAnalyzer== null) || (bestAnalyzer.getPriority() <= aParam.getPriority()))
					{
						//neuer Analyzer ist der Bessere, also dessen AnalyzeContainer setzen
						bestAnalyzer= aParam;
						this.bestACon= aCon;
					}
				}
			}
		}
		catch (NullPointerException NPEx)
			{ throw new SAXException(ERR_NO_ANALYZE_TYPE); }
		catch (Exception e)
			{ throw new SAXException(e.getMessage()); }
		
		this.currACon= null;
		this.matchingAnalyzer= null;
	}
}
