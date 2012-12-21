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
import annis.service.objects.AnnisCorpus;
import annis.service.objects.CorpusConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Window;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
  
  /**
   * Gets or creates a web resource to the ANNIS service.
   *
   * @param uri The URI where the service can be found
   * @param user The user object or null (should be of type {@link AnnisUser}).
   * @return A reference to the ANNIS service root resource.
   */
  public static WebResource getAnnisWebResource(String uri, Object user)
  {
    
    if(user != null && user instanceof AnnisUser)
    {
      return ((AnnisUser) user).getClient().resource(uri);
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
   * This is a convenience wrapper to {@link #getAnnisWebResource(java.lang.String, java.lang.Object) }
   * that gets all the needed information from the Vaadin {@link Application}
   * 
   * @param app  The Vaadin application.
   * @return A reference to the ANNIS service root resource.
   */
  public static WebResource getAnnisWebResource(Application app)
  {
    // get URI used by the application
    String uri = app.getProperty(KEY_WEB_SERVICE_URL);
    
    // if already authentificated the REST client is set as the "user" property
    Object user  = app.getUser();
    
    return getAnnisWebResource(uri, user);
  }


  public static String getContext(Application app)
  {
    WebApplicationContext context = (WebApplicationContext) app.getContext();
    return context.getHttpSession().getServletContext().getContextPath();
  }

  public static String generateCitation(Application app, String aql,
    List<String> corpora,
    int contextLeft, int contextRight)
  {
    try
    {
      StringBuilder sb = new StringBuilder();

      URI appURI = app.getURL().toURI();

      sb.append(getContext(app));
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
    catch (URISyntaxException ex)
    {
      log.error(null, ex);
    }
    return "ERROR";
  }
  
  public static CorpusConfig getCorpusConfig(String corpus, 
    Application app, Window window)
  {
    CorpusConfig corpusConfig = new CorpusConfig();
    corpusConfig.setConfig(new TreeMap<String, String>());
    
    try
    {
      corpusConfig = Helper.getAnnisWebResource(app).path("query")
        .path("corpora").path(URLEncoder.encode(corpus, "UTF-8"))
        .path("config").get(CorpusConfig.class);
    }
    catch(UnsupportedEncodingException ex)
    {
      window.showNotification("could not query corpus configuration", ex.
        getLocalizedMessage(), Window.Notification.TYPE_TRAY_NOTIFICATION);
    }
    catch (UniformInterfaceException ex)
    {
      window.showNotification("could not query corpus configuration", ex.
        getLocalizedMessage(), Window.Notification.TYPE_WARNING_MESSAGE);
    }
    return corpusConfig;
  }
}
