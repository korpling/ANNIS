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

import com.vaadin.ui.Button;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author tom
 */
class MetaBox extends Panel implements Button.ClickListener
{

  private Button btClose;
  private VerticalLayout sb;
  private PrecedenceQueryBuilder sq;
  
  public MetaBox(String ebene, PrecedenceQueryBuilder sq)
  {
    this.sq = sq;
    sb = new VerticalLayout();
    sb.setImmediate(true);
    
    // close
    btClose = new Button("Close", (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    
    // metabox values for ebene
    Collection<String> annonames = new TreeSet<String>();
    for(String a :sq.getAvailableMetaLevels(ebene))
    {
      annonames.add(a.replaceFirst("^[^:]*:", ""));
    }
    
    TwinColSelect l = new TwinColSelect(ebene);
    for (String annoname : annonames) {
      l.addItem(annoname);
    }
    l.setRows(10);
    l.setNullSelectionAllowed(true);
    l.setMultiSelect(true);
    l.setImmediate(true);
    l.setLeftColumnCaption("Available levels");
    l.setRightColumnCaption("Selected levels");
    l.setWidth("350px");

    sb.addComponent(l);
    sb.addComponent(btClose);
    addComponent(sb);

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
