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

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;


/**
 *
 * @author Martin
 */
public class SpanBox extends Panel implements Button.ClickListener, ComboBox.ValueChangeListener
{
  private PrecedenceQueryBuilder sq;
  private VerticalLayout option;
  private CheckBox chbWithin;
  private ComboBox cbSpan;
  private ComboBox cbSpanValue;
  private SearchBox sb;
  private CheckBox reBox; 
  
  private static final String SPANBOX_LABEL = "Search within span:";
  private static final String ACTIVATOR_DESCRIPTION = "Add some AQL code to the query to make it limited to a specific span.";
  private static final String SPANNAME_LABEL = "span level:";
  private static final String SPANVALUE_LABEL = "span value:";  
  private static final String CB_WIDTH = "130px";
  public static final String REBOX_LABEL = "Regex";
  public static final String REBOX_DESCRIPTION = "Tick to allow for a regular expression";
  
  public SpanBox(PrecedenceQueryBuilder sq)
  {
    this.sq = sq;    
    option = new VerticalLayout();
    option.setImmediate(true);  
    
    chbWithin = new CheckBox(SPANBOX_LABEL);
    chbWithin.setDescription(ACTIVATOR_DESCRIPTION);
    chbWithin.setImmediate(true);
//    chbWithin.addListener((Button.ClickListener) this);
    
    cbSpan = new ComboBox();
    cbSpanValue = new ComboBox();
    
    cbSpan.setCaption(SPANNAME_LABEL);
    cbSpan.setEnabled(false);
    cbSpan.setNullSelectionAllowed(false);
    cbSpan.setImmediate(true);
    cbSpan.setWidth(CB_WIDTH);
    cbSpan.addListener((ValueChangeListener) this);   
    
    cbSpanValue.setCaption(SPANVALUE_LABEL);
    cbSpanValue.setEnabled(false);
    cbSpanValue.setNullSelectionAllowed(false);
    cbSpanValue.setImmediate(true);    
    cbSpanValue.setWidth(CB_WIDTH);    
    
    reBox = new CheckBox(REBOX_LABEL);
    reBox.setDescription(REBOX_DESCRIPTION);
    reBox.setEnabled(false);
    reBox.setImmediate(true);    
//    reBox.addListener((Button.ClickListener) this);    
    
    option.addComponent(chbWithin);
    option.addComponent(cbSpan);
    option.addComponent(cbSpanValue);
    option.addComponent(reBox);
    
    setContent(option);
  }
  
  @Override
  public void buttonClick(Button.ClickEvent event)
  {
    if (event.getComponent() == chbWithin)
    {
      if (chbWithin.booleanValue())
      {
        cbSpan.setEnabled(true);
        if(cbSpan.size() == 0)//1st time
        {
          Collection<String> annonames = sq.getAvailableAnnotationNames();
          for (String annoname : annonames)
          {
            cbSpan.addItem(sq.killNamespace(annoname));
          }                  
        }
        cbSpan.setInputPrompt(SPANNAME_LABEL);
        String checkLevel = (cbSpan.getValue()==null) ? "" : cbSpan.getValue().toString();
        cbSpanValue.setEnabled(!checkLevel.equals(""));
      }
      else
      {
        cbSpanValue.setEnabled(false); 
        reBox.setEnabled(false);
      }
      
      cbSpan.setEnabled(chbWithin.booleanValue());     
      
    }
    else if (event.getComponent() == reBox)
    {
      boolean r = reBox.booleanValue();
      cbSpanValue.setNewItemsAllowed(r);
      if (!r)
      {      
        buildBoxValues(cbSpanValue, cbSpan.getValue().toString(), sq);      
      }
      else if(cbSpanValue.getValue()!=null)
      {
        String escapedItem = sq.escapeRegexCharacters(cbSpanValue.getValue().toString());
        cbSpanValue.addItem(escapedItem);
        cbSpanValue.setValue(escapedItem);    
      }
    }
  }
  
  public static void buildBoxValues(ComboBox cb, String level, PrecedenceQueryBuilder sq)
  {
    /*
     * this method deletes the user's regular expressions
     * from the ComboBox
     * (actually it simply refills the box)
     * ---
     * SearchBox uses this method, too
     */    
    String value = (cb.getValue()!=null) ? cb.getValue().toString() : "";
    Collection<String> annovals = sq.getAnnotationValues(level);    
    cb.removeAllItems();
    for (String s : annovals)
    {
      cb.addItem(s);
    }
    if (annovals.contains(value))
    {
      cb.setValue(value);
    }
    else 
    {
      cb.setValue(null);
    }
  }
  
  @Override
  public void valueChange(ValueChangeEvent event)
  {
         
    cbSpanValue.removeAllItems();
    String level = cbSpan.getValue().toString();
    buildBoxValues(cbSpanValue, level, sq);
    cbSpanValue.setInputPrompt(level);
    cbSpanValue.setValue(null);
    cbSpanValue.setEnabled(true);
    reBox.setEnabled(true);      

  }
  
  public boolean searchWithinSpan()
  {
    return chbWithin.booleanValue();
  }
  
  public String getSpanName()
  {
    return cbSpan.getValue().toString();
  }
  
  public String getSpanValue()
  {
    if (cbSpanValue.getValue()==null){return "";}
    return cbSpanValue.getValue().toString();
  }
  
  public boolean isRegEx()
  {
    return reBox.booleanValue();
  }
  
}
