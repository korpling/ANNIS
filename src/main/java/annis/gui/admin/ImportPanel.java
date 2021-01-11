/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.gui.admin;

import annis.gui.LoginListener;
import annis.libgui.Background;
import annis.libgui.Helper;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.ApiResponse;
import org.corpus_tools.annis.JSON;
import org.corpus_tools.annis.api.AdministrationApi;
import org.corpus_tools.annis.api.model.ImportResult;
import org.corpus_tools.annis.api.model.Job;
import org.corpus_tools.annis.api.model.Job.StatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class ImportPanel extends Panel implements Upload.ProgressListener, Upload.FinishedListener,
    Upload.StartedListener, Upload.Receiver, LoginListener {

  public static class ImportRequest {
    String email;

    /**
     * @return the email
     */
    public String getEmail() {
      return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
      this.email = email;
    }
  }

  private class WaitForFinishRunner implements Runnable {
    private final UI ui;
    private final String uuid;

    private int currentMessageIndex = 0;
    private JSON json;

    public WaitForFinishRunner(String uuid, UI ui) {
      this.ui = ui;
      this.uuid = uuid;
      this.json = new JSON();
    }

    private void appendFromBackground(List<String> message) {
      for (String m : message) {
        appendFromBackground(m);
      }
    }

    private void appendFromBackground(final String message) {
      ui.access(() -> appendMessage(message));
    }

    private void outputNewMessages(List<String> allMessages) {
      if (currentMessageIndex < allMessages.size()) {
        final List<String> newMessages =
            allMessages.subList(currentMessageIndex, allMessages.size());

        currentMessageIndex = allMessages.size();

        appendFromBackground(newMessages);

      }
    }

    @Override
    public void run() {
      AdministrationApi api = new AdministrationApi(Helper.getClient(ui));
      // check the overall status
      Job job = null;
      try {
        do {
          job = null;
          ApiResponse<String> response =
              api.getApiClient().execute(api.getJobCall(uuid, null), String.class);


          // If the response type returns an object of type Job, get it here
          int statusCode = response.getStatusCode();
          if (statusCode == HttpStatus.ACCEPTED.value()) {
            job = json.deserialize(response.getData(), Job.class);
            outputNewMessages(job.getMessages());
          } else if (statusCode == HttpStatus.OK.value()) {
            // The last messages are given as array of strings
            String[] finishMessages = json.deserialize(response.getData(), String[].class);
            for (int i = 0; i < finishMessages.length; i++) {
              appendFromBackground(finishMessages[i]);
            }
          } else {
            appendFromBackground("Unknown status code " + response.getStatusCode());
          }

          Thread.sleep(500);
        } while (job != null && job.getStatus() == StatusEnum.RUNNING);

        if (job == null) {
          appendFromBackground("Finished.");
        }
        if (job != null) {
          if (job.getStatus() == StatusEnum.FAILED) {
            appendFromBackground("FAILED.");
          } else {
            appendFromBackground("Unknown status.");
          }
        }

        ui.access(() -> {
          progress.setVisible(false);
          upload.setEnabled(true);
        });

      } catch (InterruptedException ex) {
        log.error(null, ex);
        Thread.currentThread().interrupt();
      } catch (ApiException ex) {
        if (ex.getCode() == HttpStatus.GONE.value()) {
          // Decode the Job object with its included error messages
          job = json.deserialize(ex.getResponseBody(), Job.class);
          outputNewMessages(job.getMessages());
        } else {
          appendFromBackground(
              "Exception while polling for import status: " + ex.getMessage() + "\n"
                  + ex.getResponseBody());
        }
        ui.access(() -> {
          progress.setVisible(false);
          upload.setEnabled(true);
        });
      }

    }

  }

  private static final long serialVersionUID = 2246043360503976290L;
  private static final Logger log = LoggerFactory.getLogger(ImportPanel.class);
  private final VerticalLayout layout;
  private final TextArea txtMessages;
  private final Upload upload;
  private final CheckBox cbOverwrite;
  private final ProgressBar progress;
  private final Label lblProgress;

  private final Button btDetailedLog;

  private File temporaryCorpusFile;

  public ImportPanel() {

    setSizeFull();

    layout = new VerticalLayout();
    layout.setWidth("100%");
    layout.setHeight("100%");
    layout.setMargin(true);

    setContent(layout);

    FormLayout form = new FormLayout();
    layout.addComponent(form);

    cbOverwrite = new CheckBox("Overwrite existing corpus");
    form.addComponent(cbOverwrite);

    HorizontalLayout actionBar = new HorizontalLayout();
    actionBar.setSpacing(true);
    actionBar.setWidth("100%");

    upload = new Upload("", this);
    upload.setButtonCaption("Upload ZIP file with relANNIS corpus and start import");
    upload.addStartedListener(this);
    upload.addFinishedListener(this);
    upload.setEnabled(true);

    actionBar.addComponent(upload);

    progress = new ProgressBar();
    progress.setIndeterminate(true);
    progress.setVisible(false);

    actionBar.addComponent(progress);

    lblProgress = new Label();
    lblProgress.setWidth("100%");

    actionBar.addComponent(lblProgress);

    actionBar.setExpandRatio(lblProgress, 1.0f);
    actionBar.setComponentAlignment(lblProgress, Alignment.MIDDLE_LEFT);
    actionBar.setComponentAlignment(upload, Alignment.MIDDLE_LEFT);
    actionBar.setComponentAlignment(progress, Alignment.MIDDLE_LEFT);

    layout.addComponent(actionBar);

    btDetailedLog = new Button();
    btDetailedLog.setStyleName(ValoTheme.BUTTON_LINK);
    btDetailedLog.addClickListener(event -> setLogVisible(!isLogVisible()));
    layout.addComponent(btDetailedLog);

    txtMessages = new TextArea();
    txtMessages.setSizeFull();
    txtMessages.setValue("");
    txtMessages.setReadOnly(true);
    layout.addComponent(txtMessages);

    layout.setExpandRatio(txtMessages, 1.0f);

    setLogVisible(false);
    appendMessage("Ready.");

  }

  private void appendMessage(String message) {
    lblProgress.setValue(message);

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

  private boolean isLogVisible() {
    return txtMessages.isVisible();
  }

  @Override
  public void onLogin() {
    upload.setEnabled(true);
  }

  @Override
  public void onLogout() {
    upload.setEnabled(false);
  }

  @Override
  public OutputStream receiveUpload(String filename, String mimeType) {
    try {
      temporaryCorpusFile = File.createTempFile(filename, ".zip");
      temporaryCorpusFile.deleteOnExit();
      return new FileOutputStream(temporaryCorpusFile);
    } catch (IOException ex) {
      log.error(null, ex);
    }
    return null;
  }

  private void setLogVisible(boolean visible) {
    txtMessages.setVisible(visible);
    if (visible) {
      btDetailedLog.setCaption("Hide log");
      btDetailedLog.setIcon(VaadinIcons.MINUS_SQUARE_LEFT_O, "minus sign");
      layout.setExpandRatio(btDetailedLog, 0.0f);
    } else {
      btDetailedLog.setCaption("Show log");
      btDetailedLog.setIcon(VaadinIcons.PLUS_SQUARE_LEFT_O, "plus sign");
      layout.setExpandRatio(btDetailedLog, 1.0f);
    }
  }

  private void startImport() {

    AdministrationApi api = new AdministrationApi(Helper.getClient(UI.getCurrent()));
    try {
      ApiResponse<ImportResult> response =
          api.importPostWithHttpInfo(temporaryCorpusFile, cbOverwrite.getValue());


      if (response.getStatusCode() == HttpStatus.ACCEPTED.value()) {
        String uuid = response.getData().getUuid();
        appendMessage("Import requested, update UUID is " + uuid);

        UI ui = UI.getCurrent();
        Background.run(new WaitForFinishRunner(uuid, ui));

      } else {
        upload.setEnabled(true);
        progress.setVisible(false);
        appendMessage("Error (response code " + response.getStatusCode() + ")");
      }
    } catch (ApiException ex) {
      upload.setEnabled(true);
      progress.setVisible(false);
      appendMessage("Error (response code " + ex.getCode() + "): " + ex.getResponseBody());
    }



  }

  public void updateMode(boolean isLoggedIn) {
    upload.setEnabled(isLoggedIn);
  }

  @Override
  public void updateProgress(long readBytes, long contentLength) {
    float ratioComplete = (float) readBytes / (float) contentLength;

    DecimalFormat format = new DecimalFormat("#0.00");
    appendMessage("uploaded " + format.format(ratioComplete * 100.0f) + "%");
  }

  @Override
  public void uploadFinished(Upload.FinishedEvent event) {

    appendMessage("Finished upload, starting import");
    startImport();
  }

  @Override
  public void uploadStarted(Upload.StartedEvent event) {
    upload.setEnabled(false);
    progress.setVisible(true);
    progress.setEnabled(true);
    appendMessage("Started upload");
    event.getUpload().addProgressListener(this);
  }

}
