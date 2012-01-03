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

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonPackage;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
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
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

/**
 *
 * @author thomas
 */
@Provider
public class SaltProjectProvider implements MessageBodyWriter<SaltProject>,
  MessageBodyReader<SaltProject>
{

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return (MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)
      || MediaType.TEXT_XML_TYPE.isCompatible(mediaType))
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

    Resource resource = new XMIResourceImpl();
    // add the project itself
    resource.getContents().add(project);

    // add all SCorpusGraph, SDocument and SDocumentGraph elements
    for (SCorpusGraph corpusGraph : project.getSCorpusGraphs())
    {
      for (SDocument doc : corpusGraph.getSDocuments())
      {
        resource.getContents().add(doc);
        if (doc.getSDocumentGraph() != null)
        {
          resource.getContents().add(doc.getSDocumentGraph());
        }
      }
    }
    resource.save(entityStream, null);
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return (MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)
      || MediaType.TEXT_XML_TYPE.isCompatible(mediaType))
      && SaltProject.class.isAssignableFrom(type);
  }

  @Override
  public SaltProject readFrom(Class<SaltProject> type, Type genericType,
    Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws
    IOException,
    WebApplicationException
  {
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet.getPackageRegistry().put(SaltCommonPackage.eINSTANCE.getNsURI(),
      SaltCommonPackage.eINSTANCE);
    
    XMIResourceImpl resource = new XMIResourceImpl();
    resourceSet.getResources().add(resource);
    
    resource.load(entityStream, null);

    return (SaltProject) resource.getContents().get(0);
  }
}
