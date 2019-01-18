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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.corpus_tools.graphannis.CorpusStorageManager.ImportFormat;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Files;

import annis.ServiceConfig;
import annis.dao.QueryDao;
import annis.dao.SaltExport;
import annis.dao.autogenqueries.QueriesGenerator;
import annis.exceptions.AnnisException;
import annis.security.UserConfig;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.ImportJob;
import annis.service.objects.QueryLanguage;
import annis.tabledefs.ANNISFormatVersion;
import annis.tabledefs.Column;
import annis.tabledefs.Table;
import annis.utils.ANNISFormatHelper;
import au.com.bytecode.opencsv.CSVReader;

/**
 *
 */
public class AdministrationDao extends AbstractAdminstrationDao {

    private static final Logger log = LoggerFactory.getLogger(AdministrationDao.class);

    private final ServiceConfig cfg = ConfigFactory.create(ServiceConfig.class);

    private DeleteCorpusDao deleteCorpusDao;

    public AdministrationDao(QueriesGenerator queriesGenerator) {
        this.queriesGenerator = queriesGenerator;

        this.mimeTypeMapping = new LinkedHashMap<>();
        // TODO: make this configurable for the user
        this.mimeTypeMapping.put("webm", "video/webm");
        this.mimeTypeMapping.put("ogg", "audio/ogg");
        this.mimeTypeMapping.put("wav", "audio/wav");
        this.mimeTypeMapping.put("mp3", "audio/mpeg");
        this.mimeTypeMapping.put("flac", "audio/flac");
        this.mimeTypeMapping.put("mp4", "video/mp4");
        this.mimeTypeMapping.put("pdf", "application/pdf");
        this.mimeTypeMapping.put("css", "text/css");
        this.mimeTypeMapping.put("config", "application/x-config+text");
        this.mimeTypeMapping.put("properties", "application/text+plain");
        this.mimeTypeMapping.put("json", "application/json");

        this.generateExampleQueries = cfg.generateExampleQueries();

        this.jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

    }

    public static AdministrationDao create(QueryDao queryDao, DeleteCorpusDao deleteCorpusDao) {
        AdministrationDao adminDao = new AdministrationDao(QueriesGenerator.create(queryDao));
        adminDao.setQueryDao(queryDao);
        adminDao.setDeleteCorpusDao(deleteCorpusDao);

        return adminDao;
    }

    /**
     * Searches for textes which are empty or only contains whitespaces. If that is
     * the case the visualizer and no document visualizer are defined in the corpus
     * properties file a new file is created and stores a new config which disables
     * document browsing.
     *
     *
     * @param corpusID
     *                     The id of the corpus which texts are analyzed.
     */
    private void analyzeTextTable(String toplevelCorpusName) {
        List<String> rawTexts = getQueryDao().getRawText(toplevelCorpusName);

        // pattern for checking the token layer
        final Pattern WHITESPACE_MATCHER = Pattern.compile("^\\s+$");

        for (String s : rawTexts) {

            if (s != null && WHITESPACE_MATCHER.matcher(s).matches()) {
                // deactivate doc browsing if no document browser configuration
                // is
                // exists
                if (getQueryDao().getDocBrowserConfiguration(toplevelCorpusName) == null) {
                    // should exists anyway
                    Properties corpusConf;
                    try {
                        corpusConf = getQueryDao().getCorpusConfiguration(toplevelCorpusName);
                    } catch (FileNotFoundException ex) {
                        log.error("not found a corpus configuration, so skip analyzing the text table", ex);
                        return;
                    }

                    // disable document browsing if it is not explicit switch on
                    // by the
                    // user in the corpus.properties
                    boolean hasKey = corpusConf.containsKey("browse-documents");
                    boolean isActive = Boolean.parseBoolean(corpusConf.getProperty("browse-documents"));

                    if (!(hasKey && isActive)) {
                        log.info("disable document browser");
                        corpusConf.put("browse-documents", "false");
                        getQueryDao().setCorpusConfiguration(toplevelCorpusName, corpusConf);
                    }

                    // once disabled don't search in further texts
                    return;
                }
            }
        }
    }

    public ImportStatus initImportStatus() {
        return new CorpusAdministration.ImportStatsImpl();
    }

    public enum EXAMPLE_QUERIES_CONFIG {
        IF_MISSING, TRUE, FALSE
    }

    /**
     * If this is true and no example_queries.tab is found, automatic queries are
     * generated.
     */
    private EXAMPLE_QUERIES_CONFIG generateExampleQueries;

    private final String schemaVersion = "3.4.3";

    /**
     * A mapping for file-endings to mime types.
     */
    private final Map<String, String> mimeTypeMapping;

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
    // DO NOT CHANGE THE ORDER OF THIS LIST! Doing so may cause foreign key
    // failures during import.

    private final ObjectMapper jsonMapper = new ObjectMapper();

    private final QueriesGenerator queriesGenerator;

    private final Table resolverTable = new Table(FILE_RESOLVER_VIS_MAP)
            .c(new Column("id").type(Column.Type.INTEGER).primaryKey()).c("corpus").index("corpus").c("version")
            .c("namespace").c("element").c(new Column("vis_type").notNull()).c(new Column("display_name").notNull())
            .c("visibility").c_int("order").c("mappings");

    private final Table textTable = new Table("text").c("corpus_path").index("corpus_path").c_int("id").c("name")
            .c("text");

    private final Table annotationsTable = new Table("annotations").c("corpus").index("corpus").c("name").index("name")
            .c("value").c("type").c("sub_type").c("edge_name");

    private final Table metaDataCache = new Table("metadata_cache").c("corpus").index("corpus").c("path").c("type")
            .c("namespace").c("name").c("value").index("corpus", "type", "path").index("corpus", "type")
            .index("corpus", "path");

    private final Table mediaFilesTable = new Table("media_files").c(new Column("filename").unique()).c("corpus_path")
            .index("corpus_path").c("mime_type").index("mime_type").c("title").index("title");

    private final Table repositoryMetaDataTable = new Table("repository_metadata").c(new Column("name").unique())
            .c("value");

    private final Table urlShortenerTable = new Table("url_shortener").c(new Column("id").primaryKey()).c("owner")
            .c("created").c("url").index("url");

    private final Table exampleQueriesTable = new Table(EXAMPLE_QUERIES_TAB)
            .c(new Column("id").type(Column.Type.INTEGER).primaryKey()).c(new Column("example_query").notNull())
            .c(new Column("description").notNull()).c(new Column("corpus").notNull());

    private final Table userConfigTable = new Table("user_config").c(new Column("id").primaryKey()).c("config");

    private final Table corpusAliasTable = new Table("corpus_alias").c(new Column("alias").primaryKey()).c("corpus")
            .index("corpus");

    private final Table corpusInfoTable = new Table("corpus_info").c(new Column("name").primaryKey()).c_int("docs")
            .c_int("tokens").c("source_path");

    /**
     * Get the real schema name and version as used by the database.
     *
     * @return
     */
    public String getDatabaseSchemaVersion() {
        try (Connection conn = createConnection(DB.SERVICE_DATA, true)) {

            List<String> result = getQueryRunner().query(conn,
                    "SELECT \"value\" FROM repository_metadata WHERE \"name\"='schema-version'",
                    new ColumnListHandler<>(1));

            String schema = result.size() > 0 ? (String) result.get(0) : "";
            return schema;
        } catch (SQLException ex) {
            String error = "Wrong database schema (too old to get the exact number), "
                    + "please initialize the database.";
            log.error(error);
        }
        return "";
    }

    public boolean checkDatabaseSchemaVersion() throws AnnisException {

        // create all tables that do not exist yet
        initSQLiteSchema();

        String dbSchemaVersion = getDatabaseSchemaVersion();
        if (getSchemaVersion() != null && !getSchemaVersion().equalsIgnoreCase(dbSchemaVersion)) {
            String error = "Wrong database schema \"" + dbSchemaVersion + "\", please initialize the database.";
            log.error(error);
            throw new AnnisException(error);
        }
        return true;
    }

    /**
     * Reads ANNIS files from several directories.
     *
     * @param path
     *                              Specifies the path to the corpora, which should
     *                              be imported.
     * @param aliasName
     *                              An alias name for this corpus. Can be null.
     * @param overwrite
     *                              If set to true conflicting top level corpora are
     *                              deleted.
     * @param waitForOtherTasks
     *                              If true wait for other tasks to finish, if false
     *                              abort.
     *
     * @return true if successful
     */
    public boolean importCorpus(String path, String aliasName, boolean overwrite) {

        // this will throw an exception if the database has the wrong schema version
        checkDatabaseSchemaVersion();

        ANNISFormatVersion version = getANNISFormatVersion(path);

        if (version == ANNISFormatVersion.UNKNOWN) {
            log.error("Unknown ANNIS import format version");
            return false;
        }

        String toplevelCorpusName = ANNISFormatHelper
                .extractToplevelCorpusNames(new File(path, "corpus" + version.getFileSuffix()));

        if (toplevelCorpusName == null) {
            return false;
        }

        // remove conflicting top level corpora, when override is set to true.
        if (overwrite) {
            deleteCorpusDao.checkAndRemoveTopLevelCorpus(toplevelCorpusName);
        } else {
            checkTopLevelCorpus(toplevelCorpusName);
        }

        importBinaryData(path, toplevelCorpusName);

        try {
            convertToGraphANNIS(toplevelCorpusName, path, version);
            computeCorpusStatistics(toplevelCorpusName, path);
        } catch (GraphANNISException e) {
            log.error("Could not import graphANNIS", e);
            return false;
        }

        importTexts(toplevelCorpusName, path, version);
        importResolverTable(toplevelCorpusName, path, version);
        importExampleQueries(toplevelCorpusName, path, version);

        // create empty corpus properties file
        if (getQueryDao().getCorpusConfigurationSave(toplevelCorpusName) == null) {
            log.info("creating new corpus.properties file");
            getQueryDao().setCorpusConfiguration(toplevelCorpusName, new Properties());
        }

        analyzeTextTable(toplevelCorpusName);
        generateMetadataCache(toplevelCorpusName);
        generateAnnotationsTable(toplevelCorpusName);
        generateExampleQueries(toplevelCorpusName);

        if (aliasName != null && !aliasName.isEmpty()) {
            addCorpusAlias(toplevelCorpusName, aliasName);
        }
        return true;
    }

    ///// Subtasks of importing a corpus
    /**
     * Makes sure all tables needed by SQLite are available.
     *
     * @throws java.sql.SQLException
     */
    protected void initSQLiteSchema() {
        try {
            createTableIfNotExists(DB.SERVICE_DATA, repositoryMetaDataTable,
                    new File(getScriptPath(), "repository_metadata.annis"), null);
            createTableIfNotExists(DB.SERVICE_DATA, userConfigTable, null, null);
            createTableIfNotExists(DB.SERVICE_DATA, urlShortenerTable, null, null);

            createTableIfNotExists(DB.CORPUS_REGISTRY, corpusInfoTable, null, null);
            createTableIfNotExists(DB.CORPUS_REGISTRY, resolverTable,
                    new File(getScriptPath(), "resolver_vis_map.annis"), null);
            createTableIfNotExists(DB.CORPUS_REGISTRY, mediaFilesTable, null, null);
            createTableIfNotExists(DB.CORPUS_REGISTRY, exampleQueriesTable, null, null);
            createTableIfNotExists(DB.CORPUS_REGISTRY, corpusAliasTable, null, null);
            createTableIfNotExists(DB.CORPUS_REGISTRY, textTable, null, null);
            createTableIfNotExists(DB.CORPUS_REGISTRY, annotationsTable, null, null);
            createTableIfNotExists(DB.CORPUS_REGISTRY, metaDataCache, null, null);

        } catch (SQLException ex) {
            log.error("Can not create SQL schema", ex);
        }
    }

    protected void convertToGraphANNIS(String corpusName, String path, ANNISFormatVersion version)
            throws GraphANNISException {

        log.info("importing corpus into graphANNIS");
        getQueryDao().getCorpusStorageManager().importFromFileSystem(path, ImportFormat.RelANNIS, corpusName);

    }

    private void importResolverTable(String corpusName, String path, ANNISFormatVersion version) {

        log.info("importing resolver entries");
        File importDir = new File(path);

        Function<String[], String[]> lineModifier = (line) -> {
            if (line == null) {
                return line;
            }
            // check that the resolver entry is applied to correct corpus
            if (line[0] == null || !line[0].equals(corpusName)) {
                log.warn("resolver entry references wrong corpus \"" + line[0] + "\" and was rewritten");
                line[0] = corpusName;
            }
            if (line.length == 8) {
                String[] updateLine = new String[9];
                updateLine[0] = line[0];
                updateLine[1] = line[1];
                updateLine[2] = line[2];
                updateLine[3] = line[3];
                updateLine[4] = line[4];
                updateLine[5] = line[5];
                // use default value
                updateLine[6] = "hidden";
                updateLine[7] = line[6];
                updateLine[8] = line[7];

                return updateLine;
            } else {
                return line;
            }
        };

        try {
            importSQLiteTable(DB.CORPUS_REGISTRY, resolverTable,
                    new File(importDir, FILE_RESOLVER_VIS_MAP + version.getFileSuffix()), lineModifier);
        } catch (SQLException ex) {
            log.error("Could not import resolver file {}", path, ex);
        }
    }

    private void importExampleQueries(String corpusName, String path, ANNISFormatVersion version) {

        File importDir = new File(path);
        File exampleFile = new File(importDir, EXAMPLE_QUERIES_TAB + version.getFileSuffix());

        if (exampleFile.exists() && exampleFile.isFile()) {
            log.info("importing example queries entries");

            if (generateExampleQueries == (EXAMPLE_QUERIES_CONFIG.IF_MISSING)) {
                generateExampleQueries = EXAMPLE_QUERIES_CONFIG.FALSE;
            }

            Function<String[], String[]> lineModifier = (line) -> {
                if (line == null) {
                    return line;
                }
                // add the corpus name to the row
                if (line.length == 2) {
                    String[] updateLine = new String[3];
                    updateLine[0] = line[0];
                    updateLine[1] = line[1];
                    updateLine[2] = corpusName;
                    return updateLine;
                } else {
                    return line;
                }
            };

            try {
                importSQLiteTable(DB.CORPUS_REGISTRY, exampleQueriesTable, exampleFile, lineModifier);
            } catch (SQLException ex) {
                log.error("Could not import example queries file {}", path, ex);
            }
        } else {
            if (generateExampleQueries == EXAMPLE_QUERIES_CONFIG.IF_MISSING) {
                generateExampleQueries = EXAMPLE_QUERIES_CONFIG.TRUE;
            }

            log.info(EXAMPLE_QUERIES_TAB + version.getFileSuffix() + " file not found");
        }

    }

    private void importTexts(String corpusName, String path, ANNISFormatVersion version) {

        log.info("importing text table");
        File importDir = new File(path);

        // get the ID of all documents that are referenced in the text table
        final Map<Long, String> docID2Name = new HashMap<>();
        File corpusFile = new File(importDir, "corpus" + version.getFileSuffix());
        try (CSVReader csvReader = new CSVReader(
                new InputStreamReader(new FileInputStream(corpusFile), StandardCharsets.UTF_8), '\t', (char) 0)) {

            for (String[] line = csvReader.readNext(); line != null; line = csvReader.readNext()) {
                long id = Long.parseLong(line[0]);
                String name = line[1];
                String type = line[2];
                if ("DOCUMENT".equals(type)) {
                    docID2Name.put(id, name);
                }
            }

        } catch (IOException | ArrayIndexOutOfBoundsException | NumberFormatException | NullPointerException ex) {
            log.error("Failed to read file {}", corpusFile, ex);
        }

        // get the document ID for all texts by iterating over the node tabel, where
        // these are connected
        final Map<Long, Long> text2doc = new HashMap<>();
        if (version == ANNISFormatVersion.V3_1 || version == ANNISFormatVersion.V3_2) {
            File nodeFile = new File(importDir, "node" + version.getFileSuffix());
            try (CSVReader csvReader = new CSVReader(
                    new InputStreamReader(new FileInputStream(nodeFile), StandardCharsets.UTF_8), '\t', (char) 0)) {

                for (String[] line = csvReader.readNext(); line != null; line = csvReader.readNext()) {
                    long textRef = Long.parseLong(line[1]);
                    long docRef = Long.parseLong(line[2]);
                    text2doc.put(textRef, docRef);
                }

            } catch (IOException | ArrayIndexOutOfBoundsException | NumberFormatException | NullPointerException ex) {
                log.error("Failed to read file {}", corpusFile, ex);
            }
        }

        final Multiset<String> textsPerDoc = HashMultiset.create();

        Function<String[], String[]> lineModifier = (line) -> {
            if (line == null) {
                return line;
            }
            if (line.length == 4) {
                long docID = Long.parseLong(line[0]);
                String docName = docID2Name.get(docID);
                if (docName == null) {
                    log.warn("Could not import text with ID {} because document with ID {} was not "
                            + "found in corpus table", line[1], docID);
                    return null;
                }

                String[] updateLine = new String[4];
                updateLine[0] = corpusName + "/" + docName;
                updateLine[1] = line[1];
                updateLine[2] = line[2];
                updateLine[3] = line[3];
                return updateLine;
            } else if (line.length == 3) {
                long textID = Long.parseLong(line[0]);
                // text ID is globally unique, get the actual doc ID from the map
                Long docID = text2doc.get(textID);
                if (docID == null) {
                    log.warn("Could not import text with ID {} because no matching document was found"
                            + " in node table", line[1], textID);
                    return null;
                }
                String docName = docID2Name.get(docID);
                if (docName == null) {
                    log.warn("Could not import text with ID {} because document with ID {} was not "
                            + "found in corpus table", textID, docID);
                    return null;
                }

                String[] updateLine = new String[4];
                updateLine[0] = corpusName + "/" + docName;
                // give an relative ID to the text
                updateLine[1] = "" + textsPerDoc.count(docName);
                updateLine[2] = line[1];
                updateLine[3] = line[2];

                textsPerDoc.add(docName);

                return updateLine;
            } else {
                log.warn("Invalid text table entry detected and ignored: {}", (Object) line);
                return null;
            }
        };

        try {
            importSQLiteTable(DB.CORPUS_REGISTRY, textTable, new File(importDir, "text" + version.getFileSuffix()),
                    lineModifier);
        } catch (SQLException ex) {
            log.error("Could not import text table {}", path, ex);
        }
    }

    void importBinaryData(String path, String toplevelCorpusName) {
        log.info("importing all binary data from ExtData");
        File extData = new File(path + "/ExtData");
        if (extData.canRead() && extData.isDirectory()) {
            // import toplevel corpus media files
            File[] topFiles = extData.listFiles((FileFilter) FileFileFilter.FILE);
            if (topFiles != null) {
                for (File data : topFiles) {
                    String extension = FilenameUtils.getExtension(data.getName());
                    try {
                        if (mimeTypeMapping.containsKey(extension)) {
                            log.info("import " + data.getCanonicalPath() + " to staging area");

                            importSingleFile(data.getCanonicalPath(), toplevelCorpusName);
                        } else {
                            log.warn("not importing " + data.getCanonicalPath() + " since file type is unknown");
                        }
                    } catch (IOException ex) {
                        log.error("no canonical path given", ex);
                    }
                }
            }

            // get each subdirectory (which corresponds to an document name)
            File[] documents = extData.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
            if (documents != null) {
                for (File doc : documents) {
                    if (doc.isDirectory() && doc.canRead()) {
                        File[] dataFiles = doc.listFiles((FileFilter) FileFileFilter.FILE);
                        if (dataFiles != null) {
                            for (File data : dataFiles) {
                                String extension = FilenameUtils.getExtension(data.getName());
                                try {
                                    if (mimeTypeMapping.containsKey(extension)) {
                                        log.info("import " + data.getCanonicalPath() + " to staging area");

                                        importSingleFile(data.getCanonicalPath(),
                                                toplevelCorpusName + "/" + doc.getName());
                                    } else {
                                        log.warn("not importing " + data.getCanonicalPath()
                                                + " since file type is unknown");
                                    }
                                } catch (IOException ex) {
                                    log.error("no canonical path given", ex);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateAnnotationsTable(String corpusName) {
        log.info("Generating annotations table for corpus {}", corpusName);

        // list annotations with most-used value in graphANNIS
        List<AnnisAttribute> attributes = getQueryDao().listAnnotations(Arrays.asList(corpusName), true, true);

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY)) {
            conn.setAutoCommit(false);

            // cache these entries in the annotation table
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO annotations VALUES (?,?,?,?,?,?)");

            for (AnnisAttribute a : attributes) {
                if (a.getValueSet().isEmpty()) {
                    stmt.setString(1, corpusName);
                    stmt.setString(2, a.getName());
                    stmt.setString(3, null);
                    stmt.setString(4, a.getType().name());
                    stmt.setString(5, a.getSubtype().toString());
                    stmt.setString(6, a.getEdgeName());

                    stmt.executeUpdate();

                } else {
                    for (String val : a.getValueSet()) {
                        stmt.setString(1, corpusName);
                        stmt.setString(2, a.getName());
                        stmt.setString(3, val);
                        stmt.setString(4, a.getType().name());
                        stmt.setString(5, a.getSubtype().toString());
                        stmt.setString(6, a.getEdgeName());

                        stmt.executeUpdate();
                    }
                }
            }

            conn.commit();

        } catch (SQLException ex) {
            log.error("Cannot generate annotations table", ex);
        }
    }

    private void generateMetadataCache(String corpusName) {
        log.info("Generating metadata_cache table for corpus {}", corpusName);

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY)) {
            conn.setAutoCommit(false);

            SCorpusGraph corpusGraph = SaltExport
                    .mapCorpusGraph(getQueryDao().getCorpusStorageManager().corpusGraph(corpusName));

            // cache these entries in the metadata_cache table
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO metadata_cache(corpus, path, type, namespace, name, value) VALUES (?,?,?,?,?,?)");

            for (SCorpus corpus : corpusGraph.getCorpora()) {
                // add an entry for each meta annotation
                for (SMetaAnnotation anno : corpus.getMetaAnnotations()) {
                    stmt.setString(1, corpusName);
                    stmt.setString(2, Joiner.on('/').join(corpus.getPath().segmentsList()));
                    stmt.setString(3, "CORPUS");
                    if (anno.getNamespace() == null || anno.getNamespace().isEmpty()) {
                        stmt.setNull(4, Types.VARCHAR);
                    } else {
                        stmt.setString(4, anno.getNamespace());
                    }
                    stmt.setString(5, anno.getName());
                    stmt.setString(6, anno.getValue_STEXT());
                    stmt.executeUpdate();
                }
            }

            for (SDocument document : corpusGraph.getDocuments()) {
                // add an entry for the document itself
                stmt.setString(1, corpusName);
                stmt.setString(2, Joiner.on('/').join(document.getPath().segmentsList()));
                stmt.setString(3, "DOCUMENT");
                stmt.setString(4, "annis");
                stmt.setString(5, "doc");
                stmt.setString(6, document.getName());
                stmt.executeUpdate();

                for (SMetaAnnotation anno : document.getMetaAnnotations()) {
                    stmt.setString(1, corpusName);
                    stmt.setString(2, Joiner.on('/').join(document.getPath().segmentsList()));
                    stmt.setString(3, "DOCUMENT");
                    if (anno.getNamespace() == null || anno.getNamespace().isEmpty()) {
                        stmt.setNull(4, Types.VARCHAR);
                    } else {
                        stmt.setString(4, anno.getNamespace());
                    }
                    stmt.setString(5, anno.getName());
                    stmt.setString(6, anno.getValue_STEXT());
                    stmt.executeUpdate();
                }
            }

            conn.commit();

        } catch (SQLException | GraphANNISException ex) {
            log.error("Cannot generate annotations table", ex);
        }
    }

    /**
     * Imports a single binary file.
     *
     * @param file
     *                               Specifies the file to be imported.
     * @param toplevelCorpusName
     *                               The toplevel corpus name
     */
    private void importSingleFile(String file, String toplevelCorpusName) {

        BinaryImportHelper preStat = new BinaryImportHelper(file, getRealDataDir(), toplevelCorpusName,
                mimeTypeMapping);
        try (Connection conn = createConnection(DB.CORPUS_REGISTRY);
                PreparedStatement stmt = conn.prepareStatement(BinaryImportHelper.SQL)) {
            preStat.doInPreparedStatement(stmt);
        } catch (SQLException ex) {
            log.error("Cannot import binary file {}", file, ex);
        }
    }

    void computeCorpusStatistics(String toplevelCorpusName, String path) throws GraphANNISException {

        File f = new File(path);
        String absolutePath = path;
        try {
            absolutePath = f.getCanonicalPath();
        } catch (IOException ex) {
            log.error("Something went really wrong when calculating the canonical path", ex);
        }

        log.info("computing statistics for top-level corpus");

        // get number of tokens
        int tokCount = getQueryDao().count("tok", QueryLanguage.AQL, Arrays.asList(toplevelCorpusName));

        // get number of documents
        SCorpusGraph corpusGraph = SaltExport
                .mapCorpusGraph(getQueryDao().getCorpusStorageManager().corpusGraph(toplevelCorpusName));

        int documentCount = corpusGraph.getDocuments().size();

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY)) {

            getQueryRunner().update(conn,
                    "INSERT INTO corpus_info(\"name\", docs, tokens, source_path) VALUES(?,?,?,?)", toplevelCorpusName,
                    documentCount, tokCount, absolutePath);

        } catch (SQLException ex) {
            log.error("Could not insert corpus information into database", ex);
        }
    }

    ///// Other sub tasks

    /**
     * Delete files not used by this instance in the data directory.
     */
    public void cleanupData() {

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {

            List<String> allFilesInDatabaseList = getQueryRunner().query(conn, "SELECT filename FROM media_files AS m",
                    new ColumnListHandler<>(1));

            File dataDir = getRealDataDir();

            Set<File> allFilesInDatabase = new HashSet<>();
            for (String singleFileName : allFilesInDatabaseList) {
                allFilesInDatabase.add(new File(dataDir, singleFileName));
            }

            log.info("Cleaning up the data directory");
            // go through each file of the folder and check if it is not
            // included
            File[] childFiles = dataDir.listFiles();
            if (childFiles != null) {
                for (File f : childFiles) {
                    if (f.isFile() && !allFilesInDatabase.contains(f)) {
                        if (!f.delete()) {
                            log.warn("Could not delete {}", f.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            log.error("Error when cleaning up data", ex);
        }

    }

    /**
     * Provides a list where the keys are the aliases and the values are the corpus
     * names.
     *
     * @param dbFile
     * @return
     */
    public Multimap<String, String> listCorpusAlias(File dbFile) {
        Multimap<String, String> result = TreeMultimap.create();

        try (Connection conn = dbFile == null ? createConnection(DB.CORPUS_REGISTRY, true)
                : createConnection(dbFile, true)) {

            ResultSetHandler<Multimap<String, String>> rsh = new ResultSetHandler<Multimap<String, String>>() {
                @Override
                public Multimap<String, String> handle(ResultSet rs) throws SQLException {
                    Multimap<String, String> data = TreeMultimap.create();
                    while (rs.next()) {
                        // alias -> corpus name
                        data.put(rs.getString(1), rs.getString(2));
                    }
                    return data;
                }
            };
            result = getQueryRunner().query(conn, "SELECT alias, corpus FROM corpus_alias", rsh);

        } catch (SQLException ex) {
            if (dbFile == null) {
                log.error("Could not query corpus list", ex);
            } else {
                log.error("Could not query corpus list for the file " + dbFile.getAbsolutePath(), ex);
            }
        }

        return result;
    }

    public UserConfig retrieveUserConfig(final String userName) {
        String sql = "SELECT * FROM user_config WHERE id=?";
        UserConfig config = new UserConfig();

        try (Connection conn = createConnection(DB.SERVICE_DATA, true)) {
            ResultSetHandler<UserConfig> rsh = new ResultSetHandler<UserConfig>() {
                @Override
                public UserConfig handle(ResultSet rs) throws SQLException {
                    // default to empty config
                    UserConfig c = new UserConfig();

                    if (rs.next()) {
                        try {
                            c = jsonMapper.readValue(rs.getString("config"), UserConfig.class);
                        } catch (IOException ex) {
                            log.error("Could not parse JSON that is stored in database (user configuration)", ex);
                        }
                    }
                    return c;
                }

            };
            config = getQueryRunner().query(conn, sql, rsh, userName);

        } catch (SQLException ex) {
            log.error("Could not query user configuration for {}", userName, ex);
        }

        return config;
    }

    public void storeUserConfig(String userName, UserConfig config) {
        String sqlUpdate = "UPDATE user_config SET config=? WHERE id=?";
        String sqlInsert = "INSERT INTO user_config(id, config) VALUES(?,?)";
        try {
            String jsonVal = jsonMapper.writeValueAsString(config);

            try (Connection conn = createConnection(DB.SERVICE_DATA)) {
                conn.setAutoCommit(false);

                if (getQueryRunner().update(conn, sqlUpdate, jsonVal, userName) == 0) {
                    // if no row was affected there is no entry yet and we
                    // should create one
                    getQueryRunner().update(conn, sqlInsert, userName, jsonVal);
                }

                conn.commit();
            } catch (SQLException ex) {
                log.error("Could not store user configuration for {}", userName, ex);
            }
        } catch (IOException ex) {
            log.error("Cannot serialize user config JSON for database.", ex);
        }
    }

    public void deleteUserConfig(String userName) {
        try (Connection conn = createConnection(DB.SERVICE_DATA)) {
            conn.setAutoCommit(false);

            getQueryRunner().update(conn, "DELETE FROM user_config WHERE id=?", userName);

            conn.commit();
        } catch (SQLException ex) {
            log.error("Could not delete user configuration for {}", userName, ex);
        }
    }

    public void addCorpusAlias(String corpusName, String alias) {
        try (Connection conn = createConnection(DB.SERVICE_DATA)) {
            conn.setAutoCommit(false);

            getQueryRunner().update(conn, "INSERT INTO corpus_alias (alias, corpus) VALUES(?,?)", alias, corpusName);

            conn.commit();

            log.info("adding alias {} for corpus {}", alias, corpusName);
        } catch (SQLException ex) {
            log.error("Could add alias {} for corpus", alias, corpusName, ex);
        }
    }

    public void sendImportStatusMail(String adress, String corpusPath, ImportJob.Status status, String additionalInfo) {
        if (adress == null || adress.isEmpty() || corpusPath == null) {
            return;
        }

        // check valid properties
        if (cfg.mailSender() == null || cfg.mailSender().isEmpty()) {
            log.warn("Could not send status mail because \"annis.mail-sender\" "
                    + "property was not configured in conf/annis-service-properties.");
            return;
        }

        try {
            SimpleEmail mail = new SimpleEmail();
            List<InternetAddress> to = new LinkedList<>();
            to.add(new InternetAddress(adress));

            StringBuilder sbMsg = new StringBuilder();
            sbMsg.append("Dear Sir or Madam,\n");
            sbMsg.append("\n");
            sbMsg.append("this is the requested status update to the ANNIS corpus import "
                    + "you have started. Please note that this message is automated and "
                    + "if you have any question regarding the import you have to ask the "
                    + "administrator of the ANNIS instance directly.\n\n");

            mail.setTo(to);
            if (status == ImportJob.Status.SUCCESS) {
                mail.setSubject("ANNIS import finished successfully (" + corpusPath + ")");
                sbMsg.append("Status:\nThe corpus \"").append(corpusPath)
                        .append("\" was successfully imported and can be used from now on.\n");
            } else if (status == ImportJob.Status.ERROR) {
                mail.setSubject("ANNIS import *failed* (" + corpusPath + ")");
                sbMsg.append("Status:\nUnfortunally the corpus \"").append(corpusPath)
                        .append("\" could not be imported successfully. "
                                + "You may ask the administrator of the ANNIS installation for "
                                + "assistance why the corpus import failed.\n");
            } else if (status == ImportJob.Status.RUNNING) {
                mail.setSubject("ANNIS import started (" + corpusPath + ")");
                sbMsg.append("Status:\nThe import of the corpus \"").append(corpusPath).append("\" was started.\n");
            } else if (status == ImportJob.Status.WAITING) {
                mail.setSubject("ANNIS import was scheduled (" + corpusPath + ")");
                sbMsg.append("Status:\nThe import of the corpus \"").append(corpusPath)
                        .append("\" was scheduled and is currently waiting for other imports to "
                                + "finish. As soon as the previous imports are finished this import "
                                + "job will be executed.\n");
            } else {
                // we don't know how to handle this, just don't send a message
                return;
            }
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                sbMsg.append("Addtional information:\n");
                sbMsg.append(additionalInfo).append("\n");
            }

            sbMsg.append("\n\nSincerely yours,\n\nthe ANNIS import service.");
            mail.setMsg(sbMsg.toString());
            mail.setHostName("localhost");
            mail.setFrom(cfg.mailSender());

            mail.send();
            log.info("Send status ({}) mail to {}.", new String[] { status.name(), adress });

        } catch (AddressException | EmailException ex) {
            log.warn("Could not send mail: " + ex.getMessage());
        }
    }

    ///// Getter / Setter

    /**
     * Get the name and version of the schema this @{link AdministrationDao} is
     * configured to work with.
     *
     * @return
     */
    public String getSchemaVersion() {
        return schemaVersion;
    }

    public Map<String, String> getMimeTypeMapping() {
        return mimeTypeMapping;
    }

    /**
     * Generates example queries if no example queries tab file is defined by the
     * user.
     */
    private void generateExampleQueries(String toplevelCorpusName) {
        // set in the annis.properties file.
        if (generateExampleQueries == EXAMPLE_QUERIES_CONFIG.TRUE) {
            queriesGenerator.generateQueries(toplevelCorpusName);
        }
    }

    /**
     * @return the generateExampleQueries
     */
    public EXAMPLE_QUERIES_CONFIG isGenerateExampleQueries() {
        return generateExampleQueries;
    }

    /**
     * @param generateExampleQueries
     *                                   the generateExampleQueries to set
     */
    public void setGenerateExampleQueries(EXAMPLE_QUERIES_CONFIG generateExampleQueries) {
        this.generateExampleQueries = generateExampleQueries;
    }

    /**
     * @return the queriesGenerator
     */
    public QueriesGenerator getQueriesGenerator() {
        return queriesGenerator;
    }

    public DeleteCorpusDao getDeleteCorpusDao() {
        return deleteCorpusDao;
    }

    public void setDeleteCorpusDao(DeleteCorpusDao deleteCorpusDao) {
        this.deleteCorpusDao = deleteCorpusDao;
    }

    /**
     * Checks, if a already exists a corpus with the same name of the top level
     * corpus in the corpus.tab file. If this is the case an Exception is thrown and
     * the import is aborted.
     *
     * @param corpusName
     *
     * @throws annis.administration.DefaultAdministrationDao.ConflictingCorpusException
     */
    private void checkTopLevelCorpus(String corpusName) throws ConflictingCorpusException {
        if (existConflictingTopLevelCorpus(corpusName)) {
            String msg = "There already exists a top level corpus with the name: " + corpusName;
            throw new ConflictingCorpusException(msg);
        }
    }

    private ANNISFormatVersion getANNISFormatVersion(String path) {
        File pathDir = new File(path);
        if (pathDir.isDirectory()) {
            // check for existance of "annis.version" file
            File versionFile = new File(pathDir, "annis.version");
            if (versionFile.isFile() && versionFile.exists()) {
                try {
                    // read the first line
                    String firstLine = Files.readFirstLine(versionFile, Charsets.UTF_8);
                    if ("3.3".equals(firstLine.trim())) {
                        return ANNISFormatVersion.V3_3;
                    }
                } catch (IOException ex) {
                    log.warn("Could not read annis.version file", ex);
                }
            } else {
                // we have to distinguish between 3.1 and 3.2
                File nodeTab = new File(pathDir, "node.tab");
                if (nodeTab.isFile() && nodeTab.exists()) {
                    try {
                        String firstLine = Files.readFirstLine(nodeTab, Charsets.UTF_8);
                        List<String> cols = Splitter.on('\t').splitToList(firstLine);
                        if (cols.size() == 13) {
                            return ANNISFormatVersion.V3_2;
                        } else if (cols.size() == 10) {
                            return ANNISFormatVersion.V3_1;
                        }
                    } catch (IOException ex) {
                        log.warn("Could not read node.tab file", ex);
                    }
                }
            }
        }
        return ANNISFormatVersion.UNKNOWN;
    }

    public static class ConflictingCorpusException extends AnnisException {

        public ConflictingCorpusException(String msg) {
            super(msg);
        }
    }

}
