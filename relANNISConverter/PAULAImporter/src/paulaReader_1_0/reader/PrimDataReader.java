package paulaReader_1_0.reader;

import java.io.File;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import paulaReader_1_0.PAULAMapperInterface;

/**
 * Dieser Reader liest ein PAULA 1.0 Dokument aus, dass dem Typ primData genügt. 
 * Es werden alle Textknoten gelesen und mitsamt ihrer Daten, über ein Callback, 
 * an eine das Interface paula_1_0.Mapper implementierende Klasse übergeben. Diese 
 * Klasse kann nun die Daten aus dem PAULA-Dokument verarbeiten. 
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public class PrimDataReader extends PAULAReader 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PrimDataReader";		//Name dieses Tools
	private static final String VERSION=	"1.0";				//Version des Tools
	private static final String PAULA_VERSION=	"1.0";			//Unterstützter PAULA-Standard
	private static final boolean DEBUG= false;					//DEBUG-Schalter		
	
	private String text= "";						//eigentliche Primärdaten
	private boolean startText= false;				//gibt an, ob das aktuelle Element Texte enthält
	private String paulaID= null;					//Paula_id
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
	 * @param korpusPath String - Pfad durch den KOrpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - zu parsende PAULA-Datei
	 * @param logger Logger - Logger zur Nachrichtenausgabe
	 */
	public PrimDataReader(	PAULAMapperInterface mapper,
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
	 * Liest den Primärtext dieses Dokumentes aus und schreibt ees in das interne 
	 * Textfeld.
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(	char[] ch,
            				int start,
            				int length) throws SAXException
    {
		//Der folgende Text sind Primärdaten
		if (this.startText)
		{
			String textNode= "";
	    	for (int i= 0; i < length; i++)
	    		{ textNode= textNode +ch[start+i]; }
	    	//Leerzeichen entfernen
	    	//textNode= textNode.trim();
	    	//Text speichern, wenn es einen gibt
	    	if (textNode.length() > 0) this.text= this.text + textNode;
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
		//TAG HEADER gefunden
		if (this.isTAGorAttribute(qName, TAG_HEADER))
		{
			for(int i= 0; i < attributes.getLength(); i++)
			{	
				if (this.isTAGorAttribute(attributes.getQName(i), ATT_HEADER_PAULA_ID))
					this.paulaID= attributes.getValue(i);
			}
		}
		//Element erreicht bei dem der Text beginnt
		else if (this.isTAGorAttribute(qName, TAG_TEXT_BODY))
			this.startText= true;
    }
	
	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(	String uri,
            				String localName,
            				String qName) throws SAXException
    {
		//Element erreicht bei dem der Text endet
		if (this.isTAGorAttribute(qName, TAG_TEXT_BODY))
		{
			this.startText= false;
			//aus den Primärdaten einen PD-Knoten im Korpusgraphen erstellen
			try
			{
				//PrimDataConnector im Mapper aufrufen
				this.mapper.primDataConnector(this.korpusPath, this.paulaFile, this.paulaID, this.text);
				
			}
			catch (Exception e)
				{ 
				e.printStackTrace();
				throw new SAXException(e.getMessage()); }
		}
		this.text= "";
    }
}
