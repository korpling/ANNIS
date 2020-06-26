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

import annis.CommonHelper;
import annis.ServiceConfig;
import annis.examplequeries.ExampleQuery;
import annis.exceptions.AnnisTimeoutException;
import annis.model.Annotation;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisAttribute.SubType;
import annis.service.objects.AnnisAttribute.Type;
import annis.service.objects.AnnisBinaryMetaData;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.CorpusConfig;
import annis.service.objects.CorpusConfigMap;
import annis.service.objects.DocumentBrowserConfig;
import annis.service.objects.FrequencyTable;
import annis.service.objects.FrequencyTableQuery;
import annis.service.objects.Match;
import annis.service.objects.MatchAndDocumentCount;
import annis.service.objects.MatchGroup;
import annis.service.objects.OrderType;
import annis.service.objects.QueryLanguage;
import annis.sqlgen.AnnisAttributeHelper;
import annis.sqlgen.ByteHelper;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListDocumentsHelper;
import annis.sqlgen.ListExampleQueriesHelper;
import annis.sqlgen.MetaByteHelper;
import annis.sqlgen.MetadataCacheHelper;
import annis.sqlgen.extensions.AnnotateQueryData;
import annis.sqlgen.extensions.LimitOffsetQueryData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
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
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.corpus_tools.graphannis.CorpusStorageManager;
import org.corpus_tools.graphannis.CorpusStorageManager.ResultOrder;
import org.corpus_tools.graphannis.LogLevel;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.corpus_tools.graphannis.model.Component;
import org.corpus_tools.graphannis.model.ComponentType;
import org.corpus_tools.graphannis.model.FrequencyTableEntry;
import org.corpus_tools.graphannis.model.Graph;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.util.SaltUtil;
import org.corpus_tools.salt.util.internal.persistence.SaltXML10Writer;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryDaoImpl extends AbstractDao implements QueryDao {

    private static final Logger log = LoggerFactory.getLogger(QueryDaoImpl.class);

    public static CorpusStorageManager.QueryLanguage convertQueryLanguage(QueryLanguage ql) {
        switch (ql) {
        case AQL:
            return CorpusStorageManager.QueryLanguage.AQL;
        case AQL_QUIRKS_V3:
            return CorpusStorageManager.QueryLanguage.AQLQuirksV3;
        default:
            return CorpusStorageManager.QueryLanguage.AQL;
        }
    }

    public static QueryDao create() throws GraphANNISException {
        QueryDaoImpl result = new QueryDaoImpl();

        return result;
    }

    private final ServiceConfig cfg = ConfigFactory.create(ServiceConfig.class);

    // generated sql for example queries and fetches the result
    private final ListExampleQueriesHelper listExampleQueriesHelper = new ListExampleQueriesHelper();

    private final AnnisAttributeHelper attributeHelper = new AnnisAttributeHelper();

    private int timeout;

    private final CorpusStorageManager corpusStorageMgr;

    private final ExecutorService exec = Executors.newCachedThreadPool();

    private final Pattern validQNamePattern = Pattern
            .compile("([a-zA-Z_%][a-zA-Z0-9_\\-%]*:)?[a-zA-Z_%][a-zA-Z0-9_\\-%]*");

    private final ListCorpusSqlHelper listCorpusSqlHelper = new ListCorpusSqlHelper();

    private HashMap<String, Properties> corpusConfiguration;

    private final ByteHelper byteHelper = new ByteHelper();

    private final MetaByteHelper metaByteHelper = new MetaByteHelper();

    private WatchService graphannisLogfileWatcher;

    protected QueryDaoImpl() throws GraphANNISException {
        final File logfile = new File(this.getGraphANNISDir(), "graphannis.log");

        if (cfg.maxCorpusCacheSize() >= 0) {
            // use the specific maximum size, convert from MB into Bytes
            long maxSize = cfg.maxCorpusCacheSize() * 1024 * 1024;
            this.corpusStorageMgr = new CorpusStorageManager(QueryDaoImpl.this.getGraphANNISDir().getAbsolutePath(),
                    logfile.getAbsolutePath(), LogLevel.Info, cfg.parallelQueryExecution(), maxSize);
        } else {
            // use automatic mode
            this.corpusStorageMgr = new CorpusStorageManager(QueryDaoImpl.this.getGraphANNISDir().getAbsolutePath(),
                    logfile.getAbsolutePath(), LogLevel.Info, cfg.parallelQueryExecution());
        }
        // initialize timeout with value from config (can be overwritten by API)
        this.timeout = cfg.timeout();

        // add a watcher for the logfile and emit a logging event whenever the logfile
        // changes
        try {
            graphannisLogfileWatcher = FileSystems.getDefault().newWatchService();

            final Path logfilePath = logfile.toPath();
            logfilePath.getParent().register(graphannisLogfileWatcher, StandardWatchEventKinds.ENTRY_MODIFY);
            // start a background thread
            new Thread(() -> {
                while (graphannisLogfileWatcher != null) {
                    try {
                        WatchKey wk = graphannisLogfileWatcher.poll(5, TimeUnit.SECONDS);
                        if (wk != null) {
                            for (WatchEvent<?> event : wk.pollEvents()) {
                                if (event.context() instanceof Path) {
                                    Path changed = (Path) event.context();
                                    if (changed.toString().equals("graphannis.log")) {
                                        // read the last line of the logfile and log it
                                        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(logfile, 4096,
                                                StandardCharsets.UTF_8)) {
                                            parseAndReportGraphANNISLogEntry(reader);
                                        }
                                    }
                                }
                            }
                            wk.reset();
                        }
                    } catch (ClosedWatchServiceException ex) {
                        return;
                    } catch (InterruptedException | IOException ex) {
                        log.error("Error when reading graphANNIS logfile", ex);
                        return;
                    }
                }
            }).start();
            ;
        } catch (IOException ex) {
            log.error("Could not register service to check the graphANNIS logfile", ex);
        }

    }

    public ResultOrder convertOrder(OrderType type) {
        switch (type) {
        case ascending:
            return ResultOrder.Normal;
        case descending:
            return ResultOrder.Inverted;
        case random:
            return ResultOrder.Randomized;
        case unsorted:
            return ResultOrder.NotSorted;
        default:
            return ResultOrder.Normal;
        }
    }

    @Override
    public long count(String query, QueryLanguage queryLanguage, List<String> corpusList) throws GraphANNISException {
        final CorpusStorageManager.QueryLanguage ql = convertQueryLanguage(queryLanguage);

        Future<Long> result = exec.submit(() -> corpusStorageMgr.count(corpusList, query, ql));

        try {
            if (getTimeout() > 0) {
                return result.get(getTimeout(), TimeUnit.MILLISECONDS);
            } else {
                return result.get();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            if (ex.getCause() instanceof GraphANNISException) {
                throw ((GraphANNISException) ex.getCause());
            } else {
                result.cancel(true);
                throw (new AnnisTimeoutException());
            }
        }
    }

    @Override
    public MatchAndDocumentCount countMatchesAndDocuments(String query, QueryLanguage queryLanguage,
            List<String> corpusList) throws GraphANNISException {

        final CorpusStorageManager.QueryLanguage ql = convertQueryLanguage(queryLanguage);

        Collections.sort(corpusList);

        Future<MatchAndDocumentCount> result = exec.submit(() -> {

            CorpusStorageManager.CountResult data = corpusStorageMgr.countExtra(corpusList, query, ql);

            return new MatchAndDocumentCount(data.matchCount, data.documentCount);
        });

        try {
            if (getTimeout() > 0) {
                return result.get(getTimeout(), TimeUnit.MILLISECONDS);
            } else {
                return result.get();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            if (ex.getCause() instanceof GraphANNISException) {
                throw ((GraphANNISException) ex.getCause());
            } else {
                result.cancel(true);
                throw (new AnnisTimeoutException());
            }
        }
    }

    @Override
    public void exportCorpus(String toplevelCorpus, File outputDirectory) throws GraphANNISException {

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

    private SDocumentGraph fetchDocumentWithContext(Match m, AnnotateQueryData annoExt) throws GraphANNISException {

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
        Optional<String> segmentation = Optional.empty();
        if (annoExt.getSegmentationLayer() != null && !annoExt.getSegmentationLayer().isEmpty()) {
            segmentation = Optional.of(annoExt.getSegmentationLayer());
        }
        SDocumentGraph result = SaltExport.map(
                corpusStorageMgr.subgraph(corpusName, matchedIDs, annoExt.getLeft(), annoExt.getRight(), segmentation));

        return result;
    }

    @Override
    public List<Match> find(String query, QueryLanguage queryLanguage, List<String> corpora,
            LimitOffsetQueryData limitOffset) throws GraphANNISException {

        Preconditions.checkNotNull(limitOffset, "LimitOffsetQueryData must be valid");

        final CorpusStorageManager.QueryLanguage ql = convertQueryLanguage(queryLanguage);

        ResultOrder ordering = convertOrder(limitOffset.getOrder());

        Future<List<Match>> result = exec.submit(() -> {

            ArrayList<Match> data = new ArrayList<>();

            String[] matchesRaw = corpusStorageMgr.find(corpora, query, ql, limitOffset.getOffset(),
                    limitOffset.getLimit(), ordering);

            for (int i = 0; i < matchesRaw.length; i++) {
                data.add(Match.parseFromString(matchesRaw[i]));
            }

            return data;
        });

        try {
            if (getTimeout() > 0) {
                return result.get(getTimeout(), TimeUnit.MILLISECONDS);
            } else {
                return result.get();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            if (ex.getCause() instanceof GraphANNISException) {
                throw ((GraphANNISException) ex.getCause());
            } else {
                result.cancel(true);
                throw (new AnnisTimeoutException());
            }
        }
    }

    @Override
    public boolean find(String query, QueryLanguage queryLanguage, List<String> corpora,
            LimitOffsetQueryData limitOffset, final OutputStream out) throws GraphANNISException {

        Collections.sort(corpora);

        Preconditions.checkNotNull(limitOffset, "LimitOffsetQueryData must be valid");

        final CorpusStorageManager.QueryLanguage ql = convertQueryLanguage(queryLanguage);

        ResultOrder ordering = convertOrder(limitOffset.getOrder());

        Future<Boolean> result = exec.submit(() -> {

            try {
                PrintWriter w = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

                String[] matchesRaw = corpusStorageMgr.find(corpora, query, ql, limitOffset.getOffset(),
                        limitOffset.getLimit(), ordering);

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

        try

        {
            if (getTimeout() > 0) {
                return result.get(getTimeout(), TimeUnit.MILLISECONDS);
            } else {
                return result.get();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            if (ex.getCause() instanceof GraphANNISException) {
                throw ((GraphANNISException) ex.getCause());
            } else {
                result.cancel(true);
                throw (new AnnisTimeoutException());
            }
        }
    }

    @Override
    public FrequencyTable frequency(String query, QueryLanguage queryLanguage, List<String> corpusList,
            FrequencyTableQuery freqQuery) throws GraphANNISException {

        Collections.sort(corpusList);

        final CorpusStorageManager.QueryLanguage ql = convertQueryLanguage(queryLanguage);

        FrequencyTable result = new FrequencyTable();
        if (freqQuery.isEmpty()) {
            return result;
        }

        List<FrequencyTableEntry<String>> freqTable = corpusStorageMgr.frequency(corpusList, query, ql,
                freqQuery.toString());
        if (freqTable != null) {
            for (FrequencyTableEntry<String> e : freqTable) {
                result.addEntry(new FrequencyTable.Entry(e.getTuple(), e.getCount()));
            }
        }

        return result;
    }

    @Override
    public InputStream getBinary(String toplevelCorpusName, String corpusName, String mimeType, String title,
            int offset, int length) {

        AnnisBinaryMetaData binary = null;
        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {
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
    public List<AnnisBinaryMetaData> getBinaryMeta(String toplevelCorpusName, String corpusName) {

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {
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
    public HashMap<String, Properties> getCorpusConfiguration() {
        return corpusConfiguration;
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
    public Properties getCorpusConfigurationSave(String corpus) {
        try {
            return getCorpusConfiguration(corpus);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    @Override
    public CorpusStorageManager getCorpusStorageManager() {
        return this.corpusStorageMgr;
    }

    @Override
    public DocumentBrowserConfig getDefaultDocBrowserConfiguration() {
        String path = System.getProperty("annis.home") + "/conf" + "/document-browser.json";
        try (InputStream input = new FileInputStream(path);) {

            // map json to pojo
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(input, DocumentBrowserConfig.class);
        } catch (FileNotFoundException ex) {
            log.error("file \"${annis.home}/conf/document-browser.json\" does not exists", ex);
        } catch (IOException ex) {
            log.error("problems with reading ${annis.home}/conf/document-browser.json", ex);
        }

        return null;
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
    public List<ExampleQuery> getExampleQueries(List<String> corpora) {
        if (corpora == null || corpora.isEmpty()) {
            return null;
        } else {
            List<ExampleQuery> result = new LinkedList<>();
            try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {
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

    @Override
    public List<String> getRawText(String topLevelCorpus) {
        if (topLevelCorpus == null) {
            throw new IllegalArgumentException("corpus name may not be null");
        }

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {

            List<String> texts = getQueryRunner().query(conn, "SELECT \"text\" FROM text WHERE corpus_path like ?",
                    new ColumnListHandler<>(1), topLevelCorpus + "/%");

            return texts;
        } catch (SQLException ex) {
            log.error("Failed to get raw text for corpus {}", topLevelCorpus, ex);
        }
        return new LinkedList<>();
    }

    @Override
    public List<String> getRawText(String topLevelCorpus, String documentName) {
        if (topLevelCorpus == null || documentName == null) {
            throw new IllegalArgumentException("top level corpus and document name may not be null");
        }

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {

            List<String> texts = getQueryRunner().query(conn, "SELECT \"text\" FROM text WHERE corpus_path=?",
                    new ColumnListHandler<>(1), topLevelCorpus + "/" + documentName);

            return texts;
        } catch (SQLException ex) {
            log.error("Failed to get raw text for document {}/{}", topLevelCorpus, documentName, ex);
        }

        return new LinkedList<>();
    }

    @Override
    public List<ResolverEntry> getResolverEntries(SingleResolverRequest request) {
        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {
            ResolverDaoHelper helper = new ResolverDaoHelper();
            PreparedStatement stmt = helper.createPreparedStatement(conn);
            helper.fillPreparedStatement(request, stmt);
            List<ResolverEntry> result = helper.handle(stmt.executeQuery());
            return result;
        } catch (SQLException ex) {
            log.error("Could not get resolver entries from database", ex);
            return new LinkedList<>();
        }
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public SaltProject graph(MatchGroup matchGroup, AnnotateQueryData annoExt) throws GraphANNISException {
        SaltProject p = SaltFactory.createSaltProject();

        if (matchGroup != null && annoExt != null) {

            for (Match m : matchGroup.getMatches()) {

                // create a corpus graph for each match
                SCorpusGraph corpusGraph = p.createCorpusGraph();

                List<URI> certainDocumentIDs = new LinkedList<>();
                List<URI> possibleCorpusIDs = new LinkedList<>();

                for (java.net.URI rawID : m.getSaltIDs()) {
                    URI id = URI.createURI(rawID.toASCIIString());
                    if (id.fragment() == null) {
                        possibleCorpusIDs.add(id);
                    } else {
                        certainDocumentIDs.add(id.trimFragment());
                    }
                }

                for (URI id : certainDocumentIDs) {
                    if (corpusGraph.getNode(id.toString()) == null) {
                        corpusGraph.createDocument(id);
                    }
                }
                for (URI id : possibleCorpusIDs) {
                    if (corpusGraph.getNode(id.toString()) == null) {
                        corpusGraph.createCorpus(id);
                    }
                }

                // If this match refers only to documents and (sub-) corpus matches, only create
                // a corpus graph.
                if (!certainDocumentIDs.isEmpty()) {

                    // Fetch the document graph for the match and add it to the already created
                    // document node
                    SNode docRaw = corpusGraph.getNode(certainDocumentIDs.get(0).toString());
                    if (docRaw instanceof SDocument) {
                        SDocument doc = (SDocument) docRaw;
                        SDocumentGraph docGraph = fetchDocumentWithContext(m, annoExt);
                        doc.setDocumentGraph(docGraph);
                        CommonHelper.addMatchToDocumentGraph(m, doc);
                    }
                }
            }
        }

        return p;
    }

    public void init() {
        parseCorpusConfiguration();
    }

    @Override
    public List<AnnisAttribute> listAnnotations(List<String> corpusList, boolean listValues,
            boolean onlyMostFrequentValues) {

        List<AnnisAttribute> result = new LinkedList<>();

        {
            for (String corpusName : corpusList) {

                Map<String, Set<String>> nodeAnnos = new TreeMap<>();

                List<org.corpus_tools.graphannis.model.Annotation> annoList = corpusStorageMgr
                        .listNodeAnnotations(corpusName, listValues, onlyMostFrequentValues);
                for (org.corpus_tools.graphannis.model.Annotation anno : annoList) {
                    String anno_name = anno.getKey().getName();
                    if (anno_name == null || anno_name.isEmpty()) {
                        anno_name = "_";
                    }
                    if (!"annis".equals(anno.getKey().getNs())) {
                        String qname;
                        if (anno.getKey().getNs() == null || anno.getKey().getNs().isEmpty()) {
                            qname = anno_name;
                        } else {
                            qname = anno.getKey().getNs() + ":" + anno_name;
                        }
                        if (qname != null) {
                            Set<String> values = nodeAnnos.get(qname);
                            if (values == null) {
                                values = new LinkedHashSet<>();
                                nodeAnnos.put(qname, values);
                            }
                            String val = anno.getValue();
                            if (val != null) {
                                values.add(val);
                            }
                        }
                    }
                }

                for (Entry<String, Set<String>> e : nodeAnnos.entrySet()) {
                    AnnisAttribute att = new AnnisAttribute();

                    att.setName(e.getKey());
                    att.setValueSet(e.getValue());

                    boolean isMeta = false;
                    if (validQNamePattern.matcher(e.getKey()).matches()) {
                        String query = e.getKey() + " _ident_ annis:node_type=\"corpus\"";
                        // check if the sub-type is a "normal" node or a meta-data annotation
                        try {
                            if (corpusStorageMgr.count(Arrays.asList(corpusName), query,
                                    CorpusStorageManager.QueryLanguage.AQL) > 0) {
                                isMeta = true;
                            }
                        } catch (GraphANNISException ex) {
                            log.error("Could not determine if attribute is a node or meta attribute. Query:\n{}", query,
                                    ex);
                        }
                    }
                    if (isMeta) {
                        att.setType(Type.meta);
                        att.setSubtype(SubType.m);
                    } else {
                        att.setType(Type.node);
                        att.setSubtype(SubType.n);
                    }
                    result.add(att);
                }
            }

        }

        ComponentType[] allComponentTypes = new ComponentType[] { ComponentType.Dominance, ComponentType.Pointing };
        for (String corpusName : corpusList) {
            for (ComponentType ctype : allComponentTypes) {
                for (Component c : corpusStorageMgr.getAllComponentsByType(corpusName, ctype)) {
                    AnnisAttribute att = new AnnisAttribute();
                    att.setType(Type.edge);
                    att.setEdgeName(c.getName());
                    att.setSubtype(ctype == ComponentType.Dominance ? SubType.d : SubType.p);

                    result.add(att);

                    // also find all edge annotations for this component
                    Map<String, Set<String>> edgeAnnos = new TreeMap<>();
                    List<org.corpus_tools.graphannis.model.Annotation> edgeAnnoList = corpusStorageMgr
                            .listEdgeAnnotations(corpusName, ctype, c.getName(), c.getLayer(), listValues,
                                    onlyMostFrequentValues);
                    for (org.corpus_tools.graphannis.model.Annotation anno : edgeAnnoList) {
                        String qname;
                        if (anno.getKey().getNs() == null || anno.getKey().getNs().isEmpty()) {
                            qname = anno.getKey().getName();
                        } else {
                            qname = anno.getKey().getNs() + ":" + anno.getKey().getName();
                        }
                        if (qname != null) {
                            Set<String> values = edgeAnnos.get(qname);
                            if (values == null) {
                                values = new LinkedHashSet<>();
                                edgeAnnos.put(qname, values);
                            }
                            String val = anno.getValue();
                            if (val != null) {
                                values.add(val);
                            }

                        }
                    }
                    for (Entry<String, Set<String>> e : edgeAnnos.entrySet()) {
                        AnnisAttribute attAnno = new AnnisAttribute();
                        attAnno.setType(Type.edge);
                        attAnno.setEdgeName(c.getName());
                        attAnno.setName(e.getKey());
                        attAnno.setValueSet(e.getValue());
                        attAnno.setSubtype(ctype == ComponentType.Dominance ? SubType.d : SubType.p);

                        result.add(attAnno);
                    }
                }
            }
        }

        List<String> segmentationNames = listSegmentationNames(corpusList);
        for (String s : segmentationNames) {
            AnnisAttribute att = new AnnisAttribute();
            att.setType(Type.segmentation);
            att.setSubtype(SubType.unknown);
            att.setName(s);
            result.add(att);
        }

        return result;
    }

    @Override
    public List<AnnisAttribute> listAnnotationsFromCache(List<String> corpusList) {
        List<AnnisAttribute> result = new LinkedList<>();

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {

            for (String corpus : corpusList) {

                result.addAll(getQueryRunner().query(conn, "SELECT * FROM annotations WHERE corpus = ?",
                        attributeHelper, corpus));
            }
        } catch (SQLException ex) {
            log.error("Could not list annotations from cache", ex);
        }

        return result;
    }

    @Override
    public List<AnnisCorpus> listCorpora() {
        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {
            return getQueryRunner().query(conn, listCorpusSqlHelper.createSqlQuery(), listCorpusSqlHelper);
        } catch (SQLException ex) {
            log.error("Listing corpora failed", ex);
        }
        return new LinkedList<>();
    }

    @Override
    public List<AnnisCorpus> listCorpora(List<String> corpusNames) {
        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {
            return getQueryRunner().query(conn, listCorpusSqlHelper.createSqlQueryWithList(corpusNames.size()),
                    listCorpusSqlHelper, corpusNames.toArray());
        } catch (SQLException ex) {
            log.error("Listing corpora failed", ex);
        }

        return new LinkedList<>();
    }

    @Override
    public List<Annotation> listCorpusAnnotations(String toplevelCorpusName) throws GraphANNISException {
        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {
            return getQueryRunner().query(conn,
                    "SELECT * FROM metadata_cache WHERE corpus = ? AND \"type\" = 'CORPUS' AND path=?",
                    new MetadataCacheHelper(), toplevelCorpusName, toplevelCorpusName);

        } catch (SQLException ex) {
            log.error("Could not list corpus annotations from database", ex);
            return new LinkedList<>();
        }
    }

    @Override
    public List<Annotation> listCorpusAnnotations(String toplevelCorpusName, String documentName, boolean exclude)
            throws GraphANNISException {

        boolean isToplevel = Objects.equals(toplevelCorpusName, documentName);

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {
            if (isToplevel) {
                return getQueryRunner().query(conn,
                        "SELECT * FROM metadata_cache WHERE corpus = ? AND \"type\" = 'CORPUS' AND path=?",
                        new MetadataCacheHelper(), toplevelCorpusName, toplevelCorpusName);
            } else {
                if (exclude) {
                    return getQueryRunner().query(conn,
                            "SELECT * FROM metadata_cache WHERE corpus = ? AND \"type\" = 'DOCUMENT' AND path LIKE ?",
                            new MetadataCacheHelper(), toplevelCorpusName, "%/" + documentName);
                } else {
                    return getQueryRunner().query(conn,
                            "SELECT * FROM metadata_cache WHERE corpus = ? AND "
                                    + "((\"type\" = 'DOCUMENT' AND path LIKE ?) OR (\"type\"= 'CORPUS' AND path = ?))",
                            new MetadataCacheHelper(), toplevelCorpusName, "%/" + documentName, toplevelCorpusName);
                }
            }
        } catch (SQLException ex) {
            log.error("Could not list corpus annotations from database", ex);
            return new LinkedList<>();
        }
    }

    @Override
    public List<Annotation> listDocuments(String toplevelCorpusName) {

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {
            return getQueryRunner().query(conn,
                    "SELECT * FROM metadata_cache WHERE corpus = ? AND \"type\" = 'DOCUMENT' AND namespace = 'annis' AND \"name\"='doc'",
                    new ListDocumentsHelper(), toplevelCorpusName);
        } catch (SQLException ex) {
            log.error("Could not list documents from database", ex);
            return new LinkedList<>();
        }
    }

    @Override
    public List<Annotation> listDocumentsAnnotations(String toplevelCorpusName, boolean listRootCorpus)
            throws GraphANNISException {

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY, true)) {
            if (listRootCorpus) {
                return getQueryRunner().query(conn,
                        "SELECT * FROM metadata_cache WHERE corpus = ? AND (\"type\" = 'DOCUMENT' OR path=?)",
                        new MetadataCacheHelper(), toplevelCorpusName, toplevelCorpusName);
            } else {
                return getQueryRunner().query(conn,
                        "SELECT * FROM metadata_cache WHERE corpus = ? AND \"type\" = 'DOCUMENT'",
                        new MetadataCacheHelper(), toplevelCorpusName);
            }
        } catch (SQLException ex) {
            log.error("Could not list document annotations from database", ex);
            return new LinkedList<>();
        }
    }

    private List<String> listSegmentationNames(List<String> corpusList) {
        LinkedList<String> result = new LinkedList<>();
        for (String corpus : corpusList) {
            for (Component orderRelComponent : corpusStorageMgr.getAllComponentsByType(corpus,
                    ComponentType.Ordering)) {
                if (!orderRelComponent.getName().isEmpty()) {
                    result.add(orderRelComponent.getName());
                }
            }
        }
        return result;
    }

    // /// Getter / Setter

    private void parseAndReportGraphANNISLogEntry(ReversedLinesFileReader reader) throws IOException {
        String lastLine = reader.readLine();

        Pattern formatPattern = Pattern.compile("^[0-9]+:[0-9]+:[0-9]+ \\[(.+)\\] (.*)");
        while (lastLine != null) {

            Matcher m = formatPattern.matcher(lastLine);
            if (m.matches()) {
                switch (m.group(1)) {
                case "DEBUG":
                    log.debug(m.group(2));
                    break;
                case "TRACE":
                    log.trace(m.group(2));
                    break;
                case "WARN":
                    log.warn(m.group(2));
                    break;
                case "ERROR":
                    log.error(m.group(2));
                    break;
                default:
                    log.info(m.group(2));
                    break;
                }
                return;
            }

            lastLine = reader.readLine();
        }
    }

    private void parseCorpusConfiguration() {
        corpusConfiguration = new HashMap<>();

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

    }

    @Override
    public SaltProject retrieveAnnotationGraph(String toplevelCorpusName, String documentName,
            List<String> nodeAnnotationFilter) throws GraphANNISException {
        URI docURI = SaltUtil.createSaltURI(toplevelCorpusName).appendSegment(documentName);

        Graph rawGraph;

        boolean fallbackToAll = false;
        if (nodeAnnotationFilter == null || nodeAnnotationFilter.isEmpty()) {
            fallbackToAll = true;
        } else {
            nodeAnnotationFilter = nodeAnnotationFilter.stream().map(anno_name -> anno_name.replaceFirst("::", ":"))
                    .collect(Collectors.toList());
            for (String nodeAnno : nodeAnnotationFilter) {
                if (!validQNamePattern.matcher(nodeAnno).matches()) {
                    // If we can't produce a valid query for this annotation name fallback
                    // to retrieve all annotations.
                    fallbackToAll = true;
                    break;
                }
            }
        }

        if (fallbackToAll) {
            rawGraph = corpusStorageMgr.subcorpusGraph(toplevelCorpusName, Arrays.asList(docURI.toString()));
        } else {
            StringBuilder aql = new StringBuilder("(a#tok");
            for (String nodeAnno : nodeAnnotationFilter) {
                aql.append(" | a#");
                aql.append(nodeAnno);
            }
            aql.append(") & d#annis:node_name=\"");
            aql.append(toplevelCorpusName);
            aql.append("/");
            aql.append(documentName);
            aql.append("\" & #a @* #d");

            rawGraph = corpusStorageMgr.subGraphForQuery(toplevelCorpusName, aql.toString(),
                    org.corpus_tools.graphannis.CorpusStorageManager.QueryLanguage.AQL);
        }

        SDocumentGraph graph = SaltExport.map(rawGraph);

        // wrap the single document into a SaltProject
        SaltProject project = SaltFactory.createSaltProject();
        SCorpusGraph corpusGraph = project.createCorpusGraph();
        SCorpus rootCorpus = corpusGraph.createCorpus(null, toplevelCorpusName);
        SDocument doc = corpusGraph.createDocument(rootCorpus, documentName);

        doc.setDocumentGraph(graph);

        return project;

    }

    @Override
    public void setCorpusConfiguration(HashMap<String, Properties> corpusConfiguration) {
        this.corpusConfiguration = corpusConfiguration;
    }

    @Override
    public void setCorpusConfiguration(String toplevelCorpusName, Properties props) {

        try (Connection conn = createConnection(DB.CORPUS_REGISTRY)) {
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
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void shutdown() throws InterruptedException {

        // force all running jobs to stop
        exec.awaitTermination(5, TimeUnit.SECONDS);
        exec.shutdownNow();

        // manually unload all corpora from cache
        if (corpusStorageMgr != null) {
            try {
                for (String corpus : corpusStorageMgr.list()) {
                    corpusStorageMgr.unloadCorpus(corpus);
                }
            } catch (GraphANNISException e) {
                log.warn("Error when unloading corpora from cache", e);
            }
        }

        // stop watching the graphANNIS logfile
        try {
            if (graphannisLogfileWatcher != null) {
                graphannisLogfileWatcher.close();
            }
        } catch (IOException ex) {
            log.error("Could not close file system watch", ex);
        }

    }
}
