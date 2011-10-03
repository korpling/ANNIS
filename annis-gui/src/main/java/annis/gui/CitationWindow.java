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

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import java.net.URL;
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
    Matcher m = citationPattern.matcher(relativeUri);
    if(m.matches())
    {
//      // AQL
//      String aql = "";
//      if(m.group(1) != null)
//      {
//        formPanelSearch.getComponent('queryAnnisQL').setValue(Url.decode(params[1]));
//      }
//
//      // CIDS
//      if(params[2] != undefined)
//      {
//        var cids = params[2].split(',');
//        var selection = new Array();
//        for(i=0;i<cids.length;i++)
//        {
//          var index = store.findExact('name',cids[i]);
//          selection[i] = store.getAt(index);
//        }
//        selectionModel.selectRecords(selection, false);
//      }
//
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
