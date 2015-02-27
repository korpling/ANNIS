/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.gui.docbrowser.DocBrowserController;
import annis.gui.requesthandler.BinaryRequestHandler;
import annis.gui.requesthandler.LoginServletRequestHandler;
import annis.gui.requesthandler.ResourceRequestHandler;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Helper;
import annis.service.objects.AnnisCorpus;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CommonUI extends AnnisBaseUI
{
  private static final Logger log = LoggerFactory.getLogger(CommonUI.class);
  
   private final static Escaper urlPathEscape = UrlEscapers.
    urlPathSegmentEscaper();

  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);
    
    getSession().addRequestHandler(new LoginServletRequestHandler());    
    getSession().addRequestHandler(new ResourceRequestHandler());
    getSession().addRequestHandler(new BinaryRequestHandler());

  }
  
  /**
   * Takes a list of raw corpus names as given by the #c parameter and returns a
   * list of corpus names that are known to exist. It also replaces alias names
   * with the real corpus names.
   *
   * @param originalNames
   * @return
   */
  protected Set<String> getMappedCorpora(List<String> originalNames)
  {
    WebResource rootRes = Helper.getAnnisWebResource();
    Set<String> mappedNames = new HashSet<>();
    // iterate over given corpora and map names if necessary
    for (String selectedCorpusName : originalNames)
    {
      // get the real corpus descriptions by the name (which could be an alias)
      try
      {
        List<AnnisCorpus> corporaByName
          = rootRes.path("query").path("corpora").path(urlPathEscape.escape(
              selectedCorpusName))
          .get(new GenericType<List<AnnisCorpus>>()
            {
          });

        if (corporaByName == null || corporaByName.isEmpty())
        {
          // When we did not get any answer for this corpus we might not have
          // the rights to access it yet. Since we want to preserve the "c"
          // parameter in the string we should still remember it.
          // See https://github.com/korpling/ANNIS/issues/330
          mappedNames.add(selectedCorpusName);
        }
        else
        {
          for (AnnisCorpus c : corporaByName)
          {
            mappedNames.add(c.getName());
          }
        }
      }
      catch (ClientHandlerException ex)
      {
        String msg = "alias mapping does not work for alias: "
          + selectedCorpusName;
        log.error(msg, ex);
        Notification.show(msg, Notification.Type.TRAY_NOTIFICATION);
      }
    }
    return mappedNames;
  }
  
}
