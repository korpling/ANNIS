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

import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 *
 * @author thomas
 */
public class AboutPanel extends Panel
{

  private static ClassResource logo_res;
  public AboutPanel(Application app)
  {
    if(logo_res == null)
    {
      logo_res = new ClassResource(AboutPanel.class,
      "annis-logo.jpg", app);
    }
    setWidth("400px");
    setHeight("-1px");
  }

  @Override
  public void attach()
  {
    super.attach();

    Embedded logo = new Embedded();
    logo.setSource(logo_res);
    logo.setType(Embedded.TYPE_IMAGE);
        
    addComponent(logo);
    addComponent(new Label("Annis is a project of the "
      + "<a href=\"http://www.sfb632.uni-potsdam.de/\">SFB632</a>.", Label.CONTENT_XHTML));
    addComponent(new Label("Homepage: "
      + "<a href=\"http://www.sfb632.uni-potsdam.de/d1/annis/\">"
      + "http://www.sfb632.uni-potsdam.de/d1/annis/</a>.", Label.CONTENT_XHTML));
    addComponent(new Label("Version: " + getApplication().getVersion()));
    
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
    
    ((VerticalLayout) getContent()).setComponentAlignment(logo, Alignment.MIDDLE_CENTER);
    ((VerticalLayout) getContent()).setComponentAlignment(btOK, Alignment.MIDDLE_CENTER);
    
  }
}
