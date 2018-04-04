/*
 * Copyright 2012 SFB 632.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.util.internal.persistence.SaltXML10Handler;
import org.corpus_tools.salt.util.internal.persistence.SaltXML10Writer;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import annis.model.AnnisConstants;

/**
 *
 * @author thomas
 */
@Provider
public class SaltProjectProvider implements MessageBodyWriter<SaltProject>,
  MessageBodyReader<SaltProject>
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SaltProjectProvider.class);

  private static final XMLOutputFactory outFactory = XMLOutputFactory.newFactory();

  public static final MediaType APPLICATION_XMI_XML = new MediaType(
    "application",
    "xmi+xml");

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return (MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)
      || MediaType.TEXT_XML_TYPE.isCompatible(mediaType)
      || APPLICATION_XMI_XML.isCompatible(mediaType))
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
  public void writeTo(SaltProject project,
    Class<?> type, Type genericType, Annotation[] annotations,
    MediaType mediaType,
    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
    throws IOException, WebApplicationException
  {

    SaltXML10Writer writer = new SaltXML10Writer();

    try
    {
      XMLStreamWriter xml = outFactory.createXMLStreamWriter(entityStream, "UTF-8");

      xml.writeStartDocument("1.1");
      xml.writeCharacters("\n");
      long startTime = System.currentTimeMillis();
      
      // output XMI root element
      writer.writeXMIRootElement(xml);
      
      for(SCorpusGraph corpusGraph : project.getCorpusGraphs())
      {
        for(SDocument doc : corpusGraph.getDocuments())
        {
          // make sure that any ANNIS feature on the document is copied to the document graph
          SDocumentGraph docGraph = doc.getDocumentGraph();
          for(SFeature feat : doc.getFeatures())
          {
            if(AnnisConstants.ANNIS_NS.equals(feat.getNamespace()))
            {
              SFeature newFeat = SaltFactory.createSFeature();
              feat.copy(newFeat);
              docGraph.addFeature(newFeat);
            }
          }
          
          writer.writeObjects(xml, docGraph);
        }
      }
      xml.writeEndDocument();
      long endTime = System.currentTimeMillis();
      log.debug("Saving XMI (" + mediaType.toString() + ") needed {} ms",
        endTime - startTime);
    }
    catch (XMLStreamException ex)
    {
      log.error("exception when serializing SDocumentGraph", ex);
    }
  }
  
  @Override
  public boolean isReadable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return (MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)
      || MediaType.TEXT_XML_TYPE.isCompatible(mediaType)
      || APPLICATION_XMI_XML.isCompatible(mediaType))
      && SaltProject.class.isAssignableFrom(type);
  }

  @Override
  public SaltProject readFrom(Class<SaltProject> type, Type genericType,
    Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws
    IOException,
    WebApplicationException
  {
    SaltProject result = SaltFactory.createSaltProject();
    
    SAXParser parser;
		XMLReader xmlReader;
    SAXParserFactory factory = SAXParserFactory.newInstance();
    MixedContentHandler handler = new MixedContentHandler();
    
    try
    {
      parser = factory.newSAXParser();
      xmlReader = parser.getXMLReader();
      xmlReader.setContentHandler(handler);
      InputSource source = new InputSource(entityStream);
      source.setEncoding("UTF-8");
      xmlReader.parse(source);
      
      for(SDocumentGraph g : handler.getDocGraphs())
      {
        
        // create a separate corpus graph for each document
        SCorpusGraph corpusGraph = SaltFactory.createSCorpusGraph();
        
        SCorpus parentCorpus = null;
        SDocument doc = null;
        
        List<SNode> nodes = g.getNodes();
        Iterator<String> it;
        if(nodes != null && !nodes.isEmpty())
        {
          // the path of each node ID is always the document/corpus path
          it = nodes.get(0).getPath().segmentsList().iterator();
        }
        else
        {
          // Old salt versions had a separate ID for the document graph
          // which was the document name with the suffix "_graph".
          // Thus this method of getting the corpus path is only the fallback.
          it = g.getPath().segmentsList().iterator();
        }
        
        
        while(it.hasNext())
        {
          String name = it.next();
          if(it.hasNext())
          {
            // this is a sub-corpus
            parentCorpus = corpusGraph.createCorpus(parentCorpus, name);
          }
          else
          {
            // no more path elements left, must be a document
            doc = corpusGraph.createDocument(parentCorpus, name);
            break;
          }
        }
        if(doc != null)
        {
          result.addCorpusGraph(corpusGraph);
          doc.setDocumentGraph(g);
        }
      }
      
    }
    catch(ParserConfigurationException | SAXException ex)
    {
      log.error("Error when parsing XMI", ex);
    }
    return result;
  }
  
  public static class MixedContentHandler extends SaltXML10Handler
  {
    public List<SDocumentGraph> getDocGraphs()
    {
      List<SDocumentGraph> docGraphs = new LinkedList<>();
      for(Object o : getRootObjects())
      {
        if(o instanceof SDocumentGraph) {
          docGraphs.add((SDocumentGraph) o);
        }
      }
      return docGraphs;
    }
  }

  /**
   * An {@link InputStream} that delegates all of the actions to a delegate
   * object but can't be closed.
   *
   * In {@link SaltProjectProvider#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
   * } we have to make sure the provided {@link InputStream} is not closed.
   * Unfurtunally the SAX parser will always close the stream and there seems to
   * be no option to avoid that. Thus we use this hack where the {@link #close()
   * }
   * function does nothing.
   */
  public static class NonCloseableInputStream extends InputStream
  {

    private final InputStream delegate;

    public NonCloseableInputStream(InputStream delegate)
    {
      this.delegate = delegate;
    }

    @Override
    public int read() throws IOException
    {
      return delegate.read();
    }

    @Override
    public int read(byte[] b) throws IOException
    {
      return delegate.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
      return delegate.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException
    {
      delegate.reset();
    }

    @Override
    public long skip(long n) throws IOException
    {
      return delegate.skip(n);
    }

    @Override
    public void close() throws IOException
    {
      // ignore
    }

    @Override
    public int available() throws IOException
    {
      return delegate.available();
    }

    @Override
    public boolean markSupported()
    {
      return delegate.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit)
    {
      delegate.mark(readlimit);
    }
  }

}
