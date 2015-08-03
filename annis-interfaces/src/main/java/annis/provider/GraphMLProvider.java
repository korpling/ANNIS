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
package annis.provider;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Provider
public class GraphMLProvider implements MessageBodyWriter<SaltProject>
{

  public static final MediaType APPLICATION_GRAPHML = new MediaType(
    "application",
    "graphml");

  private final static Logger log = LoggerFactory.getLogger(
    GraphMLProvider.class);

  public final static String NS = "http://graphml.graphdrawing.org/xmlns";
  
  @Override
  public boolean isWriteable(
    Class<?> type, Type genericType, Annotation[] annotations,
    MediaType mediaType)
  {
    return (APPLICATION_GRAPHML.isCompatible(mediaType))
      && SaltProject.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(SaltProject t,
    Class<?> type, Type genericType, Annotation[] annotations,
    MediaType mediaType)
  {
    return -1;
  }

  @Override
  public void writeTo(SaltProject t,
    Class<?> type, Type genericType, Annotation[] annotations,
    MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
    OutputStream entityStream) throws IOException, WebApplicationException
  {

    XMLOutputFactory factory = XMLOutputFactory.newFactory();
    try
    {
      XMLStreamWriter w = factory.createXMLStreamWriter(entityStream);
      w.setDefaultNamespace(NS);

      w.writeStartDocument();

      w.writeStartElement(NS, "graphml");
      w.writeDefaultNamespace(NS);
      w.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
      w.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", 
        "schemaLocation", NS + " http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd");
      
      // labels we use
      w.writeStartElement(NS, "key");
      w.writeAttribute(NS, "id", "name");
      w.writeAttribute(NS, "for", "node");
      w.writeAttribute(NS, "attr.type", "string");
      w.writeEndElement();
      
      // write out the corpus structure as the "root" graph
      EList<SCorpusGraph> corpusGraphs = t.getSCorpusGraphs();
      if (corpusGraphs != null)
      {
        for (SCorpusGraph corpusGraph : corpusGraphs)
        {
          w.writeStartElement(NS, "graph");
          w.writeAttribute(NS, "edgedefault", "directed");
          w.writeAttribute(NS, "id", corpusGraph.getSId());
          writeCorpusStructure(w, corpusGraph);
          w.writeEndElement();
        }
      }
      w.writeEndDocument();
      w.close();
    }
    catch (XMLStreamException ex)
    {
      log.error("Could not write GraphML", ex);
    }
    entityStream.flush();
  }

  private void writeCorpusStructure(XMLStreamWriter w, SCorpusGraph corpusGraph)
    throws XMLStreamException
  {
    // output each corpus, sub-corpus and document as node
    EList<SNode> corpora = corpusGraph.getSNodes();
    if (corpora != null)
    {
      for (SNode c : corpora)
      {
        writeSCorpusNode(w, c);
      }
    }

    // also output the edges between the (sub-) corpora
  }

  private void writeSCorpusNode(XMLStreamWriter w, SNode c) throws XMLStreamException
  {
    w.writeStartElement(NS, "node");
    URI id = c.getSElementPath();
    if(id.fragment() != null)
    {
      w.writeAttribute(NS, "id", c.getSElementPath().fragment());
    }
    else
    {
      w.writeAttribute(NS, "id", id.segment(id.segmentCount()-1));
    }
    
    w.writeStartElement(NS, "data");
    w.writeAttribute(NS, "key", "name");
    w.writeCharacters(c.getSName());
    w.writeEndElement();
    w.writeEndElement();
  }

}
