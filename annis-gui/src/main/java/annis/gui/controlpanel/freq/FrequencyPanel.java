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
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class FrequencyPanel extends VerticalLayout
{
  private Table tblFrequencyDefinition;
  public FrequencyPanel()
  {
    setWidth("100%");
    setHeight("100%");
    
    
    tblFrequencyDefinition = new Table();
    tblFrequencyDefinition.setSortDisabled(true);
    
    tblFrequencyDefinition.setWidth("100%");
    tblFrequencyDefinition.setHeight("100%");
    
    tblFrequencyDefinition.setEditable(true);
    
    tblFrequencyDefinition.addContainerProperty("Nr", Integer.class, 1);
    tblFrequencyDefinition.addContainerProperty("Type", ComboBox.class, 
      FrequencyTableEntryType.span);
    tblFrequencyDefinition.addContainerProperty("Annotation", String.class, "pos");
    
    tblFrequencyDefinition.setColumnHeader("Nr", "#node");
    tblFrequencyDefinition.setColumnHeader("Type", "");
    tblFrequencyDefinition.setColumnHeader("Annotation", "Annotation");
    
    tblFrequencyDefinition.addItem(createNewTableRow(1,
      FrequencyTableEntryType.span, ""), "test");
    //tblFrequencyDefinition.setColumnExpandRatio("Nr", 0.1f);
    //tblFrequencyDefinition.setColumnExpandRatio("Type", 0.1f);
    //tblFrequencyDefinition.setColumnExpandRatio("Annotation", 0.8f);
    
    addComponent(tblFrequencyDefinition);
  }
  
  private Object[] createNewTableRow(int nodeNr, FrequencyTableEntryType type, String annotation)
  {
    TextField txtNode = new TextField();
    txtNode.setValue("" + nodeNr);
    txtNode.addValidator(new IntegerValidator("Node reference must be a valid number"));
    txtNode.setColumns(2);
    
    final ComboBox cbType = new ComboBox();
    cbType.addItem("span");
    cbType.addItem("annotation");
    cbType.setValue(type.name());
    cbType.setNullSelectionAllowed(false);
    cbType.setWidth("-1px");
    
    final TextField txtAnno = new TextField();
    txtAnno.setValue(annotation);
    txtAnno.setEnabled(false);
    txtAnno.setWidth("100%");
    
    cbType.addListener(new Property.ValueChangeListener() 
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      {
        if("span".equals(cbType.getValue()))
        {
          txtAnno.setEnabled(false);
        }
        else
        {
          txtAnno.setEnabled(true);
        }
      }
    });
    
    return new Object[] {txtNode, cbType, txtAnno};
  }
}
