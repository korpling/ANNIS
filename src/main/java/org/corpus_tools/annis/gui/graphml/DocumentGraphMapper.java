package org.corpus_tools.annis.gui.graphml;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.TreeMultimap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.Component;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.SaltUtil;

/**
 * Maps a GraphML stream to Salt
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 *
 */
public class DocumentGraphMapper extends AbstractGraphMLMapper {

  private static final String ANNIS_TOK = "annis::tok";

  private final SDocumentGraph graph;

  private final Set<String> hasOutgoingCoverageEdge;
  private final Set<String> hasOutgoingDominanceEdge;
  private final Set<Pair<String, String>> hasNonEmptyDominanceEdge;

  private final Multimap<String, String> isPartOf;

  private boolean hasTimeline;


  protected DocumentGraphMapper() {
    this.graph = SaltFactory.createSDocumentGraph();
    this.hasOutgoingCoverageEdge = new HashSet<>();
    this.hasOutgoingDominanceEdge = new HashSet<>();
    this.hasNonEmptyDominanceEdge = new HashSet<>();

    this.isPartOf = HashMultimap.create();
    this.hasTimeline = false;
  }


  public static SDocumentGraph map(File inputFile) throws IOException, XMLStreamException {
    DocumentGraphMapper mapper = new DocumentGraphMapper();
    mapper.execute(inputFile);
    return mapper.graph;
  }

  @Override
  protected void firstPass(XMLEventReader reader) throws XMLStreamException {
    Map<String, String> keys = new TreeMap<>();
    int level = 0;
    boolean inGraph = false;
    Optional<String> currentNodeId = Optional.empty();
    Optional<String> currentDataKey = Optional.empty();
    Optional<String> currentSourceId = Optional.empty();
    Optional<String> currentTargetId = Optional.empty();
    Optional<String> currentComponent = Optional.empty();

    Map<String, String> data = new HashMap<>();

    Multimap<String, String> tokenIdByComponentName = TreeMultimap.create();
    Map<String, String> tokenToValue = new HashMap<>();

    while (reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      switch (event.getEventType()) {
        case XMLEvent.START_ELEMENT:
          level++;
          StartElement startElement = event.asStartElement();
          // create all new nodes
          switch (startElement.getName().getLocalPart()) {
            case "graph":
              if (level == 2) {
                inGraph = true;
              }
              break;
            case "key":
              if (level == 2) {
                addAnnotationKey(keys, startElement);
              }
              break;
            case "node":
              if (inGraph && level == 3) {
                Attribute id = startElement.getAttributeByName(new QName("id"));
                if (id != null) {
                  currentNodeId = Optional.ofNullable(id.getValue());
                }
              }
              break;
            case "edge":
              if (inGraph && level == 3) {
                // Get the source and target node IDs
                Attribute source = startElement.getAttributeByName(new QName("source"));
                Attribute target = startElement.getAttributeByName(new QName("target"));
                Attribute label = startElement.getAttributeByName(new QName("label"));
                Attribute component = startElement.getAttributeByName(new QName("label"));
                if (label != null) {
                  Component c = parseComponent(label.getValue());
                  if (source != null) {
                    if (c.getType() == AnnotationComponentType.COVERAGE) {
                      hasOutgoingCoverageEdge.add(source.getValue());
                    } else if (c.getType() == AnnotationComponentType.DOMINANCE) {
                      hasOutgoingDominanceEdge.add(source.getValue());
                    } else if (c.getType() == AnnotationComponentType.PARTOF) {
                      isPartOf.put(Helper.addSaltPrefix(source.getValue()),
                          Helper.addSaltPrefix(target.getValue()));
                    }
                    if (target != null && c.getType() == AnnotationComponentType.DOMINANCE
                        && !c.getName().isEmpty()) {
                      hasNonEmptyDominanceEdge.add(Pair.of(source.getValue(), target.getValue()));
                    }
                  }
                }
                if (source != null && target != null && component != null) {
                  currentSourceId = Optional.ofNullable(source.getValue());
                  currentTargetId = Optional.ofNullable(target.getValue());
                  currentComponent = Optional.ofNullable(component.getValue());
                }
              }
              break;
            case "data":
              Attribute key = startElement.getAttributeByName(new QName("key"));
              if (key != null) {
                currentDataKey = Optional.ofNullable(key.getValue());
              }
              break;
          }
          break;
        case XMLEvent.CHARACTERS:
          if (currentDataKey.isPresent() && inGraph && level == 4) {
            String annoKey = keys.get(currentDataKey.get());
            if (annoKey != null) {
              // Copy all data attributes into our own map
              data.put(annoKey, event.asCharacters().getData());
            }
          }
          break;
        case XMLEvent.END_ELEMENT:
          EndElement endElement = event.asEndElement();
          switch (endElement.getName().getLocalPart()) {
            case "graph":
              inGraph = false;
              break;
            case "node":
              String tokValue = data.get("annis::tok");
              if (tokValue != null && currentNodeId.isPresent()) {
                tokenToValue.put(tokValue, currentNodeId.get());
              }

              currentNodeId = Optional.empty();
              data.clear();
              break;
            case "edge":
              if (currentComponent.isPresent() && currentSourceId.isPresent()
                  && currentTargetId.isPresent()) {
                Component component = parseComponent(currentComponent.get());
                if (component.getType() == AnnotationComponentType.ORDERING) {
                  tokenIdByComponentName.put(component.getName(), currentSourceId.get());
                  tokenIdByComponentName.put(component.getName(), currentTargetId.get());
                }
              }

              currentSourceId = Optional.empty();
              currentTargetId = Optional.empty();
              currentComponent = Optional.empty();
              data.clear();
              break;
            case "data":
              if (currentDataKey.isPresent()) {
                String annoKey = keys.get(currentDataKey.get());
                // Add an empty data entry if this element did not have any character child
                if (annoKey != null && !data.containsKey(annoKey)) {
                  data.put(annoKey, "");
                }
              }
              currentDataKey = Optional.empty();
              break;
          }

          level--;
          break;
      }
    }

    // Check if this GraphML file has a timeline.
    this.hasTimeline = false;
    if (tokenIdByComponentName.keySet().size() > 1) {
      boolean hasNonEmptyBaseToken = false;
      Pattern whitespacePattern = Pattern.compile("\\s*");

      for (String tokId : tokenIdByComponentName.get("")) {
        String tokValue = tokenToValue.getOrDefault(tokId, "");
        // Check if this base token value is non-empty
        if (!whitespacePattern.matcher(tokValue).matches()) {
          hasNonEmptyBaseToken = true;
          break;
        }
      }

      if (!hasNonEmptyBaseToken) {
        this.hasTimeline = true;
      }
    }

  }

  @Override
  protected void secondPass(XMLEventReader reader) throws XMLStreamException {
    // Maps an internal ID to a fully qualified annotation name
    Map<String, String> keys = new TreeMap<>();
    int level = 0;
    boolean inGraph = false;
    Optional<String> currentNodeId = Optional.empty();
    Optional<String> currentDataKey = Optional.empty();
    Optional<String> currentSourceId = Optional.empty();
    Optional<String> currentTargetId = Optional.empty();
    Optional<String> currentComponent = Optional.empty();

    SortedMap<String, STextualDS> datasourcesInGraphMl = new TreeMap<>();
    Map<SToken, SToken> gapEdges = new HashMap<>();

    Map<String, String> data = new HashMap<>();

    while (reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      switch (event.getEventType()) {
        case XMLEvent.START_ELEMENT:
          level++;
          StartElement startElement = event.asStartElement();
          // create all new nodes
          switch (startElement.getName().getLocalPart()) {
            case "graph":
              if (level == 2) {
                inGraph = true;
              }
              break;
            case "key":
              if (level == 2) {
                addAnnotationKey(keys, startElement);
              }
              break;
            case "node":
              if (inGraph && level == 3) {
                Attribute id = startElement.getAttributeByName(new QName("id"));
                if (id != null) {
                  currentNodeId = Optional.ofNullable(id.getValue());
                }
              }
              break;
            case "edge":
              if (inGraph && level == 3) {
                // Get the source and target node IDs
                Attribute source = startElement.getAttributeByName(new QName("source"));
                Attribute target = startElement.getAttributeByName(new QName("target"));
                Attribute component = startElement.getAttributeByName(new QName("label"));
                if (source != null && target != null && component != null) {
                  currentSourceId = Optional.ofNullable(source.getValue());
                  currentTargetId = Optional.ofNullable(target.getValue());
                  currentComponent = Optional.ofNullable(component.getValue());
                }
              }
              break;
            case "data":
              Attribute key = startElement.getAttributeByName(new QName("key"));
              if (key != null) {
                currentDataKey = Optional.ofNullable(key.getValue());
              }
              break;
          }
          break;
        case XMLEvent.CHARACTERS:
          if (currentDataKey.isPresent() && inGraph && level == 4) {
            String annoKey = keys.get(currentDataKey.get());
            if (annoKey != null) {
              // Copy all data attributes into our own map
              data.put(annoKey, event.asCharacters().getData());
            }
          }
          break;
        case XMLEvent.END_ELEMENT:
          EndElement endElement = event.asEndElement();
          switch (endElement.getName().getLocalPart()) {
            case "graph":
              inGraph = false;
              break;
            case "node":
              if (currentNodeId.isPresent()) {
                String nodeType = data.get("annis::node_type");
                if ("node".equals(nodeType)) {
                  // Map node and add it
                  SNode n = mapNode(currentNodeId.get(), data);
                  graph.addNode(n);
                } else if ("datasource".equals(nodeType)) {
                  // Create a textual datasource of this name
                  STextualDS ds = SaltFactory.createSTextualDS();
                  setNodeName(ds, currentNodeId.get());
                  mapLabels(ds, data, true);
                  ds.setText("");
                  datasourcesInGraphMl.put(Helper.addSaltPrefix(currentNodeId.get()), ds);
                }
              }
              currentNodeId = Optional.empty();
              data.clear();
              break;
            case "edge":
              // add edge
              if (currentSourceId.isPresent() && currentTargetId.isPresent()
                  && currentComponent.isPresent()) {
                mapAndAddEdge(currentSourceId.get(), currentTargetId.get(), currentComponent.get(),
                    data, gapEdges);
              }

              currentSourceId = Optional.empty();
              currentTargetId = Optional.empty();
              currentComponent = Optional.empty();
              data.clear();
              break;
            case "data":
              if (currentDataKey.isPresent()) {
                String annoKey = keys.get(currentDataKey.get());
                // Add an empty data entry if this element did not have any character child
                if (annoKey != null && !data.containsKey(annoKey)) {
                  data.put(annoKey, "");
                }
              }
              currentDataKey = Optional.empty();
              break;
          }

          level--;
          break;
      }
    }

    // Always create own own data sources from the tokens. Get all real token roots (ignore gaps)
    // and create a data source for each of them.
    recreateTextForTokenRoots(graph, gapEdges, datasourcesInGraphMl);

    // Create the text annotation for the segmentation nodes
    Multimap<String, SNode> orderRoots = graph.getRootsByRelationType(SALT_TYPE.SORDER_RELATION);
    if (orderRoots.isEmpty() && graph.getTokens().size() == 1) {
      // if there is only one token, there won't be any order relations
      orderRoots.put("", graph.getTokens().get(0));
    }
    orderRoots.keySet().forEach(name -> {
      ArrayList<SNode> roots = new ArrayList<>(orderRoots.get(name));
      if (SaltUtil.SALT_NULL_VALUE.equals(name)) {
        name = null;
      }
      if (name != null && !"".equals(name)) {
        // add the text as label to the spans
        addTextToSegmentation(name, roots);
      }
    });

    addNodeLayers();
  }


  /**
   * Resolve the ID to a fully qualified annotation name
   * 
   * @param keys
   * @param reader
   */
  private void addAnnotationKey(Map<String, String> keys, StartElement event) {

    Attribute id = event.getAttributeByName(new QName("id"));
    Attribute annoKey = event.getAttributeByName(new QName("attr.name"));

    if (id != null && id.getValue() != null && annoKey != null && annoKey.getValue() != null) {
      keys.put(id.getValue(), annoKey.getValue());
    }
  }

  private SNode mapNode(String nodeName, Map<String, String> labels) {
    SNode newNode;

    if ((labels.containsKey(ANNIS_TOK))) {
      if (!this.hasTimeline && hasOutgoingCoverageEdge.contains(nodeName)) {
        newNode = SaltFactory.createSSpan();
      } else {
        newNode = SaltFactory.createSToken();
      }
    } else if (hasOutgoingDominanceEdge.contains(nodeName)) {
      newNode = SaltFactory.createSStructure();
    } else {
      newNode = SaltFactory.createSSpan();
    }

    setNodeName(newNode, nodeName);
    mapLabels(newNode, labels, false);

    return newNode;
  }

  private void mapAndAddEdge(String sourceId, String targetId, String componentRaw,
      Map<String, String> labels, Map<SToken, SToken> gapEdges) {

    SNode source = graph.getNode(Helper.addSaltPrefix(sourceId));
    SNode target = graph.getNode(Helper.addSaltPrefix(targetId));

    // Split the component description into its parts
    Component component = parseComponent(componentRaw);

    if (source != null && target != null && source != target) {

      SRelation<?, ?> rel = null;
      switch (component.getType()) {
        case DOMINANCE:
          if ((component.getName() == null || component.getName().isEmpty())
              && hasNonEmptyDominanceEdge.contains(Pair.of(sourceId, targetId))) {
            // We don't include edges that have no type if there is an edge
            // between the same nodes which has a type.
            // In this case, exclude this relation
            return;
          } // end mirror check
          rel = graph.createRelation(source, target, SALT_TYPE.SDOMINANCE_RELATION, null);

          break;
        case POINTING:
          rel = graph.createRelation(source, target, SALT_TYPE.SPOINTING_RELATION, null);
          break;
        case ORDERING:
          if ("annis".equals(component.getLayer())
              && "datasource-gap".equals(component.getName())) {
            if (source instanceof SToken && target instanceof SToken) {
              gapEdges.put((SToken) source, (SToken) target);
            }
          } else {
            rel = graph.createRelation(source, target, SALT_TYPE.SORDER_RELATION, null);
          }

          break;
        case COVERAGE:
          // only add coverage edges in salt to spans, not structures
          if (source instanceof SSpan && target instanceof SToken) {
            rel = graph.createRelation(source, target, SALT_TYPE.SSPANNING_RELATION, null);
          }
          break;
        default:
          break;
      }

      if (rel != null) {
        rel.setType(component.getName());

        // map edge labels
        mapLabels(rel, labels, false);
        addEdgeLayers(component, rel);
      }
    }
  }

  private void recreateTextForTokenRoots(SDocumentGraph graph, Map<SToken, SToken> gapEdges,
      SortedMap<String, STextualDS> datasourcesInGraphMl) {

    Map<SToken, SToken> nextToken = new HashMap<>();
    Map<SToken, SToken> incomingOrderingEdgesWithGaphs = new HashMap<>();

    for (SOrderRelation rel : graph.getOrderRelations()) {
      if ((rel.getType() == null || "".equals(rel.getType())) && rel.getSource() instanceof SToken
          && rel.getTarget() instanceof SToken) {
        SToken source = (SToken) rel.getSource();
        SToken target = (SToken) rel.getTarget();

        nextToken.put(source, target);
        incomingOrderingEdgesWithGaphs.put(target, source);
      }
    }

    for (Map.Entry<SToken, SToken> rel : gapEdges.entrySet()) {
      incomingOrderingEdgesWithGaphs.put(rel.getValue(), rel.getKey());
    }

    // Get all root nodes (tokens without any incoming ordering edge, including gap edges)
    List<SToken> roots = graph.getTokens().stream()
        .filter(t -> !incomingOrderingEdgesWithGaphs.containsKey(t)).collect(Collectors.toList());

    for (SToken rootForText : roots) {
      final StringBuilder text = new StringBuilder();

      Map<SToken, Range<Integer>> token2Range = new HashMap<>();

      // traverse the token chain using the order relations
      SToken currentToken = rootForText;
      while (currentToken != null) {
        addTokenToText(currentToken, text, token2Range);

        SToken previousToken = currentToken;
        currentToken = nextToken.get(previousToken);
        // Step over the possible gap
        if (currentToken == null) {
          currentToken = gapEdges.get(previousToken);
        }
      }


      STextualDS ds = graph.createTextualDS(text.toString());
      if (datasourcesInGraphMl.size() == 1) {
        STextualDS origDs = datasourcesInGraphMl.get(datasourcesInGraphMl.firstKey());
        ds.setName(origDs.getName());
      }

      // add all relations
      token2Range.forEach((t, r) -> {
        STextualRelation rel = SaltFactory.createSTextualRelation();
        rel.setSource(t);
        rel.setTarget(ds);
        rel.setStart(r.lowerEndpoint());
        rel.setEnd(r.upperEndpoint());
        graph.addRelation(rel);
      });
    }


  }

  private void addTokenToText(SToken token, StringBuilder text,
      Map<SToken, Range<Integer>> token2Range) {
    SFeature featTokWhitespaceBefore = token.getFeature("annis::tok-whitespace-before");
    if (featTokWhitespaceBefore != null) {
      text.append(featTokWhitespaceBefore.getValue().toString());
    }

    SFeature featTok = token.getFeature(ANNIS_TOK);
    if (featTok != null) {
      int idxStart = text.length();
      text.append(featTok.getValue_STEXT());
      token2Range.put(token, Range.closed(idxStart, text.length()));
    }

    SFeature featTokWhitespaceAfter = token.getFeature("annis::tok-whitespace-after");
    if (featTokWhitespaceAfter != null) {
      text.append(featTokWhitespaceAfter.getValue().toString());
    }
  }



  private void addTextToSegmentation(final String name, List<SNode> rootNodes) {

    // traverse the token chain using the order relations
    graph.traverse(rootNodes, SGraph.GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "ORDERING_" + name,
        new GraphTraverseHandler() {
          @Override
          public boolean checkConstraint(SGraph.GRAPH_TRAVERSE_TYPE traversalType,
              String traversalId, @SuppressWarnings("rawtypes") SRelation relation, SNode currNode,
              long order) {
            if (relation == null) {
              return true;
            } else if (relation instanceof SOrderRelation
                && Objects.equal(name, relation.getType())) {
              return true;
            } else {
              return false;
            }
          }

          @Override
          public void nodeLeft(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
              SNode currNode, SRelation<SNode, SNode> relation, SNode fromNode, long order) {}

          @Override
          public void nodeReached(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
              SNode currNode, SRelation<SNode, SNode> relation, SNode fromNode, long order) {

            SFeature featTok = currNode.getFeature(ANNIS_TOK);
            if (featTok != null && currNode instanceof SSpan) {
              // only add the annotation if not yet existing (e.g. in another namespace)
              for (SAnnotation existingAnno : currNode.getAnnotations()) {
                if (Objects.equal(name, existingAnno.getName())) {
                  return;
                }
              }
              currNode.createAnnotation(null, name, featTok.getValue().toString());
            }
          }
        });
  }

  @Override
  protected SGraph getGraph() {
    return this.graph;
  }

}
