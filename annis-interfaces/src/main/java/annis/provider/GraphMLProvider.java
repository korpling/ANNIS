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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.util.internal.persistence.GraphMLWriter;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Provider
public class GraphMLProvider implements MessageBodyWriter<SaltProject>
{

  public static final MediaType APPLICATION_GRAPHML = new MediaType(
    "application",
    "graphml+xml");

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

    // collect document graphs from the salt project
    List<SDocument> docs = new LinkedList<>();
    List<SCorpusGraph> corpusGraphs = t.getCorpusGraphs();
    if(corpusGraphs != null)
    {
      for(SCorpusGraph c : corpusGraphs)
      {
        if(c.getDocuments() != null)
        {
          docs.addAll(c.getDocuments());
        }
      }
    }
    GraphMLWriter.writeDocuments(entityStream, docs);
  }
  
  

}
