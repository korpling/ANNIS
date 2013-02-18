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
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
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
  public static final int WIDTH=250;
  
  private static final String[] NODE_OPERATORS = new String[] 
  {
    "=", "~", "!=", "!~"
  };
  
  private Set<String> annoNames;
  
  private TigerQueryBuilderCanvas parent;
  private Button btEdge;
  private Button btAdd;
  private Button btClear;
  private Button btClose;
  private Button btMove;
  private HorizontalLayout toolbar;
  private List<ConstraintLayout> constraints;
  private boolean prepareEdgeDock;
  private int id;
  private VerticalLayout vLayout;

  public NodeWindow(int id, TigerQueryBuilderCanvas parent)
  {
    this.parent = parent;
    this.id = id;
    this.annoNames = new TreeSet<String>();
    
    for(String a :parent.getAvailableAnnotationNames())
    {
      annoNames.add(a.replaceFirst("^[^:]*:", ""));
    }
    constraints = new ArrayList<ConstraintLayout>();
    
    setSizeFull();
    
    // HACK: use our own border since the one from chameleon does not really work
    addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    addStyleName("border-layout");
    addStyleName("solid-white-background");
    
    prepareEdgeDock = false;

    vLayout = new VerticalLayout();
    setContent(vLayout);
    vLayout.setWidth("100%");
    vLayout.setHeight("-1px");
    vLayout.setMargin(false);
    vLayout.setSpacing(false);

    toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    toolbar.setWidth("100%");
    toolbar.setHeight("-1px");
    toolbar.setMargin(false);
    toolbar.setSpacing(false);
    vLayout.addComponent(toolbar);

    btMove = new Button("");
    btMove.setIcon(new ThemeResource("tango-icons/22x22/view-fullscreen.png"));
    btMove.setDescription("<strong>Move node</strong><br />Click, hold and move mouse to move the node.");
    btMove.addStyleName(ChameleonTheme.BUTTON_LINK);
    btMove.addStyleName("drag-source-enabled");
    toolbar.addComponent(btMove);
   
    
    btEdge = new Button("");
    btEdge.setIcon(new ThemeResource("tango-icons/22x22/go-jump.png"));
    btEdge.addClickListener((Button.ClickListener) this);
    btEdge.addStyleName(ChameleonTheme.BUTTON_LINK);
    btEdge.setDescription("<strong>Add Edge</strong><br />"
      + "To create a new edge between "
      + "two nodes click this button first. "
      + "Then define a destination node by clicking its \"Dock\" "
      + "button.<br>You can cancel the action by clicking this button "
      + "(\"Cancel\") again.");
    btEdge.setImmediate(true);
    toolbar.addComponent(btEdge);
    
    btAdd = new Button("");
    btAdd.setIcon(new ThemeResource("tango-icons/22x22/list-add.png"));
    btAdd.addStyleName(ChameleonTheme.BUTTON_LINK);
    btAdd.addClickListener((Button.ClickListener) this);
    btAdd.setDescription("<strong>Add Node Condition</strong><br />"
      + "Every condition will constraint the node described by this window. "
      + "Most conditions limit the node by defining which annotations and which "
      + "values of the annotation a node needs to have.");
    toolbar.addComponent(btAdd);
    
    btClear = new Button("");
    btClear.setIcon(new ThemeResource("tango-icons/22x22/edit-clear.png"));
    btClear.addStyleName(ChameleonTheme.BUTTON_LINK);
    btClear.addClickListener((Button.ClickListener) this);
    btClear.setDescription("<strong>Clear All Node Conditions</strong>");
    toolbar.addComponent(btClear);

    btClose = new Button("");
    btClose.setIcon(new ThemeResource("tango-icons/22x22/process-stop.png"));
    btClose.setDescription("<strong>Close</strong><br />Close this node description window");
    btClose.addStyleName(ChameleonTheme.BUTTON_LINK);
    btClose.addClickListener((Button.ClickListener) this);
    toolbar.addComponent(btClose);

    toolbar.setComponentAlignment(btMove, Alignment.MIDDLE_LEFT);
    toolbar.setComponentAlignment(btEdge, Alignment.MIDDLE_CENTER);
    toolbar.setComponentAlignment(btClose, Alignment.MIDDLE_RIGHT);

  }

  public void setPrepareEdgeDock(boolean prepare)
  {
    this.prepareEdgeDock = prepare;

    btClear.setVisible(!prepare);
    btClose.setVisible(!prepare);
    btAdd.setVisible(!prepare);
    btMove.setVisible(!prepare);

    if(prepare)
    {
      btEdge.setCaption("Dock");
      btEdge.setIcon(new ThemeResource("pixel.png"));
    }
    else
    {
      btEdge.setIcon(new ThemeResource("tango-icons/22x22/go-jump.png"));
      btEdge.setCaption("");
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
        btEdge.setIcon(new ThemeResource("pixel.png"));
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
      vLayout.addComponent(c);
      if(parent != null)
      {
        parent.updateQuery();
      }
    }
    else if(event.getButton() == btClear)
    {
      for(ConstraintLayout c : constraints)
      {
        vLayout.removeComponent(c);
      }
      constraints.clear();
      if(parent != null)
      {
        parent.updateQuery();
      }
    }
  }

  public Button getBtMove()
  {
    return btMove;
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

    private TigerQueryBuilderCanvas parent;
    private ComboBox cbName;
    private ComboBox cbOperator;
    private TextField txtValue;

    public ConstraintLayout(TigerQueryBuilderCanvas parent, Set<String> annoNames)
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
