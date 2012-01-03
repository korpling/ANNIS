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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    String[] splittedIDs = rawCorpusNames.split(",");
    List<Long> corpusIDs = new LinkedList<Long>();
    for (int i = 0; i < splittedIDs.length; i++)
    {
      try
      {
        corpusIDs.add(Long.parseLong(splittedIDs[i]));
      }
      catch (NumberFormatException ex)
      {
        return Response.status(Response.Status.BAD_REQUEST).type(
          MediaType.TEXT_PLAIN).entity("invalid number: "
          + splittedIDs[i]).build();
      }
    }

    QueryData data = annisDao.parseAQL(query, corpusIDs);
    //   List<String> corpusNames = Arrays.asList(rawCorpusNames.split(","));
    //    QueryData data = annisDao.parseAQL(query, annisDao.listCorpusByName(
    //      corpusNames));
    int count = annisDao.count(data);
    return Response.ok("" + count).type(MediaType.TEXT_PLAIN).build();

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
