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
import annis.gui.beans.CorpusBrowserEntry;
import annis.gui.model.Query;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class CorpusBrowserPanel extends Panel
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    CorpusBrowserPanel.class);

  /**
   *
   */
  private static final long serialVersionUID = -1029743017413951838L;

  private AnnisCorpus corpus;

  private Table tblNodeAnno;

  private BeanItemContainer<CorpusBrowserEntry> containerNodeAnno;

  private Table tblEdgeTypes;

  private BeanItemContainer<CorpusBrowserEntry> containerEdgeType;

  private Table tblEdgeAnno;

  private BeanItemContainer<CorpusBrowserEntry> containerEdgeAnno;

  private Table tblMetaAnno;

  private BeanItemContainer<CorpusBrowserEntry> containerMetaAnno;

  private CitationLinkGenerator citationGenerator;

  private QueryController controller;

  public CorpusBrowserPanel(final AnnisCorpus corpus,
    QueryController controller)
  {
    super("Available annotations");
    this.corpus = corpus;
    this.controller = controller;

    setSizeFull();

    Accordion accordion = new Accordion();
    setContent(accordion);
    accordion.setSizeFull();

    containerNodeAnno = new BeanItemContainer<CorpusBrowserEntry>(
      CorpusBrowserEntry.class);
    containerNodeAnno.setItemSorter(new ExampleSorter());

    containerEdgeType = new BeanItemContainer<CorpusBrowserEntry>(
      CorpusBrowserEntry.class);
    containerEdgeType.setItemSorter(new ExampleSorter());

    containerEdgeAnno = new BeanItemContainer<CorpusBrowserEntry>(
      CorpusBrowserEntry.class);
    containerEdgeAnno.setItemSorter(new ExampleSorter());

    containerMetaAnno = new BeanItemContainer<CorpusBrowserEntry>(
      CorpusBrowserEntry.class);
    containerMetaAnno.setItemSorter(new ExampleSorter());

    citationGenerator = new CitationLinkGenerator();

    tblNodeAnno = new ExampleTable(citationGenerator, containerNodeAnno);
    tblNodeAnno.addValueChangeListener(new ExampleListener());

    tblEdgeTypes = new ExampleTable(citationGenerator, containerEdgeType);
    tblEdgeTypes.addValueChangeListener(new ExampleListener());

    tblEdgeAnno = new ExampleTable(citationGenerator, containerEdgeAnno);
    tblEdgeAnno.addValueChangeListener(new ExampleListener());

    tblMetaAnno = new ExampleTable(citationGenerator, containerMetaAnno);
    tblMetaAnno.addValueChangeListener(new ExampleListener());

    boolean stripNodeAnno = true;
    boolean stripEdgeName = true;
    boolean stripEdgeAnno = true;
    HashSet<String> nodeAnnoNames = new HashSet<String>();
    HashSet<String> edgeAnnoNames = new HashSet<String>();
    HashSet<String> edgeNames = new HashSet<String>();
    HashSet<String> fullEdgeNames = new HashSet<String>();
    boolean hasDominance = false;

    List<AnnisAttribute> attributes = fetchAnnos(corpus.getName());

    // do some preparations first
    for (AnnisAttribute a : attributes)
    {
      if (a.getType() == AnnisAttribute.Type.node)
      {
        // check for ambigous names
        String name = killNamespace(a.getName());
        if (nodeAnnoNames.contains(name))
        {
          stripNodeAnno = false;
        }
        nodeAnnoNames.add(name);
      }
      else if (a.getType() == AnnisAttribute.Type.edge)
      {
        fullEdgeNames.add(a.getEdgeName());

        // check if we need to add the general dominance example edge
        if (a.getSubtype() == AnnisAttribute.SubType.d)
        {
          hasDominance = true;
        }

        String annoName = killNamespace(a.getName());
        if (edgeAnnoNames.contains(annoName))
        {
          stripEdgeAnno = false;
        }
        edgeAnnoNames.add(annoName);

      }
    }

    // check if collected edge names are unique
    for (String edgeName : fullEdgeNames)
    {
      String name = killNamespace(edgeName);
      if (edgeNames.contains(name))
      {
        stripEdgeName = false;
      }
      edgeNames.add(name);
    }

    if (hasDominance)
    {
      CorpusBrowserEntry cbe = new CorpusBrowserEntry();
      cbe.setName("(dominance)");
      cbe.setCorpus(corpus);
      cbe.setExample("node & node & #1 > #2");
      containerEdgeType.addBean(cbe);
    }

    // secound round, fill the actual containers
    Set<String> metaAnnosKey = new HashSet<String>();
    for (AnnisAttribute a : attributes)
    {
      // if the annotation name is already in the example skip this.
      if (a.getType() == AnnisAttribute.Type.meta
        && !metaAnnosKey.contains(killNamespace(a.getName())))
      {
        String name = killNamespace(a.getName());
        metaAnnosKey.add(name);
        CorpusBrowserEntry cbe = new CorpusBrowserEntry();
        cbe.setName(name);
        cbe.setExample(
          "node & meta::" + name + "=\"" + getFirst(a.getValueSet()) + "\"");
        cbe.setCorpus(corpus);
        containerMetaAnno.addBean(cbe);
      }

      if (a.getType() == AnnisAttribute.Type.node)
      {
        String name = stripNodeAnno ? killNamespace(a.getName()) : a.getName();
        CorpusBrowserEntry cbe = new CorpusBrowserEntry();
        cbe.setName(name);
        cbe.setExample(name + "=\"" + getFirst(a.getValueSet()) + "\"");
        cbe.setCorpus(corpus);
        containerNodeAnno.addBean(cbe);
      }
      else if (a.getType() == AnnisAttribute.Type.edge)
      {
        // edge type entry (multiple entries will be removed automatically)
        CorpusBrowserEntry cbeEdgeType = new CorpusBrowserEntry();
        String name = stripEdgeName ? killNamespace(a.getEdgeName()) : a.
          getEdgeName();
        cbeEdgeType.setName(name);
        cbeEdgeType.setCorpus(corpus);
        if (a.getSubtype() == AnnisAttribute.SubType.p)
        {
          cbeEdgeType.setExample(
            "node & node & #1 ->" + killNamespace(name) + " #2");
        }
        else if (a.getSubtype() == AnnisAttribute.SubType.d)
        {
          cbeEdgeType.setExample(
            "node & node & #1 >" + killNamespace(name) + " #2");
        }
        containerEdgeType.addBean(cbeEdgeType);

        // the edge annotation entry

        if (!a.getValueSet().isEmpty())
        {
          CorpusBrowserEntry cbeEdgeAnno = new CorpusBrowserEntry();
          String edgeAnno = stripEdgeAnno
            ? killNamespace(a.getName()) : a.getName();
          cbeEdgeAnno.setName(edgeAnno);
          cbeEdgeAnno.setCorpus(corpus);
          if (a.getSubtype() == AnnisAttribute.SubType.p)
          {
            cbeEdgeAnno.setExample("node & node & #1 ->"
              + killNamespace(a.getEdgeName()) + "["
              + killNamespace(a.getName()) + "=\""
              + getFirst(a.getValueSet())
              + "\"] #2");
          }
          else if (a.getSubtype() == AnnisAttribute.SubType.d)
          {
            cbeEdgeAnno.setExample("node & node & #1 >["
              + killNamespace(a.getName()) + "=\""
              + getFirst(a.getValueSet()) + "\"] #2");
          }
          containerEdgeAnno.addBean(cbeEdgeAnno);
        }
      }
    }

    tblNodeAnno.setSortContainerPropertyId("name");
    tblEdgeTypes.setSortContainerPropertyId("name");
    tblEdgeAnno.setSortContainerPropertyId("name");

    if (containerNodeAnno.size() == 0)
    {
      placeEmptyLabel(accordion, "Node Annotations");
    }
    else
    {
      accordion.addTab(tblNodeAnno, "Node Annotations", null);
    }

    if (tblEdgeAnno.getContainerDataSource().size() == 0)
    {
      placeEmptyLabel(accordion, "Edge Annotations");
    }
    else
    {
      accordion.addTab(tblEdgeAnno, "Edge Annotations", null);
    }

    if (tblEdgeTypes.getContainerDataSource().size() == 0)
    {
      placeEmptyLabel(accordion, "Edge Types");
    }
    else
    {
      accordion.addTab(tblEdgeTypes, "Edge Types", null);
    }

    if (tblMetaAnno.getContainerDataSource().size() == 0)
    {
      placeEmptyLabel(accordion, "Meta Annotations");
    }
    else
    {
      accordion.addTab(tblMetaAnno, "Meta Annotations", null);
    }
  }

  private List<AnnisAttribute> fetchAnnos(String toplevelCorpus)
  {
    Collection<AnnisAttribute> result = new ArrayList<AnnisAttribute>();
    try
    {
      WebResource service = Helper.getAnnisWebResource();
      if (service != null)
      {
        WebResource query = service.path("query").path("corpora")
          .path(URLEncoder.encode(toplevelCorpus, "UTF-8"))
          .path("annotations")
          .queryParam("fetchvalues", "true")
          .queryParam("onlymostfrequentvalues", "true");
        result = query.get(new AnnisAttributeListType());
      }
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
    return new LinkedList<AnnisAttribute>(result);
  }

  public static class ExampleTable extends Table
  {

    public ExampleTable(CitationLinkGenerator citationGenerator,
      BeanItemContainer<CorpusBrowserEntry> container)
    {
      setContainerDataSource(container);
      addGeneratedColumn("genlink", citationGenerator);
      setSizeFull();
      setSelectable(true);
      setMultiSelect(false);

      addGeneratedColumn("example", new ColumnGenerator()
      {
        @Override
        public Object generateCell(Table source, Object itemId, Object columnId)
        {
          CorpusBrowserEntry corpusBrowserEntry = (CorpusBrowserEntry) itemId;
          Label l = new Label(corpusBrowserEntry.getExample());
          l.setContentMode(ContentMode.TEXT);
          l.addStyleName("corpus-font-force");
          return l;
        }
      });

      setVisibleColumns(new String[]
      {
        "name", "example", "genlink"
      });
      setColumnHeaders(new String[]
      {
        "Name", "Example (click to use query)", "URL"
      });
      setColumnExpandRatio("name", 0.3f);
      setColumnExpandRatio("example", 0.7f);
      setImmediate(true);
    }
  }

  public class ExampleListener implements ValueChangeListener
  {

    @Override
    public void valueChange(ValueChangeEvent event)
    {

      CorpusBrowserEntry cbe = (CorpusBrowserEntry) event.getProperty().
        getValue();
      Set<String> corpusNameSet = new HashSet<String>();
      corpusNameSet.add(corpus.getName());
      if (controller != null)
      {
        controller.setQuery(new Query(cbe.getExample(), corpusNameSet));
      }
    }
  }

  public static class ExampleSorter extends DefaultItemSorter
  {

    @Override
    protected int compareProperty(Object propertyId, boolean sortDirection,
      Item item1, Item item2)
    {
      if ("name".equals(propertyId))
      {
        String val1 = (String) item1.getItemProperty(propertyId).getValue();
        String val2 = (String) item2.getItemProperty(propertyId).getValue();

        if (sortDirection)
        {
          return val1.compareToIgnoreCase(val2);
        }
        else
        {
          return val2.compareToIgnoreCase(val1);
        }
      }
      else
      {
        return super.compareProperty(propertyId, sortDirection, item1,
          item2);
      }
    }
  }

  private String killNamespace(String qName)
  {
    String[] splitted = qName.split(":");
    return splitted[splitted.length - 1];
  }

  private String getFirst(Collection<String> list)
  {
    Iterator<String> it = list.iterator();
    return it.hasNext() ? it.next() : null;
  }

  private static class AnnisAttributeListType extends GenericType<List<AnnisAttribute>>
  {

    public AnnisAttributeListType()
    {
    }
  }

  /**
   * Places a label with the text "(no metadata)" in the centered middle of the accordion
   * tab.
   *
   * @param accordion the target accordion
   * @param caption is set as caption of the accordion tab
   */
  private void placeEmptyLabel(Accordion accordion, String caption)
  {
    VerticalLayout v = new VerticalLayout();
    v.setSizeFull();
    Label l = new Label("(no metadata)");
    v.addComponent(l);
    l.setSizeUndefined();
    v.setComponentAlignment(l, Alignment.MIDDLE_CENTER);
    accordion.addTab(v, "Meta Annotations", null);
    l.setSizeUndefined();
  }
}
