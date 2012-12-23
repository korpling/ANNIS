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
package annis.gui;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ChameleonTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class HelpUsPanel extends Panel
{
  
  private static final Logger log = LoggerFactory.getLogger(HelpUsPanel.class);
    
  private VerticalLayout layout;
  
  public HelpUsPanel()
  {
    setSizeFull();
    
    layout = (VerticalLayout) getContent();
    
    layout.setSizeFull();
    layout.setMargin(false, false, true, false);
  }

  @Override
  public void attach()
  {
    super.attach();
    
    
    HorizontalLayout hLayout = new HorizontalLayout();
    hLayout.setSizeFull();
    hLayout.setMargin(false);
    
    Label lblOpenSource = new Label();
    
    lblOpenSource.setValue(
      "<p>ANNIS is Open Source software. "
      + "This means you are free to download the source code and add new features or make other adjustments to ANNIS on your own.<p/>"
      + "Here are some examples how you can help ANNIS:"
      + "<ul>"
      + "<li>Fix or report problems (bugs) you encounter when using the ANNIS software.</li>"
      + "<li>Add new features.</li>"
      + "<li>Enhance the documentation</li>"
      + "</ul>"
      + "<p>Feel free to visit our GitHub page for more information: <a href=\"https://github.com/korpling/ANNIS\" target=\"_blank\">https://github.com/korpling/ANNIS</a></p>"
    );
    lblOpenSource.setContentMode(Label.CONTENT_XHTML);
    lblOpenSource.setStyleName("opensource");
    lblOpenSource.setWidth("100%");
    lblOpenSource.setHeight("-1px");
    
    Link lnkFork = new Link();
    lnkFork.setResource(new ExternalResource("https://github.com/korpling/ANNIS"));
    lnkFork.setIcon(new ExternalResource("https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png"));
    lnkFork.setTargetName("_blank");
    
    Panel panelLabel = new Panel();
    panelLabel.setSizeFull();
    panelLabel.addComponent(lblOpenSource);
    panelLabel.setStyleName(ChameleonTheme.PANEL_BORDERLESS);
    
    hLayout.addComponent(panelLabel);
    hLayout.addComponent(lnkFork);
    hLayout.setComponentAlignment(panelLabel, Alignment.TOP_LEFT);
    hLayout.setComponentAlignment(lnkFork, Alignment.TOP_RIGHT);
    hLayout.setExpandRatio(panelLabel, 1.0f);
    
    addComponent(hLayout);
    
    Button btOK = new Button("OK");
    btOK.addListener(new Button.ClickListener() {

      @Override
      public void buttonClick(ClickEvent event)
      {
        Window subwindow = getWindow();
        subwindow.getParent().removeWindow(subwindow);
      }
    });
    addComponent(btOK);
    
    layout.setComponentAlignment(hLayout, Alignment.MIDDLE_CENTER);
    layout.setComponentAlignment(btOK, Alignment.MIDDLE_CENTER);
    layout.setExpandRatio(hLayout, 1.0f);
    
  }
}
