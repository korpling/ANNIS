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
import annis.resolver.ResolverEntry;
import annis.resolver.ResolverEntry.ElementType;
import annis.resolver.SingleResolverRequest;
import annis.service.objects.Match;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author thomas
 */
public class ResultSetPanel extends Panel implements ResolverProvider
{

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

    resultPanelList = new LinkedList<SingleResultPanel>();
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
    indicator.setIndeterminate(false);
    indicator.setValue(0f);
    indicator.setPollingInterval(100);

    layout.addComponent(indicator);
    layout.setComponentAlignment(indicator, Alignment.BOTTOM_CENTER);

  }

  @Override
  public void attach()
  {
    super.attach();

    String propBatchSize = getApplication().getProperty("result-fetch-batchsize");
    final int batchSize = propBatchSize == null ? 3 : Integer.parseInt(propBatchSize);
    // enable indicator in order to get refresh GUI regulary
    indicator.setEnabled(true);

    ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
    
    Runnable run = new Runnable()
    {
      @Override
      public void run()
      {
        ExecutorService executorService = Executors.newFixedThreadPool(batchSize);

        for (int offset = 0; offset < matches.size(); offset += batchSize)
        {

          int upperEnd = Math.min(offset + batchSize, matches.size());
          synchronized (getApplication())
          {
            indicator.setCaption("fetching subgraphs " + (offset + 1) + " to " + (upperEnd));
            indicator.setValue((float) offset / (float) matches.size());
          }
          
          Map<Integer, Future<SingleResultPanel>> tasks 
            = loadNextResultBatch(batchSize, offset, executorService);

          // wait until all tasks are done
          for(int i=offset; i < offset + batchSize; i++)
          {
            if(tasks.containsKey(i))
            {
              Future<SingleResultPanel> future = tasks.get(i);
              try
              {
                SingleResultPanel panel =  future.get();
                // add the panel
                synchronized(getApplication())
                {
                  panel.setWidth("100%");
                  panel.setHeight("-1px");
                  layout.addComponent(panel);
                }
              }
              catch (Exception ex)
              {
                Logger.getLogger(ResultSetPanel.class.getName()).log(Level.SEVERE, null, ex);
              }
            }
          }
        }

        synchronized (getApplication())
        {
          indicator.setEnabled(false);
          indicator.setVisible(false);
          layout.removeComponent(indicator);
        }
      }
    };

    singleExecutor.submit(run);



  }

  private Map<Integer, Future<SingleResultPanel>> loadNextResultBatch(int batchSize, 
    int offset, ExecutorService executorService)
  {

    Map<Integer, Future<SingleResultPanel>> tasks =
      Collections.synchronizedMap(new HashMap<Integer, Future<SingleResultPanel>>());

    ListIterator<Match> it = matches.listIterator(offset);
    while (it.hasNext() && (it.nextIndex() - offset) < batchSize)
    {
      int i = it.nextIndex();
      Match m = it.next();

      // get subgraph for match
      WebResource res = Helper.getAnnisWebResource(getApplication());

      if (res != null)
      {
        res = res.path("search/subgraph")
          .queryParam("q", StringUtils.join(m.getSaltIDs(), ","))
          .queryParam("left", "" + contextLeft)
          .queryParam("right", "" + contextRight);

        if (segmentationName != null)
        {
          res = res.queryParam("seglayer", segmentationName);
        }

        Future<SingleResultPanel> f = lazyLoadResultPanel(executorService, res, m, i, this, batchSize);
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
          Logger.getLogger(ResultSetPanel.class.getName()).log(Level.WARNING,
            "NullPointerException when using Salt, was trying to get layer name",
            ex);
        }
      }
    }

    for (String ns : nodeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getSCorpusGraph().
        getSRootCorpus().get(0).getSName(), ns,
        ElementType.node));
    }
    for (String ns : edgeLayers)
    {
      resolverRequests.add(new SingleResolverRequest(doc.getSCorpusGraph().
        getSRootCorpus().get(0).getSName(), ns,
        ElementType.edge));
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
              Logger.getLogger(ResultSetPanel.class.getName())
                .log(Level.SEVERE, "could not query resolver entries: "
                + res.toString(), ex);
            }
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(ResultSetPanel.class.getName())
            .log(Level.SEVERE, null, ex);
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
    final WebResource subgraphRes, final Match match, final int offset,
    final ResolverProvider rsProvider, final int batchSize)
  {
    final int resultNumber = start + offset;

    Callable<SingleResultPanel> run = new Callable<SingleResultPanel>()
    {
      @Override
      public SingleResultPanel call()
      {
        // load result asynchronous
        SaltProject p = subgraphRes.get(SaltProject.class);

        SingleResultPanel result;
        // get synchronized again in order not to confuse Vaadin
        synchronized (getApplication())
        {
          tokenAnnotationLevelSet.addAll(CommonHelper.getTokenAnnotationLevelSet(p));
          parent.updateTokenAnnos(tokenAnnotationLevelSet);

          result = 
            new SingleResultPanel(
              p.getSCorpusGraphs().get(0).getSDocuments().get(0),
              resultNumber, rsProvider, ps, parent.getVisibleTokenAnnos(), segmentationName);
        }
        return result;
      }
    };

    return executorService.submit(run);
  }
}
