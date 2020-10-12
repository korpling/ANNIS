package annis.gui;

import static annis.gui.TestHelper.awaitCondition;
import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static com.github.mvysny.kaributesting.v8.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.components.codemirror.AqlCodeEditor;
import annis.gui.docbrowser.DocBrowserPanel;
import annis.gui.docbrowser.DocBrowserTable;
import annis.gui.resultview.SingleResultPanel;
import annis.gui.widgets.AutoHeightIFrame;
import annis.gui.widgets.grid.AnnotationGrid;
import annis.gui.widgets.grid.Row;
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
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@ActiveProfiles({"desktop", "test"})
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
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

  private void selectCorpus() {
    UI.getCurrent().getNavigator().navigateTo("");

    // Filter for the corpus name in case the corpus list has too many entries and does not show
    // the pcc2 corpus yet
    _setValue(_get(TextField.class, spec -> spec.withPlaceholder("Filter")), "pcc2");

    // Explicitly select the corpus
    @SuppressWarnings("unchecked")
    Grid<String> grid = _get(Grid.class,
        spec -> spec.withId("SearchView-ControlPanel-TabSheet-CorpusListPanel-tblCorpora"));
    grid.getSelectionModel().select("pcc2");
  }

  private void executeTokenSearch() {
    selectCorpus();

    // Set the query and submit query
    _get(AqlCodeEditor.class).getPropertyDataSource().setValue("tok");
    _click(_get(Button.class, spec -> spec.withCaption("Search")));

    awaitCondition(240, () -> !_find(SingleResultPanel.class).isEmpty());
  }

  @Test
  void tokenSearchPcc2() throws InterruptedException, IOException {

    executeTokenSearch();

    // Test that the cell values have the correct token value
    SingleResultPanel resultPanel = _find(SingleResultPanel.class).get(0);
    KWICComponent kwicVis = _get(resultPanel, KWICComponent.class);
    AnnotationGrid kwicGrid = _get(kwicVis, AnnotationGrid.class);
    ArrayList<Row> tokens = kwicGrid.getRowsByAnnotation().get("tok");
    assertEquals(1, tokens.size());
    assertEquals(6, tokens.get(0).getEvents().size());
    assertEquals("Feigenblatt", tokens.get(0).getEvents().get(0).getValue());
    assertEquals("Die", tokens.get(0).getEvents().get(1).getValue());
    assertEquals("Jugendlichen", tokens.get(0).getEvents().get(2).getValue());
    assertEquals("in", tokens.get(0).getEvents().get(3).getValue());
    assertEquals("Zossen", tokens.get(0).getEvents().get(4).getValue());
    assertEquals("wollen", tokens.get(0).getEvents().get(5).getValue());

    // Open the coreference visualizer and check that IFrame component is loaded
    Button btOpenVisualizer =
        _get(resultPanel, Button.class, spec -> spec.withCaption("coreference (discourse)"));
    _click(btOpenVisualizer);
    awaitCondition(120, () -> !_find(resultPanel, AutoHeightIFrame.class).isEmpty());
    AutoHeightIFrame iframe = _get(resultPanel, AutoHeightIFrame.class, spec -> spec.withCount(1));
    assertTrue(iframe.getState().getUrl().startsWith("/vis-iframe-res/"));

    // Close the visualizer again
    _click(btOpenVisualizer);
    awaitCondition(120, () -> _find(resultPanel, AutoHeightIFrame.class).isEmpty());
  }

  @Test
  void shareSingleResult() {
    executeTokenSearch();

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
  void showDocumentRawText() {
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
