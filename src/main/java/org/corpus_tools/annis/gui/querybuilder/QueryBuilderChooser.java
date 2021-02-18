/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.querybuilder;

import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.ui.ComboBox;
import java.util.HashMap;
import java.util.Map;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.IDGenerator;
import org.corpus_tools.annis.gui.QueryController;

/**
 * Wrapper for selecting and showing the desired query builder.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class QueryBuilderChooser extends Panel implements Property.ValueChangeListener {
  /**
   * 
   */
  private static final long serialVersionUID = 3757379562038691611L;
  private final QueryController controller;
  private final ComboBox cbChooseBuilder;
  private final Map<String, String> short2caption;
  private final Map<String, QueryBuilderPlugin> pluginRegistry;
  private Component lastComponent;
  private final VerticalLayout layout;
  private Component component;

  public QueryBuilderChooser(AnnisUI ui) {
    this.controller = ui.getQueryController();

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

    for (QueryBuilderPlugin<Component> b : ui.getQueryBuilderPlugins()) {
      short2caption.put(b.getShortName(), b.getCaption());
      pluginRegistry.put(b.getCaption(), b);
      cbChooseBuilder.addItem(b.getCaption());
    }

    cbChooseBuilder.addValueChangeListener(this);

    layout.addComponent(cbChooseBuilder);
    layout.setExpandRatio(cbChooseBuilder, 0.0f);

    if (ui.getInstanceConfig().getDefaultQueryBuilder() != null) {
      cbChooseBuilder.setValue(short2caption.get(ui.getInstanceConfig().getDefaultQueryBuilder()));
    }
  }

  @Override
  public void attach() {
    super.attach();
    IDGenerator.assignIDForFields(this, cbChooseBuilder);
  }

  public Component getQueryBuilder() {
    return component;
  }

  @Override
  public void valueChange(ValueChangeEvent event) {
    QueryBuilderPlugin<Component> plugin = pluginRegistry.get(event.getProperty().getValue());
    if (plugin == null) {
      Notification.show("Invalid selection (plugin not found)", Notification.Type.WARNING_MESSAGE);
    } else {
      component = plugin.createComponent(controller);
      if (lastComponent != null) {
        layout.removeComponent(lastComponent);
      }
      layout.addComponent(component);
      layout.setExpandRatio(component, 1.0f);
      lastComponent = component;
    }
  }

}
