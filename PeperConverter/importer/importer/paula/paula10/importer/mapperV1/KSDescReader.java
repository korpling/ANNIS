package importer.paula.paula10.importer.mapperV1;

import java.io.File;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Liest eine Korpus-Typ Datei vom Typ (typed_korp.dtd) aus und erzeugt daraus die 
 * entsprechenden PDDesc-Objekte. Die PDDesc-Objekte werden in der Reihenfolge 
 * in die Liste geschrieben, in der sie in der Datei stehen.
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class KSDescReader extends DefaultHandler2 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PDDescReader";		//Name dieses Tools
	
	private static final String KW_YES=		"yes";		//Schlüsselwort für ja
	private static final String KW_NO=		"no";		//Schlüsselwort für nein

	//Elementname und Attribute zum Elementknoten korpus
	private static final String TAG_CORP=			"corpus";	//erstes Tag dieses Dokumentes
	private static final String ATT_CORP_NAME=	"name";		//Nmae des Attributes Korpus.name
	
	//Elementname und Attribute zum Elementknoten korpus
	private static final String TAG_DOC=			"document";	//erstes Tag dieses Dokumentes
	private static final String ATT_DOC_NAME=		"name";		//Nmae des Attributes Korpus.name
	
	//Elementname und Attribute zum Elementknoten anno
	private static final String TAG_ANNO=			"anno";		//Name des Tags für den Bereich der Annotationsdaten

	//Elementname und Attribute zum Elementknoten anno
	private static final String TAG_META=			"meat";		//Name des Tags für den Bereich der Metadaten

	//Elementname und Attribute zum Elementknoten file
	private static final String TAG_FILE=			"file";				//einzelner Eintrag
	//private static final String ATT_FILE_NAME=		"name";				//Name des Attributes name
	private static final String ATT_ANATYPE_TYPE=	"analyze_type";		//Name des Attributes analyze_type
	private static final String ATT_FILE_IMP=		"import";			//Name des Attributes import
	private static final String ATT_FILE_PATH=		"path";				//Name des Attributes path
	private static final String ATT_FILE_DTD=		"dtd";				//Name des Attributes DTD
	
	private Locator locator= null;
	
	//von der aufrufenden Klasse übergebene Variablen
	private Vector<PDDesc> pdList= null;		//Liste für die PDDesc-Objekte
	private KSDesc rootKSDesc= null;			//WurzelKorpus
	private Stack<KSDesc> ksDescStack= null;	//Stack mit den KSDesc-Objekten, aktuelles ist immer oben
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_XML_ERR=		MSG_ERR + "There´s an error in the xml-file: ";
	private static final String MSG_XML_ERR_END=	" This error occurs in line, col: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_KORP_NAME=	MSG_ERR + "The Korpus has no name.";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Instanziiert ein PDFileReader-Objekt. Die PDDesc-Objekte werden während des Parsens 
	 * der XML-Datei in ide übergebene Liste geschrieben. Ist diese Liste nicht 
	 * Vorinitialisiert, wird ein Fehler geworfen.
	 * @exception Fehler, wenn Liste nicht initeilisiert ist
	 */
	public KSDescReader() throws Exception
	{
		this.ksDescStack= new Stack<KSDesc>();
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Gibt ein KSDesc-Objekt zurück, dass die Wurzel der Korpora darstellt.
	 * @return KSDesc, dass Wurzel ist
	 */
	public KSDesc getRoot()
		{ return(this.rootKSDesc); }
	//-------------------------------------- SAX -Methoden -------------------------
	public void setDocumentLocator(Locator locator)
	{
		this.locator= locator;
	}
	
	public void startElement(	String uri,
					            String localName,
					            String qName,
					            Attributes attributes) throws SAXException
	{
		try
		{
			//Tag CORP gelesen
			if (qName.equalsIgnoreCase(TAG_CORP))
			{
				String name= null;
				for (int i= 0; i < attributes.getLength(); i++)
					if (attributes.getQName(i).equalsIgnoreCase(ATT_CORP_NAME)) 
						name= attributes.getValue(i);
				if ((name== null) || (name.equalsIgnoreCase("")))
					throw new Exception(ERR_EMPTY_KORP_NAME);
				KSDesc ksDesc= new KSDesc(name);
				//wenn auf Stack KSDesc existiert, dann dieses in das vorhandene anhängen
				if ((!this.ksDescStack.isEmpty())&&(this.ksDescStack.peek()!= null)) this.ksDescStack.peek().addKorpus(ksDesc);
				//aktuelles KorpusDesc-Object auf Stack legen
				this.ksDescStack.push(ksDesc);
				//wenn kein Wurzelkorpus existiert, dann dieses als Wurzel nehmen
				if (this.rootKSDesc== null) this.rootKSDesc= ksDesc; 
			}
			//Tag DOCUMENT gelesen
			if (qName.equalsIgnoreCase(TAG_DOC))
			{
				
			}
			//Tag ANNOS gelesen
			else if (qName.equalsIgnoreCase(TAG_ANNO))
			{
				
			}
			//Tag META gelesen
			else if (qName.equalsIgnoreCase(TAG_META))
			{
				
			}
			//Tag FILE gelesen
			else if (qName.equalsIgnoreCase(TAG_FILE))
			{
				String anaType= null;
				String imp= null;
				String path= null;
				String dtd= null;
				//alle Attribute durchgehen
				for (int i= 0; i < attributes.getLength(); i++)
				{
					//wenn Attribut file.anaType
					if (attributes.getQName(i).equalsIgnoreCase(ATT_ANATYPE_TYPE)) 
						anaType= attributes.getValue(i);
					//wenn Attribut file.import
					else if (attributes.getQName(i).equalsIgnoreCase(ATT_FILE_IMP)) 
						imp= attributes.getValue(i);
					//wenn Attribut file.path
					else if (attributes.getQName(i).equalsIgnoreCase(ATT_FILE_PATH)) 
						path= attributes.getValue(i);
					//wenn Attribut file.dtd
					else if (attributes.getQName(i).equalsIgnoreCase(ATT_FILE_DTD)) 
						dtd= attributes.getValue(i);
				}
				//wenn Datei importiert werden soll
				if (imp.equalsIgnoreCase(KW_YES))
				{
					File paulaFile= new File(path);
					PDDesc pdDesc= new PDDesc(paulaFile, dtd, anaType);
					//neue PDListe erstellen, wenn keine vorhanden
					if (this.pdList== null) this.pdList= new Vector<PDDesc>();
					this.pdList.add(pdDesc);
				}
			}
		}
		catch (Exception e)
		{ throw new SAXException(e.getMessage()); }
	}
	
	public void endElement(	String uri,
            				String localName,
            				String qName) throws SAXException
     {
		//Tag CORP gelesen
		if (qName.equalsIgnoreCase(TAG_CORP))
		{
			// wenn es pdListe gibt, dann anfügen
			if (this.pdList!= null)
				this.ksDescStack.peek().setPDList(this.pdList);
			//oberstes Element von Stack löschen
			this.ksDescStack.pop();
			this.pdList= null;
		}
		//Tag FILE gelesen
		else if (qName.equalsIgnoreCase(TAG_FILE))
		{
			
		}
	 }
	//-------------------------------------- Ende SAX -Methoden -------------------------
	
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	


}
