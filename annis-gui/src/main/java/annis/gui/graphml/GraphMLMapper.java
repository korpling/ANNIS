package annis.gui.graphml;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SNode;

/**
 * Maps a GraphML stream to Salt
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 *
 */
public class GraphMLMapper {

  private XMLEventReader reader;

  private final SDocumentGraph docGraph;

  private final BiMap<Integer, SNode> nodesByID;

  private final Map<Integer, Integer> node2timelinePOT;

  protected GraphMLMapper(XMLEventReader reader) {
    this.reader = reader;
    this.docGraph = SaltFactory.createSDocumentGraph();
    this.nodesByID = HashBiMap.create();
    this.node2timelinePOT = new HashMap<>();
  }

  public static SDocumentGraph mapDocumentGraph(InputStream input) throws XMLStreamException {
    SDocumentGraph graph = SaltFactory.createSDocumentGraph();

    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.IS_VALIDATING, false);

    XMLEventReader reader = factory.createXMLEventReader(input);

    GraphMLMapper mapper = new GraphMLMapper(reader);
    mapper.mapDocGraph();

    return mapper.docGraph;
  }

  private void mapDocGraph() throws XMLStreamException {

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
        case XMLEvent.START_DOCUMENT:
          level++;
          StartElement startElement = event.asStartElement();
          // create all new nodes
          switch (startElement.getName().getLocalPart()) {
            case "graph":
              if(level == 2) {
                inGraph = true;
              }
              break;
            case "key":
              if(level == 2) {
                addAnnotationKey(keys, startElement);
              }
              break;
            case "node":
              if(inGraph && level == 3) {
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
              if(key != null) {
                currentDataKey = Optional.ofNullable(key.getValue());
              }
              break;
          }
          break;
        case XMLEvent.CHARACTERS:
          if(currentDataKey.isPresent() && inGraph && level == 4) {
            String annoKey = keys.get(currentDataKey.get());
            if(annoKey != null) {
              // Copy all data attributes into our own map
              data.put(annoKey, event.asCharacters().getData());
            }
          }
          break;
        case XMLEvent.END_ELEMENT:
          EndElement endElement = event.asEndElement();
          switch(endElement.getName().getLocalPart()) {
            case "graph":
              inGraph = false;
              break;
            case "node":
              if (currentNodeId.isPresent()) {
                // Resolve labels to their actual qualified annotation name and map node
                mapNode(currentNodeId.get(), resolveLabels(data, keys));
              }
              currentNodeId = Optional.empty();
              break;
            case "edge":
              // TODO add edge

              currentSourceId = Optional.empty();
              currentTargetId = Optional.empty();
              currentComponent = Optional.empty();
              break;
            case "data":
              currentDataKey = Optional.empty();
              break;
          }
          
          level--;
          break;
      }
    }
  }

  private static Map<String, String> resolveLabels(Map<String, String> data,
      Map<String, String> keys) {
    LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
    
    for(Map.Entry<String, String> e : data.entrySet()) {
      String resolvedAnnoName = keys.get(e.getKey());
      if(resolvedAnnoName != null) {
        result.put(resolvedAnnoName, e.getValue());
      }
    }
    
    return result;
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

    //
    // if (labels.containsKey("tok") && !hasCoverageEdge(node)) {
    // newNode = SaltFactory.createSToken();
    // } else if (hasDominanceEdge(node)) {
    // newNode = SaltFactory.createSStructure();
    // } else {
    // newNode = SaltFactory.createSSpan();
    // }
    //
    // if (!nodeName.startsWith("salt:/")) {
    // nodeName = "salt:/" + nodeName;
    // }
    // newNode.setId(nodeName);
    // // get the name from the ID
    // newNode.setName(newNode.getPath().fragment());
    //
    // mapLabels(newNode, labels, false);

    return newNode;
  }


}
