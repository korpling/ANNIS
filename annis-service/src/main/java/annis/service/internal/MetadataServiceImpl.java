/*
 * Copyright 2013 SFB 632.
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
package annis.service.internal;

import annis.dao.QueryDao;
import annis.model.Annotation;
import annis.service.MetadataService;
import annis.service.objects.AnnisBinaryMetaData;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@Component
@Path("annis/meta")
public class MetadataServiceImpl implements MetadataService
{

  private Logger log = LoggerFactory.getLogger(MetadataServiceImpl.class);

  private QueryDao queryDao;

  @GET
  @Path("corpus/{toplevel}/closure")
  @Produces(
    {
    MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
  })
  public List<Annotation> getMetadata(
    @PathParam("toplevel") String topLevelCorpus)
  {
    return getMetadata(topLevelCorpus, true);
  }

  @GET
  @Path("corpus/{toplevel}")
  @Produces(
    {
    MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
  })
  public List<Annotation> getMetadataTopLevel(
    @PathParam("toplevel") String topLevelCorpus)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("meta:" + topLevelCorpus);

    return getQueryDao().listCorpusAnnotations(topLevelCorpus);
  }

  @Override
  public List<Annotation> getMetadata(
    @PathParam("toplevel") String topLevelCorpus,
    @DefaultValue(value = "false") boolean closure)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("meta:" + topLevelCorpus);

    return getQueryDao().listDocumentsAnnotations(topLevelCorpus, closure);
  }

  @GET
  @Path("docnames/{toplevel}")
  @Produces(
    {
    MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
  })
  @Override
  public List<Annotation> getDocNames(
    @PathParam("toplevel") String topLevelCorpus)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("meta:" + topLevelCorpus);

    return getQueryDao().listDocuments(topLevelCorpus);
  }

  @GET
  @Path("binary/{top}/{doc}")
  @Produces(
    {
    MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
  })
  @Override
  public List<AnnisBinaryMetaData> binaryMeta(
    @PathParam("top") String toplevelCorpusName,
    @PathParam("doc") String doc)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("meta:" + toplevelCorpusName);

    return getQueryDao().getBinaryMeta(toplevelCorpusName, doc);
  }

  @GET
  @Path("doc/{toplevel}")
  @Produces(
    {
    MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
  })
  public List<Annotation> getMetaDataDoc(@PathParam("toplevel") String topLevel)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("meta:" + topLevel);
    
    return getQueryDao().listDocumentsAnnotations(topLevel, false);
  }

  @GET
  @Path("doc/{toplevel}/{doc}")
  @Produces(
    {
    MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
  })
  public List<Annotation> getMetaDataDoc(
    @PathParam("toplevel") String topLevelCorpus,
    @PathParam("doc") String doc)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("meta:" + topLevelCorpus);

    return getQueryDao().listCorpusAnnotations(topLevelCorpus, doc, true);
  }

  @GET
  @Path("doc/{toplevel}/{doc}/path")
  @Produces(
    {
    MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON
  })
  public List<Annotation> getMetadataDoc(
    @PathParam("toplevel") String toplevelCorpus, @PathParam("doc") String doc)
  {
    return getMetadataDoc(toplevelCorpus, doc, false);
  }

  @Override
  public List<Annotation> getMetadataDoc(String topLevelCorpus, String docname,
    boolean path)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("meta:" + topLevelCorpus);

    return getQueryDao().listCorpusAnnotations(topLevelCorpus, docname, path);
  }

  /**
   * Log the successful initialization of this bean.
   *
   * <p> XXX: This should be a private method annotated with
   * <tt>@PostConstruct</tt>, but that doesn't seem to work. As a work-around,
   * the method is called by Spring as an init-method.
   */
  public void init()
  {
    // check version of PostgreSQL
    queryDao.checkDatabaseVersion();

    // log a message after successful startup
    log.info("ANNIS MetadataService loaded.");
  }

  /**
   * @return the queryDao
   */
  public QueryDao getQueryDao()
  {
    return queryDao;
  }

  /**
   * @param queryDao the queryDao to set
   */
  public void setQueryDao(QueryDao queryDao)
  {
    this.queryDao = queryDao;
  }
}
