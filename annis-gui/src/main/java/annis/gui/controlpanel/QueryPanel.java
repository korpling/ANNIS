/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.controlpanel;

import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.exceptions.AnnisServiceFactoryException;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class QueryPanel extends Panel implements TextChangeListener
{
  private TextField txtQuery;
  private Label lblStatus;
  private Button btShowResult;
  private Button btHistory;
  
  public QueryPanel()
  {
    setSizeFull();
    
    GridLayout layout = new GridLayout(2, 3);
    setContent(layout);
    layout.setSizeFull();
    layout.setSpacing(true);
    layout.setMargin(true);
    
    layout.addComponent(new Label("AnnisQL:"), 0, 0);    
    layout.addComponent(new Label("Status:"), 0, 2);
    
    layout.setRowExpandRatio(0, 1.0f);
    layout.setColumnExpandRatio(0, 0.2f);
    layout.setColumnExpandRatio(1, 0.8f);
    
    txtQuery = new TextField();
    txtQuery.setSizeFull();
    txtQuery.setTextChangeTimeout(1000);
    layout.addComponent(txtQuery, 1, 0);
    
    Panel panelStatus = new Panel();
    panelStatus.setWidth(100f, UNITS_PERCENTAGE);
    panelStatus.setHeight(3.5f, UNITS_EM);
    ((VerticalLayout) panelStatus.getContent()).setMargin(false);
    ((VerticalLayout) panelStatus.getContent()).setSpacing(false);
    
    lblStatus = new Label();
    lblStatus.setContentMode(Label.CONTENT_PREFORMATTED);
    lblStatus.setValue("Ok");
    lblStatus.setSizeFull();
    
    panelStatus.addComponent(lblStatus);
    
    layout.addComponent(panelStatus, 1, 2);
    
    setScrollable(true);
    
    
    Panel buttonPanel = new Panel();
    HorizontalLayout buttonPanelLayout = new HorizontalLayout();
    buttonPanel.setContent(buttonPanelLayout);
    buttonPanelLayout.setWidth(100f, UNITS_PERCENTAGE);
    layout.addComponent(buttonPanel, 1, 1);
    
    btShowResult = new Button("Show Result");
    btShowResult.setWidth(100f, UNITS_PERCENTAGE);
    buttonPanel.addComponent(btShowResult);
    
    btHistory = new Button("History");
    btHistory.setWidth(100f, UNITS_PERCENTAGE);
    buttonPanel.addComponent(btHistory);
  }

  @Override
  public void attach()
  {
    super.attach();
    
    txtQuery.addListener(this);
  }
  
  
  
  public void setQuery(String query)
  {
    if(txtQuery != null)
    {
      txtQuery.setValue(query);
    }
  }

  @Override
  public void textChange(TextChangeEvent event)
  {
    // validate query
    try
    {
      AnnisService service = AnnisServiceFactory.getClient(getApplication().getProperty("AnnisRemoteService.URL"));
      if(service.isValidQuery(event.getText()))
      {
        lblStatus.setValue("Ok");
      }
    }
    catch(AnnisQLSyntaxException ex)
    {
      lblStatus.setValue(ex.getMessage());
    }
    catch(AnnisQLSemanticsException ex)
    {
      lblStatus.setValue(ex.getMessage());
    }
    catch(AnnisServiceFactoryException ex)
    {
      Logger.getLogger(QueryPanel.class.getName()).log(Level.SEVERE, "Could not connect to service", ex);
      getWindow().showNotification("Could not connect to service: " + ex.getMessage(), 
        Notification.TYPE_TRAY_NOTIFICATION);
    }    catch(RemoteException ex)
    {
      Logger.getLogger(QueryPanel.class.getName()).log(Level.SEVERE,
        "Remote exception when communicating with service", ex);
      getWindow().showNotification("Remote exception when communicating with service: " + ex.getMessage(), 
        Notification.TYPE_TRAY_NOTIFICATION);
    }
  }
}
