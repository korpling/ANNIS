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
package annis.gui.components;

import annis.gui.ReportBugWindow;
import annis.gui.SearchUI;
import com.google.common.base.Preconditions;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

/**
 * A dialog that displays the message of an exception and allows to show
 * the stack trace if requested.
 * 
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ExceptionDialog extends Window implements Button.ClickListener
{
  private Panel detailsPanel;
  private Label lblStacktrace;
  private Button btDetails;
  private Button btClose;
  private Button btReportBug;
  
  public ExceptionDialog(Throwable ex)
  {
    this(ex, null);
  }
  
  public ExceptionDialog(Throwable ex, String caption)
  {
    Preconditions.checkNotNull(ex);
    
    VerticalLayout layout = new VerticalLayout();
    setContent(layout);
    layout.setSizeFull();
    
    if(caption == null)
    {
      setCaption("ERROR (" + ex.getClass().getSimpleName() + ")");
    }
    else
    {
      setCaption(caption);
    }
    
    Label lblMessage = new Label(ex.getMessage());
    lblMessage.addStyleName("message-caption");
    layout.addComponent(lblMessage);
    
    HorizontalLayout detailLayout = new HorizontalLayout();
    detailLayout.addStyleName("exception-dlg-details");
    detailLayout.setWidth("100%");
    detailLayout.setHeight("-1px");
    layout.addComponent(detailLayout);
    
    btDetails = new Button("Show Details", this);
    btDetails.setStyleName(BaseTheme.BUTTON_LINK);
    detailLayout.addComponent(btDetails);
    
    btReportBug = new Button("Report Bug", this);
    btReportBug.setStyleName(BaseTheme.BUTTON_LINK);
    btReportBug.setVisible(false);
    UI ui = UI.getCurrent();
    if(ui instanceof SearchUI)
    {
      btReportBug.setVisible(((SearchUI) ui).canReportBugs());
    }
    detailLayout.addComponent(btReportBug);
    detailLayout.setComponentAlignment(btDetails, Alignment.TOP_LEFT);
    detailLayout.setComponentAlignment(btReportBug, Alignment.TOP_RIGHT);
    
    StringBuilder details = new StringBuilder();
    details.append(ex.getLocalizedMessage());
    details.append("\nat\n");
    StackTraceElement[] st = ex.getStackTrace();
    for(int i=0; i < st.length; i++)
    {
      details.append(st[i].toString());
      details.append("\n");
    }
    
    lblStacktrace = new Label(details.toString(), ContentMode.PREFORMATTED);
    detailsPanel = new Panel(lblStacktrace);
    detailsPanel.setSizeFull();
    detailsPanel.setVisible(false);
    lblStacktrace.setSizeUndefined();
    lblStacktrace.setVisible(true);
    layout.addComponent(detailsPanel);
    
    btClose = new Button("OK", this);
    layout.addComponent(btClose);
    
    layout.setComponentAlignment(btClose, Alignment.BOTTOM_CENTER);
    layout.setExpandRatio(detailsPanel, 1.0f);
    layout.setExpandRatio(lblMessage, 0.5f);
  }

  @Override
  public void buttonClick(Button.ClickEvent event)
  {
    if(event.getButton() == btDetails)
    {
      if(detailsPanel.isVisible())
      {
        detailsPanel.setVisible(false);
        btDetails.setCaption("Show Details");
        setHeight("200px");
      }
      else
      {
        detailsPanel.setVisible(true);
        btDetails.setCaption("Hide Details");
        setHeight("500px");
      }
    }
    else if(event.getButton() == btClose)
    {
      this.close();
    }
    else if(event.getButton() == btReportBug)
    {
      this.close();
      UI ui = UI.getCurrent();
      if(ui instanceof SearchUI)
      {
        ((SearchUI) ui).reportBug();
      }
    }
  }
  
  
  public static void show(Throwable ex)
  {
    show(ex, null);
  }
  
  public static void show(Throwable ex, String caption)
  {
    ExceptionDialog dlg = new ExceptionDialog(ex);
    dlg.setClosable(true);
    dlg.setModal(true);
    dlg.setResizable(true);
    dlg.setWidth("500px");
    dlg.setHeight("200px");
    
    UI.getCurrent().addWindow(dlg);
    dlg.center();
  }
}
