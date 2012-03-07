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
package annis.gui.controlpanel;

import annis.gui.beans.HistoryEntry;
import annis.gui.Helper;
import annis.gui.SearchWindow;
import annis.security.AnnisUser;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author thomas
 */
public class ControlPanel extends Panel
{

  private static final long serialVersionUID = -2220211539424865671L;
  private QueryPanel queryPanel;
  private CorpusListPanel corpusList;
  private SearchWindow searchWindow;
  private Window window;
  private String lastQuery;
  private Map<String, AnnisCorpus> lastCorpusSelection;
  private SearchOptionsPanel searchOptions;
  private ListOrderedSet<HistoryEntry> history;

  public ControlPanel(SearchWindow searchWindow)
  {
    super("Search Form");
    this.searchWindow = searchWindow;
    this.history = new ListOrderedSet<HistoryEntry>();

    setStyleName(ChameleonTheme.PANEL_BORDERLESS);
    addStyleName("control");

    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setHeight(100f, UNITS_PERCENTAGE);

    Accordion accordion = new Accordion();
    accordion.setHeight(100f, Layout.UNITS_PERCENTAGE);

    corpusList = new CorpusListPanel(this);

    searchOptions = new SearchOptionsPanel();

    queryPanel = new QueryPanel(this);
    queryPanel.setHeight(18f, Layout.UNITS_EM);

    accordion.addTab(corpusList, "Corpus List", null);
    accordion.addTab(searchOptions, "Search Options", null);
    accordion.addTab(new ExportPanel(queryPanel, corpusList), "Export", null);


    addComponent(queryPanel);
    addComponent(accordion);

    layout.setExpandRatio(accordion, 1.0f);
  }

  @Override
  public void attach()
  {
    super.attach();
    this.window = getWindow();
  }

  public void setQuery(String query, Map<String, AnnisCorpus> corpora)
  {
    if (queryPanel != null && corpusList != null)
    {
      queryPanel.setQuery(query);
      if (corpora != null)
      {
        corpusList.selectCorpora(corpora);
      }
    }
  }

  public void setQuery(String query, Map<String, AnnisCorpus> corpora,
    int contextLeft, int contextRight)
  {
    setQuery(query, corpora);
    searchOptions.setLeftContext(contextLeft);
    searchOptions.setRightContext(contextRight);
  }

  public Map<String, AnnisCorpus> getSelectedCorpora()
  {
    return corpusList.getSelectedCorpora();
  }

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    super.paintContent(target);
  }

  public void executeQuery()
  {
    if (getApplication() != null && getApplication().getUser() == null)
    {
      getWindow().showNotification("Please login first",
        Window.Notification.TYPE_WARNING_MESSAGE);
    }
    else if (getApplication() != null && corpusList != null && queryPanel
      != null)
    {

      Map<String, AnnisCorpus> rawCorpusSelection =
        corpusList.getSelectedCorpora();

      // filter corpus selection by logged in user
      lastCorpusSelection = new TreeMap<String, AnnisCorpus>(rawCorpusSelection);
      AnnisUser user = (AnnisUser) getApplication().getUser();
      if (user != null)
      {
        lastCorpusSelection.keySet().retainAll(user.getCorpusNameList());
      }
      lastQuery = queryPanel.getQuery();
      if (lastCorpusSelection.isEmpty())
      {
        getWindow().showNotification("Please select a corpus",
          Window.Notification.TYPE_WARNING_MESSAGE);
        return;
      }
      if ("".equals(lastQuery))
      {
        getWindow().showNotification("Empty query",
          Window.Notification.TYPE_WARNING_MESSAGE);
        return;
      }

      HistoryEntry e = new HistoryEntry();
      e.setQuery(lastQuery);
      e.setCorpora(getSelectedCorpora());

      // remove it first in order to let it appear on the beginning of the list
      history.remove(e);
      history.add(0, e);

      queryPanel.updateShortHistory(history.asList());

      queryPanel.setCountIndicatorEnabled(true);
      CountThread countThread = new CountThread();
      countThread.start();

      searchWindow.showQueryResult(lastQuery, lastCorpusSelection,
        searchOptions.getLeftContext(), searchOptions.getRightContext(),
        searchOptions.getResultsPerPage());


    }
  }

  public Set<HistoryEntry> getHistory()
  {
    return history;
  }

  private class CountThread extends Thread
  {

    private int count = -1;

    @Override
    public void run()
    {
      WebResource res = Helper.getAnnisWebResource(getApplication());
      //AnnisService service = Helper.getService(getApplication(), window);
      if (res != null)
      {
        try
        {
          Set<String> corpusNames = new TreeSet<String>();
          for(AnnisCorpus c : lastCorpusSelection.values())
          {
            corpusNames.add(c.getName());
          }
          
          count = Integer.parseInt(res.path("search").path("count").queryParam(
            "q", lastQuery).queryParam("corpora",
            StringUtils.join(corpusNames, ",")).get(
            String.class));
        }
        catch (UniformInterfaceException ex)
        {
          if (ex.getResponse().getStatus() == 400)
          {
            window.showNotification(
              ex.getResponse().getEntity(String.class), "parsing error",
              Window.Notification.TYPE_ERROR_MESSAGE);
          }
          else
          {
            window.showNotification(
              ex.getResponse().getEntity(String.class), "unknown error " + ex.
              getResponse().getStatus(),
              Window.Notification.TYPE_ERROR_MESSAGE);
          }
        }
      }

      queryPanel.setStatus("" + count + " matches");
      searchWindow.updateQueryCount(count);

      queryPanel.setCountIndicatorEnabled(false);
    }

    public int getCount()
    {
      return count;
    }
  }
}
