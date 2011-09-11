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
package annis.gui.resultview;

import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.gui.ServiceHelper;
import annis.gui.TestPanel;
import annis.gui.paging.PagingCallback;
import annis.gui.paging.PagingComponent;
import annis.service.ifaces.AnnisResultSet;
import com.vaadin.addon.chameleon.ChameleonTheme;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class ResultViewPanel extends Panel implements PagingCallback
{

  private Label lblInfo;
  private PagingComponent paging;
  private ResultSetPanel resultPanel;
  private String aql;
  private Set<Long> corpora;
  private int contextLeft, contextRight, pageSize;
  private AnnisResultQuery query;
  private VerticalLayout layout;
  private Panel scrollPanel;
  private ProgressIndicator progressResult;

  public ResultViewPanel(String aql, Set<Long> corpora, int contextLeft, int contextRight, int pageSize)
  {
    this.aql = aql;
    this.corpora = corpora;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
    this.pageSize = pageSize;

    setSizeFull();

    VerticalLayout mainLayout = (VerticalLayout) getContent();
    mainLayout.setMargin(false);
    mainLayout.setSizeFull();


    lblInfo = new Label();
    lblInfo.setValue("Result for query \"" + aql.replaceAll("\n", " ") + "\"");

    paging = new PagingComponent(0, pageSize);

    scrollPanel = new Panel();
    scrollPanel.setSizeFull();
    scrollPanel.addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    layout = (VerticalLayout) scrollPanel.getContent();
    layout.setMargin(false);
    layout.setWidth("100%");
    layout.setHeight("-1px");

    mainLayout.addComponent(lblInfo);
    mainLayout.addComponent(paging);
    mainLayout.addComponent(scrollPanel);

    mainLayout.setSizeFull();
    mainLayout.setExpandRatio(scrollPanel, 1.0f);

    progressResult = new ProgressIndicator();
    progressResult.setIndeterminate(true);
    progressResult.setEnabled(false);
  }

  @Override
  public void attach()
  {
    super.attach();

    paging.addCallback(this);

    query = new AnnisResultQuery(new LinkedList<Long>(corpora), aql,
      contextLeft, contextRight, ServiceHelper.getService(getApplication(), getWindow()));
    createPage(0, pageSize);
  }

  public void setCount(int count)
  {
    paging.setCount(count);
  }

  @Override
  public void createPage(final int start, final int limit)
  {
    if(query != null)
    {

      layout.removeAllComponents();
      progressResult.setEnabled(true);
      layout.addComponent(progressResult);

      Runnable r = new Runnable()
      {

        @Override
        public void run()
        {
          try
          {
            AnnisResultSet result = query.loadBeans(start, limit);
            resultPanel = new ResultSetPanel(result, start);
            
            progressResult.setEnabled(false);
            layout.removeAllComponents();            
            layout.addComponent(resultPanel);
          }
          catch(AnnisQLSemanticsException ex)
          {
            lblInfo.setValue("Semantic error: " + ex.getLocalizedMessage());
          }
          catch(AnnisQLSyntaxException ex)
          {
            lblInfo.setValue("Syntax error: " + ex.getLocalizedMessage());
          }
          catch(AnnisCorpusAccessException ex)
          {
            lblInfo.setValue("Corpus access error: " + ex.getLocalizedMessage());
          }
          catch(Exception ex)
          {
            Logger.getLogger(ResultViewPanel.class.getName()).log(Level.SEVERE, "unknown exception in result view", ex);
            lblInfo.setValue("unknown exception: " + ex.getLocalizedMessage());
          }
        }
      };
      Thread t = new Thread(r);
      t.start();
      
    }
  }

  private class WorkerThread extends Thread
  {

    @Override
    public void run()
    {
      super.run();
    }
  }
}
