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
import annis.gui.MatchedNodeColors;
import annis.gui.MetaDataPanel;
import annis.gui.PluginSystem;
import annis.gui.visualizers.VisualizerPlugin;
import annis.gui.visualizers.component.KWICPanel;
import static annis.model.AnnisConstants.*;
import annis.resolver.ResolverEntry;
import annis.service.objects.CorpusConfig;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.*;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class SingleResultPanel extends VerticalLayout implements
  Button.ClickListener
{

  private static final String HIDE_KWIC = "hide_kwic";
  private static final String INITIAL_OPEN = "initial_open";
  private static final ThemeResource ICON_RESOURCE = new ThemeResource(
    "info.gif");
  private SDocument result;
  private Map<SNode, Long> markedAndCovered;
  private Map<String, String> markedCoveredMap;
  private Map<String, String> markedExactMap;
  private ResolverProvider resolverProvider;
  private PluginSystem ps;
  private List<KWICPanel> kwicPanels;
  private List<VisualizerPanel> mediaVisualizer;
  private List<String> mediaIDs;
  private Button btInfo;
  private int resultNumber;
  private List<String> path;
  private Set<String> visibleTokenAnnos;
  private String segmentationName;
  private CorpusConfig corpusConfig;
  private List<SNode> token;
  private boolean wasAttached;
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    SingleResultPanel.class);
  // TODO should be configurable with resolver_vis_tab
  private String[] alwaysVisibleVis =
  {
    "kwic"
  };

  public SingleResultPanel(final SDocument result, int resultNumber,
    ResolverProvider resolverProvider, PluginSystem ps,
    Set<String> visibleTokenAnnos, String segmentationName)
  {
    this.ps = ps;
    this.result = result;
    this.resolverProvider = resolverProvider;
    this.resultNumber = resultNumber;
    this.visibleTokenAnnos = visibleTokenAnnos;
    this.segmentationName = segmentationName;

    calculateHelperVariables();

    setWidth("100%");
    setHeight("-1px");

    setMargin(false);
    setSpacing(false);

    HorizontalLayout infoBar = new HorizontalLayout();
    infoBar.addStyleName("docPath");
    infoBar.setWidth("100%");
    infoBar.setHeight("-1px");

    addComponent(infoBar);

    Label lblNumber = new Label("" + (resultNumber + 1));
    infoBar.addComponent(lblNumber);
    lblNumber.setSizeUndefined();

    btInfo = new Button();
    btInfo.setStyleName(ChameleonTheme.BUTTON_LINK);
    btInfo.setIcon(ICON_RESOURCE);
    btInfo.addListener((Button.ClickListener) this);
    infoBar.addComponent(btInfo);

    path = CommonHelper.getCorpusPath(result.getSCorpusGraph(),
      result);
    Collections.reverse(path);

    Label lblPath = new Label("Path: " + StringUtils.join(path, " > "));
    lblPath.setWidth("100%");
    lblPath.setHeight("-1px");
    infoBar.addComponent(lblPath);
    infoBar.setExpandRatio(lblPath, 1.0f);
    infoBar.setSpacing(true);
  }

  @Override
  public void attach()
  {
    try
    {

      if (wasAttached)
      {
        return;
      }
      wasAttached = true;

      // get corpus properties

      corpusConfig =
        Helper.getCorpusConfig(path.get(0), getApplication(), getWindow());

      ResolverEntry[] entries =
        resolverProvider.getResolverEntries(result);
      mediaIDs = mediaVisIds(entries);
      List<VisualizerPanel> visualizers = new LinkedList<VisualizerPanel>();
      List<VisualizerPanel> openVisualizers = new LinkedList<VisualizerPanel>();
      mediaVisualizer = new ArrayList<VisualizerPanel>();


      // hacky implemented ComponentVisualizer
      for (int i = 0; i < alwaysVisibleVis.length; i++)
      {
        String id = "resolver-" + resultNumber + "-" + i;
        CustomLayout visContainer = this.visContainer(id, "compVis");

        for (STextualDS text : result.getSDocumentGraph().getSTextualDSs())
        {
          token = CommonHelper.getSortedSegmentationNodes(segmentationName,
            result.getSDocumentGraph());

          markedAndCovered = calculateMarkedAndCoveredIDs(result, token);
          calulcateColorsForMarkedAndCoverd();

          VisualizerPanel p = new VisualizerPanel(alwaysVisibleVis[i], result,
            token, visibleTokenAnnos, markedAndCovered, markedExactMap,
            markedCoveredMap, text, mediaIDs, mediaVisualizer, id, this,
            segmentationName, ps, visContainer);
          visualizers.add(p);

        }
      }
      // /hacky implemented ComponentVisualizer

      for (int i = 0; i < entries.length; i++)
      {
        String id = "resolver-" + resultNumber + "-" + i;
        CustomLayout visContainer = this.visContainer(id, "iframe");

        VisualizerPanel p = new VisualizerPanel(entries[i], result, ps,
          markedExactMap, markedCoveredMap, visContainer, mediaIDs, id);

        if ("media".equals(entries[i].getVisType())
          || "video".equals(entries[i].getVisType())
          || "audio".equals(entries[i].getVisType()))
        {
          mediaVisualizer.add(p);
        }

        visualizers.add(p);
        Properties mappings = entries[i].getMappings();
        if (Boolean.parseBoolean(mappings.getProperty(INITIAL_OPEN, "false")))
        {
          openVisualizers.add(p);
        }
      }

      for (VisualizerPanel p : visualizers)
      {
        addComponent(p);
      }

      for (VisualizerPanel p : openVisualizers)
      {
        p.toggleVisualizer(false);
      }
    }
    catch (Exception ex)
    {
      log.error("problems with initializing Visualizer Panel", ex);
    }
  }

  //TODO apply this to new plugin system
  public void setSegmentationLayer(String segmentationName)
  {
    this.segmentationName = segmentationName;
    if (kwicPanels != null)
    {
      for (KWICPanel kwic : kwicPanels)
      {
//        removeComponent(kwic);
      }

      kwicPanels.clear();
//      addKWICPanels();
    }
  }

  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {

    for (String visName : alwaysVisibleVis)
    {
      VisualizerPlugin vis = ps.getVisualizer(visName);
      vis.setVisibleTokenAnnosVisible(annos);
    }

    if (kwicPanels != null)
    {
      for (KWICPanel kwic : kwicPanels)
      {
        kwic.setVisibleTokenAnnosVisible(annos);
      }
    }
  }

  private void calculateHelperVariables()
  {
    markedExactMap = new HashMap<String, String>();
    markedCoveredMap = new HashMap<String, String>();

    SDocumentGraph g = result.getSDocumentGraph();
    if (g != null)
    {
      for (SNode n : result.getSDocumentGraph().getSNodes())
      {

        SFeature featMatched = n.getSFeature(ANNIS_NS, FEAT_MATCHEDNODE);
        Long match = featMatched == null ? null : featMatched.
          getSValueSNUMERIC();

        if (match != null)
        {
          int color = Math.max(0, Math.min((int) match.longValue() - 1,
            MatchedNodeColors.values().length - 1));
          SFeature feat = n.getSFeature(ANNIS_NS, FEAT_INTERNALID);
          if (feat != null)
          {
            markedExactMap.put("" + feat.getSValueSNUMERIC(),
              MatchedNodeColors.values()[color].name());
          }
        }

      }
    }
  }

  private void calulcateColorsForMarkedAndCoverd()
  {
    for (Entry<SNode, Long> markedEntry : markedAndCovered.entrySet())
    {
      int color = Math.max(0, Math.min((int) markedEntry.getValue().longValue()
        - 1,
        MatchedNodeColors.values().length - 1));
      SFeature feat = markedEntry.getKey().getSFeature(ANNIS_NS, FEAT_INTERNALID);
      if (feat != null)
      {
        markedCoveredMap.put("" + feat.getSValueSNUMERIC(),
          MatchedNodeColors.values()[color].name());
      }
    }
  }

  private Map<SNode, Long> calculateMarkedAndCoveredIDs(
    SDocument doc, List<SNode> segNodes)
  {
    Set<String> matchedNodes = new HashSet<String>();
    Map<SNode, Long> initialCovered = new HashMap<SNode, Long>();

    // add all covered nodes
    for (SNode n : doc.getSDocumentGraph().getSNodes())
    {
      SFeature featMatched = n.getSFeature(ANNIS_NS,
        FEAT_MATCHEDNODE);
      Long match = featMatched == null ? null : featMatched.getSValueSNUMERIC();

      if (match != null)
      {
        matchedNodes.add(n.getSId());
        initialCovered.put(n, match);
      }
    }

    // calculate covered nodes
    SingleResultPanel.CoveredMatchesCalculator cmc = new SingleResultPanel.CoveredMatchesCalculator(doc.
      getSDocumentGraph(), initialCovered);
    Map<SNode, Long> covered = cmc.getMatchedAndCovered();


    if (segmentationName != null)
    {
      // filter token
      Map<SToken, Long> coveredToken = new HashMap<SToken, Long>();
      for (Map.Entry<SNode, Long> e : covered.entrySet())
      {
        if (e.getKey() instanceof SToken)
        {
          coveredToken.put((SToken) e.getKey(), e.getValue());
        }
      }

      for (SNode segNode : segNodes)
      {
        if (segNode != null && !covered.containsKey(segNode))
        {
          long leftTok =
            segNode.getSFeature(ANNIS_NS, FEAT_LEFTTOKEN).getSValueSNUMERIC();
          long rightTok =
            segNode.getSFeature(ANNIS_NS, FEAT_RIGHTTOKEN).getSValueSNUMERIC();

          // check for each covered token if this segment is covering it
          for (Map.Entry<SToken, Long> e : coveredToken.entrySet())
          {
            long entryTokenIndex = e.getKey().getSFeature(ANNIS_NS,
              FEAT_TOKENINDEX).getSValueSNUMERIC();
            if (entryTokenIndex <= rightTok && entryTokenIndex >= leftTok)
            {
              // add this segmentation node to the covered set
              covered.put(segNode, e.getValue());
              break;
            }
          } // end for each covered token
        } // end if not already contained
      } // end for each segmentation node
    }

    return covered;
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    if (event.getButton() == btInfo)
    {
      Window infoWindow = new Window("Info for " + result.getSId());

      infoWindow.setModal(false);
      MetaDataPanel meta = new MetaDataPanel(path.get(0), path.get(path.size()
        - 1));
      infoWindow.setContent(meta);
      infoWindow.setWidth("400px");
      infoWindow.setHeight("400px");

      getWindow().addWindow(infoWindow);
    }
  }

  public static class CoveredMatchesCalculator implements SGraphTraverseHandler
  {

    private Map<SNode, Long> matchedAndCovered;
    private long currentMatchPos;

    public CoveredMatchesCalculator(SDocumentGraph graph,
      Map<SNode, Long> initialMatches)
    {
      this.matchedAndCovered = initialMatches;

      currentMatchPos = 1;
      if (initialMatches.size() > 0)
      {
        graph.traverse(new BasicEList<SNode>(initialMatches.keySet()),
          GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "CoveredMatchesCalculator",
          (SGraphTraverseHandler) this, true);
      }
    }

    @Override
    public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType,
      String traversalId, SNode currNode, SRelation edge, SNode fromNode,
      long order)
    {
      if (matchedAndCovered.containsKey(fromNode) && !matchedAndCovered.
        containsKey(currNode))
      {
        currentMatchPos = matchedAndCovered.get(fromNode);
        matchedAndCovered.put(currNode, currentMatchPos);
      }

    }

    @Override
    public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
      SNode currNode, SRelation edge, SNode fromNode, long order)
    {
    }

    @Override
    public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType,
      String traversalId, SRelation edge, SNode currNode, long order)
    {
      if (edge == null || edge instanceof SDominanceRelation
        || edge instanceof SSpanningRelation)
      {
        return true;
      }
      else
      {
        return false;
      }
    }

    public Map<SNode, Long> getMatchedAndCovered()
    {
      return matchedAndCovered;
    }
  }

  private CustomLayout visContainer(String id, String place)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<div id=\"").append(id).append("\">");
    sb.append("<div location=\"btEntry\"></div>");
    sb.append("  <div location=\"").append(place).append("\"></div>");
    sb.append("</div>");
    try
    {
      return new CustomLayout(new ByteArrayInputStream(sb.toString().getBytes()));
    }
    catch (IOException ex)
    {
      log.error("problems with generating vis container", ex);
    }
    return null;
  }

  private List<String> mediaVisIds(ResolverEntry[] entries)
  {
    List<String> mediaIds = new ArrayList<String>();
    int counter = 0;
    for (ResolverEntry e : entries)
    {
      String id = "resolver-" + resultNumber + "-" + counter++;
      if ("media".equals(e.getVisType())
        || "audio".equals(e.getVisType())
        || "video".equals(e.getVisType()))
      {
        mediaIds.add(id);
      }
    }
    return mediaIds;
  }
}
