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
import com.vaadin.ui.OptionGroup;
import com.vaadin.data.Item;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author martin
 * @author tom
 */
public class MetaBox extends Panel implements Button.ClickListener
{
  private Button btClose;
  private OptionGroup tcs;
  private VerticalLayout sb;
  private FlatQueryBuilder sq;
  private final String datum;
  
  public MetaBox(String level, FlatQueryBuilder sq)
  {
    this.sq = sq;
    sb = new VerticalLayout();
    sb.setImmediate(true);
    datum = level;
    // close
    btClose = new Button(SearchBox.BUTTON_CLOSE_LABEL, (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    // metabox values for ebene
    Collection<String> annonames = new TreeSet<String>();
    for(String a :sq.getAvailableMetaLevels(level))
    {
      annonames.add(a);
    }
    OptionGroup l = new OptionGroup(level);
    
    for (String annoname : annonames) {
      if (!annoname.isEmpty()){        
        l.addItem(annoname);
      }
    }
    
    l.setMultiSelect(true);
    l.setNullSelectionAllowed(true);    
    l.setImmediate(true);
    
    tcs = l;
    sb.addComponent(tcs);
    sb.addComponent(btClose);
    setContent(sb);
  }
  
  public String getMetaDatum()
  {
    return datum;
  }
  
  public Collection<String> getValues()
  {
    Collection<String> result = (Collection)tcs.getValue();
    return result;
  } 
  
  public void setValue(Collection<String> values)
  {
    tcs.setValue(values);
  }
  
  public void setValue(String value)
  {
    tcs.setValue(value);
  }
  
  @Override
  public void buttonClick(Button.ClickEvent event)
  {
    if(event.getButton() == btClose)
    {
      sq.removeMetaBox(this);
    }
  } 
}