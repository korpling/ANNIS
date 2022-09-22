package org.corpus_tools.annis.gui;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static com.github.mvysny.kaributesting.v8.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.google.common.collect.Sets;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import org.corpus_tools.annis.gui.controlpanel.SearchOptionsPanel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.vaadin.hene.popupbutton.PopupButton;

@SpringBootTest
@ActiveProfiles({"desktop", "test", "headless"})
@WebAppConfiguration
class ExportPanelTest {

  @Autowired
  private BeanFactory beanFactory;

  private AnnisUI ui;

  @BeforeEach
  public void setup() {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    this.ui = beanFactory.getBean(AnnisUI.class);
    MockVaadin.setup(() -> ui);
  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  @Test
  void testCSVExport() throws Exception {
    // Prepare query
    ui.getQueryController().setQuery(QueryGenerator.displayed().corpora(Sets.newHashSet("pcc2"))
        .query("tok=\"Feigenblatt\"").build());
    
    // Click on the "More" button and then "Export"
    PopupButton moreButton = _get(PopupButton.class, spec -> spec.withCaption("More"));
    moreButton.setPopupVisible(true);
    _click(_get(Button.class, spec -> spec.withCaption("Export")));

    // Make sure the Export tab is there
    ExportPanel panel = _get(ExportPanel.class);

    // The download button should be disabled
    Button downloadButton = _get(panel, Button.class, spec -> spec.withCaption("Download"));
    assertFalse(downloadButton.isEnabled());

    // Click on "Perform Export" button, wait until export is finished and download button is
    // enabled
    _click(_get(panel, Button.class, spec -> spec.withCaption("Perform Export")));
    TestHelper.awaitCondition(30, downloadButton::isEnabled);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  void testTextColumnExportContextChange() throws Exception {
    // Prepare query
    ui.getQueryController().setQuery(
        QueryGenerator.displayed().corpora(Sets.newHashSet("pcc2")).query("pos=\"NE\"").build());

    // Click on the "More" button and then "Export"
    PopupButton moreButton = _get(PopupButton.class, spec -> spec.withCaption("More"));
    moreButton.setPopupVisible(true);
    _click(_get(Button.class, spec -> spec.withCaption("Export")));

    // Make sure the Export tab is there
    ExportPanel panel = _get(ExportPanel.class);

    // Change to text column exporter
    ComboBox exporterSelection = _get(panel, ComboBox.class, spec -> spec.withCaption("Exporter"));
    _setValue(exporterSelection, "TextColumnExporter");

    // Get the search options panel that should be in sync
    SearchOptionsPanel searchOptions = _get(SearchOptionsPanel.class);

    // Check that the initial values are correct
    ComboBox leftContextExportPanel =
        _get(panel, ComboBox.class, spec -> spec.withCaption("Left Context"));
    ComboBox rightContextExportPanel =
        _get(panel, ComboBox.class, spec -> spec.withCaption("Right Context"));
    assertEquals(leftContextExportPanel.getValue(), 5);
    assertEquals(rightContextExportPanel.getValue(), 5);

    ComboBox leftContextSearchOptions =
        _get(searchOptions, ComboBox.class, spec -> spec.withCaption("Left Context"));
    ComboBox rightContextSearchOptions =
        _get(searchOptions, ComboBox.class, spec -> spec.withCaption("Right Context"));
    assertEquals(leftContextSearchOptions.getValue(), 5);
    assertEquals(rightContextSearchOptions.getValue(), 5);

    assertEquals(ui.getQueryState().getLeftContext(), 5);
    assertEquals(ui.getQueryState().getRightContext(), 5);

    // Change the value of the combo boxes and test that the query state and the search panel
    // options have changed
    _setValue(leftContextExportPanel, 10);
    _setValue(rightContextExportPanel, 20);

    assertEquals(ui.getQueryState().getLeftContext(), 10);
    assertEquals(ui.getQueryState().getRightContext(), 20);
    assertEquals(leftContextSearchOptions.getValue(), 10);
    assertEquals(rightContextSearchOptions.getValue(), 20);



  }

}
