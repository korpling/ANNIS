/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.libgui;

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;

import annis.CommonHelper;
import annis.model.Annotation;
import annis.provider.SaltProjectProvider;
import annis.service.objects.CorpusConfig;
import annis.service.objects.CorpusConfigMap;
import annis.service.objects.DocumentBrowserConfig;
import annis.service.objects.RawTextWrapper;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.vaadin.server.JsonCodec;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import elemental.json.JsonValue;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import javax.ws.rs.core.UriBuilder;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class Helper {

  /**
   * Casts a list of Annotations to the Type <code>{@literal List<Annotation>}</code>
   */
  public static class AnnotationListType extends GenericType<List<Annotation>> {

    public AnnotationListType() {}
  }

  /**
   * Marks all nodes which are dominated by already marked nodes.
   *
   * 1. Sort ascending all initial marked nodes by the size of the intervall between left and right
   * token index.
   *
   * 2. Traverse the salt document graph with the sorted list of step 1. as root nodes and mark all
   * children with the same match position. Already marked nodes are omitted.
   *
   * Note: The algorithm prevents nested marked nodes to be overwritten. Nested nodes must have a
   * smaller intervall from left to right by default, so this should always work.
   *
   */
  public static class CoveredMatchesCalculator implements GraphTraverseHandler {

    private final Map<SNode, Long> matchedAndCovered;
    private final Map<SToken, Integer> token2index;
    private final SDocumentGraph graph;

    private final Comparator<SNode> comp = new Comparator<SNode>() {

      @Override
      public int compare(SNode o1, SNode o2) {
        // generate several helper variables we want to compare
        Range<Integer> range1 = Helper.getLeftRightSpan(o1, graph, token2index);
        Range<Integer> range2 = Helper.getLeftRightSpan(o2, graph, token2index);

        int leftTokIdxO1 = range1.lowerEndpoint();
        int rightTokIdxO1 = range1.upperEndpoint();
        int leftTokIdxO2 = range2.lowerEndpoint();
        int rightTokIdxO2 = range2.upperEndpoint();

        int intervallO1 = Math.abs(leftTokIdxO1 - rightTokIdxO1);
        int intervallO2 = Math.abs(leftTokIdxO2 - rightTokIdxO2);

        SFeature featMatch1 = o1.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
        SFeature featMatch2 = o2.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
        long matchNode1 = featMatch1 == null ? Long.MAX_VALUE : featMatch1.getValue_SNUMERIC();
        long matchNode2 = featMatch2 == null ? Long.MAX_VALUE : featMatch2.getValue_SNUMERIC();

        SFeature o1_feat_id = o1.getFeature(ANNIS_NS, "node_id");
        SFeature o2_feat_id = o2.getFeature(ANNIS_NS, "node_id");
        long o1_internal_id = o1_feat_id == null ? 0 : o1_feat_id.getValue_SNUMERIC().longValue();
        long o2_internal_id = o2_feat_id == null ? 0 : o2_feat_id.getValue_SNUMERIC().longValue();

        // use a comparison chain which is much less verbose and better readable
        return ComparisonChain.start().compare(intervallO1, intervallO2)
            .compare(range1.lowerEndpoint(), range2.lowerEndpoint())
            .compare(range1.upperEndpoint(), range2.upperEndpoint()).compare(matchNode1, matchNode2)
            .compare(o1_internal_id, o2_internal_id).result();

      }
    };

    public CoveredMatchesCalculator(SDocumentGraph graph, Map<SNode, Long> initialMatches,
        Map<SToken, Integer> token2index) {
      this.graph = graph;
      this.matchedAndCovered = initialMatches;
      this.token2index = token2index;

      Map<SNode, Long> sortedMatchedNodes = new TreeMap<>(comp);

      for (Map.Entry<SNode, Long> entry : initialMatches.entrySet()) {
        SNode n = entry.getKey();
        sortedMatchedNodes.put(n, entry.getValue());
      }

      if (initialMatches.size() > 0) {
        graph.traverse(new ArrayList<>(sortedMatchedNodes.keySet()),
            GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "CoveredMatchesCalculator", this, true);
      }
    }

    @Override
    public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
        SRelation edge, SNode currNode, long order) {
      if (edge == null || edge instanceof SDominanceRelation || edge instanceof SSpanningRelation) {
        return true;
      } else {
        return false;
      }
    }

    public Map<SNode, Long> getMatchedAndCovered() {
      return matchedAndCovered;
    }

    @Override
    public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        SRelation edge, SNode fromNode, long order) {}

    @Override
    public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
        @SuppressWarnings("rawtypes") SRelation edge, SNode fromNode, long order) {
      if (fromNode != null && matchedAndCovered.containsKey(fromNode) && currNode != null) {
        long currentMatchPos = matchedAndCovered.get(fromNode);

        // only update the map when there is no entry yet or if the new index/position
        // is smaller
        Long oldMatchPos = matchedAndCovered.get(currNode);
        if (oldMatchPos == null) {
          matchedAndCovered.put(currNode, currentMatchPos);
        }
      }

    }
  }

  private final static UIConfig cfg = ConfigFactory.create(UIConfig.class);

  public final static String DEFAULT_CONFIG = "default-config";

  // the name of the web font class, the css class contains !important.
  public final static String CORPUS_FONT_FORCE = "corpus-font-force";

  // the name of the web font class.
  public final static String CORPUS_FONT = "corpus-font";

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(Helper.class);

  private static final ThreadLocal<Client> anonymousClient = new ThreadLocal<>();

  private static final String ERROR_MESSAGE_CORPUS_PROPS_HEADER =
      "Corpus properties does not exist";

  private static final String ERROR_MESSAGE_CORPUS_PROPS =
      "<div><p><strong>ANNIS can not access the corpus properties</strong></p>"
          + "<h2>possible reasons are:</h2>" + "<ul>" + "<li>the ANNIS service is not running</li>"
          + "<li>the corpus properties are not well defined</li></ul>"
          + "<p>Please ask the responsible admin or consult the ANNIS "
          + "<a href=\"http://korpling.github.io/ANNIS/doc/\">Documentation</a>.</p></div>";

  private static final String ERROR_MESSAGE_DOCUMENT_BROWSER_HEADER =
      "Problems with parsing the document browser configuration.";

  private static final String ERROR_MESSAGE_DOCUMENT_BROWSER_BODY =
      "<div><p>Maybe there is a syntax error in the json file.</p></div>";

  private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();

  private final static Escaper jerseyExtraEscape =
      Escapers.builder().addEscape('{', "%7B").addEscape('}', "%7D").addEscape('%', "%25").build();

  public static Map<SNode, Long> calculateMarkedAndCovered(SDocument doc, List<SNode> segNodes,
      String segmentationName) {
    Map<SNode, Long> initialCovered = new HashMap<>();

    // add all covered nodes
    for (SNode n : doc.getDocumentGraph().getNodes()) {
      SFeature featMatched = n.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
      Long match = featMatched == null ? null : featMatched.getValue_SNUMERIC();

      if (match != null) {
        initialCovered.put(n, match);
      }
    }
    final Map<SToken, Integer> token2index =
        Helper.createToken2IndexMap(doc.getDocumentGraph(), null);

    // calculate covered nodes
    CoveredMatchesCalculator cmc =
        new CoveredMatchesCalculator(doc.getDocumentGraph(), initialCovered, token2index);
    Map<SNode, Long> covered = cmc.getMatchedAndCovered();

    if (segmentationName != null) {
      // filter token
      Map<SToken, Long> coveredToken = new HashMap<>();
      for (Map.Entry<SNode, Long> e : covered.entrySet()) {
        SNode n = e.getKey();
        if (n instanceof SToken) {
          coveredToken.put((SToken) n, e.getValue());
        }
      }

      for (SNode segNode : segNodes) {
        if (!covered.containsKey(segNode)) {

          Range<Integer> segRange =
              Helper.getLeftRightSpan(segNode, doc.getDocumentGraph(), token2index);
          int leftTok = segRange.lowerEndpoint();
          int rightTok = segRange.upperEndpoint();

          // check for each covered token if this segment is covering it
          for (Map.Entry<SToken, Long> e : coveredToken.entrySet()) {
            Range<Integer> tokRange =
                Helper.getLeftRightSpan(e.getKey(), doc.getDocumentGraph(), token2index);
            long entryTokenIndex = tokRange.lowerEndpoint();
            if (entryTokenIndex <= rightTok && entryTokenIndex >= leftTok) {
              // add this segmentation node to the covered set
              covered.put(segNode, e.getValue());
              break;
            }
          } // end for each covered token
        } // end if not already contained
      } // end for each segmentation node
    }

    return covered;
  }

  /**
   * Returns a formatted string containing the type of the exception, the message and the
   * stacktrace.
   *
   * @param ex Exception
   * @return message
   */
  public static String convertExceptionToMessage(Throwable ex) {
    StringBuilder sb = new StringBuilder();
    if (ex != null) {
      sb.append("Exception type: ").append(ex.getClass().getName()).append("\n");
      sb.append("Message: ").append(ex.getLocalizedMessage()).append("\n");
      sb.append("Stacktrace: \n");
      StackTraceElement[] st = ex.getStackTrace();
      for (int i = 0; i < st.length; i++) {
        sb.append(st[i].toString());
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  /**
   * Create a REST web service client which is not authenticated.
   *
   * @return A newly created client.
   */
  public static Client createRESTClient() {
    return createRESTClient(null, null);
  }

  /**
   * Creates an authenticated REST client
   *
   * @param userName user name to authenticate with
   * @param password password to authenticate with
   * @return A newly created client.
   */
  public static Client createRESTClient(String userName, String password) {

    DefaultApacheHttpClient4Config rc = new DefaultApacheHttpClient4Config();
    rc.getClasses().add(SaltProjectProvider.class);

    ThreadSafeClientConnManager clientConnMgr = new ThreadSafeClientConnManager();
    clientConnMgr.setDefaultMaxPerRoute(10);
    rc.getProperties().put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, clientConnMgr);

    if (userName != null && password != null) {
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY,
          new UsernamePasswordCredentials(userName, password));

      rc.getProperties().put(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER,
          credentialsProvider);
      rc.getProperties().put(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION,
          true);

    }

    Client c = ApacheHttpClient4.create(rc);
    return c;
  }

  public static Map<SToken, Integer> createToken2IndexMap(final SDocumentGraph graph,
      final STextualDS textualDS) {
    final Map<SToken, Integer> token2index = new LinkedHashMap<>();

    if (graph == null) {
      return token2index;
    }

    Multimap<String, SNode> orderRootsByType =
        graph.getRootsByRelationType(SALT_TYPE.SORDER_RELATION);
    Set<SNode> orderRoots = new HashSet<>(orderRootsByType.get(""));
    List<SToken> sortedTokens;

    if (textualDS == null) {
      // get tokens of all texts
      sortedTokens = graph.getSortedTokenByText();
    } else {
      final DataSourceSequence<Number> seq = new DataSourceSequence<>();
      seq.setDataSource(textualDS);
      seq.setStart(0);
      seq.setEnd(textualDS.getText() != null ? textualDS.getText().length() : 0);
      sortedTokens = graph.getSortedTokenByText(graph.getTokensBySequence(seq));
    }

    if (sortedTokens != null) {
      int i = 0;
      for (final SToken t : sortedTokens) {
        if (i > 0 && orderRoots.contains(t)) {
          // introduce a gap because the token stream is not complete
          i += 1;
        }
        token2index.put(t, i++);
      }
    }

    return token2index;
  }

  public static <T> JsonValue encodeGeneric(Object v) {
    return JsonCodec.encode(v, null, v.getClass().getGenericSuperclass(), null).getEncodedValue();
  }

  /**
   * This will percent encode Jersey template argument braces (enclosed in "{...}") and the percent
   * character. Both would not be esccaped by jersey and/or would cause an error when this is not a
   * valid template.
   *
   * @param v the value
   * @return encoded value
   */
  public static String encodeJersey(String v) {
    String encoded = jerseyExtraEscape.escape(v);
    return encoded;
  }

  /**
   * Encodes a String so it can be used as path parameter.
   *
   * @param v value
   * @return encoded value
   */
  public static String encodePath(String v) {
    String encoded = urlPathEscape.escape(v);
    return encoded;
  }

  /**
   * Encodes a String so it can be used as query parameter.
   *
   * @param v value
   * @return encoded value
   */
  public static String encodeQueryParam(String v) {
    String encoded = UrlEscapers.urlFormParameterEscaper().escape(v);
    return encoded;
  }

  public static String generateCorpusLink(Set<String> corpora) {
    try {
      URI appURI = UI.getCurrent().getPage().getLocation();

      String fragment = "_c=" + CommonHelper.encodeBase64URL(StringUtils.join(corpora, ","));

      return new URI(appURI.getScheme(), null, appURI.getHost(), appURI.getPort(), appURI.getPath(),
          null, fragment).toASCIIString();
    } catch (URISyntaxException ex) {
      log.error(null, ex);
    }
    return "ERROR";
  }

  /**
   * Gets or creates an asynchronous web resource to the ANNIS service.
   *
   * @param uri The URI where the service can be found
   * @param user The user object or null (should be of type {@link AnnisUser}).
   * @return A reference to the ANNIS service root resource.
   */
  public static AsyncWebResource getAnnisAsyncWebResource(String uri, AnnisUser user) {

    if (user != null) {
      try {
        return user.getClient().asyncResource(uri);
      } catch (LoginDataLostException ex) {
        log.error("Could not restore the login-data from session, user will invalidated", ex);
        setUser(null);
        UI ui = UI.getCurrent();
        if (ui instanceof AnnisBaseUI) {
          ((AnnisBaseUI) ui).getLoginDataLostBus().post(ex);
        }
      }
    }

    // use the anonymous client
    if (anonymousClient.get() == null) {
      // anonymous client not created yet
      anonymousClient.set(createRESTClient());
    }

    return anonymousClient.get().asyncResource(uri);
  }

  /**
   * Gets or creates an asynchronous web resource to the ANNIS service.
   *
   * This is a convenience wrapper to
   * {@link #getAnnisWebResource(java.lang.String, annis.security.AnnisUser) } that does not need
   * any arguments
   *
   * @return A reference to the ANNIS service root resource.
   */
  public static AsyncWebResource getAnnisAsyncWebResource(UI ui) {
    // get URI used by the application
    String uri = getServiceURL(ui.getSession());

    // if already authentificated the REST client is set as the "user" property
    AnnisUser user = getUser(ui);

    return getAnnisAsyncWebResource(uri, user);
  }

  /**
   * Gets or creates a web resource to the ANNIS service.
   *
   * @param uri The URI where the service can be found
   * @param user The user object or null (should be of type {@link AnnisUser}).
   * @return A reference to the ANNIS service root resource.
   */
  public static WebResource getAnnisWebResource(String uri, AnnisUser user) {

    if (user != null) {
      try {
        return user.getClient().resource(uri);
      } catch (LoginDataLostException ex) {
        log.error("Could not restore the login-data from session, user will invalidated", ex);
        setUser(null);
        UI ui = UI.getCurrent();
        if (ui instanceof AnnisBaseUI) {
          ((AnnisBaseUI) ui).getLoginDataLostBus().post(ex);
        }
      }
    }

    // use the anonymous client
    if (anonymousClient.get() == null) {
      // anonymous client not created yet
      anonymousClient.set(createRESTClient());
    }

    return anonymousClient.get().resource(uri);
  }

  /**
   * Gets or creates a web resource to the ANNIS service.
   *
   * This is a convenience wrapper to
   * {@link #getAnnisWebResource(java.lang.String, annis.security.AnnisUser) } that does not need
   * any arguments
   *
   * @return A reference to the ANNIS service root resource.
   */
  public static WebResource getAnnisWebResource(UI ui) {

    if (ui != null) {
      VaadinSession vSession = ui.getSession();

      return getAnnisWebResource(vSession);
    } else {
      return null;
    }
  }

  public static WebResource getAnnisWebResource(VaadinSession vSession) {

    // get URI used by the application
    String uri = getServiceURL(vSession);

    // if already authentificated the REST client is set as the "user" property
    AnnisUser user = getUser(vSession);

    return getAnnisWebResource(uri, user);

  }

  public static String getContext(UI ui) {
    if (VaadinService.getCurrentRequest() != null) {
      return VaadinService.getCurrentRequest().getContextPath();
    } else {
      return (String) ui.getSession().getAttribute(AnnisBaseUI.CONTEXT_PATH);
    }

  }

  /**
   * Loads the corpus config of a specific corpus.
   *
   * @param corpus The name of the corpus, for which the config is fetched.
   * @return A {@link CorpusConfig} object, which wraps a {@link Properties} object. This Properties
   *         object stores the corpus configuration as simple key-value pairs.
   */
  public static CorpusConfig getCorpusConfig(String corpus, UI ui) {

    if (corpus == null || corpus.isEmpty()) {
      Notification.show("no corpus is selected",
          "please select at leas one corpus and execute query again",
          Notification.Type.WARNING_MESSAGE);
      return null;
    }

    CorpusConfig corpusConfig = new CorpusConfig();

    try {
      corpusConfig = Helper.getAnnisWebResource(ui).path("query").path("corpora")
          .path(urlPathEscape.escape(corpus)).path("config").get(CorpusConfig.class);
    } catch (UniformInterfaceException | ClientHandlerException ex) {
      if (!AnnisBaseUI.handleCommonError(ex, "get corpus configuration")) {
        new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER, ERROR_MESSAGE_CORPUS_PROPS,
            Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
      }
    }

    return corpusConfig;
  }

  /**
   * Loads the available corpus configurations for a list of specific corpora.
   *
   *
   * @param corpora A Set of corpora names.
   * @return A {@link CorpusConfigMap} object, which wraps a Map of {@link Properties} objects. The
   *         keys to the properties are the corpus names. A Properties object stores the corpus
   *         configuration as simple key-value pairs. The map includes the default configuration.
   */
  public static CorpusConfigMap getCorpusConfigs(Set<String> corpora, UI ui) {

    CorpusConfigMap corpusConfigurations = new CorpusConfigMap();

    for (String corpus : corpora) {
      corpusConfigurations.put(corpus, getCorpusConfig(corpus, ui));
    }

    corpusConfigurations.put(DEFAULT_CONFIG, getDefaultCorpusConfig(ui));
    return corpusConfigurations;
  }

  /**
   * Loads the all available corpus configurations.
   *
   *
   * @return A {@link CorpusConfigMap} object, which wraps a Map of {@link Properties} objects. The
   *         keys to the properties are the corpus names. A Properties object stores the corpus
   *         configuration as simple key-value pairs. The Map includes also the default corpus
   *         configuration.
   */
  public static CorpusConfigMap getCorpusConfigs(UI ui) {

    CorpusConfigMap corpusConfigurations = null;

    try {
      corpusConfigurations = Helper.getAnnisWebResource(ui).path("query").path("corpora")
          .path("config").get(CorpusConfigMap.class);
    } catch (UniformInterfaceException | ClientHandlerException ex) {
      UI.getCurrent().access(() -> {
        if (!AnnisBaseUI.handleCommonError(ex, "get corpus configurations")) {
          new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER, ERROR_MESSAGE_CORPUS_PROPS,
              Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
        }
      });
    }

    if (corpusConfigurations == null) {
      corpusConfigurations = new CorpusConfigMap();
    }

    corpusConfigurations.put(DEFAULT_CONFIG, getDefaultCorpusConfig(ui));

    return corpusConfigurations;
  }

  public static CorpusConfig getDefaultCorpusConfig(UI ui) {

    CorpusConfig defaultCorpusConfig = new CorpusConfig();

    try {
      defaultCorpusConfig = Helper.getAnnisWebResource(ui).path("query").path("corpora")
          .path(DEFAULT_CONFIG).get(CorpusConfig.class);
    } catch (UniformInterfaceException | ClientHandlerException ex) {
      if (!AnnisBaseUI.handleCommonError(ex, "get default corpus configuration")) {
        new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER, ERROR_MESSAGE_CORPUS_PROPS,
            Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
      }
    }

    return defaultCorpusConfig;
  }

  public static DocumentBrowserConfig getDocBrowserConfig(String corpus, UI ui) {
    try {
      DocumentBrowserConfig docBrowserConfig =
          Helper.getAnnisWebResource(ui).path("query").path("corpora").path("doc_browser_config")
              .path(urlPathEscape.escape(corpus)).get(DocumentBrowserConfig.class);

      return docBrowserConfig;
    } catch (UniformInterfaceException | ClientHandlerException ex) {
      log.error("problems with fetching document browsing", ex);
      if (!AnnisBaseUI.handleCommonError(ex, "get document browser configuration")) {
        new Notification(ERROR_MESSAGE_DOCUMENT_BROWSER_HEADER, ERROR_MESSAGE_DOCUMENT_BROWSER_BODY,
            Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
      }
    }

    return null;
  }

  public static Range<Integer> getLeftRightSpan(SNode node, SDocumentGraph graph,
      Map<SToken, Integer> token2index) {
    int left = Integer.MAX_VALUE;
    int right = Integer.MIN_VALUE;
    if (node instanceof SToken) {
      left = Math.min(left, token2index.get(node));
      right = Math.max(right, token2index.get(node));
    } else {
      List<SToken> overlappedToken = graph.getOverlappedTokens(node);
      for (SToken t : overlappedToken) {
        left = Math.min(left, token2index.get(t));
        right = Math.max(right, token2index.get(t));
      }
    }

    return Range.closed(left, right);
  }

  /**
   * Retrieve all the meta data for a given document of a corpus including the metadata of all
   * corora in the path to the document.
   *
   * @param toplevelCorpusName Specifies the the toplevel corpus
   * @param documentName Specifies the document
   * @return Returns also the metada of the all parent corpora. There must be at least one of them.
   */
  public static List<Annotation> getMetaData(String toplevelCorpusName, String documentName,
      UI ui) {
    List<Annotation> result = new ArrayList<Annotation>();
    WebResource res = Helper.getAnnisWebResource(ui);
    try {
      res = res.path("meta").path("doc").path(urlPathEscape.escape(toplevelCorpusName));

      if (documentName != null) {
        res = res.path(urlPathEscape.escape(documentName));
      }

      if (documentName != null && !toplevelCorpusName.equals(documentName)) {
        res = res.path("path");
      }

      result = res.get(new GenericType<List<Annotation>>() {});
    } catch (UniformInterfaceException | ClientHandlerException ex) {
      log.error(null, ex);
      if (!AnnisBaseUI.handleCommonError(ex, "retrieve metada")) {
        Notification.show("Remote exception: " + ex.getLocalizedMessage(),
            Notification.Type.WARNING_MESSAGE);
      }
    }
    return result;
  }

  /**
   * Retrieve the meta data for a given document of a corpus.
   *
   * @param toplevelCorpusName specifies the toplevel corpus
   * @param documentName specifies the document.
   * @return returns only the meta data for a single document.
   */
  public static List<Annotation> getMetaDataDoc(String toplevelCorpusName, String documentName,
      UI ui) {
    List<Annotation> result = new ArrayList<Annotation>();
    WebResource res = Helper.getAnnisWebResource(ui);
    try {
      res = res.path("meta").path("doc").path(urlPathEscape.escape(toplevelCorpusName));
      res = res.path(urlPathEscape.escape(documentName));

      result = res.get(new GenericType<List<Annotation>>() {});
    } catch (UniformInterfaceException | ClientHandlerException ex) {
      log.error(null, ex);
      if (!AnnisBaseUI.handleCommonError(ex, "retrieve metadata")) {
        Notification.show("Remote exception: " + ex.getLocalizedMessage(),
            Notification.Type.WARNING_MESSAGE);
      }
    }
    return result;
  }

  /**
   * Get the qualified name separated by a single ":" when a namespace exists.
   *
   * @param anno annotation
   * @return qualified name
   */
  public static String getQualifiedName(SAnnotation anno) {
    if (anno != null) {
      if (anno.getNamespace() == null || anno.getNamespace().isEmpty()) {
        return anno.getName();
      } else {
        return anno.getNamespace() + ":" + anno.getName();
      }
    }
    return "";
  }

  public static RawTextWrapper getRawText(String corpusName, String documentName, UI ui) {
    RawTextWrapper texts = null;
    try {
      WebResource webResource = getAnnisWebResource(ui);
      webResource = webResource.path("query").path("rawtext").path(corpusName).path(documentName);
      texts = webResource.get(RawTextWrapper.class);
    }

    catch (UniformInterfaceException | ClientHandlerException ex) {
      log.error("can not retrieve raw text");
      if (!AnnisBaseUI.handleCommonError(ex, "retrieve raw text")) {
        Notification.show("can not retrieve raw text", ex.getLocalizedMessage(),
            Notification.Type.WARNING_MESSAGE);
      }
    }

    return texts;
  }

  public static String getServiceURL(VaadinSession session) {
    if (session != null) {
      String overriddenByInit =
          session.getConfiguration().getInitParameters().getProperty("AnnisWebService.URL");;
      if (overriddenByInit != null) {
        return overriddenByInit;
      }
    }
    return cfg.webserviceURL();

  }

  public static AnnisUser getUser(UI ui) {

    if (ui != null) {
      VaadinSession vSession = ui.getSession();
      return getUser(vSession);
    }
    return null;
  }

  public static AnnisUser getUser(VaadinSession vSession) {

    WrappedSession wrappedSession = null;

    if (vSession != null) {
      wrappedSession = vSession.getSession();
    }

    if (wrappedSession != null) {

      Object o = wrappedSession.getAttribute(AnnisBaseUI.USER_KEY);
      if (o != null && o instanceof AnnisUser) {
        return (AnnisUser) o;
      }
    }
    return null;
  }

  public static boolean isKickstarter(VaadinSession session) {
    if (session != null) {
      return Boolean.parseBoolean(session.getConfiguration().getInitParameters()
          .getProperty("kickstarterEnvironment", "false"));
    } else {
      return false;
    }
  }

  /**
   * Returns true if the right-to-left heuristic should be disabled.
   *
   * @return True if RTL is disabled
   */
  public static boolean isRTLDisabled(UI ui) {
    String disableRtl = (String) ui.getSession().getAttribute("disable-rtl");
    return "true".equalsIgnoreCase(disableRtl);
  }

  public static void setUser(AnnisUser user) {
    if (user == null) {
      VaadinSession.getCurrent().getSession().removeAttribute(AnnisBaseUI.USER_KEY);
    } else {
      VaadinSession.getCurrent().getSession().setAttribute(AnnisBaseUI.USER_KEY, user);
    }
  }

  public static String shortenURL(URI original, UI ui) {
    WebResource res = Helper.getAnnisWebResource(ui).path("shortener");
    String appContext = Helper.getContext(ui);

    String path = original.getRawPath();
    if (path.startsWith(appContext)) {
      path = path.substring(appContext.length());
    }

    String localURL = path;
    if (original.getRawQuery() != null) {
      localURL = localURL + "?" + original.getRawQuery();
    }
    if (original.getRawFragment() != null) {
      localURL = localURL + "#" + original.getRawFragment();
    }

    String shortID = res.post(String.class, localURL);

    return UriBuilder.fromUri(original).replacePath(appContext + "/").replaceQuery("").fragment("")
        .queryParam("id", shortID).build().toASCIIString();

  }

}
