package paulaReader_1_0.reader;

import java.io.File;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import paulaReader_1_0.PAULAMapperInterface;

/**
 * Dieser Reader liest ein PAULA 1.0 Dokument aus, dass dem Typ MetaStructDataData genügt. 
 * Es werden alle Meta-Struktur-Knoten gelesen und mitsamt ihrer Daten, über ein Callback, 
 * an eine das Interface paula_1_0.Mapper implementierende Klasse übergeben. Diese 
 * Klasse kann nun die Daten aus dem PAULA-Dokument verarbeiten. <br/>
 * ACHTUNG: DIESER READER IST BISHER NUR EIN DUMMY
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public class MetaStructDataReader extends PAULAReader 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"MetaStructDataReader";		//Name dieses Tools
	private static final String VERSION=	"1.0";						//Version des Tools
	private static final String PAULA_VERSION=	"1.0";					//Unterstützter PAULA-Standard
	
	private static final boolean DEBUG=		true;						//DEBUG-Schalter
	private String paulaID= null;				//Paula_id
	private String slType= null;				//Type der StructList
	private String structID= null;				//ID der StructList
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
	public MetaStructDataReader(	PAULAMapperInterface mapper,
									String korpusPath, 
									File paulaFile,
									Logger logger) throws Exception
	{
		super(TOOLNAME, VERSION, mapper, korpusPath, paulaFile, logger);
		if (this.logger!= null) this.logger.debug(MSG_STD + "object initialized: " + this);
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
//--------------------------------- Start SAX-Methoden --------------------------------
	
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
		//Tag STRUCTLIST gefunden
		else if (this.isTAGorAttribute(qName, TAG_STRUCT_STRUCTLIST))
		{
			for(int i= 0; i < attributes.getLength(); i++)
			{	
				//Attribut STRUCTLIST.TYPE gefunden
				if (this.isTAGorAttribute(attributes.getQName(i), ATT_STRUCT_STRUCTLIST_TYPE))
					this.slType= attributes.getValue(i);
			}
		}
		//Tag STRUCT gefunden
		else if (this.isTAGorAttribute(qName, TAG_STRUCT_STRUCT))
		{
			for(int i= 0; i < attributes.getLength(); i++)
			{	
				//Attribut STRUCT.ID gefunden
				if (this.isTAGorAttribute(attributes.getQName(i), ATT_STRUCT_STRUCT_ID))
					this.structID= attributes.getValue(i);
			}
		}
		//Tag REL gefunden
		else if (this.isTAGorAttribute(qName, TAG_STRUCT_REL))
		{
			String relID= null;	//ID dieses Knotens
			String relHref= null;	//Referenzdocument dieses Knotens
			for(int i= 0; i < attributes.getLength(); i++)
			{	
				//Attribut REL.ID gefunden
				if (this.isTAGorAttribute(attributes.getQName(i), ATT_STRUCT_REL_ID))
					relID= attributes.getValue(i);
				//Attribut REL.ID gefunden
				if (this.isTAGorAttribute(attributes.getQName(i), ATT_STRUCT_REL_HREF))
					relHref= attributes.getValue(i);
			}
			this.mapper.metaStructDataConnector(this.korpusPath, this.paulaFile, this.paulaID, this.slType, this.structID, relID, relHref);
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
	//--------------------------------- Ende SAX-Methoden ---------------------------------
}
