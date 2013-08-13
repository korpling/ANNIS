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

import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
  private VerticalLayout layout;
  private ProgressBar progress;
  private TextArea txtMessages;
  private Upload upload;
  
  public ImportPanel()
  {
    setSizeFull();
    layout = new VerticalLayout();
    layout.setWidth("100%");
    layout.setHeight("-1px");
    setContent(layout);
    
    upload = new Upload("", this);
    upload.setButtonCaption("Upload ZIP file with relANNIS corpus");
    upload.setImmediate(true);
    upload.addStartedListener(this);
    upload.addFinishedListener(this);
    
    layout.addComponent(upload);
    
    progress = new ProgressBar();
    progress.setWidth("100%");
    layout.addComponent(progress);
    
    txtMessages = new TextArea();
    txtMessages.setSizeFull();
    txtMessages.setValue("Ready.");
    txtMessages.setReadOnly(true);
    layout.addComponent(txtMessages);
    
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
    txtMessages.setReadOnly(true);
  }

  @Override
  public void updateProgress(long readBytes, long contentLength)
  {
    progress.setValue((float) readBytes / (float) contentLength);
  }

  @Override
  public void uploadFinished(Upload.FinishedEvent event)
  {
    appendMessage("Finished upload, starting import");
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
      File tmpFile = File.createTempFile(filename, ".out");
      return new FileOutputStream(tmpFile);
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
    return null;
  }
  
}
