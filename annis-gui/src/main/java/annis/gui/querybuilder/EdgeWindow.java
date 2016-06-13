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

import annis.gui.querybuilder.NodeWindow.SimpleNewItemHandler;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 *
 * @author thomas
 */
public class EdgeWindow extends Panel implements Button.ClickListener
{
  
  private TigerQueryBuilderCanvas parent;
  
  private ComboBox cbOperator;
  private Button btClose;
  private NodeWindow source;
  private NodeWindow target;
  private TextField txtOperator; 
  
  private final static String CUSTOM = "custom";
  
  public EdgeWindow(final TigerQueryBuilderCanvas parent, NodeWindow source, NodeWindow target)
  {
    this.parent = parent;
    this.source = source;
    this.target = target;
    
    setSizeFull();
    
    // HACK: use our own border since the one from chameleon does not really work
    addStyleName(ValoTheme.PANEL_BORDERLESS);
    addStyleName("border-layout");
    addStyleName("white-panel");
    
    VerticalLayout vLayout = new VerticalLayout();
    setContent(vLayout);
    vLayout.setMargin(false);
    
    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    toolbar.setWidth("100%");
    toolbar.setHeight("-1px");
    vLayout.addComponent(toolbar);
    
    Label lblTitle = new Label("AQL Operator");
    lblTitle.setWidth("100%");
    
    toolbar.addComponent(lblTitle);
    toolbar.setComponentAlignment(lblTitle, Alignment.MIDDLE_LEFT);
    toolbar.setExpandRatio(lblTitle, 1.0f);
    
    btClose = new Button();
    btClose.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
    btClose.addStyleName(ValoTheme.BUTTON_SMALL);
    btClose.setIcon(FontAwesome.TIMES_CIRCLE);
    btClose.setWidth("-1px");
    btClose.addClickListener((Button.ClickListener) this);
    toolbar.addComponent(btClose);
    
    toolbar.setComponentAlignment(btClose, Alignment.MIDDLE_RIGHT);
    toolbar.setExpandRatio(btClose, 0.0f);
    
    cbOperator = new ComboBox();
    cbOperator.setNewItemsAllowed(false);
    cbOperator.setTextInputAllowed(false);
    cbOperator.setNullSelectionAllowed(true);
    cbOperator.addItem(CUSTOM);
    cbOperator.setItemCaption(CUSTOM, "custom");
    cbOperator.setNullSelectionItemId(CUSTOM);
    cbOperator.setNewItemHandler(new SimpleNewItemHandler(cbOperator));
    cbOperator.setImmediate(true);
    vLayout.addComponent(cbOperator);
    for(AQLOperator o : AQLOperator.values())
    {
      cbOperator.addItem(o);
      cbOperator.setItemCaption(o, o.getDescription() +  " (" + o.getOp() + ")");
    }
    cbOperator.setValue(AQLOperator.DIRECT_PRECEDENCE);
    cbOperator.addValueChangeListener(new ValueChangeListener()
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      {
        
        Object val = event.getProperty().getValue();
        if(val instanceof AQLOperator)
        {
          txtOperator.setValue(((AQLOperator) val).getOp());
        }
      }
    });
    
    cbOperator.setWidth("100%");
    cbOperator.setHeight("20px");
    
    txtOperator = new TextField();
    txtOperator.setValue(".");
    txtOperator.setInputPrompt("select operator definition");
    txtOperator.setSizeFull();
    txtOperator.addValueChangeListener(new OperatorValueChangeListener(parent));
    txtOperator.setImmediate(true);
    
    vLayout.addComponent(txtOperator);
    
    vLayout.setExpandRatio(cbOperator, 1.0f);
    
  }
  
  @Override
  public void buttonClick(ClickEvent event)
  {
    if(event.getButton() == btClose)
    {      
      parent.deleteEdge(this);
    }
  }

  public NodeWindow getSource()
  {
    return source;
  }

  public NodeWindow getTarget()
  {
    return target;
  }
  
  private AQLOperator findAQLOperatorForText(String txt)
  {
    for(AQLOperator op : AQLOperator.values())
    {
      if(op.getOp().equals(txt))
      {
        return op;
      }
    }
    return null;
  }
  
  public String getOperator()
  {
    String val = txtOperator.getValue();
    if(val == null || val.isEmpty())
    {
      return "SET_OPERATOR";
    }
    else
    {
      return val;
    }
  }

  private class OperatorValueChangeListener implements ValueChangeListener
  {

    private final TigerQueryBuilderCanvas parent;

    public OperatorValueChangeListener(TigerQueryBuilderCanvas parent)
    {
      this.parent = parent;
    }

    @Override
    public void valueChange(ValueChangeEvent event)
    {
      
      Object oldVal = cbOperator.getValue();
      if(CUSTOM.equals(oldVal))
      {
        oldVal = null;
      }
      AQLOperator newVal = findAQLOperatorForText((String) event.getProperty().getValue());
      if(oldVal != newVal)
      {
        cbOperator.setValue(newVal);
      }
      if(parent != null)
      {
        parent.updateQuery();
      }
    }
  }
  
}
