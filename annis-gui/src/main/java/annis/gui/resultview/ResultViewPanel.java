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
import annis.libgui.PluginSystem;
import annis.gui.QueryController;
import annis.gui.components.OnLoadCallbackExtension;
import annis.gui.model.PagedResultQuery;
import annis.gui.paging.PagingComponent;
import annis.libgui.Helper;
import annis.libgui.InstanceConfig;
import static annis.gui.controlpanel.SearchOptionsPanel.KEY_DEFAULT_BASE_TEXT_SEGMENTATION;
import annis.libgui.ResolverProviderImpl;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.CorpusConfig;
import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class ResultViewPanel extends VerticalLayout implements
  OnLoadCallbackExtension.Callback
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    ResultViewPanel.class);

  public static final String NULL_SEGMENTATION_VALUE = "tokens (default)";

  private Map<HashSet<SingleResolverRequest>, List<ResolverEntry>> cacheResolver;

  public static final String FILESYSTEM_CACHE_RESULT =
    "ResultSetPanel_FILESYSTEM_CACHE_RESULT";

  private PagingComponent paging;

  private ProgressBar progressResult;

  private PluginSystem ps;

  private MenuItem miTokAnnos;

  private MenuItem miSegmentation;

  private TreeMap<String, Boolean> tokenAnnoVisible;

  private QueryController controller;

  private String selectedSegmentationLayer;

  private Set<String> segmentationLayerSet =
    Collections.synchronizedSet(new TreeSet<String>());

  private Set<String> tokenAnnotationLevelSet =
    Collections.synchronizedSet(new TreeSet<String>());

  private InstanceConfig instanceConfig;

  private CssLayout resultLayout;

  private List<SingleResultPanel> resultPanelList;

  private String segmentationName;

  private int currentResults;
  private int numberOfResults;

  private transient BlockingQueue<SaltProject> projectQueue;

  private PagedResultQuery currentQuery;

  public ResultViewPanel(QueryController controller,
    PluginSystem ps, InstanceConfig instanceConfig)
  {
    this.tokenAnnoVisible = new TreeMap<String, Boolean>();
    this.ps = ps;
    this.controller = controller;
    this.selectedSegmentationLayer = controller.getPreparedQuery().getSegmentation();

    cacheResolver =
      Collections.synchronizedMap(
      new HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>>());

    resultPanelList =
      Collections.synchronizedList(new LinkedList<SingleResultPanel>());


    resultLayout = new CssLayout();
    resultLayout.addStyleName("result-view-css");
    Panel resultPanel = new Panel(resultLayout);
    resultPanel.setSizeFull();
    resultPanel.addStyleName(ChameleonTheme.PANEL_BORDERLESS);


    this.instanceConfig = instanceConfig;

    setSizeFull();
    setMargin(false);


    MenuBar mbResult = new MenuBar();
    mbResult.setWidth("100%");
    addComponent(mbResult);

    miSegmentation = mbResult.addItem("Base text", null);
    miTokAnnos = mbResult.addItem("Token Annotations", null);

    progressResult = new ProgressBar();

    progressResult.setVisible(false);

    addComponent(progressResult);
    addComponent(resultPanel);

    setComponentAlignment(progressResult, Alignment.MIDDLE_CENTER);

    setExpandRatio(mbResult, 0.0f);
    setExpandRatio(progressResult, 0.0f);
    setExpandRatio(resultPanel, 1.0f);

    paging = new PagingComponent();

    addComponent(paging, 1);

    setComponentAlignment(paging, Alignment.TOP_CENTER);
    setExpandRatio(paging, 0.0f);

  }

  public void showMatchSearchInProgress(PagedResultQuery q)
  {
    resultLayout.removeAllComponents();

    progressResult.setIndeterminate(true);
    progressResult.setCaption("Searching for \"" + q.getQuery().replaceAll("\n",
      " ") + "\"");
    progressResult.setVisible(true);
    setExpandRatio(progressResult, 1.0f);
  
    segmentationName = q.getSegmentation();
  }

  public void showNoResult()
  {
    resultLayout.removeAllComponents();
    currentResults = 0;

    progressResult.setCaption("");
    progressResult.setVisible(false);

    // nothing to show since we have an empty result
    Label lblNoResult = new Label("No matches found.");
    lblNoResult.setSizeUndefined();
    resultLayout.addComponent(lblNoResult);
    setComponentAlignment(lblNoResult, Alignment.MIDDLE_CENTER);
    setExpandRatio(lblNoResult, 1.0f);
  }

  public void showSubgraphSearchInProgress(PagedResultQuery q, float percent)
  {
    if (percent == 0.0f)
    {
      resultLayout.removeAllComponents();
      currentResults = 0;
    }

    progressResult.setIndeterminate(false);
    progressResult.setCaption("");
    progressResult.setVisible(true);
    setExpandRatio(progressResult, 0.0f);
    progressResult.setValue(percent);
  }

  public void setQueryResultQueue(BlockingQueue<SaltProject> queue,
    PagedResultQuery q, int numberOfResults)
  {
    this.projectQueue = queue;
    this.currentQuery = q;
    this.numberOfResults = numberOfResults;

    paging.setPageSize(q.getLimit(), false);
    paging.setInfo(q.getQuery());

    resultLayout.removeAllComponents();
    resultPanelList.clear();

    Set<String> corpora = q.getCorpora();

    if (corpora.size() == 1)
    {
      CorpusConfig corpusConfig = Helper.getCorpusConfig(corpora.iterator().
        next());
      if (corpusConfig != null && corpusConfig.getConfig() != null
        && corpusConfig.getConfig().containsKey(
        KEY_DEFAULT_BASE_TEXT_SEGMENTATION))
      {
        this.selectedSegmentationLayer = corpusConfig.getConfig(
          KEY_DEFAULT_BASE_TEXT_SEGMENTATION);
      }
    }

    // get the first query result
    SaltProject first = queue.poll();
    Preconditions.checkState(first != null,
      "There must be already an element in the queue");

    addQueryResult(q, first);
  }

  private void resetQueryResultQueue()
  {
    this.projectQueue = null;
    this.currentQuery = null;
    this.currentResults = 0;
    this.numberOfResults = 0;
  }

  private void addQueryResult(PagedResultQuery q, SaltProject p)
  {
    if(q == null)
    {
      return;
    }
    
    List<SingleResultPanel> newPanels = new LinkedList<SingleResultPanel>();
    try
    {
      if (p == null)
      {
        Notification.show("Could not get subgraphs",
          Notification.Type.TRAY_NOTIFICATION);
      }
      else
      {
        updateVariables(p);
        newPanels = createPanels(p, q.getOffset() + currentResults);
        currentResults += newPanels.size();
        
        if(currentResults == numberOfResults)
        {
          resetQueryResultQueue();
        }
     
        for (SingleResultPanel panel : newPanels)
        {
          resultPanelList.add(panel);
          resultLayout.addComponent(panel);
        }

        if (projectQueue != null && !newPanels.isEmpty() && currentResults < numberOfResults)
        {
          // add a callback so we can load the next single result
          OnLoadCallbackExtension ext = new OnLoadCallbackExtension(this, 250);
          ext.extend(newPanels.get(newPanels.size() - 1));
        }
        
      }
    }
    catch (Throwable ex)
    {
      log.error(null, ex);
    }

    

  }

  public void showFinishedSubgraphSearch()
  {
    progressResult.setVisible(false);
  }

  private List<SingleResultPanel> createPanels(SaltProject p, int offset)
  {
    List<SingleResultPanel> result = new LinkedList<SingleResultPanel>();

    int i = 0;
    for (SCorpusGraph corpusGraph : p.getSCorpusGraphs())
    {
      SingleResultPanel panel = new SingleResultPanel(corpusGraph.
        getSDocuments().get(0),
        i + offset, new ResolverProviderImpl(cacheResolver), ps,
        tokenAnnotationLevelSet, segmentationName,
        instanceConfig);
      i++;

      panel.setWidth("100%");
      panel.setHeight("-1px");

      OnLoadCallbackExtension ext = new OnLoadCallbackExtension(this);
      ext.extend(panel);
      result.add(panel);
    }
    return result;
  }

  private void updateVariables(SaltProject p)
  {
    segmentationLayerSet.addAll(CommonHelper.getOrderingTypes(p));
    tokenAnnotationLevelSet.addAll(CommonHelper.
      getTokenAnnotationLevelSet(p));

    updateSegmentationLayer(segmentationLayerSet);
    updateTokenAnnos(tokenAnnotationLevelSet);
  }

  public void setCount(int count)
  {
    paging.setCount(count, false);
    paging.setStartNumber(controller.getPreparedQuery().getOffset());
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

  private void updateSegmentationLayer(Set<String> segLayers)
  {
    miSegmentation.removeChildren();

    segLayers.add("");

    for (String s : segLayers)
    {
      MenuItem miSingleSegLayer =
        miSegmentation.addItem(
        (s == null || "".equals(s)) ? NULL_SEGMENTATION_VALUE : s,
        new MenuBar.Command()
      {
        @Override
        public void menuSelected(MenuItem selectedItem)
        {
          selectedSegmentationLayer = selectedItem.getText();
          if (NULL_SEGMENTATION_VALUE.equals(selectedSegmentationLayer))
          {
            selectedSegmentationLayer = null;
          }
          for (MenuItem mi : miSegmentation.getChildren())
          {
            mi.setChecked(mi == selectedItem);
          }

          setSegmentationLayer(selectedSegmentationLayer);
        }
      });

      miSingleSegLayer.setCheckable(true);
      miSingleSegLayer.setChecked(
        (selectedSegmentationLayer == null && "".equals(s))
        || s.equals(selectedSegmentationLayer));
    }
  }

  private void updateTokenAnnos(Set<String> tokenAnnotationLevelSet)
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

          setVisibleTokenAnnosVisible(getVisibleTokenAnnos());
        }
      });

      miSingleTokAnno.setCheckable(true);
      miSingleTokAnno.setChecked(tokenAnnoVisible.get(a).booleanValue());
    }
  }

  @Override
  public boolean onCompononentLoaded(AbstractClientConnector source)
  {
    if (source != null && projectQueue != null && currentQuery != null)
    {
      try
      {
        final SaltProject p = projectQueue.poll(250, TimeUnit.MILLISECONDS);
        if (p == null)
        {
          log.debug("no SaltProject graph in queue");
          return false;
        }
        log.debug("adding new SaltProject graph");
        addQueryResult(currentQuery, p);
        return true;

      }
      catch (InterruptedException ex)
      {
        log.warn(null, ex);
      }
    }
    
    return true;
  }

  private void setVisibleTokenAnnosVisible(Set<String> annos)
  {
    for (SingleResultPanel p : resultPanelList)
    {
      p.setVisibleTokenAnnosVisible(annos);
    }
  }

  private void setSegmentationLayer(String segmentationLayer)
  {
    for (SingleResultPanel p : resultPanelList)
    {
      p.setSegmentationLayer(segmentationLayer);
    }
  }

  public PagingComponent getPaging()
  {
    return paging;
  }
}
