package org.corpus_tools.annis.gui.admin.reflinks;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.SearchApi;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.Background;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.query_references.UrlShortener;
import org.springframework.web.util.UriComponentsBuilder;

public class MigrationPanel extends Panel
    implements Upload.Receiver, Upload.FinishedListener, Upload.FailedListener {

  private static final int TEXTFIELD_WIDTH = 30;

  static final String QUERY_PREFIX = "Query:";
  static final String UUID_PREFIX = "UUID: \"";
  static final String CORPUS_PREFIX = "Corpus: \"";

  private static final long serialVersionUID = -6893786947746535332L;

  private final TextArea txtMessages = new TextArea();
  private final Upload exportedFileUpload = new Upload();
  final ProgressBar progress = new ProgressBar();
  final TextField emailText = new TextField();
  final Button migrateButton = new Button("Start migration");

  private File urlShortenerFile;
  AnnisUI ui;

  private FormLayout formLayout;

  public MigrationPanel() {
    TextField serviceUrl = new TextField("Legacy ANNIS service URL");
    serviceUrl.setWidth(TEXTFIELD_WIDTH, Unit.EM);
    serviceUrl.setPlaceholder("https://example.com/annis3-service");
    TextField serviceUsername = new TextField("Username for legacy ANNIS service");
    serviceUsername.setWidth(TEXTFIELD_WIDTH, Unit.EM);
    PasswordField servicePassword = new PasswordField("Password for legacy ANNIS service");
    servicePassword.setWidth(30, Unit.EM);
    CheckBox skipExisting = new CheckBox("Skip existing UUIDs");

    migrateButton.addClickListener(event -> {
      txtMessages.setValue("");
      progress.setValue(0.0f);
      progress.setCaption("");
      Multimap<QueryStatus, URLShortenerDefinition> failedQueries = HashMultimap.create();
      String url = serviceUrl.getValue();
      String username = serviceUsername.getValue();
      String password = servicePassword.getValue();
      boolean skip = skipExisting.getValue();
      Background.runWithCallback(() -> {
        try {
          return migrateUrlShortener(url, username, password, skip, failedQueries);
        } catch (ApiException | IOException ex) {
          ExceptionDialog.show(ex, "Migrating URL shortener table failed", ui);
          return 0;
        }
      }, new MigrationCallback(this, failedQueries));

    });
    
    emailText.setCaption("E-Mail for status reports (optional)");
    emailText.setPlaceholder("you@example.com");
    emailText.setWidth(TEXTFIELD_WIDTH, Unit.EM);


    formLayout = new FormLayout(exportedFileUpload, serviceUrl, serviceUsername,
        servicePassword, skipExisting);
  }

  @Override
  public void attach() {
    super.attach();

    this.ui = (AnnisUI) getUI();
    setSizeFull();

    VerticalLayout layout = new VerticalLayout(formLayout, migrateButton, progress, txtMessages);
    layout.setSizeFull();
    layout.setMargin(true);
    setContent(layout);
    layout.setExpandRatio(txtMessages, 1.0f);

    if (ui.getConfig().getMailHost() != null) {
      formLayout.addComponent(emailText);
    }

    exportedFileUpload.setCaption("Exported URL shortener entries as CSV file");
    exportedFileUpload.setReceiver(this);
    exportedFileUpload.addFinishedListener(this);
    exportedFileUpload.addFailedListener(this);


    progress.setValue(0.0f);
    progress.setCaption("");
    progress.setWidthFull();

    txtMessages.setSizeFull();
    txtMessages.setValue("");
    txtMessages.setReadOnly(true);
    txtMessages.addStyleName("message-output");


    migrateButton.setEnabled(false);
    migrateButton.setDisableOnClick(true);

  }

  void setMessageAndScrollToEnd(String message) {
    txtMessages.setValue(message);
    txtMessages.setCursorPosition(txtMessages.getValue().length() - 1);
  }

  private boolean checkSingleQuery(URLShortenerDefinition q, SearchApi searchApi,
      UrlShortener urlShortener, OkHttpClient client, HttpUrl searchServiceBaseUrl,
      Multimap<QueryStatus, URLShortenerDefinition> failedQueries) {
    // check the query
    try {
      ui.access(() -> progress.setCaption(String.format("testing query %s on corpus %s (UUID %s)",
          q.getQuery().getQuery().trim(), q.getQuery().getCorpora(), q.getUuid())));

      QueryStatus status = q.test(searchApi, client, searchServiceBaseUrl);

      // insert URLs into new database
      URI temporary = null;

      if (status != QueryStatus.OK) {
        failedQueries.put(status, q);
        // Link the UUID to an error page temporarily, until the issue is fixed.
        // Remember the original URL, so the temporary URL can just be set
        // to null to resolve to the original URL when the issue is fixed in ANNIS.
        temporary = UriComponentsBuilder.newInstance().pathSegment("unsupported-query")
            .queryParam("url", q.getUri().toASCIIString()).build().toUri();
      }
      urlShortener.migrate(q.getUri(), temporary, "anonymous", q.getUuid(),
          q.getCreationTime() == null ? new Date() : q.getCreationTime().toDate());

      if (status == QueryStatus.OK) {
        return true;
      }

    } catch (RuntimeException ex) {
      q.setErrorMsg(getErrorMessage(ex));
      failedQueries.put(QueryStatus.FAILED, q);
    }

    return false;
  }

  private String getErrorMessage(Exception ex) {
    if (ex == null) {
      return "";
    } else if (ex.getMessage() == null) {
      return ex.getClass().toString();
    } else {
      return ex.getMessage();
    }
  }

  private boolean readUrlShortenerLine(String[] line, boolean skipExisting,
      Set<String> knownCorpora, SearchApi searchApi, OkHttpClient client,
      HttpUrl searchServiceBaseUrl, Multimap<QueryStatus, URLShortenerDefinition> failedQueries) {

    if (line.length != 4) {
      return false;
    }

    // parse URL
    try {
      URLShortenerDefinition q = URLShortenerDefinition.parse(line[3], line[0], line[2]);
      if (q != null) {
        // check if all corpora exist in the new instance
        List<String> corpusNames =
            q.getQuery() == null || q.getQuery().getCorpora() == null ? new LinkedList<>()
                : new LinkedList<>(q.getQuery().getCorpora());
        corpusNames.stream().filter(c -> !knownCorpora.contains(c)).forEach(q::addUnknownCorpus);

        if (!q.getUnknownCorpora().isEmpty()) {
          failedQueries.put(QueryStatus.UNKNOWN_CORPUS, q);
        } else if (corpusNames.isEmpty()) {
          q.setErrorMsg("Corpus name is empty");
          failedQueries.put(QueryStatus.FAILED, q);
        } else if (ui.getUrlShortener().unshorten(q.getUuid()).isPresent()) {
          if (skipExisting) {
            return false;
          } else {
            failedQueries.put(QueryStatus.UUID_EXISTS, q);
          }
        } else {
          return checkSingleQuery(q, searchApi, ui.getUrlShortener(), client, searchServiceBaseUrl,
              failedQueries);
        }
      }
    } catch (RuntimeException | URISyntaxException ex) {
      URLShortenerDefinition q =
          new URLShortenerDefinition(null, URLShortenerDefinition.parseUUID(line[0]), null);
      q.setErrorMsg(getErrorMessage(ex));
      failedQueries.put(QueryStatus.FAILED, q);
    }

    return false;
  }

  private Optional<OkHttpClient> createClient(String serviceURL, String username, String password) {
    OkHttpClient.Builder client = new OkHttpClient.Builder();

    HttpUrl parsedServiceUrl = HttpUrl.parse(serviceURL);
    if (username != null && password != null) {
      client.authenticator(new Authenticator() {

        @Override
        public Request authenticate(Route route, Response response) throws IOException {
          // Only try authentication once
          if (response.priorResponse() == null) {
            String credential = Credentials.basic(username, password);
            return response.request().newBuilder().header("Authorization", credential).build();
          } else {
            return null;
          }
        }
      });

      // test authentication and fail early
      HttpUrl testUrl = parsedServiceUrl.newBuilder().addPathSegment("annis")
          .addPathSegment("admin").addPathSegment("is-authenticated").build();
      Request testRequest =
          new Request.Builder().url(testUrl).build();
      try {
        String result =
            client.build().newCall(testRequest).execute().body().string();
        if (!"true".equalsIgnoreCase(result)) {
          ui.access(() -> Notification
              .show("Authentication failed, please check the provided user name and password",
                  Notification.Type.ERROR_MESSAGE));
          return Optional.empty();
        }
      } catch (IOException ex) {
        ui.access(() -> ExceptionDialog.show(ex, "Could not connect to legacy service", ui));
        return Optional.empty();
      }
    }
    return Optional.of(client.build());
  }

  private int migrateUrlShortener(String serviceURL, String username, String password,
      boolean skipExisting, Multimap<QueryStatus, URLShortenerDefinition> failedQueries)
      throws ApiException, IOException {


    int successfulQueries = 0;

    HttpUrl parsedServiceUrl = HttpUrl.parse(serviceURL);
    HttpUrl searchServiceBaseUrl = parsedServiceUrl.newBuilder().addPathSegment("annis")
        .addPathSegment("query").addPathSegment("search").build();

    ApiClient apiClient = Helper.getClient(ui);
    CorporaApi corporaApi = new CorporaApi(apiClient);
    SearchApi searchApi = new SearchApi(apiClient);
    Set<String> knownCorpora = new HashSet<>(corporaApi.listCorpora());

    Optional<OkHttpClient> client = createClient(serviceURL, username, password);

    if (client.isPresent() && urlShortenerFile != null && urlShortenerFile.isFile()) {
      // Count all lines first so we can calculate the progress
      long numberOfQueries = 0;
      try (Stream<String> stream = Files.lines(urlShortenerFile.toPath(), StandardCharsets.UTF_8)) {
        numberOfQueries = stream.count();
      }
      long processedQueries = 0;
      try (CSVReader csvReader = new CSVReader(new FileReader(urlShortenerFile), '\t')) {
        String[] line;
        while ((line = csvReader.readNext()) != null) {
          if (readUrlShortenerLine(line, skipExisting, knownCorpora,
              searchApi, client.get(), searchServiceBaseUrl, failedQueries)) {
            successfulQueries++;
          }
          processedQueries++;
          float progressValue = numberOfQueries > 0 && processedQueries < numberOfQueries
              ? ((float) processedQueries / (float) numberOfQueries)
              : 1.0f;
          ui.access(() -> progress.setValue(progressValue));
        }
      }
    }

    return successfulQueries;
  }

  @Override
  public OutputStream receiveUpload(String filename, String mimeType) { // NO_UCD (test only)
    try {
      urlShortenerFile = File.createTempFile(filename, "");
      urlShortenerFile.deleteOnExit();
      exportedFileUpload.setButtonCaption(filename + " (Click to change)");
      return new FileOutputStream(urlShortenerFile);
    } catch (IOException ex) {
      ExceptionDialog.show(ex, getUI());
    }
    return null;
  }

  @Override
  public void uploadFinished(FinishedEvent event) { // NO_UCD (test only)
    migrateButton.setEnabled(true);
  }

  @Override
  public void uploadFailed(FailedEvent event) {
    setMessageAndScrollToEnd("Could not upload file: " + event.toString());
  }

  public String getMessages() {
    return txtMessages.getValue();
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

}
