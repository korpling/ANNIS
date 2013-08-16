/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.gui.tutorial.TutorialPanel;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.JavaScript;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class HelpPanel extends Accordion
{
  private TutorialPanel tutorial;
  private ExampleQueriesPanel examples;
  
  public HelpPanel(SearchUI parent)
  {
    setSizeFull();
    
    tutorial = new TutorialPanel();
    tutorial.setHeight("99%");
    
    examples = new ExampleQueriesPanel(
      "example queries", parent, this);
    examples.setHeight("99%");
    
    addTab(tutorial, "Tutorial", new ThemeResource("tango-icons/22x22/help-browser.png"));
    addTab(examples, "Example Queries", new ThemeResource("tango-icons/22x22/edit-paste.png"));
    setSelectedTab(examples);
    addStyleName("help-tab");
    
  }

  public ExampleQueriesPanel getExamples()
  {
    return examples;
  }
  
}
