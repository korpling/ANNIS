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
import annis.gui.model.PagedResultQuery;
import annis.gui.paging.PagingComponent;
import annis.libgui.Helper;
import annis.libgui.InstanceConfig;
import static annis.gui.controlpanel.SearchOptionsPanel.KEY_DEFAULT_BASE_TEXT_SEGMENTATION;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.CorpusConfig;
import annis.service.objects.Match;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
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
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class ResultViewPanel extends VerticalLayout implements ResolverProvider
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

  public ResultViewPanel(QueryController controller,
    PluginSystem ps, InstanceConfig instanceConfig)
  {
    this.tokenAnnoVisible = new TreeMap<String, Boolean>();
    this.ps = ps;
    this.controller = controller;
    this.selectedSegmentationLayer = controller.getQuery().getSegmentation();

    cacheResolver =
      Collections.synchronizedMap(
      new HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>>());
    
    resultPanelList =
      Collections.synchronizedList(new LinkedList<SingleResultPanel>());

    resultLayout = new CssLayout();
    resultLayout.addStyleName("result-view-css");

    Set<String> corpora = controller.getQuery().getCorpora();

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

    this.instanceConfig = instanceConfig;

    setSizeFull();
    setMargin(false);


    MenuBar mbResult = new MenuBar();
    mbResult.setWidth("100%");

    miSegmentation = mbResult.addItem("Base text", null);

    miTokAnnos = mbResult.addItem("Token Annotations", null);

    PagedResultQuery q = controller.getQuery();

    paging = new PagingComponent(q.getOffset(), q.getLimit());
    paging.setInfo(q.getQuery());
    paging.addCallback(controller);

    addComponent(mbResult);
    addComponent(paging);

    progressResult = new ProgressBar();

    progressResult.setVisible(false);

    addComponent(progressResult);
    addComponent(resultLayout);
    
    
    setComponentAlignment(paging, Alignment.TOP_CENTER);
    setComponentAlignment(progressResult, Alignment.MIDDLE_CENTER);

    setExpandRatio(mbResult, 0.0f);
    setExpandRatio(paging, 0.0f);
    setExpandRatio(progressResult, 0.0f);
    setExpandRatio(resultLayout, 1.0f);

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
    if(percent == 0.0f)
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
  

  public void addQueryResult(PagedResultQuery q, SaltProject p)
  {
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
      }
    }
    catch (Throwable ex)
    {
      log.error(null, ex);
    }

    for (SingleResultPanel panel : newPanels)
    {
      resultPanelList.add(panel);
      resultLayout.addComponent(panel);
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
        i + offset, this, ps, tokenAnnotationLevelSet, segmentationName,
        instanceConfig);
      i++;

      panel.setWidth("100%");
      panel.setHeight("-1px");

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
    paging.setStartNumber(controller.getQuery().getOffset());
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
  public ResolverEntry[] getResolverEntries(SDocument doc)
  {
    HashSet<ResolverEntry> visSet = new HashSet<ResolverEntry>();

    // create a request for resolver entries
    HashSet<SingleResolverRequest> resolverRequests =
      new HashSet<SingleResolverRequest>();

    Set<String> nodeLayers = new HashSet<String>();

    for (SNode n : doc.getSDocumentGraph().getSNodes())
    {
      for (SLayer layer : n.getSLayers())
      {
        nodeLayers.add(layer.getSName());
      }
    }

    Set<String> edgeLayers = new HashSet<String>();
    for (SRelation e : doc.getSDocumentGraph().getSRelations())
    {
      for (SLayer layer : e.getSLayers())
      {
        try
        {
          edgeLayers.add(layer.getSName());
        }
        catch (NullPointerException ex)
        {
          log.warn("NullPointerException when using Salt, was trying to get layer name",
            ex);
        }
      }
    }

    for (String ns : nodeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getSCorpusGraph().
        getSRootCorpus().get(0).getSName(), ns, ResolverEntry.ElementType.node));
    }
    for (String ns : edgeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getSCorpusGraph().
        getSRootCorpus().get(0).getSName(), ns, ResolverEntry.ElementType.edge));
    }

    // query with this resolver request and make sure it is unique
    if (cacheResolver.containsKey(resolverRequests))
    {
      visSet.addAll(cacheResolver.get(resolverRequests));
    }
    else
    {
      List<ResolverEntry> resolverList = new LinkedList<ResolverEntry>();

      WebResource resResolver = Helper.getAnnisWebResource()
        .path("query").path("resolver");

      for (SingleResolverRequest r : resolverRequests)
      {
        List<ResolverEntry> tmp;
        try
        {
          String corpusName = URLEncoder.encode(r.getCorpusName(), "UTF-8");
          String namespace = r.getNamespace();
          String type = r.getType() == null ? null : r.getType().toString();
          if (corpusName != null && namespace != null && type != null)
          {
            WebResource res = resResolver.path(corpusName).path(namespace).path(type);
            try
            {
              tmp = res.get(new ResolverEntryListType());
              resolverList.addAll(tmp);
            }
            catch (Exception ex)
            {
              log.error("could not query resolver entries: "
                + res.toString(), ex);
            }
          }
        }
        catch (UniformInterfaceException ex)
        {
          log.error(null, ex);
        }
        catch (ClientHandlerException ex)
        {
          log.error(null, ex);
        }
        catch(UnsupportedEncodingException ex)
        {
          log.error(null, ex);
        }
      }
      visSet.addAll(resolverList);
      cacheResolver.put(resolverRequests, resolverList);
    }
    // sort everything
    ResolverEntry[] visArray = visSet.toArray(new ResolverEntry[visSet.size()]);
    Arrays.sort(visArray, new ResolverEntryComparator());
    return visArray;
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
  
  private static class ResolverEntryListType extends GenericType<List<ResolverEntry>>
  {

    public ResolverEntryListType()
    {
    }
  }
  
  private static class ResolverEntryComparator implements Comparator<ResolverEntry>, Serializable
  {

    public ResolverEntryComparator()
    {
    }

    @Override
    public int compare(ResolverEntry o1, ResolverEntry o2)
    {
      if (o1.getOrder() < o2.getOrder())
      {
        return -1;
      }
      else if (o1.getOrder() > o2.getOrder())
      {
        return 1;
      }
      else
      {
        return 0;
      }
    }
  }
}
