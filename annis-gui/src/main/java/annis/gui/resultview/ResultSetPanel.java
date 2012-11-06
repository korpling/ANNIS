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
import annis.gui.Helper;
import annis.gui.PluginSystem;
import annis.gui.SearchWindow;
import annis.gui.media.MediaControllerFactory;
import annis.gui.media.MediaControllerHolder;
import annis.resolver.ResolverEntry;
import annis.resolver.ResolverEntry.ElementType;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.Match;
import annis.service.objects.SaltURIGroup;
import annis.service.objects.SaltURIGroupSet;
import annis.service.objects.SubgraphQuery;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class ResultSetPanel extends Panel implements ResolverProvider
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ResultSetPanel.class);
  private Map<HashSet<SingleResolverRequest>, List<ResolverEntry>> cacheResolver;
  public static final String FILESYSTEM_CACHE_RESULT =
    "ResultSetPanel_FILESYSTEM_CACHE_RESULT";
  private List<SingleResultPanel> resultPanelList;
  private PluginSystem ps;
  private String segmentationName;
  private int firstMatchOffset;
  
  private int contextLeft;
  private int contextRight;
  private ResultViewPanel parent;
  private List<Match> matches;
  private Set<String> tokenAnnotationLevelSet =
    Collections.synchronizedSet(new TreeSet<String>());
  private Set<String> segmentationLayerSet =
    Collections.synchronizedSet(new TreeSet<String>());
  private ProgressIndicator indicator;
  private VerticalLayout indicatorLayout;
  private CssLayout layout;

  public ResultSetPanel(List<Match> matches, PluginSystem ps,
    int contextLeft, int contextRight,
    String segmentationName,
    ResultViewPanel parent, int firstMatchOffset)
  {
    this.ps = ps;
    this.segmentationName = segmentationName;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
    this.parent = parent;
    this.matches = Collections.synchronizedList(matches);
    this.firstMatchOffset = firstMatchOffset;

    resultPanelList =
      Collections.synchronizedList(new LinkedList<SingleResultPanel>());
    cacheResolver =
      Collections.synchronizedMap(
      new HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>>());

    setSizeFull();
    
    layout = new CssLayout();
    setContent(layout);
    layout.addStyleName("result-view-css");


    addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    addStyleName("result-view");

    indicatorLayout = new VerticalLayout();
    
    indicator = new ProgressIndicator();
    indicator.setIndeterminate(true);
    indicator.setValue(0f);
    indicator.setPollingInterval(250);
    indicator.setCaption("fetching subgraphs");
    indicator.setSizeUndefined();
    
    indicatorLayout.addComponent(indicator);
    indicatorLayout.setWidth("100%");
    indicatorLayout.setHeight("-1px");
    indicatorLayout.setComponentAlignment(indicator, Alignment.TOP_CENTER);
    indicatorLayout.setVisible(true);
    
    layout.addComponent(indicatorLayout);
  }

  @Override
  public void attach()
  {
    super.attach();
    
//    layout.setWidth(getWindow().getBrowserWindowWidth() - SearchWindow.CONTROL_PANEL_WIDTH - 25, UNITS_PIXELS);
    
    // reset all registered media players    
    MediaControllerFactory mcFactory = ps.getPluginManager().getPlugin(MediaControllerFactory.class);
    if(mcFactory != null && getApplication() instanceof MediaControllerHolder)
    {
      mcFactory.getOrCreate((MediaControllerHolder) getApplication()).clearMediaPlayers();
    }
    
    // enable indicator in order to get refresh GUI regulary
    indicator.setEnabled(true);

    ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

    Callable<Boolean> run = new AllResultsFetcher();
    FutureTask<Boolean> task = new FutureTask<Boolean>(
      run)
    {
      @Override
      protected void done()
      {
        synchronized(getApplication())
        {
          indicator.setEnabled(false);
          indicator.setVisible(false);
          indicatorLayout.setVisible(false);
        }
      }
    };
    
    singleExecutor.submit(task);
  }
  
  private void addQueryResult(SaltProject p, int offset)
  {
    List<SingleResultPanel> newPanels = new LinkedList<SingleResultPanel>();
    try
    {
      if (p == null)
      {
        getWindow().showNotification("Could not get subgraphs",
          Window.Notification.TYPE_TRAY_NOTIFICATION);
      }
      {
        updateVariables(p);
        newPanels = createPanels(p, offset);
      }
    }
    catch (Exception ex)
    {
      log.error(null, ex);
    }

    for (SingleResultPanel panel : newPanels)
    {
      resultPanelList.add(panel);
      // insert just before the indicator
      int indicatorIndex = layout.getComponentIndex(indicatorLayout);
      layout.addComponent(panel);
    }
  }
  
  private void updateVariables(SaltProject p)
  {
    segmentationLayerSet.addAll(CommonHelper.getOrderingTypes(p));
    tokenAnnotationLevelSet.addAll(CommonHelper.
      getTokenAnnotationLevelSet(p));

    parent.updateSegmentationLayer(segmentationLayerSet);
    parent.updateTokenAnnos(tokenAnnotationLevelSet);
  }
  
  private List<SingleResultPanel> createPanels(SaltProject p, int offset)
  {
    List<SingleResultPanel> result = new LinkedList<SingleResultPanel>();
    
    int i=0;
    for (SCorpusGraph corpusGraph : p.getSCorpusGraphs())
    {
      SingleResultPanel panel = new SingleResultPanel(corpusGraph.getSDocuments().get(0), 
        i + offset, this, ps, tokenAnnotationLevelSet, segmentationName);
      i++;
      
      panel.setWidth("100%");
      panel.setHeight("-1px");

      result.add(panel);
    }
    return result;
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
        getSRootCorpus().get(0).getSName(), ns, ElementType.node));
    }
    for (String ns : edgeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getSCorpusGraph().
        getSRootCorpus().get(0).getSName(), ns, ElementType.edge));
    }

    // query with this resolver request and make sure it is unique
    if (cacheResolver.containsKey(resolverRequests))
    {
      visSet.addAll(cacheResolver.get(resolverRequests));
    }
    else
    {
      List<ResolverEntry> resolverList = new LinkedList<ResolverEntry>();

      WebResource resResolver = Helper.getAnnisWebResource(getApplication())
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
              tmp = res.get(new GenericType<List<ResolverEntry>>()
              {
              });
              resolverList.addAll(tmp);
            }
            catch (Exception ex)
            {
              log.error("could not query resolver entries: "
                + res.toString(), ex);
            }
          }
        }
        catch (Exception ex)
        {
          log.error(null, ex);
        }
      }
      visSet.addAll(resolverList);
      cacheResolver.put(resolverRequests, resolverList);
    }
    // sort everything
    ResolverEntry[] visArray = visSet.toArray(new ResolverEntry[0]);
    Arrays.sort(visArray, new Comparator<ResolverEntry>()
    {
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
    });
    return visArray;
  }

  public void setSegmentationLayer(String segmentationLayer)
  {
    for (SingleResultPanel p : resultPanelList)
    {
      p.setSegmentationLayer(segmentationLayer);
    }
  }

  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
    for (SingleResultPanel p : resultPanelList)
    {
      p.setVisibleTokenAnnosVisible(annos);
    }
  }

  public class AllResultsFetcher implements Callable<Boolean>
  {

    public AllResultsFetcher()
    {
    }
    
    private SaltProject executeQuery(WebResource subgraphRes,
      SubgraphQuery query)
    {
      SaltProject p = null;
      try
      {
        p = subgraphRes.post(SaltProject.class, query);
      }
      catch (UniformInterfaceException ex)
      {
        log.error(ex.getMessage(), ex);
      }

      return p;
    }

    private SubgraphQuery prepareQuery(List<Match> matchesToPrepare)
    {
      SubgraphQuery query = new SubgraphQuery();

      query.setLeft(contextLeft);
      query.setRight(contextRight);
      if (segmentationName != null)
      {
        query.setSegmentationLayer(segmentationName);
      }

      SaltURIGroupSet saltURIs = new SaltURIGroupSet();

      ListIterator<Match> it = matchesToPrepare.listIterator();
      int i = 0;
      while (it.hasNext())
      {
        Match m = it.next();
        SaltURIGroup urisForMatch = new SaltURIGroup();

        for (String s : m.getSaltIDs())
        {
          try
          {
            urisForMatch.getUris().add(new URI(s));
          }
          catch (URISyntaxException ex)
          {
            log.error(null, ex);
          }
        }
        saltURIs.getGroups().put(++i, urisForMatch);
      }

      query.setMatches(saltURIs);
      return query;
    }
    
    @Override
    public Boolean call() throws Exception
    {
      boolean allSuccessfull = true;
      
      WebResource res = Helper.getAnnisWebResource(getApplication());
      if (res != null)
      {
        res = res.path("query/search/subgraph");

        int i=firstMatchOffset; 
        for(Match m : matches)
        {
          List<Match> sub = new LinkedList<Match>();
          sub.add(m);
          SubgraphQuery query = prepareQuery(sub);
          if(query.getMatches().getGroups().size() > 0)
          {
            SaltProject p = executeQuery(res, query);
            if(p != null)
            {
              synchronized(getApplication())
              {
                addQueryResult(p, i++);
              }
            } 
          }
          else
          {
            allSuccessfull = false;
          }
        }
      }
      return allSuccessfull;
    }

 
  } // end class AllResultsFetcher
}
