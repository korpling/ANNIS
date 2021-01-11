package annis.gui.admin.reflinks;

import annis.gui.AnnisUI;
import annis.gui.components.ExceptionDialog;
import annis.gui.query_references.UrlShortener;
import annis.libgui.Background;
import annis.libgui.Helper;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FutureCallback;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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

  private final class MigrationCallback implements FutureCallback<Integer> {
    private final AnnisUI ui;
    private final Multimap<QueryStatus, URLShortenerDefinition> failedQueries;


    private MigrationCallback(Multimap<QueryStatus, URLShortenerDefinition> failedQueries,
        AnnisUI ui) {
      this.failedQueries = failedQueries;
      this.ui = ui;
    }

    @Override
    public void onSuccess(Integer successfulQueries) {
      appendMessage("Finished to import " + successfulQueries + " queries.", ui);

      // output summary and detailed list of failed queries
      final Collection<URLShortenerDefinition> unknownCorpusQueries =
          failedQueries.get(QueryStatus.UnknownCorpus);

      StringBuilder detailedStatus = new StringBuilder();


      if (!unknownCorpusQueries.isEmpty()) {
        final Map<String, Integer> unknownCorpusCount = new TreeMap<>();
        for (final URLShortenerDefinition q : unknownCorpusQueries) {
          for (final String c : q.getUnknownCorpora()) {
            final int oldCount = unknownCorpusCount.getOrDefault(c, 0);
            unknownCorpusCount.put(c, oldCount + 1);
          }
        }
        final String unknownCorpusCaption = "Unknown corpus (" + unknownCorpusCount.size()
            + " unknown corpora and " + unknownCorpusQueries.size() + " queries)";
        detailedStatus.append(unknownCorpusCaption);
        detailedStatus.append("\n");
        detailedStatus.append(Strings.repeat("=", unknownCorpusCaption.length()));
        detailedStatus.append("\n");

        for (final Map.Entry<String, Integer> e : unknownCorpusCount.entrySet()) {
          detailedStatus.append("Corpus \"" + e.getKey() + "\": " + e.getValue() + " queries");
          detailedStatus.append("\n");
        }
        detailedStatus.append("\n");
      }

      printProblematicQueries("UUID already exists", failedQueries.get(QueryStatus.UUIDExists),
          detailedStatus);
      printProblematicQueries("Count different", failedQueries.get(QueryStatus.CountDiffers),
          detailedStatus);
      printProblematicQueries("Match list different", failedQueries.get(QueryStatus.MatchesDiffer),
          detailedStatus);
      printProblematicQueries("Timeout", failedQueries.get(QueryStatus.Timeout), detailedStatus);
      printProblematicQueries("Other server error", failedQueries.get(QueryStatus.ServerError),
          detailedStatus);
      printProblematicQueries("Empty corpus list", failedQueries.get(QueryStatus.EmptyCorpusList),
          detailedStatus);
      printProblematicQueries("Failed", failedQueries.get(QueryStatus.Failed), detailedStatus);

      final String summaryString = "+ Successful: " + successfulQueries + " from "
          + (successfulQueries + failedQueries.size()) + " +";
      detailedStatus.append(Strings.repeat("+", summaryString.length()));
      detailedStatus.append("\n");
      detailedStatus.append(summaryString);
      detailedStatus.append("\n");
      detailedStatus.append(Strings.repeat("+", summaryString.length()));
      detailedStatus.append("\n");

      appendMessage(detailedStatus.toString(), ui);
    }

    @Override
    public void onFailure(Throwable t) {
      appendMessage("Failed!\n\n" + t.toString(), ui);
    }

    private void printProblematicQueries(final String statusCaption,
        final Collection<URLShortenerDefinition> queries, StringBuilder sb) {
      if (queries != null && !queries.isEmpty()) {
        final String captionWithCount = statusCaption + " (sum: " + queries.size() + ")";
        sb.append(captionWithCount);
        sb.append("\n");
        sb.append(Strings.repeat("=", captionWithCount.length()));
        sb.append("\n\n");

        for (final URLShortenerDefinition q : queries) {
          if (q.getQuery() != null && q.getQuery().getCorpora() != null) {
            sb.append("Corpus: \"" + q.getQuery().getCorpora() + "\"");
            sb.append("\n");
          }
          sb.append("UUID: \"" + q.getUuid() + "\"");
          sb.append("\n");
          if (q.getQuery() != null && q.getQuery().getQuery() != null) {
            sb.append("Query:");
            sb.append("\n");
            sb.append(q.getQuery().getQuery().trim());
            sb.append("\n");
          }
          if (q.getErrorMsg() != null) {
            sb.append("Error: " + q.getErrorMsg());
            sb.append("\n");
          }

          sb.append("-------");
          sb.append("\n");
        }
        sb.append("\n");
      }
    }

  }

  /**
   * 
   */
  private static final long serialVersionUID = -6893786947746535332L;

  private final TextArea txtMessages = new TextArea();
  private final Upload exportedFileUpload = new Upload();
  private final Button btMigrate = new Button("Start migration");

  private File urlShortenerFile;


  @Override
  public void attach() {
    super.attach();

    setSizeFull();


    exportedFileUpload.setCaption("Exported URL shortener entries as CSV file");
    exportedFileUpload.setReceiver(this);
    exportedFileUpload.addFinishedListener(this);
    exportedFileUpload.addFailedListener(this);

    TextField serviceUrl = new TextField("ANNIS service URL");
    TextField serviceUsername = new TextField("Username for ANNIS service");
    PasswordField servicePassword = new PasswordField("Password for ANNIS service");
    CheckBox skipExisting = new CheckBox("Skip existing UUIDs");


    FormLayout formLayout = new FormLayout(exportedFileUpload, serviceUrl, serviceUsername,
        servicePassword, skipExisting);

    txtMessages.setSizeFull();
    txtMessages.setValue("");
    txtMessages.setReadOnly(true);

    VerticalLayout layout = new VerticalLayout(formLayout, btMigrate, txtMessages);
    layout.setSizeFull();
    layout.setMargin(true);
    setContent(layout);
    layout.setExpandRatio(txtMessages, 1.0f);


    btMigrate.setEnabled(false);
    btMigrate.addClickListener(new Button.ClickListener() {


      @Override
      public void buttonClick(ClickEvent event) {
        txtMessages.setValue("");
        if (getUI() instanceof AnnisUI) {
          AnnisUI ui = (AnnisUI) getUI();
          Multimap<QueryStatus, URLShortenerDefinition> failedQueries = HashMultimap.create();
          Background.runWithCallback(() -> {
            try {
              return migrateUrlShortener(serviceUrl.getValue(), serviceUsername.getValue(),
                  servicePassword.getValue(), skipExisting.getValue(), ui, failedQueries);
            } catch (ApiException ex) {
              ExceptionDialog.show(ex, ui);
              return 0;
            }
          }, new MigrationCallback(failedQueries, ui));
        }
      }
    });
  }

  private void appendMessage(String message, UI ui) {

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


  private int migrateUrlShortener(String serviceURL, String username, String password,
      boolean skipExisting, AnnisUI ui, Multimap<QueryStatus, URLShortenerDefinition> failedQueries)
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
    UrlShortener urlShortener = ui.getUrlShortener();


    if (urlShortenerFile != null && urlShortenerFile.isFile()) {
      try (CSVReader csvReader = new CSVReader(new FileReader(urlShortenerFile), '\t')) {
        String[] line;
        while ((line = csvReader.readNext()) != null) {
          if (line.length == 4) {

            // parse URL
            try {
              URLShortenerDefinition q = URLShortenerDefinition.parse(line[3], line[0], line[2]);
              if (q != null) {
                // check if all corpora exist in the new instance
                List<String> corpusNames =
                    q.getQuery() == null || q.getQuery().getCorpora() == null ? new LinkedList<>()
                        : new LinkedList<>(q.getQuery().getCorpora());
                for (String c : corpusNames) {
                  if (!knownCorpora.contains(c)) {
                    q.addUnknownCorpus(c);
                  }
                }

                if (!q.getUnknownCorpora().isEmpty()) {
                  failedQueries.put(QueryStatus.UnknownCorpus, q);
                } else if (corpusNames.isEmpty()) {
                  q.setErrorMsg("Corpus name is empty");
                  failedQueries.put(QueryStatus.Failed, q);
                } else if (urlShortener.unshorten(q.getUuid()).isPresent()) {
                  if (skipExisting) {
                    continue;
                  } else {
                    failedQueries.put(QueryStatus.UUIDExists, q);
                  }
                } else {
                  // check the query
                  try {
                    appendMessage(String.format("UUID {}, testing query {} on corpus {}",
                        q.getUuid(), q.getQuery().getQuery().trim(), q.getQuery().getCorpora()),
                        ui);
                    QueryStatus status = q.test(searchApi, client.build(), searchServiceBaseUrl);

                    // insert URLs into new database
                    URI temporary = null;

                    if (status != QueryStatus.Ok) {
                      failedQueries.put(status, q);
                      // Link the UUID to an error page temporarily, until the issue is fixed.
                      // Remember the original URL, so the temporary URL can just be set
                      // to null to resolve to the original URL when the issue is fixed in ANNIS.
                      temporary =
                          UriComponentsBuilder.newInstance().pathSegment("unsupported-query")
                              .queryParam("url", q.getUri().toASCIIString()).build().toUri();
                      String lineSeparator = System.getProperty("line.separator");

                      StringBuilder sb = new StringBuilder();
                      sb.append("Query Error: " + status + lineSeparator);
                      sb.append("Corpus: \"" + q.getQuery().getCorpora() + "\"" + lineSeparator);
                      sb.append("UUID: \"" + q.getUuid() + "\"" + lineSeparator);
                      sb.append("Query:" + lineSeparator);
                      sb.append(q.getQuery().getQuery().trim() + lineSeparator);
                      sb.append("Error Message: " + q.getErrorMsg());

                      appendMessage(sb.toString(), ui);
                    }
                    urlShortener.migrate(q.getUri(), temporary, "anonymous", q.getUuid(),
                        q.getCreationTime() == null ? new Date() : q.getCreationTime().toDate());

                    if (status == QueryStatus.Ok) {
                      successfulQueries++;
                    }

                  } catch (Throwable ex) {
                    String lineSeparator = System.getProperty("line.separator");
                    StringBuilder sb = new StringBuilder();
                    sb.append("Query Error: " + QueryStatus.Failed + lineSeparator);
                    sb.append("Corpus: \"" + q.getQuery().getCorpora() + "\"" + lineSeparator);
                    sb.append("UUID: \"" + q.getUuid() + "\"" + lineSeparator);
                    sb.append("Query:" + lineSeparator);
                    sb.append(q.getQuery().getQuery().trim() + lineSeparator);
                    sb.append("Error Message: " + ex.getMessage());

                    q.setErrorMsg(ex.getMessage());

                    appendMessage(sb.toString(), ui);
                    failedQueries.put(QueryStatus.Failed, q);
                  }
                }
              }
            } catch (Throwable ex) {

              String lineSeparator = System.getProperty("line.separator");

              String errorMsg = ex.getMessage();
              if (errorMsg == null) {
                errorMsg = ex.getClass().toString();
                ex.printStackTrace();

              }

              StringBuilder sb = new StringBuilder();
              sb.append("Query Error: " + QueryStatus.Failed + lineSeparator);
              sb.append("UUID: \"" + line[0] + "\"" + lineSeparator);
              sb.append("Error Message: " + errorMsg);

              URLShortenerDefinition q =
                  new URLShortenerDefinition(null, URLShortenerDefinition.parseUUID(line[0]), null);
              q.setErrorMsg(errorMsg);
              failedQueries.put(QueryStatus.Failed, q);

              appendMessage(sb.toString(), ui);
            }
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

}
