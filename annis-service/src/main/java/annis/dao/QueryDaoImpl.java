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
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.SaltUtil;
import org.corpus_tools.salt.util.internal.persistence.SaltXML10Writer;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;

import annis.CSVHelper;
import annis.CommonHelper;
import annis.WekaHelper;
import annis.examplequeries.ExampleQuery;
import annis.exceptions.AnnisException;
import annis.model.AnnisConstants;
import annis.model.Annotation;
import annis.ql.parser.AnnisParserAntlr;
import annis.ql.parser.QueryData;
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
import annis.sqlgen.AnnotateSqlGenerator;
import annis.sqlgen.AnnotatedMatchIterator;
import annis.sqlgen.ByteHelper;
import annis.sqlgen.CountMatchesAndDocumentsSqlGenerator;
import annis.sqlgen.CountSqlGenerator;
import annis.sqlgen.FindSqlGenerator;
import annis.sqlgen.FrequencySqlGenerator;
import annis.sqlgen.ListAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusAnnotationsSqlHelper;
import annis.sqlgen.ListCorpusSqlHelper;
import annis.sqlgen.ListDocumentsAnnotationsSqlHelper;
import annis.sqlgen.ListDocumentsSqlHelper;
import annis.sqlgen.ListExampleQueriesHelper;
import annis.sqlgen.MatrixSqlGenerator;
import annis.sqlgen.MetaByteHelper;
import annis.sqlgen.RawTextSqlHelper;
import annis.sqlgen.ResultSetTypedIterator;
import annis.sqlgen.SaltAnnotateExtractor;
import annis.sqlgen.SqlGenerator;
import annis.sqlgen.SqlGeneratorAndExtractor;

// FIXME: test and refactor timeout and transaction management
public class QueryDaoImpl extends AbstractDao implements QueryDao,
  SqlSessionModifier
{

  // SQL generators for the different query functions
  private FindSqlGenerator findSqlGenerator;

  private CountMatchesAndDocumentsSqlGenerator countMatchesAndDocumentsSqlGenerator;

  private CountSqlGenerator countSqlGenerator;

  private SaltAnnotateExtractor saltAnnotateExtractor;

  private MatrixSqlGenerator matrixSqlGenerator;

  // generated sql for example queries and fetches the result
  private ListExampleQueriesHelper listExampleQueriesHelper;

  private AnnotateSqlGenerator<SaltProject> graphSqlGenerator;

  private FrequencySqlGenerator frequencySqlGenerator;

  // reads the raw text entries of ANNIS format, originally placed in text.tab
  private RawTextSqlHelper rawTextHelper;

  private String externalFilesPath;

  // configuration
  private int timeout;

  @Override
  @Transactional(readOnly = true)
  public SaltProject graph(QueryData data)
  {
    SaltProject p = executeQueryFunction(data, graphSqlGenerator, saltAnnotateExtractor);
    List<MatchGroup> matchGroupExt = data.getExtensions(MatchGroup.class);
    SaltAnnotateExtractor.addMatchInformation(p, matchGroupExt.get(0));
    
    return p;
  }

  /**
   * @return the graphSqlGenerator
   */
  public AnnotateSqlGenerator getGraphSqlGenerator()
  {
    return graphSqlGenerator;
  }

  /**
   * @param graphSqlGenerator the graphSqlGenerator to set
   */
  public void setGraphSqlGenerator(AnnotateSqlGenerator<SaltProject> graphSqlGenerator)
  {
    this.graphSqlGenerator = graphSqlGenerator;
  }

  /**
   * @return the listDocumentsAnnotationsSqlHelper
   */
  public ListDocumentsAnnotationsSqlHelper getListDocumentsAnnotationsSqlHelper()
  {
    return listDocumentsAnnotationsSqlHelper;
  }

  /**
   * @param listDocumentsAnnotationsSqlHelper the
   * listDocumentsAnnotationsSqlHelper to set
   */
  public void setListDocumentsAnnotationsSqlHelper(
    ListDocumentsAnnotationsSqlHelper listDocumentsAnnotationsSqlHelper)
  {
    this.listDocumentsAnnotationsSqlHelper = listDocumentsAnnotationsSqlHelper;
  }

  @Override
  public List<Annotation> listDocuments(String toplevelCorpusName)
  {
    return (List<Annotation>) getJdbcTemplate().query(
      getListDocumentsSqlHelper().
      createSql(toplevelCorpusName), getListDocumentsSqlHelper());
  }

  /**
   * @return the listDocumentsSqlHelper
   */
  public ListDocumentsSqlHelper getListDocumentsSqlHelper()
  {
    return listDocumentsSqlHelper;
  }

  /**
   * @param listDocumentsSqlHelper the listDocumentsSqlHelper to set
   */
  public void setListDocumentsSqlHelper(
    ListDocumentsSqlHelper listDocumentsSqlHelper)
  {
    this.listDocumentsSqlHelper = listDocumentsSqlHelper;
  }

  @Override
  public InputStream getBinaryComplete(String toplevelCorpusName,
    String mimeType, String title)
  {
    List<AnnisBinaryMetaData> binaryMetas = getBinaryMeta(toplevelCorpusName);
    InputStream input = null;

    if (binaryMetas != null)
    {
      for (AnnisBinaryMetaData metaData : binaryMetas)
      {
        if (mimeType.equals(metaData.getMimeType()) && title.equals(metaData.
          getFileName()))
        {
          String filePath = getRealDataDir().getPath() + "/" + metaData.
            getLocalFileName();
          try
          {
            input = new FileInputStream(filePath);
            return input;
          }
          catch (FileNotFoundException ex)
          {
            log.error("could not found binary file {}", filePath, ex);
          }
        }
      }
    }

    return input;
  }

  @Override
  public List<AnnisBinaryMetaData> getBinaryMeta(String toplevelCorpusName)
  {
    return getBinaryMeta(toplevelCorpusName, toplevelCorpusName);
  }

  @Override
  public HashMap<Long, Properties> getCorpusConfiguration()
  {
    return corpusConfiguration;
  }

  @Override
  public List<String> getRawText(String topLevelCorpus, String documentName)
  {
    if (topLevelCorpus == null || documentName == null)
    {
      throw new IllegalArgumentException(
        "top level corpus and document name may not be null");
    }

    long topLevelCorpusId = mapCorpusNameToId(topLevelCorpus);
    return (List<String>) getJdbcTemplate().query(rawTextHelper.createSQL(
      topLevelCorpusId, documentName), rawTextHelper);

  }

  @Override
  public List<String> getRawText(String topLevelCorpus)
  {
    if (topLevelCorpus == null)
    {
      throw new IllegalArgumentException("corpus name may not be null");
    }

    long id = mapCorpusNameToId(topLevelCorpus);
    String sql = rawTextHelper.createSQL(id);
    return (List<String>) getJdbcTemplate().query(sql, rawTextHelper);
  }

  /**
   * @return the rawTextHelper
   */
  public RawTextSqlHelper getRawTextHelper()
  {
    return rawTextHelper;
  }

  /**
   * @param rawTextHelper the rawTextHelper to set
   */
  public void setRawTextHelper(RawTextSqlHelper rawTextHelper)
  {
    this.rawTextHelper = rawTextHelper;
  }

  @Override
  public String mapCorpusIdToName(long corpusId)
  {

    List<Long> ids = new ArrayList<>();
    ids.add(corpusId);
    List<String> names = mapCorpusIdsToNames(ids);

    if (names == null || names.isEmpty())
    {
      String msg = "corpus is not known to the system";
      throw new DataAccessException(msg)
      {
      };
    }

    return names.get(0);

  }

  @Override
  public void setCorpusConfiguration(String toplevelCorpusName, Properties props)
  {
    long corpusID = mapCorpusNameToId(toplevelCorpusName);

    String sql = "SELECT filename FROM media_files "
      + "WHERE corpus_ref=" + corpusID + " AND title = " + "'corpus.properties'";
    String fileName = getJdbcTemplate().query(sql,
      new ResultSetExtractor<String>()
      {

        @Override
        public String extractData(ResultSet rs) throws SQLException, DataAccessException
        {
          while (rs.next())
          {
            return rs.getString("filename");
          }

          return null;
        }
      });


    File dir = getRealDataDir();
    if (!dir.exists())
    {
      if (dir.mkdirs())
      {
        log.info("Created directory " + dir);
      }
      else
      {
        log.error("Directory " + dir + " doesn't exist and cannot be created");
      }
    }

    if (fileName == null)
    {
      fileName = "corpus_" 
        + CommonHelper.getSafeFileName(toplevelCorpusName) 
        + "_" + UUID.randomUUID() + ".properties";
      getJdbcTemplate().update(
        "INSERT INTO media_files VALUES ('" + fileName + "','" + corpusID
        + "', 'application/text+plain', 'corpus.properties')");
    }
    log.info("write config file: " + dir + "/" + fileName);
    try (FileOutputStream fStream = new FileOutputStream(new File(
      dir.getCanonicalPath() + "/" + fileName));
      OutputStreamWriter writer = new OutputStreamWriter(fStream,
        Charsets.UTF_8))
    {
      props.store(writer, "");

    }
    catch (IOException ex)
    {
      log.error("error: write back the corpus.properties configuration", ex);
    }
  }

  @Override
  public DocumentBrowserConfig getDocBrowserConfiguration(String topLevelCorpusName)
  {

    // try to get the corpus wise configuration
    InputStream binaryComplete = getBinaryComplete(topLevelCorpusName,
      "application/json", "document_browser.json");

    if (binaryComplete != null)
    {
      try
      {
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(binaryComplete, stringWriter, "utf-8");

        // map json to pojo
        ObjectMapper objectMapper = new ObjectMapper();
        DocumentBrowserConfig documentBrowserConfig = objectMapper.readValue(
          stringWriter.toString(), DocumentBrowserConfig.class);
        return documentBrowserConfig;
      }
      catch (IOException ex)
      {
        log.error("cannot read the document_browser.json file", ex);
      }

      
    } else {
      return getDefaultDocBrowserConfiguration();
    }

    return null;
  }

  @Override
  public DocumentBrowserConfig getDefaultDocBrowserConfiguration()
  {
      String path = System.getProperty("annis.home") + "/conf" + "/document-browser.json";
      try(InputStream input = new FileInputStream(path);)
      {
        

         // map json to pojo
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(input, DocumentBrowserConfig.class);
      }
      catch (FileNotFoundException ex)
      {
        log.error(
          "file \"${annis.home}/conf/document-browser.json\" does not exists",
          ex);
      }
      catch (IOException ex)
      {
        log.error(
          "problems with reading ${annis.home}/conf/document-browser.json", ex);
      }

      return null;
  }

//	private MatrixSqlGenerator matrixSqlGenerator;
  // SqlGenerator that prepends EXPLAIN to a query
  private static final class ExplainSqlGenerator implements
    SqlGenerator<QueryData>, ResultSetExtractor<String>
  {

    private final boolean analyze;

    private final SqlGenerator<QueryData> generator;

    private ExplainSqlGenerator(SqlGenerator<QueryData> generator,
      boolean analyze)
    {
      this.generator = generator;
      this.analyze = analyze;
    }

    @Override
    public String toSql(QueryData queryData)
    {
      StringBuilder sb = new StringBuilder();
      sb.append("EXPLAIN ");
      if (analyze)
      {
        sb.append("ANALYZE ");
      }
      sb.append(generator.toSql(queryData));
      return sb.toString();
    }

    @Override
    public String extractData(ResultSet rs) throws SQLException,
      DataAccessException
    {
      StringBuilder sb = new StringBuilder();
      while (rs.next())
      {
        sb.append(rs.getString(1));
        sb.append("\n");
      }
      return sb.toString();
    }

    @Override
    public String toSql(QueryData queryData, String indent)
    {
      // dont indent
      return toSql(queryData);
    }
  }
  private static final Logger log = LoggerFactory.
    getLogger(QueryDaoImpl.class);
  // / old

  private SqlGenerator sqlGenerator;

  private ListCorpusSqlHelper listCorpusSqlHelper;

  private ListAnnotationsSqlHelper listAnnotationsSqlHelper;

  private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsSqlHelper;

  private ListDocumentsAnnotationsSqlHelper listDocumentsAnnotationsSqlHelper;

  private ListDocumentsSqlHelper listDocumentsSqlHelper;
  // / new

  private List<SqlSessionModifier> sqlSessionModifiers;
//  private SqlGenerator findSqlGenerator;

  private ParameterizedSingleColumnRowMapper<String> planRowMapper;

  private ListCorpusByNameDaoHelper listCorpusByNameDaoHelper;

  private AnnotateSqlGenerator graphExtractor;

  private MetaDataFilter metaDataFilter;

  private AnnisParserAntlr aqlParser;

  private HashMap<Long, Properties> corpusConfiguration;

  private ByteHelper byteHelper;

  private MetaByteHelper metaByteHelper;

  public QueryDaoImpl()
  {
    planRowMapper = new ParameterizedSingleColumnRowMapper<>();
    sqlSessionModifiers = new ArrayList<>();
  }

  public void init()
  {
    parseCorpusConfiguration();
  }

  @Override
  public List<String> mapCorpusIdsToNames(List<Long> ids)
  {
    List<String> names = new ArrayList<>();

    Map<Long, String> corpusNamesById = new TreeMap<>();
    List<AnnisCorpus> corpora = listCorpora();
    for (AnnisCorpus corpus : corpora)
    {
      corpusNamesById.put(corpus.getId(), corpus.getName());
    }

    for (Long id : ids)
    {
      if (corpusNamesById.containsKey(id))
      {
        names.add(corpusNamesById.get(id));
      }
    }
    return names;
  }

  private void prepareTransaction(QueryData queryData)
  {
    JdbcTemplate jdbcTemplate = getJdbcTemplate();

    // FIXME: muss corpusConfiguration an jeden Query angehangen werden?
    // oder nur an annotate-Queries?
    queryData.setCorpusConfiguration(corpusConfiguration);

    // filter by meta data
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));

    // execute session modifiers if any
    for (SqlSessionModifier sqlSessionModifier : sqlSessionModifiers)
    {
      sqlSessionModifier.modifySqlSession(jdbcTemplate, queryData);
    }
  }

  // query functions
  @Override
  @Transactional(readOnly = true)
  public <T> T executeQueryFunction(QueryData queryData,
    final SqlGeneratorAndExtractor<QueryData, T> generator)
  {
    return executeQueryFunction(queryData, generator, generator);
  }

  
  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  @Override
  public <T> T executeQueryFunction(QueryData queryData,
    final SqlGenerator<QueryData> generator,
    final ResultSetExtractor<T> extractor)
  {

    prepareTransaction(queryData);

    // execute query and return result
    return getJdbcTemplate().query(generator.toSql(queryData), extractor);
  }

  @Override
  public void modifySqlSession(JdbcTemplate jdbcTemplate, QueryData queryData)
  {
    if (timeout > 0)
    {
      jdbcTemplate.update("SET statement_timeout TO " + timeout);
    }
  }

  @Override
  public List<ExampleQuery> getExampleQueries(List<Long> corpusIDs)
  {
    if (corpusIDs == null || corpusIDs.isEmpty())
    {
      return null;
    }
    else
    {
      return (List<ExampleQuery>) getJdbcTemplate().query(
        listExampleQueriesHelper.createSQLQuery(corpusIDs),
        listExampleQueriesHelper);
    }
  }

  @Transactional(readOnly = true)
  @Override
  public List<Match> find(QueryData queryData)
  {
    return executeQueryFunction(queryData, findSqlGenerator, findSqlGenerator);
  }

  @Transactional(readOnly = true)
  @Override
  public boolean find(final QueryData queryData, final OutputStream out)
  {
    prepareTransaction(queryData);
    Boolean finished = getJdbcTemplate().execute(
      new ConnectionCallback<Boolean>()
      {
        @Override
        public Boolean doInConnection(Connection con) throws SQLException, DataAccessException
        {
          
          try(Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY);)
          {
            String sql = findSqlGenerator.toSql(queryData);

            PrintWriter w;
            try (ResultSet rs = stmt.executeQuery(sql))
            {
              w = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
              ResultSetTypedIterator<Match> itMatches = new ResultSetTypedIterator<>(
                rs, findSqlGenerator);
              int i = 1;
              while (itMatches.hasNext())
              {
                // write single match to output stream
                Match m = itMatches.next();
                w.print(m.toString());
                w.print("\n");
                
                // flush after every 10th item
                if (i % 10 == 0)
                {
                  w.flush();
                }
                
                i++;
              } // end for each match
            }
            w.flush();
            return true;
          }
          catch (UnsupportedEncodingException ex)
          {
            log.error(
              "Your system is not able to handle UTF-8 but ANNIS really needs this charset",
              ex);
          }

          return false;
        }
      });

    return finished;
  }

  @Transactional(readOnly = true)
  @Override
  public int count(QueryData queryData)
  {
    return executeQueryFunction(queryData, countSqlGenerator, countSqlGenerator);
  }

  @Transactional(readOnly = true)
  @Override
  public MatchAndDocumentCount countMatchesAndDocuments(QueryData queryData)
  {
    return executeQueryFunction(queryData, countMatchesAndDocumentsSqlGenerator,
      countMatchesAndDocumentsSqlGenerator);
  }

  @Transactional(readOnly = true)
  @Override
  public void matrix(final QueryData queryData, final boolean outputCsv,
    final OutputStream out)
  {
    prepareTransaction(queryData);

    getJdbcTemplate().execute(new ConnectionCallback<Boolean>()
    {
      @Override
      public Boolean doInConnection(Connection con) throws SQLException, DataAccessException
      {
        
        try(Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
          ResultSet rs = stmt.executeQuery(matrixSqlGenerator.toSql(queryData));)
        {
          
          AnnotatedMatchIterator itMatches
            = new AnnotatedMatchIterator(rs, matrixSqlGenerator.
              getSpanExtractor());

          // write the header to the output stream
          PrintWriter w = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));

          if (outputCsv)
          {
            SortedMap<Integer, SortedSet<String>> columnsByNodePos
              = CSVHelper.exportCSVHeader(itMatches, w);
            w.flush();

            // go back to the beginning and print the actual data
            itMatches.reset();
            CSVHelper.exportCSVData(itMatches, columnsByNodePos, w);
          }
          else
          {
            SortedMap<Integer, SortedSet<String>> columnsByNodePos
              = WekaHelper.exportArffHeader(itMatches, w);
            w.flush();

            // go back to the beginning and print the actual data
            itMatches.reset();
            WekaHelper.exportArffData(itMatches, columnsByNodePos, w);
          }
          w.flush();
        }
        catch (UnsupportedEncodingException ex)
        {
          log.error(
            "Your system is not able to handle UTF-8 but ANNIS really needs this charset",
            ex);
        }
        return true;
      }
    });
  }

  @Transactional(readOnly = true)
  @Override
  public FrequencyTable frequency(QueryData queryData)
  {
    return executeQueryFunction(queryData, frequencySqlGenerator, 
      frequencySqlGenerator);
  }

  @Override
  @Transactional(readOnly = true)
  public String explain(SqlGenerator<QueryData> generator,
    QueryData queryData,
    final boolean analyze)
  {
    ExplainSqlGenerator gen = new ExplainSqlGenerator(generator,
      analyze);
    return executeQueryFunction(queryData, gen, gen);
  }

  @Override
  public QueryData parseAQL(String aql, List<Long> corpusList)
  {
    // parse the query
    return aqlParser.parse(aql, corpusList);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AnnisCorpus> listCorpora()
  {
    return (List<AnnisCorpus>) getJdbcTemplate().query(
      listCorpusSqlHelper.createSqlQuery(), listCorpusSqlHelper);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AnnisCorpus> listCorpora(List<Long> ids)
  {
    return (List<AnnisCorpus>) getJdbcTemplate().query(
      listCorpusSqlHelper.createSqlQueryWithList(ids.size()),
      listCorpusSqlHelper, ids.toArray());
  }

  @Override
  @Transactional(readOnly = true)
  public List<AnnisAttribute> listAnnotations(List<Long> corpusList,
    boolean listValues, boolean onlyMostFrequentValues)
  {
    return (List<AnnisAttribute>) getJdbcTemplate().query(
      listAnnotationsSqlHelper.createSqlQuery(corpusList, listValues,
        onlyMostFrequentValues), listAnnotationsSqlHelper);
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> listSegmentationNames(List<Long> corpusList)
  {
    String corpusListStr = corpusList == null || corpusList.isEmpty()
      ? "NULL" : Joiner.on(", ").join(corpusList);

    String annotationsTable = "annotations";
    if(corpusList != null && corpusList.size() == 1) {
      annotationsTable = "annotations_" + corpusList.get(0);
    }
    
    String sql = "SELECT DISTINCT \"name\"\n"
      + "FROM " + annotationsTable + " AS annotations \n"
      + "WHERE\n"
      + "  toplevel_corpus IN (" + corpusListStr + ")\n"
      + "  AND type='segmentation'";
    return getJdbcTemplate().queryForList(sql, String.class);
  }

  @Override
  @Transactional(readOnly = true)
  public SaltProject retrieveAnnotationGraph(String toplevelCorpusName,
    String documentName, List<String> nodeAnnotationFilter)
  {
    
    long toplevelCorpusID = mapCorpusNameToId(toplevelCorpusName);
    SaltProject p
      = graphSqlGenerator.queryAnnotationGraph(getJdbcTemplate(),
        toplevelCorpusID, documentName, nodeAnnotationFilter);
    return p;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Annotation> listCorpusAnnotations(String toplevelCorpusName)
  {
    final String sql = listCorpusAnnotationsSqlHelper.createSqlQuery(
      toplevelCorpusName, toplevelCorpusName, true);
    final List<Annotation> corpusAnnotations
      = (List<Annotation>) getJdbcTemplate().query(sql,
        listCorpusAnnotationsSqlHelper);
    return corpusAnnotations;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Annotation> listDocumentsAnnotations(String toplevelCorpusName,
    boolean listRootCorpus)
  {
    final String sql = listDocumentsAnnotationsSqlHelper.createSqlQuery(
      toplevelCorpusName, listRootCorpus);
    final List<Annotation> docAnnotations
      = (List<Annotation>) getJdbcTemplate().query(sql,
        listDocumentsAnnotationsSqlHelper);
    return docAnnotations;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Annotation> listCorpusAnnotations(String toplevelCorpusName,
    String documentName, boolean exclude)
  {
    String sql = listCorpusAnnotationsSqlHelper.createSqlQuery(
      toplevelCorpusName, documentName, exclude);
    final List<Annotation> cA = (List<Annotation>) getJdbcTemplate().query(sql,
      listCorpusAnnotationsSqlHelper);
    return cA;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Long> mapCorpusNamesToIds(List<String> corpusNames)
  {
    if (corpusNames == null || corpusNames.isEmpty())
    {
      return new LinkedList<>();
    }
    final String sql = listCorpusByNameDaoHelper.createSql(corpusNames);
    final List<Long> result = getJdbcTemplate().query(sql,
      listCorpusByNameDaoHelper);
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ResolverEntry> getResolverEntries(SingleResolverRequest request)
  {
    try(Connection conn = getDataSource().getConnection())
    {
      ResolverDaoHelper helper = new ResolverDaoHelper();
      PreparedStatement stmt = helper.createPreparedStatement(conn);
      helper.fillPreparedStatement(request, stmt);
      List<ResolverEntry> result = helper.extractData(stmt.executeQuery());
      return result;
    }
    catch (SQLException ex)
    {
      log.error("Could not get resolver entries from database", ex);
      return new LinkedList<>();
    }
  }

  @Override
  public Properties getCorpusConfiguration(String corpusName) throws FileNotFoundException
  {

    Properties props = new Properties();
    InputStream binary = getBinaryComplete(corpusName,
      "application/text+plain", "corpus.properties");

    if (binary == null)
    {
      throw new FileNotFoundException("no corpus.properties found for " + corpusName);
    }

    try
    {
      props.load(binary);
    }
    catch (IOException ex)
    {
      log.error("could not read corpus config--// of {}", corpusName, ex);
    }

    return props;
  }

  private void parseCorpusConfiguration()
  {
    corpusConfiguration = new HashMap<>();

    try
    {
      List<AnnisCorpus> corpora = listCorpora();
      for (AnnisCorpus c : corpora)
      {
        // copy properties from map
        Properties p;
        try
        {
          p = getCorpusConfiguration(c.getName());
        }
        catch (FileNotFoundException ex)
        {
          log.warn("no config found for {}", c.getName());
          continue;
        }

        corpusConfiguration.put(c.getId(), p);
      }
    }
    catch (org.springframework.jdbc.CannotGetJdbcConnectionException ex)
    {
      log.warn(
        "No corpus configuration loaded due to missing database connection.");
    }
    catch (org.springframework.jdbc.BadSqlGrammarException ex)
    {
      log.warn(
        "Your database schema seems to be old. Probably you need to reinit it");
    }
  }

  @Override
  public boolean checkDatabaseVersion() throws AnnisException
  {
    try(Connection conn = getJdbcTemplate().getDataSource().getConnection();)
    {
      
      DatabaseMetaData meta = conn.getMetaData();

      log.debug(
        "database info [major: " + meta.getDatabaseMajorVersion() + " minor: " + meta.
        getDatabaseMinorVersion() + " complete: " + meta.
        getDatabaseProductVersion() + " name: " + meta.getDatabaseProductName() + "]");

      if (!"PostgreSQL".equalsIgnoreCase(meta.getDatabaseProductName()))
      {
        throw new AnnisException("You did provide a database connection to a "
          + "database that is not PostgreSQL. Please note that this will "
          + "not work.");
      }
      if (meta.getDatabaseMajorVersion() < 9
        || (meta.getDatabaseMajorVersion() == 9 && meta.
        getDatabaseMinorVersion() < 1)) // we urge people to use 9.2, but 9.1 should be valid as well
      {
        throw new AnnisException("Wrong PostgreSQL version installed. Please "
          + "install at least PostgreSQL 9.2 (current installed version is "
          + meta.getDatabaseProductVersion() + ")");
      }
    }
    catch (SQLException ex)
    {
      log.error("could not get database version", ex);
    }

    return false;
  }

  @Override
  @Transactional(readOnly = true)
  public void exportCorpus(String toplevelCorpus, File outputDirectory)
  {
    
    // check if the corpus really exists
    mapCorpusNameToId(toplevelCorpus);
    
    SaltProject corpusProject = SaltFactory.createSaltProject();
    SCorpusGraph corpusGraph = SaltFactory.createSCorpusGraph();
    corpusGraph.setSaltProject(corpusProject);
    
    SCorpus rootCorpus = corpusGraph.createCorpus(null, toplevelCorpus);
    
    // add all root metadata
    for(Annotation metaAnno : listCorpusAnnotations(toplevelCorpus))
    {
      rootCorpus.createMetaAnnotation(metaAnno.getNamespace(), metaAnno.getName(),
        metaAnno.getValue());
    }
    
    File documentRootDir = new File(outputDirectory, toplevelCorpus);
    
    
    if(!outputDirectory.exists())
    {
      if(!outputDirectory.mkdirs())
      {
        log.warn("Could not create output directory \"{}\" for exporting the corpus",
          outputDirectory.getAbsolutePath());
      }
    }
    
    
    List<Annotation> docs = listDocuments(toplevelCorpus);
    int i=1;
    for(Annotation docAnno  : docs)
    {
      log.info("Loading document {} from database ({}/{})", docAnno.getName(), i, docs.size());
      SaltProject docProject = retrieveAnnotationGraph(toplevelCorpus, docAnno.getName(), null);
      if(docProject != null && docProject.getCorpusGraphs() != null
        && !docProject.getCorpusGraphs().isEmpty())
      {
        List<Annotation> docMetaData = listCorpusAnnotations(toplevelCorpus,
          docAnno.getName(), true);
        
        SCorpusGraph docCorpusGraph = docProject.getCorpusGraphs().get(0);
        // TODO: we could re-use the actual corpus structure instead of just adding a flat list of documents
        if(docCorpusGraph.getDocuments() != null)
        {
          for(SDocument doc : docCorpusGraph.getDocuments())
          {
            log.info("Removing SFeatures from {} ({}/{})", docAnno.getName(), i, docs.size());
            // remove all ANNIS specific features that require a special Java class
            SDocumentGraph graph = doc.getDocumentGraph();
            if(graph != null)
            {
              if(graph.getNodes() != null)
              {
                for(SNode n : graph.getNodes())
                {
                  n.removeLabel(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_RELANNIS_NODE);
                }
              }
              if(graph.getRelations() != null)
              {
                for(SRelation e : graph.getRelations())
                {
                  e.removeLabel(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_RELANNIS_EDGE);
                }
              }
            }
            
            log.info("Saving document {} ({}/{})", doc.getName(), i, docs.size());
            SaltUtil.saveDocumentGraph(graph, URI.createFileURI(
              new File(documentRootDir, doc.getName() + "." 
                + SaltUtil.FILE_ENDING_SALT_XML).getAbsolutePath()));
            
            SDocument docCopy = corpusGraph.createDocument(rootCorpus, doc.getName());
            log.info("Adding metadata to document {} ({}/{})", doc.getName(), i, docs.size());
            for(Annotation metaAnno : docMetaData)
            {
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
  
  

  public AnnisParserAntlr getAqlParser()
  {
    return aqlParser;
  }

  public void setAqlParser(AnnisParserAntlr aqlParser)
  {
    this.aqlParser = aqlParser;
  }

  // /// Getter / Setter
  public SqlGenerator getSqlGenerator()
  {
    return sqlGenerator;
  }

  public void setSqlGenerator(SqlGenerator sqlGenerator)
  {
    this.sqlGenerator = sqlGenerator;
  }

  public ParameterizedSingleColumnRowMapper<String> getPlanRowMapper()
  {
    return planRowMapper;
  }

  public void setPlanRowMapper(
    ParameterizedSingleColumnRowMapper<String> planRowMapper)
  {
    this.planRowMapper = planRowMapper;
  }

  public ListCorpusSqlHelper getListCorpusSqlHelper()
  {
    return listCorpusSqlHelper;
  }

  public void setListCorpusSqlHelper(ListCorpusSqlHelper listCorpusHelper)
  {
    this.listCorpusSqlHelper = listCorpusHelper;
  }

  public ListAnnotationsSqlHelper getListAnnotationsSqlHelper()
  {
    return listAnnotationsSqlHelper;
  }

  public void setListAnnotationsSqlHelper(
    ListAnnotationsSqlHelper listNodeAnnotationsSqlHelper)
  {
    this.listAnnotationsSqlHelper = listNodeAnnotationsSqlHelper;
  }

  public ListCorpusAnnotationsSqlHelper getListCorpusAnnotationsSqlHelper()
  {
    return listCorpusAnnotationsSqlHelper;
  }

  public void setListCorpusAnnotationsSqlHelper(
    ListCorpusAnnotationsSqlHelper listCorpusAnnotationsHelper)
  {
    this.listCorpusAnnotationsSqlHelper = listCorpusAnnotationsHelper;
  }

  public List<SqlSessionModifier> getSqlSessionModifiers()
  {
    return sqlSessionModifiers;
  }

  public void setSqlSessionModifiers(
    List<SqlSessionModifier> sqlSessionModifiers)
  {
    this.sqlSessionModifiers = sqlSessionModifiers;
  }

  public FindSqlGenerator getFindSqlGenerator()
  {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(FindSqlGenerator findSqlGenerator)
  {
    this.findSqlGenerator = findSqlGenerator;
  }

  public ListCorpusByNameDaoHelper getListCorpusByNameDaoHelper()
  {
    return listCorpusByNameDaoHelper;
  }

  public void setListCorpusByNameDaoHelper(
    ListCorpusByNameDaoHelper listCorpusByNameDaoHelper)
  {
    this.listCorpusByNameDaoHelper = listCorpusByNameDaoHelper;
  }

  public AnnotateSqlGenerator getGraphExtractor()
  {
    return graphExtractor;
  }

  public void setGraphExtractor(AnnotateSqlGenerator graphExtractor)
  {
    this.graphExtractor = graphExtractor;
  }

  public MetaDataFilter getMetaDataFilter()
  {
    return metaDataFilter;
  }

  public void setMetaDataFilter(MetaDataFilter metaDataFilter)
  {
    this.metaDataFilter = metaDataFilter;
  }

  public CountMatchesAndDocumentsSqlGenerator getCountMatchesAndDocumentsSqlGenerator()
  {
    return countMatchesAndDocumentsSqlGenerator;
  }

  public void setCountMatchesAndDocumentsSqlGenerator(
    CountMatchesAndDocumentsSqlGenerator countMatchesAndDocumentsSqlGenerator)
  {
    this.countMatchesAndDocumentsSqlGenerator = countMatchesAndDocumentsSqlGenerator;
  }

  public CountSqlGenerator getCountSqlGenerator()
  {
    return countSqlGenerator;
  }

  public void setCountSqlGenerator(CountSqlGenerator countSqlGenerator)
  {
    this.countSqlGenerator = countSqlGenerator;
  }

  @Override
  public CorpusConfigMap getCorpusConfigurations()
  {
    List<AnnisCorpus> annisCorpora = listCorpora();
    CorpusConfigMap cConfigs = new CorpusConfigMap();

    if (annisCorpora != null)
    {
      for (AnnisCorpus c : annisCorpora)
      {
        try
        {
          Properties p = getCorpusConfiguration(c.getName());
          if (p != null)
          {
            CorpusConfig corpusConfig = new CorpusConfig();
            corpusConfig.setConfig(p);
            cConfigs.put(c.getName(), corpusConfig);
          }
        }
        catch (FileNotFoundException ex)
        {
          log.error("no corpus.properties found for {}", c.getName());
        }
      }
    }

    return cConfigs;
  }

  @Override
  public void setCorpusConfiguration(
    HashMap<Long, Properties> corpusConfiguration)
  {
    this.corpusConfiguration = corpusConfiguration;
  }

  @Override
  public int getTimeout()
  {
    return timeout;
  }

  @Override
  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  public MatrixSqlGenerator getMatrixSqlGenerator()
  {
    return matrixSqlGenerator;
  }

  public void setMatrixSqlGenerator(MatrixSqlGenerator matrixSqlGenerator)
  {
    this.matrixSqlGenerator = matrixSqlGenerator;
  }

  public SaltAnnotateExtractor getSaltAnnotateExtractor()
  {
    return saltAnnotateExtractor;
  }

  public void setSaltAnnotateExtractor(
    SaltAnnotateExtractor saltAnnotateExtractor)
  {
    this.saltAnnotateExtractor = saltAnnotateExtractor;
  }

  public ByteHelper getByteHelper()
  {
    return byteHelper;
  }

  public void setByteHelper(ByteHelper byteHelper)
  {
    this.byteHelper = byteHelper;
  }

  @Override
  public InputStream getBinary(String toplevelCorpusName, String corpusName,
    String mimeType, String title, int offset, int length)
  {
    AnnisBinaryMetaData binary
      = (AnnisBinaryMetaData) getJdbcTemplate().query(ByteHelper.SQL,
        byteHelper.
        getArgs(toplevelCorpusName, corpusName, mimeType, title, offset,
          length),
        ByteHelper.getArgTypes(), byteHelper);

    try
    {
      // retrieve the requested part of the file from the data directory
      File dataFile = new File(getRealDataDir(), binary.getLocalFileName());

      long fileSize = dataFile.length();

      Preconditions.checkArgument(offset + length <= fileSize,
        "Range larger than the actual file size requested. Actual file size is %d bytes, %d bytes were requested.",
        fileSize, offset + length);

      FileInputStream fInput = new FileInputStream(dataFile);
      ByteStreams.skipFully(fInput, offset);
      return ByteStreams.limit(fInput, length);
    }
    catch (FileNotFoundException ex)
    {
      log.warn("Media file from database not found in data directory", ex);
    }
    catch (IOException ex)
    {
      log.warn("Error when reading media file from the data directory", ex);
    }

    return new ByteArrayInputStream(new byte[0]);
  }

  @Override
  public List<AnnisBinaryMetaData> getBinaryMeta(String toplevelCorpusName,
    String corpusName)
  {
    List<AnnisBinaryMetaData> metaData = getJdbcTemplate().query(
      MetaByteHelper.SQL,
      metaByteHelper.getArgs(toplevelCorpusName, corpusName),
      MetaByteHelper.getArgTypes(), metaByteHelper);

    // get the file size from the real file
    ListIterator<AnnisBinaryMetaData> it = metaData.listIterator();
    while (it.hasNext())
    {
      AnnisBinaryMetaData singleEntry = it.next();
      File f = new File(getRealDataDir(), singleEntry.getLocalFileName());
      singleEntry.setLength((int) f.length());
    }
    return metaData;
  }

  @Override
  public List<Long> mapCorpusAliasToIds(String alias)
  {
    try
    {
      return getJdbcTemplate().queryForList(
        "SELECT corpus_ref FROM corpus_alias WHERE alias=?",
        Long.class, alias);
    }
    catch (DataAccessException ex)
    {
      return new LinkedList<>();
    }
  }

  public FrequencySqlGenerator getFrequencySqlGenerator()
  {
    return frequencySqlGenerator;
  }

  public void setFrequencySqlGenerator(
    FrequencySqlGenerator frequencySqlGenerator)
  {
    this.frequencySqlGenerator = frequencySqlGenerator;
  }

  public MetaByteHelper getMetaByteHelper()
  {
    return metaByteHelper;
  }

  public void setMetaByteHelper(MetaByteHelper metaByteHelper)
  {
    this.metaByteHelper = metaByteHelper;
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

  public ListExampleQueriesHelper getListExampleQueriesHelper()
  {
    return listExampleQueriesHelper;
  }

  public void setListExampleQueriesHelper(
    ListExampleQueriesHelper listExampleQueriesHelper)
  {
    this.listExampleQueriesHelper = listExampleQueriesHelper;
  }

  @Override
  public long mapCorpusNameToId(String topLevelCorpus)
  {
    if (topLevelCorpus == null)
    {
      throw new IllegalArgumentException("corpus name may not be null");
    }

    List<String> corpusNames = new ArrayList<>();
    corpusNames.add(topLevelCorpus);
    List<Long> corpusIds = mapCorpusNamesToIds(corpusNames);

    if (corpusIds == null || corpusIds.isEmpty())
    {
      throw new IllegalArgumentException(
        "corpus name \"" + topLevelCorpus + "\" is not known to the system");
    }

    // corpus names of top level corpora are unique.
    return corpusIds.get(0);
  }

  @Override
  public Properties getCorpusConfigurationSave(String corpus)
  {
    try
    {
      return getCorpusConfiguration(corpus);
    }
    catch (FileNotFoundException ex)
    {
      return null;
    }
  }
}
