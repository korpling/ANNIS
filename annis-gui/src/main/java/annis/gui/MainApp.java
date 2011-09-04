/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui;

import annis.gui.tutorial.TutorialPanel;
import annis.gui.controlpanel.ControlPanel;
import com.vaadin.Application;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.File;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MainApp extends Application
{

  private Window window;
  private ControlPanel controlPanel;
  private TutorialPanel tutorialPanel;
  
  @Override
  public void init()
  {
    setTheme("annis-theme");
    
    window = new Window("Annis² Corpus Search");
    setMainWindow(window);
    
    window.getContent().setSizeFull();
    ((VerticalLayout)window.getContent()).setMargin(false);
    
    MenuBar menu = new MenuBar();
    MenuItem helpMenuItem = menu.addItem("Help", null);
    helpMenuItem.addItem("Tutorial", new MenuBar.Command() {

      @Override
      public void menuSelected(MenuItem selectedItem)
      {
        window.showNotification("Tutorial", Window.Notification.TYPE_ERROR_MESSAGE);
      }
    });
    helpMenuItem.addItem("About", new MenuBar.Command() {

      @Override
      public void menuSelected(MenuItem selectedItem)
      {
        window.showNotification("The is a prototype to tests vaadins capabilities in regards to the need of ANNIS", Window.Notification.TYPE_HUMANIZED_MESSAGE);
      }
    });

    window.addComponent(menu);
    menu.setWidth(100f, Layout.UNITS_PERCENTAGE);
    
    HorizontalLayout hLayout = new HorizontalLayout();
    hLayout.setSizeFull();
    window.addComponent(hLayout);
    ((VerticalLayout) window.getContent()).setExpandRatio(hLayout, 1.0f);

    controlPanel = new ControlPanel();
    controlPanel.setWidth(30f, Layout.UNITS_EM);
    controlPanel.setHeight(100f, Layout.UNITS_PERCENTAGE);
    hLayout.addComponent(controlPanel);
    
    tutorialPanel = new TutorialPanel();
    
    TabSheet mainTab = new TabSheet();
    mainTab.setSizeFull();
    mainTab.addTab(tutorialPanel, "Tutorial", null);
    
    hLayout.addComponent(mainTab);
    hLayout.setExpandRatio(mainTab, 1.0f);
  }
  
  public void setQuery(String query, Set<Long> corpora)
  {
    if(controlPanel != null)
    {
      controlPanel.setQuery(query, corpora);
    }
  }
}