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
package annis.gui;

import annis.examplequeries.ExampleQuery;
import annis.gui.controlpanel.ControlPanel;
import annis.gui.controlpanel.CorpusListPanel;
import annis.gui.controlpanel.QueryPanel;
import annis.gui.model.Query;
import annis.gui.resultview.ResultViewPanel;
import annis.libgui.Helper;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the auto generated queries.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class ExampleQueriesPanel extends Table
{

  // first column String
  private final String EXAMPLE_QUERY = "example query";

  private transient ExecutorService executor;

  //main ui window
  private SearchUI ui;

  // holds the current examples
  private List<ExampleQuery> examples;

  /**
   * Bean Container for example queries. Key is the corpus name.
   */
  private BeanItemContainer<ExampleQuery> egContainer;

  // gets the
  private final static Logger log = LoggerFactory.
    getLogger(ExampleQueriesPanel.class);

  // reference to the tab which holds this component
  private TabSheet.Tab tab;

  // hold the main window of annis3
  private TabSheet mainTab;

  private static final ThemeResource SEARCH_ICON = new ThemeResource(
    "tango-icons/16x16/system-search.png");

  public ExampleQueriesPanel(String caption, SearchUI ui)
  {
    super(caption);
    this.ui = ui;
    this.mainTab = ui.getTabSheet();

    //
    egContainer = new BeanItemContainer<ExampleQuery>(ExampleQuery.class);
    setContainerDataSource(egContainer);
  }

  /**
   * Sets some layout properties.
   */
  private void setUpTable()
  {

    // expand the table
    setSizeFull();

    // Allow selecting items from the table.
    setSelectable(true);

    // Send changes in selection immediately to server.
    setImmediate(true);

    // set clickhandler for execute example query
    addListener(new ExampleQueryExecuter());

    // set custom style
    addStyleName("example-queries-table");

    // put stripes to the table
    addStyleName(ChameleonTheme.TABLE_STRIPED);

    setWidth(100, Unit.PERCENTAGE);

    // configure columns
    final String COLUMN_OPEN_CORPUS_BROWSER = "open corpus browser";
    addGeneratedColumn(COLUMN_OPEN_CORPUS_BROWSER, new ShowCorpusBrowser());

    final String COLUMN_SHOW_RESULT = "show result";
    addGeneratedColumn(COLUMN_SHOW_RESULT, new ShowResultColumn());

    addGeneratedColumn("exampleQuery", new ColumnGenerator()
    {
      @Override
      public Object generateCell(Table source, Object itemId, Object columnId)
      {
        ExampleQuery eQ = (ExampleQuery) itemId;
        Label l = new Label(eQ.getExampleQuery());
        l.setContentMode(ContentMode.TEXT);
        l.addStyleName("corpus-font-force");
        return l;
      }
    });

    addGeneratedColumn("description", new ColumnGenerator()
    {
      @Override
      public Object generateCell(Table source, Object itemId, Object columnId)
      {
        ExampleQuery eQ = (ExampleQuery) itemId;
        Label l = new Label(eQ.getDescription());
        l.setContentMode(ContentMode.TEXT);
        l.addStyleName("corpus-font-force");
        return l;
      }
    });

    setVisibleColumns(new Object[]
    {
      COLUMN_SHOW_RESULT,
      "exampleQuery",
      "description",
      COLUMN_OPEN_CORPUS_BROWSER
    });


    setColumnWidth(getVisibleColumns()[0], 24);
    setColumnExpandRatio(getVisibleColumns()[1], 0.40f);
    setColumnExpandRatio(getVisibleColumns()[2], 0.40f);

    setColumnHeader(getVisibleColumns()[0], "");
    setColumnHeader(getVisibleColumns()[1], "Example Query");
    setColumnHeader(getVisibleColumns()[2], "Description");
    setColumnHeader(getVisibleColumns()[3], "open corpus browser");
  }

  @Override
  public void attach()
  {
    super.attach();

    setUpTable();

    loadExamplesFromRemote();

    addItems();
  }

  /**
   * Add items if there are any and put the example query tab in the foreground.
   */
  private void addItems()
  {
    if (examples != null && examples.size() > 0)
    {
      egContainer.addAll(examples);
      showTab();
    }
    else
    {
      hideTabSheet();
    }
  }

  /**
   * Shows the tab and put into the foreground, if no query is executed yet.
   */
  private void showTab()
  {
    if (mainTab == null)
    {
      mainTab = ui.getMainTab();
    }

    if (mainTab != null)
    {
      tab = mainTab.getTab(this);
      tab.getComponent().addStyleName("example-queries-tab");
      tab.setEnabled(true);

      if (!(mainTab.getSelectedTab() instanceof ResultViewPanel))
      {
        mainTab.setSelectedTab(tab);
      }

    }
  }

  private void hideTabSheet()
  {
    if (mainTab == null)
    {
      mainTab = ui.getMainTab();
    }

    if (mainTab != null)
    {
      tab = mainTab.getTab(this);

      if (tab != null)
      {
        tab.setEnabled(false);
      }
    }
  }

  private Panel getOpenCorpusPanel(final String corpusName)
  {
    Panel p = new Panel();
    p.addStyleName(ChameleonTheme.PANEL_BORDERLESS);

    final Button btn = new Button(corpusName);
    final HorizontalLayout l = new HorizontalLayout();

    p.setContent(l);
    btn.setStyleName(BaseTheme.BUTTON_LINK);
    btn.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        CorpusListPanel corpusList = ui.getControlPanel().getCorpusList();
        corpusList.initCorpusBrowser(corpusName);
      }
    });

    l.addComponent(btn);

    return p;
  }

  /**
   * Catches click events on the example query column.
   *
   * TODO do not use deprecated stuff
   */
  private class ExampleQueryExecuter implements ItemClickEvent.ItemClickListener
  {

    @Override
    public void itemClick(ItemClickEvent event)
    {
      if (event.getButton() == ItemClickEvent.BUTTON_LEFT)
      {
        String column = (String) event.getPropertyId();
        ControlPanel controlPanel = ui.getControlPanel();
        QueryPanel queryPanel;

        if (controlPanel == null)
        {
          log.error("controlPanel is not initialized");
          return;
        }

        queryPanel = controlPanel.getQueryPanel();
        if (queryPanel == null)
        {
          log.error("queryPanel is not initialized");
          return;
        }

        if (EXAMPLE_QUERY.equals(column))
        {
          Property query = event.getItem().getItemProperty(column);
          queryPanel.setQuery(query.toString());
        }
      }
    }
  }

  /**
   * Loads all available example queries.
   */
  private void loadExamplesFromRemote()
  {
    examples = loadExamplesFromRemote(null);
  }

  /**
   * Loads the available example queries for a specific corpus.
   *
   * @param corpusNames Specifies the corpora example queries are fetched for.
   * If it is null or empty all available example queries are fetched.
   */
  private static List<ExampleQuery> loadExamplesFromRemote(Set<String> corpusNames)
  {
    List<ExampleQuery> result = new LinkedList<ExampleQuery>();
    WebResource service = Helper.getAnnisWebResource();
    try
    {
      if (corpusNames == null || corpusNames.isEmpty())
      {
        result = service.path("query").path("corpora").path(
          "example-queries").get(new GenericType<List<ExampleQuery>>()
        {
        });
      }
      else
      {
        String concatedCorpusNames = StringUtils.join(corpusNames, ",");
        result = service.path("query").path("corpora").path(
          "example-queries").queryParam("corpora", concatedCorpusNames).get(
          new GenericType<List<ExampleQuery>>()
        {
        });
      }
    }
    catch (UniformInterfaceException ex)
    {
      // ignore
    }
    catch (Exception ex)
    {
      log.error("problems with getting example queries from remote for {}",
        corpusNames, ex);
    }
    return result;
  }

  /**
   * Sets the selected corpora and causes a reload
   *
   * @param selectedCorpus Specifies the corpora example queries are fetched
   * for. If it is null, all available example queries are fetched.
   */
  public void setSelectedCorpusInBackground(final Set<String> selectedCorpora)
  {
    getExecutor().submit(new Runnable()
    {
      @Override
      public void run()
      {
        final List<ExampleQuery> result =
          loadExamplesFromRemote(selectedCorpora);

        ui.access(new Runnable()
        {
          @Override
          public void run()
          {
            examples = result;
            try
            {
              removeAllItems();
              addItems();
              ui.push();
            }
            catch (Exception ex)
            {
              log.error("removing or adding of example queries failed for {}",
                selectedCorpora, ex);
            }
          }
        });
      }
    });


  }

  private class ShowResultColumn implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      final ExampleQuery eQ = (ExampleQuery) itemId;
      Panel p = new Panel();
      p.addStyleName(ChameleonTheme.PANEL_BORDERLESS);
      HorizontalLayout l = new HorizontalLayout();
      Button btn = new Button();
      btn.setDescription("show corpus browser for " + eQ.getCorpusName());
      btn.addStyleName(BaseTheme.BUTTON_LINK);
      btn.setIcon(SEARCH_ICON);
      btn.setDescription("show results for \"" + eQ.getExampleQuery()
        + "\" in " + eQ.getCorpusName());
      p.setContent(l);

      btn.addClickListener(new Button.ClickListener()
      {
        @Override
        public void buttonClick(Button.ClickEvent event)
        {
          ControlPanel controlPanel = ui.getControlPanel();
          QueryPanel queryPanel;
          QueryController qController;

          if (controlPanel == null)
          {
            log.error("controlPanel is not initialized");
            return;
          }

          queryPanel = controlPanel.getQueryPanel();
          if (queryPanel == null)
          {
            log.error("queryPanel is not initialized");
            return;
          }

          Set<String> corpusNameSet = new HashSet<String>();
          corpusNameSet.add(eQ.getCorpusName());
          QueryController controller = ui.getQueryController();
          if (controller != null)
          {
            controller.setQuery(new Query(eQ.getExampleQuery(), corpusNameSet));
            controller.executeQuery();
          }
        }
      });
      l.addComponent(btn);
      return p;
    }
  }

  private class ShowCorpusBrowser implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      ExampleQuery eQ = (ExampleQuery) itemId;
      return getOpenCorpusPanel(eQ.getCorpusName());
    }
  }
  
  private ExecutorService getExecutor()
  {
    if(executor == null)
    {
      executor = Executors.newSingleThreadExecutor();
    }
    return executor;
  }
}