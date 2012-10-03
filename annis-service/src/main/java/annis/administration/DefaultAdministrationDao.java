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

import annis.exceptions.AnnisException;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.StringUtils;
import org.postgresql.PGConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * - Transaktionen - Datenbank-Zugriffsrechte für verschiedene Methoden -
 * Reihenfolge der Aufrufe - Skripte in $ANNIS_HOME/scripts
 */
// FIXME: nothing in SpringAnnisAdministrationDao is tested
public class DefaultAdministrationDao implements AdministrationDao
{
  
  private static final Logger log = LoggerFactory.getLogger(AdministrationDao.class);
  // external files path
  private String externalFilesPath;
  // script path
  private String scriptPath;
  // use Spring's JDBC support
  private NamedParameterJdbcTemplate jdbcTemplate;
  //private JdbcOperations jdbcOperations;
  // save the datasource to manually retrieve connections (needed for bulk-import)
  private DataSource dataSource;
  private boolean temporaryStagingArea;
  private String schemaVersion;
  private Map<String, String> mimeTypeMapping;
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
    FILE_RESOLVER_VIS_MAP
  };
  private String[] tablesToCopyManually =
  {
    "corpus", "corpus_annotation",
    "text",
    FILE_RESOLVER_VIS_MAP,
    "corpus_stats",
    "media_files"
  };
  // tables created during import
  private String[] createdTables =
  {
    "corpus_stats",
    "media_files"
  };
  private String dbLayout;

  ///// Subtasks of creating the database
  @Override
  public void dropDatabase(String database)
  {
    String sql = "SELECT count(*) FROM pg_database WHERE datname = :database";
    SqlParameterSource args = makeArgs().addValue("database", database);
    int count = jdbcTemplate.queryForInt(sql, args);
    if (count != 0)
    {
      log.debug("dropping existing database");
      jdbcTemplate.getJdbcOperations().execute("DROP DATABASE " + database);
    }
  }
  
  @Override
  public void dropUser(String username)
  {
    String sql = "SELECT count(*) FROM pg_user WHERE usename = :username";
    SqlParameterSource args = makeArgs().addValue("username", username);
    int count = jdbcTemplate.queryForInt(sql, args);
    if (count != 0)
    {
      log.debug("dropping existing user");
      jdbcTemplate.getJdbcOperations().execute("DROP USER " + username);
    }
  }
  
  @Override
  public void createUser(String username, String password)
  {
    log.info("creating user: " + username);
    jdbcTemplate.getJdbcOperations().execute("CREATE USER " + username + " PASSWORD '" + password
      + "'");
  }
  
  @Override
  public void createDatabase(String database)
  {
    log.info("creating database: " + database
      + " ENCODING = 'UTF8' TEMPLATE template0");
    jdbcTemplate.getJdbcOperations().execute("CREATE DATABASE " + database
      + " ENCODING = 'UTF8' TEMPLATE template0");
  }
  
  @Override
  public void setupDatabase()
  {
    installPlPgSql();
    createFunctionUniqueToplevelCorpusName();
  }
  
  protected void installPlPgSql()
  {
    log.info("installing stored procedure language plpgsql");
    try
    {
      jdbcTemplate.getJdbcOperations().execute("CREATE LANGUAGE plpgsql");
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
  
  @Override
  public void createSchema()
  {
    log.info("creating ANNIS database schema (" + dbLayout + ")");
    executeSqlFromScript(dbLayout + "/schema.sql");
    
    jdbcTemplate.getJdbcOperations().execute("INSERT INTO repository_metadata "
      + "VALUES ('schema-version', '"
      + StringUtils.replace(getSchemaVersion(), "'", "''") + "');");
    
  }
  
  @Override
  public void createSchemaIndexes()
  {
    log.info("creating ANNIS database schema indexes (" + dbLayout + ")");
    executeSqlFromScript(dbLayout + "/schemaindex.sql");
  }
  
  @Override
  public void populateSchema()
  {
    log.info("populating the schemas with default values");
    bulkloadTableFromResource("resolver_vis_map",
      new FileSystemResource(new File(scriptPath, "resolver_vis_map.tab")));
    // update the sequence
    executeSqlFromScript("update_resolver_sequence.sql");
  }
  
  @Override
  @Transactional(readOnly = true)
  public String getDatabaseSchemaVersion()
  {
    try
    {
      Map<String, String> map = new HashMap<String, String>();
      
      List<Map<String, Object>> result = jdbcTemplate.queryForList(
        "SELECT \"value\" FROM repository_metadata WHERE \"name\"='schema-version'",
        map);
      
      String schema =
        result.size() > 0 ? (String) result.get(0).get("value") : "";
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
    if (getSchemaVersion() != null && !getSchemaVersion().equalsIgnoreCase(dbSchemaVersion))
    {
      String error = "Wrong database schema \"" + dbSchemaVersion + "\", please initialize the database.";
      log.error(error);
      throw new AnnisException(error);
    }
    return true;
  }
  
  @Override
  @Transactional(readOnly = false)
  public void importCorpus(String path)
  {

    // check schema version first
    checkDatabaseSchemaVersion();
    
    createStagingArea(temporaryStagingArea);
    bulkImport(path);
    
    createStagingAreaIndexes();
    
    computeTopLevelCorpus();
    analyzeStagingTables();
    
    computeLeftTokenRightToken();

//    if (true) return;

    adjustRankPrePost();
    long corpusID = updateIds();
    
    importBinaryData(path);
    extendStagingText(corpusID);
    
    computeRealRoot();

//    if (true) return;

    computeLevel();
    computeCorpusStatistics(path);
    updateCorpusStatsId(corpusID);
    
    applyConstraints();
    analyzeStagingTables();
    
    insertCorpus();
    
    computeCorpusPath(corpusID);
    
    createAnnotations(corpusID);

    // create the new facts table partition
    createFacts(corpusID);
    // the entries, which where here done, are possible after generating facts
    updateCorpusStatistic(corpusID);
    
    
    if (temporaryStagingArea)
    {
      dropStagingArea();
    }
    analyzeFacts(corpusID);
    
  }

  ///// Subtasks of importing a corpus
  protected void dropIndexes()
  {
    log.info("dropping indexes");
    for (String index : listIndexesOnTables(allTables()))
    {
      log.debug("dropping index: " + index);
      jdbcTemplate.getJdbcOperations().execute("DROP INDEX " + index);
    }
  }
  
  void createStagingArea(boolean useTemporary)
  {
    log.info("creating staging area");
    MapSqlParameterSource args = makeArgs().addValue(":tmp", useTemporary
      ? "TEMPORARY" : "UNLOGGED");
    executeSqlFromScript("staging_area.sql", args);
  }
  
  void bulkImport(String path)
  {
    log.info("bulk-loading data");
    
    
    for (String table : importedTables)
    {
      if (table.equalsIgnoreCase(FILE_RESOLVER_VIS_MAP))
      {
        try
        {

          // count cols for detecting old resolver_vis_map table format
          JdbcOperations sqlOps = jdbcTemplate.getJdbcOperations();
          File resolver_vis_tab = new File(path, table + ".tab");
          BufferedReader bReader = new BufferedReader(
            new FileReader(resolver_vis_tab));
          String firstLine = bReader.readLine();
          
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
              StringBuilder sb = new StringBuilder();
              sb.append("CREATE TABLE tmp_resolver_vis_map ");
              sb.append("( ");
              sb.append("\"corpus\"   varchar, ");
              sb.append("\"version\" 	varchar, ");
              sb.append("\"namespace\"	varchar, ");
              sb.append("\"element\"    varchar, ");
              sb.append("\"vis_type\"   varchar NOT NULL, ");
              sb.append("\"display_name\"   varchar NOT NULL, ");
              sb.append("\"order\" bigint default '0', ");
              sb.append("\"mappings\" varchar");
              sb.append(");");
              
              sqlOps.execute(sb.toString());
              
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
              
              sqlOps.execute(sb.toString());
              sqlOps.execute("DROP TABLE tmp_resolver_vis_map;");
              
              break;

            // new format
            case 9:
              bulkloadTableFromResource(tableInStagingArea(table),
                new FileSystemResource(new File(path, table + ".tab")));
              break;
            default:
              log.error("invalid amount of cols");
              throw new RuntimeException();
          }
          
        }
        catch (IOException e)
        {
          log.error("could not read {}", table + ".tab", e);
        }
        catch (FileAccessException e)
        {
          log.error("could not read {}", table + ".tab", e);
        }
      }
      else if (table.equalsIgnoreCase("node"))
      {
        bulkImportNode(path);
      }
      else
      {
        bulkloadTableFromResource(tableInStagingArea(table),
          new FileSystemResource(new File(path, table + ".tab")));
      }
    }
  }
  
  private void bulkImportNode(String path)
  {
    try
    {
      // check column number by reading first line
      File nodeTabFile = new File(path, "node.tab");
      BufferedReader reader =
        new BufferedReader(new FileReader(nodeTabFile));
      String firstLine = reader.readLine();
      
      int columnNumber = firstLine.split("\t").length;
      if (columnNumber == 13)
      {
        // new node table with segmentations
        // no special handling needed
        bulkloadTableFromResource(tableInStagingArea("node"),
          new FileSystemResource(nodeTabFile));
      }
      else if (columnNumber == 10)
      {
        // old node table without segmentations
        // create temporary table for  bulk import
        jdbcTemplate.getJdbcOperations().execute(
          "CREATE TEMPORARY TABLE _tmpnode"
          + "\n(\n"
          + "id bigint,\n"
          + "text_ref bigint,\n"
          + "corpus_ref bigint,\n"
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
        jdbcTemplate.getJdbcOperations().execute(
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
  
  void importBinaryData(String path)
  {
    log.info("importing all binary data from ExtData");
    File extData = new File(path + "/ExtData");
    if (extData.canRead() && extData.isDirectory())
    {
      // get each subdirectory (which corresponds to an document name)
      File[] documents = extData.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
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
                log.info("import " + data.getCanonicalPath() + " to staging area");
                
                // search for corpus_ref
                String sqlScript =
                  "SELECT id FROM _corpus WHERE \"name\" = ? LIMIT 1";
                long corpusID = jdbcTemplate.getJdbcOperations().queryForLong(sqlScript, doc.getName());
                
                importSingleFile(data.getCanonicalPath(), corpusID);
              }
              else
              {
                log.warn("not importing " + data.getCanonicalPath() + " since file type is unknown");
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
  
  @Deprecated
  private void importBinaryDataFromExtFileAnnotations(String path)
  {
    log.info("importing binary data");

    // pattern marks binary data in the bulk files and corresponding regexp
    String extFilePattern = "\\[ExtFile\\]";

    // search for annotations that have binary data
    String selectSql =
      "SELECT DISTINCT value, corpus_ref FROM _node, _node_annotation WHERE value LIKE "
      + "'[ExtFile]%'" + " AND node_ref = id";

    /*
     * we need the value and the corpus_ref of the media_file, so the first
     * value of the String array is the name of the file and the second is the
     * corpus_ref
     */
    List<String[]> list = jdbcTemplate.getJdbcOperations().query(
      selectSql,
      new ResultSetExtractor<List<String[]>>()
      {
        @Override
        public List<String[]> extractData(ResultSet rs) throws SQLException,
          DataAccessException
        {
          ArrayList<String[]> result = new ArrayList<String[]>();
          while (rs.next())
          {
            String[] tmp =
            {
              rs.getString("value"), rs.getString("corpus_ref")
            };
            result.add(tmp);
          }
          return result;
        }
      });
    
    for (String[] externalData : list)
    {
      assert externalData.length > 1;
      // get rid of marker
      String filename = externalData[0].replaceFirst(extFilePattern, "");
      
      log.info("import " + filename + " to staging area");
      MediaImportPreparedStatementCallbackImpl preStat = new MediaImportPreparedStatementCallbackImpl(path
        + "/ExtData/" + filename, Long.parseLong(externalData[1]), mimeTypeMapping);
      String sqlScript = "INSERT INTO _media_files VALUES (?, ?, ?, ?, ?)";
      
      jdbcTemplate.getJdbcOperations().execute(sqlScript, preStat);


      // update annotation value, set name to audio:audioFile
      String updateValueSql =
        "UPDATE _node_annotation SET value = :id, name = 'externalFile', namespace = 'external' WHERE value = :externalData";
      SqlParameterSource updateArgs = makeArgs().addValue("id", Types.BIGINT).addValue(
        "externalData", externalData[0], Types.VARCHAR);
      
      jdbcTemplate.update(updateValueSql, updateArgs);
    }
  }
  
  private void importSingleFile(String path, long corpusRef)
  {
    MediaImportPreparedStatementCallbackImpl preStat = new MediaImportPreparedStatementCallbackImpl(path, 
      corpusRef, mimeTypeMapping);
    String sqlScript = "INSERT INTO _media_files VALUES (?, ?, ?, ?, ?)";

    jdbcTemplate.getJdbcOperations().execute(sqlScript, preStat);

  }
  
  void extendStagingText(long toplevelID)
  {
    log.info("extending _text");;
    executeSqlFromScript("extend_staging_text.sql", makeArgs().addValue(":id", toplevelID));
  }
  
  void computeLeftTokenRightToken()
  {
    log.info("computing values for struct.left_token and struct.right_token");
    executeSqlFromScript("left_token_right_token.sql");
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
      log.error("Something went really wrong when calculating the canonical path", ex);
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
    log.info("analyzing rank");
    jdbcTemplate.getJdbcOperations()
      .execute("ANALYZE " + tableInStagingArea("rank"));
  }

  /**
   *
   * @return the new corpus ID
   */
  long updateIds()
  {
    log.info("updating IDs in staging area");
    
    int numOfEntries = jdbcTemplate.getJdbcOperations().queryForInt(
      "SELECT COUNT(*) from corpus_stats");
    
    long recentCorpusId = 0;
    
    if (numOfEntries > 0)
    {
      recentCorpusId = jdbcTemplate.getJdbcOperations().queryForLong(
        "SELECT max(id) FROM corpus_stats");
      log.info("the id from recently imported corpus:" + recentCorpusId);
    }
    
    MapSqlParameterSource args = makeArgs().addValue(":id", recentCorpusId);
    executeSqlFromScript("update_ids.sql", args);
    
    log.info("query for the new corpus ID");
    long result = jdbcTemplate.getJdbcOperations().queryForLong(
      "SELECT MAX(toplevel_corpus) FROM _node");
    log.info("new corpus ID is " + result);
    return result;
  }
  
  void updateCorpusStatsId(long corpusId)
  {
    log.info("updating corpus ID in corpus_stat");
    jdbcTemplate.getJdbcOperations().update("UPDATE _corpus_stats SET id = " + corpusId);
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
      int numOfEntries = jdbcTemplate.getJdbcOperations().queryForInt("SELECT COUNT(*) from "
        + tableInStagingArea(table));
      
      
      if (numOfEntries > 0)
      {
        StringBuilder sql = new StringBuilder();
        
        if (table.equalsIgnoreCase(FILE_RESOLVER_VIS_MAP))
        {
          sql.append("INSERT INTO ");
          sql.append(table);
          //FIXME DIRTY!!! find a better way instead of naming the column-names in code 
          sql.append(
            "(corpus, version, namespace, element, vis_type, display_name, visibility, \"order\", mappings)");
          sql.append(" (SELECT corpus, version, namespace, element, vis_type, display_name, visibility::resolver_visibility, \"order\", mappings FROM ");
          sql.append(tableInStagingArea(table)).append(")");
        }
        else
        {
          sql.append("INSERT INTO ");
          sql.append(table);
          sql.append(" (SELECT * FROM ");
          sql.append(tableInStagingArea(table)).append(")");
        }
        jdbcTemplate.getJdbcOperations().execute(sql.toString());
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
      jdbcTemplate.getJdbcOperations()
        .execute("DROP TABLE " + tableInStagingArea(table));
    }
    
  }
  
  void dropMaterializedTables()
  {
    log.info("dropping materialized tables");
    
    jdbcTemplate.getJdbcOperations().execute("DROP TABLE facts");
    
  }
  
  void analyzeStagingTables()
  {
    for (String t : importedTables)
    {
      log.info("analyzing " + t);
      jdbcTemplate.getJdbcOperations()
        .execute("ANALYZE " + tableInStagingArea(t));
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
  
  void analyzeFacts(long corpusID)
  {
    log.info("analyzing facts table for corpus with ID " + corpusID);
    jdbcTemplate.getJdbcOperations().execute("ANALYZE facts_" + corpusID);
    log.info("analyzing general facts table");
    jdbcTemplate.getJdbcOperations().execute("ANALYZE facts");
  }
  
  void createFacts(long corpusID)
  {
    
    MapSqlParameterSource args = makeArgs().addValue(":id", corpusID);
    
    log.info("creating materialized facts table for corpus with ID " + corpusID);
    executeSqlFromScript(dbLayout + "/facts.sql", args);
    
    clusterFacts(corpusID);
    
    log.info("indexing the new facts table (corpus with ID " + corpusID + ")");
    executeSqlFromScript(dbLayout + "/indexes.sql", args);
    
  }
  
  void clusterFacts(long corpusID)
  {
    MapSqlParameterSource args = makeArgs().addValue(":id", corpusID);
    
    log.info("clustering materialized facts table for corpus with ID "
      + corpusID);
    if (!executeSqlFromScript(dbLayout + "/cluster.sql", args))
    {
      executeSqlFromScript("cluster.sql", args);
    }
  }

  ///// Other sub tasks
  @Override
  public List<Long> listToplevelCorpora()
  {
    String sql = "SELECT id FROM corpus WHERE top_level = 'y'";
    
    
    return jdbcTemplate.getJdbcOperations()
      .query(sql, ParameterizedSingleColumnRowMapper.
      newInstance(Long.class));
  }
  
  @Transactional(readOnly = false)
  @Override
  public void deleteCorpora(List<Long> ids)
  {
    for (long l : ids)
    {
      log.debug("dropping facts table for corpus " + l);
      jdbcTemplate.getJdbcOperations().execute("DROP TABLE IF EXISTS facts_" + l);
      jdbcTemplate.getJdbcOperations().execute("DROP TABLE IF EXISTS facts_edge_" + l);
      jdbcTemplate.getJdbcOperations().execute("DROP TABLE IF EXISTS facts_node_" + l);
      log.debug("dropping annotation_pool table for corpus " + l);
      jdbcTemplate.getJdbcOperations()
        .execute("DROP TABLE IF EXISTS annotation_pool_" + l);
      log.debug("dropping annotations table for corpus " + l);
      jdbcTemplate.getJdbcOperations()
        .execute("DROP TABLE IF EXISTS annotations_" + l);
    }
    
    log.debug("recursivly deleting corpora: " + ids);
    executeSqlFromScript("delete_corpus.sql", makeArgs().addValue(":ids",
      StringUtils.join(ids, ", ")));
    
  }
  
  @Override
  public List<Map<String, Object>> listCorpusStats()
  {
    return jdbcTemplate.getJdbcOperations()
      .queryForList(
      "SELECT * FROM corpus_info ORDER BY name");
  }
  
  @Override
  public List<Map<String, Object>> listTableStats()
  {
    return jdbcTemplate.getJdbcOperations()
      .queryForList("SELECT * FROM table_stats");
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

  ///// Helpers
  private List<String> importedAndCreatedTables()
  {
    List<String> tables = new ArrayList<String>();
    tables.addAll(Arrays.asList(importedTables));
    tables.addAll(Arrays.asList(createdTables));
    return tables;
  }
  
  private List<String> allTables()
  {
    List<String> tables = new ArrayList<String>();
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
    Map parameters = args != null ? args.getValues() : new HashMap();
    BufferedReader reader = null;
    try
    {
      StringBuilder sqlBuf = new StringBuilder();
      reader = new BufferedReader(new FileReader(resource.getFile()));
      for (String line = reader.readLine(); line != null; line =
          reader.readLine())
      {
        sqlBuf.append(line).append("\n");
      }
      String sql = sqlBuf.toString();
      for (Object placeHolder : parameters.keySet())
      {
        String key = placeHolder.toString();
        String value = parameters.get(placeHolder).toString();
        log.debug("substitution for parameter '" + key + "' in SQL script: "
          + value);
        
        sql = sql.replaceAll(key, value);
      }
      return sql;
    }
    catch (IOException e)
    {
      log.error("Couldn't read SQL script from resource file.", e);
      throw new FileAccessException(
        "Couldn't read SQL script from resource file.", e);
    }
    finally
    {
      if (reader != null)
      {
        try
        {
          reader.close();
          
          
          
          
        }
        catch (IOException ex)
        {
          java.util.logging.Logger.getLogger(DefaultAdministrationDao.class
            .
            getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }

  // executes an SQL script from $ANNIS_HOME/scripts
  @Override
  public boolean executeSqlFromScript(String script)
  {
    return executeSqlFromScript(script, null);
  }

  // executes an SQL script from $ANNIS_HOME/scripts, substituting the parameters found in args
  @Override
  public boolean executeSqlFromScript(String script, MapSqlParameterSource args)
  {
    File fScript = new File(scriptPath, script);
    if (fScript.canRead() && fScript.isFile())
    {
      Resource resource = new FileSystemResource(fScript);
      log.debug("executing SQL script: " + resource.getFilename());
      String sql = readSqlFromResource(resource, args);
      jdbcTemplate.getJdbcOperations().execute(sql);
      return true;
    }
    else
    {
      log.debug("SQL script " + fScript.getName() + " does not exist");
      return false;
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
      Connection con = DataSourceUtils.getConnection(dataSource);

      // Postgres JDBC4 8.4 driver now supports the copy API
      PGConnection pgCon = (PGConnection) con;
      pgCon.getCopyAPI().copyIn(sql, resource.getInputStream());
      
      DataSourceUtils.releaseConnection(con, dataSource);
      
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
    String sql =
      ""
      + "SELECT indexname "
      + "FROM pg_indexes "
      + "WHERE tablename IN ( :tables ) "
      + "AND lower(indexname) NOT IN "
      + "	(SELECT lower(conname) FROM pg_constraint WHERE contype in ('p', 'u'))";
    SqlParameterSource args = makeArgs().addValue("tables", tables);
    return jdbcTemplate.query(sql, args, stringRowMapper());
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
    String sql =
      "SELECT pg_get_indexdef(x.indexrelid) AS indexdef "
      + "FROM pg_index x, pg_class c "
      + "WHERE x.indexrelid = c.oid "
      + "AND c.relname IN ( :indexes ) "
      + "AND pg_stat_get_numscans(x.indexrelid) " + scansOp + " 0";
    SqlParameterSource args = makeArgs().addValue("indexes",
      listIndexesOnTables(tables));
    return jdbcTemplate.query(sql, args, stringRowMapper());
  }
  
  private List<String> quotedArray(String... values)
  {
    List<String> result = new ArrayList<String>();
    for (String value : values)
    {
      result.add("'" + value + "'");
    }
    return result;
  }
  
  public List<String> listIndexDefinitions(String... tables)
  {
    String template = ""
      + "SELECT pg_get_indexdef(x.indexrelid) AS indexdef "
      + "FROM pg_index x, pg_class c, pg_indexes i "
      + "WHERE x.indexrelid = c.oid "
      + "AND c.relname = i.indexname "
      + "AND i.tablename IN ( :tables )";
    String sql = template.replaceAll(":tables", StringUtils.join(quotedArray(
      tables), ", "));
    return jdbcTemplate.getJdbcOperations().query(sql,
      new ParameterizedSingleColumnRowMapper<String>());
  }
  
  public List<String> listUsedIndexes(String... tables)
  {
    String template = ""
      + "SELECT pg_get_indexdef(x.indexrelid) AS indexdef "
      + "FROM pg_index x, pg_class c, pg_indexes i "
      + "WHERE x.indexrelid = c.oid "
      + "AND c.relname = i.indexname "
      + "AND i.tablename IN ( :tables ) "
      + "AND pg_stat_get_numscans(x.indexrelid) != 0";
    String sql = template.replaceAll(":tables", StringUtils.join(quotedArray(
      tables), ", "));
    return jdbcTemplate.getJdbcOperations().query(sql,
      new ParameterizedSingleColumnRowMapper<String>());
  }
  
  public boolean resetStatistics()
  {
    try
    {
      jdbcTemplate.getJdbcOperations().queryForList("SELECT pg_stat_reset()");
      return true;
    }
    catch (DataAccessException e)
    {
      return false;
    }
  }

  ///// Getter / Setter
  @Override
  public void setDataSource(DataSource dataSource)
  {
    this.dataSource = dataSource;
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }
  
  public NamedParameterJdbcTemplate getJdbcTemplate()
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
  
  public void setExternalFilesPath(String externalFilesPath)
  {
    this.externalFilesPath = externalFilesPath;
  }
  
  public String getDbLayout()
  {
    return dbLayout;
  }
  
  public void setDbLayout(String dbLayout)
  {
    this.dbLayout = dbLayout;
  }
  
  public boolean isTemporaryStagingArea()
  {
    return temporaryStagingArea;
  }
  
  public void setTemporaryStagingArea(boolean temporaryStagingArea)
  {
    this.temporaryStagingArea = temporaryStagingArea;
  }
  
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
}
