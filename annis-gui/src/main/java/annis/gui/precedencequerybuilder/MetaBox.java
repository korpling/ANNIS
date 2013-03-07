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
  private String selection;
  //private TwinColSelect tcs;
  private OptionGroup tcs;
  private VerticalLayout sb;
  private PrecedenceQueryBuilder sq;
  private final String datum;
  
  private static final String LEFT_COLUMN_LABEL = "Available levels";
  private static final String RIGHT_COLUMN_LABEL = "Selected levels";
  private static final String TWIN_COL_WIDTH = "40px";
  
  public MetaBox(String ebene, PrecedenceQueryBuilder sq)
  {
    this.sq = sq;
    sb = new VerticalLayout();
    sb.setImmediate(true);
    
    datum = ebene;
    
    // close
    btClose = new Button(SearchBox.BUTTON_CLOSE_LABEL, (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    
    // metabox values for ebene
    Collection<String> annonames = new TreeSet<String>();
    for(String a :sq.getAvailableMetaLevels(ebene))
    {
      annonames.add(a);
    }
    
    //TODO make it so that if annonames.length()>10 it becomes a twincolselect, and if there is less than 10 it is just optiongroup
    //TwinColSelect l = new TwinColSelect(ebene);
    OptionGroup l = new OptionGroup(ebene);
    for (String annoname : annonames) {
      if (!annoname.isEmpty()){
        l.addItem(annoname);
      }
    }
    //l.setRows(10);
    l.setNullSelectionAllowed(true);
    l.setMultiSelect(true);
    l.setImmediate(true);
    //l.setLeftColumnCaption(LEFT_COLUMN_LABEL);
    //l.setRightColumnCaption(RIGHT_COLUMN_LABEL);
    //l.setWidth(TWIN_COL_WIDTH);
    tcs = l;

    sb.addComponent(tcs);
    sb.addComponent(btClose);
    addComponent(sb);

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
  
  @Override
  public void buttonClick(Button.ClickEvent event)
  {

    if(event.getButton() == btClose)
    {
      sq.removeMetaBox(this);
    }
  } 
}
