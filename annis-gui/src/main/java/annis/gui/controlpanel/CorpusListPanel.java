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
import annis.gui.CorpusBrowserPanel;
import annis.gui.MetaDataPanel;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.AnnisCorpusSet;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class CorpusListPanel extends Panel
{

  private static final ThemeResource INFO_ICON = new ThemeResource("info.gif");
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
    
    tblCorpora.addGeneratedColumn("info", new InfoGenerator());
    
    tblCorpora.setVisibleColumns(new String[]
      {
        "name", "textCount", "tokenCount", "info"
      });
    tblCorpora.setColumnHeaders(new String[]
      {
        "Name", "Texts", "Tokens", ""
      });
    tblCorpora.setHeight(100f, UNITS_PERCENTAGE);
    tblCorpora.setWidth(100f, UNITS_PERCENTAGE);
    tblCorpora.setSelectable(true);
    tblCorpora.setMultiSelect(true);
    tblCorpora.setColumnExpandRatio("name", 0.7f);
    tblCorpora.setColumnExpandRatio("textCount", 0.15f);
    tblCorpora.setColumnExpandRatio("tokenCount", 0.15f);
    tblCorpora.setColumnWidth("info", 18);
    
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
  
  public class InfoGenerator implements Table.ColumnGenerator
  {

    @Override
    public Component generateCell(Table source, Object itemId, Object columnId)
    {
      final AnnisCorpus c = corpusContainer.getItem(itemId).getBean();
      Button l = new Button();
      l.setStyleName(BaseTheme.BUTTON_LINK);
      l.setIcon(INFO_ICON);
      l.setDescription(c.getName());
      
      l.addListener(new Button.ClickListener() {

        @Override
        public void buttonClick(ClickEvent event)
        {
          MetaDataPanel meta = new MetaDataPanel(c.getId());
          CorpusBrowserPanel browse = new CorpusBrowserPanel(c.getId());
          
          HorizontalLayout layout = new HorizontalLayout();
          layout.addComponent(meta);
          layout.addComponent(browse);
          layout.setSizeFull();
          layout.setExpandRatio(meta, 0.5f);
          layout.setExpandRatio(browse, 0.5f);
          
          Window window = new Window("Corpus information for " + c.getName() 
            + " (ID: " + c.getId() + ")", layout);
          window.setWidth(70, UNITS_EM);
          window.setHeight(40, UNITS_EM);
          window.setResizable(false);
          
          getWindow().addWindow(window);
          window.center();
        }
      });
      
      return l;
    }
    
  }
}
