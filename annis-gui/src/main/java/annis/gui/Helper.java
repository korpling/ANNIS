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
package annis.gui;

import annis.provider.SaltProjectProvider;
import annis.security.AnnisUser;
import annis.service.objects.CorpusConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(Helper.class);
  private static Client anonymousClient;
  
  
  /**
   * Creates an authentificiated REST client 
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
    
    if(userName != null && password != null)
    {
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY, 
        new UsernamePasswordCredentials(userName, password));
      
      rc.getProperties().put(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER, 
        credentialsProvider);
      rc.getProperties().put(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION, true);
      
    }
    
    Client c = ApacheHttpClient4.create(rc);
    return c;
  }
  
  /**
   * Create a REST web service client which is not authentificated.
   * @return A newly created client.
   */
  public static Client createRESTClient()
  {
    return createRESTClient(null, null);
  }
  
  public static AnnisUser getUser()
  {
    Object o = VaadinSession.getCurrent().getSession().getAttribute(
      AnnisBaseUI.USER_KEY);
    if(o instanceof AnnisUser)
    {
      return (AnnisUser) o;
    }
    else
    {
      return null;
    }
  }
  public static void setUser(AnnisUser user)
  {
    VaadinSession.getCurrent().getSession().setAttribute(AnnisBaseUI.USER_KEY,
      user);
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
    
    if(user != null)
    {
      return user.getClient().resource(uri);
    }
    
    // use the anonymous client
    if(anonymousClient == null)
    {
      // anonymous client not created yet
      anonymousClient = createRESTClient();
    }
    
    return anonymousClient.resource(uri);
  }

  /**
   * Gets or creates a web resource to the ANNIS service.
   *
   * This is a convenience wrapper to {@link #getAnnisWebResource(java.lang.String, annis.security.AnnisUser)  }
   * that does not need any arguments
   * 
   * @return A reference to the ANNIS service root resource.
   */
  public static WebResource getAnnisWebResource()
  {
    // get URI used by the application
    String uri = (String) VaadinSession.getCurrent().getAttribute(KEY_WEB_SERVICE_URL);
    
    // if already authentificated the REST client is set as the "user" property
    AnnisUser user  = getUser();
    
    return getAnnisWebResource(uri, user);
  }


  public static String getContext()
  {
    if(VaadinService.getCurrentRequest() != null)
    {
      return  VaadinService.getCurrentRequest().getContextPath();
    }
    else
    {
      return (String) VaadinSession.getCurrent().getAttribute(AnnisBaseUI.CONTEXT_PATH);
    }
    
  }
  
  private static String encodeBase64URL(String val)
  {
    try
    {
      return Base64.encodeBase64URLSafeString(val.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException ex)
    {
      log.error("Java Virtual Maschine can't handle UTF-8: I'm utterly confused", ex);
    }
    return "";
  }
  
  public static List<String> citationFragment(String aql, 
    Set<String> corpora, int contextLeft, int contextRight, String segmentation, 
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
      if(segmentation != null)
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
    Set<String> corpora, int contextLeft, int contextRight, String segmentation, 
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
  
  public static CorpusConfig getCorpusConfig(String corpus)
  {
    CorpusConfig corpusConfig = new CorpusConfig();
    corpusConfig.setConfig(new TreeMap<String, String>());
    
    try
    {
      corpusConfig = Helper.getAnnisWebResource().path("query")
        .path("corpora").path(URLEncoder.encode(corpus, "UTF-8"))
        .path("config").get(CorpusConfig.class);
    }
    catch(UnsupportedEncodingException ex)
    {
      Notification.show("could not query corpus configuration", ex.
        getLocalizedMessage(), Notification.Type.TRAY_NOTIFICATION);
    }
    catch (UniformInterfaceException ex)
    {
      Notification.show("could not query corpus configuration", ex.
        getLocalizedMessage(), Notification.Type.WARNING_MESSAGE);
    }
    return corpusConfig;
  }
  
  /**
   * Parses the fragment.
   * 
   * Fragments have the form key1=value&key2=test ...
   * @param fragment
   * @return 
   */
  public static Map<String,String> parseFragment(String fragment)
  {
    Map<String, String> result = new TreeMap<String, String>();
 
    fragment = StringUtils.removeStart(fragment, "!");
    
    String[] split = StringUtils.split(fragment, "&");
    for(String s : split)
    {
      String[] parts = s.split("=", 2);
      String name = parts[0].trim();
      String value = "";
      if(parts.length == 2)
      {
        try
        {
          // every name that starts with "_" is base64 encoded
          if(name.startsWith("_"))
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
}
