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
import annis.gui.media.MediaController;
import annis.gui.media.MediaControllerFactory;
import annis.gui.media.MediaControllerHolder;
import annis.resolver.ResolverEntry;
import annis.resolver.ResolverEntry.ElementType;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.Match;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
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
  private BeanItemContainer<Match> container;
  private List<SingleResultPanel> resultPanelList;
  private PluginSystem ps;
  private String segmentationName;
  private int start;
  private int contextLeft;
  private int contextRight;
  private ResultViewPanel parent;
  private List<Match> matches;
  private Set<String> tokenAnnotationLevelSet =
    Collections.synchronizedSet(new HashSet<String>());
  private Set<String> segmentationLayerSet =
    Collections.synchronizedSet(new HashSet<String>());
  private ProgressIndicator indicator;
  private VerticalLayout layout;

  public ResultSetPanel(List<Match> matches, int start, PluginSystem ps,
    int contextLeft, int contextRight,
    String segmentationName,
    ResultViewPanel parent)
  {
    this.ps = ps;
    this.segmentationName = segmentationName;
    this.start = start;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
    this.parent = parent;
    this.matches = Collections.synchronizedList(matches);

    resultPanelList =
      Collections.synchronizedList(new LinkedList<SingleResultPanel>());
    cacheResolver =
      Collections.synchronizedMap(
      new HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>>());

    setSizeFull();

    layout = (VerticalLayout) getContent();
    layout.setWidth("100%");
    layout.setHeight("-1px");


    addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    layout.setMargin(false);
    addStyleName("result-view");

    container = new BeanItemContainer<Match>(Match.class, this.matches);

    indicator = new ProgressIndicator();
    indicator.setIndeterminate(true);
    indicator.setValue(0f);
    indicator.setPollingInterval(250);
    indicator.setCaption("fetching subgraphs");

    layout.addComponent(indicator);
    layout.setComponentAlignment(indicator, Alignment.BOTTOM_CENTER);

  }

  @Override
  public void attach()
  {
    super.attach();

    // reset all registered media players    
    MediaControllerFactory mcFactory = ps.getPluginManager().getPlugin(MediaControllerFactory.class);
    if(mcFactory != null && getApplication() instanceof MediaControllerHolder)
    {
      mcFactory.getOrCreate((MediaControllerHolder) getApplication()).clearMediaPlayers();
    }
    
    String propBatchSize = getApplication().getProperty("result-fetch-batchsize");
    final int batchSize = propBatchSize == null ? 5 : Integer.parseInt(propBatchSize);
    // enable indicator in order to get refresh GUI regulary
    indicator.setEnabled(true);

    ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

    Runnable run = new AllResultsFetcher(batchSize);
    singleExecutor.submit(run);

  }

  private Map<Integer, Future<SingleResultPanel>> loadNextResultBatch(int batchSize,
    int offset, ExecutorService executorService, WebResource resWithoutMatch)
  {

    Map<Integer, Future<SingleResultPanel>> tasks =
      Collections.synchronizedMap(new HashMap<Integer, Future<SingleResultPanel>>());

    ListIterator<Match> it = matches.listIterator(offset);
    while (it.hasNext() && (it.nextIndex() - offset) < batchSize)
    {
      int i = it.nextIndex();
      Match m = it.next();

      List<String> encodedSaltIDs = new LinkedList<String>();
      for (String s : m.getSaltIDs())
      {
        try
        {
          encodedSaltIDs.add(URLEncoder.encode(s, "UTF-8"));
        }
        catch (UnsupportedEncodingException ex)
        {
          log.error(null, ex);
        }
      }

      // get subgraph for match
      WebResource res =
        resWithoutMatch.queryParam("q", StringUtils.join(encodedSaltIDs, ","));

      if (res != null)
      {
        Future<SingleResultPanel> f =
          lazyLoadResultPanel(executorService, res, i, this);
        tasks.put(i, f);
      }
    }

    return tasks;
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

      WebResource resResolver = Helper.getAnnisWebResource(getApplication()).
        path("resolver");

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

  private Future<SingleResultPanel> lazyLoadResultPanel(
    final ExecutorService executorService,
    final WebResource subgraphRes, final int offset,
    final ResolverProvider rsProvider)
  {
    final int resultNumber = start + offset;

    Callable<SingleResultPanel> run =
      new SingleResultFetcher(subgraphRes, resultNumber, rsProvider);

    return executorService.submit(run);
  }

  public class SingleResultFetcher implements Callable<SingleResultPanel>
  {

    private WebResource subgraphRes;
    private int resultNumber;
    private ResolverProvider rsProvider;

    public SingleResultFetcher(WebResource subgraphRes, int resultNumber, ResolverProvider rsProvider)
    {
      this.subgraphRes = subgraphRes;
      this.resultNumber = resultNumber;
      this.rsProvider = rsProvider;
    }

    @Override
    public SingleResultPanel call()
    {
      // load result asynchronous
      SaltProject p = null;
      int tries = 0;
      while (p == null && tries < 100)
      {
        try
        {
          p = subgraphRes.get(SaltProject.class);
        }
        catch (UniformInterfaceException ex)
        {
          if (ex.getResponse().getStatus() != Response.Status.SERVICE_UNAVAILABLE.getStatusCode())
          {
            log.error(ex.getMessage(), ex);
            break;
          }
          // wait some time
          try
          {
            Thread.sleep(500);
          }
          catch (InterruptedException ex1)
          {
            log.error(null, ex1);
          }
        }
        catch (Exception ex)
        {
          log.error(ex.getMessage(), ex);
          break;
        }
        tries++;
      }

      Validate.notNull(p);

      SingleResultPanel result;
      // get synchronized again in order not to confuse Vaadin
      synchronized (getApplication())
      {
        segmentationLayerSet.addAll(CommonHelper.getOrderingTypes(p));
        tokenAnnotationLevelSet.addAll(CommonHelper.getTokenAnnotationLevelSet(p));

        parent.updateSegmentationLayer(segmentationLayerSet);
        parent.updateTokenAnnos(tokenAnnotationLevelSet);

        if (p != null && p.getSCorpusGraphs().size() > 0
          && p.getSCorpusGraphs().get(0).getSDocuments().size() > 0)
        {
          result =
            new SingleResultPanel(
            p.getSCorpusGraphs().get(0).getSDocuments().get(0),
            resultNumber, rsProvider, ps, parent.getVisibleTokenAnnos(), segmentationName);
        }
        else
        {
          log.warn("did not get a proper corpus graph for URI {}",
            subgraphRes.toString());
          result = null;
        }
      }
      return result;
    }
  }

  public class AllResultsFetcher implements Runnable
  {

    private int batchSize;

    public AllResultsFetcher(int batchSize)
    {
      this.batchSize = batchSize;
    }

    @Override
    public void run()
    {
      ExecutorService executorService = Executors.newFixedThreadPool(batchSize);


      WebResource res = Helper.getAnnisWebResource(getApplication());
      if (res != null)
      {
        res = res.path("search/subgraph").queryParam("left", "" + contextLeft).queryParam("right", "" + contextRight);

        if (segmentationName != null)
        {
          res = res.queryParam("seglayer", segmentationName);
        }


        for (int offset = 0; offset < matches.size(); offset += batchSize)
        {
          Map<Integer, Future<SingleResultPanel>> tasks =
            loadNextResultBatch(batchSize, offset, executorService, res);

          waitForTasks(tasks, offset);

        }
      }

      synchronized (getApplication())
      {
        indicator.setEnabled(false);
        indicator.setVisible(false);

        for (SingleResultPanel panel : resultPanelList)
        {
          layout.addComponent(panel);
        }
      }
    }

    private void waitForTasks(Map<Integer, Future<SingleResultPanel>> tasks, int offset)
    {
      // wait until all tasks are done
      for (int i = offset; i < offset + batchSize; i++)
      {
        if (tasks.containsKey(i))
        {
          Future<SingleResultPanel> future = tasks.get(i);
          try
          {
            SingleResultPanel panel = future.get();
            if (panel == null)
            {
              synchronized (getApplication())
              {
                getWindow().showNotification("Could not get subgraph " + i,
                  Window.Notification.TYPE_TRAY_NOTIFICATION);
              }
            }
            else
            {
              // add the panel

              panel.setWidth("100%");
              panel.setHeight("-1px");

              resultPanelList.add(panel);
            }
          }
          catch (Exception ex)
          {
            log.error(null, ex);
          }
        }
      }
    }
  } // end class AllResultsFetcher
}
