package importer.paula.paula10.util.settingMgr;


import java.io.File;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
/**
 * <p>
 * Die Klasse SettingMgr liest eine XML-Datei aus, die dem Schema setMgr.xsd genügt. In dieser 
 * Datei können Einstellungen an SettingListener gebunden werden. Diese Einstellungen werden vom
 * SttingMgr ausgelesen und an die entsprechenden SettingListener übergeben. Ein SettingListener 
 * muss von der Klasse DefaultHandler2 abgeleitet sein.</br>
 * Der SettingMgr basiert auf SAX und reicht die entsprechenden Methoden nur an den SettingListener weiter.
 * es werden folgende SAX-Methoden unterstützt.
 * </p>
 * Unterstützt:
 * <ol>
 * 	<li>startDocument</li>
 * 	<li>endDocument</li>
 * 	<li>startElement</li>
 * 	<li>endElement</li>
 * 	<li>startEntity</li>
 * 	<li>endEntity</li>
 * 	<li>processingInstruction</li>
 * 	<li>comment</li>
 * 	<li>characters</li>
 * </ol>
 * @author Flo
 * @version 1.0
 */
public class SettingMgr 
{
//	 ============================================== private Variablen ==============================================
	private static final boolean DEBUG= true;	//DEBUG-Schalter
	private static final String CLASSNAME= "SettingMgr";	//Name der Klasse

	private PrintStream msgStream= null;						//Stream zur Nachrichtenausgabe
	private Hashtable<String, DefaultHandler2> setTable= null;	//Tabelle in der die Zuordnung von Objekt und Settingnamen liegt
	private String fileName= "";								//Name der Input XML-Datei
	private Vector<SettingObject> setObjects= null;				//hält alle SettingObjects in einer Liste
	
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_FILE_NAME			= "name of input file:\t";
	private static final String MSG_NUM_LISTENER		= "numberof listener:\t";
	private static final String MSG_LIST				= "list of setting listener:\n";
	private static final String MSG_WORKING				= "parsing sourcefile, please wait...";
	private static final String MSG_INIT				= CLASSNAME + " initialized.";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_FILENAME				= "ERROR ("+CLASSNAME+"):"+CLASSNAME+" könnte nicht initialisiert werden. Keine InputXML-Datei angegeben.";
	private static final String ERROR_SOURCE_NOT_WF			= "ERROR ("+CLASSNAME+"): Sourcefile is not wellformed: ";
	private static final String ERR_NO_FILE					= "ERROR ("+CLASSNAME+"): Sourcefile does not exist or is not readable: ";
	private static final String ERR_METHOD_NOT_IMPLEMENTED	= "ERROR ("+CLASSNAME+"): The method getSetEntry() is not correct implemented.";
	private static final String ERR_NO_SETLISTENER			= "ERROR ("+CLASSNAME+"): No SettingListener was given.";
	private static final String ERR_NO_SETNAME				= "ERROR ("+CLASSNAME+"): An empty SettingName was given.";
//	 ============================================== öffentl. Variablen ==============================================
//	 ============================================== Konstruktoren ==============================================
	/**
	 * einfachster Konstruktor
	 * @param fileName - String Name der XML-Datei, in der das Setting enthalten ist
	 */
	public SettingMgr(String fileName) throws Exception
	{ 
		this.setTable= new Hashtable<String, DefaultHandler2>();
		
		if (DEBUG) System.out.println(fileName);
		
		if ((fileName == null) ||(fileName.equalsIgnoreCase(""))) throw new Exception(ERR_NO_FILENAME);
		File file = new File(fileName);
		if (!(file.exists()) || (!file.canRead()))
			throw new Exception(ERR_NO_FILE + file.getCanonicalPath());
		
		this.fileName= fileName;
		//setzt die Liste der SettingObject
		this.setObjects= new Vector<SettingObject>();
	}
	/**
	 * 
	 * Instanziiert ein SettingMgr-Objekt.
	 * @param msgStr - Printstream, ein Strom zur Nachrichtenausgabe
	 * @param fileName - String Name der XML-Datei, in der das Setting enthalten ist
	 * @throws Exception
	 */
	
	public SettingMgr(String fileName, PrintStream msgStr) throws Exception
	{ 
		this(fileName);
		this.msgStream= msgStr;
		
		if (this.msgStream != null) msgStr.println(MSG_INIT);
	}
//	 ============================================== private Methoden ==============================================
	
	/**
	 * Prüft ob eine Datei mit dem gegebenen Namen existsieren.
	 * @param fileName - String, Name der Datei deren Existenz geprüft wird
	 * @return true, wenn Datei existiert, false sonst
	 */
	private boolean checkFile(String fileName)
	{
		boolean retValue= false;
		
		File file = new File(fileName);
		if ((file.exists()) && (file.canRead())) retValue= true; 
			
		return(retValue);
	}
//	 ============================================== öffentl. Methoden ==============================================
	
	/**
	 * Fügt dem SettingMgr ein Listener Objekt des Typs DefaultHandler2 hinzu, die Methoden 
	 * dieses Objektes werden aufgerufen, wenn ein Setting-element den Settingnamen 
	 * (setName) aufweist.
	 * @param setListener - DefaultHandler2, Listener Objekt, an dass der Inhalt der XML-Datei weiteregegeben wird
	 * @param setName - String, Name des SettingElements für das sich der Listener anmeldet 
	 */
	public void addSetListener(DefaultHandler2 setListener, String setName) throws Exception
	{
		//Fehler wenn setName leer
		if ((setName == null) || (setName.equalsIgnoreCase(""))) throw new Exception(ERR_NO_SETNAME);
		//Fehler, wenn Listener leer
		if (setListener == null) throw new Exception(ERR_NO_SETLISTENER);
		this.setTable.put(setName, setListener);
	}
	
	/**
	 * Fügt Settinglistener und Settingnamen in die Liste der zu überwachenden Elemente ein.
	 * Dafür wird die Methode getSetEntry() des SettingObject aufgerufen und alle in der 
	 * Tabelle befindlichen Dettinglistener werden mit den dazugehörigen Settingnamen in die 
	 * Überwachungsliste eingefügt
	 * @param obj SettingObj - Objekt, das die Methode getSetEntry() implementiert, Ist SettingObject leer, passiert nichts
	 * @exception Fehler, wenn SettingObject die Methode getSetEntry() nicht implementiert
	 */
	public void addSetListener(SettingObject obj) throws Exception
	{
		if (obj != null)
		{
			this.setObjects.add(obj);
			Hashtable<DefaultHandler2, Vector<String>> table= obj.getSetEntry();
			//Fehler, wenn methode getSetEntry nicht implementiert ist
			if (table == null) throw new Exception(ERR_METHOD_NOT_IMPLEMENTED); 
			DefaultHandler2 handler= null;
			Vector<String> setNames= null;
			Enumeration<DefaultHandler2> handlers= table.keys();
			//gehe durch alle SettingListener in der Tabelle
			while (handlers.hasMoreElements())
			{
				handler= handlers.nextElement();
				setNames= table.get(handler);
				//Gehe durch alle SettingNamen zu einem SettingListener
				for (String setName: setNames)
					this.addSetListener(handler, setName);
			}
		}
	}
	
	/**
	 * Startet das Auslesen der XML-Datei und ruft die entsprechenden Methoden der Listener-
	 * Objekte auf.
	 */
	public void start() throws Exception
	{
		//nur ausführen, wenn überhaupt SettingListener vorhanden sind
		if (!this.setTable.isEmpty())
		{
			SAXParser parser;
	        XMLReader reader;
	        SettingHandler contentHandler;
	
	        try
	        {
	                final SAXParserFactory factory= SAXParserFactory.newInstance();
	                parser= factory.newSAXParser();
	                reader= parser.getXMLReader();
	
	                //contentHandler erzeugen und setzen
	                contentHandler= new SettingHandler(this.setTable, this.msgStream);
	                reader.setContentHandler(contentHandler);
	                
	                if (msgStream != null) msgStream.println(MSG_WORKING);
	                
	                //XML-konforme Datei durch reader parsen 
	                reader.parse(this.fileName);
	        }
		 	catch (SAXParseException e1)
		 		{ throw new Exception(ERROR_SOURCE_NOT_WF+ e1.getMessage()); }
		 	
		 	//gib allen SetObjects Bescheid, dass SettingMgr alle Settings weitergeleitet hat
		 	for (SettingObject setObj: this.setObjects)
		 		setObj.readSettings();
		}
	}
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück
	 * @return String, Objektinformationen 
	 */
	public String toString()
	{
		String str= "";
		
		str= str + MSG_FILE_NAME + this.fileName + "\n";
		str= str + MSG_NUM_LISTENER + this.setTable.size() + "\n";
		str= str + MSG_LIST + "\n";
		Enumeration<String> setNames=  this.setTable.keys();
		while (setNames.hasMoreElements())
		{
			str= str + "\t>"+ setNames.nextElement();
		}
		
		return(str);
	}
}
