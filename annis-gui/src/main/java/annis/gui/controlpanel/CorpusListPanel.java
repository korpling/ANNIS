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
import com.vaadin.data.util.AbstractBeanContainer.BeanIdResolver;
import com.vaadin.data.util.BeanContainer;
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
    
    tblCorpora.setContainerDataSource(corpusContainer);
    tblCorpora.setVisibleColumns(new String[] {"name", "textCount", "tokenCount"});
    tblCorpora.setColumnHeaders(new String[] {"Name", "Texts", "Tokens"});
    tblCorpora.setHeight(100f, UNITS_PERCENTAGE);
    tblCorpora.setWidth(100f, UNITS_PERCENTAGE);
  }

  @Override
  public void attach()
  {
    super.attach();
    
    corpusContainer.addAll(getCorpusList());
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
}
