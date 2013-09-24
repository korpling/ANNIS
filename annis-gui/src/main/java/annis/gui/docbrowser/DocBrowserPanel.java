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
package annis.gui.docbrowser;

import annis.gui.SearchUI;
import annis.gui.paging.PagingCallback;
import annis.gui.paging.PagingComponent;
import annis.libgui.Helper;
import annis.model.Annotation;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class DocBrowserPanel extends Panel
{

  private transient SearchUI ui;

  private VerticalLayout layout;

  // displayed while the docnames are fetched
  private Label loadingMsg;

  // the name of the corpus from which the documents are fetched
  private String corpus;

  private DocBrowserTable table;

  private PagingComponent paging;

  private DocBrowserPanel(SearchUI ui, String corpus)
  {
    this.ui = ui;
    this.corpus = corpus;

    // init layout 
    layout = new VerticalLayout();
    setContent(layout);
    layout.setSizeFull();

    // set loading component
    loadingMsg = new Label("loading documents for " + corpus);
    layout.addComponent(loadingMsg);
    setSizeFull();

    paging = new PagingComponent();
    
//    addComponentAttachListener(new ComponentAttachListener() {
//
//      @Override
//      public void componentAttachedToContainer(ComponentAttachEvent event)
//      {
//        if (paging != null && table != null)
//        {
//          layout.setExpandRatio(paging, 0.2f);
//          layout.setExpandRatio(table, 0.8f);
//        }
//      }
//    });
  }

  @Override
  public void attach()
  {
    super.attach();
    ui.access(new LoadingDocs());
  }

  /**
   * Initiated the {@link DocBrowserPanel} and put the main tab navigation.
   *
   * @param ui The main application class of the gui.
   * @param corpus The corpus, for which the doc browser is initiated.
   * @return A new wrapper panel for a doc browser. Make sure, that this is not
   * done several times.
   */
  public static DocBrowserPanel initDocBrowserPanel(SearchUI ui, String corpus)
  {
    return new DocBrowserPanel(ui, corpus);
  }

  public void openVis(String doc, JSONObject config)
  {
    ui.getDocBrowserController().openDocVis(corpus, doc, config);
  }

  private class LoadingDocs extends Thread
  {

    @Override
    public void run()
    {
      WebResource res = Helper.getAnnisWebResource();
      final List<Annotation> docs = res.path("meta/docnames/" + corpus).
        get(new Helper.AnnotationListType());

      loadingMsg.setVisible(false);
      layout.removeComponent(loadingMsg);

      paging.addCallback(new PagingCallback()
      {
        @Override
        public void switchPage(int offset, int limit)
        {
          if (table != null)
          {
            layout.removeComponent(table);
          }
          
          table = DocBrowserTable.getDocBrowserTable(DocBrowserPanel.this);
          layout.addComponent(table);
          layout.setExpandRatio(table, 0.85f);
          List<Annotation> docPage = docs.subList(offset, offset + limit);
          table.setDocNames(docPage);
        }
      });

      paging.setPageSize(1, false);
      paging.setCount(docs.size(), true);

      layout.addComponent(paging, 0);
      ui.push();
    }
  }

  public String getCorpus()
  {
    return corpus;
  }

  public PagingComponent getPagingComponent()
  {
    return paging;
  }
}
