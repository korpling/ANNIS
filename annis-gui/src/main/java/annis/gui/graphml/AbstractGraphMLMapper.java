package annis.gui.graphml;

import com.google.common.base.Splitter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.tuple.Pair;
import org.atmosphere.util.ReaderInputStream;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.Component;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.core.SAnnotationContainer;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.SaltUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractGraphMLMapper {


  private static final Logger log = LoggerFactory.getLogger(AbstractGraphMLMapper.class);

  protected static void mapLabels(SAnnotationContainer n, Map<String, String> labels,
      boolean isMeta) {
    for (Map.Entry<String, String> e : labels.entrySet()) {
      Pair<String, String> qname = SaltUtil.splitQName(e.getKey());
      String name = qname.getRight();
      if (name == null || name.isEmpty()) {
        log.warn("Replacing empty label name with '_' ({}:{}={})", qname.getLeft(), name,
            e.getValue());
        name = "_";
      }
      if ("annis".equals(qname.getLeft()) && !"time".equals(name)) {
        n.createFeature(qname.getLeft(), name, e.getValue());
      } else if (isMeta) {
        n.createMetaAnnotation(qname.getLeft(), name, e.getValue());
      } else {
        n.createAnnotation(qname.getLeft(), name, e.getValue());
      }
    }
  }


  protected static Component parseComponent(String label) {
    Component result = new Component();

    if (label != null) {

      List<String> splitted = Splitter.on('/').limit(3).splitToList(label);
      result.setType(AnnotationComponentType.fromValue(splitted.get(0)));
      if (splitted.size() >= 1) {
        result.setLayer(splitted.get(1));
      } else {
        result.setLayer("");
      }
      if (splitted.size() >= 2) {
        result.setName(splitted.get(2));
      } else {
        result.setName("");
      }
    }

    return result;
  }


  public AbstractGraphMLMapper() {}


  protected void execute(Reader input) throws IOException, XMLStreamException {
    // Copy content of stream to a temporary file, so we can have multiple parse passes
    Path tmpFile = Files.createTempFile("graphannis-input-", ".graphml");
    try {
      Files.copy(new ReaderInputStream(input, StandardCharsets.UTF_8), tmpFile,
          StandardCopyOption.REPLACE_EXISTING);

      XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.IS_VALIDATING, false);

      // 1. pass, check which nodes have an outgoing edge of a certain types
      FileInputStream fileInput = new FileInputStream(tmpFile.toFile());
      XMLEventReader reader = factory.createXMLEventReader(fileInput);
      firstPass(reader);
      fileInput.close();

      // 2. pass, map nodes and edges with the correct type
      fileInput = new FileInputStream(tmpFile.toFile());
      reader = factory.createXMLEventReader(fileInput);
      secondPass(reader);
      fileInput.close();
    } finally {
      Files.deleteIfExists(tmpFile);
    }
  }

  protected abstract void firstPass(XMLEventReader reader) throws XMLStreamException;

  protected void setNodeName(SNode newNode, String nodeName) {

    if (!nodeName.startsWith("salt:/")) {
      nodeName = "salt:/" + nodeName;
    }

    newNode.setId(nodeName);
    // get the name from the ID
    newNode.setName(newNode.getPath().fragment());
  }


  protected abstract void secondPass(XMLEventReader reader) throws XMLStreamException;

  protected abstract SGraph getGraph();


  protected void addNodeLayers() {
    List<SNode> nodeList = new LinkedList<>(getGraph().getNodes());
    for (SNode n : nodeList) {
      SFeature featLayer = n.getFeature("annis", "layer");
      if (featLayer != null) {
        String layerName = featLayer.getValue_STEXT();
        List<SLayer> layer = getGraph().getLayerByName(layerName);
        if (layer == null || layer.isEmpty()) {
          SLayer newLayer = SaltFactory.createSLayer();
          newLayer.setName(layerName);
          getGraph().addLayer(newLayer);
          layer = Arrays.asList(newLayer);
        }
        layer.get(0).addNode(n);
      }
    }
  }

  protected void addEdgeLayers(Component component, SRelation<?, ?> rel) {
    String layerName = component.getLayer();
    if (layerName != null && !layerName.isEmpty()) {
      List<SLayer> layer = getGraph().getLayerByName(layerName);
      if (layer == null || layer.isEmpty()) {
        SLayer newLayer = SaltFactory.createSLayer();
        newLayer.setName(layerName);
        getGraph().addLayer(newLayer);
        layer = Arrays.asList(newLayer);
      }
      layer.get(0).addRelation(rel);
    }
  }

}
