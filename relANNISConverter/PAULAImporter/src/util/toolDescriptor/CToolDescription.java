//*********************************************** XMLPrinter ******************************************

package util.toolDescriptor;
import java.util.Vector;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * <p>
 * 	Das Modul CToolDescription bietet eine Struktur zum speichern der Beschreibungsangaben zu einem Program. 
 *  Dies sind Angaben wie die Programbeschreibung, die Programsynopsis, sowie die Aufrufflags und deren Bedeutung. 
 * </p>
 * @author Flo
 * @version 1.0
 *
 */
public class CToolDescription 
{
//	 ============================================== private Variablen ==============================================

	/**
	 * Name des Tools
	 */
	private String toolName= "";	
	
	/**
	 * Beschreibung des Tools
	 */
	private String desc= "";
	
	/**
	 * Version des Tools
	 */
	private String version= "";
	
	/**
	 * Liste von Flags
	 */
	private Vector<CFlag> flags= null;
	
	
	/**
	 * Liste von Autoren
	 */
	private Vector<CAuthor> authors= null;
	
	/**
	 * Synopsis des Tools
	 */
	private String synopsis= "";
	
	/**
	 * Gibt an, ob die Synopsis automatisch erstellt werden soll
	 */
	private boolean autoSynopsis= true;
	
	//	 *************************************** Meldungen ***************************************
	
	private static final String MSG_START	= "*********** start CToolDescription ***********";
	private static final String MSG_END	= "*********** end   CToolDescription ***********";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt ein CToolDescripion Objekt.
	 */
	public CToolDescription()
	{
		this.flags= new Vector<CFlag>();
		this.authors= new Vector<CAuthor>();
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================	

	/**
	 * Setzt den Namen des Tools auf den übergebenen.
	 * @param toolName
	 */
	public void setName(String toolName)
	{ 
		this.toolName= toolName;
		if (this.autoSynopsis) this.synopsis= toolName;
	}
	
	/**
	 * Gibt den Namen des Tools zurück.
	 * @return Name des Tools.
	 */
	public String getName()
		{ return (this.toolName); }

	/** 
	 * Setzt die Beschreibung des Tools auf die übergebenen.
	 * @param desc Strin - Beschreibung des Tools
	 */
	public void setDesc(String desc)
		{ this.desc= desc; }
	
	/**
	 * Gibt die Beschreibung des Tools zurück.
	 * @return Beschreibung des Tools.
	 */
	public String getDesc()
		{ return(this.desc); }
	
	/** 
	 * Setzt die Version des Tools auf die übergebenen.
	 * @param version String - des Tools
	 */
	public void setVersion(String version)
		{ this.version= version; }
	
	/**
	 * Gibt die Version des Tools zurück.
	 * @return Version des Tools.
	 */
	public String getVersion()
		{ return(this.version); }
	
	/**
	 * Setzt die Synopsis zum Aufruf des Tools auf die übergebenen.
	 * @param synopsis String - Aufruf des Tools
	 */
	public void setSynopsis(String synopsis)
	{ 
		this.synopsis = synopsis;
		this.autoSynopsis= false;
	}
	
	/**
	 * Gibt die Synopsis des Tools zurück.
	 * @return Synopsis des Tools
	 */
	public String getSynopsis()
		{ return(this.synopsis); }
	
	/**
	 * Fügt ein neues Flag mit dem angegebenen Namen, der Beshcreibung und der Optionalität hinzu.
	 * @param name - Name des Flags
	 * @param appendix Anhänge wie Dateinamen
	 * @param isOptional - Optionalität des Flags
	 * @param desc - Beschreibung des Flags
	 */
	public void addFlag(String name, String appendix, boolean isOptional, String desc)
	{
		CFlag flag= new CFlag(name, appendix, isOptional, desc);
		this.flags.add(flag);
		if (this.autoSynopsis)
		{
			String PrintFlag= name;
			if (appendix != null) PrintFlag= PrintFlag + " "+ appendix; 
				
			if (isOptional)
				this.synopsis= this.synopsis + " [" + PrintFlag + "]";
			else 
				this.synopsis= this.synopsis + " " + PrintFlag;
		}
	}
	
	/**
	 * Fügt ein neues Flag mit dem angegebenen Namen, der Beshcreibung und der Optionalität hinzu.
	 * @param flag - CFlag - Flag, das dieser Beschreibung hinzugefügt werden soll. 
	 */
	public void addFlag(CFlag flag)
	{ 
		if (flag != null) this.addFlag(flag.getName(), flag.getAppendix(), flag.isOptional(), flag.getDesc()); 
	}
	
	/**
	 * Fügt ein neues Flag mit dem angegebenen Namen, der Beschreibung und der Optionalität hinzu.
	 * @param name - Name des Flags
	 * @param isOptional - Optionalität des Flags
	 * @param desc - Beschreibung des Flags
	 */
	public void addFlag(String name, boolean isOptional, String desc)
		{ this.addFlag(name, null, isOptional, desc); }
	
	/**
	 * Fügt einen neuen Autor mit dem angegebenen Namen und der eMAil-Adresse hinzu.
	 * @param name - String - Name des Autors
	 * @param eMail - String - eMail-Adresse des Autors
	 */
	public void addAuthor(String name, String eMail)
	{
		if ((name.equalsIgnoreCase("")) || (eMail.equalsIgnoreCase(""))) return;
		CAuthor author= new CAuthor(name, eMail);
		authors.add(author);
	}
	
	/**
	 * Clont das übergebene CAuthor Objekt und fügt es der Autorenliste hinzu.
	 * Ist das übergebene CAuthor-Objekt leer wird es nicht hinzugefügt.
	 * @param author - CAuthor - zu clonendes CAuthor-Objekt
	 */
	public void addAuthor(CAuthor author)
	{
		if (author== null) return;
		authors.add(author.clone());
	}
	
	/**
	 * Gibt das CToolDescripion Objekt als String zurück
	 */
	public String toString()
	{
		String retStr= "";
		//Toolname
		retStr= retStr + "tool:\t\t" + this.toolName +"\n";
		//Version
		retStr= retStr + "version:\t" + this.version +"\n";
		//Authoren
		retStr= retStr + "authors:\n";
		CAuthor author= null;
		for (int i = 0; i < authors.size(); i++)
		{
			author= authors.get(i);
			retStr= retStr + "\t" + author.toString()+ "\n";
		}		
		//Synopsis
		retStr= retStr + "Synopsis:\t" + this.synopsis + "\n";
		//Beschreibung
		retStr= retStr + "Description:\n" + this.desc + "\n";
		
		//Flags
		CFlag flag= null;
			//alle Flags durchgehen
		for (int i = 0; i < flags.size(); i++)
		{
			flag= flags.get(i);
			retStr= retStr + "\n" + flag.toString();
		}
		
		return(retStr);
	}
	
	/**
	 * Erstellt eine eine Textdatei mit dem Namen fileName. Die Datei stellt eine Art readme
	 * Datei dar. Sie enthält alle Informationen des CDescription Objektes 
	 * @param fileName - Datei in die die Beschreibung geschrieben werden soll.
	 */
	public void toTXT(String fileName) throws Exception
	{
		File TXTFile = new File(fileName);
		if (TXTFile.exists()) TXTFile.delete();
		//Stream für die Ausgabedatei
		PrintStream output= new PrintStream((OutputStream)new FileOutputStream(TXTFile, true), true, "UTF-8");
		output.println(" \t***************************************************************");
		output.println(" \t****\t\t"+ this.toolName+ "\t\t****");
		output.println(" \t***************************************************************");
		output.print(this.toString());
		output.close();
	}
	
//	 ============================================== main Methode ==============================================	

	public static void main(String[] args)
	{
		System.out.println(MSG_START);
		CToolDescription desc= new CToolDescription();
		desc.setName("CToolDescription");
		desc.setDesc("Tool zum Test dieser Klasse.");
		desc.setVersion("1.0");
		desc.addFlag("-h", true, "gibt diese Hilfe aus.");
		desc.addFlag("-v", false, "gibt die Versionsnummer an.");
		desc.addFlag("-f", "Dateiname", true, "liest ne Datei ein.");
		System.out.println(desc);
		try
		{
			desc.toTXT("data/readme.txt");
		}
		catch (Exception e)
			{ e.printStackTrace(); }
		desc.setSynopsis("bla CToolDescription [-h] -v");
		System.out.println(desc);
		System.out.println(MSG_END);
	}
}
