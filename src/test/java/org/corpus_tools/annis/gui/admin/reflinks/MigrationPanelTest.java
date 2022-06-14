package org.corpus_tools.annis.gui.admin.reflinks;

import static com.github.mvysny.kaributesting.v8.LocatorJ._click;
import static com.github.mvysny.kaributesting.v8.LocatorJ._get;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import net.jcip.annotations.NotThreadSafe;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.io.IOUtils;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.SingletonBeanStoreRetrievalStrategy;
import org.corpus_tools.annis.gui.TestHelper;
import org.corpus_tools.annis.gui.security.SecurityConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@ActiveProfiles({"desktop", "test", "headless"})
@WebAppConfiguration
@NotThreadSafe
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class MigrationPanelTest {

  private static final String VALID_PARTIAL_REFERENCE_ENTRY =
      "\tanonymous\t2015-11-16 13:19:58.471+00\t/#_q=Ilpvc3NlbiI&c=pcc2&cl=5&cr=5&s=0&l=10\n";

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

  }

  @AfterEach
  public void tearDown() throws IOException {
    MockVaadin.tearDown();
    this.legacyServer.shutdown();
  }

  private void showMigrationPanel() {
    _click(_get(Button.class, spec -> spec.withCaption("Administration")));
    TabSheet tab = _get(TabSheet.class);
    panel = _get(MigrationPanel.class);
    tab.setSelectedTab(panel);
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
    showMigrationPanel();

    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    simulateUpload("1763431c-79b2-4576-b532-67e241ce8396" + VALID_PARTIAL_REFERENCE_ENTRY);
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
    showMigrationPanel();

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
    showMigrationPanel();

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
    showMigrationPanel();

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
    showMigrationPanel();

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
    showMigrationPanel();

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

  @Test
  void testLegacyServiceTimeout() throws Exception {
    showMigrationPanel();

    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    simulateUpload("29298bcc-395d-4e50-876b-f66b7ab7c66b" + VALID_PARTIAL_REFERENCE_ENTRY);
    fillOutForm();

    // Indicate several remote query database timeouts in response
    legacyServer.enqueue(new MockResponse().setBody("true"));
    legacyServer.enqueue(new MockResponse().setBody("Timeout").setResponseCode(504));
    legacyServer.enqueue(new MockResponse().setBody("Timeout").setResponseCode(504));
    legacyServer.enqueue(new MockResponse().setBody("Timeout").setResponseCode(504));
    legacyServer.enqueue(new MockResponse().setBody("Timeout").setResponseCode(504));
    legacyServer.enqueue(new MockResponse().setBody("Timeout").setResponseCode(504));

    // Start the migration process
    _click(start);

    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals("Other server error (sum: 1)\n" + "===========================\n" + "\n"
        + "Corpus: \"[pcc2]\"\n" + "UUID: \"29298bcc-395d-4e50-876b-f66b7ab7c66b\"\n" + "Query:\n"
        + "\"Zossen\"\n" + "Error: java.io.IOException: Timeout in legacy ANNIS service\n"
        + "-------\n\n" + "" + "++++++++++++++++++++++++\n" + "+ Successful: 0 from 1 +\n"
        + "++++++++++++++++++++++++\n", messages.getValue());
  }

  @Test
  void testSemanticError() throws Exception {
    showMigrationPanel();

    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    simulateUpload("ac05ec94-c529-4ae8-af48-63676f092efe\t" + "anonymous\t"
        + "2015-11-16 13:19:58.471+00\t" + "/#q=notanannotation&c=pcc2&cl=5&cr=5&s=0&l=10\n");
    fillOutForm();

    legacyServer.enqueue(new MockResponse().setBody("true"));

    // Send a single "Bad Query" response, which should map to a result of "0"
    legacyServer
        .enqueue(new MockResponse().setBody("Annotation does not exist").setResponseCode(400));

    // Start the migration process
    _click(start);

    TextArea messages = _get(panel, TextArea.class);
    TestHelper.awaitCondition(60, () -> messages.getValue().trim().endsWith("++++"),
        () -> "Migration failed, message output was:\n\n" + messages.getValue());
    assertEquals(
        "++++++++++++++++++++++++\n" + "+ Successful: 1 from 1 +\n" + "++++++++++++++++++++++++\n",
        messages.getValue());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testInvalidCredentials() throws Exception {

    showMigrationPanel();
    NotificationsKt.clearNotifications();


    // Button should be disabled until we upload a file
    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    // This URI in the line has invalid characters
    simulateUpload("cd85bc17-2d93-4850-9b6e-73a557f4d186" + VALID_PARTIAL_REFERENCE_ENTRY);
    fillOutForm();

    // Mock a failing authentication
    legacyServer.enqueue(new MockResponse().setResponseCode(401));
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

  @Test
  void testFallbackToQuirksMode() throws Exception {
    showMigrationPanel();

    Button start = _get(Button.class, spec -> spec.withCaption("Start migration"));
    assertFalse(start.isEnabled());

    // Query for ‎"Wunder" & meta::Genre="Sport" on pcc2 corpus
    // This need to downgrade the query to quirks mode because of the meta-search, but should not
    // fail
    simulateUpload(
        "ff2e2780-410d-4f9b-b781-69656897bd90\tanonymous\t2017-10-04 18:46:56.074+00\t"
            + "/#_q=Ild1bmRlciIgJiBtZXRhOjpHZW5yZT0iU3BvcnQi&c=pcc2");
    fillOutForm();

    // Mock the required responses
    legacyServer.enqueue(new MockResponse().setBody("true"));
    legacyServer.enqueue(
        new MockResponse().setBody("<matchAndDocumentCount><documentCount>1</documentCount>"
            + "  <matchCount>1</matchCount></matchAndDocumentCount>"));
    // At this point the graphANNIS query should fail and the migration would ask for a new count of
    // the
    // legacy server
    legacyServer.enqueue(
        new MockResponse().setBody("<matchAndDocumentCount><documentCount>1</documentCount>"
            + "  <matchCount>1</matchCount></matchAndDocumentCount>"));
    legacyServer.enqueue(new MockResponse().setBody("salt:/pcc2/4282#tok_2"));

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
    
  }

  @Test
  void testLoggedOut() {
    // Test that the test environment provides us with a logged in administrator user first
    Authentication oldAuth = ui.getSecurityContext().getAuthentication();
    Optional<OAuth2User> user = Helper.getUser(ui);
    assertTrue(user.isPresent());
    OAuth2User adminUser = user.get();
    assertTrue(Helper.getUserRoles(adminUser).contains("admin"));

    // Logout the current user
    ui.getSecurityContext().setAuthentication(null);
    assertFalse(Helper.getUser(ui).isPresent());

    // Change to migration tab panel
    showMigrationPanel();
    // Since we are logged out, the panel should be empty
    assertNull(panel.getContent());

    // Login in the admin user again
    ui.getSecurityContext().setAuthentication(oldAuth);
  }

  @Test
  void testNotAnAdminUser() {
    Authentication oldAuth = ui.getSecurityContext().getAuthentication();
    // Change the current user to not have the administration role
    List<String> roles = Arrays.asList();
    Instant issuedAt = Instant.now();
    Instant expiresAt = Instant.now().plus(7l, ChronoUnit.DAYS);

    // Use the secret to sign a new JWT token with admin rights
    String signedToken = JWT.create().withSubject("non-admin")
        .withClaim(SecurityConfiguration.ROLES_CLAIM, roles).withExpiresAt(Date.from(expiresAt))
        .withIssuedAt(Date.from(issuedAt)).sign(Algorithm.HMAC256("whatever-secret"));

    List<? extends GrantedAuthority> grantedAuthorities =
        Arrays.asList(new SimpleGrantedAuthority("justauser"));
    LinkedHashMap<String, Object> claims = new LinkedHashMap<>();
    claims.put("sub", "non-admin");
    OidcIdToken token = new OidcIdToken(signedToken, issuedAt, expiresAt, claims);
    DefaultOidcUser newUser = new DefaultOidcUser(grantedAuthorities, token);
    ui.getSecurityContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(newUser, signedToken, grantedAuthorities));


    // Change to migration tab panel
    showMigrationPanel();
    // Since we are logged out, the panel should be empty
    assertNull(panel.getContent());

    // Login in the admin user again
    ui.getSecurityContext().setAuthentication(oldAuth);
  }

}
