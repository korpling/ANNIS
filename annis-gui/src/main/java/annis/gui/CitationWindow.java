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
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
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
    addComponent(new Label("yeah, citation time!"));
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
    SearchWindow w = (SearchWindow) getApplication().getWindow("Search");
    if(w == null)
    {
      return null;
    }
    
    AnnisUser user = (AnnisUser) getApplication().getUser();
    if(user == null)
    {
      return null;
    }
    
    ControlPanel controlPanel = w.getControl();
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
      
      if(m.group(2) != null)
      {
        String[] cids = m.group(2).split(",");        
      }
      controlPanel.setQuery(aql, null);
      
//      // CLEFT
//      if(params[4] != undefined)
//      {
//        formPanelSimpleSearch.getComponent('padLeft').setValue(params[4]);
//      }
//      // CRIGHT
//      if(params[6] != undefined)
//      {
//        formPanelSimpleSearch.getComponent('padRight').setValue(params[6]);
//      }
    }
    return null;
  }

  
}
