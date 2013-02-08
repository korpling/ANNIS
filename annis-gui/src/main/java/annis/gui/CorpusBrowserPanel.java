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

import annis.gui.beans.CorpusBrowserEntry;
import annis.gui.controlpanel.ControlPanel;
import annis.service.objects.AnnisAttribute;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import java.net.URLEncoder;
import java.util.*;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author thomas
 */
public class CorpusBrowserPanel extends Panel
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CorpusBrowserPanel.class);

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
  private CitationLinkGenerator citationGenerator;
  private ControlPanel controlPanel;

  public CorpusBrowserPanel(final AnnisCorpus corpus,
    ControlPanel controlPanel)
  {
    super("Available annotations");
    this.corpus = corpus;
    this.controlPanel = controlPanel;

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

    citationGenerator = new CitationLinkGenerator();

    tblNodeAnno = new ExampleTable(citationGenerator, containerNodeAnno);
    tblNodeAnno.addListener(new ExampleListener());

    tblEdgeTypes = new ExampleTable(citationGenerator, containerEdgeType);
    tblEdgeTypes.addListener(new ExampleListener());
    
    tblEdgeAnno = new ExampleTable(citationGenerator, containerEdgeAnno);
    tblEdgeAnno.addListener(new ExampleListener());

    accordion.addTab(tblNodeAnno, "Node annotations", null);
    accordion.addTab(tblEdgeTypes, "Edge types", null);
    accordion.addTab(tblEdgeAnno, "Edge annotations", null);
  
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
    for(AnnisAttribute a : attributes)
    {
      if(a.getType() == AnnisAttribute.Type.node)
      {
        // check for ambigous names
        String name = killNamespace(a.getName());
        if(nodeAnnoNames.contains(name))
        {
          stripNodeAnno = false;
        }
        nodeAnnoNames.add(name);
      }      
      else if(a.getType() == AnnisAttribute.Type.edge)
      {
        fullEdgeNames.add(a.getEdgeName());
        
        // check if we need to add the general dominance example edge
        if(a.getSubtype() == AnnisAttribute.SubType.d)
        {
          hasDominance = true;
        }
        
        String annoName = killNamespace(a.getName());
        if(edgeAnnoNames.contains(annoName))
        {
          stripEdgeAnno = false;
        }
        edgeAnnoNames.add(annoName);
        
      }
    }
    
    // check if collected edge names are unique
    for(String edgeName : fullEdgeNames)
    {
      String name = killNamespace(edgeName);
      if(edgeNames.contains(name))
      {
        stripEdgeName = false;
      }
      edgeNames.add(name);
    }
    
    if(hasDominance)
    {
      CorpusBrowserEntry cbe = new CorpusBrowserEntry();
      cbe.setName("(dominance)");
      cbe.setCorpus(corpus);
      cbe.setExample("node & node & #1 > #2");
      containerEdgeType.addBean(cbe);
    }

    // secound round, fill the actual containers
    for(AnnisAttribute a : attributes)
    {
      if(a.getType() == AnnisAttribute.Type.node)
      {
        String name = stripNodeAnno ? killNamespace(a.getName()) : a.getName();
        CorpusBrowserEntry cbe = new CorpusBrowserEntry();
        cbe.setName(name);
        cbe.setExample(name + "=\"" + getFirst(a.getValueSet()) + "\"");
        cbe.setCorpus(corpus);
        containerNodeAnno.addBean(cbe);
      }
      else if(a.getType() == AnnisAttribute.Type.edge)
      {
        // edge type entry (multiple entries will be removed automatically)
        CorpusBrowserEntry cbeEdgeType = new CorpusBrowserEntry();
        String name = stripEdgeName ? killNamespace(a.getEdgeName()) : a.getEdgeName();
        cbeEdgeType.setName(name);
        cbeEdgeType.setCorpus(corpus);
        if(a.getSubtype() == AnnisAttribute.SubType.p)
        {
          cbeEdgeType.setExample("node & node & #1 ->" + killNamespace(name) + " #2");
        }
        else if(a.getSubtype() == AnnisAttribute.SubType.d)
        {
          cbeEdgeType.setExample("node & node & #1 >" + killNamespace(name) + " #2");
        }
        containerEdgeType.addBean(cbeEdgeType);
        
        // the edge annotation entry
        CorpusBrowserEntry cbeEdgeAnno = new CorpusBrowserEntry();
        String edgeAnno = stripEdgeAnno 
          ? killNamespace(a.getName()) : a.getName();
        cbeEdgeAnno.setName(edgeAnno);
        cbeEdgeAnno.setCorpus(corpus);
        if(a.getSubtype() == AnnisAttribute.SubType.p)
        {
          cbeEdgeAnno.setExample("node & node & #1 ->"
            + killNamespace(a.getEdgeName()) + "["
            + killNamespace(a.getName()) + "=\""
            + getFirst(a.getValueSet())
            + "\"] #2");
        }
        else if(a.getSubtype() == AnnisAttribute.SubType.d)
        {
          cbeEdgeAnno.setExample("node & node & #1 >[" 
           + killNamespace(a.getName()) + "=\""
           + getFirst(a.getValueSet()) + "\"] #2");
        }
        containerEdgeAnno.addBean(cbeEdgeAnno);
      }
    }

    tblNodeAnno.setSortContainerPropertyId("name");
    tblEdgeTypes.setSortContainerPropertyId("name");
    tblEdgeAnno.setSortContainerPropertyId("name");
      
  }

  private List<AnnisAttribute> fetchAnnos(String toplevelCorpus)
  {
    Collection<AnnisAttribute> result = new ArrayList<AnnisAttribute>();
    try
    {
      WebResource service = Helper.getAnnisWebResource();
      if(service != null)
      {
        WebResource query = service.path("query").path("corpora")
          .path(URLEncoder.encode(toplevelCorpus, "UTF-8"))
          .path("annotations")
          .queryParam("fetchvalues", "true")
          .queryParam("onlymostfrequentvalues", "true");
        result = query.get(new GenericType<List<AnnisAttribute>>(){});
      }
    }
    catch(Exception ex)
    {
      log.error(null, ex);
      Notification.show(
        "Remote exception: " + ex.getLocalizedMessage(),
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
      CorpusBrowserEntry cbe = (CorpusBrowserEntry) event.getProperty().getValue();
      Set<String> corpusNameSet = new HashSet<String>();
      corpusNameSet.add(corpus.getName());
      if(controlPanel != null)
      {
        controlPanel.setQuery(cbe.getExample(), corpusNameSet);
      }
    }
  }

  public static class ExampleSorter extends DefaultItemSorter
  {

    @Override
    protected int compareProperty(Object propertyId, boolean sortDirection,
      Item item1, Item item2)
    {
      if("name".equals(propertyId))
      {
        String val1 = (String) item1.getItemProperty(propertyId).getValue();
        String val2 = (String) item2.getItemProperty(propertyId).getValue();

        if(sortDirection)
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
}
