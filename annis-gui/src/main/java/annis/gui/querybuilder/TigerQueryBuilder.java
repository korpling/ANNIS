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

import annis.gui.controlpanel.ControlPanel;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;

/**
 *
 * @author thomas
 */
public class TigerQueryBuilder extends Panel implements Button.ClickListener
{

  private Button btAddNode;
  private Button btClearAll;
  private TigerQueryBuilderCanvas queryBuilder;
  
  public TigerQueryBuilder(ControlPanel controlPanel)
  {    
    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setSizeFull();
    setSizeFull();
    
    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    btAddNode = new Button("Add node", (Button.ClickListener) this);
    btAddNode.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btAddNode.setDescription("<strong>Create Node</strong><br />"
      + "Click here to add a new node specification window.<br />"
      + "To move the node, click and hold left mouse button, then move the mouse.");
    toolbar.addComponent(btAddNode);

    btClearAll = new Button("Clear all", (Button.ClickListener) this);
    btClearAll.setStyleName(ChameleonTheme.BUTTON_SMALL);
    btClearAll.setDescription("<strong>Clear all</strong><br />"
      + "Click here to delete all node specification windows and reset the query builder.");
    toolbar.addComponent(btClearAll);


    toolbar.setWidth("-1px");
    toolbar.setHeight("-1px");

    addComponent(toolbar);
    
    queryBuilder = new TigerQueryBuilderCanvas(controlPanel);
    addComponent(queryBuilder);
    
    layout.setExpandRatio(queryBuilder, 1.0f);
  }
  
  @Override
  public void buttonClick(ClickEvent event)
  {

    if(event.getButton() == btAddNode)
    {
      queryBuilder.addNode();
    }
    else if(event.getButton() == btClearAll)
    {
      queryBuilder.clearAll();
    }

  }
}
