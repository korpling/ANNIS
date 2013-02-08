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
package annis.gui.resultview;

import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.gui.PluginSystem;
import annis.gui.SearchUI;
import annis.gui.paging.PagingCallback;
import annis.gui.paging.PagingComponent;
import annis.security.AnnisUser;
import annis.service.objects.Match;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class ResultViewPanel extends Panel implements PagingCallback
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ResultViewPanel.class);
  
  public static final String NULL_SEGMENTATION_VALUE = "tokens (default)";
  
  private PagingComponent paging;
  private ResultSetPanel resultPanel;
  private String aql;
  private Set<String> corpora;
  private int contextLeft, contextRight, start, pageSize;
  private AnnisResultQuery query;
  private ProgressIndicator progressResult;
  private PluginSystem ps;
  private MenuItem miTokAnnos;
  private MenuItem miSegmentation;
  private TreeMap<String, Boolean> tokenAnnoVisible;
  private String currentSegmentationLayer;
  private VerticalLayout mainLayout;  
  private SearchUI parent;


  public ResultViewPanel(SearchUI parent, String aql, Set<String> corpora,
    int contextLeft, int contextRight, String segmentationLayer, int start, int pageSize,
    PluginSystem ps)
  {
    this.tokenAnnoVisible = new TreeMap<String, Boolean>();
    this.aql = aql;
    this.corpora = corpora;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
    this.pageSize = pageSize;
    this.ps = ps;
    this.parent = parent;
    // only allow start points at multiples of the page size
    this.start = start - (start % pageSize);
    
    this.currentSegmentationLayer = segmentationLayer;

    setSizeFull();

    mainLayout = new VerticalLayout();
    setContent(mainLayout);
    mainLayout.setMargin(false);
    mainLayout.setSizeFull();
    
    
    MenuBar mbResult = new MenuBar();
    mbResult.setWidth("100%");
    
    miSegmentation = mbResult.addItem("Base text", null);
    
    miTokAnnos = mbResult.addItem("Token Annotations", null);

    paging = new PagingComponent(start, pageSize);
    paging.setInfo("Result for query \"" + aql.replaceAll("\n", " ") + "\"");
    paging.addCallback((PagingCallback) this);
    
    mainLayout.addComponent(mbResult);
    mainLayout.addComponent(paging);
    
    mainLayout.setSizeFull();
    
    progressResult = new ProgressIndicator();
    progressResult.setIndeterminate(true);
    progressResult.setEnabled(false);
    progressResult.setPollingInterval(250);
    progressResult.setCaption("Searching for \"" + aql.replaceAll("\n", " ") + "\"");
    
    mainLayout.addComponent(progressResult);
    
    mainLayout.setComponentAlignment(paging, Alignment.TOP_CENTER);
    mainLayout.setComponentAlignment(progressResult, Alignment.MIDDLE_CENTER);
    
    mainLayout.setExpandRatio(mbResult, 0.0f);
    mainLayout.setExpandRatio(paging, 0.0f);
    mainLayout.setExpandRatio(progressResult, 1.0f);
    
    try
    {
      query = new AnnisResultQuery(corpora, aql);
      createPage(start, pageSize);
    
    }
    catch (Exception ex)
    {
      log.error("something failed", ex);
    }
  }

  public void setCount(int count)
  {
    paging.setCount(count, false);
    paging.setStartNumber(start);
  }

  @Override
  public void createPage(final int start, final int limit)
  {
    parent.updateFragment(aql, corpora, contextLeft, contextRight, currentSegmentationLayer, start,
      limit);
    
    if (query != null)
    {
      progressResult.setEnabled(true);
      progressResult.setVisible(true);
      if (resultPanel != null)
      {
        resultPanel.setVisible(false);
      }
      
      final ResultViewPanel finalThis = this;

      
      Callable<List<Match>> r = new Callable<List<Match>>() 
      {
 
        @Override
        public List<Match> call()
        {
          VaadinSession session = VaadinSession.getCurrent();
          try
          {

            AnnisUser user = session.getAttribute(AnnisUser.class);
            return query.loadBeans(start, limit, user);
          }
          catch (AnnisQLSemanticsException ex)
          {
            session.lock();
            try 
            {
              paging.setInfo("Semantic error: " + ex.getLocalizedMessage());
            }
            finally
            {
              session.unlock();
            }
          }
          catch (AnnisQLSyntaxException ex)
          {
            session.lock();
            try
            {
              paging.setInfo("Syntax error: " + ex.getLocalizedMessage());
            }
            finally
            {
              session.unlock();
            }
          }
          catch (AnnisCorpusAccessException ex)
          {
            session.lock();
            try
            {
              paging.setInfo("Corpus access error: " + ex.getLocalizedMessage());
            }
            finally
            {
              session.unlock();
            }
          }
          catch (Exception ex)
          {
            log.error(
              "unknown exception in result view", ex);
            session.lock();
            try
            {
              paging.setInfo("unknown exception: " + ex.getLocalizedMessage());
            }
            finally
            {
              session.unlock();
            }
          }
          finally
          {
            session.lock();
            try
            {
              progressResult.setVisible(false);
              progressResult.setEnabled(false);
            }
            finally
            {
              session.unlock();
            }
          }        
          return null;  
        }
      };
      
      FutureTask<List<Match>> task = new FutureTask<List<Match>>(r)
      {
        @Override
        protected void done()
        {
          if(isCancelled())
          {
            return;
          }
          
          VaadinSession session = VaadinSession.getCurrent();

          try
          {
            List<Match> result = get();
            if(result == null)
            {
              return;
            }
            
            session.lock();
            try
            {
              if (resultPanel != null)
              {
                mainLayout.removeComponent(resultPanel);
              }
              
              progressResult.setEnabled(false);              
              progressResult.setVisible(false);
              //mainLayout.setExpandRatio(progressResult, 0.0f);
              
              if(result.size() > 0)
              {
                resultPanel = new ResultSetPanel(result, ps,
                  contextLeft, contextRight,
                  currentSegmentationLayer, finalThis, start);

                mainLayout.addComponent(resultPanel);
                mainLayout.setExpandRatio(resultPanel, 1.0f);
                mainLayout.setComponentAlignment(resultPanel, Alignment.TOP_CENTER);

                resultPanel.setVisible(true);
              }
              else
              {
                // nothing to show since we have an empty result
                Label lblNoResult = new Label("No matches found.");
                lblNoResult.setSizeUndefined();
                mainLayout.addComponent(lblNoResult);
                mainLayout.setComponentAlignment(lblNoResult, Alignment.MIDDLE_CENTER);
                mainLayout.setExpandRatio(lblNoResult, 1.0f);
              }
            }
            finally
            {
              session.unlock();
            }
            
          }
          catch (Exception ex)
          {
            log.error("Could not get result of future task", ex);
            session.lock();
            try 
            {
              paging.setInfo("unknown exception: " + ex.getLocalizedMessage());
            }
            finally
            {
              session.unlock();
            }
          }
          
        }
      };
      
      Executor exec = Executors.newSingleThreadExecutor();
      exec.execute(task);
      
      
    }
  }

  public Set<String> getVisibleTokenAnnos()
  {
    TreeSet<String> result = new TreeSet<String>();

    for (Entry<String, Boolean> e : tokenAnnoVisible.entrySet())
    {
      if (e.getValue().booleanValue() == true)
      {
        result.add(e.getKey());
      }
    }

    return result;
  }

  public void updateSegmentationLayer(Set<String> segLayers)
  {
    miSegmentation.removeChildren();
    
    segLayers.add("");
    
    for(String s : segLayers)
    {
      MenuItem miSingleSegLayer = 
        miSegmentation.addItem((s == null || "".equals(s)) ?  NULL_SEGMENTATION_VALUE : s, 
        new MenuBar.Command() 
      {

        @Override
        public void menuSelected(MenuItem selectedItem)
        {
          currentSegmentationLayer = selectedItem.getText();
          if(NULL_SEGMENTATION_VALUE.equals(currentSegmentationLayer))
          {
            currentSegmentationLayer = null;
          }
          for(MenuItem mi : miSegmentation.getChildren())
          {
            mi.setChecked(mi == selectedItem);
          }
          
          resultPanel.setSegmentationLayer(currentSegmentationLayer);
        }
      });
     
      miSingleSegLayer.setCheckable(true);
      miSingleSegLayer.setChecked(
        (currentSegmentationLayer == null && "".equals(s)) 
        || s.equals(currentSegmentationLayer));
    }
  }

  public void updateTokenAnnos(Set<String> tokenAnnotationLevelSet)
  {
    // add new annotations
    for (String s : tokenAnnotationLevelSet)
    {
      if (!tokenAnnoVisible.containsKey(s))
      {
        tokenAnnoVisible.put(s, Boolean.TRUE);
      }
    }

    miTokAnnos.removeChildren();

    for (String a : tokenAnnotationLevelSet)
    {
      MenuItem miSingleTokAnno = miTokAnnos.addItem(a, new MenuBar.Command()
      {

        @Override
        public void menuSelected(MenuItem selectedItem)
        {

          if (selectedItem.isChecked())
          {
            tokenAnnoVisible.put(selectedItem.getText(), Boolean.TRUE);
          }
          else
          {
            tokenAnnoVisible.put(selectedItem.getText(), Boolean.FALSE);
          }

          resultPanel.setVisibleTokenAnnosVisible(getVisibleTokenAnnos());
        }
      });

      miSingleTokAnno.setCheckable(true);
      miSingleTokAnno.setChecked(tokenAnnoVisible.get(a).booleanValue());

    }

  }

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    super.paintContent(target);
  }
}
