package annis.gui.flatquerybuilder;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertEquals;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.AnnisUI;
import annis.gui.querybuilder.QueryBuilderChooser;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.v7.ui.ComboBox;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@ActiveProfiles("desktop")
@WebAppConfiguration
class FlatQueryBuilderTest {


  @Autowired
  private BeanFactory beanFactory;

  private AnnisUI ui;

  @BeforeEach
  public void setup() {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    this.ui = beanFactory.getBean(AnnisUI.class);
    MockVaadin.setup(() -> this.ui);

  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  @Test
  void flatQueryBuilderLinguisticSequence() throws InterruptedException {
    ui.getQueryState().setSelectedCorpora(Sets.newSet("pcc2"));

    // Click on the query builder button and select the flat query builder
    _click(_get(Button.class, spec -> spec.withCaption("Query<br />Builder")));

    ComboBox queryBuilderChooser =
        _get(_get(QueryBuilderChooser.class), ComboBox.class);
    queryBuilderChooser.select("Word sequences and meta information");

    // Initialize the query builder
    FlatQueryBuilder queryBuilder = _get(FlatQueryBuilder.class);
    List<Button> initButtons =
        _find(queryBuilder, Button.class, spec -> spec.withCaption("Initialize"));
    assertEquals(3, initButtons.size());
    _click(initButtons.get(0));

    // The button caption should have been changed
    List<MenuBar> addButtons = _find(queryBuilder, MenuBar.class);
    assertEquals(3, addButtons.size());

    // Add a single token by clicking on the first "Add" menu entry
    queryBuilder.addLinguisticSequenceBox("PP");

    SearchBox searchBox = _get(queryBuilder, SearchBox.class);
    _get(searchBox, ComboBox.class).select("PP");

    // Create the AQL query
    _click(_get(queryBuilder, Button.class, spec -> spec.withCaption("Create AQL Query")));

    assertEquals("PP=/PP/", ui.getQueryState().getAql().getValue());
  }
}
