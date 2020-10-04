package annis.gui.it;

import static annis.gui.TestHelper.awaitCondition;
import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static com.github.mvysny.kaributesting.v8.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.AnnisUI;
import annis.gui.components.codemirror.AqlCodeEditor;
import annis.gui.resultview.SingleResultPanel;
import annis.gui.widgets.grid.AnnotationGrid;
import annis.gui.widgets.grid.Row;
import annis.visualizers.component.kwic.KWICComponent;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@ActiveProfiles("desktop")
@WebAppConfiguration
class SearchTest {

    @Autowired
    private BeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
        MockVaadin.setup(() -> beanFactory.getBean(AnnisUI.class));
    }

    @AfterEach
    public void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    void tokenSearchPcc2() throws InterruptedException {
        UI.getCurrent().getNavigator().navigateTo("");

        // Filter for the corpus name in case the corpus list has too many entries and does not show
        // the
        // pcc2 corpus yet
        _setValue(_get(TextField.class, spec -> spec.withPlaceholder("Filter")), "pcc2");

        // Explicitly select the corpus
        @SuppressWarnings("unchecked")
        Grid<String> grid = _get(Grid.class,
            spec -> spec.withId("SearchView-ControlPanel-TabSheet-CorpusListPanel-tblCorpora"));
        grid.getSelectionModel().select("pcc2");

        // Set the query and submit query
        _get(AqlCodeEditor.class).getPropertyDataSource().setValue("tok");
        _click(_get(Button.class, spec -> spec.withCaption("Search")));

        awaitCondition(30, () -> !_find(SingleResultPanel.class).isEmpty());


        // Test that the cell values have the correct token value
        SingleResultPanel secondResult = _find(SingleResultPanel.class).get(0);
        KWICComponent kwicVis = _get(secondResult, KWICComponent.class);
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
    }

}
