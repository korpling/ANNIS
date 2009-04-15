// ************************************* Klasse CDescReader **********************************

package util.toolDescriptor;

import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.Attributes;

import java.util.Vector; 
import java.util.Stack;

import java.io.PrintStream;

/** 
 * <p>
 * 	Die Klasse CDescReader ist abgeleitet von der Klasse DefaultHandler2. Sie liest eine XML-Datei
 *  aus, die dem Schema description.xsd genügt. Aus dem Inhalt der XMLDatei wird die Beschreibung 
 *  zu einem Tool geldes und in ein CDescription Objekt geschrieben. Es kann der Inahlt zu einem, 
 *  einer Menge von Tools oder allen der in der XML-Datei enthaltenen Tools gelesen werden. 
 * </p>
 * 
 * @author Flo
 * @version 1.0
 */
public class CDescReader extends DefaultHandler2
{
//	 ============================================== private Variablen ==============================================
	//private boolean DEBUG= true;						//Debug-Schalter
	private PrintStream msg= null;						//NachrichtenStream
	private Vector<String> toolNames= null;				//Liste der noch zu lesenden Tools
	private Vector<CToolDescription> toolDescs= null;	//Liste der ToolDescriptions
	private Stack<String> tagStack= null;				//Stack zum speichern der aktuellen Tags
	private CToolDescription currDesc= null;			//aktuelles CToolDescription Objekt, dass beschrieben wird.
	private CFlag currFlag= null;						//aktuelles CFlag Objekt, dass beschrieben wird
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Liest alle in der XML-Datei enthaltenen Tools aus und schreibt sie in CToolDescription-Objekte
	 * diese werden in dem übergebenen Vektor abgelegt.
	 * @param toolDescs Vektor in dem die gefüllten CToolDescription Objekte abgelegt werden. 
	 */
	public CDescReader(Vector<CToolDescription> toolDescs, PrintStream msgStream)
	{
		this.msg= msgStream;
		this.toolDescs= toolDescs;
		tagStack= new Stack<String>();
	}
	
	/**
	 * Liest alle angegebenen Tools aus der XML-Datei aus und schreibt sie in 
	 * CToolDescription-Objekte. Diese werden in dem übergebenen Vektor abgelegt.
	 * @param toolDescs toolDescs Vektor in dem die gefüllten CToolDescription Objekte abgelegt werden.
	 * @param toolNames Namen der Tools, deren Beschreibung gelesen werden soll.
	 */
	public CDescReader(Vector<CToolDescription> toolDescs, PrintStream msgStream, Vector<String> toolNames)
	{
		this(toolDescs, msgStream);
		this.toolNames= toolNames;
	}
	
	/**
	 * Liest ein übergebenes tool aus der XML-Datei aus und schreibt es in ein 
	 * CToolDescription-Objekt.
	 * @param toolDesc Objekt in das die Daten aus der XML Datei geschrieben werden 
	 * @param toolName Name des Tools, dessen Beschreibung gelesen werden soll.
	 **/
	/*
	public CDescReader(CToolDescription toolDesc, String toolName)
	{
		
	}
	*/
	
//	 ============================================== private Methoden ==============================================
	/**
	 * Prüft ob der übergebene Toolname zur Liste der Tools gehört, die ausgelesen werden sollen.
	 * Also ob toolName in toolNames vorkommt
	 * @param toolName String - Name des zu überprüfenden Tools.
	 */
	private boolean isInteresting(String toolName)
	{
		// alle Tools sollen ausgelesen werden
		if (toolNames == null) return(true);
		
		// Tool gehört zur interessanten Menge
		return(this.toolNames.contains(toolName));
	}
//	 ============================================== öffentl. Methoden ==============================================

	/** 
	 * TODO: jedes Enter in der XML Datei bedeutet, dass erneut diese FKT aufgerufen wird, 
	 * ebenso Sonderzeichen, also muss der Text noch zusammengeklatscht werden. 
	 * @see org.xml.sax.ext.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
	{
		//Wenn Tag zu Ende ist entstehen leere Character
		String text= new String(ch).substring(start,start+length).trim();
		if (text.length() == 0) return;
		//System.out.println("los: \t"+text);
		
		// SYNOPSIS-Tag ist aktuell
		if (this.tagStack.peek().equalsIgnoreCase("SYNOPSIS")) 
		{
			if (this.currDesc != null) this.currDesc.setSynopsis(text);
		}
		// DESC-Tag ist aktuell
		else if (this.tagStack.peek().equalsIgnoreCase("DESC")) 
		{
			if (this.currDesc != null) this.currDesc.setDesc(text);
			//System.out.println(this.currDesc.getDesc());
		}
		// FLAG-Tag ist aktuell
		else if (this.tagStack.peek().equalsIgnoreCase("FLAG")) 
		{
			if (this.currFlag != null) this.currFlag.setDesc(text);
		}
	}
	
	/** 
	 * @see org.xml.sax.ext.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	{
		this.tagStack.add(qName);
		if (this.msg != null) this.msg.println("<"+qName+">");
		
		// TOOL-Tag wurde gelesen
		if (qName.equalsIgnoreCase("TOOL"))
		{
			// Attribute durchgehen
	    	for (int i= 0; i< attributes.getLength(); i++)
	        { 
	    		// NAME-Attribut gelesen
	    		if (attributes.getQName(i).equalsIgnoreCase("NAME")) 
	    		{
	    			if (this.msg != null) this.msg.println("read tool: "+attributes.getValue(i));
	    			if (this.isInteresting(attributes.getValue(i)))
	    			{
	    				//interessantes Tool gelesen
	    				this.currDesc= new CToolDescription(); 
	    				currDesc.setName(attributes.getValue(i));		
	    			}
	    		}
	    		// VERSION-Attribut gelesen
	    		if (attributes.getQName(i).equalsIgnoreCase("VERSION"))
	    			if (this.currDesc != null) this.currDesc.setVersion(attributes.getValue(i)); 
	        }	
		}
		//AUTHOR-Tag gelesen
		else if (qName.equalsIgnoreCase("AUTHOR"))
		{
			CAuthor author= new CAuthor();
			// Attribute durchgehen
	    	for (int i= 0; i< attributes.getLength(); i++)
	        { 
	    		// NAME-Attribut gelesen
	    		if (attributes.getQName(i).equalsIgnoreCase("NAME")) 
	    			{ author.setName(attributes.getValue(i));}
	    		else if (attributes.getQName(i).equalsIgnoreCase("EMAIL")) 
    				{ author.setEMail(attributes.getValue(i));}
	        }
	    	if (this.currDesc != null) this.currDesc.addAuthor(author);
		}
		//FLAG-Tag gelesen
		else if (qName.equalsIgnoreCase("FLAG"))
		{
			//Attribute durchgehen
	    	for (int i= 0; i< attributes.getLength(); i++)
	        { 
	    		// NAME-Attribut gelesen
	    		if (attributes.getQName(i).equalsIgnoreCase("NAME")) 
	    			{ this.currFlag= new CFlag(attributes.getValue(i));}
	    		
	    		//ISOPTIONAL-Attribut
	    		else if (attributes.getQName(i).equalsIgnoreCase("ISOPTIONAL"))
	    		{ 
	    			boolean isOptional= false;
	    			if (attributes.getValue(i).equalsIgnoreCase("TRUE")) isOptional= true; 
	    			if (this.currFlag!= null) this.currFlag.setIsOptional(isOptional); 
	    		}
	    		
	    		//APPENDIX-Attribut
	    		else if (attributes.getQName(i).equalsIgnoreCase("APPENDIX"))
	    			{ if (this.currFlag != null) this.currFlag.setAppendix(attributes.getValue(i)); }
	        }	
		}
	}
	
	/** 
	 * @see org.xml.sax.ext.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
	{
		//TOOL-Tag gelesen
		if (qName.equalsIgnoreCase("TOOL"))
		{
			if (this.msg != null) this.msg.println("tool ende");
			this.toolDescs.add(this.currDesc);
			this.currDesc= null;
		}
		// FLAG-Tag gelesen
		if (qName.equalsIgnoreCase("FLAG"))
		{
			if (this.currDesc != null) this.currDesc.addFlag(this.currFlag);
			this.currFlag= null;
		}
		tagStack.pop();
	}

}
