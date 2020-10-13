package annis.gui;

import static annis.gui.TestHelper.awaitCondition;
import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.controlpanel.CorpusListPanel;
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
import java.util.List;
import java.util.stream.Collectors;
import org.corpus_tools.annis.api.model.Annotation;
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
  void showPcc2CorpusBrowser() {
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
    assertEquals("corpus: pcc2", accordion.getTab(0).getCaption());
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
      List<String> displayedNames = dataProvider.getItems().stream()
          .map(i -> nameColumn.getValueProvider().apply(i)).collect(Collectors.toList());
      List<String> displayedValues = dataProvider.getItems().stream()
          .map(i -> valueColumn.getValueProvider().apply(i).getValue())
          .collect(Collectors.toList());

      assertEquals(Arrays.asList("URL", "annotation_description", "annotation_levels", "full_name",
          "language", "source", "version"), displayedNames);

      assertEquals(6, displayedValues.size());
      assertEquals("<a href=\"https://www.aclweb.org/anthology/W04-0213.pdf\">link</a>",
          displayedValues.get(0));
      assertEquals("German", displayedValues.get(4));
      assertEquals("6.0", displayedValues.get(5));
    }
  }

}
