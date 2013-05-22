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
package annis.gui.flatquerybuilder;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @author Martin
 * @author tom
 */
public class VerticalNode extends Panel implements Button.ClickListener
{
  
  private Set<String> annonames;
  private FlatQueryBuilder sq;
  private Button btClose;
  private VerticalLayout v;
  private Collection<SearchBox> sboxes;
  private AddMenu am;
  
  private static final String WIDTH = "150px";
  
  public VerticalNode(String ebene, FlatQueryBuilder sq)
  {      
    this.sq = sq;
    v = new VerticalLayout();
    sboxes = new ArrayList<SearchBox>(); 
    btClose = new Button(SearchBox.BUTTON_CLOSE_LABEL, (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    SearchBox sb = new SearchBox(ebene, sq, this);
    sboxes.add(sb);   
    annonames = sq.getAvailableAnnotationNames();
    am = new AddMenu(sq, this, ebene);
    HorizontalLayout vntoolbar = new HorizontalLayout();
    vntoolbar.addComponent(this.am);
    vntoolbar.addComponent(btClose);
    v.addComponent(vntoolbar);
    v.addComponent(sb);
    v.setSpacing(true);
    setWidth(WIDTH);
    setContent(v);
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
    annonames.add(s.getAttribute());
    am.reActivateItem(s.getAttribute());
  }

  public void createSearchBox(String ebene)
  {
    SearchBox sb = new SearchBox(ebene, sq, this);
    this.sboxes.add(sb);
    v.addComponent(sb);
  }
  
  public Collection<SearchBox> getSearchBoxes()
  {
    return this.sboxes;
  }
  
  public Collection<String> getAnnonames()
  {
    return annonames;
  }
}