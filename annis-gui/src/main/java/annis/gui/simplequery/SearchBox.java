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
package annis.gui.simplequery;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.Set;

/**
 *
 * @author tom
 */
public class SearchBox extends Panel implements Button.ClickListener
{
  
  private int id;
  private Button btClose;
  private VerticalNode vn;

  public SearchBox(String ebene, SimpleQuery sq, VerticalNode vn)
  {
    
    this.vn = vn;
    
    VerticalLayout sb = new VerticalLayout();
    sb.setImmediate(true);
    
    // searchbox values for ebene
    Collection<String> annonames = new TreeSet<String>();
    for(String a :sq.getAvailableAnnotationLevels(ebene))
    {
      annonames.add(a.replaceFirst("^[^:]*:", ""));
    }
    ComboBox l = new ComboBox(ebene);
    l.setInputPrompt(ebene);
    l.setWidth("100px");
    // configure & load content
    l.setImmediate(true);
    for (String annoname : annonames) 
    {
      l.addItem(annoname);
    }
    sb.addComponent(l);
    
    // searchbox tickbox for regex
    CheckBox cb = new CheckBox("Regex");
    cb.setDescription("Tick to allow for a regular expression");
    cb.setImmediate(true);
    sb.addComponent(cb);
    
    // close the searchbox
    btClose = new Button("Close", (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    sb.addComponent(btClose);
    
    addComponent(sb);
    
  }
 
  @Override
  public void buttonClick(Button.ClickEvent event)
  {

    if(event.getButton() == btClose)
    {
      vn.removeSearchBox(this);
    }  
  }
  
}
