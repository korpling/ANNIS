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

import annis.gui.AnnisUI;
import annis.gui.UIConfig;
import annis.gui.graphml.CorpusGraphMapper;
import annis.model.AnnisConstants;
import annis.service.objects.Match;
import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.Client;
import com.vaadin.server.JsonCodec;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import elemental.json.JsonValue;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.api.model.AnnoKey;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.Component;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.CorpusConfigurationContext;
import org.corpus_tools.annis.api.model.CorpusConfigurationView;
import org.corpus_tools.annis.api.model.FindQuery;
import org.corpus_tools.annis.api.model.FindQuery.OrderEnum;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.auth.Authentication;
import org.corpus_tools.annis.auth.HttpBearerAuth;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Label;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class Helper {

  public static List<String> getCorpusPath(SCorpusGraph corpusGraph, SDocument doc) {
    final List<String> result = new LinkedList<String>();

    result.add(doc.getName());
    SCorpus c = corpusGraph.getCorpus(doc);
    List<SNode> cAsList = new ArrayList<>();
    cAsList.add(c);
    corpusGraph.traverse(cAsList, GRAPH_TRAVERSE_TYPE.BOTTOM_UP_DEPTH_FIRST, "getRootCorpora",
        new GraphTraverseHandler() {
          @Override
          public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
              SRelation edge, SNode currNode, long order) {
            return true;
          }

          @Override
          public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
              SNode currNode, SRelation edge, SNode fromNode, long order) {}

          @Override
          public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
              SNode currNode, SRelation edge, SNode fromNode, long order) {
            result.add(currNode.getName());
          }
        });
    return result;
  }

  public static List<String> getCorpusPath(String uri) {
    if (uri.startsWith("salt:/")) {
      uri = uri.substring("salt:/".length());
    }
    String rawPath = StringUtils.strip(uri, "/ \t");

    // split on raw path (so "/" in corpus names are still encoded)
    String[] path = rawPath.split("/");

    // decode every single part by itself
    ArrayList<String> result = new ArrayList<>(path.length);
    for (int i = 0; i < path.length; i++) {
      try {
        // in the last part, try to remove any possible fragment
        if (i == path.length - 1) {
          int fragmentStart = path[i].lastIndexOf('#');
          if (fragmentStart >= 0) {
            path[i] = path[i].substring(0, fragmentStart);
          }
        }
        result.add(URLDecoder.decode(path[i], "UTF-8"));
      } catch (UnsupportedEncodingException ex) {
        log.error(null, ex);
        // fallback
        result.add(path[i]);
      }
    }

    return result;
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


  public final static Escaper AQL_REGEX_VALUE_ESCAPER = Escapers.builder()
      // This is used by AQL to mark the end of the regular expressions
      .addEscape('/', "\\x2F")
      // The next ones are meta characters for the regex-syntax crate
      // (see its is_meta_character function)
      .addEscape('\\', "\\\\").addEscape('.', "\\.").addEscape('+', "\\+").addEscape('*', "\\*")
      .addEscape('?', "\\?").addEscape('(', "\\(").addEscape(')', "\\)").addEscape('|', "\\|")
      .addEscape('[', "\\[").addEscape('[', "\\]").addEscape('{', "\\{").addEscape('}', "\\}")
      .addEscape('^', "\\^").addEscape('$', "\\$").addEscape('#', "\\#").addEscape('&', "\\&")
      .addEscape('-', "\\-").addEscape('~', "\\~").build();


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

  public static ApiClient getClient(UI ui) {
    UIConfig config = null;
    if (ui instanceof AnnisUI) {
      config = ((AnnisUI) ui).getConfig();
    }
    return getClient(ui.getSession(), config);
  }

  public static ApiClient getClient(VaadinSession ui, UIConfig config) {
    ApiClient client = Configuration.getDefaultApiClient();
    if (config != null) {
      // Use the configuration to allow changing the path to the web-service
      client.setBasePath(config.getWebserviceURL());
    }
    AnnisUser user = Helper.getUser(ui);
    if (user != null && user.getToken() != null) {
      Authentication auth = client.getAuthentication("bearerAuth");
      if (auth instanceof HttpBearerAuth) {
        HttpBearerAuth bearerAuth = (HttpBearerAuth) auth;
        bearerAuth.setBearerToken(user.getToken());

        // TODO: get a new token if expired
      }
    }
    return client;
  }

  public static Set<AnnoKey> getMetaAnnotationNames(String corpus, UI ui) throws ApiException {
    CorporaApi api = new CorporaApi(getClient(ui));
    SearchApi search = new SearchApi(getClient(ui));

    final List<org.corpus_tools.annis.api.model.Annotation> nodeAnnos =
        api.nodeAnnotations(corpus, false, true).stream()
            .filter(a -> !Objects.equals(a.getKey().getNs(), "annis")
                && !Objects.equals(a.getKey().getName(), "tok"))
            .collect(Collectors.toList());

    final Set<AnnoKey> metaAnnos = new HashSet<>();
    // Check for each annotation if its actually a meta-annotation
    for (org.corpus_tools.annis.api.model.Annotation a : nodeAnnos) {
      FindQuery q = new FindQuery();
      q.setCorpora(Arrays.asList(corpus));
      q.setQuery("annis:node_type=\"corpus\" _ident_ " + getQName(a.getKey()));
      // Not sorting the results is much faster, especially if we only fetch the first
      // item
      // (we are only interested if a match exists, not how many items or which one)
      q.setOrder(OrderEnum.NOTSORTED);
      q.setLimit(1);
      q.setOffset(0);

      q.setQueryLanguage(QueryLanguage.AQL);
      File findResult = search.find(q);
      if (findResult != null && findResult.isFile())
        try {
          Optional<String> anyLine =
              Files.lines(findResult.toPath(), StandardCharsets.UTF_8).findAny();
          if (anyLine.isPresent() && !anyLine.get().isEmpty()) {
            metaAnnos.add(a.getKey());
          }
        } catch (IOException ex) {
          log.error("Error when accessing file with find results", ex);
        }
    }

    return metaAnnos;
  }


  public static String getQName(AnnoKey key) {
    if (key.getNs() == null || key.getNs().isEmpty()) {
      return key.getName();
    } else {
      return key.getNs() + ":" + key.getName();
    }
  }

  public static String getQName(Component c) {
    if (c.getLayer() == null || c.getLayer().isEmpty()) {
      return c.getName();
    } else {
      return c.getLayer() + ":" + c.getName();
    }
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

  public static String encodeBase64URL(String val) {
    try {
      return Base64.encodeBase64URLSafeString(val.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException ex) {
      log.error("Java Virtual Maschine can't handle UTF-8: I'm utterly confused", ex);
    }
    return "";
  }

  public static String generateCorpusLink(Set<String> corpora) {
    try {
      URI appURI = UI.getCurrent().getPage().getLocation();

      String fragment = "_c=" + encodeBase64URL(StringUtils.join(corpora, ","));

      return new URI(appURI.getScheme(), null, appURI.getHost(), appURI.getPort(), appURI.getPath(),
          null, fragment).toASCIIString();
    } catch (URISyntaxException ex) {
      log.error(null, ex);
    }
    return "ERROR";
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
  public static CorpusConfiguration getCorpusConfig(String corpus, UI ui) {

    if (corpus == null || corpus.isEmpty()) {
      Notification.show("no corpus is selected",
          "please select at least one corpus and execute query again",
          Notification.Type.WARNING_MESSAGE);
      return null;
    }

    CorpusConfiguration corpusConfig = new CorpusConfiguration();

    CorporaApi api = new CorporaApi(getClient(ui));

    try {
      corpusConfig = api.corpusConfiguration(corpus);
    } catch (ApiException ex) {
      if (!AnnisBaseUI.handleCommonError(ex, "get corpus configuration")) {
        new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER, ERROR_MESSAGE_CORPUS_PROPS,
            Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
      }
    }

    return corpusConfig;
  }


  public static CorpusConfiguration getDefaultCorpusConfig() {

    CorpusConfiguration defaultCorpusConfig = new CorpusConfiguration();

    defaultCorpusConfig.setView(new CorpusConfigurationView());
    defaultCorpusConfig.setContext(new CorpusConfigurationContext());
    defaultCorpusConfig.setExampleQueries(new LinkedList<>());
    defaultCorpusConfig.setVisualizers(new LinkedList<>());

    defaultCorpusConfig.getView().setPageSize(10);
    defaultCorpusConfig.getContext().setDefault(5);
    defaultCorpusConfig.getContext().setSizes(Arrays.asList(1, 2, 5, 10));
    defaultCorpusConfig.getContext().setMax(Integer.MAX_VALUE);

    return defaultCorpusConfig;
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
   * @param documentName Specifies the document or leave empty if only the corpus meta data should
   *        be fetched.
   * @return Returns also the metada of the all parent corpora. There must be at least one of them.
   */
  public static List<SMetaAnnotation> getMetaData(String toplevelCorpusName,
      Optional<String> documentName, UI ui) {
    List<SMetaAnnotation> result = new ArrayList<>();
    SearchApi api = new SearchApi(Helper.getClient(ui));

    try {

      // Get the corpus graph and with it the meta data on the corpus/document nodes
      String aql;
      if (documentName.isPresent()) {
        aql = "(annis:node_type=\"corpus\" _ident_ annis:doc=/"
            + AQL_REGEX_VALUE_ESCAPER.escape(documentName.get()) + "/)" + " |"
            + "(annis:node_type=\"corpus\" _ident_ annis:doc=/"
            + AQL_REGEX_VALUE_ESCAPER.escape(documentName.get())
            + "/ @* annis:node_type=\"corpus\")";
      } else {
        aql = "annis:node_type=\"corpus\" _ident_ annis:node_name=/"
            + AQL_REGEX_VALUE_ESCAPER.escape(toplevelCorpusName) + "/";
      }
      File graphML = api.subgraphForQuery(toplevelCorpusName, aql, QueryLanguage.AQL,
          AnnotationComponentType.PARTOF);
      SCorpusGraph cg = CorpusGraphMapper.map(graphML);
      for (SNode n : cg.getNodes()) {
        result.addAll(n.getMetaAnnotations());
      }
    } catch (ApiException | XMLStreamException | IOException ex) {
      log.error(null, ex);
      if (!AnnisBaseUI.handleCommonError(ex, "retrieve metadata")) {
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
  public static List<SMetaAnnotation> getMetaDataDoc(String toplevelCorpusName, String documentName,
      UI ui) {
    List<SMetaAnnotation> result = new ArrayList<>();
    SearchApi api = new SearchApi(Helper.getClient(ui));

    try {

      // Get the corpus graph and with it the meta data on the corpus/document nodes
      File graphML = api.subgraphForQuery(toplevelCorpusName,
          "annis:node_type=\"corpus\" _ident_ annis:doc=/"
              + AQL_REGEX_VALUE_ESCAPER.escape(documentName) + "/",
          QueryLanguage.AQL, AnnotationComponentType.PARTOF);

      SCorpusGraph cg = CorpusGraphMapper.map(graphML);

      for (SNode n : cg.getNodes()) {
        result.addAll(n.getMetaAnnotations());
      }

    } catch (ApiException | XMLStreamException | IOException ex) {
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


  public static void setUser(AnnisUser user) {
    if (user == null) {
      VaadinSession.getCurrent().getSession().removeAttribute(AnnisBaseUI.USER_KEY);
    } else {
      VaadinSession.getCurrent().getSession().setAttribute(AnnisBaseUI.USER_KEY, user);
    }
  }

  public static void addMatchToDocumentGraph(Match match, SDocument document) {
    List<String> allUrisAsString = new LinkedList<>();
    long i = 1;
    for (String u : match.getSaltIDs()) {
      allUrisAsString.add(u.replace(",", "%2C"));
      if (!u.startsWith("salt:/")) {
        u = "salt:/" + u;
      }
      SNode matchedNode = document.getDocumentGraph().getNode(u);
      // set the feature for this specific node
      if (matchedNode != null) {
        SFeature existing =
            matchedNode.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE);
        if (existing == null) {
          SFeature featMatchedNode = SaltFactory.createSFeature();
          featMatchedNode.setNamespace(AnnisConstants.ANNIS_NS);
          featMatchedNode.setName(AnnisConstants.FEAT_MATCHEDNODE);
          featMatchedNode.setValue(i);
          matchedNode.addFeature(featMatchedNode);
        }

      }
      i++;
    }
    SFeature existingFeatIDs =
        document.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDIDS);
    if (existingFeatIDs == null) {
      SFeature featIDs = SaltFactory.createSFeature();
      featIDs.setNamespace(AnnisConstants.ANNIS_NS);
      featIDs.setName(AnnisConstants.FEAT_MATCHEDIDS);
      featIDs.setValue(Joiner.on(",").join(allUrisAsString));
      document.addFeature(featIDs);
    }

    SFeature existingFeatAnnos =
        document.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDANNOS);
    if (existingFeatAnnos == null) {
      SFeature featAnnos = SaltFactory.createSFeature();
      featAnnos.setNamespace(AnnisConstants.ANNIS_NS);
      featAnnos.setName(AnnisConstants.FEAT_MATCHEDANNOS);
      featAnnos.setValue(Joiner.on(",").join(match.getAnnos()));
      document.addFeature(featAnnos);
    }
  }

  /**
   * Calculates a {@link SOrderRelation} node chain of a {@link SDocumentGraph}.
   *
   * <p>
   * If no segmentation name is set, a list of sorted {@link SToken} will be returned.
   * <p>
   *
   * @param segName The segmentation name, for which the chain is computed.
   * @param graph The salt document graph, which is traversed for the segmentation.
   *
   * @return Returns a List of {@link SNode}, which is sorted by the {@link SOrderRelation}.
   */
  public static List<SNode> getSortedSegmentationNodes(String segName, SDocumentGraph graph) {
    List<SNode> token = new ArrayList<SNode>();

    if (segName == null) {
      // if no segmentation is given just return the sorted token list
      List<SToken> unsortedToken = graph.getSortedTokenByText();
      if (unsortedToken != null) {
        token.addAll(unsortedToken);
      }
    } else {
      // get the very first node of the order relation chain
      Set<SNode> startNodes = new LinkedHashSet<SNode>();
      if (graph != null) {
        List<SNode> orderRoots = graph.getRootsByRelation(SALT_TYPE.SORDER_RELATION);
        if (orderRoots != null) {
          // collect the start nodes of a segmentation chain of length 1
          for (SNode n : orderRoots) {
            for (SRelation<?, ?> rel : n.getOutRelations()) {
              if (rel instanceof SOrderRelation) {
                // the type is the name of the relation
                if (segName.equals(rel.getType())) {
                  startNodes.add(n);
                  break;
                }
              }
            }
          }
        }
      }

      Set<String> alreadyAdded = new HashSet<String>();

      // add all nodes on the order relation chain beginning from the start node
      for (SNode s : startNodes) {
        SNode current = s;
        while (current != null) {
          token.add(current);
          List<SRelation<SNode, SNode>> out = graph.getOutRelations(current.getId());
          current = null;
          if (out != null) {
            for (SRelation<? extends SNode, ? extends SNode> e : out) {
              if (e instanceof SOrderRelation) {
                current = ((SOrderRelation) e).getTarget();
                if (alreadyAdded.contains(current.getId())) {
                  // abort if cycle detected
                  current = null;
                } else {
                  alreadyAdded.add(current.getId());
                }
                break;
              }
            }
          }
        }
      }
    }

    return token;
  }


  /**
   * Detects arabic characters in a string.
   *
   * <p>
   * Every character is checked, if its bit representation lies between:
   * <code>[1425, 1785] | [64286, 65019] | [65136, 65276]</code>
   *
   * </p>
   *
   * @param str The string to be checked.
   * @return returns true, if arabic characters are detected.
   */
  public static boolean containsRTLText(String str) {
    if (str != null) {
      for (int i = 0; i < str.length(); i++) {
        char cc = str.charAt(i);
        // hebrew extended and basic, arabic basic and extendend
        if (cc >= 1425 && cc <= 1785) {
          return true;
        }
        // alphabetic presentations forms (hebrwew) to arabic presentation forms A
        else if (cc >= 64286 && cc <= 65019) {
          return true;
        }
        // arabic presentation forms B
        else if (cc >= 65136 && cc <= 65276) {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * Gets the spannend/covered text for a token. This will get all {@link STextualRelation} edges
   * for a {@link SToken} from the {@link SDocumentGraph} and calculates the appropiate substring
   * from the {@link STextualDS}.
   *
   * @param tok The {@link SToken} which is overlapping the text sequence.
   * @return An empty {@link String} object, if there is no {@link STextualRelation}
   */
  public static String getSpannedText(SToken tok) {
    SGraph graph = tok.getGraph();

    List<SRelation<SNode, SNode>> edges = graph.getOutRelations(tok.getId());
    for (SRelation<? extends SNode, ? extends SNode> e : edges) {
      if (e instanceof STextualRelation) {
        STextualRelation textRel = (STextualRelation) e;
        return textRel.getTarget().getText().substring(textRel.getStart(), textRel.getEnd());
      }
    }
    return "";
  }


  /**
   * Finds the {@link STextualDS} for a given node. The node must dominate a token of this text.
   *
   * @param node Salt node to find the textual data source for
   * @param graph document graph
   * @return textual datasource or null if not connected to one
   */
  public static STextualDS getTextualDSForNode(SNode node, SDocumentGraph graph) {
    if (node != null) {
      List<DataSourceSequence> dataSources =
          graph.getOverlappedDataSourceSequence(node, SALT_TYPE.STEXT_OVERLAPPING_RELATION);
      if (dataSources != null) {
        for (DataSourceSequence seq : dataSources) {
          if (seq.getDataSource() instanceof STextualDS) {
            return (STextualDS) seq.getDataSource();
          }
        }
      }
    }
    return null;
  }

  public static Set<String> getTokenAnnotationLevelSet(SaltProject p) {
    Set<String> result = new TreeSet<String>();

    for (SCorpusGraph corpusGraphs : p.getCorpusGraphs()) {
      for (SDocument doc : corpusGraphs.getDocuments()) {
        SDocumentGraph g = doc.getDocumentGraph();
        result.addAll(getTokenAnnotationLevelSet(g));
      }
    }

    return result;
  }

  public static Set<String> getTokenAnnotationLevelSet(SDocumentGraph graph) {
    Set<String> result = new TreeSet<String>();

    if (graph != null) {
      for (SToken n : graph.getTokens()) {
        for (SAnnotation anno : n.getAnnotations()) {
          result.add(anno.getQName());
        }
      }
    }

    return result;
  }


  /**
   * Checks a {@link SNode} if it is member of a specific {@link SLayer}.
   *
   * @param layerName Specifies the layername to check.
   * @param node Specifies the node to check.
   * @return true - it is true when the name of layername corresponds to the name of any label of
   *         the SNode.
   */
  public static boolean checkSLayer(String layerName, SNode node) {
    // robustness
    if (layerName == null || node == null) {
      return false;
    }

    Set<SLayer> sLayers = node.getLayers();
    if (sLayers != null) {
      for (SLayer l : sLayers) {
        Collection<Label> labels = l.getLabels();
        if (labels != null) {
          for (Label label : labels) {
            if (layerName.equals(label.getValue())) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  /**
   * Gets all names of a corpus from a salt project.
   *
   * @param p Salt project
   * @return returns an empty list if project is empty or null.
   */
  public static Set<String> getToplevelCorpusNames(SaltProject p) {
    Set<String> names = new HashSet<>();

    if (p != null && p.getCorpusGraphs() != null) {
      for (SCorpusGraph g : p.getCorpusGraphs()) {
        if (g.getRoots() != null) {
          for (SNode c : g.getRoots()) {
            names.add(c.getName());
          }
        }
      }
    }

    return names;
  }

  /**
   * Parses the fragment.
   *
   * Fragments have the form key1=value&key2=test ...
   *
   * @param fragment fragment to parse
   * @return a map with the keys and values of the fragment
   */
  public static LinkedHashMap<String, String> parseFragment(String fragment) {
    LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

    fragment = StringUtils.removeStart(fragment, "!");

    String[] split = StringUtils.split(fragment, "&");
    if (split != null) {
      for (String s : split) {
        String[] parts = s.split("=", 2);
        String name = parts[0].trim();
        String value = "";
        if (parts.length == 2) {
          try {
            // every name that starts with "_" is base64 encoded
            if (name.startsWith("_")) {
              value = new String(Base64.decodeBase64(parts[1]), "UTF-8");
            } else {
              value = URLDecoder.decode(parts[1], "UTF-8");
            }
          } catch (UnsupportedEncodingException ex) {
            log.error(ex.getMessage(), ex);
          }
        }
        name = StringUtils.removeStart(name, "_");

        result.put(name, value);
      }
    }
    return result;
  }
}
