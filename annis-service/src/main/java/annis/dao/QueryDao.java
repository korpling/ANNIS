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

import annis.administration.BinaryImportHelper;
import annis.examplequeries.ExampleQuery;
import annis.exceptions.AnnisException;
import annis.model.Annotation;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisBinaryMetaData;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.CorpusConfigMap;
import annis.service.objects.DocumentBrowserConfig;
import annis.service.objects.FrequencyTable;
import annis.service.objects.Match;
import annis.service.objects.MatchAndDocumentCount;
import annis.sqlgen.SqlGenerator;
import annis.sqlgen.SqlGeneratorAndExtractor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.corpus_tools.salt.common.SaltProject;
import org.springframework.jdbc.core.ResultSetExtractor;

public interface QueryDao
{

  public SaltProject retrieveAnnotationGraph(String toplevelCorpusName,
    String documentName, List<String> nodeAnnotationFilter);

  public List<AnnisCorpus> listCorpora();

  public List<AnnisCorpus> listCorpora(List<Long> ids);

  public List<AnnisAttribute> listAnnotations(List<Long> corpusList,
    boolean listValues, boolean onlyMostFrequentValues);

  public List<Annotation> listCorpusAnnotations(String toplevelCorpusName);

  public List<String> listSegmentationNames(List<Long> corpusList);

  /**
   * Creates sql for getting annotations of corpora.
   *
   * @param toplevelCorpusName The toplevel corpus defines the root.
   * @param corpusName Specifies the document, for which the annotations are
   * fetched.
   * @param exclude If set to true, the top level corpus annotations are
   * excluded. Only has an effect, if corpus name is different from top level
   * corpus name.
   * @return Valid sql as string.
   */
  public List<Annotation> listCorpusAnnotations(String toplevelCorpusName,
    String documentName, boolean exclude);

  /**
   * Gets a part of a binary file plus meta data from database.
   *
   * @param toplevelCorpusName
   * @param corpusName
   * @param mimeType The mime type of the binary to fetch.
   * @param title The title of the binary to fetch or null if any with correct
   * mime type.
   * @param offset starts with 1
   * @param length
   * @return
   */
  public InputStream getBinary(String toplevelCorpusName, String corpusName,
    String mimeType, String title, int offset, int length);

  /**
   * Gets a complete binary file from annis.
   *
   * <p>
   * It assumes, that only one binary file is stored with the combination
   * <b>toplevelCorpusName</b> and <b>mimeType</b>. If there are mor than one,
   * the first file is taken.</p>
   *
   * @param toplevelCorpusName Specifies the corpus, for which the file is
   * fetched.
   * @param mimeType The mime type of the binary to fetch.
   * @param title The title of the binary to fetch or null if any with correct
   * mime type.
   *
   * @return Returns an {@link InputStream} of the file. Returns null, when the
   * binary file does not exist.
   */
  public InputStream getBinaryComplete(String toplevelCorpusName,
    String mimeType, String title);

  /**
   * Gets meta data about existing binary files from database.
   *
   *
   * @param toplevelCorpusName
   * @param subCorpusName
   * @return A list of all {@link AnnisBinaryMetaData} for a sub corpus.
   */
  public List<AnnisBinaryMetaData> getBinaryMeta(String toplevelCorpusName);

  /**
   * Gets meta data about existing binary files from database.
   *
   *
   *
   * @param toplevelCorpusName
   * @param subCorpusName
   * @return A list of all {@link AnnisBinaryMetaData} for a sub corpus.
   */
  public List<AnnisBinaryMetaData> getBinaryMeta(String toplevelCorpusName,
    String subCorpusName);

  public List<Long> mapCorpusAliasToIds(String alias);

  public List<ResolverEntry> getResolverEntries(SingleResolverRequest request);

  public QueryData parseAQL(String aql, List<Long> corpusList);

  int count(QueryData queryData);

  MatchAndDocumentCount countMatchesAndDocuments(QueryData queryData);

  List<Match> find(QueryData queryData);

  public boolean find(final QueryData queryData, final OutputStream out);

  /**
   * Returns a part of a salt document according the saltIDs, we get with the 
   * {@link AnnisDao#find(annis.ql.parser.QueryData)
   *
   * @param queryData should include an extensions with a {@code List<URI>}
   * object
   * @return a salt graph
   */
  SaltProject graph(QueryData queryData);

  String explain(SqlGenerator<QueryData> generator, QueryData queryData,
    final boolean analyze);

  FrequencyTable frequency(QueryData queryData);

  public void matrix(final QueryData queryData, boolean outputCSV,
    final OutputStream out);

  public <T> T executeQueryFunction(QueryData queryData,
    final SqlGeneratorAndExtractor<QueryData, T> generator);

  public <T> T executeQueryFunction(QueryData queryData,
    final SqlGenerator<QueryData> generator,
    final ResultSetExtractor<T> extractor);

  /**
   * Gets the corpus configuration from all imported corpora.
   *
   * @return The return value is the Key of corpus table entry.
   * @deprecated Use {@link #getCorpusConfigurations()} instead.
   */
  public HashMap<Long, Properties> getCorpusConfiguration();

  /**
   * Gets the corpus configuration from all imported corpora.
   *
   * @return The return value is the Key of corpus table entry.
   */
  public CorpusConfigMap getCorpusConfigurations();

  /**
   * Reads the document browser configuration from the filesystem and returns
   * null if there is none.
   *
   * @param topLevelCorpusName The name of the corpus the configuraion is
   * fetched for.
   *
   * @return A JSONObject which holds the configuration.
   */
  public DocumentBrowserConfig getDocBrowserConfiguration(String topLevelCorpusName);

  /**
   * Reads the document browser configuration which is configure system wide in
   * ${annis.home}/conf/document-browser.json
   *
   * @return An pojo which holds the configuration.
   */
  public DocumentBrowserConfig getDefaultDocBrowserConfiguration();

  public void setCorpusConfiguration(
    HashMap<Long, Properties> corpusConfiguration);

  ///// configuration
  void setTimeout(int milliseconds);

  int getTimeout();

  public List<String> mapCorpusIdsToNames(List<Long> ids);

  /**
   * Return a list of internal IDs for a list of top-level corpus names.
   *
   * @param corpusNames
   * @return
   */
  public List<Long> mapCorpusNamesToIds(List<String> corpusNames);

  /**
   * Get a specific configuration of a corpus from directory.
   *
   * <p>
   * The corpus config files are actually stored in the
   * {@code <user>/.annis/data/corpus} directory, decorated with a {@link UUID}.
   * The actual name of a specific corpus property file is stored in the
   * media_file table.<p>
   *
   *
   * @param topLevelCorpus Determines the corpus.
   *
   * @return The corpus configuration is represented as Key-Value-Pairs.
   *
   * @see BinaryImportHelper
   * @throws FileNotFoundException If no corpus properties file exists a
   * exception is thrown.
   *
   */
  public Properties getCorpusConfiguration(String topLevelCorpus) throws FileNotFoundException;

  /**
   * Get a specific configuration of a corpus from directory.
   *
   * @param topLevelCorpus Determines the corpus.
   *
   * @return The corpus configuration is represented as Key-Value-Pairs.
   */
  public Properties getCorpusConfigurationSave(String topLevelCorpus);

  /**
   * Called to check if the database management program has the right version
   */
  public boolean checkDatabaseVersion() throws AnnisException;

  /**
   * Retrieves all metadata of a corpus including all subcorpora and documents.
   *
   * @param toplevelCorpusName Determines the root corpus.
   * @param withRootCorpus If true, the annotations of the root corpus are
   * included.
   * @return list of annotations. It is possible that some values are null.
   */
  public List<Annotation> listDocumentsAnnotations(String toplevelCorpusName,
    boolean withRootCorpus);

  /**
   * Gets all documents names for a specific corpus
   *
   * @param toplevelCorpusName the corpus determines which docs are loaded.
   * @return Contains name and pre for sorting the documents.
   */
  public List<Annotation> listDocuments(String toplevelCorpusName);

  /**
   * Fetches a list with auto generated queries.
   *
   * @param corpusIDs determines the corpora, for which the example queries are
   * defined. If null then all auto generated queries are fetched.
   * @return Is null, if no example queries exists in the database or no corpus
   * ids are specified.
   */
  public List<ExampleQuery> getExampleQueries(List<Long> corpusIDs);

  /**
   * Returns the raw text from the text.tab file of the ANNIS format.
   *
   * @param topLevelCorpus The name of the corpus.
   * @param documentName The name of the document
   * @return "" if no text.tab is empty
   */
  public List<String> getRawText(String topLevelCorpus, String documentName);

  /**
   * Returns the raw text fromt the text table of a specific corpus.
   *
   * @param topLevelCorpus Specifies the corpus for which the texts are fetched.
   * @return A list of all texts.
   */
  public List<String> getRawText(String topLevelCorpus);

  /**
   * Returns the corpus internal corpus id of a top level corpus.
   *
   * @param topLevelCorpus the name of the corpus
   * @return the corpus id
   */
  public long mapCorpusNameToId(String topLevelCorpus);

  /**
   * Returns the corpus name for a given corpus id.
   *
   * @param corpusId the id of the corpus
   * @return the name of the corpus. The name of each top level corpus is
   * unique.
   */
  public String mapCorpusIdToName(long corpusId);

  /**
   * Stores a corpus configuration. If the properties object is empty, an empty
   * file is written in the annis data directory.
   *
   * <p>
   * The name of the corpus properties file follows the schema:<br>
   * <pre>corpus_&lt;toplevelCorpusName&gt;_&lt;UUID&gt;.properties</pre>
   * </p>
   *
   * @param topLevelCorpus The name of the corpus, for which the properties are
   * written.
   * @param props The properties
   */
  public void setCorpusConfiguration(String topLevelCorpus, Properties props);
  
  public void exportCorpus(String toplevelCorpus, File outputDirectory);
}
