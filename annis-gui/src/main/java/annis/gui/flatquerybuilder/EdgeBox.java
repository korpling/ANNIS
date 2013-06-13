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

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import java.util.HashMap;

/**
 * @author martin
 * @author tom
 */
public class EdgeBox extends Panel
{
  private ComboBox edge;
  private static final String[][] BASIS_OPERATORS = new String[][]
  {
    {".",".2",".1,2",".*"},
    { ".\t[is directly preceding]",
      ".2\t[is preceding with one token in between]",
      ".1,2\t[is directly preceding or with one token in between]",
      ".*\t[is indirectly preceding]"}
  };
  /*BASIS_OPERATORS + userdefined Operators (with Description):*/
  private static HashMap<String, String> EO;
  private static final String UD_EO_DESCRIPTION = "\t(user defined)";
  private static final String WIDTH = "45px";
  
  public EdgeBox (FlatQueryBuilder sq)
  {
    initEOs();
    edge = new ComboBox();
    edge.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT);
    for(String o : EO.keySet())
    {
      edge.addItem(o);
      edge.setItemCaption(o, EO.get(o));
    }
    edge.setNewItemsAllowed(true);
    edge.setTextInputAllowed(true);
    edge.setValue(BASIS_OPERATORS[1][0]);
    edge.setWidth(WIDTH);
    edge.setNullSelectionAllowed(false);
    edge.setImmediate(true);
    edge.addFocusListener(new FieldEvents.FocusListener(){
      @Override
      public void focus(FieldEvents.FocusEvent e)
      {
        //this prevents the creation of an invalid entry
        edge.select(null);
      }
    });
    edge.addValueChangeListener(new ValueChangeListener(){
      @Override
      public void valueChange(ValueChangeEvent e)
      {
         String value = edge.getValue().toString();
         if(!value.equals(""))
         {
          if(!EO.containsKey(value))
          {          
            String caption = value+UD_EO_DESCRIPTION;
            EO.put(value, caption);            
            edge.setItemCaption(value, caption);
          }
         }
      }
    });    
    setContent(edge);    
  }
  
  private void initEOs()
  {
    EO = new HashMap<String, String>();
    for(int i=0; i<BASIS_OPERATORS[0].length; i++)
    {
      EO.put(BASIS_OPERATORS[0][i], BASIS_OPERATORS[1][i]);
    }
  }
      
  public String getValue()
  {
    return edge.getValue().toString();
  }
  
  public void setValue(String value)
  {    
    if(!EO.containsKey(value))
    {
      String caption = value+UD_EO_DESCRIPTION;
      EO.put(value, caption);
      edge.addItem(value);
      edge.setItemCaption(value, caption);
    }
    edge.setValue(value);
  }
}