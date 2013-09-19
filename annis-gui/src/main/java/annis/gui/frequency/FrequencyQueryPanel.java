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
package annis.gui.frequency;

import annis.gui.QueryController;
import annis.gui.model.PagedResultQuery;
import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class FrequencyQueryPanel extends VerticalLayout
{
  private Table tblFrequencyDefinition;
  private Button btAdd;
  private Button btDeleteRow;
  private Button btShowFrequencies;
  private int counter;
  private FrequencyResultPanel resultPanel;
  private Button btShowQuery;
  private VerticalLayout queryLayout;
  private QueryController controller;
  
  public FrequencyQueryPanel(final QueryController controller)
  {
    this.controller = controller;
    
    setWidth("99%");
    setHeight("99%");
    
    queryLayout = new VerticalLayout();
    queryLayout.setWidth("100%");
    queryLayout.setHeight("-1px");
    
    tblFrequencyDefinition = new Table();
    tblFrequencyDefinition.setImmediate(true);
    tblFrequencyDefinition.setSortEnabled(false);
    tblFrequencyDefinition.setSelectable(true);
    tblFrequencyDefinition.setMultiSelect(true);
    tblFrequencyDefinition.setEditable(true);
    tblFrequencyDefinition.addValueChangeListener(new Property.ValueChangeListener() 
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
    tblFrequencyDefinition.setHeight("-1px");
    
    
    tblFrequencyDefinition.addContainerProperty("nr", TextField.class, null);
    tblFrequencyDefinition.addContainerProperty("type", ComboBox.class, null);
    tblFrequencyDefinition.addContainerProperty("annotation", TextField.class, null);
    
    tblFrequencyDefinition.setColumnHeader("nr", "Node");
    tblFrequencyDefinition.setColumnHeader("type", "Type");
    tblFrequencyDefinition.setColumnHeader("annotation", "Annotation");
    
    tblFrequencyDefinition.setRowHeaderMode(Table.RowHeaderMode.INDEX);
    
    counter = 0;
    tblFrequencyDefinition.addItem(createNewTableRow(1,
      FrequencyTableEntryType.span, ""), counter++);
    tblFrequencyDefinition.setColumnExpandRatio("nr", 0.15f);
    tblFrequencyDefinition.setColumnExpandRatio("type", 0.3f);
    tblFrequencyDefinition.setColumnExpandRatio("annotation", 0.65f);
    
    queryLayout.addComponent(tblFrequencyDefinition);
    
    HorizontalLayout layoutButtons = new HorizontalLayout();
    
    btAdd = new Button("Add");
    btAdd.addClickListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        int nr = 1;
        // get the highest number of values from the existing defitions
        for(Object id : tblFrequencyDefinition.getItemIds())
        {
          AbstractField textNr = (AbstractField) tblFrequencyDefinition.getItem(id)
            .getItemProperty("nr").getValue();
          nr = Math.max(nr, Integer.parseInt((String) textNr.getValue()));
          
        }
        tblFrequencyDefinition.addItem(createNewTableRow(nr+1,
          FrequencyTableEntryType.span, ""), counter++);
      }
    });
    layoutButtons.addComponent(btAdd);
    
    btDeleteRow = new Button("Delete selected row(s)");
    btDeleteRow.setEnabled(false);
    btDeleteRow.addClickListener(new Button.ClickListener() 
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
    layoutButtons.setMargin(true);
    layoutButtons.setHeight("-1px");
    layoutButtons.setWidth("100%");
    
    queryLayout.addComponent(layoutButtons);
    
    btShowFrequencies = new Button("Perform frequency analysis");
    btShowFrequencies.setDisableOnClick(true);
    btShowFrequencies.addClickListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        ArrayList<FrequencyTableEntry> freqDefinition = new ArrayList<FrequencyTableEntry>();
        for(Object oid : tblFrequencyDefinition.getItemIds())
        {
          FrequencyTableEntry entry = new FrequencyTableEntry();
          
          Item item = tblFrequencyDefinition.getItem(oid);
          AbstractField textNr = (AbstractField) item.getItemProperty("nr").getValue();
          AbstractField textKey = (AbstractField) item.getItemProperty("annotation").getValue();
          AbstractSelect cbType = (AbstractSelect) item.getItemProperty("type").getValue();
          
          entry.setKey((String) textKey.getValue());
          entry.setReferencedNode(Integer.parseInt((String) textNr.getValue()));
          entry.setType(FrequencyTableEntryType.valueOf((String) cbType.getValue()));
          freqDefinition.add(entry);
        }
        
        if(controller != null)
        {
          controller.setQueryFromUI();
          executeFrequencyQuery(freqDefinition);
        }
          
      }
    });
    queryLayout.addComponent(btShowFrequencies);
    
    queryLayout.setComponentAlignment(tblFrequencyDefinition, Alignment.TOP_CENTER);
    queryLayout.setComponentAlignment(layoutButtons, Alignment.TOP_CENTER);
    queryLayout.setComponentAlignment(btShowFrequencies, Alignment.TOP_CENTER);
    
    queryLayout.setExpandRatio(tblFrequencyDefinition, 1.0f);
    queryLayout.setExpandRatio(layoutButtons, 0.0f);
    queryLayout.setExpandRatio(btShowFrequencies, 0.0f);
    
    addComponent(queryLayout);
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

  public void executeFrequencyQuery(List<FrequencyTableEntry> freqDefinition)
  {
    if (controller != null && controller.getPreparedQuery() != null)
    {
      PagedResultQuery preparedQuery = controller.getPreparedQuery();
      
      if (preparedQuery.getCorpora()== null || preparedQuery.getCorpora().isEmpty())
      {
        Notification.show("Please select a corpus", Notification.Type.WARNING_MESSAGE);
        btShowFrequencies.setEnabled(true);
        return;
      }
      if ("".equals(preparedQuery.getQuery()))
      {
        Notification.show("Empty query",  Notification.Type.WARNING_MESSAGE);
        btShowFrequencies.setEnabled(true);
        return;
      }
      if(resultPanel != null)
      {
        removeComponent(resultPanel);
      }
      resultPanel = new FrequencyResultPanel(preparedQuery.getQuery(), preparedQuery.getCorpora(),
        freqDefinition, this);
      addComponent(resultPanel);
      setExpandRatio(resultPanel, 1.0f);
    }
  }
  
  public void setExecuteFrequencyAnalysisButtonEnabled(boolean enabled)
  {
    btShowFrequencies.setEnabled(enabled);
  }
  
}
