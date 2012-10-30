
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

import annis.CommonHelper;
import java.net.URISyntaxException;
import static java.util.Arrays.asList;
import annis.WekaHelper;
import annis.dao.AnnisDao;
import annis.dao.AnnotatedMatch;
import annis.service.objects.Match;
import annis.model.Annotation;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisBinary;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.CorpusConfig;
import annis.sqlgen.AnnotateQueryData;
import annis.sqlgen.LimitOffsetQueryData;
import annis.sqlgen.SaltURIs;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.stereotype.Component;

/**
 * Methods for querying the database.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 * @author Benjamin Weißenfels
 */
@Component
@Path("annis/query")
public class QueryService
{

  private final static Logger log = LoggerFactory.getLogger(QueryService.class);
  private final static Logger queryLog = LoggerFactory.getLogger("QueryLog");
  private AnnisDao annisDao;
  private WekaHelper wekaHelper;
  private int maxContext = 10;
  private int port = 5711;
  
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
    annisDao.checkDatabaseVersion();
    
    // log a message after successful startup
    log.info("ANNIS QueryService loaded.");
  }

  @GET
  @Path("search/count")
  @Produces("plain/text")
  public Response count(@QueryParam("q") String query,
    @QueryParam("corpora") String rawCorpusNames)
  {
    
    requiredParameter(query, "q", "AnnisQL query");
    requiredParameter(rawCorpusNames, "corpora", "comma separated list of corpus names");

    Subject user = SecurityUtils.getSubject();
    List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
    for(String c : corpusNames)
    {
      user.checkPermission("query:count:" + c);
    }
    
    
    QueryData data = queryDataFromParameters(query, rawCorpusNames);
    long start = new Date().getTime();
    int count = annisDao.count(data);
    long end = new Date().getTime();
    logQuery("COUNT", query, splitCorpusNamesFromRaw(rawCorpusNames), end - start);
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
    @DefaultValue("5") @QueryParam("right") String rightRaw,
    @QueryParam("seglayer") String segmentationLayer) throws IOException
  {
    requiredParameter(query, "q", "AnnisQL query");
    requiredParameter(rawCorpusNames, "corpora", "comma separated list of corpus names");

    Subject user = SecurityUtils.getSubject();
    List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
    for(String c : corpusNames)
    {
      user.checkPermission("query:annotate:" + c);
    }
    
    int offset = Integer.parseInt(offsetRaw);
    int limit = Integer.parseInt(limitRaw);
    int left = Math.min(maxContext, Integer.parseInt(leftRaw));
    int right = Math.min(maxContext, Integer.parseInt(rightRaw));

    QueryData data = queryDataFromParameters(query, rawCorpusNames);
    String logParameters = createAnnotateLogParameters(left, right, offset,
      limit);

    data.addExtension(new LimitOffsetQueryData(offset, limit));
    data.addExtension(new AnnotateQueryData(left, right, segmentationLayer));
    long start = new Date().getTime();
    SaltProject p = annisDao.annotate(data);
    long end = new Date().getTime();
    logQuery("ANNOTATE", query, splitCorpusNamesFromRaw(rawCorpusNames), end - start, logParameters);
    return p;

  }

  @GET
  @Path("search/find")
  @Produces("application/xml")
  public List<Match> find(@QueryParam("q") String query,
    @QueryParam("corpora") String rawCorpusNames,
    @DefaultValue("0") @QueryParam("offset") String offsetRaw,
    @DefaultValue("10") @QueryParam("limit") String limitRaw) throws IOException
  {
    requiredParameter(query, "q", "AnnisQL query");
    requiredParameter(rawCorpusNames, "corpora", "comma separated list of corpus names");

    Subject user = SecurityUtils.getSubject();
    List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
    for(String c : corpusNames)
    {
      user.checkPermission("query:find:" + c);
    }
    
    
    int offset = Integer.parseInt(offsetRaw);
    int limit = Integer.parseInt(limitRaw);

    QueryData data = queryDataFromParameters(query, rawCorpusNames);
    data.setCorpusConfiguration(annisDao.getCorpusConfiguration());
    data.addExtension(new LimitOffsetQueryData(offset, limit));
    return annisDao.find(data);
  }
  
  
  
  /**
   * Get result as matrix in WEKA (ARFF) format.
   */
  @GET
  @Path("search/matrix")
  @Produces("text/plain")
  public String matrix(
    @QueryParam("q") String query,
    @QueryParam("corpora") String rawCorpusNames)
  {
    requiredParameter(query, "q", "AnnisQL query");
    requiredParameter(rawCorpusNames, "corpora", "comma separated list of corpus names");
    
    Subject user = SecurityUtils.getSubject();
    List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
    for(String c : corpusNames)
    {
      user.checkPermission("query:matrix:" + c);
    }
    
    
    QueryData data = queryDataFromParameters(query, rawCorpusNames);
    
    long start = new Date().getTime();
    List<AnnotatedMatch> matches = annisDao.matrix(data);
    long end = new Date().getTime();
    logQuery("MATRIX", query, splitCorpusNamesFromRaw(rawCorpusNames), end - start);
    
    if(matches.isEmpty())
    {
      return "(empty)";
    }
    else
    {
      return WekaHelper.exportAsArff(matches);
    }
  }
    

  /**
   * Get a graph as {@link SaltProject} of a set of Salt IDs.
   *
   * @param saltIDs saltIDs must have at least one saltId, more than one id are
   * separated by + or space
   * @param leftRaw left context parameter
   * @param rightRaw right context parameter
   * @return the graph of this hit.
   */
  @GET
  @Path("search/subgraph")
  @Produces("application/xml")
  public SaltProject subgraph(@QueryParam("q") String saltIDs,
    @DefaultValue("5") @QueryParam("left") String leftRaw,
    @DefaultValue("5") @QueryParam("right") String rightRaw,
    @QueryParam("seglayer") String segmentationLayer)
  {
    String[] ids;
    List<URI> saltURI = new SaltURIs();
    QueryData data = new QueryData();
    int left = Integer.parseInt(leftRaw);
    int right = Integer.parseInt(rightRaw);
  
    data.addExtension(new AnnotateQueryData(left, right, segmentationLayer));

    // some robustness stuff
    if (saltIDs == null)
    {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter 'q'").build());
    }

    // check if this is a valid URI
    ids = saltIDs.split("\\s*,\\s*");
    for (String id : ids)
    {
      try
      {
        URI saltID = new URI(id);
        saltURI.add(saltID);

        if (saltID.getScheme() == null
          || !saltID.getScheme().equals("salt"))
        {
          throw new WebApplicationException(
            Response.status(Response.Status.BAD_REQUEST).type(
            MediaType.TEXT_PLAIN).entity(
            "the scheme is not the salt identifier scheme").build());
        }
      }
      catch (URISyntaxException ex)
      {
        String msg = id + " is not a valid salt scheme";
        log.error(msg, ex);
      }
    }

    
    // collect list of used corpora and created pseudo QueryNodes for each URI
    Set<String> corpusNames = new TreeSet<String>();
    List<QueryNode> pseudoNodes = new ArrayList<QueryNode>(saltURI.size());
    for(java.net.URI u : saltURI)
    {
      pseudoNodes.add(new QueryNode());
      
      corpusNames.add(CommonHelper.getCorpusPath(u).get(0));
    }
    
    Subject user = SecurityUtils.getSubject();
    for(String c : corpusNames)
    {
      user.checkPermission("query:subgraph:" + c);
    }
    
    
    List<Long> corpusIDs = annisDao.mapCorpusNamesToIds(new LinkedList<String>(corpusNames));
    
    data.setCorpusList(corpusIDs);
    data.addAlternative(pseudoNodes);
    
    data.addExtension(saltURI);
    return annisDao.graph(data);
  }

  @GET
  @Path("graphs/{top}/{doc}")
  @Produces("application/xml")
  public SaltProject graph(@PathParam("top") String toplevelCorpusName,
    @PathParam("doc") String documentName)
  {
    
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:subgraph:" + toplevelCorpusName);
    
    try
    {
      long start = new Date().getTime();
      SaltProject p = annisDao.retrieveAnnotationGraph(toplevelCorpusName,
        documentName);
      long end = new Date().getTime();
      logQuery("GRAPH", toplevelCorpusName, documentName, end - start);
      return p;
    }
    catch (Exception ex)
    {
      log.error("error when accessing graph " + toplevelCorpusName + "/"
        + documentName, ex);
      throw new WebApplicationException(ex);
    }
  }

  @GET
  @Path("resolver/{corpusName}/{namespace}/{type}")
  @Produces("application/xml")
  public List<ResolverEntry> resolver(@PathParam("corpusName") String corpusName,
    @PathParam("namespace") String namespace,
    @PathParam("type") String type)
  {
    ResolverEntry.ElementType enumType = ResolverEntry.ElementType.valueOf(type);
    SingleResolverRequest r = new SingleResolverRequest(corpusName, namespace,
      enumType);
    return annisDao.getResolverEntries(r);
  }

  @GET
  @Path("corpora")
  @Produces("application/xml")
  public List<AnnisCorpus> corpora()
  {
    List<AnnisCorpus> allCorpora = annisDao.listCorpora();
    List<AnnisCorpus> allowedCorpora = new LinkedList<AnnisCorpus>();
    // filter by which corpora the user is allowed to access
    Subject user = SecurityUtils.getSubject();
    for(AnnisCorpus c : allCorpora)
    {
      if(user.isPermitted("query:*:" + c.getName()))
      {
        allowedCorpora.add(c);
      }
    }
    return allowedCorpora;
  }

  @GET
  @Path("corpora/{top}/config")
  @Produces("application/xml")
  public CorpusConfig corpusconfig(@PathParam("top") String toplevelName)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:config:" + toplevelName);

    
    Map<String, String> tmp = annisDao.getCorpusConfiguration(toplevelName);
    CorpusConfig result = new CorpusConfig();
    result.setConfig(tmp);
    return result;
  }

  @GET
  @Path("corpora/{top}/annotations")
  @Produces("application/xml")
  public List<AnnisAttribute> annotations(
    @PathParam("top") String toplevelCorpus,
    @DefaultValue("false") @QueryParam("fetchvalues") String fetchValues,
    @DefaultValue("false") @QueryParam("onlymostfrequentvalues") String onlyMostFrequentValues
  )
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:annotations:" + toplevelCorpus);
    
    List<String> list = new LinkedList<String>();
    list.add(toplevelCorpus);
    List<Long> corpusList = annisDao.mapCorpusNamesToIds(list);

    return annisDao.listAnnotations(corpusList,
      Boolean.parseBoolean(fetchValues), Boolean.parseBoolean(onlyMostFrequentValues)
    );
  }
  
  /**
   * Return true if this is a valid query or throw exception when invalid
   * @param query Query to check for validity
   * @return 
   */
  @GET
  @Path("check")
  public String check(@QueryParam("q") String query)
  {
    annisDao.parseAQL(query, new LinkedList<Long>());
    return "ok";
  }
  
  @GET
  @Path("corpora/{top}/metadata")
  @Produces("application/xml")
  public List<Annotation> getMetadata(
    @PathParam("top") String toplevelCorpusName)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:meta:" + toplevelCorpusName);
    
    return annisDao.listCorpusAnnotations(toplevelCorpusName);
  }

  @GET
  @Path("corpora/{top}/{document}/metadata")
  @Produces("application/xml")
  public List<Annotation> getMetadata(
    @PathParam("top") String toplevelCorpusName,
    @PathParam("document") String documentName)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:meta:" + toplevelCorpusName);
    
    if(documentName == null)
    {
      documentName = toplevelCorpusName;
    }
    return annisDao.listCorpusAnnotations(toplevelCorpusName, documentName);
  }
  
  /**
   * Get an Annis Binary object identified by its id.
   * 
   * @param id
   * @param rawOffset the part we want to start from, we start from 0
   * @param rawLength how many bytes we take
   * @return AnnisBinary
   */
  @GET
  @Path("corpora/{top}/{document}/binary/{offset}/{length}")
  @Produces("application/xml")
  public AnnisBinary binary(
    @PathParam("top") String toplevelCorpusName,
    @PathParam("document") String corpusName,
    @PathParam("offset") String rawOffset, 
    @PathParam("length") String rawLength)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:binary:" + toplevelCorpusName);
    
    int offset = Integer.parseInt(rawOffset);
    int length = Integer.parseInt(rawLength);
    
    AnnisBinary bin = null;
    log.debug("fetching  " + (length / 1024) + "kb (" + offset + "-" + (offset + length) + ") from binary "
      + toplevelCorpusName + "/" + corpusName);

    bin = annisDao.getBinary(toplevelCorpusName, corpusName, offset + 1, length);

    log.debug("fetch successfully");
    return bin;
  }
  
  /**
   * Get the Metadata of an Annis Binary object identified by its id. This 
   * function calls getBinary(long id, 1, 1), so this function does not work, 
   * if the specs of getBinary(long id, int offset,int length) changed.
   * 
   * @param id
   * @return AnnisBinaryMetaData
   */
  @GET
  @Path("corpora/{top}/{document}/binary/meta")
  @Produces("application/xml")
  public AnnisBinary binaryMeta(
    @PathParam("top") String toplevelCorpusName,
    @PathParam("document") String documentName)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:binary:" + toplevelCorpusName);
    
    AnnisBinary bin = null;
    bin = annisDao.getBinary(toplevelCorpusName, documentName, 1, 1);

    return bin;
  }

  private String createAnnotateLogParameters(int left, int right, int offset,
    int limit)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("left: ");
    sb.append(left);
    sb.append(", ");
    sb.append("right: ");
    sb.append(right);
    sb.append(", ");
    sb.append("offset: ");
    sb.append(offset);
    sb.append(", ");
    sb.append("limit: ");
    sb.append(limit);
    String logParameters = sb.toString();
    return logParameters;
  }
  

  private void logQuery(String queryFunction, String toplevelCorpus,
    String documentName, long runtime)
  {
    logQuery(queryFunction, null, asList(toplevelCorpus), runtime, "document: "
      + documentName);
  }

  private void logQuery(String queryFunction, String annisQuery,
    List<String> corpusNames, long runtime)
  {
    logQuery(queryFunction, annisQuery, corpusNames, runtime, null);
  }

  private void logQuery(String queryFunction, String annisQuery,
    List<String> corpusNames, long runtime, String options)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("function: ");
    sb.append(queryFunction);
    sb.append(", ");
    if (annisQuery != null && !annisQuery.isEmpty())
    {
      sb.append("query: ");
      sb.append(annisQuery);
      sb.append(", ");
    }
    sb.append("corpus: ");
    sb.append(corpusNames);
    sb.append(", ");
    sb.append("runtime: ");
    sb.append(runtime);
    sb.append(" ms");
    if (options != null && !options.isEmpty())
    {
      sb.append(", ");
      sb.append(options);
    }
    String message = sb.toString();
    queryLog.info(message);
  }
  
  /**
   * Throw an exception if the parameter is missing.
   * @param value Value which is checked for null.
   * @param name The short name of parameter.
   * @param description A one line description of the meaing of the parameter.
   */
  private void requiredParameter(String value, String name, String description)
    throws WebApplicationException
  {
    if (value == null)
    {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required parameter '" + name + "' (" + description + ")").build());
    }
  }
  
  /**
   * Get the {@link QueryData} from a query and the corpus names
   * @param query The AQL query.
   * @param rawCorpusNames The name of the toplevel corpus names seperated by ",".
   * @return calculated {@link QueryData} for the given parametes.
   * 
   * @throws WebApplicationException Thrown if some corpora are unknown to the system.
   */
  private QueryData queryDataFromParameters(String query, String rawCorpusNames)
    throws WebApplicationException
  {
    List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
    List<Long> corpusIDs = annisDao.mapCorpusNamesToIds(
      corpusNames);
    if (corpusIDs.size() != corpusNames.size())
    {
      throw new WebApplicationException(
        Response.status(Response.Status.NOT_FOUND).type(
        "text/plain").entity("one ore more corpora are unknown to the system").
        build());
    }
    return annisDao.parseAQL(query, corpusIDs);
  }
  
  /**
   * Splits a list of corpus names into a proper java list.
   * @param rawCorpusNames The corpus names separated by ",".
   * @return 
   */
  private List<String> splitCorpusNamesFromRaw(String rawCorpusNames)
  {
    return Arrays.asList(rawCorpusNames.split(","));
  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
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
