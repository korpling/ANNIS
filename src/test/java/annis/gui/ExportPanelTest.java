package annis.gui;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertFalse;

import annis.SingletonBeanStoreRetrievalStrategy;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.google.common.collect.Sets;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
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
import org.vaadin.hene.popupbutton.PopupButton;

@SpringBootTest
@ActiveProfiles({"desktop", "test"})
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
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
  void testCSVExport() {
    // Prepare query
    ui.getQueryState().setSelectedCorpora(Sets.newHashSet("pcc2"));
    ui.getQueryState().getAql().setValue("tok=\"Feigenblatt\"");
    
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

}
