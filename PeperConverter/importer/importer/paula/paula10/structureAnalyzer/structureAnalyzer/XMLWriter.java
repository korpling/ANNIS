package importer.paula.paula10.structureAnalyzer.structureAnalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.util.Vector;

import importer.paula.paula10.structureAnalyzer.util.korpGraph.Graph;
import importer.paula.paula10.structureAnalyzer.util.korpGraph.Node;
import importer.paula.paula10.structureAnalyzer.util.korpGraph.TraversalObject;

/**
 * Die Klasse XMLWriter dient der Ausgabe eines korpus-structure-trees in eine XML-Datei. 
 * @author Florian Zipser
 * @version 1.0
 */
public class XMLWriter implements TraversalObject 
{
	private class AttValPair
	{
		public String name= null;
		public String value= null;
		
		public AttValPair(String name, String val)
		{
			this.name= name;
			this.value= val;
		}
	}
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"XMLWriter";		//Name dieses Tools
	private static final boolean DEBUG=	false;					//Debug-Schalter
	private static final String FILE_DTD= "corp_struct.dtd";	//Name der DTD, auf die die hier erzeugte XML-Datei matscht
	private static final String PATH_DTD= "/paula10/";		//Pfad zur DTD
	
	
	private enum TAG_ENDED {TRUE, FALSE, BOTH};
	private static final String KW_NODE_TYPE=		"NODE_TYPE";
	private static final String KW_CORP_NODE=		"CORP";
	private static final String KW_CORP_NAME=		"CORP_NAME";
	private static final String KW_DOC_NODE=		"DOC";
	private static final String KW_DOC_NAME=		"DOC_NAME";
	private static final String KW_FILE_NODE=		"PAULAFILE";
	
	private static final String KW_FILE_NAME=		"FILE_NAME";
	private static final String KW_PATH_NAME=		"PATH_NAME";
	
	
	//alles zum Tag Korpus
	private static final String TAG_CORP= "corpus";
	private static final String TAG_DOC= "document";
	private static final String ATT_KORP_NAME= "name";
	
	//alles zum Tag file
	private static final String TAG_FILE= "file";		
	private static final String ATT_FILE_NAME= "name";
	private static final String ATT_FILE_PATH= "path";
	
	private String outStr= null;			//String, der die Ausgabedatei zwischenspeichert
	Vector<AttValPair> attributes= null;
	private int currDepth= 0;				//gibt die aktuelle Tiefe im Baum an
	private String KW_FILE=	"";				//Name f�r das Attribut aCon
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START_FCT=		MSG_STD + "start method: ";
	private static final String MSG_END_FCT=		MSG_STD + "end method: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_OUTFILE=		MSG_ERR + "The given output-xml-file is empty.";
	private static final String ERR_KSTREE_NOT_EXISTS=	MSG_ERR + "The given korpus-structure-tree does not exists.";
	private static final String ERR_EMPTY_ATT_NAME=		MSG_ERR + "The name for the attribute file list in node is empty.";
	private static final String ERR_NO_NAME_OR_PATH=	MSG_ERR + "The file-name or path-name is not given for file: ";
//	 ============================================== Konstruktoren ==============================================
//	 ============================================== private Methoden ==============================================
	/**
	 * Gibt einen String zur�ck, der aus dem �bergebenen String einen XML-konformen String
	 * macht.
	 * @param comment String - in einen XML-Kommentarknoten zu packender String
	 * @return XML-Kommentar aus dem �bergebenen String
	 */
	/**
	private String printComment(String comment)
	{
		String retStr= "";
		
		if ((comment != null) && (!comment.equalsIgnoreCase("")))
		{
			retStr= this.printTab() + "<!-- " + comment + " -->\n";
		}
		return(retStr);
	}**/
	
	/**
	 * Gibt einen String zur�ck, der aus der richtigen Anzahl an Tabs besteht, entsprechend
	 * zur aktuellen Baumtiefe.
	 */
	private String printTab()
	{
		String retStr= "";
		for (int i= 0; i < this.currDepth; i++)
			retStr= retStr + "\t";
		
		return(retStr);
	}
	/**
	 * Gibt einen String zur�ck, der den XML-Kopf enth�lt
	 */
	private String printHeader()
	{
		String retStr= "";
		retStr= "<?xml version=\"1.0\" standalone=\"no\"?>\n";
		retStr= retStr + "<!DOCTYPE " + TAG_CORP + " SYSTEM \""+ FILE_DTD + "\">\n";
		return(retStr);
	}
	
	/**
	 * Erstellt ein XML-Elementknoten mit entsprechenden Attributknoten auf den outStr.
	 * @param name node - der zu schreibende Knoten
	 * @param attributes - Attribut-Wert-Paar der Attributknoten
	 * @param end boolean - gibt an, ob das Element ge�ffnet oder geschlossen wird
	 * @return Elementknoten mit Attributknoten als String
	 */
	private String printElem(Node node, Vector<AttValPair> attributes, TAG_ENDED end) throws Exception
	{
		String elemStr= "";
		String type= "";
		//wenn Knoten vom Typ Korpus
		if (((String)node.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_CORP_NODE))
			type= TAG_CORP;
		//wenn Knoten vom Typ Dokument
		else if (((String)node.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_DOC_NODE))
			type= TAG_DOC;
			//wenn Knoten vom Typ Datei
		else if (((String)node.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_FILE_NODE))
			type= TAG_FILE;
		
		//wenn Typ corp ist
		if (((String)node.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_CORP_NODE))
		{
			if ((end== TAG_ENDED.FALSE) || (end== TAG_ENDED.BOTH))
			{
				elemStr= "<" + type;
				//Korpusnamen ermitteln
				String corpName= (String )node.getValue(KW_CORP_NAME);
				elemStr= elemStr + " " + "name" + "= \"" + corpName + "\"" + ">";;
			}
			else
			{
				elemStr= "</" + type + ">";
			}
			elemStr= this.printTab() + elemStr + "\n";
		}
		//wenn Typ document ist
		else if (((String)node.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_DOC_NODE))
		{
			if ((end== TAG_ENDED.FALSE) || (end== TAG_ENDED.BOTH))
			{
				elemStr= "<" + type;
				//Korpusnamen ermitteln
				String docName= (String )node.getValue(KW_DOC_NAME);
				elemStr= elemStr + " " + "name" + "= \"" + docName + "\"" + ">";;
			}
			else
			{
				elemStr= "</" + type + ">";
			}
			elemStr= this.printTab() + elemStr + "\n";
		}
		//wenn Typ file ist, kann das Tag immer geschlossen werden, ist hier nicht sch�n
		else if (((String)node.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_FILE_NODE))
		{
			if ((end!= TAG_ENDED.TRUE))
			{
				try
				{
					elemStr= this.printTab() + "<" + type;
					//Dateinamen hinzuf�gen
					String fileName= (String)node.getValue(KW_FILE_NAME);
					elemStr= elemStr + " " + "name" + "= \"" + fileName + "\"";
					//Pfadnamen hinzuf�gen
					String pathName= (String)node.getValue(KW_PATH_NAME);
					elemStr= elemStr + " " + "path" + "= \"" + pathName + "\"";
				}
				catch (Exception e)
				{ e.printStackTrace(); throw new Exception(ERR_NO_NAME_OR_PATH + node.getName()); }
				elemStr= elemStr + "/>\n";
			}
		}
		return(elemStr);
	}
	
	/**
	 * Kopiert die DTD, auf die die erzeugte XML-Datei matcht in das Zielverzeichniss.
	 */
	private void copyDTD(File src, File dest) throws Exception
	{
		  //static void copy( InputStream in, OutputStream out ) throws IOException 
		 	//static void copyFile( String src, String dest ) 
		FileInputStream  fis = null; 
		FileOutputStream fos = null; 
		
		fis = new FileInputStream(src); 
		fos = new FileOutputStream(dest); 
		 
		byte[] buffer = new byte[ 0xFFFF ]; 
		for ( int len; (len = fis.read(buffer)) != -1; ) 
			fos.write( buffer, 0, len ); 
		
		fis.close();
		fos.close();
	}
	
	/**
	 * Extrahiert einen Namen aus der �bergebenen Datei.
	 * @param fileName String - Dateiname aus der der Name zu abstrahieren ist
	 * @return abstrahierter Name aus dem gegebenen Dateinamen
	 */
	private String extractName(String fileName)
	{
		File file= new File(fileName);
		//schneidet die Andung .xml ab
		String name= file.getName().subSequence(0, file.getName().length()-4).toString();
		return(name);
	}
//	 ============================================== �ffentliche Methoden ==============================================
// --------------------- Methoden vom TraversalObject ---------------------
	public void nodeReached(	Node currNode, 
								Node father, 
								long order) throws Exception 
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "nodeReached()");
		
		//Knoten korpus schreiben
		//Attribute erstellen
		attributes.clear();
		AttValPair attribute = new AttValPair(ATT_KORP_NAME, currNode.getName());
		attributes.add(attribute);
		
		//old this.outStr= this.outStr + printElem(TAG_KORP, attributes, TAG_ENDED.FALSE);
		this.outStr= this.outStr + printElem(currNode, attributes, TAG_ENDED.FALSE);
		
		this.currDepth++;

		if (DEBUG) System.out.println(MSG_END_FCT + "nodeReached()");
	}
	
	public void nodeLeft(	Node currNode, 
							Node father, 
							long order) throws Exception 
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "nodeLeft()");
		
		//wenn Knoten �ber Dateien verf�gt
		if (currNode.hasValue(this.KW_FILE))
		{
			Vector<String> fileNameList= ((Vector<String>) currNode.getValue(KW_FILE));
			AttValPair attribute;
			for(String fileName: fileNameList)
			{
				attributes.clear();
				attribute = new AttValPair(ATT_FILE_NAME, extractName(fileName));
				attributes.add(attribute);
				attribute = new AttValPair(ATT_FILE_PATH, fileName);
				attributes.add(attribute);
				//old this.outStr= this.outStr + printElem(TAG_FILE, attributes, TAG_ENDED.BOTH);
				this.outStr= this.outStr + printElem(currNode, attributes, TAG_ENDED.BOTH);
			}
		}
		this.currDepth--;
		//old this.outStr= this.outStr + printElem(TAG_KORP, null, TAG_ENDED.TRUE);
		this.outStr= this.outStr + printElem(currNode, null, TAG_ENDED.TRUE);
		
		
		if (DEBUG) System.out.println(MSG_END_FCT + "nodeLeft()");
	}
// --------------------- Ende Methoden vom TraversalObject ---------------------

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
	 * Schreibt den �bergebenen Korpus-Baum in die �bergebene Ausgabedatei. Der Korpus
	 * -Baum wird in XML-Notation in die Datei geschrieben und bindet sich an die DTD
	 * korp_struct.dtd.
	 * @param outFile File - Das Dateiobjekt, in das der KOrpusbaum geschrieben werden muss
	 * @param korpTree Graph - der zu traversierende Graph
	 * @param KW_FILE String - Name f�r das Attribut aCon innerhalb eines Knotens im Korpusbaum
	 */
	public void printTree(File outFile, Graph korpTree, String KW_FILE) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "printTree()");
		
		if (outFile== null) throw new Exception(ERR_EMPTY_OUTFILE);
		//if (outFile.exists()) throw new Exception(ERR_FILE_NOT_EXITS + outFile.getCanonicalPath());
		if (korpTree== null) throw new Exception(ERR_KSTREE_NOT_EXISTS);
		if ((KW_FILE== null) || (KW_FILE.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ATT_NAME);
		
		this.KW_FILE= KW_FILE;
		
		//alles initialisieren
		this.outStr= "";
		this.currDepth= 0;
		attributes= new Vector<AttValPair>();
		
		this.outStr= this.outStr + this.printHeader(); 
		korpTree.depthFirst(this);
		
		PrintStream outStream= new PrintStream(outFile);
		outStream.print(this.outStr);
		
		//DTD in das Zielverzeichnis kopieren
		File srcDTDFile= new File(this.getSettingDir()+"/"+PATH_DTD+ FILE_DTD);
		File dstDTDFile= new File(outFile.getParent()+"/"+FILE_DTD);
		this.copyDTD(srcDTDFile, dstDTDFile);
		
		if (DEBUG) System.out.println(MSG_END_FCT + "printTree()");
	}
}
