package annis.service.objects;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.model.Edge.EdgeType;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisToken;

@Deprecated
public class AnnisResultImpl implements AnnisResult
{

  // this class is sent to the frontend
  private static final long serialVersionUID = 1648848837712346094L;

  // the wrapped graph object
  private AnnotationGraph graph;
  // cache paula string
  private String paulaString = null;

  public AnnisResultImpl()
  {
  }

  public AnnisResultImpl(AnnotationGraph graph)
  {
    this.graph = graph;
  }

  public List<AnnisToken> getTokenList()
  {
    List<AnnisToken> result = new ArrayList<AnnisToken>();
    for(AnnisNode node : graph.getTokens())
    {
      AnnisTokenImpl annisToken = new AnnisTokenImpl(node.getId(),
        node.getSpannedText(), node.getLeft(), node.getRight(),
        node.getTokenIndex(), node.getCorpus());
      for(Annotation annotation : node.getNodeAnnotations())
      {
        annisToken.put(annotation.getQualifiedName(), annotation.getValue());
      }
      result.add(annisToken);
    }
    return result;
  }

  public long getStartNodeId()
  {
    List<AnnisNode> tokens = graph.getTokens();
    if(!tokens.isEmpty())
    {
      return tokens.get(0).getId();
    }
    else
    {
      return 0;
    }
  }

  public long getEndNodeId()
  {
    List<AnnisNode> tokens = graph.getTokens();
    if(!tokens.isEmpty())
    {
      return tokens.get(tokens.size() - 1).getId();
    }
    else
    {
      return 0;
    }
  }

  public Set<String> getAnnotationLevelSet()
  {
    Set<String> result = new HashSet<String>();
    for(AnnisNode node : graph.getNodes())
    {
      if(!node.isToken())
      {
        for(Annotation annotation : node.getNodeAnnotations())
        {
          result.add(annotation.getQualifiedName());
        }
      }
    }
    return result;
  }

  public Set<String> getTokenAnnotationLevelSet()
  {
    Set<String> result = new HashSet<String>();
    for(AnnisNode token : graph.getTokens())
    {
      for(Annotation annotation : token.getNodeAnnotations())
      {
        result.add(annotation.getQualifiedName());
      }
    }
    return result;
  }

  public String getMarkerId(Long nodeId)
  {
    return isMarked(nodeId) ? String.valueOf(nodeId) : null;
  }

  public boolean hasMarker(String markerId)
  {
    return isMarked(Long.parseLong(markerId));
  }

  public String getPaula()
  {
    if(paulaString != null)
    {
      return paulaString;
    }
    try
    {

      // namespaces
      Set<String> namespaces = new HashSet<String>();
      for(AnnisNode node : graph.getNodes())
      {
        addNamespace(namespaces, node.getNamespace());
        for(Annotation annotation : node.getNodeAnnotations())
        {
          addNamespace(namespaces, annotation.getNamespace());
        }
      }
      for(Edge edge : graph.getEdges())
      {
        for(Annotation annotation : edge.getAnnotations())
        {
          addNamespace(namespaces, annotation.getNamespace());
        }
      }

      // fn: token -> nodes above that token
      Map<AnnisNode, List<AnnisNode>> nodesAboveToken = new HashMap<AnnisNode, List<AnnisNode>>();
      for(AnnisNode token : graph.getTokens())
      {

        // collect tokens above a node using breadth-first search, to minimize bugs in PAULA-Unart
        List<AnnisNode> nodesAboveCurrentToken = new ArrayList<AnnisNode>();
        LinkedList<AnnisNode> queue = new LinkedList<AnnisNode>();

        for(Edge edge : token.getIncomingEdges())
        {
          if(edge.getSource() != null)
          {
            queue.add(edge.getSource());
          }
        }
        while(!queue.isEmpty())
        {
          AnnisNode node = queue.removeFirst();
          // can't use set, because order has to preserved
          if(!nodesAboveCurrentToken.contains(node))
          {
            nodesAboveCurrentToken.add(node);
          }
          for(Edge edge : node.getIncomingEdges())
          {
            if(edge.getSource() != null)
            {
              queue.add(edge.getSource());
            }
          }
        }

        // reverse order, so it goes from the roots to the token
        Collections.reverse(nodesAboveCurrentToken);
        nodesAboveToken.put(token, nodesAboveCurrentToken);
      }

      // xml header
      Document paulaDom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = paulaDom.createElement("RESULT");
      for(String namespace : namespaces)
      {
        root.setAttribute("xmlns:" + namespace, namespace);
      }
      root.setAttribute("xmlns:annis", "annis");
      paulaDom.appendChild(root);
      Element paula = paulaDom.createElement("paula");
      root.appendChild(paula);
      Element inline = paulaDom.createElement("inline");
      paula.appendChild(inline);

      // node annotation
      for(AnnisNode token : graph.getTokens())
      {

        // append nodes above token starting at roots
        Element last = inline;
        for(AnnisNode node : nodesAboveToken.get(token))
        {
          Element element = paulaDom.createElement(node.getQualifiedName());
          element.setAttribute("_id", String.valueOf(node.getId()));
          for(Annotation annotation : node.getNodeAnnotations())
          {
            element.setAttribute(annotation.getQualifiedName(), annotation.getValue());
          }
          last.appendChild(element);
          last = element;
        }

        // append token
        Element tok = paulaDom.createElement("tok");
        tok.setAttribute("_id", String.valueOf(token.getId()));
        for(Annotation annotation : token.getNodeAnnotations())
        {
          tok.setAttribute(annotation.getQualifiedName(), annotation.getValue());
        }
        last.appendChild(tok);
        String text = token.getSpannedText();
        tok.appendChild(paulaDom.createTextNode(text != null ? text : ""));
      }

      // edges
      for(Edge edge : graph.getEdges())
      {
        if(edge.getSource() == null)
        {
          continue;
        }
        if(edge.getAnnotations().isEmpty() && edge.getEdgeType() != EdgeType.POINTING_RELATION)
        {
          continue;
        }
        Element rel = paulaDom.createElement("_rel");
        rel.setAttribute("_src", String.valueOf(edge.getSource().getId()));
        rel.setAttribute("_dst", String.valueOf(edge.getDestination().getId()));
        for(Annotation annotation : edge.getAnnotations())
        {
          rel.setAttribute(annotation.getQualifiedName(), annotation.getValue());
        }
        if(edge.getEdgeType() == EdgeType.POINTING_RELATION)
        {
          rel.setAttribute("annis:type", "p");
          rel.setAttribute("annis:subtype", edge.getQualifiedName() + "_" + edge.getComponent());
        }
        inline.appendChild(rel);

      }

      // create XML
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();
      trans.setOutputProperty(OutputKeys.INDENT, "yes");

      StringWriter writer = new StringWriter();
      DOMSource domSource = new DOMSource(paulaDom);
      StreamResult streamResult = new StreamResult(writer);
      trans.transform(domSource, streamResult);

      String utf8 = new String(writer.toString().getBytes("UTF-8"), "UTF-8");
      paulaString = utf8;

      return paulaString;
    }
    catch(Exception e)
    {
      throw new RuntimeException("couldn't generate PAULA-Unart: " + e.getMessage());
    }
  }

  private void addNamespace(Set<String> namespaces, String namespace)
  {
    if(namespace != null)
    {
      namespaces.add(namespace);
    }
  }

  private boolean isMarked(Long nodeId)
  {
    return graph.getMatchedNodeIds().contains(nodeId);
  }

  @Deprecated
  public String getJSON()
  {
    StringBuffer json = new StringBuffer();
    json.append("{'_id':'" + getStartNodeId() + "," + getEndNodeId() + "', '_textId': '" + jsonGetTextId() + "', '_text':'" + jsonGetText().replace("'", "\\'") + "'");

    //add annotation levels
    json.append(", '_levels': [");
    int c = 0;
    for(String level : this.getAnnotationLevelSet())
    {
      json.append(((c++ > 0) ? ", " : "") + "'" + level + "'");
    }
    json.append("]");

    //add a list of marked objects
    json.append(", '_markedObjects': [");
    c = 0;
    for(Long id : jsonMarkedIds())
    {
      if(c++ > 0)
      {
        json.append(", ");
      }
      json.append(id);
    }
    json.append("]");

    //add token annotation levels
    json.append(", '_tokenLevels': [");
    c = 0;
    for(String level : this.getTokenAnnotationLevelSet())
    {
      json.append(((c++ > 0) ? ", " : "") + "'" + level + "'");
    }
    json.append("]");

    int tokenCount = 0;

    List<AnnisToken> tokenList = this.getTokenList();
    // XXX: Tokens unterhalb eines markierten Knoten werden nicht weiter markiert
    int matchStart = 0;
    int matchEnd = tokenList.size() - 1;
//		int matchStart = tokenList.size() - 1, matchEnd = 0;
    for(AnnisToken token : tokenList)
    {
      if(jsonHasNodeMarker(token.getId()))
      {
//				if(tokenCount > matchEnd)
//					matchEnd = tokenCount;
//				if(tokenCount < matchStart)
//					matchStart = tokenCount;
      }
      String marker = jsonHasNodeMarker(token.getId()) ? getMarkerId(token.getId()) : "";
      json.append(",'" + tokenCount++ + "':{'_id': " + token.getId()
        + ", '_text':'" + (token.getText() != null ? token.getText().replace("'", "\\'") : "")
        + "', '_marker':'" + marker + "'" + ", '_corpusId':'" + token.getCorpusId());
      for(Map.Entry<String, String> annotation : token.entrySet())
      {
        json.append(", '" + annotation.getKey() + "':'" + annotation.getValue().replace("'", "\\'") + "'");
      }
      json.append("}");
    }
    json.append(", '_matchStart' : '" + matchStart + "'");
    json.append(", '_matchEnd' : '" + matchEnd + "'");
    json.append("}");
    return json.toString();
  }

  private Set<Long> jsonMarkedIds()
  {
    return graph.getMatchedNodeIds();
  }

  private String jsonGetText()
  {
    List<String> tokenSpans = new ArrayList<String>();
    for(AnnisNode token : graph.getTokens())
    {
      tokenSpans.add(token.getSpannedText());
    }
    return StringUtils.join(tokenSpans, " ");
  }

  private String jsonGetTextId()
  {
    if(graph.getNodes().isEmpty())
    {
      return "1";
    }
    else
    {
      return String.valueOf(graph.getNodes().get(0).getTextId());
    }
  }

  private boolean jsonHasNodeMarker(long id)
  {
    return isMarked(id);
  }
}
