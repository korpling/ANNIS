package paulaReader_1_0.reader;

import java.io.File;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import paulaReader_1_0.PAULAMapperInterface;

import reader.*;

/**
 * Die Klasse PAULAReader ist eine abstrakte Klasse zum Einlesen von PAULA 1.0 Dokumenten.
 * Von dieser Klasse werden die spezifischen Reader, abgeleitet, die die DOkumente parsen.
 * Für jeden Analysetyp gibt es einen Reader.
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public abstract class PAULAReader extends AbstractReader 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 		"PAULAReader";		//Name dieses Tools
	private static final String VERSION=		"1.0";				//Version des Tools
	private static final String PAULA_VERSION=	"1.0";				//Unterstützter PAULA-Standard
	private static final String PAULA_CTYPE=	"NON";				//Typ nach dem Klassifikationsmodell
	
	//Spezifizierung der PAULA-Tags und Paula-Attribute
	//Generelle Spezifizierung (TAG oder ATT)_(Name der dtd)_(Name des Tags)_(Name des Attributs wenn es sich um ATT handelt)
	protected static final String[] ATT_ID=	{"id"};				//Attribut id
	
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
	protected static final String[] TAG_MULTI_MULTIFEAT= 		{"multifeat"};		//Tagname des Tags feat
	protected static final String[] ATT_MULTI_MULTIFEAT_ID= 	{"id"};				//Attributname des Attributs multifeat.id
	protected static final String[] ATT_MULTI_MULTIFEAT_HREF= 	{"xlink:href", "href"};		//Attributname des Attributs multifeat.href
	
	//Tags und Attribute für Dateien vom paulatyp MULTIFEAT(feat.dtd)
	protected static final String[] TAG_MULTI_FEAT= 			{"feat"};		//Tagname des Tags feat
	protected static final String[] ATT_MULTI_FEAT_ID= 			{"id"};					//Attributname des Attributs feat.id
	protected static final String[] ATT_MULTI_FEAT_NAME= 		{"name"};		//Attributname des Attributs feat.name
	protected static final String[] ATT_MULTI_FEAT_VALUE= 		{"value"};		//Attributname des Attributs feat.value
	
	/**
	 * Der Mapper, der für das Callback aufgerufen wird.
	 */
	protected PAULAMapperInterface mapper= null;	
	
	protected String readerName= null;							//Name des spezifischen Readers
	protected String readerVersion= null;						//Version des spezifischen Readers
	protected String korpusPath= null;							//Pfad durch den Korpus in dem das aktuelle Dokument liegt
	protected File paulaFile= null;								//aktuelle geparste PAULA-Datei
	
	/**
	 * Speichert alle gelelesenen Attribut-Werte des Attributes id
	 */
	protected Collection<String> idList= null;
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String MSG_TO_IMPLEMENT=			MSG_ERR + "this methode must be implemented by derived PAULAReader-object.";
	private static final String ERR_CONSTRAINT_UNIQUE_ID=	MSG_ERR + "There is an error in given paula document. The id value has to be unique, but this document contains the following id value more than once: ";
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt den Toolnamen dieser Klasse zurück. dies ist ein abstrakter Name, der nicht 
	 * dem Namen der Klasse gleichen muss.
	 * @return Toolname dieser Klasse 
	 */
	public static String getToolName()
		{ return(TOOLNAME); }
	
	/**
	 * Gibt die Version dieser Klasse zurück.
	 * @return Version dieser Klasse 
	 */
	public static String getVersion()
		{ return(VERSION); }
	
	/**
	 * Gibt die von dieser Klasse unterstützte PAULA-Version zurück.
	 * @return Version dieser Klasse 
	 */
	public static String getPAULAVersion()
		{ return(PAULA_VERSION); }
	
	/**
	 * Gibt die Typbezeichnung für PAULA-Dateien zurück, die von diesem Reader bearbeitet
	 * werden können. Diese Typbezeichnung entspricht dem PAULA-Klassifikationsmodell.
	 * @return PAULA-Typ nach dem Klassifikationsmodell
	 */
	//public static String getPAULACType()
	//	{ return(PAULA_CTYPE); }
	
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein Reader Objekt und setzt den logger zur Nachrichtenausgabe. 
	 * Während des Lesens wird der übergebene Mapper über ein Callback aufgerufen um
	 * die entsprechenden Daten zu verarbeiten.
	 * @param readerName String - Name des spezifischen Readers
	 * @param readerVersion String - Version des spezifischen Readers
	 * @param mapper PAULAMapperInterface - Der Mapper, der für das Callback aufgerufen wird
	 * @param korpusPath String - Pfad durch den KOrpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - zu parsende PAULA-Datei
	 * @param logger Logger - Logger zur Nachrichtenausgabe
	 */
	public PAULAReader(	String readerName, 
						String readerVersion,
						PAULAMapperInterface mapper,
						String korpusPath,
						File paulaFile,
						Logger logger)
	{
		super(logger);
		this.readerName= readerName;
		this.readerVersion= readerVersion;
		this.mapper= mapper;
		this.paulaFile= paulaFile;
		this.korpusPath= korpusPath;
		
		//id-Liste initialisisern
		this.idList= new Vector<String>();
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== protected Methoden ==============================================
	/**
	 * Gibt zurück, ob ein gegebener Attribut oder Tagname in der übergebenen Liste
	 * von Attribut- oder Wertnamen enthalten ist.
	 * @param val String - zu suchender Attribut- oder Tagname
	 * @param list String[] - Stringliste in der gesucht werden soll 
	 */
	public boolean isTAGorAttribute(String val, String[] list)
	{
		boolean retVal= false; 
		for (String element: list)
			if (element.equalsIgnoreCase(val)) retVal= true;
		return(retVal);
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Gibt den Namen des spezifischen Readers zurück.
	 * @return Name des spezifischen Readers
	 */
	public String getReaderName() throws Exception
		{ return(this.readerName); }
	
	/**
	 * Gibt die Version des spezifischen Readers zurück.
	 * @return Version des spezifischen Readers
	 */
	public String getReaderVersion() throws Exception
		{ return(this.readerVersion); }
	
	/**
	 * Gibt die Typbezeichnung für PAULA-Dateien zurück, die von diesem Reader bearbeitet
	 * werden können. Diese Typbezeichnung entspricht dem PAULA-Klassifikationsmodell.
	 * @return PAULA-Typ nach dem Klassifikationsmodell
	 */
	public String getPAULACType()
		{ return(PAULA_CTYPE); }
	
	/**
	 * Erstellt eine Tabellendatei für das DDD-Modell aus einer xml-Datei im Paula-Format.
	 * @param xmlFile File - Das zu parsende PAULA-Dokument 
	 */
	public void parse(File xmlFile) throws Exception
	{ 
		//parse aktuelles Dokument
		this.setReader(this).parse(xmlFile.getCanonicalPath());
	}
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= 	"tool: " + TOOLNAME + "\tversion: " + 
					VERSION + "\t supported PAULA version: "+ PAULA_VERSION;
		return(retStr);
	}
	
	/**
	 * Gibt eine Auswertung über das lesen des PAULA-Dokumentes zurück. Die Anzahl der 
	 * verschiedenen gelesenen Elemente wird in einem String zurückgegeben.
	 * @return Ausertungsstring
	 */
	public String getEvaluation() throws Exception
	{
		throw new Exception(MSG_TO_IMPLEMENT);
	}

//	 --------------------------- SAX Methoden ---------------------------
	
	/**
	 * Prüft ein paar allgemeine Constraints, die Paula-Dateien einhalten müssen.
	 * Die folgenden Constraints werden überprüft:
	 * <ul>
	 * 	<li>Wert des Attributes 'id' muss eindeutig sein (es darf keine zwei Elemente mit dem gleichen Wert geben)</li>
	 * </ul>
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(	String uri,
            					String localName,
            					String qName,
            					Attributes attributes) throws SAXException
    {
		//Attribue überprüfen
		for(int i= 0; i < attributes.getLength(); i++)
		{
			//ID-Attribut gefunden
			if (this.isTAGorAttribute(attributes.getQName(i), ATT_ID))
			{
				if (this.idList.contains(attributes.getValue(i).toUpperCase()))
					throw new SAXException(ERR_CONSTRAINT_UNIQUE_ID + attributes.getValue(i));
				this.idList.add(attributes.getValue(i).toUpperCase());
			}
		}
    }
		
}
