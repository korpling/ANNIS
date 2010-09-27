package annis.administration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.annotation.Transactional;

import annis.AnnisRunnerException;
import org.apache.log4j.Logger;

public class CorpusAdministration {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired private SpringAnnisAdministrationDao administrationDao;
	
	public void initializeDatabase(String host, String port, String database, String user, String password, String defaultDatabase, String superUser, String superPassword) {
		log.info("Creating Annis database and user.");
		
		// connect as super user to the default database to create new user and database
		administrationDao.setDataSource(createDataSource(host, port, defaultDatabase, superUser, superPassword));
		administrationDao.dropDatabase(database);
		administrationDao.dropUser(user);
		administrationDao.createUser(user, password);
		administrationDao.createDatabase(database);
	
		// switch to new database, but still as super user to install stored procedure compute_rank_level
		administrationDao.setDataSource(createDataSource(host, port, database, superUser, superPassword));
		administrationDao.installPlPgSql();
		administrationDao.createFunctionComputeRankLevel();
		administrationDao.createFunctionComputeSpannedTokens();
		administrationDao.createFunctionUniqueToplevelCorpusName();
		
		// switch to new database as new user for the rest
		administrationDao.setDataSource(createDataSource(host, port, database, user, password));
		administrationDao.createSchema();
		administrationDao.populateSchema();
    administrationDao.rebuildIndexes();
		
		// write database information to property file
		writeDatabasePropertiesFile(host, port, database, user, password);
	}
	
	@Transactional(readOnly = false)
	public void importCorpora(boolean temporaryStagingArea, List<String> paths) {
		
		// import each corpus
		for (String path : paths) {
			log.info("Importing corpus from: " + path);
			
			administrationDao.createStagingArea(temporaryStagingArea);
			administrationDao.bulkImport(path);
			administrationDao.computeTopLevelCorpus();
  
			long corpusID = administrationDao.updateIds();
      
			administrationDao.importBinaryData(path);
      
      administrationDao.createStagingAreaIndexes();
      administrationDao.analyzeStagingTables();
      
//			// finish transaction here to debug computation of left|right-token
			//if (true) return;
			administrationDao.computeLeftTokenRightToken();
			administrationDao.computeLevel();
			administrationDao.computeCorpusStatistics();
      administrationDao.updateCorpusStatsId(corpusID);

			administrationDao.applyConstraints();
      administrationDao.analyzeStagingTables();

			administrationDao.insertCorpus();
      // create the new facts table partition
      administrationDao.createFacts(corpusID);

			administrationDao.dropStagingArea();

      administrationDao.analyzeFacts(corpusID);

      log.info("Finished import from: " + path);
		}
	}
	
	public void importCorpora(boolean temporaryStagingArea, String... paths) {
		importCorpora(temporaryStagingArea, Arrays.asList(paths));
	}
	
	@Transactional(readOnly = false)
	public void deleteCorpora(List<Long> ids) {
		// check if corpus exists
		List<Long> corpora = administrationDao.listToplevelCorpora();
		for (long id : ids)
			if ( ! corpora.contains(id) )
				throw new AnnisRunnerException("Corpus does not exist (or is not a top-level corpus): " + id);
		
		log.info("Deleting corpora: " + ids);
		administrationDao.deleteCorpora(ids);
    log.info("Finished deleting corpora: " + ids);
	}
	
	public List<Map<String, Object>> listCorpusStats() {
		return administrationDao.listCorpusStats();
	}
	
	public List<Map<String, Object>> listTableStats() {
		return administrationDao.listTableStats();
	}

	public List<String> listUsedIndexes() {
		return administrationDao.listUsedIndexes();
	}

	public List<String> listUnusedIndexes() {
		return administrationDao.listUnusedIndexes();
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

		// DriverManagerDataSource is deprecated
		// return new DriverManagerDataSource("org.postgresql.Driver", url, user, password);

		// why is this better?
		// XXX: how to construct the datasource?    
		return new SimpleDriverDataSource(new Driver(), url, user, password);
	}
	
	///// Getter / Setter

	public SpringAnnisAdministrationDao getAdministrationDao() {
		return administrationDao;
	}

	public void setAdministrationDao(SpringAnnisAdministrationDao databaseUtils) {
		this.administrationDao = databaseUtils;
	}

}
