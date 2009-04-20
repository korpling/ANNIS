package importer.paula.paula10.importer.mapperV1;

import java.io.File;

/**
 * Die Klasse PDDesc (PAULA Document Description) beherbergt Metainformationen zu einer 
 * Paula-Datei. Diese Metainformationen werden aus einer Korpus KOrpus-Typ-Datei
 * ausgelsen. Metainformationen sind:
 * <ul>
 * 	<li>der Dateiname</li>
 * 	<li>die DTD</li>
 * 	<li>der PD-Typ</li>
 * </ul> 
 * @author Florian Zipser
 * @version 1.0
 */
public class PDDesc 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PDDesc";		//Name dieses Tools
	
	private File file= null;		//Dateiname der hier repräsentierten Datei
	private String dtd= 	"";			//DTD zu der hier repräsentierten Datei
	private String pdType= 	"";			//PD-Typ zu der hier repräsentierten Datei
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_PARAM=	MSG_ERR + "One of the initil parameters is empty: ";
	private static final String ERR_FILE_NOT_EXIST=	MSG_ERR + "The file does not exist: ";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Instantiiert ein PDDesc-Objekt. Dabei werden die nötigen PAULA-Dateiinformationen gesetzt.
	 * @param file String 		- Name der PAULA-Datei
	 * @param dtd String		- Die zu der PAULA-Datei gehördende DTD
	 * @param pdType String		- Der PAULA-Datei-Type zu der Datei
	 * @exception Fehler, wenn einer der Parameter leer oder null ist.
	 */
	public PDDesc(File file, String dtd, String pdType) throws Exception
	{
		if (file == null) throw new Exception(ERR_EMPTY_PARAM + "fileName");
		if (!file.exists()) throw new Exception(ERR_FILE_NOT_EXIST + file.getAbsolutePath());
		
		if ((dtd == null)|| (dtd.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_PARAM + "dtd");
		if ((pdType == null)|| (pdType.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_PARAM + "pdType");
		this.file= file;
		this.dtd= dtd;
		this.pdType= pdType;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Gibt den Namen der durch dieses Objekt repräsentierten Datei zurück.
	 * @return  Name der Datei
	 */
	public String getFileName()
		{ return(this.file.getName()); }
	
	/**
	 * Gibt den komplettten Namen der durch dieses Objekt repräsentierten Datei zurück.
	 * @return  Name der Datei mit Pfad
	 */
	public String getFullFileName() throws Exception
		{ return(this.file.getCanonicalPath()); }
	
	/**
	 * Gibt die Datei die durch dieses Objekt repräsentiert wird zurück.
	 * @return  Zieldatei
	 */
	public File getFile()
		{ return(this.file); }
	
	/**
	 * Gibt die DTD der durch dieses Objekt repräsentierten Datei zurück.
	 * @return  Name der Datei
	 */
	public String getDTD()
		{ return(this.dtd); }
	
	/**
	 * Gibt den PD-Typ der durch dieses Objekt repräsentierten Datei zurück.
	 * @return  PD-Typ der Datei
	 */
	public String getPDType()
		{ return(this.pdType); }
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "file name: "+ this.getFileName() + ",\tdtd: " + this.dtd + ",\tpd-type: " +this.pdType;
		return(retStr);
	}
}
