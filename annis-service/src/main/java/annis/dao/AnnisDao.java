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
import annis.service.objects.AnnisBinary;
import annis.service.objects.AnnisBinaryMetaData;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.MatchAndDocumentCount;
import annis.sqlgen.SqlGenerator;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.util.Map;
import org.springframework.jdbc.core.ResultSetExtractor;

public interface AnnisDao
{

  public SaltProject retrieveAnnotationGraph(String toplevelCorpusName,
    String documentName);

  public List<AnnisCorpus> listCorpora();

  public List<AnnisAttribute> listAnnotations(List<Long> corpusList,
    boolean listValues, boolean onlyMostFrequentValues);

  public List<Annotation> listCorpusAnnotations(String toplevelCorpusName);

  public List<Annotation> listCorpusAnnotations(String toplevelCorpusName,
    String documentName);

  /**
   * Gets a part of a binary file plus meta data from database.
   *
   * @param toplevelCorpusName
   * @param corpusName
   * @param offset starts with 1
   * @param length
   * @return
   */
  public AnnisBinary getBinary(String toplevelCorpusName, String corpusName,
    String mimeType, int offset, int length);

  /**
   * Gets meta data about existing binary files from database.
   *
   * @param toplevelCorpusName
   * @param corpusName
   * @param offset starts with 1
   * @param length
   * @return
   */
  public List<AnnisBinaryMetaData> getBinaryMeta(String toplevelCorpusName,
    String corpusName);

  public List<ResolverEntry> getResolverEntries(SingleResolverRequest request);

  public QueryData parseAQL(String aql, List<Long> corpusList);

  int count(QueryData queryData);

  MatchAndDocumentCount countMatchesAndDocuments(QueryData queryData);

  List<Match> find(QueryData queryData);

  /**
   * Returns a part of a salt document according the saltIDs, we get with null null   {@link AnnisDao#find(annis.ql.parser.QueryData)
   *
   * @param queryData should include an extensions with a {@code List<URI>}
   * object
   * @return a salt graph
   */
  SaltProject graph(QueryData queryData);

  SaltProject annotate(QueryData queryData);

  String explain(SqlGenerator<QueryData, ?> generator, QueryData queryData,
    final boolean analyze);

  List<AnnotatedMatch> matrix(QueryData queryData);

  public <T> T executeQueryFunction(QueryData queryData,
    final SqlGenerator<QueryData, T> generator);

  public <T> T executeQueryFunction(QueryData queryData,
    final SqlGenerator<QueryData, T> generator,
    final ResultSetExtractor<T> extractor);

  /**
   * Gets the corpus configuration from all imported corpora.
   *
   * @return The return value is the Key of corpus table entry.
   */
  public HashMap<Long, Properties> getCorpusConfiguration();

  public void setCorpusConfiguration(
    HashMap<Long, Properties> corpusConfiguration);

  ///// configuration
  void setTimeout(int milliseconds);

  int getTimeout();

  public List<String> mapCorpusIdsToNames(List<Long> ids);

  public List<Long> mapCorpusNamesToIds(List<String> corpusNames);

  /**
   * Get a specific configuration of a corpus from directory
   * {@code <annis.home>/conf/corpora/<corpus.name>}.
   *
   * @return The corpus configuration is represented as Key-Value-Pairs.
   */
  public Map<String, String> getCorpusConfiguration(String corpusName);

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
}
