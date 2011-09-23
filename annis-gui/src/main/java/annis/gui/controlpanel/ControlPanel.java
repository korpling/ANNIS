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

import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.gui.MainApp;
import annis.gui.Helper;
import annis.service.AnnisService;
import com.vaadin.addon.chameleon.ChameleonTheme;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author thomas
 */
public class ControlPanel extends Panel
{

  private static final long serialVersionUID = -2220211539424865671L;
  private QueryPanel queryPanel;
  private CorpusListPanel corpusList;
  private MainApp app;
  private Window window;
  private String lastQuery;
  private Set<Long> lastCorpusSelection;
  private SearchOptionsPanel searchOptions;

  public ControlPanel(MainApp app)
  {
    super("Search Form");
    this.app = app;

    setStyleName(ChameleonTheme.PANEL_BORDERLESS);
    addStyleName("control");

    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setHeight(100f, UNITS_PERCENTAGE);

    Accordion accordion = new Accordion();
    accordion.setHeight(100f, Layout.UNITS_PERCENTAGE);

    corpusList = new CorpusListPanel(this);

    searchOptions = new SearchOptionsPanel();

    queryPanel = new QueryPanel(this);
    queryPanel.setHeight(18f, Layout.UNITS_EM);

    accordion.addTab(corpusList, "Corpus List", null);
    accordion.addTab(searchOptions, "Search Options", null);
    accordion.addTab(new ExportPanel(queryPanel, corpusList), "Export", null);


    addComponent(queryPanel);
    addComponent(accordion);

    layout.setExpandRatio(accordion, 1.0f);
  }

  @Override
  public void attach()
  {
    super.attach();
    this.window = getWindow();
  }

  public void setQuery(String query, Set<Long> corpora)
  {
    if(queryPanel != null && corpusList != null)
    {
      queryPanel.setQuery(query);
      corpusList.selectCorpora(corpora);
    }
  }

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    super.paintContent(target);
  }

  public void executeQuery()
  {
    if(app != null && app.getUser() == null)
    {
      getWindow().showNotification("Please login first",
        Window.Notification.TYPE_WARNING_MESSAGE);
    }
    else if(app != null && corpusList != null && queryPanel != null)
    {

      Set<Long> rawCorpusSelection = corpusList.getSelectedCorpora();

      // filter corpus selection by logged in user
      lastCorpusSelection = new TreeSet<Long>(rawCorpusSelection);
      lastCorpusSelection.retainAll(app.getUser().getCorpusIdList());

      lastQuery = queryPanel.getQuery();
      if(lastCorpusSelection.isEmpty())
      {
        getWindow().showNotification("Please select a corpus",
          Window.Notification.TYPE_WARNING_MESSAGE);
        return;
      }
      if("".equals(lastQuery))
      {
        getWindow().showNotification("Empty query",
          Window.Notification.TYPE_WARNING_MESSAGE);
        return;
      }

      queryPanel.setCountIndicatorEnabled(true);
      CountThread countThread = new CountThread();
      countThread.start();

      app.showQueryResult(lastQuery, lastCorpusSelection,
        searchOptions.getLeftContext(), searchOptions.getRightContext(),
        searchOptions.getResultsPerPage());
    }
  }

  private class CountThread extends Thread
  {

    private int count = -1;

    @Override
    public void run()
    {
      AnnisService service = Helper.getService(app, window);
      if(service != null)
      {
        try
        {

          count = service.getCount(new LinkedList<Long>(
            lastCorpusSelection), lastQuery);

        }
        catch(RemoteException ex)
        {
          Logger.getLogger(ControlPanel.class.getName()).log(
            Level.SEVERE, null, ex);
          window.showNotification(ex.getLocalizedMessage(),
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
        catch(AnnisQLSemanticsException ex)
        {
          window.showNotification(
            "Sematic error: " + ex.getLocalizedMessage(),
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
        catch(AnnisQLSyntaxException ex)
        {
          window.showNotification(
            "Syntax error: " + ex.getLocalizedMessage(),
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
        catch(AnnisCorpusAccessException ex)
        {
          window.showNotification(
            "Corpus access error: " + ex.getLocalizedMessage(),
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
        catch(Exception ex)
        {
          window.showNotification(
            "unknown exception: " + ex.getLocalizedMessage(),
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
      }

      queryPanel.setStatus("" + count + " matches");
      app.updateQueryCount(count);

      queryPanel.setCountIndicatorEnabled(false);
    }

    public int getCount()
    {
      return count;
    }
  }
}
