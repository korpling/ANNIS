package org.corpus_tools.annis.gui;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static com.github.mvysny.kaributesting.v8.LocatorJ._setValue;
import static org.corpus_tools.annis.gui.TestHelper.awaitCondition;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.mvysny.kaributesting.v8.GridKt;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.github.mvysny.kaributesting.v8.NotificationsKt;
import com.vaadin.data.provider.Query;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.v7.ui.TextArea;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import kotlin.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.corpus_tools.annis.api.model.Annotation;
import org.corpus_tools.annis.api.model.FindQuery.OrderEnum;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.components.codemirror.AqlCodeEditor;
import org.corpus_tools.annis.gui.components.medialement.MediaElementPlayer;
import org.corpus_tools.annis.gui.controlpanel.ControlPanel;
import org.corpus_tools.annis.gui.controlpanel.CorpusListPanel;
import org.corpus_tools.annis.gui.controlpanel.CorpusListPanel.CorpusWithSize;
import org.corpus_tools.annis.gui.controlpanel.SearchOptionsPanel;
import org.corpus_tools.annis.gui.docbrowser.DocBrowserPanel;
import org.corpus_tools.annis.gui.docbrowser.DocBrowserTable;
import org.corpus_tools.annis.gui.resultview.ResultViewPanel;
import org.corpus_tools.annis.gui.resultview.SingleCorpusResultPanel;
import org.corpus_tools.annis.gui.resultview.SingleResultPanel;
import org.corpus_tools.annis.gui.visualizers.component.grid.GridComponent;
import org.corpus_tools.annis.gui.visualizers.component.kwic.KWICComponent;
import org.corpus_tools.annis.gui.visualizers.component.kwic.KWICMultipleTextComponent;
import org.corpus_tools.annis.gui.widgets.AutoHeightIFrame;
import org.corpus_tools.annis.gui.widgets.grid.AnnotationGrid;
import org.corpus_tools.annis.gui.widgets.grid.GridEvent;
import org.corpus_tools.annis.gui.widgets.grid.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@ActiveProfiles({"desktop", "test", "headless"})
@WebAppConfiguration
class AnnisUITest {

  @Autowired
  private BeanFactory beanFactory;

  private AnnisUI ui;

  @BeforeEach
  public void setup() {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    ui = beanFactory.getBean(AnnisUI.class);


    MockVaadin.setup(() -> ui);

    CorpusSet testCorpusSet = new CorpusSet();
    testCorpusSet.setName("test");
    testCorpusSet.getCorpora().add("pcc2");
    ui.getInstanceConfig().getCorpusSets().add(testCorpusSet);

  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  @Test
  void testTitleFromInstanceConfig() {
    assertEquals("ANNIS", ui.getInstanceConfig().getInstanceDisplayName());
  }

  @Test
  void testChangeCorpusSet() {
    CorpusListPanel corpusListPanel = _get(CorpusListPanel.class);

    @SuppressWarnings("unchecked")
    Grid<CorpusWithSize> corpusList = _get(corpusListPanel, Grid.class);

    @SuppressWarnings("unchecked")
    ComboBox<String> corpusSetChooser = _get(corpusListPanel, ComboBox.class);
    assertEquals(null, corpusSetChooser.getValue());


    assertTrue(GridKt._size(corpusList) > 1);

    _setValue(corpusSetChooser, "test");

    assertEquals(1, GridKt._size(corpusList));
    CorpusWithSize firstEntry = GridKt._get(corpusList, 0);
    assertEquals("pcc2", firstEntry.getName());
  }

  @SuppressWarnings("unchecked")
  @Test
  void showSelectedCorporaOnly() throws Exception {
    CorpusListPanel corpusListPanel = _get(CorpusListPanel.class);


    Grid<CorpusWithSize> corpusList = _get(corpusListPanel, Grid.class);

    selectCorpus("pcc2", true);
    selectCorpus("parallel.sample", false);
    _setValue(_get(TextField.class, spec -> spec.withPlaceholder("Filter")), "");

    long oldCorpusItemsSize = GridKt._size(corpusList);

    assertTrue(oldCorpusItemsSize > 2);
    // Only show the selected ones
    _setValue(_get(CheckBox.class, spec -> spec.withCaption("Selected only")), true);

    long updatedCorpusItemsSize = GridKt._size(corpusList);
    assertEquals(2, updatedCorpusItemsSize);

    // Show unselected again
    _setValue(_get(CheckBox.class, spec -> spec.withCaption("Selected only")), false);
    updatedCorpusItemsSize = GridKt._size(corpusList);
    assertEquals(oldCorpusItemsSize, updatedCorpusItemsSize);
  }

  private void selectCorpus(String corpusName) throws Exception {
    selectCorpus(corpusName, true);
  }

  private void selectCorpus(String corpusName, boolean unselectOld) throws Exception {

    // Filter for the corpus name in case the corpus list has too many entries and does not show
    // the selected corpus yet
    _setValue(_get(TextField.class, spec -> spec.withPlaceholder("Filter")), corpusName);

    @SuppressWarnings("unchecked")
    Grid<CorpusWithSize> grid = _get(Grid.class,
        spec -> spec.withId("SearchView-ControlPanel-TabSheet-CorpusListPanel-tblCorpora"));
    if (unselectOld) {
      grid.getSelectionModel().deselectAll();
      awaitCondition(30, () -> ui.getQueryState().getSelectedCorpora().isEmpty(),
          () -> "Corpus list was not empty");
    }

    // Wait until the (refreshed) corpus list is shown
    awaitCondition(30,
        () -> grid.getDataProvider().fetch(new Query<>())
            .anyMatch(c -> corpusName.equals(c.getName())),
        () -> "Corpus " + corpusName + " did not appear in corpus list");

    // Explicitly select the corpus
    Optional<CorpusWithSize> entry =
        grid.getDataProvider().fetch(new Query<>()).filter(c -> corpusName.equals(c.getName()))
            .findFirst();
    if (entry.isPresent()) {
      grid.getSelectionModel().select(entry.get());

    }

    awaitCondition(30, () -> ui.getQueryState().getSelectedCorpora().contains(corpusName),
        () -> "Could not select corpus " + corpusName + ", "
            + ui.getQueryState().getSelectedCorpora() + " was the current selection.");
  }


  private void executeSearch(String corpusName, String query, int matchCount, int documentCount)
      throws Exception {
    selectCorpus(corpusName);

    // Set the query and submit query
    _get(AqlCodeEditor.class).getPropertyDataSource().setValue(query);
    MockVaadin.INSTANCE.clientRoundtrip();
    awaitCondition(5, () -> query.equals(ui.getQueryState().getAql().getValue()));
    awaitCondition(5, () -> "Valid query, click on \"Search\" to start searching."
        .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()));

    Button searchButton = _get(Button.class, spec -> spec.withCaption("Search"));
    _click(searchButton);

    // Wait until the count is displayed
    String expectedStatus = "" + matchCount + " matches\nin " + documentCount
        + (documentCount == 1 ? " document" : " documents");
    awaitCondition(60,
        () -> expectedStatus
            .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()),
        () -> "Waited for status \"" + expectedStatus + "\" but was \""
            + ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus() + "\"",
        1000);


    awaitCondition(60, () -> {
      ResultViewPanel resultView = _get(ResultViewPanel.class);

      return _find(resultView, SingleResultPanel.class).size() == Math.min(matchCount, 10);
    }, 100);
  }

  @Test
  void tokenSearchPcc2() throws Exception {
    executeSearch("pcc2", "tok", 399, 2);

    // Test that the cell values have the correct token value
    SingleResultPanel resultPanel = _find(SingleResultPanel.class).get(0);
    KWICComponent kwicVis = _get(resultPanel, KWICComponent.class);
    AnnotationGrid kwicGrid = _get(kwicVis, AnnotationGrid.class);
    ArrayList<Row> tokens = kwicGrid.getRowsByAnnotation().get("tok");
    assertEquals(1, tokens.size());
    assertEquals(Arrays.asList("Feigenblatt", "Die", "Jugendlichen", "in", "Zossen", "wollen"),
        tokens.get(0).getEvents().stream().map(GridEvent::getValue).collect(Collectors.toList()));


    // Check the annotation values are shown
    ArrayList<Row> lemmaRows = kwicGrid.getRowsByAnnotation().get("tiger::lemma");
    assertEquals(1, lemmaRows.size());
    assertEquals(Arrays.asList("Feigenblatt", "der", "jugendliche", "in", "Zossen", "wollen"),
        lemmaRows.get(0).getEvents().stream().map(GridEvent::getValue)
            .collect(Collectors.toList()));

    ArrayList<Row> posRows = kwicGrid.getRowsByAnnotation().get("tiger::pos");
    assertEquals(1, posRows.size());
    assertEquals(Arrays.asList("NN", "ART", "NN", "APPR", "NE", "VMFIN"),
        posRows.get(0).getEvents().stream().map(GridEvent::getValue).collect(Collectors.toList()));

    // Test that we can show the first metadata for the button
    List<Button> infoButtons = _find(Button.class,
        spec -> spec.withPredicate(b -> "Show metadata".equals(b.getDescription())));
    assertEquals(10, infoButtons.size());
    _click(infoButtons.get(0));
    Window infoWindow = _get(Window.class);
    assertEquals("Info for salt:/pcc2/11299", infoWindow.getCaption());

    awaitCondition(30, () -> !_find(infoWindow, Accordion.class).isEmpty());

    Accordion metaAccordion = _get(infoWindow, Accordion.class);
    @SuppressWarnings("rawtypes")
    List<Grid> metadataGrids = _find(metaAccordion, Grid.class);
    assertEquals(2, metadataGrids.size());
    assertEquals("11299 (document)", metaAccordion.getTab(metadataGrids.get(0)).getCaption());
    assertEquals("pcc2 (corpus)", metaAccordion.getTab(metadataGrids.get(1)).getCaption());

    @SuppressWarnings("unchecked")
    Annotation firstAnno = (Annotation) GridKt._get(metadataGrids.get(0), 0);
    assertEquals("Dokumentname", firstAnno.getKey().getName());
    assertEquals("pcc-11299", firstAnno.getVal());
    @SuppressWarnings("unchecked")
    Annotation secondAnno = (Annotation) GridKt._get(metadataGrids.get(0), 1);
    assertEquals("Genre", secondAnno.getKey().getName());
    assertEquals("Politik", secondAnno.getVal());
    @SuppressWarnings("unchecked")
    Annotation thirdAnno = (Annotation) GridKt._get(metadataGrids.get(0), 2);
    assertEquals("Titel", thirdAnno.getKey().getName());
    assertEquals("Feigenblatt", thirdAnno.getVal());



    // Disable the part-of-speech token annotation display
    TreeSet<String> visibleAnnos = new TreeSet<>(Arrays.asList("tiger::lemma"));
    resultPanel.setVisibleTokenAnnosVisible(visibleAnnos);
    assertNull(kwicGrid.getRowsByAnnotation().get("tiger:pos"));
    assertNotNull(kwicGrid.getRowsByAnnotation().get("tiger::lemma"));

    // Change the context and test that the KWIC displayed also changed
    resultPanel.changeContext(1, 6, false);

    // Since the action will replace the whole result panel, we have to get all
    // variables again.
    List<String> expectedTokens =
        Arrays.asList("Feigenblatt", "Die", "Jugendlichen", "in", "Zossen", "wollen", "ein");
    awaitCondition(10, () -> {
      List<SingleResultPanel> allResults = _find(SingleResultPanel.class);
      if (allResults.isEmpty()) {
        return false;
      }
      List<AnnotationGrid> annoGrids = _find(allResults.get(0), AnnotationGrid.class);
      if (annoGrids.isEmpty()) {
        return false;
      }
      ArrayList<Row> tokensUpdatedContext = annoGrids.get(0).getRowsByAnnotation().get("tok");
      if (tokensUpdatedContext.isEmpty()) {
        return false;
      }
      List<String> actualTokens = tokensUpdatedContext.get(0).getEvents().stream()
          .map(GridEvent::getValue).collect(Collectors.toList());
      return expectedTokens.equals(actualTokens);
    });

  }

  @Test
  void searchPcc2InverseOrder() throws Exception {

    selectCorpus("pcc2");


    // Set inverse order in search options
    TabSheet optionTabSheet = _get(_get(ControlPanel.class), TabSheet.class);
    optionTabSheet.setSelectedTab(_get(SearchOptionsPanel.class));

    awaitCondition(10,
        () -> !_find(optionTabSheet, ComboBox.class, spec -> spec.withCaption("Order")).isEmpty());

    @SuppressWarnings("unchecked")
    ComboBox<OrderEnum> orderComboBox =
        _get(optionTabSheet, ComboBox.class, spec -> spec.withCaption("Order"));
    _setValue(orderComboBox, OrderEnum.INVERTED);

    // Set the query and submit query
    _get(AqlCodeEditor.class).getPropertyDataSource().setValue("\"Die\"");
    MockVaadin.INSTANCE.clientRoundtrip();
    awaitCondition(5, () -> "\"Die\"".equals(ui.getQueryState().getAql().getValue()));
    awaitCondition(5, () -> "Valid query, click on \"Search\" to start searching."
        .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()));

    Button searchButton = _get(Button.class, spec -> spec.withCaption("Search"));
    _click(searchButton);

    // Wait until the count is displayed
    String expectedStatus = "4 matches\nin 2 documents";
    awaitCondition(60,
        () -> expectedStatus
            .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()),
        () -> "Waited for status \"" + expectedStatus + "\" but was \""
            + ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus() + "\"");

    ResultViewPanel resultView = _get(ResultViewPanel.class);

    awaitCondition(30, () -> _find(resultView, SingleResultPanel.class).size() == 4);

    // Test that the cell values have the correct token value
    SingleResultPanel resultPanel = _find(SingleResultPanel.class).get(0);
    KWICComponent kwicVis = _get(resultPanel, KWICComponent.class);
    AnnotationGrid kwicGrid = _get(kwicVis, AnnotationGrid.class);
    ArrayList<Row> tokens = kwicGrid.getRowsByAnnotation().get("tok");
    assertEquals(1, tokens.size());
    assertEquals(
        Arrays.asList("fürs", "Dallgower", "Tor", "gab", ".", "Die", "Seeburger", "und", "einige",
            "Groß-Glienicker", "haben"),
        tokens.get(0).getEvents().stream().map(GridEvent::getValue).collect(Collectors.toList()));
  }

  @Test
  void searchPccDocumentMatches() throws Exception {

    selectCorpus("pcc2");

    // Set the query and submit query
    _get(AqlCodeEditor.class).getPropertyDataSource().setValue("Genre");
    MockVaadin.INSTANCE.clientRoundtrip();
    awaitCondition(5, () -> "Genre".equals(ui.getQueryState().getAql().getValue()));
    awaitCondition(5, () -> "Valid query, click on \"Search\" to start searching."
        .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()));

    Button searchButton = _get(Button.class, spec -> spec.withCaption("Search"));
    _click(searchButton);

    // Wait until the count is displayed
    String expectedStatus = "2 matches";
    awaitCondition(60,
        () -> expectedStatus
            .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()),
        () -> "Waited for status \"" + expectedStatus + "\" but was \""
            + ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus() + "\"");

    // Test that the special corpus result panel visualizer is shown
    awaitCondition(30, () -> _find(SingleCorpusResultPanel.class).size() == 2);
    List<SingleCorpusResultPanel> results = _find(SingleCorpusResultPanel.class);
    assertNotNull(_get(results.get(0), Label.class, spec -> spec.withValue("Path: pcc2 > 11299")));
    assertNotNull(_get(results.get(0), Button.class,
        spec -> spec.withPredicate(b -> b.getIcon() == VaadinIcons.INFO_CIRCLE)));

    // The standard SingleResult panel should not be visible
    assertEquals(0, _find(SingleResultPanel.class).size());
  }

  @Test
  void searchPccCorpusMatches() throws Exception {

    selectCorpus("pcc2");

    // Set the query and submit query
    _get(AqlCodeEditor.class).getPropertyDataSource().setValue("URL");
    MockVaadin.INSTANCE.clientRoundtrip();
    awaitCondition(5, () -> "URL".equals(ui.getQueryState().getAql().getValue()));
    awaitCondition(5, () -> "Valid query, click on \"Search\" to start searching."
        .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()));

    Thread.sleep(500);

    Button searchButton = _get(Button.class, spec -> spec.withCaption("Search"));
    _click(searchButton);

    Thread.sleep(500);

    // Wait until the count is displayed
    String expectedStatus = "1 match";
    awaitCondition(60,
        () -> expectedStatus
            .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()),
        () -> "Waited for status \"" + expectedStatus + "\" but was \""
            + ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus() + "\"");

    // Test that the special corpus result panel visualizer is shown
    awaitCondition(30, () -> _find(SingleCorpusResultPanel.class).size() == 1);
    List<SingleCorpusResultPanel> results = _find(SingleCorpusResultPanel.class);
    assertNotNull(_get(results.get(0), Label.class, spec -> spec.withValue("Path: pcc2")));
    assertNotNull(_get(results.get(0), Button.class,
        spec -> spec.withPredicate(b -> b.getIcon() == VaadinIcons.INFO_CIRCLE)));

    /// The standard SingleResult panel should not be visible
    assertEquals(0, _find(SingleResultPanel.class).size());
  }


  @Test
  void openVisualizerPcc2() throws Exception {
    executeSearch("pcc2", "tok", 399, 2);

    SingleResultPanel resultPanel = _find(SingleResultPanel.class).get(0);
    _get(resultPanel, KWICComponent.class);

    // Open the coreference visualizer and check that IFrame component is loaded
    Button btOpenCorefVisualizer =
        _get(resultPanel, Button.class, spec -> spec.withCaption("coreference (discourse)"));
    _click(btOpenCorefVisualizer);
    awaitCondition(120, () -> !_find(resultPanel, AutoHeightIFrame.class).isEmpty());
    AutoHeightIFrame iframe = _get(resultPanel, AutoHeightIFrame.class, spec -> spec.withCount(1));
    assertTrue(iframe.getState().getUrl().startsWith("/vis-iframe-res/"));

    // Close the visualizer again
    _click(btOpenCorefVisualizer);
    awaitCondition(60, () -> _find(resultPanel, AutoHeightIFrame.class).isEmpty());

    // Open a HTML visualizer
    Button btOpenHtmlVisualizer = _get(resultPanel, Button.class,
        spec -> spec.withCaption("information structure (document)"));
    _click(btOpenHtmlVisualizer);
    awaitCondition(65,
        () -> !_find(resultPanel, Panel.class,
            spec -> spec.withPredicate(p -> p.getStyleName().startsWith("annis-wrapped-htmlvis-")))
                .isEmpty(),
        2000);

    Panel htmlPanel = _get(resultPanel, Panel.class,
        spec -> spec.withPredicate(p -> p.getStyleName().startsWith("annis-wrapped-htmlvis-")));
    Label htmlLabel = _get(htmlPanel, Label.class);
    assertEquals(ContentMode.HTML, htmlLabel.getContentMode());
    assertTrue(
        htmlLabel.getValue().startsWith("<span class=\"tok\"  style=\" color:\" >Feigenblatt<"));

  }

  @Test
  void tokenSearchDialog() throws Exception {

    executeSearch("dialog.demo", "tok", 102, 1);

    // Test that there is a grid visualizer
    SingleResultPanel resultPanel = _find(SingleResultPanel.class).get(0);
    GridComponent gridVis = _get(resultPanel, GridComponent.class,
        spec -> spec.withPredicate(g -> !(g instanceof KWICComponent)));
    AnnotationGrid annoGrid = _get(gridVis, AnnotationGrid.class);
    ArrayList<Row> tokens = annoGrid.getRowsByAnnotation().get("default_ns::norm0");
    assertEquals(1, tokens.size());

    assertEquals(Arrays.asList("äh", "fang", "einfach", "mal", "an"),
        tokens.get(0).getEvents().stream().map(GridEvent::getValue).collect(Collectors.toList()));

    // Open the video visualizer and check that media component is loaded
    Button btOpenVisualizer = _get(resultPanel, Button.class, spec -> spec.withCaption("video"));
    _click(btOpenVisualizer);
    awaitCondition(120, () -> !_find(resultPanel, MediaElementPlayer.class).isEmpty());
    MediaElementPlayer player =
        _get(resultPanel, MediaElementPlayer.class, spec -> spec.withCount(1));
    assertEquals("video/webm", player.getState().getMimeType());
    assertEquals(
        "/Binary?file=dialog.demo%2Fdialog.demo%2Fdialog.demo.webm&toplevelCorpusName=dialog.demo",
        player.getState().getResourceURL());

    // Close the visualizer again
    _click(btOpenVisualizer);
    awaitCondition(120, () -> _find(resultPanel, MediaElementPlayer.class).isEmpty());
  }

  @Test
  void parallelTextKwic() throws Exception {
    executeSearch("parallel.sample", "tok ->align_e-g tok", 5, 1);

    // Test that there are two KWIC panels with the correct table cells
    SingleResultPanel resultPanel = _find(SingleResultPanel.class).get(0);
    KWICMultipleTextComponent parentKwicVis = _get(resultPanel, KWICMultipleTextComponent.class);

    List<KWICComponent> kwicVisualizers = _find(parentKwicVis, KWICComponent.class);
    assertEquals(2, kwicVisualizers.size());


    AnnotationGrid firstKwicGrid = _get(kwicVisualizers.get(0), AnnotationGrid.class);
    ArrayList<Row> tokens = firstKwicGrid.getRowsByAnnotation().get("tok");
    assertEquals(1, tokens.size());
    assertEquals(Arrays.asList("This", "is", "an", "example", "."),
        tokens.get(0).getEvents().stream().map(GridEvent::getValue).collect(Collectors.toList()));

    AnnotationGrid secondKwicGrid = _get(kwicVisualizers.get(1), AnnotationGrid.class);
    tokens = secondKwicGrid.getRowsByAnnotation().get("tok");
    assertEquals(1, tokens.size());
    assertEquals(Arrays.asList("Das", "ist", "ein", "Beispielsatz", "."),
        tokens.get(0).getEvents().stream().map(GridEvent::getValue).collect(Collectors.toList()));

  }

  @Test
  void shareSingleResult() throws Exception {
    executeSearch("pcc2", "tok", 399, 2);

    // Activate the share window
    SingleResultPanel resultPanel = _find(SingleResultPanel.class).get(0);
    _click(_get(resultPanel, Button.class,
        spec -> spec.withPredicate(b -> "Share match reference".equals(b.getDescription()))));

    // Get the window which shows all the different links
    Window shareWindow = _get(Window.class, spec -> spec.withCaption("Match reference link"));
    TextArea linkTextField =
        _get(shareWindow, TextArea.class, spec -> spec.withCaption("Link for publications"));
    URI shortUrl = URI.create(linkTextField.getValue());
    List<NameValuePair> paramsShortUrl =
        URLEncodedUtils.parse(shortUrl.getQuery(), StandardCharsets.UTF_8);
    assertEquals(1, paramsShortUrl.size());
    assertEquals("id", paramsShortUrl.get(0).getName());
    // Un-shorten the URL and examine its parts
    Optional<URI> originalUrl =
        ui.getUrlShortener().unshorten(UUID.fromString(paramsShortUrl.get(0).getValue()));
    assertTrue(originalUrl.isPresent());
    if (originalUrl.isPresent()) {
      List<NameValuePair> paramsOriginalUrl =
          URLEncodedUtils.parse(originalUrl.get().getRawQuery(), StandardCharsets.UTF_8);
      assertFalse(paramsOriginalUrl.isEmpty());
      assertTrue(paramsOriginalUrl.stream()
          .anyMatch(p -> EmbeddedVisUI.KEY_LEFT.equals(p.getName()) && "5".equals(p.getValue())));
      assertTrue(paramsOriginalUrl.stream()
          .anyMatch(p -> EmbeddedVisUI.KEY_RIGHT.equals(p.getName()) && "5".equals(p.getValue())));
      assertFalse(
          paramsOriginalUrl.stream().anyMatch(p -> EmbeddedVisUI.KEY_INSTANCE.equals(p.getName())));
      assertTrue(paramsOriginalUrl.stream()
          .anyMatch(p -> EmbeddedVisUI.KEY_SEARCH_INTERFACE.equals(p.getName())
              && p.getValue().startsWith("http://localhost:8080#_q=")));
      assertTrue(
          paramsOriginalUrl.stream().anyMatch(p -> EmbeddedVisUI.KEY_MATCH.equals(p.getName())
              && "pcc2/11299#tok_1".equals(p.getValue())));
    }
  }


  @Test
  void aboutWindow() {
    UI.getCurrent().getNavigator().navigateTo("");

    _click(_get(Button.class, spec -> spec.withCaption("About ANNIS")));

    // Check that the windows has opened and no error message is shown
    Window window = _get(Window.class, spec -> spec.withCaption("About ANNIS"));
    assertNotNull(window);
    assertEquals(0, _find(ExceptionDialog.class).size());

    // Close the window again
    Button btClose = _get(Button.class, spec -> spec.withCaption("Close"));
    assertNotNull(btClose);
    _click(btClose);

    // Window should be closed
    assertEquals(0, _find(Window.class, spec -> spec.withCaption("About ANNIS")).size());
  }

  @Test
  void openSourceWindow() {
    UI.getCurrent().getNavigator().navigateTo("");

    _click(_get(Button.class, spec -> spec.withCaption("Help us make ANNIS better!")));

    // Check that the windows has opened
    assertNotNull(_get(Window.class, spec -> spec.withCaption("Help us make ANNIS better!")));

    // Close the window again
    Button btClose = _get(Button.class, spec -> spec.withCaption("Close"));
    assertNotNull(btClose);
    _click(btClose);

    // Window should be closed
    assertEquals(0,
        _find(Window.class, spec -> spec.withCaption("Help us make ANNIS better!")).size());
  }

  @Test
  void showDocumentRawText() throws Exception {
    UI.getCurrent().getNavigator().navigateTo("");

    ui.getSearchView().getDocBrowserController().openDocBrowser("pcc2");

    DocBrowserPanel panel = _get(DocBrowserPanel.class);

    awaitCondition(120, () -> !_find(panel, DocBrowserTable.class).isEmpty());

    DocBrowserTable docBrowserTable = _get(panel, DocBrowserTable.class);
    List<Button> fullTextButtons =
        _find(docBrowserTable, Button.class, spec -> spec.withCaption("full text"));
    assertEquals(2, fullTextButtons.size());

    // Filter by the document name to reduce the number of buttons we can press
    TextField textFilter = _get(panel, TextField.class);
    _setValue(textFilter, "11299");

    // Wait until filter is applied
    awaitCondition(30,
        () -> _find(docBrowserTable, Button.class, spec -> spec.withCaption("full text"))
            .size() == 1);

    // Click on the button to open the full text visualization for the first document
    _click(_get(docBrowserTable, Button.class, spec -> spec.withCaption("full text")));

    Component rawTextPanel = ui.getSearchView().getTabSheet().getSelectedTab();
    Tab selectedTab = ui.getSearchView().getTabSheet().getTab(rawTextPanel);
    assertEquals("pcc2 > 11299 - ...", selectedTab.getCaption());

    // Wait for label to appear
    awaitCondition(20, () -> !_find(rawTextPanel, Label.class).isEmpty());
    Label rawTextLabel = _get(rawTextPanel, Label.class);
    assertTrue(rawTextLabel.getValue()
        .startsWith("Feigenblatt Die Jugendlichen in Zossen wollen ein Musikcafé ."));
    assertTrue(rawTextLabel.getValue().endsWith("Die glänzten diesmal noch mit Abwesenheit ."));

  }

  @Test
  void showDocumentRawTextParallel() throws Exception {
    UI.getCurrent().getNavigator().navigateTo("");

    ui.getSearchView().getDocBrowserController().openDocBrowser("parallel.sample");

    DocBrowserPanel panel = _get(DocBrowserPanel.class);

    awaitCondition(120, () -> !_find(panel, DocBrowserTable.class).isEmpty());

    DocBrowserTable docBrowserTable = _get(panel, DocBrowserTable.class);

    // Click on the button to open the full text visualization for the first document
    _click(_get(docBrowserTable, Button.class, spec -> spec.withCaption("full text")));

    Component rawTextPanel = ui.getSearchView().getTabSheet().getSelectedTab();
    Tab selectedTab = ui.getSearchView().getTabSheet().getTab(rawTextPanel);
    assertEquals("parallel.sample...", selectedTab.getCaption());

    // Wait for label to appear
    awaitCondition(20, () -> _find(rawTextPanel, Label.class).size() == 2, 1000);
    List<Label> rawTextLabels = _find(rawTextPanel, Label.class);
    assertEquals("This is an example . ", rawTextLabels.get(0).getValue());
    assertEquals("Das ist ein Beispielsatz . ", rawTextLabels.get(1).getValue());

  }

  @Test
  void emptyQueryStatus() throws Exception {
    selectCorpus("pcc2");

    // Set and empty query and submit query
    _get(AqlCodeEditor.class).getPropertyDataSource().setValue("");
    MockVaadin.INSTANCE.clientRoundtrip();
    awaitCondition(5, () -> "".equals(ui.getQueryState().getAql().getValue()));

    Button searchButton = _get(Button.class, spec -> spec.withCaption("Search"));
    _click(searchButton);

    // Wait until the status is displayed
    String expectedStatus = "Empty query";
    awaitCondition(60,
        () -> expectedStatus
            .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()),
        () -> "Waited for status \"" + expectedStatus + "\" but was \""
            + ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus() + "\"");
    assertEquals(1,
        _find(com.vaadin.ui.TextArea.class, spec -> spec.withValue(expectedStatus)).size());
  }

  @Test
  void emptyCorpusStatus() throws Exception {
    @SuppressWarnings("unchecked")
    Grid<String> grid = _get(_get(CorpusListPanel.class), Grid.class);
    grid.getSelectionModel().deselectAll();

    // Wait until the (refreshed) corpus list is shown
    awaitCondition(30, () -> ui.getQueryState().getSelectedCorpora().isEmpty(),
        () -> "Selecting no corpus failed");

    // Set and empty query and submit query
    _get(AqlCodeEditor.class).getPropertyDataSource().setValue("tok");
    MockVaadin.INSTANCE.clientRoundtrip();
    awaitCondition(5, () -> "tok".equals(ui.getQueryState().getAql().getValue()));
    awaitCondition(10, () -> "Please select a corpus from the list below, then click on \"Search\"."
        .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()));

    Button searchButton = _get(Button.class, spec -> spec.withCaption("Search"));
    _click(searchButton);

    // Wait until the message is displayed
    String expectedStatus = "No corpus selected";
    awaitCondition(60,
        () -> expectedStatus
            .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()),
        () -> "Waited for status \"" + expectedStatus + "\" but was \""
            + ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus() + "\"");
    assertEquals(1,
        _find(com.vaadin.ui.TextArea.class, spec -> spec.withValue(expectedStatus)).size());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testAqlError() throws Exception {
    NotificationsKt.clearNotifications();
    selectCorpus("pcc2");

    // Set the query and check that a the syntax error is reported as status, not as error dialog
    _get(AqlCodeEditor.class).getPropertyDataSource().setValue("tok &");
    MockVaadin.INSTANCE.clientRoundtrip();

    awaitCondition(5, () -> "tok &".equals(ui.getQueryState().getAql().getValue()));
    awaitCondition(5, () -> "Unexpected end of query."
        .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()));

    NotificationsKt.expectNoNotifications();

    // Also check that executing the query show a notification
    _get(AqlCodeEditor.class).getPropertyDataSource().setValue("tok & tok");
    MockVaadin.INSTANCE.clientRoundtrip();
    Button searchButton = _get(Button.class, spec -> spec.withCaption("Search"));
    _click(searchButton);
    String expectedStatus = "Variable \"#2\" not bound (use linguistic operators)";
    awaitCondition(60,
        () -> expectedStatus
            .equals(ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus()),
        () -> "Waited for status \"" + expectedStatus + "\" but was \""
            + ui.getSearchView().getControlPanel().getQueryPanel().getLastPublicStatus() + "\"");
    assertEquals(1,
        _find(com.vaadin.ui.TextArea.class, spec -> spec.withValue(expectedStatus)).size());

    NotificationsKt.expectNotifications(new Pair<String, String>("Parsing error", expectedStatus));

  }

  @Test
  void testCorpusFragment() throws Exception {

    assertTrue(ui.getQueryState().getSelectedCorpora().isEmpty());

    Page.getCurrent().setUriFragment("c=pcc2");
    awaitCondition(15, () -> ui.getQueryState().getSelectedCorpora().size() == 1
        && ui.getQueryState().getSelectedCorpora().contains("pcc2"));

  }

}
