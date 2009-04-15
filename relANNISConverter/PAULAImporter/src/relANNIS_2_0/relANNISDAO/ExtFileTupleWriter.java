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
public class ExtFileTupleWriter extends TupleWriter 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= "IDTupleWriter";
	protected long relId= 0;							//freie relationale Id
	
	/**
	 * Gibt an, wenn der Writer zum ersten Mal geflusht wird.
	 */
	protected boolean firstFlush= true;
	
	/**
	 * Verzeichnis  in das die externen Dateien geschrieben werden
	 */
	protected File extFileDir= null;
	//	 *************************************** Meldungen ***************************************
	//private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_TUPLE=		MSG_ERR + "Cannot add a tuple, because the given tuple is empty.";
	private static final String ERR_EMPTY_EXTFILE=		MSG_ERR + "Cannot add a tuple, because the given external file reference is empty.";
	private static final String ERR_NO_EXTDIR=			MSG_ERR + "Cannot create a tuple writer, because the external direcory refernce does not exists.";
	private static final String ERR_DST_FILE_EXISTS=	MSG_ERR + "Cannot add a tuple and copy the file to external file directory, because the given file already exists: ";

//	 ============================================== statische Methoden ==============================================
	/**
	 * This method copies a binary file. The file reference will be
	 * overridden if exists. 
	 * @param from File - file which should be copied
	 * @param to File - file reference to where the file should copied 
	 */
	protected static void copyFiles(File from, File to) throws Exception
	{
		RandomAccessFile inFile = new RandomAccessFile(from,"r");
	    RandomAccessFile outFile = new RandomAccessFile(to, "rw");
	    while (outFile.length() < inFile.length()) {
	        outFile.write(inFile.read());
	    }
	    inFile.close();
	    outFile.close();
	}
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Creates a new ExtFileTupleWriter-Object which works like a TupleWriter
	 * with extension for copying external files in a special directory. Also 
	 * the pathname will be inserted as attribute in tuple.
	 * @param relName String - the name of the relation, to where the tuples will be flushed
	 * @param outFile File - reference to the output file (bulk loader file for postgres)
	 * @param extDir  File - reference to the external directory
	 * @param override boolean - if set to true, a already existing outfile will be overridden
	 * @param isTemp boolean - says if the outfile is just temprorary
	 */
	public ExtFileTupleWriter(	String relName, 
								File outFile, 
								File extDir,
								boolean override,
								boolean isTemp) throws Exception
	{
		super(relName, outFile, override, isTemp);
		if (extDir== null) 
			throw new Exception(ERR_NO_EXTDIR);
		this.extFileDir= extDir;
		//wenn extDir nicht existiert, dann erstellen
		if (!extDir.exists())
			extDir.mkdir();
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Adds a new tuple to the sequence of tuples and inserts it to the tuple stream.
	 * In opposite to TupleWriter this object stores an external file in seperate
	 * folder and adds a reference to the tuple.
	 * @param Vector<String> tuple - the tuple for insert
	 * @param extFile File - the external file which has to be added to the tuple and the external directory
	 * @param pos long - the position where the reference in the tuple should be inserted
	 * @param subDir String - an extension of the external directory where the file should be placed
	 * @return a file object which points to the final destination where the given file is placed 
	 */
	public File addTuple(Vector<String> tuple, File extFile, long pos) throws Exception
	{
		return(this.addTuple(tuple, extFile, pos, null));
	}
	
	/**
	 * Adds a new tuple to the sequence of tuples and inserts it to the tuple stream.
	 * In opposite to TupleWriter this object stores an external file in seperate
	 * folder and adds a reference to the tuple.
	 * @param Vector<String> tuple - the tuple for insert
	 * @param extFile File - the external file which has to be added to the tuple and the external directory
	 * @param pos long - the position where the reference in the tuple should be inserted
	 * @param subDir String - an extension of the external directory where the file should be placed
	 * @param override boolean - should be true if the a file with the name of where the extFile should be copied already exists 
	 * @return a file object which points to the final destination where the given file is placed 
	 */
	public File addTuple(	Vector<String> tuple, 
							File extFile, 
							long pos,
							boolean override) throws Exception
	{
		return(this.addTuple(tuple, extFile, pos, null, override));
	}
	
	/**
	 * Adds a new tuple to the sequence of tuples and inserts it to the tuple stream.
	 * In opposite to TupleWriter this object stores an external file in seperate
	 * folder and adds a reference to the tuple.
	 * @param Vector<String> tuple - the tuple for insert
	 * @param extFile File - the external file which has to be added to the tuple and the external directory
	 * @param pos long - the position where the reference in the tuple should be inserted
	 * @param subDir String - an extension of the external directory where the file should be placed
	 * @return a file object which points to the final destination where the given file is placed 
	 */
	public File addTuple(	Vector<String> tuple, 
							File extFile, 
							long pos, 
							String subDir) throws Exception
	{
		return(this.addTuple(tuple, extFile, pos, subDir, false));
	}
	
	/**
	 * Adds a new tuple to the sequence of tuples and inserts it to the tuple stream.
	 * In opposite to TupleWriter this object stores an external file in seperate
	 * folder and adds a reference to the tuple.
	 * @param Vector<String> tuple - the tuple for insert
	 * @param extFile File - the external file which has to be added to the tuple and the external directory
	 * @param pos long - the position where the reference in the tuple should be inserted (starts at 0)
	 * @param subDir String - an extension of the external directory where the file should be placed
	 * @param override boolean - should be true if the a file with the name of where the extFile should be copied already exists 
	 * @return a file object which points to the final destination where the given file is placed 
	 */
	public File addTuple(	Vector<String> tuple, 
							File extFile, 
							long pos, 
							String subDir,
							boolean override) throws Exception
	{
		//Fehlerprüfung der Parameter
		if ((tuple== null) || (tuple.isEmpty())) throw new Exception(ERR_EMPTY_TUPLE);
		if ((extFile== null)) throw new Exception(ERR_EMPTY_EXTFILE);
		
		File dstFile= null;
		//Verzeichniss, in das diese Datei geschrieben werden soll
		File dstDir= this.extFileDir;
		// dstDir erstellen
		if ((subDir!= null) && (!subDir.equalsIgnoreCase("")))
		{
			dstDir= new File(this.extFileDir.getAbsolutePath() + "/"+ subDir);
			if (!dstDir.exists())
				dstDir.mkdir();
		}	
		dstFile= new File(dstDir.getCanonicalPath() + "/" +extFile.getName());
		//Fehler, wenn Zieldatei bereits vorhanden
		if ((dstFile.exists()) && (!override))
			throw new Exception(ERR_DST_FILE_EXISTS + dstFile.getCanonicalPath());
		//wenn Dateien nicht gleich sind
		if (!dstFile.equals(extFile))	
			ExtFileTupleWriter.copyFiles(extFile, dstFile);
		Vector<String> finalTuple= new Vector<String>(); 
		boolean addAudioRef= false;
		
		String extFilePath= "";//dstFile.getCanonicalPath().replace("\\", "/");
		extFilePath= "[ExtFile]"+subDir+"/"+dstFile.getName();
		//tupel erstellen
		for (int i= 0; i< tuple.size(); i++)
		{
			//Stelle an der der Blob eingefügt werden soll gefunden
			if (i== pos)
			{
				finalTuple.add(extFilePath);
				finalTuple.add(tuple.elementAt(i));
				addAudioRef= true;
			}
			else
			{
				finalTuple.add(tuple.elementAt(i));
			}
		}
		if (!addAudioRef)
			finalTuple.add(extFilePath);
		
		super.addTuple(finalTuple);
		return(dstFile);
	}
}
