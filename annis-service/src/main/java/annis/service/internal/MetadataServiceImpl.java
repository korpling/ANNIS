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

import annis.dao.AnnisDao;
import annis.model.Annotation;
import annis.service.MetadataService;
import annis.service.objects.AnnisBinaryMetaData;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@Path("annis/query")
public class MetadataServiceImpl implements MetadataService
{

  private AnnisDao annisDao;

  @GET
  @Path("corpus/{toplevel}/closure")
  public List<Annotation> getMetadata(
    @PathParam("toplevel") String topLevelCorpus)
  {
    return getMetadata(topLevelCorpus, true);
  }

  @GET
  @Path("corpus/{toplevel}")
  @Override
  public List<Annotation> getMetadata(
    @PathParam("toplevel") String topLevelCorpus,
    @DefaultValue(value = "false") boolean closure)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:meta:" + topLevelCorpus);

    return annisDao.listDocumentsAnnotations(topLevelCorpus, closure);
  }

  @GET
  @Path("docnames/{toplevel}")
  @Override
  public List<Annotation> getDocNames(
    @PathParam("toplevel") String topLevelCorpus)
  {
    return annisDao.listDocuments(topLevelCorpus);
  }

  @GET
  @Path("binary/{top}/{doc}")
  @Override
  public List<AnnisBinaryMetaData> binaryMeta(
    @PathParam("top") String toplevelCorpusName,
    @PathParam("doc") String doc)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:binary:" + toplevelCorpusName);

    return annisDao.getBinaryMeta(toplevelCorpusName, doc);
  }

  @GET
  @Path("doc/{toplevel}")
  public List<Annotation> getMetaDataDoc(
    @PathParam("toplevel") String topLevelCorpus)
  {
    return getMetadataDoc(topLevelCorpus, null, false);
  }

  @GET
  @Path("doc/{toplevel}/{doc}")
  public List<Annotation> getMetaDataDoc(
    @PathParam("toplevel") String topLevelCorpus, @PathParam("doc") String doc)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:meta:" + topLevelCorpus);

    return annisDao.listCorpusAnnotations(topLevelCorpus, doc, false);
  }

  @GET
  @Path("doc/{toplevel}/{doc}/path")
  @Override
  public List<Annotation> getMetadataDoc(String topLevelCorpus, String docname,
    boolean path)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:meta:" + topLevelCorpus);

    if (docname == null)
    {
      return annisDao.listDocumentsAnnotations(topLevelCorpus, path);
    }
    else
    {
      return annisDao.listCorpusAnnotations(topLevelCorpus, docname, false);
    }
  }
}
