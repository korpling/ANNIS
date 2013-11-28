/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.admin;

import annis.libgui.Helper;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.DecimalFormat;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ImportPanel extends Panel 
  implements Upload.ProgressListener, Upload.FinishedListener, Upload.StartedListener,
  Upload.Receiver
{
  
  private static final Logger log = LoggerFactory.getLogger(ImportPanel.class);
  private final VerticalLayout layout;
  private final TextArea txtMessages;
  private final Upload upload;
  private final TextField txtMail;
  private final CheckBox cbOverwrite;
  private final TextField txtAlias;
  
  private File temporaryCorpusFile;
  
  public ImportPanel()
  {
    setSizeFull();
    layout = new VerticalLayout();
    layout.setWidth("100%");
    layout.setHeight("100%");
    setContent(layout);
    
    FormLayout form = new FormLayout();
    layout.addComponent(form);
    
    cbOverwrite = new CheckBox("Overwrite existing corpus");
    form.addComponent(cbOverwrite);
    
    txtMail = new TextField("e-mail address for status updates");
    txtMail.addValidator(new EmailValidator("Must be a valid e-mail address"));
    form.addComponent(txtMail);
    
    txtAlias = new TextField("alias name");
    form.addComponent(txtAlias);
    
    upload = new Upload("", this);
    upload.setButtonCaption("Upload ZIP file with relANNIS corpus");
    upload.setImmediate(true);
    upload.addStartedListener(this);
    upload.addFinishedListener(this);
    
    layout.addComponent(upload);
    
    txtMessages = new TextArea();
    txtMessages.setSizeFull();
    txtMessages.setValue("Ready.");
    txtMessages.setReadOnly(true);
    layout.addComponent(txtMessages);
    
    layout.setExpandRatio(txtMessages, 1.0f);
    
  }
  
  private void appendMessage(String message)
  {
    txtMessages.setReadOnly(false);
    String oldVal = txtMessages.getValue();
    if(oldVal == null || oldVal.isEmpty())
    {
      txtMessages.setValue(message);
    }
    else
    {
      txtMessages.setValue(oldVal + "\n" + message);
    }
    
    txtMessages.setCursorPosition(txtMessages.getValue().length()-1);
    txtMessages.setReadOnly(true);
  }

  @Override
  public void updateProgress(long readBytes, long contentLength)
  {
    float progress = (float) readBytes / (float) contentLength;
    DecimalFormat format = new DecimalFormat("#0.00");
    appendMessage("uploaded " + format.format(progress*100.0f) + "%");
  }

  @Override
  public void uploadFinished(Upload.FinishedEvent event)
  {
    appendMessage("Finished upload, starting import");
    startImport();
  }

  @Override
  public void uploadStarted(Upload.StartedEvent event)
  {
    appendMessage("Started upload");
    event.getUpload().addProgressListener(this);
  }

  @Override
  public OutputStream receiveUpload(String filename, String mimeType)
  {
    try
    {
      temporaryCorpusFile = File.createTempFile(filename, ".zip");
      temporaryCorpusFile.deleteOnExit();
      return new FileOutputStream(temporaryCorpusFile);
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
    return null;
  }
  
  private void startImport()
  {
    WebResource res = Helper.getAnnisWebResource().path("admin").path("import");
    String mail = txtMail.getValue();
    if(txtMail.isValid() && mail != null && !mail.isEmpty())
    {
      res = res.queryParam("statusMail", mail);
    }
    if(cbOverwrite.getValue() == true)
    {
      res = res.queryParam("overwrite", "true");
    }
    String alias = txtAlias.getValue();
    if(alias != null && !alias.isEmpty())
    {
      res = res.queryParam("alias", alias);
    }
    try
    {
      ClientResponse response 
        = res.entity(new FileInputStream(temporaryCorpusFile)).type(
          "application/zip").post(ClientResponse.class);
      
      if(response.getStatus() == Response.Status.ACCEPTED.getStatusCode())
      {
        URI location = response.getLocation();
        appendMessage("Import requested, update URL is " + location);
      }
      else
      {
        appendMessage("Error (response code " + response.getStatus() + "): " + response.getEntity(String.class));
      }
      
    }
    catch (FileNotFoundException ex)
    {
      log.error(null, ex);
    }
    
  }
  
}
