package annis.dao;

import java.util.List;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.resolver.ResolverEntry;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;


public interface AnnisDao {

	int countMatches(List<Long> corpusList, String dddQuery);
	
	List<Match> findMatches(List<Long> corpusList, String dddQuery);
	
	String plan(String dddQuery, List<Long> corpusList, boolean analyze);
	
	List<AnnotationGraph> retrieveAnnotationGraph(List<Match> matches, int left, int right);

	List<AnnotationGraph> retrieveAnnotationGraph(List<Long> corpusList, String dddQuery, long offset, long limit, int left, int right);
	
	AnnotationGraph retrieveAnnotationGraph(long textId);
	
	List<AnnisNode> annotateMatches(List<Match> matches);
	
	int doWait(int seconds);
	
	List<AnnisCorpus> listCorpora();
	
	List<Long> listCorpusByName(List<String> corpusNames);
	
	List<AnnisAttribute> listNodeAnnotations(List<Long> corpusList, boolean listValues);
	
	List<Annotation> listCorpusAnnotations(long id);

  public List<ResolverEntry> getResolverEntries(long corpusId, List<String> namespaces );
		
}