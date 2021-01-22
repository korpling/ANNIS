package annis.gui.admin.reflinks;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import annis.SingletonBeanStoreRetrievalStrategy;
import annis.gui.AnnisUI;
import annis.gui.TestHelper;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.github.mvysny.kaributesting.v8.NotificationsKt;
import com.nimbusds.jose.util.StandardCharset;
import com.vaadin.spring.internal.UIScopeImpl;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
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

    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    TestHelper.awaitCondition(10, () -> start.isEnabled());
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
        "1763431c-79b2-4576-b532-67e241ce8396\tanonymous\t2015-11-16 13:19:58.471+00\t/#_q=Ilpvc3NlbiI&c=pcc2&cl=5&cr=5&s=0&l=10\n");
    fillOutForm();

    // Mock the required responses
    legacyServer.enqueue(new MockResponse().setBody("true"));
    legacyServer.enqueue(
        new MockResponse().setBody("<matchAndDocumentCount><documentCount>1</documentCount>"
            + "  <matchCount>1</matchCount></matchAndDocumentCount>"));
    legacyServer.enqueue(new MockResponse().setBody("salt:/pcc2/11299#tok_5"));

    // Start the migration process
    assertTrue(start.isEnabled());
    _click(start);

    // Check that migration was successful
    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals(
        "++++++++++++++++++++++++\n" + "+ Successful: 1 from 1 +\n" + "++++++++++++++++++++++++\n",
        messages.getValue());

    // Importing the UUID again should return an error
    legacyServer.enqueue(new MockResponse().setBody("true"));
    _click(start);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals("UUID already exists (sum: 1)\n" + "============================\n" + "\n"
        + "Corpus: \"[pcc2]\"\n" + "UUID: \"1763431c-79b2-4576-b532-67e241ce8396\"\n" + "Query:\n"
        + "\"Zossen\"\n" + "-------\n\n" + "++++++++++++++++++++++++\n"
        + "+ Successful: 0 from 1 +\n" + "++++++++++++++++++++++++\n", messages.getValue());

    // Try again but explicitly skip existing UUIDs
    _get(panel, CheckBox.class).setValue(true);
    legacyServer.enqueue(new MockResponse().setBody("true"));
    _click(start);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals(
        "++++++++++++++++++++++++\n" + "+ Successful: 0 from 0 +\n" + "++++++++++++++++++++++++\n",
        messages.getValue());
  }

  @Test
  void testUnknownCorpus() throws Exception {
    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    simulateUpload(
        "00c086a9-bd99-4661-8d47-3f05431baf62\tanonymous\t2015-11-16 13:19:58.471+00\t/#_q=Ilpvc3NlbiI&c=ThisCorpusShouldNeverExist&cl=5&cr=5&s=0&l=10\n");
    fillOutForm();

    // Mock the required responses
    legacyServer.enqueue(new MockResponse().setBody("true"));

    // Start the migration process
    assertTrue(start.isEnabled());
    _click(start);

    // Check that the missing corpus was detected
    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals("Unknown corpus (1 unknown corpora and 1 queries)\n"
        + "================================================\n"
        + "Corpus \"ThisCorpusShouldNeverExist\": 1 queries\n\n" + "++++++++++++++++++++++++\n"
        + "+ Successful: 0 from 1 +\n" + "++++++++++++++++++++++++\n", messages.getValue());

  }

  @Test
  void testFailingQuery() throws Exception {
    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    simulateUpload(
        "98b5e738-2b24-4bbc-90b4-f6d5fe57416c\tanonymous\t2015-11-16 13:19:58.471+00\t/#_q=Ilpvc3NlbiI&c=pcc2&cl=5&cr=5&s=0&l=10\n");
    fillOutForm();

    // Mock the required responses
    legacyServer.enqueue(new MockResponse().setBody("true"));
    // Send a different count twice, one for the new AQL version and one for the quirks mode
    legacyServer.enqueue(
        new MockResponse().setBody("<matchAndDocumentCount><documentCount>0</documentCount>"
            + "  <matchCount>0</matchCount></matchAndDocumentCount>"));
    legacyServer.enqueue(
        new MockResponse().setBody("<matchAndDocumentCount><documentCount>0</documentCount>"
            + "  <matchCount>0</matchCount></matchAndDocumentCount>"));

    // Start the migration process
    _click(start);

    // Check that the missing corpus was detected
    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals(
        "Count different (sum: 1)\n" + "========================\n" + "\n" + "Corpus: \"[pcc2]\"\n"
            + "UUID: \"98b5e738-2b24-4bbc-90b4-f6d5fe57416c\"\n" + "Query:\n" + "\"Zossen\"\n"
            + "Error: should have been 0 but was 1\n" + "-------\n\n" + "++++++++++++++++++++++++\n"
            + "+ Successful: 0 from 1 +\n" + "++++++++++++++++++++++++\n",
        messages.getValue());
  }

  @Test
  void testInvalidLine() throws Exception {
    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    // This CSV line is missing a column
    simulateUpload(
        "98b5e738-2b24-4bbc-90b4-f6d5fe57416c\t2015-11-16 13:19:58.471+00\t/#_q=Ilpvc3NlbiI&c=pcc2&cl=5&cr=5&s=0&l=10\n");
    fillOutForm();

    // Mock the required responses
    legacyServer.enqueue(new MockResponse().setBody("true"));

    // Start the migration process
    _click(start);

    // Check that the missing column was not counted in the total number of queries
    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals(
        "++++++++++++++++++++++++\n" + "+ Successful: 0 from 0 +\n" + "++++++++++++++++++++++++\n",
        messages.getValue());
  }

  @Test
  void testInvalidUri() throws Exception {
    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    // This URI in the line has invalid characters
    simulateUpload(
        "899e9bef-3a16-4d03-8ed3-70aa1abd125d\tanonymous\t2015-11-16 13:19:58.471+00\thttp:/invalid<host>\n");
    fillOutForm();

    // Mock the required responses
    legacyServer.enqueue(new MockResponse().setBody("true"));

    // Start the migration process
    _click(start);

    // Check that the invalid character was detected and logged
    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals("FAILED (sum: 1)\n" + "===============\n" + "\n" + "Corpus: \"[]\"\n"
        + "UUID: \"899e9bef-3a16-4d03-8ed3-70aa1abd125d\"\n"
        + "Error: Illegal character in path at index 13: http:/invalid<host>\n" + "-------\n\n" + ""
        + "++++++++++++++++++++++++\n" + "+ Successful: 0 from 1 +\n"
        + "++++++++++++++++++++++++\n", messages.getValue());
  }

  @Test
  void testEmptyCorpusName() throws Exception {
    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    // This linked URL is missing the corpus parameter
    simulateUpload(
        "a3cbc7da-1511-473d-abc2-fe531ff5db9a\tanonymous\t2015-11-16 13:19:58.471+00\t/#_q=Ilpvc3NlbiI&cl=5&cr=5&s=0&l=10\n");
    fillOutForm();

    // Mock the required responses
    legacyServer.enqueue(new MockResponse().setBody("true"));

    // Start the migration process
    _click(start);

    // Check that the missing column was not counted in the total number of queries
    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals("FAILED (sum: 1)\n" + "===============\n" + "\n"
        + "UUID: \"a3cbc7da-1511-473d-abc2-fe531ff5db9a\"\n" + "Error: Corpus name is empty\n"
        + "-------\n\n" + "" + "++++++++++++++++++++++++\n" + "+ Successful: 0 from 1 +\n"
        + "++++++++++++++++++++++++\n", messages.getValue());
  }


  @SuppressWarnings("unchecked")
  @Test
  void testInvalidCredentials() throws Exception {
    NotificationsKt.clearNotifications();

    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    // This URI in the line has invalid characters
    simulateUpload(
        "899e9bef-3a16-4d03-8ed3-70aa1abd125d\tanonymous\t2015-11-16 13:19:58.471+00\thttp:/invalid<host>\n");
    fillOutForm();

    // Mock a failing authentication
    legacyServer.enqueue(new MockResponse().setBody("false"));

    // Start the migration process
    _click(start);
    
    // Wait for check to fail
    TestHelper.awaitCondition(10,
        () -> start.isEnabled() && !NotificationsKt.getNotifications().isEmpty());

    // Check that notification is shown
    NotificationsKt.expectNotifications(new kotlin.Pair<String, String>(
        "Authentication failed, please check the provided user name and password", null));

  }

}
