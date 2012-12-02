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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.Set;
//added by Martin:

/**
 *
 * @author tom
 */
public class SearchBox extends Panel implements Button.ClickListener
{
  
  private int id;
  private Button btClose;
  private VerticalNode vn;
  private String ebene;
  private ComboBox cb;  
  private CheckBox reBox;//by Martin, tick for regular expression

  public SearchBox(String ebene, SimpleQuery sq, VerticalNode vn)
  {
    
    this.vn = vn;
    this.ebene = ebene;
    
    VerticalLayout sb = new VerticalLayout();
    sb.setImmediate(true);
    
    // searchbox values for ebene
    Collection<String> annonames = new TreeSet<String>();
    for(String a :sq.getAvailableAnnotationLevels(ebene))
    {
      annonames.add(a.replaceFirst("^[^:]*:", ""));
    }
    ComboBox l = new ComboBox(ebene);
    this.cb = l;
    l.setInputPrompt(ebene);
    l.setWidth("130px");
    // configure & load content
    l.setImmediate(true);
    for (String annoname : annonames) 
    {
      l.addItem(annoname);
    }
    sb.addComponent(l);
    
    HorizontalLayout sbtoolbar = new HorizontalLayout();
    sbtoolbar.setSpacing(true);
     
    // searchbox tickbox for regex
    CheckBox tb = new CheckBox("Regex");
    tb.setDescription("Tick to allow for a regular expression");
    tb.setImmediate(true);
    sbtoolbar.addComponent(tb);
    tb.addListener((Button.ClickListener) this);
    reBox = tb;
    
    // close the searchbox
    btClose = new Button("Close", (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    sbtoolbar.addComponent(btClose);
    
    sb.addComponent(sbtoolbar);
    
    addComponent(sb);

  }
 
 
  @Override
  public void buttonClick(Button.ClickEvent event)
  {

    if(event.getButton() == btClose)
    {
      vn.removeSearchBox(this);      
    }
    
    if(event.getComponent()==reBox)
    {
      cb.setNewItemsAllowed(reBox.booleanValue());//2be continued
      //cb.setTextInputAllowed(reBox.booleanValue()); 
    }
  }
  
  public String getAttribute()
  {
    return ebene;
  }
  
  public String getValue()
  {
    return cb.toString();
  }
  
  public boolean isRegEx()
  {
    return reBox.booleanValue();
  }
}
