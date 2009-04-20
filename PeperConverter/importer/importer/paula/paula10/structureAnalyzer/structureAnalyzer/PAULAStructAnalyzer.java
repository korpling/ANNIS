package importer.paula.paula10.structureAnalyzer.structureAnalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import importer.paula.paula10.structureAnalyzer.util.korpGraph.Graph;
import importer.paula.paula10.structureAnalyzer.util.korpGraph.Node;
import importer.paula.paula10.util.timer.Timer;
import importer.paula.paula10.util.toolDescriptor.CToolDescription;
import importer.paula.paula10.util.toolDescriptor.CToolDescriptor;

/**
 * Die Klasse PAULAStructAnalyzer �bernimmt eine Pre-Analyse eines PAULA-Verzeichnisses. 
 * Dabei wird der gegebene Korpus in einen Umschlag (ein verzeichnis) gelegt und es wird
 * eine die Korpusstruktur beschreibende Datei vom Typ struct_korp.dtd erzeugt.<br/>
 * 
 * New PAULAStructureAnalyzer, wich differents document-objects and corpus objects. 
 * A paula file can only lay in a document and a document must be a member of a 
 * corpus-object. A corpus-object can have other corpus objects they are called
 * subcorpus-objects.
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public class PAULAStructAnalyzer
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PAULAStructAnalyzer";		//Name dieses Tools
	//private static final String VERSION= 	"1.0";					//Name dieses Tools
	private static final boolean DEBUG= false;						//Debug-Schalter
	
	//Pfad und Dateiname f�r Settingfiles
	private static final String FILE_DESCRIPTION= 	"/PAULAStructureAnalyzer/settings/description.xml";	//Name der Datei, die dieses Tool beschreibt
	private static final String FILE_LOG=			"/PAULAStructureAnalyzer/settings/log4j.xml";			//Name der log4j Datei
	private static final String FILE_DEST=			"corp_struct.xml";			//Zieldatei, in die die Struktur geschrieben werden soll
	private static final String FILE_DOT=			"corp_struct";
	
	//Namen von Verzeichnissen, die keine Korpora sind
	//private static final String[] NON_KOPUS_NAMES= {"-coref", "-is", "-rst", "-tiger"};
	
	//Schl�sselworte
	private static final String KW_DST_PREFIX=		"ENV_";	//Prefix f�r den Umschlagnamen
	private static final String KW_XML_LIST=		"XML_LIST";
	private static final String KW_NODE_TYPE=		"NODE_TYPE";
	private static final String KW_CORP_NODE=		"CORP";
	private static final String KW_CORP_NAME=		"CORP_NAME";
	private static final String KW_DOC_NODE=		"DOC";
	private static final String KW_DOC_NAME=		"DOC_NAME";
	private static final String KW_FILE_NODE=		"PAULAFILE";
	private static final String KW_FILE_NAME=		"FILE_NAME";
	private static final String KW_PATH_NAME=		"PATH_NAME";
	private static final String KW_PATH_SEP=		"/";
	
	//Flags f�r den Programmaufruf
	private static final String FLAG_HELP=			"-h";	//Flag f�r Hilfe
	private static final String FLAG_DOT=			"-dot";	//Flag Graph als Dot ausgeben
	private static final String FLAG_SRC_PATH=		"-s";	//Flag f�r Quellordner
	private static final String FLAG_DST_PATH=		"-d";	//Flag f�r Zielordner, in der Korpus geschrieben wird
	private static final String FLAG_SUBDIR=		"-sd";	//Flag f�r SubDirectory im Zielpfad	
		
	private Logger logger = null;				//logger f�r log4j
	/**
	 * Der Envelope-Folder in den alles geschrieben wird
	 */
	private File envPath= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START=			"******************** start relANNISConverter::"+TOOLNAME+" ********************";
	private static final String MSG_END=			"******************** end start relANNISConverter::"+TOOLNAME+" ********************";
	private static final String MSG_INITED=			MSG_STD + "object has been initialized";
	private static final String MSG_SUCCESS=		MSG_STD + "corpus structure has been successfully analyzed.";
	private static final String MSG_TIME=			MSG_STD + "time required to analyze folder: ";
	private static final String MSG_START_FCT=		MSG_STD + "start methode: ";
	private static final String MSG_END_FCT=		MSG_STD + "end methode: ";
	private static final String MSG_ENV=				MSG_STD + "creating envelope....................OK";
	private static final String MSG_COPYING=			MSG_STD + "copying files to destination.........OK";
	private static final String MSG_CREATING_KORPTREE=	MSG_STD + "creating corpus tree.................OK";
	private static final String MSG_CREATING_DOT=		MSG_STD + "creating dot file from corpus tree...OK";
	private static final String MSG_PRINT_TO_XML=		MSG_STD + "print corpus tree to xml.............OK";
	private static final String MSG_SRC_PATH=			MSG_STD + "source path:\t\t";
	private static final String MSG_DST_PATH=			MSG_STD + "destination path:\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_SRC=			MSG_ERR + "The given source path (path to corpus) is empty.";
	private static final String ERR_SRC_NOT_EXISTS=		MSG_ERR + "The given source path does not exist: ";
	private static final String ERR_EMPTY_DST=			MSG_ERR + "The given destination path (name of path for the envekope) is empty.";
	private static final String ERR_SRC_NOT_DIR=		MSG_ERR + "The given source path is not a folder: ";
	private static final String ERR_DST_NOT_DIR=		MSG_ERR + "The given source path is not a folder: ";
	private static final String ERR_EMPTY_FOLDER=		MSG_ERR + "The given source folder is empty.";
	private static final String ERR_FOLDER_NOT_EXISTS=	MSG_ERR + "The given source folder does not exists: ";
	private static final String ERR_NO_FOLDER=			MSG_ERR + "The given source folder is not a folder: ";
	private static final String ERR_EMPTY_FILE=			MSG_ERR + "The given file is empty.";
	private static final String ERR_FILE_NOT_EXISTS=	MSG_ERR + "The given file does not exits: ";
	//private static final String ERR_NOT_A_FILE=			MSG_ERR + "The given file is not a file: ";
	private static final String ERR_CANNOT_CREATE_DIR=	MSG_ERR + "Cannot create directory: ";
	private static final String ERR_FILE_NO_FOLDER=		MSG_ERR + "The given file is not a folder: ";
	private static final String ERR_FILE_NO_FILE=		MSG_ERR + "The given file is not a file: ";
	//private static final String ERR_CANNOT_CREATE_FILE=	MSG_ERR + "Cannot create file: ";
	private static final String ERR_NO_PAULA_FILES_4_DOC=	MSG_ERR + "There is an error in corpus structure, because there is a folder without a paula-document or a subdirectory: ";
	private static final String ERR_NO_PARENT_DOC=			MSG_ERR + "Cannot create a document node, because there is no corpus node above the following document node: ";
	private static final String ERR_NO_PARENT_FILE=			MSG_ERR + "Cannot create a file node, because there is no document node above the following file node: ";
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Instanziiert ein Objekt vom Typ PAULAStructAnalyzer. 
	 */
	public PAULAStructAnalyzer()
	{
		Logger.getLogger(PAULAStructAnalyzer.class);
		if (this.logger != null) this.logger.debug(MSG_INITED);
	}
	
	/**
	 * Instanziiert ein Objekt vom Typ PAULAStructAnalyzer.
	 * @param logger Logger - ein Logger zur Nachrichtenausgabe 
	 */
	public PAULAStructAnalyzer(Logger logger)
	{
		this.logger= logger;
		if (this.logger != null) this.logger.debug(MSG_INITED);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Es werden alle Dateien, die im Quellverzeichniss liegen in das Zielverzeichniss 
	 * geschrieben. Beide verzeichniss m�ssen bereits existieren.
	 * @param srcPath File - Quellpfad
	 * @param destPath File - Zielpfad
	 */
	private void copyFiles(File srcPath, File destPath) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "copyFiles()");
		
		if (srcPath == null) throw new Exception(ERR_EMPTY_FOLDER);
		if (!srcPath.exists()) throw new Exception(ERR_FOLDER_NOT_EXISTS);
		if (destPath== null) throw new Exception(ERR_EMPTY_FOLDER);
		
		String dstPathName= destPath.getCanonicalPath() + KW_PATH_SEP +srcPath.getName();
		this.copyFilesRek(srcPath, new File(dstPathName));
		
		if (DEBUG) System.out.println(MSG_END_FCT + "copyFiles()");
	}
	
	/**
	 * Geht rekursiv ein verzeichniss durch und kopiert Dateien von der Quelle zum Ziel.
	 * @param srcPath
	 * @param destPath
	 */
	private void copyFilesRek(File srcPath, File destPath) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "copyFilesRek()");
	
		//wenn Verzeichniss nicht existiert, dann erstellen
		if (!destPath.exists()) 
			if(!destPath.mkdirs()) throw new Exception(ERR_FILE_NO_FOLDER);
		
		File fileList[]= srcPath.listFiles();
		
		//Dateien in Korpusknoten eintragen, wenn vorhanden
		if ((fileList!= null) && (fileList.length > 0))
		{
			for (int i = 0; i < fileList.length; i++)
			{
				//wenn File eine Datei ist
				if(fileList[i].isFile()) 
				{
					this.copyFile(fileList[i], new File(destPath.getCanonicalFile() + KW_PATH_SEP + fileList[i].getName()));
				}
				//wenn File ein Verzeichniss ist
				else
				{
					this.copyFilesRek(fileList[i], new File(destPath.getCanonicalFile() + KW_PATH_SEP + fileList[i].getName()));
				}
			}
			
			if (DEBUG) System.out.println(MSG_END_FCT + "copyFilesRek()");
		}
	}
	
	/**
	 * Kopiert eine Quelldatei in eine Zieldatei
	 * @param src File - zu kopierende Datei
	 * @param dst File - Zieldatei
	 */
	private void copyFile(File src, File dst) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "copyFile()");
	
		if (src == null) throw new Exception(ERR_EMPTY_FILE);
		if (!src.exists()) throw new Exception(ERR_FILE_NOT_EXISTS);
		if (!src.isFile()) throw new Exception(ERR_FILE_NO_FILE);
		
		if (dst == null) throw new Exception(ERR_EMPTY_FILE);
		
		FileInputStream  fis = null; 
		FileOutputStream fos = null; 
		
		fis = new FileInputStream(src ); 
		fos = new FileOutputStream(dst); 
		 
		byte[] buffer = new byte[ 0xFFFF ]; 
		for ( int len; (len = fis.read(buffer)) != -1; ) 
			fos.write( buffer, 0, len ); 
		
		fis.close();
		fos.close();
		if (DEBUG) System.out.println(MSG_END_FCT + "copyFile()");
	}

	/**
	 * Erstellt einen Baum, der das Korpus enth�lt aus dem gegebenen PAULA-Verzeichniss.
	 * @param srcFolder File - PAULA-Verzeichniss, der das Quellcorpus enth�lt
	 * @return Baumstruktur des Korpus 
	 */
	private Graph createKorpTree(File srcFolder) throws Exception
	{
		if (srcFolder == null) throw new Exception(ERR_EMPTY_FOLDER);
		if (!srcFolder.exists()) throw new Exception(ERR_FOLDER_NOT_EXISTS+ srcFolder.getCanonicalPath());
		if (!srcFolder.isDirectory()) throw new Exception(ERR_NO_FOLDER+ srcFolder.getCanonicalPath());
		
		Graph korpTree= null;
		korpTree= new Graph(true, true, logger);
		
		rekCreateKorpTree(srcFolder, korpTree, null, null);
		
		return(korpTree);
	}
	
	/**
	 * Liest den Inhalt eines Verzeichnisses und erzeugt f�r ein Verzeichniss einen Knoten
	 * Jede PAULA-XML-Dateien wird in eine Liste geschrieben und als Attribute an einen
	 * Knoten geh�ngt. Diese Methode durchsucht rekursiv alle Verzeichnisse. 
	 * @param currFolder File - aktuell zu durchsuchendes Verzeichniss
	 * @param korpTree Graph - Graph in den die Knoten geschrieben werden
	 * @param parent Node - Vaterverzeichniss
	 * @param path String - 
	 * @throws Exception
	 */
	private void rekCreateKorpTree(	File currFolder, 
									Graph korpTree, 
									Node parent, 
									String path) throws Exception
	{ 
		if (DEBUG) System.out.println(MSG_START_FCT + "rekCreateFolder()");
		String newPath= null;
		
		if (path != null) newPath= path + KW_PATH_SEP +currFolder.getName();
		else newPath= currFolder.getName();
		//System.out.println("NewPath: "+ newPath);
		//if (path != null) newPath= path + KW_PATH_SEP +currFolder.getName();
		//else newPath= this.envPath.getCanonicalPath();
		/**
		 * Liste aller file-Objekte in diesem Ordner
		 */
		File fileList[]= currFolder.listFiles();		
		/**
		 * Liste aller PAULA-Dateien in diesem Ordner
		 */
		Vector<File> paulaFileList= new Vector<File>();
		/**
		 * Liste aller Unterverzeichnisse in diesem Ordner
		 */
		Vector<File> folderList= new Vector<File>();
		//wenn aktuelles Verzeichniss Unterverzeichnisse oder Dateien enth�lt
		if ((fileList!= null) && (fileList.length > 0))
		{
			for (File currFile: fileList)
			{
				//wenn file ein Verzeichniss ist
				if (currFile.isDirectory())	
				{
					folderList.add(currFile);
				}
				//wenn file eine XML-Datei ist
				else if (this.isXML(currFile))
				{
					paulaFileList.add(currFile);
				}
			}
		}
		//wenn Verzeichniss Unterverzeichnisse hat --> Verzeichniss ist Korpus
		if ((folderList!= null) && (!folderList.isEmpty()))
		{
			Node corpNode= null;
			String nodeName= currFolder.getCanonicalPath();
			corpNode= new Node(nodeName);
			//den erzeugten Knoten als Korpusknoten kenntlich machen
			corpNode.setValue(KW_NODE_TYPE, KW_CORP_NODE);
			corpNode.setValue(KW_CORP_NAME, currFolder.getName());
			//Korpusknoten in Graph einf�gen
			korpTree.addNode(corpNode);
			if (parent!= null) korpTree.createEdge(parent.getName(), corpNode.getName());
			//alle Subkorpora oder Dokumente unter dem aktuellen Korpus durchgehen
			for (File currFile: folderList)
			{
				this.rekCreateKorpTree(currFile, korpTree, corpNode, newPath);
			}
		}
		//wenn Verzeichniss keine Unterverzeichnisse hat --> Verzeichniss ist Document
		else
		{
			if ((paulaFileList== null) || (paulaFileList.isEmpty()))
				throw new Exception(ERR_NO_PAULA_FILES_4_DOC + currFolder.getName());
			Node docNode= new Node(currFolder.getCanonicalPath());
			//den erzeugten Knoten als Korpusknoten kenntlich machen
			docNode.setValue(KW_NODE_TYPE, KW_DOC_NODE);
			docNode.setValue(KW_DOC_NAME, currFolder.getName());
			//Korpusknoten in Graph einf�gen
			korpTree.addNode(docNode);
			if (parent!= null) korpTree.createEdge(parent.getName(), docNode.getName());
			else throw new Exception(ERR_NO_PARENT_DOC + currFolder.getName());
			Node paulaNode= null;
			//alle PAULA-Dokumente unter dem aktuellen Dokument durchgehen
			for (File currFile: paulaFileList)
			{
				String paulaName= newPath + KW_PATH_SEP + currFile.getName();
				paulaNode= new Node(paulaName);
				//Typ dieses Knotens setzen
				paulaNode.setValue(KW_NODE_TYPE, KW_FILE_NODE);
				//Namen der Datei setzen
				paulaNode.setValue(KW_FILE_NAME, currFile.getName());
				//Pfad der Datei setzen
				paulaNode.setValue(KW_PATH_NAME, this.envPath.getCanonicalPath() + KW_PATH_SEP + newPath + KW_PATH_SEP +currFile.getName());//currFile.getCanonicalPath());
				//System.out.println("FilePath: "+ this.envPath.getCanonicalPath() + KW_PATH_SEP + newPath + KW_PATH_SEP + currFile.getName());//currFile.getCanonicalPath());
				//Korpusknoten in Graph einf�gen
				korpTree.addNode(paulaNode);
				if (parent!= null) korpTree.createEdge(docNode.getName(), paulaNode.getName());
				else throw new Exception(ERR_NO_PARENT_FILE + currFile.getName());
			}
			/*
			for (File currFile: paulaFileList)
			{
				if (this.logger!= null) this.logger.debug(MSG_STD +"is xml file:\t"+fileList[i].getCanonicalPath()+"...YES");
				//relativen Dateinamen erstellen
				String relFileName= newPath + "\\" + fileList[i].getName();
				xmlList.add(relFileName);
			}*/
		}
		/*
		//neuen Korpusknoten erstellen, wenn Korpusname nicht in Blacklist(NON_KORPUS_NAMES)
		if (this.isKorpusName(currFolder.getName()))
		{
			//Korpusknoten erstellen
			//korpNode= new Node(currFolder.getName());
			korpNode= new Node(newPath);
		}
		
		//Dateien in Korpusknoten eintragen, wenn vorhanden
		if ((fileList!= null) && (fileList.length > 0))
		{
			Vector<String> xmlList= new Vector<String>();
			for (int i = 0; i < fileList.length; i++)
			{
				//wenn File eine XML-Datei ist
				if (isXML(fileList[i])) 
				{
					if (this.logger!= null) this.logger.debug(MSG_STD +"is xml file:\t"+fileList[i].getCanonicalPath()+"...YES");
					//relativen Dateinamen erstellen
					String relFileName= newPath + "\\" + fileList[i].getName();
					xmlList.add(relFileName);
					//Datei in das Envelope-Verzeichniss kopieren
					//this.copyFile(fileList[i], new File(newPath));
				}
				else 
				{
					//wenn Datei ein Verzeichniss ist
					if (fileList[i].isDirectory()) folderList.add(fileList[i]); 
					if (this.logger!= null) this.logger.debug(MSG_STD +"is xml file:\t"+fileList[i].getCanonicalPath()+"...NO");
				}
			}
			//wenn aktuelles verzeichnis ein neuer Korpus ist
			if (korpNode != null) korpNode.setValue(KW_XML_LIST, xmlList);
			//wenn Verzeichniss kein neuer Korpus ist
			else
			{
				Vector<String> oldFiles= (Vector<String>)parent.getValue(KW_XML_LIST);
				oldFiles.addAll(xmlList);
				parent.changeValue(KW_XML_LIST, oldFiles);
			}
		}
		//wenn Verzeichniss ein neuer Korpus ist, dann einen Korpusknoten in den Graphen schreiben
		if (korpNode != null) korpTree.addNode(korpNode);
		//Kante einf�gen, wenn es einen Vaterfolder gibt und der aktuelle Folder ein neuer Korpus ist
		if ((parent!= null) && (korpNode != null))
		{
			korpTree.createEdge(parent.getName(), korpNode.getName());
		}
		
		// wenn es weitere Verzeichnisse in diesem Verzeichniss gibt
		if (!folderList.isEmpty())
		{
			for(File childFolder: folderList)
			{
				this.rekCreateKorpTree(childFolder, korpTree, korpNode, newPath);
			}
		}	
		*/
		if (DEBUG) System.out.println(MSG_END_FCT + "rekCreateFolder()");
	}
	
	/**
	 * Pr�ft, ob eine Datei eine XML-Endung besitzt
	 * @param file File- zu pr�fende Datei
	 * @return true, wenn Datei eine XML-Datei, false sonst
	 */
	private boolean isXML(File file) throws Exception
	{
		if (file== null) throw new Exception(ERR_EMPTY_FILE);
		if (!file.exists()) throw new Exception(ERR_FILE_NOT_EXISTS);
		
		boolean retVal= false;
		if (file.isFile()) 
		{
			String xmlEnding= ".xml";
			Pattern pattern= Pattern.compile(xmlEnding, Pattern.CASE_INSENSITIVE);
			Matcher matcher= pattern.matcher(file.getName());
			if (matcher.find()) retVal= true;
		}	
		return(retVal);
	}
	
	
	/**
	 * Gibt true zur�ck, wenn der Name nicht in der Liste der Nicht-Korpusnamen steht. 
	 * Dadurch wird festgelegt, ob es sich bei dem gegebenen Korpusnamen wirklich um einen
	 * Korpusnamen handelt oder um ANNIS spezifische Verzeichnissnamen.
	 * @param name String - der zu pr�fende Korpusname
	 * @return true, wenn name kein spezifischer ANNIS-verzeichnissnamen, false sonst
	 */
	private boolean isKorpusName(String name)
	{
		/*
		Pattern pattern= null;
		for (String nonKorpusName: NON_KOPUS_NAMES)
		{
			pattern= Pattern.compile(nonKorpusName, Pattern.CASE_INSENSITIVE);
			Matcher matcher= pattern.matcher(name);
			if (matcher.find()) return(false); 
		}
		*/
		return(true);
	}
	
	/**
	 * Schreibt den �bergebenen corpus-Graphen in die �bergebene Datei. Dies entspricht
	 * dem XML-Format.
	 * @param korpGraph Graph - der zu traversierende Graph
	 * @param destFile File -Zieldatei, in die der Graph geschrieben werden soll 
	 */
	private void printToXML(Graph korpGraph, File destFile) throws Exception
	{
		XMLWriter xmlWriter= new XMLWriter();
		xmlWriter.setSettingDir(getSettingDir());
		xmlWriter.printTree(destFile, korpGraph, KW_XML_LIST);
	}
	
	/**
	 * Erzeugt einen neuen Umschlag f�r diesen Korpus in dem �bergebenen Verzeichniss und
	 * kopiert alle Korpusdateien in dieses Verzeichniss.
	 * @param dstPath File - Zielverzeichniss, in das der Umschlag geschrieben wird.
	 * @param korpName Dtring - Name des Hauptcorpus
	 * @return Der Umschlag wird als File-Objekt zur�ckgegeben
	 */
	private File createENV(File dstPath, String korpName) throws Exception
	{
		String envName= dstPath.getCanonicalPath() + KW_PATH_SEP + KW_DST_PREFIX + korpName;
		File envFile= new File(envName);
		if (!envFile.exists())
			if (!envFile.mkdirs()) throw new Exception(ERR_CANNOT_CREATE_DIR + envName);
		
		return(envFile);
	}
//	 ============================================== �ffentliche Methoden ==============================================
	private File settingDir= null;
	public void setSettingDir(File settingDir)
	{
		this.settingDir= settingDir;
	} 
	
	public File getSettingDir()
	{
		return(this.settingDir);
	}
	/**
	 * Analysiert das �bergene Verzeichniss srcPath, erstellt einen Umschlag und schreibt
	 * diesen in das Verzeichniss dstPath. Inhalt des Umschlages sind eine Analyse-Datei
	 * vom Typ korp_struct.dtd und das Korpus
	 * @param srcPath File - Quellverzeichniss (das Korpus)
	 * @param dstPath File - Zielverzeichniss (Ort an den der Umschlag geschrieben wird)
	 * @param toDot boolean - Gibt an, ob eine Dot-Datei dieses Graphens erstellt werden soll
	 * @param subDIr boolean - Gibt an, ob ein Unterverzeichniss im Zielpfad angelegt werden soll
	 * @return File-objekt, dass auf den Umschlag zeigt
	 */
	public File analyze(	File srcPath, 
							File dstPath,
							boolean toDot,
							boolean subDir) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "analyze()");
			
		if (srcPath == null) throw new Exception(ERR_EMPTY_SRC);
		if (!srcPath.exists()) throw new Exception(ERR_SRC_NOT_EXISTS + srcPath.getCanonicalPath());
		if (!srcPath.isDirectory()) throw new Exception(ERR_SRC_NOT_DIR + srcPath.getCanonicalPath());
		if (dstPath == null) throw new Exception(ERR_EMPTY_DST);
		if ((dstPath.exists()) && (!dstPath.isDirectory())) throw new Exception(ERR_DST_NOT_DIR + srcPath.getCanonicalPath());
		
		//erstelle neuen Umschlag
		if (subDir)
			this.envPath = this.createENV(dstPath, srcPath.getName());
		else this.envPath = dstPath;
		//System.out.println("ENV Ordner: "+ this.envPath);
		if (this.logger!=  null) this.logger.info(MSG_ENV); 
		//kopiere Dateien aus Quelle in neuen Umschlag
		this.copyFiles(srcPath, envPath);
		if (this.logger!=  null) this.logger.info(MSG_COPYING);
		//erstelle Korpusgrtaph zu der Dateistruktur
		Graph korpTree= this.createKorpTree(srcPath);
		if (this.logger!=  null) this.logger.info(MSG_CREATING_KORPTREE);
		//Dot-Datei erstellen, wenn gew�nscht
		if (toDot)
		{
			//erstelle Dot in den Umschlag
			korpTree.prinToDot(envPath + KW_PATH_SEP + FILE_DOT);
			if (this.logger!=  null) this.logger.info(MSG_CREATING_DOT);
		}
		//erstelle AusgabeDatei
		File dstFile= new File(envPath + KW_PATH_SEP +FILE_DEST);
		this.printToXML(korpTree, dstFile);
		if (this.logger!=  null) this.logger.info(MSG_PRINT_TO_XML);
		
		if (DEBUG) System.out.println(MSG_END_FCT + "analyze()");
		return(envPath);
	}
	
//	 ============================================== main Methode ==============================================	

	public static void main(String args[])
	{
		System.out.println(MSG_START);
		
		//Variablen f�r Parameter
		boolean help= false;				//Hilfe ausgeben
		boolean dot= false;					//DOT ausgeben ausgeben
		String srcPathStr= "";				//Name des Korpusverzeichnis
		String dstPathStr= "";				//Name des des Ziels in das der Umschlag kommt
		boolean subDir= false;				//Subdirectory im Zielpfad
		
		//log4j einrichten
		Logger logger= Logger.getLogger(PAULAStructAnalyzer.class);	//log4j initialisieren
		DOMConfigurator.configure(FILE_LOG);			//log-FileEinstellungen
		
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
				
				// Subdirectory im DST-PAth erzeugen ausgeben
				if (args[i].equalsIgnoreCase(FLAG_SUBDIR)) subDir= true;
				
				// Graph als dot ausgeben
				else if (args[i].equalsIgnoreCase(FLAG_DOT)) dot= true;
				
				// zu analysierendes Verzeichniss als Verzeichniss
				else if (args[i].equalsIgnoreCase(FLAG_SRC_PATH))
				{
					if (args.length < i+2) help= true;
					else
					{
						srcPathStr= args[i+1];
						i++;
					}
				}
				
				// zu analysierendes Verzeichniss als Archiv
				else if (args[i].equalsIgnoreCase(FLAG_DST_PATH))
				{
					if (args.length < i+1) help= true;
					else
					{
						dstPathStr= args[i+1];
						i++;
					}
				}
			}
			
			//wichtige Flags fehlen, also help = true
			if (srcPathStr.equalsIgnoreCase("") && (dstPathStr.equalsIgnoreCase("")))
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
				PAULAStructAnalyzer analyzer= new PAULAStructAnalyzer(logger);
				File srcPath= new File(srcPathStr);
				File dstPath= new File(dstPathStr);
				System.out.println(MSG_SRC_PATH + srcPath.getCanonicalPath());
				System.out.println(MSG_DST_PATH + dstPath.getCanonicalPath());
				analyzer.analyze(srcPath, dstPath, dot, subDir);
				System.out.println();
				System.out.println(MSG_SUCCESS);
				//Timer stoppen
				timer.stop();
				System.out.println (MSG_TIME + timer);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		System.out.println(MSG_END);
	}
}
