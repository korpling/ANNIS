package relANNIS_2_0.relANNISDAO;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Vector;

/**
 * Dieser TupleWriter kann BLOBs in ein Insert-Script für Postgress schreiben.
 * Außerdem werden alle Blob-Dateien in ein übergebenes Verzeichniss kopiert
 * und von dort aus importiert.
 * @author Administrator
 *
 */
public class BLOBTupleWriter extends IDTupleWriter 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= "IDTupleWriter";
	protected long relId= 0;							//freie relationale Id
	
	/**
	 * Gibt an, wenn der Writer zum ersten Mal geflusht wird.
	 */
	protected boolean firstFlush= true;
	
	/**
	 * Verzeichnis  in das die Blob-Dateien geschrieben werden
	 */
	protected File blobDir= null;
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_TUPLE_GIVEN=		MSG_ERR + "Cannot create a copy, a tuple was already given to the object.";
	private static final String ERR_NO_BLOBPATH=		MSG_ERR + "Cannot add the given tuple, because the blob Path is empty.";
	private static final String ERR_EMPTY_BLOBPATH=		MSG_ERR + "Cannot add the given tuple, because the blob Path is empty.";
	private static final String ERR_NO_BLOBFILE=		MSG_ERR + "Cannot add the given tuple, because the doesn´t exist or is not a file: ";
	private static final String ERR_NO_ATTNAMES=		MSG_ERR + "Cannot print TupleWriter stream, because the attribute names are not given. Please call setAttNames() first.";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Instanziiert ein IDTupleWriter-Objekt, dass sich selbst um die relationale ID kümmert.
	 * @param absName String - abstrakter Name der Rellation nach dem DDD-Modell
	 * @param outFile File - Referenz auf die Ausgabedatei
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param relId long - relationale Id (freie Id des ersten Tupels)
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist 
	 */
	public BLOBTupleWriter(	String absName,	
							String relName, 
							File outFile, 
							boolean override, 
							long relId, 
							boolean isTemp) throws Exception
	{
		super(absName, relName, outFile, override, relId, isTemp);
		this.relId= relId;
		this.setSeperator(", ");
	}
	
	/**
	 * Instanziiert ein IDTupleWriter-Objekt, dass sich selbst um die relationale ID kümmert.
	 * @param absName String - abstrakter Name der Rellation nach dem DDD-Modell
	 * @param outFile File - Referenz auf die Ausgabedatei
	 * @param blobDir File - Referenz auf das Verzeichniss, in das die Blobdateien kopiert werden
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param relId long - relationale Id (freie Id des ersten Tupels)
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist 
	 */
	public BLOBTupleWriter(	String absName,	
							String relName, 
							File outFile,
							File blobDir,
							boolean override, 
							long relId, 
							boolean isTemp) throws Exception
	{
		this(absName, relName, outFile, override, relId, isTemp);
		if (blobDir!= null)
		{
			//wenn blobDir nicht existiert, dann erstellen
			if (!blobDir.exists())
			{
				blobDir.mkdir();
			}
			this.blobDir= blobDir;
		}
	}
//	 ============================================== private Methoden ==============================================
	
	/**
	 * Macht aus dem Blob einen Ausdruck, damit er von Postgres importiert werden kann.
	 * @param blobPath File - BLOB als Datei
	 */
	private String getBlob2String(File blobPath) throws Exception
	{
		if (blobPath== null) throw new Exception(ERR_EMPTY_BLOBPATH); 
		if (!blobPath.exists()) throw new Exception(ERR_NO_BLOBFILE);
		if (!blobPath.isFile()) throw new Exception(ERR_NO_BLOBFILE + blobPath.getAbsolutePath());
		
		String blobFile= blobPath.getAbsolutePath();
		//wenn es Blob-Verzeichnis gibt, dann Datei dorthin kopieren
		if (this.blobDir!= null)
		{
			blobFile= this.blobDir.getAbsolutePath() + "/" + blobPath.getName();
			
			RandomAccessFile inFile = new RandomAccessFile(blobPath,"r");
	        RandomAccessFile outFile = new RandomAccessFile(blobFile, "rw");
	        while (outFile.length() < inFile.length()) {
	            outFile.write(inFile.read());
	        }
	        inFile.close();
	        outFile.close();
		}
		
		String retStr= null;
		retStr= "lo_import(E'"+blobFile.replace("\\", "/")+"')";
		return(retStr);
	}
	
	/**
	 * Schreibt die Kopfzeile auf den Stream.
	 */
	private void printHead() throws Exception
	{
		this.oStream.print("INSERT INTO "+ this.getRelName() + " (");
		Vector<String> attNames= this.getAttNames();
		if (attNames== null) throw new Exception(ERR_NO_ATTNAMES);
		for (String attName: attNames)
		{
			this.oStream.print(attName);
			//wenn Element nicht das letzte ist
			if (!attNames.lastElement().equalsIgnoreCase(attName))
				this.oStream.print(", ");
		}
		this.oStream.println(") VALUES");
	}
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Erhöht den ID-Wert um eins, ohne dass es ein Tupel zu diesem Wert gibt.
	 */
	public void incIDVal()
	{ this.relId++; }
	
	/**
	 * Nimmt ein übergebenes Tupel an, dieses wird als Tupel nach internem Schwellwert geschrieben. 
	 * Wenn tuple leer geschieht nichts. Zwischen den Listeneinträgen wird mit dem Seperator getrennt.
	 * Mit flush() sofort geschrieben werden. Um den relationalen Id-Wert muss sich nicht gekümmert 
	 * werden.
	 * @param tuple Vector<String> - Liste der zu schreibenden Tupel
	 */
	public void addTuple(Vector<String> tuple) throws Exception
	{ addTuple2(tuple); }
	
	/**
	 * Nimmt ein übergebenes Tupel an, dieses wird als Tupel nach internem Schwellwert geschrieben. 
	 * Wenn tuple leer geschieht nichts. Zwischen den Listeneinträgen wird mit dem Seperator getrennt.
	 * Mit flush() sofort geschrieben werden. Um den relationalen Id-Wert muss sich nicht gekümmert 
	 * werden. Die Datei, die als Blob angefügt werden soll, wird immer als letzte Spalte eingefügt.
	 * @param tuple Vector<String> - Liste der zu schreibenden Tupel
	 * @param blobPath File - Pfad zu der Datei, die als Blob eingefügt werden soll
	 * @return Der vergebene id Wert für dieses Tupel
	 */
	public long addTuple2(Vector<String> tuple, File blobPath) throws Exception
	{
		return(addTuple2(tuple, blobPath, tuple.size()));
	}
	
	/**
	 * Nimmt ein übergebenes Tupel an, dieses wird als Tupel nach internem Schwellwert geschrieben. 
	 * Wenn tuple leer geschieht nichts. Zwischen den Listeneinträgen wird mit dem Seperator getrennt.
	 * Mit flush() sofort geschrieben werden. Um den relationalen Id-Wert muss sich nicht gekümmert 
	 * werden. Mit pos kann die poition angegeben werden, an der die Blob-Datei als Spalte eingefügt werden soll.
	 * @param tuple Vector<String> - Liste der zu schreibenden Tupel
	 * @param blobPath File - Pfad zu der Datei, die als Blob eingefügt werden soll
	 * @param pos long - Stelle/Spalte in die die Blob-Datei eingefügt werden soll, 0 = als erste Spalte tuple.size= letzte Spalte
	 * @return Der vergebene id Wert für dieses Tupel
	 */
	public long addTuple2(Vector<String> tuple, File blobPath, long pos) throws Exception
	{
		Vector<String> idTuple= new Vector<String>();
		boolean printBLOB= false;	//gibt an, ob der BLOB ausgegeben wurde
		String val= null;
		//gehe das Tupel durch
		for (int i= 0; i< tuple.size(); i++)
		{
			//Stelle an der der Blob eingefügt werden soll gefunden
			if (i== pos)
			{
				String path= this.getBlob2String(blobPath);
				printBLOB= true;
				if (path== null) throw new Exception(ERR_NO_BLOBPATH);
				idTuple.add(path);
			}
			val= tuple.get(i);
			try{
				new Long(val);
				idTuple.add(val);
			}
			catch (Exception e)
			{
				idTuple.add("\'"+val+"\'");
			}
		}
		if (!printBLOB)
		{
			String path= this.getBlob2String(blobPath);
			printBLOB= true;
			if (path== null) throw new Exception(ERR_NO_BLOBPATH);
			idTuple.add(path);
		}
		System.out.println("AttNames" + this.getAttNames());
		System.out.println("Tuple" + tuple);
		System.out.println("Tuple" + idTuple);
		return(super.addTuple2(idTuple));
	}
	
	/**
	 * Erstellt eine exakte Kopie dieses IDTupleWriter-Objektes und gibt diese zurück.
	 * Diese Methode kann nur aufgerufen werden, bevor ein Tupel diesem Objekt übergeben
	 * wurde.
	 * @return Kopie dieses Objektes
	 * @throws Exception Fehler wenn Kopie nicht erstellt werden kann
	 */
	public IDTupleWriter clone2() throws Exception
	{
		//Fehler wenn bereits Tupel übergeben wurden
		if (this.getNumOfTuples() > 0) throw new Exception(ERR_TUPLE_GIVEN);
		IDTupleWriter tWriter= new IDTupleWriter(this.getAbsName(), this.getRelName(), this.getPath(), this.getFileName(), !this.append, this.relId, this.isTemp);
		tWriter.setAttNames(this.getAttNames());
		return(tWriter);
	}
	
	/**
	 * Schreibt die übergebenen Tupel in die Datei.
	 */
	public void flush() throws Exception
	{
		// nur flushen, wenn überhaupt etwas in Tupelliste steht
		if (this.tuples.size() > 0)
		{
			//wenn zum ersten mal geflusht wird 
			if (this.firstFlush)
			{
				this.printHead();
			}
			System.out.println(this.tuples);
			for (String tuple: tuples)
			{
				//, und Leerzeile, wenn nicht zum ersten Mal geflusht wird 
				if (!this.firstFlush)this.oStream.println(",");
				this.oStream.print("("+tuple+")");// + "\n");
				System.out.println("last: "+ this.tuples.lastElement());
				System.out.println("curr: "+ tuple);
				//wenn tuple nicht das letzte ist, dann , ausgeben
				if (!this.tuples.lastElement().equalsIgnoreCase(tuple))
					this.oStream.print(",");
			}
			this.tuples.clear();
			this.oStream.flush();
			this.firstFlush= false;
		}
	}
	
	/**
	 * Schreibt die letzen Daten auf den Stream und schließt diesen.
	 */
	public void close() throws Exception
	{
		//letzte Daten schreiben
		this.flush();
		//Abschluss für die Datei schreiben
		this.oStream.println("");
		this.oStream.print(";");
		//Stream schließen
		this.oStream.close();
	}
}
