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
import annis.gui.AnnisUI;
import annis.gui.QueryController;
import annis.gui.components.OnLoadCallbackExtension;
import annis.gui.controlpanel.QueryPanel;
import annis.gui.objects.DisplayedResultQuery;
import annis.gui.objects.PagedResultQuery;
import annis.gui.paging.PagingComponent;
import annis.libgui.Helper;
import annis.libgui.IDGenerator;
import annis.libgui.InstanceConfig;
import annis.libgui.PluginSystem;
import annis.libgui.ResolverProviderImpl;
import annis.model.AnnisConstants;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.CorpusConfig;
import annis.service.objects.Match;
import com.google.common.base.Preconditions;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SNode;
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

  private final Map<HashSet<SingleResolverRequest>, List<ResolverEntry>> cacheResolver;

  public static final String FILESYSTEM_CACHE_RESULT
    = "ResultSetPanel_FILESYSTEM_CACHE_RESULT";

  public static final String MAPPING_HIDDEN_ANNOS = "hidden_annos";

  private final PagingComponent paging;

  private final PluginSystem ps;

  private final MenuItem miTokAnnos;

  private final MenuItem miSegmentation;

  private final TreeMap<String, Boolean> tokenAnnoVisible;

  private final QueryController controller;

  private final Set<String> segmentationLayerSet
    = Collections.synchronizedSet(new TreeSet<String>());

  private final Set<String> tokenAnnotationLevelSet
    = Collections.synchronizedSet(new TreeSet<String>());

  private final InstanceConfig instanceConfig;

  private final CssLayout resultLayout;

  private final List<SingleResultPanel> resultPanelList;

  private String segmentationName;

  private int currentResults;

  private int numberOfResults;
  private ArrayList<Match> allMatches;

  private transient BlockingQueue<SaltProject> projectQueue;

  private PagedResultQuery currentQuery;
  private final DisplayedResultQuery initialQuery;
  private final AnnisUI sui;

  public ResultViewPanel(AnnisUI ui,
    PluginSystem ps, InstanceConfig instanceConfig, DisplayedResultQuery initialQuery)
  {
    this.sui = ui;
    this.tokenAnnoVisible = new TreeMap<>();
    this.ps = ps;
    this.controller = ui.getQueryController();
    this.initialQuery = initialQuery;
    
    cacheResolver
      = Collections.synchronizedMap(
        new HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>>());

    resultPanelList
      = Collections.synchronizedList(new LinkedList<SingleResultPanel>());

    resultLayout = new CssLayout();
    resultLayout.addStyleName("result-view-css");
    Panel resultPanel = new Panel(resultLayout);
    resultPanel.setSizeFull();
    resultPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
    resultPanel.addStyleName("result-view-panel");

    this.instanceConfig = instanceConfig;

    setSizeFull();
    setMargin(false);

    MenuBar mbResult = new MenuBar();
    mbResult.setWidth("100%");
    mbResult.addStyleName("menu-hover");
    addComponent(mbResult);

    miSegmentation = mbResult.addItem("Base text", null);
    miTokAnnos = mbResult.addItem("Token Annotations", null);

    addComponent(resultPanel);

    setExpandRatio(mbResult, 0.0f);
    setExpandRatio(resultPanel, 1.0f);

    paging = new PagingComponent();

    addComponent(paging, 1);

    setComponentAlignment(paging, Alignment.TOP_CENTER);
    setExpandRatio(paging, 0.0f);
  }

  @Override
  public void attach()
  {
    super.attach();
    IDGenerator.assignIDForFields(ResultViewPanel.this, resultLayout);
    IDGenerator.assignIDForEachField(paging);
  }
  
  

  /**
   * Informs the user about the searching process.
   *
   * @param query Represents a limited query
   */
  public void showMatchSearchInProgress(PagedResultQuery query)
  {
    resultLayout.removeAllComponents();
    segmentationName = query.getSegmentation();
  }

  public void showNoResult()
  {
    resultLayout.removeAllComponents();
    currentResults = 0;

    // nothing to show since we have an empty result
    Label lblNoResult = new Label("No matches found.");
    lblNoResult.setWidth("100%");
    lblNoResult.addStyleName("result-view-no-content");

    resultLayout.addComponent(lblNoResult);

    showFinishedSubgraphSearch();
  }

  public void showSubgraphSearchInProgress(PagedResultQuery q, float percent)
  {
    if (percent == 0.0f)
    {
      resultLayout.removeAllComponents();
      currentResults = 0;
    }

  }

  /**
   * Set a new querys in result panel.
   *
   * @param queue holds the salt graph
   * @param q holds the ordinary query
   * @param allMatches All matches.
   */
  public void setQueryResultQueue(BlockingQueue<SaltProject> queue,
    PagedResultQuery q, ArrayList<Match> allMatches)
  {
    this.projectQueue = queue;
    this.currentQuery = q;
    this.numberOfResults = allMatches.size();
    this.allMatches = allMatches;

    paging.setPageSize(q.getLimit(), false);
    paging.setInfo(q.getQuery());

    resultLayout.removeAllComponents();
    resultPanelList.clear();

    // get the first query result
    SaltProject first = queue.poll();
    Preconditions.checkState(first != null,
      "There must be already an element in the queue");

    addQueryResult(q, Arrays.asList(first));
  }

  private void resetQueryResultQueue()
  {
    this.projectQueue = null;
    this.currentQuery = null;
    this.currentResults = 0;
    this.numberOfResults = 0;
  }

  private void addQueryResult(PagedResultQuery q, List<SaltProject> subgraphList)
  {

    if (q == null)
    {
      return;
    }

    List<SingleResultPanel> newPanels = new LinkedList<>();
    try
    {
      if (subgraphList == null || subgraphList.isEmpty())
      {
        Notification.show("Could not get subgraphs",
          Notification.Type.TRAY_NOTIFICATION);
      }
      else
      {
        for (SaltProject p : subgraphList)
        {
          updateVariables(p);
          newPanels = createPanels(p, currentResults, q.getOffset() + currentResults);
          currentResults += newPanels.size();

          String strResults = numberOfResults > 1 ? "results" : "result";
          sui.getSearchView().getControlPanel().getQueryPanel().setStatus(sui.getSearchView().getControlPanel().
            getQueryPanel().getLastPublicStatus(),
            " (showing " + currentResults + "/" + numberOfResults + " " + strResults + ")");

          if (currentResults == numberOfResults)
          {
            resetQueryResultQueue();
          }

          for (SingleResultPanel panel : newPanels)
          {
            resultPanelList.add(panel);
            resultLayout.addComponent(panel);
            panel.setSegmentationLayer(sui.getQueryState().getVisibleBaseText().getValue());
          }
        }

        if (currentResults == numberOfResults)
        {
          showFinishedSubgraphSearch();
          if(!initialQuery.getSelectedMatches().isEmpty())
          {
            // scroll to the first selected match
            JavaScript.eval(
              "$(\".v-panel-content-result-view-panel\").animate({scrollTop: $(\".selected-match\").offset().top - $(\".result-view-panel\").offset().top}, 1000);");
          }
        }

        if (projectQueue != null && !newPanels.isEmpty() && currentResults < numberOfResults)
        {
          log.debug("adding callback for result " + currentResults);
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
    //Search complete, stop progress bar control
    if (sui.getSearchView().getControlPanel().getQueryPanel().getPiCount() != null)
    {
      if (sui.getSearchView().getControlPanel().getQueryPanel().getPiCount().isVisible())
      {
        sui.getSearchView().getControlPanel().getQueryPanel().getPiCount().setVisible(false);
        sui.getSearchView().getControlPanel().getQueryPanel().getPiCount().setEnabled(false);
      }
    }
    // also remove the info how many results have been fetched
    QueryPanel qp = sui.getSearchView().getControlPanel().getQueryPanel();
    qp.setStatus(qp.getLastPublicStatus());
  }

  private List<SingleResultPanel> createPanels(SaltProject p, int localMatchIndex, long globalOffset)
  {
    List<SingleResultPanel> result = new LinkedList<>();

    int i = 0;
    for (SCorpusGraph corpusGraph : p.getCorpusGraphs())
    {
      SDocument doc = corpusGraph.getDocuments().get(0);
      Match m = new Match();
      if(allMatches != null && localMatchIndex >= 0 && localMatchIndex < allMatches.size())
      {
        m = allMatches.get(localMatchIndex);
      }
      
      SingleResultPanel panel = new SingleResultPanel(doc, m,
        i + globalOffset, new ResolverProviderImpl(cacheResolver), ps, sui,
        getVisibleTokenAnnos(), segmentationName, controller,
        instanceConfig, initialQuery);

      i++;

      panel.setWidth("100%");
      panel.setHeight("-1px");

      result.add(panel);
    }
    return result;
  }

  private void updateVariables(SaltProject p)
  {
    segmentationLayerSet.addAll(getSegmentationNames(p));
    tokenAnnotationLevelSet.addAll(CommonHelper.getTokenAnnotationLevelSet(p));
    Set<String> hiddenTokenAnnos = null;

    Set<String> corpusNames = CommonHelper.getToplevelCorpusNames(p);

    for (String corpusName : corpusNames)
    {

      CorpusConfig corpusConfig = Helper.getCorpusConfig(corpusName);

      if (corpusConfig != null && corpusConfig.containsKey(MAPPING_HIDDEN_ANNOS))
      {
        hiddenTokenAnnos = new HashSet<>(
          Arrays.asList(
            StringUtils.split(
              corpusConfig.getConfig(MAPPING_HIDDEN_ANNOS), ",")
          )
        );
      }
    }

    if (hiddenTokenAnnos != null)
    {
      for (String tokenLevel : hiddenTokenAnnos)
      {
        if (tokenAnnotationLevelSet.contains(tokenLevel))
        {
          tokenAnnotationLevelSet.remove(tokenLevel);
        }
      }
    }

    updateSegmentationLayer(segmentationLayerSet);
    updateVisibleToken(tokenAnnotationLevelSet);
  }

  private Set<String> getSegmentationNames(SaltProject p)
  {
    Set<String> result = new TreeSet<>();

    for (SCorpusGraph corpusGraphs : p.getCorpusGraphs())
    {
      for (SDocument doc : corpusGraphs.getDocuments())
      {
        SDocumentGraph g = doc.getDocumentGraph();
        if (g != null)
        {
          // collect the start nodes of a segmentation chain of length 1
          for (SNode n : g.getNodes())
          {
            SFeature feat
              = n.getFeature(AnnisConstants.ANNIS_NS,
                AnnisConstants.FEAT_FIRST_NODE_SEGMENTATION_CHAIN);
            if (feat != null && feat.getValue_STEXT() != null)
            {
              result.add(feat.getValue_STEXT());
            }
          }
        } // end if graph not null
      }
    }

    return result;
  }

  public void setCount(int count)
  {
    paging.setCount(count, false);
    paging.setStartNumber(initialQuery.getOffset());
  }

  public SortedSet<String> getVisibleTokenAnnos()
  {
    TreeSet<String> result = new TreeSet<>();

    for (Entry<String, Boolean> e : tokenAnnoVisible.entrySet())
    {
      if (e.getValue().booleanValue() == true)
      {
        result.add(e.getKey());
      }
    }

    return result;
  }

  /**
   * Listens to events on the base text menu and updates the segmentation layer.
   */
  private class MenuBaseTextCommand implements MenuBar.Command
  {

    @Override
    public void menuSelected(MenuItem selectedItem)
    {
      // remember old value
      String oldSegmentationLayer = sui.getQueryState().getVisibleBaseText().getValue();

      // set the new selected item
      String newSegmentationLayer = selectedItem.getText();

      if (NULL_SEGMENTATION_VALUE.equals(newSegmentationLayer))
      {
        newSegmentationLayer = null;
      }
      for (MenuItem mi : miSegmentation.getChildren())
      {
        mi.setChecked(mi == selectedItem);
      }

      if (oldSegmentationLayer != null)
      {
        if (!oldSegmentationLayer.equals(newSegmentationLayer))
        {
          setSegmentationLayer(newSegmentationLayer);
        }
      }
      else if (newSegmentationLayer != null)
      {
        // oldSegmentation is null, but selected is not
        setSegmentationLayer(newSegmentationLayer);
      }

      //update URL with newly selected segmentation layer
      sui.getQueryState().getVisibleBaseText().setValue(newSegmentationLayer);
      sui.getSearchView().updateFragment(sui.getQueryController().getSearchQuery());
    }
  }

  private void updateSegmentationLayer(Set<String> segLayers)
  {

    // clear the menu base text
    miSegmentation.removeChildren();

    // add the default token layer
    segLayers.add("");

    // iterate of all segmentation layers and add them to the menu
    for (String s : segLayers)
    {
      // the new menu entry
      MenuItem miSingleSegLayer;

      /**
       * TODO maybe it would be better, to mark the default text level
       * corresponding to the corpus.properties.
       *
       * There exists always a default text level.
       */
      if (s == null || "".equals(s))
      {
        miSingleSegLayer = miSegmentation.addItem(
          NULL_SEGMENTATION_VALUE, new MenuBaseTextCommand());
      }
      else
      {
        miSingleSegLayer = miSegmentation.addItem(s, new MenuBaseTextCommand());
      }

      // mark as selectable
      miSingleSegLayer.setCheckable(true);

      /**
       * Check if a segmentation item must set checked. If no segmentation layer
       * is selected, set the default layer as selected.
       */
      final String selectedSegmentationLayer = sui.getQueryState().getVisibleBaseText().getValue();
      if ((selectedSegmentationLayer == null && "".equals(s))
        || s.equals(selectedSegmentationLayer))
      {
        miSingleSegLayer.setChecked(true);
      }
      else
      {
        miSingleSegLayer.setChecked(false);
      }
    } // end iterate for segmentation layer
  }

  public void updateVisibleToken(Set<String> tokenAnnotationLevelSet)
  {
    // if no token annotations are there, do not show this mneu
    if (tokenAnnotationLevelSet == null
      || tokenAnnotationLevelSet.isEmpty())
    {
      miTokAnnos.setVisible(false);
    }
    else
    {
      miTokAnnos.setVisible(true);
    }

    // add new annotations
    if (tokenAnnotationLevelSet != null)
    {
      for (String s : tokenAnnotationLevelSet)
      {
        if (!tokenAnnoVisible.containsKey(s))
        {
          tokenAnnoVisible.put(s, Boolean.TRUE);
        }
      }
    }

    miTokAnnos.removeChildren();

    if (tokenAnnotationLevelSet != null)
    {
      for (final String a : tokenAnnotationLevelSet)
      {
        MenuItem miSingleTokAnno = miTokAnnos.addItem(a.replaceFirst("::", ":"),
          new MenuBar.Command()
          {
            @Override
            public void menuSelected(MenuItem selectedItem)
            {

              if (selectedItem.isChecked())
              {
                tokenAnnoVisible.put(a, Boolean.TRUE);
              }
              else
              {
                tokenAnnoVisible.put(a, Boolean.FALSE);
              }

              setVisibleTokenAnnosVisible(getVisibleTokenAnnos());
            }
          });

        miSingleTokAnno.setCheckable(true);
        miSingleTokAnno.setChecked(tokenAnnoVisible.get(a).booleanValue());
      }
    }
  }

  @Override
  public boolean onCompononentLoaded(AbstractClientConnector source)
  {
    if (source != null)
    {
      if (projectQueue != null && currentQuery != null)
      {
        LinkedList<SaltProject> subgraphs = new LinkedList<>();
        SaltProject p;
        while ((p = projectQueue.poll()) != null)
        {
          log.debug("Polling queue for SaltProject graph");
          subgraphs.add(p);
        }
        if (subgraphs.isEmpty())
        {
          log.debug("no SaltProject graph in queue");
          return false;
        }

        log.debug("taken {} SaltProject graph(s) from queue", subgraphs.size());
        addQueryResult(currentQuery, subgraphs);
        return true;

      }
    }

    return true;
  }

  private void setVisibleTokenAnnosVisible(SortedSet<String> annos)
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
