package relANNIS_2_0.relANNISDAO;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;



/**
 * Liest die Eigenschaften von Relationen aus, dazu gehören Relationsname und Attribute.
 * Diese Eigenschaften werden aus einer Setting-Datei in Form eines XML-Stream ausgelesen. 
 * Die Elemente müssen dabei folgende Form haben:
 * <relation name= "CDATA" file= "CDATA" tupleType= "ID">
 * 		<attribute name= "CDATA" type= "CDATA"/>*
 * </relation>
 * Aus der Beschreibung der DDD-Relationen werden die entsprechenden TupleWriter erzeugt.
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public class RelationSetReader extends DefaultHandler2 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"RelationSetReader";		//Name dieses Tools
	private static final String[] setNames= {"SQL_DDDModel"};	//Namen der Settings, auf die dieser Listener hören soll		 
	
	//Relationsname und Attribute für Element Relation
	private static final String KW_EL_REL=		"relation";			//Elementname für Relationen
	private static final String KW_ATT_ABS_NAME=	"absName";		//Attributname für den abstrakten relationsnamen nach dem DDD-Modell
	private static final String KW_ATT_REL_NAME=	"relName";		//Attributname für den relationsnamen
	private static final String KW_ATT_REL_FILE=	"file";			//Attributname für den relationsnamen
	private static final String KW_ATT_REL_TYPE=	"tupleType";	//Attributname für Relation, das angibt ob ein TupleWriter oder ein IDTupleWriter erzeugtwerden soll
	private static final String KW_ATT_REL_IDVAL=	"idVal";		//Attributname für Relation, welches der Attribute der ID-Wert ist
	private static final String KW_ATT_REL_EXTDIR=	"extDir";		//Attributname für Relation, welches angibt, wo BLOB-Dateien gespeichert werden 
	
	//Relationsname und Attribute für Element Attribute
	private static final String KW_EL_ATT=		"attribute";	//Elementname für Attribute
	private static final String KW_ATT_ATT_NAME=	"name";		//Attributname für den attributnamen
	private static final String KW_ATT_ATT_TYPE=	"type";		//Attributname für den attributtypen
	
	//Relationsname und Attribute für Element Path
	private static final String KW_EL_COLL=		"collection";	//Elementname für Collection
	private static final String KW_ATT_COLL_SRC=	"src";		//Attributname für den Collection Pfadname
	private static final String KW_ATT_COLL_TEMP=	"temp";		//Attributname für die Aangabe, ob die COllection nur temporär ist
	
	private static final String KW_VALUE_IDW=		"ID";		//Wert für IDTupleWriter
	private static final String KW_VALUE_NONIDW=	"nonID";	//Wert für normalen TupleWriter
	private static final String KW_VALUE_BLOB=		"BLOB";		//Wert für BLOB- TupleWriter
	private static final String KW_VALUE_EXTFILE=	"extFile";	//Wert für ExtFile- TupleWriter
	
	private static final String KW_NO=	"no";					//Wert für no
	private static final String KW_YES=	"yes";					//Wert für yes
	
	private File dstFolder= null;
	
	private Hashtable<String, TupleWriter> tWriters= null;	//Liste der zu erzeugenden TupleWriter
	private String currAbsName= "";							//anstarkter Name der aktuellen Relation
	private String currRelName= "";							//Name der aktuellen Relation
	private String currRelFile= "";							//Name der Ausgabedatei
	private String currTupleType= "";						//IDTupleWriter oder TupleWriter
	private String currIDVal= "";							//gibt an, welches Attribut der ID-Wert ist
	private String currPathSrc= "";							//Name des Verzeichnisses, in das die AUsgabedatei geschrieben werden soll						
	private String currExtDirName= "";							//File-Referenz auf das Verzeuchniss, indem BLOB-Dateien gespeichert werden 
	private boolean currIsTemp= false;						//gibt an ob aktuelle Datei nur temporär ist
	
	private Vector<String> currAttNames= null;				//Namen der aktuellen Attribute
	private Vector<String> currAttTypes= null;				//typen der aktuellen Attribute
	private DBConnector dbConnector= null;					//Objekt, dass auf die DB zugreift, wichtig um ID-Werte lesen zu können
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen **************************************
	private static final String ERR_NO_LIST=			MSG_ERR + "The given TupleWriter-list is null.";
	private static final String ERR_LIST_NOT_EMPTY=		MSG_ERR + "The given TupleWriter-list is not empty.";
	private static final String ERR_FORMAT_NO_RELNAME=	MSG_ERR + "There is an Setting-File format error: The name of relation is not given";
	private static final String ERR_FORMAT_ATT=			MSG_ERR + "There is an Setting-File format error: The names and types of attributes aren´t fully given. ";
	private static final String ERR_FORMAT_NO_FILENAME=	MSG_ERR + "There is an Setting-File format error: The name of output file is not given.";
	private static final String ERR_NO_DBCONNECTOR=		MSG_ERR + "The given db connector is empty.";
	private static final String ERR_NO_IDVAL=			MSG_ERR + "The type "+KW_ATT_REL_TYPE+" was set to "+KW_VALUE_IDW+" but no id value is given. Set the Attribut: '"+KW_ATT_REL_IDVAL+"' to thename of id value. Error in relation: ";
	private static final String ERR_EXTDIR=				MSG_ERR + "Cannot create TupleWriter for BLOB, because extDir is wrong: ";
	private static final String ERR_NO_EXTDIR=			MSG_ERR + "Cannot create TupleWriter for ExtFile, because extDir is wrong:";
	private static final String ERR_NO_DST_FOLDER=		MSG_ERR + "Cannot create  RelationSetReader, because the dstFolder is empty.";
//	 ============================================== statische Methoden ==============================================
	
	/**
	 * Gibt alle SettingNamen zurück, auf die dieser SettingListener hört.
	 * @return Vector<String> - Namen der Settings, auf die dieser Listener hört
	 */
	public static Vector<String> getSetNames()
	{
		Vector<String> setNames= new Vector<String>();
		for(String name : RelationSetReader.setNames)
			setNames.add(name);
		return(setNames);
	}
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein KGWriterObjekt und nimmt eine leere, aber initialisierte Liste von 
	 * TupleWriter - Objekten an. Dises werden beim parsen der Setting-Datei erzeugt und an 
	 * das übergebene Objekt angehangen.
	 * @param tWriters  Vector<TupleWriter> - leere aber initialisierte Liste für TWriter
	 * @param dstFolder File  Output-Verzeichniss
	 * @param dbConnector DBConnector - EIn Objekt vom Typ DBConnector, dieses wird benötigt um die initialen ID-Werte der Relationen auszulesen
	 * @exception Fehler, wenn Liste nicht leer oder nicht initialisiert
	 */
	public RelationSetReader(	Hashtable<String, TupleWriter> tWriters, 
								File dstFolder,
								DBConnector dbConnector) throws Exception
	{
		if (tWriters== null) throw new Exception(ERR_NO_LIST);
		if (!tWriters.isEmpty()) throw new Exception(ERR_LIST_NOT_EMPTY);
		this.tWriters= tWriters;
		
		if(dstFolder== null) throw new Exception(ERR_NO_DST_FOLDER);
			this.dstFolder= dstFolder;
		
		if (dbConnector == null) throw new Exception(ERR_NO_DBCONNECTOR);
		this.dbConnector= dbConnector;
		
		//currAttNames initialisieren
		this.currAttNames= new Vector<String>();
		//currAttTypes initialisieren
		this.currAttTypes= new Vector<String>();
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
//	 ---------------------------------- SAX Methoden ----------------------------------
	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */ 
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		try
		{
			String absName= "";			//Wert des Attribut absNname
			String relName= "";			//Wert des Attribut relName
			String relFile= "";			//Wert des Attribut fileName
			String tupleType= "";		//gibt an welcher Writer genutzt werden soll
			String idVal= "";			//gibt an welcher Wert der ID-Wert ist
			String attName= "";			//Wert des Attribut name
			String type= "";			//Wert des Attribut type
			String pathSrc= "";			//Wert des Attribut Path.src
			boolean pathTemp= false;		//Wert des Attribut Path.temp
			
			//gehe durch alle Attribute		
			for (int i= 0; i < attributes.getLength(); i++)
			{
				//Attribut ist Relation.absName
				if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_ABS_NAME)) absName= attributes.getValue(i).toUpperCase();
				//Attribut ist Relation.relName
				if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_REL_NAME)) relName= attributes.getValue(i);
				//Attribut ist Relation.file
				if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_REL_FILE)) relFile= attributes.getValue(i);
				//Attribut ist Relation.extDir
				else if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_REL_EXTDIR)) 
					this.currExtDirName= attributes.getValue(i);
				//gibt an welcher Writer genutzt werden soll
				else if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_REL_TYPE)) tupleType= attributes.getValue(i);
				//gibt an welches Attribut der IDWert ist
				else if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_REL_IDVAL)) idVal= attributes.getValue(i);
				//Attribut ist Attribut.name
				else if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_ATT_NAME)) attName= attributes.getValue(i);
				//Attribut ist Attribut.type
				else if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_ATT_TYPE)) type= attributes.getValue(i);
				//Attribut ist Path.src		
				else if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_COLL_SRC)) pathSrc= attributes.getValue(i);
				//Attribut ist Path.temp		
				else if (attributes.getQName(i).equalsIgnoreCase(KW_ATT_COLL_TEMP)) 
				{
					if (attributes.getValue(i).equalsIgnoreCase(KW_NO)) pathTemp= false;
					else if (attributes.getValue(i).equalsIgnoreCase(KW_YES)) pathTemp= true;
				}
			}
			//Elementname ist Relation
			if (qName.equalsIgnoreCase(KW_EL_REL))
			{
				//Fehler wenn tupleType= ID und idVal nicht gesetzt
				if ((tupleType.equalsIgnoreCase(KW_VALUE_IDW)) && (idVal.equalsIgnoreCase("")))
					throw new Exception(ERR_NO_IDVAL + relName);
				this.currAbsName= absName;
				this.currRelName= relName;
				this.currRelFile= relFile;
				this.currTupleType= tupleType;
				this.currIDVal= idVal;
			}
			//ElementName ist Attribute
			else if (qName.equalsIgnoreCase(KW_EL_ATT))
			{
				this.currAttNames.add(attName);
				this.currAttTypes.add(type);
			}
			//Element ist Collection
			else if (qName.equalsIgnoreCase(KW_EL_COLL))
			{
				this.currPathSrc= pathSrc;
				this.currIsTemp= pathTemp;
			}
		}
		catch (Exception e)
			{throw new SAXException(e.getMessage());}
	}
	
	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException 
	{
		//TupleWriter erzeugen, wenn Element eine Relation ist
		//Elementname ist Relation
		if (qName.equalsIgnoreCase(KW_EL_REL))
		{
			//prüfe ob relName, attNames und attTypes gegeben sind
			if (this.currRelName.equalsIgnoreCase("")) throw new SAXException(ERR_FORMAT_NO_RELNAME);
			if (this.currRelFile.equalsIgnoreCase("")) throw new SAXException(ERR_FORMAT_NO_FILENAME);
			if ((this.currAttNames.isEmpty()) || (this.currAttNames.size() != this.currAttTypes.size())) throw new SAXException(ERR_FORMAT_ATT);
			try
			{
				TupleWriter tWriter;
				//TupleWriter für einfache Relationen erzeugen
				if (this.currTupleType.equalsIgnoreCase(KW_VALUE_NONIDW))
				{
					File outFile= new File(this.currPathSrc +"/"+ this.currRelFile);
					tWriter= new TupleWriter(this.currAbsName, this.currRelName, outFile, true, this.currIsTemp);
					//tWriter= new TupleWriter(this.currAbsName, this.currRelName, this.currPathSrc, this.currRelFile, true, this.currIsTemp);
					tWriter.setAttNames(this.currAttNames);
					tWriters.put(this.currAbsName, tWriter);
				}
				//IDTupleWriter für Relationen, deren ID autom. gesetzt werden soll erzeugen
				else if (this.currTupleType.equalsIgnoreCase(KW_VALUE_IDW))
				{
					long idVal= this.dbConnector.getNewIDFromRel(this.currRelName, this.currIDVal);
					tWriter= new IDTupleWriter(this.currAbsName, this.currRelName, this.currPathSrc, this.currRelFile, true, idVal, this.currIsTemp);
					tWriter.setAttNames(this.currAttNames);
					tWriters.put(this.currAbsName, tWriter);
				}
				//BLOBTupleWriter für Relationen, die BLOB-ID´s enthalten und deren ID autom. gesetzt werden soll erzeugen
				else if (this.currTupleType.equalsIgnoreCase(KW_VALUE_BLOB))
				{
					long idVal= this.dbConnector.getNewIDFromRel(this.currRelName, this.currIDVal);
					File outFile= new File(this.currPathSrc +"/"+ this.currRelFile);
					//wenn ein Pfad für die externen Dateien angegeben wurde
					if ((this.currExtDirName!= null) || (!this.currExtDirName.equalsIgnoreCase("")))
					{
						try
						{
							File extDir= new File(this.currPathSrc+ "/"+this.currExtDirName);
							if (extDir.exists())
								extDir.mkdir();
							tWriter= new BLOBTupleWriter(this.currAbsName, this.currRelName, outFile, this.dstFolder, true, idVal, this.currIsTemp);
						}
						catch (Exception e)
						{throw new Exception(ERR_EXTDIR);}
					}
					else
					{
						tWriter= new BLOBTupleWriter(this.currAbsName, this.currRelName, outFile, true, idVal, this.currIsTemp);
						
					}	
					tWriter.setAttNames(this.currAttNames);
					tWriters.put(this.currAbsName, tWriter);
				}
				//ExtFileTupleWriter für Relationen, die Verweise auf externe Dateien enthalten
				else if (this.currTupleType.equalsIgnoreCase(KW_VALUE_EXTFILE))
				{
					File outFile= new File(this.currPathSrc +"/"+ this.currRelFile);
					//wenn ein Pfad für die externen Dateien angegeben wurde
					if ((this.currExtDirName!= null) || (!this.currExtDirName.equalsIgnoreCase("")))
					{
						try
						{
							//File extDir= new File(this.currPathSrc+ "/"+this.currExtDirName);
							File extDir= new File(this.dstFolder + "/ExtData");
							if (extDir.exists())
								extDir.mkdir();
							tWriter= new ExtFileTupleWriter(this.currRelName, outFile, extDir, true, this.currIsTemp);
						}
						catch (Exception e)
						{throw new Exception(ERR_EXTDIR);}
					}
					else
					{
						throw new Exception(ERR_NO_EXTDIR);
						
					}	
					tWriter.setAttNames(this.currAttNames);
					tWriters.put(this.currAbsName, tWriter);
				}
			}
			catch (Exception e)
				{e.printStackTrace();
				throw new SAXException(e.getMessage());}
			//globale Attribute zu einer Relation zurücksetzen
			this.currRelFile= "";
			this.currRelName= "";
			this.currAttNames.clear();
			this.currAttTypes.clear();
		}
	}
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "name:\t" + TOOLNAME;
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	


}
