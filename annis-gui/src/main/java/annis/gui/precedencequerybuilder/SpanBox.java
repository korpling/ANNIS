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
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Panel;
import com.vaadin.data.Property.ValueChangeListener;
import java.util.Collection;
import java.util.TreeSet;

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
  
  public SpanBox(PrecedenceQueryBuilder sq)
  {
    this.sq = sq;    
    option = new VerticalLayout();
    option.setImmediate(true);  
    
    chbWithin = new CheckBox("Search within: ");
    chbWithin.setDescription("Add some AQL code to the query to make it limited to the chosen span.");
    chbWithin.setImmediate(true);
    chbWithin.addListener((Button.ClickListener) this);
    
    cbSpan = new ComboBox();
    cbSpanValue = new ComboBox();
    
    cbSpan.setCaption("span:");
    cbSpan.setEnabled(false);
    cbSpan.setNullSelectionAllowed(false);
    cbSpan.setImmediate(true);
    cbSpan.setWidth("130px");
    cbSpan.addListener((ValueChangeListener) this);   
    
    cbSpanValue.setCaption("span value:");
    cbSpanValue.setEnabled(false);
    cbSpanValue.setNullSelectionAllowed(false);
    cbSpanValue.setImmediate(true);    
    cbSpanValue.setWidth("130px");    
    
    reBox = new CheckBox("regex");
    reBox.setEnabled(false);
    reBox.setImmediate(true);    
    reBox.addListener((Button.ClickListener) this);    
    
    option.addComponent(chbWithin);
    option.addComponent(cbSpan);
    option.addComponent(cbSpanValue);
    option.addComponent(reBox);
    
    addComponent(option);
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
        cbSpan.setInputPrompt("span name");
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
    if (event.getComponent() == reBox)
    {
      cbSpanValue.setNewItemsAllowed(reBox.booleanValue());
      if (!reBox.booleanValue())
      {        
        //delete regex-value:
        String checkValue = cbSpanValue.getValue().toString();
        Collection<String> annovals = getAnnotationValues(cbSpan.getValue().toString());
        if(!annovals.contains(checkValue))
        {
          cbSpanValue.removeItem(checkValue);
          cbSpanValue.setValue(annovals.iterator().next());
        }        
      }
    }
  }
  
  @Override
  public void valueChange(ValueChangeEvent event)
  {
    if (true) //modify later <----- !!! !!! !!!
    {      
      cbSpanValue.removeAllItems();
      Collection<String> annonames = getAnnotationValues(cbSpan.getValue().toString());
      for (String a : annonames)
      {
        cbSpanValue.addItem(a);          
      }
      String first = annonames.iterator().next();
      cbSpanValue.setValue(first);
      cbSpanValue.setEnabled(true);
      reBox.setEnabled(true);      
    }
  }
  
  private Collection<String> getAnnotationValues(String level)
  {
    Collection<String> names = new TreeSet<String>();
    
    for(String s : sq.getAvailableAnnotationLevels(level))
    {
      sq.killNamespace(s);
      names.add(s.replaceFirst("^[^:]*:", ""));
    }
    
    return names;
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
    return cbSpanValue.getValue().toString();
  }
  
  public boolean isRegEx()
  {
    return reBox.booleanValue();
  }
  
}
