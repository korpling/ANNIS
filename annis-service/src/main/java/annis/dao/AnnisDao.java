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
import annis.dao.objects.AnnotatedMatch;
import annis.examplequeries.ExampleQuery;
import annis.exceptions.AnnisException;
import annis.service.objects.Match;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import annis.model.Annotation;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisBinaryMetaData;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.FrequencyTable;
import annis.service.objects.CorpusConfigMap;
import annis.service.objects.MatchAndDocumentCount;
import annis.sqlgen.SqlGenerator;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import org.springframework.jdbc.core.ResultSetExtractor;

public interface AnnisDao
{

  public SaltProject retrieveAnnotationGraph(String toplevelCorpusName,
    String documentName);

  public List<AnnisCorpus> listCorpora();

  public List<AnnisAttribute> listAnnotations(List<Long> corpusList,
    boolean listValues, boolean onlyMostFrequentValues);

  public List<Annotation> listCorpusAnnotations(String toplevelCorpusName);

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
   * <p>It assumes, that only one binary file is stored with the combination
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

  public List<ResolverEntry> getResolverEntries(SingleResolverRequest request);

  public QueryData parseAQL(String aql, List<Long> corpusList);

  int count(QueryData queryData);

  MatchAndDocumentCount countMatchesAndDocuments(QueryData queryData);

  List<Match> find(QueryData queryData);

  public boolean find(final QueryData queryData, final OutputStream out);

  /**
   * Returns a part of a salt document according the saltIDs, we get with null
   * null null null null null null null null null null null null null null null
   * null null null null null null null null null null null null null   {@link AnnisDao#find(annis.ql.parser.QueryData)
   *
   * @param queryData should include an extensions with a {@code List<URI>}
   * object
   * @return a salt graph
   */
  SaltProject graph(QueryData queryData);

  SaltProject annotate(QueryData queryData);

  String explain(SqlGenerator<QueryData, ?> generator, QueryData queryData,
    final boolean analyze);
  
  FrequencyTable frequency(QueryData queryData);

  public void matrix(final QueryData queryData, final OutputStream out);


  public <T> T executeQueryFunction(QueryData queryData,
    final SqlGenerator<QueryData, T> generator);

  public <T> T executeQueryFunction(QueryData queryData,
    final SqlGenerator<QueryData, T> generator,
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
   * <p>The corpus config files are actually stored in the
   * {@code <user>/.annis/data/corpus} directory, decorated with a {@link UUID}.
   * The actual name of a specific corpus property file is stored in the
   * media_file table.<p>
   *
   * @return The corpus configuration is represented as Key-Value-Pairs.
   *
   * @see BinaryImportHelper
   *
   */
  public Properties getCorpusConfiguration(String corpusName);

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
   *
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
}
