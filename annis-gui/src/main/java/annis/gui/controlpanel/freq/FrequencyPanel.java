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
package annis.gui.controlpanel.freq;

import annis.service.objects.FrequencyTableEntryType;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class FrequencyPanel extends VerticalLayout
{
  private Table tblFrequencyDefinition;
  private Button btAdd;
  private Button btDeleteRow;
  
  private int counter;
  
  public FrequencyPanel()
  {
    setWidth("100%");
    setHeight("100%");
    
    
    tblFrequencyDefinition = new Table();
    tblFrequencyDefinition.setImmediate(true);
    tblFrequencyDefinition.setSortDisabled(true);
    tblFrequencyDefinition.setSelectable(true);
    tblFrequencyDefinition.setMultiSelect(true);
    tblFrequencyDefinition.setEditable(true);
    tblFrequencyDefinition.addListener(new Property.ValueChangeListener() 
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      {
        if(tblFrequencyDefinition.getValue() == null 
          || ((Set<Object>) tblFrequencyDefinition.getValue()).isEmpty())
        {
          btDeleteRow.setEnabled(false);
        }
        else
        {
          btDeleteRow.setEnabled(true);
        }
      }
    });
    
    tblFrequencyDefinition.setWidth("100%");
    tblFrequencyDefinition.setHeight("100%");
    
    
    tblFrequencyDefinition.addContainerProperty("nr", TextField.class, null);
    tblFrequencyDefinition.addContainerProperty("type", ComboBox.class, null);
    tblFrequencyDefinition.addContainerProperty("annotation", TextField.class, null);
    
    tblFrequencyDefinition.setColumnHeader("nr", "#node");
    tblFrequencyDefinition.setColumnHeader("type", "");
    tblFrequencyDefinition.setColumnHeader("annotation", "Annotation");
    
    counter = 0;
    tblFrequencyDefinition.addItem(createNewTableRow(1,
      FrequencyTableEntryType.span, ""), counter++);
    tblFrequencyDefinition.setColumnExpandRatio("nr", 0.15f);
    tblFrequencyDefinition.setColumnExpandRatio("type", 0.3f);
    tblFrequencyDefinition.setColumnExpandRatio("annotation", 0.65f);
    
    addComponent(tblFrequencyDefinition);
    
    HorizontalLayout layoutButtons = new HorizontalLayout();
    
    btAdd = new Button("Add");
    btAdd.addListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        tblFrequencyDefinition.addItem(createNewTableRow(counter+1,
          FrequencyTableEntryType.span, ""), counter++);
      }
    });
    layoutButtons.addComponent(btAdd);
    
    btDeleteRow = new Button("Delete selected row(s)");
    btDeleteRow.setEnabled(false);
    btDeleteRow.addListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        Set<Object> selected = new HashSet((Set<Object>) tblFrequencyDefinition.getValue());
        for(Object o : selected)
        {
          tblFrequencyDefinition.removeItem(o);
        }
      }
    });
    layoutButtons.addComponent(btDeleteRow);
    
    addComponent(layoutButtons);
  }
  
  private Object[] createNewTableRow(int nodeNr, FrequencyTableEntryType type, String annotation)
  {
    TextField txtNode = new TextField();
    txtNode.setValue("" + nodeNr);
    txtNode.addValidator(new IntegerValidator("Node reference must be a valid number"));
    txtNode.setWidth("100%");
    
    final ComboBox cbType = new ComboBox();
    cbType.addItem("span");
    cbType.addItem("annotation");
    cbType.setValue(type.name());
    cbType.setNullSelectionAllowed(false);
    cbType.setWidth("100%");
    cbType.setImmediate(true);
    
    final TextField txtAnno = new TextField();
    txtAnno.setValue(annotation);
    txtAnno.setEnabled(false);
    txtAnno.setInputPrompt("disabled");
    txtAnno.setWidth("100%");
    
    cbType.addListener(new Property.ValueChangeListener() 
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      {
        if("span".equals(cbType.getValue()))
        {
          txtAnno.setEnabled(false);
          txtAnno.setInputPrompt("disabled");
        }
        else
        {
          txtAnno.setEnabled(true);
          txtAnno.setInputPrompt("");
        }
      }
    });
    
    return new Object[] {txtNode, cbType, txtAnno};
  }
}
