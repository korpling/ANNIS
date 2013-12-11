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

import annis.model.Annotation;
import annis.provider.SaltProjectProvider;
import annis.service.objects.CorpusConfig;
import annis.service.objects.CorpusConfigMap;
import annis.service.objects.RawTextWrapper;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.slf4j.LoggerFactory;

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

  private static final ThreadLocal<Client> anonymousClient = new ThreadLocal<Client>();

  private static final String ERROR_MESSAGE_CORPUS_PROPS_HEADER
    = "Corpus properties does not exist";

  private static final String ERROR_MESSAGE_CORPUS_PROPS
    = "<div><p><strong>ANNIS can not access the corpus properties</strong></p>"
    + "<h2>possible reasons are:</h2>"
    + "<ul>"
    + "<li>the ANNIS service is not running</li>"
    + "<li>the corpus properties are not well defined</li></ul>"
    + "<p>Please ask the responsible admin or consult the ANNIS "
    + "<a href=\"http://korpling.github.io/ANNIS\">Documentation</a>.</p></div>";

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

    rc.getProperties().put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER,
      new ThreadSafeClientConnManager());

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
      wrappedSession = vSession.getSession();

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
      return user.getClient().resource(uri);
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
      return user.getClient().asyncResource(uri);
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
    // get URI used by the application
    String uri = (String) VaadinSession.getCurrent().getAttribute(
      KEY_WEB_SERVICE_URL);

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
    String segmentation,
    int start, int limit)
  {
    List<String> result = new ArrayList<String>();
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
    }
    catch (UnsupportedEncodingException ex)
    {
      log.warn(ex.getMessage(), ex);
    }

    return result;
  }

  public static String generateCitation(String aql,
    Set<String> corpora, int contextLeft, int contextRight,
    String segmentation,
    int start, int limit)
  {
    try
    {
      URI appURI = UI.getCurrent().getPage().getLocation();

      return new URI(appURI.getScheme(), null,
        appURI.getHost(), appURI.getPort(),
        getContext(), null,
        StringUtils.join(citationFragment(aql, corpora,
            contextLeft, contextRight, segmentation, start, limit), "&"))
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
        .path(URLEncoder.encode(toplevelCorpusName, "UTF-8"));
      res = res.path(URLEncoder.encode(documentName, "UTF-8"));

      result = res.get(new GenericType<List<Annotation>>()
      {
      });
    }
    catch (UniformInterfaceException ex)
    {
      log.error(null, ex);
      Notification.show(
        "Remote exception: " + ex.getLocalizedMessage(),
        Notification.Type.WARNING_MESSAGE);
    }
    catch (ClientHandlerException ex)
    {
      log.error(null, ex);
      Notification.show(
        "Remote exception: " + ex.getLocalizedMessage(),
        Notification.Type.WARNING_MESSAGE);
    }
    catch (UnsupportedEncodingException ex)
    {
      log.error(null, ex);
      Notification.show(
        "UTF-8 encoding is not supported on server, this is weird: " + ex.
        getLocalizedMessage(),
        Notification.Type.WARNING_MESSAGE);
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
        .path(URLEncoder.encode(toplevelCorpusName, "UTF-8"));

      if (documentName != null)
      {
        res = res.path(documentName);
      }

      if (documentName != null && !toplevelCorpusName.equals(documentName))
      {
        res = res.path("path");
      }

      result = res.get(new GenericType<List<Annotation>>()
      {
      });
    }
    catch (UniformInterfaceException ex)
    {
      log.error(null, ex);
      Notification.show(
        "Remote exception: " + ex.getLocalizedMessage(),
        Notification.Type.WARNING_MESSAGE);
    }
    catch (ClientHandlerException ex)
    {
      log.error(null, ex);
      Notification.show(
        "Remote exception: " + ex.getLocalizedMessage(),
        Notification.Type.WARNING_MESSAGE);
    }
    catch (UnsupportedEncodingException ex)
    {
      log.error(null, ex);
      Notification.show(
        "UTF-8 encoding is not supported on server, this is weird: " + ex.
        getLocalizedMessage(),
        Notification.Type.WARNING_MESSAGE);
    }
    return result;
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
        .path("corpora").path(URLEncoder.encode(corpus, "UTF-8"))
        .path("config").get(CorpusConfig.class);
    }
    catch (UnsupportedEncodingException ex)
    {
      new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER,
        ERROR_MESSAGE_CORPUS_PROPS, Notification.Type.WARNING_MESSAGE, true)
        .show(Page.getCurrent());
    }
    catch (UniformInterfaceException ex)
    {
      new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER,
        ERROR_MESSAGE_CORPUS_PROPS, Notification.Type.WARNING_MESSAGE, true)
        .show(Page.getCurrent());
    }
    catch (ClientHandlerException ex)
    {
      new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER,
        ERROR_MESSAGE_CORPUS_PROPS, Notification.Type.WARNING_MESSAGE, true)
        .show(Page.getCurrent());
    }

    return corpusConfig;
  }

  public static CorpusConfig getDefaultCorpusConfig()
  {

    CorpusConfig defaultCorpusConfig = new CorpusConfig();

    try
    {
      defaultCorpusConfig = Helper.getAnnisWebResource().path("query")
        .path("corpora").path("default-config").get(CorpusConfig.class);
    }
    catch (UniformInterfaceException ex)
    {
      new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER,
        ERROR_MESSAGE_CORPUS_PROPS, Notification.Type.WARNING_MESSAGE, true)
        .show(Page.getCurrent());
    }
    catch (ClientHandlerException ex)
    {
      new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER,
        ERROR_MESSAGE_CORPUS_PROPS, Notification.Type.WARNING_MESSAGE, true)
        .show(Page.getCurrent());
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
    catch (UniformInterfaceException ex)
    {
      new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER,
        ERROR_MESSAGE_CORPUS_PROPS, Notification.Type.WARNING_MESSAGE, true)
        .show(Page.getCurrent());
    }
    catch (ClientHandlerException ex)
    {
      new Notification(ERROR_MESSAGE_CORPUS_PROPS_HEADER,
        ERROR_MESSAGE_CORPUS_PROPS, Notification.Type.WARNING_MESSAGE, true)
        .show(Page.getCurrent());
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
    sb.append("Exception type: ").append(ex.getClass().getName()).append("\n");
    sb.append("Message: ").append(ex.getLocalizedMessage()).append("\n");
    sb.append("Stacktrace: \n");
    StackTraceElement[] st = ex.getStackTrace();
    for (int i = 0; i < st.length; i++)
    {
      sb.append(st[i].toString());
      sb.append("\n");
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

    catch (UniformInterfaceException ex)
    {
      Notification.show("can not retrieve raw text", ex.
        getLocalizedMessage(), Notification.Type.WARNING_MESSAGE);
    }
    catch (ClientHandlerException ex)
    {
      Notification.show("can not retrieve raw text", ex.
        getLocalizedMessage(), Notification.Type.WARNING_MESSAGE);
    }

    return texts;
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
}
