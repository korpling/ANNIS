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

import annis.libgui.Helper;
import annis.model.Annotation;
import annis.service.objects.CorpusConfig;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List documents for a specific corpus.
 *
 * @author benjamin
 */
public class DocBrowserTable extends Table
{

  private Logger log = LoggerFactory.getLogger(DocBrowserTable.class);

  private BeanItemContainer<Annotation> annoBean;

  private final DocBrowserPanel parent;

  private static final ThemeResource EYE_ICON = new ThemeResource("eye.png");

  private static final ThemeResource INFO_ICON = new ThemeResource("info.gif");

  // the key for the json config of the doc visualization
  private static final String DOC_BROWSER_CONFIG_KEY = "browse-document-visualizers";

  private JSONObject docVisualizerConfig;

  void setDocNames(List<Annotation> docs)
  {
    annoBean = new BeanItemContainer<Annotation>(docs);
    annoBean.addAll(docs);
    setContainerDataSource(annoBean);
    addGeneratedColumn("document name", new DocNameColumnGen());
    addGeneratedColumn("open visualizer", new DocViewColumn());
    addGeneratedColumn("info browser", new InfoButtonColumnGen());
    setVisibleColumns(new Object[]
    {
      "document name", "open visualizer", "info browser"
    });

    setColumnHeaders("document name", "visualizer", "");
    setColumnWidth("info browser", 20);
  }

  private DocBrowserTable(DocBrowserPanel parent)
  {

    this.parent = parent;

    // configure layout
    setSizeFull();

    // put stripes to the table
    addStyleName(ChameleonTheme.TABLE_STRIPED);

    this.docVisualizerConfig = getDocBrowserConfig();
  }

  private class InfoButtonColumnGen implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      Annotation a = (Annotation) itemId;
      final String docName = a.getName();
      Button btn = new Button();
      btn.setStyleName(BaseTheme.BUTTON_LINK);
      btn.setIcon(INFO_ICON);
      btn.addClickListener(new Button.ClickListener()
      {
        @Override
        public void buttonClick(Button.ClickEvent event)
        {
          try
          {
            // get the metadata of a specific doc
            WebResource res = Helper.getAnnisWebResource();
            res = res.path("meta/doc/").path(parent.getCorpus()).path(docName);
            List<Annotation> annos = res.get(new Helper.AnnotationListType());

            // create datasource and bind it to a table
            BeanItemContainer<Annotation> dataSource = new BeanItemContainer<Annotation>(
              Annotation.class, annos);
            Table metaTable = new Table();
            metaTable.setContainerDataSource(dataSource);

            // style the table
            metaTable.setVisibleColumns(new Object[]
            {
              "name", "value"
            });
            metaTable.setColumnHeaders("name", "value");
            metaTable.setSizeFull();

            // create and style the extra window for the metadata table
            Window metaWin = new Window();
            metaWin.setContent(metaTable);
            metaWin.setCaption("metadata doc " + docName);
            metaWin.center();
            metaWin.setWidth(400, Unit.PIXELS);
            metaWin.setHeight(400, Unit.PIXELS);

            // paint the window
            parent.getUI().addWindow(metaWin);
          }
          catch (UniformInterfaceException ex)
          {
            log.error("can not retrieve metadata for document " + docName, ex);
          }

        }
      });
      return btn;
    }
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
      Panel p = new Panel();
      VerticalLayout l = new VerticalLayout();
      p.addStyleName(ChameleonTheme.PANEL_BORDERLESS);
      try
      {
        JSONArray configArray = docVisualizerConfig.getJSONArray("vis");

        for (int i = 0; i < configArray.length(); i++)
        {

          JSONObject config = configArray.getJSONObject(i);
          String docName = ((Annotation) itemId).getName();
          Button openVis = new Button(config.getString("displayName"));
          openVis.setDescription(
            "open visualizer with the full text of " + docName);
          openVis.addClickListener(new OpenVisualizerWindow(docName, config));
          openVis.setStyleName(BaseTheme.BUTTON_LINK);
          l.addComponent(openVis);

        }

      }
      catch (JSONException ex)
      {
        log.error("cannnot retrieve json object", ex);
      }

      p.setContent(l);
      return p;
    }
  }

  private JSONObject getDocBrowserConfig()
  {
    CorpusConfig corpusConfig = Helper.getCorpusConfig(parent.getCorpus());

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

  public static DocBrowserTable getDocBrowserTable(DocBrowserPanel parent)
  {
    DocBrowserTable docBrowserTable = new DocBrowserTable(parent);
    return docBrowserTable;
  }

  private class OpenVisualizerWindow implements Button.ClickListener
  {

    private String docName;

    private JSONObject config;

    public OpenVisualizerWindow(String docName, JSONObject config)
    {
      this.docName = docName;
      this.config = config;
    }

    @Override
    public void buttonClick(Button.ClickEvent event)
    {

      parent.openVis(docName, config);
    }
  }
}
