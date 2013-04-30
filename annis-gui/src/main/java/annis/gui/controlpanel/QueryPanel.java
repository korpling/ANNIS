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

import annis.libgui.Helper;
import annis.gui.HistoryPanel;
import annis.gui.QueryController;
import annis.gui.beans.HistoryEntry;
import annis.gui.components.VirtualKeyboard;
import annis.gui.model.Query;
import annis.libgui.InstanceConfig;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.server.ClassResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.LoggerFactory;
import org.vaadin.hene.popupbutton.PopupButton;

/**
 *
 * @author thomas
 */
public class QueryPanel extends GridLayout implements TextChangeListener,
  ValueChangeListener
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(QueryPanel.class);
  
  public static final int MAX_HISTORY_MENU_ITEMS = 5;
  
  // the view name
  public static final String NAME = "query";
  
  private TextArea txtQuery;
  private Label lblStatus;
  private Button btShowResult;
  private PopupButton btHistory;
  private ListSelect lstHistory;
  private QueryController controller;
  private ProgressIndicator piCount;
  private String lastPublicStatus;
  private List<HistoryEntry> history;
  private Window historyWindow;
  
  public QueryPanel(final QueryController controller, InstanceConfig instanceConfig)
  {
    super(2,3);
    this.controller = controller;
    this.lastPublicStatus = "Ok";
    this.history = new LinkedList<HistoryEntry>();
    
    setSpacing(true);
    setMargin(true);

    addComponent(new Label("AnnisQL:"), 0, 0);
    addComponent(new Label("Status:"), 0, 2);

    setRowExpandRatio(0, 1.0f);
    setColumnExpandRatio(0, 0.2f);
    setColumnExpandRatio(1, 0.8f);

    txtQuery = new TextArea();
    txtQuery.addStyleName("query");
    txtQuery.addStyleName("corpus-font-force");
    txtQuery.addStyleName("keyboardInput");
    txtQuery.setWidth("100%");
    txtQuery.setHeight(10f, Unit.EM);
    txtQuery.setTextChangeTimeout(1000);
    txtQuery.addTextChangeListener((TextChangeListener) this);
    
    addComponent(txtQuery, 1, 0);
    
    final VirtualKeyboard virtualKeyboard;
    if(instanceConfig.getKeyboardLayout() == null)
    {
      virtualKeyboard = null;
    }
    else
    {
      virtualKeyboard = new VirtualKeyboard();
      virtualKeyboard.setKeyboardLayout(instanceConfig.getKeyboardLayout());
      virtualKeyboard.extend(txtQuery);
    }
    VerticalLayout panelStatusLayout = new VerticalLayout();
    panelStatusLayout.setHeight("-1px");
    panelStatusLayout.setWidth(100f, Unit.PERCENTAGE);
    
    
    lblStatus = new Label();
    lblStatus.setContentMode(ContentMode.HTML);
    lblStatus.setValue(this.lastPublicStatus);
    lblStatus.setWidth("100%");
    lblStatus.setHeight(3.5f, Unit.EM);
    lblStatus.addStyleName("border-layout");

    panelStatusLayout.addComponent(lblStatus);

    addComponent(panelStatusLayout, 1, 2);

    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setWidth("100%");
    addComponent(buttonLayout, 1, 1);

    piCount = new ProgressIndicator();
    piCount.setIndeterminate(true);
    piCount.setEnabled(false);
    piCount.setVisible(false);
    piCount.setPollingInterval(60000);
    panelStatusLayout.addComponent(piCount);


    btShowResult = new Button("Show Result");
    btShowResult.setWidth("100%");
    btShowResult.addClickListener(new ShowResultClickListener());
    btShowResult.setDescription("<strong>Show Result</strong><br />Ctrl + Enter");
    btShowResult.setClickShortcut(KeyCode.ENTER, ModifierKey.CTRL);
    btShowResult.setDisableOnClick(true);

    buttonLayout.addComponent(btShowResult);

    VerticalLayout historyListLayout = new VerticalLayout();
    historyListLayout.setSizeUndefined();
    
    lstHistory = new ListSelect();
    lstHistory.setWidth("200px");
    lstHistory.setNullSelectionAllowed(false);
    lstHistory.setValue(null);
    lstHistory.addValueChangeListener((ValueChangeListener) this);
    lstHistory.setImmediate(true);
    
    Button btShowMoreHistory = new Button("Show more details", new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        if(historyWindow == null)
        {
          historyWindow = new Window("History");
          historyWindow.setModal(false);
          historyWindow.setWidth("400px");
          historyWindow.setHeight("250px");
        }
        historyWindow.setContent(new HistoryPanel(history, controller));
        
        if(UI.getCurrent().getWindows().contains(historyWindow))
        {
          historyWindow.bringToFront();
        }
        else
        {          
          UI.getCurrent().addWindow(historyWindow);
        }
      }
    });
    btShowMoreHistory.setWidth("100%");
    
    historyListLayout.addComponent(lstHistory);
    historyListLayout.addComponent(btShowMoreHistory);
    
    historyListLayout.setExpandRatio(lstHistory, 1.0f);
    historyListLayout.setExpandRatio(btShowMoreHistory, 0.0f);
    
    btHistory = new PopupButton("History");
    btHistory.setContent(historyListLayout);
    btHistory.setDescription("<strong>Show History</strong><br />"
      + "Either use the short overview (arrow down) or click on the button "
      + "for the extended view.");
    buttonLayout.addComponent(btHistory);
   
    if(virtualKeyboard != null)
    {
      Button btShowKeyboard = new Button();
      btShowKeyboard.setDescription("Click to show a virtual keyboard");
      btShowKeyboard.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
      btShowKeyboard.setIcon(new ClassResource(VirtualKeyboard.class, "keyboard.png"));
      btShowKeyboard.addClickListener(new ShowKeyboardClickListener(virtualKeyboard));
      buttonLayout.addComponent(btShowKeyboard);
    }
    buttonLayout.setExpandRatio(btShowResult, 1.0f);
    

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
      AsyncWebResource annisResource = Helper.getAnnisAsyncWebResource();
      Future<String> future = annisResource.path("query").path("check").queryParam("q", query)
        .get(String.class);
      
      // wait for maximal one seconds
      
      try
      {
        String result = future.get(1, TimeUnit.SECONDS);
        if ("ok".equalsIgnoreCase(result))
        {
          lblStatus.setValue(lastPublicStatus);
        }
        else
        {
          lblStatus.setValue(result);
        }
      }
      catch (InterruptedException ex)
      {
        log.warn(null, ex);
      }
      catch (ExecutionException ex)
      {
        // ok, there was some serios error
        log.error(null, ex);
      }
      catch (TimeoutException ex)
      {
        lblStatus.setValue("Validation of query took too long.");
      }
      
    }
    catch(UniformInterfaceException ex)
    {
      if(ex.getResponse().getStatus() == 400)
      {
        lblStatus.setValue(ex.getResponse().getEntity(String.class));
      }
      else
      {
        log.error(
          "Exception when communicating with service", ex);
        Notification.show("Exception when communicating with service: " + ex.getMessage(),
          Notification.Type.TRAY_NOTIFICATION);
      }
    }
    catch(ClientHandlerException ex)
    {
      log.error(
          "Could not connect to web service", ex);
        Notification.show("Could not connect to web service: " + ex.getMessage(),
          Notification.Type.TRAY_NOTIFICATION);
    }
  }
  
  @Override
  public void valueChange(ValueChangeEvent event)
  {
    btHistory.setPopupVisible(false);
    HistoryEntry e = (HistoryEntry) event.getProperty().getValue();
    if(controller != null && e != null)
    {
      controller.setQuery(new Query(e.getQuery(), e.getCorpora()));
    }
  }

  public class ShowResultClickListener implements Button.ClickListener
  {

    @Override
    public void buttonClick(ClickEvent event)
    {
      if(controller != null)
      {
        controller.setQuery((txtQuery.getValue()));
        controller.executeQuery();        
      }
    }
  }

  public void setCountIndicatorEnabled(boolean enabled)
  {
    if(piCount != null && btShowResult != null && lblStatus != null)
    {
      lblStatus.setVisible(!enabled);
      piCount.setVisible(enabled);
      piCount.setEnabled(enabled);
      
      btShowResult.setEnabled(!enabled);      
    }
  }

  public void setStatus(String status)
  {
    if(lblStatus != null)
    {
      lblStatus.setValue(status);
      lastPublicStatus = status;
    }
  }

  private static class ShowKeyboardClickListener implements ClickListener
  {

    private final VirtualKeyboard virtualKeyboard;

    public ShowKeyboardClickListener(VirtualKeyboard virtualKeyboard)
    {
      this.virtualKeyboard = virtualKeyboard;
    }

    @Override
    public void buttonClick(ClickEvent event)
    {
      virtualKeyboard.show();
    }
  }
}
