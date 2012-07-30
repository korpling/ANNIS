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

import annis.CommonHelper;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.gui.CitationWindow;
import annis.gui.PluginSystem;
import annis.gui.paging.PagingCallback;
import annis.gui.paging.PagingComponent;
import annis.security.AnnisUser;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.Match;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class ResultViewPanel extends Panel implements PagingCallback
{
  private PagingComponent paging;
  private ResultSetTable resultPanel;
  private String aql;
  private Map<String, AnnisCorpus> corpora;
  private int contextLeft, contextRight, pageSize;
  private AnnisResultQuery query;
  private ProgressIndicator progressResult;
  private PluginSystem ps;
  private MenuItem miTokAnnos;
  private MenuItem miSegmentation;
  private TreeMap<String, Boolean> tokenAnnoVisible;
  private String currentSegmentationLayer;
  private VerticalLayout mainLayout;

  public ResultViewPanel(String aql, Map<String, AnnisCorpus> corpora,
    int contextLeft, int contextRight, String segmentationLayer, int pageSize,
    PluginSystem ps)
  {
    this.tokenAnnoVisible = new TreeMap<String, Boolean>();
    this.aql = aql;
    this.corpora = corpora;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
    this.pageSize = pageSize;
    this.ps = ps;
    
    this.currentSegmentationLayer = segmentationLayer;

    setSizeFull();

    mainLayout = (VerticalLayout) getContent();
    mainLayout.setMargin(false);
    mainLayout.setSizeFull();

    MenuBar mbResult = new MenuBar();
    mbResult.setWidth("100%");
    
    miSegmentation = mbResult.addItem("Segmentation layer", null);
    
    miTokAnnos = mbResult.addItem("Token Annotations", null);

    mbResult.addItem("Show Citation URL", new MenuBar.Command()
    {

      @Override
      public void menuSelected(MenuItem selectedItem)
      {
        showCitationURLWindow();
      }
    });

    paging = new PagingComponent(0, pageSize);
    paging.setInfo("Result for query \"" + aql.replaceAll("\n", " ") + "\"");
    paging.addCallback((PagingCallback) this);

    
    mainLayout.addComponent(mbResult);
    mainLayout.addComponent(paging);
    
    mainLayout.setSizeFull();
    
    progressResult = new ProgressIndicator();
    progressResult.setIndeterminate(true);
    progressResult.setEnabled(false);

    mainLayout.addComponent(progressResult);
  }

  @Override
  public void attach()
  {
    query = new AnnisResultQuery(corpora.keySet(), aql, getApplication());
    createPage(0, pageSize);

    super.attach();
  }

  public void setCount(int count)
  {
    paging.setCount(count, false);
  }

  @Override
  public void createPage(final int start, final int limit)
  {

    if (query != null)
    {
      progressResult.setEnabled(true);
      progressResult.setVisible(true);
      if (resultPanel != null)
      {
        resultPanel.setVisible(false);
      }

      Runnable r = new Runnable()
      {

        @Override
        public void run()
        {
          try
          {

            AnnisUser user = null;
            synchronized(getApplication()) 
            {
              if (getApplication() != null)
              {
                user = (AnnisUser) getApplication().getUser();
              }
            }
            
            List<Match> result = query.loadBeans(start, limit, user);

            synchronized(getApplication()) 
            {
              // TODO: update menu when first real subgraph was queried
//              updateSegmentationLayer(result);
//              updateTokenAnnos(result);

              if (resultPanel != null)
              {
                mainLayout.removeComponent(resultPanel);
              }
              resultPanel = new ResultSetTable(result, start, ps,
                getVisibleTokenAnnos(), currentSegmentationLayer);

              mainLayout.addComponent(resultPanel);
              mainLayout.setExpandRatio(resultPanel, 1.0f);

              resultPanel.setVisible(true);
            }
            
          }
          catch (AnnisQLSemanticsException ex)
          {
            synchronized(getApplication()) 
            {
              paging.setInfo("Semantic error: " + ex.getLocalizedMessage());
            }
          }
          catch (AnnisQLSyntaxException ex)
          {
            synchronized(getApplication()) 
            {
              paging.setInfo("Syntax error: " + ex.getLocalizedMessage());
            }
          }
          catch (AnnisCorpusAccessException ex)
          {
            synchronized(getApplication()) 
            {
              paging.setInfo("Corpus access error: " + ex.getLocalizedMessage());
            }
          }
          catch (Exception ex)
          {
            Logger.getLogger(ResultViewPanel.class.getName()).log(Level.SEVERE,
              "unknown exception in result view", ex);
            synchronized(getApplication()) 
            {
              paging.setInfo("unknown exception: " + ex.getLocalizedMessage());
            }
          }
          finally
          {
            synchronized(getApplication()) 
            {
              progressResult.setVisible(false);
              progressResult.setEnabled(false);
            }
          }
        }
      };
      Thread t = new Thread(r);
      t.start();

    }
  }

  private Set<String> getVisibleTokenAnnos()
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

  private void showCitationURLWindow()
  {
    final Window w =
      new CitationWindow(
      getApplication(),
      aql, corpora, contextLeft, contextRight);

    getWindow().addWindow(w);
    w.center();
  }
  
  private void updateSegmentationLayer(SaltProject p)
  {
    miSegmentation.removeChildren();
    
    Set<String> segLayers = CommonHelper.getOrderingTypes(p);
    
    segLayers.add("");
    
    for(String s : segLayers)
    {
      MenuItem miSingleSegLayer = miSegmentation.addItem("".equals(s) ? "<default>" : s, 
        new MenuBar.Command() 
      {

        @Override
        public void menuSelected(MenuItem selectedItem)
        {
          currentSegmentationLayer = selectedItem.getText();
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

  private void updateTokenAnnos(SaltProject p)
  {
    Set<String> tokenAnnotationLevelSet = CommonHelper.
      getTokenAnnotationLevelSet(p);


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
