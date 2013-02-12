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
package annis.gui;

import annis.gui.beans.HistoryEntry;
import annis.gui.controlpanel.ControlPanel;
import annis.service.objects.MatchAndDocumentCount;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;

/**
 * Manages all the query related actions.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class QueryController
{
  private SearchUI ui;
  
  private String lastQuery = "";
  private Set<String> lastCorpusSelection = new HashSet<String>();
  private ListOrderedSet<HistoryEntry> history;
  
  
  public QueryController(SearchUI ui)
  {
    this.ui = ui;    
    this.history = new ListOrderedSet<HistoryEntry>();
  }
  
  public void updateCorpusSetList()
  {
    ui.getControlPanel().getCorpusList().updateCorpusSetList();
  }
  
  
  public void setQuery(String query, Set<String> corpora,
    int contextLeft, int contextRight)
  {
    setQuery(query, corpora);
    ui.getControlPanel().getSearchOptions().setLeftContext(contextLeft);
    ui.getControlPanel().getSearchOptions().setRightContext(contextRight);
  }
  
  public void setQuery(String query, Set<String> corpora)
  {
    ui.getControlPanel().getQueryPanel().setQuery(query);
    if (corpora != null)
    {
      ui.getControlPanel().getCorpusList().selectCorpora(corpora);
    }
  }
  
  public void corpusSelectionChanged()
  {
    ui.getControlPanel().getSearchOptions()
      .updateSegmentationList(ui.getControlPanel().getCorpusList().getSelectedCorpora());
  }
  
  public void offsetLimitChanged(int offset, int limit)
  {
    // TODO: update the fragment (vaadin7)
  }
  
   public void executeQuery()
  {
    executeQuery(false);
  }

  public void executeQuery(boolean onlyCount)
  {

    lastQuery = ui.getControlPanel().getQueryPanel().getQuery();
    lastCorpusSelection = ui.getControlPanel().getCorpusList().getSelectedCorpora();

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

    ui.getControlPanel().getQueryPanel().updateShortHistory(history.asList());

    ui.getControlPanel().getQueryPanel().setCountIndicatorEnabled(true);
    CountThread countThread = new CountThread();
    countThread.start();

    if(!onlyCount)
    {
      ui.showQueryResult(lastQuery, lastCorpusSelection,
        ui.getControlPanel().getSearchOptions().getLeftContext(), 
        ui.getControlPanel().getSearchOptions().getRightContext(),
        ui.getControlPanel().getSearchOptions().getSegmentationLayer(),
        0, ui.getControlPanel().getSearchOptions().getResultsPerPage());
    }
  }
  
  public Set<String> getSelectedCorpora()
  {
    return ui.getControlPanel().getCorpusList().getSelectedCorpora();
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
        ui.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);
        if(count != null)
        {
          String documentString = count.getDocumentCount() > 1 ? "documents" : "document";
          String matchesString = count.getMatchCount() > 1 ? "matches" : "match";
          
          ui.getControlPanel().getQueryPanel().setStatus("" + count.getMatchCount() + " " + matchesString
            + " <br/>in " + count.getDocumentCount() + " " + documentString );
          ui.updateQueryCount(count.getMatchCount());
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
