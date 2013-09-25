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
import annis.service.objects.CorpusConfig;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  // the key for the json config of the doc visualization
  private static final String DOC_BROWSER_CONFIG_KEY = "browse-document-visualizers";

  private Logger log = LoggerFactory.getLogger(DocBrowserPanel.class);

  private CorpusConfig corpusConfig;

  /**
   * Normally get the page size from annis-service.properties for the paging
   * component. If something went wrong this value or the amount of documents
   * within the corpus is used: min(pageSize, amountOf(documents))
   */
  private final int PAGE_SIZE = 20;

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
  }

  @Override
  public void attach()
  {
    super.attach();
    ui.access(new LoadingDocs());
  }

  /**
   * Normally get the page size from annis-service.properties for the paging
   * component. If something went wrong this value or the amount of documents
   * within the corpus is used:
   *
   * <code>min(configValue, min(pageSize, amountOf(documents)))</code>
   *
   * @param docSize The amount of documents with this corpus.
   *
   * @return the page size, which is never bigger than the doc size.
   */
  public int getPageSize(int docSize)
  {
    int result = Math.min(PAGE_SIZE, docSize);
    try
    {
      result = Math.min(result, getDocBrowserConfig().getInt(
        "pageSize"));
    }
    catch (JSONException ex)
    {
      log.warn(
        "cannot read the docvisualizer pageSize, so it's set to " + PAGE_SIZE,
        ex);

    }
    return result;
  }

  JSONObject getDocBrowserConfig()
  {
    // check first, if the a config is already fetched.
    if (corpusConfig != null)
    {
      corpusConfig = Helper.getCorpusConfig(corpus);
    }

    if (corpusConfig == null || !corpusConfig.getConfig().containsKey(
      DOC_BROWSER_CONFIG_KEY))
    {
      corpusConfig = Helper.getDefaultCorpusConfig();
    }

    String c = corpusConfig.getConfig().getProperty(DOC_BROWSER_CONFIG_KEY);
    try
    {
      return new JSONObject(c);
    }
    catch (JSONException ex)
    {
      log.error("could not read the doc browser config", ex);
    }

    return null;
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

      paging.setPageSize(getPageSize(docs.size()), true);
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
