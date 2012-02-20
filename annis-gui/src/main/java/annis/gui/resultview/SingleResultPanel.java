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
import annis.gui.PluginSystem;
import annis.gui.Helper;
import annis.gui.MatchedNodeColors;
import annis.gui.MetaDataPanel;
import annis.model.AnnisConstants;
import annis.resolver.ResolverEntry;
import annis.service.AnnisService;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.GRAPH_TRAVERSE_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDominanceRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SGraphTraverseHandler;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.BasicEList;

/**
 *
 * @author thomas
 */
public class SingleResultPanel extends VerticalLayout implements
  Button.ClickListener
{

  private static final ThemeResource ICON_RESOURCE = new ThemeResource(
    "info.gif");
  private SDocument result;
  private Map<SNode, Long> markedAndCovered;
  private Map<String, String> markedCoveredMap;
  private Map<String, String> markedExactMap;
  private ResolverProvider resolverProvider;
  private PluginSystem ps;
  private List<KWICPanel> kwicPanels;
  private Button btInfo;
  private int resultNumber;
  private List<String> path;
  private Set<String> visibleTokenAnnos;
  private AnnisService service;

  public SingleResultPanel(final SDocument result, int resultNumber,
    ResolverProvider resolverProvider, PluginSystem ps,
    Set<String> visibleTokenAnnos)
  {
    this.ps = ps;
    this.result = result;
    this.resolverProvider = resolverProvider;
    this.resultNumber = resultNumber;
    this.visibleTokenAnnos = visibleTokenAnnos;

    calculateHelperVariables();

    setWidth("100%");
    setHeight("-1px");


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
    service = Helper.getService(getApplication(), getWindow());
    if (service != null && resolverProvider != null)
    {
      try
      {
        ResolverEntry[] entries =
          resolverProvider.getResolverEntries(result, service);
        List<String> mediaIDs = mediaVisIds(entries);
        List<VisualizerPanel> visualizers = new LinkedList<VisualizerPanel>();
        List<VisualizerPanel> mediaVisualizer = new ArrayList<VisualizerPanel>();


        for (int i = 0; i < entries.length; i++)
        {
          String id = "resolver-" + resultNumber + "-" + i;
          CustomLayout customLayout = this.customLayout(id);

          VisualizerPanel p = new VisualizerPanel(entries[i], result, ps,
            markedExactMap, markedCoveredMap, customLayout, mediaIDs);

          if ("video".equals(entries[i].getVisType())
            || "audio".equals(entries[i].getVisType()))
          {
            mediaVisualizer.add(p);
          }

          visualizers.add(p);
        }

        kwicPanels = new ArrayList<KWICPanel>();
        for (STextualDS text : result.getSDocumentGraph().getSTextualDSs())
        {
          KWICPanel kwic = new KWICPanel(result, visibleTokenAnnos,
            markedAndCovered, text, mediaIDs, mediaVisualizer, this);

          addComponent(kwic);
          kwicPanels.add(kwic);
        }


        for (VisualizerPanel p : visualizers)
        {
          addComponent(p);
        }
      }
      catch (RemoteException ex)
      {
        Logger.getLogger(SingleResultPanel.class.getName()).log(Level.SEVERE,
          "could not get resolver entries", ex);
        getWindow().showNotification("could not get resolver entries: ", ex.
          getLocalizedMessage(), Notification.TYPE_TRAY_NOTIFICATION);
      }
    }

    super.attach();
  }

  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
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

    for (SNode n : result.getSDocumentGraph().getSNodes())
    {

      SFeature featMatched = n.getSFeature(AnnisConstants.ANNIS_NS,
        AnnisConstants.FEAT_MATCHEDNODE);
      Long match = featMatched == null ? null : featMatched.getSValueSNUMERIC();

      if (match != null)
      {
        int color = Math.max(0, Math.min((int) match.longValue() - 1,
          MatchedNodeColors.values().length - 1));
        SFeature feat = n.getSFeature(AnnisConstants.ANNIS_NS,
          AnnisConstants.FEAT_INTERNALID);
        if (feat != null)
        {
          markedExactMap.put("" + feat.getSValueSNUMERIC(),
            MatchedNodeColors.values()[color].name());
        }
      }

    }

    markedAndCovered = calculateMarkedAndCoveredIDs(result);

    for (Entry<SNode, Long> markedEntry : markedAndCovered.entrySet())
    {
      int color = Math.max(0, Math.min((int) markedEntry.getValue().longValue()
        - 1,
        MatchedNodeColors.values().length - 1));
      SFeature feat = markedEntry.getKey().getSFeature(AnnisConstants.ANNIS_NS,
        AnnisConstants.FEAT_INTERNALID);
      if (feat != null)
      {
        markedCoveredMap.put("" + feat.getSValueSNUMERIC(),
          MatchedNodeColors.values()[color].name());
      }
    }
  }

  private Map<SNode, Long> calculateMarkedAndCoveredIDs(
    SDocument doc)
  {
    Set<String> matchedNodes = new HashSet<String>();
    Map<SNode, Long> initialCovered = new HashMap<SNode, Long>();

    // add all covered nodes
    for (SNode n : doc.getSDocumentGraph().getSNodes())
    {
      SFeature featMatched = n.getSFeature(AnnisConstants.ANNIS_NS,
        AnnisConstants.FEAT_MATCHEDNODE);
      Long match = featMatched == null ? null : featMatched.getSValueSNUMERIC();

      if (match != null)
      {
        matchedNodes.add(n.getSId());
        initialCovered.put(n, match);
      }
    }

    CoveredMatchesCalculator cmc = new CoveredMatchesCalculator(doc.
      getSDocumentGraph(), initialCovered);

    return cmc.getMatchedAndCovered();
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

  private CustomLayout customLayout(String id)
  {
    String layout = ""
      + "<div id=\"" + id + "\">"
      + "  <div location=\"btEntry\"></div>"
      + "  <div location=\"iframe\"></div>"
      + "</div>";
    try
    {
      return new CustomLayout(new ByteArrayInputStream(layout.getBytes()));
    }
    catch (IOException ex)
    {
      Logger.getLogger(SingleResultPanel.class.getName()).
        log(Level.SEVERE, null, ex);
    }
    return null;
  }

  private List<String> mediaVisIds(ResolverEntry[] entries) throws
    RemoteException
  {
    List<String> mediaIds = new ArrayList<String>();
    int counter = 0;
    for (ResolverEntry e : entries)
    {
      String id = "resolver-" + resultNumber + "-" + counter++;
      if ("audio".equals(e.getVisType()) || "video".equals(e.getVisType()))
      {
        mediaIds.add(id);
      }
    }
    return mediaIds;
  }
}
