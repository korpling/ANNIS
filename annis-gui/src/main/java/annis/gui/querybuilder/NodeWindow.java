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

import com.vaadin.addon.chameleon.ChameleonTheme;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author thomas
 */
public class NodeWindow extends Panel
{
  public NodeWindow(int number)
  {
    setWidth("99%");
    setHeight("99%");
    
    VerticalLayout vLayout = (VerticalLayout) getContent();
    vLayout.setMargin(false);
    
    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    toolbar.setWidth("100%");
    toolbar.setHeight("-1px");
    addComponent(toolbar);
    
    Button btEdge = new Button("Edge");
    btEdge.setStyleName(ChameleonTheme.BUTTON_LINK);
    toolbar.addComponent(btEdge);
    Button btAdd = new Button("Add");
    btAdd.setStyleName(ChameleonTheme.BUTTON_LINK);
    toolbar.addComponent(btAdd);
    Button btClear = new Button("Clear");
    btClear.setStyleName(ChameleonTheme.BUTTON_LINK);
    toolbar.addComponent(btClear);
    
    Button btClose = new Button("X");
    btClose.setStyleName(ChameleonTheme.BUTTON_LINK);
    toolbar.addComponent(btClose);
    
    toolbar.setComponentAlignment(btClose, Alignment.MIDDLE_RIGHT);
    
    Label lblNode = new Label("node " + number);
    addComponent(lblNode);

    vLayout.setExpandRatio(lblNode, 1.0f);
    
  }
}
