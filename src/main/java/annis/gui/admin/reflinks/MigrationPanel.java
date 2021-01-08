package annis.gui.admin.reflinks;

import annis.gui.AnnisUI;
import annis.gui.query_references.UrlShortener;
import annis.libgui.Helper;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
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

public class MigrationPanel extends Panel {

  /**
   * 
   */
  private static final long serialVersionUID = -6893786947746535332L;

  private final TextArea txtMessages = new TextArea();



  @Override
  public void attach() {
    super.attach();

    TextField serviceUrl = new TextField("ANNIS service URL");
    TextField serviceUsername = new TextField("Username for ANNIS service");
    TextField servicePassword = new TextField("Password for ANNIS service");
    CheckBox skipExisting = new CheckBox("Skip existing UUIDs");


    FormLayout formLayout =
        new FormLayout(serviceUrl, serviceUsername, servicePassword, skipExisting);

    Button btMigrate = new Button("Start migration");

    txtMessages.setSizeFull();
    txtMessages.setValue("");
    txtMessages.setReadOnly(true);

    VerticalLayout layout = new VerticalLayout(formLayout, btMigrate, txtMessages);
    setContent(layout);
  }

  private void appendMessage(String message) {

    txtMessages.setReadOnly(false);
    String oldVal = txtMessages.getValue();
    if (oldVal == null || oldVal.isEmpty()) {
      txtMessages.setValue(message);
    } else {
      txtMessages.setValue(oldVal + "\n" + message);
    }

    txtMessages.setCursorPosition(txtMessages.getValue().length() - 1);
    txtMessages.setReadOnly(true);
  }

  private int migrateUrlShortener(List<String> paths, String serviceURL, String username,
      String password, boolean skipExisting,
      Multimap<QueryStatus, URLShortenerDefinition> failedQueries) throws ApiException {


    if (paths == null || serviceURL == null || !(UI.getCurrent() instanceof AnnisUI)) {
      return 0;
    }
    AnnisUI ui = (AnnisUI) UI.getCurrent();

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
        appendMessage(ex.toString());
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


    for (String p : paths) {
      File urlShortenerFile = new File(p);
      if (urlShortenerFile.isFile()) {
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
                  corporaApi.listCorpora();
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
                  } else if (urlShortener.unshorten(q.getUuid()) != null) {
                    if (skipExisting) {
                      continue;
                    } else {
                      failedQueries.put(QueryStatus.UUIDExists, q);
                    }
                  } else {
                    // check the query
                    try {
                      appendMessage(String.format("UUID {}, testing query {} on corpus {}",
                          q.getUuid(), q.getQuery().getQuery().trim(), q.getQuery().getCorpora()));
                      QueryStatus status = q.test(searchApi, client.build(), searchServiceBaseUrl);

                      // insert URLs into new database
                      URI temporary = null;

                      if (status != QueryStatus.Ok) {
                        failedQueries.put(status, q);
                        // Link the UUID to an error page temporarily, until the issue is fixed.
                        // Remember the original URL, so the temporary URL can just be set
                        // to null to resolve to the original URL when the issue is fixed in ANNIS.
                        temporary = new HttpUrl.Builder().addPathSegment("unsupported-query")
                            .addQueryParameter("url", q.getUri().toASCIIString()).build().uri();
                        String lineSeparator = System.getProperty("line.separator");

                        StringBuilder sb = new StringBuilder();
                        sb.append("Query Error: " + status + lineSeparator);
                        sb.append("Corpus: \"" + q.getQuery().getCorpora() + "\"" + lineSeparator);
                        sb.append("UUID: \"" + q.getUuid() + "\"" + lineSeparator);
                        sb.append("Query:" + lineSeparator);
                        sb.append(q.getQuery().getQuery().trim() + lineSeparator);
                        sb.append("Error Message: " + q.getErrorMsg());

                        appendMessage(sb.toString());
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

                      appendMessage(sb.toString());
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

                URLShortenerDefinition q = new URLShortenerDefinition(null,
                    URLShortenerDefinition.parseUUID(line[0]), null);
                q.setErrorMsg(errorMsg);
                failedQueries.put(QueryStatus.Failed, q);

                appendMessage(sb.toString());
              }
            }
          }

        } catch (FileNotFoundException ex) {
          appendMessage("File with URL shortener table not found");
        } catch (IOException ex) {
          appendMessage("Migrating URL shortener table failed\n\n" + ex.toString());
        }
      }
    }

    return successfulQueries;
  }

}
