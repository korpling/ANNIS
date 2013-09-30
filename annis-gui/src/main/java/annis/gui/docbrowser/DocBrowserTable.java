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
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
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
import java.util.logging.Level;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List documents for a specific corpus.
 *
 * @author Benjamin Weißenfels<b.pixeldrama@gmail.com>
 */
public class DocBrowserTable extends Table
{

  private Logger log = LoggerFactory.getLogger(DocBrowserTable.class);

  private final DocBrowserPanel docBrowserPanel;

  private static final ThemeResource INFO_ICON = new ThemeResource("info.gif");

  // the key for the visualizer json array list
  public final String VIS_CONFIG_KEY = "vis";

  /**
   * Represents the config of the doc visualizer. If there are meta data names
   * defined, also additional columns are generated
   */
  private JSONObject docVisualizerConfig;

  // the key for the meta cols, which are generated in the main table
  private final String VIS_META_CONFIG = "metadata";

  // the key for the meta data namespace
  public final String VIS_META_CONFIG_NAMESPACE = "namespace";

  // the key for the meta data name
  private final String VIS_META_CONFIG_NAME = "name";

  private final String ORDER_BY = "orderBy";

  // cache for doc meta data
  private Map<String, List<Annotation>> docMetaDataCache;

  private IndexedContainer container;

  /**
   * Updates the table with docnames and generate the additional columns defined
   * by the user.
   *
   * @param docs the list of documents, wrapped in the {@link Annotation} POJO
   */
  void setDocNames(List<Annotation> docs)
  {

    container = new IndexedContainer();

    container.addContainerProperty("document name", String.class, "n/a");
    MetaColumns metaCols = generateMetaColumns();

    for (MetaDatum metaDatum : metaCols.visibleColumns)
    {
      container.
        addContainerProperty(metaDatum.getColName(), String.class, "n/a");
    }

    container.addContainerProperty("info", Button.class, null);
    container.addContainerProperty("visualizer", Panel.class, null);

    for (Annotation a : docs)
    {
      String doc = a.getName();
      Item row = container.addItem(doc);
      row.getItemProperty("document name").setValue(doc);

      // add the metadata columns. Their number is not fixed
      for (MetaDatum metaDatum : metaCols.visibleColumns)
      {
        String value = generateCell(doc, metaDatum);
        row.getItemProperty(metaDatum.getColName()).setValue(value);
      }

      for (MetaDatum metaDatum : metaCols.sortColumns)
      {
        if (!metaCols.visibleColumns.contains(metaDatum))
        {
          String value = generateCell(doc, metaDatum);
          row.getItemProperty(metaDatum.getColName()).setValue(value);
        }
      }

      row.getItemProperty("visualizer").setValue(generateVisualizerLinks(doc));
      row.getItemProperty("info").setValue(generateInfoButtonCell(doc));
    }

    setContainerDataSource(container);
    Object[] metaDataColNames = new Object[metaCols.visibleColumns.size()];

    for (int i = 0; i < metaDataColNames.length; i++)
    {
      metaDataColNames[i] = metaCols.visibleColumns.get(i).getColName();
    }

    Object[] columnNames = ArrayUtils.addAll(ArrayUtils.addAll(new Object[]
    {
      "document name"
    }, metaDataColNames), new Object[]
    {
      "visualizer", "info"
    });


    setVisibleColumns(columnNames);

    for (Object colName : columnNames)
    {
      setColumnHeader((String) colName, (String) colName);
    }

    setColumnWidth("info", 26);

    sortByMetaData();
  }

  private class MetaColumns
  {

    List<MetaDatum> visibleColumns;

    List<MetaDatum> sortColumns;

    public MetaColumns()
    {
      this.visibleColumns = new ArrayList<MetaDatum>();
      this.sortColumns = new ArrayList<MetaDatum>();
    }
  }

  private MetaColumns generateMetaColumns()
  {

    MetaColumns metaColumns = new MetaColumns();


    if (docVisualizerConfig.has(VIS_META_CONFIG))
    {
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
            name = c.getString(VIS_META_CONFIG_NAME);

            MetaDatum metaDatum = new MetaDatum(namespace, name);
            metaColumns.visibleColumns.add(metaDatum);
          }
        }
      }
      catch (JSONException ex)
      {
        log.error("cannot retrieve meta array from doc visualizer config", ex);
      }
    }

    return metaColumns;
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

    addStyleName("docvis-table");

    this.docVisualizerConfig = docBrowserPanel.getDocBrowserConfig();
  }

  public Button generateInfoButtonCell(final String docName)
  {
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

  /**
   * Sort the table by a given config. The config includes metadata keys and the
   * table is sorted lexicographically by their values. If not config for
   * sorting is determined the document name is used for sorting.
   */
  private void sortByMetaData()
  {
    JSONArray sortingConfig = null;
    try
    {
      sortingConfig = docVisualizerConfig.getJSONArray(ORDER_BY);
    }
    catch (JSONException ex)
    {
      log.warn("no sorting by meta data defined -> use document name");
    }


    if (sortingConfig == null || sortingConfig.length() == 0)
    {
      sort(new Object[]
      {
        "document name"
      }, new boolean[]
      {
        true
      });

      return;
    }

    Object[] sortByColumns = new Object[sortingConfig.length()];
    boolean[] ascendingOrDescending = new boolean[sortingConfig.length()];

    for (int i = 0; i < sortingConfig.length(); i++)
    {
      try
      {
        JSONObject jsonConfig = sortingConfig.getJSONObject(i);
        MetaDatum metaDatum;
        String namespace = null;
        String name;

        if (jsonConfig.has("namespace"))
        {
          namespace = jsonConfig.getString("namespace");
        }

        name = jsonConfig.getString("name");
        metaDatum = new MetaDatum(namespace, name);
        sortByColumns[i] = metaDatum.getColName();

        if (jsonConfig.has("ascending"))
        {
          ascendingOrDescending[i] = jsonConfig.getBoolean("ascending");
        }
        else
        {
          ascendingOrDescending[i] = true;
        }
      }
      catch (JSONException ex)
      {
        log.warn("cannot read sorting config for corpus "
          + docBrowserPanel.getCorpus());
      }

      sort(sortByColumns, ascendingOrDescending);
    }

  }

  private Panel generateVisualizerLinks(String docName)
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
        Button openVis = new Button(config.getString("displayName"));
        openVis.setDescription(
          "open visualizer with the full text of " + docName);
        openVis.addClickListener(new OpenVisualizerWindow(docName, config,
          openVis));
        openVis.setStyleName(BaseTheme.BUTTON_LINK);
        openVis.setDisableOnClick(true);
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

  public static DocBrowserTable getDocBrowserTable(DocBrowserPanel parent)
  {
    DocBrowserTable docBrowserTable = new DocBrowserTable(parent);
    return docBrowserTable;
  }

  private class OpenVisualizerWindow implements Button.ClickListener
  {

    private String docName;

    private JSONObject config;

    private final Button button;

    public OpenVisualizerWindow(String docName, JSONObject config, Button btn)
    {
      this.button = btn;
      this.docName = docName;
      this.config = config;
    }

    @Override
    public void buttonClick(Button.ClickEvent event)
    {

      docBrowserPanel.openVis(docName, config, button);
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

  private String generateCell(String documentName, MetaDatum metaDatum)
  {
    List<Annotation> metaData = getDocMetaData(documentName);

    // lookup meta data
    for (Annotation a : metaData)
    {
      if (metaDatum.namespace != null
        && metaDatum.namespace.equals(a.getNamespace())
        && metaDatum.name.equals(a.getName()))
      {
        return a.getValue();
      }

      if (metaDatum.name.equals(a.getName()))
      {
        return a.getValue();
      }
    }

    return "";
  }

  private class MetaDatum
  {

    String namespace;

    String name;

    public MetaDatum(String namespace, String name)
    {
      this.namespace = namespace;
      this.name = name;
    }

    String getColName()
    {
      return namespace != null && !namespace.equalsIgnoreCase("null") ? namespace + ":" + name : name;
    }

    @Override
    public boolean equals(Object m)
    {
      if (m == null && !(m instanceof MetaDatum))
      {
        return false;
      }

      if (this == m)
      {
        return true;
      }

      if (getColName().equals(((MetaDatum) m).getColName()))
      {
        return true;
      }

      return false;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 97 * hash + (this.namespace != null ? this.namespace.hashCode() : 0);
      hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
      return hash;
    }
  }
}