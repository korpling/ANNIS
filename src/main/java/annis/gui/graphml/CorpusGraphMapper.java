package annis.gui.graphml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusDocumentRelation;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SCorpusRelation;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SGraph;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

/**
 * Maps a GraphML stream to Salt
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 *
 */
public class CorpusGraphMapper extends AbstractGraphMLMapper {

  private final SCorpusGraph graph;

  private final Set<String> hasIncomingPartOfEdge;


  protected CorpusGraphMapper() {
    this.graph = SaltFactory.createSCorpusGraph();
    this.hasIncomingPartOfEdge = new HashSet<>();
  }

  public static SCorpusGraph map(File inputFile) throws IOException, XMLStreamException {
    CorpusGraphMapper mapper = new CorpusGraphMapper();
    mapper.execute(inputFile);
    return mapper.graph;
  }

  @Override
  protected void firstPass(XMLEventReader reader) throws XMLStreamException {
    while (reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      if (event.isStartElement()) {
        StartElement element = event.asStartElement();
        if ("edge".equals(element.getName().getLocalPart())) {
          Attribute target = element.getAttributeByName(new QName("target"));
          Attribute label = element.getAttributeByName(new QName("label"));
          if (label != null) {
            Component c = parseComponent(label.getValue());
            if (target != null) {
              if (c.getType() == AnnotationComponentType.PARTOF) {
                hasIncomingPartOfEdge.add(target.getValue());
              }
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
              if (currentNodeId.isPresent() && "corpus".equals(data.get("annis::node_type"))) {
                // Map node and add it
                SNode n = mapNode(currentNodeId.get(), data);
                graph.addNode(n);
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

    if (!hasIncomingPartOfEdge.contains(nodeName)) {
      newNode = SaltFactory.createSDocument();
    } else {
      newNode = SaltFactory.createSCorpus();
    }

    setNodeName(newNode, nodeName);
    mapLabels(newNode, labels, true);

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
        case PARTOF:
          if (source instanceof SCorpus && target instanceof SDocument) {
            SCorpusDocumentRelation corpusDocRel = SaltFactory.createSCorpusDocumentRelation();
            corpusDocRel.setSource((SCorpus) source);
            corpusDocRel.setTarget((SDocument) target);
            graph.addRelation(corpusDocRel);
            rel = corpusDocRel;
          } else if (source instanceof SCorpus && target instanceof SCorpus) {
            SCorpusRelation corpusDocRel = SaltFactory.createSCorpusRelation();
            corpusDocRel.setSource((SCorpus) source);
            corpusDocRel.setTarget((SCorpus) target);
            graph.addRelation(corpusDocRel);
          }
          break;
        default:
          break;
      }

      if (rel != null) {
        // map edge labels
        mapLabels(rel, labels, false);
        addEdgeLayers(component, rel);
      }
    }
  }

  @Override
  protected SGraph getGraph() {
    return this.graph;
  }
}
