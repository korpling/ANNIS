/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.slf4j.LoggerFactory;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.UserError;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Version;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

import annis.VersionInfo;
import annis.libgui.Helper;


/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ReportBugWindow extends Window
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(MetaDataPanel.class);

  private FieldGroup form;
  
  private Button btSubmit;
  private Button btCancel;
  private Throwable cause;
  
  public ReportBugWindow(final String bugEMailAddress, final byte[] screenImage, 
    final String imageMimeType, Throwable cause)
  {
    this.cause = cause;
    
    setSizeUndefined();
    setCaption("Report Problem");
          
    ReportFormLayout layout = new ReportFormLayout();
    setContent(layout);
    
    layout.setWidth("100%");
    layout.setHeight("-1px");
    
    setHeight("420px");
    setWidth("750px");
    
    form = new FieldGroup(new BeanItem<>(new BugReport()));
    form.bindMemberFields(layout);
    form.setBuffered(true);
    
    final ReportBugWindow finalThis = this;
    btSubmit = new Button("Submit problem report", new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        try
        {
          form.commit();

          if(sendBugReport(bugEMailAddress, screenImage, imageMimeType))
          {
            Notification.show("Problem report was sent",
              "We will answer your problem report as soon as possible",
              Notification.Type.HUMANIZED_MESSAGE);
          }
          
          UI.getCurrent().removeWindow(finalThis);

        }
        catch (FieldGroup.CommitException ex)
        {
          List<String> errorFields = new LinkedList<>();
          for(Field<?> f : form.getFields())
          {
            if (f instanceof AbstractComponent)
            {
              AbstractComponent c = (AbstractComponent) f;
              if (f.isValid())
              {
                c.setComponentError(null);
              }
              else
              {
                errorFields.add(f.getCaption()); 
                c.setComponentError(new UserError("Validation failed: "));
              }
            }            
          } // for each field
          String message = "Please check the error messages "
            + "(place mouse over red triangle) for the following fields:<br />";
          message = message + StringUtils.join(errorFields, ",<br/>");
          Notification notify = new Notification("Validation failed", 
            message, Notification.Type.WARNING_MESSAGE,
            true);
          notify.show(UI.getCurrent().getPage());
        }
        catch (Exception ex)
        {
          log.error("Could not send bug report", ex);
          Notification.show("Could not send bug report", ex.
            getMessage(),
            Notification.Type.WARNING_MESSAGE);
        }
      }
    });

    btCancel = new Button("Cancel", new Button.ClickListener()
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        form.discard();
        UI.getCurrent().removeWindow(finalThis);
      }
    });
    
    addScreenshotPreview(layout, screenImage, imageMimeType);

    HorizontalLayout buttons = new HorizontalLayout();
    buttons.addComponent(btSubmit);
    buttons.addComponent(btCancel);

    layout.addComponent(buttons);
    
  }
  
  private void addScreenshotPreview(Layout layout, final byte[] rawImage, String mimeType)
  {
    StreamResource res = new StreamResource(
      new ScreenDumpStreamSource(rawImage), "screendump_" + UUID.randomUUID().toString() + ".png"
    );
    res.setMIMEType(mimeType);

    final Image imgScreenshot =
      new Image("Attached screenshot", res);
    imgScreenshot.setAlternateText("Screenshot of the ANNIS browser window, "
      + "no other window or part of the desktop is captured.");
    imgScreenshot.setVisible(false);
    imgScreenshot.setWidth("100%");
    
    Button btShowScreenshot = new Button("Show attached screenshot", new ShowScreenshotClickListener(imgScreenshot));
    btShowScreenshot.addStyleName(BaseTheme.BUTTON_LINK);
    btShowScreenshot.setIcon(FontAwesome.PLUS_SQUARE_O);
    
    layout.addComponent(btShowScreenshot);
    layout.addComponent(imgScreenshot);
  }
  

  private boolean sendBugReport(String bugEMailAddress, 
    byte[] screenImage, String imageMimeType)
  {
    MultiPartEmail mail = new MultiPartEmail();
    try
    {
      // server setup
      mail.setHostName("localhost");

      // content of the mail
      mail.addReplyTo(form.getField("email").getValue().toString(), 
        form.getField("name").getValue().toString());
      mail.setFrom(bugEMailAddress);
      mail.addTo(bugEMailAddress);

      mail.setSubject("[ANNIS BUG] " + form.getField("summary").getValue().
        toString());

      // TODO: add info about version etc.
      StringBuilder sbMsg = new StringBuilder();

      sbMsg.append("Reporter: ").append(form.getField("name").getValue().
        toString()).append(" (").append(form.getField("email").getValue().
        toString()).append(")\n");
      sbMsg.append("Version: ").append(VersionInfo.getVersion()).append(
        "\n");
      sbMsg.append("Vaadin Version: ").append(Version.getFullVersion()).append(
        "\n");
      sbMsg.append("Browser: ").append(Page.getCurrent().getWebBrowser().getBrowserApplication()).append("\n");
      sbMsg.append("URL: ").append(UI.getCurrent().getPage().getLocation().toASCIIString()).append(
        "\n");

      sbMsg.append("\n");

      sbMsg.append(form.getField("description").getValue().toString());
      mail.setMsg(sbMsg.toString());

      if (screenImage != null)
      { 
        mail.attach(new ByteArrayDataSource(screenImage, imageMimeType),
            "screendump.png", "Screenshot of the browser content at time of problem report");
      }
       
      File logfile = new File(VaadinService.getCurrent().getBaseDirectory(), "/WEB-INF/log/annis-gui.log");
      if(logfile.exists() && logfile.isFile() && logfile.canRead())
      {
        mail.attach(new FileDataSource(logfile), "annis-gui.log", "Logfile of the GUI (shared by all users)");
      }
      
      if(cause != null)
      {
        try
        {
          mail.attach(new ByteArrayDataSource(Helper.convertExceptionToMessage(
            cause), "text/plain"), "exception.txt", 
          "Exception that caused the user to report the problem");
        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
      }
    
      mail.send();
      return true;

    }
    catch (EmailException ex)
    {
      Notification.show("E-Mail not configured on server", 
        "If this is no Kickstarter version please ask the administrator (" 
        + bugEMailAddress 
        + ") of this ANNIS-instance for assistance. "
        + "Problem reports are not available for ANNIS Kickstarter", Notification.Type.ERROR_MESSAGE);
      log.error(null,
        ex);
      return false;
    }
  }
  
  
  public static class BugReport
  {
    private String summary = "";
    private String description = 
      "What steps will reproduce the problem?\n"
      + "1.\n"
      + "2.\n"
      + "3.\n"
      + "\n"
      + "What is the expected result?\n"
      + "\n"
      + "\n"
      + "What happens instead\n"
      + "\n"
      + "\n"
      + "Please provide any additional information below.\n";
    private String name = "";
    private String email = "";

    public String getSummary()
    {
      return summary;
    }

    public void setSummary(String summary)
    {
      this.summary = summary;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getEmail()
    {
      return email;
    }

    public void setEmail(String mail)
    {
      this.email = mail;
    }  
  }
  
  public static class ReportFormLayout extends FormLayout
  {
    @PropertyId("summary")
    private TextField txtSummary;

    @PropertyId("description")
    private TextArea txtDescription;

    @PropertyId("name")
    private TextField txtName;

    @PropertyId("email")
    private TextField txtMail;
    
    public ReportFormLayout()
    {
      txtSummary = new TextField("Short Summary");
      txtSummary.setRequired(true);
      txtSummary.setRequiredError("You must provide a summary");
      txtSummary.setWidth("100%");

      txtDescription = new TextArea("Long Description");
      txtDescription.setRequired(true);
      txtDescription.setRequiredError("You must provide a description");
      txtDescription.setRows(10);
      txtDescription.setWidth("100%");

      txtName = new TextField("Your Name");
      txtName.setRequired(true);
      txtName.setRequiredError("You must provide your name");
      txtName.setWidth("100%");

      txtMail = new TextField("Your e-mail adress");
      txtMail.setRequired(true);
      txtMail.setRequiredError("You must provide a valid e-mail adress");
      txtMail.addValidator(new EmailValidator(
        "You must provide a valid e-mail adress"));
      txtMail.setWidth("100%");

      addComponents(txtSummary, txtDescription, txtName, txtMail);

    }

  }

  private static class ScreenDumpStreamSource implements StreamResource.StreamSource
  {

    private final byte[] rawImage;

    public ScreenDumpStreamSource(byte[] rawImage)
    {
      this.rawImage = rawImage;
    }

    @Override
    public InputStream getStream()
    {
      return new ByteArrayInputStream(rawImage);
    }
  }

  private static class ShowScreenshotClickListener implements ClickListener
  {

    private final Image imgScreenshot;

    public ShowScreenshotClickListener(Image imgScreenshot)
    {
      this.imgScreenshot = imgScreenshot;
    }

    @Override
    public void buttonClick(ClickEvent event)
    {
      imgScreenshot.setVisible(!imgScreenshot.isVisible());
      if(imgScreenshot.isVisible())
      {
        event.getButton().setCaption("Hide attached screenshot");
        event.getButton().setIcon(FontAwesome.MINUS_SQUARE_O);
      }
      else
      {
        event.getButton().setCaption("Show attached screenshot");
        event.getButton().setIcon(FontAwesome.PLUS_SQUARE_O);
      }
    }
  }

}
