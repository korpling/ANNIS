/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.admin;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import java.util.Set;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class PopupTwinColumnSelect extends CustomField<Set>
{

  private final HorizontalLayout layout;

  private final TextField txtValue;
  private final TwinColSelect selector;
  
  public PopupTwinColumnSelect(Container predefinedItems)
  {
    txtValue = new TextField();
    txtValue.setConverter(new CommaSeperatedStringConverter());
    txtValue.setWidth("100%");

    selector = new TwinColSelect();
    selector.setNewItemsAllowed(true);
    
    selector.setContainerDataSource(predefinedItems);
    
    PopupView popup = new PopupView("Select", selector);

    layout = new HorizontalLayout(txtValue, popup);
    layout.setExpandRatio(popup, 0.0f);
    layout.setExpandRatio(txtValue, 1.0f);
    layout.setWidth("100%");
  }

  @Override
  protected Component initContent()
  {
    return layout;
  }

  @Override
  public void setPropertyDataSource(Property newDataSource)
  {
    addAllItemsFromProperty(newDataSource);
    
    txtValue.setPropertyDataSource(newDataSource);
    selector.setPropertyDataSource(newDataSource);
    super.setPropertyDataSource(newDataSource);
    
  }
  

  @Override
  public void valueChange(Property.ValueChangeEvent event)
  {
    addAllItemsFromProperty(event.getProperty());
    super.valueChange(event);
  }
  
  private void addAllItemsFromProperty(Property prop)
  {
     if(prop != null && prop.getValue() != null 
      && prop.getType() == Set.class)
    {
      Set items = (Set) prop.getValue();
      for (Object o : items)
      {
        selector.addItem(o);
      }
    }
  }

  @Override
  public Class<? extends Set> getType()
  {
    return Set.class;
  }

  
}
