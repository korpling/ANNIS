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
package annis.gui.querybuilder;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;

/**
 *
 * @author thomas
 */
public class EdgeWindow extends Panel implements Button.ClickListener
{
  private TigerQueryBuilder parent;
  
  private Button btClose;
  private NodeWindow source;
  private NodeWindow target;
  
  
  public EdgeWindow(TigerQueryBuilder parent, NodeWindow source, NodeWindow target)
  {
    this.parent = parent;
    this.source = source;
    this.target = target;
    
    setWidth("99%");
    setHeight("99%");
    
    
    VerticalLayout vLayout = (VerticalLayout) getContent();
    vLayout.setMargin(false);
    
    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    toolbar.setWidth("100%");
    toolbar.setHeight("-1px");
    addComponent(toolbar);
        
    btClose = new Button("X");
    btClose.setStyleName(ChameleonTheme.BUTTON_LINK);
    toolbar.addComponent(btClose);
    
    toolbar.setComponentAlignment(btClose, Alignment.MIDDLE_RIGHT);
    
    Label lblNode = new Label("edge " + source.getNumber() + " -> " + target.getNumber());
    addComponent(lblNode);

    vLayout.setExpandRatio(lblNode, 1.0f);
    
  }
  
  @Override
  public void buttonClick(ClickEvent event)
  {
    if(event.getButton() == btClose)
    {      
      // TODO: delete this edge
    }
  }

  public NodeWindow getSource()
  {
    return source;
  }

  public NodeWindow getTarget()
  {
    return target;
  }
  
  
  
}
