package relANNIS_2_0.relANNISDAO;

import java.io.File;
import java.util.Vector;

/**
 * Die Klasse gibt eObjekte zur Verfügung, die relationale Tupel auf einen Stream schreiben
 * können. Dabei kümmern sich diese Objekte selbständig um die Vergabe der relationalen ID. 
 * @author Florian Zipser
 * @version 1.0
 */
public class IDTupleWriter extends TupleWriter 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= "IDTupleWriter";
	protected long relId= 0;							//freie relationale Id
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_TUPLE_GIVEN=		MSG_ERR + "Cannot create a copy, a tuple was already given to the object.";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Instanziiert ein IDTupleWriter-Objekt, dass sich selbst um die relationale ID kümmert.
	 * @param absName String - abstrakter Name der Rellation nach dem DDD-Modell
	 * @param relName String - Name der relation, in die die Tupel kommen
	 * @param outFileName String - name der Ausgabedatei
	 * @param pathName String - Name des Verzeichnisses in das die Datei geschrieben werden soll
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param relId long - relationale Id (freie Id des ersten Tupels)
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist 
	 */
	public IDTupleWriter(	String absName,	
							String relName, 
							String pathName, 
							String outFileName, 
							boolean override, 
							long relId, 
							boolean isTemp) throws Exception
	{
		super(absName, relName, pathName, outFileName, override, isTemp);
		this.relId= relId;
	}
	
	/**
	 * Instanziiert ein IDTupleWriter-Objekt, dass sich selbst um die relationale ID kümmert.
	 * @param absName String - abstrakter Name der Rellation nach dem DDD-Modell
	 * @param relName String - Name der relation, in die die Tupel kommen
	 * @param outFile File - Referenz auf die Ausgabedatei
	 * @param override boolean - true, bereits bestehende Datei kann überschrieben werden, false wenn Tupel an die Datei angehängt werden sollen
	 * @param relId long - relationale Id (freie Id des ersten Tupels)
	 * @param isTemp boolean - true, wenn die Datei wieder gelöscht werden soll, false sonst 
	 * @exception Fehler, wenn Dateiname leer ist 
	 */
	public IDTupleWriter(	String absName,	
							String relName, 
							File outFile, 
							boolean override, 
							long relId, 
							boolean isTemp) throws Exception
	{
		super(absName, relName, outFile, override, isTemp);
		this.relId= relId;
	}
//	 ============================================== private Methoden ==============================================
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
	 * werden.
	 * @param tuple Vector<String> - Liste der zu schreibenden Tupel
	 * @return Der vergebene id Wert für dieses Tupel
	 */
	public long addTuple2(Vector<String> tuple) throws Exception
	{
		Vector<String> idTuple= new Vector<String>();
		idTuple.add(String.valueOf(this.relId));
		idTuple.addAll(tuple);
		super.addTuple(idTuple);
		this.relId++;
		return(this.relId-1);
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
	
	
}
