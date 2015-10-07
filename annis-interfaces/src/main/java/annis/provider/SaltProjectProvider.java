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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import static org.corpus_tools.salt.util.internal.persistence.SaltXML10Dictionary.ATT_XMI_VERSION;
import static org.corpus_tools.salt.util.internal.persistence.SaltXML10Dictionary.NS_SALTCORE;
import static org.corpus_tools.salt.util.internal.persistence.SaltXML10Dictionary.NS_SDOCUMENTSTRUCTURE;
import static org.corpus_tools.salt.util.internal.persistence.SaltXML10Dictionary.NS_VALUE_SALTCORE;
import static org.corpus_tools.salt.util.internal.persistence.SaltXML10Dictionary.NS_VALUE_SDOCUMENTSTRUCTURE;
import static org.corpus_tools.salt.util.internal.persistence.SaltXML10Dictionary.NS_VALUE_XMI;
import static org.corpus_tools.salt.util.internal.persistence.SaltXML10Dictionary.NS_VALUE_XSI;
import static org.corpus_tools.salt.util.internal.persistence.SaltXML10Dictionary.NS_XMI;
import static org.corpus_tools.salt.util.internal.persistence.SaltXML10Dictionary.NS_XSI;
import org.corpus_tools.salt.util.internal.persistence.SaltXML10Writer;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
@Provider
public class SaltProjectProvider implements MessageBodyWriter<SaltProject>,
  MessageBodyReader<SaltProject>
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SaltProjectProvider.class);

  private static final XMLOutputFactory factory = XMLOutputFactory.newFactory();

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
      XMLStreamWriter xml = factory.createXMLStreamWriter(entityStream, "UTF-8");

      xml.writeStartDocument("1.0");
      xml.writeCharacters("\n");
      long startTime = System.currentTimeMillis();
      
      // output XMI root element
      xml.writeStartElement(NS_XMI, "XMI", NS_VALUE_XMI);
			xml.writeNamespace(NS_SDOCUMENTSTRUCTURE, NS_VALUE_SDOCUMENTSTRUCTURE);
			xml.writeNamespace(NS_XMI, NS_VALUE_XMI);
			xml.writeNamespace(NS_XSI, NS_VALUE_XSI);
			xml.writeNamespace(NS_SALTCORE, NS_VALUE_SALTCORE);
			xml.writeAttribute(NS_VALUE_XMI, ATT_XMI_VERSION, "2.0");
      
      writer.writeSaltProject(xml, project);
      
      for(SCorpusGraph corpusGraph : project.getCorpusGraphs())
      {
        for(SDocument doc : corpusGraph.getDocuments())
        {
          writer.writeDocumentGraph(xml, doc.getDocumentGraph());
        }
      }
      xml.writeEndDocument();
      long endTime = System.currentTimeMillis();
      log.debug("Saving XMI (" + mediaType.toString() + ") needed {} ms",
        endTime - startTime);
      xml.writeEndDocument();
    }
    catch (XMLStreamException ex)
    {
      log.error("exception when serializing SDocumentGraph", ex);
    }
  }
  
  private void writeHead( XMLStreamWriter xml)
  {
    
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return (MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)
      || MediaType.TEXT_XML_TYPE.isCompatible(mediaType)
      || APPLICATION_XMI_XML.isCompatible(mediaType))
      && SDocumentGraph.class.isAssignableFrom(type);
  }

  @Override
  public SaltProject readFrom(Class<SaltProject> type, Type genericType,
    Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws
    IOException,
    WebApplicationException
  {
    SaltProject result = SaltFactory.createSaltProject();

    return result;
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
