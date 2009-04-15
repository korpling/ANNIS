package util.xPointer;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import util.xPointer.XPtrRef.POINTERTYPE;


/**
 * Die Klasse XPtrInterpreter stellt Objekte zur Verfügung, denen XPointer übergeben werden 
 * können und von den Objekten interprtiert werden. Ein Objekt dieser Klasse gibt eine 
 * geordnete Menge von XPointerReferenzen zurück, die dem gegebenen XPointer entsprechen.
 * Verarbeitbare XPointertypen sind: Einzelelemente, Elementbereiche, Elementsequenzen.<br/>
 * Die unterstützte Syntax:<br/>
 * KW_SHARP		:=	#<br/>
 * KW_XPTR		:=	KW_SHARP xpointer<br/>
 * KW_RANGE		:=	/range-to<br/>
 * KW_LBRACE	:= 	(<br/>
 * KW_RBRACE	:= 	)<br/>
 * <br/>
 * idVal			:=	[A-Za-z0-9_-]<br/>
 * ShorthandPtr		:=	KW_SHARP idVal<br/>
 * idPtr			:= 	id KW_LBRACE 'idVal' KW_RBRACE<br/>
 * range			:= 	idPtr KW_RANGE KW_LBRACE idPtr KW_RBRACE<br/>
 * <br/>
 * Einzelelement	:= 	ShorthandPtr<br/>
 * Elementbereich	:=	KW_XPTR KW_LBRACE range  KW_RBRACE<br/>
 * Elementsequenz	:=	KW_LBRACE ( Elementbereich | Einzelelement) (Elementbereich | Einzelelement)* KW_RBRACE<br/>
 * <br/>
 * Einzel-XML-Datei	:=	[a-zA-Z_0-9]+[.][a-zA-Z_0-9]+;
 * @author Florian Zipser
 * @version 1.0
 */
public class XPtrInterpreter 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"XPtrInterpreter";		//Name dieses Tools
	private static boolean DEBUG=		false;					//DEBUG-Schalter
	
	private static final String KW_XML_ENDING=	".xml";				//Endung einer xml-Datei
	
	private String base= null;		//Dokumentname auf den sich der XPointer bezieht
	private String xPtr= null;		//Xpointer der interpretiert werden soll
	private Logger logger= null;	//Logger für log4j
	
	//	Patterndefinition
	
	//String pattern= "#xpointer[(]string-range[(][/][/][A-Z]*,['][A-Z]*['],[0-9]*,[0-9]*[)][)]";
	//#xpointer(string-range(//body,'',1,5))
	//private static final String strRange= idPtr + "[/]range-to" + "[()]"+idPtr + "[()]";
	
	//einfacher numerischer Wert
	private static final String REGEX_numVal= 		"[0-9]+";
	//Inhalt der string-ranke Fkt
	private static final String REGEX_strRangeCont=	"[/][/]body\\s*,\\s*[']\\s*[']\\s*,\\s*"+REGEX_numVal+"\\s*,\\s*"+REGEX_numVal;
	//kompletter Aufruf der string-range Fkt
	private static final String REGEX_strRange=		"string-range[(]"+REGEX_strRangeCont+"[)]";
	//XPointer mit string-range
	private static final String REGEX_strRangePtr= 	"#xpointer[(]" + REGEX_strRange+"[)]";
	
	//Dateireferenz
	private static final String REGEX_xmlFileXPtr= "^[^#]+\\.xml$";
	//ID eines Elementes
	private static final String REGEX_idVal=	"\\s*[a-zA-Z0-9_-[.]]+\\s*";
	//ShorthandPointernotation für einfache Token
	private static final String REGEX_shPtr=	"\\s*#" + REGEX_idVal;
	//voller ShorthandPointernotation für einfache Token mit Dateinamen voran (file.xml#shPointer)
	private static final String REGEX_full_shPtr= "[^#]+\\.xml"+ "#"+ REGEX_idVal;
	//Pointer mit der Funktion id
	private static final String REGEX_idPtr= "id[(][']"+  REGEX_idVal +"['][)]";
	//Bereichsangabe (kein XPointer)
	private static final String REGEX_range= REGEX_idPtr + "[/]range-to" + "[()]"+REGEX_idPtr + "[()]";
	//Tokenbereich
	private static final String REGEX_rangePtr= 	"#xpointer[(]" + REGEX_range+"[)]";
	//Tokensequenz
	private static final String REGEX_seqPtr= "[(](" + REGEX_rangePtr +"|"+ REGEX_shPtr +"|"+REGEX_full_shPtr+")"+"([,]("+ REGEX_rangePtr +"|"+ REGEX_shPtr +"|"+REGEX_full_shPtr+"))*"+ "[)]";
	//private static final String REGEX_seqPtr= "[(](" + REGEX_rangePtr +"|"+ REGEX_shPtr +")([,]("+ REGEX_rangePtr +"|"+ REGEX_shPtr +"))*"+ "[)]";
	 

	
	/**
	 * Tokentype kann einfaches Token (kontinuierlich), Tokenbereich(kontinuierlich), 
	 * Tokensequenz (diskontinuierlich) oder Fehlerwert sein. 
	 */
	enum TOKENTYPE{TOKEN, TOKENRANGE, TOKENSEQ, STRTOKENRANGE, SIMPLE_XML_FILE, ERROR};
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	
	private static final String KW_BASE_DEL=		"#";	//der Delimiter von Basisdokument und XPointer
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_XPTR=		MSG_ERR + "The given XPointer is empty.";
	private static final String ERR_EMPTY_BASE=		MSG_ERR + "The given base name is empty.";
	//private static final String ERR_NO_FROM=		MSG_ERR + "No interval start was given";
	//private static final String ERR_NO_TO=			MSG_ERR + "No interval end was given";
	private static final String ERR_NO_EX=			MSG_ERR + "No xpointer expression was given.";
	private static final String ERR_WRONG_EX=		MSG_ERR + "The given xpointer expression does not follows the standard: ";
	private static final String ERR_TOO_MUCH_DEL=	MSG_ERR + "An incorrect expression was given, there are two much delimiters: ";
	private static final String ERR_BASE_NOT_XML=	MSG_ERR + "The base included in the xpointer is no xml file.";
	private static final String ERR_EMPTY_EX=		MSG_ERR + "The given expression is empty.";
	private static final String ERR_NO_BASE=		MSG_ERR + "The given expression doesn´t conatain any base document. ";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt ein leeres XPtrInterpreter-Objekt. Der Dokumentname des Zieldokumentes, sowie
	 * der Xpointer müssen seperat gesetzt werden. Der Dokumentname muss nur dann gesetzt werden,
	 * wenn er nicht im XPointer steckt.
	 */
	public XPtrInterpreter()
	{}
	
	/**
	 * Erzeugt ein XPtrInterüreter-Objekt. Dabei wird der Dokumentname der XPtrRef-Objekte auf
	 * den übergebenen gesetzt, sofern der übergebne XPointer sich nicht auf ein anderes 
	 * Dokument bezieht. Ist der base-Wert leer, muss sich das DOkument im XPointer befinden,
	 * ist auch dieser leer muss er später gesetzt werden
	 * @param base String - Name des Dokuments, auf das sich der XPointer bezieht
	 * @param xPtr String - zu interpretierender XPointer
	 */
	public XPtrInterpreter(String base, String xPtr) throws Exception
	{
		//wenn base nicht gesetzt ist
		if (base== null)
		{
			String[] parts= this.extractBaseXPtr(xPtr);
			this.setInterpreter(parts[0], parts[1]);
			return;
		}
		try
		{
			//wenn Basis übergeben wurde und der XPtr eine Basis enthält
			String[] parts= this.extractBaseXPtr(xPtr);
			this.setInterpreter(parts[0], parts[1]);
		}
		//wenn Basis und Xpointer(ohne eigene Basis) gegeben ist
		catch (Exception e)
			{this.setInterpreter(base, xPtr);}
	}
	
//	 ============================================== private Methoden ==============================================
	/**
	 * Extrahiert aus einer gegebenen XPointer-Expression den eigentlichen XPointer und
	 * das Basisdokument. Funktioniert nur für einfache Pointer: dateiname#id
	 * @param ex String - Xpointer-Expression
	 * @return Liste von Paaren, erster Wert ist immer das Basisdokument, zwieter der XPointer
	 */
	/*
	private Vector<String[]> extractBaseXPtr(String ex) throws Exception
	{
		Vector<String[]> retVec= new Vector<String[]>();
		
		return(retVec);
	}*/
	
	/**
	 * Extrahiert aus einer gegebenen XPointer-Expression den eigentlichen XPointer und
	 * das Basisdokument. Funktioniert nur für einfache Pointer: dateiname#id
	 * @param ex String - Xpointer-Expression
	 * @return 2-wertiges String-Array, erster Wert ist das Basisdokument, zwieter der XPointer
	 */
	
	private String[] extractBaseXPtr(String ex) throws Exception
	{
		if ((ex== null) || (ex.equalsIgnoreCase("")))
			throw new Exception(ERR_EMPTY_EX);
		String[] retArr= new String[2];
		// base-Pattern ist .xml#
		String basePattern= ".xml"+KW_BASE_DEL; 
		
		//wenn keine Basis existiert, dann Array mit [null, ex] erzeugen
		if (!ex.contains(basePattern)) 
		{
			//throw new Exception(ERR_NO_BASE);
			retArr[0]= null;
			retArr[1]= ex;
		}
		else 
		{
			String[] parts= ex.split(KW_BASE_DEL);
			//wenn mehr als 2 Delimiter vorhanden sind
			if (parts.length > 2) 
			{
				retArr[0]= parts[0];
				retArr[1]= ex;
				//throw new Exception(ERR_TOO_MUCH_DEL + ex);
			}
			else
			{
				//prüfen ob das Ende des 1 parts auf .xml endet
				String lastLetter= parts[0].substring(parts[0].length()-KW_XML_ENDING.length());
				if (!lastLetter.equalsIgnoreCase(KW_XML_ENDING)) throw new Exception(ERR_BASE_NOT_XML);
				retArr[0]= parts[0];
				retArr[1]= KW_BASE_DEL + parts[1];
			}
		}
		
		return(retArr);
	}
	
	/**
	 * Erzeugt aus einer gegebenen XPointer-Expression eine Liste von XptrTarget-Objekten
	 * und gibt diese zurück.
	 */
	private Vector<XPtrRef> getXPtrRefs(String ex) throws Exception
	{
		//Fehler wenn ex leer ist
		if ((ex== null) || (ex.equalsIgnoreCase(""))) throw new Exception(ERR_NO_EX);
		
		TOKENTYPE tokType= this.getXPtrType(ex);
		if (this.logger != null) this.logger.debug(MSG_STD + "xpointer expression is "+tokType);
		
		if (DEBUG) System.out.println(MSG_STD + "type of pointer: "+tokType);
		
		//Fehler, wenn ex nicht dem hier deklarierten Standard genügt
		if (tokType== TOKENTYPE.ERROR) throw new Exception(ERR_WRONG_EX + ex);
		
		Vector<XPtrRef> trList= new Vector<XPtrRef>();
		
		//Wenn ex einfaches Token zum Ziel hat
		if (tokType == TOKENTYPE.TOKEN)
		{
			trList.add(this.getFromSimpleToken(ex));
		}
		//	Wenn ex Tokenbereich zum Ziel hat
		else if (tokType == TOKENTYPE.TOKENRANGE)
		{
			trList.add(this.getFromTokenRange(ex));
		}
		//	Wenn ex Tokensequenz zum Ziel hat
		else if (tokType == TOKENTYPE.TOKENSEQ)
		{
			//extrahiere die einfachen Ziele und Bereichsziele
			String strPat= "(" + REGEX_rangePtr + "|" + REGEX_shPtr +"|"+ REGEX_full_shPtr +")";
			Pattern pattern= Pattern.compile(strPat, Pattern.CASE_INSENSITIVE);
			Matcher matcher= pattern.matcher(ex);
			while (matcher.find())
			{
				//System.out.println("ex: "+ex);
				//System.out.println("Matcher group: "+matcher.group());
				//Pattern pat2= Pattern.compile(xPtr, Pattern.CASE_INSENSITIVE);
				Pattern pat2= Pattern.compile(REGEX_rangePtr, Pattern.CASE_INSENSITIVE);
				Matcher match2= pat2.matcher(matcher.group());
				//wenn String Bereich ist
				if (match2.find())
				{
					//System.out.println("ist Bereich");
					trList.add(this.getFromTokenRange(match2.group()));
				}
				//wenn String kein Bereich
				else 
				{
					String ptr= matcher.group();
					//Sequenzüberreste entfernen
					ptr= ptr.replace("(", "");
					ptr= ptr.replace(")", "");
					ptr= ptr.replace(",", "");
					trList.add(this.getFromSimpleToken(ptr));
				}
			}
		}
		//Wenn ex string-range Token zum Ziel hat
		else if (tokType == TOKENTYPE.STRTOKENRANGE)
		{
			trList.add(this.getFromStringRange(ex));
		}
		//Wenn ex einzelne Datei zum Ziel hat
		else if (tokType == TOKENTYPE.SIMPLE_XML_FILE)
		{
			trList.add(this.getFromSimpleXMLFile(ex));
		}
		
		return(trList);
	}
	
	/**
	 * Prüft einen Eingabestring, auf das enthaltensein einer XPointerstruktur. 
	 * Unterschieden werden drei verschiedene Typen: einfaches Token (String ist ein 
	 * xml-ID-Wert (kontinuierlich)), Tokenbereich (String enthält eine Bereichsangabe, 
	 * von einer Token-ID bis zu einer Token-ID (kontinuierlich)), Tokensequenz 
	 * (String enthält eine Sequenz von einfachen Token oder von Tokenbereichen 
	 * (diskontinuierlich))
	 */
	private TOKENTYPE getXPtrType(String chckStr) 
	{
		//entferne alle Leerzeichen aus dem gegebenen XPointer
		chckStr= chckStr.replaceAll(" ", "");
		
		//Definition der RegEx-Vars
		Pattern pattern= null;	//zu prüfendes Pattern
		Matcher matcher= null;	//genutzter Matcher	
		String strPattern= "";	//String Pattern
		
		//String ist Tokenbereich (String enthält den String xpointer und range-to)
		strPattern= REGEX_rangePtr;
		pattern= Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);
		matcher= pattern.matcher(chckStr);
		if (matcher.matches()) return(TOKENTYPE.TOKENRANGE);
		
		//String ist Tokensequenz (String enthält den String xpointer, Inhalte sind einfache Token oder Tokenbereiche)
		strPattern= REGEX_seqPtr;
		pattern= Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);
		matcher= pattern.matcher(chckStr);
		if (matcher.matches()) return(TOKENTYPE.TOKENSEQ);
		
		//String ist einfaches Token (der String xpointer ist nicht enthalten und es ist keine Sequenz)
		strPattern= REGEX_shPtr;
		pattern= Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);
		matcher= pattern.matcher(chckStr);
		//chStr passt auf ShorthandPointer, da Tokenbereich und Tokensequenz bereits geprüft wurden, muss es sich um ShorthandPointer handeln 
		if (matcher.matches()) return(TOKENTYPE.TOKEN);
		
		//String ist Stringbereich
		strPattern= REGEX_strRangePtr;
		pattern= Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);
		matcher= pattern.matcher(chckStr);
		//chStr passt auf String-Range Pointer 
		if (matcher.matches()) return(TOKENTYPE.STRTOKENRANGE);
		
		//String ist einzelne Datei
		strPattern= REGEX_xmlFileXPtr;
		pattern= Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);
		matcher= pattern.matcher(chckStr);
		//chStr passt auf einfache XML-Datei- Pointer 
		if (matcher.matches()) return(TOKENTYPE.SIMPLE_XML_FILE);

		return(TOKENTYPE.ERROR);
	}
	
	/**
	 * Liest eine Expression aus, die einem einfachen Token entspricht. Der ID-Wert wird 
	 * extrahiert und ein XPtrTarget-Objekt erzeugt und zurückgegeben
	 * @param ex String - einfaches Token
	 * @return XPtrTarget-Objekt, das der ID entspricht
	 * @throws Exception
	 */
	private XPtrRef getFromSimpleToken(String ex) throws Exception
	{
		XPtrRef tar= null; 
		Pattern pattern1= Pattern.compile(REGEX_full_shPtr, Pattern.CASE_INSENSITIVE);
		Matcher matcher1= pattern1.matcher(ex);
		
		//wenn das Basis-Dokument im Pointer steht
		if (matcher1.find())
		{
			String parts[]= ex.split("#");
			//extrahiere die ID des Tokens
			Pattern pattern= Pattern.compile(REGEX_idVal, Pattern.CASE_INSENSITIVE);
			Matcher matcher= pattern.matcher(parts[1]);
			if (matcher.find())
			{
				//es sollte nur einen passenden Wert geben
				String id= matcher.group().trim();
				tar= new XPtrRef(parts[0].trim(), id);
				tar.setType(POINTERTYPE.ELEMENT);
			}
		}
		else
		{
			//extrahiere die ID des Tokens
			Pattern pattern= Pattern.compile(REGEX_idVal, Pattern.CASE_INSENSITIVE);
			Matcher matcher= pattern.matcher(ex);
			if (matcher.find())
			{
				//es sollte nur einen passenden Wert geben
				String id= matcher.group().trim();
				tar= new XPtrRef(this.base, id);
				tar.setType(POINTERTYPE.ELEMENT);
			}
		}
		return(tar);
	}
	
	/**
	 * Liest eine Expression aus, die einem Tokenbereich entspricht. Der ID-Wert wird 
	 * extrahiert und ein XPtrTarget-Objekt erzeugt und zurückgegeben
	 * @param ex String - einfaches Token
	 * @return XPtrTarget-Objekt, das der ID entspricht
	 * @throws Exception
	 */
	private XPtrRef getFromTokenRange(String ex) throws Exception
	{
		XPtrRef tar= null; 
		
		//extrahiere die ID des Tokens
		Pattern pattern= Pattern.compile("[']"+REGEX_idVal + "[']", Pattern.CASE_INSENSITIVE);
		Matcher matcher= pattern.matcher(ex);
		String from=null;
		String to=null;
		int i = 0;		//Zählvariable
		while (matcher.find())
		{
			//Syntaxfehler, wenn es mehr als zwei id´s gibt
			if (i > 1) throw new Exception(ERR_WRONG_EX + ex);
			//Id´s haben vorne und hinten je einen Anführungsstrich
			else if (i== 0) from= matcher.group().replaceAll("'","");
			else if (i== 1) to= matcher.group().replaceAll("'","");
			i++;
		}
		tar= new XPtrRef(this.base, from.trim(), to.trim());
		tar.setType(POINTERTYPE.ELEMENT);
		
		return(tar);
	}
	
	/**
	 * Liest eine Expression aus, die einem string-range Bereich entspricht. Der Startwert
	 * und die Zeichenlänge werden extrahiert und als ID das XPtrRef-Objekt geschrieben.
	 * @param ex String - einfaches Token
	 * @return XPtrTarget-Objekt, das Startposition des STrings und Länge enthält
	 * @throws Exception
	 */
	private XPtrRef getFromStringRange(String ex) throws Exception
	{
		XPtrRef tar= null; 
		//extrahiere die Startposition und Länge des Tokens
		Pattern pattern= Pattern.compile(REGEX_numVal);
		Matcher matcher= pattern.matcher(ex);
		String start= "";
		String length="";
		int i = 0;		//Zählvariable
		while (matcher.find())
		{
			//Syntaxfehler, wenn es mehr als zwei id´s gibt
			if (i > 1) throw new Exception(ERR_WRONG_EX + ex);
			//Id´s haben vorne und hinten je einen Anführungsstrich
			else if (i== 0) start= matcher.group();
			else if (i== 1) length= matcher.group();
			i++;
		}
		tar= new XPtrRef(this.base, start.trim(), length.trim());
		tar.setType(POINTERTYPE.TEXT);
		
		return(tar);
	}
	
	/**
	 * Liest eine Expression aus, die einer einzelnen Datei entspricht. Der Startwert
	 * und die Zeichenlänge werden extrahiert und als ID das XPtrRef-Objekt geschrieben.
	 * @param ex String - einfaches Token
	 * @return XPtrRef-Objekt
	 * @throws Exception
	 */
	private XPtrRef getFromSimpleXMLFile(String ex) throws Exception
	{
		XPtrRef xPtrRef= new XPtrRef(ex, ex);
		xPtrRef.setType(XPtrRef.POINTERTYPE.XMLFILE);
		return(xPtrRef);
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Setzt einen Log4J-Logger. wird dieser gesetzt, gibt das Objekt debug Messages aus.
	 * @param logger Logger - für das Log4J 
	 */
	public void setLogger(Logger logger)
		{ this.logger= logger; }
	
	/**
	 * Setzt dieses Interpreter Objekt neu. dabei wird geprüft ob im xPtr eine 
	 * Dokumentenbasis angeben ist. Wenn dem so ist wird diese anstatt der übergebenen
	 * gesetzt. Ansonsten wird die Dokumentenbasis auf base gesetzt.
	 * @param base String - XML-Dokumentenbasis, diese wird als Basis genommen, wenn xPtr keine enthält
	 * @param xPtr String - XPointerziel, eventuell mit Basis
	 */
	public void setInterpreter(String base, String xPtr) throws Exception
	{
		String[] parts;
		try
		{
			parts= this.extractBaseXPtr(xPtr);
		}
		catch (Exception e)
			{throw new Exception(ERR_NO_BASE + xPtr);}
		//Vector<String[]> entries= extractBaseXPtr(xPtr);
		//for (String[] parts : entries)
		{
			//wenn xPtr Basis-Dokument enthält
			if (parts[0]!= null) 
			{
				this.base= parts[0];
				this.xPtr= parts[1];
			}
			//wenn XPtr kein Basis-Dokument enthält
			else
			{
				this.base= base;
				this.xPtr= xPtr;
			}
		}
	}
	
	/**
	 * Setzt den XPointer, der interpretiert werden soll neu.
	 * @param xPtr String - zu Interpretierender XPointer Wert
	 */
	public void	setXPtr(String xPtr) throws Exception
	{
		if ((xPtr == null)||(xPtr.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_XPTR);
		this.xPtr= xPtr;
	}
	
	/**
	 * Setzt das Basisdokument neu. Auf dieses bezieht sich der übergebene XPointer.
	 * @param base String - Basisdokument für diesen XPointer
	 */
	public void setBase(String base) throws Exception
	{
		if ((base == null)||(base.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_BASE);
		this.base= base;
	}
	
	/**
	 * Gibt den Typ zurück, den dieser XPointer hat. 
	 * @return Typ des XPointers
	 * @exception Fehler, wenn XPointer nicht gesetzt
	 */
	public TOKENTYPE getXPtrType() throws Exception
		{ return(this.getXPtrType(this.xPtr)); }
	
	/**
	 * Gibt eine geordnete Menge von XPointer-Refernzielen zurück.
	 * @return Ziele dieses Xpointers
	 */
	public Vector<XPtrRef> getResult() throws Exception
		{ return(this.getXPtrRefs(this.xPtr)); }
	
	/**
	 * Gibt den Xpointer dieses Objekts zurück, sofern dieser gesetzt ist.
	 * @return XPointer dieses Objekts
	 */
	public String getXPtr()
		{ return(this.xPtr); }
	
	/**
	 * Gibt den Dokumentnamen auf den sich der Xpointer dieses Objekts zurück, 
	 * sofern dieser gesetzt ist.
	 * @return Dokumentname
	 */
	public String getDoc()
		{ return(this.base); }
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= MSG_STD + "document name: " +this.base + ", xpointer: " + this.xPtr;
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	

	private static void printRefs(Vector<XPtrRef> refs, String xPtr, String xmlBase) throws Exception
	{
		System.out.println("Test for pointer: "+ xPtr + ", document: "+ xmlBase);
		for (XPtrRef ref: refs)
		{
			System.out.println("type: "+ ref.getType());
			if ( ref.getType()== XPtrRef.POINTERTYPE.ELEMENT)
			{
				if (!ref.isRange())
				{
					System.out.println("Zielknoten: "+ ref.getDoc() +"#"+ ref.getID());
				}
				else 
				{
					System.out.println("Knoten von: "+ ref.getDoc() +"#"+ ref.getLeft() + " bis: "+ xmlBase +"#"+ ref.getRight());
				}
			}
			else if ( ref.getType()== XPtrRef.POINTERTYPE.TEXT)
			{
				System.out.println("linke Textgrenze: " + ref.getLeft()+ ", rechte Textgrenze: "+ ref.getRight());
			}
		}
	}
	
	public static void main(String[] args)
	{
		System.out.println("********************** Start "+ TOOLNAME+" Test **********************");
		DEBUG= true;
		try
		{
			XPtrInterpreter interpreter= new XPtrInterpreter();
			String xmlBase= "doc.xml";
			//Single Pointer
			System.out.println("->single Pointer");
			String xPtr= "#tok1";
			interpreter.setInterpreter(xmlBase, xPtr);
			printRefs(interpreter.getResult(), xPtr, xmlBase);
			
			//Range
			System.out.println("->range Pointer");
			xPtr= "#xpointer(id('primmarkSeg_1')/range-to(id('primmarkSeg_3')))";
			interpreter.setInterpreter(xmlBase, xPtr);
			printRefs(interpreter.getResult(), xPtr, xmlBase);
			
			//Sequence1
			System.out.println("->sequence Pointer 1");
			xPtr= "(#tok_167,#tok_168)";
			interpreter.setInterpreter(xmlBase, xPtr);
			printRefs(interpreter.getResult(), xPtr, xmlBase);
			
			//Sequence2
			System.out.println("->sequence Pointer 2");
			xPtr= "(#xpointer(id('tok_3')/range-to(id('tok_7'))), #tok3)";
			interpreter.setInterpreter(xmlBase, xPtr);
			printRefs(interpreter.getResult(), xPtr, xmlBase);
			
			//Sequence3
			System.out.println("->sequence Pointer 3");
			xPtr= "(#xpointer(id('tok_3')/range-to(id('tok_7'))),#xpointer(id('tok_9')/range-to(id('tok_17'))))";
			interpreter.setInterpreter(xmlBase, xPtr);
			printRefs(interpreter.getResult(), xPtr, xmlBase);
			
			//Sequence4
			System.out.println("->sequence Pointer 4");
			xPtr= "(#xpointer(id('tok_3')/range-to(id('tok_5'))),#xpointer(id('tok_7')/range-to(id('tok_8'))))";
			interpreter.setInterpreter(xmlBase, xPtr);
			printRefs(interpreter.getResult(), xPtr, xmlBase);
			
			//Pointer mit Base 1
			System.out.println("->Pointer mit Base 1");
			xPtr= "urml.maz-10205.seg.xml#seg_1";
			interpreter.setInterpreter(xmlBase, xPtr);
			printRefs(interpreter.getResult(), xPtr, xmlBase);
			
			//Pointer mit Base mehreren Basen
			System.out.println("->Sequenz mit unterschiedlichen Basis-Dokumenten (iiner- und außerhalb des Pointers)");
			xPtr= "(#primmarkSeg_22,bla.xml#primmarkSeg_34)";
			interpreter.setInterpreter(xmlBase, xPtr);
			printRefs(interpreter.getResult(), xPtr, xmlBase);
			
			//Pointer mit Base mehreren Basen
			System.out.println("->Sequenz mit unterschiedlichen Basis-Dokumenten (iiner- und außerhalb des Pointers)");
			xPtr= "(#primmarkSeg_22,mmax.pocos.maz-10205.primmarkSeg.xml#primmarkSeg_34)";
			interpreter.setInterpreter(xmlBase, xPtr);
			printRefs(interpreter.getResult(), xPtr, xmlBase);
			
			//Pointer mit mehreren Basen
			System.out.println("->Sequenz mit unterschiedlichen Basis-Dokumenten");
			xPtr= "(mmax.pocos.maz-10205.primmarkSeg.xml#primmarkSeg_22,mmax.pocos.maz-10205.primmarkSeg.xml#primmarkSeg_34)";
			interpreter.setInterpreter(xmlBase, xPtr);
			printRefs(interpreter.getResult(), xPtr, xmlBase);
			
		}
		catch (Exception e)
			{e.printStackTrace();}
		System.out.println("********************** End "+ TOOLNAME+" Test **********************");
	}

}
