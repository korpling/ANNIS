package paulaAnalyzer;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.ext.DefaultHandler2;

import specificAnalyzer.AbstractAnalyzer;

import util.depGraph.FileDepGraph;
import util.depGraph.Graph;
import util.depGraph.Node;
import util.depGraph.TraversalObject;
import util.settingMgr.SettingMgr;
import util.settingMgr.SettingObject;
import util.timer.Timer;
import util.toolDescriptor.CToolDescription;
import util.toolDescriptor.CToolDescriptor;

/**
 * Die Klasse PAULAAnalyzer analysiert ein durch ein Verzeichniss gegebenes PAULA-Korpus.
 * Das Korpus muss Pre-Analysiert sein, und �ber eine korp_struct-Datei verf�gen. 
 * Der PAULA-Analyzer wendet eine Vilezahl von spezifischen Analyzern an, um eine bestimmte
 * PAULA-Datei zu analysieren und zu klassifizieren. Die Menge der spezifischen Analyzer 
 * ist dabei erweiterbar.<br/>
 * Die Ergebnisse der Klassifikation und Analyse werden in eine Datei vom Typ 
 * <i>typed_korp.dtd</> im Envelope-Verzeichniss des gegebenen Korpus angelegt.
 *   
 * @author Florian Zipser
 * @version 1.0
 */
public class PAULAAnalyzer implements SettingObject, TraversalObject
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PAULAAnalyzer";		//Name dieses Tools
	private static final String VERSION= 	"1.0";					//Name dieses Tools
	
	//vorgegebene Schl�sselworte
	protected static final String KW_NAME= "analyzer name";			//Schl�sselwort f�r den Analyzer name
	protected static final String KW_VERSION= "version";			//Schl�sselwort f�r die Version
	
	//Namen der Flags
	private static final String FLAG_HELP= "-h";					//Flag f�r Hilfe ausgeben
	private static final String FLAG_DOT= "-dot";					//Flag, das angibt ob der entstehende Graph als Dot ausgegeben werden soll
	private static final String FLAG_FOLDER= "-f";					//Flag f�r Namen des Verzeuchnisses
	private static final String FLAG_ARCHIVE= "-a";					//Flag f�r Namen des Archives
	
	//Pfad und Dateiname f�r Settingfiles
	private static final String FILE_SETTING=		"./PAULAAnalyzer/settings/PASettings.xml";					//Name der default Setting-Datei
	private static final String FILE_DESCRIPTION= 	"./PAULAAnalyzer/settings/description.xml";	//Name der Datei, die dieses Tool beschreibt
	private static final String FILE_LOG=			"./PAULAAnalyzer/settings/log4j.properties";			//Name der log4j Datei
	private static final String FILE_KORP_STRUCT=	"corp_struct.xml";				//default name der Korpusstrukturdatei
	private static final String FILE_TYPED_KORP=	"typed_corp.xml";				//default name der Korpusstrukturdatei
		
	//Namen f�r auszulesende Settings
	private static final String SET_ANALYZERS=		"SPEC_ANALYZERS";				//Keyword um Namen der speziellen Analyzer zu lesen
	
	private PASettingHandler setHandler= null;
	
	private Logger logger= Logger.getLogger(PAULAAnalyzer.class);			//log4j zur Nachrichtenausgabe
	private MainAnalyzer mainAnalyzer= null;			//Analyzer, der den Inhalt der paulaDatei auf die specificAnalyzer verteilt 
	
	private Vector<AnalyzerParams> anaParamlist= null;					//nutzbare Analyzer
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START=			"******************** start "+TOOLNAME+" ********************";
	private static final String MSG_END=			"******************** end "+TOOLNAME+" ********************";
	private static final String MSG_INIT=			MSG_STD + "PAULAAnalyzer is successfully initialized...";
	private static final String MSG_INVOKE_FCT=		MSG_STD + "invoke methode ";
	private static final String MSG_IN_PROCESS=		MSG_STD + "analyzing is in process for folder: ";
	private static final String MSG_SUCCESS=		MSG_STD + "PAULA-source has been successfully analyzed.";
	private static final String MSG_TIME=			MSG_STD + "time required to analyze folder: ";
	//	 *************************************** Fehlermeldungen ***************************************
	//private static final String ERR_NO_FILENAME=		MSG_ERR + "The given filename fpr setting file is empty.";
	private static final String ERR_SET_NOT_READ=		MSG_ERR + "The setting file has not been read.";
	private static final String ERR_EMPTY_FOLDER=		MSG_ERR + "The given target folder is empty. No Korpus files can be found.";
	private static final String ERR_NO_FOLDER=			MSG_ERR + "The given folder is not a folder: ";
	private static final String ERR_FOLDER_NOT_EXISTS=	MSG_ERR + "The given envelope-folder for in- and output does not exist: ";
	private static final String ERR_KSFILE_NOT_EXISTS=	MSG_ERR + "The given corpus-structure-file does not exist: ";
	private static final String ERR_KSTREE_NOT_A_TREE=	MSG_ERR + "The computed corpus-strucure-graph is not a tree.";
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt eine Instanz des PAULAAnalyzers. 
	 * @param logger Logger - Ein logger f�r die Nachrichtenausgabe (log4j)
	 */
	public PAULAAnalyzer(Logger logger) throws Exception
	{
		init(logger, FILE_SETTING);
	}
	
	/**
	 * Instanziiert ein PAULAAnalyzer-Objekt und liest aus der �bergebenen Setting-Datei.
	 * @param logger Logger - Ein logger f�r die Nachrichtenausgabe (log4j) 
	 * @param setFile String - Name der Setting Datei
	 * @throws Exception
	 */
	public PAULAAnalyzer(Logger logger, String setFile) throws Exception
	{
		init(logger, setFile);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Initaialisiert dieses Objekt.
	 * @param logger Logger - Ein logger f�r die Nachrichtenausgabe (log4j)
	 * @param setFile String - Name der Setting Datei
	 */
	private void init(Logger logger, String setFile) throws Exception
	{
		this.logger= logger;
		
		if (this.logger != null) logger.debug(MSG_INIT + this);
	}
	
	/**
	 * �berpr�ft die interne Liste mit AnalyzeParams-Objekten. Die einzelnen Parameter werden auf 
	 * Stimmigkeit hin �berpr�ft.
	 * @throws Exception
	 */
	private void checkAnalyzerParams() throws Exception
	{
		if (logger != null) this.logger.debug("has to be implemented (PAULAAnalyzer.checkAnalyzerParams())");
		if (logger != null) this.logger.debug("found analyzers:");
		for (AnalyzerParams aParam : this.anaParamlist)
			if (logger != null) this.logger.debug("\t"+aParam);
	}
	
	
	private Vector<AnalyzeContainer> analyzeFiles(Vector<AnalyzeContainer> aConList) throws Exception
	{
		if (this.anaParamlist== null) throw new Exception(ERR_SET_NOT_READ);
		
		//Abh�ngigkeitsgraphen f�r die PAULA-Dateien erzeugen (gerichtet, nicht geordered, logger)
		FileDepGraph depGraph= new FileDepGraph();
		
		//durch alle AnalyzeContainerObjekte gehen
		for (AnalyzeContainer aCon: aConList)
		{
			//einen Container f�r die Datei erzeugen
			aCon= this.mainAnalyzer.analyze(aCon, depGraph);
			//DEBUG
			//System.out.println(aCon);
		}
		return(depGraph.serialize());
	}
//	 ============================================== �ffentliche Methoden ==============================================

	// ----------------------------------- Methoden f�r SettingObject
	/**
	 * @see util.settingMgr.SettingObject#getSetEntry() 
	 */
	public Hashtable<DefaultHandler2, Vector<String>> getSetEntry() throws Exception
	{
		Hashtable<DefaultHandler2, Vector<String>> setTable= new Hashtable<DefaultHandler2, Vector<String>>();
		Vector<String> setVec= new Vector<String>();
		setVec.add(SET_ANALYZERS);
		
		//SettingHandler erzeugen
		this.setHandler= new PASettingHandler();
		
		setTable.put(this.setHandler, setVec);
		
		return(setTable);
	}
	
	/**
	 * @see util.settingMgr.SettingObject#readSettings()
	 */
	public void readSettings() throws Exception
	{
		this.anaParamlist= this.setHandler.getAnalyzerParams();
		this.checkAnalyzerParams();
		
		//Analyzer initialisieren
		for (AnalyzerParams aParam: this.anaParamlist)
		{
			aParam.setAnalyzer((AbstractAnalyzer) Class.forName(aParam.getClassName()).newInstance());
		}
		//MainAnalyzer initialisieren
		this.mainAnalyzer= new MainAnalyzer(this.anaParamlist);
	}
	
	// ----------------------------------- Ende Methoden f�r SettingObject
	
	// ----------------------------------- Methoden f�r TraversalObject
	/**
	 * @see util.depGraph.TraversalObject#nodeReached(Node, Node, long)
	 */
	public void nodeReached(Node currNode, Node father, long order) throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_INVOKE_FCT + "nodeReached()");
		//wenn der aktuelle Knoten �ber zu analysierende Dateien verf�gt
		if (currNode.hasValue(KSFileReader.getKWACONLIST()))
		{
			Vector<AnalyzeContainer> aConList= ((Vector<AnalyzeContainer>)currNode.getValue(KSFileReader.getKWACONLIST()));
			if ((aConList!= null) && (!aConList.isEmpty()))
			{
				logger.info("analyzing corpus: "+currNode.getName());
				Vector<AnalyzeContainer> newAConList= this.analyzeFiles(aConList);
				currNode.changeValue(KSFileReader.getKWACONLIST(), newAConList);
			}
		}
	}
	
	/**
	 * @see util.depGraph.TraversalObject#nodeLeft(Node, Node, long)
	 */
	public void nodeLeft(Node currNode, Node father, long order) throws Exception
	{
		if (this.logger!= null) this.logger.debug(MSG_INVOKE_FCT + "nodeLeft()");
	}
	
	// ----------------------------------- Ende Methoden f�r TraversalObject
	
	
	/**
	 * St��t das laden der specificAnalyzers an, die in der Datei 'setFile' aufgelistet sind.
	 * @param setFile String - Name der Datei, die die specificANalyzers beshcreibt
	 */
	public void loadAnalyzers(String setFile) throws Exception
	{
		//Settings aus Datei lesen
		SettingMgr setMgr= new SettingMgr(setFile);
		setMgr.addSetListener(this);
		setMgr.start();
	}
	
	/**
	 * St��t das laden der specificAnalyzers an, die in der Datei 'setFile' aufgelistet sind.
	 */
	public void loadAnalyzers() throws Exception
		{ this.loadAnalyzers(FILE_SETTING); } 
	
	/**
	 * Analysiert den Inhalt des �bergebenen Verzeichnisses. Dabei wird nach der Strukturdatei
	 * gesucht und eine Analysedatei erstellt.
	 * @param folder File - zu analysierendes Verzeichniss
	 * @param toDot boolean - Gibt an, ob der erstellte Graph als Dot ausgegeben werden soll
	 */
	public void analyze(File folder, boolean toDot) throws Exception
	{
		this.analyze(folder, FILE_KORP_STRUCT, toDot);
	}
	
	/**
	 * Analysiert den Inhalt des �bergebenen Verzeichnisses. Dabei wird nach der Strukturdatei
	 * gesucht und eine Analysedatei erstellt.
	 * @param folder File - zu analysierendes Verzeichniss
	 * @param ksFileName - Name der Kopus_StructureDatei
	 * @param toDot boolean - Gibt den erzeugten Graphen als Dot aus, wenn dies gew�nscht wird
	 */
	public void analyze(File folder, String ksFileName, boolean toDot) throws Exception
	{
		//wenn Verzeichniss leer
		if (folder== null) throw new Exception(ERR_EMPTY_FOLDER);
		if (!folder.exists()) throw new Exception(ERR_FOLDER_NOT_EXISTS + folder.getCanonicalPath()); 
		if (!folder.isDirectory()) throw new Exception(ERR_NO_FOLDER + folder.getCanonicalPath());
		
		//suche nach corpus_Structur-Datei
		File ksFile= new File(folder.getCanonicalFile() + "/" + ksFileName);
		if (!ksFile.exists()) throw new Exception(ERR_KSFILE_NOT_EXISTS + ksFile.getCanonicalPath());
		
		//corpusStruktur auslesen und in Baum schreiben
		KSFileReader ksReader= new KSFileReader(logger); 
		Graph ksTree= ksReader.getKSGraph(ksFile);
		//Graph als Dot ausgeben, wenn gew�nscht
		if (toDot)
		{
			//corpusStrukturgraphen ausgeben
			ksTree.prinToDot(folder.getCanonicalFile() + "/ksGraph");
		}
		if (!ksTree.isTree()) throw new Exception(ERR_KSTREE_NOT_A_TREE);
		//Ausgabedatei erstellen
		File outFile= new File(folder.getCanonicalPath() + "/" + FILE_TYPED_KORP );
		
		//corpusStrukturgraphen per Tiefensuche durchsuchen
		ksTree.depthFirst(this);
		XMLWriter xmlWriter= new XMLWriter();
		xmlWriter.printTree(outFile, ksTree, KSFileReader.getKWACONLIST());
	}
	
	/**
	 * Gibt Informationen �ber dieses Objekt als String zur�ck. 
	 * @return String - Informationen �ber dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= KW_NAME + ": " + TOOLNAME + ", " + KW_VERSION + ": " + VERSION;
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	

	public static void main(String args[])
	{
		//log4j einrichten
		Logger logger= Logger.getLogger(PAULAAnalyzer.class);	//log4j initialisieren
		PropertyConfigurator.configureAndWatch("./settings/log4j.properties", 60*1000 );
		//PropertyConfigurator.configureAndWatch(FILE_LOG, 60*1000 );
		
		logger.info(MSG_START);
		
		//Variablen f�r Parameter
		boolean help= false;			//Hilfe ausgeben
		boolean dot= false;			//Graph als Dot ausgeben
		String folder= "";			//Name des Verzeichnisses 
		String archive= "";			//Name des Archives
		
		
		
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
				else if (args[i].equalsIgnoreCase(FLAG_FOLDER))
				{
					if (args.length < i+2) help= true;
					else
					{
						folder= args[i+1];
						i++;
					}
				}
				
				// zu analysierendes Verzeichniss als Archiv
				else if (args[i].equalsIgnoreCase(FLAG_ARCHIVE))
				{
					if (args.length < i+1) help= true;
					else
					{
						archive= args[i+1];
						i++;
					}
				}
			}
			
			//wichtige Flags fehlen, also help = true
			if (folder.equalsIgnoreCase("") && (archive.equalsIgnoreCase("")))
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
				PAULAAnalyzer paulaAna= new PAULAAnalyzer(logger);
				paulaAna.loadAnalyzers();
				File envFolder= new File(folder);
				
				logger.info(MSG_IN_PROCESS + envFolder.getCanonicalPath());
				
				paulaAna.analyze(envFolder, dot);
				
				//System.out.println();
				logger.info(MSG_SUCCESS);
				//Timer stoppen
				timer.stop();
				logger.info(MSG_TIME + timer);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		logger.info(MSG_END);
	}
}
