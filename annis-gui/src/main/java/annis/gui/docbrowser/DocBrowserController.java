/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.docbrowser;

import annis.gui.SearchUI;
import annis.libgui.PluginSystem;
import com.vaadin.server.ClientConnector;
import com.vaadin.ui.TabSheet;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a global controller for the doc browser feature.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class DocBrowserController
{

  // holds the complete state of the gui
  private transient final SearchUI ui;

  // track the already initiated doc browsers
  private transient final Map<String, TabSheet.Tab> initedDocBrowsers;

  public DocBrowserController(SearchUI ui)
  {
    this.ui = ui;
    this.initedDocBrowsers = new HashMap<String, TabSheet.Tab>();
  }

  public void openDocBrowser(String corpus)
  {
    if (!initedDocBrowsers.containsKey(corpus))
    {
      TabSheet.Tab tab = DocBrowserPanel.initDocBrowserPanel(ui, corpus);
      initedDocBrowsers.put(corpus, tab);
      tab.getComponent().addDetachListener(new DetachDocBrowserListener(corpus));
    }
    else
    {
      ui.getTabSheet().setSelectedTab(initedDocBrowsers.get(corpus));
    }
  }

  private class DetachDocBrowserListener implements
    ClientConnector.DetachListener
  {

    String corpus;

    public DetachDocBrowserListener(String corpus)
    {
      this.corpus = corpus;
    }

    @Override
    public void detach(ClientConnector.DetachEvent event)
    {
      initedDocBrowsers.remove(corpus);
    }
  }
}
