/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.libgui;

import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_MATCHEDANNOS;
import static annis.model.AnnisConstants.FEAT_MATCHEDIDS;
import static annis.model.AnnisConstants.FEAT_MATCHEDNODE;
import static annis.model.AnnisConstants.FEAT_RELANNIS_NODE;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.eclipse.emf.common.util.BasicEList;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
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

import annis.model.Annotation;
import annis.model.RelannisNodeFeature;
import annis.provider.SaltProjectProvider;
import annis.service.objects.CorpusConfig;
import annis.service.objects.CorpusConfigMap;
import annis.service.objects.DocumentBrowserConfig;
import annis.service.objects.Match;
import annis.service.objects.OrderType;
import annis.service.objects.RawTextWrapper;
import elemental.json.JsonValue;

/**
 *
 * @author thomas
 */
public class Helper
{

  public final static String KEY_WEB_SERVICE_URL = "AnnisWebService.URL";

  public final static String DEFAULT_CONFIG = "default-config";

  // the name of the web font class, the css class contains !important.
  public final static String CORPUS_FONT_FORCE = "corpus-font-force";

  // the name of the web font class.
  public final static String CORPUS_FONT = "corpus-font";

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    Helper.class);

  private static final ThreadLocal<Client> anonymousClient = new ThreadLocal<>();

  private static final String ERROR_MESSAGE_CORPUS_PROPS_HEADER
    = "Corpus properties does not exist";

  private static final String ERROR_MESSAGE_CORPUS_PROPS
    = "<div><p><strong>ANNIS can not access the corpus properties</strong></p>"
    + "<h2>possible reasons are:</h2>"
    + "<ul>"
    + "<li>the ANNIS service is not running</li>"
    + "<li>the corpus properties are not well defined</li></ul>"
    + "<p>Please ask the responsible admin or consult the ANNIS "
    + "<a href=\"http://korpling.github.io/ANNIS/doc/\">Documentation</a>.</p></div>";

  private static final String ERROR_MESSAGE_DOCUMENT_BROWSER_HEADER
    = "Problems with parsing the document browser configuration.";

  private static final String ERROR_MESSAGE_DOCUMENT_BROWSER_BODY
    = "<div><p>Maybe there is a syntax error in the json file.</p></div>";

  private final static Escaper urlPathEscape = UrlEscapers.
    urlPathSegmentEscaper();

  private final static Escaper jerseyExtraEscape = Escapers.builder()
    .addEscape('{', "%7B")
    .addEscape('}', "%7D")
    .addEscape('%', "%25")
    .build();

  /**
   * Creates an authentificiated REST client
   *
   * @param userName
   * @param password
   * @return A newly created client.
   */
  public static Client createRESTClient(String userName, String password)
  {

    DefaultApacheHttpClient4Config rc = new DefaultApacheHttpClient4Config();
    rc.getClasses().add(SaltProjectProvider.class);

    ThreadSafeClientConnManager clientConnMgr = new ThreadSafeClientConnManager();
    clientConnMgr.setDefaultMaxPerRoute(10);
    rc.getProperties().put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER,
      clientConnMgr);

    if (userName != null && password != null)
    {
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(userName, password));

      rc.getProperties().put(
        ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER,
        credentialsProvider);
      rc.getProperties().put(
        ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION,
        true);

    }

    Client c = ApacheHttpClient4.create(rc);
    return c;
  }

  /**
   * Create a REST web service client which is not authentificated.
   *
   * @return A newly created client.
   */
  public static Client createRESTClient()
  {
    return createRESTClient(null, null);
  }

  public static AnnisUser getUser()
  {

    VaadinSession vSession = VaadinSession.getCurrent();
    WrappedSession wrappedSession = null;

    if (vSession != null)
    {
      wrappedSession = vSession.getSession();
    }

    if (wrappedSession != null)
    {

      Object o = VaadinSession.getCurrent().getSession().getAttribute(
        AnnisBaseUI.USER_KEY);
      if (o != null && o instanceof AnnisUser)
      {
        return (AnnisUser) o;
      }
    }

    return null;
  }

  public static void setUser(AnnisUser user)
  {
    if (user == null)
    {
      VaadinSession.getCurrent().getSession().removeAttribute(
        AnnisBaseUI.USER_KEY);
    }
    else
    {
      VaadinSession.getCurrent().getSession().setAttribute(AnnisBaseUI.USER_KEY,
        user);
    }
  }

  /**
   * Gets or creates a web resource to the ANNIS service.
   *
   * @param uri The URI where the service can be found
   * @param user The user object or null (should be of type {@link AnnisUser}).
   * @return A reference to the ANNIS service root resource.
   */
  public static WebResource getAnnisWebResource(String uri, AnnisUser user)
  {

    if (user != null)
    {
      try
      {
        return user.getClient().resource(uri);
      }
      catch (LoginDataLostException ex)
      {
        log.error(
          "Could not restore the login-data from session, user will invalidated",
          ex);
        setUser(null);
        UI ui = UI.getCurrent();
        if (ui instanceof AnnisBaseUI)
        {
          ((AnnisBaseUI) ui).getLoginDataLostBus().post(ex);
        }
      }
    }

    // use the anonymous client
    if (anonymousClient.get() == null)
    {
      // anonymous client not created yet
      anonymousClient.set(createRESTClient());
    }

    return anonymousClient.get().resource(uri);
  }

  /**
   * Gets or creates an asynchronous web resource to the ANNIS service.
   *
   * @param uri The URI where the service can be found
   * @param user The user object or null (should be of type {@link AnnisUser}).
   * @return A reference to the ANNIS service root resource.
   */
  public static AsyncWebResource getAnnisAsyncWebResource(String uri,
    AnnisUser user)
  {

    if (user != null)
    {
      try
      {
        return user.getClient().asyncResource(uri);
      }
      catch (LoginDataLostException ex)
      {
        log.error(
          "Could not restore the login-data from session, user will invalidated",
          ex);
        setUser(null);
        UI ui = UI.getCurrent();
        if (ui instanceof AnnisBaseUI)
        {
          ((AnnisBaseUI) ui).getLoginDataLostBus().post(ex);
        }
      }
    }

    // use the anonymous client
    if (anonymousClient.get() == null)
    {
      // anonymous client not created yet
      anonymousClient.set(createRESTClient());
    }

    return anonymousClient.get().asyncResource(uri);
  }

  /**
   * Gets or creates a web resource to the ANNIS service.
   *
   * This is a convenience wrapper to {@link #getAnnisWebResource(java.lang.String, annis.security.AnnisUser)
   * }
   * that does not need any arguments
   *
   * @return A reference to the ANNIS service root resource.
   */
  public static WebResource getAnnisWebResource()
  {

    VaadinSession vSession = VaadinSession.getCurrent();

    // get URI used by the application
    String uri = null;

    if (vSession != null)
    {
      uri = (String) VaadinSession.getCurrent().getAttribute(
        KEY_WEB_SERVICE_URL);
    }

    // if already authentificated the REST client is set as the "user" property
    AnnisUser user = getUser();

    return getAnnisWebResource(uri, user);
  }

  /**
   * Gets or creates an asynchronous web resource to the ANNIS service.
   *
   * This is a convenience wrapper to {@link #getAnnisWebResource(java.lang.String, annis.security.AnnisUser)
   * }
   * that does not need any arguments
   *
   * @return A reference to the ANNIS service root resource.
   */
  public static AsyncWebResource getAnnisAsyncWebResource()
  {
    // get URI used by the application
    String uri = (String) VaadinSession.getCurrent().getAttribute(
      KEY_WEB_SERVICE_URL);

    // if already authentificated the REST client is set as the "user" property
    AnnisUser user = getUser();

    return getAnnisAsyncWebResource(uri, user);
  }

  public static String getContext()
  {
    if (VaadinService.getCurrentRequest() != null)
    {
      return VaadinService.getCurrentRequest().getContextPath();
    }
    else
    {
      return (String) VaadinSession.getCurrent().getAttribute(
        AnnisBaseUI.CONTEXT_PATH);
    }

  }

  public static String encodeBase64URL(String val)
  {
    try
    {
      return Base64.encodeBase64URLSafeString(val.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException ex)
    {
      log.
        error(
          "Java Virtual Maschine can't handle UTF-8: I'm utterly confused",
          ex);
    }
    return "";
  }

  public static List<String> citationFragment(String aql,
    Set<String> corpora, int contextLeft, int contextRight,
    String segmentation, String visibleBaseText,
    long start, int limit, OrderType order,
    Set<Long> selectedMatches)
  {
    List<String> result = new ArrayList<>();
    try
    {
      result.add("_q=" + encodeBase64URL(aql));
      result.add("_c="
        + encodeBase64URL(StringUtils.join(corpora, ",")));
      result.add("cl="
        + URLEncoder.encode("" + contextLeft, "UTF-8"));
      result.add("cr="
        + URLEncoder.encode("" + contextRight, "UTF-8"));
      result.add("s="
        + URLEncoder.encode("" + start, "UTF-8"));
      result.add("l="
        + URLEncoder.encode("" + limit, "UTF-8"));
      if (segmentation != null)
      {
        result.add("_seg="
          + encodeBase64URL(segmentation));
      }
      // only output "bt" if it is not the same as the context segmentation
      if (!Objects.equals(visibleBaseText, segmentation))
      {
        result.add("_bt=" + (visibleBaseText == null ? "" : encodeBase64URL(
          visibleBaseText)));
      }
      if (order != OrderType.ascending && order != null)
      {
        result.add("o=" + order.toString());
      }
      if (selectedMatches != null && !selectedMatches.isEmpty())
      {
        result.add("m=" + Joiner.on(',').join(selectedMatches));
      }
    }
    catch (UnsupportedEncodingException ex)
    {
      log.warn(ex.getMessage(), ex);
    }

    return result;
  }

  public static URI generateCitation(String aql,
    Set<String> corpora, int contextLeft, int contextRight,
    String segmentation, String visibleBaseText,
    long start, int limit, OrderType order,
    Set<Long> selectedMatches)
  {
    try
    {
      URI appURI = UI.getCurrent().getPage().getLocation();

      return new URI(appURI.getScheme(), null,
        appURI.getHost(), appURI.getPort(),
        appURI.getPath(), null,
        StringUtils.join(citationFragment(
            aql, corpora, contextLeft, contextRight, segmentation,
            visibleBaseText, start, limit, order, selectedMatches
          ), "&"));
    }
    catch (URISyntaxException ex)
    {
      log.error(null, ex);
    }
    return null;
  }

  public static String generateCorpusLink(Set<String> corpora)
  {
    try
    {
      URI appURI = UI.getCurrent().getPage().getLocation();

      String fragment = "_c="
        + encodeBase64URL(StringUtils.join(corpora, ","));

      return new URI(appURI.getScheme(), null,
        appURI.getHost(), appURI.getPort(),
        appURI.getPath(), null,
        fragment)
        .toASCIIString();
    }
    catch (URISyntaxException ex)
    {
      log.error(null, ex);
    }
    return "ERROR";
  }

  public static String generateClassicCitation(String aql,
    List<String> corpora,
    int contextLeft, int contextRight)
  {
    StringBuilder sb = new StringBuilder();

    URI appURI = UI.getCurrent().getPage().getLocation();

    sb.append(getContext());
    sb.append("/Cite/AQL(");
    sb.append(aql);
    sb.append("),CIDS(");
    sb.append(StringUtils.join(corpora, ","));
    sb.append("),CLEFT(");
    sb.append(contextLeft);
    sb.append("),CRIGHT(");
    sb.append(contextRight);
    sb.append(")");

    try
    {
      return new URI(appURI.getScheme(), null,
        appURI.getHost(), appURI.getPort(),
        sb.toString(), null, null).toASCIIString();
    }
    catch (URISyntaxException ex)
    {
      log.error(null, ex);
    }
    return "ERROR";
  }

  /**
   * Retrieve the meta data for a given document of a corpus.
   *
   * @param toplevelCorpusName specifies the toplevel corpus
   * @param documentName specifies the document.
   * @return returns only the meta data for a single document.
   */
  public static List<Annotation> getMetaDataDoc(String toplevelCorpusName,
    String documentName)
  {
    List<Annotation> result = new ArrayList<Annotation>();
    WebResource res = Helper.getAnnisWebResource();
    try
    {
      res = res.path("meta").path("doc")
        .path(urlPathEscape.escape(toplevelCorpusName));
      res = res.path(urlPathEscape.escape(documentName));

      result = res.get(new GenericType<List<Annotation>>()
      {
      });
    }
    catch (UniformInterfaceException | ClientHandlerException ex)
    {
      log.error(null, ex);
      if(!AnnisBaseUI.handleCommonError(ex, "retrieve metadata"))
      {
        Notification.show(
          "Remote exception: " + ex.getLocalizedMessage(),
          Notification.Type.WARNING_MESSAGE);
      }
    }
    return result;
  }

  /**
   * Retrieve all the meta data for a given document of a corpus including the
   * metadata of all corora in the path to the document.
   *
   * @param toplevelCorpusName Specifies the the toplevel corpus
   * @param documentName Specifies the document
   * @return Returns also the metada of the all parent corpora. There must be at
   * least one of them.
   */
  public static List<Annotation> getMetaData(String toplevelCorpusName,
    String documentName)
  {
    List<Annotation> result = new ArrayList<Annotation>();
    WebResource res = Helper.getAnnisWebResource();
    try
    {
      res = res.path("meta").path("doc")
        .path(urlPathEscape.escape(toplevelCorpusName));

      if (documentName != null)
      {
        res = res.path(urlPathEscape.escape(documentName));
      }

      if (documentName != null && !toplevelCorpusName.equals(documentName))
      {
        res = res.path("path");
      }

      result = res.get(new GenericType<List<Annotation>>()
      {
      });
    }
    catch (UniformInterfaceException | ClientHandlerException ex)
    {
      log.error(null, ex);
      if(!AnnisBaseUI.handleCommonError(ex, "retrieve metada"))
      {
        Notification.show(
          "Remote exception: " + ex.getLocalizedMessage(),
          Notification.Type.WARNING_MESSAGE);
      }
    }
    return result;
  }

  public static DocumentBrowserConfig getDocBrowserConfig(String corpus)
  {
    try
    {
      DocumentBrowserConfig docBrowserConfig = Helper.getAnnisWebResource().
        path("query")
        .path("corpora").path("doc_browser_config")
        .path(urlPathEscape.escape(corpus))
        .get(DocumentBrowserConfig.class);

      return docBrowserConfig;
    }
    catch (UniformInterfaceException | ClientHandlerException ex)
    {
      log.error("problems with fetching document browsing", ex);
      if(!AnnisBaseUI.handleCommonError(ex, "get document browser configuration"))
      {
        new Notification(ERROR_MESSAGE_DOCUMENT_BROWSER_HEADER,
          ERROR_MESSAGE_DOCUMENT_BROWSER_BODY, Notification.Type.WARNING_MESSAGE,
          true).show(Page.getCurrent());
      }
    }

    return null;
  }

  /**
   * Loads the corpus config of a specific corpus.
   *
   * @param corpus The name of the corpus, for which the config is fetched.
   * @return A {@link CorpusConfig} object, which wraps a {@link Properties}
   * object. This Properties object stores the corpus configuration as simple
   * key-value pairs.
   */
  public static CorpusConfig getCorpusConfig(String corpus)
  {

    if (corpus == null || corpus.isEmpty())
    {
      Notification.show("no corpus is selected",
        "please select at leas one corpus and execute query again",
        Notification.Type.WARNING_MESSAGE);
      return null;
    }

    CorpusConfig corpusConfig = new CorpusConfig();

    try
    {
      corpusConfig = Helper.getAnnisWebResource().path("query")
        .path("corpora").path(urlPathEscape.escape(corpus))
        .path("config").get(CorpusConfig.class);
    }
    catch (UniformInterfaceException | ClientHandlerException ex)
    {
      if(!AnnisBaseUI.handleCommonError(ex, "get corpus configuration"))
      {
        new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER,
          ERROR_MESSAGE_CORPUS_PROPS, Notification.Type.WARNING_MESSAGE, true)
          .show(Page.getCurrent());
      }
    }

    return corpusConfig;
  }

  public static CorpusConfig getDefaultCorpusConfig()
  {

    CorpusConfig defaultCorpusConfig = new CorpusConfig();

    try
    {
      defaultCorpusConfig = Helper.getAnnisWebResource().path("query")
        .path("corpora").path(DEFAULT_CONFIG).get(CorpusConfig.class);
    }
    catch (UniformInterfaceException | ClientHandlerException ex)
    {
      if(!AnnisBaseUI.handleCommonError(ex, "get default corpus configuration"))
      {
        new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER,
          ERROR_MESSAGE_CORPUS_PROPS, Notification.Type.WARNING_MESSAGE, true)
          .show(Page.getCurrent());
      }
    }

    return defaultCorpusConfig;
  }

  /**
   * Loads the all available corpus configurations.
   *
   *
   * @return A {@link CorpusConfigMap} object, which wraps a Map of
   * {@link Properties} objects. The keys to the properties are the corpus
   * names. A Properties object stores the corpus configuration as simple
   * key-value pairs. The Map includes also the default corpus configuration.
   */
  public static CorpusConfigMap getCorpusConfigs()
  {

    CorpusConfigMap corpusConfigurations = null;

    try
    {
      corpusConfigurations = Helper.getAnnisWebResource().path(
        "query").path("corpora").path("config").get(CorpusConfigMap.class);
    }
    catch (UniformInterfaceException | ClientHandlerException ex)
    {
      UI.getCurrent().access(new Runnable()
      {

        @Override
        public void run()
        {
          if(!AnnisBaseUI.handleCommonError(ex, "get corpus configurations"))
          {
            new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER,
              ERROR_MESSAGE_CORPUS_PROPS, Notification.Type.WARNING_MESSAGE, true).
              show(Page.getCurrent());
          }
        }
      });
    }

    if (corpusConfigurations == null)
    {
      corpusConfigurations = new CorpusConfigMap();
    }

    corpusConfigurations.put(DEFAULT_CONFIG, getDefaultCorpusConfig());

    return corpusConfigurations;
  }

  /**
   * Loads the available corpus configurations for a list of specific corpora.
   *
   *
   * @param corpora A Set of corpora names.
   * @return A {@link CorpusConfigMap} object, which wraps a Map of
   * {@link Properties} objects. The keys to the properties are the corpus
   * names. A Properties object stores the corpus configuration as simple
   * key-value pairs. The map includes the default configuration.
   */
  public static CorpusConfigMap getCorpusConfigs(Set<String> corpora)
  {

    CorpusConfigMap corpusConfigurations = new CorpusConfigMap();

    for (String corpus : corpora)
    {
      corpusConfigurations.put(corpus, getCorpusConfig(corpus));
    }

    corpusConfigurations.put(DEFAULT_CONFIG, getDefaultCorpusConfig());
    return corpusConfigurations;
  }

  /**
   * Parses the fragment.
   *
   * Fragments have the form key1=value&key2=test ...
   *
   * @param fragment
   * @return
   */
  public static Map<String, String> parseFragment(String fragment)
  {
    Map<String, String> result = new TreeMap<String, String>();

    fragment = StringUtils.removeStart(fragment, "!");

    String[] split = StringUtils.split(fragment, "&");
    for (String s : split)
    {
      String[] parts = s.split("=", 2);
      String name = parts[0].trim();
      String value = "";
      if (parts.length == 2)
      {
        try
        {
          // every name that starts with "_" is base64 encoded
          if (name.startsWith("_"))
          {
            value = new String(Base64.decodeBase64(parts[1]), "UTF-8");
          }
          else
          {
            value = URLDecoder.decode(parts[1], "UTF-8");
          }
        }
        catch (UnsupportedEncodingException ex)
        {
          log.error(ex.getMessage(), ex);
        }
      }
      name = StringUtils.removeStart(name, "_");

      result.put(name, value);
    }
    return result;
  }

  /**
   * Returns a formatted string containing the type of the exception, the
   * message and the stacktrace.
   *
   * @param ex
   * @return
   */
  public static String convertExceptionToMessage(Throwable ex)
  {
    StringBuilder sb = new StringBuilder();
    if(ex != null)
    {
      sb.append("Exception type: ").append(ex.getClass().getName()).append("\n");
      sb.append("Message: ").append(ex.getLocalizedMessage()).append("\n");
      sb.append("Stacktrace: \n");
      StackTraceElement[] st = ex.getStackTrace();
      for (int i = 0; i < st.length; i++)
      {
        sb.append(st[i].toString());
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  public static RawTextWrapper getRawText(String corpusName, String documentName)
  {
    RawTextWrapper texts = null;
    try
    {
      WebResource webResource = getAnnisWebResource();
      webResource = webResource.path("query").path("rawtext").path(corpusName).
        path(documentName);
      texts = webResource.get(RawTextWrapper.class);
    }

    catch (UniformInterfaceException | ClientHandlerException ex)
    {
      log.error("can not retrieve raw text");
      if(!AnnisBaseUI.handleCommonError(ex, "retrieve raw text"))
      {
        Notification.show("can not retrieve raw text", ex.
          getLocalizedMessage(), Notification.Type.WARNING_MESSAGE);
      }
    }

    return texts;
  }

  /**
   * Get the qualified name seperated by a single ":" when a namespace exists.
   *
   * @param anno
   * @return
   */
  public static String getQualifiedName(SAnnotation anno)
  {
    if (anno != null)
    {
      if (anno.getNamespace() == null || anno.getNamespace().isEmpty())
      {
        return anno.getName();
      }
      else
      {
        return anno.getNamespace()+ ":" + anno.getName();
      }
    }
    return "";
  }

  /**
   * Returns true if the right-to-left heuristic should be disabled.
   *
   * @return
   */
  public static boolean isRTLDisabled()
  {
    String disableRtl = (String) VaadinSession.getCurrent().getAttribute(
      "disable-rtl");
    return "true".equalsIgnoreCase(
      disableRtl);
  }

  /**
   * This will percent encode Jersey template argument braces (enclosed in
   * "{...}") and the percent character. Both would not be esccaped by jersey
   * and/or would cause an error when this is not a valid template.
   *
   * @param v
   * @return
   */
  public static String encodeJersey(String v)
  {
    String encoded = jerseyExtraEscape.escape(v);
    return encoded;
  }

  /**
   * Encodes a String so it can be used as path param.
   *
   * @param v
   * @return
   */
  public static String encodePath(String v)
  {
    String encoded = urlPathEscape.escape(v);
    return encoded;
  }

  /**
   * Encodes a String so it can be used as query param.
   *
   * @param v
   * @return
   */
  public static String encodeQueryParam(String v)
  {
    String encoded = UrlEscapers.urlFormParameterEscaper().escape(v);
    return encoded;
  }

  /**
   * Casts a list of Annotations to the Type <code>List<Annotation></code>
   */
  public static class AnnotationListType extends GenericType<List<Annotation>>
  {

    public AnnotationListType()
    {
    }
  }

  public static <T> JsonValue encodeGeneric(Object v)
  {
    return JsonCodec.encode(v, null, v.getClass().getGenericSuperclass(), null).
      getEncodedValue();
  }

  public static Map<String, String> calculateColorsForMarkedExact(
    SDocument result)
  {
    Map<String, String> markedExactMap = new HashMap<>();
    if (result != null)
    {
      SDocumentGraph g = result.getDocumentGraph();
      if (g != null)
      {
        for (SNode n : result.getDocumentGraph().getNodes())
        {

          SFeature featMatched = n.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
          Long matchNum = featMatched == null ? null : featMatched.
            getValue_SNUMERIC();

          if (matchNum != null)
          {
            int color = Math.max(0, Math.min((int) matchNum.longValue() - 1,
              MatchedNodeColors.values().length - 1));
            RelannisNodeFeature feat = RelannisNodeFeature.extract(n);
            if (feat != null)
            {
              markedExactMap.put("" + feat.getInternalID(),
                MatchedNodeColors.values()[color].name());
            }
          }

        }
      } // end if g not null
    } // end if result not null
    return markedExactMap;
  }

  public static void calulcateColorsForMarkedAndCovered(SDocument result,
    Map<String, Long> markedAndCovered, Map<String, String> markedCoveredMap)
  {
    if (markedAndCovered != null)
    {
      for (Map.Entry<String, Long> markedEntry : markedAndCovered.entrySet())
      {
        int color = Math.max(0, Math.min((int) markedEntry.getValue().
          longValue()
          - 1,
          MatchedNodeColors.values().length - 1));
        SNode n = result.getDocumentGraph().getNode(markedEntry.getKey());
        RelannisNodeFeature feat = RelannisNodeFeature.extract(n);

        if (feat != null)
        {
          markedCoveredMap.put("" + feat.getInternalID(),
            MatchedNodeColors.values()[color].name());
        }
      } // end for each entry in markedAndCoverd
    } // end if markedAndCovered not null
  }

  public static Map<String, Long> calculateMarkedAndCoveredIDs(
    SDocument doc, List<SNode> segNodes, String segmentationName)
  {
    Map<String, Long> initialCovered = new HashMap<>();

    // add all covered nodes
    for (SNode n : doc.getDocumentGraph().getNodes())
    {
      SFeature featMatched = n.getFeature(ANNIS_NS,
        FEAT_MATCHEDNODE);
      Long match = featMatched == null ? null : featMatched.getValue_SNUMERIC();

      if (match != null)
      {
        initialCovered.put(n.getId(), match);
      }
    }

    // calculate covered nodes
    CoveredMatchesCalculator cmc = new CoveredMatchesCalculator(
      doc.
      getDocumentGraph(), initialCovered);
    Map<String, Long> covered = cmc.getMatchedAndCovered();

    if (segmentationName != null)
    {
      // filter token
      Map<SToken, Long> coveredToken = new HashMap<>();
      for (Map.Entry<String, Long> e : covered.entrySet())
      {
        SNode n = doc.getDocumentGraph().getNode(e.getKey());
        if (n instanceof SToken)
        {
          coveredToken.put((SToken) n, e.getValue());
        }
      }

      for (SNode segNode : segNodes)
      {
        RelannisNodeFeature featSegNode = (RelannisNodeFeature) segNode.
          getFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

        if (!covered.containsKey(segNode.getId()))
        {
          long leftTok = featSegNode.getLeftToken();
          long rightTok = featSegNode.getRightToken();

          // check for each covered token if this segment is covering it
          for (Map.Entry<SToken, Long> e : coveredToken.entrySet())
          {
            RelannisNodeFeature featTok = (RelannisNodeFeature) e.getKey().
              getFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
            long entryTokenIndex = featTok.getTokenIndex();
            if (entryTokenIndex <= rightTok && entryTokenIndex >= leftTok)
            {
              // add this segmentation node to the covered set
              covered.put(segNode.getId(), e.getValue());
              break;
            }
          } // end for each covered token
        } // end if not already contained
      } // end for each segmentation node
    }

    return covered;
  }

  /**
   * Marks all nodes which are dominated by already marked nodes.
   *
   * 1. Sort ascending all initial marked nodes by the size of the intervall
   * between left and right token index.
   *
   * 2. Traverse the salt document graph with the sorted list of step 1. as root
   * nodes and mark all children with the same match position. Already marked
   * nodes are omitted.
   *
   * Note: The algorithm prevents nested marked nodes to be overwritten. Nested
   * nodes must have a smaller intervall from left to right by default, so this
   * should always work.
   *
   */
  public static class CoveredMatchesCalculator implements GraphTraverseHandler
  {

    private Map<String, Long> matchedAndCovered;

    private final static Comparator<SNode> comp = new Comparator<SNode>()
    {
      
      @Override
      public int compare(SNode o1, SNode o2)
      {
        // generate several helper variables we want to compare
        RelannisNodeFeature feat1 = (RelannisNodeFeature) o1.getFeature(
            ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
        RelannisNodeFeature feat2 = (RelannisNodeFeature) o2.getFeature(
          ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

        long leftTokIdxO1 = feat1.getLeftToken();
        long rightTokIdxO1 = feat1.getRightToken();
        long leftTokIdxO2 = feat2.getLeftToken();
        long rightTokIdxO2 = feat2.getRightToken();

        int intervallO1 = (int) Math.abs(leftTokIdxO1 - rightTokIdxO1);
        int intervallO2 = (int) Math.abs(leftTokIdxO2 - rightTokIdxO2);

        SFeature featMatch1 = o1.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
        SFeature featMatch2 = o2.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
        long matchNode1 = featMatch1 == null ? Long.MAX_VALUE : featMatch1.getValue_SNUMERIC();
        long matchNode2 = featMatch2 == null ? Long.MAX_VALUE : featMatch2.getValue_SNUMERIC();
        
        // use a comparison chain which is much less verbose and better readable
        return ComparisonChain.start()
            .compare(intervallO1, intervallO2)
            .compare(feat1.getLeftToken(),  feat2.getLeftToken())
            .compare(feat1.getRightToken(), feat2.getRightToken())
            .compare(matchNode1, matchNode2)
            .compare(feat1.getInternalID(), feat2.getInternalID())
            .result();

      }
    };

    public CoveredMatchesCalculator(SDocumentGraph graph,
      Map<String, Long> initialMatches)
    {
      this.matchedAndCovered = initialMatches;

      Map<SNode, Long> sortedMatchedNodes = new TreeMap<>(comp);

      for (Map.Entry<String, Long> entry : initialMatches.entrySet())
      {
        SNode n = graph.getNode(entry.getKey());
        sortedMatchedNodes.put(n, entry.getValue());
      }

      if (initialMatches.size() > 0)
      {
        graph.traverse(new BasicEList<>(sortedMatchedNodes.
          keySet()),
          GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "CoveredMatchesCalculator",
          (GraphTraverseHandler) this, true);
      }
    }

    @Override
    public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType,
      String traversalId, SNode currNode, SRelation edge, SNode fromNode,
      long order)
    {
      if (fromNode != null
        && matchedAndCovered.containsKey(fromNode.getId())
        && currNode != null)
      {
        long currentMatchPos = matchedAndCovered.get(fromNode.getId());
        
        // only update the map when there is no entry yet or if the new index/position is smaller
        Long oldMatchPos = matchedAndCovered.get(currNode.getId());
        if(oldMatchPos == null)
        {          
          matchedAndCovered.put(currNode.getId(), currentMatchPos);
        }
      }

    }

    @Override
    public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
      SNode currNode, SRelation edge, SNode fromNode, long order)
    {
    }

    @Override
    public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType,
      String traversalId, SRelation edge, SNode currNode, long order)
    {
      if (edge == null || edge instanceof SDominanceRelation
        || edge instanceof SSpanningRelation)
      {
        return true;
      }
      else
      {
        return false;
      }
    }

    public Map<String, Long> getMatchedAndCovered()
    {
      return matchedAndCovered;
    }
  }

  public static String shortenURL(URI original)
  {
    WebResource res = Helper.getAnnisWebResource().path("shortener");
    String appContext = Helper.getContext();

    String path = original.getRawPath();
    if (path.startsWith(appContext))
    {
      path = path.substring(appContext.length());
    }

    String localURL = path;
    if (original.getRawQuery() != null)
    {
      localURL = localURL + "?" + original.getRawQuery();
    }
    if (original.getRawFragment() != null)
    {
      localURL = localURL + "#" + original.getRawFragment();
    }

    String shortID = res.post(String.class, localURL);

    return UriBuilder.fromUri(original).replacePath(appContext + "/").
      replaceQuery(
        "").fragment("").queryParam("id",
        shortID).build().toASCIIString();

  }

  public static boolean isKickstarter(VaadinSession session)
  {
    if(session != null)
    {
      return Boolean.parseBoolean(
        session.getConfiguration().getInitParameters()
        .getProperty("kickstarterEnvironment",
          "false"));
    }
    else
    {
      return false;
    }
  }
  
  public static void addMatchToDocumentGraph(Match match, SDocument document)
  {
    List<String> allUrisAsString = new LinkedList<>();
    long i = 1;
    for (URI u : match.getSaltIDs())
    {
      allUrisAsString.add(u.toASCIIString());
      SNode matchedNode = document.getDocumentGraph().getNode(u.toASCIIString());
      // set the feature for this specific node
      if (matchedNode != null)
      {
        SFeature existing = matchedNode.getFeature(ANNIS_NS, FEAT_MATCHEDNODE);
        if (existing == null)
        {
          SFeature featMatchedNode = SaltFactory.createSFeature();
          featMatchedNode.setNamespace(ANNIS_NS);
          featMatchedNode.setName(FEAT_MATCHEDNODE);
          featMatchedNode.setValue(i);
          matchedNode.addFeature(featMatchedNode);
        }
      }
      i++;
    }

    SFeature featIDs = SaltFactory.createSFeature();
    featIDs.setNamespace(ANNIS_NS);
    featIDs.setName(FEAT_MATCHEDIDS);
    featIDs.setValue(Joiner.on(",").join(allUrisAsString));
    document.addFeature(featIDs);

    SFeature featAnnos = SaltFactory.createSFeature();
    featAnnos.setNamespace(ANNIS_NS);
    featAnnos.setName(FEAT_MATCHEDANNOS);
    featAnnos.setValue(Joiner.on(",").join(match.getAnnos()));
    document.addFeature(featAnnos);

  }
  
}
