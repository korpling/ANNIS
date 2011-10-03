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

import annis.gui.controlpanel.ControlPanel;
import annis.security.AnnisUser;
import annis.service.ifaces.AnnisCorpus;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author thomas
 */
public class CitationWindow extends Window implements URIHandler
{

  // regular expression matching, CLEFT and CRIGHT are optional
  // indexes: AQL=1, CIDS=2, CLEFT=4, CRIGHT=6
  private Pattern citationPattern =
    Pattern.compile("AQL\\((.*)\\),CIDS\\(([^)]*)\\)(,CLEFT\\(([^)]*)\\),)?(CRIGHT\\(([^)]*)\\))?");

  public CitationWindow()
  {
    addComponent(new Label("please wait while you are redirected..."));
    addURIHandler((URIHandler) this);
  }

  @Override
  public String getName()
  {
    return "Cite";
  }

  @Override
  public DownloadStream handleURI(URL context, String relativeUri)
  {
    SearchWindow searchWindow = (SearchWindow) getApplication().getWindow("Search");
    if(searchWindow == null)
    {
      return null;
    }

    AnnisUser user = (AnnisUser) getApplication().getUser();
    if(user == null)
    {
      return null;
    }

    Map<Long, AnnisCorpus> userCorpora = user.getCorpusList();
    Map<String, AnnisCorpus> name2Corpus = Helper.calculateName2Corpus(userCorpora);

    ControlPanel controlPanel = searchWindow.getControl();
    Matcher m = citationPattern.matcher(relativeUri);
    if(m.matches())
    {
      // AQL
      String aql = "";
      if(m.group(1) != null)
      {
        try
        {
          aql = URLDecoder.decode(m.group(1), "UTF-8");
        }
        catch(UnsupportedEncodingException ex)
        {
          Logger.getLogger(CitationWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

      // CIDS      
      HashMap<Long, AnnisCorpus> selectedCorpora = new HashMap<Long, AnnisCorpus>();
      if(m.group(2) != null)
      {
        String[] cids = m.group(2).split(",");
        for(String name : cids)
        {
          AnnisCorpus c = name2Corpus.get(name);
          if(c != null)
          {
            selectedCorpora.put(c.getId(), c);
          }
        }
      }

      // CLEFT and CRIGHT
      if(m.group(4) != null && m.group(6) != null)
      {
        int cleft = 0;
        int cright = 0;
        try
        {
          cleft = Integer.parseInt(m.group(4));
          cright = Integer.parseInt(m.group(6));
        }
        catch(NumberFormatException ex)
        {
          Logger.getLogger(CitationWindow.class.getName()).log(Level.SEVERE, 
            "could not parse context value", ex);
        }
        controlPanel.setQuery(aql, selectedCorpora, cleft, cright);
      }
      else
      {
        controlPanel.setQuery(aql, selectedCorpora);
      }
      
      // open the search window
      open(new ExternalResource(searchWindow.getURL()));
      
    }
    return null;
  }
}
