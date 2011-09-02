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

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

/**
 *
 * @author thomas
 */
public class QueryPanel extends Panel
{
  private TextField txtQuery;
  private TextField txtResult;
  private Button btShowResult;
  private Button btHistory;
  
  public QueryPanel()
  {
    setSizeFull();
    
    GridLayout layout = new GridLayout(2, 3);
    setContent(layout);
    layout.setSizeFull();
    layout.setSpacing(true);
    layout.setMargin(true);
    
    layout.addComponent(new Label("AnnisQL:"), 0, 0);    
    layout.addComponent(new Label("Result:"), 0, 2);
    
    layout.setRowExpandRatio(0, 1.0f);
    layout.setColumnExpandRatio(0, 0.2f);
    layout.setColumnExpandRatio(1, 0.8f);
    
    txtQuery = new TextField();
    txtQuery.setSizeFull();
    layout.addComponent(txtQuery, 1, 0);
    
    txtResult = new TextField();
    txtResult.setWidth(100, UNITS_PERCENTAGE);
    txtResult.setHeight(3.5f, UNITS_EM);
    layout.addComponent(txtResult, 1, 2);
    txtResult.setValue("Hello World");
    //txtResult.setReadOnly(true);
    
    Panel buttonPanel = new Panel();
    HorizontalLayout buttonPanelLayout = new HorizontalLayout();
    buttonPanel.setContent(buttonPanelLayout);
    buttonPanelLayout.setWidth(100f, UNITS_PERCENTAGE);
    layout.addComponent(buttonPanel, 1, 1);
    
    btShowResult = new Button("Show Result");
    btShowResult.setWidth(100f, UNITS_PERCENTAGE);
    buttonPanel.addComponent(btShowResult);
    
    btHistory = new Button("History");
    btHistory.setWidth(100f, UNITS_PERCENTAGE);
    buttonPanel.addComponent(btHistory);
  }
  
  public void setQuery(String query)
  {
    if(txtQuery != null)
    {
      txtQuery.setValue(query);
    }
  }
}
