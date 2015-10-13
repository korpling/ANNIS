/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class EmbedVisualizationGenerator extends Panel
{
  private final HorizontalLayout layout;
  private final ListSelect visSelector;
  private final Accordion generatorSelector;
  
  public EmbedVisualizationGenerator()
  {
    visSelector = new ListSelect("Select visualization");
    visSelector.setHeight("100%");
    generatorSelector = new Accordion();
    generatorSelector.addTab(new Label("Test"), "Link");
    generatorSelector.addTab(new Label("Test"), "Webpage");
    generatorSelector.addTab(new Label("Test"), "Preview");
    
    layout = new HorizontalLayout(visSelector);
    layout.setSizeFull();
    setContent(layout);
    setSizeFull();
  }
}
