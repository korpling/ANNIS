package org.corpus_tools.annis.gui;

import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.corpus_tools.annis.gui.TestHelper.awaitCondition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Window;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.corpus_tools.annis.api.model.Annotation;
import org.corpus_tools.annis.gui.controlpanel.CorpusListPanel;
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
class MetaDataPanelTest {

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

  @Test
  void showPcc2CorpusBrowser() throws Exception {
    // Open a corpus browser for pcc2
    Button button = mock(Button.class);
    CorpusListPanel corpusList = ui.getSearchView().getControlPanel().getCorpusList();
    corpusList.initCorpusBrowser("pcc2", button);

    // Get window by its title
    Window w = _get(Window.class, spec -> spec.withCaption("Corpus information for pcc2"));

    // Check that the caption for the meta data panel is correct and it contains the meta data for
    // pcc2
    MetaDataPanel metaPanel = _get(w, MetaDataPanel.class, spec -> spec.withCaption("Metadata"));
    // Wait for corpus browser content to load
    awaitCondition(30, () -> _find(w, ProgressBar.class).isEmpty());

    Accordion accordion = _get(metaPanel, Accordion.class);
    assertEquals("pcc2", accordion.getTab(0).getCaption());
    @SuppressWarnings("unchecked")
    Grid<Annotation> metaGrid = _get(metaPanel, Grid.class);

    @SuppressWarnings("unchecked")
    Column<Annotation, String> nameColumn =
        (Column<Annotation, String>) metaGrid.getColumns().get(0);
    assertEquals("Name", nameColumn.getCaption());

    @SuppressWarnings("unchecked")
    Column<Annotation, Label> valueColumn =
        (Column<Annotation, Label>) metaGrid.getColumns().get(1);
    assertEquals("Value", valueColumn.getCaption());

    assertTrue(metaGrid.getDataProvider() instanceof ListDataProvider<?>);
    if (metaGrid.getDataProvider() instanceof ListDataProvider<?>) {
      @SuppressWarnings("unchecked")
      ListDataProvider<Annotation> dataProvider =
          (ListDataProvider<Annotation>) metaGrid.getDataProvider();
      Set<String> displayedNames = dataProvider.getItems().stream()
          .map(i -> nameColumn.getValueProvider().apply(i)).collect(Collectors.toSet());
      Set<String> displayedValues = dataProvider.getItems().stream()
          .map(i -> valueColumn.getValueProvider().apply(i).getValue())
          .collect(Collectors.toSet());

      assertEquals(new HashSet<>(Arrays.asList("URL", "annotation_description", "annotation_levels",
          "full_name", "language", "source", "version")), displayedNames);

      assertEquals(7, displayedValues.size());
      assertTrue(displayedValues
          .contains(
              "<a href=\"http://www.aclweb.org/anthology/W/W04/W04-0213.pdf\" target=\"_new\">link</a>"));
      assertTrue(displayedValues.contains("German"));
      assertTrue(displayedValues.contains("6.0"));
    }
  }

}
