package importer.paula.paula10.analyzer.paulaAnalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.util.Vector;

import importer.paula.paula10.analyzer.util.depGraph.Graph;
import importer.paula.paula10.analyzer.util.depGraph.Node;
import importer.paula.paula10.analyzer.util.depGraph.TraversalObject;

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
	private static final String FILE_DTD= "typed_corp.dtd";		//Name der DTD, auf die die hier erzeugte XML-Datei matscht
	private static final String PATH_DTD= "/paula10/";		//Pfad zur DTD
	
	//Keywords um einen Knoten zu bestimmen
	private static final String KW_NODE_TYPE=		"NODE_TYPE";
	private static final String KW_CORP_NODE=		"CORP";
	private static final String KW_CORP_NAME=		"CORP_NAME";
	private static final String KW_DOC_NODE=		"DOC";
	private static final String KW_DOC_NAME=		"DOC_NAME";
	
	private enum TAG_ENDED {TRUE, FALSE, BOTH};
	
	//alles zum Tag Korpus
	private static final String TAG_CORP= "corpus";		
	private static final String ATT_CORP_NAME= "name";

	//alles zum Tag Dokument
	private static final String TAG_DOC= "document";
	private static final String ATT_DOC_NAME= "name";
	
	//alles zum Tag Meta
	private static final String TAG_META= "meta";	
	
	//alles zum Tag Anno
	private static final String TAG_ANNO= "anno";
	
	//alles zum Tag file
	private static final String TAG_FILE= "file";		
	private static final String ATT_FILE_NAME= "name";
	private static final String ATT_FILE_PATH= "path";
	private static final String ATT_FILE_TYPE= "analyze_type";
	private static final String ATT_FILE_IMPORT= "import";
	private static final String ATT_FILE_DTD=	"dtd";
	private static final String ATT_VAL_IMPORT_YES= "yes";
	private static final String ATT_VAL_IMPORT_NO= "no";
	
	private String outStr= null;			//String, der die Ausgabedatei zwischenspeichert
	Vector<AttValPair> attributes= null;
	private int currDepth= 0;				//gibt die aktuelle Tiefe im Baum an
	private String KW_ACON_LIST=	"";					//Name f�r das Attribut aCon
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START_FCT=		MSG_STD + "start method: ";
	private static final String MSG_END_FCT=		MSG_STD + "end method: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_OUTFILE=		MSG_ERR + "The given output-xml-file is empty.";
	private static final String ERR_FILE_NOT_EXITS=		MSG_ERR + "The given output-xml-file does not exist: ";
	private static final String ERR_KSTREE_NOT_EXISTS=	MSG_ERR + "The given korpus-structure-tree does not exists.";
	private static final String ERR_WRONG_ABSTYPE=		MSG_ERR + "This object cannot print analyze-container-objects of abstract type: ";
//	 ============================================== Konstruktoren ==============================================
//	 ============================================== private Methoden ==============================================
	/**
	 * Gibt einen String zur�ck, der aus dem �bergebenen String einen XML-konformen String
	 * macht.
	 * @param comment String - in einen XML-Kommentarknoten zu packender String
	 * @return XML-Kommentar aus dem �bergebenen String
	 */
	private String printComment(String comment)
	{
		String retStr= "";
		
		if ((comment != null) && (!comment.equalsIgnoreCase("")))
		{
			retStr= this.printTab() + "<!-- " + comment + " -->\n";
		}
		return(retStr);
	}
	
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
	 * @param name String - Name des Elementknotens
	 * @param attributes - Attribut-Wert-Paar der Attributknoten
	 * @param end boolean - gibt an, ob das Element ge�ffnet oder geschlossen wird
	 * @return Elementknoten mit Attributknoten als String
	 */
	private String printElem(String name, Vector<AttValPair> attributes, TAG_ENDED end)
	{
		String elemStr= "";
		
		if ((end== TAG_ENDED.FALSE) || (end== TAG_ENDED.BOTH))
		{
			elemStr= "<" + name;
			if (attributes != null)
				for(AttValPair att : attributes)
					elemStr= elemStr + " " + att.name + "= \"" + att.value + "\"";
			if (end== TAG_ENDED.FALSE)	
				elemStr= elemStr + ">";
			else elemStr= elemStr + "/>";
		}
		else
		{
			elemStr= "</" + name + ">";
		}
		
		elemStr= this.printTab() + elemStr + "\n";
		
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
		
		
		fis = new FileInputStream(src ); 
		fos = new FileOutputStream(dest); 
		 
		byte[] buffer = new byte[ 0xFFFF ]; 
		for ( int len; (len = fis.read(buffer)) != -1; ) 
			fos.write( buffer, 0, len ); 
		
		fis.close();
		fos.close();
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
		AttValPair attribute= null;
		//wenn Knoten ein Korpusknoten ist
		if (((String)currNode.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_CORP_NODE))
		{
			attribute= new AttValPair(ATT_CORP_NAME, (String)currNode.getValue(KW_CORP_NAME));
		}
		//wenn Knoten ein Dokumentknoten ist
		else if (((String)currNode.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_DOC_NODE))
			attribute= new AttValPair(ATT_DOC_NAME, (String)currNode.getValue(KW_DOC_NAME));
		attributes.add(attribute);
		
		//wenn Knoten ein Korpusknoten ist
		if (((String)currNode.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_CORP_NODE))
			this.outStr= this.outStr + printElem(TAG_CORP, attributes, TAG_ENDED.FALSE);
		//wenn Knoten ein Dokumentknoten ist
		else if (((String)currNode.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_DOC_NODE))
			this.outStr= this.outStr + printElem(TAG_DOC, attributes, TAG_ENDED.FALSE);
		this.currDepth++;

		if (DEBUG) System.out.println(MSG_END_FCT + "nodeReached()");
	}
	
	public void nodeLeft(	Node currNode, 
							Node father, 
							long order) throws Exception 
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "nodeLeft()");
		
		//Knoten file schreiben wenn es ACONs gibt
		if (currNode.hasValue(this.KW_ACON_LIST))
		{
			Vector<AnalyzeContainer> aConList= ((Vector<AnalyzeContainer>)currNode.getValue(this.KW_ACON_LIST));
			if ((aConList!= null) && (!aConList.isEmpty()))
			{
				AttValPair attribute= null;
				String metaStructPart= "";
				String metaPart= "";
				String annoPart= "";
				for (AnalyzeContainer aCon: aConList)
				{
					attributes.clear();
					//Name der PAULA-Datei
					attribute = new AttValPair(ATT_FILE_NAME, aCon.getPAULAID());
					attributes.add(attribute);
					//Annotationstyp der PAULA-Datei
					attribute = new AttValPair(ATT_FILE_TYPE, aCon.getAnaType());
					attributes.add(attribute);
					//Flag, ob Datei importiert werden soll
					if (aCon.getToImport()) attribute = new AttValPair(ATT_FILE_IMPORT, ATT_VAL_IMPORT_YES);
					else attribute = new AttValPair(ATT_FILE_IMPORT, ATT_VAL_IMPORT_NO);
					attributes.add(attribute);
					//Pfad der PAULA-Datei
					attribute = new AttValPair(ATT_FILE_PATH, aCon.getPAULAFile().getCanonicalPath());
					attributes.add(attribute);
					//DTD der PAULA-Datei
					attribute = new AttValPair(ATT_FILE_DTD, aCon.getDTD().getCanonicalPath());
					attributes.add(attribute);
					//Meta_Struct_Data
					if (aCon.getAbsAnaType()== AnalyzeContainer.ABS_ANA_TYPE.META_STRUCT_DATA)
					{
						this.currDepth++;
						metaStructPart= metaStructPart + this.printComment(aCon.getComment());
						metaStructPart= metaStructPart + this.printElem(TAG_FILE, attributes, TAG_ENDED.BOTH);
						this.currDepth--;
					}
					//Meta_Data
					else if (aCon.getAbsAnaType()== AnalyzeContainer.ABS_ANA_TYPE.META_DATA)
					{
						this.currDepth++;
						metaPart= metaPart + this.printComment(aCon.getComment());
						metaPart= metaPart + this.printElem(TAG_FILE, attributes, TAG_ENDED.BOTH);
						this.currDepth--;
					}
					//Anno_Data
					else if (aCon.getAbsAnaType()== AnalyzeContainer.ABS_ANA_TYPE.ANNO_DATA)
					{
						this.currDepth++;
						annoPart= annoPart + this.printComment(aCon.getComment());
						annoPart= annoPart + this.printElem(TAG_FILE, attributes, TAG_ENDED.BOTH);
						this.currDepth--;
					}
					else 
					{
						throw new Exception(ERR_WRONG_ABSTYPE + aCon.getAbsAnaType());
					}
				}
				//KorpusInhalt schreiben
				//wenn es MetaStructPart gibt
				if ((!metaStructPart.equalsIgnoreCase("")) || (!metaPart.equalsIgnoreCase(""))) 
				{
					this.outStr= this.outStr + this.printElem(TAG_META, null, TAG_ENDED.FALSE);
					this.outStr= this.outStr + metaStructPart + metaPart;  
					this.outStr= this.outStr + this.printElem(TAG_META, null, TAG_ENDED.TRUE);
				}
				if (!annoPart.equalsIgnoreCase("")) 
				{
					this.outStr= this.outStr + this.printElem(TAG_ANNO, null, TAG_ENDED.FALSE);
					this.outStr= this.outStr + annoPart;  
					this.outStr= this.outStr + this.printElem(TAG_ANNO, null, TAG_ENDED.TRUE);
				}
			}
		}
		
		this.currDepth--;
		
		//wenn Knoten ein Korpusknoten ist
		if (((String)currNode.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_CORP_NODE))
			this.outStr= this.outStr + printElem(TAG_CORP, null, TAG_ENDED.TRUE);
		//wenn Knoten ein Dokumentknoten ist
		else if (((String)currNode.getValue(KW_NODE_TYPE)).equalsIgnoreCase(KW_DOC_NODE))
			this.outStr= this.outStr + printElem(TAG_DOC, null, TAG_ENDED.TRUE);
		
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
	 * Schreibt den �bergebenen korpus-structure-tree in die �bergebene Datei als XML.
	 * @param outFile File - Datei in die der Baum geschrieben werden soll
	 * @param ksTree Graph - korpus-structure-tree, Baum der in die Zieldatei geschrieben werden soll
	 * @param KW_ACON_LIST String - Name des Attributes, hinterdem sich die ACON-Liste verbirgt 
	 */
	public void printTree(File outFile, Graph ksTree, String KW_ACON_LIST) throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "printTree()");
		
		if (outFile== null) throw new Exception(ERR_EMPTY_OUTFILE);
		//if (outFile.exists()) throw new Exception(ERR_FILE_NOT_EXITS + outFile.getCanonicalPath());
		if (ksTree== null) throw new Exception(ERR_KSTREE_NOT_EXISTS);
		
		//alles initialisieren
		this.outStr= "";
		this.currDepth= 0;
		attributes= new Vector<AttValPair>();
		this.KW_ACON_LIST= KW_ACON_LIST;
		
		this.outStr= this.outStr + this.printHeader(); 
		ksTree.depthFirst(this);
		
		PrintStream outStream= new PrintStream(outFile);
		outStream.print(this.outStr);
		
		//DTD in das Zielverzeichnis kopieren
		File srcDTDFile= new File(this.getSettingDir()+PATH_DTD+ FILE_DTD);
		File dstDTDFile= new File(outFile.getParent()+"/"+FILE_DTD);
		this.copyDTD(srcDTDFile, dstDTDFile);
		
		if (DEBUG) System.out.println(MSG_END_FCT + "printTree()");
	}
}
