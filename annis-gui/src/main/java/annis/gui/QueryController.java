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

import annis.libgui.Helper;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.gui.beans.HistoryEntry;
import annis.libgui.media.MediaController;
import annis.gui.model.PagedResultQuery;
import annis.gui.model.Query;
import annis.gui.paging.PagingCallback;
import annis.gui.paging.PagingComponent;
import annis.gui.resultview.AnnisResultQuery;
import annis.gui.resultview.ResultViewPanel;
import annis.libgui.visualizers.IFrameResourceMap;
import annis.libgui.AnnisUser;
import annis.service.objects.Match;
import annis.service.objects.MatchAndDocumentCount;
import com.github.wolfie.refresher.Refresher;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

/**
 * Manages all the query related actions.
 * 
 * <strong>This class is not reentrant.</strong>
 * It is expected that you call the functions from the Vaadin session lock context,
 * either implicitly (e.g. from a component constructor or a handler callback)
 * or explicitly with {@link VaadinSession#lock() }.
 * 
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class QueryController implements PagingCallback, Refresher.RefreshListener
{
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ResultViewPanel.class);

  private SearchUI ui;
  
  private PagedResultQuery lastQuery;
  private ListOrderedSet<HistoryEntry> history;
  private ResultViewPanel lastResultView;
  private AnnisResultQuery resultFetcher;
  private MatchAndDocumentCount lastCount;
  
  private FutureTask<List<Match>> resultTask;
  private FutureTask<MatchAndDocumentCount> countTask;
 
  
  public QueryController(SearchUI ui)
  {
    this.ui = ui;    
    this.history = new ListOrderedSet<HistoryEntry>();
    this.resultTask = null;
  }
  
  public void updateCorpusSetList()
  {
    ui.getControlPanel().getCorpusList().updateCorpusSetList();
  }
  
  public void setQuery(String query)
  {
    setQuery(new Query(query, ui.getControlPanel().getCorpusList().getSelectedCorpora()));
  }
  
  public void setQuery(Query query)
  {
    PagedResultQuery paged = new PagedResultQuery(
      ui.getControlPanel().getSearchOptions().getLeftContext(),
      ui.getControlPanel().getSearchOptions().getRightContext(),
      0,
      ui.getControlPanel().getSearchOptions().getResultsPerPage(),
      ui.getControlPanel().getSearchOptions().getSegmentationLayer(),
      query.getQuery(), query.getCorpora()
    );
    setQuery(paged);
  }
  
  public void setQuery(PagedResultQuery query)
  {
    // only allow offset at multiples of the limit size
    query.setOffset(query.getOffset() - (query.getOffset() % query.getLimit()));
    
    lastQuery = query;
    
    
    ui.getControlPanel().getQueryPanel().setQuery(query.getQuery());
    ui.getControlPanel().getSearchOptions().setLeftContext(query.
      getContextLeft());
    ui.getControlPanel().getSearchOptions().setRightContext(query.
      getContextRight());
    ui.getControlPanel().getSearchOptions().setSegmentationLayer(query.
      getSegmentation());

    if (query.getCorpora() != null)
    {
      ui.getControlPanel().getCorpusList().selectCorpora(query.getCorpora());
    }
  }
  
  public void executeQuery()
  {
    executeQuery(true, true);
  }
  
  public void cancelQueries()
  {
    // abort last task if running
    if (resultTask != null && !resultTask.isDone())
    {
      Notification.show("Canceled result query", Notification.Type.TRAY_NOTIFICATION);
      resultTask.cancel(true);
    }
    if(countTask != null && !countTask.isDone())
    {
      Notification.show("Canceled count query", Notification.Type.TRAY_NOTIFICATION);
      countTask.cancel(true);
    }
  }
  
  public void executeQuery(boolean executeCount, boolean executeResult)
  {

    Validate.notNull(lastQuery, "You have to set a query before you can execute it.");

    cancelQueries();
    
    // cleanup resources
    VaadinSession session = VaadinSession.getCurrent();
    session.setAttribute(IFrameResourceMap.class, new IFrameResourceMap());
    if(session.getAttribute(MediaController.class) != null)
    {
      session.getAttribute(MediaController.class).clearMediaPlayers();
    }
    
    
    ui.updateFragment(lastQuery);
    
    HistoryEntry e = new HistoryEntry();
    e.setCorpora(lastQuery.getCorpora());
    e.setQuery(lastQuery.getQuery());
    // remove it first in order to let it appear on the beginning of the list
    history.remove(e);
    history.add(0, e);
    ui.getControlPanel().getQueryPanel().updateShortHistory(history.asList());


    if (lastQuery.getCorpora() == null || lastQuery.getCorpora().isEmpty())
    {
      Notification.show("Please select a corpus",
        Notification.Type.WARNING_MESSAGE);
      return;
    }
    if ("".equals(lastQuery.getQuery()))
    {
      Notification.show("Empty query", Notification.Type.WARNING_MESSAGE);
      return;
    }
    
    
    resultFetcher = null;
    AsyncWebResource asyncRes = Helper.getAnnisAsyncWebResource();
      
    if(executeResult)
    {    
      
      resultFetcher = new AnnisResultQuery(lastQuery.getCorpora(), lastQuery.getQuery());

      // remove old result from view
      if (lastResultView != null)
      {
        ui.getMainTab().removeComponent(lastResultView);
      }
      lastResultView = new ResultViewPanel(this, ui, ui.getInstanceConfig());
      ui.getMainTab().addTab(lastResultView, "Query Result");
      ui.getMainTab().setSelectedTab(lastResultView);

      
      resultTask = new FutureTask<List<Match>>(new ResultCallable(
        resultFetcher, lastQuery.getOffset(), lastQuery.getLimit(), 
        lastResultView.getPaging()))
      {

        @Override
        protected void done()
        {
          if(!isCancelled())
          {
            VaadinSession session = VaadinSession.getCurrent();
            session.lock();
            try
            {
              lastResultView.setResult(get(), lastQuery.getContextLeft(), 
                lastQuery.getContextRight(), lastQuery.getSegmentation(),
                lastQuery.getOffset());
            }
            catch(InterruptedException ex)
            {
              log.error(null, ex);
            }
            catch(ExecutionException ex)
            {
              log.error(null, ex);
            }
            finally
            {
              session.unlock();
            }
          }
        }
        
      };
      
      Executor exec = Executors.newSingleThreadExecutor();
      exec.execute(resultTask);     
    }
    
    if (executeCount)
    {
      // start count query
      ui.getControlPanel().getQueryPanel().setCountIndicatorEnabled(true);
      countTask =
        new FutureTask<MatchAndDocumentCount>(new CountCallable())
      {
        @Override
        protected void done()
        {
          if(!isCancelled())
          {
            VaadinSession session = VaadinSession.getCurrent();
            session.lock();
            try
            { 
              ui.getControlPanel().getQueryPanel().setCountIndicatorEnabled(false);
      
              lastCount = get();
              String documentString = lastCount.getDocumentCount() > 1 ? "documents" : "document";
              String matchesString = lastCount.getMatchCount() > 1 ? "matches" : "match";

              ui.getControlPanel().getQueryPanel().setStatus("" + lastCount.
                getMatchCount() + " " + matchesString
                + " <br/>in " + lastCount.getDocumentCount() + " " + documentString);
              if (lastResultView != null && lastCount.getMatchCount() > 0)
              {
                lastResultView.setCount(lastCount.getMatchCount());
              }
            }
            catch (InterruptedException ex)
            {
              log.info(null, ex);
            }
            catch (ExecutionException ex)
            {
              log.info(null, ex);
            }
            finally
            {
              session.unlock();
            }
          }
        }
      }; // end FutureTask
      Executor exec = Executors.newSingleThreadExecutor();
      exec.execute(countTask);   
    }
  }
  
  public void corpusSelectionChanged()
  {
    ui.getControlPanel().getSearchOptions()
      .updateSegmentationList(ui.getControlPanel().getCorpusList().getSelectedCorpora());
  }
  
  
  public Set<String> getSelectedCorpora()
  {
    return ui.getControlPanel().getCorpusList().getSelectedCorpora();
  }
  
  public PagedResultQuery getQuery()
  {
    return lastQuery;
  }

  @Override
  public void switchPage(int offset, int limit)
  {
    if(lastQuery != null)
    {
      lastQuery.setOffset(offset);
      lastQuery.setLimit(limit);
      
      // execute the result query again
      executeQuery(false, true);
      if(lastResultView != null && lastCount != null)
      {
        lastResultView.setCount(lastCount.getMatchCount());
      }
    }
  }

  @Override
  public void refresh(Refresher source)
  {
    // TODO
  }
  
  private static class ResultCallable implements Callable<List<Match>>
  {

    private AnnisResultQuery resultFetcher;
    private int offset, limit;
    private PagingComponent paging;
    public ResultCallable(AnnisResultQuery resultFetcher, int offset, int limit, 
      PagingComponent paging)
    {
      this.resultFetcher = resultFetcher;
      this.offset = offset;
      this.limit = limit;
      this.paging = paging;
    }
    
    private void showError(String error)
    {
      VaadinSession session = VaadinSession.getCurrent();
      session.lock();
      try
      {
        paging.setInfo(error);
      }
      finally
      {
        session.unlock();
      }
    }
    
    @Override
    public List<Match> call() throws Exception
    {
      try
      {
        AnnisUser user = Helper.getUser();
        return resultFetcher.loadBeans(offset, limit, user);
      }
      catch (AnnisQLSemanticsException ex)
      {
        showError("Semantic error: " + ex.getLocalizedMessage());
      }
      catch (AnnisQLSyntaxException ex)
      {
        showError("Syntax error: " + ex.getLocalizedMessage());
      }
      catch (AnnisCorpusAccessException ex)
      {
        showError("Corpus access error: " + ex.getLocalizedMessage());
      }
      catch (Exception ex)
      {
        log.error(
          "unknown exception in result view", ex);
        showError("unknown exception: " + ex.getLocalizedMessage());
        
      }

      return null;  
    }
    
  }
  
  private class CountCallable implements Callable<MatchAndDocumentCount>
  {

    @Override
    public MatchAndDocumentCount call() throws Exception
    {
      WebResource res = Helper.getAnnisWebResource();
      
      MatchAndDocumentCount c = null;

      VaadinSession session = VaadinSession.getCurrent();
      //AnnisService service = Helper.getService(getApplication(), window);
      if (res != null)
      {
        try
        {
          c = res.path("query").path("search").path("count").queryParam(
            "q", lastQuery.getQuery()).queryParam("corpora",
            StringUtils.join(lastQuery.getCorpora(), ",")).get(MatchAndDocumentCount.class);
          
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
      return c;
    }
  }
    
  
  
}
