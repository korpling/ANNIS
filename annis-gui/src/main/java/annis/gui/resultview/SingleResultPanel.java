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
import annis.libgui.MatchedNodeColors;
import annis.gui.MetaDataPanel;
import annis.libgui.InstanceConfig;
import annis.libgui.PluginSystem;
import static annis.model.AnnisConstants.*;
import annis.model.RelannisNodeFeature;
import annis.resolver.ResolverEntry;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.*;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class SingleResultPanel extends CssLayout implements
  Button.ClickListener
{

  private static final String HIDE_KWIC = "hide_kwic";

  private static final String INITIAL_OPEN = "initial_open";

  private static final ThemeResource ICON_RESOURCE = new ThemeResource(
    "info.gif");

  private transient SDocument result;

  private transient Map<SNode, Long> markedAndCovered;

  private Map<String, String> markedCoveredMap;

  private Map<String, String> markedExactMap;

  private transient PluginSystem ps;

  private List<VisualizerPanel> visualizers;

  private Button btInfo;

  private List<String> path;

  private String segmentationName;

  private transient List<SToken> token;

  private HorizontalLayout infoBar;

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    SingleResultPanel.class);

  public SingleResultPanel(final SDocument result, int resultNumber,
    ResolverProvider resolverProvider, PluginSystem ps,
    Set<String> visibleTokenAnnos, String segmentationName,
    InstanceConfig instanceConfig)
  {
    this.ps = ps;
    this.result = result;
    this.segmentationName = segmentationName;

    calculateHelperVariables();

    setWidth("100%");
    setHeight("-1px");

    infoBar = new HorizontalLayout();
    infoBar.addStyleName("docPath");
    infoBar.setWidth("100%");
    infoBar.setHeight("-1px");

    Label lblNumber = new Label("" + (resultNumber + 1));
    infoBar.addComponent(lblNumber);
    lblNumber.setSizeUndefined();

    btInfo = new Button();
    btInfo.setStyleName(ChameleonTheme.BUTTON_LINK);
    btInfo.setIcon(ICON_RESOURCE);
    btInfo.addClickListener((Button.ClickListener) this);
    infoBar.addComponent(btInfo);

    path = CommonHelper.getCorpusPath(result.getSCorpusGraph(),
      result);
    Collections.reverse(path);

    MinMax minMax = getIds(result.getSDocumentGraph());

    // build label
    StringBuilder sb = new StringBuilder("Path: ");
    sb.append(StringUtils.join(path, " > "));
    sb.append(" (tokens ").append(minMax.min);
    sb.append(" - ").append(minMax.max).append(")");

    Label lblPath = new Label(sb.toString());

    lblPath.setWidth("100%");
    lblPath.setHeight("-1px");
    infoBar.addComponent(lblPath);
    infoBar.setExpandRatio(lblPath, 1.0f);
    infoBar.setSpacing(true);

    // THIS WAS in attach()
    addComponent(infoBar);

    try
    {
      ResolverEntry[] entries =
        resolverProvider.getResolverEntries(result);
      visualizers = new LinkedList<VisualizerPanel>();
      List<VisualizerPanel> openVisualizers = new LinkedList<VisualizerPanel>();

      token = result.getSDocumentGraph().getSortedSTokenByText();

      List<SNode> segNodes = CommonHelper.getSortedSegmentationNodes(
        segmentationName,
        result.getSDocumentGraph());

      markedAndCovered = calculateMarkedAndCoveredIDs(result, segNodes);
      calulcateColorsForMarkedAndCoverd();

      String resultID = "" + new Random().nextInt(Integer.MAX_VALUE);

      for (int i = 0; i < entries.length; i++)
      {
        String htmlID = "resolver-" + resultNumber + "_" + i;

        VisualizerPanel p = new VisualizerPanel(
          entries[i], result,
          token, visibleTokenAnnos, markedAndCovered,
          markedCoveredMap, markedExactMap,
          htmlID, resultID, this,
          segmentationName, ps, instanceConfig);

        visualizers.add(p);
        Properties mappings = entries[i].getMappings();
        if (Boolean.parseBoolean(mappings.getProperty(INITIAL_OPEN, "false")))
        {
          openVisualizers.add(p);
        }

      } // for each resolver entry

      for (VisualizerPanel p : visualizers)
      {
        addComponent(p);
      }

      for (VisualizerPanel p : openVisualizers)
      {
        p.toggleVisualizer(true, null);
      }

    }
    catch (RuntimeException ex)
    {
      log.error("problems with initializing Visualizer Panel", ex);
    }
    catch (Exception ex)
    {
      log.error("problems with initializing Visualizer Panel", ex);
    }
  }

  public void setSegmentationLayer(String segmentationName)
  {
    this.segmentationName = segmentationName;

    if (result != null)
    {
      List<SNode> segNodes = CommonHelper.getSortedSegmentationNodes(
        segmentationName,
        result.getSDocumentGraph());
      markedAndCovered = calculateMarkedAndCoveredIDs(result, segNodes);
      for (VisualizerPanel p : visualizers)
      {
        p.setSegmentationLayer(segmentationName, markedAndCovered);
      }
    }
  }

  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
    for (VisualizerPanel p : visualizers)
    {
      p.setVisibleTokenAnnosVisible(annos);
    }
  }

  private void calculateHelperVariables()
  {
    markedExactMap = new HashMap<String, String>();
    markedCoveredMap = new HashMap<String, String>();

    if (result != null)
    {
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
            RelannisNodeFeature feat = RelannisNodeFeature.extract(n);
            if (feat != null)
            {
              markedExactMap.put("" + feat.getInternalID(),
                MatchedNodeColors.values()[color].name());
            }
          }

        }
      } // end if g not null
    } // end if result not null
  }

  private void calulcateColorsForMarkedAndCoverd()
  {
    if (markedAndCovered != null)
    {
      for (Entry<SNode, Long> markedEntry : markedAndCovered.entrySet())
      {
        int color = Math.max(0, Math.min((int) markedEntry.getValue().
          longValue()
          - 1,
          MatchedNodeColors.values().length - 1));
        RelannisNodeFeature feat = RelannisNodeFeature.extract(markedEntry.
          getKey());

        if (feat != null)
        {
          markedCoveredMap.put("" + feat.getInternalID(),
            MatchedNodeColors.values()[color].name());
        }
      } // end for each entry in markedAndCoverd
    } // end if markedAndCovered not null
  }

  private Map<SNode, Long> calculateMarkedAndCoveredIDs(
    SDocument doc, List<SNode> segNodes)
  {
    Map<SNode, Long> initialCovered = new HashMap<SNode, Long>();

    // add all covered nodes
    for (SNode n : doc.getSDocumentGraph().getSNodes())
    {
      SFeature featMatched = n.getSFeature(ANNIS_NS,
        FEAT_MATCHEDNODE);
      Long match = featMatched == null ? null : featMatched.getSValueSNUMERIC();

      if (match != null)
      {
        initialCovered.put(n, match);
      }
    }

    // calculate covered nodes
    SingleResultPanel.CoveredMatchesCalculator cmc = new SingleResultPanel.CoveredMatchesCalculator(
      doc.
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
        RelannisNodeFeature featSegNode = (RelannisNodeFeature) segNode.
          getSFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

        if (!covered.containsKey(segNode))
        {
          long leftTok = featSegNode.getLeftToken();
          long rightTok = featSegNode.getRightToken();

          // check for each covered token if this segment is covering it
          for (Map.Entry<SToken, Long> e : coveredToken.entrySet())
          {
            RelannisNodeFeature featTok = (RelannisNodeFeature) e.getKey().
              getSFeature(ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
            long entryTokenIndex = featTok.getTokenIndex();
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
    if (event.getButton() == btInfo && result != null)
    {
      Window infoWindow = new Window("Info for " + result.getSId());

      infoWindow.setModal(false);
      MetaDataPanel meta = new MetaDataPanel(path.get(0), path.get(path.size()
        - 1));
      infoWindow.setContent(meta);
      infoWindow.setWidth("400px");
      infoWindow.setHeight("400px");

      UI.getCurrent().addWindow(infoWindow);
    }
  }

  /**
   * Marks all nodes which are dominated by already marked nodes.
   *
   * 1. Sort ascending all initial marked nodes by the size of the intervall
   * between left and right token index.
   *
   * 2. Traverse the salt document graph with the sorted list of step 1. as root
   * nodes and mark all children with the same match position. Already marked
   * nodes are omitted.
   *
   * Note: The algorithm prevents nested marked nodes to be overwritten. Nested
   * nodes must have a smaller intervall from left to right by default, so this
   * should always work.
   *
   */
  public static class CoveredMatchesCalculator implements SGraphTraverseHandler
  {

    private Map<SNode, Long> matchedAndCovered;

    private long currentMatchPos;

    public CoveredMatchesCalculator(SDocumentGraph graph,
      Map<SNode, Long> initialMatches)
    {
      this.matchedAndCovered = initialMatches;

      Map<SNode, Long> sortedByOverlappedTokenIntervall = new TreeMap<SNode, Long>(
        new Comparator<SNode>()
      {
        @Override
        public int compare(SNode o1, SNode o2)
        {
          RelannisNodeFeature feat1 = (RelannisNodeFeature) o1.getSFeature(
            ANNIS_NS, FEAT_RELANNIS_NODE).getValue();
          RelannisNodeFeature feat2 = (RelannisNodeFeature) o2.getSFeature(
            ANNIS_NS, FEAT_RELANNIS_NODE).getValue();

          long leftTokIdxO1 = feat1.getLeftToken();
          long rightTokIdxO1 = feat1.getRightToken();
          long leftTokIdxO2 = feat2.getLeftToken();
          long rightTokIdxO2 = feat2.getRightToken();

          int intervallO1 = (int) Math.abs(leftTokIdxO1 - rightTokIdxO1);
          int intervallO2 = (int) Math.abs(leftTokIdxO2 - rightTokIdxO2);

          if (intervallO1 - intervallO2 != 0)
          {
            return intervallO1 - intervallO2;
          }
          else if (feat1.getLeftToken() - feat2.getRightToken() != 0)
          {
            return (int) (feat1.getLeftToken() - feat2.getRightToken());
          }
          else if (feat1.getRightToken() - feat2.getRightToken() != 0)
          {
            return (int) (feat1.getRightToken() - feat2.getRightToken());
          }
          else
          {
            return (int) (feat1.getInternalID() - feat2.getInternalID());
          }
        }
      });

      for (Map.Entry<SNode, Long> entry : initialMatches.entrySet())
      {
        sortedByOverlappedTokenIntervall.put(entry.getKey(), entry.getValue());
      }

      currentMatchPos = 1;
      if (initialMatches.size() > 0)
      {
        graph.traverse(new BasicEList<SNode>(sortedByOverlappedTokenIntervall.
          keySet()),
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

  private class MinMax
  {

    long min;

    long max;

  }

  private MinMax getIds(SDocumentGraph graph)
  {
    EList<SToken> sTokens = graph.getSTokens();


    MinMax minMax = new MinMax();
    minMax.min = Long.MAX_VALUE;
    minMax.max = Long.MIN_VALUE;

    if (sTokens != null)
    {
      for (SToken t : sTokens)
      {
        RelannisNodeFeature f = (RelannisNodeFeature) t.getSFeature(ANNIS_NS,
          FEAT_RELANNIS_NODE).getValue();

        if (minMax.min > f.getTokenIndex())
        {
          minMax.min = f.getTokenIndex();
        }

        if (minMax.max < f.getTokenIndex())
        {
          minMax.max = f.getTokenIndex();
        }
      }
    }

    return minMax;
  }
}
