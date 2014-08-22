/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.administration;

import annis.dao.AnnisDao;
import annis.dao.autogenqueries.QueriesGenerator;
import annis.examplequeries.ExampleQuery;
import annis.exceptions.AnnisException;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.security.UserConfig;
import annis.utils.DynamicDataSource;
import com.google.common.base.Preconditions;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.DelegatingConnection;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.postgresql.PGConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * - Transaktionen - Datenbank-Zugriffsrechte für verschiedene Methoden -
 * Reihenfolge der Aufrufe - Skripte in $ANNIS_HOME/scripts
 */
// FIXME: nothing in SpringAnnisAdministrationDao is tested
public class DefaultAdministrationDao implements AdministrationDao
{

  private static final Logger log = LoggerFactory.getLogger(
    AdministrationDao.class);

  // external files path
  private String externalFilesPath;

  // script path
  private String scriptPath;

  // use Spring's JDBC support
  private JdbcTemplate jdbcTemplate;

  // if this is true, the staging area is not deleted
  private boolean temporaryStagingArea;

  private StatementController statementController;

  private DynamicDataSource dataSource;

  /**
   * Searches for textes which are empty or only contains whitespaces. If that
   * is the case the visualizer and no document visualizer are defined in the
   * corpus properties file a new file is created and stores a new config which
   * disables document browsing.
   *
   *
   * @param corpusID The id of the corpus which texts are analyzed.
   */
  private void analyzeTextTable(String toplevelCorpusName)
  {
    List<String> rawTexts = annisDao.getRawText(toplevelCorpusName);

    // pattern for checking the token layer
    final Pattern WHITESPACE_MATCHER = Pattern.compile("^\\s+$");

    for (String s : rawTexts)
    {

      if (s != null && WHITESPACE_MATCHER.matcher(s).matches())
      {
        // deactivate doc browsing if no document browser configuration is exists
        if (annisDao.getDocBrowserConfiguration(toplevelCorpusName) == null)
        {
          // should exists anyway
          Properties corpusConf;
          try
          {
            corpusConf = annisDao.getCorpusConfiguration(toplevelCorpusName);
          }
          catch (FileNotFoundException ex)
          {
            log.error(
              "not found a corpus configuration, so skip analyzing the text table",
              ex);
            return;
          }

          // disable document browsing if it is not explicit switch on by the
          // user in the corpus.properties
          boolean hasKey = corpusConf.containsKey("browse-documents");
          boolean isActive = Boolean.parseBoolean(corpusConf.getProperty(
            "browse-documents"));

          if (!(hasKey && isActive))
          {
            log.info("disable document browser");
            corpusConf.put("browse-documents", "false");
            annisDao.setCorpusConfiguration(toplevelCorpusName, corpusConf);
          }

          // once disabled don't search in further texts
          return;
        }
      }
    }
  }

  @Override
  public ImportStatus initImportStatus()
  {
    return new CorpusAdministration.ImportStatsImpl();
  }

  public enum EXAMPLE_QUERIES_CONFIG
  {

    IF_MISSING, TRUE, FALSE

  }
  /**
   * If this is true and no example_queries.tab is found, automatic queries are
   * generated.
   */
  private EXAMPLE_QUERIES_CONFIG generateExampleQueries;

  private String schemaVersion;

  /**
   * A mapping for file-endings to mime types.
   */
  private Map<String, String> mimeTypeMapping;

  private Map<String, String> tableInsertSelect;

  private Map<String, String> tableInsertFrom;

  // all files have to carry this suffix.
  private static final String REL_ANNIS_FILE_SUFFIX = ".tab";

  /**
   * Optional tab for example queries. If this tab not exist, a dummy file from
   * the resource folder is used.
   */
  private static final String EXAMPLE_QUERIES_TAB = "example_queries";

  /**
   * The name of the file and the relation containing the resolver information.
   */
  private static final String FILE_RESOLVER_VIS_MAP = "resolver_vis_map";
  // tables imported from bulk files
  // DO NOT CHANGE THE ORDER OF THIS LIST!  Doing so may cause foreign key failures during import.

  private String[] importedTables =
  {
    "corpus", "corpus_annotation",
    "text", "node", "node_annotation",
    "component", "rank", "edge_annotation",
    FILE_RESOLVER_VIS_MAP, EXAMPLE_QUERIES_TAB
  };

  private String[] tablesToCopyManually =
  {
    "corpus", "corpus_annotation",
    "text",
    FILE_RESOLVER_VIS_MAP,
    EXAMPLE_QUERIES_TAB,
    "corpus_stats",
    "media_files"
  };
  // tables created during import

  private String[] createdTables =
  {
    "corpus_stats",
    "media_files"
  };

  private AnnisDao annisDao;

  private ObjectMapper jsonMapper = new ObjectMapper();

  private QueriesGenerator queriesGenerator;

  /**
   * Called when Spring configuration finished
   */
  public void init()
  {
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
    jsonMapper.setAnnotationIntrospector(introspector);
    // the json should be as compact as possible in the database
    jsonMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT,
      false);
  }

  ///// Subtasks of creating the database
  protected void dropDatabase(String database)
  {

    log.debug("dropping possible existing database");
    jdbcTemplate.execute("DROP DATABASE IF EXISTS " + database);

  }

  protected void dropUser(String username)
  {
    log.debug("dropping possible existing user");
    jdbcTemplate.execute("DROP USER IF EXISTS " + username);

  }

  protected void createUser(String username, String password)
  {
    log.info("creating user: " + username);
    jdbcTemplate.execute("CREATE USER " + username + " PASSWORD '" + password
      + "'");
  }

  protected void createDatabase(String database, String owner)
  {
    log.info("creating database: " + database
      + " OWNER " + owner + " ENCODING = 'UTF8'");
    jdbcTemplate.execute("CREATE DATABASE " + database
      + " OWNER " + owner + " ENCODING = 'UTF8'");
  }

  protected void installPlPgSql()
  {
    log.info("installing stored procedure language plpgsql");
    try
    {
      jdbcTemplate.execute("CREATE LANGUAGE plpgsql");
    }
    catch (Exception ex)
    {
      log.warn("plpqsql was already installed: " + ex.getMessage());
    }
  }

  protected void createFunctionUniqueToplevelCorpusName()
  {
    log.info("creating trigger function: unique_toplevel_corpus_name");
    executeSqlFromScript("unique_toplevel_corpus_name.sql");
  }

  protected void createSchema()
  {
    log.info("creating ANNIS database schema (" + getSchemaVersion() + ")");
    executeSqlFromScript("schema.sql");

    // update schema version
    jdbcTemplate.execute(
      "DELETE FROM repository_metadata WHERE \"name\"='schema-version'");

    jdbcTemplate.execute("INSERT INTO repository_metadata "
      + "VALUES ('schema-version', '"
      + StringUtils.replace(getSchemaVersion(), "'", "''") + "');");

  }

  protected void createSchemaIndexes()
  {
    log.info(
      "creating ANNIS database schema indexes (" + getDatabaseSchemaVersion() + ")");
    executeSqlFromScript("schemaindex.sql");
  }

  protected void populateSchema()
  {
    log.info("populating the schemas with default values");
    bulkloadTableFromResource("resolver_vis_map",
      new FileSystemResource(new File(scriptPath,
          FILE_RESOLVER_VIS_MAP + REL_ANNIS_FILE_SUFFIX)));
    // update the sequence
    executeSqlFromScript("update_resolver_sequence.sql");

    log.info(
      "creating immutable functions for extracting annotations");
    executeSqlFromScript("functions_get.sql");
  }

  /**
   * Get the real schema name and version as used by the database.
   *
   * @return
   */
  @Override
  @Transactional(readOnly = true, propagation = Propagation.NESTED)
  public String getDatabaseSchemaVersion()
  {
    try
    {

      List<Map<String, Object>> result = jdbcTemplate.
        queryForList(
          "SELECT \"value\" FROM repository_metadata WHERE \"name\"='schema-version'");

      String schema
        = result.size() > 0 ? (String) result.get(0).get("value") : "";
      return schema;
    }
    catch (DataAccessException ex)
    {
      String error = "Wrong database schema (too old to get the exact number), "
        + "please initialize the database.";
      log.error(error);
    }
    return "";
  }

  @Override
  public boolean checkDatabaseSchemaVersion() throws AnnisException
  {
    String dbSchemaVersion = getDatabaseSchemaVersion();
    if (getSchemaVersion() != null && !getSchemaVersion().equalsIgnoreCase(
      dbSchemaVersion))
    {
      String error = "Wrong database schema \"" + dbSchemaVersion + "\", please initialize the database.";
      log.error(error);
      throw new AnnisException(error);
    }
    return true;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  private boolean lockCorpusTable(boolean waitForOtherTasks)
  {
    try
    {
      log.info("Locking corpus table to ensure no other import is running");
      jdbcTemplate.execute(
        "LOCK TABLE corpus IN EXCLUSIVE MODE" + (waitForOtherTasks ? "" : " NOWAIT"));
      return true;
    }
    catch (DataAccessException ex)
    {
      return false;
    }
  }

  @Transactional(readOnly = false, propagation = Propagation.NEVER)
  @Override
  public void initializeDatabase(String host, String port, String database,
    String user, String password, String defaultDatabase, String superUser,
    String superPassword, boolean useSSL, String pgSchema)
  {
    // connect as super user to the default database to create new user and database
    if (superPassword != null)
    {
      log.info("Creating Annis database and user.");
      dataSource.setInnerDataSource(createDataSource(host, port,
        defaultDatabase, superUser, superPassword, useSSL, pgSchema));

      createDatabaseAndUserInTransaction(database, user, password);
    }
    // switch to new database as new user for the rest of the initialization procedure
    dataSource.setInnerDataSource(createDataSource(host, port, database,
      user, password, useSSL, pgSchema));

    //
    if (pgSchema != null && !"public".equalsIgnoreCase(pgSchema))
    {
      pgSchema = pgSchema.toLowerCase().replaceAll("[^a-z0-9]", "_");
      log.info("creating PostgreSQL schema {}", pgSchema);
      // we have to create a schema before we can use it
      try
      {
        jdbcTemplate.execute("CREATE SCHEMA " + pgSchema + ";");
      }
      catch (DataAccessException ex)
      {
        // ignore if the schema already exists
        log.info("schema " + pgSchema + " already exists");
      }
    }

    createSchemaInTransaction();

  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  private void createDatabaseAndUserInTransaction(String database,
    String user, String password)
  {
    dropDatabase(database);
    dropUser(user);
    createUser(user, password);
    createDatabase(database, user);

    installPlPgSql();
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  private void createSchemaInTransaction()
  {
    createFunctionUniqueToplevelCorpusName();
    createSchema();
    createSchemaIndexes();
    populateSchema();
  }

  private BasicDataSource createDataSource(File dbProperties)
    throws IOException, URISyntaxException
  {
    BasicDataSource result = null;

    Properties props = new Properties();
    try (InputStream is = new FileInputStream(dbProperties))
    {
      props.load(is);

      String rawJdbcURL = props.getProperty("datasource.url").trim();

      rawJdbcURL = StringUtils.removeStart(rawJdbcURL, "jdbc:");
      URI jdbcURL = new URI(rawJdbcURL);

      result = createDataSource(
        jdbcURL.getHost(),
        "" + jdbcURL.getPort(),
        jdbcURL.getPath().substring(1), // remove the "/" at the beginning
        props.getProperty("datasource.username"),
        props.getProperty("datasource.password"),
        "true".equalsIgnoreCase(props.getProperty("datasource.ssl")),
        props.getProperty("datasource.schema"));
      ;
    }

    return result;
  }

  private BasicDataSource createDataSource(String host, String port,
    String database,
    String user, String password, boolean useSSL,
    String schema)
  {

    String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

    // DriverManagerDataSource is deprecated
    // return new DriverManagerDataSource("org.postgresql.Driver", url, user, password);
    BasicDataSource result = new BasicDataSource();
    result.setUrl(url);
    if (useSSL)
    {
      result.setConnectionProperties("ssl=true");
    }
    result.setUsername(user);
    result.setPassword(password);
    result.setValidationQuery("SELECT 1;");
    result.setAccessToUnderlyingConnectionAllowed(true);
    if (schema == null)
    {
      schema = "public";
    }
    result.setConnectionInitSqls(Arrays.asList(
      "SET search_path TO \"$user\"," + schema));

    result.setDriverClassName("org.postgresql.Driver");

    return result;
  }

  @Override
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW,
    isolation = Isolation.READ_COMMITTED)
  public boolean importCorpus(String path,
    String aliasName,
    boolean override,
    boolean waitForOtherTasks)
  {

    // check schema version first
    checkDatabaseSchemaVersion();

    if (!lockCorpusTable(waitForOtherTasks))
    {
      log.error("Another import is currently running");
      return false;
    }

    // explicitly unset any timeout
    jdbcTemplate.update("SET statement_timeout TO 0");

    createStagingArea(temporaryStagingArea);
    bulkImport(path);

    String toplevelCorpusName = getTopLevelCorpusFromTmpArea();

    // remove conflicting top level corpora, when override is set to true.
    if (override)
    {
      checkAndRemoveTopLevelCorpus();
    }
    else
    {
      checkTopLevelCorpus();
    }

    createStagingAreaIndexes();

    computeTopLevelCorpus();
    analyzeStagingTables();

    computeLeftTokenRightToken();

    removeUnecessarySpanningRelations();

    addUniqueNodeNameAppendix();
    adjustRankPrePost();
    adjustTextId();
    long corpusID = updateIds();

    importBinaryData(path, toplevelCorpusName);

    extendStagingText(corpusID);
    extendStagingExampleQueries(corpusID);

    analyzeAutoGeneratedQueries(corpusID);

    computeRealRoot();
    computeLevel();
    computeCorpusStatistics(path);
    updateCorpusStatsId(corpusID);
    computeSpanFromSegmentation();

    applyConstraints();
    analyzeStagingTables();

    insertCorpus();

    computeCorpusPath(corpusID);

    createAnnotations(corpusID);

    createAnnoCategory(corpusID);

    // create the new facts table partition
    createFacts(corpusID);

    // the entries, which where here done, are possible after generating facts
    updateCorpusStatistic(corpusID);

    if (temporaryStagingArea)
    {
      dropStagingArea();
    }

    // create empty corpus properties file
    if (annisDao.getCorpusConfigurationSave(toplevelCorpusName) == null)
    {
      log.info("creating new corpus.properties file");
      annisDao.setCorpusConfiguration(toplevelCorpusName, new Properties());
    }

    analyzeFacts(corpusID);
    analyzeTextTable(toplevelCorpusName);
    generateExampleQueries(corpusID);

    if (aliasName != null && !aliasName.isEmpty())
    {
      addCorpusAlias(corpusID, aliasName);
    }
    return true;
  }

  ///// Subtasks of importing a corpus
  protected void dropIndexes()
  {
    log.info("dropping indexes");
    for (String index : listIndexesOnTables(allTables()))
    {
      log.debug("dropping index: " + index);
      jdbcTemplate.execute("DROP INDEX " + index);
    }
  }

  void createStagingArea(boolean useTemporary)
  {
    log.info("creating staging area");
    MapSqlParameterSource args = makeArgs().addValue(":tmp", useTemporary
      ? "TEMPORARY" : "UNLOGGED");
    executeSqlFromScript("staging_area.sql", args);
  }

  /**
   * Reads tab seperated files from the filesystem, but it takes only files into
   * account with the {@link DefaultAdministrationDao#REL_ANNIS_FILE_SUFFIX}
   * suffix. Further it is straight forward except for the
   * {@link DefaultAdministrationDao#FILE_RESOLVER_VIS_MAP} and the
   * {@link DefaultAdministrationDao#EXAMPLE_QUERIES_TAB}. This is done by this
   * method automatically.
   *
   * <ul>
   *
   * <li>{@link DefaultAdministrationDao#FILE_RESOLVER_VIS_MAP}: For backwards
   * compatibility, the columns must be counted, since there exists one
   * additional column for visibility behaviour of visualizers.</li>
   *
   * <li>{@link DefaultAdministrationDao#EXAMPLE_QUERIES_TAB}: Takes into
   * account the state of {@link #generateExampleQueries}.</li>
   *
   * </ul>
   *
   * @param path The path to the relANNIS. The files have to have this suffix
   * {@link DefaultAdministrationDao#REL_ANNIS_FILE_SUFFIX}
   */
  void bulkImport(String path)
  {
    log.info("bulk-loading data");

    for (String table : importedTables)
    {
      if (table.equalsIgnoreCase(FILE_RESOLVER_VIS_MAP))
      {
        importResolverVisMapTable(path, table);
      }
      // check if example query exists. If not copy it from the resource folder.
      else if (table.equalsIgnoreCase(EXAMPLE_QUERIES_TAB))
      {
        File f = new File(path, table + REL_ANNIS_FILE_SUFFIX);
        if (f.exists())
        {
          log.info(table + REL_ANNIS_FILE_SUFFIX + " file exists");
          bulkloadTableFromResource(tableInStagingArea(table),
            new FileSystemResource(f));

          if (generateExampleQueries == (EXAMPLE_QUERIES_CONFIG.IF_MISSING))
          {
            generateExampleQueries = EXAMPLE_QUERIES_CONFIG.FALSE;
          }
        }
        else
        {
          if (generateExampleQueries == EXAMPLE_QUERIES_CONFIG.IF_MISSING)
          {
            generateExampleQueries = EXAMPLE_QUERIES_CONFIG.TRUE;
          }

          log.info(table + REL_ANNIS_FILE_SUFFIX + " file not found");
        }
      }
      else if (table.equalsIgnoreCase("node"))
      {
        bulkImportNode(path);
      }
      else
      {
        bulkloadTableFromResource(tableInStagingArea(table),
          new FileSystemResource(new File(path, table + REL_ANNIS_FILE_SUFFIX)));
      }
    }
  }

  private void bulkImportNode(String path)
  {
    // check column number by reading first line
    File nodeTabFile = new File(path, "node.tab");
    try (BufferedReader reader
      = new BufferedReader(new InputStreamReader(
          new FileInputStream(nodeTabFile), "UTF-8"));)
    {

      String firstLine = reader.readLine();

      int columnNumber = firstLine == null ? 13
        : StringUtils.splitPreserveAllTokens(firstLine, '\t').length;
      if (columnNumber == 13)
      {
        // new node table with segmentations
        // no special handling needed
        bulkloadTableFromResource(tableInStagingArea("node"),
          new FileSystemResource(nodeTabFile));
      }
      else if (columnNumber == 10)
      {
        jdbcTemplate.execute("DROP TABLE IF EXISTS _tmpnode;");
        // old node table without segmentations
        // create temporary table for  bulk import
        jdbcTemplate.execute(
          "CREATE TEMPORARY TABLE _tmpnode"
          + "\n(\n"
          + "id bigint,\n"
          + "text_ref integer,\n"
          + "corpus_ref integer,\n"
          + "namespace varchar(100),\n"
          + "name varchar(100),\n"
          + "\"left\" integer,\n"
          + "\"right\" integer,\n"
          + "token_index integer,\n"
          + "continuous boolean,\n"
          + "span varchar(2000)\n"
          + ");");

        bulkloadTableFromResource("_tmpnode",
          new FileSystemResource(nodeTabFile));

        log.info("copying nodes from temporary helper table into staging area");
        jdbcTemplate.execute(
          "INSERT INTO " + tableInStagingArea("node") + "\n"
          + "  SELECT id, text_ref, corpus_ref, namespace, name, \"left\", "
          + "\"right\", token_index, "
          + "NULL AS seg_name, NULL AS seg_left, NULL AS seg_right, "
          + "continuous, span\n"
          + "FROM _tmpnode");
      }
      else
      {
        throw new RuntimeException("Illegal number of columns in node.tab, "
          + "should be 13 or 10 but was " + columnNumber);
      }
        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
  }

  void createStagingAreaIndexes()
  {
    log.info("creating indexes for staging area");
    executeSqlFromScript("indexes_staging.sql");
  }

  void computeTopLevelCorpus()
  {
    log.info("computing top-level corpus");
    executeSqlFromScript("toplevel_corpus.sql");
  }

  void importBinaryData(String path, String toplevelCorpusName)
  {
    log.info("importing all binary data from ExtData");
    File extData = new File(path + "/ExtData");
    if (extData.canRead() && extData.isDirectory())
    {
      // import toplevel corpus media files
      File[] topFiles = extData.listFiles((FileFilter) FileFileFilter.FILE);
      for (File data : topFiles)
      {
        String extension = FilenameUtils.getExtension(data.getName());
        try
        {
          if (mimeTypeMapping.containsKey(extension))
          {
            log.info("import " + data.getCanonicalPath() + " to staging area");

            // search for corpus_ref
            String sqlScript
              = "SELECT id FROM _corpus WHERE top_level IS TRUE LIMIT 1";
            long corpusID = jdbcTemplate.queryForLong(sqlScript);

            importSingleFile(data.getCanonicalPath(), toplevelCorpusName,
              corpusID);
          }
          else
          {
            log.warn(
              "not importing " + data.getCanonicalPath() + " since file type is unknown");
          }
        }
        catch (IOException ex)
        {
          log.error("no canonical path given", ex);
        }
      }

      // get each subdirectory (which corresponds to an document name)
      File[] documents = extData.listFiles(
        (FileFilter) DirectoryFileFilter.DIRECTORY);
      for (File doc : documents)
      {
        if (doc.isDirectory() && doc.canRead())
        {
          File[] dataFiles = doc.listFiles((FileFilter) FileFileFilter.FILE);
          for (File data : dataFiles)
          {
            String extension = FilenameUtils.getExtension(data.getName());
            try
            {
              if (mimeTypeMapping.containsKey(extension))
              {
                log.info(
                  "import " + data.getCanonicalPath() + " to staging area");

                // search for corpus_ref
                String sqlScript
                  = "SELECT id FROM _corpus WHERE \"name\" = ? LIMIT 1";
                long corpusID = jdbcTemplate.queryForLong(sqlScript, doc.
                  getName());

                importSingleFile(data.getCanonicalPath(), toplevelCorpusName,
                  corpusID);
              }
              else
              {
                log.
                  warn(
                    "not importing " + data.getCanonicalPath() + " since file type is unknown");
              }
            }
            catch (IOException ex)
            {
              log.error("no canonical path given", ex);
            }
          }
        }
      }
    }
  }

  /**
   * Imports a single binary file.
   *
   * @param file Specifies the file to be imported.
   * @param corpusRef Assigns the file this corpus.
   * @param toplevelCorpusName The toplevel corpus name
   */
  private void importSingleFile(String file, String toplevelCorpusName,
    long corpusRef)
  {

    BinaryImportHelper preStat = new BinaryImportHelper(file, getRealDataDir(),
      toplevelCorpusName,
      corpusRef, mimeTypeMapping);
    jdbcTemplate.execute(BinaryImportHelper.SQL, preStat);

  }

  /**
   * Updates the example queries table in the staging area. The final toplevel
   * corpus must already be computed.
   *
   * @param toplevelID The final top level corpus id.
   *
   */
  void extendStagingExampleQueries(long toplevelID)
  {
    log.info("extending _example_queries");
    executeSqlFromScript("extend_staging_example_queries.sql",
      makeArgs().addValue(":id", toplevelID));
  }

  void extendStagingText(long toplevelID)
  {
    log.info("extending _text");
    executeSqlFromScript("extend_staging_text.sql", makeArgs().addValue(":id",
      toplevelID));
  }

  void computeLeftTokenRightToken()
  {
    log.info("computing values for struct.left_token and struct.right_token");
    executeSqlFromScript("left_token_right_token.sql");

    // re-analyze node since we added new columns
    log.info("analyzing node");
    jdbcTemplate.execute("ANALYZE " + tableInStagingArea("node"));

  }

  void computeRealRoot()
  {
    log.info("computing real root for rank");
    executeSqlFromScript("root.sql");
  }

  void computeLevel()
  {
    log.info("computing values for rank.level (dominance and precedence)");
    executeSqlFromScript("level.sql");

    log.info("computing values for rank.level (coverage)");
    executeSqlFromScript("level_coverage.sql");
  }

  void computeCorpusStatistics(String path)
  {

    File f = new File(path);
    String absolutePath = path;
    try
    {
      absolutePath = f.getCanonicalPath();
    }
    catch (IOException ex)
    {
      log.error(
        "Something went really wrong when calculating the canonical path", ex);
    }

    log.info("computing statistics for top-level corpus");
    MapSqlParameterSource args = makeArgs().addValue(":path", absolutePath);
    executeSqlFromScript("corpus_stats.sql", args);
  }

  void updateCorpusStatistic(long corpusID)
  {
    MapSqlParameterSource args = makeArgs().addValue(":id", corpusID);
    log.info("updating statistics for top-level corpus");
    executeSqlFromScript("corpus_stats_upd.sql", args);
  }

  void computeCorpusPath(long corpusID)
  {
    MapSqlParameterSource args = makeArgs().addValue(":id", corpusID);
    log.info("computing path information of the corpus tree for corpus with ID "
      + corpusID);
    executeSqlFromScript("compute_corpus_path.sql", args);
  }

  protected void adjustRankPrePost()
  {
    log.info("updating pre and post order in _rank");
    executeSqlFromScript("adjustrankprepost.sql");
    log.info("analyzing _rank");
    jdbcTemplate.execute("ANALYZE " + tableInStagingArea("rank"));
  }

  protected void adjustTextId()
  {
    log.info("updating id in _text and text_ref in _node");
    executeSqlFromScript("adjusttextid.sql");
    log.info("analyzing _node and _text");
    jdbcTemplate.execute("ANALYZE " + tableInStagingArea("text"));
    jdbcTemplate.execute("ANALYZE " + tableInStagingArea("node"));
  }

  protected void addUniqueNodeNameAppendix()
  {
    // first check if this is actually necessary
    log.info("check if node names are unique");

    jdbcTemplate.execute(
      "ALTER TABLE _node ADD COLUMN unique_name_appendix varchar;");

    List<Integer> checkDuplicate = jdbcTemplate.queryForList(
      "SELECT COUNT(*) from _node GROUP BY \"name\", corpus_ref HAVING COUNT(*) > 1 LIMIT 1",
      Integer.class);
    if (checkDuplicate.isEmpty())
    {
      log.info("node names are unique, no update necessary");
    }
    else
    {
      log.info("add an unique node name appendix");
      executeSqlFromScript("unique_node_name_appendix.sql");
    }
  }

  /**
   *
   * @return the new corpus ID
   */
  long updateIds()
  {

    int numOfEntries = jdbcTemplate.queryForInt(
      "SELECT COUNT(*) from corpus_stats");

    long recentCorpusId = 0;

    if (numOfEntries > 0)
    {
      recentCorpusId = jdbcTemplate.queryForLong(
        "SELECT max(id) FROM corpus_stats");
      log.info("the id from recently imported corpus:" + recentCorpusId);
    }

    log.info("updating IDs in staging area");
    MapSqlParameterSource args = makeArgs().addValue(":id", recentCorpusId);
    executeSqlFromScript("update_ids.sql", args);

    log.info("query for the new corpus ID");
    long result = jdbcTemplate.queryForLong(
      "SELECT MAX(toplevel_corpus) FROM _node");
    log.info("new corpus ID is " + result);
    return result;
  }

  void computeSpanFromSegmentation()
  {
    log.info("computing span value for segmentation nodes");
    executeSqlFromScript("span_from_segmentation.sql");
  }

  void updateCorpusStatsId(long corpusId)
  {
    log.info("updating corpus ID in corpus_stat");
    jdbcTemplate.update("UPDATE _corpus_stats SET id = " + corpusId);
  }

  void applyConstraints()
  {
    log.info("activating relational constraints");
    executeSqlFromScript("constraints.sql");
  }

  void insertCorpus()
  {
    log.info("moving corpus from staging area to main db");
    for (String table : tablesToCopyManually)
    {
      int numOfEntries = jdbcTemplate.queryForInt("SELECT COUNT(*) from "
        + tableInStagingArea(table));

      if (numOfEntries > 0)
      {
        StringBuilder sql = new StringBuilder();

        String predefinedFrom
          = tableInsertFrom == null ? null : tableInsertFrom.get(table);
        String predefinedSelect
          = tableInsertSelect == null ? null : tableInsertSelect.get(table);

        if (predefinedFrom != null || predefinedSelect != null)
        {
          if (predefinedFrom == null)
          {
            predefinedFrom = predefinedSelect;
          }

          sql.append("INSERT INTO ");
          sql.append(table);
          sql.append(" ( ");
          sql.append(predefinedSelect);

          sql.append(" ) (SELECT ");
          sql.append(predefinedFrom);
          sql.append(" FROM ");
          sql.append(tableInStagingArea(table)).append(")");
        }
        else
        {
          sql.append("INSERT INTO ");
          sql.append(table);
          sql.append(" (SELECT * FROM ");
          sql.append(tableInStagingArea(table)).append(")");
        }
        jdbcTemplate.execute(sql.toString());
      }
    }
  }

  void dropStagingArea()
  {
    log.info("dropping staging area");

    // tables must be dropped in reverse order
    List<String> tables = importedAndCreatedTables();
    Collections.reverse(tables);

    for (String table : tables)
    {
      jdbcTemplate.execute("DROP TABLE " + tableInStagingArea(table));
    }

  }

  void dropMaterializedTables()
  {
    log.info("dropping materialized tables");

    jdbcTemplate.execute("DROP TABLE facts");

  }

  void analyzeStagingTables()
  {
    for (String t : importedTables)
    {
      log.info("analyzing " + t);
      jdbcTemplate.execute("ANALYZE " + tableInStagingArea(t));
    }
  }

  void createAnnotations(long corpusID)
  {
    MapSqlParameterSource args = makeArgs().addValue(":id", corpusID);
    log.info("creating annotations table for corpus with ID " + corpusID);
    executeSqlFromScript("annotations.sql", args);

    log.info("indexing annotations table for corpus with ID " + corpusID);
    executeSqlFromScript("indexes_annotations.sql", args);
  }

  void createAnnoCategory(long corpusID)
  {
    MapSqlParameterSource args = makeArgs().addValue(":id", corpusID);
    log.
      info("creating annotation category table for corpus with ID " + corpusID);
    executeSqlFromScript("annotation_category.sql", args);
  }

  void analyzeFacts(long corpusID)
  {
    log.info("analyzing facts table for corpus with ID " + corpusID);
    jdbcTemplate.execute("ANALYZE facts_" + corpusID);

    log.info("analyzing parent facts table");
    jdbcTemplate.execute("ANALYZE facts");
  }

  void createFacts(long corpusID)
  {

    MapSqlParameterSource args = makeArgs().addValue(":id", corpusID);

    log.info("creating materialized facts table for corpus with ID " + corpusID);
    executeSqlFromScript("facts.sql", args);

    clusterFacts(corpusID);

    log.info("indexing the new facts table (corpus with ID " + corpusID + ")");
    executeSqlFromScript("indexes.sql", args);

  }

  void clusterFacts(long corpusID)
  {
    MapSqlParameterSource args = makeArgs().addValue(":id", corpusID);

    log.info("clustering materialized facts table for corpus with ID "
      + corpusID);
    executeSqlFromScript("cluster.sql", args);

  }

  void removeUnecessarySpanningRelations()
  {
    log.info("setting \"continuous\" to a correct value");
    executeSqlFromScript("set_continuous.sql");

    // re-analyze node since we added new columns
    log.info("analyzing node");
    jdbcTemplate.execute("ANALYZE " + tableInStagingArea("node"));

    log.info("removing unnecessary span relations");
    executeSqlFromScript("remove_span_relations.sql");

  }

  ///// Other sub tasks
  @Override
  public List<Long> listToplevelCorpora()
  {
    String sql = "SELECT id FROM corpus WHERE top_level = 'y'";

    return jdbcTemplate.query(sql, ParameterizedSingleColumnRowMapper.
      newInstance(Long.class));
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  @Override
  public void deleteCorpora(List<Long> ids, boolean acquireLock)
  {
    if (acquireLock && !lockCorpusTable(false))
    {
      log.error("Another import is currently running");
      return;
    }

    File dataDir = getRealDataDir();

    for (long l : ids)
    {
      log.info("deleting external data files");

      List<String> filesToDelete = jdbcTemplate.queryForList(
        "SELECT filename FROM media_files AS m, corpus AS top, corpus AS child\n"
        + "WHERE\n"
        + "  m.corpus_ref = child.id AND\n"
        + "  top.id = ? AND\n"
        + "  child.pre >= top.pre AND child.post <= top.post", String.class, l);
      for (String fileName : filesToDelete)
      {
        File f = new File(dataDir, fileName);
        if (f.exists())
        {
          if (!f.delete())
          {
            log.warn("Could not delete {}", f.getAbsolutePath());
          }
        }
      }

      log.info("dropping tables");

      log.debug("dropping facts table for corpus " + l);
      jdbcTemplate.execute("DROP TABLE IF EXISTS facts_" + l);
      jdbcTemplate.execute("DROP TABLE IF EXISTS facts_edge_" + l);
      jdbcTemplate.execute("DROP TABLE IF EXISTS facts_node_" + l);
      log.debug("dropping annotation_pool table for corpus " + l);
      jdbcTemplate.execute("DROP TABLE IF EXISTS annotation_pool_" + l);
      log.debug("dropping annotations table for corpus " + l);
      jdbcTemplate.execute("DROP TABLE IF EXISTS annotations_" + l);
    }

    log.info("recursivly deleting corpora: " + ids);

    executeSqlFromScript("delete_corpus.sql", makeArgs().addValue(":ids",
      StringUtils.join(ids, ", ")));
  }

  @Transactional(readOnly = true)
  @Override
  public void cleanupData()
  {

    List<String> allFilesInDatabaseList = jdbcTemplate.queryForList(
      "SELECT filename FROM media_files AS m", String.class);

    File dataDir = getRealDataDir();

    Set<File> allFilesInDatabase = new HashSet<>();
    for (String singleFileName : allFilesInDatabaseList)
    {
      allFilesInDatabase.add(new File(dataDir, singleFileName));
    }

    log.info("Cleaning up the data directory");
    // go through each file of the folder and check if it is not included
    File[] childFiles = dataDir.listFiles();
    if (childFiles != null)
    {
      for (File f : childFiles)
      {
        if (f.isFile() && !allFilesInDatabase.contains(f))
        {
          if (!f.delete())
          {
            log.warn("Could not delete {}", f.getAbsolutePath());
          }
        }
      }
    }
  }

  @Override
  public List<Map<String, Object>> listCorpusStats()
  {
    return jdbcTemplate.queryForList(
      "SELECT * FROM corpus_info ORDER BY name");
  }

  @Override
  public List<Map<String, Object>> listCorpusStats(File databaseProperties)
  {
    List<Map<String, Object>> result = new LinkedList<>();
    DataSource origDataSource = dataSource.getInnerDataSource();
    try
    {
      dataSource.setInnerDataSource(createDataSource(databaseProperties));
      result = jdbcTemplate.queryForList(
        "SELECT * FROM corpus_info ORDER BY name");
    }
    catch (IOException | URISyntaxException | DataAccessException ex)
    {
      log.error(
        "Could not query corpus list for the file " + databaseProperties.
        getAbsolutePath(), ex);
    }
    finally
    {
      dataSource.setInnerDataSource(origDataSource);
    }
    return result;
  }

  @Override
  public List<String> listUsedIndexes()
  {
    log.info("retrieving list of used indexes");
    return listIndexDefinitions(true);
  }

  @Override
  public List<String> listUnusedIndexes()
  {
    log.info("retrieving list of unused indexes");
    return listIndexDefinitions(false);
  }

  @Override
  @Transactional(readOnly = true)
  public UserConfig retrieveUserConfig(final String userName)
  {
    String sql = "SELECT * FROM user_config WHERE id=?";
    UserConfig config = jdbcTemplate.query(sql, new Object[]
    {
      userName
    },
      new ResultSetExtractor<UserConfig>()
      {
        @Override
        public UserConfig extractData(ResultSet rs) throws SQLException, DataAccessException
        {

          // default to empty config
          UserConfig c = new UserConfig();

          if (rs.next())
          {
            try
            {
              c = jsonMapper.readValue(rs.getString("config"),
                UserConfig.class);
            }
            catch (IOException ex)
            {
              log.
              error(
                "Could not parse JSON that is stored in database (user configuration)",
                ex);
            }
          }
          return c;
        }
      });

    return config;
  }

  @Override
  @Transactional(readOnly = false)
  public void storeUserConfig(String userName, UserConfig config)
  {
    String sqlUpdate = "UPDATE user_config SET config=?::json WHERE id=?";
    String sqlInsert = "INSERT INTO user_config(id, config) VALUES(?,?)";
    try
    {
      String jsonVal = jsonMapper.writeValueAsString(config);

      // if no row was affected there is no entry yet and we should create one
      if (jdbcTemplate.update(sqlUpdate, jsonVal, userName) == 0)
      {
        jdbcTemplate.update(sqlInsert, userName, jsonVal);
      }
    }
    catch (IOException ex)
    {
      log.error("Cannot serialize user config JSON for database.", ex);
    }
  }

  @Override
  public void addCorpusAlias(long corpusID, String alias)
  {
    jdbcTemplate.update(
      "INSERT INTO corpus_alias (alias, corpus_ref)\n"
      + "VALUES(\n"
      + "  ?, \n"
      + "  ?\n"
      + ");",
      alias, corpusID);
  }

  ///// Helpers
  private List<String> importedAndCreatedTables()
  {
    List<String> tables = new ArrayList<>();
    tables.addAll(Arrays.asList(importedTables));
    tables.addAll(Arrays.asList(createdTables));
    return tables;
  }

  private List<String> allTables()
  {
    List<String> tables = new ArrayList<>();
    tables.addAll(Arrays.asList(importedTables));
    tables.addAll(Arrays.asList(createdTables));
    //tables.addAll(Arrays.asList(materializedTables));
    return tables;
  }

  // tables in the staging area have their names prefixed with "_"
  private String tableInStagingArea(String table)
  {
    return "_" + table;
  }

  private MapSqlParameterSource makeArgs()
  {
    return new MapSqlParameterSource();
  }

  private ParameterizedSingleColumnRowMapper<String> stringRowMapper()
  {
    return ParameterizedSingleColumnRowMapper.newInstance(String.class);
  }

  // reads the content from a resource into a string
  @SuppressWarnings("unchecked")
  private String readSqlFromResource(Resource resource,
    MapSqlParameterSource args)
  {
    // XXX: uses raw type, what are the parameters to Map in MapSqlParameterSource?
    Map<String, Object> parameters = args != null ? args.getValues() : new HashMap();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
      new FileInputStream(
        resource.
        getFile()), "UTF-8"));)
    {
      StringBuilder sqlBuf = new StringBuilder();

      for (String line = reader.readLine(); line != null; line
        = reader.readLine())
      {
        sqlBuf.append(line).append("\n");
      }
      String sql = sqlBuf.toString();
      for (Entry<String, Object> placeHolderEntry : parameters.entrySet())
      {
        String key = placeHolderEntry.getKey();
        String value = placeHolderEntry.getValue().toString();
        log.debug("substitution for parameter '" + key + "' in SQL script: "
          + value);
        sql = sql.replaceAll(key, Matcher.quoteReplacement(value));
      }
      return sql;
      }
      catch (IOException e)
      {
        log.error("Couldn't read SQL script from resource file.", e);
        throw new FileAccessException(
          "Couldn't read SQL script from resource file.", e);
      }
  }

  // executes an SQL script from $ANNIS_HOME/scripts
  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public PreparedStatement executeSqlFromScript(String script)
  {
    return executeSqlFromScript(script, null);
  }

  /**
   * Registers a {@link PreparedStatement} to the {@link StatementController}.
   */
  private class CancelableStatements implements PreparedStatementCreator,
    PreparedStatementCallback<Void>
  {

    String sqlQuery;

    PreparedStatement statement;

    public CancelableStatements(String sql,
      StatementController statementController)
    {
      sqlQuery = sql;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection con) throws SQLException
    {
      if (statementController != null && statementController.isCancelled())
      {
        throw new SQLException("process was cancelled");
      }

      statement = con.prepareCall(sqlQuery);
      if (statementController != null)
      {
        statementController.registerStatement(statement);
      }
      return statement;
    }

    @Override
    public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException
    {
      ps.execute();
      return null;
    }
  }

  // executes an SQL script from $ANNIS_HOME/scripts, substituting the parameters found in args
  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public PreparedStatement executeSqlFromScript(String script,
    MapSqlParameterSource args)
  {
    File fScript = new File(scriptPath, script);
    if (fScript.canRead() && fScript.isFile())
    {
      Resource resource = new FileSystemResource(fScript);
      log.debug("executing SQL script: " + resource.getFilename());
      String sql = readSqlFromResource(resource, args);
      CancelableStatements cancelableStats = new CancelableStatements(
        sql, statementController);

      // register the statement, so we could try to interrupt it in the gui.
      if (statementController != null)
      {
        statementController.registerStatement(cancelableStats.statement);
      }
      else
      {
        log.debug("statement controller is not initialized");
      }

      jdbcTemplate.execute(cancelableStats, cancelableStats);
      return cancelableStats.statement;
    }
    else
    {
      log.debug("SQL script " + fScript.getName() + " does not exist");
      return null;
    }
  }

  private <T> T querySqlFromScript(String script,
    ResultSetExtractor<T> resultSetExtractor)
  {
    File fScript = new File(scriptPath, script);
    if (fScript.canRead() && fScript.isFile())
    {
      Resource resource = new FileSystemResource(fScript);
      log.debug("executing SQL script: " + resource.getFilename());
      String sql = readSqlFromResource(resource, null);
      return jdbcTemplate.query(sql, resultSetExtractor);
    }
    else
    {
      log.debug("SQL script " + fScript.getName() + " does not exist");
      return null;
    }
  }

  // bulk-loads a table from a resource
  private void bulkloadTableFromResource(String table, Resource resource)
  {
    log.debug("bulk-loading data from '" + resource.getFilename()
      + "' into table '" + table + "'");
    String sql = "COPY " + table
      + " FROM STDIN WITH DELIMITER E'\t' NULL AS 'NULL'";

    try
    {
      // retrieve the currently open connection if running inside a transaction
      Connection originalCon = DataSourceUtils.getConnection(dataSource);
      Connection con = originalCon;
      if (con instanceof DelegatingConnection)
      {
        DelegatingConnection<?> delCon = (DelegatingConnection<?>) con;
        con = delCon.getInnermostDelegate();
      }

      Preconditions.checkState(con instanceof PGConnection,
        "bulk-loading only works with a PostgreSQL JDBC connection");

      // Postgres JDBC4 8.4 driver now supports the copy API
      PGConnection pgCon = (PGConnection) con;
      pgCon.getCopyAPI().copyIn(sql, resource.getInputStream());

      DataSourceUtils.releaseConnection(originalCon, dataSource);

    }
    catch (SQLException e)
    {
      throw new DatabaseAccessException(e);
    }
    catch (IOException e)
    {
      throw new FileAccessException(e);
    }
  }

  // get a list of indexes on the imported Snd created tables tables which are not
  // auto-created by postgres (namely, primary key and unique constraints)
  // exploits the fact that the index has the same name as the constraint
  private List<String> listIndexesOnTables(List<String> tables)
  {
    String sql
      = ""
      + "SELECT indexname "
      + "FROM pg_indexes "
      + "WHERE tablename IN (" + StringUtils.repeat("?", ",", tables.size()) + ") "
      + "AND lower(indexname) NOT IN "
      + "	(SELECT lower(conname) FROM pg_constraint WHERE contype in ('p', 'u'))";

    return jdbcTemplate.query(sql, tables.toArray(), stringRowMapper());
  }

  private List<String> listIndexDefinitions(boolean used)
  {
    return listIndexDefinitions(used, allTables());
  }

  /*
   * Returns the CREATE INDEX statement for all indexes on the Annis tables,
   * that are not auto-created by PostgreSQL (primary keys and unique
   * constraints).
   *
   * @param used	If True, return used indexes. If False, return unused indexes
   * (scan count is 0).
   */
  public List<String> listIndexDefinitions(boolean used, List<String> tables)
  {
    String scansOp = used ? "!=" : "=";
    String sql
      = "SELECT pg_get_indexdef(x.indexrelid) AS indexdef "
      + "FROM pg_index x, pg_class c "
      + "WHERE x.indexrelid = c.oid "
      + "AND c.relname IN ( " + StringUtils.repeat("?", ",", tables.size()) + ") "
      + "AND pg_stat_get_numscans(x.indexrelid) " + scansOp + " 0";
    return jdbcTemplate.query(sql, tables.toArray(), stringRowMapper());
  }

  public List<String> listIndexDefinitions(String... tables)
  {
    String sql = ""
      + "SELECT pg_get_indexdef(x.indexrelid) AS indexdef "
      + "FROM pg_index x, pg_class c, pg_indexes i "
      + "WHERE x.indexrelid = c.oid "
      + "AND c.relname = i.indexname "
      + "AND i.tablename IN ( " + StringUtils.repeat("?", ",", tables.length) + " )";
    return jdbcTemplate.query(sql, tables,
      new ParameterizedSingleColumnRowMapper<String>());
  }

  public List<String> listUsedIndexes(String... tables)
  {
    String sql = ""
      + "SELECT pg_get_indexdef(x.indexrelid) AS indexdef "
      + "FROM pg_index x, pg_class c, pg_indexes i "
      + "WHERE x.indexrelid = c.oid "
      + "AND c.relname = i.indexname "
      + "AND i.tablename IN ( " + StringUtils.repeat("?", ",", tables.length) + " ) "
      + "AND pg_stat_get_numscans(x.indexrelid) != 0";
    return jdbcTemplate.query(sql, tables,
      new ParameterizedSingleColumnRowMapper<String>());
  }

  public boolean resetStatistics()
  {
    try
    {
      jdbcTemplate.queryForList("SELECT pg_stat_reset()");
      return true;
    }
    catch (DataAccessException e)
    {
      return false;
    }
  }

  ///// Getter / Setter
  public void setDataSource(DynamicDataSource dataSource)
  {
    this.dataSource = dataSource;
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public JdbcTemplate getJdbcTemplate()
  {
    return jdbcTemplate;
  }

  public String getScriptPath()
  {
    return scriptPath;
  }

  public void setScriptPath(String scriptPath)
  {
    this.scriptPath = scriptPath;
  }

  public String getExternalFilesPath()
  {
    return externalFilesPath;
  }

  public File getRealDataDir()
  {
    File dataDir;
    if (getExternalFilesPath() == null || getExternalFilesPath().isEmpty())
    {
      // use the default directory
      dataDir = new File(System.getProperty("user.home"), ".annis/data/");
    }
    else
    {
      dataDir = new File(getExternalFilesPath());
    }
    return dataDir;
  }

  public void setExternalFilesPath(String externalFilesPath)
  {
    this.externalFilesPath = externalFilesPath;
  }

  public boolean isTemporaryStagingArea()
  {
    return temporaryStagingArea;
  }

  public void setTemporaryStagingArea(boolean temporaryStagingArea)
  {
    this.temporaryStagingArea = temporaryStagingArea;
  }

  /**
   * Get the name and version of the schema this @{link AdministrationDao} is
   * configured to work with.
   */
  public String getSchemaVersion()
  {
    return schemaVersion;
  }

  public void setSchemaVersion(String schemaVersion)
  {
    this.schemaVersion = schemaVersion;
  }

  public Map<String, String> getMimeTypeMapping()
  {
    return mimeTypeMapping;
  }

  public void setMimeTypeMapping(Map<String, String> mimeTypeMapping)
  {
    this.mimeTypeMapping = mimeTypeMapping;
  }

  public Map<String, String> getTableInsertSelect()
  {
    return tableInsertSelect;
  }

  public void setTableInsertSelect(Map<String, String> tableInsertSelect)
  {
    this.tableInsertSelect = tableInsertSelect;
  }

  public Map<String, String> getTableInsertFrom()
  {
    return tableInsertFrom;
  }

  public void setTableInsertFrom(Map<String, String> tableInsertFrom)
  {
    this.tableInsertFrom = tableInsertFrom;
  }

  private void readOldResolverVisMapFormat(File resolver_vis_tab)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("CREATE TABLE tmp_resolver_vis_map ");
    sb.append("( ");
    sb.append("\"corpus\"   varchar, ");
    sb.append("\"version\" 	varchar, ");
    sb.append("\"namespace\"	varchar, ");
    sb.append("\"element\"    varchar, ");
    sb.append("\"vis_type\"   varchar NOT NULL, ");
    sb.append("\"display_name\"   varchar NOT NULL, ");
    sb.append("\"order\" integer default '0', ");
    sb.append("\"mappings\" varchar");
    sb.append(");");

    jdbcTemplate.execute(sb.toString());

    bulkloadTableFromResource("tmp_resolver_vis_map",
      new FileSystemResource(resolver_vis_tab));

    sb = new StringBuilder();

    sb.append("INSERT INTO ");
    sb.append(tableInStagingArea(FILE_RESOLVER_VIS_MAP));
    sb.append("\n\t");
    sb.append(" (");
    sb.append("corpus, ");
    sb.append("version, ");
    sb.append("namespace, ");
    sb.append("element, ");
    sb.append("vis_type, ");
    sb.append("display_name, ");
    sb.append("\"order\", ");
    sb.append("mappings");
    sb.append(")");
    sb.append("\n");
    sb.append("SELECT tmp.corpus, ");
    sb.append("tmp.version, ");
    sb.append("tmp.namespace, ");
    sb.append("tmp.element, ");
    sb.append("tmp.vis_type, ");
    sb.append("tmp.display_name, ");
    sb.append("tmp.\"order\", ");
    sb.append("tmp.mappings");
    sb.append("\n\t");
    sb.append("FROM tmp_resolver_vis_map AS tmp; ");

    jdbcTemplate.execute(sb.toString());
    jdbcTemplate.execute("DROP TABLE tmp_resolver_vis_map;");
  }

  /**
   * Imported the old and the new version of the resolver_vis_map.tab. The new
   * version has an additional column for visibility status of the
   * visualization.
   *
   * @param path The path to the relAnnis file.
   * @param table The final table in the database of the resolver_vis_map table.
   */
  private void importResolverVisMapTable(String path, String table)
  {
    try
    {

      // count cols for detecting old resolver_vis_map table format
      File resolver_vis_tab = new File(path, table + REL_ANNIS_FILE_SUFFIX);

      if (!resolver_vis_tab.isFile())
      {
        return;
      }

      String firstLine;
      try (BufferedReader bReader = new BufferedReader(
        new InputStreamReader(new FileInputStream(resolver_vis_tab), "UTF-8")))
      {
        firstLine = bReader.readLine();
      }

      int cols = 9; // default number
      if (firstLine != null)
      {
        String[] entries = firstLine.split("\t");
        cols = entries.length;
        log.debug("the first row: {} amount of cols: {}", entries, cols);
      }

      switch (cols)
      {
        // old format
        case 8:
          readOldResolverVisMapFormat(resolver_vis_tab);
          break;
        // new format
        case 9:
          bulkloadTableFromResource(tableInStagingArea(table),
            new FileSystemResource(new File(path, table + REL_ANNIS_FILE_SUFFIX)));
          break;
        default:
          log.error("invalid amount of cols");
          throw new RuntimeException();
      }

    }
    catch (IOException e)
    {
      log.error("could not read {}", table, e);
    }
    catch (FileAccessException e)
    {
      log.error("could not read {}", table, e);
    }
  }

  /**
   * Generates example queries if no example queries tab file is defined by the
   * user.
   */
  private void generateExampleQueries(long corpusID)
  {
    // set in the annis.properties file.
    if (generateExampleQueries == EXAMPLE_QUERIES_CONFIG.TRUE)
    {
      queriesGenerator.generateQueries(corpusID);
    }
  }

  /**
   * @return the generateExampleQueries
   */
  public EXAMPLE_QUERIES_CONFIG isGenerateExampleQueries()
  {
    return generateExampleQueries;
  }

  /**
   * @param generateExampleQueries the generateExampleQueries to set
   */
  public void setGenerateExampleQueries(
    EXAMPLE_QUERIES_CONFIG generateExampleQueries)
  {
    this.generateExampleQueries = generateExampleQueries;
  }

  /**
   * Counts nodes and operators of the AQL example query and writes it back to
   * the staging area.
   *
   * @param corpusID specifies the corpus, the analyze things.
   *
   */
  private void analyzeAutoGeneratedQueries(long corpusID)
  {
    // read the example queries from the staging area
    List<ExampleQuery> exampleQueries = jdbcTemplate.query(
      "SELECT * FROM _" + EXAMPLE_QUERIES_TAB, new RowMapper<ExampleQuery>()
      {
        @Override
        public ExampleQuery mapRow(ResultSet rs, int i) throws SQLException
        {
          ExampleQuery eQ = new ExampleQuery();
          eQ.setExampleQuery(rs.getString("example_query"));
          return eQ;
        }
      });

    // count the nodes of the aql Query
    countExampleQueryNodes(exampleQueries);

    // fetch the operators
    getOperators(exampleQueries, "\\.(\\*)?|\\>|\\>\\*|_i_");

    writeAmountOfNodesBack(exampleQueries);
  }

  /**
   * Maps example queries to integer, which represents the amount of nodes of
   * the aql query.
   *
   */
  private void countExampleQueryNodes(List<ExampleQuery> exampleQueries)
  {

    for (ExampleQuery eQ : exampleQueries)
    {

      QueryData query = getAnnisDao().parseAQL(eQ.getExampleQuery(), null);

      int count = 0;
      for (List<QueryNode> qNodes : query.getAlternatives())
      {
        count += qNodes.size();
      }

      eQ.setNodes(count);
    }
  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  /**
   * Writes the counted nodes and the used operators back to the staging area.
   *
   */
  private void writeAmountOfNodesBack(List<ExampleQuery> exampleQueries)
  {
    StringBuilder sb = new StringBuilder();

    for (ExampleQuery eQ : exampleQueries)
    {
      sb.append("UPDATE ").append("_").append(EXAMPLE_QUERIES_TAB).append(
        " SET ");
      sb.append("nodes=").append(String.valueOf(eQ.getNodes()));
      sb.append(" WHERE example_query='");
      sb.append(eQ.getExampleQuery()).append("';\n");

      sb.append("UPDATE ").append("_").append(EXAMPLE_QUERIES_TAB).append(
        " SET ");
      sb.append("used_ops='").append(String.valueOf(eQ.getUsedOperators()));
      sb.append("' WHERE example_query='");
      sb.append(eQ.getExampleQuery()).append("';\n");
    }

    jdbcTemplate.execute(sb.toString());
  }

  /**
   * Fetches operators used in the {@link ExampleQuery#getExampleQuery()} with a
   * given regex.
   *
   * @param exQueries Set the used operators property of each member.
   * @param regex The regex to search operators.
   */
  private void getOperators(List<ExampleQuery> exQueries, String regex)
  {

    Pattern opsRegex = Pattern.compile(regex);
    for (ExampleQuery eQ : exQueries)
    {
      List<String> ops = new ArrayList<>();
      Matcher m = opsRegex.matcher(eQ.getExampleQuery().replaceAll("\\s", ""));

      while (m.find())
      {
        ops.add(m.group());
      }

      eQ.setUsedOperators("{" + StringUtils.join(ops, ",") + "}");
    }
  }

  /**
   * @return the queriesGenerator
   */
  public QueriesGenerator getQueriesGenerator()
  {
    return queriesGenerator;
  }

  /**
   * @param queriesGenerator the queriesGenerator to set
   */
  public void setQueriesGenerator(
    QueriesGenerator queriesGenerator)
  {
    this.queriesGenerator = queriesGenerator;
  }

  /**
   * Retrieves the name of the top level corpus in the corpus.tab file.
   *
   * <p>
   * At this point, the tab files must be in the staging area.</p>
   *
   * @return The name of the toplevel corpus or an empty String if no top level
   * corpus is found.
   */
  private String getTopLevelCorpusFromTmpArea()
  {
    String sql = "SELECT name FROM " + tableInStagingArea("corpus")
      + " WHERE type='CORPUS'\n"
      + "AND pre = (SELECT min(pre) FROM " + tableInStagingArea("corpus") + ")\n"
      + "AND post = (SELECT max(post) FROM " + tableInStagingArea("corpus") + ")";

    return jdbcTemplate.query(sql, new ResultSetExtractor<String>()
    {
      @Override
      public String extractData(ResultSet rs) throws SQLException,
        DataAccessException
      {
        if (rs.next())
        {
          return rs.getString("name");
        }
        else
        {
          return null;
        }
      }
    });
  }

  /**
   * Checks, if a already exists a corpus with the same name of the top level
   * corpus in the corpus.tab file. If this is the case an Exception is thrown
   * and the import is aborted.
   *
   * @throws
   * annis.administration.DefaultAdministrationDao.ConflictingCorpusException
   */
  private void checkTopLevelCorpus() throws ConflictingCorpusException
  {
    String corpusName = getTopLevelCorpusFromTmpArea();
    if (existConflictingTopLevelCorpus(corpusName))
    {
      String msg
        = "There already exists a top level corpus with the name: " + corpusName;
      throw new ConflictingCorpusException(msg);
    }
  }

  /**
   * Checks, if there already exists a top level corpus.
   *
   * @param topLevelCorpusName The name of the corpus, which is checked.
   * @return Is false, if the no top level coprpus exists.
   */
  @Transactional(propagation = Propagation.MANDATORY)
  private boolean existConflictingTopLevelCorpus(String topLevelCorpusName)
  {
    String sql = "SELECT count(name) as amount FROM corpus WHERE name='"
      + topLevelCorpusName + "'";
    Integer numberOfCorpora = getJdbcTemplate().query(sql,
      new ResultSetExtractor<Integer>()
      {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException
        {
          if (rs.next())
          {
            return rs.getInt("amount");
          }
          else
          {
            return 0;
          }
        }
      });

    return numberOfCorpora > 0;
  }

  public static class ConflictingCorpusException extends AnnisException
  {

    public ConflictingCorpusException(String msg)
    {
      super(msg);
    }
  }

  /**
   * Deletes a top level corpus, when it is already exists.
   */
  private void checkAndRemoveTopLevelCorpus()
  {
    String corpusName = getTopLevelCorpusFromTmpArea();
    if (existConflictingTopLevelCorpus(corpusName))
    {
      log.info("delete conflicting corpus: {}", corpusName);
      List<String> corpusNames = new LinkedList<>();
      corpusNames.add(corpusName);
      deleteCorpora(annisDao.mapCorpusNamesToIds(corpusNames), false);
    }
  }

  @Override
  public void registerGUICancelThread(StatementController statementCon)
  {
    this.statementController = statementCon;
  }

}
