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
package annis.gui.resultview;

import annis.gui.AnnisUI;
import annis.gui.MetaDataPanel;
import annis.gui.QueryController;
import annis.gui.query_references.ShareSingleMatchGenerator;
import annis.libgui.Helper;
import annis.libgui.IDGenerator;
import annis.model.DisplayedResultQuery;
import annis.model.PagedResultQuery;
import annis.service.objects.Match;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.ComboBox;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SNode;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class SingleResultPanel extends CssLayout
    implements Button.ClickListener, VisualizerContextChanger {
  private static class AddNewItemHandler implements AbstractSelect.NewItemHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -8372467324750771745L;
    final private ComboBox combobox;

    public AddNewItemHandler(ComboBox comboBox) {
      this.combobox = comboBox;
    }

    @Override
    public void addNewItem(String newValue) {

      String ERROR_MESSAGE_HEADER = "Illegal value";

      try {
        int i = Integer.parseInt(newValue);

        if (i < 0) {
          new Notification(ERROR_MESSAGE_HEADER, "<div><p>context &lt; 0 makes no sense</p></div>",
              Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
        } else {

          Item it = combobox.getContainerDataSource().addItem(i);
          // check if the item was actually added or might have been available before.
          if (it != null) {
            it.getItemProperty("number").setValue(i);

            if (combobox.getContainerDataSource() instanceof IndexedContainer) {
              ((IndexedContainer) combobox.getContainerDataSource()).sort(new Object[] {"number"},
                  new boolean[] {true});
            }

            combobox.select(i);
          }
        }
      } catch (NumberFormatException ex) {
        new Notification(ERROR_MESSAGE_HEADER, "<div><p>Only numbers are allowed.</p></div>",
            Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
      }
    }
  }

  private class ContextChangeListener implements Property.ValueChangeListener {

    /**
     * 
     */
    private static final long serialVersionUID = 2450929501646415981L;

    long resultNumber;

    boolean left;

    public ContextChangeListener(long resultNumber, boolean left) {
      this.resultNumber = resultNumber;
      this.left = left;
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
      showReloadingProgress();
      lftCtxCombo.setEnabled(false);
      rghtCtxCombo.setEnabled(false);
      int ctx = Integer.parseInt(event.getProperty().getValue().toString());
      changeContext(resultNumber, ctx, left);
    }
  }

  private static final long serialVersionUID = 2L;

  private static final String INITIAL_OPEN = "initial_open";

  private static final Resource ICON_RESOURCE = FontAwesome.INFO_CIRCLE;

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SingleResultPanel.class);

  private SDocument result;

  private Map<String, String> markedExactMap;

  private final AnnisUI ui;
  private List<VisualizerPanel> visualizers;

  private List<VisualizerRule> resolverEntries;

  private final Button btInfo;

  private final Button btLink;

  private final List<String> path;

  private String segmentationName;

  private final HorizontalLayout infoBar;

  private final QueryController queryController;

  private final long resultNumber;

  private final ResolverProvider resolverProvider;

  private final Set<String> visibleTokenAnnos;

  private ProgressBar reloadVisualizer;

  private final ComboBox lftCtxCombo;

  private final ComboBox rghtCtxCombo;

  private final Map<Integer, Boolean> visualizerState;

  private PagedResultQuery query;

  private final Match match;

  public SingleResultPanel(final SDocument result, Match match, long resultNumber,
      ResolverProvider resolverProvider, AnnisUI ui, Set<String> visibleTokenAnnos,
      String segmentationName, QueryController controller, DisplayedResultQuery query) {
    this.ui = ui;
    this.result = result;
    this.segmentationName = segmentationName;
    this.queryController = controller;
    this.resultNumber = resultNumber;
    this.resolverProvider = resolverProvider;
    this.visibleTokenAnnos = visibleTokenAnnos;
    this.query = query;
    this.match = match;

    calculateHelperVariables();

    setWidth("100%");
    setHeight("-1px");

    if (query != null && query.getSelectedMatches().contains(resultNumber)) {
      addStyleName("selected-match");
    }

    infoBar = new HorizontalLayout();
    infoBar.addStyleName("info-bar");
    infoBar.setWidth("100%");
    infoBar.setHeight("-1px");

    Label lblNumber = new Label("" + (resultNumber + 1));
    infoBar.addComponent(lblNumber);
    lblNumber.setSizeUndefined();

    btLink = new Button();
    btLink.setStyleName(ValoTheme.BUTTON_BORDERLESS);
    btLink.setIcon(FontAwesome.SHARE_ALT);
    btLink.setDescription("Share match reference");
    btLink.setDisableOnClick(true);
    btLink.addClickListener(event -> showShareSingleMatchGenerator());
    infoBar.addComponent(btLink);

    btInfo = new Button();
    btInfo.setStyleName(ValoTheme.BUTTON_BORDERLESS);
    btInfo.setIcon(ICON_RESOURCE);
    btInfo.setDescription("Show metadata");
    btInfo.addClickListener(this);
    infoBar.addComponent(btInfo);

    /**
     * Extract the top level corpus name and the document name of this single result.
     */
    path = Helper.getCorpusPath(result.getGraph(), result);
    Collections.reverse(path);

    // build label
    StringBuilder sb = new StringBuilder("Path: ");
    sb.append(StringUtils.join(path, " > "));

    Label lblPath = new Label(sb.toString());
    lblPath.addStyleName("path-label");

    lblPath.setWidth("100%");
    lblPath.setHeight("-1px");
    infoBar.addComponent(lblPath);
    infoBar.setExpandRatio(lblPath, 1.0f);
    infoBar.setSpacing(false);

    this.visualizerState = new HashMap<>();

    // init context combox
    lftCtxCombo = new ComboBox();
    rghtCtxCombo = new ComboBox();

    lftCtxCombo.setWidth(50, Unit.PIXELS);
    rghtCtxCombo.setWidth(50, Unit.PIXELS);

    lftCtxCombo.setNullSelectionAllowed(false);
    rghtCtxCombo.setNullSelectionAllowed(false);

    lftCtxCombo.addStyleName(ValoTheme.COMBOBOX_SMALL);
    rghtCtxCombo.addStyleName(ValoTheme.COMBOBOX_SMALL);

    IndexedContainer lftCtxContainer = new IndexedContainer();
    IndexedContainer rghtCtxContainer = new IndexedContainer();

    // and a property for sorting
    lftCtxContainer.addContainerProperty("number", Integer.class, 0);
    rghtCtxContainer.addContainerProperty("number", Integer.class, 0);

    for (int i = 0; i < 30; i += 5) {
      lftCtxContainer.addItem(i).getItemProperty("number").setValue(i);
      rghtCtxContainer.addItem(i).getItemProperty("number").setValue(i);
    }

    int lftContextIdx = query == null ? 0 : query.getLeftContext();
    lftCtxContainer.addItem(lftContextIdx);
    lftCtxContainer.sort(new Object[] {"number"}, new boolean[] {true});

    int rghtCtxIdx = query == null ? 0 : query.getRightContext();
    rghtCtxContainer.addItem(rghtCtxIdx);

    rghtCtxContainer.sort(new Object[] {"number"}, new boolean[] {true});

    lftCtxCombo.setContainerDataSource(lftCtxContainer);
    rghtCtxCombo.setContainerDataSource(rghtCtxContainer);

    lftCtxCombo.select(lftContextIdx);
    rghtCtxCombo.select(rghtCtxIdx);

    lftCtxCombo.setNewItemsAllowed(true);
    rghtCtxCombo.setNewItemsAllowed(true);

    lftCtxCombo.setImmediate(true);
    rghtCtxCombo.setImmediate(true);

    lftCtxCombo.setNewItemHandler(new AddNewItemHandler(lftCtxCombo));
    rghtCtxCombo.setNewItemHandler(new AddNewItemHandler(rghtCtxCombo));

    lftCtxCombo.addValueChangeListener(new ContextChangeListener(resultNumber, true));
    rghtCtxCombo.addValueChangeListener(new ContextChangeListener(resultNumber, false));

    Label leftCtxLabel = new Label("left context: ");
    Label rightCtxLabel = new Label("right context: ");

    leftCtxLabel.setWidth("-1px");
    rightCtxLabel.setWidth("-1px");

    HorizontalLayout ctxLayout = new HorizontalLayout();
    ctxLayout.setSpacing(true);
    ctxLayout.addComponents(leftCtxLabel, lftCtxCombo, rightCtxLabel, rghtCtxCombo);
    infoBar.addComponent(ctxLayout);

    addComponent(infoBar);
  }

  @Override
  public void attach() {
    super.attach();

    initVisualizer();
    if (ui.getConfig().isShortenReferenceLinks() && !ui.isDesktopMode()) {
      btLink.setVisible(true);
    } else {
      btLink.setVisible(false);
    }
    IDGenerator.assignIDForFields(SingleResultPanel.this, infoBar, btInfo);
  }

  @Override
  public void buttonClick(ClickEvent event) {
    if (event.getButton() == btInfo && result != null) {
      Window infoWindow = new Window("Info for " + result.getId());

      infoWindow.setModal(false);
      MetaDataPanel meta = new MetaDataPanel(path.get(0), Optional.of(path.get(path.size() - 1)));
      infoWindow.setContent(meta);
      infoWindow.setWidth("400px");
      infoWindow.setHeight("400px");

      UI.getCurrent().addWindow(infoWindow);
    }
  }

  private void calculateHelperVariables() {
    markedExactMap = new HashMap<>();
  }

  @Override
  public void changeContext(long resultNumber, int context, boolean left) {
    // delegates the task to the query controller.

    queryController.changeContext(query, match, resultNumber, context, this, left);
  }

  protected SDocument getResult() {
    return result;
  }

  private void initVisualizer() {
    try {
      resolverEntries = resolverProvider == null ? new LinkedList<>()
          : resolverProvider.getResolverEntries(result, UI.getCurrent());
      visualizers = new LinkedList<>();

      List<VisualizerPanel> openVisualizers = new LinkedList<>();

      List<SNode> segNodes =
          Helper.getSortedSegmentationNodes(segmentationName, result.getDocumentGraph());
      Map<SNode, Long> markedAndCovered =
          Helper.calculateMarkedAndCovered(result, segNodes, segmentationName);

      String resultID = "" + new Random().nextInt(Integer.MAX_VALUE);

      int i = 0;
      for (VisualizerRule visRule : resolverEntries) {
        String htmlID = "resolver-" + resultNumber + "_" + i;

        VisualizerPanel p = new VisualizerPanel(visRule, i, result, match, visibleTokenAnnos,
            markedAndCovered, htmlID, resultID, this, segmentationName, ui);

        visualizers.add(p);

        // check if there is the visibility of a visualizer changed
        // since it the whole result panel was loaded. If not the entry of the
        // resolver entry is used, for determine the visibility status
        if (visualizerState.containsKey(i)) {
          if (visualizerState.get(i)) {
            openVisualizers.add(p);
          }
        } else {
          if (Boolean.parseBoolean(visRule.getMappings().getOrDefault(INITIAL_OPEN, "false"))) {
            openVisualizers.add(p);
          }
        }
        i++;
      } // for each resolver entry

      // attach visualizer
      for (VisualizerPanel p : visualizers) {
        addComponent(p);
      }

      for (VisualizerPanel p : openVisualizers) {
        p.toggleVisualizer(true, null);
      }
    } catch (RuntimeException ex) {
      log.error("problems with initializing Visualizer Panel", ex);
    } catch (Exception ex) {
      log.error("problems with initializing Visualizer Panel", ex);
    }
  }

  @Override
  public void registerVisibilityStatus(int entryId, boolean status) {
    visualizerState.put(entryId, status);
  }

  public void setSegmentationLayer(String segmentationName) {
    this.segmentationName = segmentationName;

    if (result != null) {
      List<SNode> segNodes =
          Helper.getSortedSegmentationNodes(segmentationName, result.getDocumentGraph());
      Map<SNode, Long> markedAndCovered =
          Helper.calculateMarkedAndCovered(result, segNodes, segmentationName);
      for (VisualizerPanel p : visualizers) {
        p.setSegmentationLayer(segmentationName, markedAndCovered);
      }
    }
  }

  public void setVisibleTokenAnnosVisible(SortedSet<String> annos) {
    for (VisualizerPanel p : visualizers) {
      p.setVisibleTokenAnnosVisible(annos);
    }
  }

  private void showReloadingProgress() {
    // remove the old visualizer
    for (VisualizerPanel v : visualizers) {
      this.removeComponent(v);
    }

    // first set loading indicator
    reloadVisualizer = new ProgressBar(1.0f);
    reloadVisualizer.setIndeterminate(true);
    reloadVisualizer.setSizeFull();
    reloadVisualizer.setHeight(150, Unit.PIXELS);
    addComponent(reloadVisualizer);
  }

  private void showShareSingleMatchGenerator() {
    // select the current match
    if (ui != null) {
      ui.getQueryState().getSelectedMatches().getValue().clear();
      ui.getQueryState().getSelectedMatches().getValue().add(resultNumber);
      ui.getSearchView().updateFragment(ui.getQueryController().getSearchQuery());
    }

    Window window = new ShareSingleMatchGenerator(resolverEntries, match, query, segmentationName,
        ui.getVisualizerPlugins());
    window.setWidth(790, Unit.PIXELS);
    window.setHeight(680, Unit.PIXELS);
    window.setResizable(true);
    window.setModal(true);

    window.addCloseListener(e -> btLink.setEnabled(true));
    window.setCaption("Match reference link");

    UI.getCurrent().addWindow(window);
  }

  @Override
  public void updateResult(SaltProject p, PagedResultQuery query) {
    this.query = query;
    if (p != null && p.getCorpusGraphs() != null && !p.getCorpusGraphs().isEmpty()
        && p.getCorpusGraphs().get(0) != null && p.getCorpusGraphs().get(0).getDocuments() != null
        && !p.getCorpusGraphs().get(0).getDocuments().isEmpty()) {
      this.result = p.getCorpusGraphs().get(0).getDocuments().get(0);
    }

    removeComponent(reloadVisualizer);
    initVisualizer();

    lftCtxCombo.setEnabled(true);
    rghtCtxCombo.setEnabled(true);
  }
}
