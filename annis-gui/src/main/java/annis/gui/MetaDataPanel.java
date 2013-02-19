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
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import java.net.URLEncoder;
import java.util.*;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class MetaDataPanel extends Panel
{
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(MetaDataPanel.class);
  
  private VerticalLayout layout;
  private String toplevelCorpusName;
  private String documentName;

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

    // load meta data from service
    BeanItemContainer<Annotation> mData =
      new BeanItemContainer<Annotation>(Annotation.class);

    // are we called from the corpusBrowser or there is no subcorpus stay here:
    if (documentName == null)
    {
      mData.addAll(getMetaData(toplevelCorpusName, null));
      layout.addComponent(setupTable(mData));
    }
    else
    {
      Map<Integer, List<Annotation>> hashMData = splitListAnnotations();
      List<BeanItemContainer<Annotation>> l = putInBeanContainer(hashMData);
      Accordion accordion = new Accordion();
      accordion.setSizeFull();
      layout.addComponent(accordion);

      for (BeanItemContainer<Annotation> item : l)
      {
        String corpusName = item.getIdByIndex(0).getCorpusName();
        accordion.addTab(setupTable(item),
          (toplevelCorpusName.equals(corpusName)) ? "corpus: " + corpusName
          : "document: " + corpusName);
      }
    }
  }

  private List<Annotation> getMetaData(String toplevelCorpusName,
    String documentName)
  {
    List<Annotation> result = new ArrayList<Annotation>();
    WebResource res = Helper.getAnnisWebResource();
    try
    {
      res = res.path("query").path("corpora")
        .path(URLEncoder.encode(toplevelCorpusName, "UTF-8"));
      if (documentName != null)
      {
        res = res.path(documentName);
      }
      res = res.path("metadata");

      result = res.get(new GenericType<List<Annotation>>() {});
    }
    catch (Exception ex)
    {
      log.error(
        null, ex);
      Notification.show("Remote exception: "
        + ex.getLocalizedMessage(),
        Notification.Type.WARNING_MESSAGE);
    }
    return result;
  }

  private Table setupTable(BeanItemContainer<Annotation> metaData)
  {
    final BeanItemContainer<Annotation> mData = metaData;
    Table tblMeta = new Table();
    tblMeta.setContainerDataSource(mData);
    tblMeta.addGeneratedColumn("genname", new Table.ColumnGenerator()
    {
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
    });
    tblMeta.addGeneratedColumn("genvalue", new Table.ColumnGenerator()
    {
      @Override
      public Component generateCell(Table source, Object itemId, Object columnId)
      {
        Annotation anno = mData.getItem(itemId).getBean();
        Label l = new Label(anno.getValue(), Label.CONTENT_RAW);
        return l;
      }
    });


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

  private Map<Integer, List<Annotation>> splitListAnnotations()
  {
    List<Annotation> metadata = getMetaData(toplevelCorpusName, documentName);
    Map<Integer, List<Annotation>> hashMetaData =
      new TreeMap<Integer, List<Annotation>>(Collections.reverseOrder());

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
}
