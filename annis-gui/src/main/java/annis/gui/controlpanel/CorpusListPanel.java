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
package annis.gui.controlpanel;

import annis.exceptions.AnnisServiceFactoryException;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.AnnisCorpusSet;
import com.vaadin.data.Item;
import com.vaadin.data.util.AbstractBeanContainer.BeanIdResolver;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.ui.AbstractSelect.MultiSelectMode;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class CorpusListPanel extends Panel
{

  BeanContainer<Long, AnnisCorpus> corpusContainer;
  private Table tblCorpora;

  public CorpusListPanel()
  {
    setSizeFull();

    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setSizeFull();

    tblCorpora = new Table();
    addComponent(tblCorpora);

    corpusContainer = new BeanContainer<Long, AnnisCorpus>(AnnisCorpus.class);
    corpusContainer.setBeanIdProperty("id");
    corpusContainer.setItemSorter(new CorpusSorter());

    tblCorpora.setContainerDataSource(corpusContainer);
    tblCorpora.setVisibleColumns(new String[]
      {
        "name", "textCount", "tokenCount"
      });
    tblCorpora.setColumnHeaders(new String[]
      {
        "Name", "Texts", "Tokens"
      });
    tblCorpora.setHeight(100f, UNITS_PERCENTAGE);
    tblCorpora.setWidth(100f, UNITS_PERCENTAGE);
    tblCorpora.setSelectable(true);
    tblCorpora.setMultiSelect(true);
  }

  @Override
  public void attach()
  {
    super.attach();

    corpusContainer.addAll(getCorpusList());    
    tblCorpora.setSortContainerPropertyId("name");
    tblCorpora.sort();
  }

  private List<AnnisCorpus> getCorpusList()
  {
    List<AnnisCorpus> result = new ArrayList<AnnisCorpus>();
    try
    {
      AnnisService service = AnnisServiceFactory.getClient(getApplication().getProperty("AnnisRemoteService.URL"));
      AnnisCorpusSet corpora = service.getCorpusSet();
      result.addAll(corpora);
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

  public class CorpusSorter extends DefaultItemSorter
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
}
