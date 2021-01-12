package annis.gui.admin.reflinks;

import annis.gui.AnnisUI;
import annis.gui.components.ExceptionDialog;
import annis.gui.query_references.UrlShortener;
import annis.libgui.Background;
import annis.libgui.Helper;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import org.springframework.web.util.UriComponentsBuilder;

public class MigrationPanel extends Panel
    implements Upload.Receiver, Upload.FinishedListener, Upload.FailedListener {

  private static final int TEXTFIELD_WIDTH = 30;

  private static final String ERROR_MESSAGE_PREFIX = "Error Message: ";

  static final String QUERY_PREFIX = "Query:";

  static final String UUID_PREFIX = "UUID: \"";

  private static final String QUERY_ERROR_PREFIX = "Query Error: ";

  static final String CORPUS_PREFIX = "Corpus: \"";

  private static final long serialVersionUID = -6893786947746535332L;

  private final TextArea txtMessages = new TextArea();
  private final Upload exportedFileUpload = new Upload();
  private final Button btMigrate = new Button("Start migration");

  private File urlShortenerFile;
  AnnisUI ui;


  @Override
  public void attach() {
    super.attach();

    this.ui = (AnnisUI) getUI();
    setSizeFull();


    exportedFileUpload.setCaption("Exported URL shortener entries as CSV file");
    exportedFileUpload.setReceiver(this);
    exportedFileUpload.addFinishedListener(this);
    exportedFileUpload.addFailedListener(this);

    TextField serviceUrl = new TextField("Legacy ANNIS service URL");
    serviceUrl.setWidth(TEXTFIELD_WIDTH, Unit.EM);
    serviceUrl.setPlaceholder("https://example.com/annis3-service");
    TextField serviceUsername = new TextField("Username for legacy ANNIS service");
    serviceUsername.setWidth(TEXTFIELD_WIDTH, Unit.EM);
    PasswordField servicePassword = new PasswordField("Password for legacy ANNIS service");
    servicePassword.setWidth(30, Unit.EM);
    CheckBox skipExisting = new CheckBox("Skip existing UUIDs");


    FormLayout formLayout = new FormLayout(exportedFileUpload, serviceUrl, serviceUsername,
        servicePassword, skipExisting);

    txtMessages.setSizeFull();
    txtMessages.setValue("");
    txtMessages.setReadOnly(true);
    txtMessages.addStyleName("message-output");

    VerticalLayout layout = new VerticalLayout(formLayout, btMigrate, txtMessages);
    layout.setSizeFull();
    layout.setMargin(true);
    setContent(layout);
    layout.setExpandRatio(txtMessages, 1.0f);


    btMigrate.setEnabled(false);
    btMigrate.addClickListener(event -> {
      txtMessages.setValue("");
      Multimap<QueryStatus, URLShortenerDefinition> failedQueries = HashMultimap.create();
      Background.runWithCallback(() -> {
        try {
          return migrateUrlShortener(serviceUrl.getValue(), serviceUsername.getValue(),
              servicePassword.getValue(), skipExisting.getValue(), failedQueries);
        } catch (ApiException ex) {
          ExceptionDialog.show(ex, ui);
          return 0;
        }
      }, new MigrationCallback(this, failedQueries));

    });
  }

  void appendMessage(String message, UI ui) {

    ui.access(() -> {
      String oldVal = txtMessages.getValue();
      if (oldVal == null || oldVal.isEmpty()) {
        txtMessages.setValue(message);
      } else {
        txtMessages.setValue(oldVal + "\n" + message);
      }

      txtMessages.setCursorPosition(txtMessages.getValue().length() - 1);
    });

  }

  private void reportSingleQueryFailureStatus(QueryStatus status, URLShortenerDefinition q) {
    String lineSeparator = "\n";

    StringBuilder sb = new StringBuilder();
    sb.append(QUERY_ERROR_PREFIX + status + lineSeparator);
    sb.append(CORPUS_PREFIX + q.getQuery().getCorpora() + "\"" + lineSeparator);
    sb.append(UUID_PREFIX + q.getUuid() + "\"" + lineSeparator);
    sb.append(QUERY_PREFIX + lineSeparator);
    sb.append(q.getQuery().getQuery().trim() + lineSeparator);
    sb.append(ERROR_MESSAGE_PREFIX + q.getErrorMsg());

    appendMessage(sb.toString(), ui);
  }

  private boolean checkSingleQuery(URLShortenerDefinition q, SearchApi searchApi,
      UrlShortener urlShortener, OkHttpClient client, HttpUrl searchServiceBaseUrl,
      Multimap<QueryStatus, URLShortenerDefinition> failedQueries) {
    // check the query
    try {
      appendMessage(String.format("UUID %s, testing query %s on corpus %s", q.getUuid(),
          q.getQuery().getQuery().trim(), q.getQuery().getCorpora()), ui);
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

        reportSingleQueryFailureStatus(status, q);
      }
      urlShortener.migrate(q.getUri(), temporary, "anonymous", q.getUuid(),
          q.getCreationTime() == null ? new Date() : q.getCreationTime().toDate());

      if (status == QueryStatus.OK) {
        return true;
      }

    } catch (RuntimeException ex) {
      q.setErrorMsg(getErrorMessage(ex));
      failedQueries.put(QueryStatus.FAILED, q);

      reportSingleQueryFailureStatus(QueryStatus.FAILED, q);
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
      reportSingleQueryFailureStatus(QueryStatus.FAILED, q);

      failedQueries.put(QueryStatus.FAILED, q);

    }

    return false;
  }

  private int migrateUrlShortener(String serviceURL, String username, String password,
      boolean skipExisting, Multimap<QueryStatus, URLShortenerDefinition> failedQueries)
      throws ApiException {


    int successfulQueries = 0;

    OkHttpClient.Builder client = new OkHttpClient.Builder();
    HttpUrl parsedServiceUrl = HttpUrl.parse(serviceURL);
    if (username != null && password != null) {
      client.authenticator(new Authenticator() {

        @Override
        public Request authenticate(Route route, Response response) throws IOException {
          String credential = Credentials.basic(username, password);
          return response.request().newBuilder().header("Authorization", credential).build();
        }
      });

      // test authentication and fail early
      HttpUrl testUrl = parsedServiceUrl.newBuilder().addPathSegment("annis")
          .addPathSegment("admin").addPathSegment("is-authenticated").build();
      Request testRequest = new Request.Builder().url(testUrl).build();
      String result = "";
      try {
        result = client.build().newCall(testRequest).execute().body().string();
      } catch (IOException ex) {
        appendMessage(ex.toString(), ui);
      }
      Preconditions.checkArgument("true".equalsIgnoreCase(result), "Authentication failed");
    }

    HttpUrl searchServiceBaseUrl = parsedServiceUrl.newBuilder().addPathSegment("annis")
        .addPathSegment("query").addPathSegment("search").build();

    ApiClient apiClient = Helper.getClient(ui);
    CorporaApi corporaApi = new CorporaApi(apiClient);
    SearchApi searchApi = new SearchApi(apiClient);
    Set<String> knownCorpora = new HashSet<>(corporaApi.listCorpora());


    if (urlShortenerFile != null && urlShortenerFile.isFile()) {
      try (CSVReader csvReader = new CSVReader(new FileReader(urlShortenerFile), '\t')) {
        String[] line;
        while ((line = csvReader.readNext()) != null) {
          if (readUrlShortenerLine(line, skipExisting, knownCorpora,
              searchApi, client.build(), searchServiceBaseUrl, failedQueries)) {
            successfulQueries++;
          }
        }
      } catch (FileNotFoundException ex) {
        appendMessage("File with URL shortener table not found", ui);
      } catch (IOException ex) {
        appendMessage("Migrating URL shortener table failed\n\n" + ex.toString(), ui);
      }
    }


    return successfulQueries;
  }

  @Override
  public OutputStream receiveUpload(String filename, String mimeType) {
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
  public void uploadFinished(FinishedEvent event) {
    btMigrate.setEnabled(true);

    appendMessage("Finished CSV file upload", getUI());
  }

  @Override
  public void uploadFailed(FailedEvent event) {
    appendMessage("Could not upload file: " + event.toString(), getUI());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((btMigrate == null) ? 0 : btMigrate.hashCode());
    result = prime * result + ((exportedFileUpload == null) ? 0 : exportedFileUpload.hashCode());
    result = prime * result + ((txtMessages == null) ? 0 : txtMessages.hashCode());
    result = prime * result + ((ui == null) ? 0 : ui.hashCode());
    result = prime * result + ((urlShortenerFile == null) ? 0 : urlShortenerFile.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    MigrationPanel other = (MigrationPanel) obj;
    if (btMigrate == null) {
      if (other.btMigrate != null)
        return false;
    } else if (!btMigrate.equals(other.btMigrate))
      return false;
    if (exportedFileUpload == null) {
      if (other.exportedFileUpload != null)
        return false;
    } else if (!exportedFileUpload.equals(other.exportedFileUpload))
      return false;
    if (txtMessages == null) {
      if (other.txtMessages != null)
        return false;
    } else if (!txtMessages.equals(other.txtMessages))
      return false;
    if (ui == null) {
      if (other.ui != null)
        return false;
    } else if (!ui.equals(other.ui))
      return false;
    if (urlShortenerFile == null) {
      if (other.urlShortenerFile != null)
        return false;
    } else if (!urlShortenerFile.equals(other.urlShortenerFile))
      return false;
    return true;
  }


}
