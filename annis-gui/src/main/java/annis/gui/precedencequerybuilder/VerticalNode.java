/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.precedencequerybuilder;

import com.vaadin.ui.MenuBar;
import java.util.Collection;
import java.util.Set;
import com.vaadin.ui.VerticalLayout;
import annis.gui.precedencequerybuilder.SearchBox;
import annis.gui.precedencequerybuilder.AddMenu;
import com.vaadin.ui.Panel;
import annis.gui.precedencequerybuilder.PrecedenceQueryBuilder;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;

/**
 *
 * @author tom
 */
public class VerticalNode extends Panel implements Button.ClickListener
{
  
  private Set<String> annonames;
  private PrecedenceQueryBuilder sq;
  private Button btClose;
  private VerticalLayout v;
  private Collection<SearchBox> sboxes;
  
  public VerticalNode(String ebene, PrecedenceQueryBuilder sq)
  {
        
    this.sq = sq;
    v = new VerticalLayout();
    sboxes = new ArrayList<SearchBox>();
    
    btClose = new Button("Close", (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
        
    SearchBox sb = new SearchBox(ebene, sq, this); //SearchBox has takes an argument to 
      // tell it for which annotation level it should search
    sboxes.add(sb);
    
    annonames = sq.getAvailableAnnotationNames();
    for (SearchBox haveSB : sboxes){
      annonames.remove(haveSB.getAttribute());
    }
    AddMenu am = new AddMenu(annonames, sq, this); //AddMenu creates a menubar from 
      // which users can pick the annotation level they are interested in
    
    VerticalLayout vntoolbar = new VerticalLayout();
    vntoolbar.addComponent(btClose);
    vntoolbar.addComponent(am);
    v.addComponent(vntoolbar);
    v.addComponent(sb);
    setWidth("200px");
    addComponent(v);
    
  }
  
@Override
  public void buttonClick(Button.ClickEvent event)
  {

    if(event.getButton() == btClose)
    {
      sq.removeVerticalNode(this);
    }  
  }

public void removeSearchBox(SearchBox s)
  {
    v.removeComponent(s);
    this.sboxes.remove(s);
  }

public void createSearchBox(String ebene)
  {
    SearchBox sb = new SearchBox(ebene, sq, this);
    this.sboxes.add(sb);
    v.addComponent(sb);
  }
public Collection getSearchBoxes()
{
  return this.sboxes;
}
}