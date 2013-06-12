
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
import static java.util.Arrays.asList;
import annis.WekaHelper;
import annis.dao.AnnisDao;
import annis.examplequeries.ExampleQuery;
import annis.service.objects.Match;
import annis.model.Annotation;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.QueryService;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisBinaryMetaData;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.CorpusConfig;
import annis.service.objects.CorpusConfigMap;
import annis.service.objects.MatchAndDocumentCount;
import annis.service.objects.SaltURIGroup;
import annis.sqlgen.AnnotateQueryData;
import annis.sqlgen.LimitOffsetQueryData;
import annis.service.objects.SubgraphQuery;
import annis.sqlgen.MatrixQueryData;
import com.google.mimeparse.MIMEParse;
import com.sun.jersey.api.core.ResourceConfig;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.stereotype.Component;

/**
 * Methods for querying the database.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@Component
@Path("annis/query")
public class QueryServiceImpl implements QueryService
{

  private final static Logger log = LoggerFactory.getLogger(
    QueryServiceImpl.class);

  private final static Logger queryLog = LoggerFactory.getLogger("QueryLog");

  private AnnisDao annisDao;

  private WekaHelper wekaHelper;

  private int port = 5711;

  private CorpusConfig defaultCorpusConfig;

  @Context
  private UriInfo uriInfo;

  @Context
  private HttpServletRequest request;

  @Context
  private ResourceConfig rc;

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
  @Produces("application/xml")
  public Response count(@QueryParam("q") String query,
    @QueryParam("corpora") String rawCorpusNames)
  {

    requiredParameter(query, "q", "AnnisQL query");
    requiredParameter(rawCorpusNames, "corpora",
      "comma separated list of corpus names");

    Subject user = SecurityUtils.getSubject();
    List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
    for (String c : corpusNames)
    {
      user.checkPermission("query:count:" + c);
    }

    QueryData data = queryDataFromParameters(query, rawCorpusNames);
    long start = new Date().getTime();
    MatchAndDocumentCount count = annisDao.countMatchesAndDocuments(data);
    long end = new Date().getTime();

    logQuery("COUNT", query, splitCorpusNamesFromRaw(rawCorpusNames),
      end - start);

    return Response.ok(count).type(MediaType.APPLICATION_XML_TYPE).build();
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
    requiredParameter(rawCorpusNames, "corpora",
      "comma separated list of corpus names");

    Subject user = SecurityUtils.getSubject();
    List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
    for (String c : corpusNames)
    {
      user.checkPermission("query:annotate:" + c);
    }

    int offset = Integer.parseInt(offsetRaw);
    int limit = Integer.parseInt(limitRaw);
    int left = Math.min(getContextLeft(), Integer.parseInt(leftRaw));
    int right = Math.min(getContextRight(), Integer.parseInt(rightRaw));

    QueryData data = queryDataFromParameters(query, rawCorpusNames);
    String logParameters = createAnnotateLogParameters(left, right, offset,
      limit);

    data.addExtension(new LimitOffsetQueryData(offset, limit));
    data.addExtension(new AnnotateQueryData(left, right, segmentationLayer));
    long start = new Date().getTime();
    SaltProject p = annisDao.annotate(data);
    long end = new Date().getTime();
    logQuery("ANNOTATE", query, splitCorpusNamesFromRaw(rawCorpusNames),
      end - start, logParameters);
    return p;

  }

  @GET
  @Path("search/find")
  @Produces("text/plain")
  public StreamingOutput findRaw(@QueryParam("q") final String query,
    @QueryParam("corpora") final String rawCorpusNames,
    @DefaultValue("0") @QueryParam("offset") String offsetRaw,
    @DefaultValue("-1") @QueryParam("limit") String limitRaw) throws IOException
  {
    requiredParameter(query, "q", "AnnisQL query");
    requiredParameter(rawCorpusNames, "corpora",
      "comma separated list of corpus names");

    Subject user = SecurityUtils.getSubject();
    List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
    for (String c : corpusNames)
    {
      user.checkPermission("query:find:" + c);
    }

    int offset = Integer.parseInt(offsetRaw);
    int limit = Integer.parseInt(limitRaw);

    final QueryData data = queryDataFromParameters(query, rawCorpusNames);
    data.setCorpusConfiguration(annisDao.getCorpusConfiguration());
    data.addExtension(new LimitOffsetQueryData(offset, limit));

    return new StreamingOutput()
    {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
        long start = new Date().getTime();
        annisDao.find(data, output);
        long end = new Date().getTime();
        logQuery("FIND", query, splitCorpusNamesFromRaw(rawCorpusNames),
          end - start);
      }
    };

  }

  @GET
  @Path("search/find")
  @Produces("application/xml")
  public List<Match> findXml(@QueryParam("q") String query,
    @QueryParam("corpora") String rawCorpusNames,
    @DefaultValue("0") @QueryParam("offset") String offsetRaw,
    @DefaultValue("-1") @QueryParam("limit") String limitRaw) throws IOException
  {
    requiredParameter(query, "q", "AnnisQL query");
    requiredParameter(rawCorpusNames, "corpora",
      "comma separated list of corpus names");

    Subject user = SecurityUtils.getSubject();
    List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
    for (String c : corpusNames)
    {
      user.checkPermission("query:find:" + c);
    }


    int offset = Integer.parseInt(offsetRaw);
    int limit = Integer.parseInt(limitRaw);

    QueryData data = queryDataFromParameters(query, rawCorpusNames);
    data.setCorpusConfiguration(annisDao.getCorpusConfiguration());
    data.addExtension(new LimitOffsetQueryData(offset, limit));

    long start = new Date().getTime();
    List<Match> matches = annisDao.find(data);
    long end = new Date().getTime();
    logQuery("FIND", query, splitCorpusNamesFromRaw(rawCorpusNames), end - start);

    return matches;
  }

  /**
   * Get result as matrix in WEKA (ARFF) format.
   */
  @GET
  @Path("search/matrix")
  @Produces("text/plain")
  public StreamingOutput matrix(
    final @QueryParam("q") String query,
    final @QueryParam("corpora") String rawCorpusNames,
    @QueryParam("metakeys") String rawMetaKeys)
  {
    requiredParameter(query, "q", "AnnisQL query");
    requiredParameter(rawCorpusNames, "corpora",
      "comma separated list of corpus names");

    Subject user = SecurityUtils.getSubject();
    List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
    for (String c : corpusNames)
    {
      user.checkPermission("query:matrix:" + c);
    }

    final QueryData data = queryDataFromParameters(query, rawCorpusNames);

    MatrixQueryData ext = new MatrixQueryData();
    if (rawMetaKeys != null)
    {
      ext.setMetaKeys(splitMatrixKeysFromRaw(rawMetaKeys));
    }
    data.addExtension(ext);

    StreamingOutput result = new StreamingOutput()
    {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
        long start = new Date().getTime();
        annisDao.matrix(data, output);
        long end = new Date().getTime();
        logQuery("MATRIX", query, splitCorpusNamesFromRaw(rawCorpusNames),
          end - start);
      }
    };

    return result;
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
  @POST
  @Path("search/subgraph")
  @Produces(
    {
    "application/xml", "application/xmi+xml", "application/xmi+binary"
  })
  public SaltProject subgraph(final SubgraphQuery query)
  {
    // some robustness stuff
    if (query == null)
    {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST).type(
        MediaType.TEXT_PLAIN).entity(
        "missing required request body").build());
    }

    QueryData data = new QueryData();

    data.addExtension(new AnnotateQueryData(query.getLeft(), query.getRight(),
      query.getSegmentationLayer(), query.getFilter()));

    Set<String> corpusNames = new TreeSet<String>();

    for (SaltURIGroup singleMatch : query.getMatches().getGroups().values())
    {
      // collect list of used corpora and created pseudo QueryNodes for each URI
      List<QueryNode> pseudoNodes = new ArrayList<QueryNode>(singleMatch.
        getUris().size());
      for (java.net.URI u : singleMatch.getUris())
      {
        pseudoNodes.add(new QueryNode());
        corpusNames.add(CommonHelper.getCorpusPath(u).get(0));
      }

      data.addAlternative(pseudoNodes);
    }

    Subject user = SecurityUtils.getSubject();
    for (String c : corpusNames)
    {
      user.checkPermission("query:subgraph:" + c);
    }

    List<String> corpusNamesList = new LinkedList<String>(corpusNames);
    List<Long> corpusIDs = annisDao.mapCorpusNamesToIds(corpusNamesList);

    data.setCorpusList(corpusIDs);
    data.addExtension(query.getMatches());
    long start = new Date().getTime();
    SaltProject p = annisDao.graph(data);
    long end = new Date().getTime();
    logQuery("SUBGRAPH", "", corpusNamesList, end - start);

    return p;
  }

  @GET
  @Path("graphs/{top}/{doc}")
  @Produces(
    {
    "application/xml", "application/xmi+xml", "application/xmi+binary"
  })
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
    for (AnnisCorpus c : allCorpora)
    {
      if (user.isPermitted("query:*:" + c.getName()))
      {
        allowedCorpora.add(c);
      }
    }

    return allowedCorpora;
  }

  @GET
  @Path("corpora/config")
  @Produces("application/xml")
  public CorpusConfigMap corpusConfigs()
  {
    CorpusConfigMap corpusConfigs = annisDao.getCorpusConfigurations();
    CorpusConfigMap result = new CorpusConfigMap();
    Subject user = SecurityUtils.getSubject();

    if (corpusConfigs != null)
    {
      for (String c : corpusConfigs.getCorpusConfigs().keySet())
      {
        if (user.isPermitted("query:*:" + c))
        {
          result.put(c, corpusConfigs.get(c));
        }
      }
    }

    return result;
  }

  @GET
  @Path("corpora/default-config")
  @Produces("application/xml")
  public CorpusConfig corpusDefaultConfig()
  {
    return defaultCorpusConfig;
  }

  @GET
  @Path("corpora/{top}/config")
  @Produces("application/xml")
  public CorpusConfig corpusConfig(@PathParam("top") String toplevelName)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:config:" + toplevelName);
    Properties tmp = annisDao.getCorpusConfiguration(toplevelName);
    CorpusConfig corpusConfig = new CorpusConfig();
    corpusConfig.setConfig(tmp);

    return corpusConfig;
  }

  @GET
  @Path("corpora/{top}/annotations")
  @Produces("application/xml")
  public List<AnnisAttribute> annotations(
    @PathParam("top") String toplevelCorpus,
    @DefaultValue("false") @QueryParam("fetchvalues") String fetchValues,
    @DefaultValue("false") @QueryParam("onlymostfrequentvalues") String onlyMostFrequentValues) throws WebApplicationException
  {
    try
    {
      Subject user = SecurityUtils.getSubject();
      user.checkPermission("query:annotations:" + toplevelCorpus);


      List<String> list = new LinkedList<String>();
      String decode = URLDecoder.decode(toplevelCorpus, "UTF-8");
      log.info("corpus annotations for {}", decode);
      list.add(decode);
      List<Long> corpusList = annisDao.mapCorpusNamesToIds(list);

      return annisDao.listAnnotations(corpusList,
        Boolean.parseBoolean(fetchValues), Boolean.parseBoolean(
        onlyMostFrequentValues));
    }
    catch (Exception ex)
    {
      log.error("could not get annotations for {}", toplevelCorpus, ex);
      throw new WebApplicationException(400);
    }
  }

  /**
   * Return true if this is a valid query or throw exception when invalid
   *
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
  @Path("corpora/{top}/documents")
  @Produces(MediaType.APPLICATION_XML)
  public List<Annotation> getDocNames(@PathParam("top") String topLevelCorpus)
  {
    return annisDao.listDocuments(topLevelCorpus);
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
    @PathParam("document") String documentName,
    @QueryParam("exclude") @DefaultValue("false") boolean exclude)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:meta:" + toplevelCorpusName);

    if (documentName == null)
    {
      documentName = toplevelCorpusName;
    }

    return annisDao.listCorpusAnnotations(toplevelCorpusName, documentName,
      exclude);
  }

  @GET
  @Path("corpora/{top}/allmetadata")
  @Produces("application/xml")
  public List<Annotation> getAllMetadata(
    @PathParam("top") String toplevelCorpusName)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:meta:" + toplevelCorpusName);
    return annisDao.listDocumentsAnnotations(toplevelCorpusName, true);
  }

  @GET
  @Path("corpora/{top}/docmetadata")
  @Produces("application/xml")
  public List<Annotation> getDocMetadata(
    @PathParam("top") String toplevelCorpusName)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:meta:" + toplevelCorpusName);
    return annisDao.listDocumentsAnnotations(toplevelCorpusName, false);
  }

  @GET
  @Path("corpora/{top}/{document}/binary/{offset}/{length}")
  public Response binary1(
    @PathParam("top") String toplevelCorpusName,
    @PathParam("document") String corpusName,
    @PathParam("offset") String rawOffset,
    @PathParam("length") String rawLength)
  {
    return binary(toplevelCorpusName, corpusName, rawOffset, rawLength, null);
  }

  @GET
  @Path("corpora/{top}/{document}/binary")
  public Response binary2(
    @PathParam("top") String toplevelCorpusName,
    @PathParam("document") String corpusName)
  {
    return binary(toplevelCorpusName, corpusName, null, null, null);
  }

  @GET
  @Path("corpora/{top}/{document}/binary/{file}/{offset}/{length}")
  public Response binary3(
    @PathParam("top") String toplevelCorpusName,
    @PathParam("document") String corpusName,
    @PathParam("file") String file,
    @PathParam("offset") String rawOffset,
    @PathParam("length") String rawLength)
  {
    return binary(toplevelCorpusName, corpusName, rawOffset, rawLength, file);
  }

  @GET
  @Path("corpora/{top}/{document}/binary/{file}")
  public Response binary4(
    @PathParam("top") String toplevelCorpusName,
    @PathParam("document") String corpusName,
    @PathParam("file") String file)
  {
    return binary(toplevelCorpusName, corpusName, null, null, file);
  }

  /**
   * Get an Annis Binary object identified by its id.
   *
   * @param id
   * @param rawOffset the part we want to start from, we start from 0
   * @param rawLength how many bytes we take
   * @return AnnisBinary
   */
  @Override
  public Response binary(
    String toplevelCorpusName,
    String corpusName,
    String rawOffset,
    String rawLength,
    String fileName)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:binary:" + toplevelCorpusName);

    String acceptHeader = request.getHeader("Accept");
    if (acceptHeader == null || acceptHeader.isEmpty())
    {
      acceptHeader = "*/*";
    }

    List<AnnisBinaryMetaData> meta = annisDao.getBinaryMeta(toplevelCorpusName,
      corpusName);
    HashMap<String, AnnisBinaryMetaData> matchedMetaByType = new LinkedHashMap<String, AnnisBinaryMetaData>();

    for (AnnisBinaryMetaData m : meta)
    {
      if (fileName == null)
      {
        // just add all available media types
        if (!matchedMetaByType.containsKey(m.getMimeType()))
        {
          matchedMetaByType.put(m.getMimeType(), m);
        }
      }
      else
      {
        // check if this binary has the right title/file name
        if (fileName.equals(m.getFileName()))
        {
          matchedMetaByType.put(m.getMimeType(), m);
        }
      }
    }

    if (matchedMetaByType.isEmpty())
    {
      return Response.status(Response.Status.NOT_FOUND)
        .entity("Requested binary not found")
        .build();
    }

    // find the best matching mime type
    String bestMediaTypeMatch =
      MIMEParse.bestMatch(matchedMetaByType.keySet(), acceptHeader);
    if (bestMediaTypeMatch.isEmpty())
    {
      return Response.status(Response.Status.NOT_ACCEPTABLE)
        .entity("Client must accept one of the following media types: "
        + StringUtils.join(matchedMetaByType.keySet(), ", "))
        .build();
    }
    MediaType mediaType = MediaType.valueOf(bestMediaTypeMatch);

    int offset = 0;
    int length = 0;

    if (rawLength == null || rawOffset == null)
    {
      // use matched binary meta data to get the complete file size
      AnnisBinaryMetaData matchedBinary = matchedMetaByType.get(mediaType.
        toString());
      if (matchedBinary != null)
      {
        length = matchedBinary.getLength();
      }
    }
    else
    {
      // use the provided information
      offset = Integer.parseInt(rawOffset);
      length = Integer.parseInt(rawLength);
    }

    log.debug(
      "fetching  " + (length / 1024) + "kb (" + offset + "-" + (offset + length) + ") from binary "
      + toplevelCorpusName + "/" + corpusName + (fileName == null ? "" : fileName) + " "
      + mediaType.toString());

    final InputStream stream = annisDao.
      getBinary(toplevelCorpusName, corpusName, mediaType.toString(), fileName,
      offset, length);

    log.debug("fetch successfully");
    StreamingOutput result = new StreamingOutput()
    {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException
      {
        try
        {
          IOUtils.copy(stream, output);
          output.flush();
        }
        finally
        {
          stream.close();
        }
      }
    };

    return Response.ok(result, mediaType).build();
  }

  /**
   * Fetches the example queries for a specific corpus.
   *
   * @param rawCorpusNames specifies the corpora the examples are fetched from.
   *
   */
  @GET
  @Path("corpora/example-queries/")
  @Produces(MediaType.APPLICATION_XML)
  public List<ExampleQuery> getExampleQueries(
    @QueryParam("corpora") String rawCorpusNames) throws WebApplicationException
  {

    try
    {
      String[] corpusNames;
      if (rawCorpusNames != null)
      {
        corpusNames = rawCorpusNames.split(",");
      }
      else
      {
        List<AnnisCorpus> allCorpora = annisDao.listCorpora();
        corpusNames = new String[allCorpora.size()];
        for (int i = 0; i < corpusNames.length; i++)
        {
          corpusNames[i] = allCorpora.get(i).getName();
        }
      }

      List<String> allowedCorpora = new ArrayList<String>();

      // filter by which corpora the user is allowed to access
      Subject user = SecurityUtils.getSubject();
      for (String c : corpusNames)
      {
        if (user.isPermitted("query:*:" + c))
        {
          allowedCorpora.add(c);
        }
      }

      List<Long> corpusIDs = annisDao.mapCorpusNamesToIds(allowedCorpora);
      return annisDao.getExampleQueries(corpusIDs);
    }
    catch (Exception ex)
    {
      throw new WebApplicationException(400);
    }
  }

  /**
   * Get the Metadata of an Annis Binary object identified by its id.
   *
   * @param id
   * @return AnnisBinaryMetaData
   */
  @GET
  @Path("corpora/{top}/{document}/binary/meta")
  @Produces("application/xml")
  @Override
  public List<AnnisBinaryMetaData> binaryMeta(
    @PathParam("top") String toplevelCorpusName,
    @PathParam("document") String documentName)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("query:binary:" + toplevelCorpusName);

    return annisDao.getBinaryMeta(toplevelCorpusName, documentName);

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
   *
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
        "missing required parameter '" + name + "' (" + description + ")").
        build());
    }
  }

  /**
   * Get the {@link QueryData} from a query and the corpus names
   *
   * @param query The AQL query.
   * @param rawCorpusNames The name of the toplevel corpus names seperated by
   * ",".
   * @return calculated {@link QueryData} for the given parametes.
   *
   * @throws WebApplicationException Thrown if some corpora are unknown to the
   * system.
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
   *
   * @param rawCorpusNames The corpus names separated by ",".
   * @return
   */
  private List<String> splitCorpusNamesFromRaw(String rawCorpusNames)
  {
    return Arrays.asList(rawCorpusNames.split(","));
  }

  /**
   * Splits a list of qualified (meta-) annotation names into a proper java
   * list.
   *
   * @param rawCorpusNames The qualified names separated by ",".
   * @return
   */
  private List<MatrixQueryData.QName> splitMatrixKeysFromRaw(String raw)
  {
    LinkedList<MatrixQueryData.QName> result = new LinkedList<MatrixQueryData.QName>();

    String[] split = raw.split(",");
    for (String s : split)
    {
      String[] nameSplit = s.trim().split(":", 2);
      MatrixQueryData.QName qname = new MatrixQueryData.QName();
      if (nameSplit.length == 2)
      {
        qname.namespace = nameSplit[0].trim();
        qname.name = nameSplit[1].trim();
      }
      else
      {
        qname.name = nameSplit[0].trim();
      }
      result.add(qname);
    }

    return result;
  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  /**
   * Retrieves the max right context.
   *
   * @return The context is read from the annis-service.properties file and
   * should be >= 0.
   */
  public int getContextRight()
  {
    return getCorpusConfigIntValues("max-context-right");
  }

  /**
   * Retrieves the max left context.
   *
   * @return The context is read from the annis-service.properties file and
   * should be >= 0.
   */
  public int getContextLeft()
  {
    return getCorpusConfigIntValues("max-context-left");
  }

  /**
   * Extract corpus configurations values with numeric values.
   *
   * @param context Must be a valid key of the corpus configuration section in
   * the annis-service.properties file.
   * @return Parses the String representation of the value to int and returns
   * it.
   *
   */
  private int getCorpusConfigIntValues(String context)
  {
    int value = Integer.parseInt(defaultCorpusConfig.getConfig().getProperty(
      context));

    if (value < 0)
    {
      throw new IllegalStateException("the value must be > 0");
    }

    return value;
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

  /**
   * @return the defaultCorpusConfig
   */
  public CorpusConfig getDefaultCorpusConfig()
  {
    return defaultCorpusConfig;
  }

  /**
   * @param defaultCorpusConfig the defaultCorpusConfig to set
   */
  public void setDefaultCorpusConfig(
    CorpusConfig defaultCorpusConfig)
  {
    this.defaultCorpusConfig = defaultCorpusConfig;
  }
}
