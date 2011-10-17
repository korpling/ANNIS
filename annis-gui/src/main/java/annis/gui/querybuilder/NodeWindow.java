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
public class NodeWindow extends Panel implements Button.ClickListener
{
  private TigerQueryBuilder parent;
  
  private Button btEdge;
  private Button btAdd;
  private Button btClear;
  private Button btClose;
  
  private boolean prepareEdgeDock;
  
  private int id;
  
  public NodeWindow(int id, TigerQueryBuilder parent)
  {
    this.parent = parent;
    this.id = id;
    
    setWidth("99%");
    setHeight("99%");
    
    prepareEdgeDock = false;
    
    VerticalLayout vLayout = (VerticalLayout) getContent();
    vLayout.setMargin(false);
    
    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    toolbar.setWidth("100%");
    toolbar.setHeight("-1px");
    addComponent(toolbar);
    
    btEdge = new Button("Edge");
    btEdge.setStyleName(ChameleonTheme.BUTTON_LINK);
    btEdge.addListener((Button.ClickListener) this);
    toolbar.addComponent(btEdge);
    btAdd = new Button("Add");
    btAdd.setStyleName(ChameleonTheme.BUTTON_LINK);
    toolbar.addComponent(btAdd);
    btClear = new Button("Clear");
    btClear.setStyleName(ChameleonTheme.BUTTON_LINK);
    toolbar.addComponent(btClear);
    
    btClose = new Button("X");
    btClose.setStyleName(ChameleonTheme.BUTTON_LINK);
    btClose.addListener((Button.ClickListener) this);
    toolbar.addComponent(btClose);
    
    toolbar.setComponentAlignment(btClose, Alignment.MIDDLE_RIGHT);
    
    Label lblNode = new Label("node " + id);
    addComponent(lblNode);

    vLayout.setExpandRatio(lblNode, 1.0f);
    
  }
  
  public void setPrepareEdgeDock(boolean prepare)
  {
    this.prepareEdgeDock = prepare;
    
    btClear.setVisible(!prepare);
    btClose.setVisible(!prepare);
    btAdd.setVisible(!prepare);
    
    if(prepare)
    {
      btEdge.setCaption("Dock");
    }
    else
    {
      btEdge.setCaption("Edge");
    }
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    if(event.getButton() == btEdge)
    {      
      if(prepareEdgeDock)
      {
        setPrepareEdgeDock(false);
        parent.addEdge(this);
      }
      else
      {
        parent.prepareAddingEdge(this);
        setPrepareEdgeDock(true);
        btEdge.setCaption("Cancel");
      }
    }
    else if(event.getButton() == btClose)
    {
      parent.deleteNode(this);
    }
  }

  public int getID()
  {
    return id;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(obj == null)
    {
      return false;
    }
    if(getClass() != obj.getClass())
    {
      return false;
    }
    final NodeWindow other = (NodeWindow) obj;
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 41 * hash + this.id;
    return hash;
  }
  
  
  
  
}
