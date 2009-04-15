package annisservice.administration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.Transactional;

import annisservice.administration.exceptions.AdministrationException;
import annisservice.administration.exceptions.FileAccessException;

public class CorpusAdministration {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired private AnnisDatabaseUtils databaseUtils;
	
	public void initializeDatabase(String host, String port, String database, String user, String password, String defaultDatabase, String superUser, String superPassword) {
		log.info("Creating Annis database and user.");
		
		// connect as super user to the default database to create new user and database
		databaseUtils.setDataSource(createDataSource(host, port, defaultDatabase, superUser, superPassword));
		databaseUtils.dropDatabase(database);
		databaseUtils.dropUser(user);
		databaseUtils.createUser(user, password);
		databaseUtils.createDatabase(database);
	
		// switch to new database, but still as super user to install stored procedure compute_rank_level
		databaseUtils.setDataSource(createDataSource(host, port, database, superUser, superPassword));
		databaseUtils.installPlPython();
		databaseUtils.createFunctionComputeRankLevel();
		
		// switch to new database as new user for the rest
		databaseUtils.setDataSource(createDataSource(host, port, database, user, password));
		databaseUtils.createSchema();
		databaseUtils.populateSchema();
		databaseUtils.createMaterializedTables();
		
		// write database information to property file
		writeDatabasePropertiesFile(host, port, database, user, password);
	}
	
	@Transactional(readOnly = false)
	public void importCorpora(List<String> paths) {
		// first drop indexes on source tables
		databaseUtils.dropIndexes();
		
		// then import each corpus
		for (String path : paths) {
			log.info("Importing corpus from: " + path);
			
			databaseUtils.createStagingArea();
			databaseUtils.bulkImport(path);
			databaseUtils.importBinaryData(path);
			databaseUtils.computeLeftTokenRightToken();
			databaseUtils.computeLevel();
			databaseUtils.computeComponents();
			databaseUtils.computeCorpusStatistics();
			databaseUtils.updateIds();
			databaseUtils.applyConstraints();
			databaseUtils.insertCorpus();
			databaseUtils.dropStagingArea();
		}
		
		// finally rebuild materialized tables and indexes
		log.info("Rebuilding materialized tables and indexes.");
		databaseUtils.dropMaterializedTables();
		databaseUtils.createMaterializedTables();
		databaseUtils.rebuildIndexes();
	}
	
	public void importCorpora(String... paths) {
		importCorpora(Arrays.asList(paths));
	}
	
	@Transactional(readOnly = false)
	public void deleteCorpora(List<Long> ids) {
		// check if corpus exists
		List<Long> corpora = databaseUtils.listToplevelCorpora();
		for (long id : ids)
			if ( ! corpora.contains(id) )
				throw new AdministrationException("Corpus does not exist (or is not a top-level corpus): " + id);
		
		log.info("Deleting corpora: " + ids);
		databaseUtils.deleteCorpora(ids);
	}
	
	public List<Map<String, Object>> listCorpusStats() {
		return databaseUtils.listCorpusStats();
	}
	
	public List<Map<String, Object>> listTableStats() {
		return databaseUtils.listTableStats();
	}

	public List<String> listUsedIndexes() {
		return databaseUtils.listUsedIndexes();
	}

	public List<String> listUnusedIndexes() {
		return databaseUtils.listUnusedIndexes();
	}
	
	///// Helper
	
	private void writeDatabasePropertiesFile(String host, String port,
			String database, String user, String password) {
		File file = new File(System.getProperty("annis.home") + "/conf", "database.properties");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write("# database configuration\n");
			writer.write("datasource.driver=org.postgresql.Driver\n");
			writer.write("datasource.url=jdbc:postgresql://" + host + ":" + port + "/" + database + "\n");
			writer.write("datasource.username=" + user + "\n");
			writer.write("datasource.password=" + password + "\n");
			writer.close();
		} catch (IOException e) {
			log.error("Couldn't write database properties file", e);
			throw new FileAccessException(e);
		}
		log.info("Wrote database configuration to " + file.getAbsolutePath());
	}
	

	private DataSource createDataSource(String host, String port, String database, String user, String password) {
		String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
		return new DriverManagerDataSource("org.postgresql.Driver", url, user, password);
	}
	
	///// Getter / Setter

	public AnnisDatabaseUtils getDatabaseUtils() {
		return databaseUtils;
	}

	public void setDatabaseUtils(AnnisDatabaseUtils databaseUtils) {
		this.databaseUtils = databaseUtils;
	}

}
