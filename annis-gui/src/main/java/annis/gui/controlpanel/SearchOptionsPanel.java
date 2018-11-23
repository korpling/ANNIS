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
package annis.gui.controlpanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;

import annis.gui.AnnisUI;
import annis.gui.components.HelpButton;
import annis.gui.objects.QueryUIState;
import annis.libgui.Background;
import annis.libgui.Helper;
import annis.service.objects.CorpusConfig;
import annis.service.objects.CorpusConfigMap;
import annis.service.objects.OrderType;
import annis.service.objects.SegmentationList;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class SearchOptionsPanel extends FormLayout
{

  public static final String NULL_SEGMENTATION_VALUE = "tokens (default)";

  public static final String KEY_DEFAULT_CONTEXT_SEGMENTATION = "default-context-segmentation";

  public static final String KEY_DEFAULT_BASE_TEXT_SEGMENTATION = "default-base-text-segmentation";

  public static final String KEY_MAX_CONTEXT_LEFT = "max-context-left";

  public static final String KEY_MAX_CONTEXT_RIGHT = "max-context-right";

  public static final String KEY_CONTEXT_STEPS = "context-steps";

  public static final String KEY_DEFAULT_CONTEXT = "default-context";

  public static final String KEY_RESULT_PER_PAGE = "results-per-page";

  public static final String DEFAULT_CONFIG = "default-config";
  
  public static final int DEFAULT_CONTEXT = 5;
  public static final int DEFAULT_CONTEXT_STEPS = 5;
  public static final int DEFAULT_MAX_CONTEXT = 20;

  private static final Logger log = LoggerFactory.getLogger(
    SearchOptionsPanel.class);

  private final ComboBox cbLeftContext;

  private final ComboBox cbRightContext;

  private final ComboBox cbResultsPerPage;

  private final ComboBox cbSegmentation;
  private final HelpButton segmentationHelp;

  private final ComboBox cbOrder;

  // TODO: make this configurable
  private static final List<Integer> PREDEFINED_PAGE_SIZES = ImmutableList.of(
    1, 2, 5, 10, 20, 25
  );

  public static final List<Integer> PREDEFINED_CONTEXTS = ImmutableList.of(
    0, 1, 2, 5, 10, 20
  );

  
  private final ProgressBar pbLoadConfig;
  
  
  private final BeanItemContainer<OrderType> orderContainer
    = new BeanItemContainer<>(OrderType.class,
      Lists.newArrayList(OrderType.values()));
  private final IndexedContainer contextContainerLeft = new IndexedContainer();
  private final IndexedContainer contextContainerRight = new IndexedContainer();
  private final IndexedContainer segmentationContainer = new IndexedContainer();
  private final IndexedContainer resultsPerPageContainer = new IndexedContainer();
 
  private final AtomicInteger maxLeftContext = new AtomicInteger(Integer.MAX_VALUE);
  private final AtomicInteger maxRightContext = new AtomicInteger(Integer.MAX_VALUE);
  
  private boolean updateStateFromConfig = true;
  
  private QueryUIState state;
  
  public SearchOptionsPanel()
  {
    setWidth("100%");
    setHeight("-1px");
        
    pbLoadConfig = new ProgressBar();
    pbLoadConfig.setIndeterminate(true);
    pbLoadConfig.setCaption("Loading search options...");
    addComponent(pbLoadConfig);
    
    cbLeftContext = new ComboBox("Left Context", contextContainerLeft);
    cbRightContext = new ComboBox("Right Context", contextContainerRight);
    cbResultsPerPage = new ComboBox("Results Per Page", resultsPerPageContainer);

    cbLeftContext.setNullSelectionAllowed(false);
    cbRightContext.setNullSelectionAllowed(false);
    cbResultsPerPage.setNullSelectionAllowed(false);

    cbLeftContext.setNewItemsAllowed(true);
    cbRightContext.setNewItemsAllowed(true);
    cbResultsPerPage.setNewItemsAllowed(true);

    cbLeftContext.setTextInputAllowed(true);
    cbRightContext.setTextInputAllowed(true);
    cbResultsPerPage.setTextInputAllowed(true);

    cbLeftContext.setImmediate(true);
    cbRightContext.setImmediate(true);
    cbResultsPerPage.setImmediate(true);
    
    cbSegmentation = new ComboBox("Show context in", segmentationContainer);

    cbSegmentation.setTextInputAllowed(false);
    cbSegmentation.setNullSelectionAllowed(true);
    cbSegmentation.setNewItemsAllowed(true);
    cbSegmentation.setNullSelectionItemId(NULL_SEGMENTATION_VALUE);
    cbSegmentation.addItem(NULL_SEGMENTATION_VALUE);

    cbSegmentation.setDescription(
      "If corpora with multiple "
      + "context definitions are selected, a list of available context units will be "
      + "displayed. By default context is calculated in ‘tokens’ "
      + "(e.g. 5 minimal units to the left and right of a search result). "
      + "Some corpora might offer further context definitions, e.g. in "
      + "syllables, word forms belonging to different speakers, normalized or "
      + "diplomatic segmentations of a manuscript, etc.");
    
    segmentationHelp = new HelpButton<Object>(cbSegmentation);

    cbOrder = new ComboBox("Order", orderContainer);
    cbOrder.setNewItemsAllowed(false);
    cbOrder.setNullSelectionAllowed(false);
    cbOrder.setImmediate(true);

    cbLeftContext.setVisible(false);
    cbRightContext.setVisible(false);
    cbResultsPerPage.setVisible(false);
    cbOrder.setVisible(false);
    segmentationHelp.setVisible(false);
    
    
    
    addComponent(cbLeftContext);
    addComponent(cbRightContext);
    
    addComponent(segmentationHelp);
    addComponent(cbResultsPerPage);
    addComponent(cbOrder);

  }

  @Override
  public void attach()
  {
    super.attach();
    
    contextContainerLeft.setItemSorter(new IntegerIDSorter());
    contextContainerRight.setItemSorter(new IntegerIDSorter());
    resultsPerPageContainer.setItemSorter(new IntegerIDSorter());
    
    resultsPerPageContainer.removeAllItems();
    for(Integer i :PREDEFINED_PAGE_SIZES)
    {
      resultsPerPageContainer.addItem(i);
    }

    if (getUI() instanceof AnnisUI)
    {
      AnnisUI ui = (AnnisUI) getUI();
      
      state = ui.getQueryState();
      
      Background.run(new CorpusConfigUpdater(ui, state.getSelectedCorpora().getValue(), false));      

      cbLeftContext.setNewItemHandler(new CustomContext(maxLeftContext, contextContainerLeft,
        state.getLeftContext()));
      cbRightContext.setNewItemHandler(new CustomContext(maxRightContext, contextContainerRight,
        state.getRightContext()));
      cbResultsPerPage.setNewItemHandler(new CustomResultSize(resultsPerPageContainer, state.getLimit()));

      
      cbLeftContext.setPropertyDataSource(state.getLeftContext());
      cbRightContext.setPropertyDataSource(state.getRightContext());
      cbResultsPerPage.setPropertyDataSource(state.getLimit());
      cbSegmentation.setPropertyDataSource(state.getContextSegmentation());
      
      
      orderContainer.removeAllItems();
      for(OrderType t : OrderType.values())
      {
        orderContainer.addItem(t);
      }
      cbOrder.setPropertyDataSource(state.getOrder());
      
    }
  }

  public void updateSearchPanelConfigurationInBackground(
    final Set<String> corpora, final AnnisUI ui)
  {
    setLoadingState(true);
    // remove custom adjustments
    contextContainerLeft.removeAllItems();
    contextContainerRight.removeAllItems();
    cbSegmentation.removeAllItems();
    
    
    // reload the config in the background
    Background.run(new CorpusConfigUpdater(ui, corpora, true));
  }
  
  private static Integer getInteger(String key, CorpusConfig config)
  {
    String s = config.getConfig(key);
    if(s != null)
    {
      return Integer.parseInt(s);
    }
    return null;
  }

  private static List<String> getSegmentationNamesFromService(
    Set<String> corpora)
  {
    List<String> segNames = new ArrayList<>();
    WebResource service = Helper.getAnnisWebResource();
    if (service != null)
    {
      for (String corpus : corpora)
      {
        try
        {
          SegmentationList segList
            = service.path("query").path("corpora").path(Helper.encodeJersey(
                corpus))
            .path("segmentation-names")
            .get(SegmentationList.class);
          segNames.addAll(segList.getSegmentatioNames());
        }
        catch (UniformInterfaceException ex)
        {
          if (ex.getResponse().getStatus() == 403)
          {
            log.debug(
              "Did not have access rights to query segmentation names for corpus",
              ex);
          }
          else
          {
            log.warn("Could not query segmentation names for corpus", ex);
          }
        }
      }

    }

    return segNames;
  }

  private void updateSegmentations(String segment,
    List<String> segNames, boolean updateValue)
  {
    
    cbSegmentation.setNullSelectionItemId(NULL_SEGMENTATION_VALUE);
    cbSegmentation.addItem(NULL_SEGMENTATION_VALUE);

    if ("tok".equalsIgnoreCase(segment))
    {
      if(state != null && updateValue)
      {
        state.getContextSegmentation().setValue(null);
      }
    }
    else if (segment != null)
    {
      cbSegmentation.addItem(segment);
      if(state != null && updateValue)
      {
        cbSegmentation.setValue(segment);
      }
    }

    if (segNames != null && !segNames.isEmpty())
    {
      for (String s : segNames)
      {
        if (!s.equalsIgnoreCase(segment))
        {
          cbSegmentation.addItem(s);
        }
      }
    }
  }

  /**
   * If all values of a specific corpus property have the same value, this value
   * is returned, otherwise the value of the default configuration is choosen.
   *
   * @param key The property key.
   * @param corpora Specifies the selected corpora.
   * @return A value defined in the copurs.properties file or in the
   * admin-service.properties
   */
  private String mergeConfigValue(String key, Set<String> corpora,
    CorpusConfigMap corpusConfigurations)
  {
    Set<String> values = new TreeSet<>();
    for(String corpus : corpora)
    {
      CorpusConfig config = corpusConfigurations.get(corpus);
      if(config != null)
      {
        String v = config.getConfig(key);
        if(v != null)
        {
          values.add(v);
        }
      }
    }
    if(values.size() > 1 || values.isEmpty())
    {
      // fallback to the default values
      CorpusConfig defaultConfig = corpusConfigurations.get(DEFAULT_CONFIG);
      if(defaultConfig != null && defaultConfig.containsKey(key))
      {
        return defaultConfig.getConfig(key);
      }
    }    

      // ok, just return the first value as a fallback of the fallback
    if(!values.isEmpty())
    {
      return values.iterator().next();
    }

    return null;
  }

  /**
   * Builds a single config for selection of one or muliple corpora.
   *
   * @param corpora Specifies the combination of corpora, for which the config
   * is calculated.
   * @param corpusConfigurations  A map containg the known corpus configurations.
   * @return A new config which takes into account the segementation of all
   * selected corpora.
   */
  private CorpusConfig mergeConfigs(Set<String> corpora, 
    CorpusConfigMap corpusConfigurations)
  {
    CorpusConfig corpusConfig = new CorpusConfig();

    // calculate the left and right context.
    String leftCtx = mergeConfigValue(KEY_MAX_CONTEXT_LEFT, corpora,
      corpusConfigurations);
    String rightCtx = mergeConfigValue(KEY_MAX_CONTEXT_RIGHT,
      corpora, corpusConfigurations);
    corpusConfig.setConfig(KEY_MAX_CONTEXT_LEFT, leftCtx);
    corpusConfig.setConfig(KEY_MAX_CONTEXT_RIGHT, rightCtx);

    // calculate the default-context
    corpusConfig.setConfig(KEY_CONTEXT_STEPS, mergeConfigValue(
      KEY_CONTEXT_STEPS, corpora, corpusConfigurations));
    corpusConfig.setConfig(KEY_DEFAULT_CONTEXT, mergeConfigValue(
      KEY_DEFAULT_CONTEXT, corpora, corpusConfigurations));

    // get the results per page
    corpusConfig.setConfig(KEY_RESULT_PER_PAGE, mergeConfigValue(
      KEY_RESULT_PER_PAGE, corpora, corpusConfigurations));

    corpusConfig.setConfig(KEY_DEFAULT_CONTEXT_SEGMENTATION, checkSegments(
      KEY_DEFAULT_CONTEXT_SEGMENTATION, corpora, corpusConfigurations));

    corpusConfig.setConfig(KEY_DEFAULT_BASE_TEXT_SEGMENTATION, checkSegments(
      KEY_DEFAULT_BASE_TEXT_SEGMENTATION, corpora, corpusConfigurations));

    return corpusConfig;
  }

  /**
   * Checks, if all selected corpora have the same default segmentation layer.
   * If not the tok layer is taken, because every corpus has this one.
   *
   * @param key the key for the segementation config, must be
   * {@link #KEY_DEFAULT_BASE_TEXT_SEGMENTATION} or
   * {@link #KEY_DEFAULT_CONTEXT_SEGMENTATION}.
   * @param corpora the corpora which has to be checked.
   * @return "tok" or a segment which is defined in all corpora.
   */
  private String checkSegments(String key, Set<String> corpora, CorpusConfigMap corpusConfigurations)
  {
    String segmentation = null;
    for (String corpus : corpora)
    {

      CorpusConfig c = null;

      if (corpusConfigurations.containsConfig(corpus))
      {
        c = corpusConfigurations.get(corpus);
      }
      else
      {
        c = corpusConfigurations.get(DEFAULT_CONFIG);
      }

      // do nothing if not even default config is set
      if (c == null)
      {
        continue;
      }

      String tmpSegment = c.getConfig(key);

      /**
       * If no segment is set in the corpus config use always the tok segment.
       */
      if (tmpSegment == null)
      {
        return corpusConfigurations.get(DEFAULT_CONFIG).getConfig(key);
      }

      if (segmentation == null)
      {
        segmentation = tmpSegment;
        continue;
      }

      if (!segmentation.equals(tmpSegment)) // return the default config
      {
        return corpusConfigurations.get(DEFAULT_CONFIG).getConfig(key);
      }
    }

    if (segmentation == null)
    {
      return corpusConfigurations.get(DEFAULT_CONFIG).getConfig(key);
    }
    else
    {
      return segmentation;
    }
  }


  /**
   * Updates context combo boxes.
   *
   * @param c the container, which is updated.
   * @param maxCtx the larges context values until context steps are calculated.
   * @param ctxSteps the step range.
   * @param keepCustomValues If this is true all custom values are kept.
   */
  private void updateContext(Container c ,int maxCtx, int ctxSteps, boolean keepCustomValues)
  {

    if(!keepCustomValues)
    {
      c.removeAllItems();
    }
    
    for (Integer i : PREDEFINED_CONTEXTS)
    {
      if (i < maxCtx)
      {
        c.addItem(i);
      }
    }

    for (int step = ctxSteps; step < maxCtx; step += ctxSteps)
    {
      c.addItem(step);
    }

    c.addItem(maxCtx);

  }
  
  private void setLoadingState(boolean isLoading)
  {
    pbLoadConfig.setVisible(isLoading);
          
    cbLeftContext.setVisible(!isLoading);
    cbRightContext.setVisible(!isLoading);
    cbResultsPerPage.setVisible(!isLoading);
    cbOrder.setVisible(!isLoading);
    segmentationHelp.setVisible(!isLoading);
  }

  public boolean isUpdateStateFromConfig()
  {
    return updateStateFromConfig;
  }

  public void setUpdateStateFromConfig(boolean updateStateFromConfig)
  {
    this.updateStateFromConfig = updateStateFromConfig;
  }
  
  

  private static class CustomResultSize implements AbstractSelect.NewItemHandler
  {
    private final IndexedContainer container;
    private final Property<Integer> prop;
    
    public CustomResultSize(IndexedContainer container, Property<Integer> prop)
    {
      this.container = container;
      this.prop = prop;
    }

    @Override
    public void addNewItem(String resultPerPage)
    {
      try
      {
        int i = Integer.parseInt((String) resultPerPage);

        if (i < 1)
        {
          throw new IllegalArgumentException(
            "result number has to be a positive number greater or equal than 1");
        }
        container.addItem(i);
        container.sort(null, null);
        prop.setValue(i);
      }
      catch (NumberFormatException ex)
      {
        Notification.show("invalid result per page input",
          "Please enter valid numbers [0-9]",
          Notification.Type.WARNING_MESSAGE);
      }
      catch (IllegalArgumentException ex)
      {
        Notification.show("invalid result per page input",
          ex.getMessage(), Notification.Type.WARNING_MESSAGE);
      }

    }
  }

  private class CorpusConfigUpdater implements Runnable
  {
    
    private final AnnisUI ui;
    private final Set<String> corpora;
    private final QueryUIState state;
    private final boolean corpusSelectionChanged;
    
    public CorpusConfigUpdater(AnnisUI ui, Set<String> corpora, boolean corpusSelectionChanged)
    {
      this.ui = ui;
      this.state = ui.getQueryState();
      this.corpora = corpora;
      this.corpusSelectionChanged = corpusSelectionChanged;
    }

    @Override
    public void run()
    {
      final List<String> segmentations = getSegmentationNamesFromService(corpora);
      
      final Set<String> corporaWithDefault = new TreeSet<>(corpora);
      corporaWithDefault.add(DEFAULT_CONFIG);

      final CorpusConfigMap corpusConfigs = new CorpusConfigMap();
      for(String c : corporaWithDefault)
      {
        corpusConfigs.put(c, ui.getCorpusConfigWithCache(c));
      }
      
      // if there are not any defaults create them
      if (!corpusConfigs.containsConfig(DEFAULT_CONFIG))
      {
        CorpusConfig defaultConfig = new CorpusConfig();
        defaultConfig.setConfig(KEY_MAX_CONTEXT_LEFT, "" + DEFAULT_MAX_CONTEXT);
        defaultConfig.setConfig(KEY_MAX_CONTEXT_RIGHT, "" + DEFAULT_MAX_CONTEXT);
        defaultConfig.setConfig(KEY_CONTEXT_STEPS, "" + DEFAULT_CONTEXT_STEPS);
        defaultConfig.setConfig(KEY_RESULT_PER_PAGE, "10");
        defaultConfig.setConfig(KEY_DEFAULT_CONTEXT, "" + DEFAULT_CONTEXT);
        defaultConfig.setConfig(KEY_DEFAULT_CONTEXT_SEGMENTATION, "tok");
        defaultConfig.setConfig(KEY_DEFAULT_BASE_TEXT_SEGMENTATION, "tok");
        corpusConfigs.put(DEFAULT_CONFIG, defaultConfig);
      }

      // update GUI
      ui.access(new Runnable()
      {

        @Override
        public void run()
        {
          setLoadingState(false);
          
          CorpusConfig c = mergeConfigs(corpora, corpusConfigs);
          
          Integer resultsPerPage = getInteger(KEY_RESULT_PER_PAGE, c);
          Integer leftCtx = getInteger(KEY_MAX_CONTEXT_LEFT, c);
          if(leftCtx != null)
          {
            maxLeftContext.set(leftCtx);
          }
          
          Integer rightCtx = getInteger(KEY_MAX_CONTEXT_RIGHT, c);
          if(rightCtx != null)
          {
            maxRightContext.set(rightCtx);
          }
          Integer defaultCtx = getInteger(KEY_DEFAULT_CONTEXT, c);

          Integer ctxSteps = getInteger(KEY_CONTEXT_STEPS, c);
          String segment = c.getConfig(KEY_DEFAULT_CONTEXT_SEGMENTATION);
          
          updateContext(contextContainerLeft, leftCtx == null ? DEFAULT_CONTEXT : leftCtx, 
            ctxSteps == null ? DEFAULT_CONTEXT_STEPS : ctxSteps, true);
          updateContext(contextContainerRight, rightCtx == null ? DEFAULT_CONTEXT : rightCtx, 
            ctxSteps == null ? DEFAULT_CONTEXT_STEPS : ctxSteps, true);
          if (defaultCtx != null && updateStateFromConfig && corpusSelectionChanged)
          {
            state.getLeftContext().setValue(defaultCtx);
            state.getRightContext().setValue(defaultCtx);
          }
          updateSegmentations(segment, segmentations, updateStateFromConfig && !corpora.isEmpty());
          if(resultsPerPage != null && updateStateFromConfig && corpusSelectionChanged)
          {
            state.getLimit().setValue(resultsPerPage);
          }
          // reset if corpus selection has changed
          if(corpusSelectionChanged)
          {
            updateStateFromConfig = true;
          }
        }
        
      });
    }

  }

  private static class CustomContext implements AbstractSelect.NewItemHandler
  {
    private final AtomicInteger maxCtx;
    private final IndexedContainer container;
    private final Property<Integer> prop;
    
    public CustomContext(AtomicInteger maxCtx,IndexedContainer container,
      Property<Integer> prop)
    {
      this.maxCtx = maxCtx;
      this.container = container;
      this.prop = prop;
    }

    @Override
    public void addNewItem(String context)
    {
      try
      {
        int i = Integer.parseInt((String) context);

        if (i < 0)
        {
          throw new IllegalArgumentException(
            "context has to be a positive number or 0");
        }

        if (i > maxCtx.get())
        {
          throw new IllegalArgumentException(
            "The context is greater than, than the max value defined in the corpus property file.");
        }
        
        // everything ok, add the value
        container.addItem(i);
        container.sort(null, null);
        prop.setValue(i);
      }
      catch (NumberFormatException ex)
      {
        Notification.show("invalid context input",
          "Please enter valid numbers [0-9]",
          Notification.Type.WARNING_MESSAGE);
      }
      catch (IllegalArgumentException ex)
      {
        Notification.show("invalid context input",
          ex.getMessage(),
          Notification.Type.WARNING_MESSAGE);
      }

    }
  }
  
  public static class IntegerIDSorter implements ItemSorter
  {

    @Override
    public void setSortProperties(Container.Sortable container,
      Object[] propertyId, boolean[] ascending)
    {
      // does nothing
    }

    @Override
    public int compare(Object itemId1, Object itemId2)
    { 
      if(itemId1 instanceof Integer && itemId2 instanceof Integer)
      {
        return Integer.compare((Integer) itemId1, (Integer) itemId2);
      }
      return 0;
    }
    
  }
  
}
