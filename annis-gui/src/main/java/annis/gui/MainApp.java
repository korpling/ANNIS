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

import annis.gui.controlpanel.ControlPanel;
import com.vaadin.Application;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MainApp extends Application
{

  private Window window;

  @Override
  public void init()
  {
    setTheme("annis-theme");
    
    window = new Window("Annis² Corpus Search");
    setMainWindow(window);
    
    window.getContent().setSizeFull();
    
    MenuBar menu = new MenuBar();
    menu.addItem("Test", new MenuBar.Command() {

      @Override
      public void menuSelected(MenuItem selectedItem)
      {
        window.showNotification("Just a dummy", Window.Notification.TYPE_HUMANIZED_MESSAGE);
      }
    });
    
    window.addComponent(menu);
    menu.setWidth(100f, Layout.UNITS_PERCENTAGE);
    
    HorizontalLayout hLayout = new HorizontalLayout();
    hLayout.setSizeFull();
    window.addComponent(hLayout);
    ((VerticalLayout) window.getContent()).setExpandRatio(hLayout, 1.0f);

    ControlPanel controlPanel = new ControlPanel();
    controlPanel.setWidth(30f, Layout.UNITS_EM);
    controlPanel.setHeight(100f, Layout.UNITS_PERCENTAGE);
    hLayout.addComponent(controlPanel);
    Label testLabel = new Label("TODO");
    hLayout.addComponent(testLabel);
    hLayout.setExpandRatio(testLabel, 1.0f);
    
  }
}