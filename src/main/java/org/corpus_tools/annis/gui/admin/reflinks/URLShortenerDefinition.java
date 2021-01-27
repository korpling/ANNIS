package org.corpus_tools.annis.gui.admin.reflinks;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Joiner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.CountExtra;
import org.corpus_tools.annis.api.model.CountQuery;
import org.corpus_tools.annis.api.model.FindQuery;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.QueryGenerator;
import org.corpus_tools.annis.gui.objects.DisplayedResultQuery;
import org.corpus_tools.annis.gui.objects.Match;
import org.corpus_tools.annis.gui.objects.Query;
import org.corpus_tools.annis.gui.objects.QueryLanguage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

public class URLShortenerDefinition {

  private static final Logger log = LoggerFactory.getLogger(URLShortenerDefinition.class);
  private static final int MAX_RETRY = 5;


  private URI uri;
  private DisplayedResultQuery query;
  private UUID uuid;
  private DateTime creationTime;
  private Set<String> unknownCorpora = new LinkedHashSet<>();

  private String errorMsg;

  private final XmlMapper mapper;

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
    this.mapper = new XmlMapper();
    this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
      throws URISyntaxException {

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

      return QueryGenerator.displayed().left(Integer.parseInt(args.getOrDefault("cl", "0")))
          .right(Integer.parseInt(args.getOrDefault("cr", "0")))
          .offset(Integer.parseInt(args.getOrDefault("s", "0")))
          .limit(Integer.parseInt(args.getOrDefault("l", "0"))).segmentation(args.get("seg"))
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

  private QueryStatus testFind(SearchApi searchApi, OkHttpClient client,
      HttpUrl annisSearchServiceBaseUrl) throws IOException, ApiException {

    // Create a file with the matches according to the new graphANNIS based implementation
    File matchesGraphANNISFile = searchApi.find(
        new FindQuery().query(query.getQuery()).corpora(new LinkedList<>(query.getCorpora()))
            .queryLanguage(query.getApiQueryLanguage()));


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

        Match m1Parsed = Match.parseFromString(m1);
        Match m2Parsed = Match.parseFromString(m2);

        if (!Objects.equals(m1Parsed, m2Parsed)) {
          this.errorMsg = "Match " + matchNr + " (should be)" + System.lineSeparator() + m2
              + System.lineSeparator() + "(but was)" + System.lineSeparator() + m1;
          return QueryStatus.MATCHES_DIFFER;
        }
      }
    } finally {
      Files.delete(matchesGraphANNISFile.toPath());
    }

    return QueryStatus.OK;
  }


  private int getLegacyCount(OkHttpClient client, HttpUrl annisSearchServiceBaseUrl)
      throws IOException {
    for (int tries = 0; tries < MAX_RETRY; tries++) {
        HttpUrl countLegacyUrl = annisSearchServiceBaseUrl.newBuilder().addPathSegment("count")
            .addQueryParameter("q", query.getQuery())
            .addQueryParameter("corpora", Joiner.on(",").join(query.getCorpora())).build();
        try(Response countResponse =
            client.newCall(new Request.Builder().url(countLegacyUrl).build()).execute()) {

          int responseCode = countResponse.code();
          if (responseCode == 200) {
            String bodyString = countResponse.body().string();
            CountExtra result = mapper.readValue(bodyString, CountExtra.class);
            return result.getMatchCount();
          } else if (responseCode == 400) {
            // "Bad request" means there was a syntactic or semantic error.
            // Non-existing annotation names where not always handled as semantic error, so
            // reference links might exist.
            // We translate this error to "no result" instead of throwing an error.
            return 0;
          } else if (responseCode == 504) {
            // The legacy database query time-outs
            throw new IOException("Timeout in legacy ANNIS service");
          }
      } catch (IOException ex) {
        if (tries >= MAX_RETRY - 1) {
          // Rethrow server error so it can be properly processed by the calling function
          throw (ex);
        } else {
          log.warn("Server error when executing query {}", query.getQuery(), ex);
        }
      }

    }
    return 0;
  }

  private QueryStatus testQuirksMode(SearchApi searchApi, OkHttpClient client,
      HttpUrl annisSearchServiceBaseUrl) {
    URLShortenerDefinition quirksQuery = this.rewriteInQuirksMode();
    QueryStatus quirksStatus = quirksQuery.test(searchApi, client, annisSearchServiceBaseUrl);
    if (quirksStatus == QueryStatus.OK) {
      this.query = quirksQuery.query;
      this.uri = quirksQuery.uri;
      this.errorMsg = "Rewrite in quirks mode necessary";
      return QueryStatus.OK;
    } else {
      this.errorMsg = quirksQuery.getErrorMsg();
      return quirksStatus;
    }
  }

  public QueryStatus test(SearchApi searchApi, OkHttpClient client,
      HttpUrl annisSearchServiceBaseUrl) {

    if (this.query.getCorpora().isEmpty()) {
      this.errorMsg = "Empty corpus list";
      return QueryStatus.EMPTY_CORPUS_LIST;
    }

    try {

      // check count first (also warmup for the corpus)
      QueryStatus status;
      try {
        int countLegacy = getLegacyCount(client, annisSearchServiceBaseUrl);

        int countGraphANNIS = searchApi.count(
            new CountQuery().query(query.getQuery()).queryLanguage(query.getApiQueryLanguage())
                .corpora(new LinkedList<>(query.getCorpora())))
            .getMatchCount();
        if (countGraphANNIS != countLegacy) {
          this.errorMsg = "should have been " + countLegacy + " but was " + countGraphANNIS;
          status = QueryStatus.COUNT_DIFFERS;
        } else if (countGraphANNIS == 0) {
          status = QueryStatus.OK;
        } else {
          status = testFind(searchApi, client, annisSearchServiceBaseUrl);
        }

      } catch (ApiException ex) {
        if (ex.getCode() == 400 && this.query.getQueryLanguage() == QueryLanguage.AQL) {
          // Bad requests means the query was invalid, try again with quirks mode
          status = QueryStatus.SERVER_ERROR;
          this.errorMsg = ex.toString();
        } else {
          // Non-recoverable exception
          throw ex;
        }
      }


      if (status != QueryStatus.OK && this.query.getQueryLanguage() == QueryLanguage.AQL) {
        // check in quirks mode and rewrite if necessary
        status = testQuirksMode(searchApi, client, annisSearchServiceBaseUrl);
      }

      return status;
    } catch (ApiException ex) {
      if (ex.getCode() == 408 || ex.getCode() == 504) {
        this.errorMsg = "Timeout in graphANNIS";
        return QueryStatus.TIMEOUT;
      } else {
        this.errorMsg = ex.toString();
        return QueryStatus.SERVER_ERROR;
      }
    } catch (IOException ex) {
      this.errorMsg = ex.toString();
      return QueryStatus.SERVER_ERROR;
    }
  }
}
