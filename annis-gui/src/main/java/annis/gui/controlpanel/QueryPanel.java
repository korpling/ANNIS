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

import annis.gui.Helper;
import annis.gui.HistoryPanel;
import annis.gui.QueryController;
import annis.gui.beans.HistoryEntry;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.vaadin.hene.popupbutton.PopupButton;

/**
 *
 * @author thomas
 */
public class QueryPanel extends Panel implements TextChangeListener,
  ValueChangeListener, View
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
  private GridLayout mainLayout;
  private Panel panelStatus;
  private String lastPublicStatus;
  private List<HistoryEntry> history;
  private Window historyWindow;
  private String lastQueriedFragment;
  
  public QueryPanel(final QueryController controller)
  {
    this.controller = controller;
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

    txtQuery = new TextArea();
    txtQuery.addStyleName("query");
    txtQuery.setSizeFull();
    txtQuery.setTextChangeTimeout(1000);
    txtQuery.addTextChangeListener((TextChangeListener) this);

    mainLayout.addComponent(txtQuery, 1, 0);

    panelStatus = new Panel();
    panelStatus.setWidth(100f, Unit.PERCENTAGE);
    panelStatus.setHeight(3.5f, Unit.EM);
    VerticalLayout panelStatusLayout = new VerticalLayout();
    panelStatus.setContent(panelStatusLayout);
    panelStatusLayout.setSizeFull();

    lblStatus = new Label();
    lblStatus.setContentMode(ContentMode.HTML);
    lblStatus.setValue(this.lastPublicStatus);
    lblStatus.setWidth("100%");
    lblStatus.setHeight("-1px");

    panelStatusLayout.addComponent(lblStatus);

    mainLayout.addComponent(panelStatus, 1, 2);

    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setWidth("100%");
    mainLayout.addComponent(buttonLayout, 1, 1);

    piCount = new ProgressIndicator();
    piCount.setIndeterminate(true);
    piCount.setEnabled(false);
    piCount.setVisible(false);
    piCount.setPollingInterval(500);
    panelStatusLayout.addComponent(piCount);


    btShowResult = new Button("Show Result");
    btShowResult.setWidth(100f, UNITS_PERCENTAGE);
    btShowResult.addListener(new ShowResultClickListener());
    btShowResult.setDescription("<strong>Show Result</strong><br />Ctrl + Enter");
    btShowResult.setClickShortcut(KeyCode.ENTER, ModifierKey.CTRL);

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
    btHistory.setWidth(100f, Unit.PERCENTAGE);
    btHistory.setContent(historyListLayout);
    btHistory.setDescription("<strong>Show History</strong><br />"
      + "Either use the short overview (arrow down) or click on the button "
      + "for the extended view.");
    buttonLayout.addComponent(btHistory);
    

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
      WebResource annisResource = Helper.getAnnisWebResource();
      String result = annisResource.path("query").path("check").queryParam("q", query)
        .get(String.class);
      if ("ok".equalsIgnoreCase(result))
      {
        lblStatus.setValue(lastPublicStatus);
      }
      else
      {
        lblStatus.setValue(result);
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
  
  public void updateFragment(String aql, 
    Set<String> corpora, int contextLeft, int contextRight, String segmentation,
    int start, int limit)
  {
    List<String> args = Helper.citationFragmentParams(aql, corpora, 
      contextLeft, contextRight, 
      segmentation, start, limit);
      
    // set our fragment
    lastQueriedFragment = StringUtils.join(args, "&");
    UI.getCurrent().getPage().setUriFragment(NAME + "/" + lastQueriedFragment);
    
  }

  @Override
  public void enter(ViewChangeEvent event)
  {
    String parameters = event.getParameters();
    // do nothing if not changed
    if (parameters.equals(lastQueriedFragment))
    {
      return;
    }
    
    // TODO: re-enable the query fragments (vaadin7)

//    Map<String, String> args = Helper.parseFragment(parameters);
//
//    Set<String> corpora = new TreeSet<String>();
//    if (args.containsKey("c"))
//    {
//      String[] corporaSplitted = args.get("c").split("\\s*,\\s*");
//      corpora.addAll(Arrays.asList(corporaSplitted));
//    }
//
//    controlPanel.executeCount(args.get("q"), corpora);
//
//    controlPanel.s
//    showQueryResult(args.get("q"), corpora,
//      Integer.parseInt(args.get("cl")), Integer.parseInt(args.get("cr")),
//      args.get("seg"), Integer.parseInt(args.get("s")),
//      Integer.parseInt(args.get("l")));
  }
  
  @Override
  public void valueChange(ValueChangeEvent event)
  {
    btHistory.setPopupVisible(false);
    HistoryEntry e = (HistoryEntry) event.getProperty().getValue();
    if(controller != null & e != null)
    {
      controller.setQuery(e.getQuery(), e.getCorpora());
    }
  }

  public class ShowResultClickListener implements Button.ClickListener
  {

    @Override
    public void buttonClick(ClickEvent event)
    {
      if(controller != null)
      {
        controller.executeQuery();
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

  public void setStatus(String status)
  {
    if(lblStatus != null)
    {
      lblStatus.setValue(status);
      lastPublicStatus = status;
    }
  }
}
