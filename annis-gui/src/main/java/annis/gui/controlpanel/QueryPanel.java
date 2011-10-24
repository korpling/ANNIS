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

import annis.gui.beans.HistoryEntry;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.gui.Helper;
import annis.gui.HistoryPanel;
import annis.service.AnnisService;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.hene.splitbutton.SplitButton;
import org.vaadin.hene.splitbutton.SplitButton.SplitButtonClickEvent;

/**
 *
 * @author thomas
 */
public class QueryPanel extends Panel implements TextChangeListener,
  ValueChangeListener
{
  
  public static final int MAX_HISTORY_MENU_ITEMS = 5;

  private TextField txtQuery;
  private Label lblStatus;
  private Button btShowResult;
  private SplitButton btHistory;
  private ListSelect lstHistory;
  private ControlPanel controlPanel;
  private ProgressIndicator piCount;
  private HorizontalLayout buttonPanelLayout;
  private GridLayout mainLayout;
  private Panel panelStatus;
  private String lastPublicStatus;
  private List<HistoryEntry> history;
  private Window historyWindow;
  
  public QueryPanel(final ControlPanel controlPanel)
  {
    this.controlPanel = controlPanel;
    this.lastPublicStatus = "Ok";
    this.history = new LinkedList<HistoryEntry>();
    
    setSizeFull();

    mainLayout = new GridLayout(2, 3);
    setContent(mainLayout);
    mainLayout.setSizeFull();
    mainLayout.setSpacing(true);
    mainLayout.setMargin(true);

    mainLayout.addComponent(new Label("AnnisQL:"), 0, 0);
    mainLayout.addComponent(new Label("Status:"), 0, 2);

    mainLayout.setRowExpandRatio(0, 1.0f);
    mainLayout.setColumnExpandRatio(0, 0.2f);
    mainLayout.setColumnExpandRatio(1, 0.8f);

    txtQuery = new TextField();
    txtQuery.setSizeFull();
    txtQuery.setTextChangeTimeout(1000);
    txtQuery.addListener((TextChangeListener) this);

    mainLayout.addComponent(txtQuery, 1, 0);

    panelStatus = new Panel();
    panelStatus.setWidth(100f, UNITS_PERCENTAGE);
    panelStatus.setHeight(3.5f, UNITS_EM);
    ((VerticalLayout) panelStatus.getContent()).setMargin(false);
    ((VerticalLayout) panelStatus.getContent()).setSpacing(false);
    ((VerticalLayout) panelStatus.getContent()).setSizeFull();

    lblStatus = new Label();
    lblStatus.setContentMode(Label.CONTENT_TEXT);
    lblStatus.setValue(this.lastPublicStatus);
    lblStatus.setWidth("100%");
    lblStatus.setHeight("-1px");

    panelStatus.addComponent(lblStatus);

    mainLayout.addComponent(panelStatus, 1, 2);

    setScrollable(true);


    Panel buttonPanel = new Panel();
    buttonPanelLayout = new HorizontalLayout();
    buttonPanel.setContent(buttonPanelLayout);
    buttonPanelLayout.setWidth(100f, UNITS_PERCENTAGE);
    mainLayout.addComponent(buttonPanel, 1, 1);

    piCount = new ProgressIndicator();
    piCount.setIndeterminate(true);
    piCount.setEnabled(false);
    piCount.setVisible(false);
    piCount.setPollingInterval(500);
    panelStatus.addComponent(piCount);


    btShowResult = new Button("Show Result");
    btShowResult.setWidth(100f, UNITS_PERCENTAGE);
    btShowResult.addListener(new ShowResultClickListener());
    btShowResult.setDescription("<strong>Show Result</strong><br />Ctrl + Enter");
    btShowResult.setClickShortcut(KeyCode.ENTER, ModifierKey.CTRL);

    buttonPanel.addComponent(btShowResult);

    lstHistory = new ListSelect();
    lstHistory.setNullSelectionAllowed(false);
    lstHistory.setValue(null);
    lstHistory.addListener((ValueChangeListener) this);
    lstHistory.setImmediate(true);
    
    btHistory = new SplitButton("History");
    btHistory.addStyleName(SplitButton.STYLE_CHAMELEON);
    btHistory.setWidth(100f, UNITS_PERCENTAGE);
    btHistory.setComponent(lstHistory);    
    btHistory.setButtonDescription("<strong>Show History</strong><br />"
      + "Either use the short overview (arrow down) or click on the button "
      + "for the extended view.");
    buttonPanel.addComponent(btHistory);
    
    btHistory.addClickListener(new SplitButton.SplitButtonClickListener() {

      @Override
      public void splitButtonClick(SplitButtonClickEvent event)
      {
        if(historyWindow == null)
        {
          historyWindow = new Window("History");
          historyWindow.setModal(false);
          historyWindow.setWidth("400px");
          historyWindow.setHeight("250px");
        }
        historyWindow.setContent(new HistoryPanel(history, controlPanel));
        
        if(getWindow().getChildWindows().contains(historyWindow))
        {
          historyWindow.bringToFront();
        }
        else
        {          
          getWindow().addWindow(historyWindow);
        }
      }
    });

  }
  
  public void updateShortHistory(List<HistoryEntry> history)
  {
    this.history = history;
    
    lstHistory.removeAllItems();
    
    int counter = 0;
    
    for(HistoryEntry e : history)
    {
      if(counter >= MAX_HISTORY_MENU_ITEMS)
      {
        break;
      }
      else
      {
        lstHistory.addItem(e);
      }
      counter++;
    }
  }

  public void setQuery(String query)
  {
    if(txtQuery != null)
    {
      txtQuery.setValue(query);
    }
    
    validateQuery(query);
  }

  public String getQuery()
  {
    if(txtQuery != null)
    {
      return (String) txtQuery.getValue();
    }
    return "";
  }

  @Override
  public void textChange(TextChangeEvent event)
  {
    validateQuery(event.getText());
  }

  
  private void validateQuery(String query)
  {
    // validate query
    try
    {
      AnnisService service = Helper.getService(getApplication(), getWindow());
      if(service != null && service.isValidQuery(query))
      {
        lblStatus.setValue(lastPublicStatus);
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
    catch(RemoteException ex)
    {
      Logger.getLogger(QueryPanel.class.getName()).log(Level.SEVERE,
        "Remote exception when communicating with service", ex);
      getWindow().showNotification("Remote exception when communicating with service: " + ex.getMessage(),
        Notification.TYPE_TRAY_NOTIFICATION);
    }
  }

  @Override
  public void valueChange(ValueChangeEvent event)
  {
    btHistory.setPopupVisible(false);
    HistoryEntry e = (HistoryEntry) event.getProperty().getValue();
    if(controlPanel != null & e != null)
    {
      controlPanel.setQuery(e.getQuery(), e.getCorpora());
    }
  }

  public class ShowResultClickListener implements Button.ClickListener
  {

    @Override
    public void buttonClick(ClickEvent event)
    {
      if(controlPanel != null)
      {
        controlPanel.executeQuery();
      }
    }
  }

  public void setCountIndicatorEnabled(boolean enabled)
  {
    if(piCount != null && btShowResult != null)
    {
      lblStatus.setVisible(!enabled);
      piCount.setVisible(enabled);
      piCount.setEnabled(enabled);

    }
  }

  protected void setStatus(String status)
  {
    if(lblStatus != null)
    {
      lblStatus.setValue(status);
      lastPublicStatus = status;
    }
  }
}
