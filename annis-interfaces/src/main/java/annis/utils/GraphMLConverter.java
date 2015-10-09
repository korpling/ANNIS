/*
 * Copyright 2015 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.utils;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.IdentifiableElement;
import org.corpus_tools.salt.graph.Label;
import org.corpus_tools.salt.graph.LabelableElement;
import org.corpus_tools.salt.graph.Node;
import org.corpus_tools.salt.graph.Relation;
import org.corpus_tools.salt.SALT_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class GraphMLConverter
{
  private final static Logger log = LoggerFactory.getLogger(GraphMLConverter.class);
  
  public final static String NS = "http://graphml.graphdrawing.org/xmlns";
  
  public static void convertFromSalt(OutputStream out, SDocument... docs)
  {
    convertFromSalt(out, Arrays.asList(docs));
  }
  
  public static void convertFromSalt(OutputStream out, List<SDocument> docs)
  {
    XMLOutputFactory factory = XMLOutputFactory.newFactory();
    try
    {
      XMLStreamWriter w = factory.createXMLStreamWriter(out);
      w.setDefaultNamespace(NS);

      w.writeStartDocument();

      w.writeStartElement(NS, "graphml");
      w.writeDefaultNamespace(NS);
      w.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
      w.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", 
        "schemaLocation", NS + " http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd");
      
      Set<String> existingKeys = new HashSet<>();
      IDManager ids = new IDManager();

      // we always use the "salt::type" label 
      w.writeStartElement(NS, "key");
      w.writeAttribute(NS, "id", "salt::type");
      w.writeAttribute(NS, "for", "all");
      w.writeAttribute(NS, "attr.type", "string");
      w.writeEndElement();
      existingKeys.add("salt::type");
      
      if(docs != null)
      {
        // first get all possible label names and write them as "key"
        for(SDocument doc : docs)
        {
          SDocumentGraph g = doc.getDocumentGraph();
          if(g != null)
          {
            writeKeys(w, g.getNodes(), existingKeys);
            writeKeys(w, g.getRelations(), existingKeys);
            if(g.getDocument() != null)
            {
              writeKeys(w, g.getDocument().getLabels(), existingKeys);
            }
          }
        }
        // write the actual graphs
        for(SDocument d : docs)
        {
          writeSDocumentGraph(w, d.getDocumentGraph(), ids, existingKeys, true);
        }
      }
      w.writeEndDocument();
      w.close();
      
      out.flush();
    }
    catch (XMLStreamException | IOException ex)
    {
      log.error("Could not write GraphML", ex);
    }
    
  }
  
  private static void writeKeys(XMLStreamWriter w, Collection<? extends LabelableElement> elements, Set<String> existing)
    throws XMLStreamException
  {
    if (elements != null && !elements.isEmpty())
    {
      for (LabelableElement e : elements)
      {
        Collection<Label> labels = e.getLabels();
        if (labels != null && !labels.isEmpty())
        {
          for (Label l : labels)
          {
            String id = l.getQName();
            if (!existing.contains(id))
            {
              Object o = l.getValue();
              String type = null;
              if(o instanceof Boolean)
              {
                type = "boolean";
              }
              else if(o instanceof Integer)
              {
                type = "int";
              }
              else if(o instanceof Long)
              {
                type = "long";
              }
              else if(o instanceof Float)
              {
                type = "float";
              }
              else if(o instanceof Double)
              {
                type = "double";
              }
              else if(o instanceof String)
              {
                type = "string";
              }
              if(type != null)
              {
                w.writeStartElement(NS, "key");
                w.writeAttribute(NS, "id", l.getQName());
                w.writeAttribute(NS, "for", "all");
                w.writeAttribute(NS, "attr.type", type);

                w.writeEndElement();
                existing.add(id);
              }
            }
          }
        }
      }
    }
  }

  private static void writeLabels(XMLStreamWriter w, Collection<Label> labels,
    Set<String> existingKeys) 
    throws XMLStreamException
  {
    if(labels != null && !labels.isEmpty())
    {
      for(Label l : labels)
      {
        String key = l.getQName();
        if(existingKeys.contains(key))
        {
          w.writeStartElement(NS, "data");
          w.writeAttribute(NS, "key", key);
          w.writeCharacters("" + l.getValue());
          w.writeEndElement();
        }
      }
    }
  }
  
  /**
   * Writes a data element describing the type of the object.
   * @param w
   * @param o
   * @throws XMLStreamException 
   */
  private static void writeType(XMLStreamWriter w, Object o)
    throws XMLStreamException
  {
    Set<SALT_TYPE> types = SALT_TYPE.class2SaltType(o.getClass());
    if(!types.isEmpty())
    {
      w.writeStartElement(NS, "data");
      w.writeAttribute(NS, "key", "salt::type");
      w.writeCharacters(types.iterator().next().name());
      w.writeEndElement();
    }
  }

  private static void writeNode(XMLStreamWriter w, Node c, 
    IDManager ids,
    Set<String> existingKeys) throws XMLStreamException
  {
    w.writeStartElement(NS, "node");
    w.writeAttribute(NS, "id", ids.getID(c));
    
    writeType(w, c);
    writeLabels(w, c.getLabels(), existingKeys);
    w.writeEndElement();
  }
  
  private static void writeRelation(XMLStreamWriter w, Relation e, 
    IDManager ids, Set<String> existingKeys) throws XMLStreamException
  {
    w.writeStartElement(NS, "relation");
    w.writeAttribute(NS, "id", ids.getID(e));
    w.writeAttribute(NS, "source", ids.getID(e.getSource()));
    w.writeAttribute(NS, "target", ids.getID(e.getTarget()));
    
    writeType(w, e);
    writeLabels(w, e.getLabels(), existingKeys);
    
    w.writeEndElement();
  }
  
  
  private static void writeSDocumentGraph(XMLStreamWriter w, 
    SDocumentGraph g, 
    IDManager ids, Set<String> existingKeys, boolean includeDocLabels) throws XMLStreamException
  {
    if(g == null)
    {
      return;
      
    }
    List<SNode> nodes = g.getNodes();
    List<SRelation<SNode, SNode>> relations = g.getRelations();
    // graphs without nodes are not allowed
    if(nodes != null && !nodes.isEmpty())
    {
      w.writeStartElement(NS, "graph");
      w.writeAttribute(NS, "id", ids.getID(g));
      w.writeAttribute(NS, "relationdefault", "directed");
      
      if(includeDocLabels && g.getDocument() != null)
      {
        writeLabels(w, g.getDocument().getLabels(), existingKeys);
      }
      
      for(SNode n : nodes)
      {
        writeNode(w, n, ids, existingKeys);
      }
      
      if(relations != null)
      {
        for(SRelation e : relations)
        {
          writeRelation(w, e, ids, existingKeys);
        }
      }

      w.writeEndElement();
    }
  }
  
  private static class IDManager
  {
    private final AtomicLong counter = new AtomicLong(0);
    
    private final Map<IdentifiableElement, String> existing = new HashMap<>();
    
    private String getID(IdentifiableElement e)
    {
     
      String id = existing.get(e);
      if(id == null)
      {
        id = "_" + counter.getAndIncrement();
        existing.put(e, id);
      }
      return id;
    }
    
  }
}
