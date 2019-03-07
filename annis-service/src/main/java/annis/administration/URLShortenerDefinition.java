package annis.administration;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.date.DateFormatUtils;

import com.google.common.base.Joiner;

import annis.CommonHelper;
import annis.QueryGenerator;
import annis.dao.QueryDao;
import annis.model.DisplayedResultQuery;
import annis.model.Query;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.QueryLanguage;
import annis.sqlgen.extensions.LimitOffsetQueryData;

public class URLShortenerDefinition {

    private final static Logger log = LoggerFactory.getLogger(URLShortenerDefinition.class);

    private URI uri;
    private DisplayedResultQuery query;
    private UUID uuid;
    private DateTime creationTime;

    private String errorMsg;

    protected URLShortenerDefinition(URI uri, UUID uuid, DateTime creationTime) {
        this(uri, uuid, creationTime, new DisplayedResultQuery());
    }

    protected URLShortenerDefinition(URI uri, UUID uuid, DateTime creationTime, DisplayedResultQuery query) {
        this.uri = uri;
        this.uuid = uuid;
        this.query = query;
        this.errorMsg = null;
    }

    public static URLShortenerDefinition parse(String url, String uuid, String creationTime)
            throws URISyntaxException, UnsupportedEncodingException {

        URI parsedURI = new URI(url);

        DateTimeFormatter dateFormatter = DateTimeFormat
                .forPattern("yyyy-MM-dd HH:mm:ss.SSSZZ");

        URLShortenerDefinition result = new URLShortenerDefinition(parsedURI, UUID.fromString(uuid),
                dateFormatter.parseDateTime(creationTime));

        if (parsedURI.getPath().startsWith("/embeddedvis")) {

            for (NameValuePair arg : URLEncodedUtils.parse(parsedURI, "UTF-8")) {
                if ("embedded_interface".equals(arg.getName())) {
                    URI interfaceURI = new URI(arg.getValue());
                    result.query = parseFragment(interfaceURI.getFragment());
                    break;
                }
            }

        } else {
            result.query = parseFragment(parsedURI.getFragment());
        }

        return result;
    }

    private static DisplayedResultQuery parseFragment(String fragment) {
        Map<String, String> args = CommonHelper.parseFragment(fragment);
        String corporaRaw = args.get("c");
        if (corporaRaw != null) {
            Set<String> corpora = new LinkedHashSet<>(Arrays.asList(corporaRaw.split("\\s*,\\s*")));

            return QueryGenerator.displayed().left(Integer.parseInt(args.get("cl")))
                    .right(Integer.parseInt(args.get("cr"))).offset(Integer.parseInt(args.get("s")))
                    .limit(Integer.parseInt(args.get("l"))).segmentation(args.get("seg")).baseText(args.get("bt"))
                    .query(args.get("q")).corpora(corpora).build();
        }
        return null;
    }

    public URLShortenerDefinition rewriteInQuirksMode() {
        DisplayedResultQuery rewrittenQuery = new DisplayedResultQuery(this.query);
        rewrittenQuery.setQueryLanguage(QueryLanguage.AQL_QUIRKS_V3);

        UriBuilder rewrittenUri = UriBuilder.fromUri(this.uri);
        if (this.uri.getPath().startsWith("/embeddedvis")) {
            // we need to keep query parameters arguments, except for the one with the
            // linked query
            rewrittenUri.replaceQueryParam("embedded_interface", rewrittenQuery.toCitationFragment());
        } else {
            // just update the fragment, but leave everything else the same
            rewrittenUri.fragment(rewrittenQuery.toCitationFragment());
        }

        return new URLShortenerDefinition(rewrittenUri.build(), this.uuid, this.creationTime, rewrittenQuery);
    }

    public Query getQuery() {
        return query;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public UUID getUuid() {
        return uuid;
    }

    public URI getUri() {
        return uri;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public static int MAX_RETRY = 3;

    public QueryStatus test(QueryDao queryDao, WebTarget annisSearchService) throws GraphANNISException {

        if (this.query.getCorpora().isEmpty()) {
            this.errorMsg = "Empty corpus list";
            return QueryStatus.Failed;
        }

        List<Match> matchesGraphANNIS = queryDao.find(query.getQuery(), query.getQueryLanguage(),
                new LinkedList<>(query.getCorpora()), new LimitOffsetQueryData(0, Integer.MAX_VALUE));

        WebTarget findTarget = annisSearchService.path("find").queryParam("q", query.getQuery()).queryParam("corpora",
                Joiner.on(",").join(query.getCorpora()));

        try {

            int tries = 0;
            MatchGroup matchesLegacy = null;
            while (matchesLegacy == null) {
                try {
                    tries++;
                    matchesLegacy = findTarget.request(MediaType.APPLICATION_XML_TYPE).get(MatchGroup.class);
                } catch (ServerErrorException ex) {
                    if (tries >= MAX_RETRY) {
                        throw ex;
                    } else {
                        log.warn("Server error when executing query {}", query.getQuery(), ex);
                    }
                }
            }

            QueryStatus status = QueryStatus.Ok;
            if (matchesGraphANNIS.size() != matchesLegacy.getMatches().size()) {
                this.errorMsg = "should have been " + matchesLegacy.getMatches().size() + " but was "
                        + matchesGraphANNIS.size();
                status = QueryStatus.CountDiffers;
            } else {
                Iterator<Match> itGraphANNIS = matchesGraphANNIS.iterator();
                Iterator<Match> itLegacy = matchesLegacy.getMatches().iterator();
                while (itGraphANNIS.hasNext() && itLegacy.hasNext()) {
                    String m1 = itGraphANNIS.next().toString();
                    String m2 = itLegacy.next().toString();

                    if (!m1.equals(m2)) {
                        this.errorMsg = m1 + " != " + m2;
                        status = QueryStatus.MatchesDiffer;
                        break;
                    }
                }
            }

            if (status != QueryStatus.Ok && this.query.getQueryLanguage() == QueryLanguage.AQL) {
                // check in quirks mode and rewrite if necessary
                log.info("Trying quirks mode for query {} on corpus {}", this.query.getQuery(),
                        this.query.getCorpora());

                URLShortenerDefinition quirksQuery = this.rewriteInQuirksMode();
                QueryStatus quirksStatus = quirksQuery.test(queryDao, annisSearchService);
                if (quirksStatus == QueryStatus.Ok) {
                    this.query = quirksQuery.query;
                    this.uri = quirksQuery.uri;
                    this.errorMsg = "Rewrite in quirks mode necessary";
                    status = QueryStatus.Ok;
                } else {
                    this.errorMsg = quirksQuery.getErrorMsg();
                    log.warn("Quirks not sucessful");
                }
            }

            return status;

        } catch (ForbiddenException ex) {
            this.errorMsg = ex.toString();
            return QueryStatus.Failed;
        }
    }
}
