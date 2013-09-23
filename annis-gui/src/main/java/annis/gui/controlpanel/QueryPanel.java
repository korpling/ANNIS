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

import annis.gui.ExportPanel;
import annis.libgui.Helper;
import annis.gui.HistoryPanel;
import annis.gui.QueryController;
import annis.gui.SearchUI;
import annis.gui.beans.HistoryEntry;
import annis.gui.components.ExceptionDialog;
import annis.gui.components.VirtualKeyboard;
import annis.gui.model.Query;
import annis.gui.querybuilder.QueryBuilderChooser;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.ClassResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TabSheet.Tab;
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
  public static final String OK_STATUS = "Status: Ok";

  private TextArea txtQuery;
  private Label lblStatus;
  private Button btShowResult;
  //private Button btShowResultNewTab;
  private PopupButton btHistory;
  private ListSelect lstHistory;
  private QueryController controller;
  private ProgressBar piCount;
  private String lastPublicStatus;
  private List<HistoryEntry> history;
  private Window historyWindow;
  private PopupButton btMoreActions;

  public QueryPanel(SearchUI ui)
  {
    super(4,5);
    
    this.controller = ui.getQueryController();
    this.lastPublicStatus = OK_STATUS;
    this.history = new LinkedList<HistoryEntry>();

    setSpacing(true);
    setMargin(true);

    setRowExpandRatio(0, 1.0f);
    setColumnExpandRatio(0, 0.0f);
    setColumnExpandRatio(1, 0.1f);
    setColumnExpandRatio(2, 0.0f);
    setColumnExpandRatio(3, 0.0f);

    txtQuery = new TextArea();
    txtQuery.setInputPrompt("Please enter AQL query");
    txtQuery.addStyleName("query");
    txtQuery.addStyleName("corpus-font-force");
    txtQuery.addStyleName("keyboardInput");
    txtQuery.setWidth("100%");
    txtQuery.setHeight(10f, Unit.EM);
    txtQuery.setTextChangeTimeout(1000);
    txtQuery.addTextChangeListener((TextChangeListener) this);

   

    final VirtualKeyboard virtualKeyboard;
    if(ui.getInstanceConfig().getKeyboardLayout() == null)
    {
      virtualKeyboard = null;
    }
    else
    {
      virtualKeyboard = new VirtualKeyboard();
      virtualKeyboard.setKeyboardLayout(ui.getInstanceConfig().getKeyboardLayout());
      virtualKeyboard.extend(txtQuery);
    }

    lblStatus = new Label();
    lblStatus.setContentMode(ContentMode.HTML);
    lblStatus.setValue(this.lastPublicStatus);
    lblStatus.setWidth("100%");
    lblStatus.setHeight(3.5f, Unit.EM);
    lblStatus.addStyleName("border-layout");

    piCount = new ProgressBar();
    piCount.setIndeterminate(true);
    piCount.setEnabled(false);
    piCount.setVisible(false);
    

    btShowResult = new Button("Search");
    btShowResult.setWidth("100%");
    btShowResult.addClickListener(new ShowResultClickListener());
    btShowResult.setDescription("<strong>Show Result</strong><br />Ctrl + Enter");
    btShowResult.setClickShortcut(KeyCode.ENTER, ModifierKey.CTRL);
    btShowResult.setDisableOnClick(true);


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

    Button btShowKeyboard = null;
    if(virtualKeyboard != null)
    {
      btShowKeyboard = new Button();
      btShowKeyboard.setWidth("100%");
      btShowKeyboard.setDescription("Click to show a virtual keyboard");
      btShowKeyboard.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
      btShowKeyboard.addStyleName(ChameleonTheme.BUTTON_SMALL);
      btShowKeyboard.setIcon(new ClassResource(VirtualKeyboard.class, "keyboard.png"));
      btShowKeyboard.addClickListener(new ShowKeyboardClickListener(virtualKeyboard));
    }
    
    Button btShowQueryBuilder = new Button("Query<br />Builder");
    btShowQueryBuilder.setHtmlContentAllowed(true);
    btShowQueryBuilder.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btShowQueryBuilder.addStyleName(ChameleonTheme.BUTTON_ICON_ON_TOP);
    btShowQueryBuilder.setIcon(new ThemeResource("tango-icons/32x32/document-properties.png"));
    btShowQueryBuilder.addClickListener(new ShowQueryBuilderClickListener(ui));
    
    VerticalLayout moreActionsLayout = new VerticalLayout();
    moreActionsLayout.setWidth("250px");
    btMoreActions = new PopupButton("More");
    btMoreActions.setContent(moreActionsLayout);
    
//    btShowResultNewTab = new Button("Search (open in new tab)");
//    btShowResultNewTab.setWidth("100%");
//    btShowResultNewTab.addClickListener(new ShowResultInNewTabClickListener());
//    btShowResultNewTab.setDescription("<strong>Show Result and open result in new tab</strong><br />Ctrl + Shift + Enter");
//    btShowResultNewTab.setDisableOnClick(true);
//    btShowResultNewTab.setClickShortcut(KeyCode.ENTER, ModifierKey.CTRL, ModifierKey.SHIFT);
//    moreActionsLayout.addComponent(btShowResultNewTab);
    
    Button btShowExport = new Button("Export", new ShowExportClickListener(ui));
    btShowExport.setWidth("100%");
    moreActionsLayout.addComponent(btShowExport);
    
    
    /*
     * We use the grid layout for a better rendering efficiency, but this comes
     * with the cost of some complexitiy when defining the positions of the
     * elements in the layout.
     * 
     * This grid hopefully helps a little bit in understanding the "magic"
     * numbers better.
     * 
     * Q: Query text field
     * QB: Button to toggle query builder // TODO
     * KEY: Button to show virtual keyboard
     * SEA: "Search" button
     * MOR: "More actions" button 
     * HIST: "History" button
     * STAT: Text field with the real status
     * 
     *   \  0  |  1  |  2  |  3  
     * --+-----+---+---+---+-----
     * 0 |  Q  |  Q  |  Q  | QB 
     * --+-----+-----+-----+-----
     * 1 |  Q  |  Q  |  Q  | KEY 
     * --+-----+-----+-----+-----
     * 2 | SEA | MOR | HIST|     
     * --+-----+-----+-----+-----
     * 3 | STAT| STAT| STAT| STAT
     */
    addComponent(txtQuery, 0, 0, 2, 1);
    addComponent(lblStatus, 0, 3, 3, 3);
    addComponent(btShowResult, 0, 2);
    addComponent(btMoreActions, 1, 2);
    addComponent(btHistory, 2, 2);
    addComponent(btShowQueryBuilder, 3, 0);
    if(btShowKeyboard != null)
    {
      addComponent(btShowKeyboard, 3, 1);
    }

    // alignment
    setRowExpandRatio(0, 0.0f);
    setRowExpandRatio(1, 1.0f);
    setColumnExpandRatio(0, 1.0f);
    setColumnExpandRatio(1, 0.0f);
    setColumnExpandRatio(2, 0.0f);
    setColumnExpandRatio(3, 0.0f);
    
    //setComponentAlignment(btShowQueryBuilder, Alignment.BOTTOM_CENTER);
    
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
        if(ex.getCause() instanceof UniformInterfaceException)
        {
          UniformInterfaceException cause = (UniformInterfaceException) ex.
            getCause();
          if (cause.getResponse().getStatus() == 400)
          {
            lblStatus.setValue(cause.getResponse().getEntity(String.class));
          }
          else
          {
            log.error(
              "Exception when communicating with service", ex);
            ExceptionDialog.show(ex,
              "Exception when communicating with service.");
          }
        }
       // ok, there was some serios error
        log.error(null, ex);
      }
      catch (TimeoutException ex)
      {
        lblStatus.setValue("Validation of query took too long.");
      }

    }
    catch(ClientHandlerException ex)
    {
      log.error(
          "Could not connect to web service", ex);
        ExceptionDialog.show(ex, "Could not connect to web service");
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
  
//  public class ShowResultInNewTabClickListener implements Button.ClickListener
//  {
//
//    @Override
//    public void buttonClick(ClickEvent event)
//    {
//      if(controller != null)
//      {
//        controller.setQuery((txtQuery.getValue()));
//        controller.executeQuery(false);
//      }
//    }
//  }

  public void setCountIndicatorEnabled(boolean enabled)
  {
    if(piCount != null && btShowResult != null && lblStatus != null)
    {
      if(enabled)
      {
        if(!piCount.isVisible())
        {
          replaceComponent(lblStatus, piCount);
          piCount.setVisible(true);
          piCount.setEnabled(true);
        }
      }
      else
      {
        if(piCount.isVisible())
        {
          replaceComponent(piCount, lblStatus);
          piCount.setVisible(false);
          piCount.setEnabled(false);
        }
      }
      
      btShowResult.setEnabled(!enabled);
//      btShowResultNewTab.setEnabled(!enabled);
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
  
  private class ShowExportClickListener implements ClickListener
  {
    private SearchUI ui;
    private ExportPanel panel;
    private ExportOptionsPanel optionsPanel;
    
    public ShowExportClickListener(SearchUI ui)
    {
      this.ui = ui;
    }
    
    @Override
    public void buttonClick(ClickEvent event)
    {
      if(panel == null)
      {
        panel = new ExportPanel(QueryPanel.this, ui.getControlPanel().getCorpusList(), ui.getQueryController());
      }
      if(optionsPanel == null)
      {
        optionsPanel = new ExportOptionsPanel();
      }
      
      final TabSheet tabSheet = ui.getMainTab();
      Tab tab = tabSheet.getTab(panel);
      
      if(tab == null)
      {
        tab = tabSheet.addTab(panel, "Export");
        tab.setIcon(new ThemeResource("tango-icons/16x16/document-save.png"));
      }
      
      
      tab.setClosable(true);
      tabSheet.setSelectedTab(panel);
      
      btMoreActions.setPopupVisible(false);
    }
    
  }
  
  private static class ShowQueryBuilderClickListener implements ClickListener
  {
    
    private QueryBuilderChooser queryBuilder;
    private SearchUI ui;
    
    public ShowQueryBuilderClickListener(SearchUI ui)
    {
      this.ui = ui;
    }
    
    @Override
    public void buttonClick(ClickEvent event)
    {
      if(queryBuilder == null)
      {
         queryBuilder = new QueryBuilderChooser(ui.getQueryController(), ui, ui.getInstanceConfig());
      }
      final TabSheet tabSheet = ui.getMainTab();
      Tab tab = tabSheet.getTab(queryBuilder);
      
      if(tab == null)
      {
        tab = tabSheet.addTab(queryBuilder, "Query Builder", 
          new ThemeResource("tango-icons/16x16/document-properties.png"));
        
        ui.addAction(new ShortcutListener("^Query builder")
        {
          @Override
          public void handleAction(Object sender, Object target)
          {
            if(queryBuilder != null && tabSheet.getTab(queryBuilder) != null)
            {
              tabSheet.setSelectedTab(queryBuilder);
            }
          }
        });
      }
      
      tab.setClosable(true);
      tabSheet.setSelectedTab(queryBuilder);
    }
    
  }

  public QueryController getQueryController()
  {
    return this.controller;
  }
}
