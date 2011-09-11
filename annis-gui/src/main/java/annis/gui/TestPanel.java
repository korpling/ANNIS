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

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author thomas
 */
public class TestPanel extends Panel
{

  public TestPanel()
  {
    setSizeFull();
    VerticalLayout layout = (VerticalLayout) getContent();
    
    layout.setSizeFull();
    
    Label l = new Label("ABC");
    addComponent(l);
        
    Panel p = new Panel();
    p.setSizeFull();
    GridLayout g = new GridLayout(1, 1);
    Label lbl = new Label("Inner Label                                                                      sfs                sdfsf                 sdfsdf");
    lbl.setContentMode(Label.CONTENT_PREFORMATTED);
    g.addComponent(lbl, 0, 0);
    g.setSizeUndefined();
    p.setContent(g);
    
    addComponent(p);
  }
}
