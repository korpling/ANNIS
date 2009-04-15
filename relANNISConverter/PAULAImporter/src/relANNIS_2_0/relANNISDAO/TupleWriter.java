package relANNIS_2_0.relANNISDAO;

import java.io.File;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;


/**
 * Diese Klasse schreibt Tupel in eine  übergebene Datei. Diese Datei stellt dann eine 
 * Tabellendatei dar, in der die Tupelattributwerte durch einen übergebenen Seperator oder
 * den Standardseperator "tabulator" getrennt werden.<br/> 
 * Ein Tupel ist dabei ein String-Vector. Dem TupelWriter können auch die Attributnamen übergeben
 * werden, dann kann er ein oracle log file erstellen.<br/>
 * Es wird nicht sofort auf den Stream geschrieben. Erst mit flush() kann dies sichergestellt werden. 
 * @author Flo
 * @version 1.0
 */
public class TupleWriter 
{
//	 ============================================== statische Dinge ==============================================
	protected static long numOfObj= 0;				//Anzahl der bereits erzeugten Objekte
	
	/**
	 * seperator for file system
	 */
	protected static String KW_PATH_SEP=	"/";
	/**
	 * Zuordnungstabelle für die TupleWriter und den Pfad, wenn kein Objekt mehr für einen Pfad angemeldet ist, wird dieser gelöscht.
	 */
	protected static Hashtable<String, Vector<TupleWriter>> pathWriterTable= null;		
	
	/**
	 * Setzt den übergebenen TupleWriter in die Zuordnungstabelle. Damit hat dieses Objekt 
	 * Einfluss auf die Lebenszeit des Verzeichnisses, wird es gelöscht und es exitieren 
	 * keine weiteren, so wird auch der Pfad gelöscht. 
	 * @param tWriter TupleWriter - einzutragender TupleWriter
	 * @param path String - Pfad, auf dessen Lebenszeit der tWriter EInfluss hat
	 */
	protected static void setPathWriterEntry(TupleWriter tWriter, String path)
	{
		//Liste initialisieren, wenn noch nicht geshcehen
		if (pathWriterTable == null) pathWriterTable= new Hashtable<String, Vector<TupleWriter>>();
		//path existiert noch nicht in Liste
		if (!pathWriterTable.containsKey(path))
		{
			Vector<TupleWriter> writerList= new Vector<TupleWriter>();
			writerList.add(tWriter);
			pathWriterTable.put(path, writerList);
		}
		//path existiert schon in Liste
		else
		{
			pathWriterTable.get(path).add(tWriter);
		}
	}
	
	/**
	 * Löscht den übergebenen TupleWriter aus der Zuordnungstabelle. Damit hat dieses 
	 * Objekt keinen Einfluss mehr auf die Lebenszeit des Verzeichnisses, wird es gelöscht 
	 * und es exitieren keine weiteren, so wird auch der Pfad gelöscht. 
	 * @param tWriter TupleWriter - einzutragender TupleWriter
	 * @param path String - Pfad, auf dessen Lebenszeit der tWriter Einfluss hat
	 */
	protected static void delPathWriterEntry(TupleWriter tWriter, String path)
	{
		//Liste enthält einen Eintrag zu diesem tWriter
		Vector<TupleWriter> writerList= pathWriterTable.get(path);
		if (writerList != null)
		{
			//TupleWriter aus Liste und dessen Datei löschen
			if (writerList.contains(tWriter)) 
			{
				writerList.remove(tWriter);
				File file= new File(tWriter.getPath() + KW_PATH_SEP + tWriter.getFileName());
				file.delete();
			}
			//wenn List leer, dann Pfad löschen
			if (writerList.isEmpty())
			{
				//Eintrag aus Zuordnungstabelle löschen
				pathWriterTable.remove(path);
				//Pfad löschen
				File realPath= new File(tWriter.getPath());
				realPath.delete();
			}
		}
	}
	
	/**
	 * Gibt die Anzahl der bereits erzeugten TupleWriter zurück
	 * @return Anzahl der TupleWriter-Objekte
	 */
	public static long getNumOfObjects()
		{ return(numOfObj);	}
//	 ============================================== private Variablen ==============================================
	private static String TOOLNAME= 	"TupleWriter";		//Name dieses Tools
	private static final String STD_SEP=	"\t";			//Standardseperator
	private static final int TRESHOLD=	1000;				//Wert bei dem unbedingt geflusht werden soll
	private static final String STD_ENC= "UTF-8";			//Standardkodierung, mit der auf den Stream geschrieben werden soll
	
	
	private long objId= 0;							//Objektnr. dieses Objektes
	private String sep= STD_SEP;					//Seperator zur Attributwert-Trennung
	protected boolean append= false;				//gibt an, ob Datei neu geschrieben oder an sie angehängt wird (true= es wird angehängt)
	protected long numOfTuples= 0;					//Anzahl der bisher geschriebenen Tupel
	protected String pathName= "";					//Name des Pfades der Collection;
	protected boolean isTemp= false;					//gibt an, ob die erzeugte Datei temporär ist
	
	protected PrintStream oStream= null;				//Ausgabestream
	
	private String relName= "";						//Name der Relation
	private String absName= null;						//abstrakterName der Relation
	
	private File outFile= null;						//Name der Ausgabedatei
	
	protected Logger logger= null;					//Logger für log4j
	
	private String[] attNames= null;				//Attributnamen
	private boolean writeOne= false;				//gibt an ob schon ein Tupel übergeben wurde
	protected Vector<String> tuples= null;			//Liste aller Tupel
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_FLUSH=			MSG_STD + "flushing to file: ";
	private static final String MSG_DELETE=			MSG_STD + "deleting file: ";
	private static final String MSG_NOT_SET=		MSG_STD + "This value isn´t set";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_SEP=			MSG_ERR + "An empty seperator was given.";
	private static final String ERR_EMPTY_FILENAME=		MSG_ERR + "The given filename is empty.";
	private static final String ERR_EMPTY_PATHNAME=		MSG_ERR + "The given pathname is empty.";
	private static final String ERR_EMPTY_ATTNAMES=		MSG_ERR + "The attribute name list is empty.";
	private static final String ERR_EMPTY_ATTNAME=		MSG_ERR + "There is an empty attribute name.";
	private static final String ERR_GET_TUPLE=			MSG_ERR + "Cannot change settings, there is already one tuple.";
	private static final String ERR_LENGTH_MATCH=		MSG_ERR + "The length of the attribute values and the attribute names does not match.";
	private static final String ERR_NOT_IMPLEMENTED=	MSG_ERR + "This method isn´t implemented yet:\t";
	private static final String ERR_NO_NAME=			MSG_ERR + "The name of relation wasn´t given.";
	private static final String ERR_NOT_A_DIR=			MSG_ERR + "The given pathname is not a name for a directory: ";
	private static final String ERR_NOT_A_FILE=			MSG_ERR + "The given filename is not a name for a file: ";
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Initialisiert ein TupleWriter-Objekt und setzt die Ausgabetabellendatei. Setzt die
	 * Kodierung mit der auf den Stream geschrieben werden soll auf UTF-8. Für andere 
	 * Kodierungen muss ein anderer Konstruktor genutzt werden.
	 * @param absName String - abstrakter Name der Rellation nach dem DDD-Modell
	 * @param relName String - Name der relation, in die die Tupel kommen
	 * @param outFileName String - Name der Ausgabedatei erlaubt nur [a-zA-Z0-9][.][a-zA-Z0-9]+
	 * @param pathName String - Name des Verzeichnisses in das die Datei geschrieben werden soll
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist
	 * 
	 * @deprecated
	 */
	public TupleWriter(	String absName,
						String relName, 
						String pathName, 
						String outFileName, 
						boolean override,
						boolean isTemp) throws Exception
	{
		this.init(relName, pathName, outFileName, STD_ENC, override, isTemp);
	}
	
	/**
	 * Initialisiert ein TupleWriter-Objekt und setzt die Ausgabetabellendatei. Setzt die
	 * Kodierung mit der auf den Stream geschrieben werden soll die übergebene.
	 * @param absName String - abstrakter Name der Rellation nach dem DDD-Modell
	 * @param relName String - Name der relation, in die die Tupel kommen
	 * @param outFileName String - Name der Ausgabedatei erlaubt nur [a-zA-Z0-9][.][a-zA-Z0-9]+
	 * @param pathName String - Name des Verzeichnisses in das die Datei geschrieben werden soll
	 * @param encoding String - Die Kodierung in der die Tupel auf den Stream geschrieben werden sollen (bspw. UTF-8)
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist
	 * @deprecated 
	 */
	public TupleWriter(	String absName,
						String relName, 
						String pathName, 
						String outFileName, 
						String encoding,
						boolean override,
						boolean isTemp) throws Exception
	{
		this.init(relName, pathName, outFileName, encoding, override, isTemp);
	}
	
	/**
	 * Initialisiert ein TupleWriter-Objekt und setzt die Ausgabetabellendatei. Setzt die
	 * Kodierung mit der auf den Stream geschrieben werden soll auf UTF-8. Für andere 
	 * Kodierungen muss ein anderer Konstruktor genutzt werden.
	 * @param absName String - abstrakter Name der Relation nach dem DDD-Modell
	 * @param relName String - Name der Relation, in die die Tupel kommen
	 * @param outFile File - Referenz auf die Ausgabedatei
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist
	 */
	public TupleWriter(	String absName,
						String relName, 
						File outFile, 
						boolean override,
						boolean isTemp) throws Exception
	{
		this.init(relName, outFile, STD_ENC, override, isTemp);
		this.absName= absName;
	}
	
	/**
	 * Initialisiert ein TupleWriter-Objekt und setzt die Ausgabetabellendatei. Setzt die
	 * Kodierung mit der auf den Stream geschrieben werden soll auf UTF-8. Für andere 
	 * Kodierungen muss ein anderer Konstruktor genutzt werden.
	 * @param relName String - Name der Relation, in die die Tupel kommen
	 * @param outFile File - Referenz auf die Ausgabedatei
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist
	 */
	public TupleWriter(	String relName, 
						File outFile, 
						boolean override,
						boolean isTemp) throws Exception
	{
		this.init(relName, outFile, STD_ENC, override, isTemp);
	}
	
	/**
	 * Initialisiert ein TupleWriter-Objekt und setzt die Ausgabetabellendatei. Setzt die
	 * Kodierung mit der auf den Stream geschrieben werden soll die übergebene.
	 * @param absName String - abstrakter Name der Rellation nach dem DDD-Modell
	 * @param relName String - Name der relation, in die die Tupel kommen
	 * @param outFile File - Referenz auf die Ausgabedatei
	 * @param encoding String - Die Kodierung in der die Tupel auf den Stream geschrieben werden sollen (bspw. UTF-8)
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist
	 */
	public TupleWriter(	String absName,
						String relName, 
						File outFile,
						String encoding,
						boolean override,
						boolean isTemp) throws Exception
	{
		this.init(relName, outFile, encoding, override, isTemp);
		this.absName= absName;
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Initialisiert ein Objekt vom Typ TupleWriter.
	 * Dabei werden die Eingabeparameter gebprüft und gesetzt bzw. umgesetzt.
	 * @param relName String - Name der relation, in die die Tupel kommen
	 * @param outFile File - Referenz auf die Ausgabedatei
	 * @param encoding String - Die Kodierung in der die Tupel auf den STream geschrieben werden sollen (bspw. UTF-8)
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist 
	 */
	private void init(	String relName, 
						File outFile,
						String encoding,
						boolean override,
						boolean isTemp) throws Exception
	{
		this.init(relName, outFile.getParent(), outFile.getName(), encoding, override, isTemp);
	}
	/**
	 * Initialisiert ein Objekt vom Typ TupleWriter.
	 * Dabei werden die Eingabeparameter gebprüft und gesetzt bzw. umgesetzt.
	 * @param relName String - Name der relation, in die die Tupel kommen
	 * @param outFileName String - Name der Ausgabedatei erlaubt nur [a-zA-Z0-9][.][a-zA-Z0-9]+
	 * @param pathName String - Name des Verzeichnisses in das die Datei geschrieben werden soll
	 * @param encoding String - Die Kodierung in der die Tupel auf den STream geschrieben werden sollen (bspw. UTF-8)
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist 
	 */
	private void init(	String relName, 
						String pathName, 
						String outFileName,
						String encoding,
						boolean override,
						boolean isTemp) throws Exception
	{
		//Prüfe ob Verzeichnissname gesetzt und in Ordnung ist
		if ((pathName== null) || (pathName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_PATHNAME);
		File path= new File(pathName);
		//wenn Verzeichniss nicht vorhanden, dann erstellen
		if (!path.exists()) path.mkdir();
		//Prüfen ob pathName Verzeichnis ist
		if (!path.isDirectory()) throw new Exception(ERR_NOT_A_DIR + pathName);
		this.pathName= pathName;
		
		//Prüfe ob Eingabedateiname gesetzt ist
		if ((outFileName== null) || (outFileName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_FILENAME);
		this.outFile= new File(path.getAbsolutePath() + "/" + outFileName);
		//wenn Datei noch nicht existiert, dann neu erzeugen
		if (!outFile.exists()) this.outFile.createNewFile();
		//Prüfen ob Dateiname ok ist
		if (!this.outFile.isFile()) throw new Exception(ERR_NOT_A_FILE + outFileName);
		
		//Prüfe ob Eingabedatei existiert
		if ((outFile.exists()) && (!override)) 
		{
			this.append= true;
			throw new Exception(ERR_NOT_IMPLEMENTED + "TupleWriter()");
		}
		//setzen ob Datei temporär ist
		this.isTemp= isTemp;
		//prüfe ob Relname ok
		if ((relName == null) || (relName.equalsIgnoreCase(""))) throw new Exception(ERR_NO_NAME);
		this.relName= relName;
		//neue TupelListe erzeugen
		this.tuples = new Vector<String>();
		//erzeuge einen OutputStream
		oStream = new PrintStream(outFile, encoding);
		//erhöhe Anzahl der Objekte
		numOfObj++;
		//ObjektId dieses Objektes setzen
		this.objId= numOfObj;
		//Dieses Objekt in die Zuordnungstabelle eintragen 
		if (this.isTemp) TupleWriter.setPathWriterEntry(this, pathName);
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Gibt den abstrakten Relationsnamen dieses TupleWriters zurück.
	 * @return abstrakter Name des TupleWriters
	 */
	public String getAbsName()
	{ 
		if ((this.absName== null) || (this.absName.equalsIgnoreCase("")))
			return(MSG_NOT_SET);
		else return(this.absName); 
	}
	
	/**
	 * Gibt den Relationsnamen dieses TupleWriters zurück.
	 * @return Name des TupleWriters
	 */
	public String getRelName()
		{ return(this.relName); }
	
	/**
	 * Gibt den Namen der Datei zurück, in die dieses Objekt die Tupel schreibt.
	 * @return Name der Ausgabedatei
	 */
	public String getFileName()
		{ return(this.outFile.getName()); }
	
	/**
	 * Gibt das Verzeichniss zurück, in das die Ausgabedatei geschrieben wird.
	 * @return Verzeichniss der Ausgabedatei
	 */
	public String getPath()
		{ return(this.outFile.getParent()); }
	
	/**
	 * Gibt die Anzahl der an den TupleWriter übergebenen Tupel zurück.
	 * @return Anzahl der übergebenen Tupel
	 */
	public long getNumOfTuples()
		{ return(this.numOfTuples); }
	
	/**
	 * Gibt eine Liste mit den Attributnamen zurück. Wurden diese nicht gesetzt, so 
	 * ist die Liste leer. 
	 * @return Liste der Attributnamen
	 */
	public Vector<String> getAttNames() throws Exception
	{
		if ((this.attNames== null) || (this.attNames.length== 0))
			return(null);
		Vector<String> newAttNames= new Vector<String>();
		for (String attName: this.attNames)
			newAttNames.add(attName);
		return(newAttNames);
	}
	
	/**
	 * Nimmt den übergebenen seperator als Seperator zur Trennung dre Attributwerte. Kann nicht mehr aufgerufen werden, wenn bereits printTuple()
	 * aufgerufen wurde.
	 * @param seperator String - Seperator
	 * @exception Fehler, wenn seperator null oder leer
	 */
	public void setSeperator(String seperator) throws Exception
	{
		//	Fehler wenn bereits ein Tupel übergeben
		if (this.writeOne) throw new Exception(ERR_GET_TUPLE);
		//Fehler wenn sep null ist
		if ((seperator== null) || (seperator.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_SEP);
		this.sep= seperator;
	}
	
	/**
	 * Mit dieser Methode können die Attributnamen übergeben werden. Dies muss geschehen, 
	 * um die oracle-log Datei zu erzeugen. Außerdem kann anhand der Anzahl der übergebenen
	 * Attributnamen bei jedem printTuple() geprüft werden, ob die richtige Anzahl an 
	 * Attributwerten vorhanden ist. Kann nicht mehr aufgerufen werden, wenn bereits printTuple()
	 * aufgerufen wurde.
	 * @param attNames Vector<String> - Attributnamen
	 * @exception Fehler, wenn bereits printTuple() aufgerufen wurde, oder attNames = null.
	 */
	public void setAttNames(Vector<String> attNames) throws Exception
	{
		//Fehler wenn bereits ein Tupel übergeben
		if (this.writeOne) throw new Exception(ERR_GET_TUPLE);
		//Fehler wenn Liste leer
		if (attNames==  null) throw new Exception(ERR_EMPTY_ATTNAMES);
		
		this.attNames= new String[attNames.size()];
		
		int i= 0;		//counter
		for (String attName: attNames)
		{
			if ((attName== null) || (attName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ATTNAME);
			this.attNames[i]= attName;
			i++;
		}
	}
	
	/**
	 * Nimmt ein übergebenes Tupel an, dieses wird als Tupel nach internem Schwellwert geschrieben. 
	 * Wenn tuple leer geschieht nichts. Zwischen den Listeneinträgen wird mit dem Seperator getrennt.
	 * Mit flush() sofort geschrieben werden.
	 * @param tuple Vector<String> - Liste der zu schreibenden Tupel
	 */
	public void addTuple(Vector<String> tuple) throws Exception
	{
		this.writeOne= true;
		String strTuple= "";
		//Fehler wenn Anzahl an Werten ungleich der in Attributnamen
		if ((this.attNames != null) && (this.attNames.length != tuple.size())) throw new Exception(ERR_LENGTH_MATCH);
			
		if (tuple != null)
		{
			int i= 0;	//counter
			//durch alle tupel gehen
			for (String attValue: tuple)
			{
				strTuple= strTuple + attValue; 
				if (i < tuple.size()-1) strTuple= strTuple + this.sep;
				i++;
			}
			tuples.add(strTuple);
			this.numOfTuples++;
			//flushen wenn Schwellwert erreicht
			if (this.tuples.size() >= TRESHOLD) this.flush();
		}
	}
	
	/**
	 * Schribt die übergebenen Tupel in die Datei.
	 */
	public void flush() throws Exception
	{
		// nur flushen, wenn überhaupt etwas in Tupelliste steht
		if (this.tuples.size() > 0)
		{
			for (String tuple: tuples)
			{
				this.oStream.println(tuple);// + "\n");
			}
			this.tuples.clear();
			this.oStream.flush();
			
		}
	}
	
	/**
	 * Schreibt die letzen Daten auf den Stream und schließt diesen.
	 */
	public void close() throws Exception
	{
		this.flush();
		this.oStream.close();
	}
	
	/**
	 * Setzt die Anzahl der erzeugten Objekte runter und löscht die temporären Dateien.
	 */
	public void finalize() throws Exception
	{
		System.out.println(MSG_STD + "werde nun endlich gelöscht: " + this.getFileName());
		if (this.logger!= null) this.logger.debug(MSG_FLUSH + this.getFileName());
		this.flush();
		//wenn Datei nur temporär war dann wieder löschen
		if (this.isTemp) 
		{
			if (this.logger!= null) this.logger.debug(MSG_DELETE + this.getFileName());
			TupleWriter.delPathWriterEntry(this, this.pathName);
		}
		numOfObj--;
	}
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "obj: "+ this.objId;
		retStr= retStr + ", relation name: " + this.relName;
		retStr= retStr + ", abstract relation name: " + this.absName;
		retStr= retStr + ", file name: "+ this.getFileName() + ", attribute names:";
		for (String attName: this.attNames)
			retStr= retStr + " " + attName;
		
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	


}
