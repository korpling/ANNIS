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
package annis.gui.querybuilder;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author thomas
 */
public class NodeWindow extends Panel implements Button.ClickListener
{
  
  public static final int HEIGHT=100;
  public static final int WIDTH=200;
  
  public static final String[] NODE_OPERATORS = new String[] 
  {
    "=", "~", "!=", "!~"
  };
  
  private Set<String> annoNames;
  
  private TigerQueryBuilder parent;
  private Button btEdge;
  private Button btAdd;
  private Button btClear;
  private Button btClose;
  private HorizontalLayout toolbar;
  private List<ConstraintLayout> constraints;
  private boolean prepareEdgeDock;
  private int id;

  public NodeWindow(int id, TigerQueryBuilder parent)
  {
    this.parent = parent;
    this.id = id;
    this.annoNames = new TreeSet<String>();
    
    for(String a :parent.getAvailableAnnotationNames())
    {
      annoNames.add(a.replaceFirst("^[^:]*:", ""));
    }
    constraints = new ArrayList<ConstraintLayout>();

    setWidth("99%");
    setHeight("99%");

    prepareEdgeDock = false;

    VerticalLayout vLayout = (VerticalLayout) getContent();
    vLayout.setMargin(false);

    toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    toolbar.setWidth("100%");
    toolbar.setHeight("-1px");
    addComponent(toolbar);

    btEdge = new Button("Edge");
    btEdge.setStyleName(ChameleonTheme.BUTTON_LINK);
    btEdge.addListener((Button.ClickListener) this);
    toolbar.addComponent(btEdge);
    btAdd = new Button("Add");
    btAdd.setStyleName(ChameleonTheme.BUTTON_LINK);
    btAdd.addListener((Button.ClickListener) this);
    toolbar.addComponent(btAdd);
    btClear = new Button("Clear");
    btClear.setStyleName(ChameleonTheme.BUTTON_LINK);
    btClear.addListener((Button.ClickListener) this);
    toolbar.addComponent(btClear);

    btClose = new Button("X");
    btClose.setStyleName(ChameleonTheme.BUTTON_LINK);
    btClose.addListener((Button.ClickListener) this);
    toolbar.addComponent(btClose);

    toolbar.setComponentAlignment(btClose, Alignment.MIDDLE_RIGHT);
  }

  public void setPrepareEdgeDock(boolean prepare)
  {
    this.prepareEdgeDock = prepare;

    btClear.setVisible(!prepare);
    btClose.setVisible(!prepare);
    btAdd.setVisible(!prepare);

    if(prepare)
    {
      btEdge.setCaption("Dock");
    }
    else
    {
      btEdge.setCaption("Edge");
    }
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    if(event.getButton() == btEdge)
    {
      if(prepareEdgeDock)
      {
        setPrepareEdgeDock(false);
        parent.addEdge(this);
      }
      else
      {
        parent.prepareAddingEdge(this);
        setPrepareEdgeDock(true);
        btEdge.setCaption("Cancel");
      }
    }
    else if(event.getButton() == btClose)
    {
      parent.deleteNode(this);
    }
    else if(event.getButton() == btAdd)
    {
      ConstraintLayout c = new ConstraintLayout(parent, annoNames);
      c.setWidth("100%");
      c.setHeight("-1px");
      constraints.add(c);
      addComponent(c);
      if(parent != null)
      {
        parent.updateQuery();
      }
    }
    else if(event.getButton() == btClear)
    {
      for(ConstraintLayout c : constraints)
      {
        removeComponent(c);
      }
      constraints.clear();
      if(parent != null)
      {
        parent.updateQuery();
      }
    }
  }

  public int getID()
  {
    return id;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(obj == null)
    {
      return false;
    }
    if(getClass() != obj.getClass())
    {
      return false;
    }
    final NodeWindow other = (NodeWindow) obj;
    return other.getID() == getID();
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 41 * hash + this.id;
    return hash;
  }

  public List<ConstraintLayout> getConstraints()
  {
    return constraints;
  }
  
  

  public static class ConstraintLayout extends HorizontalLayout 
  implements LayoutClickListener, ValueChangeListener
  {

    private TigerQueryBuilder parent;
    private ComboBox cbName;
    private ComboBox cbOperator;
    private TextField txtValue;

    public ConstraintLayout(TigerQueryBuilder parent, Set<String> annoNames)
    {
      this.parent = parent;
      
      setWidth("100%");
      
      cbName = new ComboBox();
      cbName.setNewItemsAllowed(true);
      cbName.setNewItemHandler(new SimpleNewItemHandler(cbName));
      cbName.setImmediate(true);
      cbName.setNullSelectionAllowed(true);
      cbName.setNullSelectionItemId("tok");
      cbName.addItem("tok");
      for(String n : annoNames)
      {
        cbName.addItem(n);
      }
      cbName.setValue("tok");
      cbName.addListener((ValueChangeListener) this);
      
      
      cbOperator = new ComboBox();
      cbOperator.setNewItemsAllowed(false);
      cbOperator.setImmediate(true);
      for(String o : NODE_OPERATORS)
      {
        cbOperator.addItem(o);
      }
      cbOperator.setValue(NODE_OPERATORS[0]);
      cbOperator.addListener((ValueChangeListener) this);
      
      txtValue = new TextField();
      txtValue.setImmediate(true);
      txtValue.addListener((ValueChangeListener) this);
      
      cbOperator.setWidth("3em");
      cbName.setWidth("100%");
      txtValue.setWidth("100%");

      addComponent(cbName);
      addComponent(cbOperator);
      addComponent(txtValue);

      setExpandRatio(cbName, 0.8f);
      setExpandRatio(txtValue, 1.0f);
      
      addListener((LayoutClickListener) this);
      
    }

    @Override
    public void layoutClick(LayoutClickEvent event)
    {
      Component c = event.getClickedComponent();
      if(c != null && c instanceof AbstractField)
      {
        AbstractField f = (AbstractField) c;
        f.focus();
        if(event.isDoubleClick())
        {
          if(f instanceof AbstractTextField)
          {
            ((AbstractTextField) f).selectAll();
          }
        }
      }
    }
    
    public String getOperator()
    {
      if(cbOperator.getValue() == null)
      {
        return "";
      }
      else
      {
        return (String) cbOperator.getValue();
      }
    }
    
    public String getName()
    {
      if(cbName.getValue() == null)
      {
        return "tok";
      }
      else
      {
        return (String) cbName.getValue();
      }
    }
    
    public String getValue()
    {
      if(txtValue.getValue() == null)
      {
        return "";
      }
      else
      {
        return (String) txtValue.getValue();
      }
    }

    @Override
    public void valueChange(ValueChangeEvent event)
    {
      if(parent != null)
      {
        parent.updateQuery();
      }
    }
    
  }
  public static class SimpleNewItemHandler  implements NewItemHandler  
  {

    private ComboBox comboBox;

    public SimpleNewItemHandler(ComboBox comboBox)
    {
      this.comboBox = comboBox;
    }
    
    
    
    @Override
    public void addNewItem(String newItemCaption)
    {
      if(comboBox != null)
      {
        comboBox.addItem(newItemCaption);
        comboBox.setValue(newItemCaption);
        
      }
    }
  }
  
  

}
