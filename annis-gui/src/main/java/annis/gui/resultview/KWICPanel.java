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
package annis.gui.resultview;

import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.service.ifaces.AnnisResult;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author thomas
 */
public class KWICPanel extends Panel
{
  private AnnisResult result;
  private Table tblToken;
  private BeanItemContainer<String> containerAnnos;
  
  public KWICPanel(AnnisResult result)
  {
    this.result = result;
    
    this.addStyleName("kwic-panel");
    setSizeFull();
    
    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setSizeFull();
    
    containerAnnos = new BeanItemContainer<String>(String.class);
        
    tblToken = new Table();
    tblToken.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    tblToken.setWidth("100%");
    tblToken.setHeight("100%");
    tblToken.setPageLength(0);
    
    TreeSet<String> annos = new TreeSet<String>();
    List<AnnisNode> token = result.getGraph().getTokens();
    ArrayList<Object> visible = new ArrayList<Object>(10);
    for(AnnisNode t : token)
    {
      // add to annotation overview      
      for(Annotation a : t.getNodeAnnotations())
      {
        annos.add(a.getQualifiedName());
      }
      
      // add a column for each token
      tblToken.addGeneratedColumn(t, new TokenColumnGenerator());
      visible.add(t);
    }
    containerAnnos.addAll(annos);    
    tblToken.setContainerDataSource(containerAnnos);
    tblToken.setVisibleColumns(visible.toArray());
    
    addComponent(tblToken);
  }
  
  public class TokenColumnGenerator implements Table.ColumnGenerator
  {

    @Override
    public Component generateCell(Table source, Object itemId, Object columnId)
    {
      Label l = new Label("");
      l.setSizeUndefined();
      AnnisNode t = (AnnisNode) columnId;
      for(Annotation a : t.getNodeAnnotations())
      {
        if(a.getQualifiedName().equals(itemId))
        {
          // this is the right annotation
          l.setValue(a.getValue());
          l.setDescription(a.getQualifiedName());
          l.addStyleName("kwic-anno");
          break;
        }
      }

      return l;
    }
  }
  
}
