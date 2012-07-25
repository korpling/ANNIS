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

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import annis.model.Annotation;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.AnnisAttribute;
import annis.service.ifaces.AnnisBinary;
import annis.service.objects.AnnisCorpus;
import annis.sqlgen.SqlGenerator;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.util.Map;
import org.springframework.jdbc.core.ResultSetExtractor;

public interface AnnisDao
{

  public SaltProject retrieveAnnotationGraph(long textId);

  public SaltProject retrieveAnnotationGraph(String toplevelCorpusName,
    String documentName);

  public List<AnnisCorpus> listCorpora();

  public List<Long> listCorpusByName(List<String> corpusNames);

  public List<AnnisAttribute> listAnnotations(List<Long> corpusList,
    boolean listValues, boolean onlyMostFrequentValues);

  public List<Annotation> listCorpusAnnotations(String toplevelCorpusName);

  public List<Annotation> listCorpusAnnotations(String toplevelCorpusName,
    String documentName);

  public AnnisBinary getBinary(String corpusName, int offset, int length);

  public List<ResolverEntry> getResolverEntries(SingleResolverRequest request);

  public QueryData parseAQL(String aql, List<Long> corpusList);


  // new 
  int count(QueryData queryData);

  List<Match> find(QueryData queryData);

  /**
   * Returns a part of a salt document according the saltIDs, we get with
   * {@link AnnisDao#find(annis.ql.parser.QueryData)
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

  /**
   * Get a specific configuration of a corpus from directory
   * {@code <annis.home>/conf/corpora/<corpus.name>}.
   *
   * @return The corpus configuration is represented as Key-Value-Pairs.
   */
  public Map<String, String> getCorpusConfiguration(String corpusName);
}
