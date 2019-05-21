package annis.administration;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
import annis.service.objects.QueryLanguage;
import annis.sqlgen.extensions.LimitOffsetQueryData;

public class URLShortenerDefinition {

    private final static Logger log = LoggerFactory.getLogger(URLShortenerDefinition.class);

    private URI uri;
    private DisplayedResultQuery query;
    private UUID uuid;
    private DateTime creationTime;
    private Set<String> unknownCorpora = new LinkedHashSet<>();

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
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZZ").getParser(), 
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").getParser()};

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
            corpora.remove("");

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
    
    public Set<String> getUnknownCorpora() {
        return unknownCorpora;
    }
    
    public void addUnknownCorpus(String corpus) {
        unknownCorpora.add(corpus);
    }

    public static int MAX_RETRY = 5;

    private QueryStatus testFind(QueryDao queryDao, WebTarget annisSearchService)
            throws GraphANNISException, IOException {

        WebTarget findTarget = annisSearchService.path("find").queryParam("q", query.getQuery()).queryParam("corpora",
                Joiner.on(",").join(query.getCorpora()));

        File matchesGraphANNISFile = File.createTempFile("annis-migrate-url-shortener-graphannis", ".txt");
        matchesGraphANNISFile.deleteOnExit();

        // write all graphANNIS matches to temporary file
        try (BufferedOutputStream fileOutput = new BufferedOutputStream(new FileOutputStream(matchesGraphANNISFile))) {
            queryDao.find(query.getQuery(), query.getQueryLanguage(), new LinkedList<>(query.getCorpora()),
                    new LimitOffsetQueryData(0, Integer.MAX_VALUE), fileOutput);
        }

        // read in the file again line by line and compare it with the legacy ANNIS
        // version
        int matchNr = 0;
        try (BufferedReader matchesGraphANNIS = new BufferedReader(new FileReader(matchesGraphANNISFile));
                BufferedReader matchesLegacy = new BufferedReader(
                        new InputStreamReader(findTarget.request(MediaType.TEXT_PLAIN_TYPE).get(InputStream.class)))) {
            // compare each line
            String m1;
            String m2;
            while ((m1 = matchesGraphANNIS.readLine()) != null && (m2 = matchesLegacy.readLine()) != null) {
                matchNr++;
                
                Match parsed_m1 = Match.parseFromString(m1);
                Match parsed_m2 = Match.parseFromString(m2);

                if (!parsed_m1.equals(parsed_m2)) {
                    this.errorMsg = "Match " + matchNr + " (should be)" + System.lineSeparator() + m2
                            + System.lineSeparator() + "(but was)" + System.lineSeparator() + m1;
                    return QueryStatus.MatchesDiffer;
                }
            }
        } finally {
            if (!matchesGraphANNISFile.delete()) {
                log.warn("Could not delete temporary file {}", matchesGraphANNISFile.getAbsolutePath());
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
        } catch (AnnisTimeoutException ex) {
            this.errorMsg = "Timeout in graphANNIS";
            return QueryStatus.Failed;
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

            } else if (countGraphANNIS == 0) {
                status = QueryStatus.Ok;
            } else {
                status = testFind(queryDao, annisSearchService);
                if (status == QueryStatus.Failed) {
                    // don't try quirks mode when failed
                    return status;
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

        } catch (ForbiddenException | AnnisTimeoutException | IOException ex) {
            this.errorMsg = ex.toString();
            return QueryStatus.Failed;
        }
    }
}
