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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
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
  private VerticalLayout vframe;
  private Collection<SearchBox> sboxes;
  private AddMenu am;
  
  private static final String WIDTH = "180px";
  
  public VerticalNode(String level, FlatQueryBuilder sq)
  {
    this(level, null, sq, true, false);
  }
  
  public VerticalNode(String ebene, String value, FlatQueryBuilder sq, boolean isRegex, boolean negativeSearch)
  {      
    this.sq = sq;
    v = new VerticalLayout();
    v.setSpacing(true);
    vframe = new VerticalLayout();
    vframe.setSpacing(true);
    sboxes = new ArrayList<>(); 
    btClose = new Button(SearchBox.BUTTON_CLOSE_LABEL, (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    SearchBox sb = new SearchBox(ebene, sq, this, isRegex, negativeSearch);
    if(value!=null)
    {
      sb.setValue(value);
    }
    sboxes.add(sb);   
    annonames = sq.getAvailableAnnotationNames();
    am = new AddMenu(sq, this, ebene);
    vframe.addComponent(btClose);
    vframe.setComponentAlignment(btClose, Alignment.TOP_RIGHT);
    v.addComponent(sb);
    vframe.addComponent(v);
    vframe.addComponent(am);
    vframe.setComponentAlignment(am, Alignment.BOTTOM_RIGHT);
    setWidth(WIDTH);
    setContent(vframe);
  }
  
@Override
  public void buttonClick(Button.ClickEvent event)
  {
    if(event.getButton() == btClose)
    {
      sq.removeVerticalNode(this);
      sq.updateQuery(); //think about it
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
  
  public void addSearchBox(SearchBox sb)
  {
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