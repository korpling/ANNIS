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
import annis.gui.ServiceHelper;
import annis.service.AnnisService;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class ControlPanel extends Panel
{

  private QueryPanel queryPanel;
  private CorpusListPanel corpusList;
  private MainApp app;
  private Window window;

  public ControlPanel(MainApp app)
  {
    super("Search Form");
    this.app = app;

    addStyleName("control");

    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setHeight(100f, UNITS_PERCENTAGE);

    Accordion accordion = new Accordion();
    accordion.setHeight(100f, Layout.UNITS_PERCENTAGE);

    corpusList = new CorpusListPanel(this);
    accordion.addTab(corpusList, "Corpus List", null);
    accordion.addTab(new SearchOptionsPanel(), "Search Options", null);
    accordion.addTab(new ExportPanel(), "Export", null);

    queryPanel = new QueryPanel(this);
    queryPanel.setHeight(18f, Layout.UNITS_EM);

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

  public void executeQuery()
  {
    if(app != null && corpusList != null && queryPanel != null)
    {
      Set<Long> corpora = corpusList.getSelectedCorpora();
      String aql = queryPanel.getQuery();

      if(corpora.isEmpty())
      {
        getWindow().showNotification("Please select a corpus",
          Window.Notification.TYPE_WARNING_MESSAGE);
        return;
      }
      if("".equals(aql))
      {
        getWindow().showNotification("Empty query",
          Window.Notification.TYPE_WARNING_MESSAGE);
        return;
      }
      
      queryPanel.setCountIndicatorEnabled(true);      
      CountThread countThread = new CountThread(aql, corpora, queryPanel);
      countThread.start();
      
      // TODO
//      app.showQueryResult(aql, corpora, 5, 5);
    }
  }
  
  public class CountThread extends Thread
  {
    private String aql;
    private Set<Long> corpora;
    private int count;
    private QueryPanel queryPanel;

    public CountThread(String aql, Set<Long> corpora, QueryPanel queryPanel)
    {
      this.aql = aql;
      this.corpora = corpora;
      this.count = -1;
      this.queryPanel = queryPanel;
    }
    
    @Override
    public void run()
    {
      AnnisService service = ServiceHelper.getService(app, window);
      if(service != null)
      {
        try
        {
          count = service.getCount(new LinkedList<Long>(corpora), aql);
          
        }
        catch(RemoteException ex)
        {
          Logger.getLogger(ControlPanel.class.getName()).log(Level.SEVERE, null, ex);
          window.showNotification(ex.getLocalizedMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        }
        catch(AnnisQLSemanticsException ex)
        {
          window.showNotification("Sematic error: " + ex.getLocalizedMessage(), 
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
        catch(AnnisQLSyntaxException ex)
        {
          window.showNotification("Syntax error: " + ex.getLocalizedMessage(), 
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
        catch(AnnisCorpusAccessException ex)
        {
          window.showNotification("Corpus access error: " + ex.getLocalizedMessage(), 
            Window.Notification.TYPE_ERROR_MESSAGE);
        }
      }

      queryPanel.setStatus("" + count + " matches");
      queryPanel.setCountIndicatorEnabled(false);
    }

    public int getCount()
    {
      return count;
    }    
  }
}
