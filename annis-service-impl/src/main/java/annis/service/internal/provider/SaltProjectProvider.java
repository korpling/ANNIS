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
package annis.service.internal.provider;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonPackage;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 *
 * @author thomas
 */
@Provider
public class SaltProjectProvider implements MessageBodyWriter<SaltProject>
{

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return
      (MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType) || MediaType.TEXT_XML_TYPE.isCompatible(mediaType))
      && SaltProject.class.isAssignableFrom(type) ;
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
    File tmpProject = File.createTempFile("annis-saltproject", ".salt");
    tmpProject.deleteOnExit();
    
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet.getPackageRegistry().put(SaltCommonPackage.eINSTANCE.getNsURI(),
      SaltCommonPackage.eINSTANCE);
    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
      "salt", new XMIResourceFactoryImpl());
    Resource resource = resourceSet.createResource(URI.createFileURI(tmpProject.
      getAbsolutePath()));

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
    resource.save(null);
    
    // copy temporary file to output stream
    IOUtils.copy(FileUtils.openInputStream(tmpProject), entityStream);
    
    // delete the unused file
    tmpProject.delete();
  }
}
