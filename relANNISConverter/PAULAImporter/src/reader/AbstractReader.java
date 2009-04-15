package reader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;


/**
 * Diese Klasse stellt einen abstrakten Reader dar, der von einem DefaultHandler2 
 * abgeleitet ist. Dieser Reader stellt die Funktion zur Verfügung eine Datei an 
 * den Reader zu übergeben, Das initailisieren der SAX-Objekte wird automatisch übernommen.
 * @author Florian Zipser
 *
 */
public abstract class AbstractReader extends DefaultHandler2 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"AbstractReader";		//Name dieses Tools
	
	protected Logger logger= null;				//Logger für Log4j
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	protected static final String ERR_NOT_OVERRIDE=		MSG_ERR + "This methode has to be overriden by derived class: ";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein Reader Objekt und setzt den logger zur Nachrichtenausgabe.
	 * @param logger Logger - Logger zur Nachrichtenausgabe
	 */
	public AbstractReader(Logger logger)
	{
		this.logger= logger;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== protected Methoden ==============================================	
	/**
	 * Instatiiert einen XMLReader mit dem übergebenen DefaultHandler und gibt den
	 * XMLReader zurück. Der gegebene DefaultHandler wird als ContentHandler und 
	 * LexicalHandler gesetzt. Eine XML-Datei wird noch nicht geparst!
	 * @param reader DefaultReader2 - der Handler, der als DefaultHanlder gesetzt wird
	 * @return XMLReader, dieser kann zum parsen genutzt werden
	 * @throws Exception 
	 */
	protected XMLReader setReader(DefaultHandler2 reader) throws Exception
	{
		SAXParser parser;
        XMLReader xmlReader;
        
        final SAXParserFactory factory= SAXParserFactory.newInstance();
        parser= factory.newSAXParser();
        xmlReader= parser.getXMLReader();

        //contentHandler erzeugen und setzen
        xmlReader.setContentHandler(reader);
        //LexicalHandler setzen, damit DTD ausgelsen werden kann
        xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", reader);
        
        return(xmlReader);
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Erstellt eine Tabellendatei für das DDD-Modell aus einer xml-Datei im Paula-Format.
	 * Diese Klasse muss überschrieben werden.
	 * @param pdDesc PDDesc - Beschreibung der zu parsenden Datei
	 * @param kGraph KorpusGraph - das KorpusGraph-Objekt, in das der inhalt der Datei gelesen werden soll
	 * @param setObjectNode SetObjectDataNode - das SetObject, dem die, aus der PAULA-Datei, erzeugten Knoten unterstellt werden	
	 * @exception Fehler, wenn diese Methode nicht überschrieben wurde  
	 */
	public boolean parse() throws Exception
		{ throw new Exception(ERR_NOT_OVERRIDE); }
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		 retStr= "this method isn´t implemented";
		return(retStr);
	}
}
