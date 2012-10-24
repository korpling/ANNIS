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

import annis.gui.querybuilder.NodeWindow.SimpleNewItemHandler;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;

/**
 *
 * @author thomas
 */
public class EdgeWindow extends Panel implements Button.ClickListener
{
  
  private static final String[] EDGE_OPERATORS = new String[]
  {
    ".",".*", ".*",">",">*", ">@l", ">@r", "$", "$*", "->", "_=_", "_i_",
    "_l_", "'_r_", "_o_", "_ol_", "_or_"
  };
    
  private TigerQueryBuilderCanvas parent;
  
  private ComboBox cbOperator;
  private Button btClose;
  private NodeWindow source;
  private NodeWindow target;
  
  
  public EdgeWindow(final TigerQueryBuilderCanvas parent, NodeWindow source, NodeWindow target)
  {
    this.parent = parent;
    this.source = source;
    this.target = target;
    
    setSizeFull();
    
    
    VerticalLayout vLayout = (VerticalLayout) getContent();
    vLayout.setMargin(false);
    
    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    toolbar.setWidth("100%");
    toolbar.setHeight("20px");
    addComponent(toolbar);
        
    btClose = new Button("X");
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btClose.addListener((Button.ClickListener) this);
    toolbar.addComponent(btClose);
    
    toolbar.setComponentAlignment(btClose, Alignment.MIDDLE_RIGHT);
    
    cbOperator = new ComboBox();
    cbOperator.setNewItemsAllowed(true);
    cbOperator.setNewItemHandler(new SimpleNewItemHandler(cbOperator));
    cbOperator.setImmediate(true);
    addComponent(cbOperator);
    for(String o : EDGE_OPERATORS)
    {
      cbOperator.addItem(o);
    }
    cbOperator.setValue(EDGE_OPERATORS[0]);
    cbOperator.addListener(new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event)
      {
        if(parent != null)
        {
          parent.updateQuery();
        }
      }
    });
    
    cbOperator.setWidth("100%");
    cbOperator.setHeight("20px");
    
    vLayout.setExpandRatio(cbOperator, 1.0f);
    
  }
  
  @Override
  public void buttonClick(ClickEvent event)
  {
    if(event.getButton() == btClose)
    {      
      parent.deleteEdge(this);
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
  
  public String getOperator()
  {
    return (String) cbOperator.getValue();
  }
  
  
  
}
