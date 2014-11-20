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

import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Panel;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @author Martin Klotz (martin.klotz@hu-berlin.de)
 * @author Tom Ruette (tom.ruette@hu-berlin.de)
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
  /*last set value:*/
  private String storedValue;
  
  private static final String UD_EO_DESCRIPTION = "\t(user defined)";  
  private static final String WIDTH = "45px";
  private static final String REGEX_PATTERN = "(\\.((\\*)|([1-9]+[0-9]*(,[1-9]+[0-9]*)?))?)";
  private static final String REGEX_PATTERN_DOUBLEBOUND = "\\\\.[1-9]+[0-9]*,[1-9]+[0-9]*";
  
  public EdgeBox (FlatQueryBuilder sq)
  {
    initEOs();
    storedValue=".";
    edge = new ComboBox();
    edge.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT);
    for(String o : EO.keySet())
    {
      edge.addItem(o);
      edge.setItemCaption(o, EO.get(o));
    }
    edge.setNewItemsAllowed(true);
    edge.setTextInputAllowed(true);    
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
    edge.addBlurListener(new BlurListener(){
      @Override
      public void blur(FieldEvents.BlurEvent e)
      {
        if(edge.getValue()!=null)
        {
          String value = edge.getValue().toString(); //<--- CATCH NullPointerException HERE!
          if(!value.equals(""))
          {
           boolean valid = validOperator(value);
           if(!EO.containsKey(value) & valid)
           {          
             String caption = value+UD_EO_DESCRIPTION;
             EO.put(value, caption);
             edge.setItemCaption(value, caption);
           }
           if(!valid)
           {
             edge.removeItem(value);
             /*this should make the user recognize his/her mistake:*/
             edge.select(null);
           }
          }
          storedValue = (edge.getValue()!=null) ? edge.getValue().toString() : storedValue;
        }
        else
        {
          edge.setValue(storedValue);
        }
      }
    });
    setContent(edge);    
    edge.select(BASIS_OPERATORS[0][0]);
  }
  
  private void initEOs()
  {
    EO = new HashMap<>();
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
    boolean valid = validOperator(value);
    if(!EO.containsKey(value) & valid)
    {
      String caption = value+UD_EO_DESCRIPTION;
      EO.put(value, caption);
      edge.addItem(value);
      edge.setItemCaption(value, caption);
    }
    if(valid)
    {
      edge.setValue(value);
      storedValue = value;
    }
    else
    {
      edge.select(null);
    }
  }
  
  private boolean validOperator(String o)
  {
    String s = o.replace(" ", "");
    if(Pattern.matches(REGEX_PATTERN, s))
    {
      if(Pattern.matches(REGEX_PATTERN_DOUBLEBOUND, s))
      {
        int split = s.indexOf(",");
        String s1 = s.substring(1, split);
        String s2 = s.substring(split+1);
        return (Integer.parseInt(s1)<=Integer.parseInt(s2));
      }
      else return true;
    }    
    return false;  
  }
}