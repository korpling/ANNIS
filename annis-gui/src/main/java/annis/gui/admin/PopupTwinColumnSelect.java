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
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class PopupTwinColumnSelect extends CustomField<Set>
{

  private final HorizontalLayout layout;

  private final TextField txtValue;

  private final TwinColSelect selector;

  private final IndexedContainer selectableContainer;
 
  public PopupTwinColumnSelect(IndexedContainer selectableContainer)
  {
    if(selectableContainer == null)
    {
      selectableContainer = new IndexedContainer();
    }
    this.selectableContainer = selectableContainer;
    
    selectableContainer.setItemSorter(new StringItemSorter());
    selectableContainer.sort(null, null);
    
    txtValue = new TextField();
    txtValue.setConverter(new CommaSeperatedStringConverter());
    txtValue.setWidth("100%");

    selector = new TwinColSelect();
    selector.setConverter(new TreeSetConverter());
    selector.setNewItemsAllowed(false);
    selector.setLeftColumnCaption("Available");
    selector.setRightColumnCaption("Selected");
    selector.setContainerDataSource(selectableContainer);
    
    PopupView popup = new PopupView("Select", selector);

    layout = new HorizontalLayout(popup, txtValue);
    layout.setExpandRatio(popup, 0.0f);
    layout.setExpandRatio(txtValue, 1.0f);
    layout.setWidth("100%");
    layout.setSpacing(true);
    
    addValueChangeListener(new UpdateContainerListener());
  }

  @Override
  public void setCaption(String caption)
  {
    super.setCaption(caption);
    selector.setCaption(caption);
  }
  
  

  @Override
  protected Component initContent()
  {
    return layout;
  }

  @Override
  public void setPropertyDataSource(Property newDataSource)
  {
    super.setPropertyDataSource(newDataSource);
    txtValue.setPropertyDataSource(getPropertyDataSource());
    selector.setPropertyDataSource(getPropertyDataSource());    
  }

  public void addPredefinedSelectableItems(Collection<String> predefined)
  {
    for(String s : predefined)
    {
      selectableContainer.addItem(predefined);
    }
    selectableContainer.sort(null, null);
  }

  @Override
  public void setValue(Set newFieldValue) throws ReadOnlyException, Converter.ConversionException
  {
    // always use a sorted TreeSet
    if (newFieldValue != null
      && !(newFieldValue instanceof TreeSet))
    {
      TreeSet sortedSet = new TreeSet(String.CASE_INSENSITIVE_ORDER);
      sortedSet.addAll((Collection) newFieldValue);
      newFieldValue = sortedSet;
    }
    super.setValue(newFieldValue);
  }

  @Override
  public Class<? extends Set> getType()
  {
    return Set.class;
  }

  public static class StringItemSorter implements ItemSorter
  {

    @Override
    public void setSortProperties(Container.Sortable container,
      Object[] propertyId, boolean[] ascending)
    {

    }

    @Override
    public int compare(Object itemId1, Object itemId2)
    {
      if(itemId1 instanceof String && itemId2 instanceof String)
      {
        return String.CASE_INSENSITIVE_ORDER.compare((String) itemId1, (String) itemId2);
      }
      else
      {
        return 0;
      }
    }
    
  }
  
  public class UpdateContainerListener implements Property.ValueChangeListener
  {
    @Override
    public void valueChange(Property.ValueChangeEvent event)
    {
      Object val = event.getProperty().getValue();
      if(val instanceof Collection)
      {
        for(Object id : (Collection) val)
        {
          selectableContainer.addItem(id);
        }
      }
      else
      {
        selectableContainer.addItem(val);
      }
      selectableContainer.sort(null, null);
    }
  }
  

}
