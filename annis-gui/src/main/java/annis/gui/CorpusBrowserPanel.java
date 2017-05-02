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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;

import annis.gui.beans.CorpusBrowserEntry;
import annis.gui.objects.Query;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Helper;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisCorpus;
/**
 *
 * @author thomas
 */
@DesignRoot
public class CorpusBrowserPanel extends Panel
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    CorpusBrowserPanel.class);
  
  private final static Escaper urlPathEscape = UrlEscapers.urlPathSegmentEscaper();

  /**
   *
   */
  private static final long serialVersionUID = -1029743017413951838L;

  private AnnisCorpus corpus;

  private ExampleTable tblNodeAnno;

  private BeanItemContainer<CorpusBrowserEntry> containerNodeAnno;
  
  private Label lblNoNodeAnno;

  private ExampleTable tblEdgeTypes;

  private BeanItemContainer<CorpusBrowserEntry> containerEdgeType;
  
  private Label lblNoEdgeTypes;

  private ExampleTable tblEdgeAnno;

  private BeanItemContainer<CorpusBrowserEntry> containerEdgeAnno;
  
  private Label lblNoEdgeAnno;

  private ExampleTable tblMetaAnno;

  private BeanItemContainer<CorpusBrowserEntry> containerMetaAnno;
  
  private Label lblNoMetaAnno;

  private CitationLinkGenerator citationGenerator;

  private QueryController controller;
  
  private Accordion accordion;

  public CorpusBrowserPanel()
  {
    this(null, null);
  }
  
  public CorpusBrowserPanel(final AnnisCorpus corpus,
    QueryController controller)
  {
    super("Available annotations");
    this.corpus = corpus;
    this.controller = controller;

    Design.read("CorpusBrowserPanel.html", this);


    containerNodeAnno = new BeanItemContainer<>(
      CorpusBrowserEntry.class);
    containerNodeAnno.setItemSorter(new ExampleSorter());

    containerEdgeType = new BeanItemContainer<>(
      CorpusBrowserEntry.class);
    containerEdgeType.setItemSorter(new ExampleSorter());

    containerEdgeAnno = new BeanItemContainer<>(
      CorpusBrowserEntry.class);
    containerEdgeAnno.setItemSorter(new ExampleSorter());

    containerMetaAnno = new BeanItemContainer<>(
      CorpusBrowserEntry.class);
    containerMetaAnno.setItemSorter(new ExampleSorter());

    citationGenerator = new CitationLinkGenerator();

    tblNodeAnno.addValueChangeListener(new ExampleListener());
    tblEdgeTypes.addValueChangeListener(new ExampleListener());
    tblEdgeAnno.addValueChangeListener(new ExampleListener());
    tblMetaAnno.addValueChangeListener(new ExampleListener());
    
    tblNodeAnno.setContainerDataSource(containerNodeAnno);
    tblEdgeAnno.setContainerDataSource(containerEdgeAnno);
    tblEdgeTypes.setContainerDataSource(containerEdgeType);
    tblMetaAnno.setContainerDataSource(containerMetaAnno);
    
    tblNodeAnno.setCitationLinkGenerator(citationGenerator);
    tblEdgeAnno.setCitationLinkGenerator(citationGenerator);
    tblEdgeTypes.setCitationLinkGenerator(citationGenerator);
    tblMetaAnno.setCitationLinkGenerator(citationGenerator);

    boolean stripNodeAnno = true;
    boolean stripEdgeName = true;
    boolean stripEdgeAnno = true;
    HashSet<String> nodeAnnoNames = new HashSet<>();
    HashSet<String> edgeAnnoNames = new HashSet<>();
    HashSet<String> edgeNames = new HashSet<>();
    HashSet<String> fullEdgeNames = new HashSet<>();
    boolean hasDominance = false;
    boolean hasEmptyDominance = false;

    List<AnnisAttribute> attributes = corpus == null ? new LinkedList<AnnisAttribute>() 
        : fetchAnnos(corpus.getName());

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
          if(a.getEdgeName() == null || a.getEdgeName().isEmpty())
          {
            hasEmptyDominance = true;
          }
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

    if (hasDominance && !hasEmptyDominance)
    {
      CorpusBrowserEntry cbe = new CorpusBrowserEntry();
      cbe.setName("(dominance)");
      cbe.setCorpus(corpus);
      cbe.setExample("node & node & #1 > #2");
      containerEdgeType.addBean(cbe);
    }

    // secound round, fill the actual containers
    Set<String> metaAnnosKey = new HashSet<>();
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
        if((name == null || name.isEmpty()) && a.getSubtype() == AnnisAttribute.SubType.d)
        {
          cbeEdgeType.setName("(dominance)");
        }
        else
        {
          cbeEdgeType.setName(name);
        }
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
      lblNoNodeAnno.setVisible(true);
      tblNodeAnno.setVisible(false);;
    }
    

    if (tblEdgeAnno.getContainerDataSource().size() == 0)
    {
      lblNoEdgeAnno.setVisible(true);
      tblEdgeAnno.setVisible(false);
    }
    

    if (tblEdgeTypes.getContainerDataSource().size() == 0)
    {
      lblNoEdgeTypes.setVisible(true);
      tblEdgeTypes.setVisible(false);
    }
    

    if (tblMetaAnno.getContainerDataSource().size() == 0)
    {
      lblNoMetaAnno.setVisible(true);
      tblMetaAnno.setVisible(false);
    }
    
  }

  private List<AnnisAttribute> fetchAnnos(String toplevelCorpus)
  {
    Collection<AnnisAttribute> result = new ArrayList<>();
    try
    {
      WebResource service = Helper.getAnnisWebResource();
      if (service != null)
      {
        WebResource query = service.path("query").path("corpora")
          .path(urlPathEscape.escape(toplevelCorpus))
          .path("annotations")
          .queryParam("fetchvalues", "true")
          .queryParam("onlymostfrequentvalues", "true");
        result = query.get(new AnnisAttributeListType());
      }
    }
    catch (UniformInterfaceException | ClientHandlerException ex)
    {
      log.error(null, ex);
      if(!AnnisBaseUI.handleCommonError(ex, "fetch example annotations"))
      {
        Notification.show(
          "Remote exception: " + ex.getLocalizedMessage(),
          Notification.Type.WARNING_MESSAGE);
      }
    }
    return new LinkedList<>(result);
  }

  public class ExampleListener implements ValueChangeListener
  {

    @Override
    public void valueChange(ValueChangeEvent event)
    {

      CorpusBrowserEntry cbe = (CorpusBrowserEntry) event.getProperty().
        getValue();
      Set<String> corpusNameSet = new HashSet<>();
      if(corpus != null)
      {
        corpusNameSet.add(corpus.getName());
      }
      if (controller != null && cbe != null)
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
    if(qName == null)
    {
      return "";
    }
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
}
