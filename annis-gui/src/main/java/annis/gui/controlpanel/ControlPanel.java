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
import annis.gui.InstanceConfig;
import annis.gui.SearchUI;
import annis.gui.beans.HistoryEntry;
import annis.service.objects.MatchAndDocumentCount;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Set;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author thomas
 */
public class ControlPanel extends Panel
{

  private static final Logger log = LoggerFactory.getLogger(ControlPanel.class);
  
  private static final long serialVersionUID = -2220211539424865671L;
  private QueryPanel queryPanel;
  private CorpusListPanel corpusList;
  private SearchUI searchUI;
  private String lastQuery;
  private Set<String> lastCorpusSelection;
  private SearchOptionsPanel searchOptions;
  private ListOrderedSet<HistoryEntry> history;
  
  public ControlPanel(SearchUI searchUI, InstanceConfig instanceConfig)
  {
    super("Search Form");
    this.searchUI = searchUI;
    this.history = new ListOrderedSet<HistoryEntry>();

    setSizeFull();
    
    setStyleName(ChameleonTheme.PANEL_BORDERLESS);
    addStyleName("control");

    VerticalLayout layout = new VerticalLayout();
    setContent(layout);
    layout.setSizeFull();
    
    Accordion accordion = new Accordion();
    accordion.setHeight(100f, Layout.UNITS_PERCENTAGE);
    accordion.setWidth(100f, Layout.UNITS_PERCENTAGE);

    corpusList = new CorpusListPanel(this, instanceConfig);

    searchOptions = new SearchOptionsPanel();

    queryPanel = new QueryPanel(this);
    queryPanel.setHeight(18f, Layout.UNITS_EM);

    accordion.addTab(corpusList, "Corpus List", null);
    accordion.addTab(searchOptions, "Search Options", null);
    accordion.addTab(new ExportPanel(queryPanel, corpusList), "Export", null);

    layout.addComponent(queryPanel);
    layout.addComponent(accordion);

    layout.setExpandRatio(accordion, 1.0f);
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
  
  public void executeCount(String aql, Set<String> corpora)
  {
    queryPanel.setQuery(aql);
    corpusList.selectCorpora(corpora);
    
    executeQuery(true);
  }
  
  public void executeQuery()
  {
    executeQuery(false);
  }

  public void executeQuery(boolean onlyCount)
  {
    if (searchUI != null && corpusList != null && queryPanel
      != null)
    {
 
      lastQuery = queryPanel.getQuery();
      lastCorpusSelection = corpusList.getSelectedCorpora();
      
      if (lastCorpusSelection == null || lastCorpusSelection.isEmpty())
      {
        Notification.show("Please select a corpus",
          Notification.Type.WARNING_MESSAGE);
        return;
      }
      if ("".equals(lastQuery))
      {
        Notification.show("Empty query", Notification.Type.WARNING_MESSAGE);
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
      
      if(!onlyCount)
      {
        searchUI.showQueryResult(lastQuery, lastCorpusSelection,
          searchOptions.getLeftContext(), searchOptions.getRightContext(),
          searchOptions.getSegmentationLayer(),
          0, searchOptions.getResultsPerPage());
      }

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

      res = Helper.getAnnisWebResource();

      VaadinSession session = VaadinSession.getCurrent();
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
          
          session.lock();
          try
          {
            if (ex.getResponse().getStatus() == 400)
            {
              Notification.show(
                "parsing error",
                ex.getResponse().getEntity(String.class),
                Notification.Type.WARNING_MESSAGE);
            }
            else if(ex.getResponse().getStatus() == 504) // gateway timeout
            {
              Notification.show(
                "Timeout: query execution took too long.",
                "Try to simplyfiy your query e.g. by replacing \"node\" with an annotation name or adding more constraints between the nodes.",
                Notification.Type.WARNING_MESSAGE);
            }
            else
            {
              Notification.show(
                "unknown error " + ex.
                getResponse().getStatus(),
                ex.getResponse().getEntity(String.class),
                Notification.Type.WARNING_MESSAGE);
            }
          }
          finally
          {
            session.unlock();
          }
        }
      }

      session.lock();
      try
      {
        queryPanel.setCountIndicatorEnabled(false);
        if(count != null)
        {
          String documentString = count.getDocumentCount() > 1 ? "documents" : "document";
          String matchesString = count.getMatchCount() > 1 ? "matches" : "match";
          
          queryPanel.setStatus("" + count.getMatchCount() + " " + matchesString
            + " <br/>in " + count.getDocumentCount() + " " + documentString );
          searchUI.updateQueryCount(count.getMatchCount());
        }
      }
      finally
      {
        session.unlock();
      }
    }

    public int getCount()
    {
      return count.getMatchCount();
    }
  }
}
