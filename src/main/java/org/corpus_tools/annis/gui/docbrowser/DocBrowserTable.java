/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.docbrowser;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.themes.BaseTheme;
import com.vaadin.v7.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.annis.api.model.VisualizerRule;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.objects.DocumentBrowserConfig;
import org.corpus_tools.annis.gui.objects.MetaDataColumn;
import org.corpus_tools.annis.gui.objects.OrderBy;
import org.corpus_tools.annis.gui.objects.Visualizer;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List documents for a specific corpus.
 *
 * @author Benjamin Weißenfels{@literal <b.pixeldrama@gmail.com>}
 */
public class DocBrowserTable extends Table {

  private static class MetaColumns {

    List<MetaDataCol> visibleColumns;

    List<MetaDataCol> sortColumns;

    public MetaColumns() {
      this.visibleColumns = new ArrayList<>();
      this.sortColumns = new ArrayList<>();
    }
  }

  private static class MetaDataCol {

    String namespace;

    String name;

    boolean ascending;

    public MetaDataCol(String namespace, String name) {
      this.namespace = namespace;
      this.name = name;
    }

    public MetaDataCol(String namespace, String name, boolean ascending) {
      this(namespace, name);
      this.ascending = ascending;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final MetaDataCol other = (MetaDataCol) obj;
      if ((this.namespace == null) ? (other.namespace != null)
          : !this.namespace.equals(other.namespace)) {
        return false;
      }
      return (this.name == null) ? (other.name == null) : this.name.equals(other.name);
    }

    String getColName() {
      return namespace != null && !namespace.equalsIgnoreCase("null") ? namespace + ":" + name
          : name;
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 97 * hash + (this.namespace != null ? this.namespace.hashCode() : 0);
      hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
      return hash;
    }
  }

  private class OpenVisualizerWindow implements Button.ClickListener {

    /**
     * 
     */
    private static final long serialVersionUID = -268464505148741358L;

    private String docId;

    private VisualizerRule config;

    private final Button button;

    public OpenVisualizerWindow(String docId, VisualizerRule config, Button btn) {
      this.button = btn;
      this.docId = docId;
      this.config = config;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
      docBrowserPanel.openVis(docId, config, button);
    }
  }

  /**
   * 
   */
  private static final long serialVersionUID = -4380116858337669345L;

  private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();

  private static final Resource INFO_ICON = FontAwesome.INFO_CIRCLE;

  public static final String PROP_DOC_NAME = "document name";

  public static DocBrowserTable getDocBrowserTable(DocBrowserPanel parent) {
    DocBrowserTable docBrowserTable = new DocBrowserTable(parent);
    return docBrowserTable;
  }

  private Logger log = LoggerFactory.getLogger(DocBrowserTable.class);

  private final DocBrowserPanel docBrowserPanel;

  /**
   * Represents the config of the doc visualizer. If there are meta data names defined, also
   * additional columns are generated
   */
  private DocumentBrowserConfig docVisualizerConfig;

  // cache for doc meta data
  private final Map<String, Map<String, Set<SMetaAnnotation>>> docMetaDataCache;

  private IndexedContainer container;

  private DocBrowserTable(DocBrowserPanel parent) {

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

  private String generateCell(List<String> path, MetaDataCol metaDatum) {
    Set<SMetaAnnotation> metaData = new LinkedHashSet<>();
    if (path != null && !path.isEmpty()) {
      metaData = getDocMetaData(path.get(path.size() - 1));
    }

    // lookup meta data
    for (SMetaAnnotation a : metaData) {
      if (metaDatum.namespace != null && metaDatum.namespace.equals(a.getNamespace())
          && metaDatum.name.equals(a.getName())) {
        return a.getValue_STEXT();
      }

      if (metaDatum.namespace == null && a.getNamespace() == null
          && metaDatum.name.equals(a.getName())) {
        return a.getValue_STEXT();
      }
    }

    return "n/a";
  }

  public Button generateInfoButtonCell(final String docName) {
    Button btn = new Button();
    btn.setStyleName(ChameleonTheme.BUTTON_BORDERLESS);
    btn.setIcon(INFO_ICON);
    btn.addClickListener(event -> {

      Set<SMetaAnnotation> annos = getDocMetaData(docName);

      /**
       * Transforms to a list of key value pairs. The values concates the namespace and ordinary
       * value. Namespaces "NULL" are ignored.
       */
      // create datasource and bind it to a table
      BeanItemContainer<SMetaAnnotation> metaContainer =
          new BeanItemContainer<>(SMetaAnnotation.class);
      if (annos != null) {
        metaContainer.addAll(annos);
      }
      metaContainer.sort(new Object[] {"namespace", "name"}, new boolean[] {true, true});

      Table metaTable = new Table();
      metaTable.setContainerDataSource(metaContainer);
      metaTable.addGeneratedColumn("genname", (source, itemId, columnId) -> {
        SMetaAnnotation anno = metaContainer.getItem(itemId).getBean();
        String qName = anno.getName();
        if (anno.getNamespace() != null) {
          qName = anno.getNamespace() + ":" + qName;
        }
        Label l = new Label(qName);
        l.setSizeUndefined();
        return l;
      });
      metaTable.addGeneratedColumn("genvalue", (source, itemId, columnId) -> {
        SMetaAnnotation anno = metaContainer.getItem(itemId).getBean();
        Label l = new Label(anno.getValue_STEXT(), ContentMode.HTML);
        return l;
      });

      metaTable.setVisibleColumns("genname", "genvalue");

      metaTable.setColumnHeaders(new String[] {"Name", "Value"});
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


    });
    return btn;
  }

  private MetaColumns generateMetaColumns() {

    MetaColumns metaColumns = new MetaColumns();

    if (docVisualizerConfig == null) {
      return metaColumns;
    }

    if (docVisualizerConfig.getMetaDataColumns() != null) {
      MetaDataColumn[] metaDataCols = docVisualizerConfig.getMetaDataColumns();
      for (MetaDataColumn metaDataCol : metaDataCols) {
        metaColumns.visibleColumns
            .add(new MetaDataCol(metaDataCol.getNamespace(), metaDataCol.getName()));
      }
    }

    if (docVisualizerConfig.getOrderBy() != null) {
      OrderBy[] orderBys = docVisualizerConfig.getOrderBy();
      for (OrderBy orderBy : orderBys) {
        metaColumns.sortColumns
            .add(new MetaDataCol(orderBy.getNamespace(), orderBy.getName(), orderBy.isAscending()));
      }
    }

    return metaColumns;
  }

  private Panel generateVisualizerLinks(String docId) {
    Panel p = new Panel();
    VerticalLayout l = new VerticalLayout();
    p.addStyleName(ChameleonTheme.PANEL_BORDERLESS);

    if (docVisualizerConfig != null) {
      List<Visualizer> visualizers = docVisualizerConfig.getVisualizers();

      if (visualizers != null) {
        for (Visualizer rawVis : visualizers) {
          VisualizerRule visualizer = rawVis.toVisualizerRule();
          Button openVis = new Button(visualizer.getDisplayName());
          openVis.setDescription("open visualizer for document");
          openVis.addClickListener(new OpenVisualizerWindow(docId, visualizer, openVis));
          openVis.setStyleName(BaseTheme.BUTTON_LINK);
          openVis.setDisableOnClick(true);
          l.addComponent(openVis);
        }
      }
    }

    p.setContent(l);
    return p;
  }

  /**
   * Retrieves date from the cache or from the annis rest service for a specific document.
   *
   * @param document The document the data are fetched for.
   * @return The a list of meta data. Can be empty but never null.
   */
  private Set<SMetaAnnotation> getDocMetaData(String document) {
    // Check if the corpus already has a cached map
    Map<String, Set<SMetaAnnotation>> metaDataMap =
        docMetaDataCache.get(docBrowserPanel.getCorpus());

    if (metaDataMap == null) {
      // Create a new cached map for this corpus
      metaDataMap = new HashMap<>();
      docMetaDataCache.put(docBrowserPanel.getCorpus(), metaDataMap);
    }

    // Check if the cached map for this corpus already contains this document and directly return if when found
    Set<SMetaAnnotation> metaAnnos = metaDataMap.get(document);
    if (metaAnnos != null) {
      return metaAnnos;
    }

    // Retrieve the meta data annotations as list
    List<SMetaAnnotation> annos =
        Helper.getMetaDataDoc(docBrowserPanel.getCorpus(), document, UI.getCurrent());
    // Cache the documentation annotations for later use and return them
    metaAnnos = Collections.unmodifiableSet(new LinkedHashSet<>(annos));
    metaDataMap.put(document, metaAnnos);
    return metaAnnos;
  }

  public void setContainerFilter(Filter filter) {
    container.removeAllContainerFilters();
    container.addContainerFilter(filter);
  }

  /**
   * Updates the table with docnames and generate the additional columns defined by the user.
   *
   * @param docs the list of documents, wrapped in the {@link Annotation} POJO
   */
  void setDocuments(List<SDocument> docs) {

    container = new IndexedContainer();

    container.addContainerProperty(PROP_DOC_NAME, String.class, "n/a");
    MetaColumns metaCols = generateMetaColumns();

    for (MetaDataCol metaDatum : metaCols.visibleColumns) {
      container.addContainerProperty(metaDatum.getColName(), String.class, "n/a");
    }

    for (MetaDataCol metaDatum : metaCols.sortColumns) {
      container.addContainerProperty(metaDatum.getColName(), String.class, "n/a");
    }

    container.addContainerProperty("corpus path", String.class, "n/a");
    container.addContainerProperty("info", Button.class, null);
    container.addContainerProperty("visualizer", Panel.class, null);

    for (SDocument d : docs) {
      String doc = d.getName();
      String docId = d.getId();

      List<String> pathList = Helper.getCorpusPath(d.getId());
      if (pathList == null) {
        pathList = new LinkedList<>();
      }

      // Set a new separator:
      // corpus > ... > subcorpus > document
      String path = StringUtils.join(pathList, " > ");

      // use corpus path for row id, since it should be unique by annis db schema
      Item row = container.addItem(path);
      if (row != null) {
        row.getItemProperty(PROP_DOC_NAME).setValue(doc);

        // add the metadata columns.
        for (MetaDataCol metaDataCol : metaCols.visibleColumns) {
          String value = generateCell(pathList, metaDataCol);
          row.getItemProperty(metaDataCol.getColName()).setValue(value);
        }

        for (MetaDataCol metaDataCol : metaCols.sortColumns) {
          if (!metaCols.visibleColumns.contains(metaDataCol)) {
            // corpusName() holds the corpus path
            String value = generateCell(pathList, metaDataCol);
            row.getItemProperty(metaDataCol.getColName()).setValue(value);
          }
        }

        row.getItemProperty("corpus path").setValue(path);
        row.getItemProperty("visualizer").setValue(generateVisualizerLinks(docId));
        row.getItemProperty("info").setValue(generateInfoButtonCell(doc));
      }
    }
    setContainerDataSource(container);
    Object[] metaDataColNames = new Object[metaCols.visibleColumns.size()];

    for (int i = 0; i < metaDataColNames.length; i++) {
      metaDataColNames[i] = metaCols.visibleColumns.get(i).getColName();
    }

    Object[] columnNames =
        ArrayUtils.addAll(ArrayUtils.addAll(new Object[] {"document name"}, metaDataColNames),
            new Object[] {"corpus path", "visualizer", "info"});

    setVisibleColumns(columnNames);

    for (Object colName : columnNames) {
      setColumnHeader((String) colName, (String) colName);
    }

    sortByMetaData(metaCols.sortColumns);
  }

  /**
   * Sort the table by a given config. The config includes metadata keys and the table is sorted
   * lexicographically by their values. If not config for sorting is determined the document name is
   * used for sorting.
   */
  private void sortByMetaData(List<MetaDataCol> sortColumns) {
    if (sortColumns == null || sortColumns.isEmpty()) {
      sort(new Object[] {PROP_DOC_NAME}, new boolean[] {true});

      return;
    }

    Object[] sortByColumns = new Object[sortColumns.size()];
    boolean[] ascendingOrDescending = new boolean[sortColumns.size()];

    for (int i = 0; i < sortColumns.size(); i++) {

      sortByColumns[i] = sortColumns.get(i).getColName();

      ascendingOrDescending[i] = sortColumns.get(i).ascending;

    }

    sort(sortByColumns, ascendingOrDescending);

  }
}
