package relANNIS_2_0.relANNISDAO;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.ext.DefaultHandler2;

import util.settingMgr.SettingObject;

/**
 * Die Klasse DBConnector stellt ein Object zur Verfügung, dass die Kommunikation des 
 * PAULAImporters zur Datenbank relANNIS übernimmt. Neben dem öffnen und schließen der 
 * Datenbank-Verbindung stellt ein DBConnector-Object die TupleWriter und IDTupleWriter
 * bereit, die benötigt werden um die aus den PAULA-Dateien erstellten Tupel in die 
 * relationale Datenbank zu schreiben. 
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class DBConnector implements SettingObject
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"DBConnector";		//Name dieses Tools
	
	//Schlüsselworte
	private static final String KW_DB_NULL=		"NULL";			//Standard-Nullwert für Bulk-Dateien
	private static final String KW_DB_TRUE=		"true";			//Standard-Nullwert für Bulk-Dateien
	private static final String KW_DB_FALSE=	"false";		//Standard-Nullwert für Bulk-Dateien
	private static final String KW_DB_SEP=		"\\t";			//Standard-Seperator für Bulk-Dateien (Nicht interpretiert)
	
	//Schlüsselworte für DB-Verbindung
	private static final String KW_D=		"driver";					//Keyword für driver
	private static final String KW_H=		"host";						//Keyword für host
	private static final String KW_P=		"port";						//Keyword für port
	private static final String KW_DB=		"database";					//Keyword für database
	private static final String KW_U=		"user";						//Keyword für user
	private static final String KW_PASS=	"password";					//Keyword für password
	
	//Schlüsselworte für relANNIS-Datenbankobjekte
	private static final String KW_STRUCT_ALL=	"ALL";					//Schlüsselwort für das Wurzelelement aller Structtupel
	private static final String KW_STRUCT_ATT_NAME=	"name";					//Schlüsselwort für das Attribut struct.name
	private static final String KW_STRUCT_ATT_ID=	"ID";					//Schlüsselwort für den ID-Wert der Struct-Relation
	private static final String KW_COL_ALL=	"ALL";					//Schlüsselwort für das Wurzelelement aller korpustupel
	private static final String KW_COL_ATT_NAME=	"type";					//Schlüsselwort für das Attribut korpus.name
	private static final String KW_COL_ATT_ID=	"ID";					//Schlüsselwort für den ID-Wert der Struct-Relation
	private static final String KW_KORPUS_ALL=	"ALL";					//Schlüsselwort für das Wurzelelement aller korpustupel
	private static final String KW_KORPUS_ATT_NAME=	"name";					//Schlüsselwort für das Attribut korpus.name
	private static final String KW_ID= "id";							//Schlüsselwort für den ID_Wert der Rela
	private static final String KW_ATT_POST= "post";						//Schlüsselwort für den Post-Wert
	private static final String KW_ATT_PRE= "pre";						//Schlüsselwort für den Post-Wert
	private static final String KW_RANK_ATT_STREF=	"struct_ref";	//	//Schlüsselwort für das Attribut rank.struct_ref
	
	/**
	 * eindeutiger Corpus-Id Wert für das ALL-Tupel
	 */
	private static final Long CORPUS_ALL_CORP_ID=	0l;
	
	/**
	 * Ausgabeordner
	 */
	private File dstFolder= null;
	
	private Logger logger= null;	//logger für log4j
	//private DBParamReader pReader= null;	//Objekt zum auslesen der DB-Parameter
	//private boolean isInit= false;	//gibt an, ob die Parameter ausgelesen wurden 
	private Connection connection = null;	//aktuelle Verbindung zur Datenbank
	private Hashtable<String, TupleWriter> tWriters= null;	//Zuordnungstabelle von Relationsnamen und TupleWriter
		
	private Hashtable<String, String> dbParams= null;		//Zuordnungstabelle für Datenbankparameter
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "): ";
	private static final String MSG_SET=			MSG_STD+ "reade database parameter...";
	private static final String MSG_NOT_INIT=		MSG_STD+ "The DBConnector has not been initialized.";
	private static final String MSG_OK=				"OK";
	private static final String MSG_FAILED=			"FAILED";
	//	 *************************************** Fehlermeldungen ***************************************
	//private static final String ERR_EMPTY_FILENAME=		MSG_ERR + "The given filename for the setting file (db connection) is empty.";
	//private static final String ERR_SETFILE_NI=			MSG_ERR + "The given with settings fpr db connection file does not exist: ";
	//private static final String ERR_NOT_INIT=			MSG_ERR + "Couldn´t connect to database. The DBConnector has not been initialized.";
	private static final String ERR_NO_RELNAME=			MSG_ERR + "No relation name given. Cannot search for a TupleWriter.";
	private static final String ERR_NO_TWRITER=			MSG_ERR + "No correct TupleWriter was found for abstract relation name: ";
	private static final String ERR_VAL_NOT_GIVEN=		MSG_ERR + "The following value wasn´t given: ";
	private static final String ERR_SQL_EX=				MSG_ERR + "A sql exception occurs, this might be an internal failure. The failure was throwed during inserting into table: ";
	private static final String ERR_NO_SUPERROOT=		MSG_ERR + "An internal failure occurs. The superroot does not exist. Error from db: ";
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Erzeugt ein Objekt vom Typ DBConnector. Die Zugangsdaten für die Datenbankverbindung
	 * werden aus der übergebenen XML-Datei ausgelesen. Diese Datei muss dem Schema ???
	 * genügen.
	 * @param fileName String - Name der XML-Datei die die Verbindungsdaten enthält
	 */
	/*
	public DBConnector(String fileName) throws Exception
	{
		//Dateiname ist leer
		if ((fileName== null) || (fileName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_FILENAME);
		File setFile= new File(fileName);
		if (!setFile.exists()) throw new Exception(ERR_SETFILE_NI + fileName);
	}*/
	
	
	/**
	 * Erzeugt ein Objekt vom Typ DBConnector. Die Zugangsdaten für die Datenbankverbindung
	 * müssen Seperat gesetzt werden. 
	 * @param logger Logger - Logger zum loggen von Nachrichten
	 */
	public DBConnector(File dstFolder, Logger logger)
	{
		this.dstFolder= dstFolder;
		this.init();
		this.logger= logger;
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Initialisiert alle Objekte, die für dieses Objekt notwendig sind.
	 */
	private void init()
	{			
		//Zuordnungstabelle von tWritern und Relationsnamen erstellen
		this.tWriters= new Hashtable<String, TupleWriter>();
		//Zuordnungstabelle für DB-Parameter erstellen
		this.dbParams= new Hashtable<String, String>();
	}
	
	
	/**
	 * Prüft ob alle Datenbankparameter gesetzt sind und geibt einen Fehler wennn dem
	 * nicht so ist.
	 * @exception Fehler, wenn einer der Werte nicht gesetzt wurde
	 */
	private void checkParameter() throws Exception
	{
		//Treiber nicht gegeben
		if (!this.dbParams.containsKey(KW_D)) throw new Exception(ERR_VAL_NOT_GIVEN + KW_D);
		//Host nicht gegeben
		if (!this.dbParams.containsKey(KW_H)) throw new Exception(ERR_VAL_NOT_GIVEN + KW_H);
		//Port nicht gegeben
		if (!this.dbParams.containsKey(KW_P)) throw new Exception(ERR_VAL_NOT_GIVEN + KW_P);
		//datenbank nicht gegeben
		if (!this.dbParams.containsKey(KW_DB)) throw new Exception(ERR_VAL_NOT_GIVEN + KW_DB);
		//	Treiber nicht gegeben
		if (!this.dbParams.containsKey(KW_U)) throw new Exception(ERR_VAL_NOT_GIVEN + KW_U);
		//Passwort nicht gegeben
		if (!this.dbParams.containsKey(KW_PASS)) throw new Exception(ERR_VAL_NOT_GIVEN + KW_PASS);
	}
	
	/**
	 * Erzeugt aus den Datenbank-Parametern eine Adresse zur zu benutzenden Datenbankinstanz.
	 * @return Adresse zur Datenbank
	 */
	private String getUrl ()
	{
	    // PostgreSQL takes one of the following url-forms:
	    // ================================================
	    // jdbc:postgresql:database
	    // jdbc:postgresql://host/database
	    // jdbc:postgresql://host:port/database
	 
	    String url= ("jdbc:postgresql:" + (this.dbParams.get(KW_H)!= null ? ("//" + this.dbParams.get(KW_H)) + 
	    			(this.dbParams.get(KW_P) != null ? ":" + this.dbParams.get(KW_P) : "") + "/" : "") + this.dbParams.get(KW_DB));
	    return(url);
	 }
	
	/**
	 * Generiert ein BULK-Load statement, dieses nutzt den COPY Befehl und lädt die Daten der
	 * übergebenen Datei in die übergebene Relation.
	 * <br/>
	 * Statement: COPY relName FROM E'fileName' USING DELIMITERS E'\t' WITH NULL AS 'NULL';
	 * @param relName String - Relationsname der Relation in die geladen werden soll 
	 * @param fileName String - Dateiname der Datei aus der geladen werden soll
	 * @return Statement in SQL, das das laden ermöglicht
	 */
	private String genBULKLoadStmt(String relName, String fileName)
	{
		String fileN= fileName.replace("\\", "/");
		String stmt= "";
		stmt= 	"COPY " + relName + 
				" FROM E'" + fileN + "'"+
				" USING DELIMITERS E'"+ KW_DB_SEP + "'"+
				" WITH NULL AS '"+ KW_DB_NULL + "';"; 
		
		return(stmt);
	}
	
	/**
	 * Generiert ein BULK-Download statement, dieses nutzt den COPY Befehl und lädt die Daten der
	 * übergebenen Datei in die übergebene Relation.
	 * <br/>
	 * Statement: COPY relName FROM E'fileName' USING DELIMITERS E'\t' WITH NULL AS 'NULL';
	 * @param relName String - Relationsname der Relation in die geladen werden soll 
	 * @param fileName String - Dateiname der Datei aus der geladen werden soll
	 * @return Statement in SQL, das das laden ermöglicht
	 */
	private String genBULKDownLoadStmt(String relName, String fileName)
	{
		String fileN= fileName.replace("\\", "/");
		String stmt= "";
		stmt= 	"COPY " + relName + 
				" TO E'" + fileN + "'"+
				" USING DELIMITERS E'"+ KW_DB_SEP + "'"+
				" WITH NULL AS '"+ KW_DB_NULL + "';"; 
		
		return(stmt);
	}
	
//	 ============================================== öffentliche Methoden ==============================================
//	---------------------------- Implementierungen von SettingObject ----------------------------	
	/**
	 * @see util.SettingMgr.SettingObject#getSetEntry()
	 */
	public Hashtable<DefaultHandler2, Vector<String>> getSetEntry() throws Exception
	{
		Hashtable<DefaultHandler2, Vector<String>> table= new Hashtable<DefaultHandler2, Vector<String>>();
		//Reader für DB-Parameter erstellen
		DBParamReader pReader= new DBParamReader(this.dbParams);
		//Liste der SettingNames und SettingListener in Tabelle schreiben
		table.put(pReader, DBParamReader.getSetNames());
		//Reader für DB_Relationseigenschaften erstellen
		RelationSetReader rReader= new RelationSetReader(tWriters, this.dstFolder ,this);
		//Liste der SettingNames und SettingListener in Tabelle schreiben
		table.put(rReader, RelationSetReader.getSetNames());
		return(table);
	}
	
	/**
	 * @see util.SettingMgr.SettingObject#readSettings()
	 */
	public void readSettings() throws Exception
	{
		if (this.logger != null) this.logger.debug(MSG_SET);
	}
//	---------------------------- Ende Implementierungen von SettingObject ----------------------------	

	//--------------------------------- neue Methoden  ---------------------------------
	/**
	 * Gibt einen Datenbank NULL Wert zurück.
	 * @return DB-NULL-Wert
	 */
	public String getDBNULL()
		{ return(KW_DB_NULL);}
	
	/**
	 * Gibt einen Datenbank true Wert zurück.
	 * @return DB-NULL-Wert
	 */
	public String getDBTRUE()
		{ return(KW_DB_TRUE);}
	
	/**
	 * Gibt einen Datenbank false Wert zurück.
	 * @return DB-NULL-Wert
	 */
	public String getDBFALSE()
		{ return(KW_DB_FALSE);}
	
	/**
	 * Gibt einen TupleWriter zurück, der zu dem übergebenen Relationsnamen passt.
	 * @param relName String - Name der Relation, zu der ein TupleWriter gesucht wird
	 * @return TupleWriter-Objekt passend zu dem übergebenen Relationsnamen 
	 */
	public TupleWriter getTWriter(String relName) throws Exception
	{
		if ((relName== null) || (relName.equalsIgnoreCase(""))) throw new Exception(ERR_NO_RELNAME);
		TupleWriter tWriter= this.tWriters.get(relName.toUpperCase());
		if (tWriter == null) throw new Exception(ERR_NO_TWRITER + relName.toUpperCase());
		
		return(tWriter);
	}
		
	/**
	 * Gibt einen den maximalen verwendeten ID-Wert zu einer übergebenen Relation und einem
	 * Attributnamen zurück. Die zurückgegebene ID wird bereits verwendet und muss nicht
	 * eindeutig sein.
	 * @param absRelName String - abstrakter Name der Relation in der der maximale Eintrag gesucht wird
	 * @param attName String - Name des Attributes in dem der maximale Eintrag gesucht wird
	 * @return maximal verwendeter ID-Wert
	 * @throws Exception
	 */
	public long getMaxID(	String absRelName,
							String attName) throws Exception
							
	{
		if (this.logger != null) this.logger.debug("search new id value for relation: "+ absRelName);
		//Verbindung aufbauen, wenn keine existiert
		if (this.connection == null) this.connect();
		long idVal= 0;
		
		String relName= this.getTWriter(absRelName).getRelName();
		Statement statement = this.connection.createStatement();
		
		String stmt= "select max(" + attName +") from " + relName + ";";
		ResultSet resultSet = statement.executeQuery (stmt);
		//old newer driver resultSet.first();
		resultSet.next();
		idVal= resultSet.getLong(1);
		
		return(idVal);
	}
	
	/**
	 * Gibt einen noch nicht genutzten ID-Wert für die Relation mit dem übergebenen Namen 
	 * zurück. Dieser ID Wert ist der maximal vergebene in der Datenbank + 1. Das Attribut
	 * das den ID-Wert enthält muss den Namen ID tragen.
	 * @param absRelName String - Name der Relation, für die der IS-Wert gesucht wird
	 * @param idAtt String - Name des die ID tragenden Attributes
	 * @return neuer (noch nicht vergebener) ID-Wert
	 */
	public long getNewIDFromRel(	String relName,
									String idAtt) throws Exception
	{ 
		long idVal= 0;
		
		if (this.connection == null) this.connect();
		Statement statement = this.connection.createStatement();
		String stmt= "select max(" + idAtt +") from " + relName + ";";
		//System.out.println("statement: "+ stmt);
		ResultSet resultSet = statement.executeQuery (stmt);
		//old newer driver resultSet.first();
		resultSet.next();
		idVal= resultSet.getLong(1);
		
		return(idVal+ 1); 
	}
	
	/**
	 * Gibt einen noch nicht genutzten ID-Wert für die Relation mit dem übergebenen Namen 
	 * zurück. Dieser ID Wert ist der maximal vergebene in der Datenbank + 1. Das Attribut
	 * das den ID-Wert enthält muss den Namen ID tragen.
	 * @param absRelName String - Name der Relation, für die der IS-Wert gesucht wird
	 * @param idAtt String - Name des die ID tragenden Attributes
	 * @return neuer (noch nicht vergebener) ID-Wert
	 */
	public long getNewID(	String absRelName,
							String idAtt) throws Exception
	{ return(this.getMaxID(absRelName, idAtt)+1); }

	/**
	 * Liefert einen PP-Wert zurück, der bereitz als maximaler Post-Wert in der übergebenen
	 * Tabelle existiert.
	 * @param absRelName String - abstrakter Tabellenname
	 * @return höchster Postwert
	 */
	public long getNewPPVal(String absRelName) throws Exception
		{ return(this.getMaxID(absRelName, KW_ATT_POST)); }
	
	/**
	 * Gibt den Pre Wert des ALL-Tupels in der struct-Tabelle zurück
	 * @param absStructRelName String - Name der Struct-Tabelle
	 * @param absRankRelName String - Name der Rank-Tabelle
	 * @return
	 */
	public Long getStructAllPre(	String absStructRelName,
									String absRankRelName) throws Exception
	{
		if (this.connection == null) this.connect();
		String relStructName= ((TupleWriter)this.getTWriter(absStructRelName)).getRelName();
		String relRankName= ((TupleWriter)this.getTWriter(absRankRelName)).getRelName();
		Statement statement = this.connection.createStatement();
		String stmt=	"SELECT " 	+ KW_ATT_PRE +" "+
						"FROM "		+ relRankName+ " "+
						"WHERE "	+ KW_RANK_ATT_STREF+ " IN ("+	
						
						"SELECT "+ KW_STRUCT_ATT_ID  + " "+
						"FROM " + relStructName + " "+
						"WHERE "+KW_STRUCT_ATT_NAME +"='" + KW_STRUCT_ALL +"');";
		//System.out.println(stmt);
		ResultSet resultSet = statement.executeQuery (stmt);
		//old newer driver resultSet.first();
		resultSet.next();
		Long idVal= resultSet.getLong(1);
		
		return(idVal); 	
	}
	
	/**
	 * Erzeugt in der Korpusabelle ein All-Tupel, dass die Wurzel aller Struct-Tupel 
	 * darstellt. Der Name der Korpustabelle in der Datenbank wird über ihren abtrakten Namen
	 * ermittelt. 
	 * @param absRelName String - abstrakter Name der Korpustabelle,  
	 */
	public void createCorpusAll(String absRelName) throws Exception
	{
		//IDTupleWriter tWriter= (IDTupleWriter)this.getTWriter(absRelName);
		TupleWriter tWriter= this.getTWriter(absRelName);
		String relName= tWriter.getRelName();
		long newID= this.getNewID(absRelName, KW_STRUCT_ATT_ID);
		//prüfe ob All-Tupel bereits existiert
		String stmt= 	"SELECT 	" + KW_STRUCT_ATT_ID + " "+
						"FROM	"+ relName +" "+
						"WHERE "+ KW_KORPUS_ATT_NAME +" = '"+ KW_KORPUS_ALL + "';";
		ResultSet resultSet = this.connection.createStatement().executeQuery (stmt);
		//wenn es noch kein ALL-Element gibt, dann einfügen
		//old newer driver if (!resultSet.first())
		if (!resultSet.next())
		{
			//erzeuge das insert-Statement für die Datenbank
			//insert into korpus values(0, CORPUS_ALL_CORP_ID, 'ALL', 0,1);
			String insertStmt= 	"insert into "+ relName +" " +
								"values(" 	+ newID + ", " 
											+ CORPUS_ALL_CORP_ID + ", "
											+"'"+ KW_STRUCT_ALL+ "', "
											+"0, "
											+"1);";
			//System.out.println(insertStmt);
			Statement statement = this.connection.createStatement();
			statement.executeUpdate(insertStmt);
			//tWriter.incIDVal();
		}
	}
	
	/**
	 * Erzeugt in der Collectiontabelle ein All-Tupel, dass die Wurzel aller Struct-Tupel 
	 * darstellt. Der Name der Collectiontabelle in der Datenbank wird über ihren abtrakten Namen
	 * ermittelt. 
	 * @param absRelName String - abstrakter Name der Collectiontabelle,
	 * @param absRelName String - abstrakter Name der Ranktabelle der Collections  
	 */
	public void createColAll(	String absColRelName,
								String absColRankRelName) throws Exception
	{
		IDTupleWriter tWriter= (IDTupleWriter)this.getTWriter(absColRelName);
		String relName= tWriter.getRelName();
		long newID= this.getNewID(absColRelName, KW_STRUCT_ATT_ID);
		//prüfe ob All-Tupel bereits existiert
		String stmt= 	"SELECT 	" + KW_STRUCT_ATT_ID + " "+
						"FROM	"+ relName +" "+
						"WHERE "+ KW_STRUCT_ATT_NAME +" = '"+ KW_STRUCT_ALL + "';";
		ResultSet resultSet = this.connection.createStatement().executeQuery (stmt);
		//wenn es noch kein ALL-Element gibt, dann einfügen
		//old newer driver if (!resultSet.first())
		if (!resultSet.next())
		{
			//erzeuge das insert-Statement für die Datenbank
			String insertStmt= 	"insert into "+ relName +" " +
					"values(" 	+ newID + ", " 
								+"'"+ KW_STRUCT_ALL+ "', "
								+"'"+ KW_STRUCT_ALL + "');";
			//System.out.println(insertStmt);
			Statement statement = this.connection.createStatement();
			statement.executeUpdate(insertStmt);
			tWriter.incIDVal();
			//Tupeleintrag für die Ranktabelle erstellen
			TupleWriter rankTWriter= this.getTWriter(absColRankRelName);
			String rankRelName= rankTWriter.getRelName();
			long pre= this.getMaxID(absColRankRelName, KW_ATT_PRE);
			long post= pre +1;
			//erzeuge das insert-Statement für die Datenbank
			String insertStmt2= 	"insert into "+ rankRelName +" " +
						"values(" 	+ newID + ", "
									+ pre + ", "
									+ post + ");";
			//System.out.println("neues Statement: " + insertStmt2);
			statement.executeUpdate(insertStmt2);
		}
	}
	
	/**
	 * Erzeugt in der Structabelle ein All-Tupel, dass die Wurzel aller Struct-Tupel 
	 * darstellt. Der Name der Structtabelle in der Datenbank wird über ihren abtrakten Namen
	 * ermittelt. Außerdem wird das All-Element in die Ranktabelle eingetragen.
	 * @param absRelName String - abstrakter Name der Structtabelle,  
	 */
	public void createStructAll(	String absStructRelName,
									String absRankRelName) throws Exception
	{
		IDTupleWriter tWriter= (IDTupleWriter)this.getTWriter(absStructRelName);
		String relName= tWriter.getRelName();
		long newID= this.getNewID(absStructRelName, KW_STRUCT_ATT_ID);
		//prüfe ob All-Tupel bereits existiert
		String stmt= 	"SELECT 	" + KW_STRUCT_ATT_ID + " "+
						"FROM	"+ relName +" "+
						"WHERE "+ KW_STRUCT_ATT_NAME +" = '"+ KW_STRUCT_ALL + "';";
		ResultSet resultSet = this.connection.createStatement().executeQuery (stmt);
		//wenn es noch kein ALL-Element gibt, dann einfügen
		//old newer driver if (!resultSet.first())
		if (!resultSet.next())
		{
			//erzeuge das insert-Statement für die Datenbank
			String insertStmt= 	"insert into "+ relName +" " +
								"values(" 	+ newID + ", " 
											+this.getDBNULL()+ ", " 
											+this.getDBNULL()+ ", "
											+this.getDBNULL()+ ", "
											+"'"+ KW_STRUCT_ALL+ "', "
											+"'"+ KW_STRUCT_ALL+ "', "
											+this.getDBNULL()+ ", "
											+this.getDBNULL()+ ", "
											+this.getDBNULL()+ ", "
											+this.getDBNULL()+ ");";
			//System.out.println(insertStmt);
			Statement statement = this.connection.createStatement();
			statement.executeUpdate(insertStmt);
			tWriter.incIDVal();
			//Tupeleintrag für die Ranktabelle erstellen
			TupleWriter rankTWriter= this.getTWriter(absRankRelName);
			String rankRelName= rankTWriter.getRelName();
			long pre= this.getMaxID(absRankRelName, KW_ATT_PRE);
			long post= pre +1;
			//erzeuge das insert-Statement für die Datenbank
			String insertStmt2= 	"insert into "+ rankRelName +" " +
									"values(" 	+ pre + ", "
												+ post +", "
												+ newID + ", "
												+ this.getDBNULL() + ", " 
												+ this.getDBFALSE() + ");";
			//System.out.println("neues Statement: " + insertStmt2);
			statement.executeUpdate(insertStmt2);
		}
	}
	
	/**
	 * Macht ein Updated des Post-Wertes des ALL-Tupels in der Tabelle korpus. Der
	 * Post-Wert wird auf den übergebenen gesetzt. Das ALL-Tupel wird über das Constraint
	 * attName= allName gesucht.
	 * @param relName String - String Name der Tabelle in der das ALL-Element steht
	 * @param relName String - String Name der Tabelle in der die Structurverbindungsdaten stehen (rank) 
	 * @param post long - neuer zu setztender Postwert
	 * @throws Exception
	 */
	public void updateRankAll(	String absRankRelName,
								String absStructRelName,
								long post) throws Exception
	{
		String structRel= this.getTWriter(absStructRelName).getRelName();
		String rankRel= this.getTWriter(absRankRelName).getRelName();
		
		
		String stmt=	"UPDATE	" + rankRel + " " +
						"SET 		post="+ post+ " "+
						"WHERE 		"+KW_RANK_ATT_STREF +"= (SELECT id FROM "+structRel+" WHERE name ILIKE '"+ KW_STRUCT_ALL+ "')";
		//System.out.println("update statement2: "+ stmt);
		Statement statement = this.connection.createStatement();
		try { statement.executeUpdate(stmt); }
		catch (Exception e)
			{ throw new Exception(ERR_NO_SUPERROOT + e.getMessage());}
	}
	
	/**
	 * Macht ein Update des Post-Wertes des ALL-Tupels in der Tabelle korpus. Der
	 * Post-Wert wird auf den übergebenen gesetzt. Das ALL-Tupel wird über das Constraint
	 * attName= allName gesucht.
	 * @param absRelName String - String Name der Tabelle in der das ALL-Element geändert werden soll 
	 * @param post long - neuer zu setztender Postwert
	 * @throws Exception
	 */
	public void updateKorpusAll(	String absRelName,
									long post) throws Exception
	{
		String rankRel= this.getTWriter(absRelName).getRelName();
		
		String stmt=	"UPDATE	" + rankRel + " " +
						"SET 		post="+ post+ " "+
						"WHERE 		"+KW_KORPUS_ATT_NAME+"='" + KW_KORPUS_ALL+ "';";
		
		//System.out.println("update statement: "+ stmt);
		Statement statement = this.connection.createStatement();
		try { statement.executeUpdate(stmt); }
		catch (Exception e)
			{ throw new Exception(ERR_NO_SUPERROOT + e.getMessage());}
	}
	
	/**
	 * Macht ein Update des Post-Wertes des ALL-Tupels in der Tabelle collection. Der
	 * Post-Wert wird auf den übergebenen gesetzt. Das ALL-Tupel wird über das Constraint
	 * attName= allName gesucht.
	 * @param absRelName String - String Name der Tabelle in der das ALL-Element geändert werden soll 
	 * @param post long - neuer zu setztender Postwert
	 * @throws Exception
	 */
	public void updateColAll(	String colAbsRelName,
								String colRankAbsRelName,
								long post) throws Exception
	{
		String colRel= this.getTWriter(colAbsRelName).getRelName();
		String rankRel= this.getTWriter(colRankAbsRelName).getRelName();
		
		String subQuery= 	"SELECT	id"+ " "+
							"FROM "+ colRel+ " "+
							"WHERE "+ KW_KORPUS_ATT_NAME+ "='" + KW_KORPUS_ALL + "'";	
		
		String stmt=	"UPDATE	" + rankRel + " " +
						"SET 		post="+ post+ " "+
						"WHERE 		col_ref=( "+subQuery+" );";
		
		//System.out.println("update statement: "+ stmt);
		Statement statement = this.connection.createStatement();
		try { statement.executeUpdate(stmt); }
		catch (Exception e)
			{ throw new Exception(ERR_NO_SUPERROOT + e.getMessage());}
	}
	//--------------------------------- Ende neue Methoden  ---------------------------------
	/**
	 * Sucht die ID der Superwurzel in relANNIS und gibt diese zurück. Diese ID 
	 * repräsentiert das Element "Korpora", dem alle Korpus-Element unterstellt werden
	 * müssen. Existiert bisher keine Superwurzel wird ein Fehler zurückgegeben.
	 * 
	 * @param rootName String - Name des Superwurzel-Elementes ('Korpora')
	 * @param absElementRel String - abstrakter Name der Element-Relation (nach dem DDD_Modell)
	 * @param absRankRel String - abstrakter Name der Rank-Relation (nach dem DDD_Modell)
	 * @return ID der Superwurzel, -1, wenn keine Superwurzel vorhanden
	 * @exception Fehler, wenn keine Superwurzel existiert
	 */
	public long getSuperRootID(	String rootName,
								String absElementRel,
								String absRankRel) throws Exception
	{
		Long rootID= (long)0;
		
		String elementRel= this.getTWriter(absElementRel).getRelName();
		String rankRel= this.getTWriter(absRankRel).getRelName();
		String stmt= 	"SELECT 	e.id " +
						"FROM	"+ rankRel +" AS r, "+ elementRel +" AS e " +
						"WHERE	e.name= '"+ rootName +"' AND " +
						"e.id= r.element_id";
		
		//System.out.println("Statement: "+stmt);
		
		Statement statement = this.connection.createStatement();
		try 
		{
			ResultSet resultSet = statement.executeQuery (stmt);
			//old newer driver resultSet.first();
			resultSet.next();
			rootID= resultSet.getLong(1);
		}
		catch (Exception e)
			{
				return(-1);
				//throw new Exception(ERR_NO_SUPERROOT);}
			}
		
		return(rootID);
	}
	
	/**
	 * Sucht den Pre-Wert der Superwurzel in relANNIS und gibt diese zurück. Diese ID 
	 * repräsentiert das Element "Korpora", dem alle Korpus-Element unterstellt werden
	 * müssen. Existiert bisher keine Superwurzel wird ein Fehler zurückgegeben.
	 * 
	 * @param rootName String - Name des Superwurzel-Elementes ('Korpora')
	 * @param absElementRel String - abstrakter Name der Element-Relation (nach dem DDD_Modell)
	 * @param absRankRel String - abstrakter Name der Rank-Relation (nach dem DDD_Modell)
	 * @return ID der Superwurzel, -1, wenn keine Superwurzel vorhanden
	 * @exception Fehler, wenn keine Superwurzel existiert
	 */
	public long getSuperRootPre(	String rootName,
									String absElementRel,
									String absRankRel) throws Exception
	{
		Long rootPre= (long)0;
		
		String elementRel= this.getTWriter(absElementRel).getRelName();
		String rankRel= this.getTWriter(absRankRel).getRelName();
		String stmt= 	"SELECT 	r.pre " +
						"FROM		"+ rankRel +" AS r, "+ elementRel +" AS e " +
						"WHERE		e.name= '"+ rootName +"' AND " +
									"e.id= r.element_id";
		
		Statement statement = this.connection.createStatement();
		try 
		{
			ResultSet resultSet = statement.executeQuery (stmt);
			//old newer driver resultSet.first();
			resultSet.next();
			rootPre= resultSet.getLong(1);
		}
		catch (Exception e)
		{ //throw new Exception(ERR_NO_SUPERROOT);}
		return(-1); }
		
		return(rootPre);
	}
	
	/**
	 * Macht ein Updated des Post-Wertes der Super-Wurzel. 
	 * @param rootName
	 * @param absElementRel
	 * @param absRankRel
	 * @param post
	 * @throws Exception
	 */
	/*
	public void updateSuperRootPost(	String rootName,
										String absElementRel,
										String absRankRel,
										long post) throws Exception
	{
		String elementRel= this.getTWriter(absElementRel).getRelName();
		String rankRel= this.getTWriter(absRankRel).getRelName();
		
		String stmt=	"UPDATE	" + rankRel + " " +
						"SET 		post="+ post+ " "+
						"WHERE 		element_id= (SELECT id FROM "+elementRel+" WHERE name ILIKE '"+ rootName+ "')";
		
		Statement statement = this.connection.createStatement();
		try { statement.executeUpdate(stmt); }
		catch (Exception e)
			{ throw new Exception(ERR_NO_SUPERROOT + e.getMessage());}
	}*/
	
	
	/**
	 * Gibt einen noch nicht genutzten ID-Wert für die Relation mit dem übergebenen Namen 
	 * zurück. Dieser ID Wert ist der maximal vergebene in der Datenbank + 1. Das Attribut
	 * das den ID-Wert enthält muss den Namen ID tragen.
	 * @param relName String - Name der Relation, für die der IS-Wert gesucht wird
	 * @return neuer (noch nicht vergebener) ID-Wert
	 */
	/*
	public long getNewID(String relName) throws Exception
		{ return(this.getNewID(relName, KW_ID)); }
	*/
	
	
	/**
	 * Gibt einen neuen Pre- und Post-Orderwert für die gegebene Relation zurück. Der
	 * zurückgegebene Wert ist der Postwert des Wurzelelementes. Dieses wird gefunden
	 * durch den Constraint attName= rootName und muss eindeutig sein. 
	 * @param relName String - Relation, in der nach dem PP-Wert ggesucht werden soll
	 * @param attName String - Attributname in dem der Name der Wurzel steht
	 * @param rootName String - Name der Wurzel 
	 * @return postorderwert der Wurzel
	 * @throws Exception
	 */
	/*
	public long getMaxUsablePPVal(	String relName,
									String attName,
									String rootName) throws Exception
	{
		if (this.logger != null) this.logger.debug("search new pre- and post value for relation: "+ relName);
		//Verbindung aufbauen, wenn keine existiert
		if (this.connection == null) this.connect();
		long idVal= 0;
		
		Statement statement = this.connection.createStatement();
		//suche im Attribut Post
		String stmt= "select max(" + KW_ATT_POST +") from " + relName + ";";
		ResultSet resultSet = statement.executeQuery (stmt);
		resultSet.first();
		idVal= resultSet.getLong(1) + 1;
		
		return(idVal);
	}
	*/
	
	/**
	 * Gibt einen neuen Pre- und Post-Orderwert für die gegebene Relation zurück. Der
	 * zurückgegebene Wert ist noch nicht benutzt.
	 * @param relName String - Relation, in der nach dem PP-Wert ggesucht werden soll
	 * @return unverbrauchter Pre- und Post-Order-Wert
	 * @throws Exception
	 */
	/*
	public long getNewPPVal(String relName) throws Exception
	{
		if (this.logger != null) this.logger.debug("search new pre- and post value for relation: "+ relName);
		//Verbindung aufbauen, wenn keine existiert
		if (this.connection == null) this.connect();
		long idVal= 0;
		
		Statement statement = this.connection.createStatement();
		//suche im Attribut Post
		String stmt= "select max(" + KW_POST +") from " + relName + ";";
		ResultSet resultSet = statement.executeQuery (stmt);
		resultSet.first();
		idVal= resultSet.getLong(1) + 1;
		
		return(idVal);
	}
	*/
	
	/**
	 * Öffnet die Verbindung zu der DB, die initialiisert wurde.
	 * @exception Fehler, wenn DB-Parameter nicht initielisiert wurden.
	 */
	public void connect() throws Exception
	{
		this.checkParameter();
		try
		{
			//suche db trieber
			if (this.logger != null) this.logger.info("load jdbc driver...");
			Class.forName(this.dbParams.get(KW_D));
			if (this.logger != null) this.logger.info(MSG_OK);
			
			//öffne Verbindung
			if (this.logger != null) this.logger.info("open connection...");
			String pw= this.dbParams.get(KW_PASS); 
			if ((pw == null) || (pw.equalsIgnoreCase("")))
			{
				this.connection = DriverManager.getConnection (getUrl(),this.dbParams.get(KW_U), null);
			}
			else
			{
				this.connection = DriverManager.getConnection (getUrl(),this.dbParams.get(KW_U), this.dbParams.get(KW_PASS));
			}
			if (this.logger != null) this.logger.info(MSG_OK);
			
		}
		catch (Exception e)
		{
			if (this.logger != null) this.logger.info(MSG_FAILED);
			throw e;
		}
	}
	
	/**
	 * Erzeugt eine Liste von Tuplewritern, die der Reihenfolge in der Liste nach in die
	 * Datenbank geschrieben werden dürfen. Diese Methode ist nur für den DEBUG-Fall und
	 * das Ermitteln der Reihenfolge muss aus der XML-Datei zu lesen sein.
	 * @return Liste von Tuplewritern mit der Reihenfolge der Tuplewritern
	 */
	private Vector<TupleWriter> createFlushOrder() throws Exception
	{
		Vector<TupleWriter> retList= new Vector<TupleWriter>();
		retList.add(this.getTWriter("korpDataRel"));
		retList.add(this.getTWriter("DocDataRel"));
		retList.add(this.getTWriter("DocCorpDataRel"));
		retList.add(this.getTWriter("ColDataRel"));
		retList.add(this.getTWriter("ColRankDataRel"));
		retList.add(this.getTWriter("ColAnnoDataRel"));
		retList.add(this.getTWriter("primDataRel"));
		retList.add(this.getTWriter("StructDataRel"));
		retList.add(this.getTWriter("StructRankRel"));
		retList.add(this.getTWriter("EdgeAnnoRel"));
		retList.add(this.getTWriter("AnnoDataRel"));
		retList.add(this.getTWriter("AnnoAttDataRel"));
		retList.add(this.getTWriter("ExtFileDataRel"));
		return(retList);
	}
	
	/**
	 * Schreibt den Inhalt aller TupleWriter in die entsprechenden Datenbank-Relationen. 
	 * Die Daten werden zunächst in eine Datei geschrieben und dann per BULK-Upload in 
	 * die Datenbank eingetragen.  
	 * @throws Exception
	 */
	public void flushAll() throws Exception
	{
		//TupleWriter in die Dateien schreiben
		Vector<TupleWriter> elements= this.createFlushOrder();
		String relName= null;		//Name der Relation, in die geladen werden soll
		String fileName= null;		//Name der Datei aus der geladen werden soll
		String stmt= null;			//zu erstellendes Lade-Statement
		//tWriter in Dateien flushen
		for(TupleWriter tWriter: elements)
		{
			if (this.logger != null) this.logger.info("flushing data into file '"+tWriter.getFileName()+"'...");
			
			try
			{
				//schreibe TupleWriter in Datei
				tWriter.flush();
			}
			catch (Exception e)
			{
				if (this.logger != null) this.logger.info("FAILED");
				throw e;
			}
			if (this.logger != null) this.logger.info("OK");
		}
		//Dateien in die Datenbank schreiben
		for(TupleWriter tWriter: elements)
		{
			if (this.logger != null) this.logger.info("flushing file '"+tWriter.getFileName()+"' into table '"+tWriter.getRelName()+"'...");
			
			//lade Datei in Datenbank
			//ermittle DB-Relation
			relName= tWriter.getRelName();
			fileName= tWriter.getPath() + "/" + tWriter.getFileName();
			if (tWriter.getClass().equals(BLOBTupleWriter.class))
			{
				Runtime.getRuntime().exec("psql -d relANNIS -U relANNIS");
				/*
				stmt= "\\i " + fileName.replace("\\", "/");
				System.out.println(stmt);
				stmt= "\\q";
				Statement statement = this.connection.createStatement();
				try
				{
					statement.executeQuery(stmt);
				}
				catch (SQLException sqlEx)
				{
					sqlEx.printStackTrace();
					if (this.logger != null) this.logger.info("FAILED");
					throw new Exception(ERR_SQL_EX + relName + ". The error message from db: " + sqlEx.getMessage());
				}
				*/
			}
			else
			{
				stmt= this.genBULKLoadStmt(relName, fileName);
				Statement statement = this.connection.createStatement();
				try
				{
					statement.executeUpdate(stmt);
				}
				catch (SQLException sqlEx)
				{
					if (this.logger != null) this.logger.info("FAILED");
					throw new Exception(ERR_SQL_EX + relName + ". The error message from db: " + sqlEx.getMessage());
				}
			}
			if (this.logger != null) this.logger.info("OK");
		}
	}
	
	/**
	 * Schreibt die Daten aus der DB in ein AUsgabeverzeichniss zurück.
	 * @param dstFolder
	 */
	private void flushBack(File dstFolder) throws Exception
	{
		//TupleWriter in die Dateien schreiben
		Vector<TupleWriter> elements= this.createFlushOrder();
		
		//Liste aller Relationen erstellen
		Vector<String> relNames= new Vector<String>();
		for(TupleWriter tWriter: elements)
		{
			if (!relNames.contains(tWriter.getRelName()))
					relNames.add(tWriter.getRelName());
		}
		
		//Jede Relation in eine Datei Flushen
		String fileName= "";
		String stmt= "";
		for (String relName: relNames)
		{
			fileName= dstFolder.getAbsolutePath()+ "/" + relName + ".tab"; 
			stmt= genBULKDownLoadStmt(relName, fileName);
			if (this.logger != null) this.logger.info("flushing table table '"+relName+" into file '"+fileName+"'...");
			Statement statement = this.connection.createStatement();
			try
			{
				statement.executeUpdate(stmt);
			}
			catch (SQLException sqlEx)
			{
				if (this.logger != null) this.logger.info("FAILED");
				throw new Exception(ERR_SQL_EX + relName + ". The error message from db: " + sqlEx.getMessage());
			}
		}
		
	}
	
	/**
	 * Schließt die Datenbankverbindung und schreibt den Inhalt aller TupleWriter in 
	 * die entsprechenden Datenbank-Relationen. Die Daten werden zunächst in eine Datei
	 * geschrieben und dann per BULK-Upload in die Datenbank eingetragen.  
	 * @throws Exception
	 */
	public void close() throws Exception
	{
		try
		{
			//alle TupleWriterschreiben
			if (this.logger != null) this.logger.info("flush all tuple writer...");
			this.flushAll();
			if (this.logger != null) this.logger.info(MSG_OK);
			
			if (this.logger != null) this.logger.info("flush back all tuple writer...");
			this.flushBack(this.dstFolder);
			if (this.logger != null) this.logger.info(MSG_OK);
			
			//Datenbankverbindung schließen		
			if (this.logger != null) this.logger.info("close database connection...");
			connection.close ();
			if (this.logger != null) this.logger.info(MSG_OK);
			
			//temporäre Dateien löschen
			if (this.logger != null) this.logger.info("clear temprorary files...");
			this.tWriters= null;
			if (this.logger != null) this.logger.info(MSG_OK);
		}
		catch (Exception e)
		{
			if (this.logger != null) this.logger.info(MSG_FAILED);
			throw new Exception(e.getMessage());
		}
	}
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		
		try 
		{
			this.checkParameter();
			retStr= retStr + "driver: " + this.dbParams.get(KW_D) + ",\t";
			retStr= retStr + "host: " + this.dbParams.get(KW_H) + ",\t";
			retStr= retStr + "port: " + this.dbParams.get(KW_P) + ",\t";
			retStr= retStr + "database: " + this.dbParams.get(KW_DB) + ",\t";
			retStr= retStr + "user: " + this.dbParams.get(KW_U) + ",\t";
			retStr= retStr + "password: " + this.dbParams.get(KW_PASS) + ",\t";
		}
		catch (Exception e)
			{retStr= MSG_NOT_INIT; }
		
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	


}
