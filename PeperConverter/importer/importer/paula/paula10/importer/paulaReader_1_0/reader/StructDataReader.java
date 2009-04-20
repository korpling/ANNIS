package importer.paula.paula10.importer.paulaReader_1_0.reader;

import java.io.File;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import importer.paula.paula10.importer.paulaReader_1_0.PAULAMapperInterface;


/**
 * Dieser Reader liest ein PAULA 1.0 Dokument aus, dass dem Typ structData genügt. 
 * Es werden alle Strukturknoten gelesen und mitsamt ihrer Daten, über ein Callback, 
 * an eine das Interface paula_1_0.Mapper implementierende Klasse übergeben. Diese 
 * Klasse kann nun die Daten aus dem PAULA-Dokument verarbeiten. 
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class StructDataReader extends PAULAReader 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 		"StructDataReader";		//Name dieses Tools
	private static final String VERSION=		"1.0";				//Version des Tools
	private static final String PAULA_VERSION=	"1.0";			//Unterstützter PAULA-Standard
	private static final boolean DEBUG=			true;			//DEBUG-Schalter
	
	private String paulaID= null;					//Paula_id
	private String xmlBase= null;					//Dokument auf das sich dieses bezieht
	private String paulaType= null;					//Paula-Typ des aktuellen Dokumentes
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_STD_XML=		MSG_ERR + "An error occurs while parsing document: ";
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

//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein Reader Objekt und setzt den logger zur Nachrichtenausgabe. 
	 * Während des Lesens wird der übergebene Mapper über ein Callback aufgerufen um
	 * die entsprechenden Daten zu verarbeiten.
	 * @param mapper PAULAMapperInterface - Der Mapper, der für das Callback aufgerufen wird
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - zu parsende PAULA-Datei
	 * @param logger Logger - Logger zur Nachrichtenausgabe
	 */
	public StructDataReader(	PAULAMapperInterface mapper,
								String korpusPath, 
								File paulaFile,
								Logger logger) throws Exception
	{
		super(TOOLNAME, VERSION, mapper, korpusPath, paulaFile, logger);
		if (this.logger!= null) this.logger.debug(MSG_STD + "object initialized: " + this);
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
		retStr= 	"tool: " + TOOLNAME + "\tversion: " + 
					VERSION + "\t supported PAULA version: "+ PAULA_VERSION;
		return(retStr);
	}
//	 --------------------------- SAX Methoden ---------------------------
	public void startDocument()	throws SAXException
	{
		try
		{
			//Mapper Bescheid geben, dass das Parsen beginnt
			this.mapper.startDocument(this, this.paulaFile, this.korpusPath);
			if (this.logger != null) this.logger.debug(MSG_STD + "reading document: "+ this.paulaFile.getCanonicalPath());
		}
		catch (Exception e)
		{
			if (DEBUG) e.printStackTrace();
			throw new SAXException(ERR_STD_XML+ e.getMessage());
		}
	}
	
	public void endDocument()	throws SAXException
	{
		try
		{
			//Mapper Bescheid geben, dass das Parsen beginnt
			this.mapper.endDocument(this, this.paulaFile, this.korpusPath);
		}
		catch (Exception e)
		{
			if (DEBUG) e.printStackTrace();
			throw new SAXException(ERR_STD_XML+ e.getMessage());
		}
	}
	
	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(	String uri,
            					String localName,
            					String qName,
            					Attributes attributes) throws SAXException
    {
		//allgemeine PAULA-Constraints prüfen
		super.startElement(uri, localName, qName, attributes);
		try
		{
			//TAG HEADER gefunden
			if (this.isTAGorAttribute(qName, TAG_HEADER))
			{
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_HEADER_PAULA_ID))
						this.paulaID= attributes.getValue(i);
				}
			}
			//Tag MARKLIST gefunden
			else if (this.isTAGorAttribute(qName, TAG_MARK_MARKLIST))
			{
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					//Attribut MARKLIST.BASE gefunden
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_MARK_MARKLIST_BASE))
						this.xmlBase= attributes.getValue(i);
					//Attribut MARKLIST.TYPE gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_MARK_MARKLIST_TYPE))
						this.paulaType= attributes.getValue(i);
				}
			}
			//Tag MARK gefunden
			else if (this.isTAGorAttribute(qName, TAG_MARK_MARK))
			{
				String markID= null;	//mark.id-Wert
				String markHref= null;	//mark.href-Wert
				String markType= null;	//mark.type-Wert
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					//Attribut MARK.ID gefunden
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_MARK_MARK_ID))
						markID= attributes.getValue(i);
					//Attribut MARK.HREF gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_MARK_MARK_HREF))
						markHref= attributes.getValue(i);
					//Attribut MARK.TYPE gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_MARK_MARK_TYPE))
						markType= attributes.getValue(i);
				}
				//das PAULAMapperInterface aufrufen
				//geändert am 26-06-08
				//this.mapper.structDataConnector(this.korpusPath, this.paulaFile, this.paulaID, this.paulaType, this.xmlBase, markID, markHref, markType);
				this.mapper.markableDataConnector(this.korpusPath, this.paulaFile, this.paulaID, this.paulaType, this.xmlBase, markID, markHref, markType);
			}
		}
		catch (Exception e)
		{
			if (DEBUG) 
			{
				e.printStackTrace();
				throw new SAXException(e.getMessage());
			}
			else throw new SAXException(e.getMessage());
		}
    }
	
	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(	String uri,
            				String localName,
            				String qName) throws SAXException
    {
    }
}
