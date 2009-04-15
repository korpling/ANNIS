package paulaReader_1_0.reader;

import java.io.File;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import paulaReader_1_0.PAULAMapperInterface;


/**
 * Dieser Reader liest ein PAULA 1.0 Dokument aus, dass dem Typ multifeat
 * genügt. Alle feat-Elemente werden dabei mit den Daten der übergeordneten
 * Elemente an den übergebenen Connector übergeben. 
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class MultiFeatDataReader extends PAULAReader 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 		"MultiFeatDataReader";		//Name dieses Tools
	private static final String VERSION=		"1.0";				//Version des Tools
	private static final String PAULA_VERSION=	"1.0";			//Unterstützter PAULA-Standard
	private static final String PAULA_CTYPE=	"MultiFeatData";			//Unterstützter PAULA-Standard
	private static final boolean DEBUG=			false;			//DEBUG-Schalter
	
	private String paulaID= null;					//Paula_id
	/**
	 * Wert multiFeatList.base
	 */
	private String multiFeatListBASE= null;					//Dokument auf das sich dieses bezieht
	/**
	 * Wert multifeatlist.type
	 */
	private String multiFeatListTYPE= null;					//Paula-Typ des aktuellen Dokumentes
	/**
	 * Wert multifeat.id
	 */
	private String multiFeatID= null;
	/**
	 * Wert multifeat.href
	 */
	private String multiFeatHREF= null;
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
	public MultiFeatDataReader(	PAULAMapperInterface mapper,
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
	 * Gibt die von dieser Klasse unterstützte PAULA-Version zurück.
	 * @return Version dieser Klasse 
	 */
	public String getPAULACType()
		{ return(PAULA_CTYPE); }
	
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
			throw new SAXException(ERR_STD_XML + e.getMessage());
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
			//Tag MULTIFEATLIST gefunden
			else if (this.isTAGorAttribute(qName, TAG_MULTI_MULTIFEATLIST))
			{
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					//Attribut MULTIFEATLIST.BASE gefunden
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_MULTI_MULTIFEATLIST_BASE))
						this.multiFeatListBASE= attributes.getValue(i);
					//Attribut MULTIFEATLIST.TYPE gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_MULTI_MULTIFEATLIST_TYPE))
						this.multiFeatListTYPE= attributes.getValue(i);
				}
			}
			//Tag MULTIFEAT gefunden
			else if (this.isTAGorAttribute(qName, TAG_MULTI_MULTIFEAT))
			{
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					//Attribut MULTIFEAT.ID gefunden
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_MULTI_MULTIFEAT_ID))
						this.multiFeatID= attributes.getValue(i);
					//Attribut MULTIFEAT.HREF gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_MULTI_MULTIFEAT_HREF))
						this.multiFeatHREF= attributes.getValue(i);
				}
			}
			//Tag FEAT gefunden
			else if (this.isTAGorAttribute(qName, TAG_MULTI_FEAT))
			{
				String featID= null;	//feat.id-Wert
				String featNAME= null;	//feat.Name-Wert
				String featVALUE= null;	//feat.value-Wert
				
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					//Attribut FEAT.ID gefunden
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_MULTI_FEAT_ID))
						featID= attributes.getValue(i);
					//Attribut FEAT.NAME gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_MULTI_FEAT_NAME))
						featNAME= attributes.getValue(i);
					//Attribut FEAT.VALUE gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_MULTI_FEAT_VALUE))
						featVALUE= attributes.getValue(i);
				}
				//das PAULAMapperInterface aufrufen
				this.mapper.multiFeatDataConnector(	this.korpusPath, this.paulaFile, this.paulaID,
													this.multiFeatListTYPE, this.multiFeatListBASE, 
													this.multiFeatID, this.multiFeatHREF, 
													featID, featNAME, featVALUE);
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
