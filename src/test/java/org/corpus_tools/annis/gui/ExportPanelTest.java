package org.corpus_tools.annis.gui;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static com.github.mvysny.kaributesting.v8.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.github.mvysny.kaributesting.v8.NotificationsKt;
import com.google.common.collect.Sets;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
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

    // Set the annotation keys
    TextField keysField = _get(panel, TextField.class, spec -> spec.withCaption("Annotation Keys"));
    _setValue(keysField, "pos,lemma,pb");


    // Click on "Perform Export" button, wait until export is finished and download button is
    // enabled
    _click(_get(panel, Button.class, spec -> spec.withCaption("Perform Export")));
    TestHelper.awaitCondition(30, downloadButton::isEnabled);

    // Check that the parameters have been updated
    assertEquals(java.util.Arrays.asList("pos", "lemma", "pb"),
        ui.getQueryState().getExportAnnotationKeys());
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
    assertEquals(5, leftContextExportPanel.getValue());
    assertEquals(5, rightContextExportPanel.getValue());

    ComboBox leftContextSearchOptions =
        _get(searchOptions, ComboBox.class, spec -> spec.withCaption("Left Context"));
    ComboBox rightContextSearchOptions =
        _get(searchOptions, ComboBox.class, spec -> spec.withCaption("Right Context"));
    assertEquals(5, leftContextSearchOptions.getValue());
    assertEquals(5, rightContextSearchOptions.getValue());

    assertEquals(5, ui.getQueryState().getLeftContext());
    assertEquals(5, ui.getQueryState().getRightContext());

    // Change the value of the combo boxes and test that the query state and the search panel
    // options have changed
    _setValue(leftContextExportPanel, 10);
    _setValue(rightContextExportPanel, 20);

    assertEquals(10, ui.getQueryState().getLeftContext());
    assertEquals(20, ui.getQueryState().getRightContext());
    assertEquals(10, leftContextSearchOptions.getValue());
    assertEquals(20, rightContextSearchOptions.getValue());
  }

  @Test
  void testNonExistingSegmentation() throws Exception {

    NotificationsKt.clearNotifications();

    // Prepare query
    ui.getQueryController().setQuery(QueryGenerator.displayed().corpora(Sets.newHashSet("pcc2"))
        .query("tok=\"Feigenblatt\"").build());

    // Click on the "More" button and then "Export"
    PopupButton moreButton = _get(PopupButton.class, spec -> spec.withCaption("More"));
    moreButton.setPopupVisible(true);
    _click(_get(Button.class, spec -> spec.withCaption("Export")));

    // Make sure the Export tab is there
    ExportPanel panel = _get(ExportPanel.class);

    ComboBox exporterSelection = _get(panel, ComboBox.class, spec -> spec.withCaption("Exporter"));
    _setValue(exporterSelection, "CSVExporter");

    TextField paramField = _get(panel, TextField.class, spec -> spec.withCaption("Parameters"));
    _setValue(paramField, "segmentation=nonexisting");

    // Click on "Perform Export" button, wait until export is finished and download button is
    // still disabled, error message should be shown
    _click(_get(panel, Button.class, spec -> spec.withCaption("Perform Export")));

    TestHelper.awaitCondition(10, () -> {
      return !NotificationsKt.getNotifications().isEmpty();
    });

    Notification notification = NotificationsKt.getNotifications().get(0);
    assertEquals("Export failed", notification.getCaption());
  }

}
