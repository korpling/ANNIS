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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Panel;

/**
 *
 * @author Martin
 */
public class SpanBox extends Panel implements Button.ClickListener
{
  private PrecedenceQueryBuilder sq;
  private HorizontalLayout option;
  private CheckBox chbWithin;
  private ComboBox cbSpan;
  
  public SpanBox(PrecedenceQueryBuilder sq)
  {
    this.sq = sq;    
    option = new HorizontalLayout();
    option.setImmediate(true);
    
    
    chbWithin = new CheckBox("Search within: ");
    chbWithin.setDescription("Add some AQL code to the query to make it limited to the chosen span.");
    chbWithin.setImmediate(true);
    chbWithin.addListener((Button.ClickListener) this);
    
    cbSpan = new ComboBox();
    cbSpan.setEnabled(false);
    cbSpan.setNullSelectionAllowed(false);
    cbSpan.setImmediate(true);
    
    option.addComponent(chbWithin);
    option.addComponent(cbSpan);
    
    addComponent(option);
  }
  
  @Override
  public void buttonClick(Button.ClickEvent event)
  {
    if (event.getComponent() == chbWithin)
    {
      if(cbSpan.size() == 0)
      {
        for (String annoname : sq.getAvailableAnnotationNames())
        {
          cbSpan.addItem(sq.killNamespace(annoname));
        }        
      }      
      cbSpan.setEnabled(chbWithin.booleanValue());
    }
  }
  
  public boolean searchWithinSpan()
  {
    return chbWithin.booleanValue();
  }
  
  public String getSpanName()
  {
    return cbSpan.toString();
  }
}
