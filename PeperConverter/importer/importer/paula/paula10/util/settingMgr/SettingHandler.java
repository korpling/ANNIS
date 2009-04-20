package importer.paula.paula10.util.settingMgr;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * <p>
 * Die Klasse SettingHandler ist ein Bestandteil und dient dem Auslesen einer XML-Datei und
 * der Weiterleitung der durch den SAX-Parser aufgerufenen Methoden an die entsprechenden Objekte.
 * Diese Objekte werden in Form einer Tabelle an die Klasse SettingHandler übergeben. 
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
public class SettingHandler extends DefaultHandler2
{
//	 ============================================== private Variablen ==============================================
	//private static final boolean DEBUG= true;	//DEBUG-Schalter
	private static final String CLASSNAME= "SettingHandler";	//Name der Klasse
	
	private static final String KW_SETEL=	"setting";		//Keyword für Setting Element
	private static final String KW_ID=		"id";			//Keyword für id
	private static final String KW_XML_ID=	"xml:id";		//Keyword für id
	private static final String KW_NS=		"xmlns:";		//Keyword für Namespace
	private static final String KW_DEL_NS=	":";			//Delimiter für Namespaces und Elemente
	
	private static final String NS_SETMGR=	"irgendwo";		//Namensraum des XML-Files des SetMgr
	
	private Hashtable<String,String>  shortNS	= null;						//Hashtable für verwendete Namespacekürzel
	
	private PrintStream msgStream= null;		//Stream zur Nachrichtenausgabe
	private Hashtable<String, DefaultHandler2> setTable= null;	//Tabelle in der die ZUordnung von Objekt und Settingnamen liegt
	private Vector<DefaultHandler2> currSetLists= null;			//Liste mit den aktuell zu benachrichtigen SettingListenern
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_METHOD_CALL=	"setMgr: start of method: ";
	private static final String MSG_START=			"setMgr: Class " + CLASSNAME + " successfully initialized.";
	private static final String MSG_NS=				"setMgr: \t>namespace found with (short, long): ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_SETTABLE=	"ERROR("+CLASSNAME+"): "+CLASSNAME+" couldn´t be initialized. No object to read in or no source name given.";
	private static final String ERR_NS_DEL=			"ERROR("+CLASSNAME+"): Syntax error, the namespace delimiter:'"+KW_DEL_NS+"' was used to often.";
	private static final String ERR_NO_ID=			"ERROR("+CLASSNAME+"): No id-value is given. This is necassary for element type: "+ KW_SETEL;
	private static final String ERR_NS_NOT_DECL=	"ERROR("+CLASSNAME+"): Namespace was not declared: ";
//	 ============================================== öffentl. Variablen ==============================================
//	 ============================================== Konstruktoren ==============================================
	/**
	 * simpelster Konstruktor der Klasse SettingHandler
	 */
	public SettingHandler(Hashtable<String, DefaultHandler2> setTable) throws Exception
	{
		if ((setTable == null) || (setTable.isEmpty())) throw new Exception(ERR_NO_SETTABLE);
		this.setTable= setTable;
		//Liste der akuellen ObjektListener instantiieren
		this.currSetLists = new Vector<DefaultHandler2>();
		//Tablle der Namespaces (short, long) instantiieren
		this.shortNS= new Hashtable<String, String>();
	}
	
	/**
	 * Instanziiert ein SettingHandler-Objekt.
	 * @param msgStream - Printstream, Stream zur Nachrichtenausgabe
	 */
	public SettingHandler(	Hashtable<String, DefaultHandler2> setTable,
							PrintStream msgStream) throws Exception
	{
		this(setTable);
		if (msgStream != null) this.msgStream= msgStream;
		
		if (msgStream != null) msgStream.println(MSG_START);
		
	}
//	 ============================================== private Methoden ==============================================
	
	/**
	 * Gibt aus einem gegebenen Lang-Element-Namen (qname) den eigentlichen elementnamen 
	 * (ohne Namensraum Prefix) und den Namensraum zurück (in langer Form).
	 * @param qName - String, qName eines Elements (analog zu SAX)
	 * @return -String[], bestehend aus zwei Elementen erstens dem Elementname und zweitensdem langen Namensraum 
	 * @exception wenn das Namensraumprefix nicht definiert wurde
	 */
	private String[] getElemInfo(String qName) throws SAXException
	{
		//	trenne Elementnamen von Namespaceprefix
		String[] parts= qName.split(KW_DEL_NS);
		//es darf nur einen namespace delimiter pro element geben
		if (parts.length > 2) throw new SAXException(ERR_NS_DEL);
		String ns= "";		//Namensraumkürzel
		String elemName= "";	//bereinigter Elementname
		String fullNS= null;	//voller Namensraum (kein Kürzel)
		//wenn Element einen Namensraum besitzt
		if (parts.length== 2)
		{
			ns= parts[0];		//Namesnraum des Elements
			elemName= parts[1];		//bereinigter Elementname
			fullNS= this.shortNS.get(ns);
			//wenn Element einen Namespace benutzt, der nicht deklariert wurde
			if (fullNS == null) throw new SAXException(ERR_NS_NOT_DECL + ns);
		}
		return(new String[] {elemName, fullNS});
	}
//	 ============================================== öffentl. Methoden ==============================================
//	 ************************************ SAX-Methoden ************************************
  
   /**
    * org.xml.sax.ext.DefaultHandler#startElement(String uri, String localName, String qName, Attributes attributes)
    */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
    	if (msgStream != null) msgStream.println(MSG_METHOD_CALL + "startElement");
    	String idValue= "";		//ID-Wert dieses Knotens, wenn es ihn gibt
    	
    	//überprüfe Attribute auf Namespaces und id Werte
    	//gehe durch alle Attribute
		for (int i= 0; i< attributes.getLength(); i++)
		{
			String firstX= attributes.getQName(i); //ersten X Zeichen eines Attributnamens;
			//könnte Namespace-Attribut sein
			if (firstX.length() >= KW_NS.length())
			{
				firstX = firstX.substring(0, KW_NS.length());
				//Attribut ist Namespaceattribut
				if (firstX.equalsIgnoreCase(KW_NS))
				{
					String ns_long= attributes.getValue(i);
					String ns_short= attributes.getQName(i).substring(KW_NS.length());
					this.shortNS.put(ns_short,ns_long);
					if (msgStream != null) msgStream.println(MSG_NS + ns_short +", "+  ns_long);
				}
			}
			//wenn es ein Id-Attribut gibt
			if ((attributes.getQName(i).equalsIgnoreCase(KW_ID)) ||(attributes.getQName(i).equalsIgnoreCase(KW_XML_ID)))
			{	
				idValue= attributes.getValue(i);
			}
		}
		
		String[] info= getElemInfo(qName);
		String elemName= info[0];	//bereinigter Elementname
		String fullNS= info[1];	//ganzer namensraum
		
		if (msgStream != null) msgStream.println("setMgr: \t>element name: "+elemName+ ", element ns (long):" + fullNS + ", element id: "+ idValue);
		//prüfen ob Element aus Namensraum SetMgr kommt
		if ((fullNS != null) && (fullNS.equalsIgnoreCase(NS_SETMGR)))
		{
			// wenn Element ein Settingelement ist
			if (elemName.equalsIgnoreCase(KW_SETEL))
			{
				//Settingelement muss ID-Wert besitzen
				if (idValue.equalsIgnoreCase("")) throw new SAXException(ERR_NO_ID);
				
				DefaultHandler2 currSetList= this.setTable.get(idValue);
				//wenn es einen Listener zur aktuellen id gibt, füge ihn der aktuellen Liste hinzu
				if (currSetList != null) this.currSetLists.add(currSetList);
			}
		}
    	//Element ist nicht aus namespace SETMGR
    	else
    	{
    		for (int i= 0; i < this.currSetLists.size(); i++)
    		{
    			DefaultHandler2 df2= this.currSetLists.get(i);
    			
    			//wenn es einen Handler gibt, so soll diser aufgerufen werden
    			if (df2 != null)
    			{
    				df2.startElement(uri, localName, qName, attributes);
    			}
    		}
    	}
    }
    
    /**
     *  org.xml.sax.ext.DefaultHandler#endElement(String namespaceURI, String localName, String qName)
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
		//Namensraum des Elementes
		String[] info= getElemInfo(qName);
		String elemName= info[0];
		String fullNS= info[1];
		//prüfen ob Element aus Namensraum SetMgr kommt
		if ((fullNS != null) && (fullNS.equalsIgnoreCase(NS_SETMGR)))
		{
			if (elemName.equalsIgnoreCase(KW_SETEL))
			{
				//Listener aus aktueller Liste löschen, wenn vorhanden
				if (this.currSetLists.size() > 0)
					this.currSetLists.removeElementAt(this.currSetLists.size()-1);
			}
		}
		//Element ist normales elemenet
		else
		{
			for (int i= 0; i < this.currSetLists.size(); i++)
    		{
    			DefaultHandler2 df2= this.currSetLists.get(i);
    			//wenn es einen Handler gibt, so soll diser aufgerufen werden
    			if (df2 != null) df2.endElement(namespaceURI, localName, qName);
    		}
		}
    	if (msgStream != null) msgStream.println(MSG_METHOD_CALL+ "endElement");
    }

//	 ************************************ weiterleitende SAX-Methoden ************************************
    
	/**
	 * see org.xml.sax.ext.DefaultHandler#startDocument()
	 */
	public void startDocument() throws SAXException
	{
		if (msgStream != null) msgStream.println(MSG_METHOD_CALL+ "startDocument");
		for (int i= 0; i < this.currSetLists.size(); i++)
 		{
 			DefaultHandler2 df2= this.currSetLists.get(i);
 			//wenn es einen Handler gibt, so soll diser aufgerufen werden
 			if (df2 != null) df2.startDocument();
 		}
	}
	
	/**
	 * see org.xml.sax.ext.DefaultHandler#endDocument()
	 */
	public void endDocument() throws SAXException
	{
		if (msgStream != null) msgStream.println(MSG_METHOD_CALL+ "endDocument");
		for (int i= 0; i < this.currSetLists.size(); i++)
 		{
 			DefaultHandler2 df2= this.currSetLists.get(i);
 			//wenn es einen Handler gibt, so soll diser aufgerufen werden
 			if (df2 != null) df2.endDocument();
 		}
	}
	
	/**
	 * see org.xml.sax.ext.DefaultHandler#startEntity(String name)
	 */
	public void startEntity(String name) throws SAXException
	{
		if (msgStream != null) msgStream.println(MSG_METHOD_CALL+ "startEntity");
		for (int i= 0; i < this.currSetLists.size(); i++)
 		{
 			DefaultHandler2 df2= this.currSetLists.get(i);
 			//wenn es einen Handler gibt, so soll diser aufgerufen werden
 			if (df2 != null) df2.startEntity(name);
 		}
	}
	
	/**
	 * see org.xml.sax.ext.DefaultHandler#startEntity(String name)
	 */
	public void endEntity(String name) throws SAXException
	{
		if (msgStream != null) msgStream.println(MSG_METHOD_CALL+ "endEntity");
		for (int i= 0; i < this.currSetLists.size(); i++)
 		{
 			DefaultHandler2 df2= this.currSetLists.get(i);
 			//wenn es einen Handler gibt, so soll diser aufgerufen werden
 			if (df2 != null) df2.endEntity(name);
 		}
	}
	
	/**
     * org.xml.sax.ext.DefaultHandler#comment(char[] ch, int start, int length)
     */
	public void comment(char[] ch, int start, int length) throws SAXException
    {
		if (msgStream != null) msgStream.println(MSG_METHOD_CALL+ "comment");
		for (int i= 0; i < this.currSetLists.size(); i++)
 		{
 			DefaultHandler2 df2= this.currSetLists.get(i);
 			//wenn es einen Handler gibt, so soll diser aufgerufen werden
 			if (df2 != null) df2.comment(ch, start, length);
 		}
	}
	
	 /**
     * Receive notification of a processing instruction.
     * @see org.xml.sax.ContentHandler#processingInstruction(String,String)
     **/
    public void processingInstruction(String target, String data) throws SAXException
    {
    	if (msgStream != null) msgStream.println(MSG_METHOD_CALL+ "processingInstruction");
    	for (int i= 0; i < this.currSetLists.size(); i++)
 		{
 			DefaultHandler2 df2= this.currSetLists.get(i);
 			//wenn es einen Handler gibt, so soll diser aufgerufen werden
 			if (df2 != null) df2.processingInstruction(target, data);
 		}
	}

    /**
     * org.xml.sax.ext.DefaultHandler#characters(char[] ch, int start, int length)
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
	   if (msgStream != null) msgStream.println(MSG_METHOD_CALL+ "characters");
	   for (int i= 0; i < this.currSetLists.size(); i++)
		{
			DefaultHandler2 df2= this.currSetLists.get(i);
			//wenn es einen Handler gibt, so soll diser aufgerufen werden
			if (df2 != null) df2.characters(ch, start, length);
		}
    }
 
}
