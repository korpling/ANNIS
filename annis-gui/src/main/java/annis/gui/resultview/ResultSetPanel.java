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
import annis.libgui.Helper;
import annis.libgui.InstanceConfig;
import annis.libgui.PluginSystem;
import annis.libgui.ResolverProviderImpl;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.Match;
import annis.service.objects.SaltURIGroup;
import annis.service.objects.SaltURIGroupSet;
import annis.service.objects.SubgraphQuery;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class ResultSetPanel extends Panel
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    ResultSetPanel.class);

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

  private ProgressBar progress;

  private VerticalLayout indicatorLayout;

  private CssLayout layout;

  private InstanceConfig instanceConfig;

  public ResultSetPanel(List<Match> matches, PluginSystem ps,
    InstanceConfig instanceConfig,
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
    this.instanceConfig = instanceConfig;

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

    progress = new ProgressBar();
    progress.setIndeterminate(false);
    progress.setValue(0f);
    progress.setSizeUndefined();

    indicatorLayout.addComponent(progress);
    indicatorLayout.setWidth("100%");
    indicatorLayout.setHeight("-1px");
    indicatorLayout.setComponentAlignment(progress, Alignment.TOP_CENTER);
    indicatorLayout.setVisible(true);

    layout.addComponent(indicatorLayout);

    // enable indicator in order to get refresh GUI regulary
    progress.setEnabled(true);

    ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

    Callable<Boolean> run = new AllResultsFetcher();
    FutureTask<Boolean> task = new FutureTask<Boolean>(
      run)
    {
      @Override
      protected void done()
      {
        VaadinSession session = VaadinSession.getCurrent();
        session.lock();
        try
        {
          progress.setEnabled(false);
          progress.setVisible(false);
          indicatorLayout.setVisible(false);
        }
        finally
        {
          session.unlock();
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
        Notification.show("Could not get subgraphs",
          Notification.Type.TRAY_NOTIFICATION);
      }
      else
      {
        updateVariables(p);
        newPanels = createPanels(p, offset);
      }
    }
    catch (Throwable ex)
    {
      log.error(null, ex);
    }

    for (SingleResultPanel panel : newPanels)
    {
      resultPanelList.add(panel);
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

    int i = 0;
    for (SCorpusGraph corpusGraph : p.getSCorpusGraphs())
    {
      SingleResultPanel panel = new SingleResultPanel(corpusGraph.
        getSDocuments().get(0),
        i + offset, new ResolverProviderImpl(cacheResolver), ps,
        tokenAnnotationLevelSet, segmentationName, instanceConfig);
      i++;

      panel.setWidth("100%");
      panel.setHeight("-1px");

      result.add(panel);
    }
    return result;
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

      WebResource res = Helper.getAnnisWebResource();
      if (res != null)
      {
        res = res.path("query/search/subgraph");

        int j = 0;
        SaltProject lastProject = null;
        for (Match m : matches)
        {
          List<Match> sub = new LinkedList<Match>();
          sub.add(m);
          SubgraphQuery query = prepareQuery(sub);
          if (query.getMatches().getGroups().size() > 0)
          {
            SaltProject p = executeQuery(res, query);

            if (p != null)
            {
              lastProject = p;
            }
          }
          else
          {
            allSuccessfull = false;
          }

          VaadinSession session = VaadinSession.getCurrent();
          session.lock();
          try
          {

            if (lastProject != null)
            {
              addQueryResult(lastProject, j + firstMatchOffset);
            }
            progress.setValue((float) j++ / (float) matches.size());
            if (j == matches.size())
            {
              progress.setValue(1.0f);
            }
          }
          catch (Throwable ex)
          {
            log.error("Exception when adding query result", ex);
          }
          finally
          {
            session.unlock();
          }
        }

      }
      return allSuccessfull;
    }
  } // end class AllResultsFetcher    
}
