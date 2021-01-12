package annis.gui.admin.reflinks;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.AnnisUI;
import annis.gui.TestHelper;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.nimbusds.jose.util.StandardCharset;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import java.io.IOException;
import java.io.OutputStream;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.io.IOUtils;
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
class MigrationPanelTest {

  @Autowired
  private BeanFactory beanFactory;

  private AnnisUI ui;

  private MigrationPanel panel;

  private MockWebServer legacyServer;

  @BeforeEach
  void setup() throws IOException {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    this.ui = beanFactory.getBean(AnnisUI.class);
    this.legacyServer = new MockWebServer();
    this.legacyServer.start();

    MockVaadin.setup(() -> ui);


    _click(_get(Button.class, spec -> spec.withCaption("Administration")));
    TabSheet tab = _get(TabSheet.class);
    panel = _get(MigrationPanel.class);
    tab.setSelectedTab(panel);
  }

  @AfterEach
  public void tearDown() throws IOException {
    MockVaadin.tearDown();
    this.legacyServer.shutdown();
  }

  @Test
  void testSuccessfulMigration() throws Exception {
    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    // Simulate a manual upload
    Upload upload =
        _get(Upload.class, spec -> spec.withCaption("Exported URL shortener entries as CSV file"));
    OutputStream exampleFile = panel.receiveUpload("url_shortener.csv", "text/plain");
    IOUtils.write(
        "d84ef680-adfb-4923-b2d7-481351d81e95\tanonymous\t2015-11-16 13:19:58.471+00\t/#q=tok&c=pcc2&cl=5&cr=5&s=0&l=10\n",
        exampleFile, StandardCharset.UTF_8);
    exampleFile.close();
    panel.uploadFinished(
        new FinishedEvent(upload, "url_shortener.csv", "text/plain", -1));
    assertTrue(start.isEnabled());

    // Fill out the form
    _get(TextField.class, spec -> spec.withCaption("Legacy ANNIS service URL"))
        .setValue("http://localhost:" + legacyServer.getPort());
    _get(TextField.class, spec -> spec.withCaption("Username for legacy ANNIS service"))
        .setValue("admin");
    _get(TextField.class, spec -> spec.withCaption("Password for legacy ANNIS service"))
        .setValue("test");

    // Start the migration process
    _click(start);
    TestHelper.awaitCondition(10, start::isEnabled);

    // Check that migration was successful
    TextArea messages = _get(panel, TextArea.class);
    assertEquals("\n" + "+++++++++++++++++++++++++\n" + "+ Successful: 1 from 1 +\n"
        + "+++++++++++++++++++++++++", messages.getValue());

  }

}
