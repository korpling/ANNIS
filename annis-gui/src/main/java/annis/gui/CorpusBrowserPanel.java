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

import annis.exceptions.AnnisServiceFactoryException;
import annis.gui.beans.CorpusBrowserEntry;
import annis.gui.controlpanel.ControlPanel;
import annis.gui.controlpanel.CorpusListPanel;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisAttributeSet;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class CorpusBrowserPanel extends Panel
{

  private long corpusId;
  private Table tblNodeAnno;
  private BeanItemContainer<CorpusBrowserEntry> containerNodeAnno;

  public CorpusBrowserPanel(final long corpusId, final ControlPanel controlPanel)
  {
    super("Available annotations");
    this.corpusId = corpusId;

    setSizeFull();

    Accordion accordion = new Accordion();
    setContent(accordion);
    accordion.setSizeFull();

    containerNodeAnno = new BeanItemContainer<CorpusBrowserEntry>(CorpusBrowserEntry.class);
    containerNodeAnno.setItemSorter(new ExampleSorter());
    
    tblNodeAnno = new Table();
    tblNodeAnno.setSizeFull();
    tblNodeAnno.setSelectable(true);
    tblNodeAnno.setMultiSelect(false);
    tblNodeAnno.setContainerDataSource(containerNodeAnno);
    tblNodeAnno.addGeneratedColumn("genlink", new Table.ColumnGenerator() {

      @Override
      public Component generateCell(Table source, Object itemId, Object columnId)
      {
        return new Label("X");
      }
    });    
    tblNodeAnno.setVisibleColumns(new String[]
      {
        "name", "example", "genlink"
      });
    tblNodeAnno.setColumnHeaders(new String[]
      {
        "Name", "Example (click to use query)", "Url"
      });
    tblNodeAnno.setColumnExpandRatio("name", 0.5f);
    tblNodeAnno.setColumnExpandRatio("example", 0.5f);
    tblNodeAnno.setColumnWidth("genlink", 18);
    tblNodeAnno.setImmediate(true);
    tblNodeAnno.addListener(new Table.ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event)
      {
        CorpusBrowserEntry cbe = (CorpusBrowserEntry) event.getProperty().getValue();
        HashSet<Long> corpus = new HashSet<Long>();
        corpus.add(corpusId);
        if(controlPanel != null)
        {
          controlPanel.setQuery(cbe.getExample(), corpus);
        }
      }
    });

    accordion.addTab(tblNodeAnno, "Node annotations", null);
    accordion.addTab(new Label("test"), "Edge types", null);
    accordion.addTab(new Label("test"), "Edge annotations", null);
  }

  @Override
  public void attach()
  {
    super.attach();

    boolean stripNodeAnno = true;
    HashSet<String> nodeAnnoNames = new HashSet<String>();

    List<AnnisAttribute> attributes = fetchAnnos(corpusId);
    // check for ambigous names first
    for(AnnisAttribute a : attributes)
    {
      if(stripNodeAnno && a.getType() == AnnisAttribute.Type.node)
      {
        String name = killNamespace(a.getName());
        if(nodeAnnoNames.contains(name))
        {
          stripNodeAnno = false;
        }
        nodeAnnoNames.add(name);
      }

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
        cbe.setCorpusId(corpusId);
        containerNodeAnno.addBean(cbe);
      }

    }
    
    tblNodeAnno.setSortContainerPropertyId("name");
  }

  private List<AnnisAttribute> fetchAnnos(long corpusId)
  {
    List<AnnisAttribute> result = new ArrayList<AnnisAttribute>();
    try
    {
      AnnisService service = AnnisServiceFactory.getClient(getApplication().getProperty("AnnisRemoteService.URL"));
      List<Long> ids = new LinkedList<Long>();
      ids.add(corpusId);
      AnnisAttributeSet attributes = service.getAttributeSet(ids, true, true);
      result.addAll(attributes);
    }
    catch(RemoteException ex)
    {
      Logger.getLogger(CorpusListPanel.class.getName()).log(Level.SEVERE,
        "Remote exception when communicating with service", ex);
    }
    catch(AnnisServiceFactoryException e1)
    {
      Logger.getLogger(CorpusListPanel.class.getName()).log(Level.SEVERE,
        "Could not instanciate service", e1);
    }
    return result;
  }

  public class ExampleSorter extends DefaultItemSorter
  {

    @Override
    protected int compareProperty(Object propertyId, boolean sortDirection, Item item1, Item item2)
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
        return super.compareProperty(propertyId, sortDirection, item1, item2);
      }
    }
  }
  
  private String killNamespace(String qName)
  {
    String[] splitted = qName.split(":");
    return splitted[splitted.length-1];
  }
  
  private String getFirst(Set<String> set)
  {
    Iterator<String> it = set.iterator();
    return it.hasNext() ? it.next() : null;
  }
}
