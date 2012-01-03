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
package annis.service.internal;

import annis.WekaHelper;
import annis.dao.AnnisDao;
import annis.externalFiles.ExternalFileMgr;
import annis.ql.parser.QueryData;
import annis.sqlgen.AnnotateSqlGenerator.AnnotateQueryData;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonPackage;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.springframework.stereotype.Component;

/**
 *
 * @author thomas
 */
@Component
@Path("/annis")
public class AnnisWebService
{

  private final static Logger log = Logger.getLogger(AnnisWebService.class.
    getName());
  private AnnisDao annisDao;
  private ExternalFileMgr externalFileMgr;
  private WekaHelper wekaHelper;
  private int maxContext = 10;
  private int port = 5711;

  /**
   * Log the successful initialization of this bean.
   *
   * <p>
   * XXX: This should be a private method annotated with <tt>@PostConstruct</tt>, but
   * that doesn't seem to work.  As a work-around, the method is called
   * by Spring as an init-method.
   */
  public void sayHello()
  {
    // log a message after successful startup
    log.info("AnnisWebService loaded.");
  }

  @GET
  @Path("search/count")
  @Produces("plain/text")
  public Response count(@QueryParam("q") String query,
    @QueryParam("corpora") String rawCorpusNames)
  {

    if (query == null)
    {
      return Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter 'q'").build();
    }
    if (rawCorpusNames == null)
    {
      return Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter 'corpora'").build();
    }

    List<String> corpusNames = Arrays.asList(rawCorpusNames.split(","));
    List<Long> corpusIDs = annisDao.listCorpusByName(
      corpusNames);
    if (corpusIDs.size() != corpusNames.size())
    {
      return Response.status(Response.Status.NOT_FOUND).type(
        "text/plain").entity("one ore more corpora are unknown to the system").
        build();
    }
    QueryData data = annisDao.parseAQL(query, corpusIDs);
    int count = annisDao.count(data);
    return Response.ok("" + count).type(MediaType.TEXT_PLAIN).build();

  }

  @GET
  @Path("search/annotate")
  @Produces("application/xml")
  public SaltProject annotate(@QueryParam("q") String query,
    @QueryParam("corpora") String rawCorpusNames,
    @DefaultValue("0") @QueryParam("offset") String offsetRaw,
    @DefaultValue("10") @QueryParam("limit") String limitRaw,
    @DefaultValue("5") @QueryParam("left") String leftRaw,
    @DefaultValue("5") @QueryParam("right") String rightRaw) throws IOException
  {
    if (query == null)
    {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter 'q'").build());
    }
    if (rawCorpusNames == null)
    {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter 'corpora'").build());
    }

    int offset = Integer.parseInt(offsetRaw);
    int limit = Integer.parseInt(limitRaw);
    int left = Integer.parseInt(leftRaw);
    int right = Integer.parseInt(rightRaw);

    List<String> corpusNames = Arrays.asList(rawCorpusNames.split(","));
    List<Long> corpusIDs = annisDao.listCorpusByName(
      corpusNames);
    if (corpusIDs.size() != corpusNames.size())
    {
      throw new WebApplicationException(
        Response.status(Response.Status.NOT_FOUND).type(
        "text/plain").entity("one ore more corpora are unknown to the system").
        build());
    }
    QueryData data = annisDao.parseAQL(query, corpusIDs);
    data.addExtension(new AnnotateQueryData(offset, limit, left,
      right));
    SaltProject p = annisDao.annotate(data);
    return p;

  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  public ExternalFileMgr getExternalFileMgr()
  {
    return externalFileMgr;
  }

  public void setExternalFileMgr(ExternalFileMgr externalFileMgr)
  {
    this.externalFileMgr = externalFileMgr;
  }

  public int getMaxContext()
  {
    return maxContext;
  }

  public void setMaxContext(int maxContext)
  {
    this.maxContext = maxContext;
  }

  public WekaHelper getWekaHelper()
  {
    return wekaHelper;
  }

  public void setWekaHelper(WekaHelper wekaHelper)
  {
    this.wekaHelper = wekaHelper;
  }

  public int getPort()
  {
    return port;
  }

  public void setPort(int port)
  {
    this.port = port;
  }
}
