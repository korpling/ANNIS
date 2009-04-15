package relANNIS_2_0.relANNISDAO;

import java.util.Hashtable;
import java.util.Vector;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Diese Klasse liest Informationen über eine Datenbank-Verbindung aus einem SAX-Event-Handler
 * aus. Folgende Elementknoten müssen an diese Klasse übergeben werden.<br/>
 * <ul>
 * 	<li><entry name= "driver" value= "CDATA"/></li>
	<li><entry name= "host" value= "CDATA"/><li/>
	<li><entry name= "port" value= "CDATA"/></li>
	<li><entry name= "database" value= "CDATA"/></li>
	<li><entry name= "user" value= "CDATA"/></li>
	<li><entry name= "password" value= "CDATA"/></li>
 * </ul> 
 *  
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class DBParamReader extends DefaultHandler2 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"DBParamReader";		//Name dieses Tools
	private static final String[] setNames= {"DB_CONNECTION"};	//Namen der Settings, auf die dieser Listener hören soll		 
	
	private static final String KW_NULL=	"NOT INITILIZED";			//Standardwert
	private static final String KW_ITEM=	"entry";						//Keyword für item
	private static final String KW_ATT_NAME=	"name";						//Keyword für name
	private static final String KW_ATT_VAL=	"value";						//Keyword für value

	private static final String KW_D=		"driver";					//Keyword für driver
	private static final String KW_H=		"host";						//Keyword für host
	private static final String KW_P=		"port";						//Keyword für port
	private static final String KW_DB=		"database";					//Keyword für database
	private static final String KW_U=		"user";						//Keyword für user
	private static final String KW_PASS=	"password";					//Keyword für password
	
	private Hashtable<String, String> dbParams= null;					//Tabelle für die Datenbankparameter
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_NO_PARAM_TABLE=	MSG_ERR + "The given table for db parameter is empty. It has to be initialized.";
//	 ============================================== statische Methoden ==============================================
	/**
	 * Gibt alle SettingNamen zurück, auf die dieser SettingListener hört.
	 * @return Vector<String> - Namen der Settings, auf die dieser Listener hört
	 */
	public static Vector<String> getSetNames()
	{
		Vector<String> setNames= new Vector<String>();
		for(String name : DBParamReader.setNames)
			setNames.add(name);
		return(setNames);
	}
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein DBParamReader-Objekt und setzt die zu beschreibende 
	 * Parametertabelle.
	 * @param dbParams Hashtable<String, String> - Tabelle, in die die Parameter geschrieben werden.
	 */
	public DBParamReader(Hashtable<String, String> dbParams) throws Exception
	{
		if (dbParams == null) throw new Exception(ERR_NO_PARAM_TABLE);
		this.dbParams= dbParams;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "database parameter:\n";
		
		//wenn Treiber nicht null, dann diesen zurückgeben
		retStr= retStr + KW_D + ":\t" + (this.dbParams.get(KW_D)!= null ? this.dbParams.get(KW_D): KW_NULL) + "\n";
		//wenn Host nicht null, dann diesen zurückgeben
		retStr= retStr + KW_H + ":\t" + (this.dbParams.get(KW_H) != null ? this.dbParams.get(KW_H): KW_NULL+ "\n");
		//wenn Port nicht null, dann diesen zurückgeben
		retStr= retStr + KW_P + ":\t" + (this.dbParams.get(KW_P) != null ? this.dbParams.get(KW_P): KW_NULL+ "\n");
		//wenn database nicht null, dann diesen zurückgeben
		retStr= retStr + KW_DB + ":\t" + (this.dbParams.get(KW_DB) != null ? this.dbParams.get(KW_DB): KW_NULL + "\n");
		//wenn User nicht null, dann diesen zurückgeben
		retStr= retStr + KW_U + ":\t" + (this.dbParams.get(KW_U) != null ? this.dbParams.get(KW_U): KW_NULL + "\n");
		//wenn Password nicht null, dann diesen zurückgeben
		retStr= retStr + KW_PASS + ":\t" + (this.dbParams.get(KW_PASS) != null ? this.dbParams.get(KW_PASS): KW_NULL + "\n");
		
		return(retStr);
	}
	
	//-------------------------- SAX Methoden(DefaultHandler2) --------------------------
	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(String uri, String localName, String qName, Attributes attributes)
	 */
	public void startElement(	String uri, 
								String localName, 
								String qName, 
								Attributes attributes) throws SAXException
	{
		//wenn Elementknoten = item
		if (qName.equalsIgnoreCase(KW_ITEM))
		{
			String attName= null;
			String attVal= null;
			//ermittle Attribut-Wert-Paar
			for (int i= 0; i < attributes.getLength(); i++)
			{
				//Attribut ist name
				if(attributes.getQName(i).equalsIgnoreCase(KW_ATT_NAME)) 
							attName= attributes.getValue(i);
				//Attribut ist value
				else if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_VAL)) 
					attVal= attributes.getValue(i);
			}
			if (attName.equalsIgnoreCase(KW_D)) this.dbParams.put(KW_D, attVal);
			else if (attName.equalsIgnoreCase(KW_H)) this.dbParams.put(KW_H, attVal);
			else if (attName.equalsIgnoreCase(KW_P)) this.dbParams.put(KW_P, attVal);
			else if (attName.equalsIgnoreCase(KW_DB)) this.dbParams.put(KW_DB, attVal);
			else if (attName.equalsIgnoreCase(KW_U)) this.dbParams.put(KW_U, attVal);
			else if (attName.equalsIgnoreCase(KW_PASS)) this.dbParams.put(KW_PASS, attVal);
		}
	}
}
