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
import net.jcip.annotations.NotThreadSafe;
import okhttp3.mockwebserver.MockResponse;
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
@NotThreadSafe
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

  private void simulateUpload(String fileContent) throws Exception {
    Upload upload =
        _get(Upload.class, spec -> spec.withCaption("Exported URL shortener entries as CSV file"));
    OutputStream exampleFile = panel.receiveUpload("url_shortener.csv", "text/plain");
    IOUtils.write(fileContent, exampleFile, StandardCharset.UTF_8);
    exampleFile.close();
    panel.uploadFinished(new FinishedEvent(upload, "url_shortener.csv", "text/plain", -1));

    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(10, () -> "Finished CSV file upload".equals(messages.getValue()));
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertTrue(start.isEnabled());
  }

  private void fillOutForm() {
    _get(TextField.class, spec -> spec.withCaption("Legacy ANNIS service URL"))
        .setValue("http://localhost:" + legacyServer.getPort());
    _get(TextField.class, spec -> spec.withCaption("Username for legacy ANNIS service"))
        .setValue("admin");
    _get(TextField.class, spec -> spec.withCaption("Password for legacy ANNIS service"))
        .setValue("test");
  }

  @Test
  void testSuccessfulMigration() throws Exception {
    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    simulateUpload(
        "d84ef680-adfb-4923-b2d7-481351d81e95\tanonymous\t2015-11-16 13:19:58.471+00\t/#_q=Ilpvc3NlbiI&c=pcc2&cl=5&cr=5&s=0&l=10\n");
    fillOutForm();

    // Mock the required responses
    legacyServer.enqueue(new MockResponse().setBody("true"));
    legacyServer.enqueue(new MockResponse()
        .setBody("<matchAndDocumentCount><documentCount>1</documentCount>"
            + "  <matchCount>1</matchCount></matchAndDocumentCount>"));
    legacyServer.enqueue(
        new MockResponse().setBody("salt:/pcc2/11299#tok_5"));

    // Start the migration process
    _click(start);

    // Check that migration was successful
    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals(
        "UUID d84ef680-adfb-4923-b2d7-481351d81e95, testing query \"Zossen\" on corpus [pcc2]\n"
            + "Finished to import 1 queries.\n\n" + "++++++++++++++++++++++++\n"
            + "+ Successful: 1 from 1 +\n"
            + "++++++++++++++++++++++++\n",
        messages.getValue());

  }

  @Test
  void testUnknownCorpus() throws Exception {
    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    simulateUpload(
        "d84ef680-adfb-4923-b2d7-481351d81e95\tanonymous\t2015-11-16 13:19:58.471+00\t/#_q=Ilpvc3NlbiI&c=ThisCorpusShouldNeverExist&cl=5&cr=5&s=0&l=10\n");
    fillOutForm();

    // Mock the required responses
    legacyServer.enqueue(new MockResponse().setBody("true"));

    // Start the migration process
    _click(start);

    // Check that the missing corpus was detected
    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals(
        "Finished to import 0 queries.\n\n" + "Unknown corpus (1 unknown corpora and 1 queries)\n"
            + "================================================\n"
            + "Corpus \"ThisCorpusShouldNeverExist\": 1 queries\n\n" + "++++++++++++++++++++++++\n"
            + "+ Successful: 0 from 1 +\n" + "++++++++++++++++++++++++\n",
        messages.getValue());

  }

}
