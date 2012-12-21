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

import annis.gui.PluginSystem;
import annis.gui.controlpanel.ControlPanel;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.xeoh.plugins.base.util.PluginManagerUtil;

/**
 * Wrapper for selecting and showing the desired query builder.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class QueryBuilderChooser extends Panel implements Property.ValueChangeListener
{
  private PluginSystem pluginSystem;
  private ControlPanel controlPanel;
  private ComboBox cbChooseBuilder;
  private Map<String, String> caption2Short;
  private Map<String, QueryBuilderPlugin> pluginRegistry;
  private Component lastComponent;
  
  public QueryBuilderChooser(ControlPanel controlPanel, PluginSystem pluginSystem)
  {
    this.controlPanel = controlPanel;
    this.pluginSystem = pluginSystem;
    
    this.pluginRegistry = new HashMap<String, QueryBuilderPlugin>();
    this.caption2Short = new HashMap<String, String>();
    
    // add combobox to choose the query builder
    cbChooseBuilder = new ComboBox();
    cbChooseBuilder.setNewItemsAllowed(false);
    cbChooseBuilder.setNullSelectionAllowed(false);
    cbChooseBuilder.setImmediate(true);
    
    PluginManagerUtil util = new PluginManagerUtil(pluginSystem.getPluginManager());
    Collection<QueryBuilderPlugin> builders = util.getPlugins(QueryBuilderPlugin.class);
    
    for(QueryBuilderPlugin b : builders)
    {
      caption2Short.put(b.getCaption(), b.getShortName());
      pluginRegistry.put(b.getShortName(), b);
      cbChooseBuilder.addItem(b.getCaption());
    }
    
    cbChooseBuilder.addListener((Property.ValueChangeListener) this);
    
    addComponent(cbChooseBuilder);
  }

  @Override
  public void valueChange(ValueChangeEvent event)
  {
    String shortName = caption2Short.get((String) event.getProperty().getValue());
    if(shortName == null)
    {
      getWindow().showNotification("Invalid selection (caption not found)", 
        Window.Notification.TYPE_WARNING_MESSAGE);
      return;
    }
    
    QueryBuilderPlugin plugin = pluginRegistry.get(shortName);
    if(plugin == null)
    {
      getWindow().showNotification("Invalid selection (plugin not found)", 
        Window.Notification.TYPE_WARNING_MESSAGE);
    }
    
    Component component = plugin.createComponent(controlPanel);
    if(lastComponent != null)
    {
      removeComponent(lastComponent);
    }
    addComponent(component);
    lastComponent = component;
  }
  
  
}
