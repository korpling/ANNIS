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

import annis.gui.MetaDataPanel;
import annis.libgui.Helper;
import annis.model.Annotation;
import annis.service.objects.DocumentBrowserConfig;
import annis.service.objects.MetaDataColumn;
import annis.service.objects.OrderBy;
import annis.service.objects.Visualizer;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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

  private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();
  
  private final DocBrowserPanel docBrowserPanel;

  private static final Resource INFO_ICON = FontAwesome.INFO_CIRCLE;
  
  public static final String PROP_DOC_NAME = "document name";

  /**
   * Represents the config of the doc visualizer. If there are meta data names
   * defined, also additional columns are generated
   */
  private DocumentBrowserConfig docVisualizerConfig;

  // cache for doc meta data
  private final Map<String, Map<String, List<Annotation>>> docMetaDataCache;

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

    container.addContainerProperty(PROP_DOC_NAME, String.class, "n/a");
    MetaColumns metaCols = generateMetaColumns();

    for (MetaDataCol metaDatum : metaCols.visibleColumns)
    {
      container.
        addContainerProperty(metaDatum.getColName(), String.class, "n/a");
    }

    for (MetaDataCol metaDatum : metaCols.sortColumns)
    {
      container.
        addContainerProperty(metaDatum.getColName(), String.class, "n/a");
    }

    container.addContainerProperty("corpus path", String.class, "n/a");
    container.addContainerProperty("info", Button.class, null);
    container.addContainerProperty("visualizer", Panel.class, null);

    for (Annotation a : docs)
    {
      String doc = a.getName();

      // reverse path and delete the brackets and set a new separator:
      // corpus > ... > subcorpus > document
      List<String> pathList = a.getAnnotationPath();
      if (pathList == null)
      {
        pathList = new LinkedList<>();
      }

      Collections.reverse(pathList);
      String path = StringUtils.join(pathList, " > ");

      // use corpus path for row id, since it should be unique by annis db schema
      Item row = container.addItem(path);
      if(row != null)
      {
        row.getItemProperty(PROP_DOC_NAME).setValue(doc);

        // add the metadata columns.
        for (MetaDataCol metaDataCol : metaCols.visibleColumns)
        {
          String value = generateCell(a.getAnnotationPath(), metaDataCol);
          row.getItemProperty(metaDataCol.getColName()).setValue(value);
        }

        for (MetaDataCol metaDataCol : metaCols.sortColumns)
        {
          if (!metaCols.visibleColumns.contains(metaDataCol))
          {
            // corpusName() holds the corpus path
            String value = generateCell(a.getAnnotationPath(), metaDataCol);
            row.getItemProperty(metaDataCol.getColName()).setValue(value);
          }
        }

        row.getItemProperty("corpus path").setValue(path);
        row.getItemProperty("visualizer").setValue(generateVisualizerLinks(doc));
        row.getItemProperty("info").setValue(generateInfoButtonCell(doc));
      }
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
      "corpus path", "visualizer", "info"
    });

    setVisibleColumns(columnNames);

    for (Object colName : columnNames)
    {
      setColumnHeader((String) colName, (String) colName);
    }

    sortByMetaData(metaCols.sortColumns);
  }

  private MetaColumns generateMetaColumns()
  {

    MetaColumns metaColumns = new MetaColumns();

    if (docVisualizerConfig == null)
    {
      return metaColumns;
    }

    if (docVisualizerConfig.getMetaDataColumns() != null)
    {
      MetaDataColumn[] metaDataCols = docVisualizerConfig.getMetaDataColumns();
      for (MetaDataColumn metaDataCol : metaDataCols)
      {
        metaColumns.visibleColumns.add(new MetaDataCol(metaDataCol.
          getNamespace(),
          metaDataCol.getName()));
      }
    }

    if (docVisualizerConfig.getOrderBy() != null)
    {
      OrderBy[] orderBys = docVisualizerConfig.getOrderBy();
      for (OrderBy orderBy : orderBys)
      {
        metaColumns.sortColumns.add(new MetaDataCol(orderBy.getNamespace(),
          orderBy.getName(), orderBy.isAscending()));
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
    docMetaDataCache = new HashMap<>();

    addStyleName("docvis-table");

    this.docVisualizerConfig = docBrowserPanel.getDocBrowserConfig();
  }

  public Button generateInfoButtonCell(final String docName)
  {
    Button btn = new Button();
    btn.setStyleName(ChameleonTheme.BUTTON_BORDERLESS);
    btn.setIcon(INFO_ICON);
    btn.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        try
        {

          List<Annotation> annos = getDocMetaData(docName);

          /**
           * Transforms to a list of key value pairs. The values concates the
           * namespace and ordinary value. Namespaces "NULL" are ignored.
           */
          // create datasource and bind it to a table
          BeanItemContainer<Annotation> metaContainer
            = new BeanItemContainer<>(Annotation.class);
          metaContainer.addAll(annos);
          metaContainer.sort(new Object[] {"namespace", "name"}, new boolean[] {true, true});
          
          Table metaTable = new Table();
          metaTable.setContainerDataSource(metaContainer);
          metaTable.addGeneratedColumn("genname",
            new MetaDataPanel.MetaTableNameGenerator(metaContainer));
          metaTable.addGeneratedColumn("genvalue",
            new MetaDataPanel.MetaTableValueGenerator(metaContainer));

          metaTable.setVisibleColumns("genname", "genvalue");

          metaTable.setColumnHeaders(new String[]
          {
            "Name", "Value"
          });
          metaTable.setSizeFull();
          metaTable.setColumnWidth("genname", -1);
          metaTable.setColumnExpandRatio("genvalue", 1.0f);
          metaTable.addStyleName(ChameleonTheme.TABLE_STRIPED);

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
  private void sortByMetaData(List<MetaDataCol> sortColumns)
  {
    if (sortColumns == null || sortColumns.isEmpty())
    {
      sort(new Object[]
      {
        PROP_DOC_NAME
      }, new boolean[]
      {
        true
      });

      return;
    }

    Object[] sortByColumns = new Object[sortColumns.size()];
    boolean[] ascendingOrDescending = new boolean[sortColumns.size()];

    for (int i = 0; i < sortColumns.size(); i++)
    {

      sortByColumns[i] = sortColumns.get(i).getColName();

      ascendingOrDescending[i] = sortColumns.get(i).ascending;

    }

    sort(sortByColumns, ascendingOrDescending);

  }

  private Panel generateVisualizerLinks(String docName)
  {
    Panel p = new Panel();
    VerticalLayout l = new VerticalLayout();
    p.addStyleName(ChameleonTheme.PANEL_BORDERLESS);

    if(docVisualizerConfig != null)
    {
      Visualizer[] visualizers = docVisualizerConfig.
        getVisualizers();

      if(visualizers != null)
      {
        for (Visualizer visualizer : visualizers)
        {
          Button openVis = new Button(visualizer.getDisplayName());
          openVis.setDescription(
            "open visualizer with the full text of " + docName);
          openVis.addClickListener(new OpenVisualizerWindow(docName, visualizer,
            openVis));
          openVis.setStyleName(BaseTheme.BUTTON_LINK);
          openVis.setDisableOnClick(true);
          l.addComponent(openVis);
        }
      }
    }

    p.setContent(l);
    return p;
  }
  
  public void setContainerFilter(Filter filter)
  {
    container.removeAllContainerFilters();
    container.addContainerFilter(filter);
  }

  public static DocBrowserTable getDocBrowserTable(DocBrowserPanel parent)
  {
    DocBrowserTable docBrowserTable = new DocBrowserTable(parent);
    return docBrowserTable;
  }

  private class OpenVisualizerWindow implements Button.ClickListener
  {

    private String docName;

    private Visualizer config;

    private final Button button;

    public OpenVisualizerWindow(String docName, Visualizer config,
      Button btn)
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
   * @param document The document the data are fetched for.
   * @return The a list of meta data. Can be empty but never null.
   */
  private List<Annotation> getDocMetaData(String document)
  {
    // lookup up meta data in the cache
    if (!docMetaDataCache.containsKey(docBrowserPanel.getCorpus()))
    {
      // get the metadata for the corpus
      WebResource res = Helper.getAnnisWebResource();
      res = res.path("meta/corpus/").path(
        urlPathEscape.escape(docBrowserPanel.getCorpus())).path("closure");
      
      Map<String, List<Annotation>> metaDataMap = new HashMap<>();
      
      // create a document -> metadata map
      for (Annotation a : res.get(new Helper.AnnotationListType()))
      {
        if (a.getAnnotationPath() != null
          && !a.getAnnotationPath().isEmpty()
          && a.getType().equals("DOCUMENT"))
        {
          String docName = a.getAnnotationPath().get(0);
          if (!metaDataMap.containsKey(docName)) {
            metaDataMap.put(docName, new ArrayList<Annotation>());
          }
          metaDataMap.get(docName).add(a);
        }
      }
      docMetaDataCache.put(docBrowserPanel.getCorpus(), metaDataMap);
    }
    
    if (docMetaDataCache.get(docBrowserPanel.getCorpus()).containsKey(document))
    {
      return docMetaDataCache.get(docBrowserPanel.getCorpus()).get(document);
    }
    else
    {
      return new ArrayList<Annotation>();
    }
  }

  private String generateCell(List<String> path, MetaDataCol metaDatum)
  {
    List<Annotation> metaData = new LinkedList<>();
    if (path != null && !path.isEmpty())
    {
      metaData = getDocMetaData(path.get(path.size()-1));
    }

    // lookup meta data
    for (Annotation a : metaData)
    {
      if (metaDatum.namespace != null
        && metaDatum.namespace.equals(a.getNamespace())
        && metaDatum.name.equals(a.getName()))
      {
        return a.getValue();
      }

      if (metaDatum.namespace == null
        && a.getNamespace() == null
        && metaDatum.name.equals(a.getName()))
      {
        return a.getValue();
      }
    }

    return "n/a";
  }

  private static class MetaColumns
  {

    List<MetaDataCol> visibleColumns;

    List<MetaDataCol> sortColumns;

    public MetaColumns()
    {
      this.visibleColumns = new ArrayList<>();
      this.sortColumns = new ArrayList<>();
    }
  }

  private static class MetaDataCol
  {

    String namespace;

    String name;

    boolean ascending;

    public MetaDataCol(String namespace, String name)
    {
      this.namespace = namespace;
      this.name = name;
    }

    public MetaDataCol(String namespace, String name, boolean ascending)
    {
      this(namespace, name);
      this.ascending = ascending;
    }

    String getColName()
    {
      return namespace != null && !namespace.equalsIgnoreCase("null") ? namespace + ":" + name : name;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }
      if (getClass() != obj.getClass())
      {
        return false;
      }
      final MetaDataCol other = (MetaDataCol) obj;
      if ((this.namespace == null) ? (other.namespace != null) : !this.namespace.
        equals(other.namespace))
      {
        return false;
      }
      if ((this.name == null) ? (other.name != null) : !this.name.equals(
        other.name))
      {
        return false;
      }
      return true;
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
