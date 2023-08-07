package org.corpus_tools.annis.gui;

import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.corpus_tools.annis.gui.TestHelper.awaitCondition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Window;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import org.corpus_tools.annis.gui.components.ExampleTable;
import org.corpus_tools.annis.gui.controlpanel.CorpusListPanel;
import org.corpus_tools.annis.gui.corpusbrowser.CorpusBrowserEntry;
import org.corpus_tools.annis.gui.corpusbrowser.CorpusBrowserPanel;
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
class CorpusBrowserPanelTest {

  @Autowired
  private BeanFactory beanFactory;

  AnnisUI ui;

  private CorpusBrowserPanel panel;

  private Accordion accordion;

  @BeforeEach
  public void setup() throws Exception {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    ui = beanFactory.getBean(AnnisUI.class);

    MockVaadin.setup(() -> ui);
  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  private void openCorpusBrowser(String corpus) throws Exception {
    // Open a corpus browser for the given corpus
    Button button = mock(Button.class);
    CorpusListPanel corpusList = ui.getSearchView().getControlPanel().getCorpusList();
    corpusList.initCorpusBrowser(corpus, button, ui);

    // Get window by its title
    Window w = _get(Window.class, spec -> spec.withCaption("Corpus information for " + corpus));

    // Wait for corpus browser content to load
    awaitCondition(30, () -> _find(w, ProgressBar.class).isEmpty());

    panel = _get(w, CorpusBrowserPanel.class);
    accordion = _get(panel, Accordion.class);
  }

  private Map<String, CorpusBrowserEntry> getItems(ExampleTable tbl) {
    @SuppressWarnings("unchecked")
    ListDataProvider<CorpusBrowserEntry> provider =
        (ListDataProvider<CorpusBrowserEntry>) tbl.getDataProvider();
    return provider.getItems().stream()
        .collect(Collectors.toMap(CorpusBrowserEntry::getName, cbe -> cbe));
  }

  @Test
  void generateNodeExampleQuery() throws Exception {
    openCorpusBrowser("pcc2");

    Tab tab = accordion.getTab(0);
    assertEquals("Node Annotations", tab.getCaption());

    Map<String, CorpusBrowserEntry> items = getItems(_get(tab.getComponent(), ExampleTable.class));

    CorpusBrowserEntry catEntry = items.get("cat");
    assertNotNull(catEntry);
    assertEquals("cat=\"S\"", catEntry.getQuery());
    assertEquals(new HashSet<>(Arrays.asList("pcc2")), catEntry.getCorpora());

  }

  @Test
  void generateEdgeAnnoExampleQuery() throws Exception {
    openCorpusBrowser("pcc2");

    Tab tab = accordion.getTab(1);
    assertEquals("Edge Annotations", tab.getCaption());

    Map<String, CorpusBrowserEntry> items = getItems(_get(tab.getComponent(), ExampleTable.class));

    CorpusBrowserEntry funcEntry = items.get("dep:func");
    assertNotNull(funcEntry);
    assertEquals("node & node & #1 ->dep[func=\"punct\"] #2", funcEntry.getQuery());
    assertEquals(new HashSet<>(Arrays.asList("pcc2")), funcEntry.getCorpora());

  }


  @Test
  void generateEdgeTypesExampleQuery() throws Exception {
    openCorpusBrowser("pcc2");

    Tab tab = accordion.getTab(2);
    assertEquals("Edge Types", tab.getCaption());

    Map<String, CorpusBrowserEntry> items = getItems(_get(tab.getComponent(), ExampleTable.class));

    CorpusBrowserEntry depEntry = items.get("dep:dep");
    assertNotNull(depEntry);
    assertEquals("node & node & #1 ->dep #2", depEntry.getQuery());
    assertEquals(new HashSet<>(Arrays.asList("pcc2")), depEntry.getCorpora());

    CorpusBrowserEntry tigerEntry = items.get("tiger:");
    assertNotNull(tigerEntry);
    assertEquals("node & node & #1 > #2", tigerEntry.getQuery());
    assertEquals(new HashSet<>(Arrays.asList("pcc2")), tigerEntry.getCorpora());

    CorpusBrowserEntry edgeEntry = items.get("tiger:edge");
    assertNotNull(edgeEntry);
    assertEquals("node & node & #1 >edge #2", edgeEntry.getQuery());
    assertEquals(new HashSet<>(Arrays.asList("pcc2")), edgeEntry.getCorpora());

    CorpusBrowserEntry secedgeEntry = items.get("tiger:secedge");
    assertNotNull(secedgeEntry);
    assertEquals("node & node & #1 >secedge #2", secedgeEntry.getQuery());
    assertEquals(new HashSet<>(Arrays.asList("pcc2")), secedgeEntry.getCorpora());

  }

  @Test
  void generateMetaAnntoationsExampleQuery() throws Exception {
    openCorpusBrowser("pcc2");

    Tab tab = accordion.getTab(3);
    assertEquals("Meta Annotations", tab.getCaption());

    Map<String, CorpusBrowserEntry> items = getItems(_get(tab.getComponent(), ExampleTable.class));

    CorpusBrowserEntry genreEntry = items.get("Genre");
    assertNotNull(genreEntry);
    assertEquals("Genre=\"Sport\"", genreEntry.getQuery());
    assertEquals(new HashSet<>(Arrays.asList("pcc2")), genreEntry.getCorpora());
  }

  @Test
  void generateEdgeTypesExampleQueryAeschylus() throws Exception {
    openCorpusBrowser("Aeschylus.Persae.L1-18");

    Tab tab = accordion.getTab(2);
    assertEquals("Edge Types", tab.getCaption());

    Map<String, CorpusBrowserEntry> items = getItems(_get(tab.getComponent(), ExampleTable.class));

    CorpusBrowserEntry emptyDominanceEntry = items.get("(dominance)");
    assertNotNull(emptyDominanceEntry);
    assertEquals("node & node & #1 > #2", emptyDominanceEntry.getQuery());
    assertEquals(new HashSet<>(Arrays.asList("Aeschylus.Persae.L1-18")),
        emptyDominanceEntry.getCorpora());


    CorpusBrowserEntry typedDominanceEntry = items.get("edge");
    assertNotNull(typedDominanceEntry);
    assertEquals("node & node & #1 >edge #2", typedDominanceEntry.getQuery());
    assertEquals(new HashSet<>(Arrays.asList("Aeschylus.Persae.L1-18")),
        typedDominanceEntry.getCorpora());


  }

}
