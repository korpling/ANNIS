//*********************************************** CToolDescriptor ******************************************

package importer.paula.paula10.util.toolDescriptor;

import java.util.Vector;

import java.io.File;
import java.io.PrintStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/**
 * Der CToolDescriptor liest eine XML-Datei aus, die dem Schema description.xsd genügt.
 * Es wird ein CToolDescription Objekt erzeugt und an den Aufrufer zurückgegeben.
 * Enthalten sind der Name des Tools, eine Beschreibung, die Synopsis und die Flags.
 * @author Flo
 * @version 1.0	
 */
public class CToolDescriptor 
{
//	 ============================================== private Variablen ==============================================
	private final static String descFile= "data/description.xml";		//XML-Datei die die Toolbeshcreibung enthält
	private final static String TARGETFILE= "readme.txt";				//Name der Ausgabedatei
	private final static String TOOLNAME= "util.toolDescriptor";		//Name dieses Tools
	/**
	 * Debug-Schalter
	 */
	//private final boolean DEBUG= true;
	private PrintStream msg= null;		//Nachrichtenstream
	
	/**
	 * XML-Datei aus der gelesen wird
	 */
	private File XMLFile= null; 
	
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_START	= "*********** start CToolDescriptior ***********";
	private static final String MSG_END	= "*********** end   CToolDescriptior ***********";
	
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_FILE_NOT_EXIST= 	"ERROR: Sourcefile does not exists.";
	private static final String ERR_CANNOT_READ_FILE=	"ERROR: Can not read sourcefile.";
	private static final String ERROR_SOURCE_NOT_WF=	"ERROR: Sourcefile is not wellformed: ";
	private static final String ERR_TOOL_NOT_FOUND=		"ERROR: toolname not found in xml-file.";
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt ein CToolDescriptor Objekt.
	 * @param fileName - XML-Datei die dem Schema description.xsd genügt 
	 */
	public CToolDescriptor(String fileName) throws Exception
	{
		createFile(fileName);
	}
	
	/**
	 * Erzeugt ein CToolDescriptor Objekt und setzt den Nachrichtenstream auf den übergebenen.
	 * Bei der Abarbeitung werden Meldungen auf diesem Stream ausgegeben.
	 * @param fileName String - XML-Datei die dem Schema description.xsd genügt 
	 * @param msgStream PrintStream - Stream auf dem Nachrichten während der Abarbeitung ausgegeben werden
	 */
	public CToolDescriptor(String fileName, PrintStream msgStream) throws Exception
	{
		this.msg= msgStream;
		createFile(fileName);
	}
	
//	 ============================================== private Methoden ==============================================
	
	/**
	 * Prüft eine Quelldatei auf ihre Existenz und Lesbarkeit
	 */
	private void createFile(String fileName) throws Exception
	{
		if (msg != null) msg.print("entering checkFile("+ XMLFile +"): ");
		
		File XMLFile= new File(fileName);
		if (!XMLFile.exists()) throw new Exception(ERR_FILE_NOT_EXIST);
		if (!XMLFile.canRead()) throw new Exception(ERR_CANNOT_READ_FILE);
		
		if (msg != null) msg.println("OK (input file ok)");
		this.XMLFile= XMLFile;
	}
	
	/**
	 * Erzeugt alle nötigen Objekte zum parsen der XML-Datei und gibt ein Objekt vom Typ 
	 * CToolDescriptor zurück, dass die Eigenschaften der XML-Datei enthält.
	 */
	private void getDesc(CDescReader contentHandler) throws Exception
	{
		SAXParser parser;
        XMLReader reader;

        try
        {
                final SAXParserFactory factory= SAXParserFactory.newInstance();
                parser= factory.newSAXParser();
                reader= parser.getXMLReader();
        
                //contentHandler setzen
                reader.setContentHandler(contentHandler);
                         
                //XML-konforme Datei durch reader parsen 
                reader.parse(this.XMLFile.toString());            
        }
	 	catch (SAXParseException e)
	 		{ throw new Exception(ERROR_SOURCE_NOT_WF+ e.getMessage()); }    
    }
	
//	 ============================================== öffentliche Methoden ==============================================	
	/**
	 * Gibt dieses CToolDescriptor-Objekt als String zurück.
	 * @return CToolDescriptor als String
	 */
	public String toString()
	{
		String retStr= "";
		retStr= retStr + "XML file:\t" + this.XMLFile.toString() + "\n";
		return(retStr);
	}
	
	/**
	 * Liest die im Konstruktor angegebene XML Datei ein und erstellt für jedes in 
	 * der Datei angegebene Tool ein Objekt vom Typ CToolDescription und gibt diese zurück.
	 * @return Beschreibung des Tools in Form eines CToolDescription-Objekts 
	 */
	public Vector<CToolDescription> getDescription() throws Exception
	{
		if (msg != null) msg.println("entering getDescription()");
		//CTooldescription-Vektor erzeugen
		Vector<CToolDescription> descVector= new Vector<CToolDescription>();
		//ContentHandler erzeugen
		CDescReader contentHandler= new CDescReader(descVector, this.msg); 
		//parsen der XML-Datei und füllen des Vektors
		getDesc(contentHandler);
		return(descVector);
	}
	
	/**
	 * Liest die im Konstruktor angegebene XML Datei ein und erstellt für jedes 
	 * angegebene Tool ein Objekt vom Typ CToolDescription und gibt diese zurück.
	 * @return Beschreibung des Tools in Form eines CToolDescription-Objekts 
	 */
	public Vector<CToolDescription> getDescription(Vector<String> toolNames) throws Exception
	{
		if (msg != null) msg.println("entering getDescription("+ toolNames +")");
		//CTooldescription-Vektor erzeugen
		Vector<CToolDescription> descVector= new Vector<CToolDescription>();
		//ContentHandler erzeugen
		CDescReader contentHandler= new CDescReader(descVector, this.msg, toolNames); 
		//parsen der XML-Datei und füllen des Vektors
		getDesc(contentHandler);
		return(descVector);
	}
	
	
	/**
	 * Liest die im Konstruktor angegebene XML Datei ein und erstellt ein Objekt vom Typ
	 * CToolDescription und gibt dieses zurück.
	 * @param 	toolName String - gibt an, welches Tool gelesen werden soll, ist das tool mit dem Namen Toolname nicht enthalten,
	 * 			wird null zurückgegeben
	 * @return Beschreibung des Tools in Form eines CToolDescription-Objekts 
	 */
	public CToolDescription getDescription(String toolName) throws Exception
	{
		if (msg != null) msg.println("entering getDescription("+toolName+")");
		
		//toolName in einen Vektor stecken
		Vector<String> nameVector= new Vector<String>();
		nameVector.add(toolName);
		Vector<CToolDescription> descs =getDescription(nameVector);
		return(descs.firstElement());
	}
//	 ============================================== main Methode ==============================================	
	
	public static void main(String args[])
	{
		System.out.println(MSG_START);
		boolean printHelp= false;	//flag ob Hilfe ausgegeben werden soll
		PrintStream msgStr= null;	//Nachrichtenstream (für Eingabeflags)
		String sourceFile= "";		//Quelldatei
		String toolName= null;		//Name des auszulesenden Tools
		
		if (args.length== 0) printHelp= true;
		
		for (int i=0; i < args.length; i++)
		{
			// Hilfe ausgeben
			if (args[i].equalsIgnoreCase("-h")) printHelp= true;
			// Messages ausgeben
			else if (args[i].equalsIgnoreCase("-ct")) msgStr= System.out;
			// sourcefile
			else if (args[i].equalsIgnoreCase("-s"))
			{
				if (args.length < i+1) printHelp= true;
				else
				{
					sourceFile= args[i+1];
					i++;
				}
			}
			//Toolname
			else if (args[i].equalsIgnoreCase("-t"))
			{
				if (args.length < i+1) printHelp= true;
				else
				{
					toolName= args[i+1];
					i++;
				}
			}
		}
		
		try
		{
			//Descriptor für dieses Tool
			CToolDescriptor thisDescriptor= new CToolDescriptor(descFile);
			CToolDescription thisDescripton= thisDescriptor.getDescription(TOOLNAME);
			if (printHelp)
			{
				if (msgStr != null) msgStr.println(thisDescripton.toString());
				else System.out.println(thisDescripton.toString());
				System.out.println(MSG_END);
				return;
			}
			
			// Descriptor für das Tool, zu dem eine Readme erzeugt werden soll
			CToolDescriptor descriptor= new CToolDescriptor(sourceFile, msgStr);
			CToolDescription desc= descriptor.getDescription(toolName);
			//CToolDescription desc= descriptor.getDescription().firstElement();
			if (desc== null)
			{
				if (msgStr != null) msgStr.println(ERR_TOOL_NOT_FOUND);
				else System.out.println(ERR_TOOL_NOT_FOUND);
			}
			else desc.toTXT(TARGETFILE);
		}
		catch(Exception e)
			{ e.printStackTrace(); }
		System.out.println(MSG_END);
	}
}
