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
package annis.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.corpus_tools.annis.ql.parser.AnnisParserAntlr;
import org.corpus_tools.annis.ql.parser.QueryData;
import org.corpus_tools.graphannis.QueryToJSON;
import org.corpus_tools.graphannis.api.CorpusStorageManager;
import org.corpus_tools.graphannis.api.LogLevel;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.SaltUtil;
import org.corpus_tools.salt.util.internal.persistence.SaltXML10Writer;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.escape.Escaper;
import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;

import annis.CommonHelper;
import annis.examplequeries.ExampleQuery;
import annis.exceptions.AnnisException;
import annis.exceptions.AnnisTimeoutException;
import annis.model.AnnisConstants;
import annis.model.Annotation;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisBinaryMetaData;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.CorpusConfig;
import annis.service.objects.CorpusConfigMap;
import annis.service.objects.DocumentBrowserConfig;
import annis.service.objects.FrequencyTable;
import annis.service.objects.Match;
import annis.service.objects.MatchAndDocumentCount;
import annis.service.objects.MatchGroup;
import annis.sqlgen.ByteHelper;
import annis.sqlgen.ListCorpusAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListExampleQueriesHelper;
import annis.sqlgen.MetaByteHelper;
import annis.sqlgen.SqlGenerator;
import annis.sqlgen.extensions.AnnotateQueryData;
import annis.sqlgen.extensions.LimitOffsetQueryData;

public class QueryDaoImpl extends AbstractDao implements QueryDao, SqlSessionModifier {

    // generated sql for example queries and fetches the result
    private ListExampleQueriesHelper listExampleQueriesHelper;

    private String externalFilesPath;

    // configuration
    private int timeout;

    private final CorpusStorageManager corpusStorageMgr;

    private final ExecutorService exec = Executors.newCachedThreadPool();

    private final Escaper corpusNameEscaper = UrlEscapers.urlPathSegmentEscaper();

    @Override
    public SaltProject graph(QueryData data) {
        SaltProject p = SaltFactory.createSaltProject();

        SCorpusGraph corpusGraph = p.createCorpusGraph();
        SCorpus rootCorpus = corpusGraph.createCorpus(null, "root");

        List<MatchGroup> matchGroupList = data.getExtensions(MatchGroup.class);
        List<AnnotateQueryData> annoExtList = data.getExtensions(AnnotateQueryData.class);
        if (!matchGroupList.isEmpty() && !annoExtList.isEmpty()) {
            MatchGroup mg = matchGroupList.get(0);
            AnnotateQueryData annoExt = annoExtList.get(0);

            int i = 0;

            for (Match m : mg.getMatches()) {
                SDocumentGraph docGraph = fetchDocumentWithContext(m, annoExt);
                SDocument doc = corpusGraph.createDocument(rootCorpus, "match" + i++);
                doc.setDocumentGraph(docGraph);

                CommonHelper.addMatchToDocumentGraph(m, doc);
            }
        }

        return p;
    }

    private SDocumentGraph fetchDocumentWithContext(Match m, AnnotateQueryData annoExt) {

        String corpusName = null;
        // find all covered token
        List<String> matchedIDs = new LinkedList<>();
        for (java.net.URI id : m.getSaltIDs()) {
            if (corpusName == null) {
                // use first node as template for the corpus name
                corpusName = CommonHelper.getCorpusPath(id).get(0);
            }
            matchedIDs.add(id.toASCIIString());
        }

        SDocumentGraph result = corpusStorageMgr.subgraph(corpusName, matchedIDs, annoExt.getLeft(),
                annoExt.getRight());

        return result;
    }

    @Override
    public List<Annotation> listDocuments(String toplevelCorpusName) {
        
        SCorpusGraph corpusGraph = corpusStorageMgr.corpusGraph(toplevelCorpusName);
        
        List<Annotation> result = new LinkedList<>();
        for(SDocument doc : corpusGraph.getDocuments()) {
            Annotation anno = new Annotation();
            anno.setName(doc.getName());
            anno.setAnnotationPath(doc.getPath().segmentsList());
            result.add(anno);
        }
        
        return result;
    }


    @Override
    public InputStream getBinaryComplete(String toplevelCorpusName, String mimeType, String title) {
        List<AnnisBinaryMetaData> binaryMetas = getBinaryMeta(toplevelCorpusName);
        InputStream input = null;

        if (binaryMetas != null) {
            for (AnnisBinaryMetaData metaData : binaryMetas) {
                if (mimeType.equals(metaData.getMimeType()) && title.equals(metaData.getFileName())) {
                    String filePath = getRealDataDir().getPath() + "/" + metaData.getLocalFileName();
                    try {
                        input = new FileInputStream(filePath);
                        return input;
                    } catch (FileNotFoundException ex) {
                        log.error("could not found binary file {}", filePath, ex);
                    }
                }
            }
        }

        return input;
    }

    @Override
    public List<AnnisBinaryMetaData> getBinaryMeta(String toplevelCorpusName) {
        return getBinaryMeta(toplevelCorpusName, null);
    }

    @Override
    public HashMap<String, Properties> getCorpusConfiguration() {
        return corpusConfiguration;
    }

    @Override
    public List<String> getRawText(String topLevelCorpus, String documentName) {
        if (topLevelCorpus == null || documentName == null) {
            throw new IllegalArgumentException("top level corpus and document name may not be null");
        }

        try (Connection conn = createSQLiteConnection(true)) {

            List<String> texts = getQueryRunner().query(conn, "SELECT \"text\" FROM text WHERE corpus_path=?",
                    new ColumnListHandler<>(1), topLevelCorpus + "/" + documentName);

            return texts;
        } catch (SQLException ex) {
            log.error("Failed to get raw text for document {}/{}", topLevelCorpus, documentName, ex);
        }

        return new LinkedList<>();
    }

    @Override
    public List<String> getRawText(String topLevelCorpus) {
        if (topLevelCorpus == null) {
            throw new IllegalArgumentException("corpus name may not be null");
        }

        try (Connection conn = createSQLiteConnection(true)) {

            List<String> texts = getQueryRunner().query(conn, "SELECT \"text\" FROM text WHERE corpus_path like ?",
                    new ColumnListHandler<>(1), topLevelCorpus + "/%");

            return texts;
        } catch (SQLException ex) {
            log.error("Failed to get raw text for corpus {}", topLevelCorpus, ex);
        }
        return new LinkedList<>();
    }

    @Override
    public void setCorpusConfiguration(String toplevelCorpusName, Properties props) {

        try (Connection conn = createSQLiteConnection()) {
            conn.setAutoCommit(false);

            String sql = "SELECT filename FROM media_files " + "WHERE corpus_path=? AND title = "
                    + "'corpus.properties'";

            String fileName = getQueryRunner().query(conn, sql, new ScalarHandler<>(1), toplevelCorpusName);

            File dir = getRealDataDir();
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    log.info("Created directory " + dir);
                } else {
                    log.error("Directory " + dir + " doesn't exist and cannot be created");
                }
            }

            if (fileName == null) {
                fileName = "corpus_" + CommonHelper.getSafeFileName(toplevelCorpusName) + "_" + UUID.randomUUID()
                        + ".properties";
                getQueryRunner().update(conn,
                        "INSERT INTO media_files VALUES (?,?,'application/text+plain', 'corpus.properties')", fileName,
                        toplevelCorpusName);
            }
            log.info("write config file: " + dir + "/" + fileName);
            try (FileOutputStream fStream = new FileOutputStream(new File(dir.getCanonicalPath() + "/" + fileName));
                    OutputStreamWriter writer = new OutputStreamWriter(fStream, Charsets.UTF_8)) {
                props.store(writer, "");

            } catch (IOException ex) {
                log.error("error: write back the corpus.properties configuration", ex);
            }

            conn.commit();

        } catch (SQLException ex) {
            log.error("Error occured when setting the corpus configuration for corpus {}", toplevelCorpusName, ex);
        }

    }

    @Override
    public DocumentBrowserConfig getDocBrowserConfiguration(String topLevelCorpusName) {

        // try to get the corpus wise configuration
        InputStream binaryComplete = getBinaryComplete(topLevelCorpusName, "application/json", "document_browser.json");

        if (binaryComplete != null) {
            try {
                StringWriter stringWriter = new StringWriter();
                IOUtils.copy(binaryComplete, stringWriter, "utf-8");

                // map json to pojo
                ObjectMapper objectMapper = new ObjectMapper();
                DocumentBrowserConfig documentBrowserConfig = objectMapper.readValue(stringWriter.toString(),
                        DocumentBrowserConfig.class);
                return documentBrowserConfig;
            } catch (IOException ex) {
                log.error("cannot read the document_browser.json file", ex);
            }

        } else {
            return getDefaultDocBrowserConfiguration();
        }

        return null;
    }

    @Override
    public DocumentBrowserConfig getDefaultDocBrowserConfiguration() {
        String path = System.getProperty("annis.home") + "/conf" + "/document-browser.json";
        try (InputStream input = new FileInputStream(path);) {

            // map json to pojo
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(input, DocumentBrowserConfig.class);
        } catch (FileNotFoundException ex) {
            log.error("file \"${annis.home}/conf/document-browser.json\" does not exists", ex);
        } catch (IOException ex) {
            log.error("problems with reading ${annis.home}/conf/document-browser.json", ex);
        }

        return null;
    }

    // private MatrixSqlGenerator matrixSqlGenerator;
    // SqlGenerator that prepends EXPLAIN to a query
    private static final class ExplainSqlGenerator implements SqlGenerator<QueryData>, ResultSetExtractor<String> {

        private final boolean analyze;

        private final SqlGenerator<QueryData> generator;

        private ExplainSqlGenerator(SqlGenerator<QueryData> generator, boolean analyze) {
            this.generator = generator;
            this.analyze = analyze;
        }

        @Override
        public String toSql(QueryData queryData) {
            StringBuilder sb = new StringBuilder();
            sb.append("EXPLAIN ");
            if (analyze) {
                sb.append("ANALYZE ");
            }
            sb.append(generator.toSql(queryData));
            return sb.toString();
        }

        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString(1));
                sb.append("\n");
            }
            return sb.toString();
        }

        @Override
        public String toSql(QueryData queryData, String indent) {
            // dont indent
            return toSql(queryData);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(QueryDaoImpl.class);
    // / old

    private ListCorpusSqlHelper listCorpusSqlHelper;

    private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsSqlHelper;



    private List<SqlSessionModifier> sqlSessionModifiers;
    // private SqlGenerator findSqlGenerator;

    private ListCorpusByNameDaoHelper listCorpusByNameDaoHelper;

    private final AnnisParserAntlr aqlParser = new AnnisParserAntlr();

    private HashMap<String, Properties> corpusConfiguration;

    private ByteHelper byteHelper;

    private MetaByteHelper metaByteHelper;

    public QueryDaoImpl() {
        sqlSessionModifiers = new ArrayList<>();

        File logfile = new File(this.getGraphANNISDir(), "graphannis.log");
        this.corpusStorageMgr = new CorpusStorageManager(QueryDaoImpl.this.getGraphANNISDir().getAbsolutePath(),
                logfile.getAbsolutePath(), LogLevel.Warn);
    }

    public void init() {
        parseCorpusConfiguration();
    }

    @Override
    public CorpusStorageManager getCorpusStorageManager() {
        return this.corpusStorageMgr;
    }

    @Override
    public void modifySqlSession(JdbcTemplate jdbcTemplate, QueryData queryData) {
        if (timeout > 0) {
            jdbcTemplate.update("SET statement_timeout TO " + timeout);
        }
    }

    @Override
    public List<ExampleQuery> getExampleQueries(List<String> corpora) {
        if (corpora == null || corpora.isEmpty()) {
            return null;
        } else {
            List<ExampleQuery> result = new LinkedList<>();
            try (Connection conn = createSQLiteConnection(true)) {
                for (String c : corpora) {
                    result.addAll(
                            getQueryRunner().query(conn, ListExampleQueriesHelper.SQL, listExampleQueriesHelper, c));
                }
            } catch (SQLException ex) {
                log.error("Could not get example queries for corpora {}", Joiner.on(',').join(corpora), ex);
            }
            return result;
        }
    }

    protected List<String> escapedCorpusNames(QueryData queryData) {
        List<String> corpusList = new LinkedList<>();
        for (String c : queryData.getCorpusList()) {
            corpusList.add(corpusNameEscaper.escape(c));
        }
        return corpusList;
    }

    @Override
    public List<Match> find(QueryData queryData) {
        List<String> corpora = escapedCorpusNames(queryData);
        String query = QueryToJSON.serializeQuery(queryData.getAlternatives(), queryData.getMetaData());

        List<LimitOffsetQueryData> ext = queryData.getExtensions(LimitOffsetQueryData.class);
        Preconditions.checkArgument(ext != null && !ext.isEmpty(),
                "Query data must contain a LimitOffsetQueryData extension");
        LimitOffsetQueryData extQueryData = ext.get(0);

        Future<List<Match>> result = exec.submit(() -> {
            String[] matchesRaw = corpusStorageMgr.find(corpora, query, extQueryData.getOffset(),
                    extQueryData.getLimit());

            ArrayList<Match> data = new ArrayList<>((int) matchesRaw.length);
            for (int i = 0; i < matchesRaw.length; i++) {
                data.add(Match.parseFromString(matchesRaw[i]));
            }
            return data;
        });

        try {
            return result.get(getTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            result.cancel(true);
        }

        throw (new AnnisTimeoutException());

    }

    @Override
    public boolean find(final QueryData queryData, final OutputStream out) {
        List<String> corpora = escapedCorpusNames(queryData);
        String query = QueryToJSON.serializeQuery(queryData.getAlternatives(), queryData.getMetaData());

        List<LimitOffsetQueryData> ext = queryData.getExtensions(LimitOffsetQueryData.class);
        Preconditions.checkArgument(ext != null && !ext.isEmpty(),
                "Query data must contain a LimitOffsetQueryData extension");
        LimitOffsetQueryData extQueryData = ext.get(0);

        Future<Boolean> result = exec.submit(() -> {
            String[] matchesRaw = corpusStorageMgr.find(corpora, query, extQueryData.getOffset(),
                    extQueryData.getLimit());

            try {
                PrintWriter w = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

                for (int i = 0; i < matchesRaw.length; i++) {
                    w.print(matchesRaw[i]);
                    w.print("\n");

                    // flush after every 10th item
                    if (i % 10 == 0) {
                        w.flush();
                    }
                }

                w.flush();
                return true;
            } catch (Exception ex) {
                log.error("Could not write find data to stream", ex);
            }
            return false;
        });

        try {
            return result.get(getTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            result.cancel(true);
        }

        throw (new AnnisTimeoutException());

    }

    @Override
    public int count(QueryData queryData) {
        Future<Integer> result = exec.submit(() -> {
            return (int) corpusStorageMgr.count(escapedCorpusNames(queryData),
                    QueryToJSON.serializeQuery(queryData.getAlternatives(), queryData.getMetaData()));
        });

        try {
            return result.get(getTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            result.cancel(true);
        }

        throw (new AnnisTimeoutException());
    }

    @Override
    public MatchAndDocumentCount countMatchesAndDocuments(QueryData queryData) {

        Future<MatchAndDocumentCount> result = exec.submit(() -> {
            CorpusStorageManager.CountResult data = corpusStorageMgr.countExtra(escapedCorpusNames(queryData),
                    QueryToJSON.serializeQuery(queryData.getAlternatives(), queryData.getMetaData()));

            return new MatchAndDocumentCount((int) data.matchCount, (int) data.documentCount);
        });

        try {
            return result.get(getTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            result.cancel(true);
        }

        throw (new AnnisTimeoutException());

    }

    @Override
    public FrequencyTable frequency(QueryData queryData) {
        // TODO: implement frequency query with graphANNIS
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryData parseAQL(String aql, List<String> corpusList) {
        // parse the query
        return aqlParser.parse(aql, corpusList);
    }

    @Override
    public List<AnnisCorpus> listCorpora() {
        try (Connection conn = createSQLiteConnection(true)) {
            return getQueryRunner().query(conn, listCorpusSqlHelper.createSqlQuery(), listCorpusSqlHelper);
        } catch (SQLException ex) {
            log.error("Listing corpora failed", ex);
        }
        ;
        return new LinkedList<>();
    }

    @Override
    public List<AnnisCorpus> listCorpora(List<String> corpusNames) {
        try (Connection conn = createSQLiteConnection(true)) {
            return getQueryRunner().query(conn, listCorpusSqlHelper.createSqlQueryWithList(corpusNames.size()),
                    listCorpusSqlHelper, corpusNames.toArray());
        } catch (SQLException ex) {
            log.error("Listing corpora failed", ex);
        }

        return new LinkedList<>();
    }

    @Override
    public List<AnnisAttribute> listAnnotations(List<String> corpusList, boolean listValues,
            boolean onlyMostFrequentValues) {

        // TODO: list annotations with graphANNIS
        return new LinkedList<>();
    }

    @Override
    public List<String> listSegmentationNames(List<String> corpusList) {

        // TODO: list segmentations with graphANNIS
        return new LinkedList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public SaltProject retrieveAnnotationGraph(String toplevelCorpusName, String documentName,
            List<String> nodeAnnotationFilter) {
        URI docURI = SaltUtil.createSaltURI(toplevelCorpusName).appendSegment(documentName);

        SDocumentGraph graph = corpusStorageMgr.subcorpusGraph(toplevelCorpusName, Arrays.asList(docURI.toString()));

        // wrap the single document into a SaltProject
        SaltProject project = SaltFactory.createSaltProject();
        SCorpusGraph corpusGraph = project.createCorpusGraph();
        SCorpus rootCorpus = corpusGraph.createCorpus(null, toplevelCorpusName);
        SDocument doc = corpusGraph.createDocument(rootCorpus, documentName);

        doc.setDocumentGraph(graph);

        return project;

    }

    @Override
    @Transactional(readOnly = true)
    public List<Annotation> listCorpusAnnotations(String toplevelCorpusName) {
        final String sql = listCorpusAnnotationsSqlHelper.createSqlQuery(toplevelCorpusName, toplevelCorpusName, true);
        final List<Annotation> corpusAnnotations = (List<Annotation>) getJdbcTemplate().query(sql,
                listCorpusAnnotationsSqlHelper);
        return corpusAnnotations;
    }
    
    private List<Annotation> allAnnotationForCorpus(SNode corpus) {
        
        String type = corpus instanceof SDocument ? "DOCUMENT" : "CORPUS";
        
        List<Annotation> result = new LinkedList<>();
        
        Set<SMetaAnnotation> metaAnnos = corpus.getMetaAnnotations();
        if(metaAnnos.isEmpty()) {
            // add single entry for the document without annotation value
            Annotation anno = new Annotation();
            anno.setName(corpus.getName());
            anno.setAnnotationPath(corpus.getPath().segmentsList());
            anno.setType(type);
            
            result.add(anno);
        } else {
            // add all annotations of this document as value
            for(SMetaAnnotation meta : metaAnnos) {
                Annotation anno = new Annotation();
                anno.setName(corpus.getName());
                anno.setAnnotationPath(corpus.getPath().segmentsList());
                anno.setType(type);
               
                anno.setNamespace(meta.getNamespace());
                anno.setName(meta.getName());
                anno.setValue(meta.getValue().toString());
                
                result.add(anno);
            }
        }
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Annotation> listDocumentsAnnotations(String toplevelCorpusName, boolean listRootCorpus) {
        
        SCorpusGraph corpusGraph = corpusStorageMgr.corpusGraph(toplevelCorpusName);
        
        List<Annotation> result = new LinkedList<>();
        for(SDocument doc : corpusGraph.getDocuments()) {
            result.addAll(allAnnotationForCorpus(doc));
        }
        
        if(listRootCorpus) {
            for(SNode n : corpusGraph.getRoots()) {
                if(n instanceof SCorpus) {
                    result.addAll(allAnnotationForCorpus(n));
                    break;
                }
            }
        }
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Annotation> listCorpusAnnotations(String toplevelCorpusName, String documentName, boolean exclude) {
        String sql = listCorpusAnnotationsSqlHelper.createSqlQuery(toplevelCorpusName, documentName, exclude);
        final List<Annotation> cA = (List<Annotation>) getJdbcTemplate().query(sql, listCorpusAnnotationsSqlHelper);
        return cA;
    }

    @Override
    public List<ResolverEntry> getResolverEntries(SingleResolverRequest request) {
        try (Connection conn = createSQLiteConnection(true)) {
            ResolverDaoHelper helper = new ResolverDaoHelper();
            PreparedStatement stmt = helper.createPreparedStatement(conn);
            helper.fillPreparedStatement(request, stmt);
            List<ResolverEntry> result = helper.extractData(stmt.executeQuery());
            return result;
        } catch (SQLException ex) {
            log.error("Could not get resolver entries from database", ex);
            return new LinkedList<>();
        }
    }

    @Override
    public Properties getCorpusConfiguration(String corpusName) throws FileNotFoundException {

        Properties props = new Properties();
        InputStream binary = getBinaryComplete(corpusName, "application/text+plain", "corpus.properties");

        if (binary == null) {
            throw new FileNotFoundException("no corpus.properties found for " + corpusName);
        }

        try {
            props.load(binary);
        } catch (IOException ex) {
            log.error("could not read corpus config--// of {}", corpusName, ex);
        }

        return props;
    }

    private void parseCorpusConfiguration() {
        corpusConfiguration = new HashMap<>();

        try {
            List<AnnisCorpus> corpora = listCorpora();
            for (AnnisCorpus c : corpora) {
                // copy properties from map
                Properties p;
                try {
                    p = getCorpusConfiguration(c.getName());
                } catch (FileNotFoundException ex) {
                    log.warn("no config found for {}", c.getName());
                    continue;
                }

                corpusConfiguration.put(c.getName(), p);
            }
        } catch (org.springframework.jdbc.CannotGetJdbcConnectionException ex) {
            log.warn("No corpus configuration loaded due to missing database connection.");
        } catch (org.springframework.jdbc.BadSqlGrammarException ex) {
            log.warn("Your database schema seems to be old. Probably you need to reinit it");
        }
    }

    @Override
    public boolean checkDatabaseVersion() throws AnnisException {
        try (Connection conn = getJdbcTemplate().getDataSource().getConnection();) {

            DatabaseMetaData meta = conn.getMetaData();

            log.debug("database info [major: " + meta.getDatabaseMajorVersion() + " minor: "
                    + meta.getDatabaseMinorVersion() + " complete: " + meta.getDatabaseProductVersion() + " name: "
                    + meta.getDatabaseProductName() + "]");

            if (!"PostgreSQL".equalsIgnoreCase(meta.getDatabaseProductName())) {
                throw new AnnisException("You did provide a database connection to a "
                        + "database that is not PostgreSQL. Please note that this will " + "not work.");
            }
            if (meta.getDatabaseMajorVersion() < 9
                    || (meta.getDatabaseMajorVersion() == 9 && meta.getDatabaseMinorVersion() < 1)) // we
                                                                                                    // urge
                                                                                                    // people
                                                                                                    // to
                                                                                                    // use
                                                                                                    // 9.2,
                                                                                                    // but
                                                                                                    // 9.1
                                                                                                    // should
                                                                                                    // be
                                                                                                    // valid
                                                                                                    // as
                                                                                                    // well
            {
                throw new AnnisException("Wrong PostgreSQL version installed. Please "
                        + "install at least PostgreSQL 9.2 (current installed version is "
                        + meta.getDatabaseProductVersion() + ")");
            }
        } catch (SQLException ex) {
            log.error("could not get database version", ex);
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public void exportCorpus(String toplevelCorpus, File outputDirectory) {

        SaltProject corpusProject = SaltFactory.createSaltProject();
        SCorpusGraph corpusGraph = SaltFactory.createSCorpusGraph();
        corpusGraph.setSaltProject(corpusProject);

        SCorpus rootCorpus = corpusGraph.createCorpus(null, toplevelCorpus);

        // add all root metadata
        for (Annotation metaAnno : listCorpusAnnotations(toplevelCorpus)) {
            rootCorpus.createMetaAnnotation(metaAnno.getNamespace(), metaAnno.getName(), metaAnno.getValue());
        }

        File documentRootDir = new File(outputDirectory, toplevelCorpus);

        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {
                log.warn("Could not create output directory \"{}\" for exporting the corpus",
                        outputDirectory.getAbsolutePath());
            }
        }

        List<Annotation> docs = listDocuments(toplevelCorpus);
        int i = 1;
        for (Annotation docAnno : docs) {
            log.info("Loading document {} from database ({}/{})", docAnno.getName(), i, docs.size());
            SaltProject docProject = retrieveAnnotationGraph(toplevelCorpus, docAnno.getName(), null);
            if (docProject != null && docProject.getCorpusGraphs() != null && !docProject.getCorpusGraphs().isEmpty()) {
                List<Annotation> docMetaData = listCorpusAnnotations(toplevelCorpus, docAnno.getName(), true);

                SCorpusGraph docCorpusGraph = docProject.getCorpusGraphs().get(0);
                // TODO: we could re-use the actual corpus structure instead of
                // just adding a flat list of documents
                if (docCorpusGraph.getDocuments() != null) {
                    for (SDocument doc : docCorpusGraph.getDocuments()) {
                        log.info("Removing SFeatures from {} ({}/{})", docAnno.getName(), i, docs.size());
                        // remove all ANNIS specific features that require a
                        // special Java class
                        SDocumentGraph graph = doc.getDocumentGraph();
                        if (graph != null) {
                            if (graph.getNodes() != null) {
                                for (SNode n : graph.getNodes()) {
                                    n.removeLabel(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_RELANNIS_NODE);
                                }
                            }
                            if (graph.getRelations() != null) {
                                for (SRelation e : graph.getRelations()) {
                                    e.removeLabel(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_RELANNIS_EDGE);
                                }
                            }
                        }
                        SDocument docCopy = corpusGraph.createDocument(rootCorpus, doc.getName());
                        doc.setDocumentGraph(graph);

                        log.info("Saving document {} ({}/{})", doc.getName(), i, docs.size());
                        SaltUtil.saveDocumentGraph(graph,
                                URI.createFileURI(
                                        new File(documentRootDir, doc.getName() + "." + SaltUtil.FILE_ENDING_SALT_XML)
                                                .getAbsolutePath()));

                        log.info("Adding metadata to document {} ({}/{})", doc.getName(), i, docs.size());
                        for (Annotation metaAnno : docMetaData) {
                            docCopy.createMetaAnnotation(metaAnno.getNamespace(), metaAnno.getName(),
                                    metaAnno.getValue());
                        }
                    }
                }
            }
            i++;
        } // end for each document

        // save the actual SaltProject
        log.info("Saving corpus structure");

        File projectFile = new File(outputDirectory, SaltUtil.FILE_SALT_PROJECT);
        SaltXML10Writer writer = new SaltXML10Writer(projectFile);
        writer.writeSaltProject(corpusProject);
    }

    // /// Getter / Setter
    public ListCorpusSqlHelper getListCorpusSqlHelper() {
        return listCorpusSqlHelper;
    }

    public void setListCorpusSqlHelper(ListCorpusSqlHelper listCorpusHelper) {
        this.listCorpusSqlHelper = listCorpusHelper;
    }

    public ListCorpusAnnotationsSqlHelper getListCorpusAnnotationsSqlHelper() {
        return listCorpusAnnotationsSqlHelper;
    }

    public void setListCorpusAnnotationsSqlHelper(ListCorpusAnnotationsSqlHelper listCorpusAnnotationsHelper) {
        this.listCorpusAnnotationsSqlHelper = listCorpusAnnotationsHelper;
    }

    public List<SqlSessionModifier> getSqlSessionModifiers() {
        return sqlSessionModifiers;
    }

    public void setSqlSessionModifiers(List<SqlSessionModifier> sqlSessionModifiers) {
        this.sqlSessionModifiers = sqlSessionModifiers;
    }

    public ListCorpusByNameDaoHelper getListCorpusByNameDaoHelper() {
        return listCorpusByNameDaoHelper;
    }

    public void setListCorpusByNameDaoHelper(ListCorpusByNameDaoHelper listCorpusByNameDaoHelper) {
        this.listCorpusByNameDaoHelper = listCorpusByNameDaoHelper;
    }

    @Override
    public CorpusConfigMap getCorpusConfigurations() {
        List<AnnisCorpus> annisCorpora = listCorpora();
        CorpusConfigMap cConfigs = new CorpusConfigMap();

        if (annisCorpora != null) {
            for (AnnisCorpus c : annisCorpora) {
                try {
                    Properties p = getCorpusConfiguration(c.getName());
                    if (p != null) {
                        CorpusConfig corpusConfig = new CorpusConfig();
                        corpusConfig.setConfig(p);
                        cConfigs.put(c.getName(), corpusConfig);
                    }
                } catch (FileNotFoundException ex) {
                    log.error("no corpus.properties found for {}", c.getName());
                }
            }
        }

        return cConfigs;
    }

    @Override
    public void setCorpusConfiguration(HashMap<String, Properties> corpusConfiguration) {
        this.corpusConfiguration = corpusConfiguration;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public ByteHelper getByteHelper() {
        return byteHelper;
    }

    public void setByteHelper(ByteHelper byteHelper) {
        this.byteHelper = byteHelper;
    }

    @Override
    public InputStream getBinary(String toplevelCorpusName, String corpusName, String mimeType, String title,
            int offset, int length) {

        AnnisBinaryMetaData binary = null;
        try (Connection conn = createSQLiteConnection(true)) {
            binary = getQueryRunner().query(conn, ByteHelper.SQL, byteHelper,
                    corpusName == null ? toplevelCorpusName : toplevelCorpusName + "/" + corpusName, mimeType, mimeType,
                    title, title);
        } catch (SQLException ex) {
            log.error("Could not query binary meta data for {}/{}", toplevelCorpusName, corpusName, ex);
        }

        if (binary != null) {

            try {
                // retrieve the requested part of the file from the data
                // directory
                File dataFile = new File(getRealDataDir(), binary.getLocalFileName());

                long fileSize = dataFile.length();

                Preconditions.checkArgument(offset + length <= fileSize,
                        "Range larger than the actual file size requested. Actual file size is %d bytes, %d bytes were requested.",
                        fileSize, offset + length);

                FileInputStream fInput = new FileInputStream(dataFile);
                ByteStreams.skipFully(fInput, offset);
                return ByteStreams.limit(fInput, length);
            } catch (FileNotFoundException ex) {
                log.warn("Media file from database not found in data directory", ex);
            } catch (IOException ex) {
                log.warn("Error when reading media file from the data directory", ex);
            }
        }

        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public List<AnnisBinaryMetaData> getBinaryMeta(String toplevelCorpusName, String corpusName) {

        try (Connection conn = createSQLiteConnection(true)) {
            List<AnnisBinaryMetaData> metaData = getQueryRunner().query(conn, MetaByteHelper.SQL, metaByteHelper,
                    corpusName == null ? toplevelCorpusName : toplevelCorpusName + "/" + corpusName);

            // get the file size from the real file
            ListIterator<AnnisBinaryMetaData> it = metaData.listIterator();
            while (it.hasNext()) {
                AnnisBinaryMetaData singleEntry = it.next();
                File f = new File(getRealDataDir(), singleEntry.getLocalFileName());
                singleEntry.setLength((int) f.length());
            }
            return metaData;
        } catch (SQLException ex) {
            log.error("Could not query binary meta data for document {}/{}", toplevelCorpusName, corpusName);
        }

        return new LinkedList<>();
    }

    @Override
    public List<Long> mapCorpusAliasToIds(String alias) {
        try {
            return getJdbcTemplate().queryForList("SELECT corpus_ref FROM corpus_alias WHERE alias=?", Long.class,
                    alias);
        } catch (DataAccessException ex) {
            return new LinkedList<>();
        }
    }

    public MetaByteHelper getMetaByteHelper() {
        return metaByteHelper;
    }

    public void setMetaByteHelper(MetaByteHelper metaByteHelper) {
        this.metaByteHelper = metaByteHelper;
    }

    public String getExternalFilesPath() {
        return externalFilesPath;
    }

    public File getRealDataDir() {
        File dataDir;
        if (getExternalFilesPath() == null || getExternalFilesPath().isEmpty()) {
            // use the default directory
            dataDir = new File(System.getProperty("user.home"), ".annis/data/");
        } else {
            dataDir = new File(getExternalFilesPath());
        }
        return dataDir;
    }

    public void setExternalFilesPath(String externalFilesPath) {
        this.externalFilesPath = externalFilesPath;
    }

    public ListExampleQueriesHelper getListExampleQueriesHelper() {
        return listExampleQueriesHelper;
    }

    public void setListExampleQueriesHelper(ListExampleQueriesHelper listExampleQueriesHelper) {
        this.listExampleQueriesHelper = listExampleQueriesHelper;
    }

    @Override
    public Properties getCorpusConfigurationSave(String corpus) {
        try {
            return getCorpusConfiguration(corpus);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }
}
