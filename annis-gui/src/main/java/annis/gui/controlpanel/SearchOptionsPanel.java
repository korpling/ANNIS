/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.controlpanel;

import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;

/**
 *
 * @author thomas
 */
public class SearchOptionsPanel extends Panel
{
  private ComboBox cbLeftContext;
  private ComboBox cbRightContext;
  private ComboBox cbResultsPerPage;

  // TODO: make this configurable
  protected  static final String[] PREDEFINED_PAGE_SIZES = new String[] 
  {
    "1", "2", "5", "10", "15", "20", "25"
  };
  protected static final String[] PREDEFINED_CONTEXTS = new String[] 
  {
    "0", "1", "2", "5", "10"
  };
  
  public SearchOptionsPanel()
  {
    setSizeFull();
    
    FormLayout layout = new FormLayout();
    setContent(layout);
    
    cbLeftContext = new ComboBox("Left Context");
    cbRightContext = new ComboBox("Right Context");
    cbResultsPerPage = new ComboBox("Results Per Page");
    
    cbLeftContext.setNullSelectionAllowed(false);
    cbRightContext.setNullSelectionAllowed(false);
    cbResultsPerPage.setNullSelectionAllowed(false);
    
    cbLeftContext.setNewItemsAllowed(true);
    cbRightContext.setNewItemsAllowed(true);
    cbResultsPerPage.setNewItemsAllowed(true);
    
    cbLeftContext.addValidator(new IntegerValidator("must be a number"));
    cbRightContext.addValidator(new IntegerValidator("must be a number"));
    cbResultsPerPage.addValidator(new IntegerValidator("must be a number"));
    
    for(String s : PREDEFINED_CONTEXTS)
    {
      cbLeftContext.addItem(s);
      cbRightContext.addItem(s);
    }
    
    for(String s : PREDEFINED_PAGE_SIZES)
    {
      cbResultsPerPage.addItem(s);
    }
    
    cbLeftContext.setValue("5");
    cbRightContext.setValue("5");
    cbResultsPerPage.setValue("10");
    
    layout.addComponent(cbLeftContext);
    layout.addComponent(cbRightContext);
    layout.addComponent(cbResultsPerPage);

  }
  
  public void setLeftContext(int context)
  {
    cbLeftContext.setValue("" + context);
  }
  
  public int getLeftContext()
  {
    int result = 5;
    try
    {
      result = Integer.parseInt((String) cbLeftContext.getValue());
    }
    catch(Exception ex)
    {
      
    }
    
    return Math.max(0, result);
  }
  
  public int getRightContext()
  {
    int result = 5;
    try
    {
      result = Integer.parseInt((String) cbRightContext.getValue());
    }
    catch(Exception ex)
    {
      
    }
    
    return Math.max(0, result);
  }
  
  public void setRightContext(int context)
  {
    cbRightContext.setValue("" + context);
  }
  
  public int getResultsPerPage()
  {
    int result = 10;
    try
    {
      result = Integer.parseInt((String) cbResultsPerPage.getValue());
    }
    catch(Exception ex)
    {
      
    }
    
    return Math.max(0, result);
  }
  
  
}
