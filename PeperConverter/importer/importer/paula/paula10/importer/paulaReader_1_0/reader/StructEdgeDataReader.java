package importer.paula.paula10.importer.paulaReader_1_0.reader;

import java.io.File;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import importer.paula.paula10.importer.paulaReader_1_0.PAULAMapperInterface;

/**
 * Dieser Reader liest ein PAULA 1.0 Dokument aus, dass dem Typ StructEdgeData genügt. 
 * Es werden alle Meta-Annotations-Knoten gelesen und mitsamt ihrer Daten, über ein Callback, 
 * an eine das Interface paula_1_0.Mapper implementierende Klasse übergeben. Diese 
 * Klasse kann nun die Daten aus dem PAULA-Dokument verarbeiten. <br/>
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public class StructEdgeDataReader extends PAULAReader 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 		"StructEdgeDataReader";		//Name dieses Tools
	private static final String VERSION=		"1.0";						//Version des Tools
	private static final String PAULA_VERSION=	"1.0";						//Unterstützter PAULA-Standard
	private static final String PAULA_CTYPE=	"StructEdgeData";			//Unterstützter PAULA-Standard
	
	private static final boolean MODE_SE_NEW= true;
	
	private static final boolean DEBUG=		true;						//DEBUG-Schalter
	private String paulaID= null;				//Paula_id
	private String slType=	null;				//Attributwert von structList.type
	private String slBase=	null;				//Attributwert von structList.base
	private String structID= null;				//Attributwert von struct.id
	
	/**
	 * the parsed PAULA-File
	 */
	private File paulaFile= null;
	
	private long numStructList= 0;		//Anzahl der gelesenen StructList-Elemente
	private long numStruct= 0;			//Anzahl der gelesenen Struct-Elemente
	private long numRel= 0;				//Anzahl der gelesenen Rel-Elemente
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
	public StructEdgeDataReader(	PAULAMapperInterface mapper,
									String korpusPath, 
									File paulaFile,
									Logger logger) throws Exception
	{
		super(TOOLNAME, VERSION, mapper, korpusPath, paulaFile, logger);
		this.init();
		this.paulaFile= paulaFile;
		if (this.logger!= null) this.logger.debug(MSG_STD + "object initialized: " + this);		
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Initialisiert dieses Objekt und setzt dabei alle wichtigen Variablen auf INIT-Zustand.
	 */
	private void init()
	{
		numStructList= 0;	
		numStruct= 0;
		numRel= 0;	
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Gibt die von dieser Klasse unterstützte PAULA-Version zurück.
	 * @return Version dieser Klasse 
	 */
	public String getPAULACType()
		{ return(PAULA_CTYPE); }
	
	/**
	 * Gibt eine Auswertung über das lesen des PAULA-Dokumentes zurück. Die Anzahl der 
	 * verschiedenen gelesenen Elemente wird in einem String zurückgegeben.
	 * @return Ausertungsstring
	 */
	public String getEvaluation()
	{
		String retStr= "";
		retStr= "number of 'structList'-elements: " + this.numStructList + "\t"+
				"number of 'struct'-elements: " + this.numStruct + "\t" +
				"number of 'rel'-elements: " + this.numRel;
		return(retStr);
	}
	
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
	 * Ruft für jedes gelesene STRUCTLIST-ELEMENT die Methode ... auf
	 * Ruft für jedes gelesene STRUCT-ELEMENT die Methode ... auf 
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
					//ATTIBUTE HEADER.ID
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_HEADER_PAULA_ID))
						this.paulaID= attributes.getValue(i);
				}
			}
			//TAG STRUCTLIST gefunden
			else if (this.isTAGorAttribute(qName, TAG_STRUCT_STRUCTLIST))
			{
				this.numStructList++;
				for(int i= 0; i < attributes.getLength(); i++)
				{
					//ATTIBUTE STRUCTLIST.TYPE
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_STRUCT_STRUCTLIST_TYPE))
						this.slType= attributes.getValue(i);
					//ATTIBUTE STRUCTLIST.BASE
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_STRUCT_STRUCTLIST_BASE))
						this.slBase= attributes.getValue(i);
				}
				//wenn eine Basis gegeben, dann das aktuelle Dokument als Basis nehmen
				if ((this.slBase == null) || (this.slBase.equalsIgnoreCase("")))  
					this.slBase= this.paulaFile.getName();
			}
			//TAG STRUCT gefunden
			else if (this.isTAGorAttribute(qName, TAG_STRUCT_STRUCT))
			{
				this.numStruct++;
				for(int i= 0; i < attributes.getLength(); i++)
				{
					//ATTIBUTE STRUCT.ID
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_STRUCT_STRUCT_ID))
						this.structID= attributes.getValue(i);
				}
				//old
				/*if (!MODE_SE_NEW)
					this.mapper.structEdgeDataConnector1(this.korpusPath, this.paulaFile, this.paulaID, this.slType, this.slBase, this.structID);
				*/
				
			}
			//TAG REL gefunden
			else if (this.isTAGorAttribute(qName, TAG_STRUCT_REL))
			{
				this.numRel++;
				String relID= "";		//Attributwert von STRUCT.ID
				String relType= "";		//Attributwert von STRUCT.TYPE
				String relHref= "";		//Attributwert von STRUCT.HREF
				
				for(int i= 0; i < attributes.getLength(); i++)
				{
					//ATTIBUTE REL.ID
					if (this.isTAGorAttribute(attributes.getQName(i), ATT_STRUCT_REL_ID))
						relID= attributes.getValue(i);
					//ATTIBUTE REL.TYPE
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_STRUCT_REL_TYPE))
						relType= attributes.getValue(i);
					//ATTIBUTE REL.HREF
					else if (this.isTAGorAttribute(attributes.getQName(i), ATT_STRUCT_REL_HREF))
						relHref= attributes.getValue(i);
				}
				if (MODE_SE_NEW)
					this.mapper.structEdgeDataConnector(this.korpusPath, this.paulaFile, this.paulaID, this.slType, this.slBase, this.structID, relID, relHref, relType);
				//old
				/*
				else
					 this.mapper.structEdgeDataConnector2(this.korpusPath, this.paulaFile, this.paulaID, this.slType, this.slBase, this.structID, relID, relHref, relType);
					 */
				
			}
		}
		catch (Exception e)
		{
			if (DEBUG) e.printStackTrace();
			throw new SAXException(ERR_STD_XML+ e.getMessage());
		}
    }
	//--------------------------------- Ende SAX-Methoden ---------------------------------
}
