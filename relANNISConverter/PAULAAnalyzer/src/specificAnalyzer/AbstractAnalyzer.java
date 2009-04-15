package specificAnalyzer;

import org.xml.sax.Locator;
import org.xml.sax.ext.DefaultHandler2;

import paulaAnalyzer.AnalyzeContainer;

public abstract class AbstractAnalyzer extends DefaultHandler2
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"AbstractAnalyzer";		//Name dieses Tools
	private static final String VERSION= 	"1.0";					//Version dieses Tools
	private static final boolean DEBUG=	false;					//Debug-Schalter
	
	protected static final String KW_NAME= "analyzer name";			//Schlüsselwort für den Analyzer name
	protected static final String KW_VERSION= "version";				//Schlüsselwort für die Version
	
//	Spezifizierung der PAULA-Tags und Paula-Attribute
	//Generelle Spezifizierung (TAG oder ATT)_(Name der dtd)_(Name des Tags)_(Name des Attributs wenn es sich um ATT handelt)
	//allgemeine Tags und Attribute für alle PAULA-Dokumente (Header)
	protected static final String[] TAG_HEADER= {"header"};				//Tagname des Tags header
	protected static final String[] ATT_HEADER_PAULA_ID= {"paula_id", "sfb_id"};	//Attributname des Attributes header.paula_id
	protected static final String[] ATT_HEADER_ID=	{"id"};				//Attributname des Attributes header.id
	protected static final String[] ATT_HEADER_TYPE=	{"type"};				//Attributname des Attributes header.id
	
	//Tags und Attribute für Dateien vom paulatyp TEXT(text.dtd)
	protected static final String[] TAG_TEXT_BODY= {"body"};		//Tagname des Tags body
	
	//Tags und Attribute für Dateien vom paulatyp MARK(mark.dtd)
	protected static final String[] TAG_MARK_MARKLIST= 	{"marklist"};			//Tagname des Tags markList
	protected static final String[] ATT_MARK_MARKLIST_BASE= 	{"xml:base"};	//Attributname des Attributs markList.base	
	protected static final String[] ATT_MARK_MARKLIST_TYPE= 	{"type"};		//Attributname des Attributs markList.type
	
	protected static final String[] TAG_MARK_MARK= 			{"mark"};		//Tagname des Tags mark
	protected static final String[] ATT_MARK_MARK_ID= 		{"id"};			//Attributname des Attributs mark.id
	protected static final String[] ATT_MARK_MARK_HREF= 	{"xlink:href"};		//Attributname des Attributs mark.href
	protected static final String[] ATT_MARK_MARK_TYPE= 	{"type"};		//Attributname des Attributs mark.type
	
	//Tags und Attribute für Dateien vom paulatyp STRUCT(struct.dtd)
	protected static final String[] TAG_STRUCT_STRUCTLIST= 			{"structlist"};		//Tagname des Tags structList
	protected static final String[] ATT_STRUCT_STRUCTLIST_BASE= 	{"xml:base"};		//Attributname des Attributs structList.base	
	protected static final String[] ATT_STRUCT_STRUCTLIST_TYPE= 	{"type"};			//Attributname des Attributs structList.type
	
	protected static final String[] TAG_STRUCT_STRUCT= 			{"struct"};		//Tagname des Tags struct
	protected static final String[] ATT_STRUCT_STRUCT_ID= 		{"id"};			//Attributname des Attributs struct.id
	
	protected static final String[] TAG_STRUCT_REL= 		{"rel"};			//Tagname des Tags rel
	protected static final String[] ATT_STRUCT_REL_ID= 		{"id"};				//Attributname des Attributs rel.id
	protected static final String[] ATT_STRUCT_REL_HREF= 	{"xlink:href"};		//Attributname des Attributs rel.href
	protected static final String[] ATT_STRUCT_REL_TYPE= 	{"type"};			//Attributname des Attributs rel.type
	
	//Tags und Attribute für Dateien vom paulatyp FEAT(feat.dtd)
	protected static final String[] TAG_FEAT_FEATLIST= 		{"featlist"};		//Tagname des Tags featList
	protected static final String[] ATT_FEAT_FEATLIST_BASE= {"xml:base"};		//Attributname des Attributs featList.base	
	protected static final String[] ATT_FEAT_FEATLIST_TYPE= {"type"};			//Attributname des Attributs featList.type
	
	//Tags und Attribute für Dateien vom paulatyp FEAT(feat.dtd)
	protected static final String[] TAG_FEAT_FEAT= 		{"feat"};			//Tagname des Tags feat
	protected static final String[] ATT_FEAT_FEAT_ID= 	{"id"};				//Attributname des Attributs feat.id
	protected static final String[] ATT_FEAT_FEAT_HREF= {"xlink:href"};		//Attributname des Attributs feat.href
	protected static final String[] ATT_FEAT_FEAT_TAR= 	{"target"};			//Attributname des Attributs feat.target
	protected static final String[] ATT_FEAT_FEAT_VAL= 	{"value"};			//Attributname des Attributs feat.value
	protected static final String[] ATT_FEAT_FEAT_DESC= {"description"};	//Attributname des Attributs feat.description
	protected static final String[] ATT_FEAT_FEAT_EXP= 	{"example"};		//Attributname des Attributs feat.example
	
	//Tags und Attribute für Dateien vom paulatyp MULTIFEAT(multi.dtd)
	protected static final String[] TAG_MULTI_MULTIFEATLIST= 		{"multifeatlist"};		//Tagname des Tags featList
	protected static final String[] ATT_MULTI_MULTIFEATLIST_BASE= 	{"xml:base"};			//Attributname des Attributs featList.base	
	protected static final String[] ATT_MULTI_MULTIFEATLIST_TYPE= 	{"type"};				//Attributname des Attributs featList.type
	
	//Tags und Attribute für Dateien vom paulatyp MULTIFEAT(feat.dtd)
	protected static final String[] TAG_MULTI_FEATLIST= 		{"featList"};		//Tagname des Tags feat
	protected static final String[] ATT_MULTI_FEATLIST_HREF= 	{"xlink:href"};		//Attributname des Attributs feat.href
	
	//Tags und Attribute für Dateien vom paulatyp MULTIFEAT(feat.dtd)
	protected static final String[] TAG_MULTI_FEAT= 			{"feat"};		//Tagname des Tags feat
	protected static final String[] ATT_MULTI_FEAT_NAME= 		{"name"};		//Attributname des Attributs feat.name
	protected static final String[] ATT_MULTI_FEAT_VALUE= 		{"value"};		//Attributname des Attributs feat.value

	/**
	 * aktueller Analyze-Container, der zu analysieren ist
	 */
	protected AnalyzeContainer aCon= null;
	protected String name= TOOLNAME;
	protected String version= VERSION;
	
	/**
	 * Der DocumentLocator für das zu parsende XML-Document
	 */
	protected Locator docLocator= null;		
	//	 *************************************** Meldungen ***************************************
	protected static final String MSG_STD=			TOOLNAME + ">\t";
	protected static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START_FCT=		MSG_STD + "start method: ";
	private static final String MSG_END_FCT=		MSG_STD + "end method: ";
	//	 *************************************** Fehlermeldungen ***************************************
	protected static final String ERR_STD_XML=			MSG_ERR + "An error occurs during reading xml-document: ";	
	protected static final String ERR_NOT_IMPLEMENTED=	MSG_ERR + "This is an internal failure. This methode hasn´t been overriden by concrete analyzer.";
	protected static final String ERR_EMPTY_NAME=		MSG_ERR + "The given name of analyzer is empty.";
	protected static final String ERR_EMPTY_VERSION=	MSG_ERR + "The given version of analyzer is empty.";
	protected static final String ERR_EMPTY_DTD=		MSG_ERR + "The given dtd name for file is empty, or does not exist.";
	protected static final String ERR_EMPTY_PAULAID=	MSG_ERR + "The given PAULA type for file is empty.";
	protected static final String ERR_EMPTY_ACON=		MSG_ERR + "The given an alyze container is empty.";
	protected static final String ERR_EMPTY_VAL=		MSG_ERR + "The given val is empty. Cannot compare with string-array.";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein Object vom Typ AbstractAnalyzer, dabei werden der übergebene Name
	 * des Konkreten Analyzers und dessen Versionsnummer gesetzt, so dass sie zurückgegeben
	 * werden können.
	 * @param analyzerName String - Name des konkreten Analyzers
	 * @param version String - Version des konkreten Analyzers
	 * @exception Fehler, wenn AnalyzerName oder Versionsname leer sind 
	 */
	public AbstractAnalyzer(String analyzerName, String version) throws Exception
	{
		if ((analyzerName == null) || (analyzerName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_NAME);
		if ((version == null) || (version.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_VERSION);
		this.name= analyzerName;
		this.version= version;
	}
	
	/**
	 * Initialisiert ein Object vom Typ AbstractAnalyzer.
	 * @exception Fehler, wenn Methode nicht überschrieben wurde
	 */
	public AbstractAnalyzer() throws Exception
	{
		throw new Exception(ERR_NOT_IMPLEMENTED);
	}
//	 ============================================== protected Methoden ==============================================
	
	/**
	 * Prüft ob ein Wert in der Menge der analysierbaren Werte enthalten ist. Werte können
	 * z.B. sein: DTD-Namen, PAULA-IDs oder PAULA-Typen
	 * @param val String - Value Name der zu analysierenden Value
	 * @param analyzableVals String - Namen der analysierbaren Values
	 */
	protected boolean canAnalyzeValue(String val, String[] analyzableVals) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "canAnalyzeValue()");
		
		if ((val== null) || (val.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_VAL);
		boolean canAnalyze= false;
		for (int i= 0; i < analyzableVals.length; i++)
			if (val.equalsIgnoreCase(analyzableVals[i])) canAnalyze= true;
		
		if (DEBUG) System.out.println(MSG_END_FCT + "canAnalyzeValue()");
		
		return(canAnalyze);
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Gibt den Namen des konkreten instanziierten Analyzers zurück.
	 * @return Name des konkreten Analyzers
	 */
	public String getAnalyzerName()
		{ return(this.name); }
	
	/**
	 * Gibt die Version des konkreten instanziierten Analyzers zurück.
	 * @return Version des konkreten Analyzers
	 */
	public String getAnalyzerVersion()
		{ return(this.version); }
	
	/**
	 * Gibt zurück, ob der konkrete Analyzer eine Datei mit der übergebenen DTD und 
	 * dem übergebenen PAULA-Typ analysieren kann
	 * @param dtd String - Name der DTD, zu der die zu analysierende Datei gehört
	 * @param paulaID String - Name der PAULA-ID, zu der die zu analysierende Datei gehört
	 * @param paulaType String - Name des PAULA-Typs, zu der die zu analysierende Datei gehört
	 * @return true, wenn Datei analysiert werden kann, false sonst
	 * @exception Fehler, wenn dtd oder paulaID leer sind, oder Methode nicht überschrieben wurde
	 */
	/*
	public boolean canAnalyze(File dtd, String paulaID, String paulaType) throws Exception
	{
		if ((dtd == null) || (!dtd.exists())) throw new Exception(ERR_EMPTY_DTD);
		if ((paulaID == null) || (paulaID.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_PAULAID);
		
		throw new Exception(ERR_NOT_IMPLEMENTED);
	}*/
	
	/**
	 * Gibt zurück, ob der konkrete Analyzer eine Datei mit der übergebenen DTD und 
	 * dem übergebenen PAULA-Typ analysieren kann
	 * @param aCon AnalyzeContainer - ein Container-Objekt, in dem Dateiinformationen stehen und das Analyseergebniss geschrieben werden kann
	 * @exception Fehler, wenn Methode nicht überschrieben wurde
	 */
	public boolean canAnalyze(AnalyzeContainer aCon) throws Exception
	{
		throw new Exception(ERR_NOT_IMPLEMENTED);
	}
	
	/**
	 * Startet den AnalyseProzess und übergibt das zu beschreibende Container-Objekt.
	 * @param aCon AnalyzeContainer - ein Container-Objekt, in dem Dateiinformationen stehen und das Analyseergebniss geschrieben werden kann
	 * @exception Fehler, wenn Methode nicht überschrieben wurde
	 */
	public void startAnalyze(AnalyzeContainer aCon) throws Exception
	{
		throw new Exception(ERR_NOT_IMPLEMENTED + "startAnalyze()");
	}
	
	/**
	 * Wird aufgerufen, wenn der AnalyseProzess zu Ende ist, diese Methode gibt das vorher
	 * übergebene Objekt um die Eigenschaften Typ und Kommentar erweitert zurück.
	 * @exception Fehler, wenn Methode nicht überschrieben wurde
	 */
	public AnalyzeContainer getResult() throws Exception
	{
		throw new Exception(ERR_NOT_IMPLEMENTED);
	}
	
	/**
	 * Gibt ein den Analyse-Typ für die zu analysierende Datei zurück.
	 * @return Analyse Typ des Analysevorgangs, null wenn kein Ergebnis gefunden wurde
	 * @exception Fehler, wenn Methode nicht überschrieben wurde
	 */
	/*
	public String getType() throws Exception
	{
		throw new Exception(ERR_NOT_IMPLEMENTED);
	}
	
	/**
	 * Gibt den Kommentar zu dem Analyse-Typ für die zu analysierende Datei zurück.
	 * @return Kommentar zu dem Analyse Typ des Analysevorgangs, null wenn kein Kommentar gefunden wurde
	 * @exception Fehler, wenn Methode nicht überschrieben wurde
	 **/
	/*
	public String getComment() throws Exception
	{
		throw new Exception(ERR_NOT_IMPLEMENTED);
	}
	*/
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= KW_NAME + ": " + this.name + ", " + KW_VERSION + ": " + this.version;
		return(retStr);
	}
	
	/**
	 * Setzt den übergebenen DocumentLocator, so dass alle abgeleiteten Klassen 
	 * darauf zugreifen können. 
	 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(Locator)
	 */
	public void setDocumentLocator(Locator locator)
		{ this.docLocator= locator; }
}
