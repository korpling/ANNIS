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
package annis.gui;

import annis.libgui.Helper;
import annis.model.Annotation;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Table.ColumnGenerator;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import org.slf4j.LoggerFactory;

/**
 * Provides all corpus annotations for a corpus or for a specific search result.
 *
 * // TODO cleanup the toplevelCorpus side effects.
 *
 * @author thomas
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class MetaDataPanel extends Panel implements Property.ValueChangeListener
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    MetaDataPanel.class);

  private VerticalLayout layout;

  private String toplevelCorpusName;

  // this is only set if the metadata panel is called from a specific result.
  private String documentName;

  private ComboBox corpusSelection;

  // last selected corpus or document of the combobox
  private String lastSelectedItem;

  // holds all corpus and documents for the combox in the corpus browser panel
  private List<Annotation> docs;

  // holds the current corpus annotation table, when called from corpus browser
  private Table corpusAnnotationTable = null;

  /**
   * this empty label is currently use for empty metadata list on the left side
   * of the corpusbrowser
   */
  private Label emptyLabel = new Label("none");

  public MetaDataPanel(String toplevelCorpusName)
  {
    this(toplevelCorpusName, null);
  }

  public MetaDataPanel(String toplevelCorpusName, String documentName)
  {
    super("Metadata");

    this.toplevelCorpusName = toplevelCorpusName;
    this.documentName = documentName;

    setSizeFull();
    layout = new VerticalLayout();
    setContent(layout);
    layout.setSizeFull();

    if (documentName == null)
    {
      docs = getAllSubcorpora(toplevelCorpusName);

      HorizontalLayout selectionLayout = new HorizontalLayout();
      Label selectLabel = new Label("Select corpus/document: ");
      corpusSelection = new ComboBox();
      selectionLayout.addComponents(selectLabel, corpusSelection);
      layout.addComponent(selectionLayout);

      selectLabel.setSizeUndefined();

      corpusSelection.setWidth(100, Unit.PERCENTAGE);
      corpusSelection.setHeight("-1px");
      corpusSelection.addValueChangeListener(this);

      selectionLayout.setWidth(100, Unit.PERCENTAGE);
      selectionLayout.setHeight("-1px");
      selectionLayout.setSpacing(true);
      selectionLayout.setComponentAlignment(selectLabel, Alignment.MIDDLE_LEFT);
      selectionLayout.setComponentAlignment(corpusSelection,
        Alignment.MIDDLE_LEFT);
      selectionLayout.setExpandRatio(selectLabel, 0.4f);
      selectionLayout.setExpandRatio(corpusSelection, 0.6f);

      corpusSelection.addItem(toplevelCorpusName);
      corpusSelection.select(toplevelCorpusName);
      corpusSelection.setNullSelectionAllowed(false);
      corpusSelection.setImmediate(true);

      for (Annotation c : docs)
      {
        corpusSelection.addItem(c.getName());
      }
    }
    else
    {
      Map<Integer, List<Annotation>> hashMData = splitListAnnotations();
      List<BeanItemContainer<Annotation>> l = putInBeanContainer(hashMData);
      Accordion accordion = new Accordion();
      accordion.setSizeFull();

      // set output to none if no metadata are available
      if (l.isEmpty())
      {
        addEmptyLabel();
      }
      else
      {

        for (BeanItemContainer<Annotation> item : l)
        {
          String corpusName = item.getIdByIndex(0).getCorpusName();
          String path = toplevelCorpusName.equals(corpusName) ? "corpus: " + corpusName
            : "document: " + corpusName;

          if (item.getItemIds().isEmpty())
          {
            accordion.addTab(new Label("none"), path);
          }
          else
          {
            accordion.addTab(setupTable(item), path);
          }
        }

        layout.addComponent(accordion);
      }
    }
  }

  private Table setupTable(BeanItemContainer<Annotation> metaData)
  {
    final BeanItemContainer<Annotation> mData = metaData;
    Table tblMeta = new Table();
    tblMeta.setContainerDataSource(mData);
    tblMeta.addGeneratedColumn("genname", new MetaTableNameGenerator(mData));
    tblMeta.addGeneratedColumn("genvalue", new MetaTableValueGenerator(mData));


    tblMeta.setVisibleColumns(new String[]
    {
      "genname", "genvalue"
    });

    tblMeta.setColumnHeaders(new String[]
    {
      "Name", "Value"
    });
    tblMeta.setSizeFull();
    tblMeta.setColumnWidth("genname", -1);
    tblMeta.setColumnExpandRatio("genvalue", 1.0f);
    tblMeta.setSortContainerPropertyId("name");
    return tblMeta;
  }

  /**
   * Returns empty map if no metadata are available.
   */
  private Map<Integer, List<Annotation>> splitListAnnotations()
  {
    List<Annotation> metadata = Helper.getMetaData(toplevelCorpusName,
      documentName);

    Map<Integer, List<Annotation>> hashMetaData = new HashMap<Integer, List<Annotation>>();


    if (metadata != null && !metadata.isEmpty())
    {
      // if called from corpus browser sort the other way around.
      if (documentName != null)
      {
        hashMetaData =
          new TreeMap<Integer, List<Annotation>>(Collections.reverseOrder());
      }
      else
      {
        hashMetaData = new TreeMap<Integer, List<Annotation>>();
      }

      for (Annotation metaDatum : metadata)
      {
        int pre = metaDatum.getPre();
        if (!hashMetaData.containsKey(pre))
        {
          hashMetaData.put(pre, new ArrayList<Annotation>());
          hashMetaData.get(pre).add(metaDatum);
        }
        else
        {
          hashMetaData.get(pre).add(metaDatum);
        }
      }
    }

    return hashMetaData;
  }

  private List<BeanItemContainer<Annotation>> putInBeanContainer(
    Map<Integer, List<Annotation>> splittedAnnotationsList)
  {
    List<BeanItemContainer<Annotation>> listOfBeanItemCon =
      new ArrayList<BeanItemContainer<Annotation>>();



    for (List<Annotation> list : splittedAnnotationsList.values())
    {
      BeanItemContainer<Annotation> metaContainer =
        new BeanItemContainer<Annotation>(Annotation.class);
      metaContainer.addAll(list);

      listOfBeanItemCon.add(metaContainer);
    }
    return listOfBeanItemCon;
  }

  private List<Annotation> getAllSubcorpora(String toplevelCorpusName)
  {

    WebResource res = Helper.getAnnisWebResource();
    try
    {
      res = res.path("meta").path("docnames")
        .path(URLEncoder.encode(toplevelCorpusName, "UTF-8"));
      docs = res.get(new AnnotationListType());
    }
    catch (UniformInterfaceException ex)
    {
      log.error(null, ex);
      Notification.show(
        "Remote exception: " + ex.getLocalizedMessage(),
        Notification.Type.WARNING_MESSAGE);
    }
    catch (ClientHandlerException ex)
    {
      log.error(null, ex);
      Notification.show(
        "Remote exception: " + ex.getLocalizedMessage(),
        Notification.Type.WARNING_MESSAGE);
    }
    catch (UnsupportedEncodingException ex)
    {
      log.error(null, ex);
      Notification.show(
        "UTF-8 encoding is not supported on server, this is weird: " + ex.
        getLocalizedMessage(),
        Notification.Type.WARNING_MESSAGE);
    }

    return docs;
  }

  @Override
  public void valueChange(Property.ValueChangeEvent event)
  {
    if (lastSelectedItem == null
      || !lastSelectedItem.equals(event.getProperty().getValue()))
    {
      lastSelectedItem = event.getProperty().toString();
      List<Annotation> metaData = Helper.getMetaDataDoc(toplevelCorpusName,
        lastSelectedItem);

      if (metaData == null || metaData.isEmpty())
      {
        super.setCaption("No metadata available");
        addEmptyLabel();
        if (corpusAnnotationTable != null)
        {
          corpusAnnotationTable.removeAllItems();
        }
      }
      else
      {
        super.setCaption("Metadata");
        removeEmptyLabel();
        loadTable(toplevelCorpusName, metaData);
      }
    }
  }

  private void loadTable(String item, List<Annotation> metaData)
  {
    BeanItemContainer<Annotation> metaContainer =
      new BeanItemContainer<Annotation>(Annotation.class);
    metaContainer.addAll(metaData);

    if (corpusAnnotationTable != null)
    {
      layout.removeComponent(corpusAnnotationTable);
    }

    layout.removeComponent(emptyLabel);
    corpusAnnotationTable = setupTable(metaContainer);
    corpusAnnotationTable.setHeight(100, Unit.PERCENTAGE);
    corpusAnnotationTable.setWidth(100, Unit.PERCENTAGE);
    layout.addComponent(corpusAnnotationTable);
    layout.setExpandRatio(corpusAnnotationTable, 1.0f);
  }

  /**
   * Places a label in the middle center of the corpus browser panel.
   */
  private void addEmptyLabel()
  {
    if (emptyLabel == null)
    {
      emptyLabel = new Label("none");
    }

    if (corpusAnnotationTable != null)
    {
      layout.removeComponent(corpusAnnotationTable);
    }

    layout.addComponent(emptyLabel);

    // this has only an effect after adding the component to a parent. Bug by vaadin?
    emptyLabel.setSizeUndefined();

    layout.setComponentAlignment(emptyLabel, Alignment.MIDDLE_CENTER);
    layout.setExpandRatio(emptyLabel, 1.0f);
  }

  private void removeEmptyLabel()
  {
    if (emptyLabel != null)
    {
      layout.removeComponent(emptyLabel);
    }
  }

  private static class AnnotationListType extends GenericType<List<Annotation>>
  {

    public AnnotationListType()
    {
    }
  }

  private static class MetaTableNameGenerator implements ColumnGenerator
  {

    private final BeanItemContainer<Annotation> mData;

    public MetaTableNameGenerator(
      BeanItemContainer<Annotation> mData)
    {
      this.mData = mData;
    }

    @Override
    public Component generateCell(Table source, Object itemId, Object columnId)
    {
      Annotation anno = mData.getItem(itemId).getBean();
      String qName = anno.getName();
      if (anno.getNamespace() != null)
      {
        qName = anno.getNamespace() + ":" + qName;
      }
      Label l = new Label(qName);
      l.setSizeUndefined();
      return l;
    }
  }

  private static class MetaTableValueGenerator implements ColumnGenerator
  {

    private final BeanItemContainer<Annotation> mData;

    public MetaTableValueGenerator(
      BeanItemContainer<Annotation> mData)
    {
      this.mData = mData;
    }

    @Override
    public Component generateCell(Table source, Object itemId, Object columnId)
    {
      Annotation anno = mData.getItem(itemId).getBean();
      Label l = new Label(anno.getValue(), Label.CONTENT_RAW);
      return l;
    }
  }
}
