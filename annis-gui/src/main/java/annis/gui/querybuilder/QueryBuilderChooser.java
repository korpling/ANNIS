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
package annis.gui.querybuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.ui.ComboBox;

import annis.gui.QueryController;
import annis.libgui.InstanceConfig;
import annis.libgui.PluginSystem;
import net.xeoh.plugins.base.util.PluginManagerUtil;

/**
 * Wrapper for selecting and showing the desired query builder.
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class QueryBuilderChooser extends Panel implements Property.ValueChangeListener
{
  private final QueryController controller;
  private final ComboBox cbChooseBuilder;
  private final Map<String, String> short2caption;
  private final Map<String, QueryBuilderPlugin> pluginRegistry;
  private Component lastComponent;
  private final VerticalLayout layout;
  private Component component;
  
  public QueryBuilderChooser(QueryController controller,
    PluginSystem pluginSystem,
    InstanceConfig instanceConfig)
  {
    this.controller = controller;
    
    this.pluginRegistry = new HashMap<>();
    this.short2caption = new HashMap<>();
 
    setStyleName(ValoTheme.PANEL_BORDERLESS);
    
    layout = new VerticalLayout();
    setContent(layout);
    layout.setSizeFull();
    layout.setSpacing(true);
    setSizeFull();
    
    // add combobox to choose the query builder
    cbChooseBuilder = new ComboBox();
    cbChooseBuilder.setNewItemsAllowed(false);
    cbChooseBuilder.setNullSelectionAllowed(false);
    cbChooseBuilder.setInputPrompt("Choose a query builder");
    cbChooseBuilder.setWidth("200px");
    
    PluginManagerUtil util = new PluginManagerUtil(pluginSystem.getPluginManager());
    Collection<QueryBuilderPlugin> builders = util.getPlugins(QueryBuilderPlugin.class);
    
    for(QueryBuilderPlugin b : builders)
    {
      short2caption.put(b.getShortName(), b.getCaption());
      pluginRegistry.put(b.getCaption(), b);
      cbChooseBuilder.addItem(b.getCaption());
    }
    
    cbChooseBuilder.addValueChangeListener((Property.ValueChangeListener) this);
    
    layout.addComponent(cbChooseBuilder);
    layout.setExpandRatio(cbChooseBuilder, 0.0f);
    
    if(instanceConfig.getDefaultQueryBuilder() != null)
    {
      cbChooseBuilder.setValue(short2caption.get(instanceConfig.getDefaultQueryBuilder()));
    }
  }
  
  

  @Override
  public void valueChange(ValueChangeEvent event)
  { 
    QueryBuilderPlugin plugin = pluginRegistry.get((String) event.getProperty().getValue());
    if(plugin == null)
    {
      Notification.show("Invalid selection (plugin not found)", 
        Notification.Type.WARNING_MESSAGE);
    }
    else
    {
      component = plugin.createComponent(controller);
      if(lastComponent != null)
      {
        layout.removeComponent(lastComponent);
      }
      layout.addComponent(component);
      layout.setExpandRatio(component, 1.0f);
      lastComponent = component;
    }
  }
  
  public Component getQueryBuilder(){
    return component;
  }
  
}
