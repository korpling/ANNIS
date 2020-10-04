package annis.gui.flatquerybuilder;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._find;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.AnnisUI;
import annis.gui.querybuilder.QueryBuilderChooser;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.v7.ui.CheckBox;
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
  private FlatQueryBuilder queryBuilder;
  @BeforeEach
  public void setup() {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    this.ui = beanFactory.getBean(AnnisUI.class);
    MockVaadin.setup(() -> this.ui);

    ui.getQueryState().setSelectedCorpora(Sets.newSet("pcc2"));

    // Click on the query builder button and select the flat query builder
    _click(_get(Button.class, spec -> spec.withCaption("Query<br />Builder")));

    ComboBox queryBuilderChooser = _get(_get(QueryBuilderChooser.class), ComboBox.class);
    queryBuilderChooser.select("Word sequences and meta information");
    this.queryBuilder = _get(FlatQueryBuilder.class);
  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  private void initQueryBuilder(int buttonIndex) {
    List<Button> initButtons =
        _find(queryBuilder, Button.class, spec -> spec.withCaption("Initialize"));
    assertEquals(3, initButtons.size());
    _click(initButtons.get(buttonIndex));

    // The button caption should have been changed
    List<MenuBar> addButtons = _find(queryBuilder, MenuBar.class);
    assertEquals(3, addButtons.size());
  }

  @Test
  void twoTokenInSentence() throws InterruptedException {

    initQueryBuilder(0);

    // Add a two tokens
    queryBuilder.addLinguisticSequenceBox("tok");
    queryBuilder.addLinguisticSequenceBox("tok");

    List<SearchBox> searchBoxes = _find(queryBuilder, SearchBox.class);
    assertEquals(2, searchBoxes.size());

    _get(searchBoxes.get(0), ComboBox.class).setValue("Feigenblatt");
    _get(searchBoxes.get(1), ComboBox.class).setValue("Die");

    // Add a scope
    queryBuilder.addSpanBox("Sent");
    SpanBox spanBox = _get(queryBuilder, SpanBox.class);
    _get(spanBox, ComboBox.class).setValue("s");

    // Create the AQL query
    _click(_get(queryBuilder, Button.class, spec -> spec.withCaption("Create AQL Query")));

    assertEquals("tok=/Feigenblatt/ & tok=/Die/\n& Sent = \"s\"\n& #1 . #2\n"
        + "& #3_i_#1\n" + "& #3_i_#2",
        ui.getQueryState().getAql().getValue());
  }

  @Test
  void clear() throws InterruptedException {
    initQueryBuilder(1);

    // Clear it and initialize again
    _click(_get(queryBuilder, Button.class, spec -> spec.withCaption("Clear the Query Builder")));
    initQueryBuilder(2);

    // The button caption should have been changed
    List<MenuBar> addButtons = _find(queryBuilder, MenuBar.class);
    assertEquals(3, addButtons.size());
  }

  @Test
  void regularExpressionScope() {
    initQueryBuilder(0);

    // Add a token
    queryBuilder.addLinguisticSequenceBox("tok");
    _get(queryBuilder, SearchBox.class).setValue("Feigenblatt");
    // Add a scope and add a regular expression as value
    queryBuilder.addSpanBox("Sent");
    SpanBox spanBox = _get(queryBuilder, SpanBox.class);
    // Set first to regex mode (so the value is not escaped)
    _get(spanBox, CheckBox.class).setValue(true);
    spanBox.setValue(".*");
    
    // Create the AQL query
    _click(_get(queryBuilder, Button.class, spec -> spec.withCaption("Create AQL Query")));

    assertEquals("tok=/Feigenblatt/\n& Sent = /.*/\n& #2_i_#1",
        ui.getQueryState().getAql().getValue());
  }

  @Test
  void removeBoxes() {
    initQueryBuilder(0);
    // Add a token and two meta data boxes
    queryBuilder.addLinguisticSequenceBox("tok");
    queryBuilder.addMetaBox("Titel");
    queryBuilder.addMetaBox("Genre");

    List<VerticalNode> verticalBoxes = _find(VerticalNode.class);
    assertEquals(1, verticalBoxes.size());

    List<MetaBox> metaBoxes = _find(MetaBox.class);
    assertEquals(2, metaBoxes.size());

    // Remove these boxes again using the "X" button
    _click(_get(metaBoxes.get(0), Button.class, spec -> spec.withCaption("X")));
    _click(_get(metaBoxes.get(1), Button.class, spec -> spec.withCaption("X")));
    List<Button> closeButtons =
        _find(verticalBoxes.get(0), Button.class, spec -> spec.withCaption("X"));
    // There are two buttons, one for the inner search box and one for the enclosing vertical box
    assertEquals(2, closeButtons.size());
    _click(closeButtons.get(1));
    _click(closeButtons.get(0));

    assertEquals(0, _find(VerticalNode.class).size());
    assertEquals(0, _find(MetaBox.class).size());
  }

  @Test
  void unescape() {
    assertEquals("/.**/", queryBuilder.unescape("\\x2F.*\\*\\x2F"));
  }

  @Test
  void addInvalidEdgeOperator() {

    initQueryBuilder(0);

    // Add a two tokens
    queryBuilder.addLinguisticSequenceBox("tok");
    queryBuilder.addLinguisticSequenceBox("tok");

    // This should have generated and edge box between them
    EdgeBox box = _get(EdgeBox.class);
    // Set to valid and known operator
    box.setValue(".");
    assertEquals(".", box.getValue());

    // Set to invalid, the operator should be set to null
    box.setValue("&SF");
    assertNull(box.getValue());

    // Add a valid, but unknown operator
    box.setValue(".30");
    assertEquals(".30", box.getValue());
  }
}
