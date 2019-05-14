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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.corpus_tools.graphannis.CorpusStorageManager.CountResult;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import annis.CommonHelper;
import annis.QueryGenerator;
import annis.dao.QueryDao;
import annis.exceptions.AnnisTimeoutException;
import annis.model.DisplayedResultQuery;
import annis.model.Query;
import annis.service.objects.Match;
import annis.service.objects.MatchAndDocumentCount;
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
        this.creationTime = creationTime;
        this.errorMsg = null;
    }

    public static UUID parseUUID(String uuid) {
        return UUID.fromString(uuid);
    }

    public static DateTime parseCreationTime(String creationTime) {

        DateTimeParser[] parsers = { DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSZZ").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZZ").getParser(), };

        DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();
        return dateFormatter.parseDateTime(creationTime);
    }

    public static URLShortenerDefinition parse(String url, String uuid, String creationTime)
            throws URISyntaxException, UnsupportedEncodingException {

        URI parsedURI = new URI(url);

        URLShortenerDefinition result = new URLShortenerDefinition(parsedURI, parseUUID(uuid),
                parseCreationTime(creationTime));

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

    public static int MAX_RETRY = 5;

    private QueryStatus testFind(QueryDao queryDao, WebTarget annisSearchService, int offset, int limit)
            throws GraphANNISException {

        WebTarget findTarget = annisSearchService.path("find").queryParam("q", query.getQuery()).queryParam("corpora",
                Joiner.on(",").join(query.getCorpora()));

        List<Match> matchesGraphANNIS = queryDao.find(query.getQuery(), query.getQueryLanguage(),
                new LinkedList<>(query.getCorpora()), new LimitOffsetQueryData(offset, limit));

        int tries = 0;
        MatchGroup matchesLegacy = null;
        while (matchesLegacy == null) {
            try {
                tries++;
                matchesLegacy = findTarget.queryParam("offset", offset).queryParam("limit", limit)
                        .request(MediaType.APPLICATION_XML_TYPE).get(MatchGroup.class);
            } catch (ServerErrorException ex) {
                if (tries >= MAX_RETRY) {
                    this.errorMsg = ex.getMessage();
                    return QueryStatus.Failed;
                } else {
                    log.warn("Server error when executing query: {}. Will retry", query.getQuery(), ex);
                }
            }
        }

        Iterator<Match> itGraphANNIS = matchesGraphANNIS.iterator();
        Iterator<Match> itLegacy = matchesLegacy.getMatches().iterator();
        while (itGraphANNIS.hasNext() && itLegacy.hasNext()) {
            String m1 = itGraphANNIS.next().toString();
            String m2 = itLegacy.next().toString();

            if (!m1.equals(m2)) {
                this.errorMsg = "(should be)" + System.lineSeparator() + m2 + System.lineSeparator() + "(but was)"
                        + System.lineSeparator() + m1;
                return QueryStatus.MatchesDiffer;
            }
        }

        return QueryStatus.Ok;
    }

    public QueryStatus test(QueryDao queryDao, WebTarget annisSearchService) throws GraphANNISException {

        if (this.query.getCorpora().isEmpty()) {
            this.errorMsg = "Empty corpus list";
            return QueryStatus.Failed;
        }

        // check count first (also warmup for the corpus)
        int countGraphANNIS;

        try {
            countGraphANNIS = queryDao.count(query.getQuery(), query.getQueryLanguage(),
                    new LinkedList<>(query.getCorpora()));
        } catch (GraphANNISException ex) {
            countGraphANNIS = 0;
        }

        try {

            QueryStatus status = QueryStatus.Ok;

            Optional<Integer> countLegacy = Optional.empty();
            try {
                for (int tries = 0; tries < MAX_RETRY; tries++) {
                    try {
                        countLegacy = Optional.of(annisSearchService.path("count").queryParam("q", query.getQuery())
                                .queryParam("corpora", Joiner.on(",").join(query.getCorpora())).request()
                                .get(MatchAndDocumentCount.class).getMatchCount());
                        break;
                    } catch (ServerErrorException ex) {
                        if (tries >= MAX_RETRY - 1) {
                            this.errorMsg = ex.getMessage();
                            return QueryStatus.Failed;
                        } else {
                            log.warn("Server error when executing query {}", query.getQuery(), ex);
                        }
                    }
                }
            } catch (BadRequestException ex) {
                countLegacy = Optional.of(0);
            }

            if (countGraphANNIS != countLegacy.get()) {

                this.errorMsg = "should have been " + countLegacy.get() + " but was " + countGraphANNIS;
                status = QueryStatus.CountDiffers;

            } else {

                // execute find with smaller blocks of matches
                final int limit = 100;
                for (int offset = 0; offset + limit < countGraphANNIS; offset += limit) {
                    status = testFind(queryDao, annisSearchService, offset, limit);
                    if (status == QueryStatus.Failed) {
                        // don't try quirks mode when failed
                        return status;
                    } else if (status != QueryStatus.Ok) {
                        // failed once, don't try the other offset/limit combinations
                        break;
                    }
                }
            }

            if (status != QueryStatus.Ok && this.query.getQueryLanguage() == QueryLanguage.AQL) {
                // check in quirks mode and rewrite if necessary
                log.info("Trying quirks mode for query {} on corpus {}", this.query.getQuery().trim(),
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
                }
            }

            return status;

        } catch (ForbiddenException | AnnisTimeoutException ex) {
            this.errorMsg = ex.toString();
            return QueryStatus.Failed;
        }
    }
}
