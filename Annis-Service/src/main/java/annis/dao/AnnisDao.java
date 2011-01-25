package annis.dao;

import java.util.List;

import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;

public interface AnnisDao
{

  int countMatches(List<Long> corpusList, QueryData aql);

  public String planCount(QueryData aql, List<Long> corpusList, boolean analyze);

  public String planGraph(QueryData aql, List<Long> corpusList,
    long offset, long limit, int left, int right, boolean analyse);

  public List<AnnotationGraph> retrieveAnnotationGraph(List<Long> corpusList, QueryData aql, long offset, long limit, int left, int right);

  public AnnotationGraph retrieveAnnotationGraph(long textId);

  public List<AnnotatedMatch> matrix(final List<Long> corpusList, final QueryData aql);

  public List<AnnisCorpus> listCorpora();

  public List<Long> listCorpusByName(List<String> corpusNames);

  public List<AnnisAttribute> listNodeAnnotations(List<Long> corpusList, 
    boolean listValues, boolean onlyMostFrequentValues);

  public List<Annotation> listCorpusAnnotations(long id);

  public List<ResolverEntry> getResolverEntries(SingleResolverRequest[] request);

  public QueryData parseAQL(String aql, List<Long> corpusList);
  public QueryData parseDDDQuery(String dddquery, List<Long> corpusList);

}
