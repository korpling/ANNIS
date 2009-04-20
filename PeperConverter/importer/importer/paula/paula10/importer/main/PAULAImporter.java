package importer.paula.paula10.importer.main;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import importer.paula.paula10.util.timer.Timer;
import importer.paula.paula10.util.toolDescriptor.CToolDescription;
import importer.paula.paula10.util.toolDescriptor.CToolDescriptor;

import importer.paula.paula10.importer.mapper.AbstractMapper;
import importer.paula.paula10.importer.mapperV1.MapperV1;
/**
 * 
 * @author Florian Zipser
 * @version 1.2
 */
public class PAULAImporter 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PAULAImporter";		//Name dieses Tools
	private static final String VERSION= 	"1.1";					//Version dieses Tools
	
	//Pfad und Dateiname f�r Settingfiles
	//private static final String FILE_SETTING=		"/settings/PASettings.xml";					//Name der default Setting-Datei
	private static final String FILE_DESCRIPTION= 	"/PAULAImporter/settings/description.xml";	//Name der Datei, die dieses Tool beschreibt
	private static final String FILE_LOG=			"/PAULAImporter/settings/log4j.xml";			//Name der log4j Datei
	//private static final String FILE_KORP_STRUCT=	"korp_struct.xml";				//default name der Korpusstrukturdatei
	private static final String FILE_TMP_PATH=		"_TMP_DATA";					//tempor�res Verzeichnis f�r tab dateien
	
	//Namen der Flags
	private static final String FLAG_HELP= 			"-h";				//Flag f�r Hilfe ausgeben
	private static final String FLAG_DOT=			"-dot";				//Flag Graph als Dot ausgeben
	private static final String FLAG_FOLDER_SRC= 	"-s";				//Flag f�r Namen des Verzeichnisses
	private static final String FLAG_FOLDER_DST=	 "-d";				//Flag f�r Namen des Verzeichnisses
	
	private static boolean isRunning= false;						//gibt an, ob bereits eine Instanz dieses Tools l�uft
	
	private Logger logger= null;									//logger f�r log4j
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START=			"******************** start "+TOOLNAME+" "+ VERSION + " ********************";
	private static final String MSG_END=			"******************** end "+TOOLNAME+" "+ VERSION + " ********************";
	private static final String MSG_INIT=			MSG_STD + TOOLNAME + " "+VERSION +" is successfully initialized...";
	private static final String MSG_START_FCT=		MSG_STD + "start methode: ";
	private static final String MSG_END_FCT=		MSG_STD + "end methode: ";
	private static final String MSG_IN_PROCESS=		MSG_STD + "importing is in process for folder: ";
	private static final String MSG_SUCCESS=		MSG_STD + "PAULA-source has been successfully analyzed.";
	private static final String MSG_TIME=			MSG_STD + "time required to analyze folder: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_ALREADY_RUNNING=	MSG_ERR + "You can run only one instance of this tool and one is already running.";
	private static final String ERR_EMPTY_SRC_FOLDER=	MSG_ERR + "The given src folder is empty.";
	private static final String ERR_NOT_A_DIR=			MSG_ERR + "The given source folder is not a directory: ";
	//private static final String ERR_EMPTY_TMP_FOLDER=	MSG_ERR + "The given temprorary folder is empty.";
	private static final String ERR_TMP_FOLDER=			MSG_ERR + "Cannot create temprorary folder: ";
	private static final String ERR_EMPTY_SRC_NAME= 	MSG_ERR + "The given name for source folder is empty.";
	private static final String ERR_EMPTY_DST_NAME= 	MSG_ERR + "The given name for destination folder is empty.";
//	 ============================================== statische Methoden ==============================================	
	/**
	 * Gibt einen String mit Informationen �ber diese Klasse zur�ck.
	 * @return String mit KLasseninformationen
	 */
	public static String getInfo()
	{
		String retStr= "";
		retStr= "tool:" + TOOLNAME + "\tversion: "+ VERSION;
		return(retStr);
	}
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt eine Instanz des PAULAAnalyzers. 
	 * 
	 */
	public PAULAImporter() throws Exception
	{
		Logger.getLogger(PAULAImporter.class);
		if (isRunning) throw new Exception(ERR_ALREADY_RUNNING);
		isRunning= true;
		init(logger);
	}
	
	/**
	 * Erzeugt eine Instanz des PAULAAnalyzers. 
	 * @param logger Logger - Ein logger f�r die Nachrichtenausgabe (log4j)
	 */
	public PAULAImporter(Logger logger) throws Exception
	{
		if (isRunning) throw new Exception(ERR_ALREADY_RUNNING);
		isRunning= true;
		init(logger);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Initialisiert dieses Objekt.
	 * @param logger Logger - Logger f�r log4j
	 */
	private void init(Logger logger)
	{
		this.logger= logger;
		
		//lese die Knoten aus
		//lese die PAULAReader aus
		
		if (this.logger != null) this.logger.debug(MSG_INIT);
	}
	
	/**
	 * Diese Methode liefert einen Mapper vom Typ AbstractMapper zur�ck. Der Mapper �bernimmt
	 * die Abbildung des Quelldatenmodells auf das interne Zieldatenmodell. Diese Methode
	 * sucht je nach Einstellung den aktuellen Mapper aus.
	 * TODO der aktuelle Mapper muss aus einer Datei gesucht werden.
	 * @return aktuellen Mapper
	 */
	private AbstractMapper getMapper() throws Exception
	{
		return(new MapperV1());
	}
//	 ============================================== �ffentliche Methoden ==============================================
	
	
	/**
	 * Importiert PAULA-Dokumente aus einem gegebenen Verzeichnis und schreibt die Tabellendateien
	 * in ein tempor�rverzeichniss. Es k�nnen nur Korpora importiert werden, die zuvor getypt wurden.
	 * @param srcFolder String - Name des Quellverzeichniss des getypten Korpus
	 * @param toDot boolean - Gibt an, ob eine Dot-Datei dieses Graphens erstellt werden soll
	 */
	public void importPAULA(String srcFolder, boolean toDot) throws Exception
	{
		if (logger!= null) logger.debug(MSG_START_FCT + "importPAULA(String)");
		
		if ((srcFolder == null) || (srcFolder.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_SRC_NAME);
		File srcFile= new File(srcFolder);
		this.importPAULA(srcFile, null, null, toDot);
		
		if (logger!= null) logger.debug(MSG_END_FCT + "importPAULA(String)");
	}
	
	/**
	 * Importiert PAULA-Dokumente aus einem gegebenen Verzeichnis und schreibt die Tabellendateien
	 * in ein tempor�rverzeichniss. Es k�nnen nur Korpora importiert werden, die zuvor getypt wurden.
	 * Achtung: Das dst-Verzeihniss ist nicht ganz gekl�rt. Es wird als tmp-Verzeichniss behandelt und
	 * evtl. gel�scht.
	 * @param srcFolder String - Name des Quellverzeichniss des getypten Korpus
	 * @param dstFolder String - Name des Zielverzeichnisses des getypten Korpus
	 * @param toDot boolean - Gibt an, ob eine Dot-Datei dieses Graphens erstellt werden soll
	 */
	public void importPAULA(	String srcFolder, 
								String dstFolder, 
								boolean toDot) throws Exception
	{
		if (logger!= null) logger.debug(MSG_START_FCT + "importPAULA(String)");
		
		if ((srcFolder == null) || (srcFolder.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_DST_NAME);
		File srcFile= new File(srcFolder);
		if ((dstFolder == null) || (dstFolder.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_DST_NAME);
		File dstFile= new File(dstFolder);
		this.importPAULA(srcFile, dstFile, null, toDot);
		
		if (logger!= null) logger.debug(MSG_END_FCT + "importPAULA(String)");
	}
	
	/**
	 * Importiert PAULA-Dokumente aus einem gegebenen Verzeichnis und schreibt die Tabellendateien
	 * in ein tempor�rverzeichniss. Es k�nnen nur Korpora importiert werden, die zuvor getypt wurden.
	 * @param srcFolder File - Quellverzeichniss des getypten Korpus
	 * @param dstFolder File - Zielverzeichniss, in das die Outputdateien geschrieben werden 
	 * @param tmpFolder File - tempor�res Verzeichnis in das die Tabellendateien geschrieben werden (der Inhalt wird ggf. gel�scht)
	 * @param toDot boolean - Gibt an, ob eine Dot-Datei dieses Graphens erstellt werden soll
	 */
	public void importPAULA(	File srcFolder,
								File dstFolder,
								File tmpFolder, 
								boolean toDot) throws Exception
	{
		if (logger!= null) logger.debug(MSG_START_FCT + "importPAULA(file, file)");
		
		//pr�fen ob Verzeichnisse existieren
		if ((srcFolder== null) || (!srcFolder.exists())) throw new Exception(ERR_EMPTY_SRC_FOLDER);
		//File ist kein Verzeichnis
		if (!srcFolder.isDirectory()) throw new Exception(ERR_NOT_A_DIR + srcFolder.getCanonicalPath());
		
		//wenn tempVerzsichnis null ist
		if (tmpFolder == null) 
			tmpFolder= new File(srcFolder.getCanonicalPath() + "/" + FILE_TMP_PATH);
		
		//wenn tmpVerzeichniss nicht existiert, dann erstellen
		if (!tmpFolder.exists()) 
			//Verzeichnios kann nicht erstellt werden
			if (!tmpFolder.mkdir()) throw new Exception(ERR_TMP_FOLDER + tmpFolder.getCanonicalPath());
		
		//wenn dstVerzsichnis null ist
		if (dstFolder == null) 
			dstFolder= tmpFolder;
		if (!dstFolder.exists())
			dstFolder.mkdir();
		
		//erzeugt den aktuell zu benutzenden Mapper
		AbstractMapper mapper= this.getMapper();
		//ruft den aktuellen Mapper auf 
		mapper.map(srcFolder, dstFolder, tmpFolder, toDot);
		
		if (logger!= null) logger.debug(MSG_END_FCT + "importPAULA(file, file)");
	}
	
	/**
	 * Gibt Informationen �ber dieses Objekt als String zur�ck. 
	 * @return String - Informationen �ber dieses Objekt
	 */
	public String toString()
		{ return(getInfo()); }
	
//	 ============================================== main Methode ==============================================	
	public static void main(String args[])
	{
		//Variablen f�r Parameter
		boolean help= false;			//Hilfe ausgeben
		boolean dot= false;				//DOT ausgeben ausgeben
		String srcFolder= "";			//Name des Quellverzeichnisses
		String dstFolder= "";			//Name des Zielverzeichnisses
		
		//log4j einrichten
		Logger logger= Logger.getLogger(PAULAImporter.class);	//log4j initialisieren
		DOMConfigurator.configure(FILE_LOG);			//log-FileEinstellungen
		
		if (logger!= null) logger.info(MSG_START);
		
		
		//Zeit stoppen beginnen
		Timer timer = new Timer();
		timer.start();
		
		try
		{
			//Eingabeparameter pr�fen
			for (int i=0; i < args.length; i++)
			{
				// Hilfe ausgeben
				if (args[i].equalsIgnoreCase(FLAG_HELP)) help= true;
				
				// Graph als dot ausgeben
				else if (args[i].equalsIgnoreCase(FLAG_DOT)) dot= true;
				
				// zu analysierendes Verzeichniss als Verzeichniss
				else if (args[i].equalsIgnoreCase(FLAG_FOLDER_SRC))
				{
					if (args.length < i+2) help= true;
					else
					{
						srcFolder= args[i+1];
						i++;
					}
				}
				// Verzeichniss in das die Relationsdateien geschrieben werden sollen
				else if (args[i].equalsIgnoreCase(FLAG_FOLDER_DST))
				{
					if (args.length < i+2) help= true;
					else
					{
						dstFolder= args[i+1];
						i++;
					}
				}
			}
			
			//wichtige Flags fehlen, also help = true
			if (srcFolder.equalsIgnoreCase(""))
				{ help= true; }
			
			// Hilfe ausgeben, Program wird nicht gestartet
			if (help)
			{
				CToolDescriptor descriptor= new CToolDescriptor(FILE_DESCRIPTION);
				CToolDescription desc= descriptor.getDescription(TOOLNAME);
				logger.info(desc);
			}
			// Programm starten
			else
			{
				PAULAImporter importer= new PAULAImporter(logger);
				if (logger != null) logger.info(MSG_STD + "src folder: "+ srcFolder);
				if (logger != null) logger.info(MSG_STD + "dst folder: "+ dstFolder);
				importer.importPAULA(srcFolder, dstFolder, dot);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		timer.stop();
		if (logger!= null) logger.info(MSG_TIME + timer);
		if (logger!= null) logger.info(MSG_END);
	}


}
