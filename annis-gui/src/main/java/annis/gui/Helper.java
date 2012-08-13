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
import annis.service.objects.AnnisCorpus;
import annis.service.objects.CorpusConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author thomas
 */
public class Helper
{
  
  private static ThreadLocal<WebResource> annisWebResource = new ThreadLocal<WebResource>();
  
  public static WebResource createAnnisWebResource(String uri)
  {
    ClientConfig rc = new DefaultClientConfig();
    rc.getClasses().add(SaltProjectProvider.class);
    Client c = Client.create(rc);
    return c.resource(uri);
  }

  public static WebResource getAnnisWebResource(Application app)
  {
    if(annisWebResource.get() == null)
    {
      annisWebResource.set(createAnnisWebResource(app.getProperty("AnnisWebService.URL")));
    }
    return annisWebResource.get();
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
        Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
      }
      return "ERROR";
    }
    catch (URISyntaxException ex)
    {
      Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
    }
    return "ERROR";
  }

  public static Map<Long, AnnisCorpus> calculateID2Corpus(
    Map<String, AnnisCorpus> corpusMap)
  {
    TreeMap<Long, AnnisCorpus> result = new TreeMap<Long, AnnisCorpus>();
    for (AnnisCorpus c : corpusMap.values())
    {
      result.put(c.getId(), c);
    }
    return result;
  }
  
  
  
  public static CorpusConfig getCorpusConfig(String corpus, 
    Application app, Window window)
  {
    CorpusConfig corpusConfig = new CorpusConfig();
    corpusConfig.setConfig(new TreeMap<String, String>());
    
    try
    {
      corpusConfig = Helper.getAnnisWebResource(app).path("corpora").
        path(URLEncoder.encode(corpus, "UTF-8"))
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
