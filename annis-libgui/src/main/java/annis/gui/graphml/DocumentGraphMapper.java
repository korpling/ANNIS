package annis.gui.graphml;

import com.google.common.base.Objects;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.Component;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps a GraphML stream to Salt
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 *
 */
public class DocumentGraphMapper extends AbstractGraphMLMapper {

  private static final String IGNORED_TOK = "ignored-tok";

  static final Logger log = LoggerFactory.getLogger(DocumentGraphMapper.class);

  private final SDocumentGraph graph;

  private final Set<String> hasOutgoingCoverageEdge;
  private final Set<String> hasOutgoingDominanceEdge;
  private final Set<String> hasNonEmptyIncomingDominanceEdge;
  private final boolean mapIgnoredToken;


  protected DocumentGraphMapper(boolean mapIgnoredToken) {
    this.mapIgnoredToken = mapIgnoredToken;
    this.graph = SaltFactory.createSDocumentGraph();
    this.hasOutgoingCoverageEdge = new HashSet<>();
    this.hasOutgoingDominanceEdge = new HashSet<>();
    this.hasNonEmptyIncomingDominanceEdge = new HashSet<>();
  }

  public static SDocumentGraph map(Reader input) throws IOException, XMLStreamException {
    return map(input, false);
  }

  public static SDocumentGraph map(Reader input, boolean mapWhiteSpaceToken)
      throws IOException, XMLStreamException {
    DocumentGraphMapper mapper = new DocumentGraphMapper(mapWhiteSpaceToken);
    mapper.execute(input);
    return mapper.graph;
  }

  @Override
  protected void firstPass(XMLEventReader reader) throws XMLStreamException {
    while (reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      if (event.isStartElement()) {
        StartElement element = event.asStartElement();
        if ("edge".equals(element.getName().getLocalPart())) {
          Attribute source = element.getAttributeByName(new QName("source"));
          Attribute target = element.getAttributeByName(new QName("target"));
          Attribute label = element.getAttributeByName(new QName("label"));
          if (label != null) {
            Component c = parseComponent(label.getValue());
            if (source != null) {
              if (c.getType() == AnnotationComponentType.COVERAGE) {
                hasOutgoingCoverageEdge.add(source.getValue());
              } else if (c.getType() == AnnotationComponentType.DOMINANCE) {
                hasOutgoingDominanceEdge.add(source.getValue());
              }
            }
            if (target != null && c.getType() == AnnotationComponentType.DOMINANCE
                && !c.getName().isEmpty()) {
              hasNonEmptyIncomingDominanceEdge.add(target.getValue());
            }
          }

        }
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
                if ("node".equals(nodeType)
                    || (this.mapIgnoredToken && IGNORED_TOK.equals(nodeType))) {
                  // Map node and add it
                  SNode n = mapNode(currentNodeId.get(), data);
                  graph.addNode(n);
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
                    data);
              }

              currentSourceId = Optional.empty();
              currentTargetId = Optional.empty();
              currentComponent = Optional.empty();
              data.clear();
              break;
            case "data":
              currentDataKey = Optional.empty();
              break;
          }

          level--;
          break;
      }
    }

    // find all chains of SOrderRelations and reconstruct the texts belonging to
    // them
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
      if (this.mapIgnoredToken && "text".equals(name)) {
        // Use the special "text" ordering component to recreate the full text
        recreateText("text", roots);
      } else if (!this.mapIgnoredToken && (name == null || "".equals(name))) {
        // only re-create text if this is the default (possible virtual) tokenization
        recreateText(name, roots);
      } else {
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
    SNode newNode = SaltFactory.createSNode();

    if ((labels.containsKey("annis::tok") || labels.containsKey("annis::ignored-tok"))
        && !hasOutgoingCoverageEdge.contains(nodeName)) {
      newNode = SaltFactory.createSToken();
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
      Map<String, String> labels) {

    SNode source = graph.getNode("salt:/" + sourceId);
    SNode target = graph.getNode("salt:/" + targetId);

    // Split the component description into its parts
    Component component = parseComponent(componentRaw);

    if (source != null && target != null && source != target) {

      SRelation<?, ?> rel = null;
      switch (component.getType()) {
        case DOMINANCE:
          if (component.getLayer() == null || component.getLayer().isEmpty()) {
            // We don't include edges that have no type if there is an edge
            // between the same nodes which has a type.
            if (hasNonEmptyIncomingDominanceEdge.contains(sourceId)) {
              // exclude this relation
              return;
            }
          } // end mirror check
          rel = graph.createRelation(source, target, SALT_TYPE.SDOMINANCE_RELATION, null);

          break;
        case POINTING:
          rel = graph.createRelation(source, target, SALT_TYPE.SPOINTING_RELATION, null);
          break;
        case ORDERING:
          rel = graph.createRelation(source, target, SALT_TYPE.SORDER_RELATION, null);
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

  private void recreateText(final String textName, List<SNode> rootsForText) {

    final StringBuilder text = new StringBuilder();
    final STextualDS ds = graph.createTextualDS("");
    ds.setName(textName);

    Map<SToken, Range<Integer>> token2Range = new HashMap<>();

    // traverse the token chain using the order relations
    Iterator<SNode> itRoots = rootsForText.iterator();
    while (itRoots.hasNext()) {
      SNode root = itRoots.next();
      graph.traverse(Arrays.asList(root), SGraph.GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
          "ORDERING_" + textName, new GraphTraverseHandler() {
            @SuppressWarnings("rawtypes")
            @Override
            public boolean checkConstraint(SGraph.GRAPH_TRAVERSE_TYPE traversalType,
                String traversalId, SRelation relation, SNode currNode, long order) {
              if (relation == null) {
                return true;
              } else if (relation instanceof SOrderRelation
                  && Objects.equal(textName, relation.getType())) {
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
              if (!mapIgnoredToken && fromNode != null) {
                text.append(" ");
              }

              SFeature featTok = currNode.getFeature("annis::tok");
              if(featTok == null) {
                // The feature might also be mapped to the ignored-tok label
                // (so it is ignored by AQL "tok" queries)
                featTok = currNode.getFeature("annis::ignored-tok");
              }
              if (featTok != null && currNode instanceof SToken) {
                int idxStart = text.length();
                text.append(featTok.getValue_STEXT());
                token2Range.put((SToken) currNode, Range.closed(idxStart, text.length()));
              }
            }
          });
      if (!mapIgnoredToken && itRoots.hasNext()) {
        text.append(" ");
      }
    }

    // update the actual text
    ds.setText(text.toString());

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

  private void addTextToSegmentation(final String name, List<SNode> rootNodes) {

    // traverse the token chain using the order relations
    graph.traverse(rootNodes, SGraph.GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "ORDERING_" + name,
        new GraphTraverseHandler() {
          @Override
          public boolean checkConstraint(SGraph.GRAPH_TRAVERSE_TYPE traversalType,
              String traversalId, @SuppressWarnings("rawtypes") SRelation relation, SNode currNode,
              long order) {
            if (relation == null) {
              // TODO: check if this is ever true
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

            SFeature featTok = currNode.getFeature("annis::tok");
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