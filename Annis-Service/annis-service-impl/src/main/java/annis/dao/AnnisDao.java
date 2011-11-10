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
import annis.model.AnnotationGraph;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.sqlgen.SqlGenerator;

public interface AnnisDao
{

  public AnnotationGraph retrieveAnnotationGraph(long textId);

  public List<AnnisCorpus> listCorpora();

  public List<Long> listCorpusByName(List<String> corpusNames);

  public List<AnnisAttribute> listAnnotations(List<Long> corpusList, 
    boolean listValues, boolean onlyMostFrequentValues);

  public List<Annotation> listCorpusAnnotations(long id);

  public List<ResolverEntry> getResolverEntries(SingleResolverRequest[] request);

  public QueryData parseAQL(String aql, List<Long> corpusList);
  
  @Deprecated
  public QueryData parseDDDQuery(String dddquery, List<Long> corpusList);

// new 

    int count(QueryData queryData);
	List<Match> find(QueryData queryData);
	List<AnnotationGraph> annotate(QueryData queryData);
	String explain(SqlGenerator<?> generator, QueryData queryData, final boolean analyze);
    List<AnnotatedMatch> matrix(QueryData queryData);
    public <T> T executeQueryFunction(QueryData queryData, final SqlGenerator<T> generator);

	// needed in AnnisRunner
	public HashMap<Long, Properties> getCorpusConfiguration();
	public void setCorpusConfiguration(HashMap<Long, Properties> corpusConfiguration);

	///// configuration
	void setTimeout(int milliseconds);
	int getTimeout();

}
