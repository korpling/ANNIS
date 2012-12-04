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

import annis.gui.Helper;
import annis.gui.SearchWindow;
import annis.gui.beans.HistoryEntry;
import annis.service.objects.MatchAndDocumentCount;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Set;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;

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
  private Set<String> lastCorpusSelection;
  private SearchOptionsPanel searchOptions;
  private ListOrderedSet<HistoryEntry> history;

  public ControlPanel(SearchWindow searchWindow)
  {
    super("Search Form");
    this.searchWindow = searchWindow;
    this.history = new ListOrderedSet<HistoryEntry>();

    setSizeFull();
    
    setStyleName(ChameleonTheme.PANEL_BORDERLESS);
    addStyleName("control");

    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setSizeFull();
    
    Accordion accordion = new Accordion();
    accordion.setHeight(100f, Layout.UNITS_PERCENTAGE);
    accordion.setWidth(100f, Layout.UNITS_PERCENTAGE);

    corpusList = new CorpusListPanel(this);

    searchOptions = new SearchOptionsPanel();

    queryPanel = new QueryPanel(this);
    queryPanel.setHeight(18f, Layout.UNITS_EM);

    accordion.addTab(corpusList, "Corpus List", null);
    accordion.addTab(searchOptions, "Search Options", null);
    accordion.addTab(new ExportPanel(queryPanel, corpusList), "Export", null);
    accordion.addTab(new FrequencyQueryPanel(), "Frequency analysis", null);

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

  public void setQuery(String query, Set<String> corpora)
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

  public void setQuery(String query, Set<String> corpora,
    int contextLeft, int contextRight)
  {
    setQuery(query, corpora);
    searchOptions.setLeftContext(contextLeft);
    searchOptions.setRightContext(contextRight);
  }

  public Set<String> getSelectedCorpora()
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
    if (getApplication() != null && corpusList != null && queryPanel
      != null)
    {
 
      lastQuery = queryPanel.getQuery();
      lastCorpusSelection = corpusList.getSelectedCorpora();
      
      if (lastCorpusSelection == null || lastCorpusSelection.isEmpty())
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
      e.setCorpora(lastCorpusSelection);

      // remove it first in order to let it appear on the beginning of the list
      history.remove(e);
      history.add(0, e);

      queryPanel.updateShortHistory(history.asList());

      queryPanel.setCountIndicatorEnabled(true);
      CountThread countThread = new CountThread();
      countThread.start();

      searchWindow.showQueryResult(lastQuery, lastCorpusSelection,
        searchOptions.getLeftContext(), searchOptions.getRightContext(),
        searchOptions.getSegmentationLayer(),
        searchOptions.getResultsPerPage());


    }
  }

  public Set<HistoryEntry> getHistory()
  {
    return history;
  }
  
  public void corpusSelectionChanged()
  {
    searchOptions.updateSegmentationList(corpusList.getSelectedCorpora());
  }

  private class CountThread extends Thread
  {

    private MatchAndDocumentCount count = null;

    @Override
    public void run()
    {
      WebResource res = null;
      synchronized(getApplication()) 
      {
        res = Helper.getAnnisWebResource(getApplication());
      }
      
      //AnnisService service = Helper.getService(getApplication(), window);
      if (res != null)
      {
        try
        {
          count = res.path("query").path("search").path("count").queryParam(
            "q", lastQuery).queryParam("corpora",
            StringUtils.join(lastCorpusSelection, ",")).get(MatchAndDocumentCount.class);
        }
        catch (UniformInterfaceException ex)
        {
          synchronized(getApplication()) 
          {
            if (ex.getResponse().getStatus() == 400)
            {
              window.showNotification(
                "parsing error",
                ex.getResponse().getEntity(String.class),
                Window.Notification.TYPE_WARNING_MESSAGE);
            }
            else if(ex.getResponse().getStatus() == 504) // gateway timeout
            {
               window.showNotification(
                "Timeout: query execution took too long.",
                "Try to simplyfiy your query e.g. by replacing \"node\" with an annotation name or adding more constraints between the nodes.",
                Window.Notification.TYPE_WARNING_MESSAGE);
            }
            else
            {
              window.showNotification(
                "unknown error " + ex.
                getResponse().getStatus(),
                ex.getResponse().getEntity(String.class),
                Window.Notification.TYPE_WARNING_MESSAGE);
            }
          }
        }
      }

      synchronized(getApplication()) 
      {
        queryPanel.setCountIndicatorEnabled(false);
        if(count != null)
        {
          String documentString = count.getDocumentCount() > 1 ? "documents" : "document";
          String matchesString = count.getMatchCount() > 1 ? "matches" : "match";
          
          queryPanel.setStatus("" + count.getMatchCount() + " " + matchesString
            + " <br/>in " + count.getDocumentCount() + " " + documentString );
          searchWindow.updateQueryCount(count.getMatchCount());
        }
      }
    }

    public int getCount()
    {
      return count.getMatchCount();
    }
  }
}
