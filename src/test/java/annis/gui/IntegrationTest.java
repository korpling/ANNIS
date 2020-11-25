package annis.gui;

import static annis.gui.TestHelper.awaitCondition;
import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static com.github.mvysny.kaributesting.v8.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.components.codemirror.AqlCodeEditor;
import annis.gui.components.medialement.MediaElementPlayer;
import annis.gui.docbrowser.DocBrowserPanel;
import annis.gui.docbrowser.DocBrowserTable;
import annis.gui.resultview.ResultViewPanel;
import annis.gui.resultview.SingleResultPanel;
import annis.gui.widgets.AutoHeightIFrame;
import annis.gui.widgets.grid.AnnotationGrid;
import annis.gui.widgets.grid.GridEvent;
import annis.gui.widgets.grid.Row;
import annis.visualizers.component.grid.GridComponent;
import annis.visualizers.component.kwic.KWICComponent;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
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
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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
class IntegrationTest {

  @Autowired
  private BeanFactory beanFactory;

  private AnnisUI ui;

  @BeforeEach
  public void setup() {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    ui = beanFactory.getBean(AnnisUI.class);
    MockVaadin.setup(() -> ui);
  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  private void selectCorpus(String corpusName) {
    UI.getCurrent().getNavigator().navigateTo("");

    // Filter for the corpus name in case the corpus list has too many entries and does not show
    // the pcc2 corpus yet
    _setValue(_get(TextField.class, spec -> spec.withPlaceholder("Filter")), corpusName);

    MockVaadin.INSTANCE.clientRoundtrip();

    // Explicitly select the corpus
    @SuppressWarnings("unchecked")
    Grid<String> grid = _get(Grid.class,
        spec -> spec.withId("SearchView-ControlPanel-TabSheet-CorpusListPanel-tblCorpora"));
    grid.getSelectionModel().select(corpusName);

    MockVaadin.INSTANCE.clientRoundtrip();
  }

  private void executeTokenSearch(String corpusName) throws Exception {
    selectCorpus(corpusName);

    // Set the query and submit query
    _get(AqlCodeEditor.class).getPropertyDataSource().setValue("tok");
    MockVaadin.INSTANCE.clientRoundtrip();
    awaitCondition(60, () -> "tok".equals(ui.getQueryState().getAql().getValue()));
    Button searchButton = _get(Button.class, spec -> spec.withCaption("Search"));
    _click(searchButton);
    awaitCondition(60, () -> searchButton.isEnabled());
    awaitCondition(60, () -> !_find(ResultViewPanel.class).isEmpty());
    ResultViewPanel resultView = _get(ResultViewPanel.class);
   
    awaitCondition(60, () -> !_find(resultView, SingleResultPanel.class).isEmpty());
  }

  @Test
  void tokenSearchPcc2() throws Exception {

    executeTokenSearch("pcc2");

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


    // Disable the part-of-speech token annotation display
    TreeSet<String> visibleAnnos = new TreeSet<>(Arrays.asList("tiger::lemma"));
    resultPanel.setVisibleTokenAnnosVisible(visibleAnnos);
    assertNull(kwicGrid.getRowsByAnnotation().get("tiger:pos"));
    assertNotNull(kwicGrid.getRowsByAnnotation().get("tiger::lemma"));
  }

  @Test
  void openVisualizerPcc2() throws Exception {

    executeTokenSearch("pcc2");

    SingleResultPanel resultPanel = _find(SingleResultPanel.class).get(0);
    _get(resultPanel, KWICComponent.class);

    // Open the coreference visualizer and check that IFrame component is loaded
    Button btOpenVisualizer =
        _get(resultPanel, Button.class, spec -> spec.withCaption("coreference (discourse)"));
    _click(btOpenVisualizer);
    awaitCondition(120, () -> !_find(resultPanel, AutoHeightIFrame.class).isEmpty());
    AutoHeightIFrame iframe = _get(resultPanel, AutoHeightIFrame.class, spec -> spec.withCount(1));
    assertTrue(iframe.getState().getUrl().startsWith("/vis-iframe-res/"));

    // Close the visualizer again
    _click(btOpenVisualizer);
    awaitCondition(60, () -> _find(resultPanel, AutoHeightIFrame.class).isEmpty());
  }

  @Test
  void tokenSearchDialog() throws Exception {

    executeTokenSearch("dialog.demo");

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
  void shareSingleResult() throws Exception {
    executeTokenSearch("pcc2");

    // Activate the share window
    SingleResultPanel resultPanel = _find(SingleResultPanel.class).get(0);
    _click(_get(resultPanel, Button.class,
        spec -> spec.withPredicate((b) -> "Share match reference".equals(b.getDescription()))));

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
  void aboutWindow() throws InterruptedException {
    UI.getCurrent().getNavigator().navigateTo("");

    _click(_get(Button.class, spec -> spec.withCaption("About ANNIS")));

    // Check that the windows has opened
    assertNotNull(_get(Window.class, spec -> spec.withCaption("About ANNIS")));

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

}
