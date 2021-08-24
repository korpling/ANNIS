/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.resultview;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.IDGenerator;
import org.corpus_tools.annis.gui.QueryController;
import org.corpus_tools.annis.gui.controlpanel.QueryPanel;
import org.corpus_tools.annis.gui.objects.DisplayedResultQuery;
import org.corpus_tools.annis.gui.objects.Match;
import org.corpus_tools.annis.gui.objects.PagedResultQuery;
import org.corpus_tools.annis.gui.paging.PagingComponent;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.slf4j.LoggerFactory;

public class ResultViewPanel extends VerticalLayout {

  /**
   * Listens to events on the base text menu and updates the segmentation layer.
   */
  private class MenuBaseTextCommand implements MenuBar.Command {

    /**
     * 
     */
    private static final long serialVersionUID = -2796749478902349646L;

    @Override
    public void menuSelected(MenuItem selectedItem) {
      // remember old value
      String oldSegmentationLayer = sui.getQueryState().getVisibleBaseText().getValue();

      // set the new selected item
      String newSegmentationLayer = selectedItem.getText();

      if (NULL_SEGMENTATION_VALUE.equals(newSegmentationLayer)) {
        newSegmentationLayer = null;
      }
      for (MenuItem mi : miSegmentation.getChildren()) {
        mi.setChecked(mi == selectedItem);
      }

      if (oldSegmentationLayer != null) {
        if (!oldSegmentationLayer.equals(newSegmentationLayer)) {
          setSegmentationLayer(newSegmentationLayer);
        }
      } else if (newSegmentationLayer != null) {
        // oldSegmentation is null, but selected is not
        setSegmentationLayer(newSegmentationLayer);
      }

      // update URL with newly selected segmentation layer
      sui.getQueryState().getVisibleBaseText().setValue(newSegmentationLayer);
      sui.getSearchView().updateFragment(sui.getQueryController().getSearchQuery());
    }
  }

  /**
   * 
   */
  private static final long serialVersionUID = -5353763293248666891L;

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(ResultViewPanel.class);

  private static final String NULL_SEGMENTATION_VALUE = "tokens (default)";

  private final Map<HashSet<SingleResolverRequest>, LinkedHashSet<VisualizerRule>> cacheResolver;

  private final PagingComponent paging;

  private final MenuItem miTokAnnos;

  private final MenuItem miSegmentation;

  private final TreeMap<String, Boolean> tokenAnnoVisible;

  private final QueryController controller;

  private final Set<String> segmentationLayerSet =
      Collections.synchronizedSet(new TreeSet<String>());

  private final Set<String> tokenAnnotationLevelSet =
      Collections.synchronizedSet(new TreeSet<String>());

  private final CssLayout resultLayout;

  private final List<AbstractComponent> resultPanelList;

  private String segmentationName;

  private int currentResults;


  private final DisplayedResultQuery initialQuery;

  private final AnnisUI sui;

  public ResultViewPanel(AnnisUI ui, DisplayedResultQuery initialQuery) {
    this.sui = ui;
    this.tokenAnnoVisible = new TreeMap<>();
    this.controller = ui.getQueryController();
    this.initialQuery = initialQuery;

    cacheResolver = Collections.synchronizedMap(
        new HashMap<HashSet<SingleResolverRequest>, LinkedHashSet<VisualizerRule>>());

    resultPanelList = Collections.synchronizedList(new LinkedList<AbstractComponent>());

    resultLayout = new CssLayout();
    resultLayout.addStyleName("result-view-css");

    Panel resultPanel = new Panel(resultLayout);
    resultPanel.setSizeFull();
    resultPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
    resultPanel.addStyleName("result-view-panel");

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
    paging.setPageSize(initialQuery.getLimit(), false);
    paging.setInfo(initialQuery.getQuery());


    addComponent(paging, 1);

    setComponentAlignment(paging, Alignment.TOP_CENTER);
    setExpandRatio(paging, 0.0f);
  }

  public void addQueryResult(PagedResultQuery q, SaltProject p, ArrayList<Match> allMatches) {

    if (q == null) {
      return;
    }

    try {
      updateVariables(p);
      if (p.getCorpusGraphs().isEmpty()) {
        p.createCorpusGraph();
      }
      SCorpusGraph corpusGraph = p.getCorpusGraphs().get(0);
      AbstractComponent newPanel =
          createSingleResultPanel(corpusGraph, currentResults, q.getOffset(), allMatches);
      currentResults += 1;

      int numberOfResults = allMatches.size();
      String strResults = numberOfResults > 1 ? "results" : "result";
      sui.getSearchView().getControlPanel().getQueryPanel().setStatus(
          sui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus(),
          " (showing " + currentResults + "/" + numberOfResults + " " + strResults + ")");

      resultPanelList.add(newPanel);
      resultLayout.addComponent(newPanel);
      if (newPanel instanceof SingleResultPanel) {
        ((SingleResultPanel) newPanel)
            .setSegmentationLayer(sui.getQueryState().getVisibleBaseText().getValue());
      }



      if (currentResults == numberOfResults) {
        this.currentResults = 0;

        showFinishedSubgraphSearch();
        if (!initialQuery.getSelectedMatches().isEmpty()) {
          // scroll to the first selected match
          JavaScript.eval(
              "$(\".v-panel-content-result-view-panel\").animate({scrollTop: $(\".selected-match\").offset().top - $(\".result-view-panel\").offset().top}, 1000);");
        }
      }

    } catch (Throwable ex) {
      log.error(null, ex);
    }

  }

  @Override
  public void attach() {
    super.attach();
    IDGenerator.assignIDForFields(ResultViewPanel.this, resultLayout);
    IDGenerator.assignIDForEachField(paging);
  }


  private AbstractComponent createSingleResultPanel(SCorpusGraph corpusGraph, int localMatchIndex,
      long globalOffset, ArrayList<Match> allMatches) {
    Match m = new Match();
    if (allMatches != null && localMatchIndex >= 0 && localMatchIndex < allMatches.size()) {
      m = allMatches.get(localMatchIndex);
    }


    AbstractComponent panel;
    Optional<SDocument> doc = corpusGraph.getDocuments().stream()
        .filter(d -> d.getDocumentGraph() != null && !d.getDocumentGraph().getNodes().isEmpty())
        .findFirst();
    if (doc.isPresent()) {
      panel = new SingleResultPanel(doc.get(), m, localMatchIndex + globalOffset,
          new ResolverProviderImpl(cacheResolver), sui, getVisibleTokenAnnos(), segmentationName,
          controller, initialQuery);
    } else {
      Set<String> matchedCorpora = new LinkedHashSet<>();
      for (String id : m.getSaltIDs()) {
        SNode n = corpusGraph.getNode("salt:/" + id);
        if (n instanceof SCorpus || n instanceof SDocument) {
          matchedCorpora.add(n.getId());
        }
      }
      panel =
          new SingleCorpusResultPanel(matchedCorpora, localMatchIndex + globalOffset, initialQuery);
    }
    panel.setWidth("100%");
    panel.setHeight("-1px");

    return panel;
  }

  public PagingComponent getPaging() {
    return paging;
  }

  private Set<String> getSegmentationNames(SaltProject p) {
    Set<String> result = new TreeSet<>();

    for (SCorpusGraph corpusGraphs : p.getCorpusGraphs()) {
      for (SDocument doc : corpusGraphs.getDocuments()) {
        SDocumentGraph g = doc.getDocumentGraph();
        if (g != null) {
          List<SNode> orderRoots = g.getRootsByRelation(SALT_TYPE.SORDER_RELATION);
          // collect the start nodes of a segmentation chain of length 1
          if (orderRoots != null) {
            for (SNode n : orderRoots) {
              for (SRelation<?, ?> rel : n.getOutRelations()) {
                if (rel instanceof SOrderRelation) {
                  // the type is the name of the relation
                  result.add(rel.getType());
                }
              }
            }
          }
        } // end if graph not null
      }
    }

    return result;
  }

  public SortedSet<String> getVisibleTokenAnnos() {
    TreeSet<String> result = new TreeSet<>();

    for (Entry<String, Boolean> e : tokenAnnoVisible.entrySet()) {
      if (e.getValue().booleanValue() == true) {
        result.add(e.getKey());
      }
    }

    return result;
  }


  public void setCount(long count) {
    paging.setCount(count, false);
    paging.setStartNumber(initialQuery.getOffset());
  }


  private void setSegmentationLayer(String segmentationLayer) {
    for (Component p : resultPanelList) {
      if (p instanceof SingleResultPanel) {
        ((SingleResultPanel) p).setSegmentationLayer(segmentationLayer);
      }
    }
  }

  private void setVisibleTokenAnnosVisible(SortedSet<String> annos) {
    for (Component p : resultPanelList) {
      if (p instanceof SingleResultPanel) {
        ((SingleResultPanel) p).setVisibleTokenAnnosVisible(annos);
      }
    }
  }

  public void showFinishedSubgraphSearch() {
    // Search complete, stop progress bar control
    if (sui.getSearchView().getControlPanel().getQueryPanel().getPiCount() != null) {
      if (sui.getSearchView().getControlPanel().getQueryPanel().getPiCount().isVisible()) {
        sui.getSearchView().getControlPanel().getQueryPanel().getPiCount().setVisible(false);
        sui.getSearchView().getControlPanel().getQueryPanel().getPiCount().setEnabled(false);
      }
    }
    // also remove the info how many results have been fetched
    QueryPanel qp = sui.getSearchView().getControlPanel().getQueryPanel();
    qp.setStatus(qp.getLastPublicStatus());
  }

  /**
   * Informs the user about the searching process.
   *
   * @param query Represents a limited query
   */
  public void showMatchSearchInProgress(String segmentationName) {
    resultLayout.removeAllComponents();
    this.segmentationName = segmentationName;
  }

  public void showNoResult() {
    resultLayout.removeAllComponents();
    currentResults = 0;

    // nothing to show since we have an empty result
    Label lblNoResult = new Label("No matches found.");
    lblNoResult.setWidth("100%");
    lblNoResult.addStyleName("result-view-no-content");

    resultLayout.addComponent(lblNoResult);

    showFinishedSubgraphSearch();
  }

  public void showSubgraphSearchInProgress(PagedResultQuery q, float percent) {
    if (percent == 0.0f) {
      resultLayout.removeAllComponents();
      currentResults = 0;
    }

  }

  private void updateSegmentationLayer(Set<String> segLayers) {

    // clear the menu base text
    miSegmentation.removeChildren();

    // add the default token layer
    segLayers.add("");

    // iterate of all segmentation layers and add them to the menu
    for (String s : segLayers) {
      if (s != null) {
        // the new menu entry
        MenuItem miSingleSegLayer;
        /**
         * TODO maybe it would be better, to mark the default text level corresponding to the
         * corpus.properties.
         *
         * There exists always a default text level.
         */
        if ("".equals(s)) {
          miSingleSegLayer =
              miSegmentation.addItem(NULL_SEGMENTATION_VALUE, new MenuBaseTextCommand());
        } else {
          miSingleSegLayer = miSegmentation.addItem(s, new MenuBaseTextCommand());
        }

        // mark as selectable
        miSingleSegLayer.setCheckable(true);

        /**
         * Check if a segmentation item must set checked. If no segmentation layer is selected, set
         * the default layer as selected.
         */
        final String selectedSegmentationLayer =
            sui.getQueryState().getVisibleBaseText().getValue();
        if ((selectedSegmentationLayer == null && "".equals(s))

            || s.equals(selectedSegmentationLayer)) {
          miSingleSegLayer.setChecked(true);
        } else {
          miSingleSegLayer.setChecked(false);
        }
      }
    } // end iterate for segmentation layer
  }

  private void updateVariables(SaltProject p) {
    segmentationLayerSet.addAll(getSegmentationNames(p));
    tokenAnnotationLevelSet.addAll(Helper.getTokenAnnotationLevelSet(p));
    Set<String> hiddenTokenAnnos = null;

    Set<String> corpusNames = Helper.getToplevelCorpusNames(p);

    for (String corpusName : corpusNames) {

      CorpusConfiguration corpusConfig = Helper.getCorpusConfig(corpusName, UI.getCurrent());

      if (corpusConfig != null && corpusConfig.getView() != null
          && corpusConfig.getView().getHiddenAnnos() != null) {
        hiddenTokenAnnos = new HashSet<>(corpusConfig.getView().getHiddenAnnos());
      }
    }

    if (hiddenTokenAnnos != null) {
      for (String tokenLevel : hiddenTokenAnnos) {
        if (tokenAnnotationLevelSet.contains(tokenLevel)) {
          tokenAnnotationLevelSet.remove(tokenLevel);
        }
      }
    }

    updateSegmentationLayer(segmentationLayerSet);
    updateVisibleToken(tokenAnnotationLevelSet);
  }

  public void updateVisibleToken(Set<String> tokenAnnotationLevelSet) {
    // if no token annotations are there, do not show this mneu
    if (tokenAnnotationLevelSet == null || tokenAnnotationLevelSet.isEmpty()) {
      miTokAnnos.setVisible(false);
    } else {
      miTokAnnos.setVisible(true);
    }

    // add new annotations
    if (tokenAnnotationLevelSet != null) {
      for (String s : tokenAnnotationLevelSet) {
        if (!tokenAnnoVisible.containsKey(s)) {
          tokenAnnoVisible.put(s, Boolean.TRUE);
        }
      }
    }

    miTokAnnos.removeChildren();

    if (tokenAnnotationLevelSet != null) {
      for (final String a : tokenAnnotationLevelSet) {
        MenuItem miSingleTokAnno = miTokAnnos.addItem(a.replaceFirst("::", ":"), selectedItem -> {

          if (selectedItem.isChecked()) {
            tokenAnnoVisible.put(a, Boolean.TRUE);
          } else {
            tokenAnnoVisible.put(a, Boolean.FALSE);
          }

          setVisibleTokenAnnosVisible(getVisibleTokenAnnos());
        });

        miSingleTokAnno.setCheckable(true);
        miSingleTokAnno.setChecked(tokenAnnoVisible.get(a).booleanValue());
      }
    }
  }
}
