package org.corpus_tools.annis.gui.exporter;

import static org.corpus_tools.annis.api.model.CorpusConfigurationViewTimelineStrategy.StrategyEnum.IMPLICITFROMMAPPING;

import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.stream.XMLStreamException;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.Component;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.CorpusConfigurationViewTimelineStrategy.StrategyEnum;
import org.corpus_tools.annis.api.model.SubgraphWithContext;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.graphml.DocumentGraphMapper;
import org.corpus_tools.annis.gui.objects.Match;
import org.corpus_tools.annis.gui.util.TimelineReconstructor;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.eclipse.emf.common.util.URI;

public class ExportHelper {

  public static final String SEGMENTATION_KEY = "segmentation";

  private ExportHelper() {
    // Class with static helper functions should not be instantiated
  }

  protected static SaltProject documentGraphToProject(SDocumentGraph graph,
      List<String> corpusPath) {
    SaltProject p = SaltFactory.createSaltProject();
    SCorpusGraph cg = p.createCorpusGraph();
    URI docURI = URI.createURI("salt:/" + Joiner.on('/').join(corpusPath));
    SDocument doc = cg.createDocument(docURI);
    doc.setDocumentGraph(graph);

    return p;
  }

  private static void recreateTimelineIfNecessary(SaltProject p, CorporaApi corporaApi,
      Map<String, CorpusConfiguration> corpusConfigs)
      throws ApiException, UnsupportedEncodingException {


    Set<String> corpusNames = Helper.getToplevelCorpusNames(p);
    if (corpusNames.isEmpty()) {
      return;
    }
    String firstCorpusName = corpusNames.iterator().next();

    CorpusConfiguration config = corpusConfigs.get(firstCorpusName);
    if (config == null) {
      return;
    }

    if (config.getView().getTimelineStrategy() == null) {
      return;
    }

    StrategyEnum timelineStrategy = config.getView().getTimelineStrategy().getStrategy();
    if (timelineStrategy == StrategyEnum.EXPLICIT) {
      return;
    }

    // Get all segmentation names
    Set<String> segNames = new TreeSet<>();
    for (Component c : corporaApi.components(firstCorpusName,
        AnnotationComponentType.ORDERING.getValue(), null)) {
      if (!c.getName().isEmpty() && !"annis".equals(c.getLayer())) {
        segNames.add(c.getName());
      }
    }

    recreateTimeline(p, timelineStrategy, segNames, config);
  }

  private static void recreateTimeline(SaltProject p, StrategyEnum timelineStrategy,
      Set<String> segNames, CorpusConfiguration config) throws ApiException {
    Map<String, String> spanAnno2order = new TreeMap<>();

    if (timelineStrategy == IMPLICITFROMMAPPING
        && config.getView().getTimelineStrategy().getMappings() instanceof Map) {
      Map<?, ?> mappings = (Map<?, ?>) config.getView().getTimelineStrategy().getMappings();
      for (Map.Entry<?, ?> e : mappings.entrySet()) {
        if (e.getKey() instanceof String && e.getValue() instanceof String) {
          spanAnno2order.put((String) e.getKey(), (String) e.getValue());
        }
      }
    }


    // Recreate timeline for all documents in the result
    for (SCorpusGraph corpusGraph : p.getCorpusGraphs()) {
      if (corpusGraph.getDocuments() != null) {
        for (SDocument doc : corpusGraph.getDocuments()) {
          if (timelineStrategy == StrategyEnum.IMPLICITFROMNAMESPACE) {
            TimelineReconstructor.removeVirtualTokenizationUsingNamespace(doc.getDocumentGraph(),
                segNames);
          } else if (timelineStrategy == StrategyEnum.IMPLICITFROMMAPPING) {
            // there is a definition how to map the virtual tokenization to a real one
            TimelineReconstructor.removeVirtualTokenization(doc.getDocumentGraph(), segNames,
                spanAnno2order);
          }

        }
      }
    }
  }

  protected static Optional<SaltProject> getSubgraphForMatch(String match, CorporaApi corporaApi,
      int contextLeft, int contextRight, Map<String, String> args,
      Map<String, CorpusConfiguration> corpusConfigs)
      throws ApiException, IOException, XMLStreamException {

    // iterate over all matches and get the sub-graph for a group of matches
    Match parsedMatch = Match.parseFromString(match);

    if (!parsedMatch.getSaltIDs().isEmpty()) {
      final List<String> corpusPathForMatch = Helper.getCorpusPath(parsedMatch.getSaltIDs().get(0));
      final String corpusNameForMatch = corpusPathForMatch.get(0);

      SubgraphWithContext subgraphQuery = new SubgraphWithContext();
      subgraphQuery.setLeft(contextLeft);
      subgraphQuery.setRight(contextRight);
      subgraphQuery.segmentation(null);
      subgraphQuery.setNodeIds(parsedMatch.getSaltIDs());


      if (args.containsKey(SEGMENTATION_KEY)) {
        subgraphQuery.setSegmentation(args.get(SEGMENTATION_KEY));
      }

      File graphML = corporaApi.subgraphForNodes(corpusNameForMatch, subgraphQuery);

      SDocumentGraph docGraph = DocumentGraphMapper.map(graphML);
      SaltProject p = documentGraphToProject(docGraph, corpusPathForMatch);
      Helper.addMatchToDocumentGraph(parsedMatch, docGraph);
      recreateTimelineIfNecessary(p, corporaApi, corpusConfigs);


      return Optional.of(p);
    }
    return Optional.empty();
  }
}
