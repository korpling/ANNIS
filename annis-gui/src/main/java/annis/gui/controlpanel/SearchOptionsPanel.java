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
package annis.gui.controlpanel;

import annis.gui.AnnisUI;
import annis.gui.objects.QueryUIState;
import annis.libgui.Background;
import annis.libgui.Helper;
import annis.service.objects.CorpusConfigMap;
import com.google.common.collect.ImmutableList;
import com.vaadin.data.Binder;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComboBox.NewItemProvider;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.model.AnnotationComponentType;
import org.corpus_tools.annis.api.model.Component;
import org.corpus_tools.annis.api.model.CorpusConfiguration;
import org.corpus_tools.annis.api.model.FindQuery;
import org.corpus_tools.annis.api.model.FindQuery.OrderEnum;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public class SearchOptionsPanel extends FormLayout {

  private class CorpusConfigUpdater implements Runnable {

    private final AnnisUI ui;
    private final Collection<String> corpora;
    private final boolean corpusSelectionChanged;

    public CorpusConfigUpdater(AnnisUI ui, Collection<String> corpora,
        boolean corpusSelectionChanged) {
      this.ui = ui;
      this.corpora = corpora;
      this.corpusSelectionChanged = corpusSelectionChanged;
    }

    @Override
    public void run() {
      final List<String> segmentations = getSegmentationNamesFromService(corpora, ui);

      final Set<String> corporaWithDefault = new TreeSet<>(corpora);
      corporaWithDefault.add(DEFAULT_CONFIG);

      final CorpusConfigMap corpusConfigs = new CorpusConfigMap();
      for (String c : corporaWithDefault) {
        corpusConfigs.put(c, ui.getCorpusConfigWithCache(c));
      }

      // if there are not any defaults create them
      if (!corpusConfigs.containsConfig(DEFAULT_CONFIG)) {
        corpusConfigs.put(DEFAULT_CONFIG, Helper.getDefaultCorpusConfig());
      }

      // update GUI
      ui.access(() -> {
        setLoadingState(false);

        CorpusConfiguration c = mergeConfigs(corpora, corpusConfigs);

        if (c.getContext().getMax() == null) {
          maxContext.set(Integer.MAX_VALUE);
        } else {
          maxContext.set(c.getContext().getMax());
        }

        cbLeftContext.setItems(c.getContext().getSizes());
        cbRightContext.setItems(c.getContext().getSizes());
        cbSegmentation.setItems(segmentations);
        cbLeftContext.setValue(c.getContext().getDefault());
        cbRightContext.setValue(c.getContext().getDefault());
        cbSegmentation.setValue(c.getContext().getSegmentation());
        cbResultsPerPage.setValue(c.getView().getPageSize());


        // reset if corpus selection has changed
        if (corpusSelectionChanged) {
          updateStateFromConfig = true;
        }
      });
    }

  }

  /**
   * 
   */
  private static final long serialVersionUID = 7878445496945702778L;

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

  private static final Logger log = LoggerFactory.getLogger(SearchOptionsPanel.class);
  // TODO: make this configurable
  private static final List<Integer> PREDEFINED_PAGE_SIZES = ImmutableList.of(1, 2, 5, 10, 20, 25);

  public static final List<Integer> PREDEFINED_CONTEXTS = ImmutableList.of(0, 1, 2, 5, 10, 20);


  private static List<String> getSegmentationNamesFromService(Collection<String> corpora, UI ui) {
    List<String> segNames = new ArrayList<>();
    CorporaApi corporaApi = new CorporaApi(Helper.getClient(ui));
    for (String corpus : corpora) {
      try {
        // Get all ordering components
        for(Component c : corporaApi.components(corpus, AnnotationComponentType.ORDERING.getValue(), null)) {
          if (!c.getName().isEmpty() && !"annis".equals(c.getLayer())) {
            segNames.add(c.getName());
          }
        }
      } catch (ApiException ex) {
        if (ex.getCode() == 403) {
          log.debug("Did not have access rights to query segmentation names for corpus", ex);
        } else {
          log.warn("Could not query segmentation names for corpus", ex);
        }
      }
    }



    return segNames;
  }

  private final com.vaadin.ui.ComboBox<Integer> cbLeftContext;

  private final com.vaadin.ui.ComboBox<Integer> cbRightContext;

  private final com.vaadin.ui.ComboBox<Integer> cbResultsPerPage;
  private final com.vaadin.ui.ComboBox<String> cbSegmentation;
  private final com.vaadin.ui.ComboBox<OrderEnum> cbOrder;
  private final com.vaadin.ui.ComboBox<QueryLanguage> cbQueryLanguage;
  private final ProgressBar pbLoadConfig;


  private final AtomicInteger maxContext = new AtomicInteger(Integer.MAX_VALUE);


  private boolean updateStateFromConfig = true;

  private QueryUIState state;

  public SearchOptionsPanel() {
    setWidth("100%");
    setHeight("-1px");

    pbLoadConfig = new ProgressBar();
    pbLoadConfig.setIndeterminate(true);
    pbLoadConfig.setCaption("Loading search options...");
    addComponent(pbLoadConfig);

    cbLeftContext = new ComboBox<>("Left Context");
    cbRightContext = new ComboBox<>("Right Context");
    cbResultsPerPage = new ComboBox<>("Results Per Page");

    cbLeftContext.setEmptySelectionAllowed(false);
    cbRightContext.setEmptySelectionAllowed(false);
    cbResultsPerPage.setEmptySelectionAllowed(false);

    NewItemProvider<Integer> contextNewItemProvider = v -> {

      try {
        int numericValue = Integer.parseInt(v);
        if (numericValue >= 0 && numericValue < maxContext.get()) {
          return Optional.of(numericValue);
        }
      } catch (NumberFormatException ex) {

      }

      return Optional.empty();
    };

    cbLeftContext.setNewItemProvider(contextNewItemProvider);
    cbRightContext.setNewItemProvider(contextNewItemProvider);
    cbResultsPerPage.setNewItemProvider(v -> {
      try {
        return Optional.of(Integer.parseInt(v));
      } catch (NumberFormatException ex) {
      }
      return Optional.empty();
    });

    cbLeftContext.setTextInputAllowed(true);
    cbRightContext.setTextInputAllowed(true);
    cbResultsPerPage.setTextInputAllowed(true);


    cbSegmentation = new ComboBox<>("Show context in");

    cbSegmentation.setTextInputAllowed(false);
    cbSegmentation.setEmptySelectionAllowed(true);
    cbSegmentation.setNewItemProvider(v -> Optional.of(v));
    cbSegmentation.setEmptySelectionCaption(NULL_SEGMENTATION_VALUE);

    cbSegmentation.setDescription("If corpora with multiple "
        + "context definitions are selected, a list of available context units will be "
        + "displayed. By default context is calculated in ‘tokens’ "
        + "(e.g. 5 minimal units to the left and right of a search result). "
        + "Some corpora might offer further context definitions, e.g. in "
        + "syllables, word forms belonging to different speakers, normalized or "
        + "diplomatic segmentations of a manuscript, etc.");

    cbOrder =
        new com.vaadin.ui.ComboBox<FindQuery.OrderEnum>("Order", Arrays.asList(OrderEnum.values()));
    cbOrder.setEmptySelectionAllowed(false);
    cbOrder.setItems(OrderEnum.values());
    cbOrder.setValue(OrderEnum.NORMAL);

    cbQueryLanguage = new com.vaadin.ui.ComboBox<>("Query Language");

    Binder<QueryUIState> binder = new Binder<>();
    binder.forField(cbQueryLanguage).bind(QueryUIState::getQueryLanguage,
        QueryUIState::setQueryLanguage);
    binder.readBean(state);
    cbQueryLanguage.setItemCaptionGenerator(ql -> {
      switch (ql) {
        case AQL:
          return "AQL (latest)";
        case AQLQUIRKSV3:
          return "AQL (compatibility mode)";
      }
      return "";
    });
    cbQueryLanguage.setItems(QueryLanguage.values());
    cbQueryLanguage.setValue(QueryLanguage.AQL);
    cbQueryLanguage.setEmptySelectionAllowed(false);

    cbLeftContext.setVisible(false);
    cbRightContext.setVisible(false);
    cbResultsPerPage.setVisible(false);
    cbOrder.setVisible(false);

    addComponent(cbLeftContext);
    addComponent(cbRightContext);
    addComponent(cbSegmentation);

    addComponent(cbResultsPerPage);
    addComponent(cbOrder);
    addComponent(cbQueryLanguage);

  }

  @Override
  public void attach() {
    super.attach();

    cbResultsPerPage.setItems(PREDEFINED_PAGE_SIZES);
    cbLeftContext.setItems(PREDEFINED_CONTEXTS);
    cbRightContext.setItems(PREDEFINED_CONTEXTS);


    if (getUI() instanceof AnnisUI) {
      AnnisUI ui = (AnnisUI) getUI();
      state = ui.getQueryState();

      // Bind to UI state
      Binder<QueryUIState> binder = new Binder<>(QueryUIState.class);
      binder.setBean(state);

      binder.forField(cbLeftContext).bind("leftContext");
      binder.forField(cbRightContext).bind("rightContext");
      binder.forField(cbSegmentation).bind("contextSegmentation");
      binder.forField(cbResultsPerPage).bind("limit");
      binder.forField(cbOrder).bind("order");


      Background
          .run(new CorpusConfigUpdater(ui, new LinkedHashSet<>(state.getSelectedCorpora()), false));
    }
  }



  public boolean isUpdateStateFromConfig() {
    return updateStateFromConfig;
  }

  /**
   * Builds a single config for selection of one or muliple corpora.
   *
   * @param corpora Specifies the combination of corpora, for which the config is calculated.
   * @param corpusConfigurations A map containg the known corpus configurations.
   * @return A new config which takes into account the segementation of all selected corpora.
   */
  private CorpusConfiguration mergeConfigs(Collection<String> corpora,
      CorpusConfigMap corpusConfigurations) {

    List<CorpusConfiguration> selectedConfigs =
        corpora.stream().map(c -> corpusConfigurations.get(c)).filter(config -> config != null)
            .collect(Collectors.toList());

    if (selectedConfigs.size() == 1) {
      // Directly return the single corpus configuration
      return selectedConfigs.get(0);
    }

    // Merge the configurations into one
    CorpusConfiguration corpusConfig = Helper.getDefaultCorpusConfig();

    // Calculate merged context
    Optional<Integer> mergedDefaultCtx = selectedConfigs.stream()
        .map(config -> config.getContext().getDefault()).min(Comparator.naturalOrder());
    if (mergedDefaultCtx.isPresent()) {
      corpusConfig.getContext().setDefault(mergedDefaultCtx.get());
    }

    Optional<Integer> mergedMaxCtx = selectedConfigs.stream()
        .map(config -> config.getContext().getMax()).filter(maxCtx -> maxCtx != null)
        .min(Comparator.naturalOrder());
    if (mergedMaxCtx.isPresent()) {
      corpusConfig.getContext().setMax(mergedMaxCtx.get());
    }

    // Add all context steps and sort them
    TreeSet<Integer> contextSizes = new TreeSet<>();
    for (CorpusConfiguration config : selectedConfigs) {
      contextSizes.addAll(config.getContext().getSizes());
    }
    if (!contextSizes.isEmpty()) {
      corpusConfig.getContext().setSizes(new ArrayList<>(contextSizes));
    }

    // merge the results per page
    Optional<Integer> mergedPageSize = selectedConfigs.stream()
        .map(config -> config.getView().getPageSize()).min(Comparator.naturalOrder());
    if (mergedPageSize.isPresent()) {
      corpusConfig.getView().setPageSize(mergedPageSize.get());
    }

    // Merge both segmentation settings if they are the same, otherwise use default tokens
    // It is not guaranteed that the different corpora have the same segmentation layers, but all
    // corpora have token
    Set<String> allContextSegmentations = selectedConfigs.stream()
        .map(config -> config.getContext().getSegmentation()).collect(Collectors.toSet());
    if (allContextSegmentations.size() == 1) {
      corpusConfig.getContext().setSegmentation(allContextSegmentations.iterator().next());
    }

    Set<String> allBaseTextSegmentations = selectedConfigs.stream()
        .map(config -> config.getView().getBaseTextSegmentation()).collect(Collectors.toSet());
    if (allBaseTextSegmentations.size() == 1) {
      corpusConfig.getView().setBaseTextSegmentation(allBaseTextSegmentations.iterator().next());
    }
    return corpusConfig;
  }


  private void setLoadingState(boolean isLoading) {
    pbLoadConfig.setVisible(isLoading);

    cbLeftContext.setVisible(!isLoading);
    cbRightContext.setVisible(!isLoading);
    cbResultsPerPage.setVisible(!isLoading);
    cbSegmentation.setVisible(!isLoading);
    cbOrder.setVisible(!isLoading);
  }

  public void setUpdateStateFromConfig(boolean updateStateFromConfig) {
    this.updateStateFromConfig = updateStateFromConfig;
  }


  public void updateSearchPanelConfigurationInBackground(final Collection<String> corpora) {
    setLoadingState(true);
    // remove custom adjustments
    cbLeftContext.setItems();
    cbRightContext.setItems();
    cbSegmentation.setItems();

    UI ui = getUI();
    if (ui instanceof AnnisUI) {
      // reload the config in the background
      Background.run(new CorpusConfigUpdater((AnnisUI) ui, corpora, true));
    }
  }


}
