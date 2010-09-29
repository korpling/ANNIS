package annis.dao;

import java.util.List;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;

public interface AnnisDao
{

  int countMatches(List<Long> corpusList, String dddQuery);

  public String planCount(String dddQuery, List<Long> corpusList, boolean analyze);

  public String planGraph(String dddQuery, List<Long> corpusList,
    long offset, long limit, int left, int right, boolean analyse);

  public List<AnnotationGraph> retrieveAnnotationGraph(List<Long> corpusList, String dddQuery, long offset, long limit, int left, int right);

  public AnnotationGraph retrieveAnnotationGraph(long textId);

  public List<AnnotatedMatch> matrix(final List<Long> corpusList, final String dddquery);

  public List<AnnisCorpus> listCorpora();

  public List<Long> listCorpusByName(List<String> corpusNames);

  public List<AnnisAttribute> listNodeAnnotations(List<Long> corpusList, boolean listValues);

  public List<Annotation> listCorpusAnnotations(long id);

  public List<ResolverEntry> getResolverEntries(SingleResolverRequest[] request);

}
