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

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonPackage;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLParserPool;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
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
  private static final XMLParserPool xmlParserPool = 
    new XMLParserPoolImpl();
  
  public static final MediaType APPLICATION_XMI_BINARY = new MediaType("application",
    "xmi+binary");
  public static final MediaType APPLICATION_XMI_XML = new MediaType("application",
    "xmi+xml");

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return (MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)
      || MediaType.TEXT_XML_TYPE.isCompatible(mediaType)
      || APPLICATION_XMI_BINARY.isCompatible(mediaType)
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

    
    Resource resource;
    Map<Object,Object> options = new HashMap<Object, Object>();
    if(APPLICATION_XMI_BINARY.isCompatible(mediaType))
    {
      resource = new BinaryResourceImpl();
    }
    else
    {
      resource = new XMIResourceImpl();
      options.putAll(((XMIResourceImpl) resource).getDefaultSaveOptions());
      
      options.put(XMIResource.OPTION_ENCODING, "UTF-8");
      options.put(XMIResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
      options.put(XMIResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList());
      options.put(XMIResource.OPTION_FORMATTED, Boolean.FALSE);
      options.put(XMIResource.OPTION_PROCESS_DANGLING_HREF, "DISCARD");
    }
    
    // add the project itself
    resource.getContents().add(project);

    // add all SDocumentGraph elements
    for (SCorpusGraph corpusGraph : project.getSCorpusGraphs())
    {
      for (SDocument doc : corpusGraph.getSDocuments())
      {
        if (doc.getSDocumentGraph() != null)
        {
          resource.getContents().add(doc.getSDocumentGraph());
        }
      }
    }
    
    long startTime = System.currentTimeMillis();
    try
    {
      resource.save(new BufferedOutputStream(entityStream, 512*1024), options); 
    }
    catch(Exception ex)
    {
      log.error("exception when serializing SaltProject", ex);
    }
    long endTime = System.currentTimeMillis();
    log.debug("Saving XMI (" + mediaType.toString() +  ") needed {} ms", endTime-startTime);
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return (MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType)
      || MediaType.TEXT_XML_TYPE.isCompatible(mediaType)
      || APPLICATION_XMI_BINARY.isCompatible(mediaType)
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
   
    Resource resource;
    long startTime = System.currentTimeMillis();
    if(APPLICATION_XMI_BINARY.isCompatible(mediaType))
    {
      resource = loadBinary(entityStream);
    }
    else
    {
      resource = loadXMI(entityStream);
    }
    long endTime = System.currentTimeMillis();
    log.debug("Loading XMI (" + mediaType.toString() +  ") needed {} ms", endTime-startTime);
    
    SaltProject project = SaltCommonFactory.eINSTANCE.createSaltProject();
    
    TreeIterator<EObject> itContents = resource.getAllContents();
    while(itContents.hasNext())
    {
      EObject o = itContents.next();
      if(o instanceof SaltProject)
      {
        project = (SaltProject) o;
        break;
      }
    }
    
    return project;
  }
  
  private BinaryResourceImpl loadBinary(InputStream entityStream) throws IOException
  {
    BinaryResourceImpl resource = new BinaryResourceImpl();
    
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet.getPackageRegistry().put(SaltCommonPackage.eINSTANCE.getNsURI(),
      SaltCommonPackage.eINSTANCE);

    resourceSet.getResources().add(resource);


    try
    {
      resource.load(entityStream, null);
    }
    catch(IOException ex)
    {
      log.error("Salt binary deserialization failed", ex);
    }
    return resource;
  }
  
  private XMIResourceImpl loadXMI(InputStream entityStream) throws IOException
  {
    XMIResourceImpl resource = new XMIResourceImpl();
    
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet.getPackageRegistry().put(SaltCommonPackage.eINSTANCE.getNsURI(),
      SaltCommonPackage.eINSTANCE);

    resourceSet.getResources().add(resource);
    
    Map<Object,Object> options = resource.getDefaultLoadOptions();
    options.put(XMIResource.OPTION_USE_PARSER_POOL, xmlParserPool);
    options.put(XMIResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
    options.put(XMIResource.OPTION_DISABLE_NOTIFY, Boolean.FALSE);
    options.put(XMIResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);
    
    resource.load(entityStream, options);
    
    return resource;
  }

}
