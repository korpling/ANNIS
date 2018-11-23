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
import annis.gui.AnnisUI;
import annis.gui.ShareSingleMatchGenerator;
import annis.gui.MetaDataPanel;
import annis.gui.QueryController;
import annis.gui.objects.DisplayedResultQuery;
import annis.gui.objects.PagedResultQuery;
import annis.libgui.Helper;
import static annis.libgui.Helper.calculateMarkedAndCoveredIDs;
import annis.libgui.IDGenerator;
import annis.libgui.InstanceConfig;
import annis.libgui.PluginSystem;
import annis.libgui.ResolverProvider;
import static annis.model.AnnisConstants.ANNIS_NS;
import static annis.model.AnnisConstants.FEAT_RELANNIS_NODE;
import annis.model.RelannisNodeFeature;
import annis.resolver.ResolverEntry;
import annis.service.objects.Match;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SNode;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class SingleResultPanel extends CssLayout implements
  Button.ClickListener, VisualizerContextChanger
{
  private static final long serialVersionUID = 2L;

  private static final String INITIAL_OPEN = "initial_open";

  private static final Resource ICON_RESOURCE = FontAwesome.INFO_CIRCLE;

  private SDocument result;

  private Map<String, String> markedCoveredMap;

  private Map<String, String> markedExactMap;

  private final PluginSystem ps;
  
  private final AnnisUI ui;

  private List<VisualizerPanel> visualizers;
  private List<ResolverEntry> resolverEntries;

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

  private final Map<Long, Boolean> visualizerState;

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    SingleResultPanel.class);

  private final InstanceConfig instanceConfig;
  
  private PagedResultQuery query;
  private final Match match;

  public SingleResultPanel(final SDocument result, 
    Match match,
    long resultNumber,
    ResolverProvider resolverProvider, PluginSystem ps,
    AnnisUI ui,
    Set<String> visibleTokenAnnos, String segmentationName,
    QueryController controller, InstanceConfig instanceConfig,
    DisplayedResultQuery query)
  {
    this.ps = ps;
    this.ui = ui;
    this.result = result;
    this.segmentationName = segmentationName;
    this.queryController = controller;
    this.resultNumber = resultNumber;
    this.resolverProvider = resolverProvider;
    this.visibleTokenAnnos = visibleTokenAnnos;
    this.instanceConfig = instanceConfig;
    this.query = query;
    this.match = match;

    calculateHelperVariables();

    setWidth("100%");
    setHeight("-1px");

    if(query != null 
      && query.getSelectedMatches().contains(resultNumber))
    {
      addStyleName("selected-match");
    }
    
    infoBar = new HorizontalLayout();
    infoBar.addStyleName("info-bar");
    infoBar.setWidth("100%");
    infoBar.setHeight("-1px");

    Label lblNumber = new Label("" + (resultNumber + 1));
    infoBar.addComponent(lblNumber);
    lblNumber.setSizeUndefined();

    btInfo = new Button();
    btInfo.setStyleName(ValoTheme.BUTTON_BORDERLESS);
    btInfo.setIcon(ICON_RESOURCE);
    btInfo.setDescription("Show metadata");
    btInfo.addClickListener((Button.ClickListener) this);
    infoBar.addComponent(btInfo);
    
    btLink = new Button();
    btLink.setStyleName(ValoTheme.BUTTON_BORDERLESS);
    btLink.setIcon(FontAwesome.SHARE_ALT);
    btLink.setDescription("Share match reference");
    btLink.setDisableOnClick(true);
    btLink.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        showShareSingleMatchGenerator();
      }
    });
    infoBar.addComponent(btLink);

    /**
     * Extract the top level corpus name and the document name of this single
     * result.
     */
    path = CommonHelper.getCorpusPath(result.getGraph(), result);
    Collections.reverse(path);

    MinMax minMax = getIds(result.getDocumentGraph());

    // build label
    StringBuilder sb = new StringBuilder("Path: ");
    sb.append(StringUtils.join(path, " > "));
    sb.append(" (" + minMax.segName + " ").append(minMax.min);
    sb.append(" - ").append(minMax.max).append(")");

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

    for (int i = 0; i < 30; i += 5)
    {
      lftCtxContainer.addItem(i).getItemProperty("number").setValue(i);
      rghtCtxContainer.addItem(i).getItemProperty("number").setValue(i);
    }

    int lftContextIdx = query == null ? 0 : query.getLeftContext();
    lftCtxContainer.addItem(lftContextIdx);
    lftCtxContainer.sort(new Object[]
    {
      "number"
    }, new boolean[]
    {
      true
    });

    int rghtCtxIdx = query == null ? 0 : query.getRightContext();
    rghtCtxContainer.addItem(rghtCtxIdx);

    rghtCtxContainer.sort(new Object[]
    {
      "number"
    }, new boolean[]
    {
      true
    });

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

    lftCtxCombo.addValueChangeListener(
      new ContextChangeListener(resultNumber, true));
    rghtCtxCombo.addValueChangeListener(
      new ContextChangeListener(resultNumber, false));

    Label leftCtxLabel = new Label("left context: ");
    Label rightCtxLabel = new Label("right context: ");

    leftCtxLabel.setWidth("-1px");
    rightCtxLabel.setWidth("-1px");
    
    HorizontalLayout ctxLayout = new HorizontalLayout();
    ctxLayout.setSpacing(true);
    ctxLayout.addComponents(leftCtxLabel, lftCtxCombo, rightCtxLabel,
      rghtCtxCombo);
    infoBar.addComponent(ctxLayout);

    addComponent(infoBar);
    initVisualizer();
  }
  

  private void showShareSingleMatchGenerator()
  {
    // select the current match
    if(ui != null)
    {
      ui.getQueryState().getSelectedMatches().getValue().clear();
      ui.getQueryState().getSelectedMatches().getValue().add(resultNumber);
      ui.getSearchView().updateFragment(ui.getQueryController().getSearchQuery());
    }
    
    Window window = new ShareSingleMatchGenerator(resolverEntries, match, query, segmentationName, ps);
    window.setWidth(790, Unit.PIXELS);
    window.setHeight(580, Unit.PIXELS);
    window.setResizable(true);
    window.setModal(true);
    
    window.addCloseListener(new Window.CloseListener()
    {

      @Override
      public void windowClose(Window.CloseEvent e)
      {
        btLink.setEnabled(true);
      }
    });
    window.setCaption("Match reference link");
    
    UI.getCurrent().addWindow(window);
  }

  public void setSegmentationLayer(String segmentationName)
  {
    this.segmentationName = segmentationName;

    if (result != null)
    {
      List<SNode> segNodes = CommonHelper.getSortedSegmentationNodes(
        segmentationName,
        result.getDocumentGraph());
      Map<String, Long> markedAndCovered = calculateMarkedAndCoveredIDs(result, segNodes, segmentationName);
      for (VisualizerPanel p : visualizers)
      {
        p.setSegmentationLayer(segmentationName, markedAndCovered);
      }
    }
  }

  public void setVisibleTokenAnnosVisible(SortedSet<String> annos)
  {
    for (VisualizerPanel p : visualizers)
    {
      p.setVisibleTokenAnnosVisible(annos);
    }
  }

  private void calculateHelperVariables()
  {
    markedCoveredMap = new HashMap<>();
    
    markedExactMap = Helper.calculateColorsForMarkedExact(result);
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    if (event.getButton() == btInfo && result != null)
    {
      Window infoWindow = new Window("Info for " + result.getId());

      infoWindow.setModal(false);
      MetaDataPanel meta = new MetaDataPanel(path.get(0), path.get(path.size()
        - 1));
      infoWindow.setContent(meta);
      infoWindow.setWidth("400px");
      infoWindow.setHeight("400px");

      UI.getCurrent().addWindow(infoWindow);
    }
  }

  private void showReloadingProgress()
  {
    //remove the old visualizer
    for (VisualizerPanel v : visualizers)
    {
      this.removeComponent(v);
    }

    // first set loading indicator
    reloadVisualizer = new ProgressBar(1.0f);
    reloadVisualizer.setIndeterminate(true);
    reloadVisualizer.setSizeFull();
    reloadVisualizer.setHeight(150, Unit.PIXELS);
    addComponent(reloadVisualizer);
  }

  private void initVisualizer()
  {
    try
    {
      ResolverEntry[] entries 
        = resolverProvider == null ? new ResolverEntry[0] 
        : resolverProvider.getResolverEntries(result);
      visualizers = new LinkedList<>();
      resolverEntries = new LinkedList<>();
      
      List<VisualizerPanel> openVisualizers = new LinkedList<>();

      List<SNode> segNodes = CommonHelper.getSortedSegmentationNodes(
        segmentationName,
        result.getDocumentGraph());

      Map<String, Long> markedAndCovered = 
        Helper.calculateMarkedAndCoveredIDs(result, segNodes, segmentationName);
      Helper.calulcateColorsForMarkedAndCovered(result, markedAndCovered, markedCoveredMap);

      String resultID = "" + new Random().nextInt(Integer.MAX_VALUE);

      for (int i = 0; i < entries.length; i++)
      {
        String htmlID = "resolver-" + resultNumber + "_" + i;

        VisualizerPanel p = new VisualizerPanel(
          entries[i], result, match,
          visibleTokenAnnos, markedAndCovered,
          markedCoveredMap, markedExactMap,
          htmlID, resultID, this, segmentationName, ps, instanceConfig);

        visualizers.add(p);
        resolverEntries.add(entries[i]);
        
        Properties mappings = entries[i].getMappings();

        // check if there is the visibility of a visualizer changed
        // since it the whole result panel was loaded. If not the entry of the
        // resovler entry is used, for determine the visibility status
        if (visualizerState.containsKey(entries[i].getId()))
        {
          if (visualizerState.get(entries[i].getId()))
          {
            openVisualizers.add(p);
          }
        }
        else
        {
          if (Boolean.parseBoolean(mappings.getProperty(INITIAL_OPEN, "false")))
          {
            openVisualizers.add(p);
          }
        }
      } // for each resolver entry

      // attach visualizer
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

  @Override
  public void attach()
  {
    super.attach();
    if (Helper.isKickstarter(getSession()))
    {
      btLink.setVisible(false);
    }
    IDGenerator.assignIDForFields(SingleResultPanel.this, infoBar, btInfo);
  }
  
  

  @Override
  public void registerVisibilityStatus(long entryId, boolean status)
  {
    visualizerState.put(entryId, status);
  }

  @Override
  public void changeContext(long resultNumber, int context,
    boolean left)
  {
    //delegates the task to the query controller.
    
    queryController.changeContext(query, match, resultNumber, context, this, left);
  }

  private static class AddNewItemHandler implements AbstractSelect.NewItemHandler
  {

    final private ComboBox combobox;

    public AddNewItemHandler(ComboBox comboBox)
    {
      this.combobox = comboBox;
    }

    @Override
    public void addNewItem(String newValue)
    {

      String ERROR_MESSAGE_HEADER = "Illegal value";

      try
      {
        int i = Integer.parseInt(newValue);

        if (i < 0)
        {
          new Notification(ERROR_MESSAGE_HEADER,
            "<div><p>context &lt; 0 makes no sense</p></div>",
            Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
        }
        else
        {

          Item it = combobox.getContainerDataSource().addItem(i);
          // check if the item was actually added or might have been available before.
          if(it != null)
          {
            it.getItemProperty("number").setValue(i);

            if (combobox.getContainerDataSource() instanceof IndexedContainer)
            {
              ((IndexedContainer) combobox.getContainerDataSource()).sort(
                new Object[]
                {
                  "number"
                }, new boolean[]
                {
                  true
                });
            }

            combobox.select(i);
          }
        }
      }
      catch (NumberFormatException ex)
      {
        new Notification(ERROR_MESSAGE_HEADER,
          "<div><p>Only numbers are allowed.</p></div>",
          Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
      }
    }
  }

  private class ContextChangeListener implements
    Property.ValueChangeListener
  {

    long resultNumber;

    boolean left;


    public ContextChangeListener(long resultNumber, boolean left)
    {
      this.resultNumber = resultNumber;
      this.left = left;
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event)
    {
      showReloadingProgress();
      lftCtxCombo.setEnabled(false);
      rghtCtxCombo.setEnabled(false);
      int ctx = Integer.parseInt(event.getProperty().getValue().toString());
      changeContext(resultNumber, ctx, left);
    }
  }


  private static class MinMax
  {

    String segName = "tokens";

    long min;

    long max;

  }

  private MinMax getIds(SDocumentGraph graph)
  {
    List<SToken> sTokens = graph.getTokens();

    MinMax minMax = new MinMax();
    minMax.min = Long.MAX_VALUE;
    minMax.max = Long.MIN_VALUE;

    if (segmentationName == null)
    {
      minMax.segName = "tokens";

      if (sTokens != null)
      {
        for (SToken t : sTokens)
        {
          SFeature feature = t.getFeature(ANNIS_NS,
            FEAT_RELANNIS_NODE);
          if(feature != null && feature.getValue() instanceof RelannisNodeFeature)
          {
            RelannisNodeFeature f = (RelannisNodeFeature) feature.getValue();

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
      }
    }
    else
    {
      minMax.segName = segmentationName;

      List<SNode> nodes = CommonHelper.getSortedSegmentationNodes(
        segmentationName, graph);

      for (SNode n : nodes)
      {
        RelannisNodeFeature f = RelannisNodeFeature.extract(n);

        if (minMax.min > f.getSegIndex())
        {
          minMax.min = f.getSegIndex();
        }

        if (minMax.max < f.getSegIndex())
        {
          minMax.max = f.getSegIndex();
        }
      }
    }

    minMax.min++;
    minMax.max++;
    
    return minMax;
  }

  @Override
  public void updateResult(SaltProject p, PagedResultQuery query)
  {
    this.query = query;
    if (p != null
      && p.getCorpusGraphs() != null
      && !p.getCorpusGraphs().isEmpty()
      && p.getCorpusGraphs().get(0) != null
      && p.getCorpusGraphs().get(0).getDocuments() != null
      && !p.getCorpusGraphs().get(0).getDocuments().isEmpty())
    {
      this.result = p.getCorpusGraphs().get(0).getDocuments().get(0);
    }

    removeComponent(reloadVisualizer);
    initVisualizer();

    lftCtxCombo.setEnabled(true);
    rghtCtxCombo.setEnabled(true);
  }

  protected SDocument getResult()
  {
    return result;
  }
}
