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

import annis.gui.PluginSystem;
import annis.gui.Helper;
import annis.gui.MatchedNodeColors;
import annis.gui.MetaDataPanel;
import annis.model.AnnisNode;
import annis.model.AnnotationGraph;
import annis.resolver.ResolverEntry;
import annis.service.AnnisService;
import annis.service.ifaces.AnnisResult;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author thomas
 */
public class SingleResultPanel extends VerticalLayout implements
  Button.ClickListener
{

  private static final ThemeResource ICON_RESOURCE = new ThemeResource(
    "info.gif");
  private AnnisResult result;
  private Map<AnnisNode, Long> markedAndCovered;
  private Map<String, String> markedCoveredMap;
  private Map<String, String> markedExactMap;
  private Set<Long> containedTexts;
  private ResolverProvider resolverProvider;
  private PluginSystem ps;
  private List<KWICPanel> kwicPanels;
  private Button btInfo;
  private int resultNumber;
  private Set<String> visibleTokenAnnos;

  public SingleResultPanel(final AnnisResult result, int resultNumber,
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

    List<String> path = new LinkedList<String>(Arrays.asList(result.getGraph().
      getPath()));
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
    AnnisService service = Helper.getService(getApplication(), getWindow());
    if (service != null && resolverProvider != null)
    {
      try
      {
        ResolverEntry[] entries =
          resolverProvider.getResolverEntries(result, service);

        List<String> mediaIDs = new LinkedList<String>();
        List<VisualizerPanel> visualizers = new LinkedList<VisualizerPanel>();
        List<VisualizerPanel> mediaVisualizer = new ArrayList<VisualizerPanel>();


        int counter = 0;
        for (ResolverEntry e : entries)
        {
          String id = "resolver-" + resultNumber + "-" + counter++;
          CustomLayout customLayout = this.customLayout(id);

          VisualizerPanel p = new VisualizerPanel(e, result, ps, markedExactMap,
            markedCoveredMap, customLayout);

          if ("video".equals(e.getVisType()) || "audio".equals(e.getVisType()))
          {
            mediaIDs.add(id);
            mediaVisualizer.add(p);
          }

          visualizers.add(p);
        }

        kwicPanels = new ArrayList<KWICPanel>();
        for (long textId : containedTexts)
        {
          KWICPanel kwic = new KWICPanel(result, visibleTokenAnnos,
            markedAndCovered, textId, mediaIDs, mediaVisualizer);
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
    containedTexts = new TreeSet<Long>();

    for (AnnisNode n : result.getGraph().getNodes())
    {
      containedTexts.add(n.getTextId());

      Long match = n.getMatchedNodeInQuery();
      if (match != null)
      {
        int color = Math.max(0, Math.min((int) match.longValue() - 1,
          MatchedNodeColors.values().length - 1));
        markedExactMap.put("" + n.getId(),
          MatchedNodeColors.values()[color].name());
      }
    }

    markedAndCovered = calculateMarkedAndCoveredIDs(result.getGraph());

    for (Entry<AnnisNode, Long> markedEntry : markedAndCovered.entrySet())
    {
      int color = Math.max(0, Math.min((int) markedEntry.getValue().longValue()
        - 1,
        MatchedNodeColors.values().length - 1));
      markedCoveredMap.put("" + markedEntry.getKey().getId(),
        MatchedNodeColors.values()[color].name());
    }
  }

  private Map<AnnisNode, Long> calculateMarkedAndCoveredIDs(
    AnnotationGraph graph)
  {
    Set<Long> matchedNodes = graph.getMatchedNodeIds();
    Map<AnnisNode, Long> matchedAndCovered = new HashMap<AnnisNode, Long>();

    // add all covered nodes
    for (AnnisNode n : graph.getNodes())
    {
      if (matchedNodes.contains(n.getId()) && n.getMatchedNodeInQuery() != null)
      {
        Long matchPosition = n.getMatchedNodeInQuery();
        matchedAndCovered.put(n, matchPosition);

        long left = n.getLeftToken();
        long right = n.getRightToken();

        for (long i = left; i <= right; i++)
        {
          AnnisNode tok = graph.getToken(i);
          Long oldTokenPosition = matchedAndCovered.get(tok);
          if (oldTokenPosition == null
            || (!matchedNodes.contains(tok.getId()) && matchPosition.compareTo(
            oldTokenPosition) >= 0))
          {
            matchedAndCovered.put(tok, matchPosition);
          }
        }
      }
    }

    return matchedAndCovered;
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    if (event.getButton() == btInfo)
    {
      AnnotationGraph graph = result.getGraph();

      long textId = graph.getNodes().size() > 0 ? graph.getNodes().get(0).
        getTextId()
        : -1;

      Window infoWindow = new Window("Info for " + graph.getDocumentName());

      infoWindow.setModal(false);
      MetaDataPanel meta = new MetaDataPanel(textId);
      infoWindow.setContent(meta);
      infoWindow.setWidth("400px");
      infoWindow.setHeight("400px");

      getWindow().addWindow(infoWindow);
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
}
