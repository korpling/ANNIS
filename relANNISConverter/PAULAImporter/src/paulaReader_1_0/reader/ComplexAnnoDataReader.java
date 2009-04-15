package paulaReader_1_0.reader;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import paulaReader_1_0.PAULAMapperInterface;

import util.xPointer.XPtrInterpreter;
import util.xPointer.XPtrRef;


/**
 * Dieser Reader liest ein PAULA 1.0 Dokument aus, dass dem Typ ComplexAnnoData genügt. 
 * Es werden alle Strukturknoten gelesen und mitsamt ihrer Daten, über ein Callback, 
 * an eine das Interface paula_1_0.Mapper implementierende Klasse übergeben. Diese 
 * Klasse kann nun die Daten aus dem PAULA-Dokument verarbeiten. 
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class ComplexAnnoDataReader extends PAULAReader 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 		"ComplexAnnoDataReader";		//Name dieses Tools
	private static final String VERSION=		"1.0";				//Version des Tools
	private static final String PAULA_VERSION=	"1.0";			//Unterstützter PAULA-Standard
	private static final boolean DEBUG=			true;			//DEBUG-Schalter
	
	private static final String KW_EMPTY=		"empty";		//Keyword für target wert leer 
	
	private static String VAL_CURR_FILE=		"non current document";			//aktuell geparstes Dokument
	
	private String paulaID= null;					//Paula_id
	private String xmlBase= null;					//Dokument auf das sich dieses bezieht
	private String paulaType= null;					//Paula-Typ des aktuellen Dokumentes
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static String ERR_STD_XML=			MSG_ERR + "An error occurs while parsing document '"+ VAL_CURR_FILE +"': ";
	private static String ERR_NO_TAR_VAL=		ERR_STD_XML + "The required value for attribut 'target' is empty.";
	private static String ERR_TOO_MUCH_TARGETS=	ERR_STD_XML + "There are too much targets given in attribute 'target' (only one permissable): ";
	private static String ERR_XPTR_IS_RANGE=	ERR_STD_XML + "The given XPointer in attribute 'target' is a range (only one element permissable): "; 
	private static String ERR_XPTR_IS_TEXT=		ERR_STD_XML + "The given XPointer in attribute 'target' is a text (only one element permissable): ";
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
	public ComplexAnnoDataReader(	PAULAMapperInterface mapper,
									String korpusPath, 
									File paulaFile,
									Logger logger) throws Exception
	{
		super(TOOLNAME, VERSION, mapper, korpusPath, paulaFile, logger);
		ComplexAnnoDataReader.VAL_CURR_FILE= this.paulaFile.getName();
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
			//Tag FEATLIST gefunden
			else if (this.isTAGorAttribute(qName, TAG_FEAT_FEATLIST))
			{
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					//Attribut MARKLIST.BASE gefunden
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEATLIST_BASE))
						this.xmlBase= attributes.getValue(i);
					//Attribut MARKLIST.TYPE gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEATLIST_TYPE))
						this.paulaType= attributes.getValue(i);
				}
			}
			//Tag FEAT gefunden
			else if (this.isTAGorAttribute(qName, TAG_FEAT_FEAT))
			{
				String featID= null;	//feat.id-Wert
				String featHref= null;	//feat.href-Wert
				String featTar= null;	//feat.target-Wert
				String featVal= null;	//feat.value-Wert
				String featDesc= null;	//feat.description-Wert
				String featExp= null;	//feat.example-Wert
				
				for(int i= 0; i < attributes.getLength(); i++)
				{	
					//Attribut FEAT.ID gefunden
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_ID))
						featID= attributes.getValue(i);
					//Attribut FEAT.HREF gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_HREF))
						featHref= attributes.getValue(i);
					//Attribut FEAT.TARGET gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_TAR))
						featTar= attributes.getValue(i);
					//Attribut FEAT.VALUE gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_VAL))
						featVal= attributes.getValue(i);
					//Attribut FEAT.DESCRIPTION gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_DESC))
						featDesc= attributes.getValue(i);
					//Attribut FEAT.EXAMPLE gefunden
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_FEAT_FEAT_EXP))
						featExp= attributes.getValue(i);
				}
				// wenn featTar leer, dann Fehler
				if ((featTar== null) || (featTar.equalsIgnoreCase("")))
						throw new Exception(ERR_NO_TAR_VAL);
				
				//prüfen ob target ein Ziel referenziert oder das keyword empty enthält
				if (featTar.equalsIgnoreCase(KW_EMPTY)) featTar= null;
				else
				{
					// wenn in Target kein gültiger XPtr, dann Fehler
					XPtrInterpreter xptrInterpreter= new XPtrInterpreter();
					xptrInterpreter.setInterpreter(xmlBase, featTar);
					Vector<XPtrRef> xPtrRefs= xptrInterpreter.getResult();
					//wenn es mehr als eine Referenz gibt
					if (xPtrRefs.size() > 1)
						throw new Exception(ERR_TOO_MUCH_TARGETS + featTar);
					if (xPtrRefs.firstElement().getType().equals(XPtrRef.POINTERTYPE.ELEMENT))
					{
						if (xPtrRefs.firstElement().isRange())
							throw new Exception(ERR_XPTR_IS_RANGE + featTar);
					}
					else throw new Exception(ERR_XPTR_IS_TEXT + featTar);
				}
				
				//das PAULAMapperInterface aufrufen
				this.mapper.pointingRelDataConnector(	this.korpusPath, this.paulaFile, 
														this.paulaID, this.paulaType, this.xmlBase, 
														featVal, featHref, featTar);
//				this.mapper.complexAnnoDataConnector(	this.korpusPath, this.paulaFile, 
//														this.paulaID, this.paulaType, this.xmlBase, 
//														featVal, featHref, featTar);
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
}
