
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

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.corpus_tools.graphannis.CorpusStorageManager.QueryLanguage;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.corpus_tools.graphannis.model.NodeDesc;
import org.corpus_tools.salt.common.SaltProject;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.mimeparse.MIMEParse;

import annis.CommonHelper;
import annis.ServiceConfig;
import annis.dao.QueryDao;
import annis.examplequeries.ExampleQuery;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisAttribute.Type;
import annis.service.objects.AnnisBinaryMetaData;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.CorpusConfig;
import annis.service.objects.CorpusConfigMap;
import annis.service.objects.DocumentBrowserConfig;
import annis.service.objects.FrequencyTable;
import annis.service.objects.FrequencyTableQuery;
import annis.service.objects.Match;
import annis.service.objects.MatchAndDocumentCount;
import annis.service.objects.MatchGroup;
import annis.service.objects.OrderType;
import annis.service.objects.RawTextWrapper;
import annis.service.objects.SegmentationList;
import annis.service.objects.SubgraphFilter;
import annis.sqlgen.extensions.AnnotateQueryData;
import annis.sqlgen.extensions.LimitOffsetQueryData;

/**
 * Methods for querying the database.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
@Path("annis/query")
public class QueryService {

    private final static Logger log = LoggerFactory.getLogger(QueryService.class);

    private final static Logger queryLog = LoggerFactory.getLogger("QueryLog");

    private final ServiceConfig serviceConfig = ConfigFactory.create(ServiceConfig.class);

    @Context
    Configuration config;

    private final CorpusConfig defaultCorpusConfig;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpServletRequest request;

    @Context
    private ResourceConfig rc;

    /**
     * Log the successful initialization of this bean.
     *
     * <p>
     * XXX: This should be a private method annotated with <tt>@PostConstruct</tt>,
     * but that doesn't seem to work. As a work-around, the method is called by
     * Spring as an init-method.
     */
    public void init() {
        // log a message after successful startup
        log.info("ANNIS QueryService loaded.");
    }

    public QueryService() {
        defaultCorpusConfig = new CorpusConfig();
        defaultCorpusConfig.setConfig("max-context-left", "" + serviceConfig.maxContextLeft());
        defaultCorpusConfig.setConfig("max-context-right", "" + serviceConfig.maxContextLeft());
        defaultCorpusConfig.setConfig("default-context", "" + serviceConfig.defaultContext());
        defaultCorpusConfig.setConfig("context-steps", "" + serviceConfig.contextSteps());
        defaultCorpusConfig.setConfig("results-per-page", "" + serviceConfig.resultsPerPage());
        defaultCorpusConfig.setConfig("default-context-segmentation", serviceConfig.defaultContextSegmenation());
        defaultCorpusConfig.setConfig("default-base-text-segmentation", serviceConfig.defaultBaseTextSegmentation());
        defaultCorpusConfig.setConfig("browse-documents", Boolean.toString(serviceConfig.browseDocuments()));

    }

    private QueryDao getQueryDao() {
        Object prop = config.getProperty("queryDao");
        if (prop instanceof QueryDao) {
            return (QueryDao) prop;
        } else {
            return null;
        }
    }

    @GET
    @Path("search/count")
    @Produces("application/xml")
    public Response count(@QueryParam("q") String query, @QueryParam("corpora") String rawCorpusNames)
            throws GraphANNISException {

        requiredParameter(query, "q", "AnnisQL query");
        requiredParameter(rawCorpusNames, "corpora", "comma separated list of corpus names");

        Subject user = SecurityUtils.getSubject();
        List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
        for (String c : corpusNames) {
            user.checkPermission("query:count:" + c);
        }

        List<String> corpusList = findCorporaFromQuery(rawCorpusNames);
        long start = new Date().getTime();
        MatchAndDocumentCount count = getQueryDao().countMatchesAndDocuments(query, corpusList);
        long end = new Date().getTime();

        logQuery("COUNT", query, splitCorpusNamesFromRaw(rawCorpusNames), end - start);

        return Response.ok(count).type(MediaType.APPLICATION_XML_TYPE).build();
    }

    private StreamingOutput findRaw(final String rawCorpusNames, final String query,
            final LimitOffsetQueryData limitOffset) throws IOException {
        List<String> corpora = findCorporaFromQuery(rawCorpusNames);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                long start = new Date().getTime();
                try {
                    getQueryDao().find(query, corpora, limitOffset, output);
                } catch (GraphANNISException e) {
                    throw new WebApplicationException(e);
                }
                long end = new Date().getTime();
                logQuery("FIND", query, splitCorpusNamesFromRaw(rawCorpusNames), end - start);
            }
        };

    }

    private List<Match> findXml(final String rawCorpusNames, final String query, final LimitOffsetQueryData limitOffset)
            throws IOException, GraphANNISException {
        List<String> corpora = findCorporaFromQuery(rawCorpusNames);
        long start = new Date().getTime();
        List<Match> result = getQueryDao().find(query, corpora, limitOffset);
        long end = new Date().getTime();
        logQuery("FIND", query, splitCorpusNamesFromRaw(rawCorpusNames), end - start);
        return result;
    }

    @GET
    @Path("search/find")
    @Produces({ "application/xml", "text/plain" })
    public Response find(@QueryParam("q") String query, @QueryParam("corpora") String rawCorpusNames,
            @DefaultValue("0") @QueryParam("offset") String offsetRaw,
            @DefaultValue("-1") @QueryParam("limit") String limitRaw,
            @DefaultValue("ascending") @QueryParam("order") String orderRaw) throws IOException, GraphANNISException {
        requiredParameter(query, "q", "AnnisQL query");
        requiredParameter(rawCorpusNames, "corpora", "comma separated list of corpus names");

        Subject user = SecurityUtils.getSubject();
        List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
        for (String c : corpusNames) {
            user.checkPermission("query:find:" + c);
        }

        int offset = Integer.parseInt(offsetRaw);
        int limit = Integer.parseInt(limitRaw);

        OrderType order;
        try {
            order = OrderType.valueOf(orderRaw.toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN)
                    .entity("parameter 'order' has the invalid value '" + orderRaw + "'. It should be one of"
                            + " 'ascending', 'random' or 'descending")
                    .build());
        }

        LimitOffsetQueryData limitOffset = new LimitOffsetQueryData(offset, limit, order);

        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        if (acceptHeader == null || acceptHeader.trim().isEmpty()) {
            acceptHeader = "*/*";
        }

        List<String> knownTypes = Lists.newArrayList("text/plain", "application/xml");

        // find the best matching mime type
        String bestMediaTypeMatch = MIMEParse.bestMatch(knownTypes, acceptHeader);

        if ("text/plain".equals(bestMediaTypeMatch)) {
            return Response.ok(findRaw(rawCorpusNames, query, limitOffset), "text/plain").build();
        } else {
            List<Match> result = findXml(rawCorpusNames, query, limitOffset);
            return Response.ok().type("application/xml").entity(new GenericEntity<MatchGroup>(new MatchGroup(result)) {
            }).build();
        }

    }

    /**
     * Frequency analysis.
     * 
     * @param rawFields
     *                      Comma seperated list of result vector elements. Each
     *                      element has the form <node-nr>:<anno-name>. The
     *                      annotation name can be set to "tok" to indicate that the
     *                      span should be used instead of an annotation.
     * @throws GraphANNISException
     */
    @GET
    @Path("search/frequency")
    @Produces("application/xml")
    public FrequencyTable frequency(@QueryParam("q") String query, @QueryParam("corpora") String rawCorpusNames,
            @QueryParam("fields") String rawFields) throws GraphANNISException {
        requiredParameter(query, "q", "AnnisQL query");
        requiredParameter(rawCorpusNames, "corpora", "comma separated list of corpus names");
        requiredParameter(rawFields, "fields", "Comma seperated list of result vector elements.");

        Subject user = SecurityUtils.getSubject();
        List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
        for (String c : corpusNames) {
            user.checkPermission("query:matrix:" + c);
        }

        List<String> corpusList = findCorporaFromQuery(rawCorpusNames);
        FrequencyTableQuery freqTableQuery = FrequencyTableQuery.parse(rawFields);

        long start = new Date().getTime();
        FrequencyTable freqTable = getQueryDao().frequency(query, corpusList, freqTableQuery);
        long end = new Date().getTime();
        logQuery("FREQUENCY", query, splitCorpusNamesFromRaw(rawCorpusNames), end - start);

        return freqTable;
    }

    @POST
    @Path("search/subgraph")
    @Consumes({ "application/xml", "text/plain" })
    @Produces({ "application/xml", "application/xmi+xml", "application/xmi+binary", "application/graphml+xml" })
    public SaltProject subgraph(MatchGroup matches, @QueryParam("segmentation") String segmentation,
            @DefaultValue("0") @QueryParam("left") String leftRaw,
            @DefaultValue("0") @QueryParam("right") String rightRaw,
            @DefaultValue("all") @QueryParam("filter") String filterRaw) throws GraphANNISException {

        // some robustness stuff
        if (matches == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN)
                    .entity("missing required request body").build());
        }

        return basicSubgraph(matches, segmentation, leftRaw, rightRaw, filterRaw);
    }

    @GET
    @Path("search/subgraph")
    @Produces({ "application/xml", "application/xmi+xml", "application/xmi+binary", "application/graphml+xml" })
    public SaltProject subgraph(@QueryParam("match") String matchRaw, @QueryParam("segmentation") String segmentation,
            @DefaultValue("0") @QueryParam("left") String leftRaw,
            @DefaultValue("0") @QueryParam("right") String rightRaw,
            @DefaultValue("all") @QueryParam("filter") String filterRaw) throws GraphANNISException {
        // some robustness stuff
        requiredParameter(matchRaw, "match", "definition of the match");

        MatchGroup matches = MatchGroup.parseString(matchRaw);

        return basicSubgraph(matches, segmentation, leftRaw, rightRaw, filterRaw);
    }

    protected SaltProject basicSubgraph(MatchGroup matches, @QueryParam("segmentation") String segmentation,
            @DefaultValue("0") @QueryParam("left") String leftRaw,
            @DefaultValue("0") @QueryParam("right") String rightRaw,
            @DefaultValue("all") @QueryParam("filter") String filterRaw) throws GraphANNISException {

        Subject user = SecurityUtils.getSubject();

        int left = Integer.parseInt(leftRaw);
        int right = Integer.parseInt(rightRaw);
        SubgraphFilter filter = SubgraphFilter.valueOf(filterRaw);

        AnnotateQueryData annoExt = new AnnotateQueryData(left, right, segmentation, filter);

        Set<String> corpusNames = new TreeSet<>();

        for (Match singleMatch : matches.getMatches()) {
            // collect list of used corpora
            for (java.net.URI u : singleMatch.getSaltIDs()) {
                corpusNames.add(CommonHelper.getCorpusPath(u).get(0));
            }
        }

        for (String c : corpusNames) {
            user.checkPermission("query:subgraph:" + c);
        }

        List<String> corpusNamesList = new LinkedList<>(corpusNames);

        if (corpusNamesList == null || corpusNamesList.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST.getStatusCode());
        }

        long start = new Date().getTime();
        SaltProject p = getQueryDao().graph(matches, annoExt);
        long end = new Date().getTime();
        String options = "matches: " + matches.toString() + ", seg: " + segmentation + ", left: " + left + ", right: "
                + right + ", filter: " + filter;
        logQuery("SUBGRAPH", "", corpusNamesList, end - start, options);

        return p;
    }

    @GET
    @Path("graph/{top}/{doc}")
    @Produces({ "application/xml", "application/xmi+xml", "application/xmi+binary", "application/graphml+xml" })
    public SaltProject graph(@PathParam("top") String toplevelCorpusName, @PathParam("doc") String documentName,
            @QueryParam("filternodeanno") String filternodeanno) {

        Subject user = SecurityUtils.getSubject();
        user.checkPermission("query:subgraph:" + toplevelCorpusName);

        List<String> nodeAnnotationFilter = null;
        if (filternodeanno != null) {
            nodeAnnotationFilter = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(filternodeanno);
        }

        try {
            long start = new Date().getTime();
            SaltProject p = getQueryDao().retrieveAnnotationGraph(toplevelCorpusName, documentName,
                    nodeAnnotationFilter);
            long end = new Date().getTime();
            logQuery("GRAPH", toplevelCorpusName, documentName, end - start);
            return p;
        } catch (Exception ex) {
            log.error("error when accessing graph " + toplevelCorpusName + "/" + documentName, ex);
            throw new WebApplicationException(ex, 500);
        }
    }

    @GET
    @Path("resolver/{corpusName}/{namespace}/{type}")
    @Produces("application/xml")
    public List<ResolverEntry> resolver(@PathParam("corpusName") String corpusName,
            @PathParam("namespace") String namespace, @PathParam("type") String type) {
        ResolverEntry.ElementType enumType = ResolverEntry.ElementType.valueOf(type);
        SingleResolverRequest r = new SingleResolverRequest(corpusName, namespace, enumType);
        return getQueryDao().getResolverEntries(r);
    }

    @GET
    @Path("corpora")
    @Produces("application/xml")
    public List<AnnisCorpus> corpora() {
        List<AnnisCorpus> allCorpora = getQueryDao().listCorpora();

        List<AnnisCorpus> allowedCorpora = new LinkedList<>();

        // filter by which corpora the user is allowed to access
        Subject user = SecurityUtils.getSubject();
        for (AnnisCorpus c : allCorpora) {
            if (user.isPermitted("query:show:" + c.getName())) {
                allowedCorpora.add(c);
            }
        }

        return allowedCorpora;
    }

    @GET
    @Path("corpora/config")
    @Produces("application/xml")
    public CorpusConfigMap corpusConfigs() {
        CorpusConfigMap corpusConfigs = getQueryDao().getCorpusConfigurations();
        CorpusConfigMap result = new CorpusConfigMap();
        Subject user = SecurityUtils.getSubject();

        if (corpusConfigs != null) {
            for (String c : corpusConfigs.getCorpusConfigs().keySet()) {
                if (user.isPermitted("query:*:" + c)) {
                    result.put(c, corpusConfigs.get(c));
                }
            }
        }

        return result;
    }

    @GET
    @Path("corpora/default-config")
    @Produces("application/xml")
    public CorpusConfig corpusDefaultConfig() {
        return defaultCorpusConfig;
    }

    @GET
    @Path("corpora/{top}")
    @Produces("application/xml")
    public List<AnnisCorpus> singleCorpus(@PathParam("top") String toplevelName) {
        List<AnnisCorpus> result = new ArrayList<>();

        Subject user = SecurityUtils.getSubject();

        LinkedList<String> originalCorpusNames = new LinkedList<>();
        originalCorpusNames.add(toplevelName);

        // TODO: also add all corpora that match the alias name
        if (!originalCorpusNames.isEmpty()) {
            List<AnnisCorpus> allCorpora = getQueryDao().listCorpora(originalCorpusNames);
            for (AnnisCorpus c : allCorpora) {
                if (user.isPermitted("query:show:" + c.getName())) {
                    result.add(c);
                }
            }
        }

        return result;
    }

    @GET
    @Path("corpora/{top}/config")
    @Produces("application/xml")
    public CorpusConfig corpusConfig(@PathParam("top") String toplevelName) {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("query:config:" + toplevelName);

        try {
            Properties tmp = getQueryDao().getCorpusConfigurationSave(toplevelName);

            CorpusConfig corpusConfig = new CorpusConfig();
            corpusConfig.setConfig(tmp);

            return corpusConfig;
        } catch (Exception ex) {
            log.error("problems with reading config", ex);
            throw new WebApplicationException(ex, 500);
        }
    }

    @GET
    @Path("corpora/{top}/annotations")
    @Produces("application/xml")
    public List<AnnisAttribute> annotations(@PathParam("top") String toplevelCorpus,
            @DefaultValue("false") @QueryParam("fetchvalues") String fetchValues,
            @DefaultValue("false") @QueryParam("onlymostfrequentvalues") String onlyMostFrequentValues)
            throws WebApplicationException {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("query:annotations:" + toplevelCorpus);

        boolean fv = Boolean.parseBoolean(fetchValues);
        boolean omfv = Boolean.parseBoolean(onlyMostFrequentValues);
        
        if(fv && omfv) {
            return getQueryDao().listAnnotationsFromCache(Arrays.asList(toplevelCorpus));
        } else {
            return getQueryDao().listAnnotations(Arrays.asList(toplevelCorpus), fv, omfv);
        }
    }

    @GET
    @Path("corpora/{top}/segmentation-names")
    @Produces("application/xml")
    public SegmentationList segmentationNames(@PathParam("top") String toplevelCorpus) throws WebApplicationException {

        Subject user = SecurityUtils.getSubject();
        user.checkPermission("query:annotations:" + toplevelCorpus);
        
        List<AnnisAttribute> cachedAttributes = getQueryDao().listAnnotationsFromCache(Arrays.asList(toplevelCorpus));
        LinkedList<String> segmentations = new LinkedList<>();
        for(AnnisAttribute att : cachedAttributes) {
            if(att.getType() == Type.segmentation) {
                segmentations.add(att.getName());
            }
        }

        return new SegmentationList(segmentations);

    }

    /**
     * Return true if this is a valid query or throw exception when invalid
     *
     * @param query
     *                           Query to check for validity
     * @param rawCorpusNames
     * @return Either "ok" or an error message.
     * @throws GraphANNISException
     */
    @GET
    @Produces("text/plain")
    @Path("check")
    public String check(@QueryParam("q") String query, @DefaultValue("") @QueryParam("corpora") String rawCorpusNames)
            throws GraphANNISException {
        Subject user = SecurityUtils.getSubject();
        List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
        for (String c : corpusNames) {
            user.checkPermission("query:parse:" + c);
        }
        Collections.sort(corpusNames);
        if (getQueryDao().getCorpusStorageManager().validateQuery(corpusNames, query, QueryLanguage.AQL)) {
            return "ok";
        } else {
            return "error";
        }
    }

    /**
     * Return the list of the query nodes if this is a valid query or throw
     * exception when invalid
     *
     * @param query
     *                           Query to get the query nodes for
     * @param rawCorpusNames
     * @return
     * @throws GraphANNISException
     */
    @GET
    @Path("parse/nodes")
    @Produces("application/xml")
    public Response parseNodes(@QueryParam("q") String query,
            @DefaultValue("") @QueryParam("corpora") String rawCorpusNames) throws GraphANNISException {
        Subject user = SecurityUtils.getSubject();
        List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);
        for (String c : corpusNames) {
            user.checkPermission("query:parse:" + c);
        }
        Collections.sort(corpusNames);

        List<NodeDesc> nodes = getQueryDao().getCorpusStorageManager().getNodeDescriptions(query, QueryLanguage.AQL);

        return Response.ok(new GenericEntity<List<NodeDesc>>(nodes) {
        }).build();
    }

    @GET
    @Path("corpora/{top}/{document}/binary/{offset}/{length}")
    public Response binary1(@PathParam("top") String toplevelCorpusName, @PathParam("document") String corpusName,
            @PathParam("offset") String rawOffset, @PathParam("length") String rawLength) {
        return binary(toplevelCorpusName, corpusName, rawOffset, rawLength, null);
    }

    @GET
    @Path("corpora/{top}/{document}/binary")
    public Response binary2(@PathParam("top") String toplevelCorpusName, @PathParam("document") String corpusName) {
        return binary(toplevelCorpusName, corpusName, null, null, null);
    }

    @GET
    @Path("corpora/{top}/{document}/binary/{file}/{offset}/{length}")
    public Response binary3(@PathParam("top") String toplevelCorpusName, @PathParam("document") String corpusName,
            @PathParam("file") String file, @PathParam("offset") String rawOffset,
            @PathParam("length") String rawLength) {
        return binary(toplevelCorpusName, corpusName, rawOffset, rawLength, file);
    }

    @GET
    @Path("corpora/{top}/{document}/binary/{file}")
    public Response binary4(@PathParam("top") String toplevelCorpusName, @PathParam("document") String corpusName,
            @PathParam("file") String file) {
        return binary(toplevelCorpusName, corpusName, null, null, file);
    }

    /**
     * Get an Annis Binary object identified by its id.
     *
     * @param id
     * @param rawOffset
     *                      the part we want to start from, we start from 0
     * @param rawLength
     *                      how many bytes we take
     * @return AnnisBinary
     */
    public Response binary(String toplevelCorpusName, String corpusName, String rawOffset, String rawLength,
            String fileName) {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("query:binary:" + toplevelCorpusName);

        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        if (acceptHeader == null || acceptHeader.trim().isEmpty()) {
            acceptHeader = "*/*";
        }

        if (Objects.equals(toplevelCorpusName, corpusName)) {
            // the toplevel corpus was queried, not a document or sub-corpus
            // TODO: improve the URL patterns
            corpusName = null;
        }

        List<AnnisBinaryMetaData> meta = getQueryDao().getBinaryMeta(toplevelCorpusName, corpusName);
        HashMap<String, AnnisBinaryMetaData> matchedMetaByType = new LinkedHashMap<>();

        for (AnnisBinaryMetaData m : meta) {
            if (fileName == null) {
                // just add all available media types
                if (!matchedMetaByType.containsKey(m.getMimeType())) {
                    matchedMetaByType.put(m.getMimeType(), m);
                }
            } else {
                // check if this binary has the right title/file name
                if (fileName.equals(m.getFileName())) {
                    matchedMetaByType.put(m.getMimeType(), m);
                }
            }
        }

        if (matchedMetaByType.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Requested binary not found").build();
        }

        // find the best matching mime type
        String bestMediaTypeMatch = MIMEParse.bestMatch(matchedMetaByType.keySet(), acceptHeader);
        if (bestMediaTypeMatch.isEmpty()) {
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity("Client must accept one of the following media types: "
                            + StringUtils.join(matchedMetaByType.keySet(), ", "))
                    .build();
        }
        MediaType mediaType = MediaType.valueOf(bestMediaTypeMatch);

        int offset = 0;
        int length = 0;

        if (rawLength == null || rawOffset == null) {
            // use matched binary meta data to get the complete file size
            AnnisBinaryMetaData matchedBinary = matchedMetaByType.get(mediaType.toString());
            if (matchedBinary != null) {
                length = matchedBinary.getLength();
            }
        } else {
            // use the provided information
            offset = Integer.parseInt(rawOffset);
            length = Integer.parseInt(rawLength);
        }

        log.debug("fetching  " + (length / 1024) + "kb (" + offset + "-" + (offset + length) + ") from binary "
                + toplevelCorpusName + "/" + corpusName + (fileName == null ? "" : fileName) + " "
                + mediaType.toString());

        final InputStream stream = getQueryDao().getBinary(toplevelCorpusName, corpusName, mediaType.toString(),
                fileName, offset, length);

        log.debug("fetch successfully");
        StreamingOutput result = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    ByteStreams.copy(stream, output);
                    output.close();
                } finally {
                    stream.close();
                }
            }
        };

        return Response.ok(result, mediaType).build();
    }

    /**
     * Fetches the example queries for a specific corpus.
     *
     * @param rawCorpusNames
     *                           specifies the corpora the examples are fetched
     *                           from.
     *
     */
    @GET
    @Path("corpora/example-queries/")
    @Produces(MediaType.APPLICATION_XML)
    public List<ExampleQuery> getExampleQueries(@QueryParam("corpora") String rawCorpusNames)
            throws WebApplicationException {

        Subject user = SecurityUtils.getSubject();

        try {
            String[] corpusNames;
            if (rawCorpusNames != null) {
                corpusNames = rawCorpusNames.split(",");
            } else {
                List<AnnisCorpus> allCorpora = getQueryDao().listCorpora();
                corpusNames = new String[allCorpora.size()];
                for (int i = 0; i < corpusNames.length; i++) {
                    corpusNames[i] = allCorpora.get(i).getName();
                }
            }

            List<String> allowedCorpora = new ArrayList<>();

            // filter by which corpora the user is allowed to access
            for (String c : corpusNames) {
                if (user.isPermitted("query:*:" + c)) {
                    allowedCorpora.add(c);
                }
            }

            return getQueryDao().getExampleQueries(allowedCorpora);
        } catch (Exception ex) {
            log.error("Problem accessing example queries", ex);
            throw new WebApplicationException(ex, 500);
        }
    }

    private void logQuery(String queryFunction, String toplevelCorpus, String documentName, long runtime) {
        logQuery(queryFunction, null, asList(toplevelCorpus), runtime, "document: " + documentName);
    }

    private void logQuery(String queryFunction, String annisQuery, List<String> corpusNames, long runtime) {
        logQuery(queryFunction, annisQuery, corpusNames, runtime, null);
    }

    private void logQuery(String queryFunction, String annisQuery, List<String> corpusNames, long runtime,
            String options) {
        StringBuilder sb = new StringBuilder();
        sb.append("function: ");
        sb.append(queryFunction);
        sb.append(", ");
        if (annisQuery != null && !annisQuery.isEmpty()) {
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
        if (options != null && !options.isEmpty()) {
            sb.append(", ");
            sb.append(options);
        }
        String message = sb.toString();
        queryLog.info(message);
    }

    /**
     * Throw an exception if the parameter is missing.
     *
     * @param value
     *                        Value which is checked for null.
     * @param name
     *                        The short name of parameter.
     * @param description
     *                        A one line description of the meaing of the parameter.
     */
    private void requiredParameter(String value, String name, String description) throws WebApplicationException {
        if (value == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN)
                    .entity("missing required parameter '" + name + "' (" + description + ")").build());
        }
    }

    /**
     * Get the corpus names from the raw parameter
     *
     * @param rawCorpusNames
     *                           The name of the toplevel corpus names seperated by
     *                           ",".
     * @return cleaned up list of corpora
     *
     * @throws WebApplicationException
     *                                     Thrown if some corpora are unknown to the
     *                                     system.
     */
    private List<String> findCorporaFromQuery(String rawCorpusNames) throws WebApplicationException {
        List<String> corpusNames = splitCorpusNamesFromRaw(rawCorpusNames);

        // we don't care in which order the corpus list was given, we always sort the
        // names
        // this ensures a stable ordering and less surprises when the UI changes it's
        // behavior
        Collections.sort(corpusNames);

        List<AnnisCorpus> existingCorpora = getQueryDao().listCorpora(corpusNames);

        if (existingCorpora.size() != corpusNames.size()) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type("text/plain")
                    .entity("one ore more corpora are unknown to the system").build());
        }
        return corpusNames;
    }

    /**
     * Splits a list of corpus names into a proper java list.
     *
     * @param rawCorpusNames
     *                           The corpus names separated by ",".
     * @return
     */
    private List<String> splitCorpusNamesFromRaw(String rawCorpusNames) {
        return new ArrayList<>(Splitter.on(",").omitEmptyStrings().trimResults().splitToList(rawCorpusNames));
    }

    /**
     * Retrieves the max right context.
     *
     * @return The context is read from the annis-service.properties file and should
     *         be >= 0.
     */
    public int getContextRight() {
        return getCorpusConfigIntValues("max-context-right");
    }

    /**
     * Retrieves the max left context.
     *
     * @return The context is read from the annis-service.properties file and should
     *         be >= 0.
     */
    public int getContextLeft() {
        return getCorpusConfigIntValues("max-context-left");
    }

    /**
     * Extract corpus configurations values with numeric values.
     *
     * @param context
     *                    Must be a valid key of the corpus configuration section in
     *                    the annis-service.properties file.
     * @return Parses the String representation of the value to int and returns it.
     *
     */
    private int getCorpusConfigIntValues(String context) {
        int value = Integer.parseInt(defaultCorpusConfig.getConfig().getProperty(context));

        if (value < 0) {
            throw new IllegalStateException("the value must be > 0");
        }

        return value;
    }

    /**
     * @return the defaultCorpusConfig
     */
    public CorpusConfig getDefaultCorpusConfig() {
        return defaultCorpusConfig;
    }

    /**
     * Fetches the raw text from the text.tab file.
     *
     * @param top
     *                    the name of the top level corpus.
     * @param docname
     *                    the name of the document.
     *
     * @return Can be empty, if the corpus only contains media data or
     *         segmentations.
     */
    @GET
    @Path("rawtext/{top}/{docname}")
    @Produces(MediaType.APPLICATION_XML)
    public RawTextWrapper getRawText(@PathParam("top") String top, @PathParam("docname") String docname) {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("query:raw_text:" + top);

        RawTextWrapper result = new RawTextWrapper();
        result.setTexts(getQueryDao().getRawText(top, docname));
        return result;
    }

    @GET
    @Path("corpora/doc_browser_config/{corpus}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public DocumentBrowserConfig getDocumentBrowserConfig(@PathParam("corpus") String corpus) {
        DocumentBrowserConfig config = getQueryDao().getDocBrowserConfiguration(corpus);
        if (config == null) {
            config = getQueryDao().getDefaultDocBrowserConfiguration();
        }

        return (config != null) ? config : null;
    }
}
