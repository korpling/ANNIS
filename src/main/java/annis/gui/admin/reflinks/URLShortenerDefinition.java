package annis.gui.admin.reflinks;

import annis.QueryGenerator;
import annis.libgui.Helper;
import annis.model.DisplayedResultQuery;
import annis.model.Query;
import annis.service.objects.Match;
import annis.service.objects.QueryLanguage;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Joiner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.CountExtra;
import org.corpus_tools.annis.api.model.CountQuery;
import org.corpus_tools.annis.api.model.FindQuery;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

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

  protected URLShortenerDefinition(URI uri, UUID uuid, DateTime creationTime,
      DisplayedResultQuery query) {
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

    DateTimeParser[] parsers = {DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSZZ").getParser(),
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZZ").getParser(),
        DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").getParser()};

    DateTimeFormatter dateFormatter =
        new DateTimeFormatterBuilder().append(null, parsers).toFormatter();
    return dateFormatter.parseDateTime(creationTime);
  }

  public static URLShortenerDefinition parse(String url, String uuid, String creationTime)
      throws URISyntaxException, UnsupportedEncodingException {

    URI parsedURI = new URI(url);

    URLShortenerDefinition result =
        new URLShortenerDefinition(parsedURI, parseUUID(uuid), parseCreationTime(creationTime));

    if (parsedURI.getPath().startsWith("/embeddedvis")) {

      for (NameValuePair arg : URLEncodedUtils.parse(parsedURI, StandardCharsets.UTF_8)) {
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
    Map<String, String> args = Helper.parseFragment(fragment);
    String corporaRaw = args.get("c");
    if (corporaRaw != null) {
      Set<String> corpora = new LinkedHashSet<>(Arrays.asList(corporaRaw.split("\\s*,\\s*")));
      corpora.remove("");

      return QueryGenerator.displayed().left(Integer.parseInt(args.get("cl")))
          .right(Integer.parseInt(args.get("cr"))).offset(Integer.parseInt(args.get("s")))
          .limit(Integer.parseInt(args.get("l"))).segmentation(args.get("seg"))
          .baseText(args.get("bt")).query(args.get("q")).corpora(corpora).build();
    }
    return null;
  }

  public URLShortenerDefinition rewriteInQuirksMode() {
    DisplayedResultQuery rewrittenQuery = new DisplayedResultQuery(this.query);
    rewrittenQuery.setQueryLanguage(QueryLanguage.AQL_QUIRKS_V3);

    UriComponentsBuilder rewrittenUri = UriComponentsBuilder.fromUri(this.uri);
    if (this.uri.getPath().startsWith("/embeddedvis")) {
      // we need to keep query parameters arguments, except for the one with the
      // linked query
      rewrittenUri.queryParam("embedded_interface", rewrittenQuery.toCitationFragment());
    } else {
      // just update the fragment, but leave everything else the same
      rewrittenUri.fragment(rewrittenQuery.toCitationFragment());
    }

    return new URLShortenerDefinition(rewrittenUri.build().toUri(), this.uuid, this.creationTime,
        rewrittenQuery);
  }

  public Query getQuery() {
    return query;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
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

  private QueryStatus testFind(SearchApi searchApi, OkHttpClient client,
      HttpUrl annisSearchServiceBaseUrl) throws IOException, ApiException {

    // Create a file with the matches according to the new graphANNIS based implementation
    File matchesGraphANNISFile = searchApi.find(
        new FindQuery().query(query.getQuery()).corpora(new LinkedList<>(query.getCorpora())));


    HttpUrl findUrl = annisSearchServiceBaseUrl.newBuilder().addPathSegment("find")
        .addQueryParameter("q", query.getQuery())
        .addQueryParameter("corpora", Joiner.on(",").join(query.getCorpora())).build();

    Request legacyFindRequest =
        new Request.Builder().url(findUrl).addHeader("Accept", "text/plain").build();

    // read in the file again line by line and compare it with the legacy ANNIS
    // version
    int matchNr = 0;
    try (
        BufferedReader matchesGraphANNIS =
            new BufferedReader(new FileReader(matchesGraphANNISFile));
        BufferedReader matchesLegacy =
            new BufferedReader(client.newCall(legacyFindRequest).execute().body().charStream())) {
      // compare each line
      String m1;
      String m2;
      while ((m1 = matchesGraphANNIS.readLine()) != null
          && (m2 = matchesLegacy.readLine()) != null) {
        matchNr++;

        Match parsed_m1 = Match.parseFromString(m1);
        Match parsed_m2 = Match.parseFromString(m2);

        if (!Objects.equals(parsed_m1, parsed_m2)) {
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

  public QueryStatus test(SearchApi searchApi, OkHttpClient client,
      HttpUrl annisSearchServiceBaseUrl) {

    if (this.query.getCorpora().isEmpty()) {
      this.errorMsg = "Empty corpus list";
      return QueryStatus.EmptyCorpusList;
    }

    // check count first (also warmup for the corpus)
    int countGraphANNIS;

    try {
      countGraphANNIS = searchApi
          .count(new CountQuery().query(query.getQuery()).queryLanguage(query.getApiQueryLanguage())
              .corpora(new LinkedList<>(query.getCorpora())))
          .getMatchCount();
    } catch (ApiException ex) {
      if (ex.getCode() == 408 || ex.getCode() == 504) {
        this.errorMsg = "Timeout in graphANNIS";
        return QueryStatus.Timeout;
      } else {
        countGraphANNIS = 0;
      }
    }

    XmlMapper mapper = new XmlMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {

      QueryStatus status = QueryStatus.Ok;

      Optional<Integer> countLegacy = Optional.empty();
      for (int tries = 0; tries < MAX_RETRY; tries++) {
        try {
          HttpUrl countLegacyUrl = annisSearchServiceBaseUrl.newBuilder().addPathSegment("count")
              .addQueryParameter("q", query.getQuery())
              .addQueryParameter("corpora", Joiner.on(",").join(query.getCorpora())).build();
          ResponseBody body =
              client.newCall(new Request.Builder().url(countLegacyUrl).build()).execute().body();
          String bodyString = body.string();
          CountExtra result = mapper.readValue(bodyString, CountExtra.class);
          countLegacy = Optional.of(result.getMatchCount());
          break;
        } catch (IOException ex) {
          if (tries >= MAX_RETRY - 1) {
            this.errorMsg = ex.getMessage();
            return QueryStatus.ServerError;
          } else {
            log.warn("Server error when executing query {}", query.getQuery(), ex);
          }
        }
      }

      if (countGraphANNIS != countLegacy.get()) {

        this.errorMsg = "should have been " + countLegacy.get() + " but was " + countGraphANNIS;
        status = QueryStatus.CountDiffers;

      } else if (countGraphANNIS == 0) {
        status = QueryStatus.Ok;
      } else {
        status = testFind(searchApi, client, annisSearchServiceBaseUrl);
      }

      if (status != QueryStatus.Ok && this.query.getQueryLanguage() == QueryLanguage.AQL) {
        // check in quirks mode and rewrite if necessary
        log.info("Trying quirks mode for query {} on corpus {}", this.query.getQuery().trim(),
            this.query.getCorpora());

        URLShortenerDefinition quirksQuery = this.rewriteInQuirksMode();
        QueryStatus quirksStatus = quirksQuery.test(searchApi, client, annisSearchServiceBaseUrl);
        if (quirksStatus == QueryStatus.Ok) {
          this.query = quirksQuery.query;
          this.uri = quirksQuery.uri;
          this.errorMsg = "Rewrite in quirks mode necessary";
          status = QueryStatus.Ok;
        } else {
          status = quirksStatus;
          this.errorMsg = quirksQuery.getErrorMsg();
        }
      }

      return status;
    } catch (ApiException ex) {
      this.errorMsg = ex.toString();
      if (ex.getCode() == 408 || ex.getCode() == 504) {
        return QueryStatus.Timeout;
      }
      return QueryStatus.ServerError;
    } catch (IOException ex) {
      this.errorMsg = ex.toString();
      return QueryStatus.ServerError;
    }
  }
}
