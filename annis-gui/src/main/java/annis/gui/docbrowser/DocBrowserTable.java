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

import annis.gui.paging.PagingComponent;
import annis.libgui.Helper;
import annis.model.Annotation;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
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

  private final DocBrowserPanel docBrowserPanel;

  private static final ThemeResource EYE_ICON = new ThemeResource("eye.png");

  private static final ThemeResource INFO_ICON = new ThemeResource("info.gif");

  // the key for the visualizer json array list
  public final String VIS_CONFIG_KEY = "vis";

  /**
   * Represents the config of the doc visualizer. If there are meta data names
   * defined, also additional columns are generated
   */
  private transient JSONObject docVisualizerConfig;

  // the key for the meta cols, which are generated in the main table
  private final String VIS_META_CONFIG = "metadata";

  // the key for the meta data namespace
  public final String VIS_META_CONFIG_NAMESPACE = "namespace";

  // the key for the meta data name
  private final String VIS_META_CONFIG_NAME = "name";

  // cache for doc meta data
  private Map<String, List<Annotation>> docMetaDataCache;

  // flag for avoiding column setting twice
  private boolean generatedColumns;

  /**
   * Updates the table with docnames and generate the additional columns defined
   * by the user.
   *
   * @param docs the list of documents, wrapped in the {@link Annotation} POJO
   */
  void setDocNames(List<Annotation> docs)
  {
    PagingComponent paging = docBrowserPanel.getPagingComponent();

    annoBean = new BeanItemContainer<Annotation>(Annotation.class, docs);

    setContainerDataSource(annoBean);

    if (!generatedColumns)
    {
      generatedColumns = true;
      addGeneratedColumn("document name", new DocNameColumnGen());
      List<Object> generateMetaColumns = generateMetaColumns();
      addGeneratedColumn("open visualizer", new DocViewColumn());
      addGeneratedColumn("info browser", new InfoButtonColumnGen());
      Object[] columnNames = ArrayUtils.addAll(ArrayUtils.addAll(new Object[]
      {
        "document name"
      }, generateMetaColumns.toArray()), new Object[]
      {
        "open visualizer", "info browser"
      });

      setVisibleColumns(columnNames);

      for (Object colName : columnNames)
      {
        setColumnHeader((String) colName, (String) colName);
      }

      setColumnWidth("info browser", 26);
    }
  }

  private List<Object> generateMetaColumns()
  {

    List<Object> columnNames = new ArrayList<Object>();

    if (!docVisualizerConfig.has(VIS_META_CONFIG))
    {
      return columnNames;
    }

    try
    {
      JSONArray metaArray = docVisualizerConfig.getJSONArray(VIS_META_CONFIG);
      for (int i = 0; i < metaArray.length(); i++)
      {
        JSONObject c = metaArray.getJSONObject(i);
        String namespace = null;
        String name;

        if (c.has(VIS_META_CONFIG_NAMESPACE)
          && c.getString(VIS_META_CONFIG_NAMESPACE) != null
          && !c.getString(VIS_META_CONFIG_NAMESPACE).equalsIgnoreCase("null"))
        {
          namespace = c.getString(VIS_META_CONFIG_NAMESPACE);
        }

        if (c.has(VIS_META_CONFIG_NAME))
        {
          String colname;
          name = c.getString(VIS_META_CONFIG_NAME);

          if (namespace != null)
          {
            colname = namespace + ":" + name;
          }
          else
          {
            colname = name;
          }

          columnNames.add(colname);
          addGeneratedColumn(colname, new MetaDataColumn(namespace, name));
        }
      }
    }
    catch (JSONException ex)
    {
      log.error("cannot retrieve meta array from doc visualizer config", ex);
    }

    return columnNames;
  }

  private DocBrowserTable(DocBrowserPanel parent)
  {

    // the panel which contains this table
    this.docBrowserPanel = parent;

    // configure layout
    setSizeFull();

    // put stripes to the table
    addStyleName(ChameleonTheme.TABLE_STRIPED);

    // init metadata cache
    docMetaDataCache = new HashMap<String, List<Annotation>>();

    this.docVisualizerConfig = docBrowserPanel.getDocBrowserConfig();
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

            List<Annotation> annos = getDocMetaData(docName);

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
            docBrowserPanel.getUI().addWindow(metaWin);
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
        JSONArray configArray = docVisualizerConfig.getJSONArray(VIS_CONFIG_KEY);

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

      docBrowserPanel.openVis(docName, config);
    }
  }

  /**
   * Retrieves date from the cache or from the annis rest service for a specific
   * document.
   *
   * @param doc The document the data are fetched for.
   * @return The a list of meta data. Can be empty but never null.
   */
  private List<Annotation> getDocMetaData(String doc)
  {
    // lookup up meta data in the cache
    if (docMetaDataCache.containsKey(doc))
    {
      return docMetaDataCache.get(doc);
    }

    // get the metadata of a specific doc
    WebResource res = Helper.getAnnisWebResource();
    res = res.path("meta/doc/").path(docBrowserPanel.getCorpus()).path(doc);
    List<Annotation> annos = res.get(new Helper.AnnotationListType());

    // update cache
    docMetaDataCache.put(doc, annos);

    return annos;
  }

  private class MetaDataColumn implements Table.ColumnGenerator
  {

    String namespace;

    String name;

    public MetaDataColumn(String namespace, String name)
    {
      this.namespace = namespace;
      this.name = name;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      Annotation docs = (Annotation) itemId;
      List<Annotation> metaData = getDocMetaData(docs.getName());

      // lookup meta data
      for (Annotation a : metaData)
      {
        if (namespace != null && namespace.equals(a.getNamespace())
          && name.equals(a.getName()))
        {
          return a.getValue();
        }

        if (name.equals(a.getName()))
        {
          return a.getValue();
        }
      }
      return "";
    }
  }
}