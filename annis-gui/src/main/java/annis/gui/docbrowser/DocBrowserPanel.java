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
import annis.gui.paging.PagingComponent;
import annis.libgui.Helper;
import annis.libgui.PluginSystem;
import annis.model.Annotation;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import java.util.List;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class DocBrowserPanel extends Panel
{

  private transient SearchUI ui;

  private Layout layout;

  // displayed while the docnames are fetched
  private Label loadingMsg;

  // the name of the corpus from which the documents are fetched
  private String corpus; 

  private DocBrowserPanel(SearchUI ui, String corpus)
  {
    this.ui = ui;
    this.corpus = corpus;

    // init layout 
    layout = new VerticalLayout();
    setContent(layout);
    layout.setSizeFull();

    // init paging
    PagingComponent paging = new PagingComponent(0, 100);
    layout.addComponent(paging);

    // set loading component
    loadingMsg = new Label("loading documents for " + corpus);
    layout.addComponent(loadingMsg);
    layout.setWidth(100, Unit.PERCENTAGE);
    layout.setHeight(100, Unit.PERCENTAGE);
  }

  @Override
  public void attach()
  {
    super.attach();
    ui.access(new LoadingDocs());
  }

  public static TabSheet.Tab initDocBrowserPanel(SearchUI ui, String corpus)
  {
    TabSheet tabSheet = ui.getTabSheet();
    String caption = "doc browser " + corpus;
    DocBrowserPanel docBrowserPanel = new DocBrowserPanel(ui, corpus);
    TabSheet.Tab docBrowserTab = tabSheet.addTab(docBrowserPanel, caption);
    docBrowserTab.setClosable(true);
    tabSheet.setSelectedTab(docBrowserTab);
    return docBrowserTab;
  }

  private class LoadingDocs extends Thread
  {

    @Override
    public void run()
    {
      WebResource res = Helper.getAnnisWebResource();
      List<Annotation> docs = res.path("meta/docnames/" + corpus).
        get(new Helper.AnnotationListType());
      loadingMsg.setVisible(false);
      layout.removeComponent(loadingMsg);
      DocBrowserTable table = DocBrowserTable.getDocBrowserTable(corpus, ui);
      table.setDocNames(docs);
      layout.addComponent(table);   
      ui.push();
    }
  }
}
