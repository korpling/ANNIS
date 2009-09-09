package annis.resolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.ext.DefaultHandler2;



/**
 * This class provides objects that create a connection to a data source
 * for getting information about visualization for ANNIS 2.0. It connects
 * and disconnects to data source and gets a vizualization type for a
 * given corpus name and a given annotation name.
 * 
 * @author Florian Zipser
 *
 */
public class AnnisVizDAO implements SettingObject
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"ANNISVizDAO";		//Name dieses Tools
	/**
	 * Keywords for database connection
	 */
	private static final String KW_D=		"driver";					//Keyword f�r driver
	private static final String KW_H=		"host";						//Keyword f�r host
	private static final String KW_P=		"port";						//Keyword f�r port
	private static final String KW_DB=		"database";					//Keyword f�r database
	private static final String KW_U=		"user";						//Keyword f�r user
	private static final String KW_PASS=	"password";	
	
	/**
	 * Map wich contains all database parameter
	 */
	private Map<String, String> dbParams= null;		
	
	/**
	 * current database connection
	 */
	private Connection connection = null;	
	
	/**
	 * logger for log4j;
	 */
	Logger logger= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "): ";
	private static final String MSG_OK=				"OK";
	private static final String MSG_FAILED=			"FAILED";
	
	private static final String FILE_SETFILE=		"settings/settings_resolver_local.xml";
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_INIT=				MSG_STD +"DAO is initialized...............";
	private static final String MSG_OPEN=				MSG_STD +"opening data-source connection...";
	private static final String MSG_CLOSE=				MSG_STD +"closing data-source connection...";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_VAL_NOT_GIVEN=			MSG_ERR + "The following value wasn�t given: ";
	private static final String ERR_DB_NOT_OPEN=			MSG_ERR + "The data source connection must be opened first.";
	private static final String ERR_TOO_MUCH_VIZTYPE=		MSG_ERR + "Incorrect data. There are more than one types to the given corpus and annotation: ";
	private static final String	ERR_NO_MATCHING_TYPE=		MSG_ERR + "There is no visualization type in database for the values (corpus_id/ annoLevel): ";
	private static final String	ERR_VIZTYPE_EXISTS=			MSG_ERR + "The given visualization type already exists in datrabase: ";
	private static final String	ERR_VIZTYPE_NOT_EXISTS=		MSG_ERR + "The given visualization type does not exists in datrabase: ";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Erzeugt ein Objekt vom Typ DBConnector. Die Zugangsdaten f�r die Datenbankverbindung
	 * m�ssen Seperat gesetzt werden. 
	 * @param logger Logger - Logger zum loggen von Nachrichten
	 */
	public AnnisVizDAO(Logger logger) throws Exception
	{
		//Initialzies all needed objects.
		this.init();
		this.logger= logger;
		if (this.logger!= null) this.logger.info(MSG_INIT);
	}
//	 ============================================== private Methoden ==============================================
	/**
	 * Initialzies all needed objects.
	 */
	private void init() throws Exception
	{			
		//Zuordnungstabelle f�r DB-Parameter erstellen
		this.dbParams= new Hashtable<String, String>();
		//create setting object
		SettingMgr setMgr= new SettingMgr(FILE_SETFILE);
		setMgr.addSetListener(this);
		setMgr.start();
	}
	
	/**
	 * Checks if all database params are set.
	 * @exception Exception, if database params aren�t set
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
//	 ============================================== �ffentliche Methoden ==============================================
//	---------------------------- Implementierungen von SettingObject ----------------------------	
	/**
	 * @see util.SettingMgr.SettingObject#getSetEntry()
	 */
	public Hashtable<DefaultHandler2, Vector<String>> getSetEntry() throws Exception
	{
		Hashtable<DefaultHandler2, Vector<String>> table= new Hashtable<DefaultHandler2, Vector<String>>();
		//Reader f�r DB-Parameter erstellen
		DBParamReader pReader= new DBParamReader(this.dbParams);
		//Liste der SettingNames und SettingListener in Tabelle schreiben
		table.put(pReader, DBParamReader.getSetNames());
		return(table);
	}
	
	/**
	 * @see util.SettingMgr.SettingObject#readSettings()
	 */
	public void readSettings() throws Exception
	{
		
	}
//	---------------------------- Ende Implementierungen von SettingObject ----------------------------
	
	/**
	 * Opens database connection if and only if parameters are set.
	 * @exception Fehler, wenn DB-Parameter nicht initielisiert wurden.
	 */
	public void open() throws Exception
	{
		if (this.logger!= null) this.logger.info(MSG_OPEN);
		this.checkParameter();
		try
		{
			//suche db trieber
			if (this.logger != null) this.logger.info("load jdbc driver...");
			Class.forName(this.dbParams.get(KW_D));
			if (this.logger != null) this.logger.info(MSG_OK);
			
			//�ffne Verbindung
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
	 * Closes database connection
	 */
	public void close() throws Exception
	{
		if (this.logger!= null) this.logger.info(MSG_CLOSE);
		try
		{
			if (this.connection== null) throw new Exception(ERR_DB_NOT_OPEN);
			//Datenbankverbindung schlie�en		
			if (this.logger != null) this.logger.info("close database connection...");
			this.connection.close ();
			if (this.logger != null) this.logger.info(MSG_OK);
		}
		catch (Exception e)
		{
			if (this.logger != null) this.logger.info(MSG_FAILED);
			throw e;
		}
	}
	
	/**
	 * Returns if the given visualization type already exists in database. 
	 * @param vizType String - the value which should be checked.
	 * @return true if given visualization type already exists
	 */
	public boolean checkVizType(String vizType) throws Exception
	{
		boolean retVal= false;
		String chkStmt= 	"SELECT count(type) FROM viz_type WHERE type='"+vizType+"';";
		Statement statement = this.connection.createStatement();
		ResultSet resultSetCount= statement.executeQuery (chkStmt);
		resultSetCount.next();
		int num= resultSetCount.getInt(1);
		if (num > 0)
		{
			retVal= true;
		}
		
		return(retVal);
	}
	
	/**
	 * Inserts a new visualization type value into database. The methode
	 * checks if the given value already exists.
	 * @param vizType String - the new value which should be inserted.
	 * @exception throws an error if the given value already exists
	 */
	public void insertVizType(String vizType) throws Exception
	{
		//Pr�fe ob Typ bereits vorhanden
		if (checkVizType(vizType))
			throw new Exception(ERR_VIZTYPE_EXISTS + vizType);	
		
		//h�chste vergebene id ermitteln
		String maxStmt= "select max(id) from viz_type";
		Statement statement = this.connection.createStatement();
		ResultSet resultSetCount= statement.executeQuery(maxStmt);
		resultSetCount.next();
		//neue nichtvergebene id ermitteln
		int num= resultSetCount.getInt(1) + 1;
		
		//vizType einf�gen
		String insertStmt= "insert into viz_type values("+ num +", '"+ vizType +"')";
		statement = this.connection.createStatement();
		statement.executeUpdate(insertStmt);
	}
	
	/**
	 * Removes a new visualization type value into database. The methode
	 * checks if the given value already exists.
	 * @param vizType String - the new value which should be inserted.
	 * @exception throws an error if the given value does not exists
	 */
	public void remVizType(String vizType) throws Exception
	{
		//Pr�fe ob Typ bereits vorhanden
		if (!checkVizType(vizType))
			throw new Exception(ERR_VIZTYPE_NOT_EXISTS + vizType);
		
		//vizType entfernen
		String remStmt= "delete from viz_type where type ILIKE '"+ vizType +"'";
		Statement remStatement = this.connection.createStatement();
		remStatement.executeUpdate(remStmt);
	}
	
	/**
	 * This method returns a visualization type. The visualization type is taken
	 * from data base and is identified by unique identifier for corpus (corpus_ID) 
	 * and annotation level (annoLevel).
	 * @param corpusID Long - unique identifier for a corpus
	 * @param annoLevel String - Name of the annotation level
	 * @return visualization type
	 * @throws Exception
	 */
	public String getVizType(	Long corpusID, 
								String annoLevel) throws Exception
	{
		String retStr= null;
		
		String stmt=	"SELECT 	distinct type "+
						"FROM 	viz_type as t, corp_2_viz as c "+ 
						"WHERE	c.corpus_id = '"+corpusID+"' 	AND "+ 
						"		c.level ILIKE '"+annoLevel+"'	AND "+ 
						"		c.type_ref= t.id;";
		
		String cStmt=	"SELECT count(distinct type) "+
						"FROM 	viz_type as t, corp_2_viz as c "+ 
						"WHERE	c.corpus_id = '"+corpusID+"' 	AND "+ 
						"		c.level ILIKE '"+annoLevel+"'	AND "+ 
						"		c.type_ref= t.id;";
						
						/*				"		c.annotation IS NULL;";
		String stmt= 	"SELECT 	type "+
						"FROM 	viz_type as t, corp_2_viz as c " +
						"WHERE	c.corp_id ILIKE '"+corpusID+"'AND " +
						"		c.level ILIKE '"+annoName+"'	AND "+
						"		c.type_ref= t.id;" ;
		
		String cStmt= 	"SELECT 	count(type) "+
						"FROM 	viz_type as t, corp_2_viz as c " +
						"WHERE	c.corpus_id ILIKE '"+corpusID+"'AND " +
						"		c.annotation ILIKE '"+annoName+"'	AND "+
						"		c.type_ref= t.id;" ;
		*/
		/*
		System.out.println("statement:");
		System.out.println(stmt);
		System.out.println("cStatement:");
		System.out.println(cStmt);
		*/
		
		Statement statement = this.connection.createStatement();
		try 
		{
			ResultSet resultSetCount= statement.executeQuery (cStmt);
			resultSetCount.next();
			int num= resultSetCount.getInt(1);
			if (num > 0)
			{
				ResultSet resultSet = statement.executeQuery (stmt);
				if (num > 1) throw new Exception(ERR_TOO_MUCH_VIZTYPE + corpusID+" / "+ annoLevel);
				resultSet.next();
				retStr= resultSet.getString(1);
			}
			else throw new Exception(ERR_NO_MATCHING_TYPE + corpusID + "/" + annoLevel);
		}
		catch (Exception e)
		{ 
			throw e;
		}
		return(retStr);
	}
	
	/**
	 * This method returns a visualization type. The visualization type is taken
	 * from data base and is identified by unique identifier for corpus (corpus_ID) 
	 * and annotation level (annoLevel).
	 * @param corpusID Long - unique identifier for a corpus
	 * @param annoLevel String - name of the annotation level
	 * @param annotation String - name of the annotation
	 * @return visualization type
	 * @throws Exception
	 */
	public String getVizType(	Long corpusID, 
								String annoLevel,
								String annotation) throws Exception
	{
		String retStr= null;
		String stmt=	"SELECT 	distinct type "+
						"FROM 	viz_type as t, xcorp_2_viz as c "+ 
						"WHERE	c.corpus_id = '"+corpusID+"' 	AND "+ 
						"		c.level ILIKE '"+annoLevel+"'	AND "+ 
						"		c.annotation ILIKE '"+annotation+"'  AND " +
						"		c.type_ref= t.id;";
		
		String cStmt=	"SELECT count(distinct type) "+
						"FROM 	viz_type as t, xcorp_2_viz as c "+ 
						"WHERE	c.corpus_id = '"+corpusID+"' 	AND "+ 
						"		c.level ILIKE '"+annoLevel+"'	AND "+
						"		c.annotation ILIKE '"+annotation+"'  AND " +
						"		c.type_ref= t.id;";
		
		Statement statement = this.connection.createStatement();
		try 
		{
			ResultSet resultSetCount= statement.executeQuery (cStmt);
			resultSetCount.next();
			int num= resultSetCount.getInt(1);
			if (num > 0)
			{
				ResultSet resultSet = statement.executeQuery (stmt);
				if (num > 1) throw new Exception(ERR_TOO_MUCH_VIZTYPE + corpusID+" / "+ annoLevel);
				resultSet.next();
				retStr= resultSet.getString(1);
			}
			else throw new Exception(ERR_NO_MATCHING_TYPE + corpusID + "/" + annoLevel);
		}
		catch (Exception e)
		{ 
			throw e;
		}
		return(retStr);
	}
	
	
}
