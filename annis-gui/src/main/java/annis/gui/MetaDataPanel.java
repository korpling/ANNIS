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
import annis.model.Annotation;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class MetaDataPanel extends Panel
{
  private Table tblMeta;
  private BeanItemContainer<Annotation> metaContainer;
  private long id;
  
  public MetaDataPanel(long id)
  {
    super("Metadata");
    
    this.id = id;
    
    setSizeFull();
    VerticalLayout layout = new VerticalLayout();
    setContent(layout);
    layout.setSizeFull();    
    metaContainer = new BeanItemContainer<Annotation>(Annotation.class);
    
    tblMeta = new Table();
    layout.addComponent(tblMeta);
    
    tblMeta.setContainerDataSource(metaContainer);
    tblMeta.addGeneratedColumn("genname", new Table.ColumnGenerator() {

      @Override
      public Component generateCell(Table source, Object itemId, Object columnId)
      {
        Annotation anno = metaContainer.getItem(itemId).getBean();
        String qName = anno.getName();
        if(anno.getNamespace() != null)
        {
          qName = anno.getNamespace() + ":" + qName;
        }
        Label l = new Label(qName);
        l.setSizeUndefined();
        return l;
      }
    });
    tblMeta.addGeneratedColumn("genvalue", new Table.ColumnGenerator() {

      @Override
      public Component generateCell(Table source, Object itemId, Object columnId)
      {
        Annotation anno = metaContainer.getItem(itemId).getBean();
        Label l = new Label(anno.getValue(), Label.CONTENT_RAW);
        return l;
      }
    });
    
    
    tblMeta.setVisibleColumns(new String[] {"genname", "genvalue"});
    tblMeta.setColumnHeaders(new String[] {"Name", "Value"});
    tblMeta.setSizeFull();
    tblMeta.setColumnWidth("genname", -1);
    tblMeta.setColumnExpandRatio("genvalue", 1.0f);
  }

  @Override
  public void attach()
  {
    super.attach();
    
    // load meta data from service
    metaContainer.addAll(getMetaData(id));
    
  }
  
  private List<Annotation> getMetaData(long id)
  {
    List<Annotation> result = new ArrayList<Annotation>();
    try
    {
      AnnisService service = AnnisServiceFactory.getClient(getApplication().getProperty("AnnisRemoteService.URL"));
      result = service.getMetadata(id);
    }
    catch(RemoteException ex)
    {
      Logger.getLogger(MetaDataPanel.class.getName()).log(Level.SEVERE,
        "Remote exception when communicating with service", ex);
    }
    catch(AnnisServiceFactoryException e1)
    {
      Logger.getLogger(MetaDataPanel.class.getName()).log(Level.SEVERE,
        "Could not instanciate service", e1);
    }
    return result;
  }
  
  
}
