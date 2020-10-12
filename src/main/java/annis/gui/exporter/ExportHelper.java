package annis.gui.exporter;

import annis.gui.graphml.DocumentGraphMapper;
import annis.libgui.Helper;
import annis.service.objects.Match;
import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.model.SubgraphWithContext;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.eclipse.emf.common.util.URI;

public class ExportHelper {

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

  protected static Optional<SaltProject> getSubgraphForMatch(String match, CorporaApi corporaApi,
      int contextLeft, int contextRight, Map<String, String> args)
      throws ApiException, IOException, XMLStreamException {

    // iterate over all matches and get the sub-graph for a group of matches
    Match parsedMatch = Match.parseFromString(match);

    if (!parsedMatch.getSaltIDs().isEmpty()) {
      List<String> corpusPath = Helper.getCorpusPath(parsedMatch.getSaltIDs().get(0));

      SubgraphWithContext subgraphQuery = new SubgraphWithContext();
      subgraphQuery.setLeft(contextLeft);
      subgraphQuery.setRight(contextRight);
      subgraphQuery.setNodeIds(parsedMatch.getSaltIDs());

      if (args.containsKey("segmentation")) {
        subgraphQuery.setSegmentation(args.get("segmentation"));
      }

      File graphML = corporaApi.subgraphForNodes(corpusPath.get(0), subgraphQuery);

      SDocumentGraph docGraph = DocumentGraphMapper.map(graphML);
      SaltProject p = documentGraphToProject(docGraph, corpusPath);
      Helper.addMatchToDocumentGraph(parsedMatch, docGraph);
      
      return Optional.of(p);
    }
    return Optional.empty();
  }
}
