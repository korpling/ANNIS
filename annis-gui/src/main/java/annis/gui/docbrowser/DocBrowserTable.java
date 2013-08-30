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
import annis.libgui.Helper;
import annis.libgui.PluginSystem;
import annis.libgui.ResolverProvider;
import annis.libgui.ResolverProviderImpl;
import annis.libgui.visualizers.VisualizerInput;
import annis.libgui.visualizers.VisualizerPlugin;
import annis.model.Annotation;
import annis.resolver.ResolverEntry;
import annis.resolver.SingleResolverRequest;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List documents for a specific corpus.
 *
 * @author benjamin
 */
public class DocBrowserTable extends Table
{

  private Logger log = LoggerFactory.getLogger(DocBrowserController.class);

  private BeanItemContainer<Annotation> annoBean;

  private final transient PluginSystem ps;

  private String topLevelCorpusName;

  private Map<HashSet<SingleResolverRequest>, List<ResolverEntry>> cacheResolver;

  void setDocNames(List<Annotation> docs)
  {
    annoBean = new BeanItemContainer<Annotation>(docs);
    annoBean.addAll(docs);
    setContainerDataSource(annoBean);
    addGeneratedColumn("document name", new DocNameColumnGen());
    addGeneratedColumn("open visualizer", new DocViewColumn());
    setVisibleColumns(new Object[]
    {
      "document name", "open visualizer"
    });
  }

  private VisualizerInput createInput(String docName)
  {
    VisualizerInput input = new VisualizerInput();

    // get the whole document wrapped in a salt project
    SaltProject txt = null;
    try
    {
      topLevelCorpusName = URLEncoder.encode(topLevelCorpusName, "UTF-8");
      docName = URLEncoder.encode(docName, "UTF-8");
      WebResource annisResource = Helper.getAnnisWebResource();
      txt = annisResource.path("query").path("graphs").path(topLevelCorpusName).
        path(docName).get(SaltProject.class);
    }
    catch (RuntimeException e)
    {
      log.error("General remote service exception", e);
    }
    catch (Exception e)
    {
      log.error("General remote service exception", e);
    }

    if (txt != null)
    {
      SDocument sDoc = txt.getSCorpusGraphs().get(0).getSDocuments().get(0);
      input.setResult(sDoc);
    }
    
    // set empty mapping for avoiding errors with pure written visualizers
    input.setMappings(new Properties());
    return input;


  }

  private DocBrowserTable(String corpus, SearchUI ui)
  {
    this.ps = (PluginSystem) ui;
    this.topLevelCorpusName = corpus;
  }

  /**
   * Generates a link to the visualization configured the the corpus config.
   */
  private class DocNameColumnGen implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      Annotation a = (Annotation) itemId;
      Label l = new Label((String) a.getName());
      return l;
    }
  }

  private class DocViewColumn implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      String docName = ((Annotation) itemId).getName();
      Button openVis = new Button("open visualization");
      openVis.addClickListener(new OpenVisualizerWindow(docName));
      return openVis;
    }
  }

  public static DocBrowserTable getDocBrowserTable(String corpus, SearchUI ui)
  {
    DocBrowserTable docBrowserTable = new DocBrowserTable(corpus, ui);
    docBrowserTable.cacheResolver = Collections.synchronizedMap(
      new HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>>());;
    return docBrowserTable;
  }

  private class OpenVisualizerWindow implements Button.ClickListener
  {

    private String docName;

    public OpenVisualizerWindow(String docName)
    {
      this.docName = docName;
    }

    @Override
    public void buttonClick(Button.ClickEvent event)
    {
      VisualizerPlugin visualizer = ps.getVisualizer("discourse");
      Layout l = new HorizontalLayout();
      Component vis = visualizer.createComponent(createInput(
        docName), null);

      l.addComponent(vis);
      l.setSizeUndefined();
      Window win = new Window();
      win.setContent(l);
      win.setCaption("full text view for: " + docName);
      ((SearchUI) ps).addWindow(win);
    }
  }
}
