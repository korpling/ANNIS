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

import annis.exceptions.AnnisServiceFactoryException;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisCorpus;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

  public static AnnisService getService(Application app, Window window)
  {
    AnnisService service = null;

    if(app == null || window == null)
    {
      return service;
    }

    try
    {
      service = AnnisServiceFactory.getClient(app.getProperty("AnnisRemoteService.URL"));
    }
    catch(AnnisServiceFactoryException ex)
    {
      Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, "Could not connect to service", ex);
      window.showNotification("Could not connect to service: " + ex.getMessage(),
        Notification.TYPE_TRAY_NOTIFICATION);
    }

    return service;
  }

  public static String getContext(Application app)
  {
    WebApplicationContext context = (WebApplicationContext) app.getContext();
    return context.getHttpSession().getServletContext().getContextPath();
  }

  public static boolean containsRTLText(String str)
  {
    for(int i = 0; i < str.length(); i++)
    {
      char cc = str.charAt(i);
      // hebrew extended and basic, arabic basic and extendend
      if(cc >= 1425 && cc <= 1785)
      {
        return true;
      }
      // alphabetic presentations forms (hebrwew) to arabic presentation forms A
      else if(cc >= 64286 && cc <= 65019)
      {
        return true;
      }
      // arabic presentation forms B
      else if(cc >= 65136 && cc <= 65276)
      {
        return true;
      }
    }
    return false;
  }

  public static String generateCitation(Application app, String aql, List<String> corpora,
    int contextLeft, int contextRight)
  {
    StringBuilder sb = new StringBuilder();

    sb.append(app.getURL().toString());
    sb.append("Cite/AQL(");
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
      return new URI(sb.toString()).toASCIIString();
    }
    catch(URISyntaxException ex)
    {
      Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
    }
    return sb.toString();
  }

  public static Map<String, AnnisCorpus> calculateName2Corpus(Map<Long, AnnisCorpus> corpusMap)
  {
    TreeMap<String, AnnisCorpus> result = new TreeMap<String, AnnisCorpus>();
    for(AnnisCorpus c : corpusMap.values())
    {
      result.put(c.getName(), c);
    }
    return result;
  }
}
